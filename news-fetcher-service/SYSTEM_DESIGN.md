News Fetcher — System Design

- Purpose: Periodically ingest RSS/Atom feeds, persist raw items, publish to Kafka topic `news.raw` for downstream processing.

- Ingestion
  - Configured feed list: `feeds.urls` in `application.yml` (comma-separated). Feeds can be managed via config or moved to DB for dynamic updates.
  - Parser: Rome (`com.rometools:rome`) to support RSS/Atom variants and robust parsing.

- Deduplication
  - Compute SHA-256 over the canonical identifier (prefer `link` when present, otherwise `title+content`).
  - Store `content_hash` on `raw_news` and avoid re-saving/publishing if hash exists. This is idempotent and simple to operate without external caches.
  - For higher scale, move dedupe checks to Redis with TTLs or a Bloom filter for memory-efficient dedupe, and/or use Kafka compacted topic for dedupe metadata.

- Persistence
  - Raw items persisted in `raw_news` (JPA). `content_hash` is unique to prevent duplicates.
  - `ddl-auto:update` used for local dev; in production prefer migrations (Flyway/Liquibase).

- Messaging
  - After saving, publish the entity to Kafka topic `news.raw` (JSON serializer). Downstream services subscribe and perform refining/indexing/notification.
  - Ensure messages are idempotent or include the `content_hash` for dedupe downstream.

- Reliability & Error Handling
  - Parser/network failures logged per-feed; failure of one feed doesn't stop others.
  - Consider retry/backoff for transient network errors and circuit-breaker for flaky feeds.

- Scaling
  - Horizontal: run multiple fetcher instances, partition work by feed assignment (e.g., consistent hashing or a coordination service), or run a single scheduler that enqueues feed jobs into a work-queue (Redis/Kafka) consumed by workers.
  - Keep processing stateless; persist metadata to allow safe restarts.

- Observability
  - Metrics: items fetched, duplicates skipped, publish failures.
  - Logs: per-feed errors and parsing warnings.

- Security
  - Sanitize/limit stored content length; avoid indexing untrusted HTML without sanitization.
  - Store API keys (if used) in env vars or secret manager.

- Deployment
  - Docker image per service; orchestration via Docker Compose for local dev, Kubernetes for production.

- Next improvements
  - Add scheduled polling, feed backoff, dynamic feed management, HTML sanitization, Redis dedupe option, and monitoring dashboards.
