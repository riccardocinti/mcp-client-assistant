package com.riccardocinti.mcp_client_assistant.model;

public record ConversationResponse(
        String response,
        String conversationId,
        boolean success,
        String error
) {}
