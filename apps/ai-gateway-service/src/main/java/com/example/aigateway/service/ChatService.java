package com.example.aigateway.service;

import com.example.aigateway.config.AiModelProperties;
import com.example.aigateway.config.ToolAllowlistProperties;
import com.example.aigateway.model.ChatRequest;
import com.example.aigateway.model.ChatResponse;
import com.example.aigateway.model.RunbookSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;
  private final ToolAllowlistProperties toolAllowlistProperties;
  private final AiModelProperties aiModelProperties;
  private final RunbookSearchService runbookSearchService;
  private final ObjectMapper objectMapper;

  private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
  private static final String RUNBOOK_TOOL_NAME = "searchRunbooks";
  private static final String TOOL_DECISION_PROMPT = """
      You decide whether to call the runbook search tool.
      Respond with strict JSON: {"callTool": true|false, "query": "keywords"}.
      callTool is true only if the user asks about the system, runbooks, or docs.
      query should be a short keyword phrase; empty when callTool is false.
      """;
  private static final String ANSWER_SYSTEM_PROMPT = """
      You are a helpful assistant. If runbook search results are provided,
      use them to ground your response and cite evidence as file#section.
      """;

  public ChatService(
      ChatClient chatClient,
      ToolAllowlistProperties toolAllowlistProperties,
      AiModelProperties aiModelProperties,
      RunbookSearchService runbookSearchService,
      ObjectMapper objectMapper) {
    this.chatClient = chatClient;
    this.toolAllowlistProperties = toolAllowlistProperties;
    this.aiModelProperties = aiModelProperties;
    this.runbookSearchService = runbookSearchService;
    this.objectMapper = objectMapper;
  }

  public ChatResponse chat(ChatRequest request) {
    Optional<ToolDecision> decision = decideTool(request.message());
    ToolResult toolResult = runToolIfNeeded(decision);

    long startNs = System.nanoTime();
    Prompt prompt = buildFinalPrompt(request.message(), toolResult);
    org.springframework.ai.chat.ChatResponse response = chatClient.call(prompt);
    long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

    String answer = response.getResult().getOutput().getContent();
    List<String> evidence = toolResult == null ? Collections.emptyList() : toolResult.evidence();
    List<ChatResponse.ToolCall> toolCalls =
        toolResult == null ? Collections.emptyList() : List.of(toolResult.toolCall());
    return new ChatResponse(
        answer,
        aiModelProperties.model(),
        latencyMs,
        evidence,
        toolCalls);
  }

  private Optional<ToolDecision> decideTool(String message) {
    if (!isToolAllowed()) {
      return Optional.empty();
    }
    Prompt prompt = new Prompt(List.of(new SystemMessage(TOOL_DECISION_PROMPT), new UserMessage(message)));
    org.springframework.ai.chat.ChatResponse response = chatClient.call(prompt);
    String content = response.getResult().getOutput().getContent();
    Optional<ToolDecision> decision = parseDecision(content);
    if (decision.isPresent()) {
      return decision;
    }
    return heuristicDecision(message);
  }

  private ToolResult runToolIfNeeded(Optional<ToolDecision> decision) {
    if (decision.isEmpty() || !decision.get().callTool()) {
      return null;
    }
    String query = decision.get().query();
    RunbookSearchResult result = runbookSearchService.search(query);
    String json = serializeResult(result);
    List<String> evidence = result.matches().stream()
        .map(match -> match.file() + "#" + match.section())
        .toList();
    ChatResponse.ToolCall toolCall = new ChatResponse.ToolCall(RUNBOOK_TOOL_NAME, query, json);
    return new ToolResult(result, evidence, toolCall, json);
  }

  private Prompt buildFinalPrompt(String message, ToolResult toolResult) {
    if (toolResult == null) {
      return new Prompt(List.of(new SystemMessage(ANSWER_SYSTEM_PROMPT), new UserMessage(message)));
    }
    Message toolMessage = new SystemMessage(
        "Runbook search results (JSON):\n" + toolResult.serializedResult());
    return new Prompt(List.of(new SystemMessage(ANSWER_SYSTEM_PROMPT), toolMessage, new UserMessage(message)));
  }

  private boolean isToolAllowed() {
    List<String> allowlist = toolAllowlistProperties.allowlist();
    if (allowlist == null) {
      return false;
    }
    return allowlist.contains(RUNBOOK_TOOL_NAME);
  }

  private Optional<ToolDecision> parseDecision(String content) {
    if (content == null || content.isBlank()) {
      return Optional.empty();
    }
    Matcher matcher = JSON_OBJECT_PATTERN.matcher(content);
    if (!matcher.find()) {
      return Optional.empty();
    }
    String json = matcher.group();
    try {
      return Optional.of(objectMapper.readValue(json, ToolDecision.class));
    } catch (JsonProcessingException ex) {
      return Optional.empty();
    }
  }

  private Optional<ToolDecision> heuristicDecision(String message) {
    String lower = message.toLowerCase(Locale.ROOT);
    if (lower.contains("runbook") || lower.contains("runbooks")) {
      return Optional.of(new ToolDecision(true, message));
    }
    if (lower.contains("summarize the system")) {
      return Optional.of(new ToolDecision(true, "system overview"));
    }
    return Optional.of(new ToolDecision(false, ""));
  }

  private String serializeResult(RunbookSearchResult result) {
    try {
      return objectMapper.writeValueAsString(result);
    } catch (JsonProcessingException ex) {
      return "{\"matches\":[]}";
    }
  }

  private record ToolDecision(boolean callTool, String query) {
  }

  private record ToolResult(
      RunbookSearchResult result,
      List<String> evidence,
      ChatResponse.ToolCall toolCall,
      String serializedResult) {
  }
}
