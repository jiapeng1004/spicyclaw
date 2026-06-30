package icu.jiapeng.spicyclaw.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.agent.ClawAgentSessionManager;
import icu.jiapeng.spicyclaw.chat.dto.MessageResponse;
import icu.jiapeng.spicyclaw.chat.dto.SessionResponse;
import icu.jiapeng.spicyclaw.chat.entity.ChatMessage;
import icu.jiapeng.spicyclaw.chat.entity.ChatSession;
import icu.jiapeng.spicyclaw.chat.mapper.ChatMessageMapper;
import icu.jiapeng.spicyclaw.chat.mapper.ChatSessionMapper;
import icu.jiapeng.spicyclaw.model.ClawModelRegistryService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.event.AgentResultEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ChatService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ClawAgentSessionManager agentSessionManager;
    private final ClawModelRegistryService modelRegistryService;

    public List<SessionResponse> listSessions() {
        return sessionMapper.selectList(
                        new LambdaQueryWrapper<ChatSession>().orderByDesc(ChatSession::getUpdatedAt))
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional
    public SessionResponse createSession(String title, String modelSlug) {
        OffsetDateTime now = OffsetDateTime.now();
        String modelRef = modelRegistryService.resolveRegistryKey(modelSlug);
        ChatSession session = new ChatSession();
        session.setTitle(title == null || title.isBlank() ? "New Chat" : title);
        session.setAgentName("SpicyClaw");
        session.setModelRef(modelRef);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        sessionMapper.insert(session);
        return toSessionResponse(session);
    }

    public List<MessageResponse> listMessages(String sessionId) {
        requireSession(sessionId);
        return messageMapper.selectList(
                        new LambdaQueryWrapper<ChatMessage>()
                                .eq(ChatMessage::getSessionId, sessionId)
                                .orderByAsc(ChatMessage::getCreatedAt))
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public Flux<ServerSentEvent<String>> streamReply(String sessionId, String userContent) {
        ChatSession session = requireSession(sessionId);
        saveMessage(sessionId, "user", userContent);

        ReActAgent agent = agentSessionManager.getOrCreate(
                sessionId, agentSessionManager.requireModelRef(sessionId, session.getModelRef()));
        Msg userMsg = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(userContent).build())
                .build();

        AtomicReference<StringBuilder> assistantBuffer = new AtomicReference<>(new StringBuilder());

        return agent.streamEvents(List.of(userMsg))
                .flatMap(event -> {
                    if (event instanceof TextBlockDeltaEvent delta) {
                        String chunk = delta.getDelta();
                        if (chunk != null && !chunk.isEmpty()) {
                            assistantBuffer.get().append(chunk);
                            return Flux.just(ServerSentEvent.builder(chunk).event("delta").build());
                        }
                    }
                    if (event instanceof AgentResultEvent resultEvent) {
                        String text = resultEvent.getResult().getTextContent();
                        if (assistantBuffer.get().isEmpty() && text != null && !text.isBlank()) {
                            assistantBuffer.get().append(text);
                            return Flux.just(ServerSentEvent.builder(text).event("delta").build());
                        }
                    }
                    return Flux.empty();
                })
                .concatWith(Flux.defer(() -> {
                    persistAssistantReply(session, userContent, assistantBuffer.get().toString());
                    return Flux.just(ServerSentEvent.builder("[DONE]").event("done").build());
                }))
                .onErrorResume(error -> {
                    log.error("Agent stream failed for session {}", sessionId, error);
                    String message = "Error: " + error.getMessage();
                    persistAssistantReply(session, userContent, message);
                    return Flux.just(
                            ServerSentEvent.builder(message).event("error").build(),
                            ServerSentEvent.builder("[DONE]").event("done").build());
                });
    }

    @Transactional
    public void deleteSession(String sessionId) {
        sessionMapper.deleteById(sessionId);
        agentSessionManager.remove(sessionId);
    }

    private void persistAssistantReply(ChatSession session, String userContent, String finalText) {
        if (finalText == null || finalText.isBlank()) {
            return;
        }
        saveMessage(session.getId(), "assistant", finalText);
        session.setUpdatedAt(OffsetDateTime.now());
        if ("New Chat".equals(session.getTitle())) {
            session.setTitle(truncateTitle(userContent));
        }
        sessionMapper.updateById(session);
    }

    private ChatSession requireSession(String sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        return session;
    }

    private void saveMessage(String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(OffsetDateTime.now());
        messageMapper.insert(message);
    }

    private String truncateTitle(String text) {
        String trimmed = text.strip();
        return trimmed.length() <= 40 ? trimmed : trimmed.substring(0, 37) + "...";
    }

    private SessionResponse toSessionResponse(ChatSession session) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getAgentName(),
                session.getModelRef(),
                formatTime(session.getCreatedAt()),
                formatTime(session.getUpdatedAt()));
    }

    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole(),
                message.getContent(),
                formatTime(message.getCreatedAt()));
    }

    private String formatTime(OffsetDateTime time) {
        return time == null ? null : time.format(ISO);
    }
}
