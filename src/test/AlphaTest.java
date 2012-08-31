package test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class AlphaTest extends JOGLBase implements KeyListener {
   
   boolean useTransparency = true;
   boolean rightOrder = true;
   double alpha = 1.0f;

   public static void main(String args[]) {
      AlphaTest test = new AlphaTest();
      test.run("Test Alpha", 500, 500);
   }
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      gl2.glClearColor(1, 1, 1, 1);
      basicClear(gl2);      
      
      if (useTransparency) {
         gl2.glEnable(GL2.GL_BLEND);   
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         alpha = 0.5;
      } else {
         gl2.glDisable(GL2.GL_BLEND);   
         alpha = 1.0;
      }
      
      gl2.glLoadIdentity();
      //gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      gl2.glPushMatrix();
         if (rightOrder) {
            gl2.glColor4d(1, 0, 0, alpha);
            drawQuad(gl2, 0, 0, -20, 2);
            gl2.glColor4d(0, 1, 0, alpha);
            drawQuad(gl2, 1, 1, -18, 2);
            gl2.glColor4d(0, 0, 1, alpha);
            drawQuad(gl2, 2, 2, -16, 2);
         } else {
            gl2.glColor4d(1, 0, 0, alpha);
            drawQuad(gl2, 0, 0, -20, 2);
            gl2.glColor4d(0, 0, 1, alpha);
            drawQuad(gl2, 2, 2, -16, 2);
            gl2.glColor4d(0, 1, 0, alpha);
            drawQuad(gl2, 1, 1, -18, 2);
         }
      gl2.glPopMatrix();
      
   }
   
   public void drawQuad(GL2 gl2, double x, double y, double z, double size) {
      gl2.glBegin(GL2.GL_QUADS);    
         gl2.glVertex3d(x-size, y-size, z);
         gl2.glVertex3d(x+size, y-size, z);
         gl2.glVertex3d(x+size, y+size, z);
         gl2.glVertex3d(x-size, y+size, z);
      gl2.glEnd();
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);
      canvas.addKeyListener(this);
      
   }

   @Override
   public void keyPressed(KeyEvent e) {
      registerStandardExit(e);
      if (e.getKeyChar() == 'a') {
         useTransparency = ! useTransparency;
      }
      if (e.getKeyChar() == 'o') {
         rightOrder = ! rightOrder;
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
