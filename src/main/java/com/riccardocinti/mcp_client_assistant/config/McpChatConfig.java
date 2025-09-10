package com.riccardocinti.mcp_client_assistant.config;

import com.riccardocinti.mcp_client_assistant.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class McpChatConfig {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant.
            You can respond in plain text for normal questions.
            You can also call tools when the user explicitly requests something that matches their purpose.
            Only output a tool call if it is required. Otherwise, answer normally.
            """;

    private final OllamaChatModel ollamaChatModel;
    private final McpService mcpService;

    @Bean
    @Primary
    public ChatClient mcpChatClient() {
        log.info("Creating ChatClient with MCP tools support");

        ChatClient chatClient = ChatClient.builder(ollamaChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();

        log.info("ChatClient created using model: {} with temperature: {}",
                ollamaChatModel.getDefaultOptions().getModel(),
                ollamaChatModel.getDefaultOptions().getTemperature());

        return chatClient;
    }

    @Bean
    public OllamaOptions mcpRuntimeOptions() {
        // Only specify what we're adding at runtime - the tools
        // Everything else (model, temperature, etc.) comes from application.yml
        return OllamaOptions.builder()
                .toolCallbacks(List.of(mcpService.getToolCallbacks()))
                .build();
    }

    @Bean
    public ChatClient debugChatClient() {
        log.info("Creating debug ChatClient");

        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public ChatClient assistantChatClient() {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are a helpful AI assistant. Use the available tools when needed to provide accurate responses.")
                .build();
    }

}
