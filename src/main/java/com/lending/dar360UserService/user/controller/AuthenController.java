package com.lending.dar360UserService.user.controller;

import com.lending.dar360UserService.user.dto.LoginRequest;
import com.lending.dar360UserService.user.dto.UserLoginResponse;
import com.lending.dar360UserService.user.service.AuthenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "the Authentication API")
@Slf4j
public class AuthenController {

    private final AuthenService authenService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login")
    public ResponseEntity<String> login(
            @RequestBody @Valid LoginRequest loginRequest) {
        log.info("Login payload: email={}, browserName={}, osVersion={}", loginRequest.getEmail(), loginRequest.getBrowserName(), loginRequest.getOsVersion());
        return ResponseEntity.ok(authenService.login(loginRequest));
    }

    @PutMapping("/update-last-login/{id}")
    @Operation(summary = "Update time of last login session", description = "Update time of last login session")
    public ResponseEntity<Void> updateLastLogin(
            @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("id") UUID userId) {
        authenService.updateLastLogin(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get information of user login", description = "Get information of user login")
    public ResponseEntity<UserLoginResponse> getUserInfoLogin(
            @Parameter(description = "Email of user", example = "admin@domain.ae", in = ParameterIn.PATH) @PathVariable("email") String email) {
        return ResponseEntity.ok(authenService.getUserInfoLogin(email));
    }
}
