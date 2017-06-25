package utils;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class DataPool {
	private Queue<Long> pool;
	
	public DataPool() {
		pool = new LinkedBlockingQueue<>();
	}
	
	public DataPool(int n, int mod) {
		this();
		System.out.println("Creating DataPool...");
		Random r = new Random();
		for (long i = 0; i < n; i++)
			pool.add((long)Math.abs(r.nextInt())%mod);
		
	}
	
	public DataPool(int n) {
		this(n, n);
	}
	
	
	
	public void insert(Long l) {
		pool.add(l);
	}
	
	public Long poll() {
		return pool.poll();
	}
	
	public Long peek() {
		return pool.peek();
	}
	
	public int size() {
		return pool.size();
	}
	
	public DataPool copy() {
		DataPool copied = new DataPool();
		for (Iterator<Long> it = pool.iterator(); it.hasNext();)
			copied.insert(it.next());
		return copied;
	}
}
