package com.google.step.servlets;

import com.google.gson.Gson;
import com.opencsv.*;
import java.io.*;
import java.util.ArrayList; 
import java.util.List; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

@WebServlet("/fetch-orgs")
public class OrganizationServlet extends HttpServlet {

  private static class Organization {
    int index;
    String name;
    String statement;

    private Organization(int index, String name, String statement) {
      this.index = index;
      this.name = name;
      this.statement = statement;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String filename = "sample_data.csv";
    List<Organization> orgs = readCsv(filename);
    // Send the JSON as the response
    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(orgs));
  }

  private List<Organization> readCsv(String filename) {
    List<Organization> orgs = new ArrayList<>();
    try {
      ServletContext sc = this.getServletContext();
      InputStream is = sc.getResourceAsStream("/WEB-INF/" + filename);
      InputStreamReader isReader = new InputStreamReader(is); 
      CSVReader csvReader = new CSVReaderBuilder(isReader).withSkipLines(1).build();
      String[] nextRecord = new String[2]; 
      int index = 0;
      while ((nextRecord = csvReader.readNext()) != null) { 
        String name = nextRecord[0];
        String statement = nextRecord[1];
        Organization org = new Organization(index++, name, statement);
        orgs.add(org);
      } 
    } catch(Exception ex) {
      System.err.println(ex);
    }
    return orgs;
  }
}
