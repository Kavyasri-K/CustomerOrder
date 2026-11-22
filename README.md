# Customer Order Processing Service

A RESTful microservice built with **Spring Boot 4**, **Java 21**, **Spring Data JPA**, and **H2 In-Memory Database** to manage customer orders and order statuses.

---

## 🛠️ Technology Stack
* **Java**: 21 (Supports Java 17+)
* **Framework**: Spring Boot 4
* **Database**: In-memory H2 Database
* **ORM**: Spring Data JPA / Hibernate
* **Validation**: Jakarta Bean Validation (Hibernate Validator)
* **Testing**: JUnit 5, AssertJ, Mockito, MockMvc
* **Build Tool**: Apache Maven (Wrapper included)

---

## 📁 Project Structure

```
order
├── src
│   ├── main
│   │   ├── java/com/customer/orderservice
│   │   │   ├── controller
│   │   │   │   └── OrderController.java         # REST endpoints (/orders)
│   │   │   ├── dto
│   │   │   │   ├── CreateOrderRequest.java      # Input DTO with validation rules
│   │   │   │   ├── UpdateOrderStatusRequest.java# Status update payload
│   │   │   │   ├── OrderResponse.java           # Standard API response DTO
│   │   │   │   └── ErrorResponse.java           # Structured error details
│   │   │   ├── exception
│   │   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice for validation & errors
│   │   │   │   ├── OrderNotFoundException.java  # 404 Not Found exception
│   │   │   │   └── InvalidOrderStatusException.java # 400 Bad Request exception
│   │   │   ├── model
│   │   │   │   ├── Order.java                   # JPA entity (table: customer_orders)
│   │   │   │   └── OrderStatus.java             # Enum: CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
│   │   │   ├── repository
│   │   │   │   └── OrderRepository.java         # Spring Data JPA repository
│   │   │   ├── service
│   │   │   │   └── OrderService.java            # Business logic and transactions
│   │   │   └── OrderApplication.java            # Spring Boot entry point
│   │   └── resources
│   │       └── application.properties           # H2 & server configuration
│   └── test
│       └── java/com/customer/orderservice
│           ├── integration
│           │   └── OrderIntegrationTest.java    # Full HTTP API integration tests
│           ├── service
│           │   └── OrderServiceTest.java        # Unit tests with Mockito
│           ├── validation
│           │   └── OrderValidationTest.java     # Unit tests for bean validation
│           └── OrderApplicationTests.java       # Context load test
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

### 1. Build and Run the Service
Run with Maven wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```
Or run directly via standard Maven:
```powershell
mvn spring-boot:run
```
The server will start at: `http://localhost:8080`

### 2. Run All Tests
```powershell
.\mvnw.cmd clean test
```
Or:
```powershell
mvn clean test
```
This executes:
* **7 Service Unit Tests** (business logic & mock repository)
* **4 Validation Unit Tests** (field constraints & edge cases)
* **8 Integration Tests** (end-to-end REST API verification)
* **1 Application Context Test**

### 3. Access H2 In-Memory Database Console
* **URL**: `http://localhost:8080/h2-console`
* **JDBC URL**: `jdbc:h2:mem:orderdb`
* **User Name**: `sa`
* **Password**: *(leave blank)*

---

## 📡 REST API Documentation

### 1. Create an Order
* **Method**: `POST`
* **Endpoint**: `/orders`
* **Headers**: `Content-Type: application/json`
* **Request Body**:
  ```json
  {
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200
  }
  ```
* **Success Response (201 Created)**:
  ```json
  {
    "id": 1,
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200.0,
    "status": "CREATED",
    "createdAt": "2026-11-22T12:19:28.123456"
  }
  ```

---

### 2. Retrieve an Order by ID
* **Method**: `GET`
* **Endpoint**: `/orders/{id}`
* **Example**: `/orders/1`
* **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200.0,
    "status": "CREATED",
    "createdAt": "2026-11-22T12:19:28.123456"
  }
  ```
* **Error Response (404 Not Found)**:
  ```json
  {
    "timestamp": "2026-11-22T12:20:00.123456",
    "status": 404,
    "error": "Not Found",
    "message": "Order not found with id: 999"
  }
  ```

---

### 3. Update Order Status
* **Method**: `PUT`
* **Endpoint**: `/orders/{id}/status`
* **Example**: `/orders/1/status`
* **Headers**: `Content-Type: application/json`
* **Request Body**:
  ```json
  {
    "status": "SHIPPED"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "id": 1,
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200.0,
    "status": "SHIPPED",
    "createdAt": "2026-11-22T12:19:28.123456"
  }
  ```
* **Allowed Statuses**: `CREATED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

---

### 4. Retrieve All Orders
* **Method**: `GET`
* **Endpoint**: `/orders`
* **Success Response (200 OK)**:
  ```json
  [
    {
      "id": 1,
      "customerId": "12345",
      "product": "Laptop",
      "quantity": 1,
      "price": 1200.0,
      "status": "SHIPPED",
      "createdAt": "2026-11-22T12:19:28.123456"
    }
  ]
  ```

---

## 🛡️ Input Validation Rules

The service validates incoming payloads and returns HTTP `400 Bad Request` with field-level details:

| Field | Rule | Error Message |
| :--- | :--- | :--- |
| `customerId` | `@NotBlank` | `Customer ID is required and cannot be blank` |
| `product` | `@NotBlank` | `Product is required and cannot be blank` |
| `quantity` | `@NotNull`, `@Min(1)` | `Quantity must be greater than 0` |
| `price` | `@NotNull`, `@DecimalMin("0.01")` | `Price must be greater than 0` |
| `status` | Valid enum | `Invalid status: '...'. Allowed values: [CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED]` |

### Example 400 Validation Error Response:
```json
{
  "timestamp": "2026-11-22T12:20:15.654321",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request payload contains invalid data",
  "details": [
    "customerId: Customer ID is required and cannot be blank",
    "quantity: Quantity must be greater than 0",
    "price: Price must be greater than 0"
  ]
}
```
