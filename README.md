# WAS9 → Quarkus (MTA + Developer Lightspeed)

Standalone demo repo with **only** WebSphere/Open Liberty (WAS9) FSI apps so MTA analysis is not mixed with EAP / Spring Boot / Fuse samples.

## Apps

| App | Path |
|-----|------|
| Accounts (REST + EJB/JPA/JNDI bait) | [`apps/fsi-accounts-was9-legacy`](apps/fsi-accounts-was9-legacy) |
| Cards (REST + JMS/MDB bait) | [`apps/fsi-cards-was9-legacy`](apps/fsi-cards-was9-legacy) |
| KYC (JAX-WS/JAXB bait) | [`apps/fsi-kyc-was9-legacy`](apps/fsi-kyc-was9-legacy) |

Pristine copies: [`source-apps/`](source-apps/). Quarkus reference stubs: [`migrated-apps/`](migrated-apps/).

## Open in OpenShift Dev Spaces

Devfile is at the **repo root**: [`.devfile.yaml`](.devfile.yaml).

Factory-style URL (adjust host):

```text
https://devspaces.<cluster>/#https://github.com/panchoraposo/devlightspeed-was9
```

Or create a workspace from the Dev Spaces UI and select this repository (Devfile auto-detected at root).

### One-time setup

1. Wait for `postStart` bootstrap (JDK + MTA CLI), or run **Bootstrap MTA CLI + JDK + VS Code settings**.
2. Accept recommended extensions (MTA Core / Java / Lightspeed).
3. Configure LLM: Command Palette → `MTA: Open the Gen AI model provider configuration file`.
4. Optional Hub: set `HUB_URL` in the Devfile (no trailing space) or configure Hub in the MTA UI.

### Analysis profile (per app)

| Field | Value |
|-------|--------|
| Source | `websphere` |
| Targets | `quarkus`, `openjdk17` |

**Analyze one app folder at a time**, for example `apps/fsi-accounts-was9-legacy` — not the repo root.

## Local clone

```bash
git clone https://github.com/panchoraposo/devlightspeed-was9.git
cd devlightspeed-was9
```
