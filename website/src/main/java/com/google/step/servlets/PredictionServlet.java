package com.google.step.servlets;

import com.google.gson.Gson;
import com.google.step.similarity.OrganizationsProtos.Organizations;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/predict")
public class PredictionServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int index = Integer.parseInt(request.getParameter("org_index"));
    String path = "../../../../../../../../neighbors";
    List<Organizations.Organization> orgs = readProtobuf(path + ".txt");
    List<String> neighbors = getNeighbors(orgs, index);
    // Send the JSON as the response
    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(neighbors));
  }

  private static List<Organizations.Organization> readProtobuf(String filename) throws IOException {
    // Read the existing organizations
    Organizations orgs = Organizations.parseFrom(new FileInputStream(filename));
    return orgs.getOrgsList();
  }

  private static List<String> getNeighbors(List<Organizations.Organization> orgs, int index) {
    Organizations.Organization org = orgs.get(index);
    List<Organizations.Organization.Neighbor> neighbors = org.getNeighborsList();
    List<String> names = new ArrayList<>();
    for (Organizations.Organization.Neighbor n : neighbors) {
      names.add(n.getName());
    }
    return names;
  }

}
