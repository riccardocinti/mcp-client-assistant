package com.riccardocinti.mcp_client_assistant.service;

import com.riccardocinti.mcp_client_assistant.model.OllamaInfo;

public interface AIService {

    boolean isLLMAvailable();

    OllamaInfo getLLMInfo();

    void clearConversation(String conversationId);

    String chatWithSystemPrompt(String systemPrompt, String message);

    String chat(String userMessage, String conversationId);

    String chatSimple(String systemPrompt, String message);

}
