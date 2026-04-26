# CouchBaseDBSpringBoot

A cloud-ready Spring Boot REST API for product management backed by **Apache CouchDB**. Documents are stored as JSON in CouchDB and queried via its Mango query API over HTTP. Includes full Swagger UI documentation and Spring Actuator monitoring.

---

## Tech Stack

| Component         | Technology                           |
|-------------------|--------------------------------------|
| Language          | Java 21                              |
| Framework         | Spring Boot 3.3.4                    |
| Database          | Apache CouchDB 3.3.3 (Docker)        |
| DB Access         | CouchDB REST HTTP API + RestTemplate |
| Query Language    | CouchDB Mango (JSON)                 |
| Validation        | Spring Boot Starter Validation       |
| API Documentation | SpringDoc OpenAPI (Swagger UI) 2.6.0 |
| Monitoring        | Spring Boot Actuator                 |
| Containerisation  | Docker / docker-compose              |
| Build Tool        | Maven                                |

---

## Project Structure

```
CloudBaseDBSpringBoot/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/cloudbasedb/
    │   │   ├── CloudBaseDBApplication.java       # Entry point
    │   │   ├── config/
    │   │   │   ├── OpenApiConfig.java            # Swagger + CouchDB RestTemplate/URL beans
    │   │   │   └── DataInitializer.java          # Creates Mango indexes + seeds 5 products
    │   │   ├── controller/
    │   │   │   └── ProductController.java        # REST endpoints
    │   │   ├── dto/
    │   │   │   ├── ProductRequest.java           # Incoming request payload
    │   │   │   └── ProductResponse.java          # Outgoing response payload
    │   │   ├── entity/
    │   │   │   └── Product.java                  # CouchDB document POJO (_id, _rev)
    │   │   ├── exception/
    │   │   │   ├── ProductNotFoundException.java # Custom 404 exception
    │   │   │   └── GlobalExceptionHandler.java   # Centralised error handling
    │   │   ├── repository/
    │   │   │   ├── ProductRepository.java        # Repository interface (String IDs)
    │   │   │   └── CouchDbProductRepository.java # CouchDB HTTP implementation
    │   │   └── service/
    │   │       ├── ProductService.java           # Service interface
    │   │       └── ProductServiceImpl.java       # Service implementation
    │   └── resources/
    │       └── application.yml                   # App + CouchDB connection configuration
    └── test/
        └── java/com/cloudbasedb/
            └── CloudBaseDBApplicationTests.java
```

---

## Data Model

**Product** — stored as a CouchDB JSON document in the `products` database

| Field         | Type    | Notes                                          |
|---------------|---------|------------------------------------------------|
| `_id`         | String  | CouchDB auto-assigned UUID (mapped to `id`)    |
| `_rev`        | String  | CouchDB revision token (required for updates)  |
| `productCode` | String  | Application-level unique code (Mango indexed)  |
| `name`        | String  | Not null (Mango indexed)                       |
| `category`    | String  | Optional (Mango indexed)                       |
| `price`       | Double  | Not null, must be > 0                          |
| `stock`       | Integer | Optional, must be >= 0                         |
| `description` | String  | Optional                                       |

> `_rev` is never exposed in API responses — it is managed internally for CouchDB MVCC.

---

## API Endpoints

Base URL: `http://localhost:8096/api/products`

> Product IDs are CouchDB-generated UUIDs (e.g. `04587d5b8c73386a985024bbfe000213`), not integers.

| Method   | Endpoint                  | Description                                      | Status Codes       |
|----------|---------------------------|--------------------------------------------------|--------------------|
| `POST`   | `/`                       | Create a new product                             | 201, 400, 409      |
| `GET`    | `/{id}`                   | Get product by numeric ID                        | 200, 404           |
| `GET`    | `/`                       | Get all products                                 | 200                |
| `GET`    | `/search?name=`           | Search products by name (case-insensitive)       | 200                |
| `GET`    | `/category/{category}`    | Filter products by category (case-insensitive)   | 200                |
| `PUT`    | `/{id}`                   | Update product by numeric ID                     | 200, 400, 404, 409 |
| `DELETE` | `/{id}`                   | Delete product by numeric ID                     | 204, 404           |

### Request Body (POST / PUT)

```json
{
  "productCode": "PROD-001",
  "name": "Laptop Pro 15",
  "category": "Electronics",
  "price": 1299.99,
  "stock": 50,
  "description": "High-performance laptop with 16GB RAM and 512GB SSD"
}
```

### Response Body

```json
{
  "id": "04587d5b8c73386a985024bbfe000213",
  "productCode": "PROD-001",
  "name": "Laptop Pro 15",
  "category": "Electronics",
  "price": 1299.99,
  "stock": 50,
  "description": "High-performance laptop with 16GB RAM and 512GB SSD"
}
```

### Error Response

```json
{
  "timestamp": "2026-04-20T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99"
}
```

---

## Configuration

### CouchDB connection (`application.yml`)

| Property             | Default         | Override via env var      |
|----------------------|-----------------|---------------------------|
| `couchdb.protocol`   | `http`          | `COUCHDB_PROTOCOL`        |
| `couchdb.host`       | `localhost`     | `COUCHDB_HOST`            |
| `couchdb.port`       | `5984`          | `COUCHDB_PORT`            |
| `couchdb.username`   | `admin`         | `COUCHDB_USERNAME`        |
| `couchdb.password`   | `admin123`      | `COUCHDB_PASSWORD`        |
| `couchdb.database`   | `products`      | `COUCHDB_DATABASE`        |

> **Production:** Override credentials via environment variables — never commit passwords to source control.

### Other settings

| Property             | Value                       |
|----------------------|-----------------------------|
| Server port          | `8096`                      |
| Swagger UI path      | `/swagger-ui.html`          |
| OpenAPI docs path    | `/api-docs`                 |
| Actuator endpoints   | `health`, `info`, `metrics` |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker + Docker Compose

### 1. Start CouchDB

```bash
cd CloudBaseDBSpringBoot
docker-compose up -d
```

This starts CouchDB 3.3.3 on port `5984` and runs a one-shot initialiser that creates the `products` database and system databases.

### 2. Run the Spring Boot app

```bash
mvn spring-boot:run
```

On first startup, `DataInitializer` automatically:
- Creates Mango indexes on `productCode`, `name`, and `category`
- Seeds 5 sample products into CouchDB

### 3. Build and run as a JAR

```bash
mvn clean package -DskipTests
java -jar target/cloud-base-db-spring-boot-1.0.0.jar
```

### Connect to a remote CouchDB (production)

```bash
export COUCHDB_HOST=my-cloud-couchdb.example.com
export COUCHDB_USERNAME=admin
export COUCHDB_PASSWORD=strongpassword
export COUCHDB_DATABASE=products
java -jar target/cloud-base-db-spring-boot-1.0.0.jar
```

---

## Useful URLs

| Resource              | URL                                           |
|-----------------------|-----------------------------------------------|
| Swagger UI            | http://localhost:8096/swagger-ui.html         |
| OpenAPI JSON          | http://localhost:8096/api-docs                |
| Health Check          | http://localhost:8096/actuator/health         |
| Metrics               | http://localhost:8096/actuator/metrics        |
| CouchDB Fauxton UI    | http://localhost:5984/_utils                  |
| CouchDB products DB   | http://localhost:5984/products                |
| CouchDB health        | http://localhost:5984/_up                     |

> **CouchDB Fauxton credentials:** Username `admin`, Password `admin123`

---

## Seed Data

On first startup, `DataInitializer.java` checks if the `products` database is empty and seeds the following documents. Subsequent restarts skip seeding.

| productCode | Category    | Name                | Price    | Stock |
|-------------|-------------|---------------------|----------|-------|
| PROD-001    | Electronics | Laptop Pro 15       | $1299.99 | 50    |
| PROD-002    | Accessories | Wireless Mouse      | $29.99   | 200   |
| PROD-003    | Accessories | Mechanical Keyboard | $89.99   | 150   |
| PROD-004    | Electronics | 4K Monitor          | $499.99  | 75    |
| PROD-005    | Accessories | USB-C Hub           | $49.99   | 300   |

## CouchDB Architecture

### How documents are stored

Each product is a JSON document in the `products` database. CouchDB auto-assigns a UUID as `_id` and tracks changes via `_rev` (revision token). The `_rev` is required for updates and deletes (MVCC — no lost updates).

```
CouchDB document example:
{
  "_id": "04587d5b8c73386a985024bbfe000213",
  "_rev": "1-abc123...",
  "productCode": "PROD-001",
  "name": "Laptop Pro 15",
  "category": "Electronics",
  "price": 1299.99,
  "stock": 50,
  "description": "High-performance laptop..."
}
```

### Mango indexes

Created automatically on startup via `POST /products/_index`:

| Index name         | Field         | Used by                          |
|--------------------|---------------|----------------------------------|
| `productCode-index`| `productCode` | create (duplicate check), search |
| `name-index`       | `name`        | `GET /search?name=`              |
| `category-index`   | `category`    | `GET /category/{category}`       |

### Docker Compose services

| Service             | Image                   | Purpose                                    |
|---------------------|-------------------------|--------------------------------------------|
| `couchdb`           | `couchdb:3.3.3`         | CouchDB on port 5984 with persistent volumes|
| `couchdb-init`      | `curlimages/curl:8.7.1` | One-shot: creates system DBs + products DB |

---

## Error Handling

| Scenario                  | HTTP Status | Description                           |
|---------------------------|-------------|---------------------------------------|
| Product not found         | `404`       | `ProductNotFoundException` thrown     |
| Duplicate `productCode`   | `409`       | `IllegalArgumentException` thrown     |
| Validation failure        | `400`       | Bean Validation constraint violation  |
| Unexpected error          | `500`       | Generic fallback handler              |
