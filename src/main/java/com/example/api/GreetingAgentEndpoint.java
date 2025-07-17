package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.client.ComponentClient;
import com.example.application.GreetingAgent;
import java.util.concurrent.CompletionStage;

@HttpEndpoint("/chat")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class GreetingAgentEndpoint extends AbstractHttpEndpoint {

    private final ComponentClient componentClient;

    public GreetingAgentEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public record QueryRequest(String userId, String question) {}

    @Post("/ask")
    public CompletionStage<String> ask(QueryRequest request) {
        return componentClient
                .forAgent()
                .inSession(request.userId())
                .method(GreetingAgent::ask)
                .invokeAsync(request.question());
    }
}