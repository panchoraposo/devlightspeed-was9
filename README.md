# WAS9 → Quarkus (MTA + Developer Lightspeed)

Standalone demo repo with **only** WebSphere/Open Liberty (WAS9) FSI apps so MTA analysis is not mixed with EAP / Spring Boot / Fuse samples.

## Layout

| Path | Purpose |
|------|---------|
| [`source-apps/`](source-apps/) | **Legacy apps to analyze** (single copy — use these in MTA) |
| [`migrated-apps/`](migrated-apps/) | Red Hat Build of Quarkus 3 “after” targets (do not analyze) |
| [`.devfile.yaml`](.devfile.yaml) | OpenShift Dev Spaces workspace |

### Legacy (`source-apps/`)

| App | Path |
|-----|------|
| Accounts (REST + EJB/JPA/JNDI bait) | [`source-apps/fsi-accounts-was9-legacy`](source-apps/fsi-accounts-was9-legacy) |
| Cards (REST + JMS/MDB bait) | [`source-apps/fsi-cards-was9-legacy`](source-apps/fsi-cards-was9-legacy) |
| KYC (JAX-WS/JAXB bait) | [`source-apps/fsi-kyc-was9-legacy`](source-apps/fsi-kyc-was9-legacy) |

### Migrated (Red Hat Build of Quarkus 3)

| App | Path | Dev port | Run |
|-----|------|----------|-----|
| Accounts | [`migrated-apps/fsi-accounts-quarkus3`](migrated-apps/fsi-accounts-quarkus3) | 8088 | `./mvnw quarkus:dev` |
| Cards | [`migrated-apps/fsi-cards-quarkus3`](migrated-apps/fsi-cards-quarkus3) | 8089 | `./mvnw quarkus:dev` |
| KYC | [`migrated-apps/fsi-kyc-quarkus3`](migrated-apps/fsi-kyc-quarkus3) | 8090 | `./mvnw quarkus:dev` |

All three use platform `com.redhat.quarkus.platform:3.33.1.redhat-00006`. Cards replaces JMS/MDB with an in-process fraud worker; KYC replaces JAX-WS with REST + MicroProfile Config.

## Open in OpenShift Dev Spaces

Devfile is at the **repo root**: [`.devfile.yaml`](.devfile.yaml).

Factory-style URL (adjust host):

```text
https://devspaces.<cluster>/#https://github.com/panchoraposo/devlightspeed-was9
```

Or create a workspace from the Dev Spaces UI and select this repository (Devfile auto-detected at root).

### One-time setup

1. Wait for `postStart` bootstrap (JDK + MTA CLI + VSIX download), or run **Bootstrap MTA CLI + JDK + VS Code settings**.
2. **MTA extensions** — do **not** use the **Install Extension Pack** button (it often no-ops in Che-Code). `postStart` unpacks MTA Core + MTA Java into `/checode/remote/extensions`. When bootstrap finishes, run **Developer: Reload Window**. `source-apps/pom.xml` exists so MTA Java auto-activates (providers register). If the sidebar still says no providers, open any `.java` file under `source-apps/`, or run **Install MTA VS Code extensions**, then reload again.
3. Configure LLM: Command Palette → `MTA: Open the Gen AI model provider configuration file`.
4. Optional Hub: set `HUB_URL` in the Devfile (no trailing space) or configure Hub in the MTA UI.

### Analysis profile (per app)

| Field | Value |
|-------|--------|
| Source | `websphere` |
| Targets | `quarkus`, `openjdk17` |

**Analyze one folder under `source-apps/`**, for example `source-apps/fsi-accounts-was9-legacy`. Do not analyze the repo root or `migrated-apps/`.

## Local clone

```bash
git clone https://github.com/panchoraposo/devlightspeed-was9.git
cd devlightspeed-was9
```
