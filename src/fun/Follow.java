package fun;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.GLBuffers;

import test.JOGLBase;
import util.GraphicUtil;

/////////////////////////////////////////////////////////////////////////////////
// Flowing line segments
// Ported from C++ code, which was ported from some Pascal program, this version
// should provide better user interaction in terms of accuracy and response
/////////////////////////////////////////////////////////////////////////////////
public class Follow extends JOGLBase implements KeyListener, MouseMotionListener, MouseListener {
   
   public static void main(String args[]) {
      Follow l = new Follow();
      l.run("Follow Me", 800, 800, 30);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check if we hit an imaginary box, if we do adjust the starting coordinates
   ////////////////////////////////////////////////////////////////////////////////
   public void doPicking(GL2 gl2) {
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);      
      GraphicUtil.startPickingPerspective(gl2, buffer, mouseX, mouseY, screenWidth, screenHeight, 35);
      gl2.glTranslated(0, 0, -8);
      
      gl2.glLoadName(999);
      gl2.glPushMatrix(); 
         GraphicUtil.drawQuadFan(gl2, lines[0][0].x, lines[0][0].y, lines[0][0].z, 0.5, 0.5);
      gl2.glPopMatrix();
      
      Integer hit = GraphicUtil.finishPicking(gl2, buffer);
      
      if (hit != null) {
         System.out.println("Hit detected");   
         // now we have a hit, unproject back into object space to get the new coord
         double[] newCoord = GraphicUtil.getUnProject(gl2, glu, mouseX, mouseY);
         
         for (int i=0; i < this.NUM_LINES; i++) {
            //startX = newCoord[0];   
            //startY = newCoord[1];   
            lines[i][0].x = newCoord[0];
            lines[i][0].y = newCoord[1];
         }
      } 
      this.doPicking = false;
   }
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      
      this.screenWidth = a.getWidth();
      this.screenHeight = a.getHeight();
      
      if (this.doPicking) {
         doPicking(gl2);
      }
      
      
      
      float aspect = (float)a.getWidth() / (float)a.getHeight();
      gl2.glMatrixMode(GL2.GL_PROJECTION); 
      gl2.glLoadIdentity();
      glu.gluPerspective(35.0f, aspect, 1.0, far);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();      
      
      
      gl2.glTranslated(0, 0, -far);
      
      gl2.glRotatef(yrot, 0, 1, 0);
      
      /*
      GraphicUtil.drawAxis(gl2, 0, 0, 0);
      GraphicUtil.drawAxis(gl2, 1, 0, 0);
      */
      

      
      ////////////////////////////////////////////////////////////////////////////////
      // Do some random magic, build the line backwards
      ////////////////////////////////////////////////////////////////////////////////
      for (int i=0; i < this.NUM_LINES; i++) {
         for (int j=(this.NUM_SEGMENTS-1); j > 0; j--) {
            lines[i][j].x = (lines[i][j-1].x*1.88 + lines[i][j].x)/2.8 + (Math.random()*200-100)*0.0002;   
            lines[i][j].y = (lines[i][j-1].y*1.88 + lines[i][j].y)/2.8 + (Math.random()*200-100)*0.0002;
            lines[i][j].z = (lines[i][j-1].z*1.88 + lines[i][j].z)/2.8 + (Math.random()*200-100)*0.0002;   
            
         }
      }
      
      ////////////////////////////////////////////////////////////////////////////////
      // Finally, we build a render
      ////////////////////////////////////////////////////////////////////////////////
      for (int i=0; i < this.NUM_LINES; i++) {
         /*
         gl2.glColor4d(
               (double)i/(double)this.NUM_LINES, 
               1.0-(double)i/(double)this.NUM_LINES, 
               0.5,
               0.4);
         */
         gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
         //gl2.glLineWidth(4.0f);
         //gl2.glBegin(GL2.GL_LINE_STRIP);
         for (int j=0; j < this.NUM_SEGMENTS; j++) {
            gl2.glColor4d( (double)i/(double)this.NUM_LINES, 1.0-(double)j/(double)this.NUM_SEGMENTS, 1.0-(double)j%20/(double)20,  0.6*(double)j/(double)this.NUM_LINES);
            //gl2.glColor4d( 0.5, 0.0, 0.5, 0.5);
            gl2.glVertex3d( lines[i][j].x, lines[i][j].y, lines[i][j].z );   
         }
         gl2.glEnd();
      }
      
      // Increment the rotation angles
      //yrot += 1.5f;
      
   }
   
   
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);
      this.canvas.addKeyListener(this);
      this.canvas.addMouseMotionListener(this);
      this.canvas.addMouseListener(this);
      
      GL2 gl2 = a.getGL().getGL2();
     
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      gl2.glEnable(GL2.GL_BLEND);
      //gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
      
      
      gl2.glEnable(GL2.GL_LINE_SMOOTH);
      gl2.glShadeModel(GL2.GL_SMOOTH);
      
      gl2.glClearDepth(1.0);
      
      
      // Make sure all the segments are initialized
      for (int i=0; i < this.NUM_LINES; i++) {
         for (int j=0; j < this.NUM_SEGMENTS; j++) {
            lines[i][j] = new Line();
         }
      }
      
      ////////////////////////////////////////////////////////////////////////////////
      // Reset the starting point
      ////////////////////////////////////////////////////////////////////////////////
      for (int i=0; i < this.NUM_LINES; i++) {
         lines[i][0].x = startX;
         lines[i][0].y = startY;
         lines[i][0].z = 0;
      }      
   }

   @Override
   public void mouseDragged(MouseEvent e) {
      doPicking = true;
      mouseX = e.getX();
      mouseY = e.getY();
   }

   @Override
   public void mouseMoved(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
   }

   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         for (int i=0; i < this.NUM_LINES; i++) {
            lines[i][0].x = 0;
            lines[i][0].y = 0;
            lines[i][0].z = 0;
         }  
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
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }
   
   @Override
   public void mouseClicked(MouseEvent e) {
   }


   @Override
   public void mouseEntered(MouseEvent e) {
   }


   @Override
   public void mouseExited(MouseEvent e) {
   }


   @Override
   public void mousePressed(MouseEvent e) {
      //doPicking = true;
   }


   @Override
   public void mouseReleased(MouseEvent e) {
      tracking = false;
      doPicking = false;
   }   
   
   
   // Sub Structure
   class Line {
      public Line(double _x, double _y, double _z) { x = _x; y = _y; z = _z; }
      public Line() { x=0; y=0; z=0; }
      public double x;
      public double y;
      public double z;
   }
   
   
   public int NUM_LINES = 100;
   public int NUM_SEGMENTS = 120;
   
   public boolean doPicking = false;
   public boolean tracking = false;
   public int mouseX;
   public int mouseY;
   public int screenWidth;
   public int screenHeight;
   public float far = 8.0f;
   public Line lines[][] = new Line[NUM_LINES][NUM_SEGMENTS];
   
   public double startX = 0;
   public double startY = 0;


   GLU glu = new GLU();

   
   public float yrot = 0;

}
