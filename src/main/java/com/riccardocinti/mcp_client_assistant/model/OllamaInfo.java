package com.riccardocinti.mcp_client_assistant.model;

public record OllamaInfo(
        String model,
        Double temperature,
        int availableTools,
        boolean isAvailable
) {
}
