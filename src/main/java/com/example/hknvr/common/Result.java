package com.example.hknvr.common;

import java.util.List;

/**
 * 统一响应结果包装类
 * @param <T> 响应数据类型
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private List<String> errors;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（含错误详情）
     */
    public static <T> Result<T> error(int code, String message, List<String> errors) {
        Result<T> result = new Result<>(code, message, null);
        result.setErrors(errors);
        return result;
    }

    // ==================== Getter & Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}