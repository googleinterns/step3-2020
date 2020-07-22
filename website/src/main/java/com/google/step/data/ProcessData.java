package com.google.step.data;

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
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


public class ProcessData implements Runnable {
  private final CSVReader orgsNoClassification;
  private static String orgsWithClass = "g4npOrgs";
  private static String orgsToCheck = "submissionOrgs";

  public ProcessData(CSVReader file) {
    this.orgsNoClassification = file;
  }

  @Override
  public void run() {
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
      //Get file reader for orgs with no classifiation
      String targetTable = (orgsNoClassification != null) ? orgsWithClass : orgsToCheck;
      //Create table for orgs with classification
      database.createTable(targetTable, columns);
      //Classify each org from, file, and add to target table
      PreparedStatement statement = database.buildInsertStatement(targetTable, columns);  
      int startIndex = getLastEntryIndex(targetTable, database) + 1;
      NLPService service = new NLPService();
      if (orgsNoClassification != null) {
        passFileToStatement(orgsNoClassification, statement, startIndex, service);
      } else {
        //passSubmissionToStatement(request, statement, startIndex, service);
      }
      //Wrap up
      database.tearDown();
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