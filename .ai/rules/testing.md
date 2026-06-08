# Testing rules

This is a Maven multi-module repo **without a root reactor pom** and the modules
do **not** use `spring-boot-starter-parent`. That has two consequences you MUST
respect when adding tests to a module (e.g. `ai-refiner-service`):

## 1. Test command

Always verify with this EXACT command (run from the repo root):

```
mvn -q -f ai-refiner-service/pom.xml test
```

Do NOT use `-pl ai-refiner-service` — there is no aggregator pom, so `-pl` fails
with "Could not find the selected project in the reactor".

## 2. Required pom.xml additions (and nothing else)

The module `pom.xml` already imports the Spring Boot BOM via
`<dependencyManagement>`. To run JUnit 5 tests you must make EXACTLY these two
additive changes and leave the rest of the pom untouched:

a) Add the test dependency inside the existing top-level `<dependencies>` block
   (NOT inside `<dependencyManagement>`):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

b) Add the Surefire plugin inside `<build><plugins>` (Maven's default Surefire
   is too old to run JUnit 5, and there is no parent to manage its version):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
</plugin>
```

## Hard constraints on pom.xml

- Do NOT add a `<parent>` element. The module has none and `news-platform` does
  not exist.
- Do NOT remove the `spring-boot-dependencies` BOM import, the `<groupId>`,
  `<version>`, existing dependencies, or existing plugins.
- Only the two additive changes above are allowed.

## Test design

- Use **JUnit 5** (`org.junit.jupiter.api`).
- Prefer **pure unit tests**: instantiate the class under test directly and
  assert on its outputs. Do not start a Spring context, Kafka broker or database.
- Cover the acceptance criteria explicitly: valid input, empty input, malformed
  input, and the fallback path.
- Tests live under `src/test/java/<same package>/` within the module being changed.
