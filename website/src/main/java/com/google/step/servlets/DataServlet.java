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
  private static String orgsWithClass = "orgTable";
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      //Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      //Get all distinct classifications to develop tree
      ResultSet classes = database.getDistinct(orgsWithClass, Arrays.asList("class"), null);
      Map<String, Set<String>> classTree = createClassificationTree(classes);
      //Send out info
      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      Gson gson = new Gson();
      response.getWriter().println(gson.toJson(classTree));
    } catch (SQLException ex) {
      System.err.println(ex);
    }
    response.setContentType("text/html");
    response.getWriter().println("<p>Didn't Work</p>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Column Information for database to be created
    List<String> columns = Arrays.asList(
        "id INTEGER PRIMARY KEY", 
        "name TEXT NOT NULL", 
        "link TEXT NOT NULL", 
        "about TEXT NOT NULL",
        "class VARCHAR(255) NOT NULL");

    try {
      //Set up Proxy for handling SQL server
      CloudSQLManager database = CloudSQLManager.setUp();
      //Create table for orgs with classification
      database.createTable(orgsWithClass, columns);
      //Get file reader for orgs with no classifiation
      CSVReader orgsNoClassification = getCSVReaderFrom(request);
      //Classify each org from, file, and add to target table
      PreparedStatement statement = database.buildInsertStatement(orgsWithClass, columns);
      int startIndex = getLastEntryIndex(orgsWithClass, database) + 1;
      passFileToStatement(orgsNoClassification, statement, startIndex);
      //Wrap up
      database.tearDown();
      response.sendRedirect("upload.html");
    } catch (SQLException ex) {
      System.err.println(ex);
    } catch(Exception ex) {
      System.err.println(ex);
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

  private void passFileToStatement(CSVReader orgsFileReader, PreparedStatement statement, int index) throws IOException, Exception, SQLException {
    String[] nextRecord = new String[2]; 
    int batchAmount = 500;
    while ((nextRecord = orgsFileReader.readNext()) != null) {
      OrganizationInfo org = OrganizationInfo.getClassifiedOrgFrom(nextRecord, index);
      if (org != null) {
        org.passInfoTo(statement);
        statement.addBatch();
        index ++;
      }
      if (index % batchAmount == 0){
        statement.executeBatch();
      }
    }
    orgsFileReader.close();
    statement.executeBatch();
  }

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
          List<String> child = (parsed.peek() != null) ? Arrays.asList(parsed.peek()) : new ArrayList<String>();
          if (classTree.containsKey(parent)) {
            classTree.get(parent).addAll(child);
          } else {
            classTree.put(parent, new HashSet<>(child));
          }
        }
      }
      classes.close();
      return classTree;
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
