package com.google.step.servlets;

import com.google.step.data.*;
import java.io.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;

@WebServlet("/delete")
public class DeleteDataServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      CloudSQLManager database = CloudSQLManager.setUp();
      //Create table for orgs with classification
      database.drop("orgTable");
      database.tearDown();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
    response.sendRedirect("/upload.html");
  }
}
