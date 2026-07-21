# Changelog

## 1.2.10

### Changed

- 限定的例外化に変更: 旧来の「appeng.menu.* 全体」を避け、`appeng.menu.me.*`（ME/ターミナル系）と AE2WTLib メニューのみを境界ガード対象外に。
- `appeng.menu.implementations.*` など通常の実体スロットGUIは従来どおりの境界ガードを維持。

### Safety

- 通常GUIでの不正スロットクリック防止は従来どおり維持。
- MEターミナル/ME系の仮想スロット操作はAE2側の処理に委譲。

## 1.2.9

### Fixed

- Bypassed the container click boundary guard for AE2/AE2WTLib menus so AE2 GUI and EMI transfer actions are no longer blocked by false boundary validation.
- Kept the existing anti-invalid-click protection for non-AE GUIs unchanged.
- Preserved prior server/client slot synchronization throttling and logging safeguards.

## 1.2.8

### Fixed

- Rebased the shortcut behavior on the stable 1.2.4 interaction model while
  retaining configurable extraction amounts, normal slots, and AE2 virtual
  terminal entries.
- Added the active container ID to Stack64 custom requests so packets queued
  for an already closed menu are rejected instead of affecting the next menu.
- Rejected only non-negative vanilla click indices outside the authoritative
  menu slot list, before any slot lookup occurs.
- Kept server-menu validation on the server thread and added a final menu
  boundary guard for non-packet callers.
- Limited full menu recovery synchronization to once per second and diagnostic
  warnings to once per 30 seconds per connection.

### Safety

- Negative vanilla sentinel slot IDs remain untouched.
- Normal AE2 interactions remain owned by AE2; Stack64's dedicated AE2 amount
  request continues to identify entries by AE2 serial rather than menu index.
- Protocol 10 requires the same 1.2.8 jar on client and server.
- Added four boundary tests for valid, invalid, sentinel, and empty-menu slot
  ranges.

## 1.2.7

### Fixed

- Removed the 1.2.6 client-side translation of AE2 virtual-slot clicks. AE2 once again owns all normal terminal insertion and extraction interactions.
- Rejected invalid vanilla slot packets now trigger a full authoritative menu resynchronization, restoring the carried stack and visible slots instead of leaving client prediction stale.
- The dedicated Stack64 AE2 amount packet remains unchanged and still uses AE2 serial keys rather than vanilla slot IDs.
- Protocol 12 requires the same build on client and server.

## 1.2.6

### Fixed

- Prevent client-only AE2 `RepoSlot` indices from reaching vanilla container-click prediction or packets.
- Translate carried-item insertion into an empty AE2 terminal slot back to AE2's own interaction packet.
- Preserve the authoritative server-side out-of-range slot guard.

### Safety

- If AE2 interaction translation fails, the unsafe click is discarded before client prediction, so the carried item is not consumed or desynchronized.
- Normal menu slots and Stack64's dedicated extraction packets are unchanged.
- Protocol 11 requires the same build on client and server.

## 1.2.5

### Fixed

- Keep out-of-range slot rejection at the authoritative server packet and menu boundaries.
- Remove the client-wide slot-count interceptor, which cannot know the server's AE2 virtual-slot layout.
- Remove the forced full-menu resynchronization added in 1.2.4.
- Keep Stack64 extraction on its dedicated real-slot and AE2-serial packets.

### Safety

- Invalid `slotId >= serverMenu.slots.size()` clicks are still discarded before menu processing.
- No rejected click triggers a menu rebuild or cursor rewrite.
- Protocol 10 requires the same build on both client and server.

## 1.2.4

### Fixed

- Resynchronize the authoritative server menu after rejecting an out-of-range container click.
- Schedule the resynchronization on the server thread instead of touching menu state from the Netty thread.
- Prevent rejected AE2 virtual-slot clicks from leaving the client cursor in a persistent ghost or apparently lost state.

## 1.2.1

### Fixed

- Fixed Stack64 Taker mixins being prepared but not applied in production Forge/Arclight environments.
- Prevented AE2 virtual repository slot IDs from being sent as vanilla container clicks.
- Blocked out-of-range container clicks on both the client send path and the server packet path.
- Added a final menu boundary guard ahead of third-party `AbstractContainerMenu` mixins.
- Added rate-limited diagnostics containing screen, menu, container ID, slot ID, and slot count.
- Restored reproducible Gradle `clean build` compatibility by aligning Java sources with official mappings.

### Safety

- Valid slot clicks and negative outside-click IDs are unchanged.
- Invalid clicks are discarded without forcing a menu resynchronization.
- Stack sizes, extraction amounts, AE2 storage behavior, and BiggerStacks behavior are unchanged.

## 1.1.11

Stack64 Taker 1.1.11 rebuilds the AE2 terminal path so oversized-stack packs can take exactly 64 items without sending virtual AE2 grid slots as normal container clicks.

### Added

- Added a dedicated AE2 take-64 packet.
- Added server-side AE2 extraction by terminal entry serial.
- Added support for taking 64 stored items from AE2 terminal grid entries.
- Added a server-side invalid container click guard with diagnostic logging.

### Changed

- Normal inventory and container slots still use the regular real-slot path.
- AE2 terminal entries now use the AE2 storage path instead of pretending to be vanilla/container slots.
- Craftable-only AE2 entries with no stored amount are ignored.
- The mod now requires both client and server installation for AE2 terminal support.
- Updated the network protocol version to 5.
- Updated mod version metadata to 1.1.11.

### Fixed

- Fixed AE2 virtual RepoSlot indices being sent as normal container slot clicks.
- Fixed invalid slot clicks such as AE2 terminal slot IDs exceeding the server menu slot count.
- Reduced the chance of AE2 terminal desyncs and disconnects caused by malformed container click packets.
- Removed unsafe menu resync behavior from the invalid click guard.

### Verification

- Built `stack64-taker-1.1.11.jar`.
- Confirmed the jar contains:
  - `pack.mcmeta`
  - `META-INF/mods.toml`
  - `stack64_taker.mixins.json`
  - `Stack64Taker$Take64SlotPacket.class`
  - `Stack64Taker$Take64Ae2Packet.class`
- Confirmed the old single `Stack64Taker$Take64Packet.class` is not present.
- Installed the jar to both the client instance and the server mods folder.

### Test Notes

Recommended in-game checks:

- Open an AE2 terminal.
- Hover an item that is actually stored in the ME network with more than 64 available.
- Press the configured Take 64 shortcut.
- Confirm exactly 64 items are placed on the cursor.
- Confirm craftable-only AE2 entries do nothing.
- Confirm the server log no longer reports invalid `CraftingTermMenu` slot clicks from this action.
