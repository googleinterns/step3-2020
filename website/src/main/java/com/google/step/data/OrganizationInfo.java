package com.google.step.data;

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

    private OrganizationInfo(int id, String name, String link, String about, List<String> classification) {
      this.id = id;
      this.name = name;
      this.link = link;
      this.about = about;
      this.classification = classification;
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

  public static OrganizationInfo getClassifiedOrgFrom(String[] record, int index) throws Exception{
    String name = record[0];
    String link = record[1];
    String about = record[2];
    //Classify submission by name and about, stop if unclassifiable
    List<List<String>> classification = Arrays.asList(
        Arrays.asList("Coding","Testing","Test1"),
        Arrays.asList("Coding","Testing","Test2"),
        Arrays.asList("Coding","Testing","Test3"),
        Arrays.asList("Coding1","1Testing1","8Test1"),
        Arrays.asList("Coding1","1Testing1","8Test2"),
        Arrays.asList("Coding1","1Testing3","8Test3"),
        Arrays.asList("Coding2","2Testing2","2Test"),
        Arrays.asList("Coding2","2Testing2","2Test"),
        Arrays.asList("Coding2","2Testing2","2Test"),
        Arrays.asList("Coding3","3Testing3","3Test"),
        Arrays.asList("Coding3","3Testing3","3Test"),
        Arrays.asList("Coding3","3Testing4","3Test4"),
        Arrays.asList("Coding3","3Testing4","3Test5"));//classifyText(name + " " + about);
    if (classification.isEmpty()){
      return null;
    }  
    return new OrganizationInfo(index, name, link, about, classification.get(index%13));
  }

  public void passInfoTo(PreparedStatement statement) throws SQLException {
    statement.setInt(1, this.id);
    statement.setString(2, this.name);
    statement.setString(3, this.link);
    statement.setString(4, this.about);
    String classPath = String.join("/", this.classification);
    String parentClass = this.classification.get(this.classification.size() - 1);
    statement.setString(5, classPath);
    statement.setString(6, parentClass);
  }
}
