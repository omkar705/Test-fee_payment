package com.test_feePayment.service;

import com.test_feePayment.model.UserInput;
import com.test_feePayment.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Business logic method to register a new user
     */
    public boolean registerUser(UserInput user) {
        int rowsAffected = userRepository.save(user);
        return rowsAffected > 0;
    }
}
