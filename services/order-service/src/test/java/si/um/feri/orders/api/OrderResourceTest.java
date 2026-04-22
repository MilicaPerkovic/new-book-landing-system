package si.um.feri.orders.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import si.um.feri.orders.application.dto.CreateOrderRequest;
import si.um.feri.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OrderResource REST endpoints.
 * 
 * Tests verify REST layer functionality including:
 * - HTTP status codes
 * - Request/response handling
 * - Error mapping
 * - State transactions
 */
@QuarkusTest
public class OrderResourceTest {

    // ==================== CREATE ORDER TESTS ====================

    @Test
    public void createOrder_withValidData_returns201Created() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            2,
            new BigDecimal("19.99")
        );

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("status", equalTo("PENDING"))
            .body("quantity", equalTo(2))
            .body("priceSnapshot", hasToString(containsString("19.99")))
            .body("id", notNullValue())
            .body("createdAt", notNullValue());
    }

    @Test
    public void createOrder_withNullBookId_returns400BadRequest() {
        CreateOrderRequest request = new CreateOrderRequest(
            null,
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400)
            .body("errorCode", notNullValue());
    }

    @Test
    public void createOrder_withNullUserId_returns400BadRequest() {
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            null,
            1,
            BigDecimal.TEN
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400);
    }

    @Test
    public void createOrder_withZeroQuantity_returns400BadRequest() {
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            0,
            BigDecimal.TEN
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400);
    }

    @Test
    public void createOrder_withZeroPrice_returns400BadRequest() {
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.ZERO
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(400);
    }

    // ==================== GET ORDER TESTS ====================

    @Test
    public void getOrder_withNonExistentId_returns404NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
        .when()
            .get("/api/orders/{id}", nonExistentId)
        .then()
            .statusCode(404)
            .body("errorCode", notNullValue());
    }

    @Test
    public void getOrder_withInvalidUUID_returns400BadRequest() {
        given()
        .when()
            .get("/api/orders/{id}", "not-a-uuid")
        .then()
            .statusCode(400);
    }

    // ==================== CONFIRM ORDER TESTS ====================

    @Test
    public void confirmOrder_withPendingOrder_returns200OK() {
        // Create order first
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        String orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Confirm the order
        given()
        .when()
            .post("/api/orders/{id}/confirm", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));
    }

    @Test
    public void confirmOrder_withNonExistentId_returns404NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
        .when()
            .post("/api/orders/{id}/confirm", nonExistentId)
        .then()
            .statusCode(404);
    }

    // ==================== CANCEL ORDER TESTS ====================

    @Test
    public void cancelOrder_withPendingOrder_returns200OK() {
        // Create order first
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        String orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Cancel the order
        given()
        .when()
            .post("/api/orders/{id}/cancel", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELLED"));
    }

    @Test
    public void cancelOrder_withNonExistentId_returns404NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
        .when()
            .post("/api/orders/{id}/cancel", nonExistentId)
        .then()
            .statusCode(404);
    }

    // ==================== FULFILL ORDER TESTS ====================

    @Test
    public void fulfillOrder_withConfirmedOrder_returns200OK() {
        // Create order first
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        String orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Confirm first
        given()
        .when()
            .post("/api/orders/{id}/confirm", orderId)
        .then()
            .statusCode(200);

        // Fulfill the order
        given()
        .when()
            .post("/api/orders/{id}/fulfill", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("FULFILLED"));
    }

    @Test
    public void fulfillOrder_withPendingOrder_returns409Conflict() {
        // Create order first
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        String orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Try to fulfill without confirming
        given()
        .when()
            .post("/api/orders/{id}/fulfill", orderId)
        .then()
            .statusCode(409);
    }

    @Test
    public void fulfillOrder_withNonExistentId_returns404NotFound() {
        UUID nonExistentId = UUID.randomUUID();

        given()
        .when()
            .post("/api/orders/{id}/fulfill", nonExistentId)
        .then()
            .statusCode(404);
    }

    // ==================== INTEGRATION WORKFLOW TESTS ====================

    @Test
    public void completeOrderWorkflow_create_confirm_fulfill() {
        // Create order
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            BigDecimal.TEN
        );

        String orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Confirm
        given()
        .when()
            .post("/api/orders/{id}/confirm", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));

        // Fulfill
        given()
        .when()
            .post("/api/orders/{id}/fulfill", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("FULFILLED"));

        // Verify final state
        given()
        .when()
            .get("/api/orders/{id}", orderId)
        .then()
            .statusCode(200)
            .body("status", equalTo("FULFILLED"));
    }
}
