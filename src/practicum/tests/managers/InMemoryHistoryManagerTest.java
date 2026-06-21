package practicum.tests.managers;

import org.junit.jupiter.api.BeforeEach;
import practicum.managers.InMemoryHistoryManager;
import practicum.model.Task;

import java.time.LocalDateTime;

public class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager> {
    @BeforeEach
    public void createManager() {
        historyManager = new InMemoryHistoryManager();
        historyManager.clear();
        task = new Task("Task1", "test");
        task.setStartTime(LocalDateTime.now().minusYears(1));
        historyManager.add(task);
    }
}
