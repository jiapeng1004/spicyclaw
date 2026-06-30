package icu.jiapeng.spicyclaw.chat.controller;

import icu.jiapeng.spicyclaw.chat.dto.CreateSessionRequest;
import icu.jiapeng.spicyclaw.chat.dto.MessageResponse;
import icu.jiapeng.spicyclaw.chat.dto.SendMessageRequest;
import icu.jiapeng.spicyclaw.chat.dto.SessionResponse;
import icu.jiapeng.spicyclaw.chat.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Validated
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/sessions")
    public List<SessionResponse> listSessions() {
        return chatService.listSessions();
    }

    @PostMapping("/sessions")
    public SessionResponse createSession(
            @Valid @RequestBody(required = false) CreateSessionRequest request) {
        String title = request == null ? null : request.title();
        String modelSlug = request == null ? null : request.modelSlug();
        return chatService.createSession(title, modelSlug);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public void deleteSession(
            @PathVariable
            @NotBlank(message = "sessionId 不能为空")
            @Size(max = 36, message = "sessionId 格式无效")
            String sessionId) {
        chatService.deleteSession(sessionId);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<MessageResponse> listMessages(
            @PathVariable
            @NotBlank(message = "sessionId 不能为空")
            @Size(max = 36, message = "sessionId 格式无效")
            String sessionId) {
        return chatService.listMessages(sessionId);
    }

    @PostMapping(value = "/sessions/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMessage(
            @PathVariable
            @NotBlank(message = "sessionId 不能为空")
            @Size(max = 36, message = "sessionId 格式无效")
            String sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        return chatService.streamReply(sessionId, request.content());
    }
}
