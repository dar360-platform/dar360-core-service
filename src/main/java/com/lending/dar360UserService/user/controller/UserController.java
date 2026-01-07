package com.lending.dar360UserService.user.controller;

import com.lending.dar360UserService.user.dto.*;
import com.lending.dar360UserService.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "the User API")
public class UserController {

  private final UserService userService;

  @PostMapping("/searchUser")
  @Operation(summary = "Search user", description = "Search user")
  public ResponseEntity<Page<?>> searchUser(@RequestBody @Valid SearchUserForm searchUserForm) {
    return ResponseEntity.ok(this.userService.searchUser(searchUserForm));
  }

  @GetMapping("/export")
  @Operation(summary = "Get all users export", description = "Get all users export")
  public ResponseEntity<List<SearchUserResponseDto>> getAllUserExport() {
    return ResponseEntity.ok(this.userService.getAllUserExport());
  }

  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create user", description = "Create user")
  public ResponseEntity<UserResponse> createUser(
      @RequestBody @Valid CreateUserRequest userRequest) {
    return ResponseEntity.ok(this.userService.createUser(userRequest));
  }

  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create user (multipart)", description = "Create user with optional passport photo upload")
  public ResponseEntity<UserResponse> createUserMultipart(
      @RequestPart("user") @Valid CreateUserRequest userRequest,
      @RequestPart(value = "passportPhoto", required = false) MultipartFile passportPhoto
  ) {
    return ResponseEntity.ok(this.userService.createUser(userRequest, passportPhoto));
  }

  @PostMapping("/forgot-password")
  @Operation(summary = "Forgot password", description = "Forgot password")
  public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
    this.userService.forgotPassword(forgotPasswordDto.getEmail());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/validate-token-session/{tokenSession}")
  @Operation(summary = "Validate token session", description = "Validate token session")
  public ResponseEntity<Void> validateTokenSession(
          @Parameter(description = "Token session", in = ParameterIn.PATH) @PathVariable("tokenSession") String tokenSession) {
    this.userService.validateTokenSession(tokenSession);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/increase-token-verify-times/{tokenSession}")
  @Operation(summary = "Increase token verify times", description = "Increase token verify times")
  public ResponseEntity<Void> increaseTokenVerifyTimes(
          @Parameter(description = "Token session", in = ParameterIn.PATH) @PathVariable("tokenSession") String tokenSession) {
    this.userService.increaseTokenVerifyTimes(tokenSession);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/regenerate-token-session/{tokenSession}")
  @Operation(summary = "Regenerate token session", description = "Regenerate token session")
  public ResponseEntity<Void> regenerateTokenSession(
          @Parameter(description = "Token session", in = ParameterIn.PATH) @PathVariable("tokenSession") String tokenSession) {
    this.userService.regenerateTokenSession(tokenSession);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/submit")
  @Operation(summary = "Setup password", description = "Setup password")
  public ResponseEntity<Void> setupPassword(@RequestBody @Valid PasswordDto passwordDto) {
    this.userService.setupPassword(passwordDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/change")
  @Operation(summary = "Change password", description = "Change password")
  public ResponseEntity<Void> changePassword(@RequestBody @Valid PasswordDto passwordDto) {
    this.userService.changePassword(passwordDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/password/validate-change-password")
  @Operation(summary = "Validate change password", description = "Validate change password")
  public ResponseEntity<Void> validateChangePassword(@RequestBody @Valid PasswordDto passwordDto) {
    this.userService.validateChangePassword(passwordDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by id", description = "Get user by id")
  public ResponseEntity<UserResponse> getUserById(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("id") String id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @GetMapping("/{email}/byEmail")
  @Operation(summary = "Get user by email", description = "Get user by email")
  public ResponseEntity<UserResponse> getUserByEmail(
          @Parameter(description = "Email of user", example = "admin@domain.com", in = ParameterIn.PATH) @PathVariable("email") String email) {
    return ResponseEntity.ok(userService.getUserByEmail(email));
  }

  @GetMapping("/{userId}/passport-photo")
  @Operation(summary = "Get user passport photo", description = "Get user passport photo")
  public ResponseEntity<byte[]> getUserPassportPhoto(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("userId") String userId) {
    return userService.getUserPassportPhoto(userId);
  }

  @PutMapping("/{userId}")
  @Operation(summary = "Update user", description = "Update user")
  public ResponseEntity<UserResponse> updateUser(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH)
          @PathVariable("userId") UUID userId,
          @RequestBody @Valid UpdateUserRequest userRequest) {
    return ResponseEntity.ok(userService.updateUser(userId, userRequest));
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "Delete user", description = "Delete user")
  public ResponseEntity<Void> deleteUser(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH)
          @PathVariable("userId") UUID userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{userId}/active/{status}")
  @Operation(summary = "Update user status", description = "Update user status")
  public ResponseEntity<Void> updateUserStatus(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("userId") String userId,
          @Parameter(description = "Status of user", example = "1", in = ParameterIn.PATH) @PathVariable("status") Integer status) {
    userService.updateUserStatus(userId, status);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PutMapping("/{userId}/unlock")
  @Operation(summary = "Unlock user", description = "Unlock user")
  public ResponseEntity<Void> unlockUser(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("userId") String userId) {
    userService.unlockUser(userId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/user-department")
  @Operation(summary = "Get user full name and department name", description = "Get user full name and department name")
  public ResponseEntity<Map<UUID, UserDepartmentResponse>> getFullnameAndDepartmentName(
      @RequestBody UserDepartmentRequest request) {
    return ResponseEntity.ok(userService.getFullnameAndDepartmentName(request));
  }

  @GetMapping("/emails/level")
  @Operation(summary = "Get list emails by user level", description = "Get list emails by user level")
  public ResponseEntity<List<String>> getEmailsByUserLevel(
          @Parameter(description = "Level of user", example = "L1", in = ParameterIn.QUERY) @RequestParam("level") String level) {
    return ResponseEntity.ok(userService.getEmailsByLevel(level));
  }

  @GetMapping("/emails/department")
  @Operation(summary = "Get list emails by user department", description = "Get list emails by user department")
  public ResponseEntity<List<String>> getEmailsByUserDepartment(
          @Parameter(description = "Department of user", example = "Back Office | Credit | Sale | Compliance", in = ParameterIn.QUERY) @RequestParam("department") String department) {
    return ResponseEntity.ok(userService.getEmailsByDepartment(department));
  }

  @PostMapping("/batch/inactive-account")
  @Operation(summary = "Batch inactive account", description = "Batch inactive account")
  public ResponseEntity<Void> batchInactiveAccount() {
    CompletableFuture.runAsync(userService::batchInactiveAccount);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/level")
  @Operation(summary = "Get list users by user level", description = "Get list users by user level")
  public ResponseEntity<List<UserResponse>> getUsersByUserLevel(
          @Parameter(description = "Level of user", example = "L1", in = ParameterIn.QUERY) @RequestParam("level") String level) {
    return ResponseEntity.ok(userService.getUserByLevel(level));
  }

  @GetMapping("/find-by-full-name-email")
  @Operation(summary = "find user by full name or email", description = "find user by full name or email")
  public ResponseEntity<List<UserDepartmentResponse>> findByFullNameAndEmail(
          @Parameter(description = "Email Or full name", example = "test@abc.com", in = ParameterIn.QUERY, required = true) @RequestParam("searchText") String searchText) {
    return ResponseEntity.ok(this.userService.findByFullNameAndEmail(searchText, searchText));
  }

  @GetMapping("/email-by-code")
  @Operation(summary = "Get list email by user code", description = "Get list email by user code")
  public ResponseEntity<List<String>> getEmailsByUserCode(
          @Parameter(description = "List User Code", example = "DAR360-312312", in = ParameterIn.QUERY) @RequestParam("userCodes") List<String> userCodes) {
    return ResponseEntity.ok(userService.getEmailsByUserCodes(userCodes));
  }

  @PostMapping("/expire-login-attempt")
  @Operation(summary = "Expire login attempt", description = "Expire login attempt")
  public ResponseEntity<Void> expireLoginAttempt(@RequestBody ExpireLoginRequest request) {
    userService.expireLoginAttempt(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/login-attempts/{loginAttemptId}")
  public LoginAttemptDto getLoginAttempt(@PathVariable String loginAttemptId) {
    return userService.getLoginAttempt(loginAttemptId);
  }

  @GetMapping("/from-token/{loginAttemptId}")
  @Operation(summary = "Validate token and get user details", description = "Validate token and get user details")
  public ResponseEntity<UserResponse> validateTokenAndGetUser(
          @Parameter(description = "Login Attempt Id", in = ParameterIn.PATH) @PathVariable("loginAttemptId") UUID loginAttemptId) {
    return ResponseEntity.ok(userService.resolveUserFromLoginAttempt(loginAttemptId));
  }
}
