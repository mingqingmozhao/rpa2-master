package com.rpa.auth.dto;

public class ApiResponse<T> {
    private int code;
    private String message;
    private String msg;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String message, String msg, T data) {
        this.code = code;
        this.message = message;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, message, null);
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}

