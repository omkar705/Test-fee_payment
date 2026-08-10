# Team 2: Payment Processing & Transaction Management
## Comprehensive Beginner's Hibernate & JPA Guide

---

## 1. Team 2 Module Overview

Welcome to Team 2's module of the **Fee Payment Management System**! 

This module handles everything that happens when a student or parent attempts to pay a fee. It receives payment requests, communicates with payment gateways (simulated), tracks transaction states, maintains gateway audit logs, generates receipts upon successful completion, and protects system data integrity during high-concurrency payment surges.

---

## 2. What this module is responsible for

- **Payment Transaction Processing**: Creating and managing the lifecycle of payment transactions.
- **Simulated Payment Gateway Interaction**: Handling gateway requests, status checks, and responses.
- **Transaction Verification & Status Management**: Updating transaction states (e.g., `INITIATE`, `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`) and tracking verifiers.
- **Receipt Generation**: Automatically generating payment receipts when payments complete successfully.
- **Payment Gateway Logging**: Preserving audit logs of every gateway interaction for accounting and troubleshooting.
- **Rollback & Concurrency Readiness**: Utilizing optimistic locking (`@Version`) to safely handle simultaneous payments without race conditions.

---

## 3. What is NOT part of Team 2

- **Fee Structure & Student Fee Records (`FeePayment`)**: Managed by Team 1. Our module only stores `paymentId` as a foreign key reference.
- **Student Profile Management**: Managed by another team. Our module only references `studentId`.
- **User Authentication System**: Managed separately.
- **Full Production Gateway Integration**: We simulate gateway responses at this initial stage.

---

## 4. Database Tables Created

Hibernate automatically creates the following 3 tables in PostgreSQL / Supabase based on `@Entity` mappings:

1. `transactions`: Stores core payment transaction records.
2. `payment_gateway_logs`: Stores audit history for each gateway interaction.
3. `receipts`: Stores payment proof receipts for successful transactions.

---

## 5. Entity Relationships

Here is how our 3 entities relate to each other and to Team 1's `FeePayment` entity:

```
FeePayment (Team 1)
    ^
    | (references via paymentId)
    |
Transaction (1) <─────── (1..N) ───────> PaymentGatewayLog
    |
    | (1)
    v
Receipt (0..1)
```

- **Transaction 1 ─── N PaymentGatewayLog**: A single transaction can have multiple log entries (`INITIATE`, `VERIFY`, `STATUS_CHECK`, `REFUND`).
- **Transaction 1 ─── 0..1 Receipt**: A transaction produces at most 1 receipt only after reaching `SUCCESS` status. Failed or pending payments do not have receipts.
- **Transaction ─── FeePayment**: Transaction references `paymentId` of the student's fee record without recreating Team 1's entity.

---

## 6. Why Transaction is the Central Entity

`Transaction` is the heart of Team 2's module because:
1. Every payment flow starts and ends with a transaction.
2. Gateway logs (`PaymentGatewayLog`) are attached directly to a `Transaction`.
3. Payment receipts (`Receipt`) are issued for a successful `Transaction`.
4. Concurrency protection (`@Version`) is enforced on `Transaction` to guarantee data integrity when multiple payment requests happen at once.

---

## 7. Explanation of Each Entity

### A. `Transaction.java` (`transactions` table)
Represents a payment attempt made by a student. It maintains the status, gateway name, monetary amount, verification info, timestamp, version number, and associated logs and receipt.

### B. `PaymentGatewayLog.java` (`payment_gateway_logs` table)
Represents an individual API payload log sent to or received from a payment gateway (e.g. Razorpay, PayU, PhonePe).

### C. `Receipt.java` (`receipts` table)
Represents the official receipt document generated after a transaction is verified as successful.

---

## 8. Explanation of Every Important Field

### `Transaction` Fields:
- `transactionId` (`Long`): Primary Key. Uniquely identifies each transaction in the database.
- `paymentId` (`Long`): Foreign key reference to Team 1's `FeePayment` record.
- `transactionReference` (`String`): Unique string (e.g. `TXN-1718192039`) given to the user and gateway.
- `gatewayName` (`String`): The gateway provider used (e.g., `RAZORPAY`, `STRIPE`, `UPI`).
- `transactionStatus` (`String`): Current state (`INITIATE`, `PENDING`, `SUCCESS`, `FAILED`).
- `transactionDate` (`LocalDateTime`): Exact date and time when transaction was initiated.
- `amount` (`Double`): Monetary amount of the transaction.
- `verifiedBy` (`String`): Admin or automated system process that verified the transaction.
- `version` (`Long`): Concurrency counter annotated with `@Version` for Hibernate optimistic locking.
- `createdAt` / `updatedAt` (`LocalDateTime`): Timestamps for entity lifecycle tracking.

### `PaymentGatewayLog` Fields:
- `gatewayLogId` (`Long`): Primary Key.
- `transaction` (`Transaction`): Many-to-One object reference linking the log to its transaction.
- `gatewayName` (`String`): Name of the gateway.
- `requestData` (`String`): JSON string of request sent to gateway.
- `responseData` (`String`): JSON response received from gateway.
- `status` (`String`): Operation status (`INITIATE`, `VERIFY`, `SUCCESS`).
- `logTime` (`LocalDateTime`): Timestamp of the log event.

### `Receipt` Fields:
- `receiptId` (`Long`): Primary Key.
- `transaction` (`Transaction`): One-to-One object reference linking the receipt to its transaction.
- `studentId` (`Long`): ID of the student receiving the receipt.
- `generatedDate` (`LocalDateTime`): Timestamp when receipt was issued.
- `receiptURL` (`String`): Download URL or cloud storage path for receipt PDF.

---

## 9. What ORM Means

**ORM (Object-Relational Mapping)** is a technique that lets software developers query and manipulate data from a database using an object-oriented paradigm. Instead of manually writing SQL strings like `INSERT INTO transactions VALUES (...)`, you work with standard Java objects (`Transaction txn = new Transaction()`), and the ORM framework handles SQL translation automatically.

---

## 10. What Hibernate Does

**Hibernate** is the ORM framework implementation. It automatically:
- Converts Java classes into database tables.
- Translates Java method calls (`repository.save(transaction)`) into SQL queries (`INSERT` / `UPDATE`).
- Manages database connections and transactions.
- Tracks changes on entities and synchronized updates with PostgreSQL.

---

## 11. What JPA Is

**JPA (Jakarta Persistence API)** is a standard Java specification (a set of interfaces and annotations like `@Entity`, `@Id`, `@Table`). It defines *how* object-relational mapping should work in Java, but doesn't contain implementation code itself.

---

## 12. Difference Between JPA and Hibernate

- **JPA** is the **interface / specification** (the rulebook).
- **Hibernate** is the **implementation** (the engine that carries out the rules).

Think of JPA as a web browser specification, and Hibernate as Google Chrome. Spring Boot uses JPA annotations, and Hibernate runs under the hood.

---

## 13. Explanation of `@Entity`

Tells Hibernate that the annotated Java class should be mapped to a table in PostgreSQL.

```java
@Entity
public class Transaction { ... }
```

---

## 14. Explanation of `@Id`

Marks a field as the Primary Key of the entity table.

```java
@Id
private Long transactionId;
```

---

## 15. Explanation of `@GeneratedValue`

Specifies how the primary key value should be generated. `GenerationType.IDENTITY` tells PostgreSQL to use auto-incrementing numbers (`BIGSERIAL`).

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

---

## 16. Explanation of `@Column`

Configures table column details such as custom column names, uniqueness constraints, nullability, and text definitions.

```java
@Column(name = "transaction_reference", nullable = false, unique = true)
private String transactionReference;
```

---

## 17. Explanation of `@OneToMany`

Defines a one-to-many relationship. In our project, one `Transaction` has many `PaymentGatewayLog` entries.

```java
@OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
private List<PaymentGatewayLog> gatewayLogs = new ArrayList<>();
```

- `mappedBy = "transaction"` tells Hibernate that the `transaction` field in `PaymentGatewayLog` owns the relationship.
- `cascade = CascadeType.ALL` means saving or deleting a transaction will automatically save or delete its gateway logs.

---

## 18. Explanation of `@OneToOne`

Defines a one-to-one relationship. In our project, one `Transaction` can have at most one `Receipt`.

```java
@OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
private Receipt receipt;
```

---

## 19. Explanation of `@JoinColumn`

Specifies the foreign key column name in the database table that links the entities.

```java
@JoinColumn(name = "transaction_id", nullable = false, unique = true)
private Transaction transaction;
```

In `Receipt`, `@JoinColumn(name = "transaction_id", unique = true)` creates a foreign key column named `transaction_id` in the `receipts` table with a UNIQUE constraint to enforce 1:0..1 mapping.

---

## 20. Explanation of `@Version`

Enables **Optimistic Locking** for concurrency control.

```java
@Version
@Column(name = "version")
private Long version;
```

Hibernate automatically increments this number on every update SQL statement.

---

## 21. Why `@Version` is Important for This Project

When 10+ students or background webhooks update the same transaction simultaneously:
Without `@Version`, two concurrent requests might overwrite each other's changes blindly ("Lost Update Problem").
With `@Version`, Hibernate checks the version number before performing an update:
`UPDATE transactions SET status = 'SUCCESS', version = 2 WHERE id = 1 AND version = 1;`
If another thread updated the row first (bumping version to 2), the second update matches 0 rows and Hibernate throws an `OptimisticLockException`. This protects payment data integrity!

---

## 22. Explanation of `JpaRepository`

`JpaRepository<Transaction, Long>` is a Spring Data JPA interface. By extending it, Spring automatically generates CRUD SQL methods without writing any SQL:
- `.save(entity)` -> INSERT or UPDATE
- `.findById(id)` -> SELECT * WHERE id = ?
- `.findAll()` -> SELECT * FROM transactions
- `.deleteById(id)` -> DELETE FROM transactions WHERE id = ?

---

## 23. Explanation of `@Transactional`

Tells Spring Boot to execute all operations inside a database transaction block:
- If all database actions in the method succeed, Spring commits the transaction.
- If an exception occurs, Spring automatically **rolls back** all database changes made inside that method!

---

## 24. How Java Objects Become Database Rows

1. You instantiate a Java object: `Transaction txn = new Transaction(...);`
2. You call `transactionRepository.save(txn);`
3. Hibernate inspects the `@Entity` and `@Column` annotations on `Transaction`.
4. Hibernate builds SQL string: `INSERT INTO transactions (amount, gateway_name, ...) VALUES (5000.0, 'RAZORPAY', ...);`
5. Hibernate sends SQL to PostgreSQL (Supabase) via JDBC driver.
6. PostgreSQL writes row into the database table and returns generated ID.

---

## 25. How Database Relationships Become Java Object Relationships

- In PostgreSQL, relationships are held via Foreign Key columns (e.g. `receipts.transaction_id -> transactions.transaction_id`).
- In Java, Hibernate converts foreign keys into real object references (`receipt.getTransaction()` returns a full `Transaction` Java object).
- When fetching a `Transaction` from `TransactionRepository`, Hibernate can automatically fetch its list of `PaymentGatewayLog` objects (`transaction.getGatewayLogs()`).

---

## 26. Complete Request Flow

```
[ HTTP Client / Postman ]
        │
        │ 1. POST /api/payments (JSON payload)
        ▼
[ PaymentController.java ] ── calls paymentService.createTransaction()
        │
        ▼
[ PaymentService.java ] ── creates Transaction & PaymentGatewayLog entities
        │
        ▼
[ TransactionRepository.java ] ── Spring Data JPA
        │
        ▼
[ Hibernate / JPA ] ── translates Java objects into SQL INSERT commands
        │
        ▼
[ PostgreSQL / Supabase Database ] ── executes SQL & creates rows in tables
```

---

## 27. How to Navigate Through the Project Files

If you want to understand how a payment is created:
1. Start at **`PaymentController.java`** (`POST /api/payments`).
2. Go to **`PaymentService.java`** (`createTransaction()` method).
3. Go to **`Transaction.java`** & **`PaymentGatewayLog.java`** (understand entity structure).
4. Go to **`TransactionRepository.java`** (Spring Data JPA interface).
5. Hibernate translates objects to SQL.
6. PostgreSQL/Supabase stores database rows.

---

## 28. Where to Make Changes If You Want To:

- **Add a new transaction field**: Open `entity/Transaction.java` -> add field with `@Column` -> add getter/setter.
- **Change transaction status logic**: Open `service/PaymentService.java` -> update `updateTransactionStatus()`.
- **Add a new gateway log field**: Open `entity/PaymentGatewayLog.java` -> add field with `@Column` -> update constructor/getters.
- **Change receipt information**: Open `entity/Receipt.java` -> add field -> update `generateReceipt()` in `PaymentService.java`.
- **Add a new database relationship**: Add `@ManyToOne` or `@OneToMany` annotations in the target entity.
- **Modify an API endpoint**: Open `controller/PaymentController.java` -> edit route mappings.

---

## 29. Concurrency Preparation

The project is fully prepared for optimistic concurrency handling:
- `Transaction` entity contains `@Version private Long version;`
- Service methods are annotated with `@Transactional`.
- Entity updates happen via persistent context reloading and managed state, preventing blind overwrites.

---

## 30. What Remains to be Implemented for Team 2

1. **Concurrency Simulation Test Suite**: Executing 10+ simultaneous multithreaded requests to verify `OptimisticLockException` retry mechanisms.
2. **Payment Gateway Integration Adapter**: Real external HTTP API client integration (e.g. Webhook signature verification).
3. **Automated Refund & Rollback Handling**: Detailed failure recovery workflows.

---

## 31. Simple Testing Procedure

### Option A: Testing via cURL or Postman

#### 1. Create a Payment Transaction
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": 101,
    "gatewayName": "RAZORPAY",
    "amount": 4500.00
  }'
```
*Response*: Returns created transaction object with ID `1` and status `INITIATE`.

#### 2. Get All Transactions
```bash
curl -X GET http://localhost:8080/api/payments
```

#### 3. Update Status to SUCCESS (Triggers automatic receipt generation)
```bash
curl -X PUT http://localhost:8080/api/payments/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SUCCESS",
    "verifiedBy": "ADMIN_OFFICER",
    "studentId": 5001
  }'
```

#### 4. Retrieve Receipt for Transaction 1
```bash
curl -X GET http://localhost:8080/api/payments/1/receipt
```

---

## 32. Database Creation & Supabase Connection Troubleshooting

### A. Supabase Connection URL Configuration (`application.properties`)
To connect from Windows IPv4 networks without `UnknownHostException` errors, use the **Supabase Transaction Pooler Host**:

```properties
spring.application.name=test_fee
spring.datasource.url=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.vyfiyrpmzvxhfadwtnbu
spring.datasource.password=Oct79SDOWTujZnWB
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### B. Confirmed Generated DDL Statements
When running `mvn spring-boot:run`, Hibernate automatically generates and executes these 3 table structures in Supabase PostgreSQL:

```sql
create table payment_gateway_logs (
    gateway_log_id bigint generated by default as identity,
    gateway_name varchar(255) not null,
    log_time timestamp(6),
    request_data TEXT,
    response_data TEXT,
    status varchar(255) not null,
    transaction_id bigint not null,
    primary key (gateway_log_id)
);

create table receipts (
    receipt_id bigint generated by default as identity,
    generated_date timestamp(6),
    receipt_url varchar(255),
    student_id bigint not null,
    transaction_id bigint not null,
    primary key (receipt_id)
);

create table transactions (
    transaction_id bigint generated by default as identity,
    amount float(53) not null,
    created_at timestamp(6),
    gateway_name varchar(255) not null,
    payment_id bigint not null,
    transaction_date timestamp(6),
    transaction_reference varchar(255) not null,
    transaction_status varchar(255) not null,
    updated_at timestamp(6),
    verified_by varchar(255),
    version bigint not null,
    primary key (transaction_id)
);

alter table receipts add constraint UK_receipts_transaction_id unique (transaction_id);
alter table transactions add constraint UK_transactions_reference unique (transaction_reference);
alter table payment_gateway_logs add constraint FK_logs_transaction foreign key (transaction_id) references transactions;
alter table receipts add constraint FK_receipts_transaction foreign key (transaction_id) references transactions;
```

---

*Documentation prepared for Fee Payment Management System - Team 2 Module*
