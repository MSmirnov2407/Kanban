package model;

public class Subtask extends Task {
    protected TaskType taskType = TaskType.SUBTASK; // тип задачи как элемент из перечисления

    private Integer epicId; //эпик которому принадлежит подзадача

    public Subtask(String name, String description, Integer epicId) {
        this(name, description, epicId, null);
    } //конструктор

    public Subtask(String name, String description, Integer epicId, Integer id) {
        super(name, description, id);
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
        return taskType;
    }

    @Override
    public String toString() {
        String subtaskAsString = String.join(",", id.toString(), getTaskType().toString(),
                name, status.toString(), description, epicId.toString());
        return subtaskAsString;
    }
}
