package com.google.step;

import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.gson.Gson;
import com.google.step.servlets.DataServlet;
import com.google.step.data.*;
import com.opencsv.*;
import java.util.*;
import java.sql.*;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
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
public final class OrganizationInfoTest {
  private MockClassHandler mockHandler;
  private static String orgName = "Test For Humanity";
  private static String orgMission = "Here at test for humanity we hope to provide great testing for all of our code bases. And with that enough said.";
  private static String orgLink = "www.testing.org";
  

  @Mock
  HttpServletRequest mockHTTPRequest;

  private final class MockClassHandler implements ClassHandler {
    @Override
    public ClassifyTextResponse classifyRequest(ClassifyTextRequest request) {
      ClassificationCategory c = ClassificationCategory.newBuilder().build()
          .toBuilder().setName("/Coding/Java/Testing/Unit").build();
      ClassifyTextResponse mockClassifyResponse = ClassifyTextResponse.newBuilder()
          .addCategories(c).build();
      return mockClassifyResponse;
    }
  }

  @Before
  public void setUp() {
    mockHandler = new MockClassHandler();
  }

  @Test
  public void cSVRecordTest() {
    String[] record = {orgName, orgLink, orgMission};
    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(record, 0, mockHandler);

    Assert.assertNotNull(result);
    Assert.assertEquals(orgName, result.getName());
    Assert.assertEquals(orgMission, result.getAbout());
    Assert.assertEquals(orgLink, result.getLink());
    Assert.assertEquals(0, result.getID());
    List<String> expectedClass = Arrays.asList("Coding", "Java", "Testing", "Unit");
    Assert.assertEquals(expectedClass, result.getCategory());
  }

  @Test
  public void hTTPRequestTest() {
    //Set up Mock HTTP request
    Mockito.when(mockHTTPRequest.getParameter("name")).thenReturn(orgName);
    Mockito.when(mockHTTPRequest.getParameter("link")).thenReturn(orgLink);
    Mockito.when(mockHTTPRequest.getParameter("about")).thenReturn(orgMission);
    
    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(mockHTTPRequest, 0, mockHandler);

    Assert.assertNotNull(result);
    Assert.assertEquals(orgName, result.getName());
    Assert.assertEquals(orgMission, result.getAbout());
    Assert.assertEquals(orgLink, result.getLink());
    Assert.assertEquals(0, result.getID());
    List<String> expectedClass = Arrays.asList("Coding", "Java", "Testing", "Unit");
    Assert.assertEquals(expectedClass, result.getCategory());
  }

  @Test
  public void hTTPRequestBadAboutTest() {
    //Set up Mock HTTP request
    Mockito.when(mockHTTPRequest.getParameter("name")).thenReturn(orgName);
    Mockito.when(mockHTTPRequest.getParameter("link")).thenReturn(orgLink);
    Mockito.when(mockHTTPRequest.getParameter("about")).thenReturn("Tiny Mission Test");

    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(mockHTTPRequest, 0, mockHandler);
    Assert.assertNull(result);
  }

  @Test
  public void cSVBadAboutTest() {
    String[] record = {orgName, orgLink, "Tiny Mission Test"};
    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(record, 0, mockHandler);
    Assert.assertNull(result);
  }

  @Test
  public void hTTPRequestBadCategoryTest() {
    //Set up Mock HTTP request
    Mockito.when(mockHTTPRequest.getParameter("name")).thenReturn(orgName);
    Mockito.when(mockHTTPRequest.getParameter("link")).thenReturn(orgLink);
    Mockito.when(mockHTTPRequest.getParameter("about")).thenReturn(orgMission);

    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(mockHTTPRequest, 0, classRequest -> {
          return ClassifyTextResponse.newBuilder().build();
        });
    Assert.assertNull(result);
  }

  @Test
  public void cSVBadCategoryTest() {
    String[] record = {orgName, orgLink, orgMission};
    OrganizationInfo result = 
        OrganizationInfo.getClassifiedOrgFrom(record, 0, classRequest -> {
          return ClassifyTextResponse.newBuilder().build();
        });
    Assert.assertNull(result);
  }
}
