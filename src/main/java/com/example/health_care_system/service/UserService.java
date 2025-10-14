package com.example.health_care_system.service;

import com.example.health_care_system.dto.ChangePasswordDto;
import com.example.health_care_system.dto.LoginDto;
import com.example.health_care_system.dto.UserProfileDto;
import com.example.health_care_system.dto.UserRegistrationDto;
import com.example.health_care_system.model.PasswordResetToken;
import com.example.health_care_system.model.User;
import com.example.health_care_system.repository.PasswordResetTokenRepository;
import com.example.health_care_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private EmailService emailService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(UserRegistrationDto registrationDto) throws Exception {
        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new Exception("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new Exception("Email already exists");
        }

        // Check if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new Exception("Passwords do not match");
        }

        // Create new user
        User user = new User(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                passwordEncoder.encode(registrationDto.getPassword()),
                registrationDto.getFirstName(),
                registrationDto.getLastName()
        );

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User authenticateUser(LoginDto loginDto) throws Exception {
        User user = null;

        // Try to find user by username first, then by email
        if (loginDto.getUsernameOrEmail().contains("@")) {
            user = userRepository.findByEmail(loginDto.getUsernameOrEmail()).orElse(null);
        } else {
            user = userRepository.findByUsername(loginDto.getUsernameOrEmail()).orElse(null);
        }

        // If not found by one method, try the other
        if (user == null) {
            if (loginDto.getUsernameOrEmail().contains("@")) {
                user = userRepository.findByUsername(loginDto.getUsernameOrEmail()).orElse(null);
            } else {
                user = userRepository.findByEmail(loginDto.getUsernameOrEmail()).orElse(null);
            }
        }

        if (user == null) {
            throw new Exception("Invalid username/email or password");
        }

        if (!user.isActive()) {
            throw new Exception("Account is disabled");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new Exception("Invalid username/email or password");
        }

        return user;
    }

    public void initiatePasswordReset(String email) throws Exception {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("No account found with that email address"));

        // Delete any existing reset tokens for this email
        resetTokenRepository.deleteByEmail(email);

        // Generate a new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, email);

        // Save the token
        resetTokenRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordResetEmail(email, token);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new Exception("Invalid password reset token"));

        if (!resetToken.isValid()) {
            throw new Exception("Password reset token has expired or already been used");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new Exception("User not found"));

        // Update user password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    public PasswordResetToken validateResetToken(String token) throws Exception {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new Exception("Invalid password reset token"));

        if (!resetToken.isValid()) {
            throw new Exception("Password reset token has expired or already been used");
        }

        return resetToken;
    }

    public UserProfileDto getUserProfile(String userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUsername(user.getUsername());
        profileDto.setEmail(user.getEmail());
        profileDto.setFirstName(user.getFirstName());
        profileDto.setLastName(user.getLastName());

        return profileDto;
    }

    public User updateUserProfile(String userId, UserProfileDto profileDto) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Check if username is being changed and if it's already taken
        if (!existingUser.getUsername().equals(profileDto.getUsername())) {
            if (userRepository.existsByUsername(profileDto.getUsername())) {
                throw new Exception("Username already exists");
            }
        }

        // Check if email is being changed and if it's already taken
        if (!existingUser.getEmail().equals(profileDto.getEmail())) {
            if (userRepository.existsByEmail(profileDto.getEmail())) {
                throw new Exception("Email already exists");
            }
        }

        // Update basic profile information
        existingUser.setUsername(profileDto.getUsername());
        existingUser.setEmail(profileDto.getEmail());
        existingUser.setFirstName(profileDto.getFirstName());
        existingUser.setLastName(profileDto.getLastName());
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    public User changePassword(String userId, ChangePasswordDto changePasswordDto) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), existingUser.getPassword())) {
            throw new Exception("Current password is incorrect");
        }

        // Validate new passwords match
        if (!changePasswordDto.isPasswordMatch()) {
            throw new Exception("New passwords do not match");
        }

        // Update password
        existingUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }
}
