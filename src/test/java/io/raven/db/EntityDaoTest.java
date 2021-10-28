package io.raven.db;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.raven.db.entity.TestEntity;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityDaoTest extends AbstractDaoTest {

  private EntityDao<TestEntity> testEntityEntityDao;

  @BeforeEach
  public void before() {
    testEntityEntityDao = new EntityDao<>(uniMatrix.getSessionFactory(), TestEntity.class);
  }

  @Test
  void testSave() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("testId")
        .text("Some Text")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(testEntityEntityDao.exists(saved.get().getId()));
    assertFalse(testEntityEntityDao.exists(100L));
    Optional<TestEntity> result = testEntityEntityDao.get(saved.get().getId());
    assertEquals("Some Text", result.get().getText());
    testEntity.setText("Some New Text");
    saved = testEntityEntityDao.save(testEntity);
    result = testEntityEntityDao.get(saved.get().getId());
    assertEquals("Some New Text", result.get().getText());

    boolean updateStatus = testEntityEntityDao.update(result.get().getId(), entity -> {
      if (entity.isPresent()) {
        TestEntity e = entity.get();
        e.setText("Updated text");
        return Optional.of(e);
      }
      return null;
    });
    assertTrue(updateStatus);

    updateStatus = testEntityEntityDao.update(0L, entity -> {
      if (entity.isPresent()) {
        TestEntity e = entity.get();
        e.setText("Updated text");
        return Optional.ofNullable(e);
      }
      return null;
    });
    assertFalse(updateStatus);
  }

  @Test
  void testSaveBatch() throws Exception {
    List<TestEntity> tobeSaved = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      TestEntity testEntity = TestEntity.builder()
          .externalId("testId")
          .text("Some Text " + i)
          .build();
      tobeSaved.add(testEntity);
    }
    List<TestEntity> saved = testEntityEntityDao.save(tobeSaved);
    List<TestEntity> fetched = testEntityEntityDao.select(DetachedCriteria.forClass(TestEntity.class).add(Restrictions.eq("externalId", "testId")), e -> e);
    assertEquals(10, fetched.size());
  }

  @Test
  void testCount() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("testId")
        .text("Some Text")
        .build();
    testEntityEntityDao.save(testEntity);

    long count = testEntityEntityDao.count(DetachedCriteria.forClass(TestEntity.class).add(Restrictions.eq("externalId", "testId")));
    assertEquals(1, count);
  }

  @Test
  void testUpdate() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("updateId")
        .text("Some Text")
        .build();
    Optional<TestEntity> optionalEntity = testEntityEntityDao.save(testEntity);
    testEntityEntityDao.update(optionalEntity.get().getId(), testEntity1 -> {
      TestEntity entity = testEntity1.get();
      entity.setText("updated");
      return Optional.ofNullable(entity);
    });
    optionalEntity = testEntityEntityDao.get(optionalEntity.get().getId());
    assertEquals("updated", optionalEntity.get().getText());
  }

  @Test
  void testMax() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("ExtId")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    long max = testEntityEntityDao.max(DetachedCriteria.forClass(TestEntity.class), "id");
    assertEquals(1, max);
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("getInShard2")
        .text("Some Text1")
        .build();
    Optional<TestEntity> savedEntity = testEntityEntityDao.save(testEntity1);
    assertTrue(savedEntity.isPresent());
    max = testEntityEntityDao.max(DetachedCriteria.forClass(TestEntity.class), "id");
    assertEquals(2, max);
  }

  @Test
  void testGetMulti() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("ExtId")
        .text("Some Text5")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    List<TestEntity> entities = testEntityEntityDao.get(Lists.newArrayList(saved.get().getId()));
    assertEquals(1, entities.size());
  }

  @Test
  void testSum() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("ExtId")
        .text("Some Text5")
        .amount(BigDecimal.ZERO)
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    BigDecimal sum = testEntityEntityDao.sum(DetachedCriteria.forClass(TestEntity.class), "amount");
    assertEquals(new BigDecimal("0.00"), sum);
  }

  @Test
  void testSelect() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SelectExtId")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    List<TestEntity> lists = testEntityEntityDao.select(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SelectExtId")), testEntities -> testEntities);
    assertFalse(lists.isEmpty());
    assertEquals(1, lists.size());
  }

  @Test
  void testSelectPaginatedWithLimitAndOffset() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SelectPaginatedExtIdWithPageSizeAndLimit")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    List<TestEntity> lists = testEntityEntityDao.select(DetachedCriteria.forClass(TestEntity.class)
            .add(Restrictions.eq("externalId", "SelectPaginatedExtIdWithPageSizeAndLimit")),
        testEntities -> testEntities, 1, 0);
    assertFalse(lists.isEmpty());
    assertEquals(1, lists.size());
  }

  @Test
  void testSelectPaginatedWithLimit() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SelectPaginatedExtIdWithPageSize")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    List<TestEntity> lists = testEntityEntityDao.select(DetachedCriteria.forClass(TestEntity.class)
            .add(Restrictions.eq("externalId", "SelectPaginatedExtIdWithPageSize")),
        testEntities -> testEntities, 1);
    assertFalse(lists.isEmpty());
    assertEquals(1, lists.size());
  }

  @Test
  void testSelectWithQuery() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SelectWithQuery")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    List<TestEntity> lists = testEntityEntityDao.select("select t from TestEntity t where t.externalId = :extId", ImmutableMap.of("extId", "SelectWithQuery"), testEntities -> testEntities);
    assertFalse(lists.isEmpty());
    assertEquals(1, lists.size());
  }

  @Test
  void testSelectSingle() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SelectSingle")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    Optional<TestEntity> entity = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SelectSingle")));
    assertTrue(entity.isPresent());
  }

  @Test
  void testUpdateQuery() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("UpdateQuery")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    var result = testEntityEntityDao.update("update TestEntity e set e.text = :txt where e.externalId= :extId",
        ImmutableMap.of("extId", "UpdateQuery", "txt", "Updated"));
    assertEquals(1, result);
  }

  @Test
  void testUpdateNativeQuery() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("UpdateQuery")
        .text("Some Text1")
        .build();
    testEntityEntityDao.save(testEntity);
    var result = testEntityEntityDao.updateNative("update test_entity set text = :txt where ext_id= :extId",
        ImmutableMap.of("extId", "UpdateQuery", "txt", "Updated"));
    assertEquals(1, result);
  }

  @Test
  void testUpdateInLock() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("UpdateInLock")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    var result = testEntityEntityDao.updateInLock(saved.get().getId(), fetchedEntity -> {
      fetchedEntity.ifPresent(e -> e.setText("UpdatedInLock"));
      return fetchedEntity;
    });
    assertTrue(result);
  }

  @Test
  void testUpdateInLockInvalid() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("UpdateInLockInvalid")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    var result = testEntityEntityDao.updateInLock(-99L, fetchedEntity -> {
      fetchedEntity.ifPresent(e -> e.setText("UpdatedInLock"));
      return fetchedEntity;
    });
    assertFalse(result);
  }

  @Test
  void testTransactionContextSave() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveTransactionContextSave")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("SaveInTransactionContext")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getTransactionContext(saved.get().getId())
        .save(testEntityEntityDao, e -> testEntity1)
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveInTransactionContext")));
    assertTrue(fetched.isPresent());
  }

  @Test
  void testTransactionContextUpdate() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("TransactionContextUpdate")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("InTransactionContextUpdate")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved1 = testEntityEntityDao.save(testEntity);
    assertTrue(saved1.isPresent());
    Optional<TestEntity> saved2 = testEntityEntityDao.save(testEntity1);
    assertTrue(saved2.isPresent());
    testEntityEntityDao.getTransactionContext(saved1.get().getId())
        .update(testEntityEntityDao, saved2.get().getId(), testEntity2 -> {
          var updateEntity = testEntity2.get();
          updateEntity.setText("Updated");
          return Optional.of(updateEntity);
        })
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "InTransactionContextUpdate")));
    assertTrue(fetched.isPresent());
    assertEquals("Updated", fetched.get().getText());
  }

  @Test
  void testTransactionContextUpdateQuery() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("TransactionContextUpdate")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("InTransactionContextUpdateQuery")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved1 = testEntityEntityDao.save(testEntity);
    assertTrue(saved1.isPresent());
    Optional<TestEntity> saved2 = testEntityEntityDao.save(testEntity1);
    assertTrue(saved2.isPresent());
    var updateParams = ImmutableMap.<String, Object>builder()
        .put("txt", "Updated")
        .put("id", saved2.get().getId()).build();
    testEntityEntityDao.getTransactionContext(saved1.get().getId())
        .update(testEntityEntityDao, "update TestEntity set text =: txt where id =: id", updateParams)
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "InTransactionContextUpdateQuery")));
    assertTrue(fetched.isPresent());
    assertEquals("Updated", fetched.get().getText());
  }

  @Test
  void testBatchTransactionContextSave() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveBatchTransactionContextSave")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("SaveInBatchTransactionContext")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getBatchTransactionContext(Collections.singletonList(saved.get().getId()))
        .save(testEntityEntityDao, e -> Collections.singletonList(testEntity1))
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveBatchTransactionContextSave")));
    assertTrue(fetched.isPresent());
  }

  @Test
  void testTransactionContextSaveWithLongSupplier() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveTransactionContextSaveWithLongSupplier")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("SaveInTransactionContextWithSupplier")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getTransactionContext(() -> saved.get().getId())
        .save(testEntityEntityDao, e -> testEntity1)
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveInTransactionContextWithSupplier")));
    assertTrue(fetched.isPresent());
  }

  @Test
  void testBatchTransactionContextSaveWithSupplier() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveBatchTransactionContextSaveWithSupplier")
        .text("Some Text1")
        .build();
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("SaveInBatchTransactionContextWithSupplier")
        .text("Some Text1")
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getBatchTransactionContext(() -> Collections.singletonList(saved.get().getId()))
        .save(testEntityEntityDao, e -> Collections.singletonList(testEntity1))
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveInBatchTransactionContextWithSupplier")));
    assertTrue(fetched.isPresent());
  }

  @Test
  void testSaveInTransaction() {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveInTransactionContext")
        .text("Some Text1")
        .build();
    TestEntity saved = testEntityEntityDao.saveTransactionContext(testEntity).execute();
    assertNotNull(saved);
  }

  @Test
  void testSaveInBatchTransaction() {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveInBatchTransactionContext")
        .text("Some Text1")
        .build();
    List<TestEntity> saved = testEntityEntityDao.saveBatchTransactionContext(Collections.singletonList(testEntity)).execute();
    assertNotNull(saved);
    assertEquals(1, saved.size());
  }

  @Test
  void testSaveInTransactionWithSupplier() {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveInTransactionContextWithSupplier")
        .text("Some Text1")
        .build();
    TestEntity saved = testEntityEntityDao.saveTransactionContext(() -> testEntity).execute();
    assertNotNull(saved);
  }

  @Test
  void testSaveInBatchTransactionWithSupplier() {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveInBatchTransactionContextWithSupplier")
        .text("Some Text1")
        .build();
    List<TestEntity> saved = testEntityEntityDao.saveBatchTransactionContext(() -> Collections.singletonList(testEntity)).execute();
    assertNotNull(saved);
    assertEquals(1, saved.size());
  }

  @Test
  void testTransactionContextSaveAll() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveTransactionContextSaveAll")
        .text("Some Text1")
        .build();

    List<TestEntity> tobeSaved = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      TestEntity testChildEntity = TestEntity.builder()
          .externalId("SaveTransactionContextSaveAllChild")
          .text("Some Text " + i)
          .build();
      tobeSaved.add(testChildEntity);
    }
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getTransactionContext(saved.get().getId())
        .saveAll(testEntityEntityDao, e -> tobeSaved)
        .execute();
    List<TestEntity> fetched = testEntityEntityDao.select(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveTransactionContextSaveAllChild")), e -> e);
    assertEquals(10, fetched.size());
  }

  @Test()
  void testTransactionContextFilter() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveTransactionContextFilter")
        .text("Some Text1")
        .amount(BigDecimal.ONE)
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    TestEntity testEntity1 = TestEntity.builder()
        .externalId("TransactionContextFilterTest")
        .text("Some Text1")
        .build();
    assertThrows(IllegalArgumentException.class, () -> {
      testEntityEntityDao.getTransactionContext(saved.get().getId())
          .filter(e-> e.getText().compareTo("Some Text") == 0)
          .save(testEntityEntityDao, e -> testEntity1)
          .execute();
    });
  }

  @Test()
  void testTransactionContextMutate() throws Exception {
    TestEntity testEntity = TestEntity.builder()
        .externalId("SaveTransactionContextMutate")
        .text("Some Text1")
        .amount(BigDecimal.ONE)
        .build();
    Optional<TestEntity> saved = testEntityEntityDao.save(testEntity);
    assertTrue(saved.isPresent());
    testEntityEntityDao.getTransactionContext(saved.get().getId())
        .mutate( parent -> parent.setText("Updated"))
        .execute();
    Optional<TestEntity> fetched = testEntityEntityDao.selectSingle(DetachedCriteria.forClass(TestEntity.class)
        .add(Restrictions.eq("externalId", "SaveTransactionContextMutate")), e -> e );
    assertTrue(fetched.isPresent());
    assertEquals("Updated", fetched.get().getText());
  }
}
