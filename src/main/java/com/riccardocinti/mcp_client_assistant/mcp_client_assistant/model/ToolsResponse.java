package com.riccardocinti.mcp_client_assistant.mcp_client_assistant.model;

public record ToolsResponse(
        java.util.List<ToolInfo> tools,
        int count
) {}