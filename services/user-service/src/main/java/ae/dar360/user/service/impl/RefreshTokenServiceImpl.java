package ae.dar360.user.service.impl;

import ae.dar360.user.dto.RefreshTokenDto;
import ae.dar360.user.mapper.RefreshTokenMapper;
import ae.dar360.user.model.RefreshToken;
import ae.dar360.user.repository.RefreshTokenRepository;
import ae.dar360.user.service.RefreshTokenService;
import ae.dar360.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;
    private  final UserService userService;

    @Override
    public RefreshTokenDto findByToken(String token) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByToken(token);
        if(refreshTokenOptional.isEmpty()){
            return null;
        }
        RefreshTokenDto refreshToken = refreshTokenMapper.toDto(refreshTokenOptional.get());
        refreshToken.setUser(userService.getUserById(refreshToken.getUserId()));
        return refreshToken;
    }

    @Override
    public RefreshToken save(RefreshTokenDto refreshToken) {
        return refreshTokenRepository.save(refreshTokenMapper.toEntity(refreshToken));
    }

    @Override
    @Transactional
    public Integer deleteByToken(String token) {
        return refreshTokenRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public Integer deleteByUserId(String userId) {
        return refreshTokenRepository.deleteByUserId(userId);
    }
}
