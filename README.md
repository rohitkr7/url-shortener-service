# 🔗 High-Throughput URL Shortener & Analytics Service

A production-grade, distributed URL shortening and real-time click tracking engine engineered with **Java 17/21**, **Spring Boot**, **PostgreSQL**, and an in-memory **Caffeine** cache.

---

## 🚀 Key Features & Architectural Highlights

- **Bijective Base62 Encoding**: Converts database sequence IDs (starting at 1,000,000) to compact alphanumeric short codes (`[0-9a-zA-Z]`), eliminating hash collisions and guaranteeing deterministic mappings.
- **Sub-Millisecond Read Latency**: In-memory caching with **Caffeine** absorbs high-read traffic for redirect lookups, protecting the primary database.
- **Non-Blocking Asynchronous Click Tracking**: Asynchronous event ingestion (`@Async`) offloads telemetry logging from the critical redirect path, guaranteeing that analytics persistence never degrades redirect latency.
- **Privacy-First (GDPR-Compliant) Analytics**: Hashes client IP addresses using SHA-256 before persistence to prevent storing Personally Identifiable Information (PII).
- **Automated Schema Evolution**: Database migrations are strictly managed using **Flyway** with verified indexing on lookup and analytics columns.
- **Robust Validation & Error Handling**: Comprehensive protocol validation and RFC-compliant structured error responses.

---

## 🏗️ Architecture & Data Flow

```
                                  Client Request
                                        │
                 ┌──────────────────────┴──────────────────────┐
                 │                                             │
          POST /api/v1/urls                             GET /{shortCode}
                 │                                             │
      ┌──────────▼──────────┐                       ┌──────────▼──────────┐
      │  Payload Validation │                       │ Header & IP Capture │
      └──────────┬──────────┘                       └──────────┬──────────┘
                 │                                             │
      ┌──────────▼──────────┐                       ┌──────────┴──────────┐
      │ PostgreSQL Sequence │                       │                     │
      │   + Base62 Encode   │                (Async Event)          (Sync Lookup)
      └──────────┬──────────┘                       │                     │
                 │                          ┌───────▼────────┐    ┌───────▼────────┐
                 │                          │AnalyticsService│    │ Caffeine Cache │
                 │                          │(SHA-256 Hash)  │    └───────┬────────┘
                 │                          └───────┬────────┘            │ (Cache Miss)
                 │                                  │             ┌───────▼────────┐
                 │                                  │             │   PostgreSQL   │
                 │                                  │             └───────┬────────┘
                 ▼                                  ▼                     ▼
          ┌─────────────┐                    ┌─────────────┐        HTTP 302 Found
          │    urls     │                    │click_events │         (Redirect)
          └─────────────┘                    └─────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language & Framework** | Java 17 / 21, Spring Boot 4 |
| **Data Persistence** | Spring Data JPA, Hibernate, PostgreSQL 16 |
| **Database Migrations** | Flyway |
| **Caching Layer** | Caffeine (In-Memory L1 Cache) |
| **Asynchronous Engine** | Spring Async Task Execution |
| **Containerization** | Docker, Docker Compose |
| **Build Tool** | Apache Maven |

---

## 🗄️ Database Schema (`Flyway V1`)

### `urls` Table
| Column | Type | Description |
|---|---|---|
| `id` | `BIGINT PRIMARY KEY` | Sequence-generated (`url_id_seq` starting at 1,000,000) |
| `original_url` | `TEXT NOT NULL` | Target destination URL |
| `short_code` | `VARCHAR(16) UNIQUE` | Base62-encoded code (Indexed) |
| `created_at` | `TIMESTAMPTZ` | Timestamp of creation |
| `expires_at` | `TIMESTAMPTZ` | Optional expiration timestamp |

### `click_events` Table
| Column | Type | Description |
|---|---|---|
| `id` | `BIGSERIAL PRIMARY KEY` | Auto-incrementing identifier |
| `short_code` | `VARCHAR(16) NOT NULL` | Foreign key referencing `urls(short_code)` |
| `clicked_at` | `TIMESTAMPTZ` | Timestamp when the short URL was accessed |
| `referrer` | `TEXT` | HTTP `Referer` header |
| `user_agent` | `TEXT` | HTTP `User-Agent` header |
| `ip_hash` | `VARCHAR(64)` | SHA-256 hashed client IP address |

---

## 📡 REST API Reference

### 1. Shorten URL
`POST /api/v1/urls`

**Request:**
```bash
curl -X POST http://localhost:9090/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com"}'
```

**Response (`201 Created`):**
```json
{
  "originalUrl": "https://github.com",
  "shortCode": "4c92",
  "shortUrl": "http://localhost:9090/4c92"
}
```

---

### 2. Redirect to Target URL
`GET /{shortCode}`

**Request:**
```bash
curl -i http://localhost:9090/4c92
```

**Response (`302 Found`):**
```http
HTTP/1.1 302 Found
Location: https://github.com
```

---

### 3. Fetch Click Analytics
`GET /api/v1/urls/{shortCode}/analytics`

**Request:**
```bash
curl -X GET http://localhost:9090/api/v1/urls/4c92/analytics
```

**Response (`200 OK`):**
```json
{
  "shortCode": "4c92",
  "totalClicks": 1420,
  "recentEvents": [
    {
      "clickedAt": "2026-09-09T23:55:00Z",
      "referrer": "https://twitter.com",
      "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)...",
      "ipHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }
  ]
}
```

---

## 🚦 Getting Started Locally

### Prerequisites
- Java 17+ installed
- Docker & Docker Compose

### 1. Start the PostgreSQL Container
```bash
docker compose up -d
```

### 2. Build and Run the Application
```bash
./mvnw spring-boot:run
```

The service will start on port `9090`. Flyway will automatically execute database migrations on startup.

### 3. Run Automated Tests

* **Run Unit Tests only (Fast, no DB required):**
  ```bash
  ./mvnw test
  ```

* **Run Unit & Integration Tests (Requires running PostgreSQL):**
  ```bash
  ./mvnw verify
  ```

---

## 📦 Packaging & Standalone Execution

### 1. Build the Production JAR
```bash
./mvnw clean package
```
This generates the executable JAR inside the `target/` directory:
```
target/url-shortener-1.0.0-SNAPSHOT.jar
```

### 2. Run the JAR
```bash
java -jar target/url-shortener-1.0.0-SNAPSHOT.jar
```

### 3. Running with Custom Configuration
You can override configuration properties via CLI arguments or environment variables:

* **Via Command-Line Arguments:**
  ```bash
  java -jar target/url-shortener-1.0.0-SNAPSHOT.jar --server.port=9090 --spring.datasource.url=jdbc:postgresql://localhost:5432/shortener_db
  ```

* **Via Environment Variables:**
  ```bash
  SPRING_PROFILES_ACTIVE=prod \
  SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/shortener_db \
  SPRING_DATASOURCE_USERNAME=postgres \
  SPRING_DATASOURCE_PASSWORD=postgrespassword \
  java -jar target/url-shortener-1.0.0-SNAPSHOT.jar
  ```

### 4. Health Check Verification
Once started, verify the service is running and connected to PostgreSQL via the Spring Boot Actuator endpoint:
```bash
curl http://localhost:9090/actuator/health
# Response: {"status":"UP"}
```

---

## 🛡️ Error Handling & Resiliency

- **404 Not Found**: Thrown when requesting non-existent short codes (`UrlNotFoundException`).
- **400 Bad Request**: Triggered when providing unsupported protocols (e.g. `ftp://`), empty payloads, or malformed URIs (`InvalidUrlException`).
- **Graceful Async Degradation**: If click event persistence encounters an error, the error is logged and isolated, guaranteeing that user redirection is never interrupted.
