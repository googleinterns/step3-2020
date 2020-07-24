package com.google.step.data;

import com.google.step.similarity.OrganizationsProtos.Organizations;
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
  private Connection conn;

  private static DataSource createConnectionPool(SecretsManager secrets) {
    String CLOUD_SQL_CONNECTION_NAME = secrets.getConnection();
    String DB_NAME  = secrets.getEnvironment();
    String DB_USER  = secrets.getUserName();
    String DB_PASS  = secrets.getPasscode();
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
    SecretsManager secrets = SecretsManager.getSecrets();
    DataSource pool = createConnectionPool(secrets);
    Connection conn = pool.getConnection();
    return new CloudSQLManager(conn);
  }

  public void tearDown() throws SQLException {
    this.conn.close();
  }

  // Get contents of an entire table
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
    String where = (clauses == null) ? ";" : String.format(" WHERE %s;", String.join("AND", clauses)); 
    String query = String.format("SELECT DISTINCT %s FROM %s%s", values, tableName, where);
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
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

    System.out.println(stmtText);
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

  public ResultSet countOrgsWithNeighbors(String keyword) throws SQLException {
    String similarTo = (!keyword.isEmpty()) ? 
        "WHERE (name LIKE '%" + keyword + "%' OR about LIKE '% " + keyword + "%' OR class LIKE '%" + keyword + "%')" 
        : "";
    String query = "SELECT COUNT(*) AS total FROM g4npOrgs " + similarTo + ";";
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public ResultSet getOrgsWithNeighbors(String keyword, int offset) throws SQLException { 
    String similarTo = (!keyword.isEmpty()) ? 
        "WHERE (name LIKE '%" + keyword + "%' OR about LIKE '% " + keyword + "%' OR class LIKE '%" + keyword + "%')" 
        : "";
    String preliminaryQuery = String.format("(SELECT * FROM g4npOrgs %s LIMIT " + offset + ", 10)", similarTo);    
    String query = String.join("\n","SELECT",
            "preliminary.*, "  ,
            "n1.name AS neighbor1_name, ",
            "n2.name AS neighbor2_name, ",
            "n3.name AS neighbor3_name, ",
            "n4.name AS neighbor4_name ",
        String.format("FROM (%s) AS preliminary", preliminaryQuery),
        "INNER JOIN g4npOrgs AS n1 ",
            "ON preliminary.neighbor1 = n1.id",
        "INNER JOIN g4npOrgs AS n2 ",
            "ON preliminary.neighbor2 = n2.id",
        "INNER JOIN g4npOrgs AS n3 ",
            "ON preliminary.neighbor3 = n3.id",
        "INNER JOIN g4npOrgs AS n4 ",
            "ON preliminary.neighbor4 = n4.id",
        "ORDER BY upvotes DESC;");
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public ResultSet getOrgDetails(int id) throws SQLException {
    String preliminaryQuery = "SELECT * FROM g4npOrgs WHERE id = " + id;
    String query = String.join("\n","SELECT",
            "preliminary.*, "  ,
            "n1.name AS neighbor1_name, ",
            "n2.name AS neighbor2_name, ",
            "n3.name AS neighbor3_name, ",
            "n4.name AS neighbor4_name ",
        String.format("FROM (%s) AS preliminary", preliminaryQuery),
        "INNER JOIN g4npOrgs AS n1 ",
            "ON preliminary.neighbor1 = n1.id",
        "INNER JOIN g4npOrgs AS n2 ",
            "ON preliminary.neighbor2 = n2.id",
        "INNER JOIN g4npOrgs AS n3 ",
            "ON preliminary.neighbor3 = n3.id",
        "INNER JOIN g4npOrgs AS n4 ",
            "ON preliminary.neighbor4 = n4.id;");
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public ResultSet getUserWithEmail(String email, int id) throws SQLException {
    String query = "SELECT rating FROM ratings WHERE email = '" + email + "' AND id = " + id;
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public void executeStatement(String query) throws SQLException {
    // create the java mysql update preparedstatement
    Statement stmt = this.conn.createStatement();
    stmt.executeUpdate(query);
  }

  public void alterTableNeighbor() throws SQLException {
    String sql = "ALTER TABLE g4npOrgs ADD (neighbor1 INTEGER, neighbor2 INTEGER, neighbor3 INTEGER, neighbor4 INTEGER);";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.execute();
  }

  public void alterTable() throws SQLException {
    String sql = "ALTER TABLE g4npOrgs ADD (upvotes INTEGER);";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.execute();
  }

  public ResultSet getUpvotes(int id) throws SQLException {
    String query = "SELECT upvotes FROM g4npOrgs WHERE id = " + id;
    Statement stmt = this.conn.createStatement();
    return stmt.executeQuery(query);
  }

  public void setUpvotes(int id, int votes) throws SQLException {
    String query = "UPDATE g4npOrgs SET upvotes = " + votes + " WHERE id = " + id;
    executeStatement(query);
  }

  public ResultSet getRecommendationForUser(String email) throws SQLException {
    String query = "SELECT * FROM recommendations WHERE email = '" + email + "';";
    Statement stmt = this.conn.createStatement();
    System.out.println(stmt + "\n\n\n");
    return stmt.executeQuery(query);
  }

  public void uploadKnn(List<Organizations.Organization> orgs) throws SQLException {
    String query = "UPDATE g4npOrgs SET neighbor1 = ?, neighbor2 = ?, neighbor3 = ?, neighbor4 = ? where id = ?;";
    // create the java mysql update preparedstatement
    PreparedStatement preparedStmt = this.conn.prepareStatement(query);
    int count = 0;
    for (Organizations.Organization org : orgs) {
      System.out.println(count++);
      List<Organizations.Organization.Neighbor> neighbors = org.getNeighborsList();
      preparedStmt.setInt(1, neighbors.get(0).getId());
      preparedStmt.setInt(2, neighbors.get(1).getId());
      preparedStmt.setInt(3, neighbors.get(2).getId());
      preparedStmt.setInt(4, neighbors.get(3).getId());
      preparedStmt.setInt(5, org.getId());
      // execute the java preparedstatement
      preparedStmt.executeUpdate();
    }
  }

  public void uploadRecommendations(Map<String, List<Double>> people) throws SQLException {
    String query = "INSERT INTO recommendations (email, rec1, rec2, rec3) VALUES (?, ?, ?, ?);";
    PreparedStatement statement = this.conn.prepareStatement(query);
    for (String key : people.keySet()) {
        System.out.println(key);
        statement.setString(1, key);
        List<Double> ids = people.get(key);
        statement.setInt(2, ids.get(0).intValue());
        statement.setInt(3, ids.get(1).intValue());
        statement.setInt(4, ids.get(2).intValue());
        System.out.println(statement);
      }
      statement.execute();
  }

}
