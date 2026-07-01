package icu.jiapeng.spicyclaw.auth;

import icu.jiapeng.spicyclaw.auth.dto.LoginRequest;
import icu.jiapeng.spicyclaw.auth.dto.LoginResponse;
import icu.jiapeng.spicyclaw.auth.dto.UserResponse;
import icu.jiapeng.spicyclaw.security.ClawUserDetailsService;
import icu.jiapeng.spicyclaw.security.JwtTokenService;
import icu.jiapeng.spicyclaw.security.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spicyclaw.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ClawUserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserProfile profile = userDetailsService.requireProfileByUsername(request.username());
        JwtTokenService.TokenIssueResult token = jwtTokenService.issueToken(profile);
        return new LoginResponse(
                profile.id(),
                profile.username(),
                profile.displayName(),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn());
    }

    public UserResponse currentUser(String username) {
        UserProfile profile = userDetailsService.requireProfileByUsername(username);
        return new UserResponse(profile.id(), profile.username(), profile.displayName());
    }
}
