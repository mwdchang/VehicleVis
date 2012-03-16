package fun;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import test.JOGLBase;
import util.DCUtil;
import util.TextureFont;


/////////////////////////////////////////////////////////////////////////////////
// Convert an image into ascii/symbols
//
// up arrow   - increase sample gap 
// down arrow - decrease sample gap
/////////////////////////////////////////////////////////////////////////////////
public class Ascii extends JOGLBase implements KeyListener {
   
   public static void main(String[] args) {
      Ascii ascii = new Ascii();
      ascii.unDecorated = false;
      ascii.run("Ascii", 1100, 900);
   }
   
   public String[] symbols =  new String[]{ " ", ".", "`", "☺", "☻", "♥", "♦", "♣", "♠", "•", "◘", "○", "◙" };
   public static int sample = 8;
   public static int fontS = 9;
   public Font f  = new Font( "Consolas", Font.PLAIN, 8);    
   
      @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      int height = a.getHeight();
      int width  = a.getWidth();
      
      if (reload == true) {
         this.loadTexture(gl2);
         reload = false;   
      }
      
      this.basicClear(gl2);
      
      
      // Make screen into 2D projection
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      //gl2.glOrtho(0, width, 0, height, -10, 10);
      gl2.glOrtho(0, (w/sample)*fontS, 0, (h/sample)*fontS, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      tf.render(gl2);
      
     
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0, width, 0, height, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      texture2.enable(gl2);
      texture2.bind(gl2);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glTexCoord2f(0, 1); gl2.glVertex2d(0, 0);
         gl2.glTexCoord2f(1, 1); gl2.glVertex2d(200, 0);
         gl2.glTexCoord2f(1, 0); gl2.glVertex2d(200, 200);
         gl2.glTexCoord2f(0, 0); gl2.glVertex2d(0, 200);
      gl2.glEnd();
      texture2.disable(gl2);
     
   }
   
   public void loadTexture(GL2 gl2) {
      try {
         
         // Get a random image file
         String dirStr = "C:\\Users\\Daniel\\Pictures\\";
         File dir = new File(dirStr);
         String s[] = dir.list( new FilenameFilter() {
            public boolean accept(File dir, String name) {
               String l = name.toLowerCase();
               if (l.endsWith(".jpg") || l.endsWith(".png")) {
                  return true;
               } return false;
            }
         });
         int index = (int)(Math.random()*(s.length-1));
         File file = new File(dirStr + "IMG_4185.jpg");
         
         
         // Parse image file to texture data
         // textureData will go used for ascii conversion
         // textureData2 will be rendered as is
         textureData = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         textureData2 = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         
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
         
         // Set up the ascii texture
         Color cl = Color.MAGENTA;
         tf = new TextureFont();
         tf.width = (w/sample)*fontS;
         tf.height = (h/sample)*fontS;
         tf.clearMark();
         
         
         System.out.println("TF: " + tf.width + " " + tf.height);
         
         // Print the ascii texture, as well to STDOUT
         float tY = tf.height-fontS;
         float tX = 0;
         for (int j=0; j < h; j+=sample) {
            for (int i=0; i < w; i+=sample) {
               int c = getIntensity1( getRGBA(b, i, j, w, h));
               tf.addMark(symbols[c]+"", cl, f, tX, tY, false);
               System.out.print(symbols[c]+"");
               tX += fontS;
            }
            tX = 0;
            tY -= fontS;
            System.out.println("");
         }
         texture = TextureIO.newTexture(textureData);
         texture2 = TextureIO.newTexture(textureData2);         
         
         // Resize the frame if necessary
         if ( (w/sample)*fontS < 1680) {
            if ((h/sample)*fontS < 1050) {
               this.frame.setBounds(0, 0, (w/sample)*fontS, (w/sample)*fontS);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
   }   
   
   

   
   @Override
   public void init(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      super.init(a);
      this.canvas.addKeyListener(this); 
      gl2.glEnable(GL2.GL_TEXTURE_2D);
   }
   
   public int byte2int(byte b) {
      //return (int)(b);
      return (int)b&0xFF;
   }
   
   
   // Get the RGB value at (x,y)
   public int[] getRGBA(ByteBuffer b, int x, int y, int w, int h) {
      int index = y*w*channel + x*channel;
      return new int[] {
          byte2int(b.get(index)),
          byte2int(b.get(index+1)),
          byte2int(b.get(index+2))
      };
   }
   
   // Set the RGB at (x, y)
   public void setRGBA(ByteBuffer b, int[] rgb, int x, int y, int w, int h) {
      int index = y*w*channel + x*channel;
      b.put(index, (byte)rgb[0]);
      b.put(index+1, (byte)rgb[1]);
      b.put(index+2, (byte)rgb[2]);
      b.put(index+3, (byte)rgb[3]);
   }
   
   public int getIntensity1(int[] rgb) {
      float v = rgb[0] + rgb[1] + rgb[2];
      v = v/3;
      v = v/255; // Normalize to between 0 and 1
      
      int vt = (int)(v*(symbols.length-1));
      return vt;
      
   }
   
   public TextureData textureData;
   public TextureData textureData2;
   public Texture texture;
   public Texture texture2;
   public int channel;
   public TextureFont tf;
   public int h;
   public int w;
   public boolean reload = true;
   

   @Override
   public void keyPressed(KeyEvent arg0) {
      // TODO Auto-generated method stub
   }

   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);
      if (e.getKeyCode() == KeyEvent.VK_UP) {
         this.sample++;   
         if (sample > 20) sample = 20;
         reload = true;
      }
      if (e.getKeyCode() == KeyEvent.VK_DOWN) {
         this.sample--;   
         if (sample < 1) sample = 1;
         reload = true;
      }      
      if (e.getKeyChar() == 's') {
         System.out.println("tring to save...");
         File f = DCUtil.fileChooser();   
         try {
            System.out.println("Saving tol : " + f.getAbsolutePath());
            BufferedWriter writer = DCUtil.openWriter(f.getAbsolutePath());
            for (int j=0; j < h; j+=sample) {
               for (int i=0; i < w; i+=sample) {
                  int c = getIntensity1( getRGBA((ByteBuffer)textureData.getBuffer(), i, j, w, h));
                  writer.write(symbols[c]+"");
               }
               writer.newLine();
            }            
            writer.flush();
            writer.close();
            System.out.println("Done...");
         } catch (Exception ex) { ex.printStackTrace(); }
      }
   }

   @Override
   public void keyTyped(KeyEvent e) {
      // TODO Auto-generated method stub
   }


}
