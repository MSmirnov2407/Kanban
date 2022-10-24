package Model;

public class Subtask extends Task {
    private Integer epicId; //эпик которому принадлежит подзадача

    public Subtask(String name, String description) {
        super(name, description);
        epicId = 0;
    } //конструктор

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }
}
