package fr.istic.taa.jaxrs;

import fr.istic.taa.jaxrs.dao.generic.EntityManagerHelper;
import jakarta.persistence.EntityManager;

public class TestConnection {
    public static void main(String[] args) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        System.out.println("Connexion OK");
        em.close();
        EntityManagerHelper.closeEntityManagerFactory();
    }
}
