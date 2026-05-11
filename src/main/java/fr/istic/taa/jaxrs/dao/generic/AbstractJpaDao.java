package fr.istic.taa.jaxrs.dao.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class AbstractJpaDao<K, T extends Serializable> implements IGenericDao<K, T> {

	private final Class<T> clazz;

	protected EntityManager entityManager;

	public AbstractJpaDao(Class<T> clazz) {
		this.entityManager = EntityManagerHelper.getEntityManager();
		this.clazz = requireNonNull(clazz);
	}

	public T findOne(K id) {
		return entityManager.find(clazz, id);
	}

	public List<T> findAll() {
		return entityManager.createQuery("select e from " + clazz.getName() + " as e",clazz).getResultList();
	}

	public void save(T entity) {
		EntityTransaction t = this.entityManager.getTransaction();
		t.begin();
		entityManager.persist(entity);
		t.commit();
	}

	public T update(final T entity) {
		EntityTransaction t = this.entityManager.getTransaction();
		t.begin();
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