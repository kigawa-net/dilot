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

実装作業を開始する前に、以下のステップを順番に実行すること。

### 1. issue確認

```bash
gh issue view <番号>
```

- 対応するissueが存在することを確認する
- issueの本文に作業計画が記載されていることを確認する
- 作業計画がない場合は実装を開始せず、issueに計画を追加してから再開する

### 2. ブランチ作成

```bash
git checkout -b feature/<issue番号>-<名前>   # 機能追加
git checkout -b fix/<issue番号>-<名前>        # バグ修正
```

例: `git checkout -b feature/5-add-clone-command`

### 3. コミット

コミットメッセージ末尾に issue番号を含める:

```
feat: clone機能を追加 refs #5
fix: 重複チェックのバグを修正 fix #7
```

- 作業途中のコミットは `refs #<番号>`
- issueを完了させる最終コミットは `fix #<番号>` または `close #<番号>`

### 4. PR作成

```bash
gh pr create --title "<type>: <概要> refs #<issue番号>" --body "..."
```

PR本文に必ず含めること:
- `Closes #<issue番号>` または `refs #<issue番号>`
- 変更内容の概要

## 参照ドキュメント

- `docs/spec.md` — 設計・仕様規約
- `docs/dev.md` — 開発規約（コーディングスタイル・Git運用）
- `README.md` — 使用方法
