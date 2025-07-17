# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Akka SDK Java project for building AI agents. The project uses Akka SDK 3.4.6 with OpenAI GPT-4o-mini as the default AI model provider.

## Development Commands

### Build and Package
```bash
mvn clean compile
mvn package
```

### Run Locally
```bash
mvn exec:java
```

### Run with Docker Compose
```bash
docker-compose up
```
The application runs a 3-node cluster with:
- Node 1: http://localhost:9001
- Node 2: http://localhost:9002  
- Node 3: http://localhost:9003

### Testing
```bash
mvn test
mvn test -Dtest=ClassName#methodName  # Run specific test
```

## Architecture

### Package Structure
- `com.example.api` - HTTP and MCP endpoint definitions
- `com.example.application` - Application services and business logic
- `com.example.domain` - Domain models and Agent implementations

### Key Configuration
- **AI Model**: OpenAI GPT-4o-mini (configurable in `application.conf`)
- **Database**: PostgreSQL (R2DBC connection for persistence)
- **Clustering**: 3-node Akka cluster setup for local development

### Environment Variables
- `OPENAI_API_KEY` - Required for AI model access
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `HTTP_PORT` - HTTP server port (default: 9000)

## Akka SDK Patterns

### Agent Implementation
```java
@ComponentId("agent-name")
public class MyAgent extends Agent {
    private static final String SYSTEM_MESSAGE = "You are a helpful assistant";
    public Effect<String> handleQuery(String query) {
        return effects()
            .systemMessage(SYSTEM_MESSAGE)
            .userMessage(query)
            .thenReply();
    }
}
```

### HTTP Endpoints
```java
@HttpEndpoint("/api")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class ApiEndpoint extends AbstractHttpEndpoint {
    private final ComponentClient componentClient;

    public ApiEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/hello")
    public String hello(String sessionId, String question) {
        return componentClient
                .forAgent()
                .inSession(sessionId)
                .method(MyAgent::ask)
                .invokeAsync(question);
    }
}
```

### MCP Endpoints
```java
@McpEndpoint(serverName = "my-server", serverVersion = "1.0.0")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class McpEndpoint {
    @McpTool(name = "tool-name", description = "Tool description")
    public String toolMethod(String input) {
        return processInput(input);
    }
}
```

### Agent with MCP Tools
```java
@ComponentId("agent-name")
public class MyAgent extends Agent {
    private static final String SYSTEM_MESSAGE = "You are a helpful assistant";
    public Effect<String> handleQuery(String query) {
        return effects()
            .systemMessage(SYSTEM_MESSAGE)
            .mcpTools(RemoteMcpTools.fromService("service-name"))
            .userMessage(query)
            .thenReply();
    }
}
```

### Testing Patterns
- Extend `TestKitSupport` for agent tests
- Use `TestModelProvider.of("mock response")` to mock AI responses
- Test pattern:
```java
public class MyTest extends TestKitSupport {

    private final TestModelProvider mockModel = new TestModelProvider();

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT
                .withModelProvider(MyAgent.class, mockModel);
    }
    
    @Test
    public void testMyAgent() {
        String fixedResponse = "Some text";
        greetingModel.fixedResponse(fixedResponse);
        var result = componentClient
            .forAgent()
            .inSession("session-id")
            .method(MyAgent::handleQuery)
            .invoke("test query");
        
        assertThat(result).isEqualTo(fixedResponse);
    }
}
```

## Docker Development

The project includes a multi-node Docker setup for local development:
1. PostgreSQL database container
2. Three application nodes for cluster testing
3. Automatic service discovery and configuration

Use `docker-compose up` to start the full environment.

## Guidelines

### Akka Context Folder Guidelines
- You MUST follow the guidelines provided in the akka-context folder for all Akka SDK code