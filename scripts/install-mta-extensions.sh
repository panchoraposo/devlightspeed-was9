#!/usr/bin/env bash
# Install MTA / Java VS Code extensions into Che-Code by unpacking Open VSX .vsix
# files into /checode/remote/extensions and updating extensions.json.
#
# Why: In Dev Spaces (Che-Code browser IDE), the MTA webview "Install Extension Pack"
# button often does nothing. `code-oss --install-extension` also refuses outside an
# IDE terminal. Unpacking VSIX is the reliable path.
#
# Soft-fails (exit 0) so workspace postStart never blocks the IDE.
set -uo pipefail

VSIX_DIR="${MTA_VSIX_DIR:-/home/user/.mta-vsix}"
EXT_ROOT="${CHECODE_EXTENSIONS_DIR:-/checode/remote/extensions}"
# Pin Language Support for Java: Open VSX "latest" (1.59+) requires JDK 25+.
# Dev Spaces UDI / this Devfile use Java 17.
JAVA_EXT_VERSION="${MTA_JAVA_EXT_VERSION:-1.58.0}"
mkdir -p "${VSIX_DIR}"

log() { echo "$*" >&2; }

if [[ ! -d "${EXT_ROOT}" ]]; then
  log "WARN: ${EXT_ROOT} not found yet (Che-Code not mounted). VSIX will be cached in ${VSIX_DIR} only."
fi

# Remove Language Support for Java builds that need JDK 25+
if [[ -d "${EXT_ROOT}" ]]; then
  shopt -s nullglob
  for d in "${EXT_ROOT}"/redhat.java-1.59*; do
    log "Removing JDK25+ Java extension: ${d}"
    rm -rf "${d}"
  done
  shopt -u nullglob
fi

# Args: publisher name [version]
# Prints: <version><TAB><vsix-path> on stdout only.
download_vsix() {
  local publisher="$1" name="$2" pinned="${3:-}"
  local out meta url ver tmp api

  if [[ -n "${pinned}" ]]; then
    api="https://open-vsx.org/api/${publisher}/${name}/${pinned}"
    out="${VSIX_DIR}/${publisher}.${name}-${pinned}.vsix"
    log "Resolving Open VSX ${publisher}.${name}@${pinned}"
  else
    api="https://open-vsx.org/api/${publisher}/${name}/latest"
    out="${VSIX_DIR}/${publisher}.${name}.vsix"
    log "Resolving Open VSX latest: ${publisher}.${name}"
  fi

  if ! meta="$(curl -fsSL --retry 3 --connect-timeout 20 --max-time 60 "${api}")"; then
    log "WARN: Open VSX metadata failed for ${publisher}.${name}${pinned:+@$pinned}"
    return 1
  fi
  url="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["files"]["download"])' <<<"${meta}")" || return 1
  ver="$(python3 -c 'import json,sys; print(json.load(sys.stdin)["version"])' <<<"${meta}")" || return 1

  if [[ ! -s "${out}" ]]; then
    log "Downloading ${url}"
    tmp="${out}.partial"
    if ! curl -fL --retry 3 --connect-timeout 20 --max-time 600 -o "${tmp}" "${url}"; then
      log "WARN: download failed for ${publisher}.${name}"
      rm -f "${tmp}"
      return 1
    fi
    if command -v file >/dev/null 2>&1 && file "${tmp}" | grep -qi gzip; then
      mv "${tmp}" "${tmp}.gz"
      gunzip -f "${tmp}.gz" || true
      [[ -f "${tmp}" ]] || { log "WARN: gunzip failed"; return 1; }
    fi
    mv -f "${tmp}" "${out}"
  else
    log "Using cached ${out}"
  fi
  printf '%s\t%s\n' "${ver}" "${out}"
}

install_into_checode() {
  local publisher="$1" name="$2" ver="$3" vsix="$4"
  if [[ ! -d "${EXT_ROOT}" ]]; then
    return 0
  fi
  if [[ ! -f "${vsix}" ]]; then
    log "WARN: VSIX file missing: ${vsix}"
    return 1
  fi

  local dest="${EXT_ROOT}/${publisher}.${name}-${ver}-universal"
  if [[ -f "${dest}/package.json" ]]; then
    log "Already installed: ${dest}"
  else
    local tmp
    tmp="$(mktemp -d)"
    if ! unzip -q "${vsix}" -d "${tmp}"; then
      log "WARN: unzip failed for ${vsix}"
      rm -rf "${tmp}"
      return 1
    fi
    if [[ ! -f "${tmp}/extension/package.json" ]]; then
      log "WARN: unexpected VSIX layout in ${vsix}"
      rm -rf "${tmp}"
      return 1
    fi
    rm -rf "${dest}"
    mv "${tmp}/extension" "${dest}"
    rm -rf "${tmp}"
    log "Unpacked → ${dest}"
  fi

  python3 - "${EXT_ROOT}/extensions.json" "${publisher}" "${name}" "${ver}" "${dest}" <<'PY' || true
import json, os, sys, time, uuid
path, pub, name, ver, dest = sys.argv[1:6]
ext_id = f"{pub}.{name}"
try:
    data = json.load(open(path))
    if not isinstance(data, list):
        data = []
except Exception:
    data = []
data = [e for e in data if e.get("identifier", {}).get("id") != ext_id]
data.append({
    "identifier": {"id": ext_id, "uuid": str(uuid.uuid4())},
    "version": ver,
    "location": {"$mid": 1, "path": dest, "scheme": "file"},
    "relativeLocation": os.path.basename(dest),
    "metadata": {
        "installedTimestamp": int(time.time() * 1000),
        "source": "vsix",
        "id": str(uuid.uuid4()),
        "publisherId": pub,
        "publisherDisplayName": pub,
        "targetPlatform": "universal",
        "updated": False,
        "private": False,
        "isPreReleaseVersion": False,
        "hasPreReleaseVersion": False,
    },
})
with open(path, "w") as f:
    json.dump(data, f)
print(f"Registered {ext_id}@{ver} ({len(data)} extensions total)", file=sys.stderr)
PY
}

install_one() {
  local publisher="$1" name="$2" pinned="${3:-}"
  local info ver vsix
  if ! info="$(download_vsix "${publisher}" "${name}" "${pinned}")"; then
    return 1
  fi
  ver="${info%%$'\t'*}"
  vsix="${info#*$'\t'}"
  install_into_checode "${publisher}" "${name}" "${ver}" "${vsix}" || true
}

# Core pair required for "analysis providers are registered"
install_one redhat mta-core || true
install_one redhat mta-java || true
install_one redhat mta-vscode-extension || true
# MUST pin: latest Language Support for Java needs JDK 25+
install_one redhat java "${JAVA_EXT_VERSION}" || true
install_one redhat vscode-xml || true

log "Installed under ${EXT_ROOT}:"
ls -d "${EXT_ROOT}"/redhat.mta-* "${EXT_ROOT}"/redhat.java-* 2>/dev/null >&2 || log "(none yet)"
log "Next: Command Palette → Developer: Reload Window"
exit 0
