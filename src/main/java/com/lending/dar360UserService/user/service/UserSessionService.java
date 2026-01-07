package com.lending.dar360UserService.user.service;

import com.lending.dar360UserService.user.dto.LoginRequest;

public interface UserSessionService {
    boolean isLoginAllowed(LoginRequest request);
    void createOrUpdateSession(String loginAttemptId, LoginRequest request);
    void clearSession(String loginAttemptId);
}
