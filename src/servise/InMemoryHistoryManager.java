package servise;

import model.Task;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
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
            int id = task.getId();
            removeNode(new Node(null, task, null)); // удаляем из списка старую инф.о просмотре этой задачи
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
     *
     * @param id удаляемой из истории задачи
     */
    @Override
    public void remove(int id) {
        Node node = viewedTaskHistory.get(id); //берем узел из мапы по id
        if (node != null) {
            removeNode(node); //удаляем задачу из связного списка и из мапы
        }
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
        }
        if (tail != null) { //если последний элемент существует, то связываем его с новым
            tail.next = newNode;
        }
        tail = newNode; // передвигаем указатель последнего элемента на новый элемент
        viewedTaskHistory.put(newTask.getId(), newNode); //добавляем новый узел в мапу
    }

    /**
     * удаление задачи из списка (и из hashMap и из LinkedList
     *
     * @param node -удаляемая задача
     */
    public void removeNode(Node node) {
        Task task = node.data;
        if (task != null) {
            int id = task.getId(); //взяли id удаляемой задачи
            viewedTaskHistory.remove(id); // удаляем из мапы
            for (Node x = head; x != null; x = x.next) { //итерируемся по элементам линкедЛиста
                if (task.getId() == x.data.getId()) { //если id тасков совпадают привязываем соседей друг к другу
                    if (x == head) { //если элемент первый в списке
                        head = x.next;
                        head.prev = null;
                    } else if (x == tail) { // если элемент последний в списке
                        tail = x.prev;
                        tail.next = null;
                    } else { //элемент в середине списка
                        x.prev.next = x.next;
                        x.next.prev = x.prev;
                    }
                }
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

