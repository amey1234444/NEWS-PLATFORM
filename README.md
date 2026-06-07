# News Platform (scaffold)

Phased scaffold for the news-platform project. This workspace contains initial infrastructure and three minimal Spring Boot service skeletons to start development incrementally.

Quick start

1. Start infra services:

```bash
cd infrastructure
docker compose up -d
docker ps
```

2. Build a service and run locally with Maven:

```bash
cd news-fetcher-service
mvn spring-boot:run
```

Files added

- `infrastructure/docker-compose.yml`
- `common-protos/prompt.proto`
- `news-fetcher-service/` (skeleton)
- `ai-refiner-service/` (skeleton)
- `notification-service/` (skeleton)

Next steps: wire Kafka topics, implement fetcher logic (RSS), add AI integration in `ai-refiner-service`, add notification sinks.

LLM configuration & test

- Set an API key for the LLM provider (OpenAI-compatible) via environment variable `OPENAI_API_KEY` or Spring property `llm.api.key` in `ai-refiner-service/src/main/resources/application.yml`.
- Example run (requires infra from `infrastructure/docker-compose.yml`):

```bash
# start infra (Kafka, Postgres, Redis)
cd infrastructure
docker compose up -d

# run services (example)
cd news-fetcher-service && mvn spring-boot:run
cd ../ai-refiner-service && mvn spring-boot:run
cd ../notification-service && mvn spring-boot:run
```

- Quick manual refine test (POST to the test endpoint):

```bash
curl -X POST http://localhost:8082/api/refine/test \
	-H "Content-Type: application/json" \
	-d '{"title":"Test news","content":"This is a short news content to refine.","source":"unit-test"}'
```

If an API key is not configured the refiner will fall back to a minimal summary using the raw content. Check the `ai-refiner-service` logs for warnings about missing API key or LLM errors.

OpenRouter (Java / Spring Boot)

- OpenRouter is OpenAI-compatible; you can use it by pointing the refiner to the OpenRouter endpoint or providing an OpenRouter API key in env.
- Recommended env vars / properties:

	- `OPENROUTER_API_KEY` — preferred environment variable for OpenRouter key.
	- `OPENROUTER_API_URL` — optional custom base URL (defaults to `https://openrouter.ai/api/v1/chat/completions` when set).
	- Alternatively set Spring properties in `ai-refiner-service/src/main/resources/application.yml`:

```yaml
llm:
	api:
		url: ${LLM_API_URL:https://openrouter.ai/api/v1/chat/completions}
		key: ${OPENROUTER_API_KEY:}
	model: ${LLM_MODEL:google/gemini-2.5-flash}
```

- The `LlmClient` in `ai-refiner-service` will prefer `llm.api.key`, then `OPENROUTER_API_KEY`, then `OPENAI_API_KEY`.

- Example PowerShell to run the refiner with OpenRouter (replace the placeholder with your real key — do not commit it):

```powershell
$env:OPENROUTER_API_KEY = 'sk-REPLACE_WITH_YOUR_KEY'
cd ai-refiner-service
mvn spring-boot:run
```

- Or set `OPENROUTER_API_URL` if you need a different base URL.

Security note: Never commit API keys into source control. Use env vars, secret managers, or container secrets.

Feeds & end-to-end testing

- Where the fetcher gets news: the `news-fetcher-service` reads RSS feed URLs from `news-fetcher-service/src/main/resources/application.yml` under the `feeds.urls` property. By default this workspace contains:

	- https://news.google.com/rss
	- https://rss.cnn.com/rss/edition.rss

	Add more comma-separated feed URLs to that property to ingest additional sources.

- How the fetcher runs: the fetcher polls feeds periodically using the `fetcher.pollIntervalMs` and concurrency settings in the same `application.yml`. It persists raw items and publishes them to Kafka topic `news.raw` for downstream processing.

- Full end-to-end test (recommended, requires Docker/Kafka):

	1. Start infra (Kafka, Postgres, Redis):

	```bash
	cd infrastructure
	docker compose up -d
	```

	2. Start services (fetcher, refiner, notification):

	```bash
	# in separate shells or use a process manager
	cd news-fetcher-service && mvn spring-boot:run
	cd ../ai-refiner-service && mvn spring-boot:run
	cd ../notification-service && mvn spring-boot:run
	```

	3. Verify the fetcher publishes to Kafka: watch `news-fetcher-service` logs for publish messages or use Kafka console consumer:

	```bash
	kafka-console-consumer --bootstrap-server localhost:9092 --topic news.raw --from-beginning
	```

	4. Verify `ai-refiner-service` consumes `news.raw` and saves `RefinedNews` rows; it will publish to topic `news.refined`.

	5. Verify `notification-service` processes `news.refined` messages and delivers notifications (check logs or the configured Telegram sink).

- Quick local test without Docker (what I used here):

	- Run `ai-refiner-service` with an in-memory H2 DB and `OPENROUTER_API_KEY` set as an environment variable, then call the test endpoint:

	```powershell
	$env:OPENROUTER_API_KEY = 'sk-REPLACE_WITH_YOUR_KEY'
	$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:newsdb;DB_CLOSE_DELAY=-1'
	cd ai-refiner-service
	mvn spring-boot:run

	curl -X POST http://localhost:8082/api/refine/test \
		-H "Content-Type: application/json" \
		-d '{"title":"Test news","content":"This is a short news content to refine.","source":"unit-test"}'
	```

If you want, I can create a safe `application-local.yml` template (without secrets) and add it to `.gitignore` so you can drop keys locally without committing them.
