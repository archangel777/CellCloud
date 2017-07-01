package server;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	final Float minCellProb = 0.00f;
	final Float maxCellProb = 0.05f;
	final int batchSize = 1000;
	final int nTests = 1;
	private int sent = 0;
	private ReduceStrategy strategy;
	private StopWatch testWatch = new StopWatch();
	private boolean sendToCell;
	private Sender sender;
	private Receiver receiver;
		
	public Algorithm(Vector<Socket> sockets) {
		String strat = "sum";
		this.sockets = sockets;
		sender = new Sender(sockets);
		
		switch(strat){
		default:
		case "min":
			fixedPool = new DataPool(1000000);
			strategy = new MinStrategy();
			break;
		case "sum":
			fixedPool = new DataPool(1000000, 5);
			strategy = new SumStrategy();
			break;
		}
		
		new Thread(sender).start();
	}

	@Override
	public void run() {
		testWatch.reset();
		StopWatch watch = new StopWatch();
		System.out.println("Running....");
		for (int i = 0; i < nTests; i++) {
			sendToCell = true;
			currentPool = fixedPool.copy();
			receiver = new Receiver(currentPool);
			new Thread(receiver).start();
			//if (i%(nTests/10) == 0) System.out.print("#");
			
			watch.start();
			process();
			watch.stop();
		}
		
		System.out.println("\nFinished in an average of " + watch.getAvgTime() + " ms.");
		System.out.println("Last result was: " + currentPool.peek());
		System.out.println("Baseline value is: " + strategy.baseline(fixedPool));
		System.out.println("Avg ms: " + testWatch.getAvgTime());
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
		receiver.post(res);
	}
	
	public void sendProcessing(ArrayList<Tuple<Long>> t) throws IOException {
		sent++;
		double cellProb = (sockets.isEmpty())? 0 : Math.pow(0.3, 1./sockets.size())*(maxCellProb - minCellProb) + minCellProb;
		Random r = new Random();
		
		//if (r.nextFloat() < cellProb) {
		if (sendToCell && !sockets.isEmpty()) {
			sendToCell = false;
			sender.post(new DataPacket(strategy.strategyName(), t));
		}
		else {
			testWatch.start();
			receiveResult(strategy.reduce(t));	
			testWatch.stop();
		}

	}
	
	private class Sender implements Runnable {
		
		private BlockingQueue<DataPacket> toSend;
		private Vector<Socket> sockets;
		private int pos = 0;
		
		public Sender(Vector<Socket> sockets) {
			toSend = new LinkedBlockingQueue<>();
			this.sockets = sockets;
		}
		
		public void run() {
			try {
				while(true) {
					DataPacket pkt = toSend.take();
					if (pos >= sockets.size()) pos = 0;
					PrintWriter writer = new PrintWriter(sockets.get(pos).getOutputStream());
					writer.println(new Gson().toJson(pkt));
					writer.flush();
					pos++;
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
		
		public void post(DataPacket packet) {
			try {
				toSend.put(packet);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
private class Receiver implements Runnable {
		
		private BlockingQueue<Long> toReceive;
		private DataPool pool;
		
		public Receiver(DataPool pool) {
			toReceive = new LinkedBlockingQueue<>();
			this.pool = pool;
		}
		
		public void run() {
			try {
				while(true) {
					Long l = toReceive.take();
					synchronized(currentPool){	
						currentPool.insert(l);
						sent--;					
						currentPool.notify();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void post(Long l) {
			try {
				toReceive.put(l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
