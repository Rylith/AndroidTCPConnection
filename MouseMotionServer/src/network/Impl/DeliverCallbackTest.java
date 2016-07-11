package network.Impl;

import java.awt.MouseInfo;

import mouse.control.MouseControl;
import mouse.control.MouseListener;
import network.Interface.Channel;
import network.Interface.DeliverCallback;


public class DeliverCallbackTest implements DeliverCallback {
	private static boolean DEBUG = false;
	MouseControl mouse = new MouseControl();
	MouseListener listener = new MouseListener(mouse);
	
	public void deliver(Channel channel, byte[] bytes) {
		String msg=new String(bytes);
		String[] x_y = msg.split(",");
		switch (x_y[0]) {
			case "SCROLL":
				//Coordinates of current point
				float x = Float.parseFloat(x_y[1]);
				float y = Float.parseFloat(x_y[2]);
				//Pre-calculated "distance" in x (could be negative to inform direction)
				float distanceX = Float.parseFloat(x_y[3]);
				if(DEBUG)
					System.out.println("receive this quantity in x : " + x);
				//Pre-calculated distance in y
				float distanceY = Float.parseFloat(x_y[4]);
				if(DEBUG)
					System.out.println("receive this quantity in y : " + y);
				//Move the cursor with the distance
				if(DEBUG)
					System.out.println("Before motion : " + MouseInfo.getPointerInfo().getLocation());
				
				listener.onScroll(x, y, distanceX, distanceY);
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
			case "WINDOW":
				int xC = Integer.parseInt(x_y[1]);
				int yC = Integer.parseInt(x_y[2]);
				listener.setCenter(xC,yC);
				break;
			case "DOWN":
				float xD = Float.parseFloat(x_y[1]);
				float yD = Float.parseFloat(x_y[2]);
				listener.resetBuffers(xD, yD);
			default:
				break;
		}
		if(DEBUG){
			System.out.println("After motion : " + MouseInfo.getPointerInfo().getLocation());
			System.out.println("Message : "+msg +" on channel " + channel.toString());
		}
	}

}
