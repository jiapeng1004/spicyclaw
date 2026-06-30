package icu.jiapeng.spicyclaw.skill;

import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import icu.jiapeng.spicyclaw.skill.dto.SkillConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillConfigMapper {

    private final SpicyclawProperties properties;

    public SkillConfigResponse toResponse() {
        SpicyclawProperties.ClawHub clawhub = properties.getClawhub();
        return new SkillConfigResponse(
                properties.getSkillsDir(),
                properties.getSkillsStoreDir(),
                clawhub.getRegistryUrl(),
                clawhub.isEnabled());
    }
}
