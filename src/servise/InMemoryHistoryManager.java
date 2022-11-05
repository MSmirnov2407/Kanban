package servise;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> viewedTaskHistory; //история просмотров
    private final int MAX_HISTORY_LENGTH = 10; //максимальное количество записей в истории

    /**
     * конструктор менеджера истории просмотров
     */
    public InMemoryHistoryManager() {
        this.viewedTaskHistory = new ArrayList<Task>();
    }

    /**
     * Добавить элемент в список просмотренных
     * Выполняется проверка на null и ограничение длины списка истории
     *
     * @param task , который надо добавить в список
     */
    @Override
    public void add(Task task) {
        if (task != null) {
            viewedTaskHistory.add(task); //добавляем полученный такс в список
            if (viewedTaskHistory.size() > MAX_HISTORY_LENGTH) { //если длина истории превысила 10 запросов
                viewedTaskHistory.remove(0); //удаляем самый старый
            }
        }
    }

    /**
     * Вернуть список просмотренных задач
     *
     * @return спискок просмотренных задач/подзадач/эпиков
     */
    @Override
    public List<Task> getHistory() {
        return viewedTaskHistory;
    }
}
