### Stack64 Taker 1.2.9

#### Fixed

- AE2/AE2WTLib menu clicks were falsely treated as invalid due to slot boundary checks.
- `ContainerClickBounds` now skips invalid-click rejection for those GUI namespaces and reuses AE2's own virtual slot logic.
- Normal vanilla containers and existing anti-desync guards remain unchanged.
