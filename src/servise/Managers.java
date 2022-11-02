package servise;

public final class Managers {
    /**
     * Возвращаем новый объект менеджера задач по умолчанию
     * @return объект менеджера задач
     */
    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }
}
