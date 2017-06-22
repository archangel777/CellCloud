import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import javax.net.ServerSocketFactory;

public class Server {
	
	public static void main(String[] args) {
		try {
			Vector<Socket> socketList = new Vector<>();
			
			int port = 3000;
			ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
			
			System.out.println("Server running on PORT: " + port);
			
			Algorithm algorithm = new Algorithm(socketList);
						
			ConnectionsListener listener = new ConnectionsListener(serverSocket, socketList, algorithm);
			Thread addConnections = new Thread(listener);
			addConnections.start();
			
			Thread algorithmThread = new Thread(algorithm);
			
			Scanner scanner = new Scanner(System.in);
			String line;
			while(!(line = scanner.nextLine()).equals("exit")) {
				if (line.equals("run"))
					algorithmThread.run();
			}
			scanner.close();
			
			listener.stop();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static private class ConnectionsListener implements Runnable {
		
		ServerSocket serverSocket;
		Vector<Socket> sockets;
		Algorithm algorithm;
		boolean stopped = false;
		
		public ConnectionsListener(ServerSocket serverSocket, Vector<Socket> socketList, Algorithm algoritmh) {
			this.serverSocket = serverSocket;
			this.sockets = socketList;
			this.algorithm = algoritmh;
		}

		@Override
		public void run() {
			try {
				while(!stopped) {
					Socket newSocket = serverSocket.accept();
					synchronized (sockets) {
						System.out.println("Cellphone connected!");
						sockets.add(newSocket);
						sockets.notifyAll();
						Thread t = new Thread(new Listener(newSocket, sockets, algorithm));
						t.start();
					}
				}
			} catch (IOException e) {
				System.out.println("Cancelled connections.");
			}
			
		}
		
		public void stop() {
			stopped = true;
		}
	}
	
	static private class Listener implements Runnable {
		
		Socket socket;
		Vector<Socket> sockets;
		Algorithm algorithm;
		
		public Listener(Socket socket, Vector<Socket> sockets, Algorithm algorithm) {
			this.socket = socket;
			this.sockets = sockets;
			this.algorithm = algorithm;
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String line = null;

                while((line = in.readLine()) != null) {
                    algorithm.receiveResult(line);
                }
                
                sockets.remove(socket);
                System.out.println(socket + " disconnected!");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
}
