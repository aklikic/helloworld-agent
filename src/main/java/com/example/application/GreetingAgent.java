package com.example.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.agent.Agent;
import akka.javasdk.agent.AgentContext;

@ComponentId("greeting-agent")
public class GreetingAgent extends Agent {

    private static final String SYSTEM_MESSAGE = 
        "You are a helpful assistant that teaches greetings in different languages. " +
        "The user must provide a language. Always append the language used in parentheses in English " +
        "(e.g., \"Bonjour (French)\"). At the end of each response, append a list of previous greetings " +
        "used in the current session.";

    public Effect<String> ask(String question) {
        return effects()
                .systemMessage(SYSTEM_MESSAGE)
                .userMessage(question)
                .thenReply();
    }
}