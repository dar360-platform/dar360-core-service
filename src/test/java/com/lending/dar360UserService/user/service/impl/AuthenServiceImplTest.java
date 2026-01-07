package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.client.feign.ProductServiceFeign;
import com.lending.dar360UserService.user.client.feign.response.ProductTypeDto;
import com.lending.dar360UserService.user.dto.LoginRequest;
import com.lending.dar360UserService.user.dto.UserLoginResponse;
import com.lending.dar360UserService.user.enums.TokenStatus;
import com.lending.dar360UserService.user.enums.UserStatus;
import com.lending.dar360UserService.user.exception.ValidationException;
import com.lending.dar360UserService.user.model.LoginAttempt;
import com.lending.dar360UserService.user.model.TokenSession;
import com.lending.dar360UserService.user.model.User;
import com.lending.dar360UserService.user.repository.LoginAttemptRepository;
import com.lending.dar360UserService.user.repository.LoginFailHistoryRepository;
import com.lending.dar360UserService.user.repository.TokenSessionRepository;
import com.lending.dar360UserService.user.repository.UserRepository;

import com.lending.dar360UserService.user.service.UserSessionService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthenServiceImplTest {
  @InjectMocks private AuthenServiceImpl authenService;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private LoginFailHistoryRepository loginFailHistoryRepository;

  @Mock private TokenSessionRepository tokenSessionRepository;
    @Mock
    private UserSessionService userSessionService;

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private ProductServiceFeign productServiceFeign;

    private final UUID userId = UUID.randomUUID();

    @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(authenService, "maxLoginFail", 5);
    ReflectionTestUtils.setField(authenService, "productServiceEnabled", true);
  }

    @Test
    public void testLoginSuccess() {
         

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("password");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setLock(false);
        user.setPassword("password");

        Mockito.when(this.loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(Mockito.any(UUID.class), Mockito.any()))
                .thenReturn(3);

        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(loginRequest.getEmail(), UserStatus.DELETE.getValue()))
                .thenReturn(Optional.of(user));

        Mockito.when(this.passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .thenReturn(true);

        Mockito.when(this.loginAttemptRepository.save(Mockito.any(LoginAttempt.class)))
                .thenReturn(null);

        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
                .thenReturn(true);
        this.authenService.login(loginRequest);

        Mockito.verify(this.userRepository, Mockito.times(1)).save(user);
        Mockito.verify(this.loginAttemptRepository, Mockito.times(1)).save(Mockito.any(LoginAttempt.class));
    }



    @Test
  public void testLogin_statusInactive() {
         

        LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("admin@gmail.com");
    loginRequest.setPassword("password");
    User user = new User();
    user.setEmail("admin@gmail.com");
    user.setStatus(UserStatus.INACTIVE.getValue());
    user.setLock(false);
    user.setPassword("password");
    Mockito.when(this.loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(Mockito.any(UUID.class), Mockito.any())).thenReturn(3);
    Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(loginRequest.getEmail(), UserStatus.DELETE.getValue())).thenReturn(Optional.of(user));
    Mockito.when(this.passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
    assertThrows(ValidationException.class, () -> this.authenService.login(loginRequest));
  }

  @Test
  public void testLoginFail() {
       

      LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("admin@gmail.com");
    loginRequest.setPassword("password");
    User user = new User();
    user.setEmail("admin@gmail.com");
    user.setStatus(UserStatus.ACTIVE.getValue());
    user.setLock(false);
    user.setPassword("password");
    Mockito.when(
            this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                loginRequest.getEmail(), UserStatus.DELETE.getValue()))
        .thenReturn(Optional.empty());
    Assert.assertThrows(ValidationException.class, () -> this.authenService.login(loginRequest));
  }

  @Test
  public void testLoginWithUserLock() {
       

      LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("admin@gmail.com");
    loginRequest.setPassword("password");
    User user = new User();
    user.setEmail("admin@gmail.com");
    user.setStatus(UserStatus.ACTIVE.getValue());
    user.setLock(true);
    user.setPassword("password");
    Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                loginRequest.getEmail(), UserStatus.DELETE.getValue()))
        .thenReturn(Optional.of(user));
    Assert.assertThrows(ValidationException.class, () ->this.authenService.login(loginRequest));
  }

    @Test
    public void testLoginWithUserInActive() {
         

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("password");
        User user = new User();
        user.setEmail("admin@gmail.com");
        user.setStatus(UserStatus.INACTIVE.getValue());
        user.setLock(true);
        user.setPassword("password");
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                        loginRequest.getEmail(), UserStatus.DELETE.getValue()))
                .thenReturn(Optional.of(user));
        Assert.assertThrows(ValidationException.class, () ->this.authenService.login(loginRequest));
    }

    @Test
    public void testLoginFailWrongPassword() {
         

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("password");
        User user = new User();
        user.setEmail("admin@gmail.com");
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setLock(false);
        user.setPassword("password");
        Mockito.when(
                        this.loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(
                                Mockito.any(UUID.class), Mockito.any()))
                .thenReturn(3);
        Mockito.when(
                        this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                                loginRequest.getEmail(), UserStatus.DELETE.getValue()))
                .thenReturn(Optional.of(user));
        Mockito.when(this.passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .thenReturn(false);
        Assert.assertThrows(ValidationException.class, () ->this.authenService.login(loginRequest));
    }

  @Test
  public void testUserInfoLogin() {
       
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("admin@gmail.com");
    loginRequest.setPassword("password");
    User user = new User();
    String mail = "admin@gmail.com";
    user.setEmail(mail);
    user.setStatus(UserStatus.ACTIVE.getValue());
    user.setLock(false);
    user.setPassword("password");
    user.setProductType("id1,id2");
    ProductTypeDto productType1 = ProductTypeDto.builder()
            .id("id1")
            .code("code1")
            .isActive(true)
            .build();
    ProductTypeDto productType2 = ProductTypeDto.builder()
            .id("id2")
            .code("code2")
            .isActive(true)
            .build();

    List<ProductTypeDto> productTypes = Arrays.asList(productType1, productType2);
    when(productServiceFeign.getProductTypes(null)).thenReturn(productTypes);

    Mockito.when(
            this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                loginRequest.getEmail(), UserStatus.DELETE.getValue()))
        .thenReturn(Optional.of(user));
    UserLoginResponse u = this.authenService.getUserInfoLogin(loginRequest.getEmail());
    Assert.assertNotNull(u);
    Assert.assertEquals(2, u.getProductTypes().size());
    Assert.assertEquals("code1", u.getProductTypes().get(0));
  }


    @Test
    public void testUserInfoLogin_noProductType() {
         
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("password");
        User user = new User();
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setLock(false);
        user.setPassword("password");
        user.setProductType("id1,id2");
        when(productServiceFeign.getProductTypes(null)).thenReturn(null);

        Mockito.when(
                this.userRepository.findByEmailIgnoreCaseAndStatusNot(
                    loginRequest.getEmail(), UserStatus.DELETE.getValue()))
            .thenReturn(Optional.of(user));
        UserLoginResponse u = this.authenService.getUserInfoLogin(loginRequest.getEmail());
        Assert.assertNotNull(u);
    }

  @Test
  public void testUserInfoLoginFail() {
       
      LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("admin@gmail.com");
    loginRequest.setPassword("password");
    Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(loginRequest.getEmail(), UserStatus.DELETE.getValue()))
        .thenReturn(Optional.empty());
    String email = loginRequest.getEmail();
    Assert.assertThrows(
        ValidationException.class,
        () -> this.authenService.getUserInfoLogin(email));
  }

  @Test
  public void testUserInfoLogin_productServiceThrows() {
    String mail = "admin@gmail.com";
    User user = new User();
    user.setEmail(mail);
    user.setStatus(UserStatus.ACTIVE.getValue());
    user.setPassword("password");
    user.setProductType("id1");

    when(userRepository.findByEmailIgnoreCaseAndStatusNot(mail, UserStatus.DELETE.getValue()))
        .thenReturn(Optional.of(user));
    when(productServiceFeign.getProductTypes(null)).thenThrow(new RuntimeException("boom"));

    UserLoginResponse response = authenService.getUserInfoLogin(mail);
    Assert.assertNotNull(response);
    Assert.assertNotNull(response.getProductTypes());
    Assert.assertTrue(response.getProductTypes().isEmpty());
  }

    @Test
    public void testUpdateLastLoginSuccess() {
         

        UUID userId  = UUID.randomUUID();
        Mockito.when(this.userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
    Mockito.when(
            this.tokenSessionRepository.findByuserIdAndStatus(
                    userId, TokenStatus.ACTIVE.getValue()))
        .thenReturn(Optional.of(new TokenSession()));
        this.authenService.updateLastLogin(userId);
      verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateLastLoginFail() {
         

        UUID userId  = UUID.randomUUID();
        Mockito.when(this.userRepository.findById(userId))
                .thenReturn(Optional.empty());
       Assert.assertThrows(ValidationException.class, () -> this.authenService.updateLastLogin(userId) ) ;
    }

    @Test
    public void login_shouldThrowException_whenUserNotFound() {
        // given
         

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.empty());

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
    }

    @Test
    public void login_shouldThrowException_whenUserIsLocked() {
        // given
         

        User user = new User();
        user.setId(userId);
        user.setLock(true);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
    }

    @Test
    public void login_shouldThrowException_whenUserIsInactive() {
        // given
         

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.INACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
    }

    @Test
    public void login_shouldThrowException_whenPasswordNotMatch_andLockAfterMaxFail() {
        // given
         

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true); // Simulate session check
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(UUID.class), any()))
            .thenReturn(5); // max failed

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));

        // userRepository.save is called twice: 1st time at start, 2nd after lock
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    public void login_shouldThrowException_If_FoundActiveSession() {
        // given


        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(false); // Simulate session check
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(UUID.class), any()))
            .thenReturn(5); // max failed

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));

        // userRepository.save is called twice: 1st time at start, 2nd after lock
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void login_shouldThrowException_whenPasswordNotMatch_andLoginFailRemaining() {
        // given
         

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(UUID.class), any()))
            .thenReturn(2); // less than max fail

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));

        verify(userRepository, times(1)).save(any(User.class)); // only save once at start
    }

    @Test
    public void login_shouldReturnToken_whenPasswordMatch() {
        // given
         

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("correctPassword");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true);
        // Mock createLoginAttempt logic
        when(loginAttemptRepository.save(any())).thenAnswer(invocation -> {
            LoginAttempt attempt = invocation.getArgument(0);
            attempt.setId(UUID.randomUUID().toString());
            return attempt;
        });

        String token = authenService.login(loginRequest);

        assertNotNull(token);
    }
    @Test
    public void login_shouldThrowException_whenSsoEnabled_andPasswordNotMatch() {

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("correctPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(UUID.class), any()))
            .thenReturn(1);

        // when & then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
    }

    @Test
    public void login_shouldReturnToken_whenSsoEnabled_andPasswordMatch() {

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("correctPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("correctPassword");
        loginRequest.setSsoEnabled(true);

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true);
        // Mock createLoginAttempt
        when(loginAttemptRepository.save(any())).thenAnswer(invocation -> {
            LoginAttempt attempt = invocation.getArgument(0);
            attempt.setId(UUID.randomUUID().toString());
            return attempt;
        });

        // when
        String token = authenService.login(loginRequest);

        // then
        assertNotNull(token);
    }

    @Test
    public void login_shouldFail_whenSsoEnabled_andPasswordIsNull() {

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("correctPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword(null);
        loginRequest.setSsoEnabled(true);

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true);
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(), any()))
            .thenReturn(4);

        // when/then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
        verify(loginFailHistoryRepository, times(1)).save(any());
    }

    @Test
    public void login_shouldFail_whenSsoEnabled_andPasswordDoesNotMatch() {

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("correctPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");
        loginRequest.setSsoEnabled(true);

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true);
        when(loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(any(), any()))
            .thenReturn(4);

        // when/then
        assertThrows(ValidationException.class, () -> authenService.login(loginRequest));
        verify(loginFailHistoryRepository, times(1)).save(any());
    }

    @Test
    public void login_shouldReturnToken_whenNonSsoEnabled_andPasswordMatch() {

        User user = new User();
        user.setId(userId);
        user.setLock(false);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setPassword("encodedPassword");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("correctPassword");
        loginRequest.setSsoEnabled(false);

        when(userRepository.findByEmailIgnoreCaseAndStatusNot(anyString(), anyInt()))
            .thenReturn(Optional.of(user));
        when(userSessionService.isLoginAllowed(any(LoginRequest.class)))
            .thenReturn(true);
        when(passwordEncoder.matches("correctPassword", "encodedPassword"))
            .thenReturn(true);
        when(loginAttemptRepository.save(any())).thenAnswer(invocation -> {
            LoginAttempt attempt = invocation.getArgument(0);
            attempt.setId(UUID.randomUUID().toString());
            return attempt;
        });

        // when
        String token = authenService.login(loginRequest);

        // then
        assertNotNull(token);
        verify(passwordEncoder, times(1)).matches("correctPassword", "encodedPassword");
    }
}
