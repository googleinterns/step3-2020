package com.google.step.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.step.data.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that returns some example content. This file handles comment data */
@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: run this once 
    // try {
    //   CloudSQLManager database = CloudSQLManager.setUp();
    //   database.alterTable();
    //   database.tearDown();
    // } catch (SQLException ex) {
    //   System.err.println(ex);
    // }

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String ratingString = request.getParameter("rating");
      String org = request.getParameter("id");
      int id = Integer.parseInt(org);
      int rating = 1;
      if (ratingString.equals("good")) {
        rating = 2;
      }
      // get email from userservice
      String userEmail = userService.getCurrentUser().getEmail();
      
      try {
        // Set up Proxy for handling SQL server
        CloudSQLManager database = CloudSQLManager.setUp();
        // Column Information for database to be created
        List<String> columns = Arrays.asList(
            "email VARCHAR(255) NOT NULL", 
            "id INT NOT NULL", 
            "rating INT NOT NULL");
        // Create table for user ratings
        database.createTable("ratings", columns);
        
        String query = "INSERT INTO ratings (email, id, rating) VALUES ('" + userEmail + "', " + id + ", " + rating + ");";
        // query user email
        ResultSet rs = database.getUserWithEmail(userEmail, id);
        if (rs.next()) { 
          // the user has already rated before
          int prevRating = rs.getInt("rating");
          if (prevRating == rating) {
            database.tearDown();
            response.setContentType("text/html");
            response.getWriter().println("Already given the same rating before.");
            return;
          }
          query = "UPDATE ratings SET rating = " + rating + " WHERE email = '" + userEmail + "' AND id = " + id;
        } 
        database.executeStatement(query);

        // create upvotes column in database (only needed once)
        // database.alterTable();
        // increment upvote count
        
        ResultSet upvotes = database.getUpvotes(id);
        if (upvotes.next()) {
          int upvoteCount = upvotes.getInt("upvotes");
          if (ratingString.equals("good")) {
            database.setUpvotes(id, upvoteCount + 1);
          } else {
            database.setUpvotes(id, upvoteCount - 1);
          }
        } else {
          System.err.println("No upvote entry for id: " + id);
        }
        
        database.tearDown();
      } catch (SQLException ex) {
        System.err.println(ex);
      } catch(Exception ex) {
        System.err.println(ex);
      }
      
      response.setContentType("text/html");
      response.getWriter().println("Rated Successfully.");
    } else {
      response.setContentType("text/html");
      response.getWriter().println("Please login first.");
    }
  }

}
