package com.example;

import akka.javasdk.testkit.TestKitSupport;
import akka.javasdk.testkit.TestModelProvider;
import akka.javasdk.testkit.TestKit;
import com.example.application.GreetingAgent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest extends TestKitSupport {

    private final TestModelProvider greetingModel = new TestModelProvider();

    @Override
    protected TestKit.Settings testKitSettings() {
        return TestKit.Settings.DEFAULT
                .withModelProvider(GreetingAgent.class, greetingModel);
    }

    @Test
    public void testGreetingAgent() {
        String fixedResponse = "Hola (Spanish)\n\nPrevious greetings in this session: None";
        greetingModel.fixedResponse(fixedResponse);
        
        var result = componentClient
                .forAgent()
                .inSession("test-session")
                .method(GreetingAgent::ask)
                .invoke("How do you say hello in Spanish?");
        
        assertThat(result).isEqualTo(fixedResponse);
    }
}