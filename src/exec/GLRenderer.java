package exec;

import gui.DCTip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import model.DCColour;
import model.DCTriple;

import util.DCCamera;
import util.DWin;
import util.GraphicUtil;

import datastore.SSM;
import datastore.SchemeManager;

/////////////////////////////////////////////////////////////////////////////////
// GLRenderer is responsible for rendering all OpenGL related tasks
// It sets up the initial global initialization for the 3D scene, each task
// is responsible for its transformations and opengl state.
/////////////////////////////////////////////////////////////////////////////////
public class GLRenderer implements GLEventListener {
   
   public Vector<RenderTask> renderTaskList = new Vector<RenderTask>();
   
   // For altering projection mode (perspective and orthonormal) in render()
   public int windowHeight = 0;
   public int windowWidth = 0;
   public double windowAspect = 0.0;
   
   // Renders a test scene for sanity check
   public void renderTest(GL2 gl2) {
      gl2.glPushMatrix();   
         gl2.glBegin(GL2.GL_TRIANGLES);
            gl2.glColor3f(1.0f, 0.0f, 0.0f);
            gl2.glVertex3f(0.0f, 0.0f, 0.0f);
            gl2.glVertex3f(1.0f, 0.0f, 0.0f);
            gl2.glVertex3f(1.0f, 1.0f, 0.0f);
         gl2.glEnd();
      gl2.glPopMatrix();
   }
   
   
   @Override
   public void display(GLAutoDrawable glDrawable) {
      GL2 gl2 = glDrawable.getGL().getGL2();   
      
      
      // Clear the scene
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
      gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Start the scene
      ////////////////////////////////////////////////////////////////////////////////
      DCTip.visible = false;
      //gl2.glPushMatrix();
         for (int i=0; i < renderTaskList.size(); i++) {
            renderTaskList.elementAt(i).render(gl2);
         }
      //gl2.glPopMatrix();
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // There are certain checks that would require the openGL context, we need to do
      // these here as we cannot grab the context in another thread (as far as I know)
      // 1) Click / Picking
      ////////////////////////////////////////////////////////////////////////////////
      //if (SSM.instance().currentState == SSM.MOUSE_LPRESS) { 
      // ((SSM.instance().mouseState & SSM.MOUSE_LCLICK) == SSM.MOUSE_LCLICK ) {
         //for (int i=0; i < renderTaskList.size(); i++) {
         //renderTaskList.elementAt(0).picking(gl2);
         //}
         //SSM.instance().currentState = SSM.STATE_NORMAL;   
      //
      if ( (SSM.instance().l_mouseClicked || SSM.instance().l_mousePressed) &&
            SSM.instance().lensSelected() == 0 ) {
            //SSM.instance().magicLensSelected == 0) {
         for (int i=0; i < renderTaskList.size(); i++) {
            renderTaskList.elementAt(i).picking(gl2);
         }
         SSM.instance().l_mouseClicked = false;
      }
      
      
      
      if (SSM.instance().captureScreen) {
         String s = SSM.instance().now();
         GraphicUtil.screenCap(gl2, s);
         DWin.instance().debug("Saving Screen Cap : " + s);
         SSM.instance().captureScreen = false;
      }
      
      // Because we can...
      gl2.glClearColor( SchemeManager.gl_clear.r, SchemeManager.gl_clear.g, SchemeManager.gl_clear.b, SchemeManager.gl_clear.a);
      
      glDrawable.swapBuffers();
      
   }

   
   @Override
   public void dispose(GLAutoDrawable glDrawable) {
      System.out.println("Existing GLRenderer");
   }

   
   @Override
   public void init(GLAutoDrawable glDrawable) {
      GL2 gl2 = glDrawable.getGL().getGL2();
      
      DWin.instance().debug( gl2.glGetString(GL2.GL_VENDOR));
      DWin.instance().debug( gl2.glGetString(GL2.GL_VERSION));
      DWin.instance().debug( gl2.getGL2().glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));
      
      
      int screenDimension[][] = GraphicUtil.getScreenResolution();
      if (screenDimension.length <= 0) {
      	System.out.println("Unable to fetch screen device dimension...exiting...");
      }
      SSM.instance().LEN_TEXTURE_WIDTH  = screenDimension[0][0];
      SSM.instance().LEN_TEXTURE_HEIGHT = screenDimension[0][1];
      
      
      gl2.glShadeModel(GL2.GL_SMOOTH);
      gl2.glClearColor( SchemeManager.gl_clear.r, SchemeManager.gl_clear.g, SchemeManager.gl_clear.b, SchemeManager.gl_clear.a);
      
//      gl2.glEnable(GL2.GL_DEPTH_TEST);
      gl2.glDepthFunc(GL2.GL_LEQUAL);
      gl2.glClearDepth(1.0f);
      
      // HInts
      gl2.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
      gl2.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
      gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
      
      // Disable cull face - models may or may not be closed
      //gl2.glEnable(GL2.GL_CULL_FACE);
      //gl2.glCullFace(GL2.GL_BACK);
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);      
      
      
      // Check a few openGL specific things
      int maxTex[] = new int[1];  
      gl2.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, maxTex, 0);
      DWin.instance().error("Maximum supported texture size is : " + maxTex[0]);
      
      
      // Initialize the rendering items
      for (int i=0; i < renderTaskList.size(); i++) {
         renderTaskList.elementAt(i).init(gl2);
      }
   }
   

   
   @Override
   public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
      GL2 gl2 = glDrawable.getGL().getGL2();
      if (height <=0) height = 1;
      
      windowHeight = height;
      windowWidth = width;
      windowAspect = (float)windowWidth / (float)windowHeight; 
      
      SSM.instance().windowHeight = windowHeight;
      SSM.instance().windowWidth  = windowWidth;
      SSM.instance().refreshMagicLens = true;
      SSM.instance().refreshOITBuffers = true;
      SSM.instance().refreshOITTexture = true;
      SSM.instance().refreshGlowTexture = true;
      SSM.instance().dirtyGL = 1;
      
      gl2.glViewport(0, 0, width, height);
      /*
      float aspect = (float)width/(float)height;
      gl2.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
      gl2.glLoadIdentity();
      glu.gluPerspective(30.0f, aspect, 1.0, 1000.0);
      gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      gl2.glLoadIdentity();
      */
   }
   
   public static GLU glu = new GLU();

}
