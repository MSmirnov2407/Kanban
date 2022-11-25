package servise;

import model.Task;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private HashMap<Integer, Node> viewedTaskHistory = new HashMap<>(); //история просмотров
    private Node head; //ссылка на первый элемент двусвязного списка
    private Node tail; //сылка на последний элемент

    /**
     * конструктор менеджера истории просмотров
     */
    public InMemoryHistoryManager() {
        this.viewedTaskHistory = new HashMap<Integer, Node>();
    }

    /**
     * Добавить элемент в список просмотренных
     * Выполняется проверка на null
     *
     * @param task , который надо добавить в список
     */
    @Override
    public void add(Task task) {
        if (task != null) {
            int id = task.getId(); //взяли id полученного таска
            remove(id); // удалили старую инф. о его последнем вызове
            linkLast(task); //добавляем задачу в конец двусвязного списка и в мапу
        }
    }

    /**
     * Вернуть список просмотренных задач
     *
     * @return спискок просмотренных задач/подзадач/эпиков
     */
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    /**
     * Удалить элемент из истории просмотра по id
     * задача удаляется и из мапы и из связного списка
     *
     * @param id удаляемой из истории задачи
     */
    @Override
    public void remove(int id) {
        removeNode(viewedTaskHistory.remove(id)); //удаляем задачу из мапы и из связного списка
    }

    /**
     * добавление задачи в конец двусвязного списка
     *
     * @param newTask
     */
    public void linkLast(Task newTask) {
        Node newNode = new Node(tail, newTask, null);
        if (head == null) { //если это первый элемент, сохраняем указатель head
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode; // передвигаем указатель последнего элемента на новый элемент
        viewedTaskHistory.put(newTask.getId(), newNode); //добавляем новый узел в мапу
    }

    /**
     * удаление узла из связного сипска
     *
     * @param node -удаляемая задача в виде узла списка
     */
    public void removeNode(Node node) {
        if (node != null) {
            if (node == head) { //если элемент первый в списке
                head = node.next; // то указатель первого элемента сместится на следующий
            } else { //если нет, значит есть предыдущий элемент
                node.prev.next = node.next; //значит предыдущий ссылаем на следующий
            }
            if (node == tail) { //если элемент последний в списке
                tail = node.prev; //то указатель последнего смещаем на предыдущий
            } else { //если нет, значит есть следующий элемент
                node.next.prev = node.prev; //значит ссылаем следующий на предыдущий.
            }
        }
    }

    /**
     * возвращаем customLinkedList в виде обычого arrayList
     *
     * @return история просмотров в виде ArrayList
     */
    public ArrayList<Task> getTasks() {
        ArrayList<Task> historyArrayList = new ArrayList<>();
        for (Node x = head; x != null; x = x.next) { //обходим все элементы связного списка
            historyArrayList.add(x.data); // и добавляем эх в arrayList
        }
        return historyArrayList;
    }
}

