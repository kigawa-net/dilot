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
- `<name>`: プロジェクト名（一意の識別子）

**動作**
1. `<name>` がコンフィグに未登録であることを確認する
2. コンフィグファイルにプロジェクトエントリを追記する

**コンフィグファイル仕様**
- 保存先: `$DILOT_CONFIG`（未設定時は `~/.dilot/config.json`）
- フォーマット: JSON

```json
{
  "projects": [
    { "name": "my-project" }
  ]
}
```

**終了コード**
| コード | 意味 |
|---|---|
| 0 | 正常終了 |
| 1 | 一般エラー |
| 2 | 引数エラー（name未指定）|
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
 ├─ 引数検証（name必須）
 ├─ コンフィグファイル読み込み（存在しない場合は空として扱う）
 ├─ 同名プロジェクトの重複確認
 └─ コンフィグファイルへプロジェクトエントリを追記して保存
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
