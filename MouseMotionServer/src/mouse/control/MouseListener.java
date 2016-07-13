package mouse.control;

import java.awt.Point;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import network.Impl.Util;

public class MouseListener {

	private Point center,origin;
	private Point current;
	private Point prec;
	
	private int RAYON;
	private static final float PERCENTSCREENSIZE = 0.00f;
	
	private static double MARGE = 40;
    private double[] bufferX = new double[20];
    private double[] bufferY = new double[20];

    //To time the event on drag
    private ScheduledFuture<?> timerChangeMode = null;
    private ScheduledExecutorService task = Executors
            .newSingleThreadScheduledExecutor();
    
    private static final long TIMER_AFF = 500 ;
    private TimerTask change_mode = new TimerTask() {
        @Override
        public void run() {
            origin = current;
            borderMode = true;
        }
    };
    
    private boolean borderMode=false;
    private boolean reglin=false;
    
    private int previousSign=0;
    private int compteur=0;
    private double lastPointOnstraightLineX;
    private double lastPointOnstraightLineY;
    
    double[] coefs;
	private MouseControl mouse;
	
	public MouseListener(MouseControl mouse){
		this.mouse = mouse;
	}
    
    public void setCenter(int x, int y) {
		center = new Point(x/2,y/2);
		RAYON = center.x;
        MARGE = center.x*PERCENTSCREENSIZE;
	}
	
	public void onScroll(float x, float y, float distanceX, float distanceY) {

		int dist_x;
		int dist_y;
		
		current=new Point((int)x,(int)y);
		
		double COEF;
		
		double distance = Util.distance(center,current);
		//Log.v("BORDER", "distance: "+distance+" zone: " +(RAYON-MARGE));
		if(distance < (RAYON - MARGE)){
			if(timerChangeMode != null){
				timerChangeMode.cancel(false);
			}
			dist_x= (int) distanceX;
			dist_y= (int) distanceY;
			bufferX[compteur]=x;
			bufferY[compteur]=y;
			lastPointOnstraightLineX=bufferX[compteur];
			lastPointOnstraightLineY=bufferY[compteur];
			compteur=(compteur+1)% bufferX.length;
			borderMode=false;
			reglin=true;
			COEF=1;
		}else if(borderMode){
		
			double angleCur = Math.abs(Util.angle(center,current));
			double anglePrec = Math.abs(Util.angle(center,prec));
			
			int sign = (int) Math.signum(angleCur-anglePrec);
			//Detect reversal of the direction of rotation
			if(sign != previousSign){
				previousSign=sign;
				if(previousSign != 0){
				    origin=prec;
				}
			}
			
			double angleOr = Math.abs(Util.angle(center,origin));
			//Log.v("BORDER MODE", "angle du courant " +angleCur+" angle de l'origine: " + angleOr);
			sign = (int) Math.signum(angleCur-angleOr);
			
			//Log.v("BORDER","signe: "+sign);
			//Calcul of coefficients for the straight line
			if(reglin){
				coefs = Util.regress(bufferY,bufferX);
			}
			COEF=Math.abs(angleCur-angleOr);
			//Calcul y in function of the new x to stay on the straight line
			double y1=(coefs[0]*(lastPointOnstraightLineX + COEF)+coefs[1]);
			dist_x= (int) (sign*COEF);
			dist_y= (int) (sign*(y1 - lastPointOnstraightLineY));
			
			lastPointOnstraightLineX+=COEF;
			lastPointOnstraightLineY=y1;
			reglin=false;
		
		}else{
			if(timerChangeMode == null || timerChangeMode.isCancelled() || timerChangeMode.isDone()){
				timerChangeMode = task.schedule(change_mode, TIMER_AFF, TimeUnit.MILLISECONDS);
			}
			dist_x= /*(int) distanceX*/0;
			dist_y= /*(int) distanceY*/0;
		}
		
		prec=current;
		mouse.motion(dist_x, dist_y);
	}

	public void resetBuffers(float x,float y) {
		compteur=0;
		borderMode=false;
		reglin=true;
        for(int i = 0; i< bufferX.length; i++){
            bufferX[i]=Integer.MIN_VALUE;
        }
        for(int i = 0; i< bufferY.length; i++){
            bufferX[i]=Integer.MIN_VALUE;
        }
        //Init de the prec point before scrolling
        prec=new Point((int)x,(int)y);
		
	}
	
	

}
