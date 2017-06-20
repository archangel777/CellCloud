import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;

public class Server {
	
	public static void main(String[] args) {
		try {
			ArrayList<Socket> socketList = new ArrayList<>();
			
			int port = 3000;
			ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
			
			System.out.println("Server running on PORT: " + port);
						
			Thread t = new Thread(new ConnectionsListener(serverSocket, socketList));
			t.start();
			
			Thread algorithm = new Thread(new Algoritmh(socketList));
			algorithm.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static private class ConnectionsListener implements Runnable {
		
		ServerSocket serverSocket;
		ArrayList<Socket> socketList;
		
		public ConnectionsListener(ServerSocket serverSocket, ArrayList<Socket> socketList) {
			this.serverSocket = serverSocket;
			this.socketList = socketList;
		}

		@Override
		public void run() {
			try {
				while(true) {
					System.out.println("Waiting for connections...");
					Socket newSocket = serverSocket.accept();
					synchronized (this) {
						System.out.println("Cellphone connected!");
						socketList.add(newSocket);
						notifyAll();
						Thread t = new Thread(new Listener(newSocket));
						t.start();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	static private class Listener implements Runnable {
		
		Socket socket;
		
		public Listener(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String line = null;

                while((line = in.readLine()) != null) {
                    // Do something. Never gets here
                }
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
