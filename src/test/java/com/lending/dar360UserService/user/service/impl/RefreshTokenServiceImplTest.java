package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.dto.RefreshTokenDto;
import com.lending.dar360UserService.user.dto.UserResponse;
import com.lending.dar360UserService.user.mapper.RefreshTokenMapper;
import com.lending.dar360UserService.user.model.Department;
import com.lending.dar360UserService.user.model.RefreshToken;
import com.lending.dar360UserService.user.repository.DepartmentRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.lending.dar360UserService.user.repository.RefreshTokenRepository;
import com.lending.dar360UserService.user.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RefreshTokenServiceImplTest {
  @InjectMocks private RefreshTokenServiceImpl refreshTokenService;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private RefreshTokenMapper refreshTokenMapper;

  @Mock private UserService userService;

  @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void findByTokenSuccess() {
    String token = "121212";
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUserId("userId");
    refreshToken.setExpiryDate(OffsetDateTime.now());
    RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
    refreshTokenDto.setToken(token);
    UserResponse userResponse = new UserResponse();
    refreshTokenDto.setUser(userResponse);
    Mockito.when(this.refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
    Mockito.when(this.refreshTokenMapper.toDto(refreshToken)).thenReturn(refreshTokenDto);
    Mockito.when(this.userService.getUserById("userId")).thenReturn(userResponse);
    Assert.assertNotNull(this.refreshTokenService.findByToken(token));
  }

  @Test
  public void findByTokenFail() {
    String token = "121212";
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUserId("userId");
    refreshToken.setExpiryDate(OffsetDateTime.now());
    RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
    refreshTokenDto.setToken(token);
    UserResponse userResponse = new UserResponse();
    refreshTokenDto.setUser(userResponse);
    Mockito.when(this.refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());
    Assert.assertNull(this.refreshTokenService.findByToken(token));
  }

  @Test
  public void saveTokenSuccess() {
    String token = "121212";
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUserId("userId");
    refreshToken.setExpiryDate(OffsetDateTime.now());
    RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
    refreshTokenDto.setToken(token);
    UserResponse userResponse = new UserResponse();
    refreshTokenDto.setUser(userResponse);
    Mockito.when(this.refreshTokenMapper.toEntity(refreshTokenDto)).thenReturn(refreshToken);
    Mockito.when(this.refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);
    Assert.assertNotNull(this.refreshTokenService.save(refreshTokenDto));
  }

  @Test
  public void testDeleteByToken() {
    String token = "121212";
    Mockito.when(this.refreshTokenRepository.deleteByToken(token)).thenReturn(1);
    Assert.assertNotNull(this.refreshTokenService.deleteByToken(token));
  }

  @Test
  public void testDeleteByUserId() {
    String userId = "121212";
    Mockito.when(this.refreshTokenRepository.deleteByUserId(userId)).thenReturn(1);
    Assert.assertNotNull(this.refreshTokenService.deleteByUserId(userId));
  }
}
