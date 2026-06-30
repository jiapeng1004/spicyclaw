package icu.jiapeng.spicyclaw.connector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterMcpConnectorRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 32) String transport,
        @NotBlank @Size(max = 512) String endpoint,
        @Size(max = 500) String description) {
}
