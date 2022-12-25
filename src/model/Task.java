package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected Integer id; //уникальный номер
    protected String name; //название задачи
    protected String description; //описание
    protected Status status; //статус NEW - новая, IN_PROGRESS - в процессе, DONE - завершена
    protected long duration; //продолжительности задачи в минутах
    protected LocalDateTime startTime; //время начала выполнения задачи

//    public Task(String name, String description, Integer id) {
//        this(name, description);
//        this.id = id;
//    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.startTime = LocalDateTime.now();
        this.duration = 0;
    }

    public Task(String name, String description, LocalDateTime startTime, long duration) {
        this(name, description);
        this.startTime = startTime;
        this.duration = duration;
    }
    public Task(String name, String description, LocalDateTime startTime, long duration, Integer id) {
        this(name, description,startTime, duration);
        this.id = id;
    }



    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    /**
     * метод возвращает время окончания задачи, рассчитанное на основе startTime и duration
     * @return
     */
    public LocalDateTime getEndTime(){
      LocalDateTime endTime = startTime.plusMinutes(duration);
      return endTime;
    }

    @Override
    public String toString() {
        String taskAsString = String.join(",", id.toString(), getTaskType().toString(), name,
                status.toString(), description);
        return taskAsString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getId().equals(task.getId()) && getName().equals(task.getName())
                && getDescription().equals(task.getDescription()) && getStatus() == task.getStatus();
    }
}