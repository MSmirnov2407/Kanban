package model;

public class Subtask extends Task {
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
}
