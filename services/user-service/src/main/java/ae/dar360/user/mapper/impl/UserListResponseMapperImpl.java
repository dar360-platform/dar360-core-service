package ae.dar360.user.mapper.impl;

import ae.dar360.user.dto.SearchUserResponseDto;
import ae.dar360.user.mapper.UserListResponseMapper;
import ae.dar360.user.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserListResponseMapperImpl implements UserListResponseMapper {

  @Override
  public User toEntity(SearchUserResponseDto dto) {
    if (dto == null) {
      return null;
    }

    User user = new User();
    user.setModifiedDate(dto.getModifiedDate());
    user.setId(dto.getId());
    user.setCode(dto.getCode());
    user.setEmployeeId(dto.getEmployeeId());
    user.setFullName(dto.getFullName());
    user.setEmail(dto.getEmail());
    user.setStatus(dto.getStatus());
    user.setLastLogin(dto.getLastLogin());
    user.setModifiedBy(dto.getModifiedBy());

    return user;
  }

  @Override
  public SearchUserResponseDto toDto(User entity) {
    if (entity == null) {
      return null;
    }

    SearchUserResponseDto.SearchUserResponseDtoBuilder builder =
        SearchUserResponseDto.builder();

    builder.id(entity.getId());
    builder.code(entity.getCode());
    builder.employeeId(entity.getEmployeeId());
    builder.fullName(entity.getFullName());
    builder.email(entity.getEmail());
    if (entity.getDepartment() != null) {
      if (entity.getDepartment().getId() != null) {
        builder.departmentId(entity.getDepartment().getId().toString());
      }
      builder.departmentName(entity.getDepartment().getName());
    }
    if (entity.getStatus() != null) {
      builder.status(entity.getStatus());
    }
    builder.modifiedBy(entity.getModifiedBy());
    builder.lastLogin(entity.getLastLogin());
    builder.modifiedDate(entity.getModifiedDate());

    return builder.build();
  }

  @Override
  public List<User> toEntities(List<SearchUserResponseDto> dtos) {
    if (dtos == null) {
      return null;
    }

    List<User> entities = new ArrayList<>(dtos.size());
    for (SearchUserResponseDto dto : dtos) {
      entities.add(toEntity(dto));
    }
    return entities;
  }

  @Override
  public List<SearchUserResponseDto> toDtos(List<User> entities) {
    if (entities == null) {
      return null;
    }

    List<SearchUserResponseDto> dtos = new ArrayList<>(entities.size());
    for (User entity : entities) {
      dtos.add(toDto(entity));
    }
    return dtos;
  }
}
