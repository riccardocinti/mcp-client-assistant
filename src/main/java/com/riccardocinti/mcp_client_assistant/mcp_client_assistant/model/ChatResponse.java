package com.riccardocinti.mcp_client_assistant.mcp_client_assistant.model;

public record ChatResponse(
        String response,
        String error,
        boolean success
) {}
