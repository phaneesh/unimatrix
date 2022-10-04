package io.raven.db;

import com.google.common.collect.Lists;
import io.raven.db.entity.TestEntity;
import io.raven.db.entity.TestRelatedEntity;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniMatrixTest {

  @Test
  void initTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_init_test")
        .build();
    var uniMatrix =
        UniMatrix.builder()
            .uniMatrixConfig(uniMatrixConfig)
            .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
            .build();
    assertNotNull(uniMatrix);
  }

  @Test
  void getOrCreateSessionFactoryTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_session_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    SessionFactory sessionFactory = uniMatrix.getOrCreateSessionFactory();
    assertNotNull(sessionFactory);
    uniMatrix.close();
  }

  @Test
  void closeTest() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_close_test")
        .build();
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
    SessionFactory sessionFactory = uniMatrix.getSessionFactory();
    assertTrue(sessionFactory.isOpen());
    uniMatrix.close();
    assertTrue(sessionFactory.isClosed());
    uniMatrix.close();
    assertTrue(sessionFactory.isClosed());
  }
}
