package com.riccardocinti.mcp_client_assistant.model;

public record SystemChatRequest(
        String message,
        String systemPrompt
) {}
