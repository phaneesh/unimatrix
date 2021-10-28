package io.raven.db;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.function.Function;

public class TransactionManager {

  private final SessionFactory sessionFactory;
  private final boolean readOnly;

  @Getter
  private Session session;


  @Builder(builderMethodName = "newTransaction")
  public TransactionManager(SessionFactory sessionFactory, boolean readOnly) {
    this.sessionFactory = sessionFactory;
    this.readOnly = readOnly;
  }


  public void beforeStart() {
    session = sessionFactory.openSession();
    try {
      configureSession();
      ManagedSessionContext.bind(session);
      session.beginTransaction();
    } catch (Exception t) {
      session.close();
      session = null;
      ManagedSessionContext.unbind(sessionFactory);
      throw t;
    }
  }

  private void configureSession() {
    session.setDefaultReadOnly(readOnly);
    session.setCacheMode(CacheMode.NORMAL);
    session.setHibernateFlushMode(FlushMode.AUTO);
  }

  public void afterEnd() {
    if (session == null) {
      return;
    }
    try {
      commitTransaction();
    } catch (Exception e) {
      rollbackTransaction();
      throw e;
    } finally {
      session.close();
      session = null;
      ManagedSessionContext.unbind(sessionFactory);
    }
  }

  private void commitTransaction() {
    final Transaction txn = session.getTransaction();
    if (txn != null && txn.getStatus() == TransactionStatus.ACTIVE) {
      txn.commit();
    }
  }

  private void rollbackTransaction() {
    final Transaction txn = session.getTransaction();
    if (txn != null && txn.getStatus() == TransactionStatus.ACTIVE) {
      txn.rollback();
    }
  }

  public void onError(Exception e) {
    if (session == null) {
      return;
    }
    try {
      if (!(e instanceof ConstraintViolationException)) {
        rollbackTransaction();
      }
    } finally {
      session.close();
      session = null;
      ManagedSessionContext.unbind(sessionFactory);
    }
  }

  public <T, U> T execute(Function<U, T> function, U arg) throws UniMatrixException {
    return execute(function, arg, t -> t);
  }

  public <T, U, V> V execute(Function<U, T> function, U arg, Function<T, V> handler) throws UniMatrixException {
    return execute(function, arg, handler, true);
  }

  public <T, U, V> V execute(Function<U, T> function, U arg, Function<T, V> handler, boolean completeTransaction) throws UniMatrixException {
    if (completeTransaction) {
      beforeStart();
    }
    try {
      T result = function.apply(arg);
      V returnValue = handler.apply(result);
      if (completeTransaction) {
        afterEnd();
      }
      return returnValue;
    } catch (Exception e) {
      if (completeTransaction) {
        onError(e);
      }
      throw UniMatrixException.from().exception(e).build();
    }
  }
}
