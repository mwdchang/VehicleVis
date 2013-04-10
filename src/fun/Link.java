package fun;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import test.JOGLBase;
import util.TextureFont;

public class Link extends JOGLBase implements KeyListener {
   
   public static void main(String args[]) {
      Link link = new Link();
      link.run("Link LInk", 500, 500);
   }
   
   Texture testTexture[] = new Texture[3];
   float angle = 0;
   
   
   @Override
   public void display(GLAutoDrawable d) {
      GL2 gl2 = d.getGL().getGL2();
      this.basicClear(gl2);
      
      gl2.glLoadIdentity();
      gl2.glTranslatef(0, 0, -60);
      gl2.glRotatef(angle, 0, 1, 0);
      
      
      gl2.glColor4f(1, 1, 1, 0.5f);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA); 
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glEnable(GL2.GL_BLEND);
      for (int i=0; i < testTexture.length; i++) {
         testTexture[i].enable(gl2);
         testTexture[i].bind(gl2);
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glTexCoord2f(0, 1); gl2.glVertex3d(0, 0, 10*(i-1));
            gl2.glTexCoord2f(1, 1); gl2.glVertex3d(10, 0, 10*(i-1));
            gl2.glTexCoord2f(1, 0); gl2.glVertex3d(10, 10, 10*(i-1));
            gl2.glTexCoord2f(0, 0); gl2.glVertex3d(0, 10, 10*(i-1));
         gl2.glEnd();
         testTexture[i].disable(gl2);      
      }
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      
      // Just fooling around
      gl2.glLineWidth(2.0f);
      gl2.glColor4f(1, 0, 0, 1);
      gl2.glBegin(GL2.GL_LINES);
         gl2.glVertex3f(4, 5, -10);
         gl2.glVertex3f(4, 5, 0);
         
         gl2.glVertex3f(4, 5, 0);
         gl2.glVertex3f(1, 2, 10);
         
         gl2.glVertex3f(4, 5, 0);
         gl2.glVertex3f(8, 8, 10);
      gl2.glEnd();
      
      
      
      angle += 0.02f;
   }
   
   @Override
   public void init(GLAutoDrawable d) {
      super.init(d);
      GL2 gl2 = d.getGL().getGL2();
      
      this.canvas.addKeyListener(this);
      
      testTexture[0] = this.loadTexture(gl2, "IMG_0917.jpg");
      testTexture[1] = this.loadTexture(gl2, "IMG_1791.jpg");
      testTexture[2] = this.loadTexture(gl2, "IMG_1794.jpg");
   }

   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
   }

   @Override
   public void keyReleased(KeyEvent e) {
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   
   
   public static String dirStr = "C:\\Users\\Daniel\\Pictures\\";
   
   public Texture loadTexture(GL2 gl2, String imgName) {
      TextureData textureData;
      int channel;
      int h;
      int w;
      
      try {
         File file = new File(dirStr + imgName);
         
         // Parse image file to texture data
         textureData = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         
         // Get image attribute; height, width and channels
         ByteBuffer b = (ByteBuffer) textureData.getBuffer();
         h = textureData.getHeight();
         w = textureData.getWidth();
         int pixelFormat = textureData.getPixelFormat();
         if (pixelFormat == GL2.GL_RGBA || pixelFormat == GL2.GL_BGRA) {
            System.out.println("Channel = 4");
            channel = 4;
         } else { 
            System.out.println("Channel = 3");
            channel = 3;
         }
         
         System.out.println("Capacity = " + b.capacity());
         System.out.println("Height = " + h);
         System.out.println("Width  = " + w);
         
         
         return TextureIO.newTexture(textureData);
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      return null;
   }      
   

}
