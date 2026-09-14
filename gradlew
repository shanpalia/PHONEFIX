#!/bin/sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.3.1
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/phonefix-dists/gradle-$GRADLE_VERSION"
GRADLE_BIN="$DIST_DIR/gradle-$GRADLE_VERSION/bin/gradle"
ZIP="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  echo "Downloading Gradle $GRADLE_VERSION..."
  curl -fL --retry 3 --connect-timeout 20 "$URL" -o "$ZIP"
  rm -rf "$DIST_DIR/gradle-$GRADLE_VERSION"
  unzip -q "$ZIP" -d "$DIST_DIR"
  chmod +x "$GRADLE_BIN"
fi

exec "$GRADLE_BIN" -Dorg.gradle.appname=gradlew "$@"
