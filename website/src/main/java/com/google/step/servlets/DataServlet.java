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
    if (index != -1) {
      URL url = new URL("https://8080-5b0203f2-347a-4641-b84a-bbaedc8c3aec.us-central1.cloudshell.dev/predict");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("org_index", Integer.toString(index));
      BufferedReader in = new BufferedReader(
      new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer content = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
          content.append(inputLine);
      }
      in.close();
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(content);
    } else {
    response.sendRedirect("/upload.html");
}  }


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
        System.out.println(orgName + nextRecord[0]);
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
