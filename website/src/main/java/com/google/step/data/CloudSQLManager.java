package com.google.step.data;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.*;
import java.sql.*;
import javax.sql.DataSource;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class CloudSQLManager {
  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String CLOUD_SQL_CONNECTION_NAME =
      "mit-step-2020:us-west2:organizations";
  private static final String DB_USER = "root";
  // TODO: Fix this soon, this is pushed to a public repo
  private static final String DB_PASS = "jMMAak8xh7a7bCnq";
  private static final String DB_NAME = "orgs";
  private Connection conn;

  private CloudSQLManager(Connection conn) {
    this.conn = conn;
  }

  private static DataSource createConnectionPool() {
    // The configuration object specifies behaviors for the connection pool.
    HikariConfig config = new HikariConfig();

    // Configure which instance and what database user to connect with.
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", DB_NAME));
    config.setUsername(DB_USER); // e.g. "root", "postgres"
    config.setPassword(DB_PASS); // e.g. "my-password"

    // For Java users, the Cloud SQL JDBC Socket Factory can provide authenticated connections.
    // See https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory for details.
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_CONNECTION_NAME);

    // ... Specify additional connection properties here.
    // ...

    // Initialize the connection pool using the configuration object.
    DataSource pool = new HikariDataSource(config);
    return pool;
  }

  public static CloudSQLManager setUp() throws SQLException {
    DataSource pool = createConnectionPool();
    Connection conn = pool.getConnection();
    return new CloudSQLManager(conn);
  }

  public void tearDown() throws SQLException {
    this.conn.close();
  }

  public ResultSet get(String tableName) throws SQLException{
    String query = String.format("SELECT * FROM %s;", tableName);
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public PreparedStatement buildInsertStatement(String tableName, List<String> columns) throws SQLException {
    List<String> placeHolders = new ArrayList<>();
    for (String column : columns) {
      placeHolders.add("?");
    }
    String columnNames = columns.stream()
        .map((name) -> name.substring(0, name.indexOf(" ")))
        .collect(Collectors.joining(","));
    String stmtText = String.format("INSERT INTO %s (%s) VALUES (%s);", 
        tableName, columnNames, String.join("placeHolders", ","));
    return conn.prepareStatement(stmtText);
  }

   public void createTable(String tableName, List<String> columns) throws SQLException {
    // Safely attempt to create the table schema.
    String stmt = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", 
        tableName, String.join(",", columns));
    try (PreparedStatement createTableStatement = this.conn.prepareStatement(stmt); ) {
      createTableStatement.execute();
    }
  }

}