package test;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;

import model.DCColour;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.Animator.RepeatBehavior;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.timing.Animator.EndBehavior;
import org.jdesktop.animation.timing.triggers.FocusTriggerEvent;
import org.jdesktop.animation.timing.triggers.ActionTrigger;
import org.jdesktop.animation.timing.triggers.FocusTrigger;
import org.jdesktop.animation.timing.triggers.MouseTrigger;
import org.jdesktop.animation.timing.triggers.MouseTriggerEvent;
import org.jdesktop.animation.timing.triggers.TimingTrigger;
import org.jdesktop.animation.timing.triggers.TimingTriggerEvent;

import TimingFrameExt.DCColourEval;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class TimingFramework extends JOGLBase implements KeyListener {
   
   DCColour c1 = new DCColour(1.0, 0.0, 0.0, 0.0);
   DCColour c2 = new DCColour(0.0, 1.0, 0.0, 0.0);
   DCColour c3 = new DCColour(0.0, 0.0, 1.0, 0.0);
   
   DCColour myColour1 = new DCColour(c1);
   DCColour myColour2 = new DCColour(c2);
   DCColour myColour3 = new DCColour(c3);
   
   Animator animator[] = new Animator[3];
   
   public static void main(String args[]) {
      TimingFramework t = new TimingFramework();
      t.run();
   }
   
   
   @Override
   public void init(GLAutoDrawable g) {
      super.init(g);
      canvas.addKeyListener(this);
      
      //animator = PropertySetter.createAnimator(10000, awful, "test", c1, c2);  
      animator[0] = PropertySetter.createAnimator(1000, myColour1, "colour", new DCColourEval(), c1, c2);
      animator[1] = PropertySetter.createAnimator(1000, myColour2, "colour", new DCColourEval(), c2, c3);
      animator[2] = PropertySetter.createAnimator(1000, myColour3, "colour", new DCColourEval(), c3, c1);
   }
   
   @Override
   public void display(GLAutoDrawable g) {
      GL2 gl2 = g.getGL().getGL2();      
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
      gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
      
      gl2.glLoadIdentity();
      gl2.glPushMatrix();
         gl2.glTranslatef(0, 0, -50);
         //gl2.glRotatef(angle, 0, 0, 1);
         renderScene(gl2);
      gl2.glPopMatrix();
      angle += 0.05f;
   }
   
   public static float angle = 0.0f;
   
   public void renderScene(GL2 gl2) {
      gl2.glBegin(GL2.GL_TRIANGLES);   
         
         gl2.glColor4fv( myColour1.toArray(), 0);
         gl2.glVertex3d(0, 0, 0);
         
         gl2.glColor4fv( myColour2.toArray(), 0);
         gl2.glVertex3d(10, 0, 0);
         
         gl2.glColor4fv( myColour3.toArray(), 0);
         gl2.glVertex3d(10, 10, 0);
      gl2.glEnd();
   }


   @Override
   public void keyPressed(KeyEvent e) {
      if (e.getKeyChar() == 'b') {
         System.out.println("B pressed");
         
         if (animator[0].isRunning()) animator[0].stop();
         if (animator[1].isRunning()) animator[1].stop();
         if (animator[2].isRunning()) animator[2].stop();
         
         
         animator[0].setRepeatBehavior(RepeatBehavior.REVERSE);
         animator[0].setRepeatCount(Animator.INFINITE);
         animator[0].start();
         animator[1].setRepeatBehavior(RepeatBehavior.REVERSE);
         animator[1].setRepeatCount(Animator.INFINITE);
         animator[1].start();
         animator[2].setRepeatBehavior(RepeatBehavior.REVERSE);
         animator[2].setRepeatCount(Animator.INFINITE);
         animator[2].start();
      }
      
      if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
         System.out.println("Shutting down");
         System.exit(0);
      }
      
      
      // Just to test if the invoke later thingy will work or not
      if (e.getKeyChar() == KeyEvent.VK_SPACE){
         /*
         try {
         for (int i=0; i < 100; i ++) {
            Thread.sleep(200);
            System.out.println("Is this ever going to execute ?");
         }
         } catch (Exception ee) { ee.printStackTrace(); }
         */
         
        
         /*
         Runnable blarg = new Runnable() {
            public void run() {
               try {
                  for (int i=0; i < 100; i++) {
                     Thread.sleep(200);
                     System.out.println("Is this never going to execute ????????????");   
                  }
               } catch (Exception ee) {
                  ee.printStackTrace();
               }
            }
         };
         SwingUtilities.invokeLater( blarg );
         */
         
         /*
         try {
            SwingUtilities.invokeAndWait( blarg );
         } catch(Exception ee) { ee.printStackTrace(); }
         */
         
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
