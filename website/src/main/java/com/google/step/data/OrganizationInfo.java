package com.google.step.data;

import com.google.api.gax.rpc.ResourceExhaustedException;
import com.google.apphosting.api.DeadlineExceededException;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public final class OrganizationInfo {
  private final int id;
  private final String name;
  private final String link;
  private final String about;
  private final List<String> classification;
  private final String neighbor1;
  private final String neighbor2;
  private final String neighbor3;
  private final String neighbor4;
  private final Integer neighbor1_id;
  private final Integer neighbor2_id;
  private final Integer neighbor3_id;
  private final Integer neighbor4_id;

  //Constructor for CSVs to classification table waiting for KNN neighbors
  private OrganizationInfo(int id, String name, String link, String about, List<String> classification) {
    this.id = id;
    this.name = name;
    this.link = link;
    this.about = about;
    this.classification = classification;
    this.neighbor1 = null;
    this.neighbor1_id = null;
    this.neighbor2 = null;
    this.neighbor2_id = null;
    this.neighbor3 = null;
    this.neighbor3_id = null;
    this.neighbor4 = null;
    this.neighbor4_id = null;
  }

  //Constructor for SQL records to front end results
  private OrganizationInfo(int id, String name, String link, String about, 
        String neighbor1, String neighbor2, String neighbor3, String neighbor4,
        int neighbor1ID, int neighbor2ID, int neighbor3ID, int neighbor4ID) {
    this.id = id;
    this.name = name;
    this.link = link;
    this.about = about;
    this.neighbor1 = neighbor1;
    this.neighbor1_id = neighbor1ID;
    this.neighbor2 = neighbor2;
    this.neighbor2_id = neighbor2ID;
    this.neighbor3 = neighbor3;
    this.neighbor3_id = neighbor3ID;
    this.neighbor4 = neighbor4;
    this.neighbor4_id = neighbor4ID;
    this.classification = null;
  }


  //Develop org CSV record
  public static OrganizationInfo getClassifiedOrgFrom(String[] record, int index, ClassHandler classHandler) {
    String name = record[0];
    String link = record[1];
    String about = record[2];
    String sectionToClassify = name + " " + about;
    if (sectionToClassify.split(" ").length <= 20) {
        return null;
    }
    //Classify submission by name and about, stop if unclassifiable
    List<String> category = classHandler.getCategoryFrom(sectionToClassify); 
    try {
      if (category.isEmpty()) {
        return null;
      }  
    } catch (NullPointerException ex) {
      System.err.println(ex);
      return null;
    }
    return new OrganizationInfo(index, name, link, about, category);
  }

  //Develop org from single upload submission
  public static OrganizationInfo getClassifiedOrgFrom(HttpServletRequest request, int index, ClassHandler classHandler) {
    String name  = request.getParameter("name");
    String link  = request.getParameter("link");
    String about = request.getParameter("about");
    String sectionToClassify = name + " " + about;
    if (sectionToClassify.split(" ").length <= 20) {
        return null;
    }
    //Classify submission by name and about, stop if unclassifiable
    List<String> category = classHandler.getCategoryFrom(sectionToClassify); 
    try {
      if (category.isEmpty()) {
        return null;
      }  
    } catch (NullPointerException ex) {
      System.err.println(ex);
      return null;
    }
    return new OrganizationInfo(index, name, link, about, category);
  }

  public static OrganizationInfo getSubmissionOrgFrom(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("name");
    String link = rs.getString("link");
    String about = rs.getString("about");
    String categoryString = rs.getString("class");
    List<String> category = Arrays.asList(categoryString.split("/",0));
    return new OrganizationInfo(id, name, link, about, category);
  }
  //Get org from SQL result to send to front end
  public static OrganizationInfo getResultOrgFrom(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("name");
    String link = rs.getString("link");
    String about = rs.getString("about");
    String neighbor1 = rs.getString("neighbor1_name");
    int neighbor1ID = rs.getInt("neighbor1");
    String neighbor2 = rs.getString("neighbor2_name");
    int neighbor2ID = rs.getInt("neighbor2");
    String neighbor3 = rs.getString("neighbor3_name");
    int neighbor3ID = rs.getInt("neighbor3");
    String neighbor4 = rs.getString("neighbor4_name");
    int neighbor4ID = rs.getInt("neighbor4");
        
    return new OrganizationInfo(id, name, link, about, neighbor1, neighbor2, neighbor3, neighbor4,
        neighbor1ID, neighbor2ID, neighbor3ID, neighbor4ID);
  }

  //Pass a newly classified org to SQL statement to update table
  public void passInfoTo(PreparedStatement statement) throws SQLException {
    statement.setInt(1, this.id);
    statement.setString(2, this.name);
    statement.setString(3, this.link);
    statement.setString(4, this.about);
    String classPath = String.join("/", this.classification);
    statement.setString(5, classPath);
    statement.addBatch();

    if (this.id % 500 == 0){
        statement.executeBatch();
      } 
  }

  //Getters for testing
  public int getID() {return this.id;}
  public String getName() {return this.name;}
  public String getAbout() {return this.about;}
  public String getLink() {return this.link;}
  public List<String> getCategory() {return this.classification;}
}
