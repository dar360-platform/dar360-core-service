package ae.dar360.user.service;

import ae.dar360.user.dto.LoginRequest;

public interface UserSessionService {
    boolean isLoginAllowed(LoginRequest request);
    void createOrUpdateSession(String loginAttemptId, LoginRequest request);
    void clearSession(String loginAttemptId);
}
