import java.net.Socket;
import java.util.ArrayList;

public class Algoritmh implements Runnable {
	
	ArrayList<Socket> sockets;
	
	public Algoritmh(ArrayList<Socket> sockets) {
		this.sockets = sockets;
	}

	@Override
	public void run() {
		synchronized (this) {
			while (sockets.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Someone Connected!!!!");
	}
	
}
