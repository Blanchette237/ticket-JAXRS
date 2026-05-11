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
	
	 private final ConcertDao concertDao = new ConcertDao();
	 private final TicketDao ticketDao = new TicketDao();
	 private final ClientDao clientDao = new ClientDao();

	    public Ticket findOne(Long id) {
	        return ticketDao.findOne(id);
	    }

	    public List<Ticket> findAll() {
	        return ticketDao.findAll();
	    }

	    public long create(final TicketCreateDTO dto) throws ClientErrorException {
	        // Contrôle métier

	        // Est-ce que l'id de l'utilisateur fourni est un organisateur ?
	        var client = clientDao.findOne(dto.getUtilisateurId());
	        if (client == null) {
	            throw new BadRequestException("Utilisateur non trouvé");
	        }

	        // Le concert existe-t-il ?
	        var concert = concertDao.findOne(dto.getConcertId());
	        if (concert == null) {
	            throw new NotFoundException("Le concert n'existe pas");
	        }

	        // Le concert est à venir ?
	        if (!concert.getDate().isAfter(LocalDateTime.now())) {
	            throw new BadRequestException("Le concert a déjà eu lieu");
	        }

	        // Reste-t-il des places
	        long placesRestantes = ticketDao.countByConcert(concert);
	        if (placesRestantes >= concert.getCapaciteMax()) {
	            throw new ConflictException("Le concert est complet");
	        }

	        // La place est-elle disponible ?
	        if (ticketDao.existsByConcertAndPlace(dto.getNumeroPlace(), concert)) {
	            throw new ConflictException("La place " + dto.getNumeroPlace() + " n'est plus disponible");
	        }

	        // Création de l'entité - Mapping
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
	        concertDao.save(concert);
	        return ticket.getTicketId();
	    }

	    private Double calculPrixUnitaire(Concert concert, String numeroPlace) {
	        // TODO Calcul en fonction du concert, du genre musical, de la popularité des artistes, etc.
	        // TODO + numéro de place
//	        return 42.0d;

			//private Double calculPrixUnitaire(Concert concert, String numeroPlace) {

				double basePrice = 30.0;

				// 🎟️ Zone (VIP, A, B…)
				String zone = extractZone(numeroPlace);
				switch (zone) {
					case "VIP":
						basePrice += 50;
						break;
					case "A":
						basePrice += 20;
						break;
					case "B":
						basePrice += 10;
						break;
					default:
						basePrice += 5;
				}

				// 📍 Numéro de place (plus petit = mieux placé)
				int numero = extractNumero(numeroPlace);
				if (numero <= 10) {
					basePrice += 20;
				} else if (numero <= 30) {
					basePrice += 10;
				}

				// 🔥 Popularité du concert (exemple)
				if (concert.getPopularite() != null) {
					basePrice += concert.getPopularite() * 5;
				}

				// 📉 Capacité restante → prix dynamique
				double capacite = concert.getCapacite();
				if (capacite < 50) {
					basePrice += 15; // plus de demande → plus cher
				}

				return basePrice;
			}

	private int extractNumero(String numeroPlace) {
		return Integer.parseInt(numeroPlace.replaceAll("[^0-9]", ""));
	}

	private String extractZone(String numeroPlace) {
		return numeroPlace.replaceAll("[0-9]", "");
	}



	public void annuler(Long ticketId){
			Ticket ticket = ticketDao.findOne(ticketId);
			if(ticket== null){
				throw new NotFoundException("ticket non trouvé");
			}
			if(ticket.getStatus() != TicketStatus.ACTIVE){
				throw new BadRequestException("seul un ticket activé peut etre annulé");
			}
			ticket.setStatus(TicketStatus.ANNULE);
			//Restituer le ticket au concert
		Concert concert = ticket.getConcert();
		concert.setCapacite(concert.getCapaciteMax() + 1);
		ticketDao.update(ticket);
		concertDao.update(concert);
	}

}
