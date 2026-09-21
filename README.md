# JukeBoxPlus

A client-side Fabric mod that turns Minecraft's soundtrack into a proper music player:
browse every track (C418, Lena Raine, Aaron Cherof, Kumi Tanioka), search, play, pause, skip,
shuffle, keep a history of what the game played, and see what's playing in a small HUD overlay.

## Supported Minecraft versions

One jar per Minecraft branch, all built from the same source tree.

| Jar suffix   | Minecraft         | Java |
|--------------|-------------------|------|
| `+mc1.19.2`  | 1.19.2            | 17   |
| `+mc1.19.4`  | 1.19.3 – 1.19.4   | 17   |
| `+mc1.20.1`  | 1.20 – 1.20.1     | 17   |
| `+mc1.20.4`  | 1.20.2 – 1.20.4   | 17   |
| `+mc1.20.6`  | 1.20.5 – 1.20.6   | 21   |
| `+mc1.21.1`  | 1.21 – 1.21.1     | 21   |
| `+mc1.21.4`  | 1.21.2 – 1.21.4   | 21   |
| `+mc1.21.5`  | 1.21.5            | 21   |
| `+mc1.21.8`  | 1.21.6 – 1.21.8   | 21   |
| `+mc1.21.10` | 1.21.9 – 1.21.10  | 21   |
| `+mc1.21.11` | 1.21.11           | 21   |
| `+mc26.1`    | 26.1.x            | 25   |
| `+mc26.2`    | 26.2.x            | 25   |
| `+mc26.3`    | 26.3.x            | 25   |

Requires [Fabric Loader](https://fabricmc.net/use/) 0.19.5+ and [Fabric API](https://modrinth.com/mod/fabric-api).

## Usage

| Key (default) | Action |
|---------------|--------|
| `M` | Open / close the music player |
| `N` | Play / pause |
| `B` | Next track |
| `V` | Previous track |
| `H` | Toggle the "now playing" HUD overlay |

All keys can be changed in *Options → Controls → Key Binds → JukeBoxPlus*.

Inside the player: click a track to play it, type to search, use the buttons at the bottom for
previous / play-pause / next / shuffle / repeat, and the gear icon for settings (overlay position,
volume, whether vanilla background music is suppressed while the player is active).
`Space`, `←`, `→`, `↑`, `↓` work as shortcuts while the search box is not focused.

## Building

```bash
./gradlew build -Pmc=1.21.11      # any version from the table above
```

The jar lands in `build/libs/`. Without `-Pmc`, the default from `gradle.properties` is used.
Each version's toolchain (loom plugin, Fabric API, Java level, source overlays) is described in
`versions/<mc>.properties`.

### Project layout

```
src/main/java            shared code (player, tracker, UI drawn with a small Gfx abstraction)
src/compat/gfx-*         drawing backend: PoseStack (≤1.19), GuiGraphics (1.20–1.21.11), GuiGraphicsExtractor (26.x)
src/compat/screen-*      the Screen subclass for each family of Screen/input API
src/compat/core-*        key bindings, HUD hook, sound-engine mixin and misc. renamed APIs
versions/                one properties file per Minecraft target
data/tracks.json         track database (title, composer, album, sound event, length)
```

GitHub Actions builds all 14 targets on every push; a push to `main` publishes them as a single
release tagged with the `mod_version` from `gradle.properties`.

## License

MIT
