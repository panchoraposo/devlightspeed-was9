# fsi-kyc-quarkus3

Migrated target for `fsi-kyc-was9-legacy` (WAS9 / Open Liberty → Red Hat Build of Quarkus 3).

## What changed vs legacy

| Legacy (WAS9 / Liberty) | Migrated (Quarkus 3) |
|-------------------------|----------------------|
| JAX-WS SOAP `KycService` / `KycServiceImpl` | Quarkus REST (`KycResource`) |
| `javax.*` (JAX-WS, JAXB) | `jakarta.*` + Jackson JSON |
| `/etc/fsi/kyc/scoring.properties` + system props (`LegacyKycConfig`) | MicroProfile Config `@ConfigProperty` (`fsi.kyc.scoring.mode`) |
| SOAP handler MD5 audit (`KycAuditHandler`) | SHA-256 audit hashing (`KycAuditHasher`) |
| Singleton `KycCaseStore.getInstance()` | CDI `@ApplicationScoped` `KycCaseStore` |
| `ibm-web-bnd.xml` / `ibm-web-ext.xml` / `kyc-handlers.xml` | removed |
| In-memory store (no JDBC) | unchanged — no Hibernate/PostgreSQL |

## REST endpoints (SOAP mapping)

| SOAP operation | HTTP | Path |
|----------------|------|------|
| `submitApplication` | `POST` | `/api/kyc/applications` (alias `/api/kyc/submit`) |
| `getStatus` | `GET` | `/api/kyc/applications/{applicationId}` (alias `/api/kyc/status/{applicationId}`) |

## Run

```bash
./mvnw quarkus:dev
```

- UI / API docs: http://localhost:8090/
- Submit: `POST http://localhost:8090/api/kyc/applications`
- Status: `GET http://localhost:8090/api/kyc/applications/{applicationId}`
