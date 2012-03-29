package model;

import java.awt.Color;

/////////////////////////////////////////////////////////////////////////////////
// Just a wrapper for RGBA values so they can be stored
// and computed at the update cycle
/////////////////////////////////////////////////////////////////////////////////
public class DCColour {
   
   // Copy constructor
   public DCColour(DCColour c) {
      r = c.r;
      g = c.g;
      b = c.b;
      a = c.a;
   }
   
   
   
   // Integer constructor 0-255
   // Do not use this....not sure where is using int...track it down later
   public DCColour(int _r, int _g, int _b, int _a) {
      r = (float)_r/255.0f;
      g = (float)_g/255.0f;
      b = (float)_b/255.0f;
      a = (float)_a/255.0f;
   }
   
   // Float constructor 0.0-1.0
   public DCColour( float _r, float _g, float _b, float _a) {
      r = _r; g = _g; b = _b; a = _a;      
   }
   
   // Double constructor 0.0-1.0
   public DCColour ( double _r, double _g, double _b, double _a) {
      r = (float)_r;
      g = (float)_g;
      b = (float)_b;
      a = (float)_a;
   }
   
   public DCColour( double r, double g, double b) { 
      this(r, g, b, 0.0); 
   }
   
   
   public DCColour() { 
      this(0.0f, 0.0f, 0.0f, 0.0f);
   }
   
   public DCColour add(DCColour d) {
      return new DCColour(
         Math.min(1.0f, Math.max(0.0, d.r+r)),   
         Math.min(1.0f, Math.max(0.0, d.g+g)),   
         Math.min(1.0f, Math.max(0.0, d.b+b)),   
         Math.min(1.0f, Math.max(0.0, d.a+a))   
      );
   }
   
   
   public DCColour adjustAlpha(float v) {
      return new DCColour(r, g, b, a*v);	
   }
   
   
   public float[] toArray() {
      return new float[]{ r, g, b, a};
   }
   
   
   public String toString() {
      return "[" + r + ", " + g + ", " + b + ", " + a + "]";    
   }
   
   public float sum() {
      return r+g+b+a;
   }
   
   
   // Convert to AWT
   public Color awtRGB() {
      return new Color((int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f), (int)(1.0*255.0f));
   }
   
   public Color awtRGBA() {
      return new Color((int)(r*255.0f), (int)(g*255.0f), (int)(b*255.0f), (int)(a*255.0f));
   }

   // Alternate constructions 
   public static DCColour fromInt(int r, int g, int b, int a) {
      return new DCColour( (float)r/255.0f, (float)g/255.0f, (float)b/255.0f, (float)a/255.0f);   
   }
   public static DCColour fromInt(int r, int g, int b) {
      return fromInt(r, g, b, 255);
   }
   public static DCColour fromFloat(float r, float g, float b, float a) {
      return new DCColour(r, g, b, a); 
   }
   public static DCColour fromDouble(double r, double g, double b, double a) {
      return new DCColour(r, g, b, a); 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Setter and getter hack for animation evaluator
   ////////////////////////////////////////////////////////////////////////////////
   public void setColour(DCColour c) {
      r = c.r;
      g = c.g;
      b = c.b;
      a = c.a;
   }
   public DCColour getColour() {
      return this;   
   }
   ////////////////////////////////////////////////////////////////////////////////
   
   
   
   
   public float r,g,b,a;
   
}
