# AGENTS.md

Instructions for any AI agent working in this repository.

This is a **Java 21 / Spring Boot 3.2** microservices monorepo built with **Maven**.
Each service (`ai-refiner-service`, `news-fetcher-service`, `notification-service`)
is an independent Maven module with its own `pom.xml`.

When working on an issue or task:

1. Use **Java 21** and Spring Boot conventions already present in the touched module.
2. Keep changes **minimal and focused**; do not refactor unrelated code or other modules.
3. Put production code under `src/main/java/...` and tests under `src/test/java/...`
   for the **same module** you are changing.
4. Always **add or update JUnit 5 tests** that prove the new behaviour. Prefer
   plain unit tests (no Spring context / no Kafka / no database) for pure logic.
5. Add proper **error handling and logging** (SLF4J `Logger`) for failure paths.
6. Build/verify with Maven before opening a PR (see the configured test command).
7. **Never** edit files under any `target/` directory — they are generated build output.
8. Open a PR that explains what changed and why, and references the issue (e.g. "Closes #2").

This file is read first by the agent and treated as the source of truth.
