package com.google.step.servlets;

import com.google.gson.Gson;
import com.google.step.data.*;
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
@WebServlet("/download-ratings")
public class DownloadRatingServlet extends HttpServlet {

  private static class Rating {
    String id;
    String email;
    String rating;

    private Rating(String id, String email, String rating) {
      this.id = id;
      this.email = email;
      this.rating = rating;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      ResultSet result = database.get("ratings");

      response.setContentType("text/plain");
      response.setHeader("Content-disposition", "attachment; filename=database.json");
      OutputStream out = response.getOutputStream();
      Writer writer = new OutputStreamWriter(out, "UTF-8");

      List<Rating> ratings = new ArrayList<>();
      while (result.next()) {
        String id = result.getString("id");
        String email = result.getString("email");
        String rating = result.getString("rating");
        Rating entry = new Rating(id, email, rating);
        ratings.add(entry);   
      }
      Gson gson = new Gson();
      writer.write(gson.toJson(ratings));
      writer.close();
      database.tearDown();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

}
