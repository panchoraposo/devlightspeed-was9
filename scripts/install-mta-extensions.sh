#!/usr/bin/env bash
# Download MTA / Java VS Code extensions as .vsix for Che-Code DEFAULT_EXTENSIONS.
# Soft-fails (exit 0) so workspace postStart never blocks the IDE.
# See: https://docs.redhat.com/en/documentation/red_hat_openshift_dev_spaces/3.31/html/administration_guide/assembly_configuring-vscode
set -uo pipefail

VSIX_DIR="${MTA_VSIX_DIR:-/home/user/.mta-vsix}"
mkdir -p "${VSIX_DIR}"

download_one() {
  local publisher="$1"
  local name="$2"
  local out="${VSIX_DIR}/${publisher}.${name}.vsix"
  if [[ -s "${out}" ]]; then
    echo "Already present: ${out} ($(wc -c < "${out}") bytes)"
    return 0
  fi
  echo "Resolving Open VSX latest: ${publisher}.${name}"
  local meta url
  if ! meta="$(curl -fsSL --retry 3 --connect-timeout 20 --max-time 60 \
      "https://open-vsx.org/api/${publisher}/${name}/latest")"; then
    echo "WARN: Open VSX metadata failed for ${publisher}.${name}" >&2
    return 1
  fi
  url="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["files"]["download"])' <<<"${meta}" 2>/dev/null || true)"
  if [[ -z "${url}" ]]; then
    echo "WARN: no download URL for ${publisher}.${name}" >&2
    return 1
  fi
  echo "Downloading ${url}"
  local tmp="${out}.partial"
  if ! curl -fL --retry 3 --connect-timeout 20 --max-time 600 -o "${tmp}" "${url}"; then
    echo "WARN: download failed for ${publisher}.${name}" >&2
    rm -f "${tmp}"
    return 1
  fi
  # Open VSX sometimes returns gzip; normalize to .vsix
  if file "${tmp}" | grep -qi 'gzip'; then
    mv "${tmp}" "${tmp}.gz"
    gunzip -f "${tmp}.gz" || true
    [[ -f "${tmp}" ]] || mv "${tmp}.gz" "${tmp}" 2>/dev/null || true
  fi
  mv -f "${tmp}" "${out}"
  echo "Saved ${out} ($(wc -c < "${out}") bytes)"
}

# Core MTA + Java language provider (needed for "analysis providers are registered")
download_one redhat mta-core || true
download_one redhat mta-java || true
download_one redhat mta-vscode-extension || true
# Helpful companion extensions
download_one redhat java || true
download_one redhat vscode-xml || true

echo "VSIX directory listing:"
ls -la "${VSIX_DIR}" || true
echo "If extensions are not active yet: Command Palette → Developer: Reload Window"
exit 0
