package com.google.step.data;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public final class OrganizationInfo {
  private final Entity entity;

  public OrganizationInfo(Entity organizationEntity) {
    this.entity = organizationEntity;
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
          .map(category-> Arrays.asList(category.split("/", 0)))
          .collect(ArrayList<String>::new, List::addAll, List::addAll)
          .stream()
          .filter(category -> !category.isEmpty())
          .collect(Collectors.toCollection(ArrayList::new));
    }
    // [END language_classify_text]
  }

  private static Entity getOrganizationEntityFrom(String[] line, long index) throws Exception{
    //Classify submission by name and about, stop if unclassifiable
    List<String> classification = classifyText(line[0] + " " + line[2]);
    if (classification.isEmpty()){
      return null;
    }
    //Create key using categories as parent key
    Key classKey = classification.stream().collect(CategoryCollector.toKey());
    Entity newOrganization = 
        new Entity("Organization", classKey);
    //set org properties
    newOrganization.setProperty("name", line[0]);
    newOrganization.setProperty("webLink", line[1]);
    newOrganization.setProperty("about", line[2]);
    newOrganization.setProperty("index", index);
    newOrganization.setProperty("classification", classification);    
    return newOrganization;
  }

  public static boolean valid(OrganizationInfo item) {
    //Required fields
    try {
      if (((String) item.entity.getProperty("name")).isEmpty() || 
          ((String) item.entity.getProperty("about")).isEmpty() ||
          ((String) item.entity.getProperty("webLink")).isEmpty() ||
          !item.entity.hasProperty("classification")) {
        return false;
      }
    } catch (NullPointerException ex) {
      return false;
    }
    return true;
  }

  public Entity getEntity() {
    return this.entity;
  }

  public static List<OrganizationInfo> getOrganizationsFrom(CSVReader csvReader, long index) throws IOException {
    List<OrganizationInfo> organizations = new ArrayList<>();
    String[] nextRecord = new String[3]; 
    try {
      while ((nextRecord = csvReader.readNext()) != null) { 
        try {
          OrganizationInfo item = new OrganizationInfo(getOrganizationEntityFrom(nextRecord, index));
          if (valid(item)) { 
            organizations.add(item);
            index++;
          } 
        } catch (Exception ex) {
          System.err.println(ex);
        }
      }
    } catch (CsvValidationException ex) {
      System.err.println(ex);
    } catch (DeadlineExceededException ex) {
      return organizations;
    }
    return organizations;    
  }
}
