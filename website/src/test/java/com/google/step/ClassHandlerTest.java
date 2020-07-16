package com.google.step;

import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.step.servlets.DataServlet;
import com.google.step.data.*;
import java.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.*;
import org.mockito.Matchers.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class ClassHandlerTest {
  MockClassHandlerOneClass testHandler;
  MockClassHandlerNoClass testHandlerNoClass;
  MockClassHandlerMultipleClass testHandlerMultipleClass;

  private final class MockClassHandlerOneClass implements ClassHandler {
    @Override
    public ClassifyTextResponse classifyRequest(ClassifyTextRequest request) {
      ClassificationCategory c = ClassificationCategory.newBuilder().build()
          .toBuilder().setName("/Coding/Java/Testing/Unit").build();
      ClassifyTextResponse mockClassifyResponse = ClassifyTextResponse.newBuilder()
          .addCategories(c).build();
      return mockClassifyResponse;
    }
  }

  private final class MockClassHandlerNoClass implements ClassHandler {
    @Override
    public ClassifyTextResponse classifyRequest(ClassifyTextRequest request) {
      ClassifyTextResponse mockClassifyResponse = ClassifyTextResponse.newBuilder().build();
      return mockClassifyResponse;
    }
  }

  private final class MockClassHandlerMultipleClass implements ClassHandler {
    @Override
    public ClassifyTextResponse classifyRequest(ClassifyTextRequest request) {
      List<ClassificationCategory> c = Arrays.asList(
          ClassificationCategory.newBuilder().build()
                  .toBuilder().setName("/Coding/Java/Testing/Unit").build(),
          ClassificationCategory.newBuilder().build()
                  .toBuilder().setName("/Coding1/Java1/Testing1/Unit1").build(),
          ClassificationCategory.newBuilder().build()
                  .toBuilder().setName("/Coding2/Java2/Testing2/Unit2").build(), 
          ClassificationCategory.newBuilder().build()
                  .toBuilder().setName("/Coding3/Java3/Testing3/Unit3").build());

      ClassifyTextResponse mockClassifyResponse = ClassifyTextResponse.newBuilder()
          .addAllCategories(c).build();
      return mockClassifyResponse;
    }
  }

  @Before
  public void setUp() {
    testHandler = new MockClassHandlerOneClass();
    testHandlerNoClass = new MockClassHandlerNoClass();
    testHandlerMultipleClass = new MockClassHandlerMultipleClass();
  }

  @Test 
  public void docBuilderTest() {
    String expectedValue = "This is a test of testable code";
    Document result = testHandler.convertStringToDoc(expectedValue);
    Assert.assertEquals(expectedValue, result.getContent());
  }

  @Test
  public void createClassifyRequestTest() {
    String valueToClassify = "This value is meaningless";
    Document expected = testHandler.convertStringToDoc(valueToClassify);
    ClassifyTextRequest result = testHandler.convertDocToRequest(expected);
    Assert.assertTrue(result.hasDocument());
    Assert.assertTrue(expected.equals(result.getDocument()));
  }

  @Test
  public void parseMainCategory() {
    ClassificationCategory classToParse = ClassificationCategory.newBuilder().build()
        .toBuilder().setName("/Coding/Java/Testing/Unit").build();
    List<String> expected = Arrays.asList("Coding", "Java", "Testing", "Unit");
    List<String> actual = testHandler.parseClass(classToParse);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void fullTestOneClass() {
    String valueToClassify = "This value is meaningless";
    List<String> actual = testHandler.getCategoryFrom(valueToClassify);
    List<String> expected = Arrays.asList("Coding", "Java", "Testing", "Unit");
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void fullTestNoClass() {
    String valueToClassify = "This value is meaningless";
    List<String> actual = testHandlerNoClass.getCategoryFrom(valueToClassify);
    List<String> expected = null;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void fullTestMultipleClass() {
    String valueToClassify = "This value is meaningless";
    List<String> actual = testHandlerMultipleClass.getCategoryFrom(valueToClassify);
    List<String> expected = Arrays.asList("Coding", "Java", "Testing", "Unit");
    Assert.assertEquals(expected, actual);
  }


}