package fr.istic.taa.jaxrs.dao.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * DAO générique qui fournit les opérations CRUD pour n'importe quelle entité JPA.
 *
 * Pourquoi générique (K, T) ?
 * - K = type de la clé primaire (ex: Long pour ConcertId)
 * - T = type de l'entité (ex: Concert, Ticket, Client...)
 * Cela évite de réécrire findOne/findAll/save/update/delete pour chaque entité.
 * Chaque DAO spécialisé (ConcertDao, TicketDao...) hérite de cette classe
 * et n'a qu'à fournir sa propre classe via le constructeur.
 *
 * Pourquoi des transactions manuelles (begin/commit) ?
 * En dehors d'un conteneur Java EE complet (WildFly, Quarkus...), JPA ne gère
 * pas les transactions automatiquement. Chaque opération d'écriture doit
 * explicitement démarrer et valider une transaction.
 */
public abstract class AbstractJpaDao<K, T extends Serializable> implements IGenericDao<K, T> {

	private final Class<T> clazz;

	protected EntityManager entityManager;

	public AbstractJpaDao(Class<T> clazz) {
		this.entityManager = EntityManagerHelper.getEntityManager();
		this.clazz = requireNonNull(clazz);
	}

	public T findOne(K id) {
		// find() retourne null si l'entité n'existe pas (pas d'exception)
		return entityManager.find(clazz, id);
	}

	public List<T> findAll() {
		// JPQL : langage de requête orienté objet (noms de classes, pas de tables)
		return entityManager.createQuery("select e from " + clazz.getName() + " as e", clazz).getResultList();
	}

	public void save(T entity) {
		EntityTransaction t = this.entityManager.getTransaction();
		t.begin();
		// persist() insère l'entité et déclenche la génération de l'ID (@GeneratedValue)
		entityManager.persist(entity);
		t.commit();
	}

	public T update(final T entity) {
		EntityTransaction t = this.entityManager.getTransaction();
		t.begin();
		// merge() met à jour une entité déjà existante en base
		T res = entityManager.merge(entity);
		t.commit();
		return res;
	}

	public void delete(T entity) {
		EntityTransaction t = this.entityManager.getTransaction();
		t.begin();
		entityManager.remove(entity);
		t.commit();
	}

	public void deleteById(K entityId) {
		T entity = findOne(entityId);
		delete(entity);
	}
}
