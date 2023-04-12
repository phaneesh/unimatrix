package io.raven.db;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class AbstractDao<E> {

  private final SessionFactory sessionFactory;
  private final Class<?> entityClass;

  protected AbstractDao(SessionFactory sessionFactory, Class<E> entityClass) {
    this.sessionFactory = requireNonNull(sessionFactory);
    this.entityClass = entityClass;
  }

  @SuppressWarnings("unchecked")
  public Class<E> getEntityClass() {
    return (Class<E>) entityClass;
  }

  @SuppressWarnings("unchecked")
  protected E uniqueResult(Criteria criteria) throws HibernateException {
    return (E) requireNonNull(criteria).uniqueResult();
  }

  @SuppressWarnings("unchecked")
  protected List<E> list(Criteria criteria) throws HibernateException {
    return requireNonNull(criteria).list();
  }

  protected Session currentSession() {
    return sessionFactory.getCurrentSession();
  }

  protected E persist(E entity) throws HibernateException {
    if (currentSession().contains(entity)) {
      currentSession().refresh(entity);
    }
    currentSession().saveOrUpdate(requireNonNull(entity));
    return entity;
  }

  protected List<E> persist(List<E> entities) throws HibernateException {
    for (E entity : entities) {
      currentSession().persist(entity);
    }
    currentSession().flush();
    currentSession().clear();
    return entities;
  }
}
