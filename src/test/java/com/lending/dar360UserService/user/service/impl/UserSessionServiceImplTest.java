package com.lending.dar360UserService.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lending.dar360UserService.user.dto.LoginRequest;
import com.lending.dar360UserService.user.model.UserSessionInfo;
import com.lending.dar360UserService.user.repository.UserSessionInfoRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class UserSessionServiceImplTest {
    @Mock
    UserSessionInfoRepository userSessionInfoRepository;

    @InjectMocks
    UserSessionServiceImpl userSessionService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userSessionService, "sessionTimeoutMinutes", 5);

    }

    @Test
    public void isLoginAllowed_noExistingSession_allowed() {
        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setBrowserName("Chrome");
        request.setOsVersion("Windows");
        request.setPrivateMode(false);

        boolean allowed = userSessionService.isLoginAllowed(request);

        assertTrue(allowed);
    }

    @Test
    public void isLoginAllowed_sessionNotExpired_sameBrowserAndOs_notAllowed() {
        UserSessionInfo session = new UserSessionInfo();
        session.setEmail("test@example.com");
        session.setBrowserName("Chrome");
        session.setOsVersion("Windows");
        session.setPrivateMode(false);
        session.setLastActive(OffsetDateTime.now().minusMinutes(2));

        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.of(session));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setBrowserName("Chrome");
        request.setOsVersion("Windows");
        request.setPrivateMode(false);

        boolean allowed = userSessionService.isLoginAllowed(request);

        assertTrue(allowed);
    }

    @Test
    public void isLoginAllowed_sessionNotExpired_differentBrowser_notAllowed() {
        UserSessionInfo session = new UserSessionInfo();
        session.setEmail("test@example.com");
        session.setBrowserName("Firefox");
        session.setOsVersion("Windows");
        session.setPrivateMode(false);
        session.setLastActive(OffsetDateTime.now().minusMinutes(6));

        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.of(session));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setBrowserName("Chrome");
        request.setOsVersion("Windows");
        request.setPrivateMode(false);

        boolean allowed = userSessionService.isLoginAllowed(request);

        verify(userSessionInfoRepository).delete(session);
        assertTrue(allowed);
    }

    @Test
    public void createOrUpdateSession_existingSession_updated() {
        UserSessionInfo session = new UserSessionInfo();
        session.setEmail("test@example.com");
        session.setBrowserName("Chrome");
        session.setOsVersion("Windows");
        session.setPrivateMode(false);

        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.of(session));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setBrowserName("Firefox");
        request.setOsVersion("Linux");
        request.setPrivateMode(true);
        userSessionService.createOrUpdateSession("loginAttempt123", request);

        verify(userSessionInfoRepository).save(session);
        assertEquals("Firefox", session.getBrowserName());
        assertEquals("Linux", session.getOsVersion());
        assertTrue(session.isPrivateMode());
        assertEquals("loginAttempt123", session.getLoginAttemptId());
    }

    @Test
    public void createOrUpdateSession_noExistingSession_created() {
        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setBrowserName("Chrome");
        request.setOsVersion("Windows");
        request.setPrivateMode(false);
        userSessionService.createOrUpdateSession("loginAttempt123", request);

        verify(userSessionInfoRepository).save(any(UserSessionInfo.class));
    }

    @Test
    public void clearSession_noExistingSession_noAction() {
        when(userSessionInfoRepository.findByLoginAttemptId("loginAttempt123")).thenReturn(Optional.empty());

        userSessionService.clearSession("loginAttempt123");

        verify(userSessionInfoRepository, never()).delete(any());
    }

    @Test
    public void clearSession_existingSession_deleted() {
        UserSessionInfo session = new UserSessionInfo();
        session.setLoginAttemptId("loginAttempt123");

        when(userSessionInfoRepository.findByLoginAttemptId("loginAttempt123")).thenReturn(Optional.of(session));

        userSessionService.clearSession("loginAttempt123");

        verify(userSessionInfoRepository).delete(session);
    }

    @Test
    public void testIsLoginAllowed_withSsoEnabledAndExistingSession() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setSsoEnabled(true);

        UserSessionInfo session = new UserSessionInfo();
        session.setEmail("test@example.com");
        session.setLastActive(OffsetDateTime.now().minusMinutes(2));

        when(userSessionInfoRepository.findByEmail("test@example.com")).thenReturn(Optional.of(session));

        // Act
        boolean allowed = userSessionService.isLoginAllowed(request);

        // Assert
        verify(userSessionInfoRepository).delete(session);
        assertTrue(allowed);
    }
}