package pip;

import com.pip.wulin.server.io.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

import com.pip.wulin.server.io.*;
import java.io.*;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;


public class ProxyTestClient
    implements Runnable, UWAPApp, UWAPDataListener
{
	private int id;
	private static String proxyHost = "211.100.18.94";
	private static int proxyPort = 7002;
	private String host;
	private int port;
	UWAPConnection connection;
	private boolean connected = false;

    public ProxyTestClient(int id, String host, int port)
    {
        this.host = host;
        this.port = port;
        this.id = id;
    }
    
    public static int ip2num(String host) {
    	String[] secs = host.split("\\.");
    	return (Integer.parseInt(secs[0]) << 24) | (Integer.parseInt(secs[1]) << 16) |
    		(Integer.parseInt(secs[2]) << 8) | Integer.parseInt(secs[3]);
    }

    public void run()
    {
    	int counter = 0;
        try
        {
            connect();
            while (!connected) {
            	Thread.sleep(100);
            }
            System.out.println("Agent[" + id + "]连接成功");
            while (true) {
            	Thread.sleep(6000);
            	sendChat();
            }
        }
        catch(Exception ex)
        {
        	System.out.println("Agent[" + id + "]异常退出");
            ex.printStackTrace();
        }
    }

    private void connect() throws Exception
    {
        Socket socket = new Socket(proxyHost, proxyPort);
        UWAPSocketSConnection connection = new UWAPSocketSConnection(socket, this);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(ip2num(host));
        dos.writeShort(port);
        dos.flush();
        connection.outStream.write(bos.toByteArray());
    }

    public void registerSession(UWAPConnection conn)
    {
        connection = conn;
        connection.addDataListener(this);
        connected = true;
    }

    public boolean onGotData(UWAPConnection conn, UWAPData data[], int serialNum, int requestId)
        throws Exception
    {
        for(int i = 0; i < data.length; i++)
            process(data[i]);

        return false;
    }

    private void process(UWAPData data) throws Exception
    {
        switch(data.getAppType())
        {
        case 1:
        	String msg = data.readString();
        	System.out.println("Agent[" + id + "]: " + msg);
        	break;
        }
    }

    private void sendChat() throws Exception {
    	UWAPSegment seg = new UWAPSegment(1);
    	seg.writeString("I'm agent " + id + ".");
        connection.write(seg, -1);
    }
    
    public void onSignal(UWAPConnection uwapconnection, int i, String s)
    {
    }
    
    public static void main(String[] args) throws Exception {
    	int start = 0, end = 10;
    	String host = "211.100.18.94";
    	int port = 7003;
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equals("-help")) {
    			System.out.println("Usage: java DumbClient [-sStart] [-eEnd] [-help] [-hHost] [-pPort] [-c] [-t]");
    			return;
    		}
    		if (args[i].startsWith("-s")) {
    			start = Integer.parseInt(args[i].substring(2));
    		}
    		if (args[i].startsWith("-e")) {
    			end = Integer.parseInt(args[i].substring(2));
    		}
    		if (args[i].startsWith("-h")) {
    			host = args[i].substring(2);
    		}
    		if (args[i].startsWith("-p")) {
    			port = Integer.parseInt(args[i].substring(2));
    		}
    	}
    	for (int i = start; i < end; i++) {
    		ProxyTestClient client = new ProxyTestClient(i, host, port);
    		new Thread(client).start();
    		Thread.sleep(500);
    	}
    }
}
