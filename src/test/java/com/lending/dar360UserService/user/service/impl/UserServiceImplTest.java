package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.client.feign.NotificationServiceFeign;
import com.lending.dar360UserService.user.dto.*;
import com.lending.dar360UserService.user.enums.TokenStatus;
import com.lending.dar360UserService.user.enums.TokenTypeEnum;
import com.lending.dar360UserService.user.enums.UserStatus;
import com.lending.dar360UserService.user.exception.ValidationException;
import com.lending.dar360UserService.user.mapper.UseResponseMapper;
import com.lending.dar360UserService.user.mapper.UserListResponseMapper;
import com.lending.dar360UserService.user.mapper.UserRegisterMapper;
import com.lending.dar360UserService.user.model.Department;
import com.lending.dar360UserService.user.model.PasswordChangeHistory;
import com.lending.dar360UserService.user.model.Role;
import com.lending.dar360UserService.user.model.TokenSession;
import com.lending.dar360UserService.user.model.User;
import com.lending.dar360UserService.user.repository.*;
import com.lending.dar360UserService.user.service.UserSessionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.lending.dar360UserService.user.model.LoginAttempt;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private TokenSessionRepository tokenSessionRepository;
    @Mock
    private UserRegisterMapper userRegisterMapper;
    @Mock
    private UseResponseMapper useResponseMapper;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordChangeHistoryRepository passwordChangeHistoryRepository;
    @Mock
    private PasswordDictionaryRepository passwordDictionaryRepository;
    @Mock
    private UserListResponseMapper userListResponseMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private NotificationServiceFeign notificationServiceFeign;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "tokenSessionDuration", 3600L);
        ReflectionTestUtils.setField(userService, "urlSetUpPassword", "123");
        ReflectionTestUtils.setField(userService, "urlForgotPassword", "123");
        ReflectionTestUtils.setField(userService, "lastLoginExpireInactive", 60L);
        ReflectionTestUtils.setField(userService, "lastLoginExpireWarning", 45L);
        ReflectionTestUtils.setField(userService, "passwordExpiredInactive", 90L);
        ReflectionTestUtils.setField(userService, "passwordExpiredSendNotification", List.of(75L));

    }

    @Test
    public void testCreateUserSuccess() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EmployeeId");
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmailIgnoreCase("admin@gmail.com"))
                .thenReturn(Optional.empty());
        Mockito.when(this.departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(this.userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Assert.assertNotNull(this.userService.createUser(request));
    }

    @Test
    public void testCreateUserEmailExist() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmailIgnoreCase("admin@gmail.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(this.departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.createUser(request));
    }

    @Test
    public void testCreateUserEmployeeIdExisted() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EmployeeId");
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmployeeIdIgnoreCase("EmployeeId"))
                .thenReturn(Optional.of(user));
        Mockito.when(this.departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.createUser(request));
    }

    @Test
    public void testCreateUserDepartmentNotExist() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmailIgnoreCase("admin@gmail.com"))
                .thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.createUser(request));
    }

    @Test
    public void testCreateUserSuccessUserLatestNotNulL() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");

        User userLatest = new User();
        userLatest.setId(UUID.randomUUID());
        userLatest.setCode("IB_2024_000001");
        userLatest.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmailIgnoreCase("admin@gmail.com"))
                .thenReturn(Optional.empty());
        Mockito.when(this.departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(this.userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc())
                .thenReturn(Optional.of(userLatest));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Assert.assertNotNull(this.userService.createUser(request));
    }

    @Test
    public void testCreateUserSuccessUserLatestLastYear() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("admin@gmail.com");
        request.setMobile("+9718987678");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");

        User userLatest = new User();
        userLatest.setId(UUID.randomUUID());
        userLatest.setCode("IB_2023_000001");
        userLatest.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findByEmailIgnoreCase("admin@gmail.com"))
                .thenReturn(Optional.empty());
        Mockito.when(this.departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(this.userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc())
                .thenReturn(Optional.of(userLatest));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Assert.assertNotNull(this.userService.createUser(request));
    }

    @Test
    public void testRegenerateTokenSessionSuccess() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setUserId(UUID.randomUUID());
        tokenSession.setType(TokenTypeEnum.FORGOT.getValue());
        tokenSession.setExpiryDate(OffsetDateTime.now());
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        User user = new User();
        user.setEmail("admin@gmail.com");
        Mockito.when(userRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(user));
        this.userService.regenerateTokenSession(token);
        verify(tokenSessionRepository, times(1)).save(tokenSession);
    }

    @Test
    public void testRegenerateTokenSessionNotFound() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.regenerateTokenSession(token));
    }

    @Test
    public void testRegenerateTokenSessionNotExpired() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(100));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        assertThrows(
                ValidationException.class, () -> this.userService.regenerateTokenSession(token));
    }

    @Test
    public void testValidateTokenSession() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.validateTokenSession(token));
    }

    @Test
    public void testValidateTokenSessionSuccess() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        this.userService.validateTokenSession(token);
    }

    @Test
    public void testValidateTokenSessionExpired() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().minusMinutes(10));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        assertThrows(
                ValidationException.class, () -> this.userService.validateTokenSession(token));
    }

    @Test
    public void testValidateTokenSessionExpired2() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setStatus(TokenStatus.INACTIVE.getValue());
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(10));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        assertThrows(
                ValidationException.class, () -> this.userService.validateTokenSession(token));
    }

    @Test
    public void testValidateTokenSessionExpired3() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setVerifyTimes(5);
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(10));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        assertThrows(
                ValidationException.class, () -> this.userService.validateTokenSession(token));
    }


    @Test
    public void testIncreaseTokenVerifyTimes_userNotFound() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.increaseTokenVerifyTimes(token));
    }

    @Test
    public void testIncreaseTokenVerifyTimes_success() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().minusMinutes(10));
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        this.userService.increaseTokenVerifyTimes(token);
        Mockito.verify(tokenSessionRepository).save(tokenSession);
    }

    @Test
    public void testIncreaseTokenVerifyTimes_successTokenAttempted() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().minusMinutes(10));
        tokenSession.setVerifyTimes(1);
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        this.userService.increaseTokenVerifyTimes(token);
        Mockito.verify(tokenSessionRepository).save(tokenSession);
    }

    @Test
    public void testSetupPasswordSuccess() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        User user = new User();
        user.setFullName("");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        this.userService.setupPassword(setupPasswordDto);
        Mockito.verify(tokenSessionRepository).save(tokenSession);
    }

    @Test
    public void testSetupPasswordContainDictionary() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(true);
        assertThrows(
                ValidationException.class, () -> this.userService.setupPassword(setupPasswordDto));
    }

    @Test
    public void testSetupPasswordUserNotFound() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        User user = new User();
        user.setFullName("admin");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.setupPassword(setupPasswordDto));
    }

    @Test
    public void testSetupPasswordContainFullName() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("admin!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(tokenSessionRepository.findByToken(Mockito.anyString()))
                .thenReturn(Optional.of(tokenSession));
        User user = new User();
        user.setFullName("admin");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        assertThrows(
                ValidationException.class, () -> this.userService.setupPassword(setupPasswordDto));
    }

    @Test
    public void testUnlockSuccess() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("");
        user.setEmail("admin@gmail.com");
        user.setLock(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        this.userService.unlockUser(userId);
        verify(tokenSessionRepository, times(1)).save(any(TokenSession.class));
    }

    @Test
    public void testUnlockUserNotFound() {
        String userId = UUID.randomUUID().toString();
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.unlockUser(userId));
    }

    @Test
    public void testUnlockUserNotLock() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("");
        user.setLock(false);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        assertThrows(
                ValidationException.class, () -> this.userService.unlockUser(userId));
    }


    @Test
    public void testGetUserByEmailFail() {
        String email = "admin@gmail.com";
        User user = new User();
        user.setFullName("");
        user.setLock(false);
        Mockito.when(userRepository
                .findByEmailIgnoreCase(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.getUserByEmail(email));
    }

    @Test
    public void testGetUserByEmailSuccess() {
        String email = "admin@gmail.com";
        User user = new User();
        user.setFullName("");
        user.setProductType("p1,p2");
        user.setApprovalLevel("L1,L2");
        user.setLock(false);
        Mockito.when(userRepository
                .findByEmailIgnoreCase(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(useResponseMapper.toDto(Mockito.any())).thenReturn(new UserResponse());
        Assert.assertNotNull(this.userService.getUserByEmail(email));
    }

    @Test
    public void testGetUserByEmailSuccess2() {
        String email = "admin@gmail.com";
        User user = new User();
        user.setFullName("");
        user.setProductType("p1,p2");
        user.setApprovalLevel("L1,L2");
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("IT");
        user.setDepartment(department);
        user.setLock(false);
        Mockito.when(userRepository
                .findByEmailIgnoreCase(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(useResponseMapper.toDto(Mockito.any())).thenReturn(new UserResponse());
        Assert.assertNotNull(this.userService.getUserByEmail(email));
    }

    @Test
    public void testGetUserByEmailSuccess3() {
        String email = "admin@gmail.com";
        User user = new User();
        user.setFullName("");
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("IT");
        user.setDepartment(department);
        user.setLock(false);
        Mockito.when(userRepository
                .findByEmailIgnoreCase(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(useResponseMapper.toDto(Mockito.any())).thenReturn(new UserResponse());
        Assert.assertNotNull(this.userService.getUserByEmail(email));
    }

    @Test
    public void testGetUserByIdNotFound() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("");
        user.setLock(false);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(
                ValidationException.class, () -> this.userService.getUserById(userId));
    }

    @Test
    public void testGetUserByIdSuccess() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setFullName("");
        user.setProductType("p1,p2");
        user.setApprovalLevel("L1,L2");
        user.setLock(false);
        Mockito.when(userRepository
                .findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(useResponseMapper.toDto(Mockito.any())).thenReturn(new UserResponse());
        Assert.assertNotNull(this.userService.getUserById(userId));
    }

    @Test
    public void testValidateChangePasswordSuccess() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Page<PasswordChangeHistory> passwordChangeHistories = Mockito.mock(Page.class);
        Mockito.when(passwordChangeHistoryRepository.findByUserId(Mockito.any(), Mockito.any()))
                .thenReturn(passwordChangeHistories);
        User user = new User();
        user.setFullName("");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        this.userService.validateChangePassword(setupPasswordDto);
    }

    @Test
    public void testValidateChangePassword_userNotFound() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Page<PasswordChangeHistory> passwordChangeHistories = Mockito.mock(Page.class);
        Mockito.when(passwordChangeHistoryRepository.findByUserId(Mockito.any(), Mockito.any()))
                .thenReturn(passwordChangeHistories);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.validateChangePassword(setupPasswordDto));
    }

    @Test
    public void testChangePasswordSuccess() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Page<PasswordChangeHistory> passwordChangeHistories = Mockito.mock(Page.class);
        Mockito.when(passwordChangeHistoryRepository.findByUserId(Mockito.any(), Mockito.any()))
                .thenReturn(passwordChangeHistories);
        User user = new User();
        user.setFullName("");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        this.userService.changePassword(setupPasswordDto);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testChangePassword_userNotFound() {
        String token = Base64.getEncoder().encodeToString("test".getBytes());
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        setupPasswordDto.setToken(token);
        TokenSession tokenSession = new TokenSession();
        tokenSession.setToken("test");
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(1000));
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        Page<PasswordChangeHistory> passwordChangeHistories = Mockito.mock(Page.class);
        Mockito.when(passwordChangeHistoryRepository.findByUserId(Mockito.any(), Mockito.any()))
                .thenReturn(passwordChangeHistories);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.changePassword(setupPasswordDto));
    }

    @Test
    public void testChangePasswordContainOldHistory() {
        PasswordDto setupPasswordDto = new PasswordDto();
        setupPasswordDto.setPassword("ZAQ!202022");
        Mockito.when(passwordDictionaryRepository.containDictionaryWord(Mockito.anyString()))
                .thenReturn(false);
        PasswordChangeHistory passwordChangeHistory = new PasswordChangeHistory();
        passwordChangeHistory.setHashedPassword("12121212");
        Page<PasswordChangeHistory> passwordChangeHistories =
                new PageImpl<>(List.of(passwordChangeHistory));
        Mockito.when(passwordChangeHistoryRepository.findByUserId(Mockito.any(), Mockito.any()))
                .thenReturn(passwordChangeHistories);
        User user = new User();
        user.setFullName("");
        user.setPassword("12121212");
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        this.userService.changePassword(setupPasswordDto);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testSearchUserSuccess() {
        SearchUserForm searchUserForm = new SearchUserForm();
        searchUserForm.setPageNumber(1);
        searchUserForm.setPageSize(20);
        User user = new User();
        user.setCode("12121");
        user.setEmail("121212");
        user.setStatus(UserStatus.ACTIVE.getValue());
        List<User> users = List.of(user);
        Page<User> userPaging = new PageImpl<>(users);
        Mockito.when(userRepository.findAll(Mockito.any(UserSpecification.class), Mockito.any(Pageable.class)))
                .thenReturn(userPaging);
        assertNotNull(this.userService.searchUser(searchUserForm));
    }

    @Test
    public void testGetFullnameAndDepartmentNameSuccess() {
        UserDepartmentRequest request = new UserDepartmentRequest();
        request.setUserIds(new ArrayList<>());
        SearchUserForm searchUserForm = new SearchUserForm();
        searchUserForm.setPageNumber(1);
        searchUserForm.setPageSize(20);
        User user = new User();
        user.setCode("12121");
        user.setEmail("121212");
        user.setDepartment(new Department());
        user.setStatus(UserStatus.ACTIVE.getValue());
        List<User> users = List.of(user);
        Mockito.when(userRepository.findAllById(Mockito.any()))
                .thenReturn(users);
        assertNotNull(this.userService.getFullnameAndDepartmentName(request));
    }

    @Test
    public void testGetEmailsByLevel1() {
        Mockito.when(userRepository
                .findEmailsByApprovalLevelLikeIgnoreCase(Mockito.anyString())).thenReturn(List.of("a"));
        Assert.assertNotNull(this.userService.getEmailsByLevel("d"));
    }

    @Test
    public void testUpdateUserSuccess() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("John Doe"),
                Optional.of("EmployeeId"),
                Optional.of(departmentId),
                Optional.<Set<String>>empty(),
                Optional.of("admin@gmail.com"),
                Optional.empty(),
                Optional.of("+971501234567")
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("admin@gmail.com");
        Department department = new Department();
        department.setId(departmentId);
        department.setName("IT Department");
        Mockito.when(this.userRepository
                        .findById(Mockito.any()))
                .thenReturn(Optional.of(user));
        Mockito.when(this.departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(department));
        Mockito.when(this.userRepository.existsByEmailIgnoreCaseAndIdNot(Mockito.anyString(), Mockito.any()))
                .thenReturn(false);

        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());
        Mockito.when(this.useResponseMapper.toDto(Mockito.any(User.class))).thenReturn(new UserResponse());
        Mockito.when(this.userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(Collections.emptyList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserResponse response = this.userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateUserEmailExists() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("John Doe"),
                Optional.of("EmployeeId"),
                Optional.of(departmentId),
                Optional.<Set<String>>empty(),
                Optional.of("admin@gmail.com"),
                Optional.empty(),
                Optional.of("+971501234567")
        );
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("existing-email@gmail.gmail.com");
        Mockito.when(this.userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));
        Mockito.when(this.userRepository.existsByEmailIgnoreCaseAndIdNot(request.email().get(), userId))
                .thenReturn(true);
        assertThrows(ValidationException.class, () -> this.userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUserEmployeeIdExists() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("John Doe"),
                Optional.of("EmployeeId"),
                Optional.of(departmentId),
                Optional.<Set<String>>empty(),
                Optional.of("admin@gmail.com"),
                Optional.empty(),
                Optional.of("+971501234567")
        );
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmployeeId("EmployeeId");
        existingUser.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));
        Mockito.when(this.userRepository.existsByEmailIgnoreCaseAndIdNot(request.email().get(), userId))
                .thenReturn(false);
        Mockito.when(this.userRepository.existsByEmployeeIdIgnoreCaseAndIdNot(request.employeeId().get(), userId))
                .thenReturn(true);
        assertThrows(ValidationException.class, () -> this.userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUserEmployeeIdNotExists() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("John Doe"),
                Optional.of("NewEmployeeId"),
                Optional.of(departmentId),
                Optional.<Set<String>>empty(),
                Optional.of("admin@gmail.com"),
                Optional.empty(),
                Optional.of("+971501234567")
        );

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmployeeId("ExistingEmployeeId");
        existingUser.setEmail("admin@gmail.com");
        Department department = new Department();
        department.setId(departmentId);
        department.setName("IT Department");
        Mockito.when(this.userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));
        Mockito.when(this.userRepository.existsByEmailIgnoreCaseAndIdNot(request.email().get(), userId))
                .thenReturn(false);
        Mockito.when(this.userRepository.existsByEmployeeIdIgnoreCaseAndIdNot(request.employeeId().get(), userId))
                .thenReturn(false);
        Mockito.when(this.departmentRepository.findById(departmentId))
                .thenReturn(Optional.of(department));
        Mockito.when(this.useResponseMapper.toDto(Mockito.any(User.class))).thenReturn(new UserResponse());
        Mockito.when(this.userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(Collections.emptyList());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertDoesNotThrow(() -> this.userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUserStatusSuccess() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository
                        .findById(Mockito.any()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        this.userService.updateUserStatus(userId, 1);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateUserStatus_userNotFound() {
        String userId = UUID.randomUUID().toString();
        Mockito.when(this.userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> this.userService.updateUserStatus(userId, 1));
    }

    @Test
    public void testUpdateUserStatus_statusIsNull() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        user.setStatus(1);
        Mockito.when(this.userRepository
                        .findById(Mockito.any()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        assertThrows(ValidationException.class, () -> this.userService.updateUserStatus(userId, null));
    }

    @Test
    public void testUpdateUserStatus_statusInvalid() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        Mockito.when(this.userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        assertThrows(ValidationException.class, () -> this.userService.updateUserStatus(userId, 11));
    }

    @Test
    public void testExportAllUserSuccess() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        user.setStatus(1);
        Mockito.when(this.userRepository
                        .findAllByStatusIsNotOrderByModifiedDateDesc(2))
                .thenReturn(List.of(user));
        assertNotNull(this.userService.getAllUserExport());
    }

    @Test
    public void testBatchInactiveAccountSuccess() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("admin2@gmail.com");
        user2.setLastLogin(OffsetDateTime.now().minusDays(46));
        user2.setLastUpdatedPassword(OffsetDateTime.now().minusDays(75));
        user2.setStatus(1);
        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setEmail("admin2@gmail.com");
        user3.setLastLogin(OffsetDateTime.now().minusDays(60));
        user3.setStatus(1);
        User user4 = new User();
        user4.setId(UUID.randomUUID());
        user4.setEmail("admin2@gmail.com");
        user4.setLastLogin(OffsetDateTime.now().minusDays(46));
        user4.setLastUpdatedPassword(OffsetDateTime.now().minusDays(90));
        user4.setStatus(1);
        Mockito.when(this.userRepository
                        .findAllByStatus(1))
                .thenReturn(List.of(user, user2, user3, user4));
        this.userService.batchInactiveAccount();
        verify(userRepository, times(1)).saveAll(any());
    }

    @Test
    public void testBatchInactiveAccountSuccess_expired() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@gmail.com");
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("admin2@gmail.com");
        user2.setLastLogin(OffsetDateTime.now().minusDays(200));
        user2.setLastUpdatedPassword(OffsetDateTime.now().minusDays(200));
        user2.setStatus(1);
        User user3 = new User();
        user3.setId(UUID.randomUUID());
        user3.setEmail("admin2@gmail.com");
        user3.setLastLogin(OffsetDateTime.now().minusDays(59));
        user2.setLastUpdatedPassword(OffsetDateTime.now().minusDays(200));
        user3.setStatus(1);
        User user4 = new User();
        user4.setId(UUID.randomUUID());
        user4.setEmail("admin2@gmail.com");
        user4.setLastLogin(OffsetDateTime.now().minusDays(46));
        user4.setLastUpdatedPassword(OffsetDateTime.now().minusDays(90));
        user4.setStatus(1);
        Mockito.when(this.userRepository
                        .findAllByStatus(1))
                .thenReturn(List.of(user, user2, user3, user4));
        this.userService.batchInactiveAccount();
        verify(userRepository, times(1)).saveAll(any());
    }

    @Test
    public void testForgotPasswordSuccess() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(mail, 2))
                .thenReturn(Optional.of(user));
        this.userService.forgotPassword(mail);
        verify(tokenSessionRepository, times(1)).save(any());
    }

    @Test
    public void testForgotPasswordSuccess_withExistingToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(mail, 2)).thenReturn(Optional.of(user));

        TokenSession tokenSession = new TokenSession();
        tokenSession.setUserId(user.getId());
        tokenSession.setExpiryDate(OffsetDateTime.now());
        tokenSession.setToken(UUID.randomUUID().toString());
        tokenSession.setType(TokenTypeEnum.FORGOT.getValue());
        tokenSession.setStatus(TokenStatus.ACTIVE.getValue());
        Mockito.when(this.tokenSessionRepository.findByuserIdAndStatus(any(), any())).thenReturn(Optional.of(tokenSession));
        this.userService.forgotPassword(mail);
        verify(tokenSessionRepository, times(1)).save(any());
    }

    @Test
    public void testForgotPassword_userNotFound() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(mail, 2))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> this.userService.forgotPassword(mail));
    }

    @Test
    public void testForgotPasswordUserLock() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(1);
        user.setLock(true);
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(mail, 2))
                .thenReturn(Optional.of(user));
        assertThrows(
                ValidationException.class, () -> this.userService.forgotPassword(mail));
    }

    @Test
    public void testForgotPasswordInactive() {
        User user = new User();
        user.setId(UUID.randomUUID());
        String mail = "admin@gmail.com";
        user.setEmail(mail);
        user.setLastLogin(OffsetDateTime.now().minusDays(46));
        user.setStatus(0);
        user.setLock(false);
        Mockito.when(this.userRepository.findByEmailIgnoreCaseAndStatusNot(mail, 2))
                .thenReturn(Optional.of(user));
        assertThrows(
                ValidationException.class, () -> this.userService.forgotPassword(mail));
    }

    @Test
    public void testGetEmailsByDepartment1() {
        Mockito.when(userRepository
                .findEmailsByDepartmentLikeIgnoreCase(Mockito.anyString())).thenReturn(List.of("a"));
        Assert.assertNotNull(this.userService.getEmailsByDepartment("d"));
    }

    @Test
    public void testGetEmailsByDepartment2() {
        Assert.assertNotNull(this.userService.getEmailsByDepartment(""));
    }

    @Test
    public void testGetEmailsByLevel2() {
        Assert.assertNotNull(this.userService.getEmailsByLevel(""));
    }

    @Test
    public void getUserByLevel() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userRepository.findUserByApprovalLevelLikeIgnoreCase("L2")).thenReturn(List.of(user));
        when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());
        when(userRoleRepository.findRoleIdsByUserId(any(UUID.class))).thenReturn(Collections.emptyList());

        List<UserResponse> userResponseList = userService.getUserByLevel("L2");

        assertNotNull(userResponseList);
    }

    @Test
    public void getUserByLevel_returnNull() {
        when(userRepository.findUserByApprovalLevelLikeIgnoreCase("L2")).thenReturn(Collections.emptyList());

        List<UserResponse> userResponseList = userService.getUserByLevel("L2");

        assertEquals(0, userResponseList.size());
    }

    @Test
    public void testUpdateUserStatus_deleteUser() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setId(UUID.fromString(userId));
        user.setEmail("hoangle@aprro.com");

        Mockito.when(this.userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        this.userService.updateUserStatus(userId, UserStatus.DELETE.getValue());

        verify(userRepository, times(1)).delete(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testFindByFullNameAndEmail() {
        User user = new User();
        user.setDepartment(new Department());
        when(userRepository.findByFullNameOrEmail(anyString(), anyString(), any(Pageable.class))).thenReturn(
                new PageImpl<>(Collections.singletonList(user)));
        List<UserDepartmentResponse> users = userService.findByFullNameAndEmail("","");
        assertEquals(1, users.size());
    }

    @Test
    public void testFindByFullNameAndEmailNotFound() {
        when(userRepository.findByFullNameOrEmail(anyString(), anyString(), any(Pageable.class))).thenReturn(
                new PageImpl<>(Collections.emptyList()));
        List<UserDepartmentResponse> users = userService.findByFullNameAndEmail("","");
        assertEquals(0, users.size());
    }

    @Test
    public void getEmailsByUserCodes() {
        when(userRepository.findAllByCodeIn(any())).thenReturn(List.of(new User()));
        var userCodes = List.of("userCode");
        userService.getEmailsByUserCodes(userCodes);
        verify(userRepository, times(1)).findAllByCodeIn(userCodes);
    }

    @Test
    public void getEmailsByUserCodes_empty() {
        when(userRepository.findAllByCodeIn(any())).thenReturn(new ArrayList<>());
        var userCodes = List.of("userCode");
        userService.getEmailsByUserCodes(userCodes);
        verify(userRepository, times(1)).findAllByCodeIn(userCodes);
    }

    @Test
    public void testExpireLoginAttemptSuccess() {
        String loginAttemptId = UUID.randomUUID().toString();
        OffsetDateTime expireTime = OffsetDateTime.now();

        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setId(loginAttemptId);
        loginAttempt.setUserId(UUID.randomUUID().toString());
        loginAttempt.setLoginTime(OffsetDateTime.now().minusMinutes(5));
        loginAttempt.setExpireTime(OffsetDateTime.now().plusMinutes(1));
        loginAttempt.setExpired(false);

        Mockito.when(this.loginAttemptRepository.findById(loginAttemptId))
                .thenReturn(Optional.of(loginAttempt));

        Mockito.when(this.loginAttemptRepository.save(Mockito.any(LoginAttempt.class)))
                .thenReturn(loginAttempt);

        ExpireLoginRequest request = new ExpireLoginRequest();
        request.setLoginAttemptId(loginAttemptId);
        request.setExpired(false);
        request.setExpireTime(expireTime);

        this.userService.expireLoginAttempt(request);

        Mockito.verify(this.loginAttemptRepository, Mockito.times(1)).save(Mockito.any(LoginAttempt.class));

        verify(this.userSessionService, times(1)).clearSession(loginAttemptId);
        Assert.assertFalse(loginAttempt.isExpired());
        Assert.assertEquals(expireTime, loginAttempt.getExpireTime());
    }


    @Test
    public void testExpireLoginAttemptNoLoginAttempts() {
        String loginAttemptId = UUID.randomUUID().toString();
        OffsetDateTime expireTime = OffsetDateTime.now();

        Mockito.when(this.loginAttemptRepository.findById(loginAttemptId))
                .thenReturn(Optional.empty());

        ExpireLoginRequest request = new ExpireLoginRequest();
        request.setLoginAttemptId(loginAttemptId);
        request.setExpired(false);
        request.setExpireTime(expireTime);

        this.userService.expireLoginAttempt(request);
        verify(this.userSessionService, times(1)).clearSession(loginAttemptId);
        Mockito.verify(this.loginAttemptRepository, Mockito.times(0)).save(Mockito.any(LoginAttempt.class));
    }


    @Test
    public void testGetLastLoginAttemptByUserIdSuccess() {
        String userId = UUID.randomUUID().toString();

        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setId(UUID.randomUUID().toString());
        loginAttempt.setUserId(userId);
        loginAttempt.setLoginTime(OffsetDateTime.now().minusMinutes(10));
        loginAttempt.setExpireTime(OffsetDateTime.now().plusMinutes(1));
        loginAttempt.setExpired(false);

        Mockito.when(this.loginAttemptRepository.findById(userId))
                .thenReturn(Optional.of(loginAttempt));

        LoginAttemptDto result = this.userService.getLoginAttempt(userId);

        Assert.assertNotNull(result);
        Assert.assertEquals(loginAttempt.getId(), result.getId());
        Assert.assertEquals(loginAttempt.getUserId(), result.getUserId());
    }

    @Test
    public void testGetLastLoginAttemptByUserIdEmpty() {
        String userId = UUID.randomUUID().toString();

        Mockito.when(this.loginAttemptRepository.findById(userId))
                .thenReturn(Optional.empty());
        LoginAttemptDto result = this.userService.getLoginAttempt(userId);
        Assert.assertNull(result);
    }

    @Test
    public void testUpdateUser_withBlankFullName() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("   "),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withBlankEmail() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("   "),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withBlankEmployeeId() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.of("   "),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withInvalidMobileNumber() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("invalid-phone")
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withBlankMobileNumber() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("   ")
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withBlankRoleIdentifier() {
        UUID userId = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add("admin");
        roles.add("   ");
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withPartialRolesByUuid() {
        UUID userId = UUID.randomUUID();
        UUID roleId1 = UUID.randomUUID();
        UUID roleId2 = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add(roleId1.toString());
        roles.add(roleId2.toString());

        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Role role1 = new Role();
        role1.setId(roleId1);
        role1.setName("Admin");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findAllById(anySet())).thenReturn(List.of(role1));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withPartialRolesByName() {
        UUID userId = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add("admin");
        roles.add("manager");

        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Role role1 = new Role();
        role1.setId(UUID.randomUUID());
        role1.setName("admin");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findAllById(anySet())).thenReturn(Collections.emptyList());
        Mockito.when(roleRepository.findAllByNameInIgnoreCase(anySet())).thenReturn(List.of(role1));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_emailUnchanged() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("test@example.com"),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, never()).existsByEmailIgnoreCaseAndIdNot(anyString(), any(UUID.class));
    }

    @Test
    public void testUpdateUser_employeeIdUnchanged() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("New Name"),
                Optional.of("EMP123"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByEmployeeIdIgnoreCaseAndIdNot(anyString(), any(UUID.class))).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, never()).existsByEmployeeIdIgnoreCaseAndIdNot(anyString(), any(UUID.class));
    }

    @Test
    public void testUpdateUser_emailChangedFromNull() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("newemail@example.com"),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail(null);
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByEmailIgnoreCaseAndIdNot("newemail@example.com", userId)).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, times(1)).existsByEmailIgnoreCaseAndIdNot("newemail@example.com", userId);
    }

    @Test
    public void testUpdateUser_employeeIdChangedFromNull() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.of("NEWEMP123"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId(null);
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByEmployeeIdIgnoreCaseAndIdNot("NEWEMP123", userId)).thenReturn(false);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, times(1)).existsByEmployeeIdIgnoreCaseAndIdNot("NEWEMP123", userId);
    }

    @Test
    public void testUpdateUser_withValidMobileNumber() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("+971501234567")
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_withRolesAndEmptyUuidList() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add("admin");

        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Role role1 = new Role();
        role1.setId(roleId);
        role1.setName("admin");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findAllById(anySet())).thenReturn(Collections.emptyList());
        Mockito.when(roleRepository.findAllByNameInIgnoreCase(anySet())).thenReturn(List.of(role1));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRoleRepository, times(1)).deleteAllByUserId(userId);
        verify(userRoleRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testUpdateUser_withRolesAndEmptyNameList() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add(roleId.toString());

        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Role role1 = new Role();
        role1.setId(roleId);
        role1.setName("Admin");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository.findAllById(anySet())).thenReturn(List.of(role1));
        Mockito.when(roleRepository.findAllByNameInIgnoreCase(anySet())).thenReturn(Collections.emptyList());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(userRoleRepository, times(1)).deleteAllByUserId(userId);
        verify(userRoleRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testUpdateUser_validationFailsWithNoEmailAndEmployeeId() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("New Name"),
                Optional.of(""),
                Optional.empty(),
                Optional.empty(),
                Optional.of(""),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("");
        user.setEmployeeId("");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_caseInsensitiveDuplicateRoles() {
        UUID userId = UUID.randomUUID();
        Set<String> roles = new LinkedHashSet<>();
        roles.add("Admin");
        roles.add("admin");

        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(roles),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withValidDepartment() {
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.of("New Name"),
                Optional.empty(),
                Optional.of(departmentId),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Department department = new Department();
        department.setId(departmentId);
        department.setName("IT Department");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        verify(departmentRepository, times(1)).findById(departmentId);
    }

    @Test
    public void testUpdateUser_withValidLineManagerEmail() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("manager@example.com"),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(useResponseMapper.toDto(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(userId, request);
        assertNotNull(response);
        assertEquals("manager@example.com", user.getLineManagerEmail());
    }

    @Test
    public void testUpdateUser_withInvalidLineManagerEmail() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("invalid-email"),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testUpdateUser_withBlankLineManagerEmail() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("   "),
                Optional.empty()
        );
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setEmployeeId("EMP123");
        user.setStatus(1);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    public void testCreateUser_withValidPassportPhoto() throws IOException {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EMP001");
        request.setEmail("test@example.com");
        request.setMobile("+971501234567");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());

        MultipartFile passportPhoto = Mockito.mock(MultipartFile.class);
        byte[] photoBytes = "test-photo-data".getBytes();
        Mockito.when(passportPhoto.isEmpty()).thenReturn(false);
        Mockito.when(passportPhoto.getBytes()).thenReturn(photoBytes);
        Mockito.when(passportPhoto.getContentType()).thenReturn("image/jpeg");
        Mockito.when(passportPhoto.getOriginalFilename()).thenReturn("passport.jpg");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmployeeIdIgnoreCase("EMP001"))
                .thenReturn(Optional.empty());
        Mockito.when(departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertArrayEquals(photoBytes, savedUser.getPassportPhoto());
            assertEquals("image/jpeg", savedUser.getPassportPhotoContentType());
            assertEquals("passport.jpg", savedUser.getPassportPhotoFileName());
            return savedUser;
        });

        UserResponse response = userService.createUser(request, passportPhoto);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_withPassportPhotoIOException() throws IOException {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EMP001");
        request.setEmail("test@example.com");
        request.setMobile("+971501234567");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());

        MultipartFile passportPhoto = Mockito.mock(MultipartFile.class);
        Mockito.when(passportPhoto.isEmpty()).thenReturn(false);
        Mockito.when(passportPhoto.getBytes()).thenThrow(new IOException("File read error"));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmployeeIdIgnoreCase("EMP001"))
                .thenReturn(Optional.empty());
        Mockito.when(departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> userService.createUser(request, passportPhoto));
    }

    @Test
    public void testCreateUser_withNullPassportPhoto() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EMP001");
        request.setEmail("test@example.com");
        request.setMobile("+971501234567");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmployeeIdIgnoreCase("EMP001"))
                .thenReturn(Optional.empty());
        Mockito.when(departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.createUser(request, null);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        assertNull(user.getPassportPhoto());
    }

    @Test
    public void testCreateUser_withEmptyPassportPhoto() throws IOException {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmployeeId("EMP001");
        request.setEmail("test@example.com");
        request.setMobile("+971501234567");
        request.setApprovalLevels(List.of("L1", "L2"));
        request.setProductTypes(List.of("P1", "P2"));
        request.setDepartmentId(UUID.randomUUID().toString());

        MultipartFile passportPhoto = Mockito.mock(MultipartFile.class);
        Mockito.when(passportPhoto.isEmpty()).thenReturn(true);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmployeeIdIgnoreCase("EMP001"))
                .thenReturn(Optional.empty());
        Mockito.when(departmentRepository.findById(UUID.fromString(request.getDepartmentId())))
                .thenReturn(Optional.of(new Department()));
        Mockito.when(userRegisterMapper.toEntity(request)).thenReturn(user);
        Mockito.when(userRepository.findFirstByOrderByCreatedDateDesc()).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.createUser(request, passportPhoto);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        assertNull(user.getPassportPhoto());
    }

    private static String invokeGenerateUserCodeFromPrefix(String prefix, String latestCode) throws Exception {
        Method method = UserServiceImpl.class.getDeclaredMethod("generateUserCodeFromPrefix", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, prefix, latestCode);
    }

    @Test
    public void testGenerateUserCodeFromPrefix_emptyPrefix() throws Exception {
        assertEquals("", invokeGenerateUserCodeFromPrefix("", "IB_2024_000001"));
    }

    @Test
    public void testGenerateUserCodeFromPrefix_invalidLatestYearFormat() throws Exception {
        int year = LocalDateTime.now().getYear();
        String code = invokeGenerateUserCodeFromPrefix("IB", "IB_XXXX_000123");
        assertTrue(code.startsWith("IB_" + year + "_"));
        assertTrue(code.endsWith("_000001"));
    }

    @Test
    public void testGenerateUserCodeFromPrefix_invalidSequenceFormat() throws Exception {
        int year = LocalDateTime.now().getYear();
        String code = invokeGenerateUserCodeFromPrefix("IB", "IB_" + year + "_ABC");
        assertEquals("IB_" + year + "_000001", code);
    }

    @Test
    public void testGenerateUserCodeFromPrefix_incrementsSequence() throws Exception {
        int year = LocalDateTime.now().getYear();
        String code = invokeGenerateUserCodeFromPrefix("IB", "IB_" + year + "_000001");
        assertEquals("IB_" + year + "_000002", code);
    }

    @Test
    public void testGetUserById_whenEmail_delegatesToGetUserByEmail() {
        String email = "admin@gmail.com";
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setEmail(email);

        UserResponse dto = new UserResponse();
        dto.setId(id);
        dto.setEmail(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(useResponseMapper.toDto(user)).thenReturn(dto);
        when(userRoleRepository.findRoleIdsByUserId(id)).thenReturn(Collections.emptyList());

        UserResponse response = userService.getUserById(email);
        assertNotNull(response);
        assertEquals(email, response.getEmail());
    }

    @Test
    public void testGetUserById_whenUuid_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("admin@gmail.com");

        UserResponse dto = new UserResponse();
        dto.setId(id);
        dto.setEmail(user.getEmail());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(useResponseMapper.toDto(user)).thenReturn(dto);
        when(userRoleRepository.findRoleIdsByUserId(id)).thenReturn(Collections.emptyList());

        UserResponse response = userService.getUserById(id.toString());
        assertNotNull(response);
        assertEquals(id, response.getId());
    }

    @Test
    public void testGetUserById_invalidUuid_throwsValidationException() {
        assertThrows(ValidationException.class, () -> userService.getUserById("not-a-uuid"));
    }

    @Test
    public void testGetUserPassportPhoto_noPhoto_returnsNotFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setPassportPhoto(null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        org.springframework.http.ResponseEntity<byte[]> response = userService.getUserPassportPhoto(id.toString());
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetUserPassportPhoto_withDefaultContentType_returnsOk() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setPassportPhoto(new byte[]{1, 2, 3});
        user.setPassportPhotoContentType(null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        org.springframework.http.ResponseEntity<byte[]> response = userService.getUserPassportPhoto(id.toString());
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(new byte[]{1, 2, 3}, response.getBody());
        assertEquals(org.springframework.http.MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }


}
