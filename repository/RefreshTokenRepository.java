package com.example.FeedbackSystem.repository;

import com.example.FeedbackSystem.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    int deleteByUser_userId(int userId);
}
