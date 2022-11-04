package servise;

public final class Managers {
    /**
     * Возвращаем новый объект менеджера задач по умолчанию
     *
     * @return объект менеджера задач
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Возвращаем новый объект менеджера Истории просмотров по умолчанию
     *
     * @return объект менеджера истории просмотров
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
