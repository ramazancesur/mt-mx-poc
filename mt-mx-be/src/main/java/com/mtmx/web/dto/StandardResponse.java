package com.mtmx.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> StandardResponse<T> success(T data, String message) {
        return StandardResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> StandardResponse<T> success(T data) {
        return success(data, "İşlem başarıyla tamamlandı");
    }

    public static <T> StandardResponse<T> error(String message) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> StandardResponse<T> error(String message, T data) {
        return StandardResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
} 