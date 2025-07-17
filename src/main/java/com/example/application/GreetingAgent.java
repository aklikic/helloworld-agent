package com.example.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.agent.Agent;
import akka.javasdk.agent.AgentContext;
import akka.javasdk.agent.RemoteMcpTools;

@ComponentId("greeting-agent")
public class GreetingAgent extends Agent {

    private static final String SYSTEM_MESSAGE = 
        "You are a helpful assistant that teaches greetings in different languages. " +
        "The user must provide a language. Always append the language used in parentheses in English " +
        "(e.g., \"Bonjour (French)\"). At the end of each response, append a list of previous greetings " +
        "used in the current session. " +
        "For the first interaction with a user, display a greeting with their name (e.g., \"Hello <user name>!\"). " +
        "Use the getUserName tool to get the user's name based on their userId. " +
        "The user message format is 'userId:<userId>;question:<question>'.";

    public Effect<String> ask(String question) {
        String userId = context().sessionId();
        String formattedMessage = "userId:" + userId + ";question:" + question;
        
        return effects()
                .systemMessage(SYSTEM_MESSAGE)
                .mcpTools(RemoteMcpTools.fromService("helloworld-agent"))
                .userMessage(formattedMessage)
                .thenReply();
    }
}