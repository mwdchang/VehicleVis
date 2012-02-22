package fun;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import test.JOGLBase;

/////////////////////////////////////////////////////////////////////////////////
// Rotate a polygon in 2D space
/////////////////////////////////////////////////////////////////////////////////
public class SqRot extends JOGLBase implements KeyListener {
   
   public static void main(String[] args) {
      SqRot sqrot = new SqRot();
      sqrot.run("SQ Rot", 800, 800, 30);
   }

   public double D2R( double degree ) {
      return degree * Math.PI / 180.0;   
   }
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      
      
      float aspect = (float)a.getWidth()/ (float)a.getHeight();      
      gl2.glMatrixMode(GL2.GL_PROJECTION); 
      gl2.glLoadIdentity();
      glu.gluPerspective(30.0f, aspect, 1.0, 600.0);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();      
      
      this.basicClear(gl2);
      
      
      
      gl2.glTranslatef(0, 0, -290f);
      
      //gl2.glRotatef(current, 1, 1, 1);
      
         
      for (int idx=0; idx < current; idx++) {
         gl2.glBegin(GL2.GL_LINES);
         //gl2.glBegin(GL2.GL_LINE_LOOP);
         
         double tmp = (idx%100);
         if (tmp > 50) tmp = 100-tmp;
         
         for (int i=0; i < numPoint; i++) {
            //gl2.glColor4d((double)idx/(double)current, 1-((double)idx/(double)current), 1, 0.5);
            
            double c_radius1 = radius[i] + idx*d_radius[i];
            double c_radius2 = radius[(i+1)%numPoint] + idx*d_radius[(i+1)%numPoint];
            
            double c_angle1 = angle[i] + idx*d_angle[i];
            double c_angle2 = angle[(i+1)%numPoint] + idx*d_angle[(i+1)%numPoint];
            
            gl2.glColor4d(
                  tmp/50.0,
                  1.0-tmp/50.0,
                  c_radius1/c_radius2,
                  0.5);
            
            
            gl2.glVertex3d( Math.cos(D2R(c_angle1))*c_radius1, Math.sin(D2R(c_angle1))*c_radius1, 0.0);
            gl2.glVertex3d( Math.cos(D2R(c_angle2))*c_radius2, Math.sin(D2R(c_angle2))*c_radius2, 0.0);
            
            /*
            if (c_radius1 > 150) d_radius[i] = d_radius[i]*-1;
            if (c_radius1 < 1)   d_radius[i] = d_radius[i]*-1;
            if (c_radius2 > 150) d_radius[(i+1)%numPoint] = d_radius[(i+1)%numPoint]*-1;
            if (c_radius2 < 1)   d_radius[(i+1)%numPoint] = d_radius[(i+1)%numPoint]*-1;
            */
         }
         gl2.glEnd();
      }
   
      current++;
      if (current >= iteration) {
         current = 0;   
         resetStage();
      }
      
   }
  
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);   
      this.canvas.addKeyListener(this);
      this.resetStage();
      
      GL2 gl2 = a.getGL().getGL2();
      
      gl2.glEnable(GL2.GL_LINE_SMOOTH);
      gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      
      
      gl2.glLineWidth(2.0f);
   }
   
   
   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         this.resetStage();
      }
      if (e.getKeyChar() == 'm') {
         System.out.println("Flipping maximized state");   
         this.maximizeFrame();
      }
      if (e.getKeyChar() == '2') {
         Toolkit tk = Toolkit.getDefaultToolkit();
         Dimension sc = tk.getScreenSize();
         frame.setLocation((int)sc.getWidth()+1, 0);
      }
      if (e.getKeyChar() == '1') {
         frame.setLocation(0, 0);   
      }      
      if (e.getKeyChar() == 'a') {
         this.onlyPositiveAngle = ! this.onlyPositiveAngle;
         this.resetStage();
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset the vertices and rotation parameters
   ////////////////////////////////////////////////////////////////////////////////
   public void resetStage() {
      numPoint = 2+(int)(Math.random()*8.0); // ensure at least 2 points
      radius = new double[numPoint];
      angle = new double[numPoint];
      d_radius = new double[numPoint]; // Change in radius
      d_angle  = new double[numPoint]; // Change in angle
      
      for (int i=0; i < numPoint; i++ ) {
         radius[i] = 10.0+Math.random()*20;   
         angle[i] = Math.random()*360;   
         
         d_radius[i] = Math.random()*0.5 - 0.5;
         if (this.onlyPositiveAngle) {
            d_angle[i] = Math.random()*2.5;
         } else {
            d_angle[i] = Math.random()*6 - 3.0;
         }
         if (d_angle[i] < 0) d_angle[i] -= 0.3;
         else d_angle[i] += 0.3;
      }
      
      current = 0;
   }
   
   
   public int numPoint = 8;    // Number of points
   public int iteration = 350;   // Number of iterations for the loop
   
   public double[] radius = new double[numPoint];
   public double[] angle = new double[numPoint];
   public double[] d_radius = new double[numPoint]; // Change in radius
   public double[] d_angle  = new double[numPoint]; // Change in angle
   
   public int current=0;
   
   public boolean onlyPositiveAngle = false;

}
