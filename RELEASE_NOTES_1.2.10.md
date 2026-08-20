### Stack64 Taker 1.2.10

#### Changed

- AE2 virtual `RepoSlot` clicks are now routed through AE2's native
  `InventoryAction` and `MEStorageMenu.handleInteraction` path.
- AE2 menus are no longer broadly excluded from container click validation.
- Normal slots, blank areas, non-AE2 menus, and server-side click bounds keep
  their existing protection.
