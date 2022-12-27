package model;

import java.time.LocalDateTime;

public class Subtask extends Task {

    private Integer epicId; //эпик которому принадлежит подзадача

    public Subtask(String name, String description, Integer epicId) {
        this(name, description, epicId, null);
    } //конструктор

    public Subtask(String name, String description, Integer epicId, Integer id) {
        super(name, description, id);
        this.epicId = epicId;

    } //конструктор

    public Subtask(String name, String description, Integer epicId, LocalDateTime startTime, long duration) {
        super(name, description, startTime, duration, null);
        this.epicId = epicId;
    } //конструктор

    public Subtask(String name, String description, Integer epicId, LocalDateTime startTime, long duration, Integer id) {
        super(name, description, startTime, duration, id);
        this.epicId = epicId;
    } //конструктор

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        String subtaskAsString = String.join(",", id.toString(), getTaskType().toString(),
                name, status.toString(), description, startTime.toString(), Long.toString(duration), epicId.toString());
        return subtaskAsString;
    }
}
