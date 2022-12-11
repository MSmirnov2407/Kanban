package model;

public class Task {
    protected Integer id; //уникальный номер
    protected String name; //название задачи
    protected String description; //описание
    protected Status status; //статус NEW - новая, IN_PROGRESS - в процессе, DONE - завершена
    protected TaskType taskType = TaskType.TASK; // тип задачи как элемент из перечисления

    public Task(String name, String description, Integer id) {
        this(name, description);
        this.id = id;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
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
        return taskType;
    }

    @Override
    public String toString() {
        String taskAsString = String.join(",", id.toString(), getTaskType().toString(), name,
                status.toString(), description);
        return taskAsString;
    }
}