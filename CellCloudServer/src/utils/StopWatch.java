package utils;

public class StopWatch {
	
	private long time;
	
	private long startTime;
	private int nTests;
	
	public StopWatch() {
		time = 0;
		nTests = 0;
	}
	
	public void reset() {
		time = 0;
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public void stop() {
		time += System.currentTimeMillis() - startTime;
		nTests++;
	}
	
	public long getTime() {
		return time;
	}
	
	public double getAvgTime() {
		return time/(double)nTests;
	}
	
}
