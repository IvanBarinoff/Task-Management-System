package barinov.task.management.system.repositories;

import barinov.task.management.system.models.Person;
import barinov.task.management.system.models.Priority;
import barinov.task.management.system.models.Status;
import barinov.task.management.system.models.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer>, JpaSpecificationExecutor<Task> {

}
