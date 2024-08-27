package barinov.task.management.system.util;

import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.models.Priority;
import barinov.task.management.system.models.Status;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

@Component
public class TaskDTOValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TaskDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        TaskDTO task = (TaskDTO) target;

        ValidConvertStringToEnum<Status> statusValid = new ValidConvertStringToEnum<>(Status.class);
        ValidConvertStringToEnum<Priority> priorityValid = new ValidConvertStringToEnum<>(Priority.class);

        if(!statusValid.isValidate(task.getStatus()))
            errors.rejectValue("status", "",
                    "У задачи должен быть один из следующих статусов: " + statusValid.valuesEnumToString());

        if(!priorityValid.isValidate(task.getPriority()))
            errors.rejectValue("priority", "",
                    "У задачи должен быть один из следующих приоритетов: " + priorityValid.valuesEnumToString());
    }
}
