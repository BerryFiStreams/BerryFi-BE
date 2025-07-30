package com.berryfi.portal.dto.error;

/**
 * DTO for API error responses.
 */
public class ApiError {

    private int statusCode;
    private String message;
    private String error;

    public ApiError() {}

    public ApiError(int statusCode, String message, String error) {
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
