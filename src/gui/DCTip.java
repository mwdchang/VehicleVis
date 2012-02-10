package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import javax.media.opengl.GL2;

import util.DCUtil;
import util.TextureFont;

/////////////////////////////////////////////////////////////////////////////////
// Implements a tool tip functionality
// For 2D use only !!!
/////////////////////////////////////////////////////////////////////////////////
public class DCTip {
  
   /*
   public DCTip() {
   }
   */
   
   public static void init(GL2 gl2) {
      tf = new TextureFont();
      tf.height = 80;
      tf.width  = 150;
   }
   
   
   public static void render(GL2 gl2) {
      if (! visible) return; 
      
      //tf.renderBorder(gl2);
      
      
      // Render the box
      gl2.glColor4d(0.6, 0.6, 0.6, 0.6);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2d(tf.anchorX, tf.anchorY);
         gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY);
         gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY+tf.height);
         gl2.glVertex2d(tf.anchorX, tf.anchorY+tf.height);
      gl2.glEnd();
      
      // Renders the 'tip'
      if (tipX < tf.anchorX) {
         gl2.glBegin(GL2.GL_TRIANGLES);
            gl2.glVertex2d(tipX, tipY);
            gl2.glVertex2d(tf.anchorX, tf.anchorY);
            gl2.glVertex2d(tf.anchorX, tf.anchorY+tf.height);
         gl2.glEnd();
      } else {
         gl2.glBegin(GL2.GL_TRIANGLES);
            gl2.glVertex2d(tipX, tipY);
            gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY);
            gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY+tf.height);
         gl2.glEnd();
      }
      
      gl2.glDisable(GL2.GL_BLEND);
      tf.render(gl2);
      gl2.glEnable(GL2.GL_BLEND);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // 
   ////////////////////////////////////////////////////////////////////////////////
   public static void setTip(float px, float py, float screenWidth, float screenHeight) {
      float centerX = screenWidth/2;
      float centerY = screenHeight/2;
      //tf.clearMark();
      
      tipX = px;
      tipY = py;
      
      ////////////////////////////////////////////////////////////////////////////////
      // Check which quadrant the point is in : upper left, upper right, lower left, or lower right
      // Basically we do not want to obscure what ever we are pointing at
      ////////////////////////////////////////////////////////////////////////////////
      if (px <= centerX && py >= centerY) {
         // top left
         tf.anchorX = px + TIP_LENGTH;
         tf.anchorY = py - TIP_LENGTH - tf.height;
      } else if (px > centerX && py > centerY) {
         // top right
         tf.anchorX = px - TIP_LENGTH - tf.width;
         tf.anchorY = py - TIP_LENGTH - tf.height;
      } else if (px < centerX && py < centerY) {
         // bottom left
         tf.anchorX = px + TIP_LENGTH;
         tf.anchorY = py + TIP_LENGTH; 
      } else {
         // bottom right 
         tf.anchorX = px - TIP_LENGTH - tf.width;
         tf.anchorY = py + TIP_LENGTH;
      }
      
      //tf.addMark(txt, c, f, 5, tf.height-25); 
   }
   
   public static void clear() {
      tf.clearMark();
   }
   public static void addText(String txt) {
      tf.addMark( txt, c, f, 5, tf.height-20-tf.marks.size()*buffer);
   }
   
   
   public static Hashtable<String, String> translateTable = DCUtil.getMonthTranslationTable();
   
   public static TextureFont tf;
   public static Font f  = new Font( "Arial", Font.PLAIN, 16);    
   public static Color c = Color.BLACK;
   public static float tipX;
   public static float tipY;
   public static float TIP_LENGTH = 30;
   public static boolean visible = false;
   public static float buffer = 20;
}
