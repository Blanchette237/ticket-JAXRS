package fr.istic.taa.jaxrs.dao.generic;

import fr.istic.taa.jaxrs.domain.Client;
import fr.istic.taa.jaxrs.domain.Concert;
import fr.istic.taa.jaxrs.domain.Ticket;

import java.util.List;

public class TicketDao extends AbstractJpaDao<Long, Ticket> {
    public TicketDao() {
        super(Ticket.class);
    }

    public boolean existsByConcertAndPlace(String place, Concert concert) {
        return entityManager
                .createQuery("select count(t) > 0 from Ticket t where t.concert = :concert and t.numeroPlace = :place", Boolean.class)
                .setParameter("place", place)
                .setParameter("concert", concert)
                .getSingleResult();
    }

    public long countByConcert(Concert concert) {
        return entityManager.createQuery("select count(t) from Ticket t where t.concert = :concert", Long.class)
                .setParameter("concert", concert)
                .getSingleResult();
    }


    public List<Ticket> findByConcert(Concert concert) {
        return entityManager.createQuery("Select t From Ticket t WHERE t.concert = :concert", Ticket.class)
                .setParameter("concert", concert)
                .getResultList();
    }

    public List<Ticket> findByClient(Client client) {
        return entityManager.createQuery("Select t From Ticket t WHERE t.client = :client", Ticket.class)
                .setParameter("client", client)
                .getResultList();
    }

    public Long countActiveByClient(Client client){
        return entityManager.createQuery("Select COUNT(t) FROM Ticket t WHERE t.client = :client AND t.status = fr.istic.taa.jaxrs.domain.TicketStatus.ACTIVE", Long.class)
                .setParameter("client", client)
                .getSingleResult();
    }
}