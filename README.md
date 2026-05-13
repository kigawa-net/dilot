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

## 開発

詳細は [docs/dev.md](docs/dev.md) を参照。

## ライセンス

MIT
