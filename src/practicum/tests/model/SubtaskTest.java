package practicum.tests.model;

import org.junit.jupiter.api.Test;
import practicum.model.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubtaskTest {

    @Test
    public void shouldStoreAndUpdateEpicId() {
        var subtask = new Subtask("Subtask", "description", 7);

        assertEquals(7, subtask.getEpicId());

        subtask.setEpicId(9);
        assertEquals(9, subtask.getEpicId());
    }

    @Test
    public void toStringShouldMentionEpic() {
        var subtask = new Subtask("Subtask", "description", 7);
        subtask.setId(3);

        var text = subtask.toString();
        assertTrue(text.contains("Подзадача"));
        assertTrue(text.contains("относится к эпику 7"));
    }

    @Test
    public void shouldInheritIdBasedEquality() {
        var first = new Subtask("a", "b", 1);
        first.setId(5);

        var second = new Subtask("c", "d", 2);
        second.setId(5);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
