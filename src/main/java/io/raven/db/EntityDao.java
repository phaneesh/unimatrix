package io.raven.db;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MultiIdentifierLoadAccess;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Slf4j
public class EntityDao<T> {

  private final Class<T> entityClass;
  private final EntityInternalDao dao;

  public EntityDao(SessionFactory sessionFactory, Class<T> entityClass) {
    this.dao = new EntityInternalDao(sessionFactory, entityClass);
    this.entityClass = entityClass;
  }

  public Optional<T> get(Long ids) throws UniMatrixException {
    return get(ids, e -> e);
  }

  public <U> Optional<U> get(Long ids, Function<Optional<T>, Optional<U>> handler) throws UniMatrixException {
    return TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::get, ids, handler);
  }

  public List<T> get(List<Long> ids) throws UniMatrixException {
    return get(ids, e -> e);
  }

  public <U> List<U> get(List<Long> ids, Function<List<T>, List<U>> handler) throws UniMatrixException {
    return TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::get, ids, handler);
  }

  public boolean exists(Long id) throws UniMatrixException {
    return get(id).isPresent();
  }

  public Optional<T> save(T entity) throws UniMatrixException {
    return Optional.ofNullable(save(entity, t -> t));
  }

  public <U> U save(T entity, Function<T, U> handler) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .sessionFactory(dao.sessionFactory)
        .readOnly(false)
        .build()
        .execute(dao::save, entity, handler);
  }

  public List<T> save(List<T> entities) throws UniMatrixException {
    return save(entities, e -> e);
  }

  public <U> List<U> save(List<T> entities, Function<List<T>, List<U>> handler) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .sessionFactory(dao.sessionFactory)
        .readOnly(false)
        .build()
        .execute(dao::save, entities, handler);
  }

  public boolean updateInLock(Long id, UnaryOperator<Optional<T>> updater) throws UniMatrixException {
    return updateImpl(id, dao::getLockedForWrite, updater);
  }

  private boolean updateImpl(Long id, Function<Long, Optional<T>> getter, UnaryOperator<Optional<T>> updater) throws UniMatrixException {
    try {
      return TransactionManager.newTransaction()
          .readOnly(false)
          .sessionFactory(dao.sessionFactory)
          .build()
          .<Optional<T>, Long, Boolean>execute(getter, id, entity -> {
            if (entity.isEmpty()) {
              return false;
            }
            Optional<T> newEntity = updater.apply(entity);
            if (newEntity.isEmpty()) {
              return false;
            }
            newEntity.ifPresent(dao::update);
            return true;
          });
    } catch (Exception e) {
      throw UniMatrixException.from()
          .exception(e)
          .build();
    }
  }

  public boolean update(Long id, UnaryOperator<Optional<T>> updater) throws UniMatrixException {
    return updateImpl(id, dao::get, updater);
  }


  public TransactionContext<T> getTransactionContext(Long id) {
    return new TransactionContext<T>(dao.sessionFactory, dao::getLockedForWrite, id);
  }

  public TransactionContext<T> getTransactionContext(LongSupplier supplier) {
    return getTransactionContext(supplier.getAsLong());
  }

  public BatchTransactionContext<T> getBatchTransactionContext(List<Long> ids) {
    return new BatchTransactionContext<>(dao.sessionFactory, dao::getLockedForWrite, ids, true);
  }

  public BatchTransactionContext<T> getBatchTransactionContext(Supplier<List<Long>> supplier) {
    return getBatchTransactionContext(supplier.get());
  }

  public TransactionContext<T> saveTransactionContext(T entity) {
    return new TransactionContext<>(dao.sessionFactory, dao::save, entity);
  }

  public TransactionContext<T> saveTransactionContext(Supplier<T> generator) {
    return saveTransactionContext(generator.get());
  }

  public BatchTransactionContext<T> saveBatchTransactionContext(List<T> entities) {
    return new BatchTransactionContext<>(dao.sessionFactory, dao::save, entities);
  }

  public BatchTransactionContext<T> saveBatchTransactionContext(Supplier<List<T>> generator) {
    return saveBatchTransactionContext(generator.get());
  }

  public long count(DetachedCriteria criteria) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::count, criteria);
  }

  public BigDecimal sum(DetachedCriteria criteria, String propertyName) throws UniMatrixException {
    criteria.setProjection(Projections.sum(propertyName));
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::sum, criteria);
  }

  public long max(DetachedCriteria criteria, String propertyName) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::max, MaxParams.builder().criteria(criteria).propertyName(propertyName).build());
  }

  public <U> List<U> select(DetachedCriteria detachedCriteria, Function<List<T>, List<U>> handler, int limit, int offset) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::select, CriteriaParams.builder()
            .criteria(detachedCriteria)
            .limit(limit)
            .offset(offset)
            .build(), handler);
  }

  public <U> List<U> select(DetachedCriteria criteria, Function<List<T>, List<U>> handler, int pageSize) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::select, CriteriaParams.builder()
            .criteria(criteria)
            .limit(pageSize)
            .build(), handler);
  }

  public <U> List<U> select(DetachedCriteria criteria, Function<List<T>, List<U>> handler) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::select, criteria, handler);
  }

  public <U> List<U> select(String query, Map<String, Object> params, Function<List<T>, List<U>> handler) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::select, QueryParams.builder()
            .query(query)
            .params(params)
            .build(), handler);

  }

  public Optional<T> selectSingle(DetachedCriteria detachedCriteria) throws UniMatrixException {
    return selectSingle(detachedCriteria, t -> t);
  }

  public <U> Optional<U> selectSingle(DetachedCriteria detachedCriteria, Function<T, U> handler) throws UniMatrixException {
    return Optional.ofNullable(TransactionManager
        .newTransaction()
        .readOnly(true)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::selectSingle, detachedCriteria, handler));
  }

  public int update(String query, Map<String, Object> params) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(false)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::update, QueryParams.builder()
            .params(params)
            .query(query)
            .build());
  }

  public int updateNative(String query, Map<String, Object> params) throws UniMatrixException {
    return TransactionManager
        .newTransaction()
        .readOnly(false)
        .sessionFactory(dao.sessionFactory)
        .build()
        .execute(dao::update, QueryParams.builder()
            .params(params)
            .query(query)
            .nativeQuery(true)
            .build());
  }

  @Data
  @Builder
  private static class CriteriaParams {

    private DetachedCriteria criteria;

    @Builder.Default
    private int limit = -1;

    @Builder.Default
    private int offset = -1;

    private String fetchProfile;
  }

  @Data
  @Builder
  private static class QueryParams {

    private String query;

    private Map<String, Object> params;

    private boolean nativeQuery;
  }

  @Data
  @Builder
  private static class MaxParams {

    private DetachedCriteria criteria;

    private String propertyName;
  }

  @Getter
  public static class TransactionContext<T> {

    private final SessionFactory sessionFactory;
    private final Mode mode;
    private Function<Long, Optional<T>> function;
    private UnaryOperator<T> saver;
    private T entity;
    private Long key;
    private List<Function<T, Void>> operations = Lists.newArrayList();

    public TransactionContext(SessionFactory sessionFactory, Function<Long, Optional<T>> getter, Long key) {
      this.sessionFactory = sessionFactory;
      this.function = getter;
      this.key = key;
      this.mode = Mode.READ;
    }

    public TransactionContext(SessionFactory sessionFactory, UnaryOperator<T> saver, T entity) {
      this.sessionFactory = sessionFactory;
      this.saver = saver;
      this.entity = entity;
      this.mode = Mode.INSERT;
    }

    public TransactionContext<T> mutate(Mutator<T> mutator) {
      return apply(parent -> {
        mutator.mutator(parent);
        return null;
      });
    }

    public TransactionContext<T> apply(Function<T, Void> handler) {
      this.operations.add(handler);
      return this;
    }

    public <U> TransactionContext<T> save(EntityDao<U> lookupDao, Function<T, U> entityGenerator) {
      return apply(parent -> {
        try {
          U entity = entityGenerator.apply(parent);
          lookupDao.save(entity);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      });
    }

    public <U> TransactionContext<T> saveAll(EntityDao<U> lookupDao, Function<T, List<U>> entityGenerator) {
      return apply(parent -> {
        try {
          List<U> entities = entityGenerator.apply(parent);
          lookupDao.save(entities);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      });
    }

    public <U> TransactionContext<T> update(EntityDao<U> lookupDao, Long id, UnaryOperator<Optional<U>> handler) {
      return apply(parent -> {
        try {
          lookupDao.update(id, handler);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      });
    }

    public <U> TransactionContext<T> update(EntityDao<U> lookupDao, String query, Map<String, Object> params) {
      return apply(parent -> {
        try {
          int result = lookupDao.update(query, params);
          if (result < 1)
            throw UniMatrixException.fromMessage().message("Update operation returned result " + result).build();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      });
    }

    public TransactionContext<T> filter(Predicate<T> predicate) {
      return filter(predicate, new IllegalArgumentException("Predicate check failed"));
    }

    public TransactionContext<T> filter(Predicate<T> predicate, RuntimeException failureException) {
      return apply(parent -> {
        boolean result = predicate.test(parent);
        if (!result) {
          throw failureException;
        }
        return null;
      });
    }

    public T execute() {
      var transactionManager = TransactionManager.newTransaction()
          .readOnly(false)
          .sessionFactory(sessionFactory)
          .build();
      transactionManager.beforeStart();
      try {
        T result = generateEntity();
        operations
            .forEach(operation -> operation.apply(result));
        return result;
      } catch (Exception e) {
        transactionManager.onError(e);
        throw e;
      } finally {
        transactionManager.afterEnd();
      }
    }

    private T generateEntity() {
      Optional<T> result = Optional.empty();
      switch (mode) {
        case READ:
          result = function.apply(key);
          if (result.isEmpty()) {
            throw new RuntimeException("Entity doesn't exist for keys: " + key);
          }
          break;
        case INSERT:
          result = Optional.ofNullable(saver.apply(entity));
          break;
        default:
          break;

      }
      return result.orElse(null);
    }

    enum Mode {READ, INSERT}

    @FunctionalInterface
    public interface Mutator<T> {
      void mutator(T parent);
    }
  }


  @Getter
  public static class BatchTransactionContext<T> {

    private final SessionFactory sessionFactory;
    private final Mode mode;
    private Function<List<Long>, List<T>> function;
    private UnaryOperator<List<T>> saver;
    private List<T> entity;
    private List<Long> keys;
    private List<Function<List<T>, Void>> operations = Lists.newArrayList();

    public BatchTransactionContext(SessionFactory sessionFactory, Function<List<Long>, List<T>> getter, List<Long> keys, boolean read) {
      this.sessionFactory = sessionFactory;
      this.function = getter;
      this.keys = keys;
      this.mode = Mode.READ;
    }

    public BatchTransactionContext(SessionFactory sessionFactory, UnaryOperator<List<T>> saver, List<T> entity) {
      this.sessionFactory = sessionFactory;
      this.saver = saver;
      this.entity = entity;
      this.mode = Mode.INSERT;
    }

    public BatchTransactionContext<T> mutate(Mutator<T> mutator) {
      return apply(parent -> {
        mutator.mutator(parent);
        return null;
      });
    }

    public BatchTransactionContext<T> apply(Function<List<T>, Void> handler) {
      this.operations.add(handler);
      return this;
    }

    public <U> BatchTransactionContext<T> save(EntityDao<U> entityDao, Function<List<T>, List<U>> entityGenerator) {
      return apply(parent -> {
        try {
          List<U> entities = entityGenerator.apply(parent);
          entityDao.save(entities);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return null;
      });
    }

    public List<T> execute() {
      TransactionManager transactionManager = TransactionManager.newTransaction()
          .sessionFactory(sessionFactory).readOnly(false).build();
      transactionManager.beforeStart();
      try {
        List<T> result = generateEntity();
        operations
            .forEach(operation -> operation.apply(result));
        return result;
      } catch (Exception e) {
        transactionManager.onError(e);
        throw e;
      } finally {
        transactionManager.afterEnd();
      }
    }

    private List<T> generateEntity() {
      List<T> result = null;
      switch (mode) {
        case READ:
          result = function.apply(keys);
          if (result == null) {
            throw new RuntimeException("Entity doesn't exist for keys: " + keys);
          }
          break;
        case INSERT:
          result = saver.apply(entity);
          break;
        default:
          break;

      }
      return result;
    }

    enum Mode {READ, INSERT}

    @FunctionalInterface
    public interface Mutator<T> {
      void mutator(List<T> parent);
    }
  }

  private final class EntityInternalDao extends AbstractDao<T> {

    private final SessionFactory sessionFactory;

    public EntityInternalDao(SessionFactory sessionFactory, Class<T> entityClass) {
      super(sessionFactory, entityClass);
      this.sessionFactory = sessionFactory;
    }

    Optional<T> get(Long id) {
      return getLocked(id, LockMode.READ);
    }

    Optional<T> getLocked(Long id, LockMode lockMode) {
      return Optional.ofNullable(currentSession().get(entityClass, id, lockMode));
    }

    List<T> get(List<Long> ids) {
      return getLocked(ids, LockMode.READ);
    }

    List<T> getLocked(List<Long> ids, LockMode lockMode) {
      MultiIdentifierLoadAccess<T> multiGet = currentSession().byMultipleIds(entityClass);
      return multiGet.with(new LockOptions(lockMode)).multiLoad(ids);
    }

    Optional<T> getLockedForWrite(Long id) {
      return getLocked(id, LockMode.UPGRADE_NOWAIT);
    }

    List<T> getLockedForWrite(List<Long> ids) {
      return getLocked(ids, LockMode.UPGRADE_NOWAIT);
    }

    T save(T entity) {
      return persist(entity);
    }

    List<T> save(List<T> entities) {
      List<T> saved = new ArrayList<>();
      for (T e : entities) {
        saved.add(persist(e));
      }
      return saved;
    }

    void update(T entity) {
      currentSession().evict(entity); //Detach .. otherwise update is a no-op
      currentSession().update(entity);
    }

    List<T> select(DetachedCriteria criteria) {
      return list(criteria.getExecutableCriteria(currentSession()));
    }

    public List<T> select(final QueryParams queryParams) {
      Query<T> tQuery = currentSession().createQuery(queryParams.query, getEntityClass());
      if (queryParams.params != null)
        queryParams.params.forEach(tQuery::setParameter);
      return tQuery.getResultList();
    }

    public List<T> select(CriteriaParams criteriaParams) {
      Criteria exeCriteria = criteriaParams.criteria.getExecutableCriteria(currentSession());
      if (criteriaParams.limit != -1)
        exeCriteria.setMaxResults(criteriaParams.limit);
      if (criteriaParams.offset != -1)
        exeCriteria.setFirstResult(criteriaParams.offset);
      if (!Strings.isNullOrEmpty(criteriaParams.fetchProfile)) {
        currentSession().enableFetchProfile(criteriaParams.fetchProfile);
      }
      return list(exeCriteria);
    }

    public T selectSingle(DetachedCriteria criteria) {
      return uniqueResult(criteria.getExecutableCriteria(currentSession()));
    }

    long count(DetachedCriteria criteria) {
      return (long) criteria.getExecutableCriteria(currentSession())
          .setProjection(Projections.rowCount())
          .uniqueResult();
    }

    BigDecimal sum(DetachedCriteria criteria) {
      return (BigDecimal) criteria.getExecutableCriteria(currentSession())
          .uniqueResult();
    }

    Long max(MaxParams maxParams) {
      return (Long) maxParams.criteria.getExecutableCriteria(currentSession())
          .setProjection(Projections.max(maxParams.propertyName))
          .uniqueResult();
    }

    public int update(QueryParams updateParams) {
      if (updateParams.nativeQuery) {
        var tQuery = currentSession().createSQLQuery(updateParams.query);
        updateParams.params.forEach(tQuery::setParameter);
        return tQuery.executeUpdate();
      } else {
        var tQuery = currentSession().createQuery(updateParams.query);
        updateParams.params.forEach(tQuery::setParameter);
        return tQuery.executeUpdate();
      }
    }
  }
}
