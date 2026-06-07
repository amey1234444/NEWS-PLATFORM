Deployment & Scheduling Guide

Options provided in this repository:

1) GitHub Actions (recommended for reliability)
- A workflow `.github/workflows/daily-news.yml` runs `scripts/send-todays-news.ps1` on `windows-latest`.
- It's configured to run daily at 09:00 IST (see cron in the workflow).
- You can also trigger it manually from the Actions UI or via `workflow_dispatch` inputs.
Quick setup:
- The workflow now runs a self-contained Docker Compose job by default (mode=`compose`) for scheduled runs.
- Required repository secrets for the default compose run:
  - `TELEGRAM_BOT_TOKEN` — Telegram bot token used by the `notification` service.
  - `LLM_API_KEY` — optional, used by `ai-refiner` if configured.

Set the secrets with the GitHub CLI:

```bash
gh secret set TELEGRAM_BOT_TOKEN --body '<your-telegram-bot-token>'
gh secret set LLM_API_KEY --body '<your-llm-api-key>'
```

Manual runs:
- Default (compose) manual run — runs the self-contained compose job (recommended):

```bash
gh workflow run daily-news.yml --ref main
```

- Direct/manual run (target an external ai-refiner URL):

```bash
gh workflow run daily-news.yml --ref main --field mode=direct --field aiRefinerUrl='https://your-host:8082/api/refine/test' --field limit=10 --field sinceDays=0
```


2) VPS deployment with Docker Compose + systemd timer (runs script on host daily at 09:00 IST)

- `infrastructure/docker-compose.news.yml` contains example service definitions for `ai-refiner`, `news-fetcher`, `notification`, and a manual `news-poster` runner.
- To run services:

  docker compose -f infrastructure/docker-compose.news.yml up -d --build

- To schedule the `send-todays-news.ps1` script on a Linux VPS (uses Docker to execute the script):

  - Copy the repo to `/opt/news-platform`.
  - Create `/etc/default/news-poster` with `AI_REFINER_URL`, `LIMIT`, `SINCE_DAYS`.
  - Copy `infrastructure/systemd/news-poster.service` and `infrastructure/systemd/news-poster.timer` to `/etc/systemd/system/`.
  - Run: `sudo systemctl daemon-reload && sudo systemctl enable --now news-poster.timer`.

- Check logs with `journalctl -u news-poster.service`.

Notes & security:
- Keep secrets out of source control — use GitHub Secrets for Actions and environment files with restrictive permissions on VPS.
- If you host the services in Docker Compose on the same host, `AI_REFINER_URL` should point to `http://ai-refiner:8082/api/refine/test` when the script runs inside a container network, or `http://localhost:8082/api/refine/test` when the timer runs `docker run` mounting the repo.
