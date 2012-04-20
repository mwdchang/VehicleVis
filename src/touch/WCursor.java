package touch;

import TUIO.TuioCursor;

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
      x = y = 0;
   }
   public WCursor(int e, float x, float y, TuioCursor c) {
      element = e;
      cursor = c;
      this.x = x;
      this.y = y;
   }
   
   public float x, y;
   public int element;
   public TuioCursor cursor;
   
}
