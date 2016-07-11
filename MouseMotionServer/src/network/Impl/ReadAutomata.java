package network.Impl;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class ReadAutomata {

	SocketChannel sock;
	ByteBuffer lenBuf = ByteBuffer.allocate(4); // for reading the length of a message
	ByteBuffer msgBuf = null; // for reading a message
	static final int READING_LENGTH = 1;//State, can be an enum if there are more states
	static final int READING_MSG = 2;
	int state = READING_LENGTH; // initial state
	List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	private  boolean connectionClosed = false; 
	
	public boolean isConnectionClosed() {
		return connectionClosed;
	}

	public ReadAutomata(SocketChannel socketChannel){
		this.sock=socketChannel;
	}
	
	public byte[] handleRead() throws IOException {
		boolean find= false;
		int index = 0;
		if (state == READING_LENGTH){
			int nbread = sock.read(lenBuf);
			 if (nbread == -1) {
				 //Error of reading bytes, so close the socket to prevent other error
				 sock.close();
				 connectionClosed=true;
				 return null;
			 }
			if (lenBuf.remaining() == 0) {
				//Read the length
				int length = Util.readInt32(lenBuf.array(), 0);
				int size = buffers.size();
				for(int i=0;i<size;i++){
					if(buffers.get(i).capacity() == length){
						find=true;
						index=i;
					}
				}
				if(find){
					msgBuf = buffers.get(index);
					//System.out.println("buffer with a right size found");
				}else {
					try{
					msgBuf = ByteBuffer.allocate(length);
					buffers.add(msgBuf);
					}catch(OutOfMemoryError e){
						System.err.println(e);
						sock.close();
						connectionClosed=true;
						return null;
					}
					//System.out.println("No buffer found : length : " + length);
				}
				msgBuf.clear();
				lenBuf=(ByteBuffer) lenBuf.position(0);
				state = READING_MSG;
			}
		} 
		
		if (state == READING_MSG) {
			sock.read(msgBuf);
			if (msgBuf.remaining() == 0){ // the message has been fully received
				  // deliver it"
				byte[] msg =msgBuf.array();
				state = READING_LENGTH;
				return msg;
			}
		}
		return null;
	}

}
