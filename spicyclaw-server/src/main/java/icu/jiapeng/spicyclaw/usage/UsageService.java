package icu.jiapeng.spicyclaw.usage;

import icu.jiapeng.spicyclaw.usage.dto.UsageOverviewResponse;
import icu.jiapeng.spicyclaw.usage.dto.UsagePackageResponse;
import icu.jiapeng.spicyclaw.usage.dto.UsageRecordResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsageService {

    public UsageOverviewResponse overview(int days) {
        return new UsageOverviewResponse(activityPackage(), addonPackage(), listRecords(days));
    }

    private UsagePackageResponse activityPackage() {
        return new UsagePackageResponse(
                "activity",
                "活动赠送包",
                "生效中",
                "活动包额度：2150 积分。活动赠送积分到期后自动失效，请在有效期内使用。",
                null,
                2150,
                0,
                2150,
                "生效中",
                false);
    }

    private UsagePackageResponse addonPackage() {
        return new UsagePackageResponse(
                "addon",
                "加量包",
                null,
                "加量包额度：- 积分。如果专业版到期但加量套餐仍有积分，可继续使用加量资源直至加量资源到期或耗尽。",
                "加量包有效期 1 个月，到期失效。",
                0,
                0,
                0,
                "会员专属",
                true);
    }

    private List<UsageRecordResponse> listRecords(int days) {
        int window = Math.max(1, Math.min(days, 90));
        LocalDate end = LocalDate.now(ZoneOffset.UTC);
        LocalDate start = end.minusDays(window - 1L);
        List<UsageRecordResponse> seed = sampleRecords();
        List<UsageRecordResponse> filtered = new ArrayList<>();
        for (UsageRecordResponse record : seed) {
            LocalDate created = OffsetDateTime.parse(record.createdAt()).toLocalDate();
            if (!created.isBefore(start) && !created.isAfter(end)) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private List<UsageRecordResponse> sampleRecords() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return List.of(
                record("50cfca718e0f4a2b9c1d3e5f6a7b8c9d", 15.14,
                        "@scene#16: \"网站开发\" 一个转码服务", "auto-pro", today.minusDays(1)),
                record("41abde209c114f5aa2b3c4d5e6f70819", 8.52,
                        "帮我写一份周报，重点总结本周 Agent 接入进度", "qwen-plus", today.minusDays(2)),
                record("9f2c8d771a3b4c5d6e7f8091a2b3c4d5e", 22.00,
                        "/skill brainstorming 头脑风暴产品命名方案", "auto-pro", today.minusDays(3)),
                record("c3d4e5f60718293a4b5c6d7e8f901234", 5.30,
                        "分析上传的 sales.csv 并输出可视化建议", "qwen-plus", today.minusDays(4)),
                record("a1b2c3d4e5f60718293a4b5c6d7e8f90", 11.75,
                        "生成项目 README 和快速开始文档", "auto-pro", today.minusDays(5)));
    }

    private UsageRecordResponse record(
            String requestId, double points, String prompt, String model, LocalDate day) {
        return new UsageRecordResponse(
                requestId,
                points,
                prompt,
                model,
                day.atTime(10, 30).atOffset(ZoneOffset.UTC).toString());
    }
}
