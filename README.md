# dilot

任意のgitリポジトリをdevcontainerで起動するツール。

## 必要要件

- Docker
- [devcontainer CLI](https://github.com/devcontainers/cli) (`npm install -g @devcontainers/cli`)
- git

## インストール

[Releases](https://github.com/kigawa-net/dilot/releases) からバイナリをダウンロードして `PATH` の通った場所に配置する。

```bash
# Linux
curl -L https://github.com/kigawa-net/dilot/releases/latest/download/dilot-linux -o /usr/local/bin/dilot
chmod +x /usr/local/bin/dilot
```

またはソースからビルド:

```bash
git clone https://github.com/kigawa-net/dilot.git
cd dilot
./gradlew linkReleaseExecutableNative
cp build/bin/native/releaseExecutable/dilot.kexe /usr/local/bin/dilot
```

## 使い方

### リモートリポジトリを起動

```bash
dilot start https://github.com/example/repo.git
```

### ローカルリポジトリを起動

```bash
dilot start /path/to/repo
dilot start .
```

### オプション

| オプション | 説明 |
|---|---|
| `--template <name>` | 使用するdevcontainerテンプレートを指定 |
| `--attach` | 起動後にコンテナへアタッチする |
| `--no-clone` | クローンせずにカレントディレクトリを使用 |
| `--workdir <dir>` | クローン先ディレクトリを指定 |
| `--dry-run` | 実際には起動せず処理内容を表示 |

### テンプレート一覧

```bash
dilot templates
```

## 動作フロー

```
dilot start <repo>
    │
    ├─ URLの場合: git clone → ローカルパスへ
    │
    ├─ .devcontainer/ が存在する場合: そのまま使用
    │
    ├─ .devcontainer/ が存在しない場合: テンプレートを選択・生成
    │
    └─ devcontainer up で起動
```

## 開発

詳細は [docs/dev.md](docs/dev.md) を参照。

## ライセンス

MIT
