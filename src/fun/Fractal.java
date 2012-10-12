package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import datastore.SSM;

import test.JOGLBase;
import util.DCCamera;
import util.ShaderObj;

/////////////////////////////////////////////////////////////////////////////////
// Fractal generation using a fractal shader
// Mostly for complex number based fractals
// 
// Note the actual fractal generation logic 
// resides in vert_fractal.glsl, frag_fragctal.glsl and frag_fractal64.glsl
/////////////////////////////////////////////////////////////////////////////////
public class Fractal extends JOGLBase implements MouseWheelListener, KeyListener, MouseMotionListener, MouseListener {
   
   public static void main(String args[]) {
      Fractal f = new Fractal();
      f.run("Test Fractal", 800, 800);
   }
   
   public boolean use64 = false;

   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      
      this.basicClear(gl2);
      
      width = a.getWidth();
      height = a.getHeight();
      
      
      
      if (! pause) {
         juliaImaginary += juliaInc;
         if (juliaImaginary >= 0.95f)
            juliaInc = -juliaInc;
         else if (juliaImaginary <= 0.3f)
            juliaInc = -juliaInc;
      }
      
//      juliaReal += juliaInc2;
//      if (juliaReal >= 0.38f) 
//         juliaInc2 = -juliaInc2;
//      else if (juliaImaginary <= 0.35f)
//         juliaInc2 = -juliaInc2;
         
      
      
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0, 1, 0, 1, -1, 1);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      
      gl2.glColor3d(0.5, 0.5, 0);
      shader.bind(gl2);
         // Set shader uniform variables
         if (use64 == true) {
            shader.setUniform1i(gl2, "width", width);
            shader.setUniform1i(gl2, "height", height);
            shader.setUniform1i(gl2, "iter", iteration);
            
            
            shader.setUniformf(gl2, "cminX1", (float)dcminx);
            shader.setUniformf(gl2, "cminX2", (float)(dcminx - (double)((float)dcminx)));
            
            shader.setUniformf(gl2, "cminY1", (float)dcminy);
            shader.setUniformf(gl2, "cminY2", (float)(dcminy - (double)((float)dcminy)));
            
            shader.setUniformf(gl2, "cmaxX1", (float)dcmaxx);
            shader.setUniformf(gl2, "cmaxX2", (float)(dcmaxx - (double)((float)dcmaxx)));
            
            shader.setUniformf(gl2, "cmaxY1", (float)dcmaxy);
            shader.setUniformf(gl2, "cmaxY2", (float)(dcmaxy - (double)((float)dcmaxy)));
            
            shader.setUniformf(gl2, "juliaImaginary", juliaImaginary);
            shader.setUniformf(gl2, "juliaReal", juliaReal);
            
         } else {
            shader.setUniform1i(gl2, "width", width);
            shader.setUniform1i(gl2, "height", height);
            shader.setUniform1i(gl2, "iter", iteration);
            
            shader.setUniformf(gl2, "cminX", cminx);
            shader.setUniformf(gl2, "cminY", cminy);
            shader.setUniformf(gl2, "cmaxX", cmaxx);
            shader.setUniformf(gl2, "cmaxY", cmaxy);
            
            shader.setUniform1i(gl2, "flag", flag);
            
            shader.setUniformf(gl2, "juliaImaginary", juliaImaginary);
            shader.setUniformf(gl2, "juliaReal", juliaReal);
         }
         
         
         // Draw an empty quad so the fragments can propagate to the fragment shader
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex3d(0, 0, 0);
            gl2.glVertex3d(1, 0, 0);
            gl2.glVertex3d(1, 1, 0);
            gl2.glVertex3d(0, 1, 0);
         gl2.glEnd();
      shader.unbind(gl2);    
      
      //iteration ++;
      //if (iteration >= 64) iteration = 1;
      
      //canvas.swapBuffers();
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);      
      
      GL2 gl2 = a.getGL().getGL2();
      
      
      this.canvas.addMouseWheelListener(this);
      this.canvas.addMouseListener(this);
      this.canvas.addMouseMotionListener(this);
      this.canvas.addKeyListener(this);
     
     
      
      shader.createShader(gl2, "src\\Shader\\vert_fractal.glsl", GL2.GL_VERTEX_SHADER);
      if (use64) {
         shader.createShader(gl2, "src\\Shader\\frag_fractal64.glsl", GL2.GL_FRAGMENT_SHADER);
      } else {
         shader.createShader(gl2, "src\\Shader\\frag_fractal.glsl", GL2.GL_FRAGMENT_SHADER);
      }
      
      shader.createProgram(gl2);
      shader.linkProgram(gl2);
      
      dist = (cmaxx-cminx);
      ddist = (dcmaxx-dcminx);
      
   }
   

   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         System.out.println("Switching...");
         flag += 1;
         flag = flag % 3; 
      }
      if (e.getKeyChar() == 'p' ) {
         pause = !pause;   
      }
   }

   @Override
   public void keyReleased(KeyEvent arg0) {
   }

   @Override
   public void keyTyped(KeyEvent arg0) {
   }

   @Override
   public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
         if (e.getUnitsToScroll() > 0) {
            cminx -= dist*0.1f;
            cminy -= dist*0.1f;
            cmaxx += dist*0.1f;
            cmaxy += dist*0.1f;
            
            dcminx -= ddist*0.1;
            dcminy -= ddist*0.1;
            dcmaxx += ddist*0.1;
            dcmaxy += ddist*0.1;
         } else {
            cminx += dist*0.1f;
            cminy += dist*0.1f;
            cmaxx -= dist*0.1f;
            cmaxy -= dist*0.1f;
            
            dcminx += ddist*0.1;
            dcminy += ddist*0.1;
            dcmaxx -= ddist*0.1;
            dcmaxy -= ddist*0.1;
            
         }
         dist = (cmaxx-cminx);
         ddist = (dcmaxx-dcminx);
      }       
   }

   public int width;
   public int height;
   public ShaderObj shader = new ShaderObj();
   public int iteration = 64;
   public float cminx = -2.0f;
   public float cminy = -2.0f;
   public float cmaxx = 2.0f;
   public float cmaxy = 2.0f;
   
   public double dcminx = -2.0;
   public double dcminy = -2.0;
   public double dcmaxx = 2.0;
   public double dcmaxy = 2.0;
   
   
   public int mouseX = 0;
   public int mouseY = 0;
   public float dist = 0;
   public double ddist = 0;
   public int flag = 0;
   
   public float juliaReal = 0.35f;
   public float juliaImaginary = 0.3f;
   public float juliaInc = 0.0002f;
   public float juliaInc2 = 0.0001f;
   
   public boolean pause = false;
   
   
   
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
      // TODO Auto-generated method stub
      
   }


   @Override
   public void mouseDragged(MouseEvent e) {
      int cx = mouseX;
      int cy = mouseY;
      
      mouseX = e.getX();
      mouseY = height - e.getY();
      
      if (cx > mouseX ) {
         cminx += dist*0.01f;
         cmaxx += dist*0.01f;
         
         dcminx += ddist*0.01;
         dcmaxx += ddist*0.01;
      } else if (cx < mouseX){
         cminx -= dist*0.01f;
         cmaxx -= dist*0.01f;
         
         dcminx -= ddist*0.01;
         dcmaxx -= ddist*0.01;
      }
      
      if (cy > mouseY) {
         cminy += dist*0.01f;
         cmaxy += dist*0.01f;
         
         dcminy += ddist*0.01;
         dcmaxy += ddist*0.01;
      } else if (cy < mouseY ){
         cminy -= dist*0.01f;
         cmaxy -= dist*0.01f;
         
         dcminy -= ddist*0.01;
         dcmaxy -= ddist*0.01;
      }
      
      
   }


   @Override
   public void mouseMoved(MouseEvent e) {
      mouseX = e.getX();
      mouseY = height - e.getY();
   }
   
}
