package server;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import utils.*;

import com.google.gson.Gson;

import strategies.MinStrategy;
import strategies.ReduceStrategy;
import strategies.SumStrategy;

public class Algorithm implements Runnable {
	
	Vector<Socket> sockets;
	DataPool currentPool;
	DataPool fixedPool;
	int pos = 0;
	final Float cellProb = 0.01f;
	final int batchSize = 1000;
	final int nTests = 1000;
	private int sent = 0;
	private ReduceStrategy strategy;
		
	public Algorithm(Vector<Socket> sockets) {
		String strat = "sum";
		this.sockets = sockets;
		switch(strat){
		default:
		case "min":
			fixedPool = new DataPool(10000);
			strategy = new MinStrategy();
			break;
		case "sum":
			fixedPool = new DataPool(10000, 5);
			strategy = new SumStrategy();
			break;
		}
		
		
	}

	@Override
	public void run() {
		StopWatch watch = new StopWatch();
		System.out.println("Running....");
		for (int i = 0; i < nTests; i++) {
			currentPool = fixedPool.copy();
			if (i%(nTests/10) == 0) System.out.print("#");
			
			watch.start();
			process();
			watch.stop();
		}
		System.out.println("\nFinished in an average of " + watch.getAvgTime() + " ms.");
		System.out.println("Last result was: " + currentPool.peek());
		System.out.println("Baseline value is: " + strategy.baseline(fixedPool));
	}
	
	public void process(){
		try {
			while(currentPool.size() > 1 || sent != 0) {	
				ArrayList<Tuple<Long>> list = new ArrayList<>();
				synchronized (currentPool) {
					for (int i = 0; i < batchSize && currentPool.size() > 1; i++) {
						list.add(new Tuple<>(currentPool.poll(), currentPool.poll()));
					}
					sendProcessing(list);
					while (currentPool.size() <= 1 && sent != 0) {
						currentPool.wait();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void receiveResult(Long res) {
		synchronized(currentPool){	
			currentPool.insert(res);
			
			sent--;					
			currentPool.notify();
		}
	}
	
	public void sendProcessing(ArrayList<Tuple<Long>> t) throws IOException {
		sent++;
		double cellProb = (sockets.isEmpty())? 0 : Math.pow(0.8, 1./sockets.size());
		Random r = new Random();
		if (r.nextFloat() < cellProb) {
			if (pos >= sockets.size()) pos = 0;
			PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
			writer.println(new Gson().toJson(new DataPacket(strategy.strategyName(), t)));
			writer.flush();
			pos++;
		}
		else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					receiveResult(strategy.reduce(t));						
				}
			}).start();
		}
	}
	
}