package io.raven.db;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hibernate.cfg.Environment.CURRENT_SESSION_CONTEXT_CLASS;
import static org.hibernate.cfg.Environment.DIALECT;
import static org.hibernate.cfg.Environment.DRIVER;
import static org.hibernate.cfg.Environment.HBM2DDL_AUTO;
import static org.hibernate.cfg.Environment.PASS;
import static org.hibernate.cfg.Environment.SHOW_SQL;
import static org.hibernate.cfg.Environment.URL;
import static org.hibernate.cfg.Environment.USER;

public class UniMatrix {

  @Getter
  private SessionFactory sessionFactory;

  private final UniMatrixConfig config;
  private final List<Class<?>> entities;

  @Builder
  public UniMatrix(UniMatrixConfig uniMatrixConfig, @Singular List<Class<?>> entities) {
    this.config = uniMatrixConfig;
    this.entities = entities;
    init();
  }

  private void init() {
    sessionFactory = getOrCreateSessionFactory();
  }

  public void close() {
    if(Objects.nonNull(sessionFactory) && !sessionFactory.isClosed()) {
      sessionFactory.close();
    }
  }


  public synchronized SessionFactory getOrCreateSessionFactory() {
    if(!Objects.isNull(sessionFactory)) {
      return sessionFactory;
    }
    final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
    Map<String, Object> settings = new HashMap<>();
    settings.put(HBM2DDL_AUTO, config.isCreateSchema() ? "create-drop" : "none");
    settings.put(SHOW_SQL, config.isShowSql());
    settings.put(CURRENT_SESSION_CONTEXT_CLASS, "managed");
    settings.put(DIALECT, config.getDialect());
    settings.put(URL, config.getUrl());
    if (!Strings.isNullOrEmpty(config.getUser())) {
      settings.put(USER, config.getUser());
    }
    if (!Strings.isNullOrEmpty(config.getPassword())) {
      settings.put(PASS, config.getPassword());
    }
    settings.put(DRIVER, config.getDriverClass());
    settings.put("hibernate.hikari.connectionTimeout", "20000");
    settings.put("hibernate.hikari.minimumIdle", String.valueOf(config.getMinPoolSize()));
    settings.put("hibernate.hikari.maximumPoolSize", String.valueOf(config.getMaxPoolSize()));
    settings.put("hibernate.hikari.idleTimeout", String.valueOf(config.getIdleTimeout()));
    settings.put("hibernate.hikari.connectionTestQuery", config.getTestQuery());
    settings.put("hibernate.hikari.autoCommit", "false");
    settings.put("hibernate.hikari.maxLifetime", String.valueOf(config.getMaxAge()));
    registryBuilder.applySettings(settings);
    StandardServiceRegistry registry = registryBuilder.build();
    MetadataSources sources = new MetadataSources(registry);
    entities.forEach(sources::addAnnotatedClass);
    Metadata metadata = sources.getMetadataBuilder().build();
    sessionFactory = metadata.getSessionFactoryBuilder().applyStatisticsSupport(false).build();
    return sessionFactory;
  }
}
