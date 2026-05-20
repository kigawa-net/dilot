# dilot

任意のgitリポジトリをdevcontainerで起動するツール。

## 必要要件

- Docker
- [devcontainer CLI](https://github.com/devcontainers/cli) (`npm install -g @devcontainers/cli`)
- git

## インストール

### ワンライナー（Linux / macOS）

```bash
curl -fsSL https://raw.githubusercontent.com/kigawa-net/dilot/main/install.sh | bash
```

インストール先は `~/.local/bin/dilot`。`$DILOT_INSTALL_DIR` 環境変数で変更可能。

```bash
# インストール先を指定する場合
curl -fsSL https://raw.githubusercontent.com/kigawa-net/dilot/main/install.sh | DILOT_INSTALL_DIR=/usr/local/bin bash
```

### 手動インストール

[Releases](https://github.com/kigawa-net/dilot/releases) からプラットフォームに合ったバイナリをダウンロードして `PATH` の通った場所に配置する。

| OS | ファイル名 |
|---|---|
| Linux x86_64 | `dilot-linux-x86_64` |
| macOS arm64 | `dilot-macos-arm64` |
| Windows x86_64 | `dilot-windows-x86_64.exe` |

### ソースからビルド

```bash
git clone https://github.com/kigawa-net/dilot.git
cd dilot
./gradlew linkReleaseExecutableNative
cp build/bin/native/releaseExecutable/dilot.kexe ~/.local/bin/dilot
```

## 開発

詳細は [docs/dev.md](docs/dev.md) を参照。

## ライセンス

MIT
