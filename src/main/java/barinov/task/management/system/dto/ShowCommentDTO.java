package barinov.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dto для создания комментария")
public class ShowCommentDTO {
    private Integer id;
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
