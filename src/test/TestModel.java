package test;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import util.GraphicUtil;
import util.TextureFont;

import model.DCColour;
import model.DCComponent;
import model.FrameBufferTexture;

import datastore.MM;

/////////////////////////////////////////////////////////////////////////////////
// This class is used for testing model renders, particularly for different
// shader effects
/////////////////////////////////////////////////////////////////////////////////
public class TestModel extends JOGLBase implements KeyListener {
   
   public float rotY = 0;
   public int mode = 0;
   public boolean useHalo = true;
   
   public static void main(String args[]) {
      TestModel tm = new TestModel();
      tm.run("Test Model", 600, 600);
   }
   
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      gl2.glClearColor(1, 1, 1, 1);
      super.basicClear( gl2 );
      
      float h = a.getHeight();
      float w = a.getWidth();
      
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      
      // Car
      fbt.TEXTURE_SIZE_W = a.getWidth();
      fbt.TEXTURE_SIZE_H = a.getHeight();
      
      //System.out.println(a.getWidth() + " " + a.getHeight());
      
      if (useHalo) {
         fbt.startRecording(gl2); 
         GraphicUtil.setPerspectiveView(gl2, w/h, 30.0f, 1.0f, 500.0f, new float[]{10.0f, 40.0f, 0.0f}, new float[]{0,0,0}, new float[]{0, 1, 0} );
         //gl2.glClearColor(0, 0, 0, 0);
         gl2.glClearColor(1, 1, 1, 0);
         gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl2.glPushMatrix();
            gl2.glRotated(rotY, 0, 1, 0);
            for (DCComponent d : MM.currentModel.componentTable.values()) {
               switch (mode) {
                  case 0: { d.renderBuffer(gl2, DCColour.fromDouble(1.0, 0, 0, 1)); break; }        
                  case 1: { d.renderBufferAdj(gl2, DCColour.fromDouble(1.0, 1.0, 0.0, 0.5)); break; }
                  case 2: { d.renderBufferToon(gl2); break; }
                  case 3: { d.renderFNormal(gl2); break; }
                  case 4: { d.renderEdgeWithNoAdjacent(gl2); break; }
                  default: break;
               }
            }
            rotY += 0.3f;
         gl2.glPopMatrix();
         fbt.stopRecording(gl2);
            
         
         // Render frame buffer texturej
         GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
         //gl2.glClearColor(1, 1, 1, 1);
         //this.basicClear(gl2);
         //fbt.shader.setUniform1i(gl2, "height", a.getHeight());
         //fbt.shader.setUniform1i(gl2, "width", a.getWidth());
         fbt.render(gl2, 4.0f, 0);
      } else {
         GraphicUtil.setPerspectiveView(gl2, w/h, 30.0f, 1.0f, 500.0f, new float[]{10.0f, 40.0f, 0.0f}, new float[]{0,0,0}, new float[]{0, 1, 0} );
         gl2.glPushMatrix();
            gl2.glRotated(rotY, 0, 1, 0);
            for (DCComponent d : MM.currentModel.componentTable.values()) {
               switch (mode) {
                  case 0: { d.renderBuffer(gl2, DCColour.fromDouble(1.0, 0, 0, 1)); break; }        
                  case 1: { d.renderBufferAdj(gl2, DCColour.fromDouble(1.0, 1.0, 0.0, 0.5)); break; }
                  case 2: { d.renderBufferToon(gl2); break; }
                  case 3: { d.renderFNormal(gl2); break; }
                  case 4: { d.renderEdgeWithNoAdjacent(gl2); break; }
                  default: break;
               }
            }
            rotY += 0.3f;
         gl2.glPopMatrix();
      }
      
      
      
      /*
      GraphicUtil.setPerspectiveView(gl2, w/h, 30.0f, 1.0f, 500.0f, new float[]{10.0f, 40.0f, 0.0f}, new float[]{0,0,0}, new float[]{0, 1, 0} );
      gl2.glPushMatrix();
         gl2.glRotated(rotY, 0, 1, 0);
         for (DCComponent d : MM.currentModel.componentTable.values()) {
            switch (mode) {
               case 0: { d.renderBuffer(gl2, DCColour.fromDouble(1.0, 0, 0, 1)); break; }        
               case 1: { d.renderBufferAdj(gl2, DCColour.fromDouble(1.0, 1.0, 0.0, 0.5)); break; }
               case 2: { d.renderBufferToon(gl2); break; }
               case 3: { d.renderFNormal(gl2); break; }
               case 4: { d.renderEdgeWithNoAdjacent(gl2); break; }
               default: break;
            }
         }
         rotY += 0.1f;
      gl2.glPopMatrix();
      */
     
      
      
      
      // Labels and text
      /*
      GraphicUtil.setOrthonormalView(gl2, 0, w, 0, h, -10, 10);
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
      tf.render(gl2);
      */
   }
   

   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);   
      GL2 gl2 = a.getGL().getGL2();
      
      
      canvas.addKeyListener(this);
      
      MM.instance();
      MM.instance().loadModels();
      MM.instance().initGPU(gl2);
      
      tf = new TextureFont();
      tf.width = 400;
      tf.height = 200;
      tf.anchorX = 200;
      tf.anchorY = 300;
      tf.clearMark();
      tf.addMark("Testing car rendering", Color.YELLOW, f, 5, 5);
      
      
    fbt.TEXTURE_SIZE_W = a.getWidth();
    fbt.TEXTURE_SIZE_H = a.getHeight();
    fbt.init(gl2);
    fbt.shader.createShader(gl2, "src\\Shader\\vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
    fbt.shader.createShader(gl2, "src\\Shader\\frag_fbt_white.glsl", GL2.GL_FRAGMENT_SHADER);
    fbt.shader.createProgram(gl2);
    
    gl2.glBindAttribLocation(fbt.shader.programID,  0, "in_position");
    gl2.glBindAttribLocation(fbt.shader.programID,  1, "in_colour");
    gl2.glBindAttribLocation(fbt.shader.programID,  2, "in_texcoord");      
    
    fbt.shader.linkProgram(gl2);
    fbt.shader.bindFragColour(gl2, "outColour");      
   }
   
   
   @Override
   public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
      super.reshape(glDrawable, x, y, width, height);
      tf.dirty = 1;
   }
   
   public TextureFont tf;
   public Font f  = new Font( "Arial", Font.PLAIN, 18);

   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         mode ++;
         if (mode > 4) mode = 0;
         tf.clearMark();
         tf.addMark("Testing car rendering mode : " + mode, Color.YELLOW, f, 5, 5);
      }
      if (e.getKeyChar() == 'h') {
         useHalo = ! useHalo;   
      }
   }


   @Override
   public void keyReleased(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
   }


   @Override
   public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
   } 
   
   public FrameBufferTexture fbt = new FrameBufferTexture();
   
}
