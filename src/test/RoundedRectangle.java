package test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import test.JOGLBase;
import util.GraphicUtil;

// Test a rounded rectangle
public class RoundedRectangle extends JOGLBase implements KeyListener {
   
   public static float offset = 5.0f;
   public static int seg = 5;

   public static void main(String args[]) {
      RoundedRectangle r = new RoundedRectangle();
      r.run("Test Rounded Rectangle", 500, 500);
   }
   
   
   
   @Override
   public void display(GLAutoDrawable d) {
      GL2 gl2 = d.getGL().getGL2();
      basicClear(gl2); 
      
      gl2.glTranslated(0, 0, -100);
      //gl2.glEnable(GL2.GL_BLEND);
      //gl2.glDisable(GL2.GL_DEPTH_BUFFER);
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
      GraphicUtil.drawRoundedRect(gl2, 0, 0, 0, 18, 10, offset, seg);
      //GraphicUtil.drawPie(gl2, 0, 0, 0, 2, 120, 0, 15);
      
   }
   
   
   @Override
   public void init(GLAutoDrawable d) {
      super.init(d);
      
      GL2 gl2 = d.getGL().getGL2();
      
      this.canvas.addKeyListener(this);
      
      gl2.glShadeModel(GL2.GL_SMOOTH);
      
      
   }


   @Override
   public void keyPressed(KeyEvent e) {
      registerStandardExit(e);
      if (e.getKeyChar() == 'j') {
         offset -= 0.1f;   
      }
      if (e.getKeyChar() == 'k') {
         offset += 0.1f;   
      }
      
      if (e.getKeyChar() == 'h') {
         seg--;
      }
      if (e.getKeyChar() == 'l') {
         seg++;
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

}
