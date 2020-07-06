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
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    response.getWriter().println("<h1>Hello world!</h1>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      //Set up SQL connection
      CloudSQLManager database = CloudSQLManager.setUp();
      System.out.println("Database set up");
      String orgsWithClass = "organizationsTest2";
      //Create table for orgs with classification
      List<String> columns = Arrays.asList(
          "id INTEGER PRIMARY KEY", 
          "name VARCHAR(255) NOT NULL", 
          "link VARCHAR(255) NOT NULL", 
          "about VARCHAR(255) NOT NULL",
          "class VARCHAR(255) NOT NULL",
          "parentClass VARCHAR(255) NOT NULL");
      database.drop(orgsWithClass);
      database.createTable(orgsWithClass, columns);
      System.out.println("Target table created");

      //Get reader for orgs with no classifiation
      CSVReader orgsNoClassification = getCSVReaderFrom(request);
      System.out.println("File recieved from HTTP request");

      //Classify each org, and add to target table
      PreparedStatement statement = database.buildInsertStatement(orgsWithClass, columns);
      System.out.println("Insert statement built");
      String[] nextRecord = new String[2]; 
      int index = 0;
      while ((nextRecord = orgsNoClassification.readNext()) != null) {
        OrganizationInfo org = OrganizationInfo.getClassifiedOrgFrom(nextRecord, index);
        if (org != null) {
          org.passInfoTo(statement);
          statement.addBatch();
          index ++;
        }
      }
      System.out.println("Classifications added");
      statement.executeBatch();
      System.out.println("Insertion Executed");
      orgsNoClassification.close();

      //Get all distinct classes
      ResultSet classes = database.getDistinct(orgsWithClass, Arrays.asList("class"), null);
      System.out.println("Classifications selected");
      SortedSet<String> parents = new TreeSet<>();
      Map<String, Set<String>> classTree = new HashMap<>();
      while (classes.next()) {
        Queue<String> parsed = Arrays.stream(classes.getString("class").split("/", 0))
            .collect(Collectors.toCollection(LinkedList::new));
        parents.add(parsed.peek());
        while (!parsed.isEmpty()) {
          String parent = parsed.remove();
          List<String> child = (parsed.peek() != null) ? Arrays.asList(parsed.peek()) : new ArrayList<String>();
          if (classTree.containsKey(parent)) {
            classTree.get(parent).addAll(child);
          } else {
            classTree.put(parent, new HashSet<>(child));
          }
        }
      }
      classes.close();
      database.tearDown();
      
      while (!parents.isEmpty()) {
        printClassTree(classTree, parents, parents.first() , "");
      }
    } catch (SQLException ex) {
      System.err.println(ex);
    } catch(Exception ex) {
      System.err.println(ex);
    }
    
    response.sendRedirect("upload.html");
  }

  private void printClassTree(Map<String, Set<String>> classTree, Set<String> parents, String parent, String spacing) {
    try {
      System.out.println(spacing + parent);
      parents.remove(parent);
      if (classTree.get(parent).isEmpty()) {
        return;
      }
      classTree.get(parent).forEach(child -> printClassTree(classTree, parents, child, spacing + "     "));
    } catch(NullPointerException ex) {
      System.err.println(ex);
    }
  }

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

  // private long getLatestIndexFrom(DatastoreService datastore) {
  //   //Determine where to start index of new CSV
  //   List<Entity> lastEntry = datastore.prepare(
  //       new Query("Organization").addSort("index",SortDirection.DESCENDING))
  //       .asList(FetchOptions.Builder.withLimit(2));
  //   return (lastEntry.isEmpty()) ? 0 : (long) lastEntry.get(0).getProperty("index");
  // }
}
