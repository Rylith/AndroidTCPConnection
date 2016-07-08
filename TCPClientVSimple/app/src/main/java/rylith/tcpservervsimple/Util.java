package rylith.tcpservervsimple;

import android.graphics.Point;

public class Util {

  /** Read a signed 32bit value */
  static public int readInt32(byte bytes[], int offset) {
    int val;
    val = ((bytes[offset] & 0xFF) << 24);
    val |= ((bytes[offset+1] & 0xFF) << 16);
    val |= ((bytes[offset+2] & 0xFF) << 8);
    val |= (bytes[offset+3] & 0xFF);
    return val;
  }


  /** Write a signed 32bit value */
  static public void writeInt32(byte[] bytes, int offset, int value) {
    bytes[offset]= (byte)((value >> 24) & 0xff);
    bytes[offset+1]= (byte)((value >> 16) & 0xff);
    bytes[offset+2]= (byte)((value >> 8) & 0xff);
    bytes[offset+3]= (byte)(value & 0xff);
  }

  static public double distance(Point p1, Point p2){
     double deltaX = p1.x - p2.x;
     double deltaY = p1.y - p2.y;
     double result = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
      return result;
  }

  static public double angle(Point center, Point target){
      double angle = Math.toDegrees(Math.atan2(target.y - center.y, target.x - center.x));

      if(angle<0){
          angle+=360;
      }
      return angle;

  }

}
