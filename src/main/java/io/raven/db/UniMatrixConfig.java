package io.raven.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Data
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
public class UniMatrixConfig {

  private String url;

  private String database;

  private String driverClass;

  private String user;

  private String password;

  private String dialect;

  private boolean createSchema;

  @Builder.Default
  private boolean showSql = false;

  @Builder.Default
  private int maxPoolSize = 4;

  @Builder.Default
  private int minPoolSize = 2;

  @Builder.Default
  private int idleTimeout = 60000;

  @Builder.Default
  private String testQuery = "SELECT 1;";

  @Builder.Default
  private int maxAge = 55000;

}
