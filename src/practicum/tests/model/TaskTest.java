package practicum.tests.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.enums.Status;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTest {
    private Task task;

    @BeforeEach
    public void setUp() {
        task = new Task("Task", "description");
        task.setId(1);
        task.setStartTime(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
        task.setDuration(30);
    }

    @Test
    public void shouldBeEqualWhenIdsAreEqual() {
        var other = new Task("Other name", "Other description");
        other.setId(1);

        assertEquals(task, other);
        assertEquals(task.hashCode(), other.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenIdsDiffer() {
        var other = new Task("Task", "description");
        other.setId(2);

        assertNotEquals(task, other);
    }

    @Test
    public void shouldNotBeEqualToSubtaskWithSameId() {
        var subtask = new Subtask("Task", "description", 5);
        subtask.setId(1);

        assertNotEquals(task, subtask);
    }

    @Test
    public void shouldNotBeEqualToNullOrOtherType() {
        assertFalse(task.equals(null));
        assertFalse(task.equals("not a task"));
    }

    @Test
    public void shouldBeEqualToItself() {
        assertEquals(task, task);
    }

    @Test
    public void shouldCalculateEndTimeFromStartAndDuration() {
        assertEquals(LocalDateTime.of(2020, 1, 1, 0, 30, 0), task.getEndTime());
    }

    @Test
    public void shouldAccumulateDuration() {
        task.setDuration(10);
        task.addDuration(5);
        task.addDuration(15);

        assertEquals(30, task.getDuration());
    }

    @Test
    public void shouldHaveNewStatusByDefault() {
        assertEquals(Status.NEW, new Task("a", "b").getStatus());
    }

    @Test
    public void toStringShouldContainId() {
        assertTrue(task.toString().contains("id = 1"));
    }
}
