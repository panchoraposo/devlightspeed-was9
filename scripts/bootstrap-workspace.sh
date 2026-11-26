#!/usr/bin/env bash
# postStart bootstrap: must always exit 0 so Dev Spaces can open the IDE.
set -uo pipefail

ROOT="${PROJECT_SOURCE:-}"
if [[ -z "${ROOT}" ]]; then
  ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
fi
SCRIPTS="${ROOT}/scripts"
JDK_HOME="${JDK_HOME:-/home/user/jdk-17}"
JDTLS_JDK_HOME="${JDTLS_JDK_HOME:-/home/user/jdk-25}"

echo "Bootstrapping Dev Spaces workspace from ${ROOT}"

# VS Code / Che-Code settings for MTA (Linux analyzer path)
mkdir -p "${ROOT}/.vscode"
if [[ -f "${ROOT}/.vscode/extensions.json" ]]; then
  : # already in repo
fi

resolve_java_home() {
  local candidate
  for candidate in \
    "${JAVA_HOME:-}" \
    "${JDK_HOME:-}" \
    "${JAVA_HOME_17:-}" \
    /usr/lib/jvm/java-17-openjdk \
    /home/user/.java/current \
    /home/user/jdk-17 \
    /home/tooling/.java/current \
    /home/tooling/.sdkman/candidates/java/current \
    /home/user/.sdkman/candidates/java/current \
    /usr/lib/jvm/jre-17-openjdk
  do
    if [[ -n "${candidate}" && -x "${candidate}/bin/java" ]]; then
      echo "${candidate}"
      return 0
    fi
  done
  if command -v java >/dev/null 2>&1; then
    local java_bin
    java_bin="$(command -v java)"
    if [[ -L "${java_bin}" ]]; then
      java_bin="$(readlink -f "${java_bin}" 2>/dev/null || true)"
    fi
    if [[ -n "${java_bin}" && -x "${java_bin}" ]]; then
      echo "$(cd "$(dirname "${java_bin}")/.." && pwd)"
      return 0
    fi
  fi
  return 1
}

JAVA_RESOLVED="$(resolve_java_home || true)"
# UDI often has OpenJDK RPMs but no /usr/bin/java — fall back to Temurin in $HOME.
if [[ -z "${JAVA_RESOLVED}" ]]; then
  bash "${SCRIPTS}/install-jdk17.sh" || true
  JAVA_RESOLVED="$(resolve_java_home || true)"
fi
if [[ -n "${JAVA_RESOLVED}" ]]; then
  echo "Resolved project JAVA_HOME=${JAVA_RESOLVED}"
  "${JAVA_RESOLVED}/bin/java" -version 2>&1 || true
  mkdir -p /home/user/.local/bin
  ln -sfn "${JAVA_RESOLVED}/bin/java" /home/user/.local/bin/java || true
  ln -sfn "${JAVA_RESOLVED}/bin/javac" /home/user/.local/bin/javac || true
  if ! grep -q 'MTA_DEVSPACES_JAVA' /home/user/.bashrc 2>/dev/null; then
    cat >> /home/user/.bashrc <<EOF

# MTA_DEVSPACES_JAVA
export JAVA_HOME="${JAVA_RESOLVED}"
export PATH="\${JAVA_HOME}/bin:/home/user/.local/bin:/home/user/mta:\${PATH}"
EOF
  else
    sed -i "s|^export JAVA_HOME=.*|export JAVA_HOME=\"${JAVA_RESOLVED}\"|" /home/user/.bashrc || true
  fi
else
  echo "WARN: no project JDK found. Run: bash scripts/install-jdk17.sh" >&2
fi

# vscode-java 1.58+ needs JDK 25+ to run JDT LS; projects stay on Java 17.
bash "${SCRIPTS}/install-jdk25.sh" || true
if [[ -x "${JDTLS_JDK_HOME}/bin/java" && -f "${ROOT}/.vscode/settings.json" ]] && command -v python3 >/dev/null 2>&1; then
  echo "Pointing java.jdt.ls.java.home at ${JDTLS_JDK_HOME}"
  python3 - "${ROOT}/.vscode/settings.json" "${JDTLS_JDK_HOME}" "${JAVA_RESOLVED:-/usr/lib/jvm/java-17-openjdk}" <<'PY' || true
import json, sys
path, jdtls, project = sys.argv[1], sys.argv[2], sys.argv[3]
with open(path) as f:
    data = json.load(f)
data["java.jdt.ls.java.home"] = jdtls
runtimes = [
    {"name": "JavaSE-17", "path": project, "default": True},
    {"name": "JavaSE-25", "path": jdtls},
]
data["java.configuration.runtimes"] = runtimes
with open(path, "w") as f:
    json.dump(data, f, indent=2)
    f.write("\n")
PY
fi

bash "${SCRIPTS}/install-mta-cli.sh" || true
# Che-Code "Install Extension Pack" often no-ops; pre-download VSIX for DEFAULT_EXTENSIONS.
bash "${SCRIPTS}/install-mta-extensions.sh" || true

mkdir -p /home/user/.local/bin
if [[ -x /home/user/mta/linux-mta-cli ]]; then
  ln -sfn /home/user/mta/linux-mta-cli /home/user/.local/bin/linux-mta-cli || true
fi

echo "Workspace bootstrap finished."
echo "If Language Support for Java fails: verify /home/user/jdk-25/bin/java -version, then Reload Window."
echo "If 'No analysis providers are registered': wait for VSIX install, then Reload Window (or run Install MTA VS Code extensions)."
exit 0
