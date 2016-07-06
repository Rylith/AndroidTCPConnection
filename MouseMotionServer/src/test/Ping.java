package test;

import java.io.IOException;
import java.net.InetAddress;

import network.Impl.ConnectCallbackTest;
import network.Impl.OwnEngine;
import network.Interface.Engine;

public class Ping{

	public static void main(String[] args) {
		try {
			int portInitial=4445;
			//Il est possible de changer le port de départ
			if(args !=null && args.length != 0){
				portInitial = Integer.parseInt(args[0]);
			}
			int port = portInitial;

			Engine ping = new OwnEngine();
			ping.connect(InetAddress.getByName("127.0.0.1"), port, new ConnectCallbackTest());
			//ping.startEcho();
			String msg = "-100,0";
			
			((OwnEngine) ping).send(msg,0,msg.length());
			ping.mainloop();
			
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
