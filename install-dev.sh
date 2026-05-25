#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_DIR="${DILOT_INSTALL_DIR:-$HOME/.local/bin}"
DEST="${INSTALL_DIR}/dilot"

echo "Building dilot..."
"${SCRIPT_DIR}/gradlew" -p "${SCRIPT_DIR}" linkReleaseExecutableNative

mkdir -p "$INSTALL_DIR"
cp "${SCRIPT_DIR}/build/bin/native/releaseExecutable/dilot.kexe" "$DEST"
chmod +x "$DEST"

echo "Installed to $DEST"

if ! echo ":$PATH:" | grep -q ":${INSTALL_DIR}:"; then
    echo ""
    echo "Add the following to your shell profile (~/.bashrc, ~/.zshrc, etc.):"
    echo "  export PATH=\"\$PATH:${INSTALL_DIR}\""
fi
