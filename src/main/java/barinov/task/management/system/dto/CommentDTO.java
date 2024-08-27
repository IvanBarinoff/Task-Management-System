package barinov.task.management.system.dto;

import jakarta.validation.constraints.NotEmpty;

public class CommentDTO {

    @NotEmpty
    private String text;

    private Integer taskId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
