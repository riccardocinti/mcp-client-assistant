package com.riccardocinti.mcp_client_assistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
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
public class OllamaService {

    private final ChatClient mcpChatClient;

    private final OllamaOptions mcpRuntimeOptions;

    private final OllamaChatModel ollamaChatModel;

    private final Map<String, List<String>> conversationHistoryStrings = new ConcurrentHashMap<>();

    public String chat(String userMessage, String conversationId) {
        log.info("Processing chat message for conversation: {}", conversationId);

        // Get or create conversation history as strings
        List<String> history = conversationHistoryStrings.computeIfAbsent(
                conversationId,
                k -> new ArrayList<>()
        );

        try {
            // Build conversation context from history
            StringBuilder fullContext = new StringBuilder();
            for (String msg : history) {
                fullContext.append(msg).append("\n");
            }
            fullContext.append("User: ").append(userMessage);

            // Use the pre-configured ChatClient with MCP tools
            String response = mcpChatClient.prompt()
                    .user(fullContext.toString())
                    .options(mcpRuntimeOptions)  // Add MCP tools at runtime
                    .call()
                    .content();

            // Add to history
            history.add("User: " + userMessage);
            history.add("Assistant: " + response);

            // Trim history if it gets too long
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

    public String chatSimple(String userMessage) {
        log.info("Smart chat processing: {}", userMessage);

        boolean useTools = shouldUseTools(userMessage);
        log.debug("Tools needed: {}", useTools);

        if (useTools) {
            return chatWithTools(userMessage);
        } else {
            return chatWithoutTools(userMessage);
        }
//        log.info("Processing simple chat message");
//
//        try {
//            // Use the fluent API with MCP tools as runtime options
//            // All other settings (model, temperature, etc.) come from application.yml
//            return mcpChatClient.prompt()
//                    .user(userMessage)
//                    .options(mcpRuntimeOptions)  // Add MCP tools at runtime
//                    .call()
//                    .content();
//
//        } catch (Exception e) {
//            log.error("Error in simple chat", e);
//            throw new RuntimeException("Failed to get response from Ollama", e);
//        }
    }

    public String chatWithTools(String userMessage) {
        log.info("Processing chat WITH tools");

        try {
            // Include system prompt that encourages tool use when appropriate
            return mcpChatClient.prompt()
                    .user(userMessage)
                    .options(mcpRuntimeOptions)  // Include MCP tools
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Error in chat with tools", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public String chatWithoutTools(String userMessage) {
        log.info("Processing chat WITHOUT tools");

        try {
            // No tools, just direct response
            return mcpChatClient.prompt()
                    .user(userMessage)
                    // No .options() call - no tools available
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Error in chat without tools", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        log.info("Processing chat with system prompt");

        try {
            // Use the fluent API with custom system prompt and MCP tools
            return mcpChatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .options(mcpRuntimeOptions)  // Add MCP tools at runtime
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("Error in chat with system prompt", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public ChatResponse chatWithMetadata(String userMessage) {
        log.info("Processing chat with metadata");

        try {
            return mcpChatClient.prompt()
                    .user(userMessage)
                    .options(mcpRuntimeOptions)  // Add MCP tools at runtime
                    .call()
                    .chatResponse();

        } catch (Exception e) {
            log.error("Error getting chat response with metadata", e);
            throw new RuntimeException("Failed to get response from Ollama", e);
        }
    }

    public void clearConversation(String conversationId) {
        conversationHistoryStrings.remove(conversationId);
        log.info("Cleared conversation history for: {}", conversationId);
    }

    public List<String> getConversationHistory(String conversationId) {
        return conversationHistoryStrings.getOrDefault(conversationId, new ArrayList<>());
    }

    public boolean isOllamaAvailable() {
        try {
            // Try a simple ping-like request
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

    public OllamaInfo getOllamaInfo() {
        return new OllamaInfo(
                ollamaChatModel.getDefaultOptions().getModel(),
                ollamaChatModel.getDefaultOptions().getTemperature(),
                0,  // Tool count would need to come from McpService if needed
                isOllamaAvailable()
        );
    }

    private boolean shouldUseTools(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Keywords that suggest tool usage might be needed
        List<String> toolKeywords = List.of(
                "file", "directory", "folder", "list", "create", "delete", "write",
                "search", "find", "web", "internet", "google",
                "database", "query", "sql",
                "weather", "temperature", "forecast",
                "calculate", "compute", "math",
                "api", "fetch", "get", "post",
                "current", "latest", "now", "today"
        );

        return toolKeywords.stream().anyMatch(lowerMessage::contains);
    }

    public record OllamaInfo(
            String model,
            Double temperature,
            int availableTools,
            boolean isAvailable
    ) {
    }

}