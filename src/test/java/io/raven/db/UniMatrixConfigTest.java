package io.raven.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UniMatrixConfigTest {


  @Test
  void testDefaultBuilder() {
    UniMatrixConfig factory = UniMatrixConfig.builder()
        .dialect("Test")
        .driverClass("Test")
        .user("sa")
        .build();
    assertNotNull(factory.getDialect());
    assertNotNull(factory.getDriverClass());
    assertNotNull(factory.getUser());
    assertEquals(0, factory.getMinPoolSize());
    assertEquals(4, factory.getMaxPoolSize());
    assertFalse(factory.isCreateSchema());
    assertFalse(factory.isShowSql());
    assertEquals(35000, factory.getIdleTimeout());
    assertEquals(45000, factory.getMaxAge());
    assertEquals("SELECT 1;", factory.getTestQuery());
    assertNull(factory.getPassword());
    assertNotNull(factory.toString());
    assertNotEquals(0, factory.hashCode());
  }

  @Test
  void testDefaultConstructor() {
    UniMatrixConfig factory = new UniMatrixConfig();
    assertNull(factory.getDriverClass());
    assertNull(factory.getDialect());
    assertNull(factory.getUser());
    assertEquals(0, factory.getMinPoolSize());
    assertEquals(4, factory.getMaxPoolSize());
    assertFalse(factory.isCreateSchema());
    assertFalse(factory.isShowSql());
    assertEquals(35000, factory.getIdleTimeout());
    assertEquals(45000, factory.getMaxAge());
    assertEquals("SELECT 1;", factory.getTestQuery());
    assertNull(factory.getPassword());
    assertNotNull(factory.toString());
    assertNotEquals(0, factory.hashCode());
  }

  @Test
  void testEquals() {
    UniMatrixConfig config = new UniMatrixConfig();
    UniMatrixConfig config1 = new UniMatrixConfig();
    assertEquals(config1, config);
  }

}
