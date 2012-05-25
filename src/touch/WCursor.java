package touch;

import java.util.Vector;

import TUIO.TuioCursor;
import TUIO.TuioPoint;

////////////////////////////////////////////////////////////////////////////////
// Place holder for location aware 
////////////////////////////////////////////////////////////////////////////////
public class WCursor {
   public WCursor(int e) {
      element = e;
      cursor = null;
      x = y = 0;
   }
   
   
   public WCursor(int e, TuioCursor c) {
      element = e;
      cursor = c;
      x = c.getX();
      y = c.getY();
      timestamp = c.getTuioTime().getTotalMilliseconds();
      sessionID = c.getSessionID();
   }
   
   
   
   public long sessionID = 0;
   public int tap = 0;
   public long timestamp;      // timestamp
   public float x, y;          // Normalized positions 
   public int element;         // The element associated with the touch point
   public TuioCursor cursor; 
   
   public int state = STATE_NOTHING;
   
   public int numUpdate = 0;
   
   public static int STATE_NOTHING = 0;
   public static int STATE_MOVE = 10;
   public static int STATE_SWIPE = 11;
   public Vector<TuioPoint> points = new Vector<TuioPoint>();
   
   
}
