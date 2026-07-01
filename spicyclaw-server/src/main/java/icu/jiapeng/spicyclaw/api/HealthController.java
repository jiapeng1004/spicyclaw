package icu.jiapeng.spicyclaw.api;

import icu.jiapeng.spicyclaw.api.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthResponse health() {
        return HealthResponse.ok();
    }
}
