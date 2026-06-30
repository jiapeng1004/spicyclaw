package icu.jiapeng.spicyclaw.skill;

import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 解析并安装 ClawHub 兼容技能（SKILL.md + scripts/references/assets）。
 * 安装目标为持久化目录 {@link SpicyclawProperties#getSkillsStoreDir()}，非运行时缓存目录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClawSkillLoader {

    private static final String SKILL_FILE = "SKILL.md";

    private final SpicyclawProperties properties;

    public LoadedSkill loadFromDirectory(Path skillDir) throws IOException {
        Path skillMd = skillDir.resolve(SKILL_FILE);
        if (!Files.isRegularFile(skillMd)) {
            return null;
        }
        String markdown = Files.readString(skillMd, StandardCharsets.UTF_8);
        SkillFrontmatterParser.Parsed parsed = SkillFrontmatterParser.parse(markdown);
        Map<String, Object> metadata = new LinkedHashMap<>(parsed.metadata());
        String slug = skillDir.getFileName().toString();
        String name = String.valueOf(metadata.getOrDefault("name", slug));
        String description = String.valueOf(metadata.getOrDefault("description", ""));
        return new LoadedSkill(
                slug,
                name,
                description,
                skillDir.toAbsolutePath().toString(),
                metadata);
    }

    public LoadedSkill installFromPath(String sourcePath) throws IOException {
        Path source = Path.of(sourcePath).toAbsolutePath().normalize();
        if (!Files.exists(source)) {
            throw new IllegalArgumentException("Skill path does not exist: " + source);
        }
        Path storeRoot = Path.of(properties.getSkillsStoreDir());
        Files.createDirectories(storeRoot);
        Path targetDir;
        if (Files.isDirectory(source)) {
            targetDir = storeRoot.resolve(source.getFileName());
            materializeDirectory(source, targetDir);
        } else if (source.toString().endsWith(".zip") || source.toString().endsWith(".skill")) {
            Path tempDir = Files.createTempDirectory("spicyclaw-skill-");
            try {
                unzip(source, tempDir);
                Path skillRoot = locateSkillRoot(tempDir);
                LoadedSkill preview = loadFromDirectory(skillRoot);
                if (preview == null) {
                    throw new IllegalStateException("Zip does not contain a valid SKILL.md");
                }
                targetDir = storeRoot.resolve(preview.slug());
                materializeDirectory(skillRoot, targetDir);
            } finally {
                deleteRecursively(tempDir);
            }
        } else {
            throw new IllegalArgumentException("Unsupported skill source: " + source);
        }
        LoadedSkill loaded = loadFromDirectory(targetDir);
        if (loaded == null) {
            throw new IllegalStateException("Installed skill is missing SKILL.md: " + targetDir);
        }
        return loaded;
    }

    public void materializeDirectory(Path source, Path target) throws IOException {
        if (Files.exists(target)) {
            clearDirectory(target);
        }
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : walk.toList()) {
                Path dest = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest);
                }
            }
        }
    }

    public void clearDirectory(Path root) throws IOException {
        deleteRecursively(root);
    }

    private Path locateSkillRoot(Path root) throws IOException {
        if (Files.isRegularFile(root.resolve(SKILL_FILE))) {
            return root;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(path -> SKILL_FILE.equals(path.getFileName().toString()) && Files.isRegularFile(path))
                    .map(Path::getParent)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No SKILL.md found in zip"));
        }
    }

    private void unzip(Path zipFile, Path targetDir) throws IOException {
        try (InputStream input = Files.newInputStream(zipFile);
                ZipInputStream zis = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path dest = targetDir.resolve(entry.getName()).normalize();
                if (!dest.startsWith(targetDir)) {
                    throw new IOException("Zip entry escapes target directory: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(zis, dest);
                }
                zis.closeEntry();
            }
        }
    }

    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * 从磁盘目录解析出的技能元数据。
     *
     * @param slug        目录名 / 唯一标识
     * @param name        SKILL.md front matter 中的 name
     * @param description SKILL.md front matter 中的 description
     * @param path        持久化存储目录绝对路径
     * @param metadata    front matter 完整元数据
     */
    public record LoadedSkill(
            String slug, String name, String description, String path, Map<String, Object> metadata) {
    }
}
