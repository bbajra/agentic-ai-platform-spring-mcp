package com.example.aigateway.service;

import com.example.aigateway.config.ToolAllowlistProperties;
import com.example.aigateway.model.ChatRequest;
import com.example.aigateway.model.ChatResponse;
import java.util.Collections;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;
  private final ToolAllowlistProperties toolAllowlistProperties;

  public ChatService(ChatClient chatClient, ToolAllowlistProperties toolAllowlistProperties) {
    this.chatClient = chatClient;
    this.toolAllowlistProperties = toolAllowlistProperties;
  }

  public ChatResponse chat(ChatRequest request) {
    Prompt prompt = new Prompt(new UserMessage(request.message()));
    // Placeholder: tool allowlist will gate which MCP tools can be invoked.
    toolAllowlistProperties.allowlist();

    org.springframework.ai.chat.ChatResponse response = chatClient.call(prompt);

    String answer = response.getResult().getOutput().getContent();
    return new ChatResponse(answer, Collections.emptyList(), Collections.emptyList());
  }
}
