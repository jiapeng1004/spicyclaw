package icu.jiapeng.spicyclaw.security;

import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spicyclaw.security.enabled=true",
        "spicyclaw.security.jwt.secret=test-jwt-secret-key-at-least-32-characters-long",
        "spring.docker.compose.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration"
})
class JwtTokenServiceTest {

    @Autowired
    JwtTokenService jwtTokenService;

    @Test
    void issueAndParseToken() {
        UserProfile profile = new UserProfile("user-1", "admin", "Admin");
        JwtTokenService.TokenIssueResult issued = jwtTokenService.issueToken(profile);

        assertThat(issued.accessToken()).isNotBlank();
        assertThat(issued.tokenType()).isEqualTo("Bearer");
        assertThat(issued.expiresIn()).isPositive();

        var auth = jwtTokenService.parseAuthentication(issued.accessToken());
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getName()).isEqualTo("admin");
    }
}
