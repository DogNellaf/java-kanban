package practicum.tests.managers;

import org.junit.jupiter.api.Test;
import practicum.managers.TaskManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    public T manager;

    public Task task;

    @Test
    public void shouldRemoveTask() {
        var result = manager.remove(task.getId());

        assertTrue(result);
    }

    @Test
    public void shouldNotRemoveWhenHaveNotTasks() {
        manager.clear();
        var result = manager.remove(100);

        assertFalse(result);
    }

    @Test
    public void shouldNotRemoveWhenTryRemoveTaskWithWrongId() {
        var result = manager.remove(task.getId() + 1);

        assertFalse(result);
    }

    @Test
    public void shouldChangeTask() {
        task.setName("Task1 - NEW!!!");

        var id = task.getId();
        var name = task.getName();
        var description = task.getDescription();
        var status = task.getStatus();
        var className = task.getClass();

        var result = manager.change(task);

        assertTrue(result);

        var task2 = manager.find(id);

        assertNotEquals(task2, null);
        assertEquals(task2.getClass().getSimpleName(), className.getSimpleName());
        assertEquals(task2.getId(), id);
        assertEquals(task2.getName(), name);
        assertEquals(task2.getDescription(), description);
        assertEquals(task2.getStatus(), status);
    }

    @Test
    public void shouldThrowChangeExceptionWhenTaskIsNull() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> manager.change(null));
        assertEquals("Cannot invoke \"practicum.model.Task.getId()\" because \"task\" is null", exception.getMessage());
    }

    @Test
    public void shouldNotChangeTaskWhenHaveNotTasks() {
        manager.clear();
        var task1 = new Task("Task1", "test");
        var result = manager.change(task1);

        assertFalse(result);
    }

    @Test
    public void shouldNotChangeTaskWhenTaskHaveWrongId() {
        task.setId(task.getId() + 1);

        var result = manager.change(task);

        assertFalse(result);
    }

    @Test
    public void shouldAddTaskWhenHaveNotTasks() {
        manager.clear();
        var task1 = new Task("Task1", "test");
        var result = manager.add(task1, 0);
        assertTrue(result);
    }

    @Test
    public void shouldAddTask() {
        var task2 = new Task("Task2", "test");
        var result = manager.add(task2, 0);

        assertTrue(result);
    }

    @Test
    public void shouldAddEpic() {
        var epic = new Epic("Epic", "test");
        var result = manager.add(epic, 0);

        assertTrue(result);
    }

    @Test
    public void shouldAddSubtaskWithExistsEpic() {
        var epic = new Epic("Epic", "test");
        manager.add(epic, 0);

        var subtask = new Subtask("Subtask", "test", epic.getId());
        var result = manager.add(subtask, 0);

        assertTrue(result);
    }

    @Test
    public void shouldAddSubtaskWithNotExistsEpic() {
        var epic = new Epic("Epic", "test");
        epic.setId(10);

        var subtask = new Subtask("Subtask", "test", epic.getId());
        var result = manager.add(subtask, 0);

        assertFalse(result);
    }

    @Test
    public void shouldThrowExceptionToAddWhenTaskIsNull() {

        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> manager.add(null, 0));
        assertEquals("Cannot invoke \"practicum.model.Task.setId(int)\" because \"task\" is null", exception.getMessage());
    }

    @Test
    public void shouldAddTaskWithNewIdWhenAlreadyHaveTaskWithSameId() {
        var task2 = new Task("Task2", "test");
        var result = manager.add(task2, task.getId());

        assertTrue(result);
        assertNotEquals(task2.getId(), task.getId());
    }

    @Test
    public void shouldFindTask() {
        var task2 = manager.find(task.getId());

        assertNotEquals(task2, null);
        assertEquals(task.getClass().getSimpleName(), task2.getClass().getSimpleName());
        assertEquals(task.getId(), task2.getId());
        assertEquals(task.getName(), task2.getName());
        assertEquals(task.getDescription(), task2.getDescription());
        assertEquals(task.getStatus(), task2.getStatus());
    }

    @Test
    public void shouldNotFindTaskWhenIdIsWrong() {
        var task2 = manager.find(task.getId() + 1);

        assertNull(task2);
    }

    @Test
    public void shouldNotFindTaskWhenIdLess0() {
        var task1 = manager.find(-1);

        assertNull(task1);
    }

    @Test
    public void shouldNotFindTaskWhenHaveNotTasks() {
        manager.clear();
        var task1 = manager.find(1);

        assertNull(task1);
    }

    @Test
    public void shouldClearAll() {

        assertEquals(manager.size(), 1);

        manager.add(new Epic("Epic1", "test"), 0);
        manager.add(new Subtask("Subtask1", "test", 2), 0);

        assertEquals(manager.size(), 3);

        manager.clear();

        assertEquals(manager.size(), 0);
    }

    @Test
    public void shouldClearTasks() {

        manager.clear();

        manager.add(new Task("Task2", "test"), 0);

        var epic = new Epic("Epic1", "test");
        manager.add(epic, 0);
        manager.add(new Subtask("Subtask1", "test", epic.getId()), 0);

        assertEquals(manager.size(), 3);

        manager.clearTasks();

        assertEquals(manager.size(), 2);
        assertEquals(manager.getTasks().size(), 0);
    }

    @Test
    public void shouldClearEpics() {

        manager.clear();

        manager.add(new Task("Task", "test"), 0);
        manager.add(new Epic("Epic1", "test"), 0);
        manager.add(new Epic("Epic2", "test"), 0);
        manager.add(new Subtask("Subtask1", "test", 2), 0);

        assertEquals(manager.size(), 4);

        manager.clearEpics();

        assertEquals(manager.size(), 1);
        assertEquals(manager.getEpics().size(), 0);
    }

    @Test
    public void shouldClearSubTasks() {

        manager.clear();

        var epic = new Epic("Epic1", "test");
        manager.add(epic, 0);
        manager.add(new Subtask("Subtask1", "test", epic.getId()), 0);
        manager.add(new Subtask("Subtask2", "test", epic.getId()), 0);

        assertEquals(manager.size(), 3);

        manager.clearSubtasks();

        assertEquals(manager.size(), 1);
        assertEquals(manager.getSubtasks().size(), 0);
    }

    @Test
    public void shouldReturnAllTasks() {
        manager.clear();

        var tasks = new ArrayList<Task>();
        tasks.add(task);
        tasks.add(new Task("Task2", "test"));
        tasks.add(new Task("Task3", "test"));
        tasks.add(new Task("Task4", "test"));
        tasks.add(new Task("Task5", "test"));
        tasks.add(new Epic("Epic1", "test"));
        tasks.add(new Subtask("Subtask1", "test", 6));
        tasks.add(new Epic("Epic2", "test"));

        for (Task task : tasks) {
            manager.add(task, 0);
        }

        var result = manager.getAllTasks();

        assertEquals(result.size(), tasks.size());

        for (int i = 0; i < result.size(); i++) {
            var original = tasks.get(i);
            var returned = result.get(i);
            assertEquals(original.getClass().getSimpleName(), returned.getClass().getSimpleName());
            assertEquals(original.getId(), returned.getId());
            assertEquals(original.getName(), returned.getName());
            assertEquals(original.getDescription(), returned.getDescription());
            assertEquals(original.getStatus(), returned.getStatus());
            if (original.getClass().getSimpleName().equals("Subtask")) {
                assertEquals(((Subtask) original).getEpicId(), ((Subtask) returned).getEpicId());
            }
        }
    }

    @Test
    public void shouldReturnTasks() {

        manager.add(new Epic("Epic1", "test"), 0);

        var subtask1 = new Subtask("Subtask1", "test", 2);
        manager.add(subtask1, 0);

        var subtask2 = new Subtask("Subtask2", "test", 2);
        manager.add(subtask2, 0);

        var tasks = manager.getTasks();

        assertEquals(tasks.size(), 1);

        assertEquals(tasks.get(0).getClass().getSimpleName(), "Task");
        assertEquals(tasks.get(0).getId(), task.getId());
        assertEquals(tasks.get(0).getName(), task.getName());
        assertEquals(tasks.get(0).getDescription(), task.getDescription());
        assertEquals(tasks.get(0).getStatus(), task.getStatus());
    }

    @Test
    public void shouldReturnSubtasks() {

        manager.add(new Epic("Epic1", "test"), 0);

        var subtask1 = new Subtask("Subtask1", "test", 2);
        subtask1.setDuration(0);
        manager.add(subtask1, 0);

        var subtask2 = new Subtask("Subtask2", "test", 2);
        subtask2.setDuration(0);
        manager.add(subtask2, 0);

        var subtasks = manager.getSubtasks();

        assertEquals(subtasks.size(), 2);

        assertEquals(subtasks.get(0).getClass().getSimpleName(), "Subtask");
        assertEquals(subtasks.get(0).getId(), subtask1.getId());
        assertEquals(subtasks.get(0).getName(), subtask1.getName());
        assertEquals(subtasks.get(0).getDescription(), subtask1.getDescription());
        assertEquals(subtasks.get(0).getStatus(), subtask1.getStatus());
        assertEquals(subtasks.get(0).getEpicId(), subtask1.getEpicId());

        assertEquals(subtasks.get(1).getClass().getSimpleName(), "Subtask");
        assertEquals(subtasks.get(1).getId(), subtask2.getId());
        assertEquals(subtasks.get(1).getName(), subtask2.getName());
        assertEquals(subtasks.get(1).getDescription(), subtask2.getDescription());
        assertEquals(subtasks.get(1).getStatus(), subtask2.getStatus());
        assertEquals(subtasks.get(1).getEpicId(), subtask2.getEpicId());
    }

    @Test
    public void shouldReturnSubtasksByEpic2() {

        var epic = new Epic("Epic1", "test");
        manager.add(epic, 0);

        var subtask1 = new Subtask("Subtask1", "test", 2);
        subtask1.setStartTime(LocalDateTime.now().minusWeeks(8));
        manager.add(subtask1, 0);

        var subtask2 = new Subtask("Subtask2", "test", 2);
        subtask2.setStartTime(LocalDateTime.now().minusWeeks(6));
        manager.add(subtask2, 0);

        var epic5 = new Epic("Epic2", "test");
        manager.add(epic5, 0);

        var subtask3 = new Subtask("Subtask3", "for epic with id 5", epic5.getId());
        subtask3.setStartTime(LocalDateTime.now().minusWeeks(2));
        manager.add(subtask3, 0);

        var subtasks = manager.getSubtasks(epic5.getId());

        assertEquals(subtasks.size(), 1);

        assertEquals(subtasks.get(0).getClass().getSimpleName(), "Subtask");
        assertEquals(subtasks.get(0).getId(), subtask3.getId());
        assertEquals(subtasks.get(0).getName(), subtask3.getName());
        assertEquals(subtasks.get(0).getDescription(), subtask3.getDescription());
        assertEquals(subtasks.get(0).getStatus(), subtask3.getStatus());
        assertEquals(subtasks.get(0).getEpicId(), epic5.getId());
    }

    @Test
    public void shouldNotReturnSubtasksWhenHaveNotSubtasks() {

        var subtasks = manager.getSubtasks();

        assertEquals(subtasks.size(), 0);
    }

    @Test
    public void shouldReturnEpics() {
        var epic1 = new Epic("Epic1", "test");
        manager.add(epic1, 0);

        var epic2 = new Epic("Epic2", "test");
        manager.add(epic2, 0);

        var epics = manager.getEpics();

        assertEquals(epics.size(), 2);

        assertEquals(epics.get(0).getClass().getSimpleName(), "Epic");
        assertEquals(epics.get(0).getId(), epic1.getId());
        assertEquals(epics.get(0).getName(), epic1.getName());
        assertEquals(epics.get(0).getDescription(), epic1.getDescription());
        assertEquals(epics.get(0).getStatus(), epic1.getStatus());

        assertEquals(epics.get(1).getClass().getSimpleName(), "Epic");
        assertEquals(epics.get(1).getId(), epic2.getId());
        assertEquals(epics.get(1).getName(), epic2.getName());
        assertEquals(epics.get(1).getDescription(), epic2.getDescription());
        assertEquals(epics.get(1).getStatus(), epic2.getStatus());
    }

    @Test
    public void shouldReturnEmptyListWhenHaveNotEpics() {
        var subtasks = manager.getEpics();

        assertEquals(subtasks.size(), 0);
    }

    @Test
    public void shouldReturnTasksInfo() {
        var result = manager.getTasksInfo();

        assertNotNull(result);
    }

    @Test
    public void shouldReturnSize() {
        assertEquals(manager.size(), 1);
    }

    @Test
    public void shouldReturn0WhenHaveNotTasks() {
        manager.clear();
        assertEquals(manager.size(), 0);
    }

    @Test
    public void shouldNotAddTaskWhenTasksCrossByEndTime() {
        manager.clear();

        var epic = new Epic("TestEpic", "it's epic for tests");
        Epic.DEFAULT_MANAGER = manager;
        manager.add(epic, 0);

        var subtask = new Subtask("Subtask1", "test", epic.getId());
        subtask.setStartTime(LocalDateTime.of(
                2023, 3, 23, 13, 30, 0));
        subtask.setDuration(100);
        manager.add(subtask, 0);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(
                2023, 3, 23, 14, 30, 0));
        subtask2.setDuration(50);

        assertFalse(manager.add(subtask2, 0));
    }

    @Test
    public void shouldNotAddTaskWhenTasksCrossByStartTime() {
        manager.clear();
        var epic = new Epic("TestEpic", "it's epic for tests");
        Epic.DEFAULT_MANAGER = manager;
        manager.add(epic, 0);

        var subtask = new Subtask("Subtask1", "test", epic.getId());
        var firstSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 14, 30, 0);
        subtask.setStartTime(firstSubtaskStartTime);
        subtask.setDuration(50);
        manager.add(subtask, 0);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        var secondSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 13, 30, 0);
        subtask2.setStartTime(secondSubtaskStartTime);
        subtask2.setDuration(100);

        var result = manager.add(subtask2, 0);
        assertFalse(result);
    }

    @Test
    public void shouldAddTaskWhenTasksDoNotCrossByTime() {
        manager.clear();
        var epic = new Epic("TestEpic", "it's epic for tests");
        Epic.DEFAULT_MANAGER = manager;
        manager.add(epic, 0);

        var subtask = new Subtask("Subtask1", "test", epic.getId());
        var firstSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 14, 30, 0);
        subtask.setStartTime(firstSubtaskStartTime);
        subtask.setDuration(10);
        manager.add(subtask, 0);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        var secondSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 13, 30, 0);
        subtask2.setStartTime(secondSubtaskStartTime);
        subtask2.setDuration(100);

        var result = manager.add(subtask2, 0);
        assertTrue(result);
    }
}
