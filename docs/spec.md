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
