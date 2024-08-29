package barinov.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto для создания задачи")
public class TaskDTO {

    private String description;

    private String status;

    private String priority;

    private int executorId;

    public TaskDTO(String description, String status, String priority, int executorId) {
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.executorId = executorId;
    }

    public TaskDTO() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }
}
