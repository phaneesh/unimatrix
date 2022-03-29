package io.raven.db;

import com.google.common.collect.Lists;
import io.raven.db.entity.TestEntity;
import io.raven.db.entity.TestRelatedEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public class AbstractDaoTest {

  protected UniMatrix uniMatrix;

  @BeforeEach
  void setup() {
    UniMatrixConfig uniMatrixConfig = UniMatrixConfig.builder()
        .createSchema(true)
        .showSql(true)
        .driverClass("org.h2.Driver")
        .dialect("org.hibernate.dialect.H2Dialect")
        .database("db_test")
        .url("jdbc:h2:mem:db_test")
        .build();
    uniMatrix = UniMatrix.builder()
        .uniMatrixConfig(uniMatrixConfig)
        .entities(Lists.newArrayList(TestEntity.class, TestRelatedEntity.class))
        .build();
  }

  @AfterEach
  void teardown() {
    uniMatrix.close();
  }

}
