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

	/**
	 * Constructeur package-private réservé aux tests unitaires.
	 * Permet d'injecter des DAOs mockés (Mockito) sans toucher à la base de données.
	 * Non public : seules les classes du même package (les tests) peuvent l'utiliser.
	 */
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

	/**
	 * Crée un ticket après validation complète des règles métier.
	 * Toutes les vérifications sont effectuées AVANT toute écriture en base.
	 * Si une règle échoue, une exception HTTP appropriée est levée (400, 404, 409).
	 */
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

		// On vérifie le champ `capacite` (places restantes) et non `capaciteMax` (places totales).
		// Ces deux champs sont distincts : capaciteMax ne change jamais, capacite est décrémenté à chaque vente.
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

		// Décrémenter les places disponibles (capacite), pas le max
		concert.setCapacite(concert.getCapacite() - 1);

		ticketDao.save(ticket);
		concertDao.update(concert);
		return ticket.getTicketId();
	}

	/**
	 * Calcule le prix d'un ticket selon 4 critères cumulables :
	 *   1. Zone de la place (VIP / A / B / autre)
	 *   2. Numéro de place (les premières places sont plus chères)
	 *   3. Popularité du concert (note de 1 à 5)
	 *   4. Surge pricing : surcoût si moins de 20% de places restantes
	 *
	 * La zone et le numéro sont extraits du code de place (ex: "VIP3" → zone="VIP", numero=3).
	 */
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

		// Surge pricing : si plus de 80% des places sont vendues, le prix augmente.
		// Ratio = capacite / capaciteMax. Si < 0.2, il reste moins de 20% de places.
		if (concert.getCapaciteMax() > 0 && concert.getCapacite() / concert.getCapaciteMax() < 0.2) {
			basePrice += 15;
		}

		return basePrice;
	}

	/**
	 * Extrait le numéro depuis un code de place.
	 * Exemple : "VIP3" → 3,  "A12" → 12,  "B" → 0 (pas de numéro)
	 */
	private int extractNumero(String numeroPlace) {
		String digits = numeroPlace.replaceAll("[^0-9]", "");
		return digits.isEmpty() ? 0 : Integer.parseInt(digits);
	}

	/**
	 * Extrait la zone depuis un code de place (lettres uniquement, en majuscules).
	 * Exemple : "vip3" → "VIP",  "a12" → "A",  "B34" → "B"
	 */
	private String extractZone(String numeroPlace) {
		return numeroPlace.replaceAll("[0-9]", "").toUpperCase();
	}

	/**
	 * Annule un ticket actif et restitue la place au concert.
	 * Seul un ticket ACTIVE peut être annulé (pas ANNULE ni UTILISE).
	 * La capacité disponible du concert est incrémentée de 1.
	 */
	public void annuler(Long ticketId) {
		Ticket ticket = ticketDao.findOne(ticketId);
		if (ticket == null) {
			throw new NotFoundException("Ticket non trouvé");
		}
		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			throw new BadRequestException("Seul un ticket actif peut être annulé");
		}
		ticket.setStatus(TicketStatus.ANNULE);

		// Restituer la place : on incrémente `capacite` (places disponibles)
		// et non `capaciteMax` (total immuable).
		Concert concert = ticket.getConcert();
		concert.setCapacite(concert.getCapacite() + 1);
		ticketDao.update(ticket);
		concertDao.update(concert);
	}
}
