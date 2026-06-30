package icu.jiapeng.spicyclaw.usage.dto;

import java.util.List;

/**
 * 用量管理页聚合数据。
 */
public record UsageOverviewResponse(
        UsagePackageResponse activityPackage,
        UsagePackageResponse addonPackage,
        List<UsageRecordResponse> records) {
}
