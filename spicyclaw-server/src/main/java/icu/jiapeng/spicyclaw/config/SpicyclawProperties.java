package icu.jiapeng.spicyclaw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spicyclaw")
public class SpicyclawProperties {

    private String home = System.getProperty("user.home") + "/.spicyclaw";
    /** 运行时 skillsDir 文件缓存（启动时清空，按需从 store 刷新）。 */
    private String skillsDir;
    /** 技能持久化存储目录（安装落盘，DB path 指向此处）。 */
    private String skillsStoreDir;
    private ClawHub clawhub = new ClawHub();
    private Cors cors = new Cors();
    private Security security = new Security();

    public String getSkillsDir() {
        return skillsDir != null && !skillsDir.isBlank() ? skillsDir : home + "/skills-cache";
    }

    public String getSkillsStoreDir() {
        return skillsStoreDir != null && !skillsStoreDir.isBlank() ? skillsStoreDir : home + "/skills-store";
    }

    @Data
    public static class ClawHub {
        private String registryUrl = "https://clawhub.ai";
        private boolean enabled = true;
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }

    @Data
    public static class Security {
        /** 是否启用 Spring Security；测试环境可设为 false。 */
        private boolean enabled = true;
        private DefaultUser defaultUser = new DefaultUser();
        private Jwt jwt = new Jwt();
    }

    @Data
    public static class Jwt {
        /** HS256 签名密钥，生产环境务必通过环境变量覆盖。 */
        private String secret = "";
        /** 访问令牌有效时长（小时）。 */
        private long expirationHours = 24;
    }

    @Data
    public static class DefaultUser {
        private String username = "admin";
        private String password = "spicyclaw";
        private String displayName = "Admin";
    }
}
