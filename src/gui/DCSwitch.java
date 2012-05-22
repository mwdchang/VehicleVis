package gui;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import model.DCColour;

import util.DCUtil;
import util.GraphicUtil;
import util.TextureFont;
import datastore.Const;


////////////////////////////////////////////////////////////////////////////////
// Implements a toggle button functionality
// Composite of labels and rounded rectangles
// 
// Schematics:
//   TextureFont{Label} : RoundedRectangle{ RoundedRectangle{TextureFont{ON}} | RoundedRectangle{TextureFont{OFF}} } 
//
//
////////////////////////////////////////////////////////////////////////////////
public class DCSwitch {
   
   public DCSwitch() {
      anchorX = anchorY = 0;
      state = false;
      tf.height = height;
      tf.width = width;
      tf.addMark("TEST", Color.GRAY, font, 1, 1);
      tf.renderToTexture(null);
      
      tf_on.height = height;
      tf_on.width  = switchWidth;
      tf_on.addMark("ON", Color.BLACK, font, 1, 1);
      
      tf_off.height = height;
      tf_off.width = switchWidth;
      tf_off.addMark("OFF", Color.BLACK, font, 1, 1);
   }
   
   public void setLabel(String s) {
      label = s;
      tf.clearMark();
      tf.addMark(s, Color.GRAY, font, 1, 5);
   }
   
   
   public void render(GL2 gl2) {
      
      // Draw the buttons and stuff
      GraphicUtil.drawRoundedRect(gl2, anchorX+width+(buttonWidth/2), anchorY, 0, (buttonWidth/2), 10, 4, 6,
            DCColour.fromDouble(0.68, 0.68, 0.68, 0.65).toArray(), 
            DCColour.fromDouble(0.77, 0.77, 0.77, 0.65).toArray());
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      
      // Render an "on" or "off" state
      if (this.state == true) {
         GraphicUtil.drawRoundedRect(gl2, anchorX+width+(switchWidth/2), anchorY, 0, (switchWidth/2), 10, 4, 6,
               DCColour.fromDouble(0.0, 0.4, 0.9, 0.65).toArray(), 
               DCColour.fromDouble(0.0, 0.6, 0.7, 0.65).toArray());
         
         tf_on.anchorX = anchorX+width;
         tf_on.anchorY = anchorY-8;
         tf_on.render(gl2);
      } else {
         GraphicUtil.drawRoundedRect(gl2, anchorX+20+width+(switchWidth/2), anchorY, 0, (switchWidth/2), 10, 4, 6,
               DCColour.fromDouble(0.7, 0.7, 0.7, 0.65).toArray(), 
               DCColour.fromDouble(0.6, 0.6, 0.6, 0.65).toArray());
         
         tf_off.anchorX = anchorX+width+20;
         tf_off.anchorY = anchorY-8;
         tf_off.render(gl2);
      }
      
     
      
      
      // Do not use blending for this, to prevent labels showing through multiple layers
      tf.anchorX = anchorX;
      tf.anchorY = anchorY-8;
      tf.render(gl2, true); 
   }
   
   
   public static Font font = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 14f);
   public TextureFont tf = new TextureFont();    
   
   // Texture for on and off button, these do not need to be refreshed
   public TextureFont tf_on  = new TextureFont();
   public TextureFont tf_off = new TextureFont(); 
   
   public String label = "";
   public boolean state;
   
   public float anchorX;
   public float anchorY;
   
   public float buttonWidth = 60;
   public float switchWidth = 40;
   
   public float height = 16;
   public float width  = 80;
}
