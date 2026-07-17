# Stack64 Taker

Stack64 Taker is a tiny Forge 1.20.1 utility for modpacks that raise stack limits.
It lets players take exactly **64 items** from oversized stacks without lowering the global stack cap.

## Features

- Take up to 64 items from vanilla/container slots.
- Take 64 stored items from AE2 terminal grid entries without treating virtual grid entries as container slots.
- Configurable keybinds in Minecraft's key bindings screen.
- Server-side inventory handling to avoid ghost items.
- Server-side invalid slot click guard with diagnostic logging.

## Controls

Use either shortcut while hovering an item slot:

- Press the configured **Take 64** key.
- Or hold the configured **Take 64 modifier** key and left-click.

Defaults:

- **Take 64**: Mouse Button 4
- **Take 64 modifier**: Right Alt

## Behavior

- Works only when the carried cursor stack is empty.
- Normal inventory slots can contain any positive amount; the mod takes up to 64.
- AE2 terminal grid entries use a separate shortcut packet. The server resolves the AE2 entry serial and extracts up to 64 stored items from the ME network.
- Requires both client and server installation.

## Compatibility

This mod does not change stack sizes by itself. Pair it with a stack-size mod such as Bigger Stacks.

AE2 terminal grid slots are virtual client slots, so Stack64 Taker never sends their client slot index as a normal container click. It sends only the AE2 entry serial through the AE2 shortcut path.

The invalid click guard blocks malformed container click packets where the client sends a slot index outside the server menu's slot list. When this happens, the server logs a warning like:

```text
Stack64Taker/ClickGuard: Blocked invalid container click: player=... menu=... slotId=... serverSlotCount=...
```

## Building

Local build:

```bash
gradle build
```

GitHub Actions also builds the jar on push, pull request, and manual workflow dispatch.

The built jar will be in:

```text
build/libs/
```

## Release Checklist

1. Update the version in:
   - `build.gradle`
   - `src/main/resources/META-INF/mods.toml`
   - `src/main/resources/META-INF/MANIFEST.MF`, if present
2. Build the jar:

   ```bash
   gradle build
   ```

3. Confirm the jar contains the required metadata:

   ```bash
   jar tf build/libs/stack64-taker-<version>.jar | grep -E "pack.mcmeta|META-INF/mods.toml|stack64_taker.mixins.json"
   ```

4. Test in a Forge 1.20.1 client and server with:
   - a stack-size-increasing mod
   - AE2, if releasing AE2 terminal support
5. Commit and tag:

   ```bash
   git add .
   git commit -m "Release <version>"
   git tag v<version>
   git push origin main --tags
   ```

6. Create a GitHub Release from the tag and upload the built jar.

## License

MIT
