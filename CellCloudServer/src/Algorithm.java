import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.google.gson.Gson;

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
		while(currentPool.size() > 1) {
			ArrayList<Tuple<Long>> list = new ArrayList<>();
			for (int i = 0; i < batchSize && currentPool.size() > 1; i++) {
				list.add(new Tuple<>(currentPool.poll(), currentPool.poll()));
			}
			sendProcessing(list);
		}
	}
	
	public void receiveResult(Long res) {
		currentPool.insert(res);
	}
	
	public void sendProcessing(ArrayList<Tuple<Long>> t) {
		try {
			double cellProb = (sockets.isEmpty())? 0 : Math.pow(0.8, 1./sockets.size());
			Random r = new Random();
			if (r.nextFloat() < cellProb) {
				if (pos >= sockets.size()) pos = 0;
				PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
				writer.println(new Gson().toJson(t));
				pos++;
			}
			else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						receiveResult(reduce(t));						
					}
				}).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Long reduce(ArrayList<Tuple<Long>> t) {
        if (t.size() == 1) return Math.min(t.get(0).getT1(), t.get(0).getT2());
        Tuple<Long> t1 = t.remove(0), t2 = t.remove(0);
        t.add(new Tuple<>(Math.min(t1.getT1(), t1.getT2()), Math.min(t2.getT1(), t2.getT2())));
        return reduce(t);
    }
	
}
