package com.google.step.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that returns some example content. This file handles comment data */
@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String ratingString = request.getParameter("rating");
      String org = request.getParameter("id");
      int id = Integer.parseInt(org);
      int rating = 1;
      if (ratingString.equals("good")) {
        rating = 5;
      }
      // get email from userservice
      String userEmail = userService.getCurrentUser().getEmail();
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity ratingEntity = new Entity("Rating");
      ratingEntity.setProperty("email", userEmail);
      ratingEntity.setProperty("id", id);
      ratingEntity.setProperty("rating", rating);
      datastore.put(ratingEntity);
      response.setContentType("text/html");
      response.getWriter().println("Rated Successfully.");
    } else {
      response.setContentType("text/html");
      response.getWriter().println("Please login first.");
    }
  }

}
