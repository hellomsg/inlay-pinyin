#!/usr/bin/env bash
set -euo pipefail

release_tag=$1
for setting in RELEASE_KEYSTORE RELEASE_STORE_PASSWORD RELEASE_KEY_ALIAS RELEASE_KEY_PASSWORD; do
    if [[ -z ${!setting:-} ]]; then
        printf 'Missing signing setting: %s\n' "$setting" >&2
        exit 1
    fi
done

sdk_tools="$ANDROID_HOME/build-tools/36.0.0"
unsigned_apk=app/build/outputs/apk/release/app-release-unsigned.apk
scratch=$(mktemp -d "${RUNNER_TEMP}/inlay-signing.XXXXXXXX")
trap 'rm -rf -- "$scratch"' EXIT
umask 077
mkdir -p dist
apk="dist/inlay-pinyin-${release_tag}.apk"
printf '%s' "$RELEASE_KEYSTORE" | base64 --decode > "$scratch/release.keystore"
"$sdk_tools/zipalign" -P 16 -f 4 "$unsigned_apk" "$scratch/aligned.apk"
"$sdk_tools/apksigner" sign --ks "$scratch/release.keystore" --ks-key-alias "$RELEASE_KEY_ALIAS" \
    --ks-pass env:RELEASE_STORE_PASSWORD --key-pass env:RELEASE_KEY_PASSWORD --out "$apk" "$scratch/aligned.apk"
"$sdk_tools/apksigner" verify --print-certs "$apk" > dist/signing-certificate.txt

identity=$("$sdk_tools/aapt" dump badging "$apk" | awk '/^package: / { print }')
version="${release_tag#v}"
version_code=$(sed -n 's/^ *versionCode = \([0-9][0-9]*\)$/\1/p' app/build.gradle.kts)
if [[ "$identity" != *"name='io.github.dakingeman.inlay.pinyin'"* ||
      "$identity" != *"versionName='$version'"* || "$identity" != *"versionCode='$version_code'"* ]]; then
    echo 'The signed APK has an unexpected package name or version.' >&2
    exit 1
fi
cp LICENSE dist/LICENSE
(cd dist && sha256sum "${apk##*/}" > SHA256SUMS)
