package mouse.control;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class MouseControl {
	
	private static final boolean DEBUG = false;
	private Robot mouse;
	private static int COEF = 2;
	public MouseControl(){
		 try {
				this.mouse = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
	}
	
	public void motion(int x, int y){
		Point current_point = MouseInfo.getPointerInfo().getLocation();
		if(DEBUG)
			System.out.println("MouseControl: x,y " + x +","+y);
		int n_x = -x * COEF + current_point.x;
		int n_y = -y * COEF + current_point.y;
		if(DEBUG)
			System.out.println("MouseControl : n_x, n_y "+ n_x +", "+n_y);
		mouse.mouseMove(n_x, n_y);
	}
	
	public void press(){
		mouse.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public void release(){
		mouse.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

}
