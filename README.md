
# MCP Client Assistant

A Spring Boot web service that provides a REST API for interacting with AI language models through Ollama and Model Context Protocol (MCP) tools.

## Overview

This application serves as a bridge between web clients and AI capabilities, combining:
- **Ollama AI Chat Model**: Local AI model integration using Llama 3.1 8B
- **MCP (Model Context Protocol) Integration**: Tool execution through MCP servers
- **REST API**: HTTP endpoints for chat interactions and system monitoring

## Features

### Chat Capabilities
- **Simple Chat**: Direct conversation with the AI model
- **Conversational Chat**: Maintains conversation history with unique session IDs  
- **System Prompt Chat**: Custom system instructions for specialized responses
- **Smart Tool Detection**: Automatically determines when to use MCP tools based on user input

### MCP Tool Integration
- **Dynamic Tool Loading**: Automatically discovers and loads MCP server tools
- **GitHub Tools**: Integrated MCP server for GitHub operations
- **Builder Tools**: Integrated MCP server for build operations
- **Tool Status Monitoring**: Real-time status of connected MCP servers

### Monitoring & Health
- **Health Checks**: Monitor Ollama and MCP server connectivity
- **Tool Discovery**: List available tools from all connected MCP servers
- **Server Status**: Track connection status of individual MCP servers
- **Actuator Endpoints**: Spring Boot management endpoints for monitoring

## API Endpoints

### Chat Operations
- `POST /api/chat` - Simple chat interaction
- `POST /api/chat/conversation` - Chat with conversation history
- `POST /api/chat/system` - Chat with custom system prompt
- `DELETE /api/chat/conversation/{id}` - Clear conversation history

### System Information  
- `GET /api/health` - Application and service health status
- `GET /api/info` - Service information and configuration
- `GET /api/tools` - List available MCP tools
- `GET /api/mcp/status` - MCP server connection status

## Configuration

The application is configured via `application.yaml`:

### Ollama Configuration
- **Model**: Llama 3.1 8B
- **Base URL**: http://localhost:11434
- **Timeout**: 300 seconds

### MCP Server Configuration
- **GitHub Server**: `./servers/mcp-server-github-0.0.1-SNAPSHOT.jar`
- **Builder Server**: `./servers/mcp-server-builder-0.0.1-SNAPSHOT.jar`
- **Request Timeout**: 60 seconds

## Requirements

- **Java 21**
- **Maven 3.6+**
- **Ollama** running locally on port 11434 with Llama 3.1 8B model
- **MCP Server JARs** placed in `./servers/` directory

## Getting Started

1. **Install Ollama** and pull the required model:
   ```bash
   ollama pull llama3.1:8b
   ```

2. **Create servers directory** and add MCP server JARs:
   ```bash
   mkdir servers
   # Copy your MCP server JARs to ./servers/
   ```

3. **Build and run**:
   ```bash
   mvn clean package
   java -jar target/mcp-client-assistant-0.0.1-SNAPSHOT.jar
   ```

4. **Access the service** at http://localhost:8080

## Example Usage

### Simple Chat
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, how are you?"}'
```

### Chat with Tools
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "List files in the current directory"}'
```

### Health Check
```bash
curl http://localhost:8080/api/health
```

## Architecture

- **Spring Boot 3.5.5** with Java 21
- **Spring AI** for LLM integration and MCP client support
- **Ollama Integration** for local AI model execution
- **MCP Client** for tool execution via external servers
- **RESTful API** design with JSON request/response
- **Conversation Management** with in-memory session storage

## License

This project is licensed under the terms specified in the LICENSE file.