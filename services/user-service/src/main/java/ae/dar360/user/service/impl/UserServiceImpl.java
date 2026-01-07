package ae.dar360.user.service.impl;

import ae.dar360.user.client.feign.NotificationServiceFeign;
import ae.dar360.user.constant.CommonConstants;
import ae.dar360.user.dto.*;
import ae.dar360.user.enums.TokenStatus;
import ae.dar360.user.enums.TokenTypeEnum;
import ae.dar360.user.enums.UserStatus;
import ae.dar360.user.exception.ValidationException;
import ae.dar360.user.exception.constant.ValidationAlertEnum;
import ae.dar360.user.exception.dto.AlertMessages;
import ae.dar360.user.mapper.UseResponseMapper;
import ae.dar360.user.mapper.UserListResponseMapper;
import ae.dar360.user.mapper.UserRegisterMapper;
import ae.dar360.user.model.*;
import ae.dar360.user.repository.*;
import ae.dar360.user.service.UserService;
import ae.dar360.user.service.UserSessionService;
import ae.dar360.user.util.DateTimeUtils;
import ae.dar360.user.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Value("${app.user-token-session-expired}")
    private Long tokenSessionDuration;

    @Value("${path.url.setup-password}")
    private String urlSetUpPassword;

    @Value("${path.url.forgot-password}")
    private String urlForgotPassword;

    @Value("${app.last-login.expired.inactive}")
    private long lastLoginExpireInactive;

    @Value("${app.last-login.expired.warning}")
    private long lastLoginExpireWarning;


    @Value("${app.password.expired.inactive}")
    private long passwordExpiredInactive;

    @Value("${app.password.expired.send-notification}")
    private List<Long> passwordExpiredSendNotification;

    @Value("${app.user-session-timeout-minutes}")
    private long sessionTimeoutMinutes;

    @Value("${app.user.default-password:1234}")
    private String defaultPassword;

  private static final String USER_NAME_VARIABLE = "%%USER_NAME%%";
  private static final String USER_EMAIL_VARIABLE = "%%USER_EMAIL%%";
  private static final String MODIFIED_DATE = "modifiedDate";
  private static final String CREATED_DATE = "createdDate";
  private static final int MAX_VERIFY_TIME = 5;
  private static final Pattern UUID_REGEX =
      Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final UserRepository userRepository;
    private final TokenSessionRepository tokenSessionRepository;
    private final UserRegisterMapper userRegisterMapper;
    private final UseResponseMapper useResponseMapper;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordDictionaryRepository passwordDictionaryRepository;
    private final PasswordChangeHistoryRepository passwordChangeHistoryRepository;
    private final NotificationServiceFeign notificationServiceFeign;
    private final HttpServletRequest request;
    private final UserListResponseMapper userListResponseMapper;
    private final LoginAttemptRepository loginAttemptRepository;
    private final UserSessionService userSessionService;
    private final UserSessionInfoRepository userSessionInfoRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserResponse createUser(CreateUserRequest userRequest) {
        return createUser(userRequest, null);
    }

    @Override
    public UserResponse createUser(CreateUserRequest userRequest, MultipartFile passportPhoto) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(userRequest.getEmail());
        if (userOptional.isPresent()) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM016));
        }

        Optional<User> userEmployeeIdOptional = userRepository.findByEmployeeIdIgnoreCase(userRequest.getEmployeeId());
        if (userEmployeeIdOptional.isPresent()) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.EMPLOYEE_ID_EXISTED));
        }

        // get department info
        Department department =
                departmentRepository
                        .findById(UUID.fromString(userRequest.getDepartmentId()))
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.DEPARTMENT_NOT_EXIST)));
        User user = userRegisterMapper.toEntity(userRequest);
        user.setProductType(StringUtils.join(userRequest.getProductTypes(), ","));
        user.setApprovalLevel(StringUtils.join(userRequest.getApprovalLevels(), ","));
        user.setDeviationApprovalLevel(StringUtils.join(userRequest.getDeviationApprovalLevels(), ","));
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setLock(false);
        user.setDepartment(department);
        user.setPassword(passwordEncoder.encode(defaultPassword));

        if (passportPhoto != null && !passportPhoto.isEmpty()) {
            try {
                user.setPassportPhoto(passportPhoto.getBytes());
                user.setPassportPhotoContentType(passportPhoto.getContentType());
                user.setPassportPhotoFileName(passportPhoto.getOriginalFilename());
            } catch (Exception e) {
                log.error("Failed to read passport photo for user {}: {}", userRequest.getEmail(), e.getMessage(), e);
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.PASSPORT_PHOTO_UPLOAD_FAILED), e);
            }
        }

        // get latest userId
        Optional<User> userLatest = userRepository.findFirstByOrderByCreatedDateDesc();
        user.setCode(setCode(userLatest));
        user.setModifiedBy(getModifiedBy());
        log.info("Creating new user '{}' with lock status: {}", user.getEmail(), user.isLock());
        User userAfterSave = userRepository.save(user);
        // generate link add password token
        TokenSession tokenSession = new TokenSession();
        tokenSession.setUserId(userAfterSave.getId());
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(tokenSessionDuration));
        tokenSession.setToken(UUID.randomUUID().toString());
        tokenSession.setStatus(TokenStatus.ACTIVE.getValue());
        tokenSession.setType(TokenTypeEnum.CREATE_NEW.getValue());
        tokenSessionRepository.save(tokenSession);
        // send notification to user link change password
        sendEmailET4(user, tokenSession);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setCode(user.getCode());
        return userResponse;
    }

    private String setCode(Optional<User> userLatest) {
        if (userLatest.isPresent()) {
            final User userData = userLatest.get();
            return generateUserCodeFromPrefix(CommonConstants.PREFIX_IB, userData.getCode());
        }
        return generateUserCodeFromPrefix(CommonConstants.PREFIX_IB, StringUtils.EMPTY);
    }

    private static String generateUserCodeFromPrefix(final String prefix, final String latestCode) {
        if (StringUtils.isEmpty(prefix)) {
            return StringUtils.EMPTY;
        }
        final int year = LocalDateTime.now().getYear();
        final Integer yMax = 999999;
        int latestYear = 0;
        String[] yCurrentArr = null; // Initialize as null, will be populated if latestCode is not empty

        if (!StringUtils.isEmpty(latestCode)) {
            yCurrentArr = latestCode.split(CommonConstants.UNDERLINE);
            if (yCurrentArr.length >= 2) {
                try {
                    latestYear = Integer.parseInt(yCurrentArr[yCurrentArr.length - 2]);
                } catch (NumberFormatException ex) {
                    // Fallback: unexpected code format (e.g., legacy string). Treat as new sequence.
                    latestYear = 0;
                }
            } else {
                latestYear = 0; // Treat as new sequence if format is unexpected
            }
        }

        if (StringUtils.isEmpty(latestCode) || latestYear < year) {
            return prefix
                    + CommonConstants.UNDERLINE
                    + year
                    + CommonConstants.UNDERLINE
                    + StringUtil.convertNumberToStringFullCharacter(0, yMax, 1);
        }

        // This block is reached only if latestCode is NOT empty AND latestYear >= year
        // So, yCurrentArr should have been populated.
        // We still need to ensure yCurrentArr has at least one element for yCurrent.
        String yCurrent = "0"; // Default value
        if (yCurrentArr != null && yCurrentArr.length >= 1) { // Ensure yCurrentArr is not null and has elements
            try {
                // If parsing fails, keep default "0" so sequence restarts safely
                Integer.parseInt(yCurrentArr[yCurrentArr.length - 1]);
                yCurrent = yCurrentArr[yCurrentArr.length - 1];
            } catch (NumberFormatException ex) {
                yCurrent = "0";
            }
        }
        return prefix
                + CommonConstants.UNDERLINE
                + year
                + CommonConstants.UNDERLINE
                + StringUtil.convertNumberToStringFullCharacter(Integer.valueOf(yCurrent), yMax, 1);
    }

    @Override
    public UserResponse getUserById(String id) {
        log.debug("Getting user by id: {}", id);

        // Check if id is an email address
        if (id.contains("@")) {
            log.debug("ID appears to be an email, using getUserByEmail instead");
            return getUserByEmail(id);
        }

        // Try to parse as UUID
        try {
            return getUserResponse(getUserInfo(UUID.fromString(id)));
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", id);
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST));
        }
    }

    @Override
    public org.springframework.http.ResponseEntity<byte[]> getUserPassportPhoto(String userId) {
        log.debug("Getting passport photo for user: {}", userId);

        try {
            User user = getUserInfo(UUID.fromString(userId));

            if (user.getPassportPhoto() == null || user.getPassportPhoto().length == 0) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (user.getPassportPhotoContentType() != null) {
                headers.setContentType(org.springframework.http.MediaType.parseMediaType(user.getPassportPhotoContentType()));
            } else {
                headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);
            }
            headers.setCacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic());

            return new org.springframework.http.ResponseEntity<>(user.getPassportPhoto(), headers, org.springframework.http.HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", userId);
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST));
        }
    }

    @Transactional
    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest userRequest) {
        User user = getUserInfo(userId);

        updateUserFullName(userRequest, user);
        updateUserEmail(userRequest, user, userId);
        updateUserLineManagerEmail(userRequest, user);
        updateUserEmployeeId(userRequest, user, userId);
        updateUserMobileNumber(userRequest, user);
        updateUserDepartment(userRequest, user);
        updateUserRoles(userRequest, userId);

        validateUserIdentifiers(user);
        user.setModifiedBy(getModifiedBy());
        User saved = userRepository.save(user);
        return getUserResponse(saved);
    }

    private void updateUserFullName(UpdateUserRequest userRequest, User user) {
        userRequest.fullName().ifPresent(fullName -> {
            if (StringUtils.isBlank(fullName)) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_FULL_NAME));
            }
            user.setFullName(fullName);
        });
    }

    private void updateUserEmail(UpdateUserRequest userRequest, User user, UUID userId) {
        userRequest.email().ifPresent(email -> {
            validateEmail(email);
            checkEmailUniqueness(email, user, userId);
            user.setEmail(email);
        });
    }

    private void updateUserLineManagerEmail(UpdateUserRequest userRequest, User user) {
        userRequest.lineManagerEmail().ifPresent(lineManagerEmail -> {
            validateEmail(lineManagerEmail);
            user.setLineManagerEmail(lineManagerEmail);
        });
    }

    private void validateEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_EMAIL));
        }
        if (!StringUtil.validateEmail(email)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_EMAIL));
        }
    }

    private void checkEmailUniqueness(String email, User user, UUID userId) {
        boolean emailChanged = user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email);
        if (emailChanged && userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM016));
        }
    }

    private void updateUserEmployeeId(UpdateUserRequest userRequest, User user, UUID userId) {
        userRequest.employeeId().ifPresent(employeeId -> {
            if (StringUtils.isBlank(employeeId)) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_EMPLOYEE_ID));
            }
            checkEmployeeIdUniqueness(employeeId, user, userId);
            user.setEmployeeId(employeeId);
        });
    }

    private void checkEmployeeIdUniqueness(String employeeId, User user, UUID userId) {
        boolean employeeIdChanged = user.getEmployeeId() == null || !user.getEmployeeId().equalsIgnoreCase(employeeId);
        if (employeeIdChanged && userRepository.existsByEmployeeIdIgnoreCaseAndIdNot(employeeId, userId)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.EMPLOYEE_ID_EXISTED));
        }
    }

    private void updateUserMobileNumber(UpdateUserRequest userRequest, User user) {
        userRequest.mobileNumber().ifPresent(mobileNumber -> {
            if (StringUtils.isBlank(mobileNumber) || !StringUtil.validatePhone(mobileNumber)) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_MOBILE));
            }
            user.setMobile(mobileNumber);
        });
    }

    private void updateUserDepartment(UpdateUserRequest userRequest, User user) {
        userRequest.departmentId().ifPresent(departmentId -> {
            Department department = departmentRepository
                    .findById(departmentId)
                    .orElseThrow(() -> new ValidationException(
                            AlertMessages.alert(ValidationAlertEnum.DEPARTMENT_NOT_EXIST)));
            user.setDepartment(department);
        });
    }

    private void updateUserRoles(UpdateUserRequest userRequest, UUID userId) {
        userRequest.roleIds().ifPresent(roleIdentifiers -> {
            Set<String> sanitized = sanitizeRoleIdentifiers(roleIdentifiers);
            validateNoDuplicateRoles(sanitized);
            Set<UUID> roleUuids = resolveRoleIdentifiers(sanitized);
            assignRolesToUser(userId, roleUuids);
        });
    }

    private Set<String> sanitizeRoleIdentifiers(Set<String> roleIdentifiers) {
        Set<String> sanitized = roleIdentifiers.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (sanitized.size() != roleIdentifiers.size()) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_ROLE_ASSIGNMENT));
        }
        return sanitized;
    }

    private void validateNoDuplicateRoles(Set<String> roleIdentifiers) {
        Set<String> seen = new HashSet<>();
        for (String identifier : roleIdentifiers) {
            if (!seen.add(identifier.toLowerCase(Locale.ROOT))) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.INVALID_ROLE_ASSIGNMENT));
            }
        }
    }

    private Set<UUID> resolveRoleIdentifiers(Set<String> sanitized) {
        Set<UUID> uuidCandidates = new LinkedHashSet<>();
        Set<String> nameCandidates = new LinkedHashSet<>();

        partitionRoleIdentifiers(sanitized, uuidCandidates, nameCandidates);
        List<Role> resolvedRoles = fetchRolesByIdentifiers(uuidCandidates, nameCandidates);

        return resolvedRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void partitionRoleIdentifiers(Set<String> sanitized, Set<UUID> uuidCandidates, Set<String> nameCandidates) {
        sanitized.forEach(identifier -> {
            try {
                uuidCandidates.add(UUID.fromString(identifier));
            } catch (IllegalArgumentException ex) {
                nameCandidates.add(identifier);
            }
        });
    }

    private List<Role> fetchRolesByIdentifiers(Set<UUID> uuidCandidates, Set<String> nameCandidates) {
        List<Role> resolvedRoles = new ArrayList<>();

        if (!uuidCandidates.isEmpty()) {
            List<Role> rolesById = roleRepository.findAllById(uuidCandidates);
            if (rolesById.size() != uuidCandidates.size()) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.ROLE_NOT_EXIST));
            }
            resolvedRoles.addAll(rolesById);
        }

        if (!nameCandidates.isEmpty()) {
            List<Role> rolesByName = roleRepository.findAllByNameInIgnoreCase(nameCandidates);
            if (rolesByName.size() != nameCandidates.size()) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.ROLE_NOT_EXIST));
            }
            resolvedRoles.addAll(rolesByName);
        }

        return resolvedRoles;
    }

    private void assignRolesToUser(UUID userId, Set<UUID> roleUuids) {
        userRoleRepository.deleteAllByUserId(userId);
        if (!roleUuids.isEmpty()) {
            List<UserRole> assignments = roleUuids.stream()
                    .map(roleId -> new UserRole()
                            .setUserId(userId)
                            .setRoleId(roleId))
                    .toList();
            userRoleRepository.saveAll(assignments);
        }
    }

    private void validateUserIdentifiers(User user) {
        if (StringUtils.isBlank(user.getEmail()) && StringUtils.isBlank(user.getEmployeeId())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IDENTIFIER_REQUIRED));
        }
    }

    @Transactional
    @Override
    public void deleteUser(UUID userId) {
        User user = getUserInfo(userId);
        userRoleRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }

    @Override
    public void updateUserStatus(String userId, Integer status) {
        User user = userRepository
            .findById(UUID.fromString(userId))
            .orElseThrow(() ->
                new ValidationException(AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST))
            );

        if (status == null || !UserStatus.contains(status)) {
            throw new ValidationException(
                AlertMessages.alert(ValidationAlertEnum.USER_STATUS_NOT_MATCH));
        }

        if (status == UserStatus.DELETE.getValue()) {
            userRepository.delete(user);
        } else {
            user.setStatus(status);
            user.setModifiedBy(getModifiedBy());
            userRepository.save(user);
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));
        return getUserResponse(user);
    }

    @Override
    public List<UserResponse> getUserByLevel(String level) {
        List<User> userList = this.userRepository.findUserByApprovalLevelLikeIgnoreCase(level);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }

        return userList.stream()
                .map(this::getUserResponse)
                .toList();
    }

    @Override
    public List<UserDepartmentResponse> findByFullNameAndEmail(String fullName, String email) {
        Page<User> userList = this.userRepository.findByFullNameOrEmail(fullName, email, PageRequest.of(0, 10));
        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        return userList.getContent().stream()
                .map(u -> {
                    UserDepartmentResponse element = new UserDepartmentResponse();
                    element.setFullName(u.getFullName());
                    element.setEmail(u.getEmail());
                    element.setDepartmentName(u.getDepartment().getName());
                    element.setApprovalLevel(u.getApprovalLevel());
                    element.setApprovalLimit(StringUtils.isEmpty(u.getApprovedLimit()) ? BigDecimal.ZERO : new BigDecimal(u.getApprovedLimit()));
                    element.setUuid(u.getId());
                    return element;
                })
                .toList();
    }

    private UserResponse getUserResponse(User user) {
        UserResponse userResponse = useResponseMapper.toDto(user);
        if (Objects.nonNull(user.getDepartment())) {
            userResponse.setDepartmentId(user.getDepartment().getId().toString());
            userResponse.setDepartmentName(user.getDepartment().getName());
        }
        if (Objects.nonNull(user.getProductType())) {
            userResponse.setProductTypes(Arrays.asList(user.getProductType().split(",")));
        }
        if (Objects.nonNull(user.getApprovalLevel())) {
            userResponse.setApprovalLevels(Arrays.asList(user.getApprovalLevel().split(",")));
        }
        if (Objects.nonNull(user.getDeviationApprovalLevel())) {
            userResponse.setDeviationApprovalLevels(Arrays.asList(user.getDeviationApprovalLevel().split(",")));
        }
        List<UUID> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        if (CollectionUtils.isNotEmpty(roleIds)) {
            userResponse.setRoleIds(new ArrayList<>(roleIds));
        } else {
            userResponse.setRoleIds(new ArrayList<>());
        }
        return userResponse;
    }

    @Override
    public void unlockUser(String userId) {
        User user = getUserInfo(UUID.fromString(userId));
        if (!user.isLock()) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.USER_NOT_LOCK));
        }
        TokenSession tokenSession = generateTokenSession(user, TokenTypeEnum.UNLOCK);
        // send notification to user link change password
        sendEmailET4(user, tokenSession);
    }

    private User getUserInfo(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () ->
                                new ValidationException(
                                        AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));
    }

    private void sendEmailET4(User user, TokenSession tokenSession) {
        LinkedHashMap<String, String> variables = new LinkedHashMap<>();
        variables.put(USER_NAME_VARIABLE, user.getFullName());
        if (TokenTypeEnum.FORGOT.getValue() == tokenSession.getType()) {
            variables.put("%%HYPERLINK%%", urlForgotPassword + "?token=" + Base64.getEncoder().encodeToString(tokenSession.getToken().getBytes()));
        } else {
            variables.put("%%HYPERLINK%%", urlSetUpPassword + "?token=" + Base64.getEncoder().encodeToString(tokenSession.getToken().getBytes()));
        }
        this.sendEmail(user.getEmail(), "ET4", variables);
    }

    private void sendEmail(String email, String templateCode, Map<String, String> variables) {
        log.info("Sending email to: {}, template: {}", email, templateCode);
        CompletableFuture.runAsync(() -> {
            try {
                notificationServiceFeign.emailNotify(NotifyBodyDto.builder()
                        .sendToEmails(List.of(email))
                        .templateCode(templateCode)
                        .variables(variables).build());
                log.info("Email notification sent successfully to: {}, template: {}", email, templateCode);
            } catch (Exception e) {
                log.error("Failed to send email to: {}, template: {}, error: {}", email, templateCode, e.getMessage(), e);
            }
        });

    }

    @Override
    @Transactional
    public void regenerateTokenSession(String token) {
        String tokenDecode = new String(Base64.getDecoder().decode(token));
        TokenSession tokenSession =
                tokenSessionRepository
                        .findByToken(tokenDecode)
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXIST)));
        if (OffsetDateTime.now().isBefore(tokenSession.getExpiryDate())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXPIRED));
        }
        tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(tokenSessionDuration));
        tokenSession.setToken(UUID.randomUUID().toString());
        tokenSessionRepository.save(tokenSession);
        sendEmailET4(getUserInfo(tokenSession.getUserId()), tokenSession);
    }

    @Override
    public TokenSession validateTokenSession(String tokenSession) {
        if (StringUtils.isEmpty(tokenSession)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXIST));
        }

        String tokenDecode;
        if (UUID_REGEX.matcher(tokenSession).matches()) {
            tokenDecode = tokenSession;
        } else {
            tokenDecode = new String(Base64.getDecoder().decode(tokenSession));
        }

        TokenSession tokenSessionData =
                tokenSessionRepository
                        .findByToken(tokenDecode)
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXIST)));
        if (OffsetDateTime.now().isAfter(tokenSessionData.getExpiryDate()) ||
                (tokenSessionData.getStatus() != null && TokenStatus.INACTIVE.getValue() == tokenSessionData.getStatus()) ||
                (tokenSessionData.getVerifyTimes()!= null && tokenSessionData.getVerifyTimes() >= MAX_VERIFY_TIME )) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.TOKEN_EXPIRED));
        }
        return tokenSessionData;
    }

    @Override
    public void increaseTokenVerifyTimes(String tokenSession) {
        if (StringUtils.isEmpty(tokenSession)) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXIST));
        }
        String tokenDecode = new String(Base64.getDecoder().decode(tokenSession));
        TokenSession tokenSessionData =
                tokenSessionRepository
                        .findByToken(tokenDecode)
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.TOKEN_NOT_EXIST)));
        Integer verifyTimes = tokenSessionData.getVerifyTimes();
        if (Objects.isNull(verifyTimes)) {
            verifyTimes = 0;
        }
        tokenSessionData.setVerifyTimes(++verifyTimes);
        if (tokenSessionData.getVerifyTimes() == MAX_VERIFY_TIME) {
          tokenSessionData.setStatus(TokenStatus.INACTIVE.getValue());
        }
        tokenSessionRepository.save(tokenSessionData);
    }

    @Override
    public void setupPassword(PasswordDto passwordDto) {
        TokenSession tokenSession = validateTokenSession(passwordDto.getToken());
        User user = getUserInfo(tokenSession.getUserId());
        // Add validate: password not contain username.
        if (user == null) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.UNAUTHORIZED));
        }
        if (containsUserName(passwordDto.getPassword(), user.getFullName())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM115));
        }

        if (tokenSession.getType() != null && TokenTypeEnum.FORGOT.getValue() != tokenSession.getType()) {
            user.setLock(false);
        }
        savePassword(passwordDto, user);
        // invalid token
        tokenSession.setStatus(TokenStatus.INACTIVE.getValue());
        tokenSessionRepository.save(tokenSession);
    }

    private boolean containsUserName(String password, String userName) {
        if (StringUtils.isEmpty(userName)) {
            return false;
        }

        String finalPassword = password.toLowerCase(Locale.ROOT);
        userName = StringUtils.stripAccents(userName).toLowerCase(Locale.ROOT);
        String[] keywords = userName.split(" ");
        return Arrays.stream(keywords).anyMatch(finalPassword::contains);
    }

    @Override
    public Map<UUID, UserDepartmentResponse> getFullnameAndDepartmentName(
            UserDepartmentRequest request) {
        List<User> listUser = this.userRepository.findAllById(request.getUserIds());
        Map<UUID, UserDepartmentResponse> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(listUser)) {
            for (User u : listUser) {
                UserDepartmentResponse element = new UserDepartmentResponse();
                element.setFullName(u.getFullName());
                element.setDepartmentName(u.getDepartment().getName());
                element.setEmail(u.getEmail());
                element.setApprovalLevel(u.getApprovalLevel());
                element.setApprovalLimit(StringUtils.isEmpty(u.getApprovedLimit()) ? BigDecimal.ZERO : new BigDecimal(u.getApprovedLimit()));
                element.setUuid(u.getId());
                result.put(u.getId(), element);
            }
        }
        return result;
    }

    @Override
    public Page<SearchUserResponseDto> searchUser(SearchUserForm form) {
        // Default to sorting by createdDate descending (latest first)
        String sortField = CREATED_DATE;
        if (form.getSortKey() != null && !form.getSortKey().isEmpty()) {
            sortField = form.getSortKey();
        }

        Sort sort = Sort.by(sortField).descending();
        if ("asc".equalsIgnoreCase(form.getSortDirection())) {
            sort = Sort.by(sortField).ascending();
        }

        int validPageSize = (form.getPageSize() == null || form.getPageSize() < 1) ? 20 : form.getPageSize();
        int validPageNumber = (form.getPageNumber() == null || form.getPageNumber() < 0) ? 0 : form.getPageNumber();
        Pageable pageable = PageRequest.of(validPageNumber, validPageSize, sort);
        Page<User> users = this.userRepository.findAll(new UserSpecification(form), pageable);
        return users.map(userListResponseMapper::toDto);
    }

    @Override
    public void changePassword(PasswordDto passwordDto) {
        User user =
                userRepository
                        .findById(passwordDto.getUserId())
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));
        savePassword(passwordDto, user);
    }

    @Override
    public void validateChangePassword(PasswordDto passwordDto) {
        User user =
                userRepository
                        .findById(passwordDto.getUserId())
                        .orElseThrow(
                                () ->
                                        new ValidationException(
                                                AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));
        this.validatePassword(passwordDto, user);
    }

    private void savePassword(PasswordDto passwordDto, User user) {
        validatePassword(passwordDto, user);
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hashPassword = bCryptPasswordEncoder.encode(passwordDto.getPassword());
        user.setPassword(hashPassword);
        user.setLastUpdatedPassword(OffsetDateTime.now());
        user.setPasswordExpirationDate(OffsetDateTime.now().plusDays(60));
        user.setModifiedBy(user.getEmail());
        userRepository.save(user);
        PasswordChangeHistory passwordChangeHistory = new PasswordChangeHistory();
        passwordChangeHistory.setHashedPassword(hashPassword);
        passwordChangeHistory.setUserId(user.getId());
        passwordChangeHistoryRepository.save(passwordChangeHistory);
    }

    private void validatePassword(PasswordDto passwordDto, User user) {
        if (passwordDictionaryRepository.containDictionaryWord(passwordDto.getPassword())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM113));
        }
        if (containsUserName(passwordDto.getPassword(), user.getFullName())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM115));
        }
        if (StringUtils.isNotEmpty(user.getPassword())) {
            Pageable pageable = PageRequest.of(0, 6, Sort.by(MODIFIED_DATE).descending());
            Page<PasswordChangeHistory> passwordChangeHistories =
                    passwordChangeHistoryRepository.findByUserId(user.getId(), pageable);
            boolean exists =
                    passwordChangeHistories.stream()
                            .map(PasswordChangeHistory::getHashedPassword)
                            .anyMatch(
                                    hashedPassword ->
                                            passwordEncoder.matches(passwordDto.getPassword(), hashedPassword));
            if (exists) {
                throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM117));
            }
        }
        if (StringUtils.isNotEmpty(passwordDto.getCurrentPassword())
                && !passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM119));
        }
    }

    @Override
    public List<String> getEmailsByLevel(String level) {
        if (StringUtils.isNotBlank(level)) {
            return this.userRepository.findEmailsByApprovalLevelLikeIgnoreCase(level);
        }
        return Collections.emptyList();
    }

    @Override
    public List<SearchUserResponseDto> getAllUserExport() {
        return userListResponseMapper.toDtos(
                userRepository.findAllByStatusIsNotOrderByModifiedDateDesc(UserStatus.DELETE.getValue()));
    }

    @Override
    public List<String> getEmailsByDepartment(String department) {
        if (StringUtils.isNotBlank(department)) {
            return this.userRepository.findEmailsByDepartmentLikeIgnoreCase(department);
        }
        return Collections.emptyList();
    }

    public void forgotPassword(String email) {
        log.info("Forgot password request for email: {}", email);
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCaseAndStatusNot(email, UserStatus.DELETE.getValue());

        if (optionalUser.isEmpty()) {
            log.warn("Forgot password: User not found or deleted for email: {}", email);
            return;
        }

        User user = optionalUser.get();
        log.info("User found: {}, status: {}, locked: {}", user.getEmail(), user.getStatus(), user.isLock());

        if (user.isLock()) {
            log.warn("Forgot password: User is locked: {}", email);
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM037));
        }
        if (user.getStatus() != null
                && UserStatus.INACTIVE.getValue() == user.getStatus()) {
            log.warn("Forgot password: User is inactive: {}", email);
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM038));
        }
        TokenSession tokenSession = generateTokenSession(user, TokenTypeEnum.FORGOT);
        log.info("Generated token session for user: {}, token: {}, expires: {}",
                user.getEmail(), tokenSession.getToken(), tokenSession.getExpiryDate());
        // send notification to user link change password
        sendEmailET4(user, tokenSession);
        log.info("Forgot password email sent successfully for user: {}", user.getEmail());
    }

    private TokenSession generateTokenSession(User user, TokenTypeEnum forgot) {
        TokenSession tokenSession = null;
        Optional<TokenSession> tokenSessionOptional = tokenSessionRepository.findByuserIdAndStatus(user.getId(), TokenStatus.ACTIVE.getValue());
        if (tokenSessionOptional.isPresent()) {
            tokenSession = tokenSessionOptional.get();
            tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(tokenSessionDuration));
            tokenSessionRepository.save(tokenSession);
        } else {
            tokenSession = new TokenSession();
            tokenSession.setUserId(user.getId());
            tokenSession.setExpiryDate(OffsetDateTime.now().plusSeconds(tokenSessionDuration));
            tokenSession.setToken(UUID.randomUUID().toString());
            tokenSession.setType(forgot.getValue());
            tokenSession.setStatus(TokenStatus.ACTIVE.getValue());
            tokenSessionRepository.save(tokenSession);
        }
        return tokenSession;
    }

    @Override
    public void batchInactiveAccount() {
        boolean isContinue = false;
        List<User> userUpdated = new ArrayList<>();
        List<User> users = userRepository.findAllByStatus(UserStatus.ACTIVE.getValue());
        for (User user : users) {
            isContinue = false;
            long day = getDayFromCurrent(user.getLastLogin());
            if (day >= lastLoginExpireWarning && day < lastLoginExpireInactive) {
                // send mail notification
                Map<String, String> variables = new HashMap<>();
                variables.put(USER_NAME_VARIABLE, user.getFullName());
                variables.put(USER_EMAIL_VARIABLE, user.getEmail());
                long remainingDay = lastLoginExpireInactive - day;
                variables.put("%%PASSWORD_EXPIRATION_DAY%%", String.valueOf(remainingDay));
                variables.put("%%PASSWORD_EXPIRATION_DATE%%", DateTimeUtils.formatOffsetDateTime(OffsetDateTime.now().plusDays(remainingDay), "dd/MM/yyyy"));
                sendEmail(user.getEmail(), "ET6", variables);
            }
            if (day >= lastLoginExpireInactive) {
                // inactive account and send email
                user.setStatus(UserStatus.INACTIVE.getValue());
                userUpdated.add(user);
                LinkedHashMap<String, String> variables = new LinkedHashMap<>();
                variables.put(USER_NAME_VARIABLE, user.getFullName());
                sendEmail(user.getEmail(), "ET7", variables);
                isContinue = true;
            }
            // check last change password
            OffsetDateTime lastUpdatedPassword = user.getLastUpdatedPassword();
            if (lastUpdatedPassword == null) {
                isContinue = true;
            }
            long passwordExpired = getDayFromCurrent(lastUpdatedPassword);
            if (passwordExpiredSendNotification.contains(passwordExpired)) {
                LinkedHashMap<String, String> variables = new LinkedHashMap<>();
                variables.put(USER_NAME_VARIABLE, user.getFullName());
                long remainingTime = passwordExpiredInactive - passwordExpired;
                variables.put("%%PASSWORD_EXPIRATION_DAY%%", String.valueOf(remainingTime));
                variables.put("%%PASSWORD_EXPIRATION_DATE%%", DateTimeUtils.formatOffsetDateTime(user.getLastLogin().plusDays(60), "dd/MM/yyyy"));
                sendEmail(user.getEmail(), "ET24", variables);
                isContinue = true;
            }

            if (isContinue) {
                continue;
            }

            if (passwordExpired >= passwordExpiredInactive) {
                user.setStatus(UserStatus.INACTIVE.getValue());
                userUpdated.add(user);
                Map<String, String> variables = new HashMap<>();
                variables.put(USER_NAME_VARIABLE, user.getFullName());
                variables.put(USER_EMAIL_VARIABLE, user.getEmail());
                sendEmail(user.getEmail(), "ET25", variables);
            }
        }
        if (CollectionUtils.isNotEmpty(userUpdated)) {
            userRepository.saveAll(userUpdated);
        }
    }

    private String getModifiedBy() {
        try {
            if (null != this.request) {
                String username = request.getHeader("x-username");
                if (StringUtils.isNotBlank(username)) {
                    return username;
                }
            }
        } catch (final Exception e) {
            log.info("getCurrentAuditor {}", e.getMessage());
        }
        String systemUserName = System.getProperty("user.name");
        if (StringUtils.isNotBlank(systemUserName)) {
            return systemUserName;
        }
        return "system_user"; // Provide a robust default if all else fails
    }

    private long getDayFromCurrent(OffsetDateTime lastLogin) {
        if (lastLogin == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(lastLogin, OffsetDateTime.now());
    }

    @Override
    public List<String> getEmailsByUserCodes(List<String> userCodes) {
        List<User> userByCode = userRepository.findAllByCodeIn(userCodes);
        if(CollectionUtils.isEmpty(userByCode)) {
            return List.of();
        }
        return userByCode.stream().map(User::getEmail).toList();
    }

    public void expireLoginAttempt(ExpireLoginRequest request) {
        Optional<LoginAttempt> loginAttemptOptional = loginAttemptRepository.findById(request.getLoginAttemptId());
        if (loginAttemptOptional.isPresent()) {
            LoginAttempt loginAttempt = loginAttemptOptional.get();
            loginAttempt.setExpired(request.isExpired());
            loginAttempt.setExpireTime(request.getExpireTime());
            loginAttemptRepository.save(loginAttempt);
        }
        userSessionService.clearSession(request.getLoginAttemptId());
    }

    public LoginAttemptDto getLoginAttempt(String loginAttemptId){
        Optional<LoginAttempt> loginAttemptOptional = loginAttemptRepository.findById(loginAttemptId);
        return loginAttemptOptional.map(this::convertToDto).orElse(null);
    }

    public LoginAttemptDto convertToDto(LoginAttempt loginAttempt) {
        LoginAttemptDto dto = new LoginAttemptDto();
        dto.setId(loginAttempt.getId());
        dto.setUserId(loginAttempt.getUserId());
        dto.setLoginTime(loginAttempt.getLoginTime());
        dto.setExpireTime(loginAttempt.getExpireTime());
        dto.setExpired(loginAttempt.isExpired());
        return dto;
    }

    @Override
    public UserResponse resolveUserFromLoginAttempt(UUID loginAttemptId) {
        log.info("Resolving user from login attempt: {}", loginAttemptId);

        UserSessionInfo userSessionInfo = userSessionInfoRepository.findByLoginAttemptId(loginAttemptId.toString())
                .orElseThrow(() -> {
                    log.error("No session found for loginAttemptId: {}", loginAttemptId);
                    return new ValidationException(AlertMessages.alert(ValidationAlertEnum.UNAUTHORIZED));
                });

        log.debug("Found session for email: {}, lastActive: {}", userSessionInfo.getEmail(), userSessionInfo.getLastActive());

        long minutesSinceLastActive = ChronoUnit.MINUTES.between(userSessionInfo.getLastActive(), OffsetDateTime.now());
        log.debug("Minutes since last active: {}, timeout threshold: {}", minutesSinceLastActive, sessionTimeoutMinutes);

        if (minutesSinceLastActive > sessionTimeoutMinutes) {
            log.warn("Session expired for loginAttemptId: {} (inactive for {} minutes)", loginAttemptId, minutesSinceLastActive);
            userSessionInfoRepository.delete(userSessionInfo);
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.UNAUTHORIZED));
        }

        log.info("Session valid, returning user: {}", userSessionInfo.getEmail());
        return getUserByEmail(userSessionInfo.getEmail());
    }

    @Override
    public UserResponse register(UserRegisterRequest registerRequest) {
        // Placeholder implementation for register
        // This will need actual business logic later.
        log.warn("register() method is not fully implemented and returns null.");
        return null;
    }

    @Override
    public void verifyRera(ReraVerificationRequest verificationRequest) {
        // Placeholder implementation for verifyRera
        // This will need actual business logic later.
        log.warn("verifyRera() method is not fully implemented.");
        // Potentially, this method could update user's RERA verification status or throw an exception.
    }

    @Override
    public UserResponse inviteOwner(OwnerInviteRequest inviteRequest) {
        // Placeholder implementation for inviteOwner
        // This will need actual business logic later.
        log.warn("inviteOwner() method is not fully implemented and returns null.");
        return null; // Or throw an UnsupportedOperationException
    }

    @Override
    public List<UserResponse> getInvitedOwners() {
        // Placeholder implementation: return an empty list as there's no direct repository method for this yet.
        log.warn("getInvitedOwners() method is not fully implemented and returns an empty list.");
        return new ArrayList<>();
    }

    @Override
    public UserResponse getCurrentUser() {
        log.info("Attempting to retrieve current user from request header.");
        String userEmail = null;
        try {
            if (null != this.request) {
                userEmail = request.getHeader("x-username"); // Assuming email is passed in x-username header
                if (StringUtils.isBlank(userEmail)) {
                    // Fallback to x-email if x-username is not present
                    userEmail = request.getHeader("x-email");
                }
            }
        } catch (final Exception e) {
            log.warn("Could not retrieve user email from request header: {}", e.getMessage());
        }

        if (StringUtils.isBlank(userEmail)) {
            log.error("No user email found in request headers for current user.");
            throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.UNAUTHORIZED));
        }

        log.debug("Found current user email: {}", userEmail);
        return getUserByEmail(userEmail);
    }
}
