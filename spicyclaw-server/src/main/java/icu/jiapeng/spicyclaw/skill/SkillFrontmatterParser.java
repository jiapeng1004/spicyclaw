package icu.jiapeng.spicyclaw.skill;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 ClawHub 兼容 SKILL.md 的 YAML front matter（不依赖已废弃的 AgentScope {@code MarkdownSkillParser}）。
 */
final class SkillFrontmatterParser {

    private static final Pattern FRONTMATTER =
            Pattern.compile("^---\\r?\\n(.*?)\\r?\\n---\\r?\\n?", Pattern.DOTALL);

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private SkillFrontmatterParser() {}

    static Parsed parse(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return new Parsed(Map.of(), "");
        }
        Matcher matcher = FRONTMATTER.matcher(markdown);
        if (!matcher.find()) {
            return new Parsed(Map.of(), markdown);
        }
        try {
            Map<String, Object> metadata =
                    YAML.readValue(matcher.group(1), new TypeReference<LinkedHashMap<String, Object>>() {});
            return new Parsed(metadata, markdown.substring(matcher.end()));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid SKILL.md front matter", ex);
        }
    }

    record Parsed(Map<String, Object> metadata, String content) {}
}
