package icu.jiapeng.spicyclaw.usage;

import icu.jiapeng.spicyclaw.usage.dto.UsageOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/overview")
    public UsageOverviewResponse overview(@RequestParam(defaultValue = "7") int days) {
        return usageService.overview(days);
    }
}
