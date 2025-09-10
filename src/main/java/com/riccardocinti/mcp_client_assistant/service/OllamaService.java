package com.riccardocinti.mcp_client_assistant.service;

import com.riccardocinti.mcp_client_assistant.model.OllamaInfo;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService implements AIService {

    private final ChatClient mcpChatClient;

    private final OllamaOptions mcpRuntimeOptions;

    private final OllamaChatModel ollamaChatModel;

    private final Map<String, List<String>> conversationHistoryStrings = new ConcurrentHashMap<>();

    public String chatSimple(String systemPrompt, String userMessage) {
        log.info("Smart chat processing: {}", userMessage);

        boolean useTools = shouldUseTools(userMessage);
        log.debug("Tools needed: {}", useTools);

        if (useTools) {
            return chatWithTools(systemPrompt, userMessage);
        } else {
            return chatWithoutTools(systemPrompt, userMessage);
        }
    }

    public String chat(String userMessage, String conversationId) {
        log.info("Processing chat message for conversation: {}", conversationId);

        List<String> history = conversationHistoryStrings.computeIfAbsent(
                conversationId,
                k -> new ArrayList<>()
        );

        try {
            StringBuilder fullContext = new StringBuilder();
            for (String msg : history) {
                fullContext.append(msg).append("\n");
            }
            fullContext.append("User: ").append(userMessage);

            String response = chatSimple("", fullContext.toString());

            history.add("User: " + userMessage);
            history.add("Assistant: " + response);

            if (history.size() > 40) { // 40 entries = 20 exchanges
                history.subList(0, history.size() - 40).clear();
            }

            log.debug("Ollama response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Error calling Ollama", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        log.info("Processing chat with system prompt");

        try {
            return chatSimple(systemPrompt, userMessage);
        } catch (Exception e) {
            log.error("Error in chat with system prompt", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public boolean isLLMAvailable() {
        try {
            mcpChatClient.prompt()
                    .user("ping")
                    .call()
                    .content();
            return true;
        } catch (Exception e) {
            log.warn("Ollama is not available", e);
            return false;
        }
    }

    public void clearConversation(String conversationId) {
        conversationHistoryStrings.remove(conversationId);
        log.info("Cleared conversation history for: {}", conversationId);
    }

    public OllamaInfo getLLMInfo() {
        return new OllamaInfo(
                ollamaChatModel.getDefaultOptions().getModel(),
                ollamaChatModel.getDefaultOptions().getTemperature(),
                0,  // Tool count would need to come from McpService if needed
                isLLMAvailable()
        );
    }

    private String chatWithTools(String systemPrompt, String userMessage) {
        log.info("Processing chat WITH tools");

        try {

            var prompt = mcpChatClient.prompt().user(userMessage);
            if (StringUtils.isNotBlank(systemPrompt)) {
                prompt = prompt.system(systemPrompt);
            }
            return prompt.options(mcpRuntimeOptions).call().content();

        } catch (Exception e) {
            log.error("Error in chat with tools", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    private String chatWithoutTools(String systemPrompt, String userMessage) {
        log.info("Processing chat WITHOUT tools");

        try {

            var prompt = mcpChatClient.prompt().user(userMessage);
            if (StringUtils.isNotBlank(systemPrompt)) {
                prompt = prompt.system(systemPrompt);
            }
            return prompt.call().content();

        } catch (Exception e) {
            log.error("Error in chat without tools", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    private boolean shouldUseTools(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        String response = mcpChatClient.prompt("Analyse the user message and determine if it is needed to " +
                        "call one of the available tools or not. Always return true or false.")
                .user(lowerMessage)
                .call().content();

        return Boolean.parseBoolean(response);
    }

}