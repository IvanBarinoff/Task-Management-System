package barinov.task.management.system.util;

import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.exceptions.TaskNotCreatedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

@Component
public class GeneratorExceptionWithMessageTaskDTO {

    public void validOrGenerateException(TaskDTO taskDTO, BindingResult bindingResult, TaskDTOValidator taskDTOValidator) {
        taskDTOValidator.validate(taskDTO, bindingResult);

        if(bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();

            for(FieldError fieldError : errors) {
                errorMsg.append(fieldError.getField())
                        .append(" - ").append(fieldError.getDefaultMessage())
                        .append(';');
            }

            throw new TaskNotCreatedException(errorMsg.toString());
        }
    }
}
