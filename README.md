# Customer Order Processing Service

A RESTful microservice built with **Spring Boot 3.5**, **Java 21**, **Spring Data JPA**, and **H2 In-Memory Database** to manage customer orders and order statuses.

---

## 📋 Table of Contents
1. [Overview & Tech Stack](#-overview--tech-stack)
2. [Project Architecture & Directory Structure](#-project-architecture--directory-structure)
3. [Prerequisites](#-prerequisites)
4. [Build Instructions](#-build-instructions)
5. [Run Instructions](#-run-instructions)
6. [H2 Database Console](#-h2-database-console)
7. [API Examples (cURL & PowerShell)](#-api-examples)
   - [1. Create Order](#1-create-order-post-orders)
   - [2. Retrieve Order by ID](#2-retrieve-order-by-id-get-ordersid)
   - [3. Update Order Status](#3-update-order-status-put-ordersidstatus)
   - [4. View All Orders](#4-view-all-orders-get-orders)
   - [5. Validation & Error Handling Scenarios](#5-validation--error-handling-scenarios)
8. [Testing](#-testing)
9. [Assumptions Made](#-assumptions-made)

---

## 🛠️ Overview & Tech Stack

- **Language**: Java 21 (Compatible with Java 17+)
- **Framework**: Spring Boot 3.5.6
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: H2 In-Memory Database
- **DTOs**: Java records
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Min`, `@DecimalMin`, `@NotNull`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test (`MockMvc`), AssertJ
- **Build Tool**: Apache Maven (Maven Wrapper `mvnw` and `mvnw.cmd` included)

---

## 📁 Project Architecture & Directory Structure

```
order
├── src
│   ├── main
│   │   ├── java/com/customer/orderservice
│   │   │   ├── controller
│   │   │   │   └── OrderController.java              # REST endpoints (/orders)
│   │   │   ├── dto
│   │   │   │   ├── CreateOrderRequest.java           # Record: request payload with validations
│   │   │   │   ├── UpdateOrderStatusRequest.java     # Record: status update payload
│   │   │   │   ├── OrderResponse.java                # Record: output response DTO
│   │   │   │   └── ErrorResponse.java                # Record: structured API error DTO
│   │   │   ├── exception
│   │   │   │   ├── GlobalExceptionHandler.java       # Centralized @RestControllerAdvice
│   │   │   │   ├── OrderNotFoundException.java       # 404 Not Found
│   │   │   │   ├── InvalidOrderStatusException.java  # 400 Bad Request
│   │   │   │   └── InvalidStatusTransitionException.java # 409 Conflict
│   │   │   ├── model
│   │   │   │   ├── Order.java                        # JPA Entity (table: customer_orders)
│   │   │   │   └── OrderStatus.java                  # Enum + allowed status transitions
│   │   │   ├── repository
│   │   │   │   └── OrderRepository.java              # Spring Data JPA Repository
│   │   │   ├── service
│   │   │   │   └── OrderService.java                 # Business logic and transactions
│   │   │   └── OrderApplication.java                 # Spring Boot main class
│   │   └── resources
│   │       └── application.properties                # Database, server & H2 console settings
│   └── test
│       └── java/com/customer/orderservice
│           ├── integration
│           │   └── OrderIntegrationTest.java         # End-to-end MockMvc integration tests
│           ├── model
│           │   └── OrderStatusTest.java              # Status transition rule tests
│           ├── service
│           │   └── OrderServiceTest.java             # Unit tests for Service layer with Mockito
│           ├── validation
│           │   └── OrderValidationTest.java          # Unit tests for Request Bean Validations
│           └── OrderApplicationTests.java            # Spring Context load test
├── postman
│   └── Customer_Order_Processing_Service.postman_collection.json  # Import into Postman
├── pom.xml                                           # Maven dependencies and build configuration
└── README.md                                         # Project documentation
```

---

## ⚙️ Prerequisites

- **JDK 17 or higher** (JDK 21 recommended)
  - Verify with: `java -version`
- **Maven 3.8+** (Optional, project includes Maven Wrapper `./mvnw` and `.\mvnw.cmd`)

---

## 🔨 Build Instructions

Navigate to the project root directory where `pom.xml` is located:

```bash
cd order
```

### 1. Clean and Compile
Using Maven Wrapper on Windows:
```powershell
.\mvnw.cmd clean compile
```
Using Linux / macOS / Bash:
```bash
./mvnw clean compile
```

### 2. Package into Executable JAR
```powershell
# Windows
.\mvnw.cmd clean package

# Linux / macOS
./mvnw clean package
```
*The compiled JAR file will be generated in `target/orderservice-0.0.1-SNAPSHOT.jar`.*

### 3. Skip Tests During Packaging (Optional)
```powershell
.\mvnw.cmd clean package -DskipTests
```

---

## 🚀 Run Instructions

### Option 1: Run via Maven Wrapper (Recommended for Development)
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Option 2: Run using Standard Maven
```bash
mvn spring-boot:run
```

### Option 3: Run the Packaged JAR
```bash
java -jar target/orderservice-0.0.1-SNAPSHOT.jar
```

Once started, the application will be listening at:
```
http://localhost:8080
```

---

## 🗄️ H2 Database Console

The H2 database web console is enabled for easy verification of persisted records.

- **Console URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:orderdb`
- **Driver Class**: `org.h2.Driver`
- **User Name**: `sa`
- **Password**: *(leave empty)*

Sample SQL queries in H2 Console:
```sql
SELECT * FROM customer_orders;
```

---

## 📡 API Examples

### 1. Create Order (`POST /orders`)
Creates a new order with initial status `CREATED`.

#### cURL:
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200
  }'
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/orders" `
  -ContentType "application/json" `
  -Body '{"customerId":"12345","product":"Laptop","quantity":1,"price":1200}'
```

#### Response: `201 Created`
```json
{
  "id": 1,
  "customerId": "12345",
  "product": "Laptop",
  "quantity": 1,
  "price": 1200.00,
  "status": "CREATED",
  "createdAt": "2026-11-22T20:15:30.123456"
}
```

---

### 2. Retrieve Order by ID (`GET /orders/{id}`)
Fetches order details by its generated ID.

#### cURL:
```bash
curl -X GET http://localhost:8080/orders/1
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/orders/1"
```

#### Response: `200 OK`
```json
{
  "id": 1,
  "customerId": "12345",
  "product": "Laptop",
  "quantity": 1,
  "price": 1200.00,
  "status": "CREATED",
  "createdAt": "2026-11-22T20:15:30.123456"
}
```

---

### 3. Update Order Status (`PUT /orders/{id}/status`)
Moves an order to the next status. Only valid transitions are accepted.

| From | Allowed next status |
| :--- | :--- |
| `CREATED` | `PROCESSING`, `CANCELLED` |
| `PROCESSING` | `SHIPPED`, `CANCELLED` |
| `SHIPPED` | `DELIVERED` |
| `DELIVERED`, `CANCELLED` | *(none, terminal states)* |

#### cURL:
```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PROCESSING"
  }'
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/orders/1/status" `
  -ContentType "application/json" `
  -Body '{"status":"PROCESSING"}'
```

#### Response: `200 OK`
```json
{
  "id": 1,
  "customerId": "12345",
  "product": "Laptop",
  "quantity": 1,
  "price": 1200.00,
  "status": "PROCESSING",
  "createdAt": "2026-11-22T20:15:30.123456"
}
```

Errors: `400` for an unknown status, `404` for an unknown order, `409` for a transition that is not allowed (see Scenario D).

---

### 4. View Orders (`GET /orders`)
Retrieves orders **page by page**, optionally filtered by customer.

| Query parameter | Default | Description |
| :--- | :--- | :--- |
| `customerId` | *(none)* | Only return orders of this customer |
| `page` | `0` | Zero-based page index |
| `size` | `20` | Page size |
| `sort` | `id,desc` | Sort field and direction, e.g. `createdAt,asc` |

#### cURL:
```bash
curl -X GET "http://localhost:8080/orders"
curl -X GET "http://localhost:8080/orders?customerId=12345&page=0&size=10"
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/orders?customerId=12345&page=0&size=10"
```

#### Response: `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "customerId": "12345",
      "product": "Laptop",
      "quantity": 1,
      "price": 1200.00,
      "status": "PROCESSING",
      "createdAt": "2026-11-22T20:15:30.123456"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 5. Validation & Error Handling Scenarios

Input rules (violations return `400 Bad Request` with field-level `details`):

| Field | Rule | Error Message |
| :--- | :--- | :--- |
| `customerId` | `@NotBlank` | `Customer ID is required and cannot be blank` |
| `product` | `@NotBlank` | `Product is required and cannot be blank` |
| `quantity` | `@NotNull`, `@Min(1)` | `Quantity must be greater than 0` |
| `price` | `@NotNull`, `@DecimalMin("0.01")` | `Price must be greater than 0` |
| `status` | Valid enum value | `Invalid status: '...'. Allowed values: [CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED]` |

#### Scenario A: Missing Customer ID, Invalid Quantity & Price
**Request**:
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "",
    "product": "Mouse",
    "quantity": 0,
    "price": -15
  }'
```
**Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-11-22T20:20:15.654321",
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

#### Scenario B: Order Not Found
**Request**:
```bash
curl -X GET http://localhost:8080/orders/999
```
**Response**: `404 Not Found`
```json
{
  "timestamp": "2026-11-22T20:20:30.112233",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: 999"
}
```

#### Scenario C: Invalid Order Status
**Request**:
```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DISPATCHED"
  }'
```
**Response**: `400 Bad Request`
```json
{
  "timestamp": "2026-11-22T20:21:00.998877",
  "status": 400,
  "error": "Invalid Status",
  "message": "Invalid status: 'DISPATCHED'. Allowed values: [CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED]"
}
```

#### Scenario D: Status Transition Not Allowed
**Request** (the order is already `DELIVERED`):
```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CREATED"
  }'
```
**Response**: `409 Conflict`
```json
{
  "timestamp": "2026-11-22T20:22:00.445566",
  "status": 409,
  "error": "Invalid Status Transition",
  "message": "Cannot change order status from DELIVERED to CREATED"
}
```

---

## 🧪 Testing

The project has automated unit and integration tests (31 in total).

### Run All Tests
```powershell
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### Test Suites Included:
1. **Service layer (`OrderServiceTest.java`)**, 12 tests with a mocked repository:
   - `testCreateOrder`, `testCreateOrder_PriceScale` (price rounded to 2 decimals)
   - `testGetOrderById_Success`, `testGetOrderById_NotFound`
   - `testGetOrders_All`, `testGetOrders_ByCustomer`, `testGetOrders_BlankCustomerId`
   - `testUpdateOrderStatus_Success`, `testUpdateOrderStatus_InvalidTransition`, `testUpdateOrderStatus_FromTerminalState`, `testUpdateOrderStatus_InvalidStatus`, `testUpdateOrderStatus_NotFound`

2. **Status rules (`OrderStatusTest.java`)**, 3 tests: allowed transitions, rejected transitions, terminal states.

3. **Validation (`OrderValidationTest.java`)**, 4 tests: blank `customerId`, `quantity <= 0`, `price <= 0`, valid payload.

4. **Integration (`OrderIntegrationTest.java`)**, 11 tests with `MockMvc`:
   - `POST /orders` (success + validation failure)
   - `GET /orders/{id}` (found + 404)
   - `PUT /orders/{id}/status` (valid, 404, invalid status, illegal transition 409)
   - `GET /orders` (list, filter by `customerId`, pagination)

5. **Context load (`OrderApplicationTests.java`)**, 1 test.

### Postman
Import `postman/Customer_Order_Processing_Service.postman_collection.json` into Postman. It uses a `baseUrl` variable (default `http://localhost:8080`) and has requests for every endpoint, including the error cases.

---

## 📌 Assumptions Made

1. **Order Status Lifecycle**:
   - Default status upon creation is `CREATED`.
   - Allowed statuses: `CREATED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`.
   - Only these transitions are allowed: `CREATED -> PROCESSING -> SHIPPED -> DELIVERED`, and `CREATED`/`PROCESSING -> CANCELLED`. `DELIVERED` and `CANCELLED` are final. Anything else returns `409 Conflict`.
   - Status updates are case-insensitive (e.g., `"processing"` is converted to `PROCESSING`).

2. **Customer Model**:
   - `customerId` is a String identifier supplied by an external customer/auth service. The service does **not** check that the customer exists, and no Customer table is included.

3. **Data Types & Precision**:
   - Order IDs are auto-generated (`Long`, `GenerationType.IDENTITY`).
   - `price` is a `BigDecimal` stored as `DECIMAL(19,2)` and rounded to 2 decimal places, with `@DecimalMin("0.01")` ensuring it is positive.
   - `quantity` is an `Integer` with `@Min(1)`.

4. **Listing**:
   - `GET /orders` is paginated (default page size 20, newest first) and can be filtered by `customerId`.

5. **Persistence & Scope**:
   - An in-memory H2 database (`jdbc:h2:mem:orderdb`) is used, so data lasts only while the application runs.
   - `createdAt` is populated automatically via `@PrePersist`.

6. **Error Format**:
   - Error responses share one structure (`timestamp`, `status`, `error`, `message`, and an optional `details` array for field validation errors).
