# dilot 開発規約

## 全体方針

- Kotlin/Nativeのネイティブバイナリ単体で動作する。シェルラッパーは持たない
- 副作用を持つ処理（ファイルIO、プロセス実行）はinterfaceで抽象化してテスト可能にする
- エラーはexceptionではなく `Result<T>` または sealed classで表現することを優先する

---

## Kotlin/Native コーディング規約

### 基本ルール

- Kotlin公式コーディング規約に従う
- パッケージ名: `net.kigawa.dilot.<モジュール名>`
- ファイル名: PascalCase（例: `GitRepository.kt`）

### 命名規則

| 種別 | 記法 | 例 |
|---|---|---|
| クラス・オブジェクト | PascalCase | `ContainerManager` |
| 関数・変数 | camelCase | `cloneRepo` |
| 定数（companion / top-level val）| UPPER_SNAKE_CASE | `DEFAULT_WORKDIR` |
| パッケージ | 小文字ドット区切り | `net.kigawa.dilot.git` |

### 設計方針

- CLIフレームワークは [kotlinx-cli](https://github.com/Kotlin/kotlinx-cli) を使用する
- 外部プロセス呼び出し（`devcontainer`、`git`）はinterfaceでラップしてテスト時にモック可能にする

### コメント

- コードを見れば分かることは書かない
- 非自明な制約・外部仕様への依存・回避策がある場合のみ書く

---

## ビルド

```bash
# Linux/macOS
./gradlew linkReleaseExecutableNative

# Windows
gradlew.bat linkReleaseExecutableMingwX64
```

## テスト

```bash
# 全テスト
./gradlew nativeTest
```

---

## Gitブランチ戦略

| ブランチ | 用途 |
|---|---|
| `main` | リリース済みの安定版 |
| `develop` | 統合ブランチ |
| `feature/<name>` | 機能開発 |
| `fix/<name>` | バグ修正 |

## コミットメッセージ

```
<type>: <概要（命令形・日本語可）>

<詳細（任意）>
```

**type一覧**: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
