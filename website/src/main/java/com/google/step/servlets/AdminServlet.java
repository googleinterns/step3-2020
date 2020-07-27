package com.google.step.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.step.data.*;
import java.sql.*;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
  //Admin object contains all html for admin UI
  private final class AdminDashboard {
    String user = "Admin";
    String compareQuality = String.join("\n",
        "<h3 id=\"results-title\">Submission</h3>",
        "<div id=\"submission\"></div>",
        "<div id=\"comparisons\"></div>");
    String uploadCSV = String.join("\n",
        "<h3>Upload CSV</h3>",
        "<form id=\"uploadFormCSV\" action=\"/data\" method=\"POST\" enctype=\"multipart/form-data\">",
    	  "<label for=\"orgName\">Organization File:</label><br></br>",
        "<input type=\"file\" id=\"file\" name=\"file\" required><br></br>",
        "<input id=\"submit\" type=\"submit\"><br></br>",
        "</form>");
    String similarityWorkFlow = String.join("\n", "<h3>Text Similarity Workflow: </h3>",
        "<h3>1. Upload CSV to Cloud SQL Database with Category Tree</h4>",
        "<form id=\"uploadFormCSV\" action=\"/data\" method=\"POST\" enctype=\"multipart/form-data\">",
          "<label for=\"file\">Organization Files:</label><br></br>",
          "<input type=\"file\" id=\"file\" name=\"file\" required><br></br>",
          "<input id=\"submit-csv\" type=\"submit\"><br></br>",
        "</form>",
        "<br><br>",
        "<h4>2. Download Organizations as JSON</h4>",
        "<form id=\"download\" action=\"/download\" method=\"GET\">",
          "<label for=\"submit\">Download:</label><br></br>",
          "<input id=\"download-json\" type=\"submit\">",
        "</form>",
        "<br><br>",
        "<h4>3. Upload k-NN to Cloud SQL Database</h4>",
        "<form id=\"uploadProto\" action=\"/update\" method=\"POST\" enctype=\"multipart/form-data\">",
          "<label for=\"file\">k-NN Protos:</label><br></br>",
          "<input type=\"file\" id=\"knn-file\" name=\"file\" required><br></br>",
          "<input id=\"submit-proto\" type=\"submit\"><br></br>",
        "</form>",
        "<br><br>",
        "<h4>4. Add rating column</h4>",
        "<form id=\"uploadProto\" action=\"/rating\" method=\"POST\" enctype=\"multipart/form-data\">",
          "<input id=\"submit\" type=\"submit\"><br></br>",
        "</form>",
        "<br><br>");
    String filterWorkFlow = String.join("\n", 
        "<h3>Collaborative Filter Workflow: </h3>",
        "<p>Prerequisites: org id, knn neighbors, users and ratings</p>",
        "<p>returns: org id for users</p>",
        "<h4>1. Download Ratings as JSON</h4>",
        "<form id=\"download\" action=\"/download-ratings\" method=\"GET\">",
          "<label for=\"submit\">Download:</label><br></br>",
          "<input id=\"download-json\" type=\"submit\">",
        "</form>",
        "<br><br>",
        "<h4>2. Upload Recommendations to Cloud SQL Database</h4>",
        "<form id=\"uploadProto\" action=\"/recommend\" method=\"POST\" enctype=\"multipart/form-data\">",
          "<label for=\"file\">Recommendations:</label><br></br>",
          "<input type=\"file\" id=\"rec-file\" name=\"file\" required><br></br>",
          "<input id=\"submit-rec\" type=\"submit\"><br></br>",
        "</form>",
        "<br><br>",
        "<h4>Test Recommendations</h4>",
        "<form id=\"testRecSys\" action=\"/recommend\" method=\"GET\">",
          "<input id=\"submit-rec-test\" type=\"submit\"><br></br>",
        "</form>",
        "<br><br>",
        "<h4>Get Organizations in the database</h4>",
        "<button onclick=\"getOrgs();\">Get All</button><br><br>",
        "<input type=\"text\" id=\"keyword\">",
        "<button onclick=\"searchOrgs();\">Keyword Search</button>",
        "<ul id=\"existing-organizations\"></ul>");
    //TODO: Display SQL info
    String environmentInUse = "Testing Environment"; 
    int orgsTableSize; 
    public AdminDashboard(int size) {
      this.orgsTableSize = size;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/plain");
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    if (userService.isUserAdmin()) {
      try {
        CloudSQLManager database = CloudSQLManager.setUp();
        int orgsSize = database.getLastEntryIndex("g4npOrgs");
        response.getWriter().println(new Gson().toJson(new AdminDashboard(orgsSize)));
      } catch (SQLException ex) {
        System.err.println(ex);
      }
    } else {
      response.getWriter().println("{\"user\": \"Invalid User\"}");
    }
  }
}
