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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      ResultSet result = database.get("g4npOrgs");
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
      Gson gson = new Gson();
      writer.write(gson.toJson(orgs));
      writer.close();
      database.tearDown();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

}
