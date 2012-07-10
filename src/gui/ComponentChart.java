package gui;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import datastore.Const;
import datastore.SchemeManager;

import util.DCUtil;
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
//System.err.println("Calculate max and min " + data.length);      
      maxValue = Float.MIN_VALUE;
      minValue = Float.MAX_VALUE;
      for (int i=0; i < data.length; i++) {
         if ( data[i] > maxValue) maxValue = data[i];
         if ( data[i] < minValue) minValue = data[i];
      }
      
      for (int i=0; i < c_data.length; i++) {
         if ( c_data[i] > c_maxValue) c_maxValue = c_data[i];
         if ( c_data[i] < c_minValue) c_minValue = c_data[i];
      }
   }
   
   public int id;
   public float data[];    // Default data set
   public float c_data[];  // Comparative data set
   
   public float width;
   public float height;   
   public float anchorX = 0;
   public float anchorY = 0;   
   public float maxValue = 0;
   public float minValue = 0;
   public float c_maxValue = 0;
   public float c_minValue = 0;
   
   public DCColour colour;   
   
   public boolean active = true;
   
   // hack for global max
   public float occ = 0;
   public float c_occ = 0;
   
   // For labels and font
   public static Color labelColour = Color.BLACK;
   public static Color labelColourInactive = SchemeManager.inactive.awtRGBA();
   //public static Font  labelFont  = new Font("Arial", Font.PLAIN, 12);
   public static Font  labelFont  = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 16);
   public TextureFont tf = new TextureFont();     
   public float labelBuffer = 20.0f;
}
