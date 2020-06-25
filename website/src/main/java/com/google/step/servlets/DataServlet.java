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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.step.data.OrganizationInfo;
import com.google.gson.Gson;
import com.opencsv.*;
import java.io.*;
import javax.servlet.annotation.WebServlet;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String filename = "../../../similarity/sample_data.csv";
    int index = searchCsvFor(request.getParameter("organization"), filename);
    response.sendRedirect("/results.html?index=" + Integer.toString(index) + "&name=" + request.getParameter("organization"));
  }



  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    OrganizationInfo submission = OrganizationInfo.createInstanceFrom(request);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.prepare(submission.getQueryForDuplicates()).asList(FetchOptions.Builder.withDefaults()).forEach((duplicate -> {
      submission.merge(duplicate);
      datastore.delete(duplicate.getKey());
    }));
    if (submission.isValid()) {
      datastore.put(submission.getEntity());
      response.sendRedirect("/index.html");
    } else {
    response.sendRedirect("/");
    }
  }

  private static int searchCsvFor(String orgName,String filename) {
    try {
      FileReader filereader = new FileReader(filename); 
      CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
      String[] nextRecord = new String[2]; 
      int index = 0;
      while ((nextRecord = csvReader.readNext()) != null) {
        if (nextRecord[0].equals(orgName)) {
          return index;
        } else {
          index++;
        }
      } 
    } catch(Exception ex) {
      System.err.println(ex);
    }
    return -1;
  }
}
