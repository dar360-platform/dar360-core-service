package ae.dar360.user.service;

import ae.dar360.user.dto.RefreshTokenDto;
import ae.dar360.user.model.RefreshToken;

public interface RefreshTokenService {

    RefreshTokenDto findByToken(String token);

    RefreshToken save(RefreshTokenDto refreshToken);

    Integer deleteByToken(String token);

    Integer deleteByUserId(String userId);
}
