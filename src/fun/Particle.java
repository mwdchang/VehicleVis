package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.GLBuffers;

import test.JOGLBase;

/////////////////////////////////////////////////////////////////////////////////
// Simple Particle System
/////////////////////////////////////////////////////////////////////////////////
public class Particle extends JOGLBase implements KeyListener {
   
   public static void main(String[] args) {
      Particle p = new Particle();
      p.unDecorated = false;
      p.run("Particle System", 500, 500);
   }
   
   public static double angle = 0;
   public static double radius1 = 2;
   public static double radius2 = 1.1;
   
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      gl2.glTranslated(0, 0, -10);
      
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      this.drawFragment(gl2, 0.8, 1, 0.2, 0.5);
      
      
      
      //xpos = (radius1 + radius2)*Math.cos(angle) + 5*Math.cos((radius1+radius2)*angle/radius2);
      //ypos = (radius1 + radius2)*Math.sin(angle) + 5*Math.sin((radius1+radius2)*angle/radius2);
      //xpos = radius * Math.cos(angle);
      //ypos = radius * Math.sin(angle);
      //angle += 0.001;      
      //radius1 -= 0.001;
      //if (radius1 < 0) radius1 = 2;
   }   
   
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);   
      this.canvas.addKeyListener(this);
      
      GL2 gl2 = a.getGL().getGL2();
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      blurTexture(gl2);
      initFragment();
      

   }
   
   
   
   public void drawFragment(GL2 gl2, double r, double g, double b, double a) {
      double size; // = 0.05f;
      double slowX = 0.99f;
      double slowY = 0.99f;
      double slowZ = 0.99f;

      for (int i=0; i < this.fragmentSize; i++) {
         size = f[i].life / 2.5;
         gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
            gl2.glColor4d(r, g-f[i].life, b, a);
            gl2.glTexCoord2f(1,1); gl2.glVertex3d(f[i].x + size, f[i].y + size, f[i].z);
            gl2.glTexCoord2f(0,1); gl2.glVertex3d(f[i].x - size, f[i].y + size, f[i].z);
            gl2.glTexCoord2f(1,0); gl2.glVertex3d(f[i].x + size, f[i].y - size, f[i].z);
            gl2.glTexCoord2f(0,0); gl2.glVertex3d(f[i].x - size, f[i].y - size, f[i].z);
         gl2.glEnd();

         f[i].x += f[i].dx/180;
         f[i].y += f[i].dy/180;
         f[i].z += f[i].dz/180;

         f[i].dx *= slowX;
         f[i].dy *= slowY;
         f[i].dz *= slowZ;
         f[i].life -= f[i].fadespeed;


         if (f[i].life < 0.05f) {       
            double velocity = Math.random()*5 + 0.001;            
            double angle    = Math.random()*360 * Math.PI / 180.0;
            
            f[i] = new Fragment();
            f[i].x = xpos;
            f[i].y = ypos;
            f[i].z = zpos;
            
            f[i].dx = Math.cos(angle)*velocity;
            f[i].dy = Math.sin(angle)*velocity;
            f[i].dz = Math.random();
            
            f[i].life = 1.0;
            f[i].fadespeed = Math.random()/6.0;
         }
      }
   }
   
   
   public void blurTexture(GL2 gl2) {
      int h = 256;
      int w = 256;
      int h2 = 128;
      int w2 = 128;
      int channel = 4;
      ByteBuffer b = GLBuffers.newDirectByteBuffer(h*w*channel);
      
      for (int x=0; x < w; x++) {
         for (int y=0; y < h; y++) {
            int d = (int)Math.sqrt(  (x-w2)*(x-w2) + (y-h2)*(y-h2) );   
            int c = 255-(d*2);
            if (c < 0) c = 0;
            b.put((byte)c);
            b.put((byte)c);
            b.put((byte)c);
            b.put((byte)c);
         }
      }
      b.flip();
      
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
      gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, 3, w, h, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, b);
      
   }
   
   
   public void initFragment() {
      
      for (int i=0; i < fragmentSize; i++) {
         double velocity = Math.random()*5 + 0.001;            
         double angle    = Math.random()*360 * Math.PI / 180.0;
         
         f[i] = new Fragment();
         f[i].x = xpos;
         f[i].y = ypos;
         f[i].z = zpos;
         
         f[i].dx = Math.cos(angle)*velocity;
         f[i].dy = Math.sin(angle)*velocity;
         f[i].dz = Math.random();
         
         f[i].life = 1.0;
         f[i].fadespeed = Math.random()/6.0;
      }
   }
   
   
   public class Fragment {
      double x;
      double y;
      double z;
      
      double dx;
      double dy;
      double dz;
      
      double life;
      double fadespeed;
   }
   
   public int fragmentSize = 100;
   public Fragment f[] = new Fragment[fragmentSize];
   public double xpos = 1;
   public double ypos = 1;
   public double zpos = 0;
   
   

   @Override
   public void keyPressed(KeyEvent e) {
   }

   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }


   

}
