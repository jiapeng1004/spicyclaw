package icu.jiapeng.spicyclaw.model;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import icu.jiapeng.spicyclaw.model.dto.CreateModelRequest;
import icu.jiapeng.spicyclaw.model.dto.ModelResponse;
import icu.jiapeng.spicyclaw.model.dto.UpdateModelRequest;
import icu.jiapeng.spicyclaw.model.entity.ClawModelEntity;
import icu.jiapeng.spicyclaw.model.mapper.ClawModelMapper;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ModelRegistry;
import io.agentscope.spring.boot.properties.AgentscopeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将数据库中的模型配置同步到 AgentScope {@link ModelRegistry}，并提供 CRUD。
 * <p>
 * 注册键格式：{@code spicyclaw:{slug}}，由 {@code ReActAgent.builder().model(...)} 解析。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ClawModelRegistryService {

    private static final String REGISTRY_PREFIX = "spicyclaw:";
    private static final String DEFAULT_SLUG = "default";

    private final ClawModelMapper modelMapper;
    private final ClawModelFactory modelFactory;
    private final AgentscopeProperties agentscopeProperties;

    private final AtomicBoolean factoryRegistered = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        seedIfEmpty();
        registerFactoryIfNeeded();
        refreshAllRegistered();
    }

    public List<ModelResponse> listModels() {
        return modelMapper.selectList(
                        new LambdaQueryWrapper<ClawModelEntity>().orderByDesc(ClawModelEntity::getUpdatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ModelResponse getBySlug(String slug) {
        return toResponse(requireEnabledOrAny(slug));
    }

    public String defaultRegistryKey() {
        ClawModelEntity entity = modelMapper.selectOne(
                new LambdaQueryWrapper<ClawModelEntity>()
                        .eq(ClawModelEntity::getIsDefault, true)
                        .eq(ClawModelEntity::getEnabled, true)
                        .last("LIMIT 1"));
        if (entity == null) {
            throw new IllegalStateException("No default LLM model configured in database");
        }
        return entity.registryKey();
    }

    public String resolveRegistryKey(String slugOrRef) {
        if (!StringUtils.hasText(slugOrRef)) {
            return defaultRegistryKey();
        }
        if (slugOrRef.startsWith(REGISTRY_PREFIX)) {
            return slugOrRef;
        }
        ClawModelEntity entity = requireEnabled(slugOrRef);
        return entity.registryKey();
    }

    @Transactional
    public ModelResponse create(CreateModelRequest request) {
        ensureSlugAvailable(request.slug());
        OffsetDateTime now = OffsetDateTime.now();
        ClawModelEntity entity = new ClawModelEntity();
        entity.setSlug(request.slug());
        entity.setDisplayName(request.displayName());
        entity.setProvider(request.provider());
        entity.setModelName(request.modelName());
        entity.setApiKey(request.apiKey());
        entity.setBaseUrl(request.baseUrl());
        entity.setStream(request.stream() == null || request.stream());
        entity.setEnableThinking(request.enableThinking());
        entity.setExtraOptions(request.extraOptions());
        entity.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        entity.setEnabled(request.enabled() == null || request.enabled());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearDefaultFlag();
        }
        modelMapper.insert(entity);
        registerOne(entity);
        log.info("Created LLM model config: {} -> {}", entity.getSlug(), entity.registryKey());
        return toResponse(entity);
    }

    @Transactional
    public ModelResponse update(String slug, UpdateModelRequest request) {
        ClawModelEntity entity = requireBySlug(slug);
        if (request.displayName() != null) {
            entity.setDisplayName(request.displayName());
        }
        if (request.provider() != null) {
            entity.setProvider(request.provider());
        }
        if (request.modelName() != null) {
            entity.setModelName(request.modelName());
        }
        if (request.apiKey() != null) {
            entity.setApiKey(request.apiKey());
        }
        if (request.baseUrl() != null) {
            entity.setBaseUrl(request.baseUrl());
        }
        if (request.stream() != null) {
            entity.setStream(request.stream());
        }
        if (request.enableThinking() != null) {
            entity.setEnableThinking(request.enableThinking());
        }
        if (request.extraOptions() != null) {
            entity.setExtraOptions(request.extraOptions());
        }
        if (request.enabled() != null) {
            entity.setEnabled(request.enabled());
        }
        if (request.isDefault() != null) {
            if (request.isDefault()) {
                clearDefaultFlag();
            }
            entity.setIsDefault(request.isDefault());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        modelMapper.updateById(entity);
        registerOne(entity);
        log.info("Updated LLM model config: {}", slug);
        return toResponse(entity);
    }

    @Transactional
    public void delete(String slug) {
        ClawModelEntity entity = requireBySlug(slug);
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            throw new IllegalArgumentException("Cannot delete the default model");
        }
        modelMapper.deleteById(entity.getId());
        log.info("Deleted LLM model config: {}", slug);
    }

    private void seedIfEmpty() {
        Long count = modelMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        var dashscope = agentscopeProperties.getDashscope();
        String apiKey = dashscope.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("No LLM model in database and DASHSCOPE_API_KEY is empty — add a model via /api/models");
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        ClawModelEntity entity = new ClawModelEntity();
        entity.setSlug(DEFAULT_SLUG);
        entity.setDisplayName("Default (DashScope)");
        entity.setProvider("dashscope");
        entity.setModelName(
                StringUtils.hasText(dashscope.getModelName()) ? dashscope.getModelName() : "qwen-plus");
        entity.setApiKey(apiKey);
        entity.setStream(dashscope.isStream());
        entity.setEnableThinking(dashscope.getEnableThinking());
        entity.setIsDefault(true);
        entity.setEnabled(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        modelMapper.insert(entity);
        log.info("Seeded default LLM model from application config: {}", entity.registryKey());
    }

    private void registerFactoryIfNeeded() {
        if (!factoryRegistered.compareAndSet(false, true)) {
            return;
        }
        ModelRegistry.registerFactory(REGISTRY_PREFIX + ".*", modelRef -> {
            String slug = modelRef.substring(REGISTRY_PREFIX.length());
            ClawModelEntity entity = requireEnabled(slug);
            return modelFactory.create(entity);
        });
        log.info("Registered ModelRegistry factory for {}", REGISTRY_PREFIX + "*");
    }

    private void refreshAllRegistered() {
        List<ClawModelEntity> models = modelMapper.selectList(
                new LambdaQueryWrapper<ClawModelEntity>().eq(ClawModelEntity::getEnabled, true));
        for (ClawModelEntity entity : models) {
            registerOne(entity);
        }
    }

    private void registerOne(ClawModelEntity entity) {
        if (!Boolean.TRUE.equals(entity.getEnabled())) {
            return;
        }
        Model model = modelFactory.create(entity);
        ModelRegistry.register(entity.registryKey(), model);
    }

    private void clearDefaultFlag() {
        modelMapper.update(
                null,
                new LambdaUpdateWrapper<ClawModelEntity>().set(ClawModelEntity::getIsDefault, false));
    }

    private void ensureSlugAvailable(String slug) {
        if (modelMapper.selectCount(new LambdaQueryWrapper<ClawModelEntity>().eq(ClawModelEntity::getSlug, slug))
                > 0) {
            throw new IllegalArgumentException("Model slug already exists: " + slug);
        }
    }

    private ClawModelEntity requireBySlug(String slug) {
        ClawModelEntity entity = modelMapper.selectOne(
                new LambdaQueryWrapper<ClawModelEntity>().eq(ClawModelEntity::getSlug, slug));
        if (entity == null) {
            throw new IllegalArgumentException("Model not found: " + slug);
        }
        return entity;
    }

    private ClawModelEntity requireEnabled(String slug) {
        ClawModelEntity entity = requireBySlug(slug);
        if (!Boolean.TRUE.equals(entity.getEnabled())) {
            throw new IllegalArgumentException("Model is disabled: " + slug);
        }
        return entity;
    }

    private ClawModelEntity requireEnabledOrAny(String slug) {
        return requireBySlug(slug);
    }

    private ModelResponse toResponse(ClawModelEntity entity) {
        return new ModelResponse(
                entity.getId(),
                entity.getSlug(),
                entity.registryKey(),
                entity.getDisplayName(),
                entity.getProvider(),
                entity.getModelName(),
                maskApiKey(entity.getApiKey()),
                entity.getBaseUrl(),
                Boolean.TRUE.equals(entity.getStream()),
                entity.getEnableThinking(),
                entity.getExtraOptions(),
                Boolean.TRUE.equals(entity.getIsDefault()),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
                entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
    }

    private static String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
