package com.lending.dar360UserService.user.mapper;

import java.util.List;

public interface CommonMapper<D, T> {

  T toEntity(D dto);

  D toDto(T entity);

  List<T> toEntities(List<D> dtos);

  List<D> toDtos(List<T> entities);
}
