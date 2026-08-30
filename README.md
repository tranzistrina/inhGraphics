# inhGraphics

Paper 1.21.1 plugin for per-player visual environment control.

## Structure

- `plugin/` - Java 21 / Gradle plugin source
- `apps/bot/` - bot and administration tooling
- `runtime/server/` - Minecraft server runtime files
- `dist/` - built plugin artifacts
- `docs/` - specifications and project documentation
- `archive/` - historical backups

## Build

```bash
cd plugin
./gradlew build
```

Build output is generated in `plugin/build/libs/`.

## Platform

- Paper 1.21.1
- Java 21
- Gradle
