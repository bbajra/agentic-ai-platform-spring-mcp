package com.example.aigateway.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MetaInfoContributor implements InfoContributor {

  private final Environment environment;
  private final AiModelProperties aiModelProperties;
  private final ObjectProvider<BuildProperties> buildPropertiesProvider;
  private final ObjectProvider<GitProperties> gitPropertiesProvider;

  public MetaInfoContributor(
      Environment environment,
      AiModelProperties aiModelProperties,
      ObjectProvider<BuildProperties> buildPropertiesProvider,
      ObjectProvider<GitProperties> gitPropertiesProvider) {
    this.environment = environment;
    this.aiModelProperties = aiModelProperties;
    this.buildPropertiesProvider = buildPropertiesProvider;
    this.gitPropertiesProvider = gitPropertiesProvider;
  }

  @Override
  public void contribute(Info.Builder builder) {
    String[] profiles = environment.getActiveProfiles();
    String activeProfile = profiles.length == 0 ? "default" : String.join(",", profiles);

    BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
    String version = buildProperties == null ? "0.0.0" : buildProperties.getVersion();

    GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();
    String commitHash = resolveCommitHash(gitProperties);

    Map<String, Object> app = new LinkedHashMap<>();
    app.put("active-profile", activeProfile);
    app.put("model", aiModelProperties.model());
    app.put("provider-type", aiModelProperties.providerType());
    app.put("version", version);
    app.put("commit-hash", commitHash);

    builder.withDetail("app", app);
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
}
