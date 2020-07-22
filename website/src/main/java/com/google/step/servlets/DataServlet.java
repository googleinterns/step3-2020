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

import java.io.IOException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.step.data.OrganizationInfo;
import com.google.gson.Gson;
import com.google.step.data.*;
import com.opencsv.*;
import java.io.*;
import java.lang.Process.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
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
  private static String orgsWithClass = "g4npOrgs";
  private static String orgsToCheck = "submissionOrgs";
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      //Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      //Get all distinct classifications to develop tree
      ResultSet classes = database.getDistinct(orgsWithClass, Arrays.asList("class"), Arrays.asList("class IS NOT NULL ORDER BY class DESC"));
      Map<String, Set<String>> classTree = createClassificationTree(classes);
      // TreeSet<String> roots = new TreeSet(classTree.get("roots"));
      // printClassTree(classTree, roots, roots.first(), "" );
      //Send out info
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(classTree));
    } catch (SQLException ex) {
      System.err.println(ex);
    }
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      CSVReader file = getCSVReaderFrom(request);
      ProcessData dataProcessor = new ProcessData(file);
      Thread processData = new Thread(dataProcessor);
      processData.start();
      response.sendRedirect("/upload.html");
    } catch (FileUploadException ex) {
      System.out.println("Please dont");
    }
  }

  //Helper functions for processing new CSV files
  private int getLastEntryIndex(String tableName, CloudSQLManager database) {
    try {
      ResultSet maxIndexSet = database.getDistinct(tableName, Arrays.asList("MAX(id) AS id"), null); 
      maxIndexSet.next();
      int lastEntryIndex = maxIndexSet.getInt("id");
    return lastEntryIndex;
    } catch (SQLException ex) {
      System.err.println(ex);
      return 0;
    }
  }

  //helper functions to process uploads
  private class NLPService implements ClassHandler{
    private LanguageServiceClient service;
    NLPService() throws IOException {
      this.service = LanguageServiceClient.create();
    }

    @Override
    public ClassifyTextResponse classifyRequest(ClassifyTextRequest request) {
      try {
        return this.service.classifyText(request);
      } catch (Exception ex) {
        System.err.println(ex);
      } 
      return null;
    }
  }

  private void passSubmissionToStatement(HttpServletRequest request, PreparedStatement statement, int index, ClassHandler classHandler) throws IOException, SQLException {
    OrganizationInfo org = OrganizationInfo.getClassifiedOrgFrom(request, index, classHandler);
    org.passInfoTo(statement);
    statement.executeBatch();
  }

  private void passFileToStatement(CSVReader orgsFileReader, PreparedStatement statement, int index, ClassHandler classHandler) throws IOException, Exception, SQLException {
    String[] nextRecord = new String[2]; 
    while ((nextRecord = orgsFileReader.readNext()) != null) {
      try {
        //Create a classified Org from record
        OrganizationInfo org = OrganizationInfo.getClassifiedOrgFrom(nextRecord, index, classHandler);
        //If valid pass to SQL statement
        if (org != null) {
          org.passInfoTo(statement);
          index ++;
        }
      } catch(Exception ex) {
        System.err.println(ex);
      }
      try {
        //Throttles calls to NLP API
        Thread.sleep(100);
      } catch (InterruptedException ex) { 
        // Restore the interrupted status
        System.err.println(ex);
      } catch (DeadlineExceededException ex) {
        System.err.println(ex);
      }
    }
    orgsFileReader.close();
    statement.executeBatch();
  }


  //TODO: re upload orgs with fixed classifications to delete this
  private static Set<String> hardCodedRoots = new TreeSet<>(Arrays.asList(
      "Adult",
      "Arts & Entertainment",
      "Autos & Vehicles",
      "Beauty & Fitness",
      "Books & Literature",
      "Business & Industrial",
      "Computers & Electronics",
      "Finance",
      "Food & Drink",
      "Health",
      "Hobbies & Leisure",
      "Home & Garden",
      "Internet & Telecom",
      "Jobs & Education",
      "Law & Government",
      "News",
      "Online Communities",
      "People & Society",
      "Pets & Animals",
      "Real Estate",
      "Reference",
      "Science",
      "Sensitive Subjects",
      "Shopping",
      "Sports",
      "Travel"
    )
  );

  //Developing classification tree from already processed org info
  public static Map<String, Set<String>> createClassificationTree(ResultSet classes) throws SQLException {
    Map<String, Set<String>> classTree = new HashMap<>();
    classTree.put("roots", new TreeSet<String>());
      while (classes.next()) {
        Queue<String> parsed = Arrays.stream(classes.getString("class").split("/", 0))
            .collect(Collectors.toCollection(LinkedList::new));
        classTree.get("roots").add(parsed.peek());
        while (!parsed.isEmpty()) {
          String parent = parsed.remove();
          try {
            if (hardCodedRoots.contains(parsed.peek())) {
              if (!classTree.containsKey(parent)) {
                classTree.put(parent, new TreeSet<>());
              }
              break;
            } 
          } catch(NullPointerException ex){
            System.err.println();
          }
          List<String> child = (parsed.peek() != null) ? Arrays.asList(parsed.peek()) : new ArrayList<String>();
          if (classTree.containsKey(parent)) {
            classTree.get(parent).addAll(child);
          } else {
            classTree.put(parent, new TreeSet<>(child));
          }
        }
      }
      classes.close();
      return classTree;
  }

  //Process HTTP Request for CSV file
  private CSVReader getCSVReaderFrom(HttpServletRequest request) throws FileUploadException, IOException {
    //create file upload handler
    ServletFileUpload upload = new ServletFileUpload();
    //Search request for file
    FileItemIterator iter = upload.getItemIterator(request);
    while (iter.hasNext()) {
      FileItemStream item = iter.next();
      if (!item.isFormField()) {
        InputStreamReader fileStreamReader = new InputStreamReader(item.openStream()); 
        return new CSVReaderBuilder(fileStreamReader).withSkipLines(1).build();
      }
    }
    return null;
  }
}
