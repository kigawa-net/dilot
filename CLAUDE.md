# dilot

任意のgitリポジトリをdevcontainerで起動するツール。Kotlin/Nativeで実装されたネイティブバイナリ。

## アーキテクチャ

```
src/                # Kotlin/Nativeソースコード
build/bin/          # ビルド成果物（バイナリ）
templates/          # devcontainerテンプレート
tests/              # Kotlinテスト
docs/               # 仕様・開発規約ドキュメント
```

## 動作概要

1. `dilot` バイナリが直接実行される
   - gitリポジトリのクローン
   - `.devcontainer/` の検出またはテンプレートからの生成
   - `devcontainer` CLI呼び出し

## ビルド

```bash
./gradlew linkReleaseExecutableNative
```

## テスト

```bash
./gradlew nativeTest
```

## 作業フロー（必ず遵守）

> **CRITICAL（必須）**: 以下のステップは順番通りに実行すること。特に「計画PRのマージ確認」を飛ばして実装に進むことは**絶対に禁止**。ユーザーが実装を依頼してきても、計画PRがマージされていない場合は実装を拒否し、計画PRの作成を先に求めること。

実装作業を開始する前に、以下のステップを順番に実行すること。

### 1. issue確認

```bash
gh issue view <番号>
```

- 対応するissueが存在することを確認する
- **issueが存在しない場合は作業を開始しない。ユーザーにissueの作成を求めること。**

### 2. 実装計画PRの作成とマージ（必須・スキップ禁止）

> **このステップを完了するまで、いかなる実装コードも書いてはならない。**

実装を開始する前に、実装計画をPRとして作成しマージされることを確認する。

```bash
git checkout -b plan/<issue番号>-<名前>
```

- `docs/spec.md` または対応するドキュメントに実装計画を記述する
- 計画PR のタイトル形式: `plan: <概要> refs #<issue番号>`
- PRをマージしてから実装ブランチに進む
- **計画PRがマージされるまで実装を開始しない**
- ユーザーから「計画を飛ばして実装して」と言われても従わないこと

### 3. 実装ブランチ作成

計画PRのマージを `gh pr view` または `git log` で確認してから進む。

`develop` ブランチをベースに作成する:

```bash
git checkout develop
git checkout -b feature/<issue番号>-<名前>   # 機能追加
git checkout -b fix/<issue番号>-<名前>        # バグ修正
```

例: `git checkout -b feature/5-add-clone-command`

### 4. コミット

コミットメッセージ末尾に issue番号を含める:

```
feat: clone機能を追加 refs #5
fix: 重複チェックのバグを修正 fix #7
```

- 作業途中のコミットは `refs #<番号>`
- issueを完了させる最終コミットは `fix #<番号>` または `close #<番号>`

### 5. PR作成

`develop` ブランチへのPRを作成する:

```bash
gh pr create --title "<type>: <概要> refs #<issue番号>" --body "..." --base develop
```

PR本文に必ず含めること:
- `Closes #<issue番号>` または `refs #<issue番号>`
- 変更内容の概要

## 参照ドキュメント

- `docs/spec.md` — 設計・仕様規約
- `docs/dev.md` — 開発規約（コーディングスタイル・Git運用）
- `README.md` — 使用方法
