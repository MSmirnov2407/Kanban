package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; //список сабтасков этого эпика

    /**
     * Алексей, привет! я не очень понял механизм связи с ревьюером, поэтому решил написать здесь,
     * если есть более адекватный способ - буду рад наводке ;)
     * Во-первых большое спасибо за дружелюбный и мотивирующий подход в твоих комментариях, 10 из 10.
     * Во-вторых у меня впервые возник вопрос по замечанию, который я не в силах удержать и хочу задать.
     * Ты предлагаешь убрать вызов конструктора Epic(name,description, id) из конструктора Epic(name,description).
     * Но тогда на мой взгляд возникает проблема дублирования кода, следующего после super, в двух конструкторах Epic.
     * После вызова родительского конструктора приходится дописывать строки, относящиеся к Epic.
     * тут это всего одна строка, но их же может быть и 150.
     * Это же нарушает принцип DRY! о, Боже, я говорю на программистском!:)
     * Как в итоге быть?)
     *
     * то же относится и к классу Task..(
     * а в классе Subtask оставим как есть)
     * @param name
     * @param description
     */

    public Epic(String name, String description) {
        super(name, description);
        subtaskIds = new ArrayList<>();
    } //конструктор

    public Epic(String name, String description, Integer id) {
        super(name, description, id);
        subtaskIds = new ArrayList<>();
    } //конструктор

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    } //возвращаем все сабтаски текущего эпика

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    } //добавляем подзадачу в список этого эпика

    public void deleteSubtask(Integer sabtaskId) {
        if (subtaskIds.contains(sabtaskId)) {
            subtaskIds.remove(sabtaskId);
        }
    } //удаление одного сабтаска из эпика
}
