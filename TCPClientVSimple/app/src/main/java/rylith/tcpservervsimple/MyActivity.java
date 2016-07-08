package rylith.tcpservervsimple;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MyActivity extends Activity
{
    private static final long TIMER_AFF = 500 ;
    private static final float PERCENTSCREENSIZE = 0.1f;
    private static double COEF=1;
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;
    private GestureDetector mDetector;
    private TextView pos;
    private Canvas board;
    private Bitmap sheet;
    private Paint paint;
    private ImageView image;
    public static String SERVERIP = "192.168.43.43"; //your computer IP address
    public static int SERVERPORT = 4446;
    private View.OnTouchListener gestureListener;
    private TextView response;
    private EditText editTextAddress, editTextPort;
    private Button buttonConnect, buttonClear;
    private Activity activity;
    public static final String PREFS_SERV = "MyPrefsServ";
    private Point center;
    private Point current,prec,origin;
    private int RAYON;
    private static double MARGE = 40;
    private double[] bufferX = new double[10];
    private double[] bufferY = new double[10];

    //To time the event on drag
    private ScheduledFuture<?> timerChangeMode = null;
    private ScheduledExecutorService task = Executors
            .newSingleThreadScheduledExecutor();

    private boolean borderMode=false;
    private int previousSign=0;

    private TimerTask change_mode = new TimerTask() {
        @Override
        public void run() {
            origin = current;
            borderMode = true;
            Log.v("BORDER","Timer is active");
        }
    };
    private int compteur=0;
    private double lastPointOnstraightLineX;
    private double lastPointOnstraightLineY;
    private boolean reglin=false;
    double[] coefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        activity=this;
        pos = (TextView) findViewById(R.id.pos);
        image = (ImageView) this.findViewById(R.id.image);
        editTextAddress = (EditText) findViewById(R.id.addressEditText);
        editTextPort = (EditText) findViewById(R.id.portEditText);
        buttonConnect = (Button) findViewById(R.id.connectButton);
        buttonClear = (Button) findViewById(R.id.clearButton);
        response = (TextView) findViewById(R.id.responseTextView);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_SERV, 0);
        SERVERIP=settings.getString("SERVERIP","192.168.43.43");
        SERVERPORT = settings.getInt("serverPort", 4446);

        editTextAddress.setText(SERVERIP);
        editTextPort.setText(Integer.toString(SERVERPORT));
        //arrayList = new ArrayList<String>();

       // final EditText editText = (EditText) findViewById(R.id.editText);
       // Button send = (Button)findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        //mList = (ListView)findViewById(R.id.list);
        //mAdapter = new MyCustomAdapter(this, arrayList);
        //mList.setAdapter(mAdapter);
        //Creating canvas for drawing
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(screenSize);
        center = new Point((screenSize.x/2),(screenSize.y/2));
        Log.v("BORDER","coordonnée du centre: " + center.x + ", " + center.y);
        //if the watch is circular
        RAYON = screenSize.x/2;
        MARGE = screenSize.x*PERCENTSCREENSIZE;
        Log.v("BORDER","taille du rayon: " + RAYON);

        /*sheet = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        board = new Canvas(sheet);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        image.setImageBitmap(sheet);*/


        // connect to the server
        //new connectTask().execute("");
        mDetector = new GestureDetector(MyActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent event) {
                final String message = "PRESS,0,0";

                if (mTcpClient != null) {
                    //Log.v("Coordinates",message);
                    //new sendTask().execute(message);
                    new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                }
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
                pos.setText("Scroll:\n" +"X: "+ distanceX+"\nY: "+distanceY);

                int dist_x=0;
                int dist_y=0;
                current=new Point((int)e2.getX(),(int)e2.getY());
                double distance = Util.distance(center,current);
                //Log.v("BORDER", "distance: "+distance+" zone: " +(RAYON-MARGE));
                if(distance < (RAYON - MARGE)){
                    if(timerChangeMode != null){
                        timerChangeMode.cancel(false);
                    }
                    dist_x= (int) distanceX;
                    dist_y=(int) distanceY;
                    bufferX[compteur]=e2.getX();
                    bufferY[compteur]=e2.getY();
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
                    //Calcul of coefficient for the straight line
                    if(reglin){
                        coefs = Util.regress(bufferY,bufferX);
                    }
                    COEF=Math.abs(angleCur-angleOr);
                    //Calcul y in function of the new x to stay on the straight line
                    double y=(coefs[0]*(lastPointOnstraightLineX + COEF)+coefs[1]);
                    dist_x= (int) (sign*COEF);
                    dist_y= (int) (sign*(y - lastPointOnstraightLineY));
                    lastPointOnstraightLineX+=COEF;
                    lastPointOnstraightLineY=y;
                    reglin=false;

                }else{
                    if(timerChangeMode == null || timerChangeMode.isCancelled() || timerChangeMode.isDone()){
                        timerChangeMode = task.schedule(change_mode, TIMER_AFF,
                            TimeUnit.MILLISECONDS);
                    }

                    dist_x= (int) distanceX;
                    dist_y=(int) distanceY;
                }
                final String message = "SCROLL,"+dist_x+","+dist_y;

                if (mTcpClient != null) {
                    //Log.v("Coordinates",message);
                    //new sendTask().execute(message);
                    new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                }
                prec=current;
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                pos.setText("Pos:\n" +"X: "+ event.getX()+"\nY: "+event.getY());
                /*paint.setColor(Color.BLUE);
                board.drawPoint(event.getX(),event.getY(),paint);
                paint.setColor(Color.GREEN);
                image.invalidate();*/
                final String message = "CLICK,0,0";

                if (mTcpClient != null) {
                    //Log.v("Coordinates",message);
                    //new sendTask().execute(message);
                    new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                }
                return true;

            }

            @Override
            public boolean onDown(MotionEvent e) {
                pos.setText("Scroll:\n" +"X: "+e.getX()+"\nY: "+e.getY());
                compteur=0;
                for(int i = 0; i< bufferX.length; i++){
                    bufferX[i]=0;
                }
                for(int i = 0; i< bufferY.length; i++){
                    bufferX[i]=0;
                }
                prec=new Point((int)e.getX(),(int)e.getY());
                /*final String message = "PRESS,0,0";

                if (mTcpClient != null) {
                    //Log.v("Coordinates",message);
                    //new sendTask().execute(message);
                    new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                }*/
                return true;
            }


        }
        );
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == android.view.MotionEvent.ACTION_UP){
                    final String message = "RELEASE,0,0";
                    borderMode=false;
                    reglin=true;
                    COEF=1;
                    if(timerChangeMode != null){
                        timerChangeMode.cancel(false);
                    }
                    if (mTcpClient != null) {
                        //Log.v("Coordinates",message);
                        //new sendTask().execute(message);
                        new Thread() {
                            public void run() {
                                mTcpClient.sendMessage(message, 0, message.length());
                            }
                        }.start();

                    }
                }
                    return mDetector.onTouchEvent(event);

            }};
        image.setOnTouchListener(gestureListener);
        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new connectTask().execute(editTextAddress.getText()
                        .toString(),editTextPort
                        .getText().toString());
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editTextAddress.setText("");
                editTextPort.setText("");
            }
        });
        /*send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });*/

    }

    @Override
    protected void onStop() {
        Log.v("Stop_MainActivity","call of STOP");
        SharedPreferences settings = getSharedPreferences(PREFS_SERV, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SERVERIP", SERVERIP);
        editor.putInt("serverPort",SERVERPORT);
        // Commit the edits!
        editor.apply();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mTcpClient !=null){
            mTcpClient.closeConnection();
            mTcpClient.stopClient();
        }
        super.onDestroy();
    }

    /*@Override
    protected void onRestart() {
        Log.v("Restart_MainActivity","call of RESTART");
        new connectTask().execute(SERVERIP,Integer.toString(SERVERPORT));
        super.onRestart();
    }*/


    public class connectTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    //publishProgress(message);
                }
            },response,activity);
            try{
                if(!message[0].equals("") ){
                    SERVERIP = message[0];
                }
                if(!message[1].equals("")){
                    SERVERPORT = Integer.parseInt(message[1]);
                }
            InetAddress address = InetAddress.getByName(SERVERIP);
                Log.v("Address", address.toString());
                Log.v("Port",Integer.toString(SERVERPORT));
                mTcpClient.connect(address,SERVERPORT);
                mTcpClient.run();
            }
            catch (java.io.IOException e) {
                e.printStackTrace();
            }
            //Log.v("NETWORK","DoInBackground finished");

            return null;
        }


       /*@Override
        protected void onPostExecute(TCPClient tcpClient) {
            Log.v("PostExecute","call After sync task complete");
            switch (tcpClient.getConnectionState()){
                case "Connection failed":
                    response.setTextColor(Color.RED);
                    break;
                case "Successful connection":
                    response.setTextColor(Color.GREEN);
                    break;
                default:
            }

            response.setText(tcpClient.getConnectionState());

            super.onPostExecute(tcpClient);
        }*/



        /*@Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }*/
    }
}
