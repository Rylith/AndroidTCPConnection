package network.Impl;

import java.awt.MouseInfo;

import mouse.control.MouseControl;
import network.Interface.Channel;
import network.Interface.DeliverCallback;


public class DeliverCallbackTest implements DeliverCallback {
	private static boolean DEBUG = false;
	MouseControl mouse = new MouseControl();
	
	public void deliver(Channel channel, byte[] bytes) {
		String msg=new String(bytes);
		String[] x_y = msg.split(",");
		switch (x_y[0]) {
		case "SCROLL":
			//Pre-calculated "distance" in x (could be negative to inform direction)
			int x = Integer.parseInt(x_y[1]);
			if(DEBUG)
				System.out.println("receive this quantity in x : " + x);
			//Pre-calculated distance in y
			int y = Integer.parseInt(x_y[2]);
			if(DEBUG)
				System.out.println("receive this quantity in y : " + y);
			//Move the cursor with the distance
			if(DEBUG)
				System.out.println("Before motion : " + MouseInfo.getPointerInfo().getLocation());
			mouse.motion(x, y);
			break;
		case "PRESS":
			mouse.press();
			break;
		case "RELEASE":
			mouse.release();
			break;
		case "CLICK":
			mouse.press();
			mouse.release();
			break;
		default:
			break;
		}
		if(DEBUG){
			System.out.println("After motion : " + MouseInfo.getPointerInfo().getLocation());
			System.out.println("Message : "+msg +" on channel " + channel.toString());
		}
	}

}
