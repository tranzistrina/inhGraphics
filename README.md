# inhGraphics

Paper 26.2 plugin for per-player time, weather, render distance and particle fog.

## Structure

- `plugin/` — Paper plugin source
- `client/inhClientPATHgraphics/` — optional Fabric client companion source and jar
- `apps/` — bot and administration tooling
- `docs/` — specifications and project documentation
- `dist/` — built server plugin artifacts

## Build

```bash
cd plugin
gradle build
```

Build output is generated in `plugin/build/libs/`.

Platform: Paper 26.2, Java 25.

## Bobby / Distant Horizons / Voxy compatibility

These mods keep their own client-side chunk/LOD caches. Paper can limit the chunks it sends, but it cannot erase or disable a cache inside an unmodified client. The server plugin therefore keeps its original behavior for vanilla clients and sends an `inhgraphics:sync` payload when `/igrender` changes.

For clients using Bobby, Distant Horizons or Voxy, install the optional client companion from:

`client/inhClientPATHgraphics/dist/inhClientPATHgraphics-1.0.0.jar`

The companion applies the server render-distance limit, switches Bobby to immediate cache unloading, temporarily disables Voxy LOD rendering when a personal limit is active, restores local settings on disconnect/reset, and reports the Fabric mod list for `/igmods`. Distant Horizons/Voxy/Bobby internals are version-specific; adapters fail safely if a future version changes its internals.

## Operator command

`/igmods [игрок]`

Shows installed server plugins and, when the optional client companion is installed, the client-reported Fabric mods. Client reports are informational and are not a secure anti-cheat proof: a modified client can lie or omit mods.
