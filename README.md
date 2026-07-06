# Stack64 Taker

Stack64 Taker adds a small inventory shortcut for Minecraft Forge 1.20.1:

Hold the configured **Take 64 modifier** key and left-click a slot to take exactly **64 items** from an oversized stack.

This is useful in modpacks that raise item stack limits with mods such as Bigger Stacks, while still wanting an easy way to create a vanilla-sized stack.

## Behavior

- Works only when the cursor is empty.
- Works only on slots containing more than 64 items.
- Takes exactly 64 items from the clicked slot and places them on the cursor.
- Leaves normal 64-or-smaller stacks untouched.
- The modifier key is configurable in Minecraft's key bindings screen.
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

## License

MIT
