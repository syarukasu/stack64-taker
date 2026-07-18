# Stack64 Taker

Stack64 Taker adds configurable inventory shortcuts for Minecraft Forge 1.20.1.

Use the configured **Take amount** key over a slot to take the selected number of items. The default is 64, and the amount can be changed with the **Set take amount** key.

This is useful in modpacks that raise item stack limits with mods such as Bigger Stacks, while still wanting an easy way to create a vanilla-sized stack.

## Behavior

- Supports normal menu slots and AE2 storage-terminal virtual slots.
- Supports amounts from 1 through 1,048,576.
- Merges into a compatible stack already carried by the cursor.
- Both shortcut actions are configurable in Minecraft's key bindings screen.
- Retains the complete behavior of the 1.2.4 baseline.
- Rejects stale custom requests after the player changes menus.
- Blocks out-of-range vanilla container clicks before they reach slot lookup.
- Performs server-side validation only on the server thread.
- Rate-limits recovery synchronization and warning logs.
- Requires both client and server installation.

## Why both sides?

The client detects the shortcut, but the server performs the actual inventory change. This avoids ghost items and keeps multiplayer inventories authoritative.

## Compatibility Notes

This mod is intentionally tiny. It does not change stack sizes by itself. Pair it with a stack-size mod such as Bigger Stacks.

The current implementation targets Forge 1.20.1 and uses runtime Minecraft method names, matching Forge's production environment.

## Building

```bash
gradle clean test build --no-daemon
```

The built jar will be in:

```text
build/libs/
```

GitHub Actions runs the same Java 17 ForgeGradle build for pushes and pull requests.

## License

MIT
