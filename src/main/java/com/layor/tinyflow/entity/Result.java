package com.layor.tinyflow.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private boolean success;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = "success";
        result.data = data;
        result.success = true;
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = null;
        result.success = false;
        return result;
    }
}