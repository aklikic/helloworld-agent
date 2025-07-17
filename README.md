# Prerequisits 
* Java 21 installed
* Apache Maven
* Docker (for docker-compose run & deployment)
* OpenAI API key (set `OPENAI_API_KEY` envirtonment variable)
# Setup

```shell
akka code init helloworld-agent
```
Open Claude in IDE terminal:
# Generate an Agent

```text
Create an Akka SDK agent named `GreetingAgent` that assists users in learning greetings in different languages.
- **Input:** A string question.
- **Output:** Returns an `Effect<String>`.
- method with name `ask`
- Do not perform any environment variables check.
- Do not add JavaDoc annotations
- place it into com.example.application package
- system message should be a static variable
- **Guidelines for system prompt:**
  - The user must provide a language.
  - Always append the language used in parentheses in English (e.g., "Bonjour (French)").
  - At the end of each response, append a list of previous greetings used in the current session.
```

# Test the Agent

```text
Develop only one integration test for `GreetingAgent` in the `com.example` package, named `IntegrationTest`.
- Include a `TestModelProvider` to mock the model with a fixed response.
- Ensure all necessary classes are properly imported.
```

Run test:
```shell
mvn verify
```

# Add endpoint
```text
Implement an HTTP endpoint `GreetingAgentEndpoint` with the following specifications:
- **Base path:** `/chat`
- **Endpoint:** POST `/ask`
- **Request:** Accepts a JSON record `QueryRequest` with fields `String userId` and `String question`. 
- Put the QueryRequest in the GreetingAgentEndpoint class
- The agent should use `userId` as the sessionId and `question` as the user message.
- add acl for all access
```

# Run locally
Run the service:
```shell
mvn compile exec:java
```
Run Akka Console:
```shell
akka local console
```

# Test locally
Execute cURLs:
```shell
curl -X POST http://localhost:9000/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Tyler", "question": "How do I say hello in German?"}'
```
```shell
curl -X POST http://localhost:9000/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Tyler", "question": "How do I say hello in Spanish?"}'
```
```shell
curl -X POST http://localhost:9000/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Tyler", "question": "How do I say hello in Dutch?"}'
```

Explore the (Local Console)[http://localhost:9889]

## Docker Development

The project includes a multi-node Docker setup for local development:
1. PostgreSQL database container
2. Three application nodes for cluster testing
3. Automatic service discovery and configuration

Use `docker-compose up` to start the full environment.

## Guidelines

### Akka Context Folder Guidelines
- You MUST follow the guidelines provided in the akka-context folder for all Akka SDK code

# Run self-managed using docker-compose

## Package

```shell
mvn clean install -DskipTests -Pstandalone
```
Update `docker-compose.yml` with the correct image tag.<br>

## Run
Run the docker-compose:
```shell
docker compose up
```

## Test
### Execute cURLs
```shell
curl -X POST http://localhost:9001/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Tyler", "question": "How do I say hello in German?"}'
```
```shell
curl -X POST http://localhost:9002/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Alan", "question": "How do I say hello in Spanish?"}'
```

### Kill one instance
```shell
docker compose kill helloworld-agent-1
```
### Execute cURLs
```shell
curl -X POST http://localhost:9002/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Tyler", "question": "How do I say hello in German?"}'
```
```shell
curl -X POST http://localhost:9002/chat/ask \
-H "Content-Type: application/json" \
-d '{"userId": "Alan", "question": "How do I say hello in Spanish?"}'
```

### Clean up
```shell
docker compose down
```