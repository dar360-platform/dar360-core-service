package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rs WHERE rs.token = ?1")
    Integer deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rs WHERE rs.userId = ?1")
    Integer deleteByUserId(String userId);
}
