package com.google.step.servlets;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/** Servelt that searches for a category and returns results */
@WebServlet("/sql")
public class SearchSevlet extends HttpServlet {

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
      Statement stmt = conn.createStatement();
      String sql = "SELECT id, name, link, about FROM org";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String link = rs.getString("link");
        String about = rs.getString("about");

        System.out.print("ID: " + id);
        System.out.print(", name: " + name);
        System.out.print(", link: " + link);
        System.out.println(", about: " + about);
      }
      rs.close();
      conn.close();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DataSource pool = createConnectionPool();
    try {
      createTable(pool);
      Connection conn = pool.getConnection();
      int batch = 20; // insert orgs in batches of 20
      String stmtText = "INSERT INTO org (id, name, link, about) VALUES (?, ?, ?, ?)";
      PreparedStatement statement = conn.prepareStatement(stmtText);
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload();
      // Parse the request
      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if (!item.isFormField()) {
          // Process the input stream
          InputStreamReader isReader = new InputStreamReader(item.openStream()); 
          CSVReader csvReader = new CSVReaderBuilder(isReader).withSkipLines(1).build();
          // insert to MySQL
          String[] nextRecord = new String[2]; 
          int index = 0;
          while ((nextRecord = csvReader.readNext()) != null) { 
            String name = nextRecord[0];
            String link = nextRecord[1];
            String about = nextRecord[2];
            statement.setInt(1, index);
            statement.setString(2, name);
            statement.setString(3, link);
            statement.setString(4, about);
            statement.addBatch();
            if (index % batch == 0) {
              statement.executeBatch();
            }
            index++;
          } 
        } else {
          System.out.println(item.getName());
        }
      } 
      statement.executeBatch();
      conn.close();
    } catch (FileUploadException ex) {
      System.err.println(ex);
    } catch (SQLException ex) {
      System.err.println(ex);
    } catch(CsvValidationException ex) {
      System.err.println(ex);
    }
    response.sendRedirect("upload_sql.html");
  }

  private void createTable(DataSource pool) throws SQLException {
    // Safely attempt to create the table schema.
    try (Connection conn = pool.getConnection()) {
      String stmt = "CREATE TABLE IF NOT EXISTS org (id INTEGER PRIMARY KEY, name VARCHAR(255) NOT NULL, link VARCHAR(255) NOT NULL, about VARCHAR(255) NOT NULL);";
      try (PreparedStatement createTableStatement = conn.prepareStatement(stmt); ) {
        createTableStatement.execute();
      }
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
