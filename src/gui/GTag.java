package gui;

public class GTag {
   public GTag(float _x, float _y, float _yp, String _s, String _v) {
      x = _x;   
      y = _y;
      txt = _s;
      val = _v;
      yPrime = _yp; // bottom Y
   }
   public float x; 
   public float y;
   public float yPrime;
   public String txt;
   public String val;
}   