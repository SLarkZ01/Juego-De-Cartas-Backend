# Docker instructions

This repository includes Docker assets to run the backend and MongoDB locally.

Build and run with Docker Compose:

```powershell
# Build and start services
docker compose up --build

# Stop and remove
docker compose down
```

Notes:
- The backend listens on port 8080 and MongoDB on 27017.
- The backend is configured to connect to `mongodb:27017` when run in Docker Compose via `SPRING_DATA_MONGODB_URI` environment.
