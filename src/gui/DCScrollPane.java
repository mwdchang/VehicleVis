package gui;

import gui.TextPane.Tag;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Vector;

import javax.media.opengl.GL2;

import model.DCColour;

import org.jdesktop.animation.timing.Animator;

import util.DWin;
import util.FontRenderer;
import util.GraphicUtil;

import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;




/////////////////////////////////////////////////////////////////////////////////
// Combo Box implementation
// Basically this is a selectable scroll panel, with an additional pane that 
// triggers the scroll pane and show the selected item.
// 
// Note we assume that the scroll list item is a unique list
// Note this is hardcoded to roll up
/////////////////////////////////////////////////////////////////////////////////
public class DCScrollPane {
   
   public float anchorX = 150.0f;
   public float anchorY = 150.0f;   
   
   public float width = 230.0f;
   //public float height = 200.0f;
   public float height = 0.0f;
   public float yoffset = height;
   
   
   public static float buttonHeight = 10.0f;
   public static float spacing = 18.0f;
   public static short UP   =   1;
   public static short DOWN =   2;
   
   
   public float texPanelWidth = 230.0f;
   public float texPanelHeight = 8000.0f;
   
   public boolean visible = true;
   
   public TextureRenderer texture; 
   public Graphics2D g2d;
   
   public static Font fontArial = new Font( "Arial", Font.PLAIN, 12);   
   public static Font fontArialBold = new Font( "Arial", Font.BOLD, 12);
   
   public short direction;
   
   public FontMetrics fm;   
   public Vector<GTag> tagList = new Vector<GTag>();
   
   // The currently selected item in the taglist
   public int current = 0;
   public String currentStr = ""; 
   
   public String label = "";
   
   public boolean dirty = false;
   
   public Animator animator;
   
   public void setHeight(float h) { height = h; }
   public float getHeight() { return height; }
   
   public DCScrollPane(String s) {
      this();
      label = s;
      direction = UP;
   }
   public DCScrollPane() {
      texture = new TextureRenderer((int)texPanelWidth, (int)texPanelHeight, true, true);
      g2d = texture.createGraphics();
      g2d.setFont(fontArial);      
      
      // Get a metric
      fm = g2d.getFontMetrics();
      direction = UP;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Calculate the height of the panel, estimate the height space for each
   // text item, assuming that text item will only span a single line
   ////////////////////////////////////////////////////////////////////////////////
   public void calculate() {
      tagList.clear();
      // TODO: Calculate real height and with real data
      for (int i=0; i < 10; i++) {
         tagList.add( new GTag(10, (i+1)*spacing, 0+i*spacing, "Test " + i, "Test " + i));    
      }
   }
   
   /*
   public void calculate(String fromDate, String toDate, Integer fromMonth, Integer toMonth, Integer group) {
      int startIdx = CacheManager.instance().getDateKey(fromDate);
      int endIdx   = CacheManager.instance().getDateKey(toDate);
      
      CacheManager.instance().queryTable;
      
      System.out.println("Scroll get period is from " + startIdx + " to " + endIdx );
   }
   */
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the text content with graphics2D into a opengl compatible texture
   ////////////////////////////////////////////////////////////////////////////////
   public void renderToTexture(Color c) {
      
      if (texPanelHeight > 8192) {
         DWin.instance().error("Cannot process texture bigger than 8192");   
      }
      
      // Recreate the textureRenderer to make sure we have the write context  
      texture = null;
      texture = new TextureRenderer((int)texPanelWidth, (int)texPanelHeight, true, true);
      g2d = texture.createGraphics();
      
      // Setup anti-aliasing fonts
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      
      // Render the tags
      for (int i=0; i < tagList.size(); i++) {
         GTag t = tagList.elementAt(i);
         
         if (i % 2 == 0) 
            //g2d.setColor(Color.GRAY);
            g2d.setColor(DCColour.fromInt(200, 200, 200).convertToAWT());
         else 
            g2d.setColor(DCColour.fromInt(230, 230, 230).convertToAWT());
            //g2d.setColor(Color.LIGHT_GRAY);
         g2d.fillRect((int)0, (int)t.yPrime, (int)width, (int)spacing);
         
         //g2d.setColor(c);
         g2d.setColor(Color.BLACK);
         
//         if (i == current)  
         if (currentStr.equals(t.val))
            g2d.setFont(fontArialBold);
         else 
            g2d.setFont(fontArial);
         
         g2d.drawString(t.txt, t.x, t.y-3); //-3 is just a padding to make it like nicer (more or less centered)
         
      }
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders the texture, take into account the current offset (scroll amount)
   ////////////////////////////////////////////////////////////////////////////////
   public void render(GL2 gl2) {
      // If not visible, don't render....duh ?
      if (! this.visible) return;
      
      if (dirty) {
         this.renderToTexture(Color.BLACK);
         this.dirty = false;
      }
      
      Texture t = texture.getTexture();
      t.enable(gl2);
      t.bind(gl2);
      TextureCoords tc = t.getImageTexCoords();
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);            
      
      
      gl2.glColor4d(1.0, 1.0, 1.0, 0.5);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      
      if (direction == UP) {
	      gl2.glBegin(GL2.GL_QUADS);
	         //gl2.glTexCoord2f(0, 1);
	         gl2.glTexCoord2f(0, yoffset/texPanelHeight);
	         gl2.glVertex3f(anchorX, anchorY,0);
	         
	         //gl2.glTexCoord2f(1, 1);
	         gl2.glTexCoord2f(1, yoffset/texPanelHeight);
	         gl2.glVertex3f(anchorX+width, anchorY,0);
	         
	         //gl2.glTexCoord2f(1, 0);
	         gl2.glTexCoord2f(1, (yoffset-height)/texPanelHeight);
	         gl2.glVertex3f(anchorX+width, anchorY+height,0);
	         
	         //gl2.glTexCoord2f(0, 0);
	         gl2.glTexCoord2f(0, (yoffset-height)/texPanelHeight);
	         gl2.glVertex3f(anchorX, anchorY+height,0);
	      gl2.glEnd();
      } else {
 	      gl2.glBegin(GL2.GL_QUADS);
	         //gl2.glTexCoord2f(0, 1);
	         gl2.glTexCoord2f(0, yoffset/texPanelHeight);
	         gl2.glVertex3f(anchorX, anchorY-height-20,0);
	         
	         //gl2.glTexCoord2f(1, 1);
	         gl2.glTexCoord2f(1, yoffset/texPanelHeight);
	         gl2.glVertex3f(anchorX+width, anchorY-height-20,0);
	         
	         //gl2.glTexCoord2f(1, 0);
	         gl2.glTexCoord2f(1, (yoffset-height)/texPanelHeight);
	         gl2.glVertex3f(anchorX+width, anchorY-20,0);
	         
	         //gl2.glTexCoord2f(0, 0);
	         gl2.glTexCoord2f(0, (yoffset-height)/texPanelHeight);
	         gl2.glVertex3f(anchorX, anchorY-20,0);
	      gl2.glEnd();     	
      }
      t.disable(gl2);      
      
      
      // Draw the buttons and stuff
      /*
      gl2.glColor4d(1.0, 1.0, 0.0, 0.6);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex3d(anchorX, anchorY, 0);
         gl2.glVertex3d(anchorX+width, anchorY, 0);
         gl2.glVertex3d(anchorX+width, anchorY-20, 0);
         gl2.glVertex3d(anchorX, anchorY-20, 0);
      gl2.glEnd();
      */
      GraphicUtil.drawRoundedRect(gl2, anchorX+(width/2), anchorY-10, 0, (width/2), 10, 5, 6,
            DCColour.fromDouble(0.68, 0.68, 0.68, 0.65).toArray(), 
            DCColour.fromDouble(0.77, 0.77, 0.77, 0.65).toArray());
      
      
      if (current >= 0 && tagList.size() > 0)  {
         //FontRenderer.instance().renderOnce(gl2, anchorX+10, anchorY-15, 0, tagList.elementAt(current).s);
         FontRenderer.instance().renderOnce(gl2, anchorX+10, anchorY-15, 0, label+":"+currentStr);
      }
      
      // Draw a border
//      gl2.glColor4d(0.0, 0.0, 0.0, 0.5);
//      gl2.glBegin(GL2.GL_LINE_LOOP);
//         gl2.glVertex3d(anchorX, anchorY, 0);
//         gl2.glVertex3d(anchorX+width, anchorY, 0);
//         gl2.glVertex3d(anchorX+width, anchorY+height, 0);
//         gl2.glVertex3d(anchorX, anchorY+height, 0);
//      gl2.glEnd();
      
   }
   
   
   // Inner class to keep track of text items


}
