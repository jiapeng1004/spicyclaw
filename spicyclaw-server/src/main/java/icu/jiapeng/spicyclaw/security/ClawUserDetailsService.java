package icu.jiapeng.spicyclaw.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.security.entity.ClawUserEntity;
import icu.jiapeng.spicyclaw.security.mapper.ClawUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClawUserDetailsService implements UserDetailsService {

    private final ClawUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ClawUserEntity entity = userMapper.selectOne(
                new LambdaQueryWrapper<ClawUserEntity>().eq(ClawUserEntity::getUsername, username));
        if (entity == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(entity.getEnabled()))
                .roles("USER")
                .build();
    }

    public UserProfile requireProfileByUsername(String username) {
        ClawUserEntity entity = requireByUsername(username);
        return toProfile(entity);
    }

    public ClawUserEntity requireByUsername(String username) {
        ClawUserEntity entity = userMapper.selectOne(
                new LambdaQueryWrapper<ClawUserEntity>().eq(ClawUserEntity::getUsername, username));
        if (entity == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return entity;
    }

    private UserProfile toProfile(ClawUserEntity entity) {
        return new UserProfile(entity.getId(), entity.getUsername(), entity.getDisplayName());
    }
}
