package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Vector;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

/////////////////////////////////////////////////////////////////////////////////
// This class creates font on a texture and graphics2D, rather than using JOGL's
// TextRenderer. We are doing this for several reasons
//
// 1) Have a uniform coordinate system
// 2) Finer granularity of control, ie: if the texture is to be cached
//
// Note: It is assumed that there is a 1:1 mapping between the height/width of the
// texture to that of the screen pixel space
/////////////////////////////////////////////////////////////////////////////////
public class TextureFont {

   public TextureFont() {
      // Create a dummy texture get graphics2d context   
      texture = new TextureRenderer((int)100, (int)100, true, true);
      g2d = texture.createGraphics();
      fm = g2d.getFontMetrics();
      
      dirty = 1;
   }
   
   
   
   
   // Add a new marks to the collection of marks, does not check for dupes
   public void addMark(String s, Color c, Font f, float x, float y, boolean antiAlias) {
      marks.add( new Mark(s, c, f, x, height-y, antiAlias));
      dirty = 1;
   }
   public void addMark(String s, Color c, Font f, float x, float y) {
      marks.add( new Mark(s, c, f, x, height-y));
      dirty = 1;
   }
//   public void addMark(Mark m) {
//      marks.add( m );
//      dirty = 1;
//   }
   
   // Add a new mark in windows coordinate to the collection of marks, does
   // not check for dupes
   public void addMarkW(String s, Color c, Font f, float x, float y) {
      marks.add( new Mark(s, c, f, x, y));
      dirty = 1;
   }
   // Remove all marks
   public void clearMark() {
      marks.clear();      
      dirty = 1;
   }
   
   
   // Render to a texture context
   public void renderToTexture(Color c) {
      texture = null;
      texture = new TextureRenderer((int)width, (int)height, true, true);
      
      g2d = texture.createGraphics();      
      
      for (int i=0; i < marks.size(); i++) {
         Mark m = marks.elementAt(i);   
         if (m.useAntiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         } else  {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
         }
         
         g2d.setFont(m.font);
         g2d.setColor(m.color);
         g2d.drawString(m.str, m.xPos, m.yPos); // might be backwards
      }
   }
      
   
   // Simple border ... for debugging
   public void renderBorder(GL2 gl2) {
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      gl2.glColor4d(0.5, 0.5, 0.5, 1.0);
      gl2.glBegin(GL2.GL_LINE_LOOP);
         gl2.glVertex3f(anchorX, anchorY,0);
         gl2.glVertex3f(anchorX+width, anchorY,0);
         gl2.glVertex3f(anchorX+width, anchorY+height,0);
         gl2.glVertex3f(anchorX, anchorY+height,0);
      gl2.glEnd();
   }
   
   
   
   // Fetch an openGL texture and renders to the screen
   public void render(GL2 gl2) {
      render(gl2, true);   
   }
   
   public void render(GL2 gl2, boolean useBlend) {
      if (dirty == 1) {
         //System.out.println("Detected dirty texture");
         renderToTexture(Color.BLACK);
         dirty = 0;
      }         
      
      Texture t = texture.getTexture();
      t.enable(gl2);
      t.bind(gl2);
      TextureCoords tc = t.getImageTexCoords();
      
      if (useBlend) {
         gl2.glDisable(GL2.GL_DEPTH_TEST);
         gl2.glEnable(GL2.GL_BLEND);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);            
      } else {
         gl2.glEnable(GL2.GL_DEPTH_TEST);
         gl2.glEnable(GL2.GL_BLEND);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);            
      }
      
      gl2.glColor4d(1.0, 1.0, 1.0, opacity);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glTexCoord2f(0, 1);
         gl2.glVertex3f(anchorX, anchorY,0);
         
         gl2.glTexCoord2f(1, 1);
         gl2.glVertex3f(anchorX+width, anchorY,0);
         
         gl2.glTexCoord2f(1, 0);
         gl2.glVertex3f(anchorX+width, anchorY+height,0);
         
         gl2.glTexCoord2f(0, 0);
         gl2.glVertex3f(anchorX, anchorY+height,0);
      gl2.glEnd();
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      t.disable(gl2);         
      
      gl2.glEnable(GL2.GL_DEPTH_TEST);
      gl2.glEnable(GL2.GL_BLEND);
   }
   
   
   public TextureRenderer texture; 
   public Graphics2D g2d;
   public static FontMetrics fm;
   
   public float height;
   public float width;
   public float anchorX = 0;
   public float anchorY = 0;
   public double opacity = 1.0f;
   
   
   public Vector<Mark> marks = new Vector<Mark>();
   public int dirty = 0;
   
   
   public class Mark {
      public Mark(String s, Color c, Font f, float x, float y) {
         str = s;
         color = c;
         font = f;
         xPos = x;
         yPos = y;
      }
      public Mark(String s, Color c, Font f, float x, float y, boolean antiAlias) {
         str = s;
         color = c;
         font = f;
         xPos = x;
         yPos = y;
         useAntiAlias = antiAlias;   
      }
      public String str;
      public Color color;
      public Font font;
      public float xPos;
      public float yPos;
      public boolean useAntiAlias = true;
   }
}
