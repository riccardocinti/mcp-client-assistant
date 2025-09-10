package com.riccardocinti.mcp_client_assistant.model;

import java.util.Map;

public record HealthResponse(
        String status,
        boolean ollamaAvailable,
        boolean mcpServersHealthy,
        Map<String, Boolean> mcpServerStatus,
        OllamaInfo ollamaInfo
) {}
