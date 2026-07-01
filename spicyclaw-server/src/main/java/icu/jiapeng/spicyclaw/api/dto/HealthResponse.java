package icu.jiapeng.spicyclaw.api.dto;

/**
 * 健康检查响应。
 *
 * @param status 服务状态，正常时为 ok
 * @param app    应用标识
 */
public record HealthResponse(String status, String app) {

    public static HealthResponse ok() {
        return new HealthResponse("ok", "spicyclaw");
    }
}
