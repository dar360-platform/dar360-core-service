package ae.dar360.user.mapper.impl;

import ae.dar360.user.dto.UserResponse;
import ae.dar360.user.mapper.UseResponseMapper;
import ae.dar360.user.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UseResponseMapperImpl implements UseResponseMapper {

  @Override
  public User toEntity(UserResponse dto) {
    if (dto == null) {
      return null;
    }

    User user = new User();
    user.setCreatedDate(dto.getCreatedDate());
    user.setModifiedDate(dto.getModifiedDate());
    user.setCreatedBy(dto.getCreatedBy());
    user.setId(dto.getId());
    user.setCode(dto.getCode());
    user.setEmployeeId(dto.getEmployeeId());
    user.setFullName(dto.getFullName());
    user.setEmail(dto.getEmail());
    user.setLineManagerEmail(dto.getLineManagerEmail());
    user.setStatus(dto.getStatus());
    user.setLock(dto.isLock());
    user.setMobile(dto.getMobile());
    user.setApprovedLimit(dto.getApprovedLimit());
    user.setMaxApprovedLimit(dto.getMaxApprovedLimit());
    user.setModifiedBy(dto.getModifiedBy());

    return user;
  }

  @Override
  public UserResponse toDto(User entity) {
    if (entity == null) {
      return null;
    }

    UserResponse userResponse = new UserResponse();
    userResponse.setId(entity.getId());
    userResponse.setCode(entity.getCode());
    userResponse.setEmployeeId(entity.getEmployeeId());
    userResponse.setFullName(entity.getFullName());
    userResponse.setEmail(entity.getEmail());
    userResponse.setLineManagerEmail(entity.getLineManagerEmail());
    userResponse.setMobile(entity.getMobile());
    userResponse.setLock(entity.isLock());
    userResponse.setStatus(entity.getStatus());
    userResponse.setCreatedDate(entity.getCreatedDate());
    userResponse.setModifiedDate(entity.getModifiedDate());
    userResponse.setLastLogin(entity.getLastLogin());
    userResponse.setCreatedBy(entity.getCreatedBy());
    userResponse.setModifiedBy(entity.getModifiedBy());
    userResponse.setApprovedLimit(entity.getApprovedLimit());
    userResponse.setMaxApprovedLimit(entity.getMaxApprovedLimit());

    return userResponse;
  }

  @Override
  public List<User> toEntities(List<UserResponse> dtos) {
    if (dtos == null) {
      return null;
    }

    List<User> entities = new ArrayList<>(dtos.size());
    for (UserResponse dto : dtos) {
      entities.add(toEntity(dto));
    }
    return entities;
  }

  @Override
  public List<UserResponse> toDtos(List<User> entities) {
    if (entities == null) {
      return null;
    }

    List<UserResponse> dtos = new ArrayList<>(entities.size());
    for (User entity : entities) {
      dtos.add(toDto(entity));
    }
    return dtos;
  }
}
