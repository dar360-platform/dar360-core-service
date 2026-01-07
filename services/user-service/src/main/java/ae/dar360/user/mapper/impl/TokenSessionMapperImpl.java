package ae.dar360.user.mapper.impl;

import ae.dar360.user.dto.TokenSessionDto;
import ae.dar360.user.mapper.TokenSessionMapper;
import ae.dar360.user.model.TokenSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TokenSessionMapperImpl implements TokenSessionMapper {

  @Override
  public TokenSession toEntity(TokenSessionDto dto) {
    if (dto == null) {
      return null;
    }

    TokenSession tokenSession = new TokenSession();
    tokenSession.setId(dto.getId());
    if (dto.getUserId() != null) {
      tokenSession.setUserId(UUID.fromString(dto.getUserId()));
    }
    tokenSession.setToken(dto.getToken());
    tokenSession.setExpiryDate(dto.getExpiryDate());

    return tokenSession;
  }

  @Override
  public TokenSessionDto toDto(TokenSession entity) {
    if (entity == null) {
      return null;
    }

    TokenSessionDto tokenSessionDto = new TokenSessionDto();
    tokenSessionDto.setId(entity.getId());
    if (entity.getUserId() != null) {
      tokenSessionDto.setUserId(entity.getUserId().toString());
    }
    tokenSessionDto.setToken(entity.getToken());
    tokenSessionDto.setExpiryDate(entity.getExpiryDate());

    return tokenSessionDto;
  }

  @Override
  public List<TokenSession> toEntities(List<TokenSessionDto> dtos) {
    if (dtos == null) {
      return null;
    }

    List<TokenSession> entities = new ArrayList<>(dtos.size());
    for (TokenSessionDto dto : dtos) {
      entities.add(toEntity(dto));
    }
    return entities;
  }

  @Override
  public List<TokenSessionDto> toDtos(List<TokenSession> entities) {
    if (entities == null) {
      return null;
    }

    List<TokenSessionDto> dtos = new ArrayList<>(entities.size());
    for (TokenSession entity : entities) {
      dtos.add(toDto(entity));
    }
    return dtos;
  }
}
