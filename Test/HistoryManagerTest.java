import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.InMemoryHistoryManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HistoryManagerTest {

    public static HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager(); //создаем новый менеджер истории перед каждым тестом
    }

    /**
     * проверка метода add() - добавление элементов в пустую историю
     */
    @Test
    public void addElementsInNewManager() {
        Task task = new Task("name", "description", 0); //создали таск
        Task task2 = new Task("name2", "description2", 1); //создали таск

        historyManager.add(task); //добавили таск в историю
        historyManager.add(task2); //добавили таск в историю
        /*проверка*/
        assertEquals(2, historyManager.getHistory().size()); //в истории должно быть два элемента
        assertEquals(task, historyManager.getHistory().get(0)); //проверка  элементов
        assertEquals(task2, historyManager.getHistory().get(1));
    }

    /**
     * проверка метода add() - добавоение дублирующего элемента
     */
    @Test
    public void addElementTwice() {
        Task task = new Task("name", "description", 0); //создали таск
        historyManager.add(task); //добавили таск в историю
        historyManager.add(task); //добавили снова таск в историю

        assertEquals(1, historyManager.getHistory().size()); //в истории должен быть один элемент
        assertEquals(task, historyManager.getHistory().get(0));//проверка, что это нужный элемент
    }

    /**
     * проверка метода getHistory() при пустой истории
     */
    @Test
    public void getEmptyHistory(){
        assertNotNull(historyManager.getHistory()); //история должна быть не null
        assertEquals(0, historyManager.getHistory().size()); //но при этом пустая
    }

    /**
     * проверка метода remove() - удаление элемента из середины заполненной истории
     */
    @Test
    public void removeFromCenter(){
        Task task = new Task("name", "description", 0); //создали таск
        Task task2 = new Task("name2", "description2", 1);
        Task task3 = new Task("name3", "description3", 2);

        historyManager.add(task); //добавили таски в историю
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1); //удаление элемента истории по id
        /*проверка*/
        assertEquals(2, historyManager.getHistory().size()); //в истории должно быть два элемента
        assertEquals(task, historyManager.getHistory().get(0)); //проверка  элементов
        assertEquals(task3, historyManager.getHistory().get(1));
    }

    /**
     * проверка метода remove() - удаление элемента из начала заполненной истории
     */
    @Test
    public void removeFromBegin(){
        Task task = new Task("name", "description", 0); //создали таск
        Task task2 = new Task("name2", "description2", 1);
        Task task3 = new Task("name3", "description3", 2);

        historyManager.add(task); //добавили таски в историю
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(0); //удаление первого элемента истории
        /*проверка*/
        assertEquals(2, historyManager.getHistory().size()); //в истории должно быть два элемента
        assertEquals(task2, historyManager.getHistory().get(0)); //проверка  элементов
        assertEquals(task3, historyManager.getHistory().get(1));
    }

    /**
     * проверка метода remove() - удаление элемента из конца заполненной истории
     */
    @Test
    public void removeFromEnd(){
        Task task = new Task("name", "description", 0); //создали таск
        Task task2 = new Task("name2", "description2", 1);
        Task task3 = new Task("name3", "description3", 2);

        historyManager.add(task); //добавили таски в историю
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2); //удаление Элемента из конца истории
        /*проверка*/
        assertEquals(2, historyManager.getHistory().size()); //в истории должно быть два элемента
        assertEquals(task, historyManager.getHistory().get(0)); //проверка  элементов
        assertEquals(task2, historyManager.getHistory().get(1));
    }

    /**
     * проверка метода remove() - удаление элемента из пустой истории
     */
    @Test
    public void removeFromEmptyHistory(){
        historyManager.remove(1); //удаление элемента из пустой истории
        /*проверка*/
        assertEquals(0, historyManager.getHistory().size()); //в истории не должно быть элементов
    }
}
