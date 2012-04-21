package touch;

public class TapAction {
   public TapAction() {
      x=y=0;   
   }
   
   public TapAction(float x, float y) {
     this.x = x;
     this.y = y;
   }
   
   public float x, y;
   public int numTap = 1;
}
