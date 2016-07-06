package rylith.tcpservervsimple;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TCPClient {

    private String serverMessage;

    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private byte[] msg="0".getBytes();
    PrintWriter out;
    BufferedReader in;

    private Selector m_selector;
    private Map<SelectionKey, Channel> listKey= new HashMap<SelectionKey, Channel>();
    private Map<SelectionKey, Server> mapServer = new HashMap<SelectionKey, Server>();

    private ConnectCallback connectCallback;
    private AcceptCallback acceptCallback;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
        try {
            m_selector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */


    public void sendMessage(String message,int offset, int length){
        for(Map.Entry<SelectionKey, Channel> mapEntry : listKey.entrySet()){
            SelectionKey key = mapEntry.getKey();
            Channel channel = mapEntry.getValue();
            channel.send(message.getBytes(), offset, length);
            //Log.v("message send",message);
            handleWrite(key);
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {

            try {

                //receive the message which the server sends back

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    try {
                        m_selector.select();

                        Iterator<?> selectedKeys = this.m_selector.selectedKeys().iterator();

                        while (selectedKeys.hasNext()) {

                            SelectionKey key = (SelectionKey) selectedKeys.next();
                            selectedKeys.remove();

                            if (!key.isValid()) {
                                continue;
                            } else if (key.isAcceptable()) {
                                handleAccept(key);

                            } else if (key.isReadable()) {
                                handleRead(key);

                            } else if (key.isWritable()) {
                                handleWrite(key);

                            } else if (key.isConnectable()) {
                                handleConnect(key);
                            } else
                                System.out.println("  ---> unknown key=");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public void connect(InetAddress hostAddress, int port) throws UnknownHostException,
            SecurityException, IOException {
        SocketChannel m_ch;
        //Création de la socket de connexion
        m_ch = SocketChannel.open();
        m_ch.configureBlocking(false);
        m_ch.socket().setTcpNoDelay(true);

        SelectionKey m_key;
        // be notified when the connection to the server will be accepted
        m_key = m_ch.register(m_selector, SelectionKey.OP_CONNECT);

        // request to connect to the server
        m_ch.connect(new InetSocketAddress(hostAddress, port));
        Channel channel = new ChannelTest(m_ch);
        channel.setDeliverCallback(new DeliverCallbackTest());
        listKey.put(m_key, channel);
    }

    //Utilisé quand la connexion est acceptée
    private void handleConnect(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            return;
        }
        //Callback avertissant de la connection
        Channel channel = listKey.get(key);
        //connectCallback.connected(channel);
        //channel.send(msg, 0, msg.length);
        key.interestOps(SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);
    }

    private void handleWrite(SelectionKey key) {
        boolean finish = true;
        //System.out.println("call of handleWrite");
        Channel channel = listKey.get(key);
        try {
            finish=((ChannelTest) channel).getWriteAutomata().handleWrite();
            if(finish && ((ChannelTest) channel).getWriteAutomata().isEmpty()){
                key.interestOps(SelectionKey.OP_READ);
            }else{
                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int nbread = 0;
        Channel channel = listKey.get(key);
        try {
            msg=((ChannelTest) channel).getReadAutomata().handleRead();
        } catch (IOException e) {
            // the connection as been closed unexpectedly, cancel the selection and close the channel
            key.cancel();
            try {
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
        if (nbread == -1) {
            // the socket has been shutdown remotely cleanly"
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key.cancel();
            return;
        }
        //Si le message est non null c'est qu'il a été lu entièrement
        if(msg != null){
            ((ChannelTest) channel).getCallback().deliver(channel, msg);
            //msg = String.valueOf(Integer.valueOf(new String(msg))+1).getBytes();
            //channel.send(msg, 0, msg.length);
        }

    }

    private void handleAccept(SelectionKey key) {
        SocketChannel socketChannel = null;
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            // as if there was no accept done
            return;
        }
        // be notified when there is incoming data
        try {
            SelectionKey m_key = socketChannel.register(this.m_selector, SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);
            //Création du channel
            Channel channel = new ChannelTest(socketChannel);
            channel.setDeliverCallback(new DeliverCallbackTest());
            //Callback d'acceptation
            Server server = mapServer.get(key);
            acceptCallback.accepted(server, channel);
            listKey.put(m_key, channel);
        } catch (ClosedChannelException e) {
            handleClose(socketChannel);
        }
    }

    private void handleClose(SocketChannel socketChannel) {
        socketChannel.keyFor(m_selector).cancel();
        try{
            socketChannel.close();
        } catch (IOException e) {
            //nothing to do, the channel is already closed
        }

    }

}
