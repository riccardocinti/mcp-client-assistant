package com.riccardocinti.mcp_client_assistant.model;

import com.riccardocinti.mcp_client_assistant.service.McpService;

import java.util.Map;

public record McpStatusResponse(
        Map<String, Boolean> serverStatus,
        Map<String, McpService.ServerInfo> serverDetails,
        long connectedCount,
        long totalCount
) {}
