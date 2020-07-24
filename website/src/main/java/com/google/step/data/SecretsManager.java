package com.google.step.data;

import com.google.cloud.secretmanager.v1.*;
import java.io.IOException;
import java.util.*;

public class SecretsManager {
  private final String connectionName;
  private final String userName;
  private final String passcode;
  private final String environmentName;

  private SecretsManager(List<String> secrets) {
    this.connectionName = secrets.get(0);
    this.userName = secrets.get(1);
    this.passcode = secrets.get(2);
    this.environmentName = secrets.get(3);
  }

  public static SecretsManager getSecrets() {
    try { 
      List<String> secrets = accessSecretVersion();
      return new SecretsManager(secrets);
    } catch (ServiceConfigurationError ex) {
      System.err.println(ex);
    }
    List<String> secrets = accessLocalSecrets();
    return new SecretsManager(secrets);
  }

  public static List<String> accessSecretVersion() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "228403351598";
    String secretId = "sqlTestEnvironment";
    String versionId = "1";
    String secrets = accessSecretVersion(projectId, secretId, versionId);
    return Arrays.asList(secrets.split("/", 0));
  }

  // Access the payload for the given secret version if one exists. The version
  // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
  public static String accessSecretVersion(String projectId, String secretId, String versionId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String payload = response.getPayload().getData().toStringUtf8();
      return payload;
    } catch (IOException ex) {
      System.err.println(ex);
    }
    return "";
  }

  public static List<String> accessLocalSecrets() {
    return Arrays.asList(
        System.getenv("connectionName"),
        System.getenv("userName"),
        System.getenv("passcode"),
        System.getenv("environmentName"));
  }

  public String getConnection() { return this.connectionName; }
  public String getEnvironment() { return this.environmentName; }
  public String getUserName() { return this.userName; }
  public String getPasscode() { return this.passcode; }
}