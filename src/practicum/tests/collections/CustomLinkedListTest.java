package practicum.tests.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.collections.CustomLinkedList;
import practicum.model.Node;
import practicum.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomLinkedListTest {
    private CustomLinkedList list;

    private Task task(int id) {
        var task = new Task("Task" + id, "description");
        task.setId(id);
        return task;
    }

    @BeforeEach
    public void setUp() {
        list = new CustomLinkedList();
    }

    @Test
    public void shouldReturnEmptyListInitially() {
        assertTrue(list.getTasks().isEmpty());
    }

    @Test
    public void shouldLinkTasksInInsertionOrder() {
        list.linkLast(task(1));
        list.linkLast(task(2));
        list.linkLast(task(3));

        List<Task> tasks = list.getTasks();
        assertEquals(3, tasks.size());
        assertEquals(1, tasks.get(0).getId());
        assertEquals(2, tasks.get(1).getId());
        assertEquals(3, tasks.get(2).getId());
    }

    @Test
    public void shouldRemoveTheOnlyNodeAndStayUsable() {
        Node node = list.linkLast(task(1));

        list.removeNode(node);
        assertTrue(list.getTasks().isEmpty());

        // After removing the single element the list must still accept new nodes.
        list.linkLast(task(2));
        assertEquals(1, list.getTasks().size());
        assertEquals(2, list.getTasks().get(0).getId());
    }

    @Test
    public void shouldRemoveFirstNode() {
        Node first = list.linkLast(task(1));
        list.linkLast(task(2));
        list.linkLast(task(3));

        list.removeNode(first);

        List<Task> tasks = list.getTasks();
        assertEquals(2, tasks.size());
        assertEquals(2, tasks.get(0).getId());
        assertEquals(3, tasks.get(1).getId());
    }

    @Test
    public void shouldRemoveMiddleNode() {
        list.linkLast(task(1));
        Node middle = list.linkLast(task(2));
        list.linkLast(task(3));

        list.removeNode(middle);

        List<Task> tasks = list.getTasks();
        assertEquals(2, tasks.size());
        assertEquals(1, tasks.get(0).getId());
        assertEquals(3, tasks.get(1).getId());
    }

    @Test
    public void shouldRemoveLastNode() {
        list.linkLast(task(1));
        list.linkLast(task(2));
        Node last = list.linkLast(task(3));

        list.removeNode(last);

        List<Task> tasks = list.getTasks();
        assertEquals(2, tasks.size());
        assertEquals(1, tasks.get(0).getId());
        assertEquals(2, tasks.get(1).getId());
    }

    @Test
    public void shouldIgnoreNullNode() {
        list.linkLast(task(1));

        assertDoesNotThrow(() -> list.removeNode(null));
        assertEquals(1, list.getTasks().size());
    }
}
