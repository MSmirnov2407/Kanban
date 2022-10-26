package model;

public class Task {
    protected Integer id; //уникальный номер
    protected String name; //название задачи
    protected String description; //описание
    protected String status; //статус NEW - новая, IN_PROGRESS - в процессе, DONE - завершена

    public Task(String name, String description, Integer id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = "NEW";
    }

    public Task(String name, String description) {
        this(name, description, null);
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
        return ("id= "+this.id + ", name= " + this.name + ", status= " + this.status);
    }
}
