package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

	@Mock private ConcertDao concertDao;
	@Mock private OrganisateurDao organisateurDao;

	private ConcertService concertService;

	private ConcertCreateDTO dto;
	private Organiser organisateur;

	@BeforeEach
	void setup() {
		concertService = new ConcertService(concertDao, organisateurDao);

		organisateur = new Organiser();

		dto = new ConcertCreateDTO();
		dto.setOrganisateurId(1L);
		dto.setLieu("Zénith Paris");
		dto.setCapacite(500L);
		dto.setDescription("Grand concert");
		dto.setDateTime(LocalDateTime.now().plusDays(30));
		dto.setPopularite(4);
	}

	// ---- Tests create() ----

	@Test
	void create_organisateurInexistant_lanceBadRequest() {
		when(organisateurDao.findOne(1L)).thenReturn(null);
		assertThrows(BadRequestException.class, () -> concertService.create(dto));
	}

	@Test
	void create_capaciteNulle_lanceBadRequest() {
		dto.setCapacite(0L);
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);
		assertThrows(BadRequestException.class, () -> concertService.create(dto));
	}

	@Test
	void create_capaciteNegative_lanceBadRequest() {
		dto.setCapacite(-10L);
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);
		assertThrows(BadRequestException.class, () -> concertService.create(dto));
	}

	@Test
	void create_dateDansLePasse_lanceBadRequest() {
		dto.setDateTime(LocalDateTime.now().minusDays(1));
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);
		assertThrows(BadRequestException.class, () -> concertService.create(dto));
	}

	@Test
	void create_dateExacteAujourdhui_lanceBadRequest() {
		// LocalDateTime.now() n'est pas "after" now()
		dto.setDateTime(LocalDateTime.now().minusSeconds(1));
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);
		assertThrows(BadRequestException.class, () -> concertService.create(dto));
	}

	@Test
	void create_nominal_initialiseCapaciteEtCapaciteMax() throws Exception {
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);

		doAnswer(inv -> {
			Concert c = inv.getArgument(0);
			assertEquals(500.0, c.getCapaciteMax(), 0.001);
			assertEquals(500.0, c.getCapacite(), 0.001);
			assertEquals("Zénith Paris", c.getLieu());
			assertEquals(4.0, c.getPopularite(), 0.001);
			assertNotNull(c.getOrganiser());
			setConcertId(c, 99L);
			return null;
		}).when(concertDao).save(any(Concert.class));

		long id = concertService.create(dto);
		assertEquals(99L, id);
		verify(concertDao).save(any(Concert.class));
	}

	@Test
	void create_populariteNulle_defaultA1() throws Exception {
		dto.setPopularite(null);
		when(organisateurDao.findOne(1L)).thenReturn(organisateur);
		doAnswer(inv -> {
			Concert c = inv.getArgument(0);
			assertEquals(1.0, c.getPopularite(), 0.001);
			setConcertId(c, 1L);
			return null;
		}).when(concertDao).save(any(Concert.class));

		concertService.create(dto);
	}

	// ---- Tests delete() ----

	@Test
	void delete_concertInexistant_lanceNotFoundException() {
		when(concertDao.findOne(99L)).thenReturn(null);
		assertThrows(NotFoundException.class, () -> concertService.delete(99L));
	}

	@Test
	void delete_nominal_supprimeLeConcert() {
		Concert concert = new Concert();
		when(concertDao.findOne(1L)).thenReturn(concert);

		concertService.delete(1L);

		verify(concertDao).delete(concert);
	}

	// Helper pour simuler @GeneratedValue dans les tests
	private void setConcertId(Concert concert, Long id) throws Exception {
		Field f = Concert.class.getDeclaredField("ConcertId");
		f.setAccessible(true);
		f.set(concert, id);
	}

	// ---- Tests findOne() ----

	@Test
	void findOne_retourneLeConcert() {
		Concert concert = new Concert();
		concert.setConcertId(5L);
		when(concertDao.findOne(5L)).thenReturn(concert);

		Concert result = concertService.findOne(5L);

		assertNotNull(result);
		assertEquals(5L, result.getConcertId());
	}

	@Test
	void findOne_inexistant_retourneNull() {
		when(concertDao.findOne(999L)).thenReturn(null);
		assertNull(concertService.findOne(999L));
	}
}
