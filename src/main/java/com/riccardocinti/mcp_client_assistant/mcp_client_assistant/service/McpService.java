package com.riccardocinti.mcp_client_assistant.mcp_client_assistant.service;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpService {

    /**
     * List of MCP sync clients auto-configured by Spring AI.
     * Each client represents a connection to an MCP server.
     */
    private final List<McpSyncClient> mcpSyncClients;

    /**
     * Tool callback provider that wraps all MCP tools.
     * This is auto-configured by Spring AI and provides tools from all MCP servers.
     */
    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    @PostConstruct
    public void initialize() {
        log.info("=== MCP Service Initialization ===");
        logAvailableServers();
        logAvailableTools();
    }

    /**
     * Gets all available tool callbacks from MCP servers.
     * These can be passed to the Ollama chat model for function calling.
     *
     * @return Array of tool callbacks
     */
    public ToolCallback[] getToolCallbacks() {
        return syncMcpToolCallbackProvider.getToolCallbacks();
    }

    /**
     * Gets the tool callback provider for use with AI models.
     *
     * @return Tool callback provider
     */
    public ToolCallbackProvider getToolCallbackProvider() {
        return syncMcpToolCallbackProvider;
    }

    /**
     * Gets the status of all MCP servers.
     *
     * @return Map of server names to connection status
     */
    public Map<String, Boolean> getServerStatus() {
        return mcpSyncClients.stream()
                .collect(Collectors.toMap(
                        client -> client.getServerInfo().name(),
                        McpSyncClient::isInitialized
                ));
    }

    /**
     * Gets a summary of available tools for debugging.
     *
     * @return Human-readable summary
     */
    public String getToolsSummary() {
        var tools = syncMcpToolCallbackProvider.getToolCallbacks();
        var summary = new StringBuilder("MCP Tools Summary:\n");
        summary.append(String.format("Total tools available: %d\n", tools.length));

        for (ToolCallback tool : tools) {
            var definition = tool.getToolDefinition();
            summary.append(String.format("  - %s: %s\n",
                    definition.name(),
                    definition.description()));
        }

        return summary.toString();
    }

    /**
     * Gets detailed information about each MCP server.
     *
     * @return Map of server names to their details
     */
    public Map<String, ServerInfo> getServerDetails() {
        return mcpSyncClients.stream()
                .collect(Collectors.toMap(
                        client -> client.getServerInfo().name(),
                        client -> new ServerInfo(
                                client.getServerInfo().name(),
                                client.getServerInfo().version(),
                                client.isInitialized(),
                                countToolsForClient(client)
                        )
                ));
    }

    private int countToolsForClient(McpSyncClient client) {
        try {
            // The client should have methods to list tools
            // This is a simplified approach - actual implementation depends on the API
            return client.listTools().tools().size();
        } catch (Exception e) {
            log.error("Failed to count tools for client: {}", client.getServerInfo().name(), e);
            return 0;
        }
    }

    private void logAvailableServers() {
        if (mcpSyncClients.isEmpty()) {
            log.warn("No MCP servers configured or available");
        } else {
            log.info("MCP servers available: {}",
                    mcpSyncClients.stream()
                            .map(client -> client.getServerInfo().name())
                            .collect(Collectors.joining(", ")));
        }
    }

    private void logAvailableTools() {
        var tools = syncMcpToolCallbackProvider.getToolCallbacks();
        log.info("Total MCP tools available: {}", tools.length);

        if (log.isDebugEnabled()) {
            Arrays.stream(tools).forEach(tool -> {
                var definition = tool.getToolDefinition();
                log.debug("  - {}: {}", definition.name(), definition.description());
            });
        }
    }

    /**
     * Record for server information.
     */
    public record ServerInfo(
            String name,
            String version,
            boolean connected,
            int toolCount
    ) {
    }

}
