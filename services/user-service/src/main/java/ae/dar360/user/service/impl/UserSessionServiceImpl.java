package ae.dar360.user.service.impl;

import ae.dar360.user.dto.LoginRequest;
import ae.dar360.user.model.UserSessionInfo;
import ae.dar360.user.repository.UserSessionInfoRepository;
import ae.dar360.user.service.UserSessionService;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {
    @Value("${app.user-session-timeout-minutes}")
    private  long sessionTimeoutMinutes;
    private final UserSessionInfoRepository userSessionInfoRepository;

    public boolean isLoginAllowed(LoginRequest request) {
        Optional<UserSessionInfo> existingSession = userSessionInfoRepository.findByEmail(
            request.getEmail());
        if (existingSession.isEmpty()) {
            return true;
        }

        UserSessionInfo session = existingSession.get();
        // Check if session has timed out
        if (ChronoUnit.MINUTES.between(session.getLastActive(), OffsetDateTime.now()) > sessionTimeoutMinutes) {
            userSessionInfoRepository.delete(session);
            return true;
        }
        //for sso
        if(request.isSsoEnabled()){
            userSessionInfoRepository.delete(session);
            return true;
        }

        // Make it null-safe
        String sessionBrowserName = session.getBrowserName();
        String sessionOsVersion = session.getOsVersion();
        String requestBrowserName = request.getBrowserName();
        String requestOsVersion = request.getOsVersion();

        if (sessionBrowserName == null || sessionOsVersion == null || requestBrowserName == null || requestOsVersion == null) {
            log.warn("Missing client info on login: sessionBrowserName={}, sessionOsVersion={}, requestBrowserName={}, requestOsVersion={}",
                     sessionBrowserName, sessionOsVersion, requestBrowserName, requestOsVersion);
            return true; // Policy: allow if info is missing
        }

        return sessionBrowserName.equals(requestBrowserName) && sessionOsVersion
            .equals(requestOsVersion) && session.isPrivateMode()==request.isPrivateMode();
    }

    public void createOrUpdateSession(String loginAttemptId, LoginRequest request) {
        log.info("Creating or updating user session for loginAttemptId={}", loginAttemptId);
        try {
            Optional<UserSessionInfo> existingSession = userSessionInfoRepository.findByEmail(
                request.getEmail());
            UserSessionInfo session = existingSession.orElseGet(UserSessionInfo::new);
            session.setEmail(request.getEmail());
            session.setBrowserName(request.getBrowserName());
            session.setOsVersion(request.getOsVersion());
            session.setLoginAttemptId(loginAttemptId);
            session.setPrivateMode(request.isPrivateMode());
            session.setLastActive(OffsetDateTime.now());
            if (session.getId() == null) {
                session.setId(UUID.randomUUID());
            }

            userSessionInfoRepository.save(session);
        } catch (Exception e) {
            log.error("Error to save user session! {}", e.getMessage());
        }
    }

    public void clearSession(String loginAttemptId) {
        Optional<UserSessionInfo> existingSession = userSessionInfoRepository.findByLoginAttemptId(loginAttemptId);
        existingSession.ifPresent(userSessionInfoRepository::delete);
    }
}
