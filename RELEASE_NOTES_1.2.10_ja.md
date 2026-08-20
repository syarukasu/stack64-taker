### Stack64 Taker 1.2.10

#### 変更

- AE2の仮想`RepoSlot`クリックだけを、AE2標準の`InventoryAction`と
  `MEStorageMenu.handleInteraction`へ転送します。
- AE2メニュー全体をコンテナクリック検証から除外する処理は使用しません。
- 通常スロット、空白部分、AE2以外のGUI、サーバー側クリック境界検証は
  従来どおり保護されます。
