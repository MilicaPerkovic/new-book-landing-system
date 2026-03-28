# order-service

Reactive Quarkus 3 microservice that owns the preorder/order lifecycle for the Book Landing System. It persists orders in PostgreSQL, will expose reactive REST endpoints, and emits order-domain events over ActiveMQ Artemis.

## Current status (Part 1)
- Order aggregate (`OrderEntity`) plus lifecycle enum are implemented with optimistic locking and timestamp management.
- Flyway migration `V1__create_orders_table.sql` provisions schema `orders` with indexes required for future dashboards.
- Testcontainers-backed repository spec (`OrderRepositoryTest`) persists and reloads an order against PostgreSQL 15 to prove the reactive persistence stack works end-to-end.

## Database configuration
| Property | Default | Description |
| --- | --- | --- |
| `ORDERS_DB_HOST` | `localhost` | PostgreSQL host used by dev/test profiles |
| `ORDERS_DB_PORT` | `5440` | Host port mapped to the container/internal 5432 |
| `ORDERS_DB_DATABASE` | `orders` | Database/schema used by the service |
| `ORDERS_DB_USERNAME` | `orders_dev` | Login passed to both JDBC + reactive clients |
| `ORDERS_DB_PASSWORD` | `orders_dev` | Password counterpart |

Startup automatically runs Flyway migrations (`quarkus.flyway.migrate-at-start=true`). Schema defaults to `orders`, so make sure the database user can create schemas on first boot.

## Running the application in dev mode

1. Provision PostgreSQL (example):
   ```bash
   docker run --rm -p 5440:5432 \
     -e POSTGRES_DB=orders \
     -e POSTGRES_USER=orders_dev \
     -e POSTGRES_PASSWORD=orders_dev \
     postgres:15.6-alpine
   ```
2. From the monorepo root execute:
   ```bash
   ./mvnw -pl services/order-service quarkus:dev
   ```

The Quarkus Dev UI will be exposed at http://localhost:8080/q/dev/.

## Testing

`OrderRepositoryTest` boots PostgreSQL 15 via Testcontainers; no manual DB setup is needed. Run:

```bash
./mvnw test -pl services/order-service -am
```

## Packaging and running the application

```bash
./mvnw package -pl services/order-service -am
java -jar services/order-service/target/quarkus-app/quarkus-run.jar
```

To build an _über-jar_:

```bash
./mvnw package -pl services/order-service -am -Dquarkus.package.jar.type=uber-jar
java -jar services/order-service/target/order-service-1.0.0-SNAPSHOT-runner.jar
```

## Native executable (optional)

```bash
./mvnw package -pl services/order-service -am -Dnative
```

Or rely on containerized native builds:

```bash
./mvnw package -pl services/order-service -am -Dnative -Dquarkus.native.container-build=true
```
