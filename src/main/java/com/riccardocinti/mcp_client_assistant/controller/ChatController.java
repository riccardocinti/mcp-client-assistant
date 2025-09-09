package com.riccardocinti.mcp_client_assistant.controller;

import com.riccardocinti.mcp_client_assistant.mcp_client_assistant.model.*;
import com.riccardocinti.mcp_client_assistant.model.*;
import com.riccardocinti.mcp_client_assistant.service.McpService;
import com.riccardocinti.mcp_client_assistant.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final OllamaService ollamaService;
    private final McpService mcpService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.message());

        try {
            String response = ollamaService.chatSimple(request.message());
            return ResponseEntity.ok(new ChatResponse(response, null, true));
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Sorry, I encountered an error processing your request.",
                            e.getMessage(), false));
        }
    }

    @PostMapping("/chat/conversation")
    public ResponseEntity<ConversationResponse> chatWithConversation(@RequestBody ConversationRequest request) {
        String conversationId = request.conversationId() != null
                ? request.conversationId()
                : UUID.randomUUID().toString();

        log.info("Processing conversation chat for ID: {}", conversationId);

        try {
            String response = ollamaService.chat(request.message(), conversationId);
            return ResponseEntity.ok(new ConversationResponse(
                    response,
                    conversationId,
                    true,
                    null
            ));
        } catch (Exception e) {
            log.error("Error in conversation chat", e);
            return ResponseEntity.internalServerError()
                    .body(new ConversationResponse(
                            "Sorry, I encountered an error.",
                            conversationId,
                            false,
                            e.getMessage()
                    ));
        }
    }

    @PostMapping("/chat/system")
    public ResponseEntity<ChatResponse> chatWithSystem(@RequestBody SystemChatRequest request) {
        log.info("Chat with system prompt");

        try {
            String response = ollamaService.chatWithSystemPrompt(
                    request.systemPrompt(),
                    request.message()
            );
            return ResponseEntity.ok(new ChatResponse(response, null, true));
        } catch (Exception e) {
            log.error("Error in system chat", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Error processing request", e.getMessage(), false));
        }
    }

    @DeleteMapping("/chat/conversation/{conversationId}")
    public ResponseEntity<Map<String, Object>> clearConversation(@PathVariable String conversationId) {
        log.info("Clearing conversation: {}", conversationId);
        ollamaService.clearConversation(conversationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conversation cleared",
                "conversationId", conversationId
        ));
    }

    @GetMapping("/tools")
    public ResponseEntity<ToolsResponse> getAvailableTools() {
        var tools = mcpService.getToolCallbacks();
        var toolInfos = java.util.Arrays.stream(tools)
                .map(tool -> {
                    var def = tool.getToolDefinition();
                    return new ToolInfo(def.name(), def.description());
                })
                .toList();

        return ResponseEntity.ok(new ToolsResponse(toolInfos, toolInfos.size()));
    }

    @GetMapping("/mcp/status")
    public ResponseEntity<McpStatusResponse> getMcpStatus() {
        var serverStatus = mcpService.getServerStatus();
        var details = mcpService.getServerDetails();

        return ResponseEntity.ok(new McpStatusResponse(
                serverStatus,
                details,
                serverStatus.values().stream().filter(Boolean::booleanValue).count(),
                serverStatus.size()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        boolean ollamaAvailable = ollamaService.isOllamaAvailable();
        var mcpStatus = mcpService.getServerStatus();
        boolean allMcpHealthy = mcpStatus.values().stream().allMatch(Boolean::booleanValue);

        var health = new HealthResponse(
                ollamaAvailable && allMcpHealthy ? "UP" : "DEGRADED",
                ollamaAvailable,
                allMcpHealthy,
                mcpStatus,
                ollamaService.getOllamaInfo()
        );

        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
                "service", "MCP Client Web Service",
                "version", "1.0.0",
                "ollama", ollamaService.getOllamaInfo(),
                "mcp", Map.of(
                        "servers", mcpService.getServerStatus(),
                        "totalTools", mcpService.getToolCallbacks().length
                )
        ));
    }
}
