package rylith.tcpservervsimple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;

public class MyActivity extends Activity
{
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
    public static final String SERVERIP = "192.168.43.164"; //your computer IP address
    public static final int SERVERPORT = 4446;
    View.OnTouchListener gestureListener;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        pos = (TextView) findViewById(R.id.pos);
        image = (ImageView) this.findViewById(R.id.image);
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
        sheet = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        board = new Canvas(sheet);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        image.setImageBitmap(sheet);


        // connect to the server
        new connectTask().execute("");
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

                int dist_x= (int) distanceX;
                int dist_y=(int) distanceY;
                final String message = "SCROLL,"+dist_x+","+dist_y;

                if (mTcpClient != null) {
                    //Log.v("Coordinates",message);
                    //new sendTask().execute(message);
                    new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                pos.setText("Pos:\n" +"X: "+ event.getX()+"\nY: "+event.getY());
                paint.setColor(Color.BLUE);
                board.drawPoint(event.getX(),event.getY(),paint);
                paint.setColor(Color.GREEN);
                image.invalidate();
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
                /*if(event.getAction() == android.view.MotionEvent.ACTION_UP){
                    final String message = "RELEASE,0,0";

                    if (mTcpClient != null) {
                        //Log.v("Coordinates",message);
                        //new sendTask().execute(message);
                        new Thread(){public void run() {mTcpClient.sendMessage(message,0,message.length());}}.start();

                    }
                    return true;
                }else{*/
                    return mDetector.onTouchEvent(event);
               // }
            }};
        image.setOnTouchListener(gestureListener);

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
            });
            try{
            InetAddress address = InetAddress.getByName(SERVERIP);
                Log.v("Address", address.toString());
                Log.v("Port",Integer.toString(SERVERPORT));
                mTcpClient.connect(address,SERVERPORT);
                mTcpClient.run();
            }
            catch (java.io.IOException e) {
                e.printStackTrace();
            }


            return null;
        }

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
