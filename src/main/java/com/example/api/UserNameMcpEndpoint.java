package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.mcp.McpEndpoint;
import akka.javasdk.annotations.mcp.McpTool;
import java.util.List;
import java.util.Random;

@McpEndpoint(serverName = "username-server", serverVersion = "1.0.0")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class UserNameMcpEndpoint {

    private final List<String> names = List.of(
        "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", 
        "Grace", "Henry", "Ivy", "Jack", "Kate", "Leo", 
        "Mia", "Noah", "Olivia", "Paul", "Quinn", "Ruby", 
        "Sam", "Tina", "Uma", "Victor", "Wendy", "Xander", 
        "Yara", "Zoe"
    );

    private final Random random = new Random();

    @McpTool(name = "getUserName", description = "Retrieves a user's name based on their userId")
    public String getUserName(String userId) {
        return names.get(random.nextInt(names.size()));
    }
}