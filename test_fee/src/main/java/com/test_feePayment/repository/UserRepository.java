package com.test_feePayment.repository;

import com.test_feePayment.model.UserInput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes native SQL INSERT statement to store a new user in Supabase PostgreSQL
     */
    public int save(UserInput user) {
        String sql = """
            INSERT INTO users (username, email, phone, password)
            VALUES (?, ?, ?, ?)
            """;
        return jdbcTemplate.update(sql,
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword()
        );
    }
}
