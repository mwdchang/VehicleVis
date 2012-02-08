package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

/////////////////////////////////////////////////////////////////////////////////
// Encapsulates shader compilation and creation. Allows a program to attach multiple
// vertex and fragment shaders. (No support for geometry shaders yet...).
//
//
// Usage:
//    ShaderObj test = new ShaderObj();
//    test.createShader(gl2, "/path/to/vertex1.glsl", GL2.GL_VERTEX_SHADER);
//    test.createShader(gl2, "/path/to/vertex2.glsl", GL2.GL_VERTEX_SHADER);
//    test.createShader(gl2, "/path/to/fragment1.glsl", GL2.GL_FRAGMENT_SHADER);
//    test.createProgram();
//    test.linkProgram();
//
//    test.bind();
//       test.setUniform4f(...);
//       test.setUniform4x4f(...); 
//       ...drawing stuff...
//    test.unbind();
//
//
//
// Note: The ShaderObj class is made explicitly to be unforgiving, any errors,
// will result in a System.exit() call, this decision was made to avoid mistakes snowballing
// to a larger problem. If there is an error, fix it now !!!
//
//
// Hey, have you fallen off the shelf, can I help you get yourself back together. I'm so tired, can I help you save yourself...
/////////////////////////////////////////////////////////////////////////////////
public class ShaderObj {
   
   // Operational method
   public void bind(GL2 gl2)   { gl2.glUseProgram( programID ); }
   public void unbind(GL2 gl2) { gl2.glUseProgram( 0 ) ; }
   public void clean(GL2 gl2) {
      for (int i=0; i < vertexShaderList.size(); i++) {
         gl2.glDeleteShader(vertexShaderList.elementAt(i));
      }
      for (int i=0; i < fragmentShaderList.size(); i++) {
         gl2.glDeleteShader(fragmentShaderList.elementAt(i));
      }
      for (int i=0; i < geometryShaderList.size(); i++) {
         gl2.glDeleteShader(geometryShaderList.elementAt(i));
      }
      gl2.glDeleteProgram(programID);
      
      vertexShaderList.clear();
      geometryShaderList.clear();
      fragmentShaderList.clear();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Have a wrapper around the commonly used types 
   // Not really required but makes it nicer from an object oriented programming perspecitve
   ////////////////////////////////////////////////////////////////////////////////
   public int getUniformId(GL2 gl2, String name) {
      int id = gl2.glGetUniformLocation(programID, name );   
      if (id < 0) {
         System.err.println("Unable to find : " + name);
         System.exit(0);
      }
      return id;
   }
   public int getUniformId(GL3 gl3, String name) {
      int id = gl3.glGetUniformLocation(programID, name );   
      if (id < 0) {
         System.err.println("Unable to find : " + name);
         System.exit(0);
      }
      return id;
   }
  
   
   public void setUniform4x4(GL2 gl2, String name, float[] buffer) {
      int id = getUniformId(gl2, name);
      gl2.glUniformMatrix4fv(id, 1, false, buffer, 0); 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Umbrella setting uniform vectors
   ////////////////////////////////////////////////////////////////////////////////
   public void setUniformf(GL2 gl2, String name, float ...val){
      // Sanity
      if (val.length > 4 || val.length < 1) {
         System.out.println("Only support vals between 1 and 4");
         System.exit(0);
      }
      
      int id = getUniformId(gl2, name);
      switch (val.length) {
         case 1: { gl2.glUniform1f(id, val[0]); break; }
         case 2: { gl2.glUniform2f(id, val[0], val[1]); break; }
         case 3: { gl2.glUniform3f(id, val[0], val[1], val[2]); break; }
         case 4: { gl2.glUniform4f(id, val[0], val[1], val[2], val[3]); break; }
      }
   }
   
   // Requires GL3+
   public void setUniformd(GL3 gl3, String name, double ...val) {
      if (val.length > 4 || val.length < 1) {  
         System.out.println("Only support vals between 1 and 4");
         System.exit(0);
      }
      
      int id = getUniformId(gl3, name);
      switch (val.length) {
         case 1: { gl3.glUniform1d(id, val[0]); break; }
         case 2: { gl3.glUniform2d(id, val[0], val[1]); break; }
         case 3: { gl3.glUniform3d(id, val[0], val[1], val[2]); break; }
         case 4: { gl3.glUniform4d(id, val[0], val[1], val[2], val[3]); break; }
      }
  }
   
   
   
   /* Not really used, but keep around if this is more readable
   public void setUniform4f(GL2 gl2, String name, float a, float b, float c, float d) {
      int id = getUniformId(gl2, name);
      gl2.glUniform4f(id, a, b, c, d);
   }
   public void setUniform3f(GL2 gl2, String name, float a, float b, float c) {
      int id = getUniformId(gl2, name);
      gl2.glUniform3f(id, a, b, c);
   }
   public void setUniform2f(GL2 gl2, String name, float a, float b) {
      int id = getUniformId(gl2, name);
      gl2.glUniform2f(id, a, b);
   }
   public void setUniform1f(GL2 gl2, String name, float a) {
      int id = getUniformId(gl2, name);
      gl2.glUniform1f(id, a);
   }
   */
   
   public void setUniform1fv(GL2 gl2, String name, float[] a) {
      int id = getUniformId(gl2, name);
      gl2.glUniform1fv(id, 1, a, 0);
   }
   
   
   public void setUniform1i(GL2 gl2, String name, int a) {
      int id = getUniformId(gl2, name);
      gl2.glUniform1i(id, a);
   }
   
   
   public void bindTexture(GL2 gl, int target,String texname, int texid, int texunit) {
      gl.glActiveTexture(GL2.GL_TEXTURE0 + texunit);
      gl.glBindTexture(target, texid);
      setUniform1i(gl, texname, texunit);
      gl.glActiveTexture(GL2.GL_TEXTURE0);
   }


   public void bindTexture2D(GL2 gl, String texname, int texid, int texunit) {
      bindTexture(gl, GL2.GL_TEXTURE_2D, texname, texid, texunit);
   }

   public void bindTexture3D(GL2 gl, String texname, int texid, int texunit) {
      bindTexture(gl, GL2.GL_TEXTURE_3D, texname, texid, texunit);
   }

   public void bindTextureRECT(GL2 gl, String texname, int texid, int texunit) {
      bindTexture(gl, GL2.GL_TEXTURE_RECTANGLE_ARB, texname, texid, texunit);
   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // For some GLSL versions
   ////////////////////////////////////////////////////////////////////////////////
   public void bindFragColour(GL2 gl2, String name) {
      gl2.glBindFragDataLocation(programID, 0, name);      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create an openGL shader object
   ////////////////////////////////////////////////////////////////////////////////
   public int createShader(GL2 gl2, String name, int shaderType) {
      int shader = gl2.glCreateShader(shaderType);
      
      if ( !(shaderType == GL2.GL_VERTEX_SHADER || shaderType == GL2.GL_FRAGMENT_SHADER || shaderType == GL3.GL_GEOMETRY_SHADER) ) {
         System.out.println("Incorrection shader type : " + shaderType);
         System.exit(0);
      }
      
      try {
         System.out.println("Creating : " + name );
         
         String fsrc = "";
         String line = "";
         String buffer[];
         BufferedReader reader = new BufferedReader(new FileReader(name));
         while ((line=reader.readLine()) != null) {
            fsrc += line + "\n";
         }
         reader.close();
         buffer = new String[] { fsrc };
         gl2.glShaderSource(shader, 1, buffer, (int[])null, 0);
         gl2.glCompileShader(shader);
         if (compileStatus(gl2, shader)) {
            switch (shaderType) {
               case GL2.GL_FRAGMENT_SHADER: { fragmentShaderList.add(shader); break; }
               case GL2.GL_VERTEX_SHADER:   { vertexShaderList.add(shader); break; }
               case GL3.GL_GEOMETRY_SHADER: { geometryShaderList.add(shader); break; }
               default: System.exit(0);
            }
         } else {
            System.exit(0);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return shader;
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Validate the shader compilation process
   ////////////////////////////////////////////////////////////////////////////////
   public boolean compileStatus(GL2 gl2, int shaderID) {
      int[] params = new int[]{0};
      
      gl2.glGetShaderiv( shaderID, GL2.GL_COMPILE_STATUS, params, 0);
      if ( params[0] != 1 ) {
         System.err.println("compile status: " + params[0]);
         gl2.glGetShaderiv( shaderID, GL2.GL_INFO_LOG_LENGTH, params, 0);
         System.err.println("log length: " + params[0]);
         byte[] abInfoLog = new byte[params[0]];
         gl2.glGetShaderInfoLog(shaderID, params[0], params, 0, abInfoLog, 0);
         System.err.println( new String(abInfoLog) );
         return false;
      }
      return true;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create a program with the existing shaders
   ////////////////////////////////////////////////////////////////////////////////
   public int createProgram(GL2 gl2) {
      programID = gl2.glCreateProgram();

      for (int i = 0; i < vertexShaderList.size(); i++) {
         gl2.glAttachShader(programID, vertexShaderList.elementAt(i));
      }
      for (int i=0; i < geometryShaderList.size(); i++) {
         gl2.glAttachShader(programID, geometryShaderList.elementAt(i));
      }
      for (int i = 0; i < fragmentShaderList.size(); i++) {
         gl2.glAttachShader(programID, fragmentShaderList.elementAt(i));
      }
      //gl2.glLinkProgram(programID);      
      /*
      if (! linkStatus(gl2, programID)) {
         System.exit(0);   
      }
      */
      return programID;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Validate the linking process
   ////////////////////////////////////////////////////////////////////////////////
   public boolean linkProgram(GL2 gl2) {
      gl2.glLinkProgram(programID);
      int[] params = new int[]{0};
      gl2.glGetProgramiv( programID, GL2.GL_LINK_STATUS, params, 0);

      if (params[0] != 1) {
         System.err.println("link status: " + params[0]);
         gl2.glGetProgramiv( programID, GL2.GL_INFO_LOG_LENGTH, params, 0);
         System.err.println("log length: " + params[0]);

         byte[] abInfoLog = new byte[params[0]];
         gl2.glGetProgramInfoLog(programID, params[0], params, 0, abInfoLog, 0);
         System.err.println( new String(abInfoLog) );      
         System.exit(0);
      }       
      return true;   
   }
   
   
   // Shader specific variables and containers
   public int programID;
   public Vector<Integer> vertexShaderList = new Vector<Integer>();
   public Vector<Integer> geometryShaderList = new Vector<Integer>();
   public Vector<Integer> fragmentShaderList = new Vector<Integer>();
}
