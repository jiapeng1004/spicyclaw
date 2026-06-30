package icu.jiapeng.spicyclaw.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.state.AgentStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ClawAgentSessionManager {

    private final ClawAgentFactory agentFactory;
    private final AgentStateStore stateStore;
    private final Map<String, ReActAgent> sessionAgents = new ConcurrentHashMap<>();
    private final Map<String, String> sessionModelRefs = new ConcurrentHashMap<>();

    public ReActAgent getOrCreate(String sessionId, String modelRef) {
        String boundRef = sessionModelRefs.get(sessionId);
        if (boundRef != null && !boundRef.equals(modelRef)) {
            remove(sessionId);
        }
        sessionModelRefs.put(sessionId, modelRef);
        return sessionAgents.computeIfAbsent(
                sessionId, id -> agentFactory.createSessionAgent(id, modelRef));
    }

    public void remove(String sessionId) {
        sessionAgents.remove(sessionId);
        sessionModelRefs.remove(sessionId);
        stateStore.delete(null, sessionId);
    }

    public String requireModelRef(String sessionId, String sessionModelRef) {
        if (StringUtils.hasText(sessionModelRef)) {
            return sessionModelRef;
        }
        throw new IllegalStateException("Session " + sessionId + " has no modelRef configured");
    }
}
