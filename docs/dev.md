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

## 作業フロー規約

### issueとの紐付け

- すべての作業はissueと紐付けて行う
- issueが存在しない作業は開始しない
- ブランチ名・コミット・PRにはissue番号を含める（例: `feature/123-add-clone`、`refs #123`）

### 作業計画

- 作業計画はissueに記載する
- 作業計画が記載されたissueが作成されてから実装を開始する
- 作業計画のないissueに対して実装作業は行わない

### 作業ステップ

1. **issue確認**: 対象issueが存在し、作業計画が本文に記載されていることを確認する
2. **ブランチ作成**: `feature/<issue番号>-<名前>` または `fix/<issue番号>-<名前>` でブランチを作成する
   - 例: `git checkout -b feature/5-add-clone-command`
3. **実装**: issueの作業計画に従って実装を進める
4. **コミット**: メッセージ末尾に `refs #<issue番号>` を含める（最後のコミットはissueを閉じる場合 `fix #<issue番号>`）
5. **PR作成**: 実装完了後に `master` ブランチへのPRを作成する

### PRの規約

- タイトル形式: `<type>: <概要> refs #<issue番号>`（例: `feat: clone機能を追加 refs #5`）
- 本文に対応issueへのリンクを含める（`Closes #<issue番号>` または `refs #<issue番号>`）
- PRの変更内容はissueの作業計画と一致していること

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
