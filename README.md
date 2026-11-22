# Customer Order Processing Service

A robust RESTful microservice built with **Spring Boot**, **Java 21** (compatible with Java 17+), **Spring Data JPA**, and an **H2 In-Memory Database** to process and manage customer orders.

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
- **Framework**: Spring Boot 4
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: H2 In-Memory Database
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
│   │   │   │   ├── CreateOrderRequest.java           # Request payload with validations
│   │   │   │   ├── UpdateOrderStatusRequest.java     # Status update payload
│   │   │   │   ├── OrderResponse.java                # Output response DTO
│   │   │   │   └── ErrorResponse.java                # Structured API error DTO
│   │   │   ├── exception
│   │   │   │   ├── GlobalExceptionHandler.java       # Centralized @RestControllerAdvice
│   │   │   │   ├── OrderNotFoundException.java       # 404 Not Found exception
│   │   │   │   └── InvalidOrderStatusException.java  # 400 Bad Request exception
│   │   │   ├── model
│   │   │   │   ├── Order.java                        # JPA Entity (table: customer_orders)
│   │   │   │   └── OrderStatus.java                  # Enum: CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
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
│           ├── service
│           │   └── OrderServiceTest.java             # Unit tests for Service layer with Mockito
│           ├── validation
│           │   └── OrderValidationTest.java          # Unit tests for Request Bean Validations
│           └── OrderApplicationTests.java            # Spring Context load test
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
  "price": 1200.0,
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
  "price": 1200.0,
  "status": "CREATED",
  "createdAt": "2026-11-22T20:15:30.123456"
}
```

---

### 3. Update Order Status (`PUT /orders/{id}/status`)
Updates the status of an existing order.

#### cURL:
```bash
curl -X PUT http://localhost:8080/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SHIPPED"
  }'
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/orders/1/status" `
  -ContentType "application/json" `
  -Body '{"status":"SHIPPED"}'
```

#### Response: `200 OK`
```json
{
  "id": 1,
  "customerId": "12345",
  "product": "Laptop",
  "quantity": 1,
  "price": 1200.0,
  "status": "SHIPPED",
  "createdAt": "2026-11-22T20:15:30.123456"
}
```

---

### 4. View All Orders (`GET /orders`)
Retrieves a list of all stored orders.

#### cURL:
```bash
curl -X GET http://localhost:8080/orders
```

#### PowerShell:
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/orders"
```

#### Response: `200 OK`
```json
[
  {
    "id": 1,
    "customerId": "12345",
    "product": "Laptop",
    "quantity": 1,
    "price": 1200.0,
    "status": "SHIPPED",
    "createdAt": "2026-11-22T20:15:30.123456"
  }
]
```

---

### 5. Validation & Error Handling Scenarios

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

---

## 🧪 Testing

The project has comprehensive automated test coverage spanning unit and integration tests.

### Run All Tests
```powershell
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### Test Suites Included:
1. **Unit Tests - Service Layer (`OrderServiceTest.java`)**:
   - `createOrder_Success` - Verifies entity mapping, saving, and DTO conversion.
   - `getOrderById_Success` - Verifies order retrieval.
   - `getOrderById_NotFound` - Verifies `OrderNotFoundException` is thrown when ID does not exist.
   - `getAllOrders_Success` - Verifies listing of all orders.
   - `updateOrderStatus_Success` - Verifies status update logic.
   - `updateOrderStatus_InvalidStatus` - Verifies rejection of unknown statuses.
   - `updateOrderStatus_NotFound` - Verifies updating non-existent order.

2. **Unit Tests - Validation Logic (`OrderValidationTest.java`)**:
   - Tests blank/null `customerId`.
   - Tests `quantity <= 0`.
   - Tests `price <= 0`.
   - Tests valid payload acceptance.

3. **Integration Tests (`OrderIntegrationTest.java`)**:
   - Tests `POST /orders` (successful creation + validation rejection).
   - Tests `GET /orders/{id}` (existing order + 404 not found).
   - Tests `PUT /orders/{id}/status` (valid update + invalid status handling).
   - Tests `GET /orders` (retrieval of all persisted orders).

---

## 📌 Assumptions Made

1. **Order Status Lifecycle**:
   - Default status upon creation is `CREATED`.
   - Allowed statuses: `CREATED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, and `CANCELLED`.
   - Status updates are case-insensitive (e.g., `"shipped"` is automatically converted to `SHIPPED`).

2. **Customer Model**:
   - For this service, `customerId` is provided as a unique String identifier from an external customer/auth service. A separate Customer table was not required by the specification.

3. **Data Types & Precision**:
   - Order IDs are auto-generated numerical identifiers (`Long` with `GenerationType.IDENTITY`).
   - `price` is modeled as `Double` with `@DecimalMin("0.01")` validation ensuring it is strictly positive (`> 0`).
   - `quantity` is modeled as `Integer` with `@Min(1)` validation ensuring it is strictly positive (`> 0`).

4. **Persistence & Scope**:
   - An in-memory H2 database (`jdbc:h2:mem:orderdb`) is utilized as required, keeping the application lightweight and self-contained without requiring external database installation.
   - Data persists for the duration of the application runtime.
   - `createdAt` timestamp is automatically populated via `@PrePersist` when the order is saved.

5. **Error Format**:
   - Error responses adhere to a consistent structure (`timestamp`, `status`, `error`, `message`, and optional `details` array for field validation errors).
