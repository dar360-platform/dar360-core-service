package com.lending.dar360UserService.user.service;


import com.lending.dar360UserService.user.dto.*;
import com.lending.dar360UserService.user.model.TokenSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest userRequest);
    UserResponse createUser(CreateUserRequest userRequest, MultipartFile passportPhoto);

    UserResponse getUserById(String id);

    UserResponse updateUser(UUID userId, UpdateUserRequest userRequest);

    void deleteUser(UUID userId);

    void updateUserStatus(String userId, Integer status);

    UserResponse getUserByEmail(String email);

    void unlockUser(String userId);

    TokenSession validateTokenSession(String tokenSession);

    void increaseTokenVerifyTimes(String tokenSession);

    void setupPassword(PasswordDto passwordDto);

    void regenerateTokenSession(String tokenSession);

    Map<UUID, UserDepartmentResponse> getFullnameAndDepartmentName(UserDepartmentRequest request);

    Page<SearchUserResponseDto> searchUser(SearchUserForm searchUserForm);

    void changePassword(PasswordDto passwordDto);

    void validateChangePassword(PasswordDto passwordDto);

    List<String> getEmailsByLevel(final String level);

    List<SearchUserResponseDto> getAllUserExport();

    List<String> getEmailsByDepartment(final String department);

    void forgotPassword(String email);

    void batchInactiveAccount();

    List<UserResponse> getUserByLevel(String level);

    List<UserDepartmentResponse> findByFullNameAndEmail(String fullName, String email);

    List<String> getEmailsByUserCodes(List<String> userCodes);

    void expireLoginAttempt(ExpireLoginRequest request);

    LoginAttemptDto getLoginAttempt(String loginAttemptId);

    UserResponse resolveUserFromLoginAttempt(UUID loginAttemptId);

    ResponseEntity<byte[]> getUserPassportPhoto(String userId);
}
