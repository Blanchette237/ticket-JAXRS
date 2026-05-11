package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.TicketCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock private ConcertDao concertDao;
	@Mock private TicketDao ticketDao;
	@Mock private ClientDao clientDao;

	private TicketService ticketService;

	private Concert concertValide;
	private Client clientValide;
	private TicketCreateDTO dto;

	@BeforeEach
	void setup() {
		ticketService = new TicketService(concertDao, ticketDao, clientDao);

		clientValide = new Client(1L, "Dupont", "Jean", "pass", "jean@mail.com");

		concertValide = new Concert();
		concertValide.setConcertId(10L);
		concertValide.setDate(LocalDateTime.now().plusDays(10));
		concertValide.setCapaciteMax(100);
		concertValide.setCapacite(100);
		concertValide.setPopularite(3.0);

		dto = new TicketCreateDTO();
		dto.setUtilisateurId(1L);
		dto.setConcertId(10L);
		dto.setNumeroPlace("A5");
	}

	// ---- Tests create() ----

	@Test
	void create_clientInexistant_lanceBadRequest() {
		when(clientDao.findOne(1L)).thenReturn(null);
		assertThrows(BadRequestException.class, () -> ticketService.create(dto));
	}

	@Test
	void create_concertInexistant_lanceNotFoundException() {
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(null);
		assertThrows(NotFoundException.class, () -> ticketService.create(dto));
	}

	@Test
	void create_concertPasse_lanceBadRequest() {
		concertValide.setDate(LocalDateTime.now().minusDays(1));
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		assertThrows(BadRequestException.class, () -> ticketService.create(dto));
	}

	@Test
	void create_concertComplet_lanceConflictException() {
		concertValide.setCapacite(0);
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		assertThrows(ConflictException.class, () -> ticketService.create(dto));
	}

	@Test
	void create_placeDejaOccupee_lanceConflictException() {
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		when(ticketDao.existsByConcertAndPlace("A5", concertValide)).thenReturn(true);
		assertThrows(ConflictException.class, () -> ticketService.create(dto));
	}

	@Test
	void create_nominal_decrementeCapacite() throws Exception {
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		when(ticketDao.existsByConcertAndPlace(anyString(), any())).thenReturn(false);
		doAnswer(inv -> { setTicketId(inv.getArgument(0), 99L); return null; })
				.when(ticketDao).save(any(Ticket.class));

		ticketService.create(dto);

		assertEquals(99.0, concertValide.getCapacite(), 0.001);
		verify(concertDao).update(concertValide);
	}

	// ---- Tests pricing ----

	@Test
	void create_zoneVIP_prixCorrect() throws Exception {
		dto.setNumeroPlace("VIP1");
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		when(ticketDao.existsByConcertAndPlace(anyString(), any())).thenReturn(false);
		doAnswer(inv -> { setTicketId(inv.getArgument(0), 1L); return null; })
				.when(ticketDao).save(any(Ticket.class));

		ticketService.create(dto);

		// base=30 + VIP=50 + place≤10=20 + popularite(3)*5=15 = 115
		verify(ticketDao).save(argThat(t -> t.getPrixUnitaire() == 115.0));
	}

	@Test
	void create_zoneA_numero15_prixCorrect() throws Exception {
		dto.setNumeroPlace("A15");
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		when(ticketDao.existsByConcertAndPlace(anyString(), any())).thenReturn(false);
		doAnswer(inv -> { setTicketId(inv.getArgument(0), 2L); return null; })
				.when(ticketDao).save(any(Ticket.class));

		ticketService.create(dto);

		// base=30 + A=20 + 11-30=10 + popularite=15 = 75
		verify(ticketDao).save(argThat(t -> t.getPrixUnitaire() == 75.0));
	}

	@Test
	void create_surgePricing_ajouteSurcoût() throws Exception {
		concertValide.setCapaciteMax(100);
		concertValide.setCapacite(10); // 10% restants → surge
		dto.setNumeroPlace("B50");     // zone B, numero 50 → pas de bonus numéro
		when(clientDao.findOne(1L)).thenReturn(clientValide);
		when(concertDao.findOne(10L)).thenReturn(concertValide);
		when(ticketDao.existsByConcertAndPlace(anyString(), any())).thenReturn(false);
		doAnswer(inv -> { setTicketId(inv.getArgument(0), 3L); return null; })
				.when(ticketDao).save(any(Ticket.class));

		ticketService.create(dto);

		// base=30 + B=10 + surge=15 + popularite=15 = 70
		verify(ticketDao).save(argThat(t -> t.getPrixUnitaire() == 70.0));
	}

	// ---- Tests annuler() ----

	@Test
	void annuler_ticketInexistant_lanceNotFoundException() {
		when(ticketDao.findOne(99L)).thenReturn(null);
		assertThrows(NotFoundException.class, () -> ticketService.annuler(99L));
	}

	@Test
	void annuler_ticketDejaAnnule_lanceBadRequest() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ANNULE);
		when(ticketDao.findOne(1L)).thenReturn(ticket);
		assertThrows(BadRequestException.class, () -> ticketService.annuler(1L));
	}

	@Test
	void annuler_ticketUtilise_lanceBadRequest() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.UTILISE);
		when(ticketDao.findOne(1L)).thenReturn(ticket);
		assertThrows(BadRequestException.class, () -> ticketService.annuler(1L));
	}

	@Test
	void annuler_nominal_restituePlace() {
		concertValide.setCapacite(50);
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setConcert(concertValide);

		when(ticketDao.findOne(1L)).thenReturn(ticket);

		ticketService.annuler(1L);

		assertEquals(TicketStatus.ANNULE, ticket.getStatus());
		assertEquals(51.0, concertValide.getCapacite(), 0.001);
		verify(ticketDao).update(ticket);
		verify(concertDao).update(concertValide);
	}

	// Helper pour simuler @GeneratedValue dans les tests
	private void setTicketId(Ticket ticket, Long id) throws Exception {
		Field f = Ticket.class.getDeclaredField("TicketId");
		f.setAccessible(true);
		f.set(ticket, id);
	}
}
