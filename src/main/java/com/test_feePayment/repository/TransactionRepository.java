package com.test_feePayment.repository;

import com.test_feePayment.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    /**
     * Executes raw PostgreSQL INSERT statement for transactions
     */
    public int save(Transaction transaction) {
        String sql = """
            INSERT INTO transactions
            (payment_id, transaction_reference, gateway_name, transaction_status, amount, verified_by, version)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                transaction.getPaymentId(),
                transaction.getTransactionReference(),
                transaction.getGatewayName(),
                transaction.getTransactionStatus(),
                transaction.getAmount(),
                transaction.getVerifiedBy(),
                transaction.getVersion() != null ? transaction.getVersion() : 1L
        );
    }

    /**
     * Executes raw PostgreSQL SELECT statement to fetch all transactions
     */
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY transaction_id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Executes raw PostgreSQL SELECT statement by Primary Key
     */
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        List<Transaction> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    /**
     * Executes raw PostgreSQL SELECT statement by Unique Transaction Reference
     */
    public Optional<Transaction> findByTransactionReference(String reference) {
        String sql = "SELECT * FROM transactions WHERE transaction_reference = ?";
        List<Transaction> list = jdbcTemplate.query(sql, rowMapper, reference);
        return list.stream().findFirst();
    }

    /**
     * Executes raw PostgreSQL UPDATE statement for transaction status
     */
    public int updateStatus(Long transactionId, String status) {
        String sql = "UPDATE transactions SET transaction_status = ?, updated_at = CURRENT_TIMESTAMP WHERE transaction_id = ?";
        return jdbcTemplate.update(sql, status, transactionId);
    }

    /**
     * Executes raw PostgreSQL DELETE statement
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        return jdbcTemplate.update(sql, id);
    }
    public Long saveAndReturnId(Transaction transaction) {

    String sql = """
        INSERT INTO transactions
        (payment_id, transaction_reference, gateway_name,
         transaction_status, amount, verified_by, version)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        RETURNING transaction_id
        """;

    return jdbcTemplate.queryForObject(
            sql,
            Long.class,
            transaction.getPaymentId(),
            transaction.getTransactionReference(),
            transaction.getGatewayName(),
            transaction.getTransactionStatus(),
            transaction.getAmount(),
            transaction.getVerifiedBy(),
            transaction.getVersion() != null ? transaction.getVersion() : 1L
    );
}
}
