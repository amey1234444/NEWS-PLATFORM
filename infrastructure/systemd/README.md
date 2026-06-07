Systemd timer for running `scripts/send-todays-news.ps1` via Docker

This folder contains two unit files and a brief installation guide:

Files:
- `news-poster.service` — runs the `send-todays-news.ps1` script inside a PowerShell Docker image, mounted from `/opt/news-platform`.
- `news-poster.timer` — schedules the service daily at 09:00 IST (Asia/Kolkata).

Installation (example on an Ubuntu VPS):

1. Install Docker and pull PowerShell image if needed:

   sudo apt update
   sudo apt install -y docker.io
   sudo systemctl enable --now docker

2. Copy your repository to `/opt/news-platform` on the VPS (or choose another path):

   sudo mkdir -p /opt/news-platform
   sudo rsync -a --exclude='.git' ./ /opt/news-platform/
   sudo chown -R $USER:$USER /opt/news-platform

3. Create an environment file `/etc/default/news-poster` with values:

   AI_REFINER_URL='http://localhost:8082/api/refine/test'
   LIMIT=10
   SINCE_DAYS=0

   Adjust `AI_REFINER_URL` to point at the ai-refiner service (localhost when running via Docker Compose on same host).

4. Copy the unit files to systemd and enable the timer:

   sudo cp news-poster.service /etc/systemd/system/
   sudo cp news-poster.timer /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now news-poster.timer

5. Monitor the timer and service logs:

   systemctl list-timers --all | grep news-poster
   journalctl -u news-poster.service -b

Manual run:

- Run once immediately:

  sudo systemctl start news-poster.service

Notes:
- The `OnCalendar` in the timer uses `TZ=Asia/Kolkata` so it will fire at 09:00 IST regardless of the host timezone (systemd must support TZ in OnCalendar).
- The service uses Docker to execute the PowerShell script; this avoids requiring PowerShell installed on host.
- For extra security, store secrets (API keys) in a protected file or use Docker secrets / a secrets manager and update `news-poster.service` accordingly.
