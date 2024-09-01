package barinov.task.management.system.controllers;

import barinov.task.management.system.dto.CommentDTO;
import barinov.task.management.system.dto.ShowTaskDTO;
import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.exceptions.*;
import barinov.task.management.system.models.Comment;
import barinov.task.management.system.models.Priority;
import barinov.task.management.system.models.Status;
import barinov.task.management.system.security.PersonDetails;
import barinov.task.management.system.services.TaskService;
import barinov.task.management.system.util.*;
import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
@SecurityRequirement(name = "JWT")
@Tag(name="Task Controller", description="Контроллер для управления задачами.")
public class TaskController {

    private final TaskService taskService;
    private final TaskDTOValidator taskDTOValidator;
    private final ConverterTaskAndTaskDTOs converterTask;
    private final GeneratorExceptionWithMessageTaskDTO generatorExceptionTaskDTO;
    private final ConverterCommentAndCommentDTOs converterComment;

    @Autowired
    public TaskController(TaskService taskService, TaskDTOValidator taskValidator, ConverterTaskAndTaskDTOs converter, GeneratorExceptionWithMessageTaskDTO generatorExceptionTaskDTO, ConverterCommentAndCommentDTOs converterComment) {
        this.taskService = taskService;
        this.taskDTOValidator = taskValidator;
        this.converterTask = converter;
        this.generatorExceptionTaskDTO = generatorExceptionTaskDTO;
        this.converterComment = converterComment;
    }

    @Operation(
            summary = "Создать задачу",
            description = "Позволяет создать новою задачу"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Map<String, Integer> createTask(@RequestBody TaskDTO taskDTO,
                                          BindingResult bindingResult) {

        generatorExceptionTaskDTO.validOrGenerateException(taskDTO, bindingResult, taskDTOValidator);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PersonDetails authorDetails = (PersonDetails) authentication.getPrincipal();

        Integer taskId = taskService.saveTaskAndGetId(converterTask.convertToTask(taskDTO), authorDetails.getPerson());

        return Map.of("taskId", taskId);
    }

    @Operation(
            summary = "Изменить задачу",
            description = "Позволяет изменить задачу по её id. Доступно только автору задачи."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{taskId}")
    public void editTask(@PathVariable("taskId") int id,
                         @RequestBody JsonPatch jsonPatch,
                         @Parameter(hidden = true) TaskDTO taskDTO, BindingResult bindingResult,
                         Principal principal) {

        taskService.editTaskById(jsonPatch, id, principal, bindingResult, taskDTOValidator);
    }

    @Operation(
            summary = "Увидеть задачу",
            description = "Позволяет увидеть задачу по её id"
    )
    @GetMapping("/{taskId}")
    public ShowTaskDTO showTask(@PathVariable("taskId") int id) {
        return converterTask.convertToShowTaskDTO(taskService.getTask(id));
    }

    @Operation(
            summary = "Удалить задачу",
            description = "Позволяет удалить задачу по её id. Доступно только автору задачи"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{taskId}")
    public void deleteTask(@PathVariable("taskId") int id,
                           Principal principal) {
        taskService.deleteTask(id, principal);
    }

    @Operation(
            summary = "Изменить статус",
            description = "Позволяет изменить статус задачи по её id. Доступно автору и исполнителю задачи"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/status/{taskId}")
    public void changeStatus(@PathVariable("taskId") int id,
                             @RequestBody String status,
                             Principal principal) {

        ValidConvertStringToEnum<Status> statusValid = new ValidConvertStringToEnum<>(Status.class);

        if(!statusValid.isValidate(status))
            throw new IncorrectEnumException("У задачи должен быть один из следующих статусов: " + statusValid.valuesEnumToString());

        taskService.changeStatus(id, Status.valueOf(status), principal);
    }

    @Operation(
            summary = "Назначить исполнителя",
            description = "Позволяет назначить исполнителя по его id задаче по её id. Доступно только автору задачи"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/executor/{taskId}")
    public void setExecutor(@PathVariable("taskId") int id,
                            @RequestBody int executorId,
                            Principal principal) {

        taskService.setExecutor(id, executorId, principal);
    }

    @Operation(
            summary = "Оставить комментарий",
            description = "Позволяет оставить комментарий к задаче по её id"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/comment/{taskId}")
    public void addComment(@PathVariable("taskId") int id,
                           @RequestBody @Valid CommentDTO commentDTO) {

        Comment comment = converterComment.convertToComment(commentDTO);

        taskService.addComment(id, comment);
    }

    @Operation(
            summary = "Получить отфильтрованный список задач",
            description = "Позваляеет полусить отфильтрованный список задач. " +
                    "Фильтрация осуществляется по id исполнителя, id автора, приоритету и статусу задачи." +
                    "Задачи показываются постранично. Можно установить номер и размер страниц."
    )
    @GetMapping
    public List<ShowTaskDTO> getTaskByFilters(@RequestParam(value = "executorId", required = false) Integer executorId,
                                          @RequestParam(value = "authorId", required = false) Integer authorId,
                                          @RequestParam(value = "priority", required = false) String priority,
                                          @RequestParam(value = "status", required = false) String status,
                                          @RequestParam(value = "page",defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "5") int size) {

        Priority enumPriority = null;
        Status enumStatus = null;

        ValidConvertStringToEnum<Priority> priorityValid = new ValidConvertStringToEnum<>(Priority.class);

        if(priority != null && !priorityValid.isValidate(priority))
            throw new IncorrectEnumException("У задачи должен быть один из следующих приоритетов :" + priorityValid.valuesEnumToString());


        ValidConvertStringToEnum<Status> statusValid = new ValidConvertStringToEnum<>(Status.class);

        if(status != null && !statusValid.isValidate(status))
            throw new IncorrectEnumException("У задачи должен быть один из следующих статусов: " + statusValid.valuesEnumToString());

        if(priority != null) enumPriority = Priority.valueOf(priority);
        if(status != null) enumStatus = Status.valueOf(status);

        return taskService.getTasksByFilters(executorId, authorId, enumPriority, enumStatus, PageRequest.of(page, size));
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(NotCorrectJsonPatch exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                "Некорректное тело запроса",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(IncorrectEnumException exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                exception.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(TaskNotCreatedException exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                exception.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(TaskNotFoundException exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                "Задание с данным id не найдено",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(NoAccessToTaskException exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                "У вас нет доступа к данной задаче",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<TaskErrorResponse> handlerException(PersonNotFoundException exception) {
        TaskErrorResponse response = new TaskErrorResponse(
                "Человек с данным id не найден",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
