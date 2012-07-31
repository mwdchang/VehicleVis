package exec;

import gui.DomainFilterTask;
import gui.FilterTask;
import gui.LegendTask;
import gui.QuestionTask;
import gui.SaveLoadTask;

import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import model.DCTriple;
import model.ModelRenderer;

import touch.VFeedbackTask;
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
   
   ModelRenderer model_task = new ModelRenderer();
   FilterTask filter_task = new FilterTask();
   LegendTask legend_task = new LegendTask();
   SaveLoadTask save_task = new SaveLoadTask();
   DomainFilterTask domain_task = new DomainFilterTask();
   QuestionTask question_task = new QuestionTask();
   VFeedbackTask feedback_task = new VFeedbackTask();
   
   
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
      
 
      for (DCTriple point : SSM.dragPoints.values()) {
         filter_task.update(point.x);
      }
      
      // Trigger the range slider to update, under two conditions
      // 1) The user drags the date slider indicator
      // 2) The user loads a saved state
      //if (SSM.instance().l_mousePressed == false && filter_task.deferredRefresh) {
      if (SSM.l_mousePressed == false && filter_task.deferredRefresh && SSM.useTUIO == false) {
         System.out.println("about to unbind...");
         filter_task.unfocus();
         filter_task.deferredRefresh = false;
      }
      if (SSM.checkDragEvent == true) {
         filter_task.unfocus();
         filter_task.deferredRefresh = false;
         SSM.checkDragEvent = false;
      }
      
      
      if (SSM.dirtyLoad == 1) {
         SSM.dirtyLoad = 0;
         filter_task.loadFromSSM();
      }
      if (SSM.dirtyDateFilter == 1) {
         SSM.dirtyDateFilter = 0;
         filter_task.loadFromSSM(); 
      }      
      
      
      // Update logic goes in here
      // Note there is a precedence order here, 
      // model_renderer.resetData() should always go first because
      // it resets the cache
      if (SSM.dirty == 1) {
         model_task.resetData();
         domain_task.resetData();
         SSM.dirty = 0;
      }      
      
      // Clear the scene
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
      gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Start the scene
      ////////////////////////////////////////////////////////////////////////////////
      //DCTip.visible = false;
      model_task.render(gl2);
      filter_task.render(gl2);
      legend_task.render(gl2);
      domain_task.render(gl2);
      question_task.render(gl2);
      feedback_task.render(gl2);
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // There are certain checks that would require the openGL context, we need to do
      // these here as we cannot grab the context in another thread (as far as I know)
      // 1) Click / Picking -> order matters
      ////////////////////////////////////////////////////////////////////////////////
      if ( SSM.l_mouseClicked) {
         //for (int i=0; i < renderTaskList.size(); i++) {
         //   renderTaskList.elementAt(i).picking(gl2);
         //}
         for (int i=0; i < SSM.pickPoints.size(); i++) {
            DCTriple point = SSM.pickPoints.elementAt(i);
            domain_task.picking(gl2, point.x, point.y);
            filter_task.picking(gl2, point.x, point.y);
            legend_task.picking(gl2, point.x, point.y);
            question_task.picking(gl2, point.x, point.y);
            model_task.picking(gl2, point.x, point.y);
         }
         SSM.l_mouseClicked = false;
         SSM.pickPoints.clear();
      }
      synchronized(SSM.dragPoints){
         if (SSM.dragPoints.size() > 0) {
            for (DCTriple p : SSM.dragPoints.values())  {  
               filter_task.pickingDrag(gl2, p.x, p.y);
            }
            //SSM.stopPicking = 0;
         }
      }
      SSM.stopPicking = 0;
      
      
      if (SSM.captureScreen) {
         String s = SSM.instance().now();
         GraphicUtil.screenCap(gl2, s);
         DWin.instance().debug("Saving Screen Cap : " + s);
         SSM.captureScreen = false;
      }
      
      
      // Because we can...
      //gl2.glClearColor( SchemeManager.gl_clear.r, SchemeManager.gl_clear.g, SchemeManager.gl_clear.b, SchemeManager.gl_clear.a);
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
      SSM.LEN_TEXTURE_WIDTH  = screenDimension[0][0];
      SSM.LEN_TEXTURE_HEIGHT = screenDimension[0][1];
      
      SSM.windowWidth  = glDrawable.getWidth();
      SSM.windowHeight = glDrawable.getHeight();
      
      
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
      //for (int i=0; i < renderTaskList.size(); i++) {
      //   renderTaskList.elementAt(i).init(gl2);
      //}
      model_task.init(gl2);
      filter_task.init(gl2);
      legend_task.init(gl2);
      domain_task.init(gl2);
      question_task.init(gl2);
      feedback_task.init(gl2);
      
      // Hack test
      if (SSM.useScenario) {
         question_task.q.firstElement().set();
      }
      
      // Attemp to free some memory after all initializations
      System.gc();
   }
   

   
   @Override
   public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
      GL2 gl2 = glDrawable.getGL().getGL2();
      if (height <=0) height = 1;
      
      windowHeight = height;
      windowWidth = width;
      windowAspect = (float)windowWidth / (float)windowHeight; 
      
      SSM.windowHeight = windowHeight;
      SSM.windowWidth  = windowWidth;
      SSM.refreshMagicLens = true;
      SSM.refreshOITBuffers = true;
      SSM.refreshOITTexture = true;
      SSM.refreshGlowTexture = true;
      SSM.dirtyGL = 1;
      
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
