package icu.jiapeng.spicyclaw.skill.controller;

import icu.jiapeng.spicyclaw.skill.ClawSkillRegistry;
import icu.jiapeng.spicyclaw.skill.SkillConfigMapper;
import icu.jiapeng.spicyclaw.skill.dto.InstallSkillRequest;
import icu.jiapeng.spicyclaw.skill.dto.SkillConfigResponse;
import icu.jiapeng.spicyclaw.skill.dto.SkillEnabledRequest;
import icu.jiapeng.spicyclaw.skill.dto.SkillResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Validated
@RequiredArgsConstructor
public class SkillController {

    private final ClawSkillRegistry skillRegistry;
    private final SkillConfigMapper skillConfigMapper;

    @GetMapping
    public List<SkillResponse> listSkills() {
        return skillRegistry.listSkills();
    }

    @PostMapping("/install")
    public SkillResponse install(@Valid @RequestBody InstallSkillRequest request) throws IOException {
        return skillRegistry.install(request.path());
    }

    @PutMapping("/{slug}/enabled")
    public SkillResponse setEnabled(
            @PathVariable
            @NotBlank(message = "slug 不能为空")
            @Size(max = 128, message = "slug 不能超过128个字符")
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 只能包含小写字母、数字和连字符")
            String slug,
            @Valid @RequestBody SkillEnabledRequest request) {
        return skillRegistry.setEnabled(slug, request.enabled());
    }

    @PostMapping("/reload")
    public List<SkillResponse> reload() throws IOException {
        skillRegistry.reload();
        return skillRegistry.listSkills();
    }

    @GetMapping("/config")
    public SkillConfigResponse config() {
        return skillConfigMapper.toResponse();
    }
}
