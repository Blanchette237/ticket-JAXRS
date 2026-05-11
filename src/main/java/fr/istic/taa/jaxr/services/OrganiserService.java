package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxr.dto.OrganiserCreateDTO;
import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Organiser;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public class OrganiserService {

    private OrganisateurDao organisateurDao;

    public OrganiserService() {
        this.organisateurDao = new OrganisateurDao();
    }

    OrganiserService(OrganisateurDao organisateurDao) {
        this.organisateurDao = organisateurDao;
    }

    public List<Organiser> findAll() {
        return organisateurDao.findAll();
    }

    public Organiser findOne(Long id) {
        Organiser organiser = organisateurDao.findOne(id);
        if (organiser == null) {
            throw new NotFoundException("Organisateur non trouvé");
        }
        return organiser;
    }

    public long create(OrganiserCreateDTO dto) {
        Organiser organiser = new Organiser();
        organiser.setName(dto.getName());
        organiser.setFirstname(dto.getFirstname());
        organiser.setEmail(dto.getEmail());
        organiser.setPassword(dto.getPassword());
        organisateurDao.save(organiser);
        return organiser.getUserId();
    }
}
