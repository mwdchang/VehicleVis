package model;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.awt.TextRenderer;

import util.FontRenderer;
import util.GLSLUtil;
import util.GraphicUtil;
import util.ShaderObj;

import datastore.Const;
import datastore.SSM;
import datastore.SchemeManager;

/////////////////////////////////////////////////////////////////////////////////
// This class contains the control for a single "len" (texture)
// Unfortunately, Java does not provide a way to pass functions, so 
// we cannot simply call 
//    MagicLens.draw( renderFunction ). 
//
// Instead, our magic lens will behave like a state machine as follows.
//    magicLens.start();
//    renderFunction()
//    magicLens.stop();
/////////////////////////////////////////////////////////////////////////////////
public class MagicLens {
   
   // FBO and Texture IDs
   public int texture_FBO; // Frame buffer
   public int texture_RB;  // Render buffer
   public int texture_ID;  // Texture id
   //public int TEXTURE_SIZE = 2048;
   //public int TEXTURE_SIZE = 1024;
   public int TEXTURE_SIZE_W = 800;
   public int TEXTURE_SIZE_H = 800;
   
   
   
   // Transformation stuff
   public float rotateAngle = 0.0f;
   
   
   // Shader ids and uniform parameters
   /*
   public int vert_shader;
   public int frag_shader;
   public int shader_program;
   public int projMatrix, viewMatrix, inColour, texMap, smouseX, smouseY, sareaHeight, sareaWidth, magicLensRadius, magicLensSelected;  // Vert shader uniform   
   */
   

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
   

   public FloatBuffer diamondBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*3);
   public FloatBuffer colorBuffer   = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*4);
   public FloatBuffer texBuffer     = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, 4*2);
   /*
   public FloatBuffer diamondBuffer = GLBuffers.newDirectFloatBuffer(square);
   public FloatBuffer colorBuffer   = GLBuffers.newDirectFloatBuffer(color);
   public FloatBuffer texBuffer     = GLBuffers.newDirectFloatBuffer(texcoord);
   */
   
   
   // Utility
   GLU glu = new GLU();
   
   
   // Constructor
   public MagicLens() {
   	//TEXTURE_SIZE_W = SSM.instance().LEN_TEXTURE_WIDTH;
   	//TEXTURE_SIZE_H = SSM.instance().LEN_TEXTURE_HEIGHT;
   	TEXTURE_SIZE_H = SSM.instance().windowHeight;
   	TEXTURE_SIZE_W = SSM.instance().windowWidth;
   }
   
   
   // Our rendering scene
   public void drawStuff(GL2 gl2, int mode) {
      gl2.glPushMatrix();
         // Render the first object
         gl2.glLoadIdentity();
         gl2.glTranslated(0, 0, -80);
         gl2.glRotated(rotateAngle, 0, 0, 1);
         gl2.glBegin(GL2.GL_TRIANGLES);    
            gl2.glColor4d(1, 0, 0, 1);
            gl2.glVertex3d(0, 0, 0.0);
            gl2.glColor4d(0, 1, 0, 1);
            gl2.glVertex3d(20, 0, 0.0);
            gl2.glColor4d(0, 0, 1, 1);
            gl2.glVertex3d(20, 20, 0.0);
         gl2.glEnd();         
         
         // Render the second object
         if (mode == 0) return;
         gl2.glLoadIdentity();
         gl2.glTranslated(0, 0, -80);
         gl2.glRotated(rotateAngle, 1, 1, 0);
         GraphicUtil.drawCube(gl2, 10.0f); // Note GraphicUtil use TRI-Fans
         
         gl2.glLoadIdentity();
         gl2.glTranslated(0, 0, -80);
         gl2.glRotated(-rotateAngle, 0, 1, 1);
         GraphicUtil.drawCubeQuad(gl2, 3.0f); // Note GraphicUtil use TRI-Fans
         
      gl2.glPopMatrix();
   }
   
   
   // Start recording into the FBO/RB
   public void startRecording(GL2 gl2) {
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, texture_FBO);
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, texture_RB);
      gl2.glPushAttrib( GL2.GL_VIEWPORT_BIT);
         gl2.glViewport(0, 0, TEXTURE_SIZE_W, TEXTURE_SIZE_H);
         gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
         gl2.glLoadIdentity();
   }
   
   
   // Stop recording 
   public void stopRecording(GL2 gl2) {
      gl2.glPopAttrib();
      
      // Close out FBO
      gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
      gl2.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
      /*
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
      gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);      
      */
   }
   
   
   
   
   // Apply FBO as texture and apply shader effects
   public void renderLens(GL2 gl2, LensAttrib la) {
      gl2.glEnable(GL2.GL_BLEND);
      //gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      
      // Setup projection and modelview
      /*
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0.0, 1.0, 0.0, 1.0, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      */
      
      
      // Bind shader program
      gl2.glBindVertexArray(vao[0]);
      //gl2.glUseProgram(shader_program); 
      shaderObj.bind(gl2);
         float buffer[] = new float[16];
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         shaderObj.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         shaderObj.setUniform4x4(gl2, "modelview_matrix", buffer);

         //buffer = new float[]{0.0f, 1.0f, 1.0f, 1.0f};
         //shaderObj.setUniformf(gl2, "inColour", buffer);
         
         
         shaderObj.setUniform1i(gl2, "smouseX", la.magicLensX);
         shaderObj.setUniform1i(gl2, "smouseY", SSM.instance().windowHeight - la.magicLensY);
         
         //shaderObj.setUniform1i(gl2, "areaHeight", SSM.instance().windowHeight);
         //shaderObj.setUniform1i(gl2, "areaWidth", SSM.instance().windowWidth);
         shaderObj.setUniformf(gl2, "magicLensRadius", la.magicLensRadius);
         //shaderObj.setUniform1i(gl2, "magicLensSelected", la.magicLensSelected);
         
         if (la.magicLensSelected == 1) {
            shaderObj.setUniformf(gl2, "lensColour", SchemeManager.selected.toArray());   
         } else {
            shaderObj.setUniformf(gl2, "lensColour", SchemeManager.unselected.toArray());   
         }
         
         
         //gl2.glUniform1i(sareaHeight, SSM.instance().windowHeight);
         //gl2.glUniform1i(sareaWidth, SSM.instance().windowWidth);
         
         //gl2.glUniform1i(smouseX, la.magicLensX);
         //gl2.glUniform1i(smouseY, SSM.instance().windowHeight - la.magicLensY);
         //gl2.glUniform1i(smouseX, SSM.instance().mouseX);
         //gl2.glUniform1i(smouseY, SSM.instance().windowHeight - SSM.instance().mouseY);
         
         //gl2.glUniform1f(magicLensRadius, la.magicLensRadius);
         //gl2.glUniform1i(magicLensSelected, la.magicLensSelected);         
         
         
         //setShaderUniform(gl2, la);
         gl2.glActiveTexture(GL2.GL_TEXTURE0);
         gl2.glBindTexture(GL2.GL_TEXTURE_2D, texture_ID);
         gl2.glGenerateMipmap(GL2.GL_TEXTURE_2D);
         
         shaderObj.setUniform1i(gl2, "tex", 0);
         if (SSM.instance().useDualDepthPeeling) {
            shaderObj.setUniform1i(gl2, "useTexture", 1);
         } else {
            shaderObj.setUniform1i(gl2, "useTexture", 1);
         }
   
         //gl2.glUniform1i(texMap, 0); // Because we bound it to GL_TEXTURE0         
         
         //gl2.glUniform1i(smouseX, 0);
         //gl2.glUniform1i(smouseY, 0); 
      
         gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);
      //gl2.glUseProgram(0);     
      shaderObj.unbind(gl2);
      gl2.glBindVertexArray(0);
      
      gl2.glDisable(GL2.GL_BLEND);
      gl2.glUseProgram(0);
      
      
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
   public void init(GL2 gl2) {
      
      initFrameBuffer(gl2);      
      
      System.out.println("Texture size" + this.TEXTURE_SIZE_W + "," + this.TEXTURE_SIZE_H);
      
      diamondBuffer.put(square);
      colorBuffer.put(color);
      texBuffer.put(texcoord);
      
      // Reset ?
      diamondBuffer.flip();
      colorBuffer.flip();
      texBuffer.flip();
      
      // Generate vertex array
      gl2.glGenVertexArrays(1, vao, 0);
      gl2.glBindVertexArray(vao[0]);
      
      // Generate vertex buffer
      gl2.glGenBuffers(3, vbo, 0);
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, 12*4, diamondBuffer, GL2.GL_STATIC_DRAW);
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
      
      // Shader creation
      initShader(gl2);
   }
   
   
   // Initialize the shader programs and their uniform parameters
   public void initShader(GL2 gl2) {
      
      shaderObj.createShader(gl2, Const.SHADER_PATH+"vert_magic_lens.glsl", GL2.GL_VERTEX_SHADER);
      shaderObj.createShader(gl2, Const.SHADER_PATH+"frag_magic_lens.glsl", GL2.GL_FRAGMENT_SHADER);
      shaderObj.createProgram(gl2);
      
      gl2.glBindAttribLocation(shaderObj.programID,  0, "in_position");
      gl2.glBindAttribLocation(shaderObj.programID,  1, "in_colour");
      gl2.glBindAttribLocation(shaderObj.programID,  2, "in_texcoord");
      
      shaderObj.linkProgram(gl2);
      
      shaderObj.bindFragColour(gl2, "outColour");
      
   }
   
   // Set global parameters
   /*
   public void setShaderUniform(GL2 gl2, LensAttrib la) {
      float buffer[];
      
      buffer = new float[16];   
      gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
      gl2.glUniformMatrix4fv(projMatrix, 1, false, buffer, 0);
      
      buffer = new float[16];
      gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
      gl2.glUniformMatrix4fv(viewMatrix, 1, false, buffer, 0);
      
      buffer = new float[]{0.0f, 1.0f, 1.0f, 1.0f};
      gl2.glUniform4fv(inColour, 1, buffer, 0);      
      
      gl2.glUniform1i(sareaHeight, SSM.instance().windowHeight);
      gl2.glUniform1i(sareaWidth, SSM.instance().windowWidth);
      
      gl2.glUniform1i(smouseX, la.magicLensX);
      gl2.glUniform1i(smouseY, SSM.instance().windowHeight - la.magicLensY);
      //gl2.glUniform1i(smouseX, SSM.instance().mouseX);
      //gl2.glUniform1i(smouseY, SSM.instance().windowHeight - SSM.instance().mouseY);
      
      gl2.glUniform1f(magicLensRadius, la.magicLensRadius);
      gl2.glUniform1i(magicLensSelected, la.magicLensSelected);
   }
   */
   
   
   public ShaderObj shaderObj = new ShaderObj();

}
