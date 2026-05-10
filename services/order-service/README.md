# Order Service Quick Start

Order Service is a Quarkus microservice for order lifecycle management (create, confirm, cancel, fulfill), backed by PostgreSQL and ActiveMQ Artemis.

## Prerequisites

- Docker + Docker Compose
- Java 21
- Maven Wrapper (`./mvnw`) from this repository

## Quick Start (Docker)

Run from project root (`new-book-landing-system`):

1. Build the service jar (required for current Dockerfile):

```bash
./services/order-service/mvnw -f services/order-service/pom.xml -DskipTests package -Dquarkus.package.type=uber-jar
```

2. Build and start services:

```bash
docker compose build order-service
docker compose up -d
```

3. Verify service health:

```bash
curl http://localhost:8080/q/health
```

Expected response contains:

```json
{
   "status": "UP"
}
```

## First API Call

Create one order:

```bash
curl -X POST http://localhost:8080/api/orders \
   -H "Content-Type: application/json" \
   -d '{
      "bookId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "price": 24.99
   }'
```

## API Calls (Test Every Function)

Use these calls to verify each endpoint.

Important:
- If you use a variable, write `$ORDER_ID`.
- If you paste a literal UUID, do NOT put `$` before it.

### 1) Create order

```bash
CREATE_RESPONSE=$(curl -sS -X POST http://localhost:8080/api/orders \
   -H "Content-Type: application/json" \
   -d '{
      "bookId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "price": 24.99
   }')

echo "$CREATE_RESPONSE"
```

### 2) Extract order id

If you have jq:

```bash
ORDER_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
echo "$ORDER_ID"
```

Without jq, copy the id manually from CREATE_RESPONSE and set it:

```bash
ORDER_ID="paste-your-order-id-here"
echo "$ORDER_ID"
```

Example literal UUID style (no `$`):

```bash
curl -sS http://localhost:8080/api/orders/your-order-id-here
```

### 3) Get order by id

```bash
curl -sS http://localhost:8080/api/orders/$ORDER_ID
```

Or with a pasted UUID (no `$`):

```bash
curl -sS http://localhost:8080/api/orders/your-order-id-here
```

### 4) Confirm order

```bash
curl -sS -X POST http://localhost:8080/api/orders/$ORDER_ID/confirm
```

Or with a pasted UUID (no `$`):

```bash
curl -sS -X POST http://localhost:8080/api/orders/your-order-id-here/confirm
```

### 5) Fulfill order (works after confirm)

```bash
curl -sS -X POST http://localhost:8080/api/orders/$ORDER_ID/fulfill
```

Or with a pasted UUID (no `$`):

```bash
curl -sS -X POST http://localhost:8080/api/orders/your-order-id-here/fulfill
```

### 6) Create second order for cancel test

```bash
CANCEL_RESPONSE=$(curl -sS -X POST http://localhost:8080/api/orders \
   -H "Content-Type: application/json" \
   -d '{
      "bookId": "550e8400-e29b-41d4-a716-446655440001",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "quantity": 1,
      "price": 19.99
   }')

echo "$CANCEL_RESPONSE"
```

```bash
CANCEL_ORDER_ID=$(echo "$CANCEL_RESPONSE" | jq -r '.id')
echo "$CANCEL_ORDER_ID"
```

If you do not use jq:

```bash
CANCEL_ORDER_ID="paste-second-order-id-here"
echo "$CANCEL_ORDER_ID"
```

### 7) Cancel order

```bash
curl -sS -X POST http://localhost:8080/api/orders/$CANCEL_ORDER_ID/cancel
```

Or with a pasted UUID (no `$`):

```bash
curl -sS -X POST http://localhost:8080/api/orders/your-second-order-id-here/cancel
```

### 8) Check invalid/not found case

```bash
curl -sS http://localhost:8080/api/orders/00000000-0000-0000-0000-000000000000
```

### 9) Health check

```bash
curl -sS http://localhost:8080/q/health
```

## Swagger Checks

### Open Swagger UI in browser

```bash
open http://localhost:8080/q/swagger-ui/
```

### Open OpenAPI JSON

```bash
curl -sS http://localhost:8080/q/openapi | head -n 40
```

### What to verify in Swagger

- POST /api/orders
- GET /api/orders/{orderId}
- POST /api/orders/{orderId}/confirm
- POST /api/orders/{orderId}/cancel
- POST /api/orders/{orderId}/fulfill

Expected state rules:
- confirm works only for PENDING
- fulfill works only for CONFIRMED
- cancel works only for non-terminal orders (not CANCELLED/FULFILLED)

### Swagger primeri (Try it out)

Do not reuse one hardcoded UUID for everything.
Create a fresh order first, then use that returned id.

POST /api/orders body example:

```json
{
   "bookId": "550e8400-e29b-41d4-a716-446655440000",
   "userId": "660e8400-e29b-41d4-a716-446655440000",
   "quantity": 2,
   "price": 24.99
}
```

Step 1: in Swagger run POST /api/orders and copy id from response.

Step 2: run GET /api/orders/{orderId}

- orderId: id from Step 1

Step 3: run POST /api/orders/{orderId}/confirm

- orderId: same id from Step 1 (must still be PENDING)

Step 4: run POST /api/orders/{orderId}/fulfill

- orderId: same id after Step 3 (now CONFIRMED)

Step 5 (cancel test): create another fresh order with POST /api/orders.

Step 6: run POST /api/orders/{orderId}/cancel

- orderId: id from Step 5

If Swagger returns 409 INVALID_STATE_TRANSITION, the order is in the wrong state for that action.

## Useful URLs

- API base: `http://localhost:8080`
- Health: `http://localhost:8080/q/health`
- Swagger UI: `http://localhost:8080/q/swagger-ui/`
- Artemis console: `http://localhost:8161`
- pgAdmin: `http://localhost:5050`

## Run Tests

From project root:

```bash
./services/order-service/mvnw -f services/order-service/pom.xml test
```

## Stop / Reset

Stop services:

```bash
docker compose down
```

Full reset (containers + volumes):

```bash
docker compose down -v
```

## Local Dev Mode (without Docker for app)

1. Start only infrastructure:

```bash
docker compose up -d postgresql artemis
```

2. Run service in dev mode:

```bash
./services/order-service/mvnw -f services/order-service/pom.xml quarkus:dev
```

## Swagger UI Troubleshooting

If you get `Resource not found` on Swagger UI:

1. Use the URL with trailing slash:

```bash
http://localhost:8080/q/swagger-ui/
```

2. Ensure service is healthy:

```bash
curl http://localhost:8080/q/health
```

3. Rebuild and restart if you changed configuration:

```bash
./services/order-service/mvnw -f services/order-service/pom.xml -DskipTests package -Dquarkus.package.type=uber-jar
docker compose build order-service
docker compose up -d order-service
```
