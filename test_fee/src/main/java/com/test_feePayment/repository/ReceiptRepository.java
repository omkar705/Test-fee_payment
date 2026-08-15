package com.test_feePayment.repository;

import com.test_feePayment.model.Receipt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReceiptRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReceiptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Receipt> rowMapper = (rs, rowNum) -> {
        Receipt r = new Receipt();
        r.setReceiptId(rs.getLong("receipt_id"));
        r.setTransactionId(rs.getLong("transaction_id"));
        r.setStudentId(rs.getLong("student_id"));
        r.setGeneratedDate(rs.getObject("generated_date", OffsetDateTime.class));
        r.setReceiptURL(rs.getString("receipt_url"));
        return r;
    };

    /**
     * Executes raw PostgreSQL INSERT statement for receipt
     */
    public int save(Receipt receipt) {
        String sql = """
            INSERT INTO receipts (transaction_id, student_id, receipt_url)
            VALUES (?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                receipt.getTransactionId(),
                receipt.getStudentId(),
                receipt.getReceiptURL()
        );
    }

    /**
     * Executes raw PostgreSQL SELECT statement by transaction_id
     */
    public Optional<Receipt> findByTransactionId(Long transactionId) {
        String sql = "SELECT * FROM receipts WHERE transaction_id = ?";
        List<Receipt> list = jdbcTemplate.query(sql, rowMapper, transactionId);
        return list.stream().findFirst();
    }

    /**
     * Executes raw PostgreSQL SELECT statement by Primary Key
     */
    public Optional<Receipt> findById(Long receiptId) {
        String sql = "SELECT * FROM receipts WHERE receipt_id = ?";
        List<Receipt> list = jdbcTemplate.query(sql, rowMapper, receiptId);
        return list.stream().findFirst();
    }

    /**
     * Executes raw PostgreSQL SELECT statement to fetch all receipts
     */
    public List<Receipt> findAll() {
        String sql = "SELECT * FROM receipts ORDER BY receipt_id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
