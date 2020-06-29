// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.step.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.step.data.OrganizationInfo;
import com.opencsv.*;
import java.io.*;
import java.lang.Process.*;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    response.getWriter().println("<h1>Hello world!</h1>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    //Determine where to start index of new CSV
    List<Entity> lastEntry = datastore.prepare(
      new Query("Organization").addSort("index",SortDirection.DESCENDING))
      .asList(FetchOptions.Builder.withLimit(2));
    long lastIndex = (lastEntry.isEmpty() )? 0 : (long) lastEntry.get(0).getProperty("index");
    // Create a new file upload handler
    ServletFileUpload upload = new ServletFileUpload();
    try{
      // Parse the request
      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if (!item.isFormField()) {
          // Process the input stream
          InputStreamReader isReader = new InputStreamReader(item.openStream()); 
          OrganizationInfo.getOrganizationsFrom(
              new CSVReaderBuilder(isReader).withSkipLines(1).build(),lastIndex)
              .stream()
              .forEach(org -> datastore.put(org.getEntity()));
        } else {
          System.out.println(item.getName());
        }
      } 
    } catch (FileUploadException ex) {
      System.err.println(ex);
    }
    response.sendRedirect("upload.html");
  }
}
