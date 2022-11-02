package servise;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.ArrayList;

public interface TaskManager {
        /**
         * Возвращаем содержимое мапы tasks в виде списка всех задач
         *
         * @return список Тасков
         */
        ArrayList<Task> getTasks();

        /**
         * Возвращаем содержимое мапы subtasks в виде списка всех подзадач
         *
         * @return Список Сабтасков
         */
        ArrayList<Subtask> getSubtasks();

        /**
         * Возвращаем содержимое мапы epics в виде списка всех эпиков
         *
         * @return Список эпиков
         */
        ArrayList<Epic> getEpics();

        /**
         * Удаление всех задач
         */
        void deleteAllTasks();

        /**
         * удаление всех подзадач
         */
        void deleteAllSubtasks();

        /**
         * удаление всех эпиков
         */
        void deleteAllEpics();

        /**
         * получение таска по id
         *
         * @param id запрашиваемого таска
         * @return запрашиваемый таск
         */
        Task getTaskById(Integer id);

        /**
         * получение сабтаска по id
         *
         * @param id запрашиваемого сабтаска
         * @return запрашиваемый сабтаск
         */
        Subtask getSubtaskById(Integer id);

        /**
         * получение эпика по id
         *
         * @param id запрашиваемого эпика
         * @return запрашиваемый эпик
         */
        Epic getEpicById(Integer id);
        /**
         * создание нового таска
         *
         * @param newTask - новый объект класса task
         * @return id созданного таска
         */
        Integer createTask(Task newTask);

        /**
         * создание нового сабтаска
         * При создании проверяем, что переданный сабтаск соответсвует условиям:
         * ссылка на сабтаск не null И сабтаск привязан к эпику И такой эпик существует
         *
         * @param newSubtask новый объект Subtask
         * @return id созданного сабтаска
         */
        Integer createSubtask(Subtask newSubtask);

        /**
         * создание нового эпика
         *
         * @param newEpic новый объект Epic
         * @return id созданного эпика
         */
        Integer createEpic(Epic newEpic);

        /**
         * обновление тасков
         *
         * @param updatedTask - обновленный таск
         */
        void updateTask(Task updatedTask);

        /**
         * обновление сабтасков
         * При обновлении проверяем, что переданный сабтаск соответсвует условиям:
         * ссылка на сабтаск не null И сабтаск существует И его эпик существует
         *
         * @param updatedSubtask обновленный сабтаск
         */
        void updateSubtask(Subtask updatedSubtask);

        /**
         * обновление эпиков
         *
         * @param updatedEpic - обновляемый эпик
         */
        void updateEpic(Epic updatedEpic);

        /**
         * удаление одного таска по id
         *
         * @param id удаляемой задачи
         */
        void deleteTaskById(Integer id);

        /**
         * Удаление одного сабтаска по id
         *
         * @param id удалаемой подзадачи
         */
        void deleteSubtaskById(Integer id);

        /**
         * Удаление одного эпика по id
         *
         * @param id удаляемого эпика
         */
        void deleteEpicById(Integer id);

    /**
     * Вернуть историю просмотров задач
     * @return список Тасков/подтасков/эпиков
     */
    List<Task> getHistory();
}
