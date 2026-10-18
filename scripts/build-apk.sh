#!/usr/bin/env bash
# Builds a debug APK and prints its path.
# Usage: scripts/build-apk.sh
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

./gradlew :app:assembleDebug --console=plain

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
echo
echo "APK ready: $(realpath "$APK_PATH")"
