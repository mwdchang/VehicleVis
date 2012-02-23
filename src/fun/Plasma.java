package fun;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import test.JOGLBase;
import util.ShaderObj;


/////////////////////////////////////////////////////////////////////////////////
// Port of an old program that simulates a plasma like equation
// This java portion just initializes the parameters and
// send them off to a fragment shader, which does the heavy work
/////////////////////////////////////////////////////////////////////////////////
public class Plasma extends JOGLBase implements KeyListener {
   
   public static void main(String args[]) {
      Plasma plasma = new Plasma();   
      plasma.run("Plama", 600, 600);
   }

   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
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
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         this.resetData();
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void display(GLAutoDrawable drawable) {
      GL2 gl2 = drawable.getGL().getGL2();
      
      float w = drawable.getWidth();
      float h = drawable.getHeight();
      
      // Setup orthonormal projection
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0, w, 0, h, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      
      // Clear screen
      this.basicClear(gl2);
      
      
      // Send a quad down the pipeline, we will use the
      // shader for the actual calculation
      shader.bind(gl2);
         shader.setUniformf(gl2, "width", winWidth);
         shader.setUniformf(gl2, "height", winHeight);
         shader.setUniformf(gl2, "a", a);
         shader.setUniformf(gl2, "b", b);
         shader.setUniformf(gl2, "c", c);
         
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex3d(0, 0, 0);
            gl2.glVertex3d(winWidth, 0, 0);
            gl2.glVertex3d(winWidth, winHeight, 0);
            gl2.glVertex3d(0, winHeight, 0);
         gl2.glEnd();
      shader.unbind(gl2);
      
      
      a += 0.0002f;
      b += 0.0002f;
      
   }
   
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);
      this.canvas.addKeyListener(this);
      
      GL2 gl2 = a.getGL().getGL2();
      
      // Initialize the plasma shader
      shader = new ShaderObj();
      shader.createShader(gl2, "src\\Shader\\vert_plasma.glsl", GL2.GL_VERTEX_SHADER);
      shader.createShader(gl2, "src\\Shader\\frag_plasma.glsl", GL2.GL_FRAGMENT_SHADER);
      shader.createProgram(gl2);
      shader.linkProgram(gl2);      
      
   }
   
   
   public void resetData() {
      a = (float)(Math.random())*20.0f;
      b = (float)(Math.random())*10.0f-10.0f;
      c = (float)(Math.random())*40.0f;
   }
   
   
   public ShaderObj shader;
   
   public float a = 2.5f;
   public float b = 6.0f;
   public float c = 2.0f;

}
