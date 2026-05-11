package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.TicketCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ClientDao;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.TicketDao;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;
import fr.istic.taa.jaxrs.domain.TicketStatus;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class TicketService {

	private ConcertDao concertDao;
	private TicketDao ticketDao;
	private ClientDao clientDao;

	public TicketService() {
		this.concertDao = new ConcertDao();
		this.ticketDao = new TicketDao();
		this.clientDao = new ClientDao();
	}

	// Constructeur pour les tests (injection des DAOs mockés)
	TicketService(ConcertDao concertDao, TicketDao ticketDao, ClientDao clientDao) {
		this.concertDao = concertDao;
		this.ticketDao = ticketDao;
		this.clientDao = clientDao;
	}

	public Ticket findOne(Long id) {
		return ticketDao.findOne(id);
	}

	public List<Ticket> findAll() {
		return ticketDao.findAll();
	}

	public long create(final TicketCreateDTO dto) throws ClientErrorException {
		var client = clientDao.findOne(dto.getUtilisateurId());
		if (client == null) {
			throw new BadRequestException("Utilisateur non trouvé");
		}

		var concert = concertDao.findOne(dto.getConcertId());
		if (concert == null) {
			throw new NotFoundException("Le concert n'existe pas");
		}

		if (!concert.getDate().isAfter(LocalDateTime.now())) {
			throw new BadRequestException("Le concert a déjà eu lieu");
		}

		// Vérification des places disponibles via le champ capacite
		if (concert.getCapacite() <= 0) {
			throw new ConflictException("Le concert est complet");
		}

		if (ticketDao.existsByConcertAndPlace(dto.getNumeroPlace(), concert)) {
			throw new ConflictException("La place " + dto.getNumeroPlace() + " n'est plus disponible");
		}

		Double prixUnitaire = calculPrixUnitaire(concert, dto.getNumeroPlace());
		Ticket ticket = new Ticket();
		ticket.setConcert(concert);
		ticket.setClient(client);
		ticket.setDate_achat(LocalDateTime.now());
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setPrixUnitaire(prixUnitaire);
		ticket.setNumeroPlace(dto.getNumeroPlace());
		concert.setCapacite(concert.getCapacite() - 1);

		ticketDao.save(ticket);
		concertDao.update(concert);
		return ticket.getTicketId();
	}

	private Double calculPrixUnitaire(Concert concert, String numeroPlace) {
		double basePrice = 30.0;

		String zone = extractZone(numeroPlace);
		switch (zone) {
			case "VIP": basePrice += 50; break;
			case "A":   basePrice += 20; break;
			case "B":   basePrice += 10; break;
			default:    basePrice += 5;
		}

		int numero = extractNumero(numeroPlace);
		if (numero <= 10) {
			basePrice += 20;
		} else if (numero <= 30) {
			basePrice += 10;
		}

		if (concert.getPopularite() != null) {
			basePrice += concert.getPopularite() * 5;
		}

		// Surge pricing : moins de 20% de places restantes → surcoût
		if (concert.getCapaciteMax() > 0 && concert.getCapacite() / concert.getCapaciteMax() < 0.2) {
			basePrice += 15;
		}

		return basePrice;
	}

	private int extractNumero(String numeroPlace) {
		String digits = numeroPlace.replaceAll("[^0-9]", "");
		return digits.isEmpty() ? 0 : Integer.parseInt(digits);
	}

	private String extractZone(String numeroPlace) {
		return numeroPlace.replaceAll("[0-9]", "").toUpperCase();
	}

	public void annuler(Long ticketId) {
		Ticket ticket = ticketDao.findOne(ticketId);
		if (ticket == null) {
			throw new NotFoundException("Ticket non trouvé");
		}
		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			throw new BadRequestException("Seul un ticket actif peut être annulé");
		}
		ticket.setStatus(TicketStatus.ANNULE);
		// Restituer la place au concert
		Concert concert = ticket.getConcert();
		concert.setCapacite(concert.getCapacite() + 1);
		ticketDao.update(ticket);
		concertDao.update(concert);
	}
}
