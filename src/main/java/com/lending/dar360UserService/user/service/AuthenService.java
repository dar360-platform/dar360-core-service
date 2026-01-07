package com.lending.dar360UserService.user.service;

import com.lending.dar360UserService.user.dto.LoginRequest;
import com.lending.dar360UserService.user.dto.UserLoginResponse;

import java.util.UUID;

public interface AuthenService {

    String login(LoginRequest loginRequest);

    UserLoginResponse getUserInfoLogin(String email);

    void updateLastLogin(UUID userId);
}
