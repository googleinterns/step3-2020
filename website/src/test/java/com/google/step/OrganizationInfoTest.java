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

  // statement.setInt(1, this.id);
  //   statement.setString(2, this.name);
  //   statement.setString(3, this.link);
  //   statement.setString(4, this.about);
  //   String classPath = String.join("/", this.classification);
  //   statement.setString(5, classPath);
  //   statement.addBatch();

  //   if (this.id % 500 == 0){
  //       statement.executeBatch();
  //     } 

  @Test
  public void passInfoTest() throws SQLException {
    String[] record = {orgName, orgLink, orgMission};
    OrganizationInfo testOrg = 
        OrganizationInfo.getClassifiedOrgFrom(record, 0, mockHandler);

    PreparedStatement mockStatement = Mockito.mock(PreparedStatement.class);
    testOrg.passInfoTo(mockStatement);
    Mockito.verify(mockStatement).setInt(1, testOrg.getID());
    Mockito.verify(mockStatement).setString(2, testOrg.getName());
    Mockito.verify(mockStatement).setString(3, testOrg.getLink());
    Mockito.verify(mockStatement).setString(4, testOrg.getAbout());
    Mockito.verify(mockStatement).setString(5, "Coding/Java/Testing/Unit");
  }

  @Test
  public void passInfoExecuteTest() throws SQLException {
    String[] record = {orgName, orgLink, orgMission};
    OrganizationInfo testOrg = 
        OrganizationInfo.getClassifiedOrgFrom(record, 500, mockHandler);

    PreparedStatement mockStatement = Mockito.mock(PreparedStatement.class);
    testOrg.passInfoTo(mockStatement);
    Mockito.verify(mockStatement).executeBatch();
  }
}
