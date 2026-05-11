package fr.istic.taa.jaxrs.dao.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Fournit un EntityManager JPA partagé pour toute la durée d'une requête HTTP.
 *
 * Pourquoi ThreadLocal ?
 * Le serveur Undertow traite chaque requête HTTP dans un thread dédié.
 * ThreadLocal garantit que chaque thread dispose de son propre EntityManager,
 * évitant les conflits d'accès concurrent à la base de données.
 * Sans cela, deux requêtes simultanées partageraient le même EntityManager
 * et provoqueraient des erreurs ou des données corrompues.
 */
public class EntityManagerHelper {

	// Créé une seule fois au démarrage de l'application (coûteux à instancier)
	private static final EntityManagerFactory emf;

	// Un EntityManager distinct par thread (une instance par requête HTTP)
	private static final ThreadLocal<EntityManager> threadLocal;

	static {
		// "dev" = nom de l'unité de persistance définie dans persistence.xml
		emf = Persistence.createEntityManagerFactory("dev");
		threadLocal = new ThreadLocal<EntityManager>();
	}

	/**
	 * Retourne l'EntityManager du thread courant.
	 * En crée un nouveau s'il n'en existe pas encore pour ce thread.
	 */
	public static EntityManager getEntityManager() {
		EntityManager em = threadLocal.get();

		if (em == null) {
			em = emf.createEntityManager();
			threadLocal.set(em);
		}
		return em;
	}

	public static void closeEntityManager() {
		EntityManager em = threadLocal.get();
		if (em != null) {
			em.close();
			threadLocal.set(null);
		}
	}

	public static void closeEntityManagerFactory() {
		emf.close();
	}

	public static void beginTransaction() {
		getEntityManager().getTransaction().begin();
	}

	public static void rollback() {
		getEntityManager().getTransaction().rollback();
	}

	public static void commit() {
		getEntityManager().getTransaction().commit();
	}
}
