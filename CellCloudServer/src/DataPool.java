import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class DataPool {
	Queue<Long> pool;
	
	public DataPool(int n) {
		System.out.println("Creating DataPool...");
		pool = new LinkedList<>();
		Random r = new Random();
		for (long i = 0; i < n; i++)
			pool.add(Math.abs(r.nextInt())%n + 2039l);
	}
	
	public void insert(Long l) {
		pool.add(l);
	}
	
	public Long poll() {
		return pool.poll();
	}
	
	public int size() {
		return pool.size();
	}
}
