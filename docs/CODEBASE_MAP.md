# Stack64 Taker codebase map

> **Navigation only.** このMapはCodex・LLM・reviewerの探索量を減らすためのindexです。仕様判断はREADME、source、現行Issueを使用します。

## 使い方

1. [`../AGENTS.md`](../AGENTS.md)を読む。
2. 下のTask routeを1つ選ぶ。
3. 対象class、Mixin、resourceだけを開く。
4. packet/call flowが隣接scopeを示した場合だけ広げる。

## 固定座標

```text
Minecraft          1.20.1
Forge              47.x line
Java               17
Install            BOTH sides
Take amount        1..1,048,576
```

## Task router

| Route | Task | Read first | Source scope | Verification scope |
| --- | --- | --- | --- | --- |
| `C1` | Key input、amount screen、client config | READMEのBehavior | `client` package、main entrypointのclient registration | client manual smoke |
| `N1` | Packet/request、server authoritative inventory mutation | READMEのWhy both sides | `Stack64Taker.java`のnetwork/server handler、server Mixin | dedicated server test |
| `M1` | Container click bounds、Mixin target、vanilla click guard | READMEのsafety behavior | `ContainerClickBounds.java`, `mixin`の対象classだけ | invalid slot/stale menu negative test |
| `A1` | AE2 virtual slot compatibility | READMEのCompatibility | main handler、client screen Mixin、AE2-specific branchだけ | AE2 terminal manual test |
| `K1` | Take amount range/default/config persistence | README、client config | `Stack64TakerClientConfig.java`, amount screen/packet validation | min/max/invalid amount test |
| `R1` | Forge metadata、Mixin descriptor、lang | README | 対象resourcesだけ | resource/game load |
| `V1` | Build、CI、version/release | README Building、workflow、該当release note | build files、workflow | `clean test build` |

## Class map

| Responsibility | Path |
| --- | --- |
| Mod entrypoint、network、common registration | `src/main/java/dev/stack64taker/Stack64Taker.java` |
| Shared slot/click bounds | `src/main/java/dev/stack64taker/ContainerClickBounds.java` |
| Amount input screen | `src/main/java/dev/stack64taker/client/Stack64TakerAmountScreen.java` |
| Client config | `src/main/java/dev/stack64taker/client/Stack64TakerClientConfig.java` |
| Key mappings | `src/main/java/dev/stack64taker/client/Stack64TakerKeyMappings.java` |
| Vanilla menu guard | `src/main/java/dev/stack64taker/mixin/AbstractContainerMenuClickGuardMixin.java` |
| Client screen hook | `src/main/java/dev/stack64taker/mixin/AbstractContainerScreenMixin.java` |
| Client click guard | `src/main/java/dev/stack64taker/mixin/ClientContainerClickGuardMixin.java` |
| Server click guard | `src/main/java/dev/stack64taker/mixin/ServerClickGuardMixin.java` |
| Forge metadata | `src/main/resources/META-INF/mods.toml` |
| Mixin metadata | `src/main/resources/META-INF/MANIFEST.MF`とMixin JSON |

## Request flow

```text
key press / hovered slot
-> client-side slot/menu snapshot
-> custom request
-> server menu identity + slot bounds + amount validation
-> compatible carried stack merge
-> authoritative inventory mutation
-> bounded recovery synchronization
```

不具合調査では、このflowのどの段階で期待値が崩れたかを先に特定する。全Mixinを一括で読む必要はない。

## Negative cases

```text
menu changed after request
slot < 0 or slot >= menu size
amount outside 1..1,048,576
cursor item/components mismatch
server thread以外からmutation
vanilla out-of-range click
repeated recovery/log spam
```

security/compatibility変更では該当negative caseを必ず再確認する。

## 最小検証コマンド

```text
./gradlew clean test build --no-daemon
```

client-only manual testでは完了にせず、server handler変更時はdedicated serverで両側JARを使用する。

## 省トークン用prompt

```text
AGENTS.mdとdocs/CODEBASE_MAP.mdの<Route ID>だけを基準に作業する。
Task: <作業内容>
対象class/Mixin以外を最初に読まない。
別scopeへ広げる場合はrequest flowまたは再現結果を根拠として示す。
```
