package com.google.step.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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

  private static final String targetTable = "recommendations";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      // get email from userservice
      String userEmail = userService.getCurrentUser().getEmail();

      try {
        // Set up Proxy for handling SQL server
        CloudSQLManager database = CloudSQLManager.setUp();
        ResultSet rs = database.getRecommendationForUser(userEmail);
        int[] recs = new int[3];
        if (rs.next()) { 
          recs[0] = rs.getInt("rec1");
          recs[1] = rs.getInt("rec2");
          recs[2] = rs.getInt("rec3");
        }

        database.tearDown();
        // Send the JSON as the response
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(recs));
      } catch (SQLException ex) {
        System.err.println(ex);
      }
    } else {
      response.setContentType("text/html");
      response.getWriter().println("Please login first.");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      // read json with gson
      Map jsonData = readJson(request);
      
      // Column Information for database to be created
      List<String> columns = Arrays.asList(
          "email TEXT NOT NULL", 
          "rec1 INT NOT NULL", 
          "rec2 INT NOT NULL", 
          "rec3 INT NOT NULL");
      // Create table for recommendations
      database.createTable(targetTable, columns);

      PreparedStatement statement = database.buildInsertStatement(targetTable, columns);
      Map<String, List<Double>> people = (Map) jsonData.get("new");
      passToStatement(people, statement);

      database.tearDown();
      response.sendRedirect("/upload_sql.html");
    } catch (SQLException ex) {
      System.err.println(ex);
    } catch(Exception ex) {
      System.err.println(ex);
    }
  }

  private void passToStatement(Map<String, List<Double>> people, PreparedStatement statement) {
    try {
      // pass to SQL statement
      for (String key : people.keySet()) {
        System.out.println(key);
        statement.setString(1, key);
        List<Double> ids = people.get(key);
        statement.setInt(2, ids.get(0).intValue());
        statement.setInt(3, ids.get(1).intValue());
        statement.setInt(4, ids.get(2).intValue());
      }
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
