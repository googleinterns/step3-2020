package com.google.step.servlets;

import com.google.step.data.*;
import com.google.step.similarity.OrganizationsProtos.Organizations;
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

/** Servelt that uploads the knn binary files to Cloud SQL */
@WebServlet("/update")
public class SimilarityServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      // Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();

      // TODO: only need to alter once
      database.alterTableNeighbor();

      List<Organizations.Organization> orgs = readProtobuf(request);
      database.uploadKnn(orgs);
      database.tearDown();
    } catch (SQLException ex) {
      System.err.println(ex);
    }
    response.sendRedirect("upload_sql.html");
  }

  // Read the existing organizations
  private List<Organizations.Organization> readProtobuf(HttpServletRequest request) throws IOException {
    List<Organizations.Organization> orgList = new ArrayList<>();
    try {
      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload();
      // Parse the request
      FileItemIterator iter = upload.getItemIterator(request);
      if (iter.hasNext()) {
        FileItemStream item = iter.next();
        InputStream is = item.openStream();
        System.out.println("processing upload");
        Organizations orgs = Organizations.parseFrom(is);
        orgList = orgs.getOrgsList();
      } else {
        System.err.println("no file uploaded");
      }
    } catch (FileUploadException ex) {
      System.err.println(ex);
    }
    return orgList;
  }

}
