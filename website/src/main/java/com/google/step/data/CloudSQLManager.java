package com.google.step.data;

import com.google.step.data.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.*;
import java.sql.*;
import javax.sql.DataSource;
import java.util.*;
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

  public CloudSQLManager(Connection conn) {
    this.conn = conn;
  }

  public static CloudSQLManager setUp() throws SQLException {
    DataSource pool = createConnectionPool();
    Connection conn = pool.getConnection();
    return new CloudSQLManager(conn);
  }

  public void tearDown() throws SQLException {
    this.conn.close();
  }

  //Get contents of an entire table
  public ResultSet get(String tableName) throws SQLException{
    String query = String.format("SELECT * FROM %s;", tableName);
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  //Remove table from database
  public void drop(String tableName) {
    try {
      String query = String.format("DROP TABLE %s;", tableName);
      Statement stmt = this.conn.createStatement();
      stmt.execute(query);
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

  //Get distinct specifcied columns from table matching given clauses if any
  public ResultSet getDistinct(String tableName, List<String> columns, List<String> clauses) throws SQLException {
    String values = String.join(", ", columns);
    String where = (clauses == null) ? ";" : String.format(" WHERE %s;", String.join(" OR ", clauses)); 
    String query = String.format("SELECT DISTINCT %s FROM %s%s", values, tableName, where);
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  //Get all GN4P orgs similar to org
  public ResultSet getPossibleComparisons(String tableName, OrganizationInfo org) throws SQLException {
    List<String> attributes = Arrays.asList(
        String.format("name LIKE %s", "%" + org.getName() + "%"),
        String.format("link LIKE %s", "%" + org.getLink() + "%"),
        String.format("about LIKE %s", "%" + org.getAbout() + "%"));
    return this.getDistinct(tableName, Arrays.asList("*"), attributes);
  } 

  //create a prepared statement for insertion into a preexisting table
  public PreparedStatement buildInsertStatement(String tableName, List<String> columns) throws SQLException {
    List<String> placeHolders = new ArrayList<>();
    for (String column : columns) {
      placeHolders.add("?");
    }
    String columnNames = columns.stream()
        .map((name) -> name.substring(0, name.indexOf(" ")))
        .collect(Collectors.joining(","));
    String stmtText = String.format("INSERT INTO %s (%s) VALUES (%s);", 
        tableName, columnNames, String.join(",", placeHolders));
    return conn.prepareStatement(stmtText);
  }

  //Create new table with given columns
  public void createTable(String tableName, List<String> columns) throws SQLException {
    // Safely attempt to create the table schema.
    String stmt = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", 
        tableName, String.join(",", columns));
    try (PreparedStatement createTableStatement = this.conn.prepareStatement(stmt); ) {
      createTableStatement.execute();
    }
  }

  public ResultSet getOrgsWithNeighbors(String keyword, int offset) throws SQLException { 
    String similarTo = (!keyword.isEmpty()) ? 
        "WHERE (name LIKE '%" + keyword + "%' OR about LIKE '% " + keyword + "%' OR class LIKE '%" + keyword + "%')" 
        : "";
    String preliminaryQuery = String.format("(SELECT * FROM orgTable %sLIMIT " + offset + ", 10)", similarTo);    
    String query = String.join("\n","SELECT",
            "preliminary.*, "  ,
            "n1.name AS neighbor1_name, ",
            "n2.name AS neighbor2_name, ",
            "n3.name AS neighbor3_name, ",
            "n4.name AS neighbor4_name ",
        String.format("FROM (%s) AS preliminary", preliminaryQuery),
        "INNER JOIN orgTable AS n1" ,
            "ON preliminary.neighbor1 = n1.id",
        "INNER JOIN orgTable AS n2" ,
            "ON preliminary.neighbor2 = n2.id",
        "INNER JOIN orgTable AS n3" ,
            "ON preliminary.neighbor3 = n3.id",
        "INNER JOIN orgTable AS n4" ,
            "ON preliminary.neighbor4 = n4.id;");
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public ResultSet getOrgDetails(int id) throws SQLException {
    String preliminaryQuery = "SELECT * FROM orgTable WHERE id = " + id;
    String query = String.join("\n","SELECT",
            "preliminary.*, "  ,
            "n1.name AS neighbor1_name, ",
            "n2.name AS neighbor2_name, ",
            "n3.name AS neighbor3_name, ",
            "n4.name AS neighbor4_name ",
        String.format("FROM (%s) AS preliminary", preliminaryQuery),
        "INNER JOIN orgTable AS n1" ,
            "ON preliminary.neighbor1 = n1.id",
        "INNER JOIN orgTable AS n2" ,
            "ON preliminary.neighbor2 = n2.id",
        "INNER JOIN orgTable AS n3" ,
            "ON preliminary.neighbor3 = n3.id",
        "INNER JOIN orgTable AS n4" ,
            "ON preliminary.neighbor4 = n4.id;");
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public ResultSet getUserWithEmail(String email, int id) throws SQLException {
    String query = "SELECT * FROM ratings WHERE email = '" + email + "' AND id = " + id;
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public void executeStatement(String query) throws SQLException {
    // create the java mysql update preparedstatement
    Statement stmt = this.conn.createStatement();
    stmt.executeUpdate(query);
  }

}
