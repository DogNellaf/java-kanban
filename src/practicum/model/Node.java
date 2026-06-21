package practicum.model;

public class Node {
    private Task task;
    private Node next;
    private Node previous;

    public Node() {

    }

    public Node(Task task, Node next, Node previous) {
        this.task = task;
        this.next = next;
        this.previous = previous;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
