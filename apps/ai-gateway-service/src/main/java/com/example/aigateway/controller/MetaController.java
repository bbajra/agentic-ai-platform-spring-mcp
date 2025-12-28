package com.example.aigateway.controller;

import com.example.aigateway.config.AiModelProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetaController {

  private final Environment environment;
  private final AiModelProperties aiModelProperties;
  private final ObjectProvider<BuildProperties> buildPropertiesProvider;
  private final ObjectProvider<GitProperties> gitPropertiesProvider;

  public MetaController(
      Environment environment,
      AiModelProperties aiModelProperties,
      ObjectProvider<BuildProperties> buildPropertiesProvider,
      ObjectProvider<GitProperties> gitPropertiesProvider) {
    this.environment = environment;
    this.aiModelProperties = aiModelProperties;
    this.buildPropertiesProvider = buildPropertiesProvider;
    this.gitPropertiesProvider = gitPropertiesProvider;
  }

  @GetMapping("/meta")
  public MetaResponse meta() {
    String[] profiles = environment.getActiveProfiles();
    String activeProfile = profiles.length == 0 ? "default" : String.join(",", profiles);

    BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
    String version = buildProperties == null ? "0.0.0" : buildProperties.getVersion();

    GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();
    String commitHash = resolveCommitHash(gitProperties);

    return new MetaResponse(
        activeProfile,
        aiModelProperties.model(),
        aiModelProperties.providerType(),
        version,
        commitHash);
  }

  private String resolveCommitHash(GitProperties gitProperties) {
    if (gitProperties == null) {
      return "unknown";
    }
    String abbrev = gitProperties.get("commit.id.abbrev");
    if (abbrev != null && !abbrev.isBlank()) {
      return abbrev;
    }
    String full = gitProperties.getCommitId();
    return (full == null || full.isBlank()) ? "unknown" : full;
  }

  public record MetaResponse(
      String activeProfile,
      String modelName,
      String providerType,
      String version,
      String commitHash) {
  }
}
