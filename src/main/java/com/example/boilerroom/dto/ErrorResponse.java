package com.example.boilerroom.dto;

// Strukturerat felsvar som returneras av GlobalExceptionHandler.
// Innehåller "timestamp", "status", "error", "message" och "path" (vilken URL som anropades).
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(String timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    // Getters
    public String getTimestamp() {return timestamp;}
    public int getStatus() {return status;}
    public String getError() {return error;}
    public String getMessage() {return message;}
    public String getPath() {return path;}
}
