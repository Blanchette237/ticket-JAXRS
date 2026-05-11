package fr.istic.taa.jaxrs.dao.generic;

import fr.istic.taa.jaxrs.domain.Concert;

import java.util.List;

public class ConcertDao extends AbstractJpaDao<Long, Concert> {

	 public ConcertDao() {
	        super(Concert.class);
	    }

		public List<Concert> findByOrganisateur(Long organisateurId){
		 return entityManager
				 .createQuery("SELECT c FROM Concert c WHERE c.organiser.UserId = :id",Concert.class)
				 .setParameter("id",organisateurId)
				 .getResultList();
	}


	public List<Concert> findByLieu(String lieu){
		 return entityManager
				 .createNamedQuery("Concert.findByLieu", Concert.class)
				 .setParameter("lieu", lieu)
				 .getResultList();
	}



}
