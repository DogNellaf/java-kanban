package practicum.tests.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.managers.InMemoryTaskManager;
import practicum.model.Epic;
import practicum.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTasksManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void createManager() {
        manager = new InMemoryTaskManager();
        manager.clear();
        task = new Task("Task1", "test");
        task.setStartTime(LocalDateTime.now().minusYears(1));
        manager.add(task, 0);

        Epic.DEFAULT_MANAGER = manager;
    }

    private InMemoryTaskManager getManager() {
        return manager;
    }

    @Test
    public void shouldReturnClassName() {
        assertEquals(getManager().getClassName(task), "Task");
    }

}
