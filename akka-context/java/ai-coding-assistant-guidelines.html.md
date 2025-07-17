<!-- <nav> -->
- [Akka](../index.html)
- [AI coding assistant guidelines](ai-coding-assistant-guidelines.html)

<!-- </nav> -->

# AI coding assistant guidelines

If you are using an AI coding assistant, don’t generate any code yet, but please make sure that you refer to these guidelines when generating Akka code later.

*Patterns and conventions for consistency, maintainability, and legibility*

The following is a set of guidelines and patterns that will help keep your Akka projects consistent, predictable, and highly readable. While you’re free to choose not to follow these guidelines, they’ve been tried and tested over the years by many teams.

These guidelines apply only to the Akka SDK and do not cover usage of the lower-level Akka libraries.

## <a href="about:blank#_general"></a> General

- Use the Akka SDK version 3.4 or later.
- Use the [Project structure](../concepts/architecture-model.html) and follow `[org.application-name].[module-name].[api|application|domain]` package structure. There should be no dependencies from `domain` to `application`, and no dependencies from `application` to `api`. For example, the following are some package structures that follow these guidelines:

  - `com.northwind.trading.api`
  - `com.northwind.trading.application.StockEntity`
  - `com.acme.petclinic.domain.VisitRecord`
- Wherever possible, favor functional and fluent styles over classical imperative styles. For example, to filter a list of objects, favor `.stream().filter` rather than a mutating `for` loop.
- Use Java records for the domain model, including events, commands, requests, and responses. Records are more concise and are automatically serializable.
- Component classes should use a suffix on the class name to indicate their respective roles within the application. Domain records should not be suffixed. Suffixes can include (but are not limited to):

  - Entity
  - View
  - Workflow
  - Consumer
  - Agent
  - Endpoint

## <a href="about:blank#_domain_model"></a> Domain Model

- Prefer Java `Optional` for nullable values.
- Domain records should contain their own business logic, which includes mutation and validation.
- Prefer a domain model prefix on classes defined within the `application` or `domain` packages to differentiate. For example, an application that contains both a shopping cart and an order would be expected to have the `ShoppingCartEvent` and `OrderEvent` records.
- Never use unqualified class names like `Event`, `Workflow`, or `Agent`, unless nested inside another descriptive class or interface.
- A domain record should not emit effects, which belong to the `application` package.
- A domain record should be immutable.

  - Make a copy of collections when updating them.
  - To make single-field mutation easier, prefer the functional builder-style syntax, e.g. `withLocation(x)` that creates a new instance with the new field value rather than explicitly setting the location field

## <a href="about:blank#_entities"></a> Entities

- Command handlers only ever accept a single parameter and return effects. If a command handler needs to accept more than one parameter, those parameters should be wrapped inside a single command message.
- The command handler parameter and reply should be defined as inner record of an entity, but sometimes it makes sense to place them in the `domain` package together with other data transfer and state records. Parameter and reply can also be a plain String, Java primitive or class.
- Commands should be defined using an imperative naming convention, described as instructions of some task to be performed. For example, a command requesting a shopping cart be checked out would be `ShoppingCartEntity.Checkout`.
- Command handlers are to be implemented in the entity and not in the state or domain object. Command handlers should invoke the domain object’s validation methods and methods that encapsulate the core business logic. The entity’s command handler is responsible for choosing the appropriate effect(s) to return. The domain object should not return effects or a list of events. This keeps the domain model clean of infrastructural concerns and makes it easy to unit test in isolation.
- Command handlers that make updates without returning any information should return `akka.Done` for successful responses and `effects().error()` for validation errors.
- State of an entity should be in the `domain` package, also known as domain object.
- The `applyEvent` override should be a pure function transferring data from the event to the state and nothing else. It should never fail, because validations should have been made before persisting the event.
- Entity events should:

  - Be defined as records that implement a common sealed interface, such as `ShoppingCartEvent` with concrete events such as `ItemAdded` and `CheckedOut`.
  - Be defined in the `domain` package. While an entity’s events are only ever emitted by that entity, large enough code bases will benefit from having all of the events in an easy-to-find place.
  - Have a `@TypeName` annotation to provide a serialization key

## <a href="about:blank#_workflows"></a> Workflows

- Command handlers only ever accept a single parameter and return effects. If a command handler needs to accept more than one parameter, those parameters should be wrapped inside a single command message.
- The command handler parameter and reply should be defined as inner record of the entity, but sometimes it makes sense to place them in the `domain` package together with other data transfer and state records. Parameter and reply can also be a plain String, Java primitive or class.
- State of a workflow can be defined as a record inside the workflow, but if it contains much business logic it should be in the `domain` package, also know as domain object.
- Prefer using methods to create each step of the workflow rather than defining them directly in the `definition` method. The methods should be named the same as the step and translate from the step name strings using Java camel case, e.g. a step named `"begin-batch"` will be produced from a method named `beginBatchStep()`.
- Workflows should be explicit about timeouts and retries whenever appropriate. When calling agents the step timeout must be rather long (60 seconds) since LLM response times can be long. A workflow that orchestrates agents should define `defaultStepRecoverStrategy` with limited number of retries, to avoid too many (costly) retries to the LLM.

## <a href="about:blank#_agents"></a> Agents

- An Agent can only have one single command handler method.
- Command handlers only ever accept a single parameter and return effects. If a command handler needs to accept more than one parameter, those parameters should be wrapped inside a single command message.
- The command handler parameter and reply should be defined as inner record of the workflow, but sometimes it makes sense to place them in the `domain` package together with other data transfer and state records. Parameter and reply can also be a plain String, Java primitive or class.
- Agent classes should be stateless and not have mutable state in itself.
- The agent session id is typically a UUID to guarantee uniqueness for new agent interactions, but it can be shared between different agents that participate in the same session.
- Prefer to use a default model defined in config.

## <a href="about:blank#_views"></a> Views

- View query methods only ever accept a single parameter. If it needs more than one parameter, those parameters should be wrapped inside a single message.
- The query method parameter and reply should be defined as inner record of the view. Parameter and reply can also be a plain String, Java primitive or class.
- View row records should be public, and be named `Entry` or more specific like `ForecastEntry`.
- A query that can potentially select many rows should return a record that has a single field containing the list of results, e.g. `record ForecastEntries(List<ForecastEntry> forecasts)` where `ForecastEntries` is the type of query result.

  - The query should in that case be `SELECT * AS forecasts FROM weather_forecast WHERE …​`

## <a href="about:blank#_http_endpoints"></a> HTTP Endpoints

- Implement HTTP endpoints with the `@HttpEndpoint` and path annotations.

  - Omit root path in the `@HttpEndpoint` annotation if there is no common path for the endpoint methods.
- HTTP endpoints should not have a `@ComponentId` annotation.
- Endpoints should extend `AbstractHttpEndpoint` if they need additional request context such as headers and JWT.
- Endpoints that are only for internal use should be secured with the `service = *` attribute in the `@Acl` annotation, indicating that any service can call it but the Internet principal is not allowed.
- Request and response types should be defined as records inside the endpoint. Request and response types can also be a plain String or Java primitive.
- The endpoint should not directly return or expose domain or application types. Include a `toApi` conversion method if the response type includes many fields or nested records. `toApi` also makes for a good function to pass into a streamed mapper.
- Endpoints should return responses directly and not use `CompletionStage`, favoring a synchronous code style.
- Endpoints should prefer to return concrete, application API specific types and only use the `akka.http.javadsl.model.HttpResponse` when  needing to stream a response or return a specific status code.
- If an endpoint method creates or updates anything, it should return `HttpResponses.created()` or `HttpResponses.ok()` accordingly.
- Use the `ComponentClient` which is constructor-injected via dependency injection when communicating with other components. Use the combination of `method` followed by `invoke` to prefer a synchronous style rather than using `invokeAsync`.

## <a href="about:blank#_grpc_endpoints"></a> gRPC Endpoints

- The service and message definitions for the endpoint go in the `src/main/proto` folder in the project.
- gRPC endpoints should not have a `@ComponentId` annotation.
- gRPC endpoints should extend `AbstractGrpcEndpoint` if they need additional request context such as metadata and JWT.
- The gRPC endpoint class should implement the interface generated by the protobuf definition. A service defined as `CustomerGrpcEndpoint` in the `.proto` file will be generated in a class with the same name, `CustomerGrpcEndpoint`. This class is then extended by the developer-created endpoint class. Add the `Impl` suffix on the end of the name of the developer’s class to distinguish it from the protobuf-generated class.
- gRPC endpoints that are only for internal use should be secured with the `service = *` attribute in the `@Acl` annotation, indicating that any service can call it but the Internet principal is not allowed.
- Return the types generated by the protobuf definition from the API methods, converting from the domain objects to the protobuf types with the appropriate private `toApi` methods on the endpoint class.

## <a href="about:blank#_testing"></a> Testing

- Extend `TestKitSupport` for integration tests or component interaction testing.
- Integration tests should have the IntegrationTest suffix.
- Use `EventSourcedTestKit` for unit tests for the event sourced entity. For key value entities, use `KeyValueEntityTestKit` and for timed actions use `TimedActionTestKit`. Create a new instance of the test kit for each test method.
- Explicitly supply entity IDs for the new test kit instances to avoid confusion.
- Use Junit 5+ annotations (`@Test` etc).
- Use `assertThat` from `assertj`.
- Use `componentClient` from the `TestKitSupport` for testing components (there is no “mock” component client).
- Use `TestModelProvider` to mock responses from AI model when testing agents.

<!-- <footer> -->

<!-- </footer> -->

<!-- <aside> -->

<!-- </aside> -->