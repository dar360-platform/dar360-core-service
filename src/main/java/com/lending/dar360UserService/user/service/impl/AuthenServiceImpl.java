package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.client.feign.ProductServiceFeign;
import com.lending.dar360UserService.user.client.feign.response.ProductTypeDto;
import com.lending.dar360UserService.user.dto.LoginRequest;
import com.lending.dar360UserService.user.dto.UserLoginResponse;
import com.lending.dar360UserService.user.enums.TokenStatus;
import com.lending.dar360UserService.user.enums.UserStatus;
import com.lending.dar360UserService.user.exception.ValidationException;
import com.lending.dar360UserService.user.exception.constant.ValidationAlertEnum;
import com.lending.dar360UserService.user.exception.dto.AlertMessages;
import com.lending.dar360UserService.user.model.LoginAttempt;
import com.lending.dar360UserService.user.model.LoginFailHistory;
import com.lending.dar360UserService.user.model.TokenSession;
import com.lending.dar360UserService.user.model.User;
import com.lending.dar360UserService.user.repository.LoginAttemptRepository;
import com.lending.dar360UserService.user.repository.LoginFailHistoryRepository;
import com.lending.dar360UserService.user.repository.TokenSessionRepository;
import com.lending.dar360UserService.user.repository.UserRepository;
import com.lending.dar360UserService.user.service.AuthenService;
import com.lending.dar360UserService.user.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenServiceImpl implements AuthenService {

  @Value("${app.max-login-fail:5}")
  private int maxLoginFail;

  @Value("${feature.product-service.enabled:true}")
  private boolean productServiceEnabled;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenSessionRepository tokenSessionRepository;
  private final LoginFailHistoryRepository loginFailHistoryRepository;
  private final LoginAttemptRepository loginAttemptRepository;
  private final ProductServiceFeign productServiceFeign;
  private final UserSessionService userSessionService;

  @Override
  public String login(LoginRequest loginRequest) {
    User user =
            userRepository
                    .findByEmailIgnoreCaseAndStatusNot(loginRequest.getEmail(), UserStatus.DELETE.getValue())
                    .orElseThrow(
                            () ->
                                    new ValidationException(

                                            AlertMessages.alert(ValidationAlertEnum.IEM001)));
    log.info("Login attempt for user: '{}' - Lock status: {}", user.getEmail(), user.isLock());
    userRepository.save(user);

    if (!userSessionService.isLoginAllowed(loginRequest)) {
      throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM020));
    }

    if(!loginRequest.isSsoEnabled()) {
      if (user.isLock()) {
        throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM037));
      }
      if (user.getStatus() != null
              && UserStatus.INACTIVE.getValue() == user.getStatus()) {
        throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM038));
      }
    }
    boolean passwordMatch=false;
    if(!loginRequest.isSsoEnabled()) {
      passwordMatch= passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
    }else {
      passwordMatch = loginRequest.getPassword() != null && loginRequest.getPassword().equalsIgnoreCase(user.getPassword());
    }
    if(!passwordMatch){
      OffsetDateTime now = OffsetDateTime.now();
      LoginFailHistory loginFail = new LoginFailHistory();
      loginFail.setUserId(user.getId());
      loginFail.setLoginTime(now);
      loginFailHistoryRepository.save(loginFail);
      int numberLoginFailRemain =
          maxLoginFail- loginFailHistoryRepository.countByUserIdAndLoginTimeAfter(
                  user.getId(), now.minusHours(1));
      if(numberLoginFailRemain == 0){
        user.setLock(true);
        userRepository.save(user);
        throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM037));
      }
      throw new ValidationException(AlertMessages.alert(ValidationAlertEnum.IEM001));
    }

    String loginAttemptId = createLoginAttempt(user.getId().toString());
    userSessionService.createOrUpdateSession(loginAttemptId, loginRequest);
    return loginAttemptId;
  }


  @Override
  public UserLoginResponse getUserInfoLogin(String email) {
    User user =
        userRepository
            .findByEmailIgnoreCaseAndStatusNot(email, UserStatus.DELETE.getValue())
            .orElseThrow(
                () ->
                    new ValidationException(
                        AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));

    List<String> productTypeIds = new ArrayList<>();
    if (Objects.nonNull(user.getProductType())) {
      productTypeIds = Arrays.asList(user.getProductType().split(","));
    }
    return UserLoginResponse.builder()
        .id(user.getId())
        .password(user.getPassword())
        .email(user.getEmail())
        .code(user.getCode())
        .fullName(user.getFullName())
        .productTypes(this.getProductTypeCodes(productTypeIds))
        .build();
  }

  private List<String> getProductTypeCodes(List<String> productTypeIds) {
    if (!productServiceEnabled || CollectionUtils.isEmpty(productTypeIds)) {
      return Collections.emptyList();
    }

    try {
      List<ProductTypeDto> productTypes = this.productServiceFeign.getProductTypes(null);
      if (CollectionUtils.isEmpty(productTypes)) {
        return Collections.emptyList();
      }

      Map<String, String> productTypeMap = productTypes.stream()
              .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
              .collect(Collectors.toMap(ProductTypeDto::getId, ProductTypeDto::getCode));

      return productTypeIds.stream()
              .map(productTypeMap::get)
              .filter(Objects::nonNull)
              .toList();
    } catch (Exception ex) {
      log.warn("Unable to fetch product types from Product service: {}", ex.getMessage());
      return Collections.emptyList();
    }
  }


  @Transactional
  @Override
  public void updateLastLogin(UUID userId) {
    User user =
            userRepository
                    .findById(userId)
                    .orElseThrow(
                            () ->
                                    new ValidationException(


                                            AlertMessages.alert(ValidationAlertEnum.USER_NOT_EXIST)));
    user.setLastLogin(OffsetDateTime.now());
    user.setModifiedBy(user.getEmail());
    userRepository.save(user);
    Optional<TokenSession> tokenSessionOptional = tokenSessionRepository.findByuserIdAndStatus(userId, TokenStatus.ACTIVE.getValue());
    if(tokenSessionOptional.isPresent()){
      TokenSession tokenSession = tokenSessionOptional.get();
      tokenSession.setStatus(TokenStatus.INACTIVE.getValue());
      tokenSessionRepository.save(tokenSession);
    }
    loginFailHistoryRepository.deleteAll(loginFailHistoryRepository.getAllByUserId(user.getId()));
  }

  public String createLoginAttempt(String userId) {

    loginAttemptRepository.expireAllByUserId(userId, OffsetDateTime.now());

    LoginAttempt loginAttempt = new LoginAttempt();
    loginAttempt.setId(UUID.randomUUID().toString());
    loginAttempt.setUserId(userId);
    loginAttempt.setLoginTime(OffsetDateTime.now());
    loginAttempt.setExpireTime(OffsetDateTime.now().plusHours(2));
    loginAttempt.setExpired(false);
    loginAttemptRepository.save(loginAttempt);
    return loginAttempt.getId();
  }



}
