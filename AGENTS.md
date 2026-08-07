# Stack64 Taker agent entrypoint

このファイルはCodex・LLM・自動レビューが、小規模MODでもclient、server、Mixin、networkを毎回すべて読み込まずに作業範囲を決めるための入口です。

## 最小読込手順

1. 最初に本書と [`docs/CODEBASE_MAP.md`](docs/CODEBASE_MAP.md) だけを読む。
2. MapのTask routeを1つ選び、対象class/descriptorだけを開く。
3. client shortcut問題でserver Mixin全体、server validation問題でclient screen全体を初手から読まない。
4. compile error、再現結果、packet flowが示した場合だけ隣接scopeへ広げる。
5. CHANGELOGや全release notesの全文読込を開始条件にしない。

## 固定契約

```text
Minecraft             1.20.1
Loader                Forge
Runtime Java          17
Install               client and server
Take amount range     1..1,048,576
Primary behavior      configurable exact take shortcut
License               MIT
```

Clientはshortcut/inputを検出し、serverが実際のinventory mutationをauthoritativeに実行します。clientだけでcursor stackやslot contentsを確定しません。

Custom requestはmenu/container identity、slot bounds、amount range、carried stack compatibility、server threadを検証します。menu変更後のstale request、out-of-range vanilla click、rate-limit回避を許可しません。

Vanilla menu、AE2 virtual slot、large-stack modとの互換性を維持し、Stack64 Taker自身はstack sizeを変更しません。

## 安全規則

- server-side inventory mutationはserver threadだけで行う。
- client/server packetまたはMixin contractを片側だけ変更しない。
- slot indexをlookupする前にboundsを検証する。
- menu identityが変わったrequestを拒否する。
- cursor stack mergeはitem/components/count limitsを確認する。
- warning/recovery syncのrate limitを無効化しない。
- build成功だけでmultiplayer、AE2 virtual slots、large-stack mod互換を検証済みと書かない。

## 編集規則

- shortcut/UI変更は`client`とmain entrypointの対象handlerから読む。
- security/validation変更はMixinと`ContainerClickBounds`から読む。
- entrypoint、Mixin、packet、config、resource位置が変わる場合は `docs/CODEBASE_MAP.md` を更新する。
- version historyは該当release noteだけを読む。

## 検証順

```text
./gradlew clean test build --no-daemon
-> singleplayer shortcut smoke
-> dedicated server client/server smoke
-> stale menu / invalid slot / max amount negative tests
```

CI/buildだけの結果をmultiplayer verifiedとして扱いません。
