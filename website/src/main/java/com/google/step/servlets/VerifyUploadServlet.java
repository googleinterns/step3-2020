package com.google.step.servlets;

import java.io.IOException;

import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.step.data.OrganizationInfo;
import com.google.gson.Gson;
import com.google.step.data.*;
import java.io.*;
import java.lang.Process.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/verify")
public class VerifyUploadServlet extends HttpServlet {
  private static String orgsWithClass = "classifiedOrgs";
  private static String gNP4Table = "classifiedOrgs";
  private static String orgsToCheck = "submissionOrgs";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      Map<String, List<OrganizationInfo>> comparisonMap = new HashMap<>();
      //Set up SQL proxy
      CloudSQLManager database = CloudSQLManager.setUp();
      //Get all current submissions to check
      ResultSet uploads = database.get(orgsToCheck); 
      System.out.println("\nSelected submissions;\n");
      while (uploads.next()) {
        OrganizationInfo upload = OrganizationInfo.getSubmissionOrgFrom(uploads);
        String key = "Comparison_" + Integer.toString(upload.getID());
        List<OrganizationInfo> orgs = new ArrayList<>();
        orgs.add(upload);
        
        System.out.println("\nParsed submission from result set;\n");
        
        ResultSet rs = database.getPossibleComparisons(gNP4Table, upload);
        System.out.println("\n Selected similar orgs;\n");
        while (rs.next()) { 
          orgs.add(OrganizationInfo.getResultOrgFrom(rs));
          System.out.println("\n Added similar orgs to value list;\n");
        }
        comparisonMap.put(key, orgs);
      }

      // Send the JSON as the response
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(comparisonMap));
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
     try {
       //Set up SQL proxy
      CloudSQLManager database = CloudSQLManager.setUp();
      
     } catch (SQLException ex) {

     }
  }
}
