package com.google.step.servlets;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.*;
import java.sql.*;
import java.util.ArrayList; 
import java.util.List; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/** Servelt that searches for a category and returns results */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

  private static class Organization {
    String id;
    String name;
    String about;

    private Organization(String id, String name, String about) {
      this.id = id;
      this.name = name;
      this.about = about;
    }
  }

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String CLOUD_SQL_CONNECTION_NAME =
      "mit-step-2020:us-west2:organizations";
  private static final String DB_USER = "root";
  // TODO: Fix this soon, this is pushed to a public repo
  private static final String DB_PASS = "jMMAak8xh7a7bCnq";
  private static final String DB_NAME = "orgs";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DataSource pool = createConnectionPool();
    try {
      Connection conn = pool.getConnection();
      String sql = "SELECT * FROM org";
      Statement statement = conn.createStatement();
      ResultSet result = statement.executeQuery(sql);

      response.setContentType("text/plain");
      response.setHeader("Content-disposition", "attachment; filename=database.json");
      OutputStream out = response.getOutputStream();
      Writer writer = new OutputStreamWriter(out, "UTF-8");

      List<Organization> orgs = new ArrayList<>();
      while (result.next()) {
        String id = result.getString("id");
        String name = result.getString("name");
        String about = result.getString("about");
        Organization org = new Organization(id, name, about);
        orgs.add(org);   
      }
      statement.close();
      Gson gson = new Gson();
      writer.write(gson.toJson(orgs));
      writer.close();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

  private DataSource createConnectionPool() {
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

}
