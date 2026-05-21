# dilot 設計・仕様規約

## 目的

dilotは任意のgitリポジトリをdevcontainer環境で即座に起動することを目的とする。
ユーザーはリポジトリのURLまたはローカルパスを指定するだけでよい。

## アーキテクチャ

Kotlin/Nativeでビルドされたネイティブバイナリ単体で動作する。シェルラッパーは持たない。

```
src/
└── net.kigawa.dilot.command   # CLIコマンド定義（new）
└── net.kigawa.dilot.git       # git操作（clone / 検出）
└── net.kigawa.dilot.container # devcontainer CLI呼び出し
└── net.kigawa.dilot.template  # テンプレート管理
└── net.kigawa.dilot.config    # コンフィグファイルの読み書き

build/bin/native/releaseExecutable/dilot.kexe   # Linuxバイナリ
build/bin/mingwX64/releaseExecutable/dilot.exe  # Windowsバイナリ

```

## コマンド仕様

### `dilot new <name> <git-url>`

プロジェクトをコンフィグファイルに登録する。

**引数**
- `<name>`: プロジェクト名（必須）
  - 使用可能文字: 英数字・ハイフン・アンダースコア（正規表現: `[a-zA-Z0-9_-]+`）
  - 長さ: 1〜64文字
  - コンフィグ内で一意であること
- `<git-url>`: コアリポジトリのURL（必須）
  - `https://`、`http://`、または `git@` で始まる文字列

**オプション**
| オプション | 説明 |
|---|---|
| `--config <path>` | コンフィグファイルのパスを明示指定 |

**動作**
1. `<name>` の形式を検証する（使用可能文字・長さ）
2. `<git-url>` の形式を検証する（`https://`、`http://`、または `git@` で始まること）
3. コンフィグファイルのパスを解決する（後述の優先順位に従う）
4. コンフィグファイルが存在する場合は読み込む。存在しない場合は空コンフィグとして扱う
5. 同名プロジェクトが既に存在する場合はエラーを標準エラー出力へ出力して終了コード5で終了する
6. 新しいプロジェクトエントリを `projects` リストへ追加する
7. コンフィグファイルへアトミックに書き込む（一時ファイルへ書き込んでからリネーム）
8. 成功メッセージを標準出力へ出力する

**出力**
- 成功時（標準出力）: `Project "<name>" created.`
- エラー時（標準エラー出力）: エラー内容を示す1行メッセージ

---

### `dilot o <name>`

登録済みプロジェクトの `coreUrl` をcloneしてdevcontainerで開く。

**引数**
- `<name>`: プロジェクト名（必須）

**オプション**
| オプション | 説明 |
|---|---|
| `--config <path>` | コンフィグファイルのパスを明示指定 |

**動作**
1. コンフィグファイルからプロジェクト一覧を読み込む
2. `<name>` に一致するプロジェクトを検索する（なければ終了コード4）
3. `~/dilot/<name>/<repo-name>/` にリポジトリが未cloneなら `git clone <coreUrl> <path>` を実行する（`<repo-name>` はURLの末尾から `.git` を除いた文字列）
4. `devcontainer up --workspace-folder <path>` を実行してdevcontainerを起動する
5. `devcontainer exec --workspace-folder <path> bash` でbashを起動する

**出力**
- cloneを行う場合（標準出力）: `Cloning <url> into <path> ...`
- devcontainer起動時（標準出力）: `Starting devcontainer for '<name>' ...`
- エラー時（標準エラー出力）: エラー内容を示す1行メッセージ

---

### コンフィグファイル仕様

**パス解決（優先順位）**
1. `--config <path>` オプションで指定したパス
2. 環境変数 `$DILOT_CONFIG` の値
3. `~/.dilot/config.json`（デフォルト）

**ファイル作成挙動**
- ファイルが存在しない場合: 親ディレクトリ（`~/.dilot/`）ごと自動作成する
- エンコーディング: UTF-8
- インデント: スペース2

**フォーマット（JSONスキーマ）**

```json
{
  "version": 1,
  "projects": [
    {
      "name": "my-project",
      "coreUrl": "https://github.com/example/repo.git",
      "createdAt": "2026-05-14T00:00:00Z"
    }
  ]
}
```

| フィールド | 型 | 必須 | 説明 |
|---|---|---|---|
| `version` | integer | ○ | スキーマバージョン。現在は常に `1` |
| `projects` | array | ○ | プロジェクトエントリの配列 |
| `projects[].name` | string | ○ | プロジェクト名 |
| `projects[].coreUrl` | string | ○ | コアリポジトリのURL |
| `projects[].createdAt` | string (ISO 8601) | ○ | 登録日時（UTC） |

**スキーマバージョン管理**
- `version` フィールドが現在のバイナリが対応する最大バージョンを超える場合、読み込みを拒否してエラーを出力する

---

**終了コード**
| コード | 意味 |
|---|---|
| 0 | 正常終了 |
| 1 | 一般エラー（ファイルI/Oエラー、不正なJSON等）|
| 2 | 引数エラー（name未指定・形式不正）|
| 5 | 同名プロジェクトが既に存在する |

## 処理フロー

```
dilot new <name> <git-url>
 ├─ 引数検証
 │    ├─ name未指定 → 終了コード2
 │    ├─ name形式不正（使用可能文字・長さ）→ 終了コード2
 │    ├─ git-url未指定 → 終了コード2
 │    └─ git-url形式不正（https://, http://, git@ で始まらない）→ 終了コード2
 ├─ コンフィグファイルパス解決
 │    ├─ --config 指定あり → その値を使用
 │    ├─ $DILOT_CONFIG 設定あり → その値を使用
 │    └─ いずれも未設定 → ~/.dilot/config.json
 ├─ コンフィグファイル読み込み
 │    ├─ ファイル存在する → JSONパース
 │    │    └─ パース失敗 → 終了コード1
 │    └─ ファイル存在しない → 空コンフィグ（version:1, projects:[]）として扱う
 ├─ スキーマバージョン確認
 │    └─ version > サポート最大値 → 終了コード1
 ├─ 同名プロジェクトの重複確認
 │    └─ 存在する → エラーメッセージ出力して終了コード5
 ├─ プロジェクトエントリ追加（name, coreUrl, createdAt）
 └─ コンフィグファイルへ書き込み
      ├─ 親ディレクトリが存在しない場合は作成
      ├─ 一時ファイルへ書き込み後にリネーム（アトミック書き込み）
      └─ 成功メッセージを標準出力へ出力
```

## 制約・前提

- コンフィグファイルはデフォルトで `$DILOT_CONFIG`（未設定時は `~/.dilot/config.json`）

---

## CI/CD設計（refs #15）

### ブランチ戦略

| ブランチ | 用途 |
|---|---|
| `main` | リリース済みの安定版。pushされると自動リリース |
| `develop` | 統合ブランチ。featureブランチのマージ先 |
| `feature/<name>` | 機能開発。`develop` へPRを出す |
| `fix/<name>` | バグ修正。`develop` へPRを出す |

### PR CI ワークフロー (`.github/workflows/ci.yml`)

**トリガー**: `develop` ブランチへのPRが作成・更新されたとき

**ジョブ**:
1. `build` — `./gradlew linkReleaseExecutableNative` でビルド検証
2. `test` — `./gradlew nativeTest` でテスト実行

**環境**: `ubuntu-latest` + Java 21

### Release ワークフロー (`.github/workflows/release.yml`)

**トリガー**: `main` ブランチへのpush（`develop` → `main` マージ時）

**ジョブ**:
1. `build-linux` — Linux向けネイティブバイナリをビルド (`dilot.kexe`)
2. `release` — コミットSHAをバージョンとしてGitHub Releasesを作成してバイナリをアタッチ

**成果物**: `build/bin/native/releaseExecutable/dilot.kexe`

---

## ワンライナーインストール設計（refs #19）

### 概要

`curl https://.../install.sh | bash` でバイナリをインストールできるようにする。

### 対応プラットフォーム

| OS | アーキテクチャ | 成果物ファイル名 |
|---|---|---|
| Linux | x86_64 | `dilot-linux-x86_64` |
| macOS | arm64 | `dilot-macos-arm64` |
| Windows | x86_64 | `dilot-windows-x86_64.exe` |

### 変更箇所

#### 1. `build.gradle.kts` — macosArm64ターゲットを追加

```kotlin
macosArm64 {
    binaries {
        executable {
            entryPoint = "net.kigawa.dilot.main"
        }
    }
}
```

#### 2. `.github/workflows/release.yml` — マルチプラットフォームビルドに更新

**トリガー変更**: `main` ブランチへのpush → `v*` タグのpush

**ジョブ構成**:

| ジョブ | ランナー | Gradleタスク | 成果物 |
|---|---|---|---|
| `build-linux` | `ubuntu-latest` | `linkReleaseExecutableNative` | `dilot-linux-x86_64` |
| `build-macos` | `macos-14` | `linkReleaseExecutableMacosArm64` | `dilot-macos-arm64` |
| `build-windows` | `windows-latest` | `linkReleaseExecutableMingwX64` | `dilot-windows-x86_64.exe` |
| `release` | `ubuntu-latest` | — | 全バイナリをGitHub Releasesに公開 |

**バージョン**: タグ名（例: `v0.1.0`）をリリースバージョンとして使用

#### 3. `install.sh` — インストールスクリプト（リポジトリルートに配置）

**動作フロー**:
1. `uname -s` / `uname -m` でOS・アーキテクチャを検出
2. 対応プラットフォームでなければエラー終了
3. GitHub Releases（latest）から対応バイナリをダウンロード
4. `~/.local/bin/dilot`（Linux/macOS）または `%USERPROFILE%\.local\bin\dilot.exe`（Windows）に配置
5. 実行権限を付与（Linux/macOS）
6. PATHへの追加案内を表示

**インストール先（優先順位）**:
- `$DILOT_INSTALL_DIR` 環境変数で上書き可能
- デフォルト: `~/.local/bin`

**使用方法（想定）**:
```bash
curl -fsSL https://raw.githubusercontent.com/kigawa-net/dilot/main/install.sh | bash
```

#### 4. `README.md` — インストール手順を追記

クイックスタートセクションにワンライナーを記載する。

### リリースフロー

```
develop → main マージ後:
  git tag v<version>
  git push origin v<version>
  → release.yml が起動
  → 3プラットフォーム並列ビルド
  → GitHub Releases に公開
```

---

## リリース用GitHub Actions設計（refs #22）

### 概要

`workflow_dispatch` でバンプ種別（major/minor/patch）を選択し、最新タグから次バージョンを自動算出して `develop` から `release/vX.Y.Z` ブランチを作成・`main` へのPRを自動作成する。PRマージ後に `vX.Y.Z` タグをpushし、既存の `release.yml` でビルド・公開する。

### ワークフロー構成

#### 1. `.github/workflows/create-release.yml` — リリースブランチ作成

**トリガー**: `workflow_dispatch`

**入力パラメータ**:

| 入力名 | 型 | 選択肢 | 説明 |
|---|---|---|---|
| `bump` | choice | `patch` / `minor` / `major` | バンプするバージョン種別。デフォルトは `patch` |

**ジョブ: `create-release`**:

1. `git tag --sort=-v:refname` で最新の `v*` タグを取得し `MAJOR.MINOR.PATCH` を抽出する
   - タグが存在しない場合は `0.0.0` を初期値とする
2. `bump` 入力に応じて次バージョンを算出する
   - `major`: `(MAJOR+1).0.0`
   - `minor`: `MAJOR.(MINOR+1).0`
   - `patch`: `MAJOR.MINOR.(PATCH+1)`
3. `develop` をチェックアウト
4. `release/v<next>` ブランチを作成してpush
5. `gh pr create` で `release/v<next>` → `main` のPRを作成
   - タイトル: `release: v<next>`
   - 本文: リリース内容の概要

**必要なパーミッション**:
- `contents: write` — ブランチpush用
- `pull-requests: write` — PR作成用

#### 2. `.github/workflows/tag-release.yml` — PRマージ後タグ付け

**トリガー**: `pull_request` がクローズされ、`main` へマージされ、かつブランチ名が `release/v*` にマッチするとき

**ジョブ: `tag-release`**:

1. `main` をチェックアウト
2. ブランチ名（`release/v<version>`）からバージョンを抽出
3. `v<version>` タグを作成してpush
4. タグpushが `release.yml` をトリガーし、ビルド・公開が実行される

**必要なパーミッション**:
- `contents: write` — タグpush用

### リリースフロー（更新後）

```
1. GitHub ActionsのUIで create-release.yml を dispatch（bump: "minor" 等を選択）
   → 最新タグから次バージョンを自動算出（例: v1.2.0 → minor → v1.3.0）
   → develop から release/v1.3.0 ブランチを作成
   → main へのPRを自動作成

2. PRをレビュー・マージ
   → tag-release.yml が起動
   → v1.2.0 タグをpush

3. タグpushで release.yml が起動
   → 3プラットフォーム並列ビルド
   → GitHub Releases に公開
```

### 変更ファイル一覧（#22）

| ファイル | 変更種別 | 内容 |
|---|---|---|
| `.github/workflows/create-release.yml` | 新規作成 | dispatch でリリースブランチ・PR作成 |
| `.github/workflows/tag-release.yml` | 新規作成 | PRマージ後にタグをpush（#26 で廃止） |

---

## リリース時バイナリ添付設計（refs #26）

### 概要

`release/v*` → `main` のPRマージを1つのワークフロー（`release.yml`）で受け取り、ビルド・タグ作成・GitHub Releaseへのバイナリ添付を完結させる。#22 で追加予定の `tag-release.yml` は不要となるため廃止する。

### 変更方針

#### `release.yml` の変更

**トリガー変更**: `v*` タグのpush → `release/v*` ブランチから `main` へのPRがマージされたとき

```yaml
on:
  pull_request:
    types: [closed]
    branches:
      - main
```

全ジョブに以下の条件を付与:

```yaml
if: github.event.pull_request.merged == true && startsWith(github.event.pull_request.head.ref, 'release/v')
```

**ジョブ構成**:

| ジョブ | 変更内容 |
|---|---|
| `build-linux` | 条件追加のみ（ビルド内容は変更なし） |
| `build-macos` | 条件追加のみ |
| `build-windows` | 条件追加のみ |
| `release` | バージョンをタグ名でなくPRブランチ名から抽出。タグ作成を追加 |

**`release` ジョブの処理**:

1. PRブランチ名（`release/v1.2.0`）からバージョン `v1.2.0` を抽出
2. `git tag v1.2.0 && git push origin v1.2.0` でタグ付け
3. `gh release create v1.2.0` でバイナリを添付してGitHub Releaseを作成

**必要なパーミッション**（`release` ジョブ）:
- `contents: write` — タグpush・Release作成用

### 変更ファイル一覧（#26）

| ファイル | 変更種別 | 内容 |
|---|---|---|
| `.github/workflows/release.yml` | 変更 | トリガーをPRマージに変更。バージョン抽出・タグ作成を追加 |
| `.github/workflows/tag-release.yml` | 削除 | `release.yml` に統合されるため廃止 |
