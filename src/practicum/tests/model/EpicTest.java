package practicum.tests.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.enums.Status;
import practicum.managers.FileBackedTasksManager;
import practicum.managers.Managers;
import practicum.model.Epic;
import practicum.model.Subtask;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    private static final FileBackedTasksManager manager = (FileBackedTasksManager) Managers.getFileManager();
    private static Epic epic;
    private static Subtask subtask;

    @BeforeEach
    public void createManager() {
        manager.clear();
        epic = new Epic("TestEpic", "it's epic for tests");
        Epic.DEFAULT_MANAGER = manager;
        manager.add(epic, 1);
        subtask = new Subtask("Subtask1", "test", epic.getId());
        subtask.setStatus(Status.IN_PROGRESS);
        subtask.setDuration(0);
        manager.add(subtask, 2);
    }

    @Test
    public void shouldReturnSubtasksIds() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask2.setDuration(0);
        manager.add(subtask2, 0);

        var ids = epic.getTaskIds();

        assertEquals(ids.size(), 2);

        assertEquals(subtask.getId(), ids.get(0));
        assertEquals(subtask2.getId(), ids.get(1));
    }

    @Test
    public void shouldReturnEmptyListWhenHaveNotSubtasks() {
        manager.clear();
        var ids = epic.getTaskIds();
        assertEquals(ids.size(), 0);
    }

    @Test
    public void shouldReturnTrueWhenSubtasksContains() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask2.setDuration(0);
        manager.add(subtask2, 0);

        assertTrue(epic.contains(subtask2.getId()));
        assertTrue(epic.contains(subtask.getId()));
    }

    @Test
    public void shouldReturnFalseWhenSubtasksDoNotContains() {
        manager.add(subtask, 0);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);

        assertTrue(epic.contains(subtask.getId()));
        assertFalse(epic.contains(subtask2.getId()));
    }

    @Test
    public void shouldRemoveSubtask() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.add(subtask2, 0);

        epic.removeSubtaskId(subtask.getId());

        assertTrue(epic.contains(subtask2.getId()));
        assertFalse(epic.contains(subtask.getId()));
    }

    @Test
    public void shouldNotChangeListWhenRemoveWrongSubtask() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);

        var ids = epic.getTaskIds();

        assertEquals(ids.size(), 1);

        epic.removeSubtaskId(subtask2.getId());

        ids = epic.getTaskIds();

        assertEquals(ids.size(), 1);
        assertEquals(ids.get(0), subtask.getId());
    }

    @Test
    public void shouldAddSubtask() {
        var subtask2 = new Subtask("Subtask1", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask2.setId(1);

        var ids = epic.getTaskIds();

        assertEquals(ids.size(), 1);

        epic.addSubtaskId(subtask2.getId());

        ids = epic.getTaskIds();

        assertEquals(ids.size(), 2);
        assertEquals(ids.get(0), subtask.getId());
        assertEquals(ids.get(1), subtask2.getId());
    }

    @Test
    public void shouldNotAddSameSubtask() {

        assertEquals(epic.getTaskIds().size(), 1);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setId(subtask.getId());
        subtask2.setDuration(0);
        epic.addSubtaskId(subtask2.getId());

        assertEquals(epic.getTaskIds().size(), 1);
    }

    @Test
    public void shouldClearSubtasksList() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setId(subtask.getId() + 1);
        subtask2.setDuration(0);
        epic.addSubtaskId(subtask2.getId());

        assertEquals(epic.getTaskIds().size(), 2);

        epic.cleanSubtaskIds();

        assertEquals(epic.getTaskIds().size(), 0);
    }

    @Test
    public void shouldChangeEpicStatusToInProgressWhenHaveInProgressSubtasks() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.add(subtask2, 0);

        manager.updateEpicStatus(epic);

        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    @Test
    public void shouldNotChangeEpicStatusWhenHaveNotSubtasks() {
        epic.cleanSubtaskIds();
        manager.updateEpicStatus(epic);
        assertEquals(epic.getStatus(), Status.NEW);
    }

    @Test
    public void shouldNotChangeEpicStatusWhenAllSubtasksIsNew() {
        subtask.setStatus(Status.NEW);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        manager.add(subtask2, 0);

        manager.updateEpicStatus(epic);

        assertEquals(epic.getStatus(), Status.NEW);
    }

    @Test
    public void shouldChangeEpicStatusToDONEWhenAllSubtasksIsDONE() {
        subtask.setStatus(Status.DONE);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.DONE);
        manager.add(subtask2, 0);

        manager.updateEpicStatus(epic);

        assertEquals(epic.getStatus(), Status.DONE);
    }

    @Test
    public void shouldChangeEpicStatusToInProgressWhenHaveDoneAndNewSubtasks() {
        subtask.setStatus(Status.DONE);

        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        manager.add(subtask2, 0);

        manager.updateEpicStatus(epic);

        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    @Test
    public void shouldChangeEpicStatusToInProgressWhenAllSubtasksInProgress() {
        var subtask2 = new Subtask("Subtask2", "test", epic.getId());
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask2.setDuration(0);
        manager.add(subtask2, 0);

        manager.updateEpicStatus(epic);

        assertEquals(epic.getStatus(), Status.IN_PROGRESS);
    }

    @Test
    public void shouldReturnCorrectDuration() {

        manager.clearSubtasks();
        subtask.setDuration(10);
        subtask.setStartTime(LocalDateTime.of(2021, 1, 1, 12, 0, 0));
        manager.add(subtask, subtask.getId());

        var subtask2 = new Subtask("test", "test", epic.getId());
        subtask2.setStartTime(LocalDateTime.of(2021, 1, 1, 12, 10, 0));
        subtask2.setDuration(20);

        manager.add(subtask2, 0);

        assertEquals(epic.getDuration(), 30);
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        manager.clearSubtasks();
        subtask.setDuration(10);
        var firstSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 13, 30);
        subtask.setStartTime(firstSubtaskStartTime);
        manager.add(subtask, subtask.getId());

        var subtask2 = new Subtask("test", "test", epic.getId());
        var secondSubtaskStartTime = LocalDateTime.of(2022, 1, 1, 14, 30);
        subtask2.setStartTime(secondSubtaskStartTime);
        subtask2.setDuration(10);

        manager.add(subtask2, 0);

        assertEquals(epic.getEndTime(), subtask2.getEndTime());
    }
}