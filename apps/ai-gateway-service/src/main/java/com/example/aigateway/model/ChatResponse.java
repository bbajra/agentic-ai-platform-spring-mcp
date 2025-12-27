package com.example.aigateway.model;

import java.util.List;

public record ChatResponse(
    String answer,
    String modelUsed,
    long latencyMs,
    List<String> evidence,
    List<ToolCall> toolCalls) {
  public record ToolCall(String toolName, String input, String output) {
  }
}
