package gui;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import util.TextureFont;

import model.DCColour;

/////////////////////////////////////////////////////////////////////////////////
// Contract for rendering a small chart for a component
// It is up to the implementation to decide how to represent the data
/////////////////////////////////////////////////////////////////////////////////
public abstract class ComponentChart {
   
   public abstract void setLabel(String txt);
   public abstract void render(GL2 gl2);
   public abstract void createSegment(int seg);
   public abstract void setHighlight(short data[]);
   public abstract void renderImmediate(GL2 gl2);
   public abstract void renderBorder(GL2 gl2);
   
   
   public void setMaxValue(float max) { maxValue = max; }
   public void setMinValue(float min) { minValue = min; }
   public void setData(float d[]) { data = d; }
   public void setAnchor(float x, float y) { anchorX = x; anchorY = y; }
   public void resize(float w, float h) { width = w; height = h; }
   public void calcMaxMin() {
      maxValue = Float.MIN_VALUE;
      minValue = Float.MAX_VALUE;
      for (int i=0; i < data.length; i++) {
         if ( data[i] > maxValue) maxValue = data[i];
         if ( data[i] < minValue) minValue = data[i];
      }
   }
   
   public int id;
   public float data[];
   public float width;
   public float height;   
   public float anchorX = 0;
   public float anchorY = 0;   
   public float maxValue = 0;
   public float minValue = 0;
   public DCColour colour;   
   
   public boolean active = true;
   
   // For labels and font
   public static Color labelColour = Color.BLACK;
   public static Font  labelFont  = new Font("Arial", Font.PLAIN, 12);
   public TextureFont tf = new TextureFont();     
}
