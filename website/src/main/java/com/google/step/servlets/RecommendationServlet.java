package com.google.step.servlets;

import com.google.gson.Gson;
import com.google.step.data.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


@WebServlet("/recommend")
public class RecommendationServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      // read json with gson
      Map jsonData = readJson(request);
      Map people = (Map) jsonData.get("previous");

      for (Object obj : people.values()) {
        for (Object id : (ArrayList) obj) {
          System.out.println("id: " + id); 
        }
        
      }
        


      String targetTable = "recommendations";
      // Column Information for database to be created
      List<String> columns = Arrays.asList(
          "email TEXT NOT NULL", 
          "rec1 INT NOT NULL", 
          "rec2 INT NOT NULL", 
          "rec3 INT NOT NULL");
      // Create table for recommendations
      database.createTable(targetTable, columns);
      PreparedStatement statement = database.buildInsertStatement(targetTable, columns);
      
      database.tearDown();
      response.sendRedirect("/upload_sql.html");
    } catch (SQLException ex) {
      System.err.println(ex);
    } catch(Exception ex) {
      System.err.println(ex);
    }
  }

  private void passFileToStatement(Map data, PreparedStatement statement) {
    try {
      // TODO: pass to SQL statement
      // statement.setInt(1, this.id);
      // statement.setString(2, this.name);
      // statement.setString(3, this.link);
      // statement.setString(4, this.about);
      // String classPath = String.join("/", this.classification);
      // statement.setString(5, classPath);
      // statement.addBatch();

      statement.executeUpdate();
    } catch(Exception ex) {
      System.err.println(ex);
    }
  }

  // Process HTTP Request for JSON file
  private Map readJson(HttpServletRequest request) throws FileUploadException, IOException {
    // create file upload handler
    ServletFileUpload upload = new ServletFileUpload();
    // Search request for file
    FileItemIterator iter = upload.getItemIterator(request);
    while (iter.hasNext()) {
      FileItemStream item = iter.next();
      if (!item.isFormField()) {

        InputStreamReader inStreamReader = new InputStreamReader(item.openStream()); 
        Gson gson = new Gson();

        return gson.fromJson(inStreamReader, Map.class);
      }
    }
    return null;
  }

}
