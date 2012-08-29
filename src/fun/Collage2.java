package fun;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import model.FrameBufferTexture;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import datastore.Const;

import test.JOGLBase;
import util.GraphicUtil;
import util.ShaderObj;
import util.TextureFont;

////////////////////////////////////////////////////////////////////////////////
//
// A different collage organization, based on the concept of
// composing a complete picture with polaroids snaps.
//
// This came about after repeated listening of Frusciante's "Before the Beginning" on his
// album Empyrean and particularly Dire Straits' "Sultan of Swing", of which
// on YouTube had a collage photo that are composed into a guitar.
// 
////////////////////////////////////////////////////////////////////////////////
public class Collage2 extends JOGLBase implements KeyListener {

   public static void main(String[] args) {
      Collage2 collage = new Collage2();
      collage.isMaximized = true;
      collage.sendToNextScreen = true;
      collage.run("Collage 2", 800, 600);
   }
   
   public Texture background = null;
   public FrameBufferTexture frameFBT = null;
   public ShaderObj shaderObj = new ShaderObj();
   public ShaderObj shaderTxt = new ShaderObj();
   public boolean fbtInitDone = false;
   public boolean fbtRefreshDone = true;
   
   public boolean showLabel = false;
   
   public Vector<Location> locations = new Vector<Location>();
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      
      this.basicClear(gl2);
      
      
      if (this.fbtInitDone == false) {
         fbtInitDone = true;
         this.seedLocation(gl2);
         this.initFBT(gl2);
      }      
      
      if (this.fbtRefreshDone == false) {
         //this.fbtRefreshDone = true;
         
         // test
         for (int i=0; i < locations.size(); i++) {
            locations.elementAt(i).rotation +=  (i%2 == 0? Math.random()*4 : -Math.random()*4);   
            locations.elementAt(i).x += Math.random()*10-5;   
            locations.elementAt(i).y += Math.random()*10-5;   
            if (locations.elementAt(i).x > this.winWidth) locations.elementAt(i).x = this.winWidth;
            if (locations.elementAt(i).x < 0) locations.elementAt(i).x = 0;
            if (locations.elementAt(i).y > this.winHeight) locations.elementAt(i).y = this.winHeight;
            if (locations.elementAt(i).y < 0) locations.elementAt(i).y = 0;
         }
         this.initFBT(gl2);
      }
      
      GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
      gl2.glEnable(GL2.GL_BLEND);
      //gl2.glEnable(GL2.GL_TEXTURE_2D);
      
      
      gl2.glBindVertexArray(frameFBT.vao[0]);
      shaderObj.bind(gl2);
         float buffer[] = new float[16];
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         shaderObj.setUniform4x4(gl2, "projection_matrix", buffer);
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         shaderObj.setUniform4x4(gl2, "modelview_matrix", buffer);
         
         gl2.glActiveTexture(GL2.GL_TEXTURE0);
         gl2.glBindTexture(GL2.GL_TEXTURE_2D, frameFBT.texture_ID);
         shaderObj.setUniform1i(gl2, "tex", 0);
         
         gl2.glActiveTexture(GL2.GL_TEXTURE1);
         gl2.glBindTexture(GL2.GL_TEXTURE_2D, background.getTextureObject(gl2));
         shaderObj.setUniform1i(gl2, "tex2", 1);
         
         gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
      shaderObj.unbind(gl2);
      gl2.glBindVertexArray(0);      
      
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      super.init(a);   
      this.canvas.addKeyListener(this);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      background = loadTexture(gl2, "C:\\Users\\Daniel\\Pictures\\IMG_4976.JPG");
      
      frameFBT = new FrameBufferTexture();
      frameFBT.TEXTURE_SIZE_H = (int)this.winHeight;
      frameFBT.TEXTURE_SIZE_W = (int)this.winWidth;
      frameFBT.init(gl2);      
      
   }
   
   
   
   public void seedLocation(GL2 gl2) {
      // Seed locations
System.out.println("background : " + background.getTextureObject(gl2));      

      gl2.glDisable(GL2.GL_TEXTURE_2D);

      locations.clear();
      for (int i=0; i < 55; i++) {
         Location l = new Location();
         
         // 1) Get a random coordiant, size and angle
         l.x = Math.random()*this.winWidth;
         l.y = Math.random()*this.winHeight;
         
         // 2) Get a random coordiant, size and angle
         while(true) {
            l.x = Math.random()*this.winWidth;
            l.y = Math.random()*this.winHeight;
            
            boolean done = true;   
            for (int j=0; j < locations.size(); j++) {
               Location tmp = locations.elementAt(j);
               if (Math.sqrt( (l.x-tmp.x)*(l.x-tmp.x) + (l.y-tmp.y)*(l.y-tmp.y)) < 110) done = false;   
            }
            if (done == true) break;
         }
         
         
         
         //l.rotation = Math.random()*140 - 70;
         l.rotation = Math.random()*360;
         l.size = Math.random()*80 + 80;
         locations.add(l);
         
         // Get a random date 
         int year  = 2000 + (int)(Math.random()*(2012-2000));
         int month =  (int)(Math.random()*11);
         
         GregorianCalendar gc = new GregorianCalendar(year, month, 1);
         int day = 1 + (int)(Math.random()*( gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - 1));
         
         gc.set(year, month, day);
         
         String label = gc.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.CANADA) + " " +
                        gc.get(GregorianCalendar.DAY_OF_MONTH) + ", " +
                        gc.get(GregorianCalendar.YEAR) + 
                        " X X X X X X X "; 
         
         l.tf.width  = (float)(2*l.size);
         l.tf.height = 20;
         l.tf.addMark( label,
               //Color.GRAY, new Font("Times New Roman", Font.PLAIN, 20), 60, 5);
               Color.DARK_GRAY, new Font("John Handy LET", Font.PLAIN, 20), 50, 5);
         l.tf.renderToTexture(null);
         l.tf.anchorX = -(float)(l.size);
         l.tf.anchorY = -(float)(l.size+21);
         l.vao = GraphicUtil.createVAO(gl2, (float)(2*l.size), (float)20);
      }
   }
   
   
   public void initFBT(GL2 gl2) {
      //seedLocation(gl2);
      shaderObj.clean(gl2);
      shaderTxt.clean(gl2);
      

      
      
      shaderObj.createShader(gl2, Const.SHADER_PATH+"vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
      shaderObj.createShader(gl2, Const.SHADER_PATH+"frag_fun_collage.glsl", GL2.GL_FRAGMENT_SHADER);
      shaderObj.createProgram(gl2);
         gl2.glBindAttribLocation(shaderObj.programID,  0, "in_position");
         gl2.glBindAttribLocation(shaderObj.programID,  1, "in_colour");
         gl2.glBindAttribLocation(shaderObj.programID,  2, "in_texcoord");      
      shaderObj.linkProgram(gl2);
      shaderObj.bindFragColour(gl2, "outColour");   
      
      shaderTxt.createShader(gl2, Const.SHADER_PATH+"vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
      shaderTxt.createShader(gl2, Const.SHADER_PATH+"frag_fun_collage2.glsl", GL2.GL_FRAGMENT_SHADER);
      shaderTxt.createProgram(gl2);
         gl2.glBindAttribLocation(shaderTxt.programID,  0, "in_position");
         gl2.glBindAttribLocation(shaderTxt.programID,  1, "in_colour");
         gl2.glBindAttribLocation(shaderTxt.programID,  2, "in_texcoord");      
      shaderTxt.linkProgram(gl2);
      shaderTxt.bindFragColour(gl2, "outColour");   
      

      
      frameFBT.startRecording(gl2);
         GraphicUtil.setOrthonormalView(gl2, 0,  this.winWidth, 0, this.winHeight, -10, 10);   
         gl2.glClearColor(0, 0, 0, 0);
         gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl2.glDisable(GL2.GL_DEPTH_TEST);
         
         for (int i=0; i < locations.size(); i++) {
            Location l = locations.elementAt(i);
            double size = l.size;
            
            
            gl2.glPushMatrix();
               gl2.glTranslated(l.x, l.y, 0);
               gl2.glRotated(l.rotation, 0, 0, 1);
               
               gl2.glDisable(GL2.GL_TEXTURE_2D);
               gl2.glBegin(GL2.GL_QUADS);
                  gl2.glColor4d(0.7, 0.7, 0.7, 0.8);
                  gl2.glVertex2d(0-size-3, 0-size-(showLabel?23:3)); 
                  gl2.glVertex2d(0+size+3, 0-size-(showLabel?23:3)); 
                  gl2.glVertex2d(0+size+3, 0+size+3); 
                  gl2.glVertex2d(0-size-3, 0+size+3); 
                  
                  gl2.glColor4d(0.95, 0.95, 0.95, 0.8 + 0.2* (float)i/(float)locations.size());
                  gl2.glVertex2d(0-size-2, 0-size-(showLabel?22:2)); 
                  gl2.glVertex2d(0+size+2, 0-size-(showLabel?22:2)); 
                  gl2.glVertex2d(0+size+2, 0+size+2); 
                  gl2.glVertex2d(0-size-2, 0+size+2); 
               gl2.glEnd();
              
               gl2.glColor4d(0, 1, 0, 1);
               gl2.glBegin(GL2.GL_QUADS);
                  gl2.glVertex2d(0-size, 0-size); 
                  gl2.glVertex2d(0+size, 0-size); 
                  gl2.glVertex2d(0+size, 0+size); 
                  gl2.glVertex2d(0-size, 0+size); 
               gl2.glEnd();
               
               
               if (showLabel == true) {
                  gl2.glEnable(GL2.GL_BLEND);
                  gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                  gl2.glColor4d(1, 1, 1, 1);
                  gl2.glBindVertexArray( l.vao );
                  shaderTxt.bind(gl2);
                     gl2.glTranslated(l.tf.anchorX, l.tf.anchorY, 0);
                     float buffer[] = new float[16];
                     gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
                     shaderTxt.setUniform4x4(gl2, "projection_matrix", buffer);
                     gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
                     shaderTxt.setUniform4x4(gl2, "modelview_matrix", buffer);
                     
                     gl2.glActiveTexture(GL2.GL_TEXTURE0);
                     gl2.glBindTexture(GL2.GL_TEXTURE_2D, l.tf.texture.getTexture().getTextureObject(gl2));
                     shaderTxt.setUniform1i(gl2, "tex", 0);
                     
                     gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
                     
                  shaderTxt.unbind(gl2);
                  gl2.glBindVertexArray( 0 );
                  gl2.glDisable(GL2.GL_BLEND);
               }
               
               
            gl2.glPopMatrix();
         }
      frameFBT.stopRecording(gl2);
      
   }

   
   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      
      if (e.getKeyChar() == KeyEvent.VK_SPACE) {
         this.fbtRefreshDone = ! this.fbtRefreshDone;
      }
   }
   
   
   public static Texture loadTexture(GL2 gl2, String imgName) {
      TextureData textureData;
      
      try {
         File file = new File(imgName);
         
         // Parse image file to texture data
         textureData = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         
         // Get image attribute; height, width and channels
         ByteBuffer b = (ByteBuffer) textureData.getBuffer();
         int pixelFormat = textureData.getPixelFormat();
         if (pixelFormat == GL2.GL_RGBA || pixelFormat == GL2.GL_BGRA) {
            //System.out.println("Channel = 4");
         } else { 
            //System.out.println("Channel = 3");
         }
         return TextureIO.newTexture(textureData);
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      return null;
   }     

   
   public class Location {
      double x, y, size, rotation;
      TextureFont tf = new TextureFont();
      int vao;
   }
   
   
   // Not used
   public void keyReleased(KeyEvent arg0) {}
   public void keyTyped(KeyEvent arg0) {}

}
