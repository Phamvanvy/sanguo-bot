package pip;

import java.io.*;
import java.net.*;

import com.pip.wulin.server.io.UWAPData;

public class ProxyTestServer extends Thread {
	private static ServerSocket listenSocket;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	public ProxyTestServer(Socket sock) throws Exception {
		socket = sock;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	public void run() {
		try {
			while (true) {
				byte[] head = new byte[19];
				in.readFully(head);
				int length = new DataInputStream(new ByteArrayInputStream(head, 13, 4)).readInt();
				byte[] body = new byte[length - 19 + 1];
				in.readFully(body);
				if (length == 19) {
					int clientid = new DataInputStream(new ByteArrayInputStream(head, 5, 4)).readInt();
					System.out.println("client " + clientid + " disconnected");
				} else {
					out.write(head);
					out.write(body);
					int clientid = new DataInputStream(new ByteArrayInputStream(head, 5, 4)).readInt();
					String content = "unknown package";
					try {
						UWAPData ud = new UWAPData(body, 0, body.length - 1);
						content = ud.readString();
					} catch (Exception e1) {
					}
					System.out.println("client " + clientid + ": " + content);
				}
			}
		} catch (Exception e) {
			try {
				in.close();
				out.close();
				socket.close();
			} catch (Exception ee) {
			}
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port = 7003;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
		}
		listenSocket = new ServerSocket(port);
		System.out.println("listen on port " + port);
		while (true) {
			Socket sock = listenSocket.accept();
			new ProxyTestServer(sock).start();
		}
	}
}
