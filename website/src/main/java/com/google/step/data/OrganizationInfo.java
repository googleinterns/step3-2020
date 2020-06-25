package com.google.step.data;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.validator.UrlValidator;

public final class OrganizationInfo {
  private final Entity entity;

  public OrganizationInfo(Entity organizationEntity) {
    this.entity = organizationEntity;
  }

  private static Entity getOrgEntityFrom(HttpServletRequest request) {
    Entity newOrganization = new Entity("Organization");
    newOrganization.setProperty("name", request.getParameter("orgName"));
    newOrganization.setProperty("webLink", request.getParameter("webLink"));
    newOrganization.setProperty("donateLink", request.getParameter("donateLink"));
    newOrganization.setProperty("about", request.getParameter("about"));

    return newOrganization;
  }

  private static Entity getOrganizationEntityFrom(String[] line, int index) throws Exception{
    Entity newOrganization = new Entity("Organization");
    newOrganization.setProperty("name", line[0]);
    newOrganization.setProperty("webLink", line[1]);
    newOrganization.setProperty("about", line[2]);
    newOrganization.setProperty("index", index);
    newOrganization.setProperty("classification", classifyText(line[0] + " " + line[2]));
    return newOrganization;
  }

  public boolean isValid() {
    UrlValidator urlValidator = new UrlValidator();
    //Required fields
    try {
      if (((String) this.entity.getProperty("name")).isEmpty() || 
          ((String) this.entity.getProperty("about")).isEmpty() ||
          !urlValidator.isValid(((String) this.entity.getProperty("webLink")))) {
        return false;
      }
    } catch (ClassCastException e) {
      return false;
    }
    //Change invalid Optional Fields to null
    if (urlValidator.isValid(((String) this.entity.getProperty("donateLink")))) {
      this.entity.setProperty("donateLink",null);
    }
    return true;
  }

  public void merge(Entity duplicate) {
    this.entity.getProperties().forEach((property, value) -> {
      if (Objects.isNull(value) && !Objects.isNull(duplicate.getProperty(property))) {
        this.entity.setProperty(property, duplicate.getProperty(property));
      } 
      else if (!Objects.isNull(value) && !Objects.isNull(duplicate.getProperty(property))) {
        //TODO: Handle case where fields are conflicting
      }
    });
  }

  public Entity getEntity() {
    return this.entity;
  }

  public Query getQueryForDuplicates() {
    return new Query("Organization").addFilter("name", Query.FilterOperator.EQUAL, entity.getProperty("name"));
  }

  public static OrganizationInfo createInstanceFrom(HttpServletRequest request) {
    return new OrganizationInfo(getOrgEntityFrom(request));
  }

  public static List<OrganizationInfo> getOrganizationsFrom(CSVReader csvReader) throws IOException {
    List<OrganizationInfo> organizations = new ArrayList<>();
    String[] nextRecord = new String[3];
    int index = 0; 
    try {
      while ((nextRecord = csvReader.readNext()) != null) { 
        try {
          OrganizationInfo item = new OrganizationInfo(getOrganizationEntityFrom(nextRecord, index++)); 
            organizations.add(item);
        } catch (Exception ex) {
          System.err.println(ex);
        }
      }
    } catch (CsvValidationException ex) {
      System.err.println(ex);
    }
    return organizations;    
  }

  /** Detects categories in text using the Language Beta API. */
  private static List<String> classifyText(String text) throws Exception {
    // [START language_classify_text]
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // set content to the text string
      Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
      // detect categories in the given text
      ClassifyTextResponse response = language.classifyText(request);

      return response.getCategoriesList().stream()
      .map(ClassificationCategory::getName)
      .collect(Collectors.toList());
    }
    // [END language_classify_text]
  }
}
