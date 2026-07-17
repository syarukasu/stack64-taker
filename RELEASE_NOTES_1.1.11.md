# Stack64 Taker 1.1.11

This release rebuilds the AE2 terminal handling so the mod no longer treats AE2 virtual grid entries as normal container slots.

## Highlights

- AE2 terminal support was rebuilt around a dedicated AE2 shortcut packet.
- Normal inventories and containers still take up to 64 from real slots.
- AE2 terminal entries now send only the AE2 entry serial to the server.
- The server resolves the serial, extracts up to 64 stored items from the ME network, and places them on the cursor.
- Craftable-only AE2 entries are ignored.
- Added a server-side invalid click guard to block malformed container click packets and log useful diagnostics.

## Fixes

- Fixed AE2 terminal virtual slot IDs being sent as normal slot clicks.
- Fixed cases where AE2 terminal actions could send slot IDs beyond the server menu slot count.
- Reduced AE2 terminal desync and server disconnect risk caused by invalid container click packets.
- Removed unsafe resync behavior from the invalid click guard.

## Requirements

- Minecraft Forge 1.20.1
- Install on both client and server.
- For AE2 terminal support, AE2 must be present on both sides.
- This mod does not raise stack sizes by itself. Use it alongside a stack-size mod such as Bigger Stacks.

## Suggested Test

1. Open an AE2 terminal.
2. Hover a stored item with more than 64 available.
3. Press the configured Take 64 shortcut.
4. Confirm exactly 64 items are placed on the cursor.
5. Confirm craftable-only entries with zero stored amount do nothing.
6. Check the server log for absence of invalid `CraftingTermMenu` slot click warnings from this shortcut.

## Build Notes

Verified jar contents:

- `pack.mcmeta`
- `META-INF/mods.toml`
- `stack64_taker.mixins.json`
- `Stack64Taker$Take64SlotPacket.class`
- `Stack64Taker$Take64Ae2Packet.class`

The old single `Stack64Taker$Take64Packet.class` is not included.
