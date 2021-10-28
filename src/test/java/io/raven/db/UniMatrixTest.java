package io.raven.db;

import io.raven.db.entity.TestEntity;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniMatrixTest {

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
    var uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Collections.singletonList(TestEntity.class))
        .build();
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
        .entities(Collections.singletonList(TestEntity.class))
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
        .entities(Collections.singletonList(TestEntity.class))
        .build();
    SessionFactory sessionFactory = uniMatrix.getSessionFactory();
    assertTrue(sessionFactory.isOpen());
    uniMatrix.close();
    assertTrue(sessionFactory.isClosed());
    uniMatrix.close();
    assertTrue(sessionFactory.isClosed());
  }
}
