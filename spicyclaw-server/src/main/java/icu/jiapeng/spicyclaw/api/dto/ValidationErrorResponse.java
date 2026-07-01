package icu.jiapeng.spicyclaw.api.dto;

import java.util.List;

/**
 * 参数校验失败响应（RFC 7807 风格）。
 */
public record ValidationErrorResponse(
        String type, String title, int status, String detail, List<FieldValidationError> errors) {

    private static final String PROBLEM_TYPE = "about:blank";

    public static ValidationErrorResponse ofBody(List<FieldValidationError> errors) {
        return new ValidationErrorResponse(
                PROBLEM_TYPE, "Bad Request", 400, "请求体参数校验失败", List.copyOf(errors));
    }

    public static ValidationErrorResponse ofParams(List<FieldValidationError> errors) {
        return new ValidationErrorResponse(
                PROBLEM_TYPE, "Bad Request", 400, "路径或查询参数校验失败", List.copyOf(errors));
    }
}
