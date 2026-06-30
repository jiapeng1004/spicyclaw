package icu.jiapeng.spicyclaw.connector;

import icu.jiapeng.spicyclaw.connector.dto.McpConnectorResponse;
import icu.jiapeng.spicyclaw.connector.dto.RegisterMcpConnectorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connectors/mcp")
@RequiredArgsConstructor
public class McpConnectorController {

    private final McpConnectorService mcpConnectorService;

    @GetMapping
    public List<McpConnectorResponse> list() {
        return mcpConnectorService.list();
    }

    @PostMapping
    public McpConnectorResponse register(@Valid @RequestBody RegisterMcpConnectorRequest request) {
        return mcpConnectorService.register(
                request.name(), request.transport(), request.endpoint(), request.description());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String id) {
        mcpConnectorService.remove(id);
    }
}
