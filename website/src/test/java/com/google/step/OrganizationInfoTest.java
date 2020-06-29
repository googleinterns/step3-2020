package com.google.step;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.step.data.*;
import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class OrganizationInfoTest {
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @Test
  public void ConstructorTest() {
    Entity entity = new Entity("Test");
    OrganizationInfo test = new OrganizationInfo(entity);
    Assert.assertEquals(entity, test.getEntity());
  }

  @Test
  public void isValidAllValid() {
    Entity entity = new Entity("Test");
    entity.setProperty("name", "This is a test");
    entity.setProperty("classification", "/test");
    entity.setProperty("webLink", "www.test.org");
    entity.setProperty("about", "This test is definitely a test");
    OrganizationInfo test = new OrganizationInfo(entity);
    Assert.assertTrue(OrganizationInfo.valid(test));
  }

  @Test
  public void isValidNoClassification() {
    Entity entity = new Entity("Test");
    entity.setProperty("name", "This is a test");
    entity.setProperty("about", "This test is definitely a test");
    OrganizationInfo test = new OrganizationInfo(entity);
    Assert.assertFalse(OrganizationInfo.valid(test));
  }

  @Test
  public void isValidNoName() {
    Entity entity = new Entity("Test");
    entity.setProperty("name", "");
    entity.setProperty("classification", "/tests");
    entity.setProperty("webLink", "www.test.org");
    entity.setProperty("about", "This test is definitely a test");
    OrganizationInfo test = new OrganizationInfo(entity);
    Assert.assertFalse(OrganizationInfo.valid(test));
  }

  @Test
  public void isValidNoAbout() {
    Entity entity = new Entity("Test");
    entity.setProperty("name", "This is a test");
    entity.setProperty("classification", "/tests");
    entity.setProperty("webLink", "https://test.com");
    entity.setProperty("about", "");
    OrganizationInfo test = new OrganizationInfo(entity);
    Assert.assertFalse(OrganizationInfo.valid(test));
  }

  @Test
  public void ReduceCategoriesToKey() {
    String[] classification = {"Finance","Accounting & Auditing","Tax Preparation & Planning"};
    Key actual = Arrays.stream(classification).collect(CategoryCollector.toKey());
    Assert.assertEquals( "root(\"root\")/class(\"Finance\")/class(\"Accounting & Auditing\")/class(\"Tax Preparation & Planning\")", actual.toString());
  }
 }
