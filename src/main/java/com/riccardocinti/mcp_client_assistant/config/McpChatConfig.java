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

    /**
     * Creates a ChatClient bean that uses the auto-configured Ollama model.
     * MCP tools are added as runtime options when making calls.
     *
     * @return ChatClient configured to use MCP tools
     */
    @Bean
    @Primary
    public ChatClient mcpChatClient() {
        log.info("Creating ChatClient with MCP tools support");

        // Just create a ChatClient with the auto-configured model
        // The model already has all settings from application.yml
        ChatClient chatClient = ChatClient.builder(ollamaChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                // We'll add MCP tools at runtime for each call
                .build();

        log.info("ChatClient created using model: {} with temperature: {}",
                ollamaChatModel.getDefaultOptions().getModel(),
                ollamaChatModel.getDefaultOptions().getTemperature());

        return chatClient;
    }

    /**
     * Creates runtime options with MCP tools.
     * These options only include the tools - all other settings come from application.yml.
     *
     * @return OllamaOptions with only MCP tools configured
     */
    @Bean
    public OllamaOptions mcpRuntimeOptions() {
        // Only specify what we're adding at runtime - the tools
        // Everything else (model, temperature, etc.) comes from application.yml
        return OllamaOptions.builder()
                .toolCallbacks(List.of(mcpService.getToolCallbacks()))
                .build();
    }

    /**
     * Creates a ChatClient with debug logging enabled.
     *
     * @return ChatClient with debug advisor
     */
    @Bean
    public ChatClient debugChatClient() {
        log.info("Creating debug ChatClient");

        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * Creates a ChatClient with a helpful assistant system prompt.
     *
     * @return ChatClient with system prompt
     */
    @Bean
    public ChatClient assistantChatClient() {
        return ChatClient.builder(ollamaChatModel)
                .defaultSystem("You are a helpful AI assistant. Use the available tools when needed to provide accurate responses.")
                .build();
    }

}
