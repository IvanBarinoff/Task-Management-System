package barinov.task.management.system.util;

import barinov.task.management.system.dto.ShowTaskDTO;
import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.models.Comment;
import barinov.task.management.system.models.Task;
import barinov.task.management.system.services.CommentService;
import barinov.task.management.system.services.PersonDetailsService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ConverterTaskAndTaskDTOs {
    private final ModelMapper modelMapper;
    private final PersonDetailsService personDetailsService;
    private final CommentService commentService;

    private final ConverterCommentAndCommentDTOs converterComment;


    public ConverterTaskAndTaskDTOs(ModelMapper modelMapper, PersonDetailsService personDetailsService, CommentService commentService, ConverterCommentAndCommentDTOs converterComment) {
        this.modelMapper = modelMapper;
        this.personDetailsService = personDetailsService;
        this.commentService = commentService;
        this.converterComment = converterComment;
    }

    public Task convertToTask(TaskDTO taskDTO) {
        Task task = modelMapper.map(taskDTO, Task.class);

        if (taskDTO.getExecutorId() > 0) {
            task.setExecutor(personDetailsService.getPersonById(taskDTO.getExecutorId()));
        } else {
            task.setExecutor(null);
        }

        return task;
    }

    public TaskDTO convertToTaskDTO(Task task) {
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);

        if (task.getExecutor() != null) {
            taskDTO.setExecutorId(task.getExecutor().getId());
        } else {
            taskDTO.setExecutorId(0);
        }

        return taskDTO;
    }

    public ShowTaskDTO convertToShowTaskDTO(Task task) {
        ShowTaskDTO showTaskDTO = modelMapper.map(task, ShowTaskDTO.class);
        showTaskDTO.setAuthorId(task.getAuthor().getId());

        if (task.getExecutor() != null) {
            showTaskDTO.setExecutorId(task.getExecutor().getId());
        } else {
            showTaskDTO.setExecutorId(0);
        }
        if(task.getComments() != null) {
            showTaskDTO.setComments(task.getComments().stream()
                    .map(converterComment::convertToShowCommentDTO)
                    .collect(Collectors.toList()));
        }

        return showTaskDTO;
    }
}
