#!/usr/bin/env bash
# Install a user-local Temurin JDK 25 for vscode-java / JDT LS (no root required).
# Soft-fails (exit 0) so workspace postStart never blocks the IDE.
#
# Language Support for Java 1.58+ needs JDK 25+ to *run* the language server.
# Project compile/runtime can stay on Java 17 via java.configuration.runtimes.
set -uo pipefail

JDTLS_JDK_HOME="${JDTLS_JDK_HOME:-/home/user/jdk-25}"
JDK_URL="${JDK25_URL:-https://api.adoptium.net/v3/binary/latest/25/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk}"

if [[ -x "${JDTLS_JDK_HOME}/bin/java" ]]; then
  echo "JDK 25 already present at ${JDTLS_JDK_HOME}"
  "${JDTLS_JDK_HOME}/bin/java" -version 2>&1 || true
  exit 0
fi

TMP="$(mktemp -d)"
cleanup() { rm -rf "${TMP}"; }
trap cleanup EXIT

echo "Downloading Temurin JDK 25 → ${JDTLS_JDK_HOME}"
if ! curl -fL --retry 3 --connect-timeout 20 --max-time 300 -o "${TMP}/jdk.tgz" "${JDK_URL}"; then
  echo "WARN: JDK 25 download failed (network policy / URL). Set JDK25_URL to an internal mirror and re-run." >&2
  exit 0
fi

mkdir -p "${TMP}/extract" "${JDTLS_JDK_HOME}"
if ! tar -xzf "${TMP}/jdk.tgz" -C "${TMP}/extract"; then
  echo "WARN: failed to extract JDK 25 archive" >&2
  exit 0
fi

INNER="$(find "${TMP}/extract" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
if [[ -z "${INNER}" || ! -x "${INNER}/bin/java" ]]; then
  echo "WARN: unexpected JDK 25 archive layout" >&2
  exit 0
fi

rm -rf "${JDTLS_JDK_HOME}.partial"
mv "${INNER}" "${JDTLS_JDK_HOME}.partial"
rm -rf "${JDTLS_JDK_HOME}"
mv "${JDTLS_JDK_HOME}.partial" "${JDTLS_JDK_HOME}"

echo "Installed JDT LS JDK:"
"${JDTLS_JDK_HOME}/bin/java" -version 2>&1 || true
echo "JDTLS_JDK_HOME=${JDTLS_JDK_HOME}"
exit 0
