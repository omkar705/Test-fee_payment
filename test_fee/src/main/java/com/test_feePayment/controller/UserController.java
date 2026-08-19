package com.test_feePayment.controller;

import com.test_feePayment.model.UserInput;
import com.test_feePayment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to handle user signup POST requests
     * URL: POST http://localhost:8080/api/users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserInput user) {
        boolean isSaved = userService.registerUser(user);

        if (isSaved) {
            return ResponseEntity.ok("User registered successfully in Supabase PostgreSQL!");
        } else {
            return ResponseEntity.badRequest().body("Failed to register user.");
        }
    }
}
