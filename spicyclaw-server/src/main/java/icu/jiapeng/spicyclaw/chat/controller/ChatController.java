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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public SessionResponse createSession(@Valid @RequestBody(required = false) CreateSessionRequest request) {
        return chatService.createSession(request);
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
    public SseEmitter streamMessage(
            @PathVariable
            @NotBlank(message = "sessionId 不能为空")
            @Size(max = 36, message = "sessionId 格式无效")
            String sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        chatService.streamReply(sessionId, request, emitter);
        return emitter;
    }
}
