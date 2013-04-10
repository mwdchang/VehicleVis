package util;

import java.io.File;
import java.nio.ByteBuffer;


import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import datastore.SSM;

public class ImageLoader {
   
   public Texture texture;
   public String fileName = "";
   public boolean needToFlipCoord = false;
  
   public float tx = 1;
   
   
   public void render(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      
      if (SSM.presentationMode == true)
         tx+=20;
      else
         tx-=20;
      
      if (tx <= 0) tx = 0;
      if (tx >= SSM.windowHeight) tx = SSM.windowHeight;
      
      float viewWidth =  SSM.windowWidth;
      float viewHeight = SSM.windowHeight;
      
      GraphicUtil.setOrthonormalView(gl2, 0, viewWidth, 0, viewHeight, -10, 10);
      if (tx < viewHeight)
         gl2.glViewport(0, (int)(viewHeight-tx), (int)viewWidth, (int)viewHeight);
      else 
         gl2.glViewport(0, 0, (int)viewWidth, (int)viewHeight);
      
      /*
      if (tx < viewHeight)
         GraphicUtil.setOrthonormalView(gl2, 0, tx, 0, viewHeight, -10, 10);
      else 
         GraphicUtil.setOrthonormalView(gl2, 0, viewWidth, 0, viewHeight, -10, 10);
         */
      
      
      gl2.glColor4d(1, 1, 1, 1);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
      texture.enable(gl2);
      texture.bind(gl2);
      gl2.glBegin(GL2.GL_QUADS);
         if (needToFlipCoord) {
            gl2.glTexCoord2d(0, 1); gl2.glVertex2d(0, 0);
            gl2.glTexCoord2d(1, 1); gl2.glVertex2d(viewWidth, 0);
            gl2.glTexCoord2d(1, 0); gl2.glVertex2d(viewWidth, viewHeight);
            gl2.glTexCoord2d(0, 0); gl2.glVertex2d(0, viewHeight);
         } else {
            gl2.glTexCoord2d(0, 0); gl2.glVertex2d(0, 0);
            gl2.glTexCoord2d(1, 0); gl2.glVertex2d(viewWidth, 0);
            gl2.glTexCoord2d(1, 1); gl2.glVertex2d(viewWidth, viewHeight);
            gl2.glTexCoord2d(0, 1); gl2.glVertex2d(0, viewHeight);
         }
      gl2.glEnd();
      texture.disable(gl2);
   }
   
   public void init(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      TextureData textureData;
      
      try {
         File file = new File(fileName);
        
         // Parse image file to texture data
         textureData = TextureIO.newTextureData(gl2.getGLProfile(),  file, false, null);
         
         // Get image attribute; height, width and channels
         ByteBuffer b = (ByteBuffer) textureData.getBuffer();
         texture =  TextureIO.newTexture(textureData);
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      needToFlipCoord = texture.getMustFlipVertically();
   }   

}
