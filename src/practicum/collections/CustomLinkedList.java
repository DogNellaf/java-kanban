package practicum.collections;

import practicum.model.Node;
import practicum.model.Task;

import java.util.ArrayList;
import java.util.List;

public class CustomLinkedList {
    private Node first;
    private Node last;

    public Node linkLast(Task task) {
        Node node = new Node();
        node.setTask(task);
        if (first == null) {
            first = last = node;
        } else {
            node.setPrevious(last);
            last.setNext(node);
            last = node;
        }
        return node;
    }

    public List<Task> getTasks() {
        List<Task> list = new ArrayList<>();
        Node i = first;
        while (i != null) {
            list.add(i.getTask());
            i = i.getNext();
        }
        return list;
    }

    public void removeNode(Node node) {
        if (node == null) {
            return;
        }
        Node next = node.getNext();
        Node previous = node.getPrevious();

        if (previous == null) {
            first = next;
        } else {
            previous.setNext(next);
        }

        if (next == null) {
            last = previous;
        } else {
            next.setPrevious(previous);
        }
    }
}
