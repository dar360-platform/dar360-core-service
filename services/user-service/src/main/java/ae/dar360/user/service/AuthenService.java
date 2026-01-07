package ae.dar360.user.service;

import ae.dar360.user.dto.LoginRequest;
import ae.dar360.user.dto.UserLoginResponse;

import java.util.UUID;

public interface AuthenService {

    String login(LoginRequest loginRequest);

    UserLoginResponse getUserInfoLogin(String email);

    void updateLastLogin(UUID userId);
}
