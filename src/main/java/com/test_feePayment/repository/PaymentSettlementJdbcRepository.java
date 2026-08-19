package com.test_feePayment.repository;

import com.test_feePayment.model.PaymentSettlement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentSettlementJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentSettlementJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<PaymentSettlement> rowMapper = (rs, rowNum) -> {
        PaymentSettlement s = new PaymentSettlement();
        s.setSettlementId(rs.getLong("settlement_id"));
        s.setTransactionId(rs.getLong("transaction_id"));
        s.setMerchantAccount(rs.getString("merchant_account"));
        s.setSettledAmount(rs.getBigDecimal("settled_amount"));
        s.setCommissionFee(rs.getBigDecimal("commission_fee"));
        s.setSettlementStatus(rs.getString("settlement_status"));
        s.setSettlementDate(rs.getObject("settlement_date", OffsetDateTime.class));
        s.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return s;
    };

    /**
     * Executes raw PostgreSQL INSERT statement without Hibernate ORM
     */
    public int save(PaymentSettlement settlement) {
        String sql = """
            INSERT INTO payment_settlements (transaction_id, merchant_account, settled_amount, commission_fee, settlement_status)
            VALUES (?, ?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                settlement.getTransactionId(),
                settlement.getMerchantAccount(),
                settlement.getSettledAmount(),
                settlement.getCommissionFee(),
                settlement.getSettlementStatus() != null ? settlement.getSettlementStatus() : "PENDING"
        );
    }

    /**
     * Executes raw PostgreSQL SELECT statement to get all settlement records
     */
    public List<PaymentSettlement> findAll() {
        String sql = "SELECT * FROM payment_settlements ORDER BY settlement_id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Executes raw PostgreSQL SELECT statement by Primary Key
     */
    public Optional<PaymentSettlement> findById(Long id) {
        String sql = "SELECT * FROM payment_settlements WHERE settlement_id = ?";
        List<PaymentSettlement> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    /**
     * Executes raw PostgreSQL UPDATE statement
     */
    public int updateStatus(Long settlementId, String status) {
        String sql = "UPDATE payment_settlements SET settlement_status = ? WHERE settlement_id = ?";
        return jdbcTemplate.update(sql, status, settlementId);
    }

    /**
     * Executes raw PostgreSQL DELETE statement
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM payment_settlements WHERE settlement_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
