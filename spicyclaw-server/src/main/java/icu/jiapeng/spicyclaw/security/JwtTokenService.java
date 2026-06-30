package icu.jiapeng.spicyclaw.security;

import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String TOKEN_TYPE = "Bearer";

    private final SpicyclawProperties properties;

    public TokenIssueResult issueToken(UserProfile profile) {
        long expiresInSeconds = properties.getSecurity().getJwt().getExpirationHours() * 3600L;
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expiresInSeconds);
        String token = Jwts.builder()
                .subject(profile.username())
                .claim(CLAIM_USER_ID, profile.id())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey())
                .compact();
        return new TokenIssueResult(token, TOKEN_TYPE, expiresInSeconds);
    }

    public Authentication parseAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();
            if (!StringUtils.hasText(username)) {
                throw new JwtException("Token subject missing");
            }
            return new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (JwtException ex) {
            throw ex;
        }
    }

    private SecretKey signingKey() {
        String secret = properties.getSecurity().getJwt().getSecret();
        if (!StringUtils.hasText(secret)) {
            secret = "spicyclaw-dev-jwt-secret-change-in-production-min-32-chars";
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record TokenIssueResult(String accessToken, String tokenType, long expiresIn) {
    }
}
