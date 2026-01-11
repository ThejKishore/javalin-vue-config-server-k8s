# pltform-mgmt — Platform Configuration Management

This repository contains a Kotlin + Javalin backend and a small Vue 3 static frontend (served from the backend). It's a platform configuration management app used to onboard services, manage properties, and view audit history.

## Quick start — build & run

```bash
# Build the project and run checks
./gradlew build

# Run the application (production-style)
java -jar build/libs/pltform-mgmt.jar

# Or run in development mode
./gradlew run
```

Application URL (default): [http://localhost:7070](http://localhost:7070)

## Project layout (important folders)

- `src/main/kotlin` — app entry and common code
- `configserver/src/main/kotlin` — controllers, services, repositories
- `shared/src/main/kotlin` — shared domain models
- `src/main/resources/public` — frontend static files (HTML, JS, CSS)
- `documents/` — all markdown documentation (developer guides, testing, architecture)

## Developer workflow

- Edit backend Kotlin code → `./gradlew build` to compile
- Edit frontend static files under `src/main/resources/public` → refresh browser to see changes
- No separate frontend build step; static files are served by the backend

## Tests & checks

```bash
# Run checks and unit tests
./gradlew check

# Clean build artifacts
./gradlew clean
```

## Documentation (start here)

All documentation was moved to `documents/`. Key files:

- `documents/README.md` — Developer quick start (this project)
- `documents/QUICK_START_TESTING.md` — Quick testing guide for UI
- `documents/IMPLEMENTATION_GUIDE.md` — How components work & customization
- `documents/DELETE_FEATURE_TESTING.md` — Test cases for delete feature
- `documents/FINAL_STATUS_REPORT.md` — Final status & verification
- `documents/START_HERE.md` — High-level index and reading paths

## Contributing

- Follow existing project style (Kotlin and ES6)
- Add docs under `documents/` and update `DOCUMENTATION_INDEX.md`
- Create small, focused commits

## Need help?

- Check `documents/` for detailed guides and testing instructions
- For runtime issues, inspect server logs or browser console

## Notes

- Gradle wrapper (`./gradlew`) is included — use it for reproducible builds
- This project uses an in-memory H2 DB for local/testing environments

---

Last updated: January 10, 2026
