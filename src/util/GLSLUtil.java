package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.jogamp.opengl.util.GLBuffers;

// Static functions for setting up shader profile, validation and error checking
// Somewhat adapted from the JOGL gaming forums
// http://www.java-gaming.org/index.php?;topic=19978.0
public class GLSLUtil {
   
   public static void checkCompileError(GL2 gl, int id) {
      IntBuffer status = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 1);
      gl.glGetShaderiv(id, GL2.GL_COMPILE_STATUS, status);
      
      if (status.get() == GL.GL_FALSE){
         getInfoLog(gl, id); 
      } else {
         System.out.println("Successfully compiled shader");
      }
   }
   
   public static void checkLinkAndValidationErrors(GL2 gl, int id) {
      IntBuffer status = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 1);
      gl.glGetProgramiv(id, GL2.GL_LINK_STATUS, status);
   
      if (status.get() == GL.GL_FALSE) {
          getInfoLog(gl, id);
      } else {
          status.rewind();
          gl.glValidateProgram(id);
          gl.glGetProgramiv(id, GL2.GL_VALIDATE_STATUS, status);
          if (status.get() == GL.GL_FALSE) {
              getInfoLog(gl, id);
          } else {
              System.out.println("Successfully linked program " + id);
          }
      }
   }   
   
   
   public static int createShader(GL2 gl, String name, int shaderType) {
      int shader = gl.glCreateShader(shaderType);
      
      try {
         String fsrc = "";
         String line = "";
         String buffer[];
         BufferedReader reader = new BufferedReader(new FileReader(name));
         while ((line=reader.readLine()) != null) {
            fsrc += line + "\n";
         }
         reader.close();
         buffer = new String[] { fsrc };
         gl.glShaderSource(shader, 1, buffer, (int[])null, 0);
         gl.glCompileShader(shader);
         checkCompileError(gl, shader); 
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return shader;
   }
   
   
   public static void getInfoLog(GL2 gl, int id) {
       IntBuffer info = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 1);
       gl.glGetShaderiv(id, GL2.GL_INFO_LOG_LENGTH, info);
       ByteBuffer infoLog = (ByteBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_BYTE, info.get(0));
       gl.glGetShaderInfoLog(id, info.get(0), null, infoLog);

       String infoLogString = Charset.forName("US-ASCII").decode(infoLog).toString();
       throw new Error("Shader compile error\n" + infoLogString);       
   }
   
}
