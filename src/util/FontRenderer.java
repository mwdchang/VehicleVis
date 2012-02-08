package util;

import java.awt.Font;
import java.util.Hashtable;

import javax.media.opengl.GL2;

import model.DCColour;

import com.jogamp.opengl.util.awt.TextRenderer;


/////////////////////////////////////////////////////////////////////////////////
// Static instance to render font
// Save some memory footprint but nothing special
/////////////////////////////////////////////////////////////////////////////////
public class FontRenderer {
   
   private FontRenderer() {
      String fontFamily = "Arial";

      // Register a variety of font sizes
      DWin.instance().msg("Creating fonts...[" + 1 + " - " + MAX_SIZE + "]");
      for (int i=1; i <= MAX_SIZE; i++) {
         TextRenderer t = new TextRenderer(new Font( fontFamily, Font.PLAIN, i));
         t.setSmoothing(false);
         t.setUseVertexArrays(true);  // Speed up ?
         sizeMap.put( i, t );
      }
      
      render = sizeMap.get( DEFAULT_SIZE );

      /*
      render = new TextRenderer(new Font("SansSerif", Font.PLAIN, 14));
      render.setSmoothing(true);
      */
   }

   
   public static FontRenderer instance() {
      if (inst == null) 
         inst = new FontRenderer();
      return inst;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the bounding box of the string literal
   ////////////////////////////////////////////////////////////////////////////////
   public double[] getDimension(String s) {
      double h = render.getBounds(s).getHeight();
      double w = render.getBounds(s).getWidth();
      return new double[]{w, h};
   }
   
   
   /*
   public void renderOnceUp(GL2 gl2, float x, float y, float z, String s) {
      render.begin3DRendering();
      render.setColor(0, 0, 0, 1);
      render.draw3D(s, x, y, z, 1.0f);
      render.end3DRendering();
   }
   */


   ////////////////////////////////////////////////////////////////////////////////  
   // Set the renderer to a different font size
   ////////////////////////////////////////////////////////////////////////////////  
   public void setSize( int v ) {
      render = sizeMap.get( v );
      if (render == null) {
         render = sizeMap.get( MAX_SIZE );   
         //DWin.instance().error("Trying to access invalid font " + v);
      }
   }

   
   public void renderOnce(GL2 gl2, float x, float y, float z, String s) {
      renderOnce(gl2, x, y, z, 1.0f, s);
   }


   ////////////////////////////////////////////////////////////////////////////////
   // A complete texture rendering pass, may be a bit slow ???
   ////////////////////////////////////////////////////////////////////////////////
   public void renderOnce(GL2 gl2, float x, float y, float z, float f, String s) {
      render.begin3DRendering();
      if (fontColour != null) {
         render.setColor(fontColour.r, fontColour.g, fontColour.b, fontColour.a);
      } else {
         render.setColor(0, 0, 0, 1);
      }
      render.draw3D(s, x, y, z, f);
      render.end3DRendering();
      
   }
   
   public void setColour(DCColour c) {
      fontColour = c;    
   }
   
   
   public static int MAX_SIZE = 64;
   public static int DEFAULT_SIZE = 14;
   
   public Hashtable<Integer, TextRenderer> sizeMap = new Hashtable<Integer, TextRenderer>();   
   public TextRenderer render;
   public DCColour fontColour;
   private static FontRenderer inst;
}
