package io.raven.db;

import com.google.common.collect.Lists;
import io.raven.db.entity.TestEntity;
import io.raven.db.entity.TestRelatedEntity;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionManagerTest {

  @Test
  void beforeStartTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_init_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    var tm = TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(uniMatrix.getSessionFactory()).build();
    tm.beforeStart();
    assertEquals(TransactionStatus.ACTIVE, tm.getSession().getTransaction().getStatus());
    uniMatrix.close();
  }

  @Test
  void beforeStartExceptionTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_init_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    var tm = TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(uniMatrix.getSessionFactory()).build();
    uniMatrix.getSessionFactory().close();
    assertThrows(Exception.class, tm::beforeStart);
  }

  @Test
  void afterEndTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_init_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    var tm = TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(uniMatrix.getSessionFactory()).build();
    tm.afterEnd();
    assertNull(tm.getSession());
  }

  @Test
  void afterEndExceptionTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_init_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    var tm = TransactionManager.newTransaction()
        .readOnly(true)
        .sessionFactory(uniMatrix.getSessionFactory()).build();
    tm.beforeStart();
    tm.getSession().close();
    assertThrows(Exception.class, tm::afterEnd);
  }


}
