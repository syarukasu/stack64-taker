# Stack64 Taker 1.2.8

## English

Stack64 Taker 1.2.8 keeps the complete configurable shortcut behavior of the
stable 1.2.4 baseline and hardens invalid container-click handling.

- Supports normal menu slots and AE2 virtual storage entries.
- Supports configurable take amounts from 1 through 1,048,576.
- Includes the active container ID in custom requests, rejecting stale packets
  after a menu has changed.
- Blocks only out-of-range non-negative vanilla slot clicks before slot lookup.
- Performs authoritative menu validation on the server thread.
- Limits full recovery synchronization to once per second and warnings to once
  per 30 seconds.
- Adds four automated slot-boundary regression tests.

Protocol 10 requires the same 1.2.8 jar on the server and every client.

## 日本語

Stack64 Taker 1.2.8は、安定していた1.2.4の操作機能をすべて維持し、
不正なコンテナクリックの防御を強化した版です。

- 通常のメニュースロットとAE2仮想ストレージ項目に対応します。
- 取得数を1から1,048,576まで設定できます。
- 独自要求へ現在のcontainer IDを含め、画面切替後に届いた古いパケットを拒否します。
- スロット参照前に、範囲外の非負バニラスロット番号だけを遮断します。
- 正式なメニュー検証はサーバースレッド上で行います。
- 全状態再同期は1秒に1回、警告ログは30秒に1回へ制限します。
- スロット境界の自動回帰テストを4件追加しました。

Protocol 10のため、サーバーと全クライアントへ同じ1.2.8 JARが必要です。
