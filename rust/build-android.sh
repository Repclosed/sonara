#!/usr/bin/env bash
# Cross-compiles the Sonara native core for Android ABIs.
# Optional: the app builds and runs without this (Kotlin fallback in SonaraNativeCore.kt).
set -euo pipefail

cd "$(dirname "$0")"

: "${ANDROID_NDK_HOME:?ANDROID_NDK_HOME must point at an installed NDK}"

TARGETS=(
  "aarch64-linux-android:arm64-v8a"
  "armv7-linux-androideabi:armeabi-v7a"
  "x86_64-linux-android:x86_64"
)

OUT_DIR="../app/src/main/jniLibs"

for entry in "${TARGETS[@]}"; do
  triple="${entry%%:*}"
  abi="${entry##*:}"

  echo "==> building $triple"
  cargo build --release --target "$triple"

  mkdir -p "$OUT_DIR/$abi"
  cp "target/$triple/release/libsonara_core.so" "$OUT_DIR/$abi/libsonara_core.so"
done

echo "Done. Native libraries copied to $OUT_DIR"
