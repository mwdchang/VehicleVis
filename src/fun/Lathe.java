package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.GLBuffers;


import test.JOGLBase;
import util.GraphicUtil;

////////////////////////////////////////////////////////////////////////////////
// Simulate a lathe. It only limited to sculpting the surfaces, the mesh structure 
// does allow modifications on the inside.
////////////////////////////////////////////////////////////////////////////////
public class Lathe extends JOGLBase implements KeyListener, MouseListener, MouseMotionListener {

   public static void main(String args[]) {
      Lathe lathe = new Lathe();
      lathe.unDecorated = false;
      lathe.run("Lathe", 800, 800);
   }
   
   public boolean rotate = false;

   public float rot = 0.0f;
   public int stacks = 20;
   public int slice = 10;
   
   float diffuse[] = {0.5f, 0.5f, 0.5f, 0.5f};
   
   public double cylinder[][] = new double[360][stacks];

   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();   
      
      if (rotate) {
         rot+=1.6f;
      }
      
      this.basicClear(gl2);
      
      gl2.glTranslated(0, 0, -50);
     // gl2.glRotatef(rot, 1, 1, 0);
     gl2.glRotatef(rot, 1, 0, 0);
       //gl2.glRotatef(rot, 0, 1, 0);
      
      gl2.glEnable(GL2.GL_DEPTH_TEST);
      gl2.glEnable(GL2.GL_LIGHTING);
      gl2.glEnable(GL2.GL_LIGHT0);
      //gl2.glEnable(GL2.GL_NORMALIZE);
      
      gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
      
      for (int i=0; i < 360; i+= slice) {
         for (int j=0; j < stacks; j++) {
            drawPiece(gl2, i, cylinder[i][j], j-(stacks/2));
         }
      }
      
      if (this.picking == true) {
         System.out.println("Mouse : " + mouseX + " " + mouseY);
         IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
         GraphicUtil.startPickingPerspective(gl2, buffer, mouseX, mouseY, (int)winWidth, (int)winHeight, 30.0f);
         gl2.glTranslated(0, 0, -50);  
         gl2.glRotatef(rot, 1, 0, 0);
         for (int i=0; i < 360; i+= slice) {
            for (int j=0; j < stacks; j++) {
               drawPiece(gl2, i, cylinder[i][j], j-(stacks/2));
            }
         }
         Integer ii = GraphicUtil.finishPicking(gl2, buffer);
         double amt = 0.08;
         if (ii != null) {
            int angle = ii % 1000;
            int index = ( ii-angle )/1000;
            cylinder[angle][index] -= amt;
//            System.out.println("Selected " + angle + " " + index);
         }
         
      }
      /*
      for (int j=-5; j < 5; j++) {
         gl2.glColor4d(0.5, 0.5, 0.5, 0.5);
         for (int i=0; i < 360; i+=1) {
            drawPiece(gl2, i, 4, j);
         }
      }
      */
      
      /*
      gl2.glBegin(GL2.GL_TRIANGLES);
         gl2.glVertex3d(0, 0, 0);
         gl2.glVertex3d(10, 0, 0);
         gl2.glVertex3d(10, 10, 0);
      gl2.glEnd();
      */
   }
   
   public void init(GLAutoDrawable a) {
      super.init(a);
      winWidth = a.getWidth();   
      winHeight = a.getHeight();
      
      this.canvas.addKeyListener(this);
      this.canvas.addMouseListener(this);
      this.canvas.addMouseMotionListener(this);
      
      for (int i=0; i < 360; i++) {
         for (int j=0; j < stacks; j++) {
            cylinder[i][j] = 5;         
         }
      }
      
      
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Y and Z, 
   ////////////////////////////////////////////////////////////////////////////////
   public void drawPiece(GL2 gl2, double angle, double r, double index) {
      gl2.glLoadName( (int)( (index+(stacks/2))*1000 + angle)); 
      gl2.glPushMatrix();
      double RAD1 = angle*Math.PI/180.0;
      double RAD2 = (angle+slice)*Math.PI/180.0;
      gl2.glBegin(GL2.GL_TRIANGLES);
         gl2.glVertex3d(index, 0, 0);  
         // left
         gl2.glNormal3d(-1, 0, 0);
         gl2.glVertex3d(index-0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         gl2.glVertex3d(index-0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
         
         // right
         gl2.glNormal3d(1, 0, 0);
         gl2.glVertex3d(index, 0, 0);  
         gl2.glVertex3d(index+0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         gl2.glVertex3d(index+0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
         
         // top
         gl2.glNormal3d(index-0.5, Math.sin(RAD1)*r, -Math.cos(RAD1)*r);
         gl2.glVertex3d(index, 0, 0);  
         gl2.glVertex3d(index-0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         gl2.glVertex3d(index+0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         
         // bottom
         gl2.glNormal3d(index-0.5, -Math.sin(RAD1)*r, Math.cos(RAD1)*r);
         gl2.glVertex3d(index, 0, 0);  
         gl2.glVertex3d(index-0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
         gl2.glVertex3d(index+0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
      gl2.glEnd();
      
      // cap
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glNormal3d(0, Math.cos(RAD1), Math.sin(RAD1)); 
         gl2.glVertex3d(index-0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         gl2.glVertex3d(index+0.5, Math.cos(RAD1)*r, Math.sin(RAD1)*r);
         gl2.glVertex3d(index+0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
         gl2.glVertex3d(index-0.5, Math.cos(RAD2)*r, Math.sin(RAD2)*r);
      gl2.glEnd();
      gl2.glPopMatrix();
   }
   
   
   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);    
      
      double amt = 0.00002;
      
      if (e.getKeyCode() == KeyEvent.VK_1) for (int i=0; i < 360; i++) cylinder[i][0] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_2) for (int i=0; i < 360; i++) cylinder[i][1] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_3) for (int i=0; i < 360; i++) cylinder[i][2] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_4) for (int i=0; i < 360; i++) cylinder[i][3] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_5) for (int i=0; i < 360; i++) cylinder[i][4] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_6) for (int i=0; i < 360; i++) cylinder[i][5] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_7) for (int i=0; i < 360; i++) cylinder[i][6] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_8) for (int i=0; i < 360; i++) cylinder[i][7] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_9) for (int i=0; i < 360; i++) cylinder[i][8] -= amt;
      if (e.getKeyCode() == KeyEvent.VK_0) for (int i=0; i < 360; i++) cylinder[i][9] -= amt;
      
      if (e.getKeyCode() == KeyEvent.VK_SPACE) rotate = !rotate; 
   }
   public void keyPressed(KeyEvent arg0) { }
   public void keyTyped(KeyEvent arg0) { }

   @Override
   public void mouseClicked(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void mouseEntered(MouseEvent arg0) {
   }

   @Override
   public void mouseExited(MouseEvent arg0) {
   }


   @Override
   public void mousePressed(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
      picking = true;   
   }

   @Override
   public void mouseReleased(MouseEvent arg0) {
      picking = false;
   }
   
   

   public int mouseX, mouseY;
   public boolean picking = false;


   @Override
   public void mouseDragged(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
      picking = true;   
   }

   @Override
   public void mouseMoved(MouseEvent e) {
      System.out.println("blah");
      mouseX = e.getX();
      mouseY = e.getY();
   }

}
