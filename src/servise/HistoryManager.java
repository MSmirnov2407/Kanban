package servise;

import model.Task;
import java.util.List;

public interface HistoryManager {
    /**
     * Добавить элемент в список просмотренных
     * @param task , который надо добавить в список
     */
    void add(Task task);

    /**
     * Вернуть список просмотренных задач
     * @return спискок просмотренных задач/подзадач/эпиков
     */
    List<Task> getHistory(); //вывести список просмотренных задач
}
