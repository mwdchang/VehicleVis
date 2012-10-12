package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.GLBuffers;

import model.DCTriple;

import test.JOGLBase;
import util.GraphicUtil;


public class Landing extends JOGLBase implements MouseMotionListener, KeyListener, MouseListener {
   
   public boolean picking = true;
   public boolean doAnimate = false;
   public Vector<DCTriple> path = new Vector<DCTriple>();
   
   public int mouseX, mouseY;
   public boolean selected = false;
   
   public long fCounter = 0; 
   
   public DCTriple position = new DCTriple();
   
   public static void main(String args[]) {
      Landing landing = new Landing();
      landing.isMaximized = true;
      landing.run("Landing", 800, 800);
   }
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      GraphicUtil.setOrthonormalView(gl2, 0, winWidth, 0, winHeight, -10, 10);
      
      fCounter ++;
      
      gl2.glColor4d(1, 0, 0, 1);
      GraphicUtil.drawPie(gl2, position.x, position.y, 0, 20, 0, 360, 10);
      
      if ( picking == true) {
         picking(gl2);
         picking = false;
      }
      
      //if ( !path.isEmpty() && doAnimate && fCounter % 5 == 0) {
      if ( !path.isEmpty() && fCounter % 50 == 0) {
         System.out.println("path.size() ==> " + path.size());
         position = path.firstElement();
         path.remove(0);
      }
      
      if (path.isEmpty()) { 
         doAnimate = false;
         position.x += 0.1;
      }
      
      if (path.size() > 2) {
         gl2.glColor4d(0.5, 0.5, 0.5, 0.5);
         gl2.glBegin(GL2.GL_LINES);
         for (int i=0; i < path.size()-1; i++) {
            DCTriple d1 = path.elementAt(i);
            DCTriple d2 = path.elementAt(i+1);
            gl2.glVertex3d(d1.x, d1.y, 0);
            gl2.glVertex3d(d2.x, d2.y, 0);
         }
         gl2.glEnd();
      }
   }
   
   
   public void picking(GL2 gl2) {
      if (selected == true) return;
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      GraphicUtil.startPickingOrtho(gl2, buffer, mouseX, mouseY, (int)winWidth, (int)winHeight);
      gl2.glLoadName(999);
      gl2.glPushMatrix();
         GraphicUtil.drawPie(gl2, position.x, position.y, 0, 20, 0, 360, 10);
      gl2.glPopMatrix();    
      Integer ii = GraphicUtil.finishPicking(gl2, buffer);
      
      System.out.println(mouseX + " " + mouseY);
      
      if (ii == null) return;
      else selected = true;
   }
      
  
   
   public void init(GLAutoDrawable a) {
      this.winWidth  = a.getWidth();   
      this.winHeight = a.getHeight();   
      super.init(a);
      
      this.canvas.addMouseMotionListener(this);
      this.canvas.addKeyListener(this);
      this.canvas.addMouseListener(this);
      
      position = new DCTriple(200, 200, 0);
   }
   

   @Override
   public void mouseDragged(MouseEvent arg0) {
      mouseX = arg0.getX();
      mouseY = arg0.getY();
      picking = true;
      doAnimate = false;
      if (selected == true) {
         synchronized(path) {
            path.add(new DCTriple(mouseX, winHeight-mouseY, 0));
         }
      }
   }

   
   @Override
   public void mouseMoved(MouseEvent arg0) {
      mouseX = arg0.getX();
      mouseY = arg0.getY();
   }

   @Override
   public void keyPressed(KeyEvent arg0) {
      // TODO Auto-generated method stub
   }

   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);   
   }

   @Override
   public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mouseClicked(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mouseEntered(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mouseExited(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mousePressed(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mouseReleased(MouseEvent arg0) {
      this.doAnimate = true;
      this.selected = false;
   }


}
