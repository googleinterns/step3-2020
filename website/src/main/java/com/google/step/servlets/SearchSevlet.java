package com.google.step.servlets;

import com.google.gson.Gson;
import com.google.step.data.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
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

/** Servelt that searches for a category and returns results */
@WebServlet("/sql")
public class SearchSevlet extends HttpServlet {
 
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keyword = request.getParameter("keyword");
    int page = Integer.parseInt(request.getParameter("page"));
    // displaying 10 orgs per page
    int offset = page * 10;
    try {
      CloudSQLManager database = CloudSQLManager.setUp();
      List<OrganizationInfo> orgs = new ArrayList<>();
      ResultSet rs = database.getOrgsWithNeighbors(keyword, offset);
      while (rs.next()) { 
        orgs.add(OrganizationInfo.getResultOrgFrom(rs));
      }
      rs.close();
      
      database.tearDown();

      // Send the JSON as the response
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(orgs));
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }


  //@Override
  // public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // DataSource pool = createConnectionPool();
    // int index = 0;
    // try {
      // createTable(pool);
      // Connection conn = pool.getConnection();
      // int batch = 500; // insert orgs in batches of 500
      // String stmtText = "INSERT INTO org (id, name, link, about) VALUES (?, ?, ?, ?)";
      // PreparedStatement statement = conn.prepareStatement(stmtText);
      ////Create a new file upload handler
      // ServletFileUpload upload = new ServletFileUpload();
      ////Parse the request
      // FileItemIterator iter = upload.getItemIterator(request);
      // while (iter.hasNext()) {
        // FileItemStream item = iter.next();
        // if (!item.isFormField()) {
          ////Process the input stream
          // InputStreamReader isReader = new InputStreamReader(item.openStream()); 
          // CSVReader csvReader = new CSVReaderBuilder(isReader).withSkipLines(1).build();
          ////insert to MySQL
          // String[] nextRecord = new String[2]; 
          // while ((nextRecord = csvReader.readNext()) != null) { 
            // String name = nextRecord[0];
            // String link = nextRecord[1];
            // String about = nextRecord[2];
            // statement.setInt(1, index);
            // statement.setString(2, name);
            // statement.setString(3, link);
            // statement.setString(4, about);
            // statement.addBatch();
            // if (index % batch == 0) {
              // statement.executeBatch();
            // }
            // index++;
          // } 
        // } else {
          // System.out.println(item.getName());
        // }
      // } 
      // statement.executeBatch();
      // conn.close();
    // } catch (FileUploadException ex) {
      // System.err.println(ex);
    // } catch (SQLException ex) {
      // System.err.println(ex);
    // } catch(CsvValidationException ex) {
      // System.err.println(ex);
    // }
    // response.sendRedirect("upload_sql.html");
  //}
}
