# dilot 設計・仕様規約

## 目的

dilotは任意のgitリポジトリをdevcontainer環境で即座に起動することを目的とする。
ユーザーはリポジトリのURLまたはローカルパスを指定するだけでよい。

## アーキテクチャ

Kotlin/Nativeでビルドされたネイティブバイナリ単体で動作する。シェルラッパーは持たない。

```
src/
└── net.kigawa.dilot.command   # CLIコマンド定義（start / stop / templates / new）
└── net.kigawa.dilot.git       # git操作（clone / 検出）
└── net.kigawa.dilot.container # devcontainer CLI呼び出し
└── net.kigawa.dilot.template  # テンプレート管理
└── net.kigawa.dilot.config    # コンフィグファイルの読み書き

build/bin/native/releaseExecutable/dilot.kexe   # Linuxバイナリ
build/bin/mingwX64/releaseExecutable/dilot.exe  # Windowsバイナリ

templates/                     # devcontainerテンプレート群
```

## コマンド仕様

### `dilot start <target> [options]`

指定したリポジトリをdevcontainerで起動する。

**引数**
- `<target>`: gitリポジトリのURL または ローカルパス（省略時はカレントディレクトリ）

**オプション**
| オプション | 説明 |
|---|---|
| `--template <name>` | devcontainerテンプレートを指定 |
| `--attach` | 起動後にコンテナへアタッチ |
| `--no-clone` | クローンをスキップ（ローカルパス前提）|
| `--dry-run` | 実際の操作をせずに処理内容を出力 |
| `--workdir <dir>` | クローン先ディレクトリを指定 |

**終了コード**
| コード | 意味 |
|---|---|
| 0 | 正常終了 |
| 1 | 一般エラー |
| 2 | 引数エラー |
| 3 | git操作エラー |
| 4 | コンテナ起動エラー |

### `dilot new <name>`

プロジェクトをコンフィグファイルに登録する。

**引数**
- `<name>`: プロジェクト名（必須）
  - 使用可能文字: 英数字・ハイフン・アンダースコア（正規表現: `[a-zA-Z0-9_-]+`）
  - 長さ: 1〜64文字
  - コンフィグ内で一意であること

**オプション**
| オプション | 説明 |
|---|---|
| `--config <path>` | コンフィグファイルのパスを明示指定 |

**動作**
1. `<name>` の形式を検証する（使用可能文字・長さ）
2. コンフィグファイルのパスを解決する（後述の優先順位に従う）
3. コンフィグファイルが存在する場合は読み込む。存在しない場合は空コンフィグとして扱う
4. 同名プロジェクトが既に存在する場合はエラーを標準エラー出力へ出力して終了コード5で終了する
5. 新しいプロジェクトエントリを `projects` リストへ追加する
6. コンフィグファイルへアトミックに書き込む（一時ファイルへ書き込んでからリネーム）
7. 成功メッセージを標準出力へ出力する

**出力**
- 成功時（標準出力）: `Project "<name>" created.`
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

### `dilot stop <target>`

起動中のdevcontainerを停止する。

### `dilot templates`

利用可能なテンプレートの一覧を表示する。

## 処理フロー

```
dilot start <target>
 ├─ 引数検証
 ├─ targetがURL → git clone → ローカルパス取得
 ├─ targetがローカルパス → パス確認
 ├─ .devcontainer/ 存在確認
 │    ├─ 存在する → そのまま使用
 │    └─ 存在しない → テンプレート選択 → .devcontainer/ 生成
 ├─ devcontainer up 実行
 └─ --attach の場合 → devcontainer exec で接続

dilot new <name>
 ├─ 引数検証
 │    ├─ name未指定 → 終了コード2
 │    └─ name形式不正（使用可能文字・長さ）→ 終了コード2
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
 ├─ プロジェクトエントリ追加（name, createdAt）
 └─ コンフィグファイルへ書き込み
      ├─ 親ディレクトリが存在しない場合は作成
      ├─ 一時ファイルへ書き込み後にリネーム（アトミック書き込み）
      └─ 成功メッセージを標準出力へ出力
```

## テンプレート仕様

テンプレートは `templates/<name>/` ディレクトリに格納する。

```
templates/
├── default/               # 汎用テンプレート
│   └── devcontainer.json
├── node/                  # Node.js向け
│   └── devcontainer.json
├── python/                # Python向け
│   └── devcontainer.json
└── go/                    # Go向け
    └── devcontainer.json
```

各テンプレートは有効な `devcontainer.json` を含む。

## 依存関係

| ツール | 必須/任意 | 用途 |
|---|---|---|
| docker | 必須 | コンテナ実行 |
| devcontainer CLI | 必須 | コンテナ管理 |
| git | 必須 | リポジトリ操作 |

## 制約・前提

- devcontainer CLIが `PATH` 上に存在すること
- Dockerデーモンが起動していること
- クローン先はデフォルトで `$DILOT_WORKDIR`（未設定時は `~/.dilot/repos/`）
- コンフィグファイルはデフォルトで `$DILOT_CONFIG`（未設定時は `~/.dilot/config.json`）
