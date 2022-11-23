package model;

public class Node {
    public Node next; //ссылка на следующий элемент
    public Node prev; //ссылка на предыдущий элемент
    public Task data; //данные


    public Node(Node prev, Task data, Node next) {
        this.next = next;
        this.prev = prev;
        this.data = data;
    }
}
