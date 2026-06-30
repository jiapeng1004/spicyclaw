package icu.jiapeng.spicyclaw.usage.dto;

/**
 * 单条用量明细。
 */
public record UsageRecordResponse(
        String requestId,
        double pointsConsumed,
        String userPrompt,
        String model,
        String createdAt) {
}
