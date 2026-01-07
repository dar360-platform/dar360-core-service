package com.lending.dar360UserService.user.mapper.impl;

import com.lending.dar360UserService.user.dto.CreateUserRequest;
import com.lending.dar360UserService.user.mapper.UserRegisterMapper;
import com.lending.dar360UserService.user.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserRegisterMapperImpl implements UserRegisterMapper {

  @Override
  public User toEntity(CreateUserRequest dto) {
    if (dto == null) {
      return null;
    }

    User user = new User();
    user.setId(dto.getId());
    user.setEmployeeId(dto.getEmployeeId());
    user.setFullName(dto.getFullName());
    user.setEmail(dto.getEmail());
    user.setMobile(dto.getMobile());
    user.setLineManagerEmail(dto.getLineManagerEmail());
    user.setApprovedLimit(dto.getApprovedLimit());
    user.setMaxApprovedLimit(dto.getMaxApprovedLimit());

    return user;
  }

  @Override
  public CreateUserRequest toDto(User entity) {
    if (entity == null) {
      return null;
    }

    CreateUserRequest createUserRequest = new CreateUserRequest();
    createUserRequest.setId(entity.getId());
    createUserRequest.setEmployeeId(entity.getEmployeeId());
    createUserRequest.setFullName(entity.getFullName());
    createUserRequest.setEmail(entity.getEmail());
    createUserRequest.setMobile(entity.getMobile());
    createUserRequest.setLineManagerEmail(entity.getLineManagerEmail());
    createUserRequest.setApprovedLimit(entity.getApprovedLimit());
    createUserRequest.setMaxApprovedLimit(entity.getMaxApprovedLimit());

    return createUserRequest;
  }

  @Override
  public List<User> toEntities(List<CreateUserRequest> dtos) {
    if (dtos == null) {
      return null;
    }

    List<User> entities = new ArrayList<>(dtos.size());
    for (CreateUserRequest dto : dtos) {
      entities.add(toEntity(dto));
    }
    return entities;
  }

  @Override
  public List<CreateUserRequest> toDtos(List<User> entities) {
    if (entities == null) {
      return null;
    }

    List<CreateUserRequest> dtos = new ArrayList<>(entities.size());
    for (User entity : entities) {
      dtos.add(toDto(entity));
    }
    return dtos;
  }
}
