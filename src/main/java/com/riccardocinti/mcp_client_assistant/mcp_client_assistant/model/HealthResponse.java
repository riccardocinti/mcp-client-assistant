package com.riccardocinti.mcp_client_assistant.mcp_client_assistant.model;

import com.riccardocinti.mcp_client_assistant.mcp_client_assistant.service.OllamaService;

import java.util.Map;

public record HealthResponse(
        String status,
        boolean ollamaAvailable,
        boolean mcpServersHealthy,
        Map<String, Boolean> mcpServerStatus,
        OllamaService.OllamaInfo ollamaInfo
) {}
