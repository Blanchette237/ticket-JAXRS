package fr.istic.taa.jaxrs.dao.generic;


import fr.istic.taa.jaxrs.domain.Organiser;

public class OrganisateurDao extends AbstractJpaDao<Long, Organiser> {
	 public OrganisateurDao() {
	        super(Organiser.class);
	    }
}
