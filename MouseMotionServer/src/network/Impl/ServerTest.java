package network.Impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.Enumeration;

import network.Interface.Server;

public class ServerTest extends Server {
	private int port;
	private ServerSocketChannel m_sch;
	private static String IP_ADDRESS="";
	
	public ServerTest(int port) throws IOException {
		this.port=port;
		m_sch= ServerSocketChannel.open();
		m_sch.configureBlocking(false);
		if(IP_ADDRESS.equals("")){
			getIPAddress();
			System.out.println(IP_ADDRESS);
		}
		m_sch.socket().bind(new InetSocketAddress(IP_ADDRESS, port));
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void close() {
		try {
			m_sch.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerSocketChannel getSocket() {
		return m_sch;
	}
	
	private void getIPAddress() throws SocketException{
		boolean notFind=true;
		Enumeration e;
	    e = NetworkInterface.getNetworkInterfaces();
	    while (e.hasMoreElements() && notFind) {
	        NetworkInterface ni = (NetworkInterface) e.nextElement();
	        if (ni.isLoopback() || !ni.isUp()) continue;

	        for (Enumeration e2 = ni.getInetAddresses(); e2.hasMoreElements(); ) {
	            InetAddress ip = (InetAddress) e2.nextElement();
	            if(ip instanceof Inet4Address){
	            	IP_ADDRESS=ip.getHostAddress();
	            	notFind=false;
	            }
	        }
	        
	    }
		
	}

}
