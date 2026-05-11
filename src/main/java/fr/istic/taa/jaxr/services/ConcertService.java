package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Concert;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class ConcertService {
	private ConcertDao concertDao;
	private OrganisateurDao organisateurDao;

	public ConcertService() {
		this.concertDao = new ConcertDao();
		this.organisateurDao = new OrganisateurDao();
	}

	// Constructeur pour les tests (injection des DAOs mockés)
	ConcertService(ConcertDao concertDao, OrganisateurDao organisateurDao) {
		this.concertDao = concertDao;
		this.organisateurDao = organisateurDao;
	}

	public List<Concert> findAll() {
		return concertDao.findAll();
	}

	public Concert findOne(Long id) {
		return concertDao.findOne(id);
	}

	public long create(ConcertCreateDTO dto) {
		if (organisateurDao.findOne(dto.getOrganisateurId()) == null) {
			throw new BadRequestException("Organisateur non trouvé");
		}
		if (dto.getCapacite() <= 0) {
			throw new BadRequestException("La capacité ne peut être nulle ou négative");
		}
		if (!dto.getDateTime().isAfter(LocalDateTime.now())) {
			throw new BadRequestException("Le concert doit se tenir à une date future");
		}

		Concert concert = new Concert();
		concert.setLieu(dto.getLieu());
		concert.setDescription(dto.getDescription());
		concert.setCapaciteMax(dto.getCapacite());
		concert.setCapacite(dto.getCapacite()); // places disponibles initialisées au max
		concert.setDate(dto.getDateTime());
		concert.setPopularite(dto.getPopularite() != null ? dto.getPopularite().doubleValue() : 1.0);
		concert.setOrganiser(organisateurDao.findOne(dto.getOrganisateurId()));
		concertDao.save(concert);
		return concert.getConcertId();
	}

	public void delete(Long id) {
		Concert concert = concertDao.findOne(id);
		if (concert == null) {
			throw new NotFoundException("Concert non trouvé");
		}
		concertDao.delete(concert);
	}
}
