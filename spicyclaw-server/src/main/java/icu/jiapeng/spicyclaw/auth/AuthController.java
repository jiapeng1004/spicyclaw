package icu.jiapeng.spicyclaw.auth;

import icu.jiapeng.spicyclaw.auth.dto.LoginRequest;
import icu.jiapeng.spicyclaw.auth.dto.LoginResponse;
import icu.jiapeng.spicyclaw.auth.dto.UserResponse;
import icu.jiapeng.spicyclaw.security.ClawUserDetailsService;
import icu.jiapeng.spicyclaw.security.JwtTokenService;
import icu.jiapeng.spicyclaw.security.UserProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spicyclaw.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final ClawUserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
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

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return toResponse(userDetailsService.requireProfileByUsername(authentication.getName()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // JWT 无状态，客户端丢弃令牌即可。
    }

    private UserResponse toResponse(UserProfile profile) {
        return new UserResponse(profile.id(), profile.username(), profile.displayName());
    }
}
