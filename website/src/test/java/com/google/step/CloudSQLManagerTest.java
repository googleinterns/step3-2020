package com.google.step;

import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.step.data.*;
import com.opencsv.*;
import java.util.*;
import java.sql.*;
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
public final class CloudSQLManagerTest {
  CloudSQLManager mockSQLManager;
  private static String testTable = "Table";
  private static List<String> testColumns = Arrays.asList("column1", "column2", "column3");

  @Mock private Connection mockConnection;
  @Mock private Statement mockStatement;
  @Mock private ResultSet mockResultSet;

  @Before
  public void setUp() throws SQLException {
    mockSQLManager = new CloudSQLManager(mockConnection);
  }

  @Test
  public void getEntireTableTest() throws SQLException {
    Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
    Mockito.when(mockStatement.executeQuery("SELECT * FROM Table;")).thenReturn(mockResultSet);
    Mockito.when(mockResultSet.getString("class")).thenReturn("Table Info");

    String expected = "Table Info";
    String actual = mockSQLManager.get(testTable).getString("class");
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getDistinctValuesTest() throws SQLException {
    Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
    Mockito.when(mockStatement.executeQuery("SELECT DISTINCT column1, column2, column3 FROM Table;")).thenReturn(mockResultSet);
    Mockito.when(mockResultSet.getString("column1")).thenReturn("0");
    Mockito.when(mockResultSet.getString("column2")).thenReturn("1");
    Mockito.when(mockResultSet.getString("column3")).thenReturn("2");
    
    ResultSet result = mockSQLManager.getDistinct(testTable, testColumns, null);
    for (int i = 0; i < 3; i++) {
      String expected = Integer.toString(i);
      String actual = result.getString(testColumns.get(i));
      Assert.assertEquals(expected, actual);
    }  
  }

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

  private static String orgName = "Test For Humanity";
  private static String orgMission = "Here at test for humanity we hope to provide great testing for all of our code bases. And with that enough said.";
  private static String orgLink = "www.testing.org";

  @Test
  public void querySimilarOrgsTest() throws SQLException {
    Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);
    Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);
    String[] record = {orgName, orgLink, orgMission};
    OrganizationInfo fakeOrg = 
        OrganizationInfo.getClassifiedOrgFrom(record, 0, new MockClassHandler());
    mockSQLManager.getPossibleComparisons("fakeTable", fakeOrg);
    String nme = "'%" + orgName + "%'";
    String abt = "'%" + orgMission + "%'";
    String lnk = "'%" + orgLink + "%'";
    String expected = String.format("SELECT DISTINCT * FROM fakeTable WHERE name LIKE %s OR link LIKE %s OR about LIKE %s;",
        nme, lnk, abt);

    Mockito.verify(mockStatement).executeQuery(expected);
    
  }
}
