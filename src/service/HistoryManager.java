package service;

import model.Task;

import java.util.List;

public interface HistoryManager {
    /**
     * Добавить элемент в список просмотренных
     *
     * @param task , который надо добавить в список
     */
    void add(Task task);

    /**
     * Вернуть список просмотренных задач
     *
     * @return спискок просмотренных задач/подзадач/эпиков
     */
    List<Task> getHistory();

    /**
     * Удалить элемент из истории просмотра по id
     *
     * @param id удаляемой из истории задачи
     */
    void remove(int id);
}
