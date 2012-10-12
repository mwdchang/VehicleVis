package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import model.DCColour;

import test.JOGLBase;
import util.GraphicUtil;

public class Collage extends JOGLBase implements KeyListener {

   public static void main(String args[]) {
      Collage collage = new Collage();
      collage.isMaximized = true;
      collage.run("Test", 1400, 900);
   }
   
   public Pic result;
   
   public long time = System.currentTimeMillis();
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      
      GraphicUtil.setOrthonormalView(gl2, 0, this.winWidth, 0, this.winHeight, -10, 10);
      
      if (System.currentTimeMillis() - time > 5000) {
         this.splitPic();
         time = System.currentTimeMillis();
      }
      
      /*
      gl2.glColor4d(1, 1, 1, 1);
      gl2.glBegin(GL2.GL_TRIANGLES);
         gl2.glVertex2d(0, 50);
         gl2.glVertex2d(100, 0);
         gl2.glVertex2d(100, 100);
      gl2.glEnd();
      */
      
      renderPic(gl2, result);
   }
   
   
   @Override
   public void init(GLAutoDrawable a) {
      this.canvas.addKeyListener(this);
      
      winWidth  = a.getWidth();
      winHeight = a.getHeight();
      splitPic();
   }
   
   
   public void splitPic() {
      result = new Pic(0, 0, winWidth, winHeight);
      split(result);
      split(result);
      split(result);
      split(result);
      
      // Fix aspect ratios
      fix(result);
   }
   
   public void renderPic(GL2 gl2, Pic p) {
      if (p.children.size() == 0) {
         if (p.texture == null) {
            //File dir = new File("C:\\Users\\Daniel\\Pictures");
            //File dir = new File("C:\\Users\\Daniel\\temporary");
            //File dir = new File("C:\\Users\\Daniel\\Dropbox\\DCShare\\Canoe_June_16");
            File dir = new File("C:\\Users\\Daniel\\Pictures\\New Folder\\");
            String s[] = dir.list( new FilenameFilter() {
               public boolean accept(File dir, String name) {
                  String l = name.toLowerCase();
                  if (l.endsWith(".jpg") || l.endsWith(".png")) {
                     return true;
                  } return false;
               }
            });         
            int index = (int)(Math.random()*s.length);
            p.texture = this.loadTexture(gl2, dir+"\\"+s[index]);
         }

         //gl2.glColor4fv(p.colour.toArray(), 0);
         gl2.glColor4d(0.5, 0.5, 0.5, 1.0);
         
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex2d(p.x1+2, p.y1+2);
            gl2.glVertex2d(p.x2-2, p.y1+2);
            gl2.glVertex2d(p.x2-2, p.y2-2);
            gl2.glVertex2d(p.x1+2, p.y2-2);
         gl2.glEnd();
         
         p.texture.enable(gl2);
         p.texture.bind(gl2);
         gl2.glColor4f(1, 1, 1, (float)Math.random());
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, 1); gl2.glVertex2d(p.x1+4, p.y1+4);
            gl2.glTexCoord2f(1, 1); gl2.glVertex2d(p.x2-4, p.y1+4);
            gl2.glTexCoord2f(1, 0); gl2.glVertex2d(p.x2-4, p.y2-4);
            gl2.glTexCoord2f(0, 0); gl2.glVertex2d(p.x1+4, p.y2-4);
         gl2.glEnd();
         p.texture.disable(gl2);  
         
      } else {
         for (Pic c: p.children) {
            renderPic(gl2, c);
         }
      }
   }
   
   // Make sure the aspect ratio is not ridiculously skewed
   public void fix(Pic p) {
      if (p.children.size() > 0) {
         for (Pic c : p.children) {
            fix(c);
         }
      } else {
         if ( (p.x2-p.x1) > 1.5*(p.y2-p.y1) ) {
System.err.println("Fixing x");            
            double mx = (p.x1 + p.x2)/2.0;
            p.children.add(new Pic(p.x1, p.y1, mx, p.y2));
            p.children.add(new Pic(mx, p.y1, p.x2, p.y2));
            return;   
         }
         if ( (p.y2-p.y1) > 1.5*(p.x2-p.x1) ) {
System.err.println("Fixing y");            
            double my = (p.y1 + p.y2)/2.0;
            p.children.add(new Pic(p.x1, p.y1, p.x2, my));
            p.children.add(new Pic(p.x1, my, p.x2, p.y2));
            return; 
         }
      }
   }
   
   public void split(Pic p) {
      if (p.children.size() > 0) {
         for (Pic c : p.children) {
            split(c);
         }
      } else {
         double seed = Math.random();
         if ( (p.x2-p.x1) > 1.5*(p.y2-p.y1) ) {
         //if (seed > 0.5) {
            double mx = (p.x2 - p.x1)*(0.25 + Math.random()*0.25) + p.x1;
            //double mx = (p.x1 + p.x2)/(2+Math.random());    
            //double mx = (p.x1 + p.x2)/2.0;
            p.children.add(new Pic(p.x1, p.y1, mx, p.y2));
            p.children.add(new Pic(mx, p.y1, p.x2, p.y2));
         } else {
            double my = (p.y2 - p.y1)*(0.25 + Math.random()*0.25) + p.y1; 
            //double my = (p.y1 + p.y2)/(2+Math.random());    
            //double my = (p.y1 + p.y2)/2.0;
            p.children.add(new Pic(p.x1, p.y1, p.x2, my));
            p.children.add(new Pic(p.x1, my, p.x2, p.y2));
         }
      }
   }
   
   
   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      if (e.getKeyChar() == 'r') {
         this.splitPic();   
      }
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   public static Texture loadTexture(GL2 gl2, String imgName) {
      TextureData textureData;
      int channel;
      int h;
      int w;
      
      try {
         File file = new File(imgName);
         
         // Parse image file to texture data
         textureData = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         
         // Get image attribute; height, width and channels
         ByteBuffer b = (ByteBuffer) textureData.getBuffer();
         h = textureData.getHeight();
         w = textureData.getWidth();
         int pixelFormat = textureData.getPixelFormat();
         if (pixelFormat == GL2.GL_RGBA || pixelFormat == GL2.GL_BGRA) {
            //System.out.println("Channel = 4");
            channel = 4;
         } else { 
            //System.out.println("Channel = 3");
            channel = 3;
         }
         return TextureIO.newTexture(textureData);
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      return null;
   }      
   
   
   public class Pic {
      public Pic() { 
         x1=x2=y1=y2=0; 
      }
      public Pic(double x1, double y1, double x2, double y2) {
         this.x1 = x1;
         this.y1 = y1;
         this.x2 = x2;
         this.y2 = y2;
         this.colour = DCColour.fromDouble(Math.random(), Math.random(), Math.random(), 1.0); 
         
         
      }
      
      public String toString() {
         return x1 + " " + y1 + " " + x2 + " " + y2;
      }
      double x1, x2;
      double y1, y2;
      
      public Vector<Pic> children = new Vector<Pic>();
      public DCColour colour;
      public Texture texture;
   }

}
