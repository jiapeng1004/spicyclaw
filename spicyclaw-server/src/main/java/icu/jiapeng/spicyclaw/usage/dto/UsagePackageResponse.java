package icu.jiapeng.spicyclaw.usage.dto;

/**
 * 积分包概览。
 */
public record UsagePackageResponse(
        String id,
        String name,
        String statusLabel,
        String description,
        String hint,
        long totalPoints,
        long usedPoints,
        long remainingPoints,
        String badge,
        boolean purchasable) {
}
