package com.riccardocinti.mcp_client_assistant.model;

public record ConversationRequest(
        String message,
        String conversationId
) {}
