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

## 参照ドキュメント

- `docs/spec.md` — 設計・仕様規約
- `docs/dev.md` — 開発規約（コーディングスタイル・Git運用）
- `README.md` — 使用方法
