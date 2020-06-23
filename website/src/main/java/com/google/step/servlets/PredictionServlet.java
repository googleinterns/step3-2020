package com.google.step.servlets;

import com.google.gson.Gson;
import com.google.step.similarity.OrganizationsProtos.Organizations;
import com.opencsv.*;
import java.io.*;
import java.lang.ClassLoader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

@WebServlet("/predict")
public class PredictionServlet extends HttpServlet {

  private static class Organization {
    int index;
    String name;

    private Organization(int index, String name) {
      this.index = index;
      this.name = name;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int index = Integer.parseInt(request.getParameter("org_index"));
    List<Organizations.Organization> orgs = readProtobuf("neighbors.txt");

    String filename = "sample_data.csv";
    List<Organization> org_details = readCsv(filename);
    List<Organization> neighbors = getNeighbors(orgs, org_details, index);

    // Send the JSON as the response
    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(neighbors));
  }

  // Read the existing organizations
  private List<Organizations.Organization> readProtobuf(String filename) throws IOException {
    ServletContext sc = this.getServletContext();
    InputStream is = sc.getResourceAsStream("/WEB-INF/" + filename);
    Organizations orgs = Organizations.parseFrom(is);
    return orgs.getOrgsList();
  }

  private static List<Organization> getNeighbors(List<Organizations.Organization> orgs, List<Organization> org_details, int index) {
    Organizations.Organization org = orgs.get(index);
    List<Organizations.Organization.Neighbor> neighbors = org.getNeighborsList();
    List<Organization> similar_orgs = new ArrayList<>();
    similar_orgs.add(org_details.get(index));
    for (Organizations.Organization.Neighbor n : neighbors) {
      similar_orgs.add(org_details.get(n.getId()));
    }
    return similar_orgs;
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
        Organization org = new Organization(index++, name);
        orgs.add(org);
      } 
    } catch(Exception ex) {
      System.err.println(ex);
    }
    return orgs;
  }
}
