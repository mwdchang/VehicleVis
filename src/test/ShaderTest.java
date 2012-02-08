package test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import util.GLSLUtil;
import util.ShaderObj;

// Just a class to test for successful shader compilation
public class ShaderTest extends JOGLBase implements KeyListener {

   public static void main(String args[]) {
      ShaderTest test = new ShaderTest();
      test.run("Test", 400, 400);
   }
   
   @Override
   public void display(GLAutoDrawable arg0) {
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);   
      GL2 gl2 = a.getGL().getGL2();
      
      prog = new ShaderObj();
      prog.createShader(gl2, "src\\Shader\\dual_peeling_peel_vertex.glsl", GL2.GL_VERTEX_SHADER);
      prog.createShader(gl2, "src\\Shader\\shade_vertex.glsl", GL2.GL_VERTEX_SHADER);
      
      prog.createShader(gl2, "src\\Shader\\dual_peeling_peel_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
      prog.createShader(gl2, "src\\Shader\\shade_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
      prog.createProgram(gl2);
      
      
      //gl2.glLinkProgram(shader_program);
      //GLSLUtil.checkLinkAndValidationErrors(gl2, shader_program);      
      
      this.canvas.addKeyListener(this);
      
   }
   
   
   ShaderObj prog;
   int vertex_shader;
   int fragment_shader;
   
   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);    
   }

   @Override
   public void keyReleased(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
   }

}
