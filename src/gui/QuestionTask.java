package gui;

import java.awt.Color;

import javax.media.opengl.GL2;

import model.DCColour;

import datastore.SSM;

import util.GraphicUtil;
import util.TextureFont;

import exec.RenderTask;

public class QuestionTask implements RenderTask {

   @Override
   public void render(GL2 gl2) {
      
      if (q[0].answered()) {
         GraphicUtil.drawRoundedRect(gl2, (SSM.windowWidth - 150), (SSM.windowHeight - 100 + 20), 0,
               50, 25, 5, 5, 
               DCColour.fromDouble(0.0, 0.0, 0.8, 0.8).toArray(), 
               DCColour.fromDouble(0.0, 0.0, 0.6, 0.8).toArray());
      } else {
         GraphicUtil.drawRoundedRect(gl2, (SSM.windowWidth - 150), (SSM.windowHeight - 100 + 20), 0,
               50, 25, 5, 5, 
               DCColour.fromDouble(0.8, 0.8, 0.8, 0.8).toArray(), 
               DCColour.fromDouble(0.6, 0.6, 0.6, 0.8).toArray());
      }
      
      
      tf.anchorX = SSM.windowWidth  - 200;
      tf.anchorY = SSM.windowHeight - 100;
      tf.render(gl2);
      //tf.renderBorder(gl2);
      
      q_tf.anchorX = SSM.windowWidth - 800;
      q_tf.anchorY = SSM.windowHeight - 100;
      q_tf.render(gl2);
      
      //gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
      //gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      
   }

   @Override
   public void init(GL2 gl2) {
      tf = new TextureFont();
      tf.width = 100;
      tf.height = 40;
      tf.addMark("Proceed", Color.BLACK, GraphicUtil.font, 2, 2);
      tf.renderToTexture(null);
      
      q_tf = new TextureFont();
      q_tf.width  = 600;
      q_tf.height = 40;
      q_tf.addMark(q[0].txt, Color.BLACK, GraphicUtil.font, 2, 2);
      q_tf.renderToTexture(null);
      
   }

   
   @Override
   public void picking(GL2 gl2, float px, float py) {
      // TODO Auto-generated method stub
   }
   
   
   public TextureFont tf   = null; // Button Texture
   public TextureFont q_tf = null; // Question Texture 
   
   
   public abstract class Question {
      public abstract boolean answered();   
      public String txt;
   }
   
   public Question[] q = new Question[2];
   
   
   public QuestionTask() {
      q[0] = new Question() {
         public boolean answered() {
            return SSM.startYear == 1995 && SSM.endYear == 1996;
         }
      };
      q[0].txt = "Select between year 1995 and 1996";
      
      
   }

}
