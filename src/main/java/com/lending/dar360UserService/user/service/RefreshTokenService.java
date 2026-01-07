package com.lending.dar360UserService.user.service;

import com.lending.dar360UserService.user.dto.RefreshTokenDto;
import com.lending.dar360UserService.user.model.RefreshToken;

public interface RefreshTokenService {

    RefreshTokenDto findByToken(String token);

    RefreshToken save(RefreshTokenDto refreshToken);

    Integer deleteByToken(String token);

    Integer deleteByUserId(String userId);
}
