package com.google.step.servlets;

import com.google.step.similarity.OrganizationsProtos.Organizations;
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
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/** Servelt that uploads the knn binary files to Cloud SQL */
@WebServlet("/update")
public class SimilarityServlet extends HttpServlet {

  // Saving credentials in environment variables is convenient, but not secure - consider a more
  // secure solution such as https://cloud.google.com/kms/ to help keep secrets safe.
  private static final String CLOUD_SQL_CONNECTION_NAME =
      "mit-step-2020:us-west2:organizations";
  private static final String DB_USER = "root";
  // TODO: Fix this soon, this is pushed to a public repo
  private static final String DB_PASS = "";
  private static final String DB_NAME = "orgs";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DataSource pool = createConnectionPool();
    try {
      Connection conn = pool.getConnection();
      // TODO: only need to alter the table once
      alterTable(conn);
      String query = "UPDATE orgTable SET neighbor1 = ?, neighbor2 = ?, neighbor3 = ?, neighbor4 = ? where id = ?;";
      // create the java mysql update preparedstatement
      PreparedStatement preparedStmt = conn.prepareStatement(query);
      List<Organizations.Organization> orgs = readProtobuf(request);
      for (Organizations.Organization org : orgs) {
        List<Organizations.Organization.Neighbor> neighbors = org.getNeighborsList();
        preparedStmt.setInt(1, neighbors.get(0).getId());
        preparedStmt.setInt(2, neighbors.get(1).getId());
        preparedStmt.setInt(3, neighbors.get(2).getId());
        preparedStmt.setInt(4, neighbors.get(3).getId());
        preparedStmt.setInt(5, org.getId());
        // execute the java preparedstatement
        preparedStmt.executeUpdate();
      }
      
      conn.close();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
    response.sendRedirect("upload_sql.html");
  }

  private void alterTable(Connection conn) throws SQLException {
    String sql = "ALTER TABLE orgTable ADD (neighbor1 INTEGER, neighbor2 INTEGER, neighbor3 INTEGER, neighbor4 INTEGER);";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.execute();
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

  // Read the existing organizations
  private List<Organizations.Organization> readProtobuf(HttpServletRequest request) throws IOException {
    List<Organizations.Organization> orgList = new ArrayList<>();
    try {
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload();
      // Parse the request
      FileItemIterator iter = upload.getItemIterator(request);
      if (iter.hasNext()) {
        FileItemStream item = iter.next();
        InputStream is = item.openStream();
        Organizations orgs = Organizations.parseFrom(is);
        orgList = orgs.getOrgsList();
      } else {
        System.err.println("no file uploaded");
      }
    } catch (FileUploadException ex) {
      System.err.println(ex);
    }
    return orgList;
  }

}
