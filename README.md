# Stack64 Taker

Stack64 Taker adds a small inventory shortcut for Minecraft Forge 1.20.1:

Use either shortcut to take exactly **64 items** from an oversized stack:

- Press the configured **Take 64** key while hovering a slot.
- Or hold the configured **Take 64 modifier** key and left-click a slot.

This is useful in modpacks that raise item stack limits with mods such as Bigger Stacks, while still wanting an easy way to create a vanilla-sized stack.

## Behavior

- Works only when the cursor is empty.
- Works only on slots containing more than 64 items.
- Takes exactly 64 items from the clicked slot and places them on the cursor.
- Leaves normal 64-or-smaller stacks untouched.
- The direct action key and modifier key are configurable in Minecraft's key bindings screen.
- The default direct action key is Mouse Button 4.
- The default modifier is Right Alt.
- Requires both client and server installation.

## Why both sides?

The client detects the shortcut, but the server performs the actual inventory change. This avoids ghost items and keeps multiplayer inventories authoritative.

## Compatibility Notes

This mod is intentionally tiny. It does not change stack sizes by itself. Pair it with a stack-size mod such as Bigger Stacks.

The current implementation targets Forge 1.20.1 and uses runtime Minecraft method names, matching Forge's production environment.

## Building

```bash
gradle build
```

The built jar will be in:

```text
build/libs/
```

This repository is laid out for ForgeGradle. If you prefer the Gradle Wrapper, generate it once with a local Gradle install:

```bash
gradle wrapper
```

## Release Checklist

1. Update the version in `build.gradle`.
2. Update the version in `src/main/resources/META-INF/mods.toml` if building manually.
3. Build the jar:

   ```bash
   gradle build
   ```

4. Confirm the jar contains the required metadata:

   ```bash
   jar tf build/libs/stack64-taker-<version>.jar | grep -E "pack.mcmeta|META-INF/mods.toml"
   ```

5. Test in a Forge 1.20.1 client and server with a stack-size-increasing mod.
6. Commit and tag:

   ```bash
   git add .
   git commit -m "Release <version>"
   git tag v<version>
   git push origin main --tags
   ```

7. Create a GitHub Release from the tag and upload the built jar.

## License

MIT
