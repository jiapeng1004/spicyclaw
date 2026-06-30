package icu.jiapeng.spicyclaw.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import icu.jiapeng.spicyclaw.security.entity.ClawUserEntity;
import icu.jiapeng.spicyclaw.security.mapper.ClawUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spicyclaw.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClawUserSeeder {

    private final ClawUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SpicyclawProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultUser() {
        Long count = userMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        SpicyclawProperties.DefaultUser defaults = properties.getSecurity().getDefaultUser();
        if (!StringUtils.hasText(defaults.getUsername()) || !StringUtils.hasText(defaults.getPassword())) {
            log.warn("无用户记录且未配置默认管理员，请通过数据库或配置 spicyclaw.security.default-user 创建用户");
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        ClawUserEntity user = new ClawUserEntity();
        user.setUsername(defaults.getUsername());
        user.setPasswordHash(passwordEncoder.encode(defaults.getPassword()));
        user.setDisplayName(defaults.getDisplayName());
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        log.info("Seeded default user: {}", user.getUsername());
    }

    public void ensureUserExists(String username, String rawPassword, String displayName) {
        if (userMapper.selectCount(new LambdaQueryWrapper<ClawUserEntity>().eq(ClawUserEntity::getUsername, username))
                > 0) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        ClawUserEntity user = new ClawUserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
    }
}
