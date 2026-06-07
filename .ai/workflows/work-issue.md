# Workflow: Work Issue

Goal: take a GitHub issue and produce a focused, tested pull request that resolves it.

## Steps

1. Read the issue title, body and labels carefully. Restate the problem.
2. Read the repository instruction files (AGENTS.md, README, DEPLOYMENT) and obey them.
3. Identify the single Maven module involved (for AI Refiner work this is
   `ai-refiner-service`). Request only the source files you must read.
4. Produce a minimal implementation plan.
5. Implement the change in Java 21 / Spring Boot style. Return COMPLETE file
   contents for every edited or created file (full files, not snippets).
6. Add JUnit 5 unit tests under the module's `src/test/java/...` that prove the
   behaviour. Prefer pure unit tests with no Spring context, Kafka or database.
7. The change is verified with the configured Maven test command.
8. Write a clear PR title and body that explains what changed and why, and
   references the issue (e.g. "Closes #2").

## Constraints

- Keep the diff as small as possible; do not refactor unrelated code or modules.
- Never create or modify files under any `target/` directory.
- Follow the project's existing style and conventions.
- New dependencies (e.g. `spring-boot-starter-test`) must be added to that
  module's `pom.xml` with a version managed by the Spring Boot BOM already imported.
