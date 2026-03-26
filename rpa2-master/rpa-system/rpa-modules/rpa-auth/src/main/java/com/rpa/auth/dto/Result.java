package com.rpa.auth.dto;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 错误响应（默认401）
     */
    public static <T> Result<T> error(String message) {
        return error(401, message);
    }

    /**
     * 错误响应（自定义状态码）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(null);
        return result;
    }

    /**
     * 错误响应（带数据）
     */
    public static <T> Result<T> error(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }
}
