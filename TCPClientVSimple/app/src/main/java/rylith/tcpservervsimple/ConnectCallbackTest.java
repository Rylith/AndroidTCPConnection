package rylith.tcpservervsimple;

public class ConnectCallbackTest implements ConnectCallback{

	public void closed(Channel channel) {
		channel.close();
		
	}

	public void connected(Channel channel) {
		System.out.println("Connexion sur le channel : "+ channel.toString());
	}

}
