package com.example.health_care_system.repository;

import com.example.health_care_system.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByEmail(String email);

    List<PasswordResetToken> findByEmailAndUsedFalse(String email);

    void deleteByEmail(String email);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    void deleteByUsedTrue();
}