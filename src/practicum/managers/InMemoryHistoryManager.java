package practicum.managers;

import practicum.collections.CustomLinkedList;
import practicum.model.Node;
import practicum.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> map = new HashMap<>();
    private final CustomLinkedList list = new CustomLinkedList();

    @Override
    public void add(Task task) {
        int id = task.getId();
        if (map.containsKey(id)) {
            var element = map.get(id);
            list.removeNode(element);
            map.remove(id);
        }
        Node node = list.linkLast(task);
        map.put(task.getId(), node);
    }

    @Override
    public void clear() {
        var ids = new ArrayList<>(map.keySet());
        for (int id : ids) {
            remove(id);
        }
    }

    @Override
    public void remove(int id) {
        Node node = map.remove(id);
        if (node != null) {
            list.removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return list.getTasks();
    }
}