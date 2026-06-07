# Testing rules

- Use **JUnit 5** (`org.junit.jupiter`). Add `spring-boot-starter-test` (scope
  `test`) to the module `pom.xml` if it is not already present; its version is
  managed by the imported Spring Boot BOM, so do not hardcode a version.
- Prefer **pure unit tests**: instantiate the class under test directly and
  assert on its outputs. Do not start a Spring context, Kafka broker or database.
- Cover the acceptance criteria explicitly: valid input, empty input, malformed
  input, and the fallback path.
- Tests live under `src/test/java/<same package>/` within the module being changed.
