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
        endTime = startTime;
    } //конструктор

    public Epic(String name, String description, Integer id) {
        this(name, description);
        this.id = id;
    }
    public Epic(String name, String description, LocalDateTime startTime, long duration) {
        super(name, description, startTime, duration);
        endTime = startTime.plusMinutes(duration);
    } //конструктор

    public Epic(String name, String description, LocalDateTime startTime, long duration, Integer id) {
        this(name, description, startTime, duration);
        this.id = id;
    }

    public void setEndTime( LocalDateTime endTime){
        this.endTime = endTime;
    }

    public void setDuration( long duration){
        this.duration = duration;
    }

    public long getDuration(){
        return this.duration ;
    }

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
