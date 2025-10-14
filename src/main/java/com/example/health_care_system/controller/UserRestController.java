package com.example.health_care_system.controller;

import com.example.health_care_system.dto.*;
import com.example.health_care_system.model.User;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        try {
            User user = userService.registerUser(registrationDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Welcome " + user.getFirstName() + "!");
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto,
                                   BindingResult result,
                                   HttpSession session) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        try {
            User user = userService.authenticateUser(loginDto);
            
            // Store user in session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFirstName() + " " + user.getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Welcome back, " + user.getFirstName() + "!");
            response.put("user", createUserResponse(user));
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "You have been logged out successfully!");
        return ResponseEntity.ok(response);
    }

    // Get current user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorMessage("Not authenticated"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", createUserResponse(loggedInUser));
        return ResponseEntity.ok(response);
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        try {
            userService.initiatePasswordReset(forgotPasswordDto.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset instructions have been sent to your email address.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto,
                                          BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        if (!resetPasswordDto.isPasswordMatch()) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage("Passwords do not match"));
        }

        try {
            userService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Your password has been successfully reset!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Get user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorMessage("Please log in to access your profile."));
        }

        try {
            UserProfileDto profileDto = userService.getUserProfile(loggedInUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profileDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage("Error loading profile: " + e.getMessage()));
        }
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileDto profileDto,
                                          BindingResult result,
                                          HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorMessage("Please log in to access your profile."));
        }

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        try {
            User updatedUser = userService.updateUserProfile(loggedInUser.getId(), profileDto);
            
            // Update session
            session.setAttribute("loggedInUser", updatedUser);
            session.setAttribute("username", updatedUser.getUsername());
            session.setAttribute("fullName", updatedUser.getFirstName() + " " + updatedUser.getLastName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully!");
            response.put("user", createUserResponse(updatedUser));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                                           BindingResult result,
                                           HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorMessage("Please log in to change your password."));
        }

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse(result));
        }

        if (!changePasswordDto.isPasswordMatch()) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage("New passwords do not match"));
        }

        try {
            userService.changePassword(loggedInUser.getId(), changePasswordDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password changed successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorMessage(e.getMessage()));
        }
    }

    // Helper methods
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("active", user.isActive());
        return userMap;
    }

    private Map<String, Object> createErrorMessage(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    private Map<String, Object> createErrorResponse(BindingResult result) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("success", false);
        result.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }
}
