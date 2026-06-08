# Java style rules

- Target **Java 21**; modern, readable code. Match the surrounding style.
- Use constructor injection for Spring components (no field `@Autowired`).
- Log with SLF4J (`org.slf4j.Logger` via `LoggerFactory.getLogger(...)`); log
  warnings on recoverable failures and include context.
- Keep new logic in small, testable units (plain classes/methods) rather than
  burying it inside Kafka listeners or controllers.
- Never edit generated output under `target/`.

## Java regex / string-literal escaping (important)

- In Java string literals, every regex backslash must be DOUBLED. Use `"\\s+"`,
  `"\\n{2,}"`, `"\\[(.*?)\\]\\(.*?\\)"` — never `"\s+"` or `"\n{2,}"` (those are
  illegal escape characters and will not compile).
- Call `Pattern.compile(...)` exactly once — never `Pattern.Pattern.compile(...)`.
