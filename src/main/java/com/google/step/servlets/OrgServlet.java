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


/** Servelt that searches for a category and returns results */
@WebServlet("/org")
public class OrgServlet extends HttpServlet {
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int id = Integer.parseInt(request.getParameter("id"));
    try {
      CloudSQLManager database = CloudSQLManager.setUp();
      List<OrganizationInfo> orgs = new ArrayList<>();
      ResultSet rs = database.getOrgDetails(id);
      while (rs.next()) { 
        String name = rs.getString("name");
        System.out.println(name);
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
 
}
