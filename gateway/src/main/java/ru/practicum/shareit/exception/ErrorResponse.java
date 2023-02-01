package ru.practicum.shareit.exception;

import lombok.Data;

@Data
public class ErrorResponse {
    private final int status;
    private final String decryption;
    private final String error;
}
