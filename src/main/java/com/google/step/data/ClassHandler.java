package com.google.step.data;

import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import java.util.*;
import java.util.stream.Collectors;

public interface ClassHandler {
  default List<String> getCategoryFrom(String text) {
    Document doc = convertStringToDoc(text);
    ClassifyTextRequest request = convertDocToRequest(doc);
    ClassifyTextResponse response = classifyRequest(request);
    List<ClassificationCategory> categories = response.getCategoriesList();
    if (categories.isEmpty()) {
      return null;
    } else {
      return parseClass(categories.get(0));
    }
  }

  default List<String> parseClass(ClassificationCategory mainCategory) {
    String mainClassification = mainCategory.getName();
    return Arrays.asList(mainClassification.split("/", 0))
        .stream()
        .filter(classification -> !classification.isEmpty())
        .collect(Collectors.toList());
  }

  abstract ClassifyTextResponse classifyRequest(ClassifyTextRequest request);
  
  default ClassifyTextRequest convertDocToRequest(Document doc) {
    return ClassifyTextRequest.newBuilder().setDocument(doc).build();
  }

  default Document convertStringToDoc(String text) {
    return Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
  }
}