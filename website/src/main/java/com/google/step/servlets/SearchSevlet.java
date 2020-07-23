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
      
      ResultSet countResults = database.countOrgsWithNeighbors(keyword);
      int count = 0;
      if (countResults.next()) {
        count = countResults.getInt("total");
      }
      countResults.close();
      database.tearDown();

      // Send the JSON as the response
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      String json = gson.toJson(orgs);
      String combined = "[" + count + "," + json + "]";
      response.getWriter().println(combined);
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }
  
}
