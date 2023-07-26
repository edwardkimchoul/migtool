package tunesql.trace;

public class ArrayStack {
    int size;        //스택 배열의 크기
    int [] stack;
    
    public ArrayStack(int size) {
        this.size = size;
        stack = new int[size];
    }
    
    public void put(int item, int depth) {
        stack[depth] = item;
    }
    
    public int get(int depth) {
        return stack[depth];
    }
    
}