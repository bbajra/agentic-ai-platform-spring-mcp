package com.example.aigateway.model;

import java.util.List;

public record RunbookSearchResult(List<Match> matches) {
  public record Match(String file, String section, String snippet, int score) {
  }
}
