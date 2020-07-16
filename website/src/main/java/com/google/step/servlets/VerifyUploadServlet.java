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
      Map<OrganizationInfo, List<OrganizationInfo>> comparisonMap = new HashMap<>();
      //Set up SQL proxy
      CloudSQLManager database = CloudSQLManager.setUp();
      //Get all current submissions to check
      ResultSet uploads = database.get(orgsToCheck); 
      while (uploads.next()) {
        OrganizationInfo upload = OrganizationInfo.getSubmissionOrgFrom(uploads);
        List<OrganizationInfo> orgs = new ArrayList<>();
        ResultSet rs = database.getPossibleComparisons(gNP4Table, upload);
        while (rs.next()) { 
          orgs.add(OrganizationInfo.getResultOrgFrom(rs));
        }
        comparisonMap.put(upload, orgs);
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
}
