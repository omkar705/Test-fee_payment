package com.test_feePayment.repository;

import com.test_feePayment.model.PaymentGatewayLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class PaymentGatewayLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentGatewayLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PaymentGatewayLog> rowMapper = (rs, rowNum) -> {
        PaymentGatewayLog log = new PaymentGatewayLog();
        log.setGatewayLogId(rs.getLong("gateway_log_id"));
        log.setTransactionId(rs.getLong("transaction_id"));
        log.setGatewayName(rs.getString("gateway_name"));
        log.setRequestData(rs.getString("request_data"));
        log.setResponseData(rs.getString("response_data"));
        log.setStatus(rs.getString("status"));
        log.setLogTime(rs.getObject("log_time", OffsetDateTime.class));
        return log;
    };

    /**
     * Executes raw PostgreSQL INSERT statement for gateway log
     */
    public int save(PaymentGatewayLog log) {
        String sql = """
            INSERT INTO payment_gateway_logs (transaction_id, gateway_name, request_data, response_data, status)
            VALUES (?, ?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                log.getTransactionId(),
                log.getGatewayName(),
                log.getRequestData(),
                log.getResponseData(),
                log.getStatus()
        );
    }

    /**
     * Executes raw PostgreSQL SELECT statement to find logs by transaction_id
     */
    public List<PaymentGatewayLog> findByTransactionId(Long transactionId) {
        String sql = "SELECT * FROM payment_gateway_logs WHERE transaction_id = ? ORDER BY gateway_log_id DESC";
        return jdbcTemplate.query(sql, rowMapper, transactionId);
    }

    /**
     * Executes raw PostgreSQL SELECT statement to fetch all logs
     */
    public List<PaymentGatewayLog> findAll() {
        String sql = "SELECT * FROM payment_gateway_logs ORDER BY gateway_log_id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
