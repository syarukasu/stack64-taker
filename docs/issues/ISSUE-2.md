# Issue #2: AE2 RepoSlot操作がクライアント送信前に消える

## 症状

- ME端末でアイテム取得や自動クラフト注文を開始できない。
- クラフト状況画面やCPU操作が反応しないように見える。
- サーバーログに`Blocked invalid container click`が残らない。

## 原因

AE2の`RepoSlot`は端末表示専用の仮想Slotで、`menu.slots`には含まれない。
Stack64 Taker 1.2.8のクライアント境界ガードは、仮想Slot由来の番号を通常の
`ServerboundContainerClickPacket`用slot IDとして検証し、範囲外なら送信前に破棄していた。

## 修正

- マウス下の実Slotが`appeng.client.gui.me.common.RepoSlot`であることを確認する。
- 親Menuの継承階層に`appeng.menu.me.common.MEStorageMenu`があることを確認する。
- その二条件を満たす操作だけ、AE2の`InventoryAction`と`handleInteraction`へ変換する。
- 変換不能なClickType、通常Slot、他MOD Menuは従来の範囲ガードへ戻す。
- サーバー側の範囲外`ServerboundContainerClickPacket`保護は緩和しない。

## やってはいけないこと

- `appeng.menu.*`をまとめて範囲チェック対象外にしない。
- AE2 Menuだからという理由だけで不正slot IDをサーバーへ通さない。
- RepoSlotをバニラの実スロットとしてクリックしない。
- Stack64の数量指定操作をAE2本体の在庫会計として再実装しない。

## 試験

- 在庫0かつクラフト可能なRepoSlotの左クリックは`AUTO_CRAFT`。
- 在庫ありの左・右クリックはAE2標準の取得Action。
- Shift操作はAE2標準の`SHIFT_CLICK`/`PICKUP_SINGLE`。
- 未対応ClickTypeは変換せず範囲ガードへ戻す。
- 通常コンテナの範囲外slot IDは引き続き拒否する。
- `clean test build`が成功する。

## 実環境で確認する項目

- ME端末から通常取得・格納できる。
- 在庫0のクラフト可能項目から注文画面を開ける。
- クラフト状況画面を開ける。
- CPU選択と実行中CPU表示を操作できる。
