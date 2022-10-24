package Model;

public class Task {
    protected Integer id; //уникальный номер
    protected String name; //название задачи
    protected String description; //описание
    protected String status; //статус NEW - новая, IN_PROGRESS - в процессе, DONE - завершена

    public Task(String name, String description) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.status = "NEW";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ("name= " + this.name + ", status= " + this.status);
    }
}
