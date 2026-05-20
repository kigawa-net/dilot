#!/usr/bin/env bash
set -euo pipefail

REPO="kigawa-net/dilot"
INSTALL_DIR="${DILOT_INSTALL_DIR:-$HOME/.local/bin}"

detect_platform() {
    local os arch
    os="$(uname -s)"
    arch="$(uname -m)"

    case "$os" in
        Linux)
            case "$arch" in
                x86_64) echo "dilot-linux-x86_64" ;;
                *) echo "Unsupported architecture: $arch" >&2; exit 1 ;;
            esac
            ;;
        Darwin)
            case "$arch" in
                arm64) echo "dilot-macos-arm64" ;;
                *) echo "Unsupported architecture: $arch" >&2; exit 1 ;;
            esac
            ;;
        *)
            echo "Unsupported OS: $os" >&2
            exit 1
            ;;
    esac
}

BINARY="$(detect_platform)"
DOWNLOAD_URL="https://github.com/${REPO}/releases/latest/download/${BINARY}"
DEST="${INSTALL_DIR}/dilot"

mkdir -p "$INSTALL_DIR"

echo "Downloading $BINARY ..."
curl -fsSL "$DOWNLOAD_URL" -o "$DEST"
chmod +x "$DEST"

echo "Installed to $DEST"

if ! echo ":$PATH:" | grep -q ":${INSTALL_DIR}:"; then
    echo ""
    echo "Add the following to your shell profile (~/.bashrc, ~/.zshrc, etc.):"
    echo "  export PATH=\"\$PATH:${INSTALL_DIR}\""
fi
