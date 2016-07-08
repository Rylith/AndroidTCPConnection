package test;

import java.io.IOException;

import network.Impl.AcceptCallbackTest;
import network.Impl.OwnEngine;
import network.Interface.Engine;
import network.Interface.Server;

public class Pong{

	public static void main(String[] args) {
		try {
			int portInitial=4444;
			//Il est possible de changer le port de départ
			if(args !=null && args.length != 0){
				portInitial = Integer.parseInt(args[0]);
			}
			int port = portInitial;
			
			Engine pong = new OwnEngine();
			int i;
			for(i=0; i< 10 ;i++){
				Server contract = pong.listen(port, new AcceptCallbackTest());
				System.out.println("Server is listening on port: "+ port);
				port++;
			}
			//pong.startEcho();
			new Thread((OwnEngine)pong).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
