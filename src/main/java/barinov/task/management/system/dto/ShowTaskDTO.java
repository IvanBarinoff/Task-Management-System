package barinov.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dto для показа задачи")
public class ShowTaskDTO {

    private Integer id;
    private String description;

    private String status;

    private String priority;

    private int authorId;

    private int executorId;
    private List<ShowCommentDTO> comments;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getExecutorId() {
        return executorId;
    }

    public void setExecutorId(int executorId) {
        this.executorId = executorId;
    }

    public List<ShowCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<ShowCommentDTO> comments) {
        this.comments = comments;
    }
}
