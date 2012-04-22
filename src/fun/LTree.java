package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.FloatBuffer;
import java.util.Stack;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.jogamp.opengl.util.GLBuffers;

import test.JOGLBase;

public class LTree extends JOGLBase implements KeyListener {
   
   public static void main(String args[]) {
      LTree ltree = new LTree();
      ltree.run("L-System", 700, 700);
   }
   
   public Vector<String> head;
   public Vector<String> body;
   
   StringBuffer sb = new StringBuffer();
   public Vector<Float> tBuffer = new Vector<Float>();
   public Vector<Float> rxBuffer = new Vector<Float>();
   public Vector<Float> ryBuffer = new Vector<Float>();
   public Vector<Float> rzBuffer = new Vector<Float>();
   public Stack<float[]> stack = new Stack<float[]>();
   
   public float viewRadius = 300.0f;
   public float viewAngle = 0;  
   
   GLUquadric cyl;

   
   public StringBuffer replaceRules(StringBuffer sb, Vector<String> head, Vector<String> body, int depth) {
      for (int i=0; i < sb.length(); i++) {
         for (int hidx = 0; hidx < head.size(); hidx++) {
            char c = sb.charAt(i);
            char h = head.elementAt(hidx).charAt(0);
            
            if (c == h) {
               sb = sb.replace(i, i+1, body.elementAt(hidx));
               i += body.elementAt(hidx).length();
               break;
            }
         }
      }
      if (depth < 6) {
         return replaceRules(sb, head, body, ++depth);   
      }
      return sb;   
   }
   
   
   GLU glu = new GLU();
   
   
   public String printFloat(float a[]) {
      String str = "";   
      for (int i=0; i < a.length; i++) str += a[i] + ", ";
      return str; 
   }
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      glu.gluPerspective(30, 1.0, 1.0, 1000);
      
      double xx = Math.cos(viewAngle*3.14f/180.0f)*viewRadius;
      double yy = Math.sin(viewAngle*3.14f/180.0f)*viewRadius;
      viewAngle += 4.0;
      
      //glu.gluLookAt(290, 290, 150, 0, 0, 0, 0, 0, 1);
      glu.gluLookAt(xx, yy, 150, 0, 0, 0, 0, 0, 1);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      //gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      
      int line_index = 0;
      int rot_index = 0;
      
      stack.removeAllElements();
      float f[] = new float[16];
      gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, f, 0);
      stack.push(f);
      
      for (int i=0; i < sb.length(); i++)  {
         char c = sb.charAt(i);   
         
         /*
         gl2.glColor4d(
               ((double)line_index / (double)tBuffer.size())*0.6+0.4,
               ((double)line_index / (double)tBuffer.size())*0.6+0.4,
               ((double)line_index / (double)tBuffer.size())*0.6+0.4,
               0.5);
         */
         gl2.glColor4d(0.7, 0.4 + 0.3*( line_index/(double)tBuffer.size()), 0, 1);
        
         if (c == 'd') {
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glPushMatrix();
            float tmp[] = stack.pop();
            gl2.glMultMatrixf(tmp, 0);
            glu.gluCylinder(cyl, tBuffer.elementAt(line_index)*0.15, tBuffer.elementAt(line_index)*0.15, 0.6, 3, 3);
            gl2.glTranslatef(0.0f, 0.0f, 0.6f);
            
            float f2[] = new float[16];
            gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, f2, 0);
            stack.push(f2);            
            gl2.glPopMatrix();
            line_index++;
            //System.out.println(printFloat(tmp));
            //System.out.println(printFloat(f2));
         } else if (c == 'p' || c == 'q') {
            //System.out.println("---- stack size: "+ stack.size());
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glPushMatrix();
            float tmp[] = stack.pop();
            gl2.glMultMatrixf(tmp, 0);
            if (c == 'p') {
               gl2.glRotatef(rxBuffer.elementAt(rot_index), 1, 0, 0);
               gl2.glRotatef(ryBuffer.elementAt(rot_index), 0, 1, 0);
               gl2.glRotatef(rzBuffer.elementAt(rot_index), 0, 0, 1);
            } else {
               gl2.glRotatef(-rxBuffer.elementAt(rot_index), 1, 0, 0);
               gl2.glRotatef(-ryBuffer.elementAt(rot_index), 0, 1, 0);
               gl2.glRotatef(-rzBuffer.elementAt(rot_index), 0, 0, 1);
            }
            
            float f2[] = new float[16];
            gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, f2, 0);
            stack.push(f2);
            gl2.glPopMatrix();
            rot_index++;
            //System.out.println(printFloat(tmp));
            //System.out.println(printFloat(f2));
         } else if (c == '[') {
            float f2[] = stack.peek();
            //System.out.println(">>> " + printFloat(f2));
            stack.push(f2);
            //System.out.println("stack size: " + stack.size());
         } else if (c == ']') {
            stack.pop();
         } else if (c == 'l') {
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glPushMatrix();
            float tmp[] = stack.peek();
            gl2.glMultMatrixf(tmp, 0);
            
            gl2.glColor4d(0, ((double)i)/(double)sb.length(), 0.2, 1);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glVertex3d(-0.8, -0.8, 0);
               gl2.glVertex3d(-0.8,  0.8, 0);
               gl2.glVertex3d(0.8,   0.8, 0);
               gl2.glVertex3d(0.5,  -0.8, 0);
            gl2.glEnd();
            gl2.glPopMatrix();
         }
      }
      
      
      
   }
   
   
   @Override
   public void init(GLAutoDrawable a) {
      super.init(a);
      this.canvas.addKeyListener(this);
      
      GLU glu = new GLU();
      
      cyl = glu.gluNewQuadric();
      
      head = new Vector<String>();
      body = new Vector<String>();
      
      head.add("x");
      //body.add("d[px]d[qx]pxl");
      body.add("d[px]d[qx]d[px]d[qx]pxl");
      
      head.add("d");
      body.add("dd");
      //body.add("d[px][qx]d");
      
      StringBuffer initialStr = new StringBuffer();
      initialStr.append("x");
      
      sb = this.replaceRules(initialStr, head, body, 1);
      
      // Calculate thickness
      float count=0;
      for (int i=0; i < sb.length(); i++) {
         if (sb.charAt(i) == 'd') {
            count++;   
         } else {
            if (count > 0) {
               for (int x=0; x < count; x++) tBuffer.add(count);
            }
            count = 0;
         }
      }
      
      // Calculate rotation
      float rot_max = 50;
      float rot_min = 10;
      for (int i=0; i < sb.length(); i++) {
         if (sb.charAt(i) == 'p' || sb.charAt(i) == 'q') {
            this.rxBuffer.add( (float)Math.random()*(rot_max-rot_min));         
            this.ryBuffer.add( (float)Math.random()*(rot_max-rot_min));         
            this.rzBuffer.add( (float)Math.random()*(rot_max-rot_min));         
         }
      }
      
      
      
      System.out.println(sb);
      System.out.println(tBuffer.size());
      
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


}
