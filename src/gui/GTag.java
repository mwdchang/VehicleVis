package gui;

public class GTag {
   public GTag(float _x, float _y, float _yp, String _s, String _v, int _num) {
      x = _x;   
      y = _y;
      txt = _s;
      val = _v;
      yPrime = _yp; // bottom Y
      num = _num;
   }
   public float x; 
   public float y;
   public float yPrime;
   public String txt;
   public String val;
   
   public int num;
}   