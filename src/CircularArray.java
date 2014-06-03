
import java.util.LinkedList;

public class CircularArray {
	private LinkedList buckets[];
	private int length;
	private int start;
	private int limit; 
	private int limitLength;
	
	CircularArray(int n, int l) {
		this.length = n;
		this.start = 0;
		this.limit = l;
		this.limitLength = l;
		this.buckets = new LinkedList[n];
		for (int i = 0; i < this.length; i++) this.buckets[i] = new LinkedList();
	}
	
	public int getLimit() {
		return this.limitLength;
	}
	// Inserts and element at the start index. 
	public void insertStart(Object o) {
		this.buckets[this.start].add(o);
	}
	
	// Inserts an element at the limit index.
	public void insertLimit(Object o) {
		this.buckets[this.limit].add(o); 
	}
	
	// Move an element o already in bucket i to the start of the circular array.
	public void moveToStart(int i, Object o) {
		this.buckets[i].remove(o);
		this.buckets[this.start].add(o);
	}
	
	// Rotates our array, moving everything one position away from start. This way the elements stay fixed but still appear to be moving.
	public void rotate() {
		
		if (this.start == 0) this.start = this.length - 1;
		else this.start -= 1;
		this.buckets[this.start].clear();
		
		if (this.limit == 0) this.limit = this.length - 1;
		else this.limit -= 1;
	}
	
	// Returns the bucket at index i positions away from start. Start is always index 0!
	public LinkedList getBucket(int i) {
		i += start;
		if (i >= this.length) i -= this.length;
		return this.buckets[i];
	}
	
	public void display() {
		for (int i = 0; i < this.length; i++) {
			System.out.print(i + ": ");
			for (Object item : this.getBucket(i)) {
				System.out.print(item);
				System.out.print( " ");
			}
			System.out.println();
		}
	}
	
	public static void main(String args[]) {
		CircularArray a = new CircularArray(5, 3);
		a.insertStart(1);
		a.insertStart(2);
		a.rotate();
		a.rotate();
		a.rotate();
		a.rotate();
		a.insertLimit(3);
		a.rotate();
		a.moveToStart(4, 3);
		a.display();
	}
}
