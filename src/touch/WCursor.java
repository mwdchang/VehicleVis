package touch;

import java.util.Vector;

import model.LensAttrib;

import TUIO.TuioCursor;
import TUIO.TuioPoint;

////////////////////////////////////////////////////////////////////////////////
// Place holder for location aware 
////////////////////////////////////////////////////////////////////////////////
public class WCursor {
   public WCursor() {}
   
   public WCursor(int e) {
      element = e;
      cursor = null;
      x = y = 0;
      oldX = 0;
      oldY = 0;
      startTimestamp = System.currentTimeMillis();
   }
   
   
   public WCursor(int e, TuioCursor c) {
      element = e;
      cursor = c;
      x = c.getX();
      y = c.getY();
      timestamp = c.getTuioTime().getTotalMilliseconds();
      updTimestamp = timestamp;
      sessionID = c.getSessionID();
      oldX = 0;
      oldY = 0;
      startTimestamp = System.currentTimeMillis();
   }
   
   
   
   public long sessionID = 0;
   //public int tap = 0;
   public long timestamp;      // start timestamp
   
   public long startTimestamp; // start timestamp
   public long updTimestamp;   // updated timestamp
   public long endTimestamp;   // timestamp of removal
   
   public float oldX, oldY;    // Normalized previous positions 
   public float x, y;          // Normalized positions 
   public int element;         // The element associated with the touch point
   public TuioCursor cursor; 
   
   public LensAttrib lensReference;
   public int lensIndex = -1;
   
   public int state = STATE_NOTHING;
   public int intention = NOTHING;
   
   public int numUpdate = 0;
   
   public static int STATE_NOTHING = 0;
   public static int STATE_MOVE = 10;
   public static int STATE_SWIPE = 11;
   public static int STATE_HOLD = 12;
   
   public Vector<TuioPoint> points = new Vector<TuioPoint>();
   
   public int swipeCount = 0;
   public int swipeDirection = -1;
   public int tapCount = 0;
   
   
   public static int LEFT = 0;
   public static int RIGHT = 1;
   public static int UP = 2;
   public static int DOWN = 3;
   
   
   public static int NOTHING = 0;
   public static int MOVE_ELEMENT = 1;
   public static int SCROLL_ELEMENT = 2;
}
