package barinov.task.management.system.util;

import barinov.task.management.system.dto.CommentDTO;
import barinov.task.management.system.dto.ShowCommentDTO;
import barinov.task.management.system.models.Comment;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ConverterCommentAndCommentDTOs {
    private final ModelMapper modelMapper;

    public ConverterCommentAndCommentDTOs(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public Comment convertToComment(CommentDTO commentDTO) {
        return modelMapper.map(commentDTO, Comment.class);
    }

    public CommentDTO convertToCommentDTO(Comment comment) {
        return modelMapper.map(comment, CommentDTO.class);
    }

    public ShowCommentDTO convertToShowCommentDTO(Comment comment) {
        return modelMapper.map(comment, ShowCommentDTO.class);
    }
}
