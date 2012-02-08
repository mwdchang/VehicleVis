package gui;

import javax.media.opengl.GL2;

import model.DCColour;

import util.GraphicUtil;

import com.jogamp.opengl.util.texture.Texture;

import datastore.SSM;
import org.jdesktop.animation.timing.Animator;

/////////////////////////////////////////////////////////////////////////////////
// DIY text panel - version 2
// For better or worse, now everything is completely from scratch
/////////////////////////////////////////////////////////////////////////////////
public class DCTextPanel2 {
   
   public DCTextPanel2() {
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Need a special init method to 
   // where an openGL context is available
   ////////////////////////////////////////////////////////////////////////////////
   public void init(GL2 gl2) {
      t1 = new TextPane();   
      t2 = new TextPane();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the panel(s)
   ////////////////////////////////////////////////////////////////////////////////
   public void render(GL2 gl2) {
      float anchorX  = SSM.instance().docAnchorX;
      float anchorY  = SSM.instance().docAnchorY;
      
     // float displayW = SSM.instance().docWidth; 
     // float displayH = SSM.instance().docHeight;
      float yoffset  = SSM.instance().yoffset;
      double padding = SSM.instance().docPadding;
      

      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);            
      
      //gl2.glEnable(GL2.GL_BLEND);
      //gl2.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
      GraphicUtil.drawRoundedRect(gl2, 
            (double)(anchorX+(displayW/2)), (double)(anchorY+(displayH/2)), (double)0.0,   // xyz
            (double)(displayW/2.0)+padding, (double)(displayH/2.0)+padding,                // width and height
            40.0, 7,                                                                       // corner and segments 
            DCColour.fromDouble(0.85, 0.85, 0.85, 0.8).toArray(), 
            DCColour.fromDouble(0.80, 0.80, 0.80, 0.8).toArray());
      /*
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex3d(anchorX,          anchorY, 0);
         gl2.glVertex3d(anchorX+displayW, anchorY, 0);
         gl2.glVertex3d(anchorX+displayW, anchorY+displayH, 0);
         gl2.glVertex3d(anchorX,          anchorY+displayH, 0);
      gl2.glEnd();
      */
      
      
//      gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glColor4d(1.0, 1.0, 1.0, 1.0);
      
      
      float t1Amt = displayH + t1.textPaneHeight - yoffset;
      float t2Amt = yoffset - t1.textPaneHeight;
      
      // Sanity
      t1Amt = Math.max(t1Amt, 0);
      t2Amt = Math.max(t2Amt, 0);
      
      if (t1Amt > 0) {
         //gl2.glColor4f(1, 0.5f, 0, 1);
         //gl2.glColor4d(0.2, 0.4, 0.8, 0.8);
         //gl2.glBegin(GL2.GL_QUADS);
         //   gl2.glVertex3f(anchorX, anchorY+t2Amt, 0);
         //   gl2.glVertex3f(anchorX+displayW, anchorY+t2Amt, 0);
         //   gl2.glVertex3f(anchorX+displayW, anchorY+displayH, 0);
         //   gl2.glVertex3f(anchorX, anchorY+displayH, 0);
         //gl2.glEnd();    
         
         Texture tex = t1.texture.getTexture();
         tex.enable(gl2);
         tex.bind(gl2);         
         
         float ystart = Math.min(yoffset, t1.textPaneHeight);
         
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, ystart/t1.textPaneHeight);
            gl2.glVertex3f(anchorX, anchorY+t2Amt, 0);
            
            gl2.glTexCoord2f(1, ystart/t1.textPaneHeight);
            gl2.glVertex3f(anchorX+displayW, anchorY+t2Amt, 0);
            
            gl2.glTexCoord2f(1, (ystart-Math.min(t1Amt, displayH))/t1.textPaneHeight);
            gl2.glVertex3f(anchorX+displayW, anchorY+displayH, 0);
            
            gl2.glTexCoord2f(0, (ystart-Math.min(t1Amt, displayH))/t1.textPaneHeight);
            gl2.glVertex3f(anchorX, anchorY+displayH, 0);
         gl2.glEnd();
         tex.disable(gl2);
      }
      if (t2Amt > 0 ) {
         //gl2.glColor4f(0.5f, 0.5f, 0.5f, 1);
         //gl2.glColor4d(1.0, 1.0, 1.0, 1.0);
         
         Texture tex = t2.texture.getTexture();
         tex.enable(gl2);
         tex.bind(gl2);         
         
         float ystart = t2Amt;
         
         
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, ystart/t2.textPaneHeight);
            gl2.glVertex3f(anchorX, anchorY, 0);
            
            gl2.glTexCoord2f(1, ystart/t2.textPaneHeight);
            gl2.glVertex3f(anchorX+displayW, anchorY, 0);
            
            gl2.glTexCoord2f(1, (ystart-Math.min(t2Amt, displayH))/t2.textPaneHeight);
            gl2.glVertex3f(anchorX+displayW, anchorY+displayH-t1Amt, 0);
            
            gl2.glTexCoord2f(0, (ystart-Math.min(t2Amt, displayH))/t2.textPaneHeight);
            gl2.glVertex3f(anchorX, anchorY+displayH-t1Amt, 0);
         gl2.glEnd();
         tex.disable(gl2);
      }      
//      gl2.glEnable(GL2.GL_DEPTH_TEST);
      
   }
   
   
   public float getDisplayH() { return displayH; }
   public void  setDisplayH(float h) { displayH = h; }
   public float getDisplayW() { return displayW; }
   public void  setDisplayW(float w) { displayW = w; }
   
   public float displayH = 0;
   public float displayW = 0;
   
   public TextPane t1;
   public TextPane t2;
   
   public Animator animatorH;
   public Animator animatorW;
}
