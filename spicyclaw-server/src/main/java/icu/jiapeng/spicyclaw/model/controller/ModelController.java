package icu.jiapeng.spicyclaw.model.controller;

import icu.jiapeng.spicyclaw.model.ClawModelRegistryService;
import icu.jiapeng.spicyclaw.model.dto.CreateModelRequest;
import icu.jiapeng.spicyclaw.model.dto.ModelResponse;
import icu.jiapeng.spicyclaw.model.dto.UpdateModelRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@Validated
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ModelController {

    private final ClawModelRegistryService modelRegistryService;

    @GetMapping
    public List<ModelResponse> listModels() {
        return modelRegistryService.listModels();
    }

    @GetMapping("/{slug}")
    public ModelResponse getModel(
            @PathVariable
            @NotBlank(message = "slug 不能为空")
            @Size(max = 128, message = "slug 不能超过128个字符")
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 格式无效")
            String slug) {
        return modelRegistryService.getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelResponse createModel(@Valid @RequestBody CreateModelRequest request) {
        return modelRegistryService.create(request);
    }

    @PutMapping("/{slug}")
    public ModelResponse updateModel(
            @PathVariable
            @NotBlank(message = "slug 不能为空")
            @Size(max = 128, message = "slug 不能超过128个字符")
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 格式无效")
            String slug,
            @Valid @RequestBody UpdateModelRequest request) {
        return modelRegistryService.update(slug, request);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(
            @PathVariable
            @NotBlank(message = "slug 不能为空")
            @Size(max = 128, message = "slug 不能超过128个字符")
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 格式无效")
            String slug) {
        modelRegistryService.delete(slug);
    }
}
