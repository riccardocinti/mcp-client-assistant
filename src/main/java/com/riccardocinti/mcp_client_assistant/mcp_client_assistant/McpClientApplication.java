package com.riccardocinti.mcp_client_assistant.mcp_client_assistant;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@Slf4j
public class McpClientApplication {

    @Value("${server.port:8080}")
    private int serverPort;

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("MCP Client Web Service Starting");
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("Service will be available at: http://localhost:{}", serverPort);
        log.info("========================================");

        // Verify servers directory exists
        var serversDir = new java.io.File("./servers");
        if (serversDir.exists() && serversDir.isDirectory()) {
            var files = serversDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null && files.length > 0) {
                log.info("Found {} MCP server JAR(s) in servers directory:", files.length);
                for (var file : files) {
                    log.info("  - {}", file.getName());
                }
            }
        } else {
            log.warn("Servers directory not found at: {}", serversDir.getAbsolutePath());
            log.warn("Please create the 'servers' directory and add your MCP server JARs");
        }
    }
}
