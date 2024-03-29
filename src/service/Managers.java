package service;

import java.io.File;

public final class Managers {
    /**
     * Возвращаем новый объект менеджера задач по умолчанию
     *
     * @return объект менеджера задач
     */
    public static TaskManager getDefault() {
        return new HttpTaskManager("http://localhost:8078");
    }

    /**
     * Возвращаем новый объект менеджера Истории просмотров по умолчанию
     *
     * @return объект менеджера истории просмотров
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    /**
     * возвращаем менеджер задач, хранящий информацию в файле
     *
     * @param file - имя файла
     * @return объект менеджера задач
     */
    public static FileBackedTasksManager getFileBackedTaskManager(File file) {
        return new FileBackedTasksManager(file);
    }
}
