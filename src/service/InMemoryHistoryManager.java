package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements  HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> browsingHistory;

    public InMemoryHistoryManager() {
        browsingHistory = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        int idTask = task.getId();
        if (browsingHistory.containsKey(idTask)) {
            removeNode(browsingHistory.get(idTask));
        }
        linkLast(task);
        browsingHistory.put(idTask, tail);
    }

    @Override
    public void remove(int id) {
        if (browsingHistory.containsKey(id)) {
            removeNode(browsingHistory.get(id));
        }
    }

    private void removeNode(Node<Task> node) {
        int idTask = node.data.getId();
        node = browsingHistory.remove(idTask);
        if (node == null) {
            return;
        }
        if (node.prevNode == null) {
            head = node.nextNode;
            head.prevNode = null;
            node.nextNode = null;
        } else if (node.nextNode == null) {
            tail = node.prevNode;
            tail.nextNode = null;
            node.prevNode = null;
        } else {
            node.prevNode.nextNode = node.nextNode;
            node.nextNode.prevNode = node.prevNode;
            node.nextNode = null;
            node.prevNode = null;
        }
    }

    private void linkLast(Task task) {
        Node<Task> oldTail = tail;
        tail = new Node<>(oldTail, task, null);
        if (oldTail == null) {
            head = tail;
        } else {
            oldTail.nextNode = tail;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> node = head;
        while(node != null) {
            tasks.add(node.data);
            node = node.nextNode;
        }
        return tasks;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private static class Node<T> {
        private Node<T> prevNode;
        private Node<T> nextNode;
        private final T data;

        public Node(Node<T> prevNode, T data, Node<T> nextNode) {
            this.prevNode = prevNode;
            this.nextNode = nextNode;
            this.data = data;
        }
    }
}
