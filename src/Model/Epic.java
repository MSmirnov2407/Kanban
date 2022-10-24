package Model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasks; //список сабтасков этого эпика

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    } //конструктор

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    } //возвращаем все сабтаски текущего эпика

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask.getId());
    } //добавляем подзадачу в список этого эпика

    public void deleteOneSubtask(Integer sabtaskId) {
        if (subtasks.contains(sabtaskId)) {
            subtasks.remove(sabtaskId);
        }
    } //удаление одного сабтаска из эпика
}
