package com.lending.dar360UserService.user.controller;

import com.lending.dar360UserService.user.dto.RefreshTokenDto;
import com.lending.dar360UserService.user.model.RefreshToken;
import com.lending.dar360UserService.user.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/refresh")
@RequiredArgsConstructor
@Tag(name = "Refresh Token", description = "the Refresh Token API")
public class RefreshTokenController {

  private final RefreshTokenService refreshTokenService;

  @GetMapping("/{token}")
  @Operation(summary = "Find by token", description = "Find by token")
  ResponseEntity<RefreshTokenDto> findByToken(
          @Parameter(description = "Token", in = ParameterIn.PATH) @PathVariable("token") String token) {
    return ResponseEntity.ok(refreshTokenService.findByToken(token));
  }

  @PostMapping
  @Operation(summary = "Save refresh token", description = "Save refresh token")
  ResponseEntity<RefreshToken> save(@RequestBody @Valid RefreshTokenDto refreshToken) {
    return ResponseEntity.ok(refreshTokenService.save(refreshToken));
  }

  @DeleteMapping("/{token}")
  @Operation(summary = "Delete by token", description = "Delete by token")
  ResponseEntity<Integer> deleteByToken(
          @Parameter(description = "Token", in = ParameterIn.PATH) @PathVariable("token") String token) {
    return ResponseEntity.ok(refreshTokenService.deleteByToken(token));
  }

  @DeleteMapping("/{userId}/deleteByUser")
  @Operation(summary = "Delete by user id", description = "Delete by user id")
  ResponseEntity<Integer> deleteByUser(
          @Parameter(description = "Id of user", example = "fdbd62ba-041a-447c-b021-88c0be2d0d21", in = ParameterIn.PATH) @PathVariable("userId") String userId) {
    return ResponseEntity.ok(refreshTokenService.deleteByUserId(userId));
  }
}
