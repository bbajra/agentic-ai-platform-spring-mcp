package com.example.aigateway.service;

import com.example.aigateway.config.RunbookProperties;
import com.example.aigateway.model.RunbookSearchResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class RunbookSearchService {

  private final RunbookProperties runbookProperties;

  public RunbookSearchService(RunbookProperties runbookProperties) {
    this.runbookProperties = runbookProperties;
  }

  public RunbookSearchResult search(String query) {
    if (query == null || query.isBlank()) {
      return new RunbookSearchResult(List.of());
    }
    String basePathValue = runbookProperties.basePath();
    if (basePathValue == null || basePathValue.isBlank()) {
      return new RunbookSearchResult(List.of());
    }
    Path basePath = Path.of(basePathValue);
    if (!Files.exists(basePath)) {
      return new RunbookSearchResult(List.of());
    }

    String needle = query.toLowerCase(Locale.ROOT);
    List<String> terms = tokenize(needle);

    List<RunbookSearchResult.Match> matches = new ArrayList<>();
    try (Stream<Path> paths = Files.walk(basePath)) {
      List<Path> files = paths
          .filter(path -> path.toString().endsWith(".md"))
          .collect(Collectors.toList());
      for (Path file : files) {
        matches.addAll(searchFile(file, basePath, terms));
      }
    } catch (IOException ex) {
      return new RunbookSearchResult(List.of());
    }

    List<RunbookSearchResult.Match> topMatches = matches.stream()
        .sorted(Comparator.comparingInt(RunbookSearchResult.Match::score).reversed())
        .limit(runbookProperties.maxResults())
        .toList();

    return new RunbookSearchResult(topMatches);
  }

  private List<RunbookSearchResult.Match> searchFile(
      Path file,
      Path basePath,
      List<String> terms) throws IOException {
    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    List<RunbookSearchResult.Match> matches = new ArrayList<>();
    String section = "Introduction";
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      String trimmed = line.trim();
      if (trimmed.startsWith("#")) {
        section = trimmed.replaceFirst("^#+\\s*", "").trim();
      }
      int score = scoreLine(line, terms);
      if (score == 0) {
        continue;
      }
      String snippet = buildSnippet(lines, i, runbookProperties.snippetRadius());
      String relativeFile = basePath.relativize(file).toString();
      matches.add(new RunbookSearchResult.Match(relativeFile, section, snippet, score));
    }
    return matches;
  }

  private int scoreLine(String line, List<String> terms) {
    String lower = line.toLowerCase(Locale.ROOT);
    int score = 0;
    for (String term : terms) {
      if (lower.contains(term)) {
        score++;
      }
    }
    return score;
  }

  private List<String> tokenize(String input) {
    return Stream.of(input.split("\\s+"))
        .filter(token -> token.length() > 1)
        .toList();
  }

  private String buildSnippet(List<String> lines, int index, int radius) {
    int start = Math.max(0, index - radius);
    int end = Math.min(lines.size() - 1, index + radius);
    StringBuilder builder = new StringBuilder();
    for (int i = start; i <= end; i++) {
      if (builder.length() > 0) {
        builder.append('\n');
      }
      builder.append(lines.get(i).trim());
    }
    return builder.toString();
  }
}
