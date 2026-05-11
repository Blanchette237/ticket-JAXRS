package fr.istic.taa.jaxr.services;

import fr.istic.taa.jaxrs.dao.generic.OrganisateurDao;
import fr.istic.taa.jaxrs.domain.Organiser;
import javassist.NotFoundException;

import java.util.List;

public class OrganiserService {

    private final OrganisateurDao organisateurDao = new OrganisateurDao();

    public List<Organiser> findAll(){
        return organisateurDao.findAll();
    }

    public Organiser findOne(Long id) throws NotFoundException {
        Organiser organiser = organisateurDao.findOne(id);
        if(organiser == null){
            throw new NotFoundException("organisateur non trouvé");
        }
        return organiser;
    }
}
