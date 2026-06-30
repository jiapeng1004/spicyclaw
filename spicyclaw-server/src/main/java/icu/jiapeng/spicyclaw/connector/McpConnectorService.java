package icu.jiapeng.spicyclaw.connector;

import icu.jiapeng.spicyclaw.connector.dto.McpConnectorResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class McpConnectorService {

    private final CopyOnWriteArrayList<McpConnectorResponse> connectors = new CopyOnWriteArrayList<>();

    public List<McpConnectorResponse> list() {
        return new ArrayList<>(connectors);
    }

    public McpConnectorResponse register(String name, String transport, String endpoint, String description) {
        McpConnectorResponse created = new McpConnectorResponse(
                UUID.randomUUID().toString(),
                name,
                transport,
                endpoint,
                description == null ? "" : description,
                true,
                "未连接");
        connectors.add(created);
        return created;
    }

    public void remove(String id) {
        connectors.removeIf(c -> c.id().equals(id));
    }
}
