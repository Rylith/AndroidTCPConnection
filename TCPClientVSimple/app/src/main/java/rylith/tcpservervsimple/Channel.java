package rylith.tcpservervsimple;

import java.net.InetSocketAddress;
/**
 * This class wraps an end-point of a channel.
 * It allows to send and receive messages, 
 * stored in ByteBuffers. 
 */
public abstract class Channel {

  /**
   * Set the callback to deliver messages to.
   * @param callback
   */
  public abstract void setDeliverCallback(DeliverCallback callback);

  /**
   * Get the Inet socket address for the other side of this channel.
   * @return
   */
  public abstract InetSocketAddress getRemoteAddress();

  /**
   * Sending the given byte array, a copy is made into internal buffers,
   * so the array can be reused after sending it.
   * @param bytes
   * @param offset
   * @param length
   */
  public abstract void send(byte[] bytes, int offset, int length);

  
  public abstract void close();

}