package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; //список сабтасков этого эпика

    public Epic(String name, String description) {
        this(name, description, null);
    } //конструктор

    public Epic(String name, String description, Integer id) {
        super(name, description, id);
        subtaskIds = new ArrayList<>();
    } //конструктор

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    } //возвращаем все сабтаски текущего эпика

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    } //добавляем подзадачу в список этого эпика

    public void deleteSubtask(Integer sabtaskId) {
        if (subtaskIds.contains(sabtaskId)) {
            subtaskIds.remove(sabtaskId);
        }
    } //удаление одного сабтаска из эпика
}
