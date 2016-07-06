package network.Impl;

import network.Interface.AcceptCallback;
import network.Interface.Channel;
import network.Interface.Server;

public class AcceptCallbackTest implements AcceptCallback {

	public void accepted(Server server, Channel channel) {
		System.out.println("Acceptation connexion sur le port : " +server.getPort()+ " pour le channel "+channel.toString());

	}

	public void closed(Channel channel) {
		channel.close();
	}

}
