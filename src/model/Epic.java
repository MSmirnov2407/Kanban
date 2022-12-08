package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();; //список сабтасков этого эпика

    public Epic(String name, String description) {
        super(name, description);
    } //конструктор

    public Epic(String name, String description, Integer id) {
        super(name, description, id);
    } //конструктор

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    } //возвращаем все сабтаски текущего эпика

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    } //добавляем подзадачу в список этого эпика

    public void deleteSubtask(Integer subtaskId) {
            subtaskIds.remove(subtaskId);
    } //удаление одного сабтаска из эпика

    @Override
    public String toString() {
        String epicAsString = String.join(",",id.toString(),TaskType.EPIC.toString(),name,
                status.toString(),description);
        return epicAsString;
    }
}
