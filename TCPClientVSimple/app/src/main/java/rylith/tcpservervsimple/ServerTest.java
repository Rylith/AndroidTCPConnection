package rylith.tcpservervsimple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ServerTest extends Server {
	private int port;
	private ServerSocketChannel m_sch;
	private String m_localhost = "192.168.43.1";
	
	public ServerTest(int port) throws IOException {
		this.port=port;
		m_sch= ServerSocketChannel.open();
		m_sch.configureBlocking(false);
		m_sch.socket().bind(new InetSocketAddress(m_localhost, port));
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
	
	

}
