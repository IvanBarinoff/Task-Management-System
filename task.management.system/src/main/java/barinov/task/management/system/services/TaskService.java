package barinov.task.management.system.services;

import barinov.task.management.system.dto.ShowTaskDTO;
import barinov.task.management.system.dto.TaskDTO;
import barinov.task.management.system.exceptions.NotCorrectJsonPatch;
import barinov.task.management.system.models.*;
import barinov.task.management.system.repositories.TaskRepository;
import barinov.task.management.system.exceptions.NoAccessToTaskException;
import barinov.task.management.system.exceptions.TaskNotFoundException;
import barinov.task.management.system.util.ConverterTaskAndTaskDTOs;
import barinov.task.management.system.util.GeneratorExceptionWithMessageTaskDTO;
import barinov.task.management.system.util.TaskDTOValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final PersonDetailsService personDetailsService;
    private final CommentService commentService;
    private final ObjectMapper objectMapper;
    private final ConverterTaskAndTaskDTOs converter;
    private final GeneratorExceptionWithMessageTaskDTO generatorExceptionTaskDTO;

    @Autowired
    public TaskService(TaskRepository taskRepository, PersonDetailsService personDetailsService, CommentService commentService, ObjectMapper objectMapper, ConverterTaskAndTaskDTOs converter, GeneratorExceptionWithMessageTaskDTO generatorExceptionTaskDTO) {
        this.taskRepository = taskRepository;
        this.personDetailsService = personDetailsService;
        this.commentService = commentService;
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.generatorExceptionTaskDTO = generatorExceptionTaskDTO;
    }

    public Integer saveTaskAndGetId(Task task, Person author) {
        task.setAuthor(author);

        return taskRepository.save(task).getId();
    }

    public void editTaskById(JsonPatch jsonPatch, int id, Principal principal, BindingResult bindingResult, TaskDTOValidator validator) {
        Task task = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
        Person author = task.getAuthor();
        TaskDTO target = converter.convertToTaskDTO(task);

        if(author.getEmail().equals(principal.getName())) {
            try {
                JsonNode patched = jsonPatch.apply(objectMapper.convertValue(target, JsonNode.class));
                TaskDTO taskDTO = objectMapper.treeToValue(patched, TaskDTO.class);

                generatorExceptionTaskDTO.validOrGenerateException(taskDTO, bindingResult, validator);

                task = converter.convertToTask(taskDTO);

                task.setId(id);
                saveTaskAndGetId(task, author);
            } catch (JsonPatchException | JsonProcessingException e) {
                throw new NotCorrectJsonPatch();
            }
        } else {
            throw new NoAccessToTaskException();
        }
    }



    @Transactional(readOnly = true)
    public Task getTask(int id) {
        return taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
    }

    public void deleteTask(int id, Principal principal) {
        Task task = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);

        if(task.getAuthor().getEmail().equals(principal.getName()))
            taskRepository.deleteById(id);
        else {
            throw new NoAccessToTaskException();
        }
    }

    public void changeStatus(int id, Status status, Principal principal) {
        Task task = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);

        String currentLogin = principal.getName();

        if(task.getAuthor().getEmail().equals(currentLogin)
                || (task.getExecutor() != null && task.getExecutor().getEmail().equals(currentLogin))) {

            task.setStatus(status);
            taskRepository.save(task);
        } else {
            throw new NoAccessToTaskException();
        }
    }

    public void setExecutor(int taskId, int executorId, Principal principal) {
        Task task = taskRepository.findById(taskId).orElseThrow(TaskNotFoundException::new);

        if(task.getAuthor().getEmail().equals(principal.getName())) {
            Person executor = personDetailsService.getPersonById(executorId);
            task.setExecutor(executor);
        } else {
            throw new NoAccessToTaskException();
        }

        taskRepository.save(task);
    }

    public void addComment(int id, Comment comment) {
        Task task = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);

        List<Comment> comments = task.getComments();

        if(comments == null)
            comments = new ArrayList<>();

        comment.setTask(task);
        comments.add(comment);

        commentService.save(comment);
        taskRepository.save(task);
    }

    public List<ShowTaskDTO> getTasksByFilters(Integer executorId, Integer authorId, Priority priority, Status status, Pageable pageable) {
        Specification<Task> tasks = getSpecification(executorId, authorId, priority, status);

        return taskRepository.findAll(tasks, pageable)
                .get()
                .map(converter::convertToShowTaskDTO)
                .collect(Collectors.toList());
    }

    private Specification<Task> getSpecification(Integer executorId, Integer authorId, Priority priority, Status status) {
        List<Specification<Task>> tasks = new ArrayList<>();


        if(executorId != null) tasks.add(findByExecutor(personDetailsService.getPersonById(executorId)));
        if(authorId != null) tasks.add(findByAuthor(personDetailsService.getPersonById(authorId)));

        if(priority != null) tasks.add(findByPriority(priority));
        if(status != null) tasks.add(findByStatus(status));


        return tasks.stream().reduce(Specification::and)
                .orElse((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
    }

    private Specification<Task> findByExecutor(Person executor) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("executor").get("id"), executor.getId());
    }

    private Specification<Task> findByAuthor(Person author) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("author").get("id"), author.getId());
    }

    private Specification<Task> findByPriority(Priority priority) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), priority.toString());
    }

    private Specification<Task> findByStatus(Status status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status.toString());
    }
}
