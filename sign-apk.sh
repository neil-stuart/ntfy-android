#!/bin/sh
# Builds and signs the Home Notifications release APK into dist/.
# Signing key lives outside the repo: ~/.android/home-notifications-release.jks (+ .pass)
set -e
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
BT="$ANDROID_HOME/build-tools/36.0.0"
./gradlew assembleFdroidRelease -q
VERSION=$(grep -m1 'versionName' app/build.gradle | sed -E 's/.*"(.*)".*/\1/')
mkdir -p dist
"$BT/zipalign" -f -p 4 app/build/outputs/apk/fdroid/release/app-fdroid-release-unsigned.apk dist/.aligned.apk
"$BT/apksigner" sign --ks "$HOME/.android/home-notifications-release.jks" --ks-key-alias homenotifications \
  --ks-pass "file:$HOME/.android/home-notifications-release.pass" --out "dist/HomeNotifications-$VERSION.apk" dist/.aligned.apk
rm -f dist/.aligned.apk dist/*.idsig
"$BT/apksigner" verify "dist/HomeNotifications-$VERSION.apk"
echo "dist/HomeNotifications-$VERSION.apk"
