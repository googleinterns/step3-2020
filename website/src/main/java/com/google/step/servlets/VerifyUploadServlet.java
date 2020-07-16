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
  private static String finalOrgTable = "orgTable";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      Map<String, List<OrganizationInfo>> comparisonMap = new HashMap<>();
      //Set up SQL proxy
      CloudSQLManager database = CloudSQLManager.setUp();
      //Get all current submissions to check
      ResultSet uploads = database.getFirstUploadOrg(orgsToCheck); 
      System.out.println("\nSelected submissions;\n");
      while (uploads.next()) {
        OrganizationInfo upload = OrganizationInfo.getSubmissionOrgFrom(uploads);
        comparisonMap.put("submission", Arrays.asList(upload));
        List<OrganizationInfo> orgs = new ArrayList<>();
        
        System.out.println("\nParsed submission from result set;\n");
        
        ResultSet rs = database.getPossibleComparisons(finalOrgTable, upload);
        System.out.println("\n Selected similar orgs;\n");
        while (rs.next()) { 
          orgs.add(OrganizationInfo.getSubmissionOrgFrom(rs));
          System.out.println("\n Added similar orgs to value list;\n");
        }
        comparisonMap.put("similar", orgs);
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
      System.out.println(request.getParameter("do") + " " + request.getParameter("id"));
      ResultSet submission = database.getFirstUploadOrg(orgsToCheck); 
      submission.next();
      database.deleteOrg(orgsToCheck, Integer.parseInt(request.getParameter("id")));    

      String decision = request.getParameter("do");
      if (decision.equals("approve")) {
        List<String> columns = Arrays.asList(
            "id INTEGER PRIMARY KEY", 
            "name TEXT NOT NULL", 
            "link TEXT NOT NULL", 
            "about TEXT NOT NULL",
            "class VARCHAR(255) NOT NULL");
        OrganizationInfo orgToUpload = OrganizationInfo.getSubmissionOrgFrom(submission);
        System.out.println("\nOrg parsed;\n");
        submission.close();
        int latestEntry = database.getLastEntryIndex(finalOrgTable);
        System.out.println("\n Selected latest index;\n");
        orgToUpload.setID(latestEntry + 1);
        PreparedStatement statement = database.buildInsertStatement(finalOrgTable, columns);
        System.out.println("\nInsert statement built;\n");
        orgToUpload.passInfoTo(statement);
        statement.executeBatch();
        System.out.println("\nStatement executed\n");
      }
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }
}
