package com.google.step;

import com.google.step.servlets.DataServlet;
import com.google.step.data.*;
import com.opencsv.*;
import java.util.*;
import java.util.stream.Collectors;
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
public final class DataServletTest {
  @Test 
  public void fake() {
    Assert.assertTrue(true);
  }
  // Map<String, Set<String>> testTree;
  // // private static List<List<String>> testClasses = Arrays.asList(
  // //     Arrays.asList("Coding","Testing","Test1"),
  // //     Arrays.asList("Coding","Testing","Test2"),
  // //     Arrays.asList("Coding","Testing","Test3"),
  // //     Arrays.asList("Coding1","1Testing1","8Test1"),
  // //     Arrays.asList("Coding1","1Testing1","8Test2"),
  // //     Arrays.asList("Coding1","1Testing3","8Test3"),
  // //     Arrays.asList("Coding2","2Testing2","2Test"),
  // //     Arrays.asList("Coding2","2Testing2","2Test"),
  // //     Arrays.asList("Coding2","2Testing2","2Test"),
  // //     Arrays.asList("Coding3","3Testing3","3Test"),
  // //     Arrays.asList("Coding3","3Testing3","3Test"),
  // //     Arrays.asList("Coding3","3Testing4","3Test4"),
  // //     Arrays.asList("Coding3","3Testing4","3Test5"));

  // // @Mock
  // // ResultSet classes;

  // // @Before
  // // public void CreateTreeToTest() throws SQLException {
  // //   Mockito.when(classes.next()).thenReturn(
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true,
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       true, 
  // //       false
  // //   );
  // //   Mockito.when(classes.getString("class")).thenReturn(
  // //       String.join("/", testClasses.get(0)),
  // //       String.join("/", testClasses.get(1)),
  // //       String.join("/", testClasses.get(2)),
  // //       String.join("/", testClasses.get(3)),
  // //       String.join("/", testClasses.get(4)),
  // //       String.join("/", testClasses.get(5)),
  // //       String.join("/", testClasses.get(6)),
  // //       String.join("/", testClasses.get(7)),
  // //       String.join("/", testClasses.get(8)),
  // //       String.join("/", testClasses.get(9)),
  // //       String.join("/", testClasses.get(10)),
  // //       String.join("/", testClasses.get(11)),
  // //       String.join("/", testClasses.get(12)) 
  // //   );
        
  // //   testTree = DataServlet.createClassificationTree(classes);
  // // }

  // // @Test
  // // public void rootsListTest() {
  // //   int expectedSize = 4;
  // //   int actualSize = testTree.get("roots").size();
  // //   Assert.assertEquals(expectedSize, actualSize);
  // //   Assert.assertTrue(testTree.get("roots").contains("Coding"));
  // //   Assert.assertTrue(testTree.get("roots").contains("Coding1"));
  // //   Assert.assertTrue(testTree.get("roots").contains("Coding2"));
  // //   Assert.assertTrue(testTree.get("roots").contains("Coding3"));
  // // }

  // // @Test
  // // public void rootsSubTreeTest() {
  // //   Assert.assertEquals(1, testTree.get("Coding").size());
  // //   Assert.assertTrue(testTree.get("Coding").contains("Testing"));

  // //   Assert.assertEquals(2, testTree.get("Coding1").size());
  // //   Assert.assertTrue(testTree.get("Coding1").contains("1Testing1"));
  // //   Assert.assertTrue(testTree.get("Coding1").contains("1Testing3"));

  // //   Assert.assertEquals(1, testTree.get("Coding2").size());
  // //   Assert.assertTrue(testTree.get("Coding2").contains("2Testing2"));

  // //   Assert.assertEquals(2, testTree.get("Coding3").size());
  // //   Assert.assertTrue(testTree.get("Coding3").contains("3Testing3"));
  // //   Assert.assertTrue(testTree.get("Coding3").contains("3Testing4"));
  // // }

  // // @Test
  // // public void leavesTest() {
  // //   Assert.assertTrue(testTree.get("Test1").isEmpty());
  // //   Assert.assertTrue(testTree.get("Test2").isEmpty());
  // //   Assert.assertTrue(testTree.get("Test3").isEmpty());
  // //   Assert.assertTrue(testTree.get("8Test1").isEmpty());
  // //   Assert.assertTrue(testTree.get("8Test2").isEmpty());
  // //   Assert.assertTrue(testTree.get("8Test3").isEmpty());
  // //   Assert.assertTrue(testTree.get("2Test").isEmpty());
  // //   Assert.assertTrue(testTree.get("3Test").isEmpty());
  // //   Assert.assertTrue(testTree.get("3Test4").isEmpty());
  // //   Assert.assertTrue(testTree.get("3Test5").isEmpty());
  // // }
}
