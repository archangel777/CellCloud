import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class Algorithm implements Runnable {
	
	Vector<Socket> sockets;
	DataPool pool;
	int pos = 0;
	final Float cellProb = 0.002f;
	
	public Algorithm(Vector<Socket> sockets) {
		this.sockets = sockets;
	}

	@Override
	public void run() {
		pool = new DataPool(5000000);
		System.out.println("Running....");
		long startTime = System.currentTimeMillis();
		while(pool.size() > 1)
			synchronized (pool) {
				String s1 = pool.poll().toString();
				String s2 = pool.poll().toString();
				sendProcessing(s1, s2);
			}
		
		System.out.println("Calculated minimum: " + pool.poll());
		System.out.println("Finished in " + (System.currentTimeMillis() - startTime) + " ms.");
	}
	
	public void receiveResult(String res) {
		pool.insert(Long.valueOf(res));
	}
	
	public void sendProcessing(String s1, String s2) {
		try {
			Random r = new Random();
			if (!sockets.isEmpty() && r.nextFloat() < cellProb) {
				PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
				writer.println(s1+","+s2);
				pos++;
				if (pos >= sockets.size()) pos = 0;
			}
			else {
				receiveResult(reduce(s1, s2));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String reduce(String s1, String s2) {
		Long i1 = Long.valueOf(s1), i2 = Long.valueOf(s2);
		return (i1 < i2)? i1.toString() : i2.toString();
	}
	
}
