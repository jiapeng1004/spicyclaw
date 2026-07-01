package icu.jiapeng.spicyclaw.api.dto;

/**
 * 通用 API 错误响应（RFC 7807 风格）。
 */
public record ApiErrorResponse(String type, String title, int status, String detail) {

    private static final String PROBLEM_TYPE = "about:blank";

    public static ApiErrorResponse unauthorized(String detail) {
        return new ApiErrorResponse(PROBLEM_TYPE, "Unauthorized", 401, detail);
    }

    public static ApiErrorResponse badRequest(String detail) {
        return new ApiErrorResponse(PROBLEM_TYPE, "Bad Request", 400, detail);
    }
}
