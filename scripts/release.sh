#!/usr/bin/env bash
# Builds a signed release APK, tags it, and publishes a GitHub release.
# Usage: scripts/release.sh <versionName e.g. 0.2.0>
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

VERSION_NAME="${1:?Usage: scripts/release.sh <versionName e.g. 0.2.0>}"
TAG="v${VERSION_NAME}"

if [ ! -f keystore.properties ]; then
    echo "keystore.properties not found - release build would be unsigned. Aborting." >&2
    exit 1
fi

if git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Tag $TAG already exists." >&2
    exit 1
fi

CURRENT_CODE=$(grep -oE 'versionCode = [0-9]+' app/build.gradle.kts | grep -oE '[0-9]+')
NEXT_CODE=$((CURRENT_CODE + 1))

sed -i "s/versionCode = [0-9]*/versionCode = ${NEXT_CODE}/" app/build.gradle.kts
sed -i "s/versionName = \"[^\"]*\"/versionName = \"${VERSION_NAME}\"/" app/build.gradle.kts

./gradlew :app:assembleRelease --console=plain

APK_SRC="app/build/outputs/apk/release/app-release.apk"
APK_DEST="app-release-${TAG}.apk"
cp "$APK_SRC" "$APK_DEST"

git add app/build.gradle.kts
git commit -m "Release ${TAG} (versionCode ${NEXT_CODE})"
git tag "$TAG"
git push origin main
git push origin "$TAG"

gh release create "$TAG" "$APK_DEST" \
    --title "Closet Finder ${TAG}" \
    --generate-notes

rm -f "$APK_DEST"

echo
echo "Released ${TAG}. Obtainium/komi-store will pick it up from GitHub Releases on this repo."
