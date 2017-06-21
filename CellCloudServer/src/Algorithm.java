import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

public class Algorithm implements Runnable {
	
	Vector<Socket> sockets;
	Queue<Integer> testData = new LinkedList<>();
	int pos = 0;
	final int maxPos = 30;
	
	public Algorithm(Vector<Socket> sockets) {
		this.sockets = sockets;
		Random r = new Random();
		for (long i = 0; i < 2000000; i++)
			testData.add(Math.abs(r.nextInt())%100000 + 2039);
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		while(testData.size() > 1)
			synchronized (testData) {
				String s1 = testData.remove().toString();
				String s2 = testData.remove().toString();
				sendProcessing(s1, s2);
			}
		
		System.out.println("Calculated minimum: " + testData.remove());
		System.out.println("Finished in " + (System.currentTimeMillis() - startTime) + " ms.");
	}
	
	public void receiveResult(String res) {
		testData.add(Integer.valueOf(res));
	}
	
	public void sendProcessing(String s1, String s2) {
		try {
			if (sockets.size() > pos) {
				PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
				writer.println(s1+","+s2);
			}
			else {
				receiveResult(reduce(s1, s2));
			}
			pos++;
			if (pos > maxPos) pos = 0;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String reduce(String s1, String s2) {
		Integer i1 = Integer.valueOf(s1), i2 = Integer.valueOf(s2);
		return (i1 < i2)? i1.toString() : i2.toString();
	}
	
}
