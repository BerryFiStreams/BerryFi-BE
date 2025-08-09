package com.berryfi.portal.dto.project;

/**
 * Response DTO for project configuration.
 */
public class ProjectConfigResponse {
    private boolean success = true;
    private Object data = new Object();

    public ProjectConfigResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
