# fsi-cards-quarkus3

Migrated target for `fsi-cards-was9-legacy` (WAS9 / Open Liberty → Red Hat Build of Quarkus 3).

## What changed vs legacy

| Legacy (WAS9 / Liberty) | Migrated (Quarkus 3) |
|-------------------------|----------------------|
| `javax.*` + `@EJB` | `jakarta.*` + `@Inject` / CDI |
| JNDI `jdbc/CardsDS` | Quarkus datasource + CDI `DataSource` |
| `persistence.xml` + Hibernate 5 Criteria | `application.properties` + JPA Criteria API |
| `@Type(type="yes_no")` | `YesNoConverter` |
| JMS `FraudQueueProducer` + `FraudProcessorMDB` | In-process `FraudCheckWorker` (thread-pool async) |
| `ibm-web-bnd.xml` / `ibm-web-ext.xml` | removed |

## Run

```bash
./mvnw quarkus:dev
```

- UI: http://localhost:8089/
- API: `GET/POST http://localhost:8089/api/auths`
