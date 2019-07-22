package com.docutools.users.resources;

import java.util.UUID;

public class NotificationDTO {
    private UUID projectId;
    private String taskName;

    public NotificationDTO() {
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
}
