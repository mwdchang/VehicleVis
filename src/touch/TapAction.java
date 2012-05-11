package touch;

////////////////////////////////////////////////////////////////////////////////
// Stores an action/touch point
////////////////////////////////////////////////////////////////////////////////
public class TapAction {
   public TapAction() {
      x=y=0;   
   }
   
   public TapAction(float x, float y) {
     this.x = x;
     this.y = y;
   }
   
   public TapAction(float x, float y, int element) {
     this.x = x;
     this.y = y;
     this.element = element;
   }
   
   public float x, y;
   public int numTap = 1;
   
   public int element;
}
