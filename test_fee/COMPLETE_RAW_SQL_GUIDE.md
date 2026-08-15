# Complete Guide to Pure SQL, Database Relationships & Spring JDBC (Without ORM / Hibernate)

Welcome to the definitive guide on building data-driven Spring Boot applications using **Pure SQL** and **Spring JDBC (`JdbcTemplate`)**. This guide will teach you every core concept, line of code, and step-by-step workflow so you can create new tables, define relationships, write raw SQL queries, and manage data entirely on your own without needing ORM frameworks like Hibernate or JPA.

---

## Table of Contents
1. [Core Concepts: Pure SQL vs ORM](#1-core-concepts-pure-sql-vs-orm)
2. [PostgreSQL Table Design & Data Types](#2-postgresql-table-design--data-types)
3. [Building Database Relationships in Pure SQL](#3-building-database-relationships-in-pure-sql)
4. [Spring JDBC (`JdbcTemplate`) & RowMapper Breakdown](#4-spring-jdbc-jdbctemplate--rowmapper-breakdown)
5. [Complete Code Architecture Walkthrough](#5-complete-code-architecture-walkthrough)
6. [Step-by-Step Tutorial: How to Add a New Table & Relation](#6-step-by-step-tutorial-how-to-add-a-new-table--relation)

---

## 1. Core Concepts: Pure SQL vs ORM

### Why Pure SQL?
- **Full Control**: You write explicit SQL statements (`INSERT`, `SELECT`, `UPDATE`, `DELETE`). The database executes exactly what you write—no hidden SQL generation or unexpected schema modifications.
- **High Performance**: No overhead from Hibernate entity sessions, dirty checking, or lazy/eager loading issues.
- **Predictable Behavior**: Easily test and run your SQL queries directly in Supabase/PostgreSQL dashboards.

### Key Components of Pure SQL Stack
1. **`schema.sql`**: A SQL script placed in `src/main/resources` where table definitions (`CREATE TABLE`), indexes, and constraints live.
2. **Spring `JdbcTemplate`**: A Spring helper class that executes SQL statements, handles database connections, manages transactions, and cleans up resources.
3. **`RowMapper<T>`**: An interface that converts each row of a SQL `ResultSet` into a plain Java object (POJO).
4. **POJO (Plain Old Java Object)**: Standard Java classes with private fields, constructors, getters, and setters (NO annotations like `@Entity`, `@Id`, or `@Column`).

---

## 2. PostgreSQL Table Design & Data Types

When creating tables in PostgreSQL, choosing the correct data type and constraint is vital.

| SQL Data Type | Java Data Type | Description / Usage |
| :--- | :--- | :--- |
| `BIGINT` | `Long` | 64-bit integer, ideal for Primary Keys (`id`) and Foreign Keys |
| `VARCHAR(n)` | `String` | Variable-length text string with maximum length `n` (e.g. `VARCHAR(100)`) |
| `TEXT` | `String` | Unlimited-length text string (e.g. JSON payloads, error logs) |
| `NUMERIC(p, s)` | `BigDecimal` or `Double` | Fixed-point decimal number. `p` is total digits, `s` is scale/decimals (e.g. `NUMERIC(12, 2)` for money amounts) |
| `TIMESTAMP WITH TIME ZONE` | `OffsetDateTime` | Date and time stored with UTC timezone offset |

### Primary Keys & Auto-Increment
In modern PostgreSQL, use `GENERATED ALWAYS AS IDENTITY PRIMARY KEY`:
```sql
transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
```
- `GENERATED ALWAYS AS IDENTITY`: PostgreSQL automatically generates a unique incremental ID (`1, 2, 3...`) whenever a new row is inserted.

### Essential Table Constraints
- `NOT NULL`: Prevents empty/null values in a column.
- `UNIQUE`: Guarantees all values in the column are distinct (e.g. `transaction_reference`).
- `DEFAULT <value>`: Provides a fallback value if none is specified during `INSERT` (e.g. `DEFAULT CURRENT_TIMESTAMP`).
- `CHECK (...)`: Validates column values against a condition (e.g. `CHECK (settlement_status IN ('PENDING', 'PROCESSED', 'FAILED'))`).

---

## 3. Building Database Relationships in Pure SQL

In Pure SQL, relationships are defined using **Foreign Keys** (`REFERENCES`) in `schema.sql` and mapped by storing foreign key ID fields in Java POJOs.

### 1. One-to-Many (1 : N) Relationship
**Example**: One `Transaction` can have **many** `PaymentGatewayLogs`.

#### Step A: SQL DDL (`schema.sql`)
In the child table (`payment_gateway_logs`), add a foreign key column referencing `transactions`:
```sql
CREATE TABLE payment_gateway_logs (
    gateway_log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    gateway_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL
);
```
- `REFERENCES transactions(transaction_id)`: Links `transaction_id` to the parent table.
- `ON DELETE CASCADE`: If a parent transaction is deleted, all associated gateway logs are automatically deleted by PostgreSQL.

#### Step B: Java POJO (`PaymentGatewayLog.java`)
Store the parent ID as a plain `Long` field:
```java
public class PaymentGatewayLog {
    private Long gatewayLogId;
    private Long transactionId; // Foreign key referencing Transaction
    private String gatewayName;
    private String status;
    // Getters and Setters...
}
```

#### Step C: SQL Query in Repository (`PaymentGatewayLogRepository.java`)
Fetch all child logs belonging to a specific transaction:
```java
public List<PaymentGatewayLog> findByTransactionId(Long transactionId) {
    String sql = "SELECT * FROM payment_gateway_logs WHERE transaction_id = ? ORDER BY gateway_log_id DESC";
    return jdbcTemplate.query(sql, rowMapper, transactionId);
}
```

---

### 2. One-to-One (1 : 1) Relationship
**Example**: One `Transaction` has exactly **one** `Receipt`.

#### Step A: SQL DDL (`schema.sql`)
Add a foreign key column with the `UNIQUE` constraint in the child table:
```sql
CREATE TABLE receipts (
    receipt_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL,
    receipt_url VARCHAR(500)
);
```
- `UNIQUE`: Ensures that no two rows in `receipts` can share the same `transaction_id`.

#### Step B: Java POJO (`Receipt.java`)
```java
public class Receipt {
    private Long receiptId;
    private Long transactionId; // Unique Foreign Key
    private Long studentId;
    private String receiptURL;
    // Getters and Setters...
}
```

#### Step C: SQL Query in Repository (`ReceiptRepository.java`)
```java
public Optional<Receipt> findByTransactionId(Long transactionId) {
    String sql = "SELECT * FROM receipts WHERE transaction_id = ?";
    List<Receipt> list = jdbcTemplate.query(sql, rowMapper, transactionId);
    return list.stream().findFirst();
}
```

---

### 3. Many-to-Many (N : M) Relationship
**Example**: A `Student` can enroll in **many** `Courses`, and a `Course` has **many** `Students`.

#### Step A: SQL DDL (`schema.sql`)
Create a **Junction Table** (Join Table) containing foreign keys from both parent tables:
```sql
CREATE TABLE students (
    student_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL
);

CREATE TABLE courses (
    course_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_title VARCHAR(100) NOT NULL
);

CREATE TABLE student_courses (
    student_id BIGINT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    course_id BIGINT NOT NULL REFERENCES courses(course_id) ON DELETE CASCADE,
    enrolled_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, course_id) -- Composite Primary Key
);
```

#### Step B: SQL Query using `JOIN`
To find all courses for a given student ID:
```sql
SELECT c.course_id, c.course_title 
FROM courses c
INNER JOIN student_courses sc ON c.course_id = sc.course_id
WHERE sc.student_id = ?;
```

---

## 4. Spring JDBC (`JdbcTemplate`) & RowMapper Breakdown

### Important `JdbcTemplate` Methods

#### 1. `jdbcTemplate.update(sql, params...)`
Used for write operations (`INSERT`, `UPDATE`, `DELETE`). Returns the number of affected rows (`int`).

```java
String sql = "INSERT INTO transactions (payment_id, transaction_reference, amount) VALUES (?, ?, ?)";
int rowsInserted = jdbcTemplate.update(sql, 101L, "REF12345", 2500.00);
```

#### 2. `jdbcTemplate.query(sql, rowMapper, params...)`
Used for read operations (`SELECT`) returning multiple rows. Returns a `List<T>`.

```java
String sql = "SELECT * FROM transactions WHERE transaction_status = ?";
List<Transaction> transactions = jdbcTemplate.query(sql, rowMapper, "SUCCESS");
```

### The Role of `RowMapper<T>`
A `RowMapper` maps raw SQL database columns from a `ResultSet` (`rs`) into your Java POJO fields.

```java
private final RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
    Transaction t = new Transaction();
    t.setTransactionId(rs.getLong("transaction_id"));
    t.setPaymentId(rs.getLong("payment_id"));
    t.setTransactionReference(rs.getString("transaction_reference"));
    t.setGatewayName(rs.getString("gateway_name"));
    t.setTransactionStatus(rs.getString("transaction_status"));
    t.setTransactionDate(rs.getObject("transaction_date", OffsetDateTime.class));
    t.setAmount(rs.getDouble("amount"));
    t.setVerifiedBy(rs.getString("verified_by"));
    t.setVersion(rs.getLong("version"));
    t.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    t.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
    return t;
};
```

---

## 5. Complete Code Architecture Walkthrough

Here is how all layers connect in our project:

```
                  ┌────────────────────────┐
                  │    HTTP Client / UI    │
                  └───────────┬────────────┘
                              │ REST (JSON)
                              ▼
                  ┌────────────────────────┐
                  │   PaymentController    │  (@RestController)
                  └───────────┬────────────┘
                              │ Java Method Call
                              ▼
                  ┌────────────────────────┐
                  │     PaymentService     │  (@Service)
                  └───────────┬────────────┘
                              │ Java Method Call
                              ▼
                  ┌────────────────────────┐
                  │ TransactionRepository  │  (@Repository)
                  └───────────┬────────────┘
                              │ Native SQL Query via JdbcTemplate
                              ▼
                  ┌────────────────────────┐
                  │  Supabase PostgreSQL   │  (Database)
                  └────────────────────────┘
```

### Code Snippets from the Project

#### 1. Repository Layer (`TransactionRepository.java`)
```java
@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions (payment_id, transaction_reference, gateway_name, transaction_status, amount, verified_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                transaction.getPaymentId(),
                transaction.getTransactionReference(),
                transaction.getGatewayName(),
                transaction.getTransactionStatus(),
                transaction.getAmount(),
                transaction.getVerifiedBy()
        );
    }
}
```

#### 2. Service Layer (`PaymentService.java`)
```java
@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;

    public PaymentService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public int createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
```

#### 3. Controller Layer (`PaymentController.java`)
```java
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<String> createTransaction(@RequestBody Transaction transaction) {
        int rows = paymentService.createTransaction(transaction);
        if (rows > 0) {
            return ResponseEntity.ok("Transaction inserted into Supabase PostgreSQL successfully via Native SQL!");
        }
        return ResponseEntity.badRequest().body("Failed to insert transaction.");
    }
}
```

---

## 6. Step-by-Step Tutorial: How to Add a New Table & Relation

Follow this 5-step blueprint whenever you want to create a new table and relationship from scratch.

### Example Scenario
Let's add a new table named `refunds` linked to `transactions` in a **1-to-Many** relationship (one transaction can have multiple refunds).

---

### Step 1: Add DDL to `src/main/resources/schema.sql`

Open `schema.sql` and append the new table statement with a foreign key reference:

```sql
CREATE TABLE IF NOT EXISTS refunds (
    refund_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    refund_amount NUMERIC(12, 2) NOT NULL,
    refund_reason VARCHAR(255),
    refund_status VARCHAR(50) DEFAULT 'INITIATED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refunds_transaction ON refunds (transaction_id);
```

---

### Step 2: Create Java POJO in `com.test_feePayment.model`

Create `Refund.java`:

```java
package com.test_feePayment.model;

import java.time.OffsetDateTime;

public class Refund {

    private Long refundId;
    private Long transactionId; // Foreign key
    private Double refundAmount;
    private String refundReason;
    private String refundStatus;
    private OffsetDateTime createdAt;

    public Refund() {
    }

    public Refund(Long transactionId, Double refundAmount, String refundReason, String refundStatus) {
        this.transactionId = transactionId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus;
    }

    public Long getRefundId() { return refundId; }
    public void setRefundId(Long refundId) { this.refundId = refundId; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
```

---

### Step 3: Create Repository in `com.test_feePayment.repository`

Create `RefundJdbcRepository.java`:

```java
package com.test_feePayment.repository;

import com.test_feePayment.model.Refund;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class RefundJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public RefundJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Refund> rowMapper = (rs, rowNum) -> {
        Refund r = new Refund();
        r.setRefundId(rs.getLong("refund_id"));
        r.setTransactionId(rs.getLong("transaction_id"));
        r.setRefundAmount(rs.getDouble("refund_amount"));
        r.setRefundReason(rs.getString("refund_reason"));
        r.setRefundStatus(rs.getString("refund_status"));
        r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return r;
    };

    public int save(Refund refund) {
        String sql = """
            INSERT INTO refunds (transaction_id, refund_amount, refund_reason, refund_status)
            VALUES (?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                refund.getTransactionId(),
                refund.getRefundAmount(),
                refund.getRefundReason(),
                refund.getRefundStatus() != null ? refund.getRefundStatus() : "INITIATED"
        );
    }

    public List<Refund> findByTransactionId(Long transactionId) {
        String sql = "SELECT * FROM refunds WHERE transaction_id = ? ORDER BY refund_id DESC";
        return jdbcTemplate.query(sql, rowMapper, transactionId);
    }
}
```

---

### Step 4: Add Logic to Service

Update `PaymentService.java`:

```java
@Service
public class PaymentService {

    private final RefundJdbcRepository refundRepository;

    public PaymentService(..., RefundJdbcRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    public int processRefund(Refund refund) {
        return refundRepository.save(refund);
    }

    public List<Refund> getRefundsByTransaction(Long transactionId) {
        return refundRepository.findByTransactionId(transactionId);
    }
}
```

---

### Step 5: Expose REST Endpoint in Controller

Update `PaymentController.java`:

```java
@PostMapping("/refunds")
public ResponseEntity<String> createRefund(@RequestBody Refund refund) {
    int rows = paymentService.processRefund(refund);
    if (rows > 0) {
        return ResponseEntity.ok("Refund recorded successfully via Native SQL!");
    }
    return ResponseEntity.badRequest().body("Failed to record refund.");
}

@GetMapping("/refunds/transaction/{transactionId}")
public ResponseEntity<List<Refund>> getRefundsByTransaction(@PathVariable Long transactionId) {
    return ResponseEntity.ok(paymentService.getRefundsByTransaction(transactionId));
}
```

---

## Summary Checklist for Pure SQL Projects

1. **Write DDL in `schema.sql`**: Use `CREATE TABLE IF NOT EXISTS`, `REFERENCES`, and constraints (`NOT NULL`, `UNIQUE`, `CHECK`).
2. **Create POJO**: Pure Java class with matching field types and getters/setters.
3. **Write Repository**: Annotate with `@Repository`, inject `JdbcTemplate`, write SQL strings, and map rows using `RowMapper<T>`.
4. **Wire Service & Controller**: Delegate persistence calls from `@Service` to `@Repository` and expose JSON endpoints via `@RestController`.
