package touch;

import TUIO.TuioCursor;

////////////////////////////////////////////////////////////////////////////////
// Place holder for location aware 
////////////////////////////////////////////////////////////////////////////////
public class WCursor {
   public WCursor(int e) {
      element = e;
      cursor = null;
   }
   public WCursor(int e, TuioCursor c) {
      element = e;
      cursor = c;
   }
   
   // Short cut
   
   public int element;
   public TuioCursor cursor;
}
