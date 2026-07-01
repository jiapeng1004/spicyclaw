package icu.jiapeng.spicyclaw.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import icu.jiapeng.spicyclaw.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ApiErrorHttpWriter {

    private final ObjectMapper objectMapper;

    public void writeUnauthorized(HttpServletResponse response, String detail) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, ApiErrorResponse.unauthorized(detail));
    }

    private void write(HttpServletResponse response, int status, ApiErrorResponse body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
