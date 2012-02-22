package model;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import util.ShaderObj;

import com.jogamp.opengl.util.GLBuffers;

import datastore.SSM;

/////////////////////////////////////////////////////////////////////////////////
// This class is responsible for dumping the frame-buffer into
// a textured quad, with shader support to create post processing effects
// This is quite similar to magic lens actually, maybe it should be the base class ???
/////////////////////////////////////////////////////////////////////////////////
public class FrameBufferTexture {
   
   public int texture_FBO; // Frame buffer
   public int texture_RB;  // Render buffer
   public int texture_ID;  // Texture id 
   
   public int TEXTURE_SIZE_W = 800;
   public int TEXTURE_SIZE_H = 800;   
   
   public int vao[] = new int[1];
   public int vbo[] = new int[3];   
   
   public float square[] = {
         0.0f,  0.0f, 0.0f,      
         1.0f,  0.0f, 0.0f,      
         1.0f,  1.0f, 0.0f,      
         0.0f,  1.0f, 0.0f       
   };
     
   public float color[] = {
         1.0f, 0.0f, 0.0f, 1.0f, 
         0.0f, 1.0f, 0.0f, 1.0f,
         0.0f, 0.0f, 1.0f, 1.0f,
         1.0f, 1.0f, 1.0f, 1.0f 
   };
     
     
   public float texcoord[] = {
         0.0f, 0.0f,
         1.0f, 0.0f,
         1.0f, 1.0f,
         0.0f, 1.0f
   };    
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Constructor
   ////////////////////////////////////////////////////////////////////////////////
   public FrameBufferTexture() {
      TEXTURE_SIZE_H = SSM.instance().windowHeight;
      TEXTURE_SIZE_W = SSM.instance().windowWidth;
   }   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Start recording into the FBO/RB
   ////////////////////////////////////////////////////////////////////////////////
   public void startRecording(GL2 gl2) {
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, texture_FBO);
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, texture_RB);
      gl2.glPushAttrib( GL2.GL_VIEWPORT_BIT);
         gl2.glViewport(0, 0, TEXTURE_SIZE_W, TEXTURE_SIZE_H);
         gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
         gl2.glLoadIdentity();
   }   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Stop recording into the frame buffer
   ////////////////////////////////////////////////////////////////////////////////
   public void stopRecording(GL2 gl2) {
      gl2.glPopAttrib();
      
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initializes the frame buffer an rendering buffer for rendering to texture
   ////////////////////////////////////////////////////////////////////////////////
   public void initFrameBuffer(GL2 gl2) {
      int buffer1[] = new int[1];
      int buffer2[] = new int[1];
      int buffer3[] = new int[1];
      
      // Generate a FBO 
      gl2.glGenFramebuffers(1, buffer1, 0);
      texture_FBO = buffer1[0];
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, texture_FBO);
      
      // Generate a Render Buffer
      gl2.glGenRenderbuffers(1, buffer2, 0);
      texture_RB = buffer2[0];
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, texture_RB);
      
      // Generate a buffered texture
      gl2.glGenTextures(1, buffer3, 0);
      texture_ID = buffer3[0];
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
      gl2.glBindTexture(GL2.GL_TEXTURE_2D, texture_ID);
      
      // Bind Texture - TODO: Is this really generating a MIPMAP ????
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR); 
      gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, TEXTURE_SIZE_W, TEXTURE_SIZE_H, 0, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, null);
      //gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, TEXTURE_SIZE, TEXTURE_SIZE, 0, GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, null);
      gl2.glGenerateMipmap(GL2.GL_TEXTURE_2D);      
      
      // Bind Frame Buffer
      gl2.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D, texture_ID, 0);
      gl2.glDrawBuffer( GL2.GL_COLOR_ATTACHMENT0 );
      
      // Bind the Rendering Buffer
      gl2.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT32, TEXTURE_SIZE_W, TEXTURE_SIZE_H);
      gl2.glFramebufferRenderbuffer( GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_RENDERBUFFER, texture_RB);
      
      // Clean up
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize all the buffers
   ////////////////////////////////////////////////////////////////////////////////
   public void init(GL2 gl2, int w, int h) {
     TEXTURE_SIZE_W = w;
     TEXTURE_SIZE_H = h;
     init(gl2); 
   }
   public void init(GL2 gl2) {
      initFrameBuffer(gl2);      
      
      quadBuffer.put(square);
      colorBuffer.put(color);
      texBuffer.put(texcoord);
      
      // Reset ?
      quadBuffer.flip();
      colorBuffer.flip();
      texBuffer.flip();
      
      // Generate vertex array
      gl2.glGenVertexArrays(1, vao, 0);
      gl2.glBindVertexArray(vao[0]);
      
      // Generate vertex buffer
      gl2.glGenBuffers(3, vbo, 0);
      
      System.out.println("FBT ... " + vbo[0] + " " + vbo[1] + " " + vbo[2]);
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 12*4, quadBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(0);
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[1]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 16*4, colorBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(1, 4, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(1);
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[2]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 8*4, texBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(2, 2, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(2);      
      gl2.glBindVertexArray(0);
   }   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Apply FBO as texture and apply shader effects
   ////////////////////////////////////////////////////////////////////////////////
   public void render(GL2 gl2) {
      render(gl2, 1.0f, 1);   
   }
   public void render(GL2 gl2, float sampleRate, int useAverage) {
      gl2.glEnable(GL2.GL_BLEND);
      
      // Bind shader program
      gl2.glBindVertexArray(vao[0]);
      shader.bind(gl2);
         float buffer[] = new float[16];
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         shader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         shader.setUniform4x4(gl2, "modelview_matrix", buffer);

         shader.setUniform1i(gl2, "width", this.TEXTURE_SIZE_W);
         shader.setUniform1i(gl2, "height", this.TEXTURE_SIZE_H);
         shader.setUniformf(gl2, "sampleRate", sampleRate);
         shader.setUniform1i(gl2, "useAverage", useAverage);
         
         //setShaderUniform(gl2, la);
         gl2.glActiveTexture(GL2.GL_TEXTURE0);
         gl2.glBindTexture(GL2.GL_TEXTURE_2D, texture_ID);
         //gl2.glGenerateMipmap(GL2.GL_TEXTURE_2D);
         shader.setUniform1i(gl2, "tex", 0);
        
         gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
      shader.unbind(gl2);
      
      gl2.glBindVertexArray(0);
      gl2.glDisable(GL2.GL_BLEND);
   }   
   
   
   public FloatBuffer quadBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*3);
   public FloatBuffer colorBuffer   = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*4);
   public FloatBuffer texBuffer     = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*2);
   public ShaderObj shader = new ShaderObj();
}
