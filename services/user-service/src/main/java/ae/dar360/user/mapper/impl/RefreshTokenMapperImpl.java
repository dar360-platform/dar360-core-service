package ae.dar360.user.mapper.impl;

import ae.dar360.user.dto.RefreshTokenDto;
import ae.dar360.user.mapper.RefreshTokenMapper;
import ae.dar360.user.model.RefreshToken;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapperImpl implements RefreshTokenMapper {

  @Override
  public RefreshToken toEntity(RefreshTokenDto dto) {
    if (dto == null) {
      return null;
    }

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(dto.getId());
    refreshToken.setUserId(dto.getUserId());
    refreshToken.setToken(dto.getToken());
    refreshToken.setExpiryDate(dto.getExpiryDate());

    return refreshToken;
  }

  @Override
  public RefreshTokenDto toDto(RefreshToken entity) {
    if (entity == null) {
      return null;
    }

    RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
    refreshTokenDto.setId(entity.getId());
    refreshTokenDto.setUserId(entity.getUserId());
    refreshTokenDto.setToken(entity.getToken());
    refreshTokenDto.setExpiryDate(entity.getExpiryDate());

    return refreshTokenDto;
  }

  @Override
  public List<RefreshToken> toEntities(List<RefreshTokenDto> dtos) {
    if (dtos == null) {
      return null;
    }

    List<RefreshToken> entities = new ArrayList<>(dtos.size());
    for (RefreshTokenDto dto : dtos) {
      entities.add(toEntity(dto));
    }
    return entities;
  }

  @Override
  public List<RefreshTokenDto> toDtos(List<RefreshToken> entities) {
    if (entities == null) {
      return null;
    }

    List<RefreshTokenDto> dtos = new ArrayList<>(entities.size());
    for (RefreshToken entity : entities) {
      dtos.add(toDto(entity));
    }
    return dtos;
  }
}
