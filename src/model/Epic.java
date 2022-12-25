package model;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds = new ArrayList<>(); //список сабтасков этого эпика
    private LocalDateTime endTime ; //рассчетное время окончания эпика

    public Epic(String name, String description) {
        super(name, description);
        endTime = LocalDateTime.now(); //начальное присвоение переменных, чтобы не было null
        startTime = endTime;
    } //конструктор

    public Epic(String name, String description, Integer id) {
        super(name, description, id);
        endTime = LocalDateTime.now(); //начальное присвоение переменных, чтобы не было null
        startTime = endTime;
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
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime(){
        return endTime;
    }
}
