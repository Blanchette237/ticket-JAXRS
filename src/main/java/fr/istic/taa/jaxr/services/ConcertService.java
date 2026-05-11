package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.ConcertDao;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Concert;
import jakarta.ws.rs.BadRequestException;
import javassist.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

public class ConcertService {
    private final ConcertDao concertDao = new ConcertDao();
    private final OrganisateurDao organisateurDao = new OrganisateurDao();

    public List<Concert> findAll() {
        return concertDao.findAll();
    }

    public Concert findOne(Long id) {
        return concertDao.findOne(id);
    }

    public long create(ConcertCreateDTO dto) {
        // Est-ce que l'id de l'utilisateur fourni est un organisateur ?
        if (organisateurDao.findOne(dto.getOrganisateurId()) == null) {
            throw new BadRequestException("Organisateur non trouvé");
        }
        // Cohérence valeur
        if (dto.getCapacite() <= 0) {
            throw new BadRequestException("La capacité ne peut être nulle");
        }
        // Date de concert dans le futur ?
        if (!dto.getDateTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Le concert doit se tenir à une date future");
        }

        // Création de l'entité - Mapping
        Concert concert = new Concert();
        concert.setLieu(dto.getLieu());
        concert.setDescription(dto.getDescription());
        concert.setCapacite(dto.getCapacite());
        concert.setDate(dto.getDateTime());
        concert.setOrganiser(organisateurDao.findOne(dto.getOrganisateurId()));
        concertDao.save(concert);
        return concert.getConcertId();
    }

    public void delete (Long id) throws NotFoundException {
        Concert concert = concertDao.findOne(id);
        if(concert == null){
            throw new NotFoundException("Concert non trouvé");
        }
        concertDao.delete(concert);
    }
}