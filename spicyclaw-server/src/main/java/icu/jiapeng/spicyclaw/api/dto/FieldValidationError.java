package icu.jiapeng.spicyclaw.api.dto;

/**
 * 单字段校验错误。
 *
 * @param field   字段名或路径
 * @param message 错误说明
 */
public record FieldValidationError(String field, String message) {
}
