import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class Algorithm implements Runnable {
	
	Vector<Socket> sockets;
	DataPool currentPool;
	DataPool fixedPool;
	int pos = 0;
	final Float cellProb = 0.2f;
	final int batchSize = 100;
	final int nTests = 1000;
	
	public Algorithm(Vector<Socket> sockets) {
		this.sockets = sockets;
		fixedPool = new DataPool(10000);
	}

	@Override
	public void run() {
		
		
		long totalTime = 0;
		System.out.println("Running....");
		for (int i = 0; i < nTests; i++) {
			currentPool = fixedPool.copy();
			if (i%(nTests/10) == 0) System.out.print("#");
			long startTime = System.currentTimeMillis();
			process();
			totalTime += System.currentTimeMillis() - startTime;
		}
		System.out.println("\nFinished in an average of " + (totalTime/(double)nTests) + " ms.");
	}
	
	public void process() {
		while(currentPool.size() > 1)
			synchronized (currentPool) {
				String s = "";
				for (int i = 0; i < batchSize && currentPool.size() > 1; i++) {
					s += currentPool.poll().toString() + ",";
					s += currentPool.poll().toString() + ";";
				}
				sendProcessing(s);
			}
	}
	
	public void receiveResult(String res) {
		String[] splt = res.split(";");
		for (int i = 0; i < splt.length-1; i++)
			currentPool.insert(Long.valueOf(splt[i]));
	}
	
	public void sendProcessing(String s) {
		try {
			double cellProb = Math.pow(0.2, 1./sockets.size());
			Random r = new Random();
			if (r.nextFloat() < cellProb) {
				if (pos >= sockets.size()) pos = 0;
				PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
				writer.println(s);
				pos++;
			}
			else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						receiveResult(reduce(s));						
					}
				}).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String reduce(String s) {
		String res = "";
		String[] sl = s.split(";");
		for (int i = 0; i < sl.length-1; i++) {
			String[] values = sl[i].split(",");
			if (Long.valueOf(values[0]) < Long.valueOf(values[1]))
				res += values[0] + ";";
			else
				res += values[1] + ";";
		}
		return res;
	}
	
}
