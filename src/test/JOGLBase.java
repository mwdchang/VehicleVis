package test;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;


/////////////////////////////////////////////////////////////////////////////////
// Base class for JOGL based tests
// I dont' know why I can't get better than GL3.....not that it really 
// matters since it is unlikely going to use extensions for my test
//
// Each child class need to implement the display method
// and a main() method that invokes the run() method
//
// For example:
//    public static void main(String args[]) {
//       Child c = new Child();
//       c.run("child program", 500, 500);
//    }
// 
/////////////////////////////////////////////////////////////////////////////////
public abstract class JOGLBase implements GLEventListener {

   public GLCanvas canvas;
   
   public void run() {
      run("Demo", 600, 600);    
   }
   
   public void run(String title) {
      run(title, 600, 600);   
   }
   
   public void run(String title, int width, int height) {
      run(title, width, height, 0);   
   }
   
   public void run(String title, int width, int height, int pause) {
      this.title = title;
      GLProfile profile = GLProfile.get(GLProfile.GL3bc); 
      GLCapabilities capabilities = new GLCapabilities(profile);
      
      canvas = new GLCanvas(capabilities);     
      canvas.setSize( width, height );
      canvas.addGLEventListener( this );
     
      frame = new JFrame(title);
      frame.getContentPane().add( canvas );
      frame.setSize( frame.getContentPane().getPreferredSize());
      frame.setUndecorated(this.unDecorated);
      
      
      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension sc = tk.getScreenSize();
      if (this.sendToNextScreen == true) {
         frame.setLocation((int)sc.getWidth()+1, 0); // Just to the next screen
      }
      
      if (this.isMaximized == true) {
         frame.setExtendedState( frame.getExtendedState() | JFrame.MAXIMIZED_BOTH );
      } else {
         frame.setExtendedState( JFrame.NORMAL );
      }
      frame.setVisible(true);
     
      
      frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.out.println("Shutting down");   
            System.exit(0);
         }
      });
      
      
     canvas.requestFocusInWindow();
     canvas.requestFocus();
     
      
      long updateFrequency = 1000;
      long currentTime = 0;
      long lastTime = System.currentTimeMillis();
      int fps = 0;
      
      
      // Rendering loop
      while (true) {
         canvas.display();    
         currentTime = System.currentTimeMillis();
         fps++;
         
         // Crude FPS counter
         if (currentTime - lastTime >= updateFrequency) {
            frame.setTitle(this.title + "\tFPS: "  + fps);
            fps = 0;
            lastTime = currentTime; 
         }         
         
         // Experimental pause
         if (pause > 0) {
            try { Thread.sleep(pause); } catch(Exception e) {}      
         }
         
      }
      
   }
   
   
   
   public abstract void display(GLAutoDrawable arg0); 

   
   @Override
   public void dispose(GLAutoDrawable arg0) {
      System.out.println("All Done...........");
   }

   @Override
   public void init(GLAutoDrawable glDrawable) {
       GL2 gl2 = glDrawable.getGL().getGL2();      
       winHeight = glDrawable.getHeight();
       winWidth  = glDrawable.getWidth();
      
       gl2.glShadeModel(GL2.GL_SMOOTH);
       gl2.glClearColor(0, 0, 0, 0);
       gl2.glEnable(GL2.GL_DEPTH_TEST);
       gl2.glDepthFunc(GL2.GL_LESS);
       gl2.glClearDepth(1.0f);
   }

   @Override
   public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
      GL2 gl2 = glDrawable.getGL().getGL2();   
      
      winHeight = glDrawable.getHeight();
      winWidth  = glDrawable.getWidth();
      
      float aspect = (float)width / (float)height;
      gl2.glMatrixMode(GL2.GL_PROJECTION); 
      gl2.glLoadIdentity();
      glu.gluPerspective(30.0f, aspect, 1.0, 2000.0);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
   }
   
   
   // Basic clearing wrapper for depth and colour buffer
   public void basicClear(GL2 gl2) {
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);   
      gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);   
      gl2.glLoadIdentity();
   }
   
   // Standard ESC register
   public void registerStandardExit(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
         System.out.println("Shutting down");
         System.exit(0);
      }
   }
   
   
   // Maximize current frame to full screen
   public void maximizeFrame() {
      
      if (this.isMaximized == false) {
         frame.setExtendedState( frame.getExtendedState() | JFrame.MAXIMIZED_BOTH );
      } else {
         frame.setExtendedState( JFrame.NORMAL );
      }
      isMaximized = !isMaximized;
      
      
      /*
      if (isMaximized == false) {
         canvas.removeNotify();
         frame.removeNotify();
         frame.setExtendedState( frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
         frame.setUndecorated(true);
         frame.addNotify();
         canvas.addNotify();
      } else {
         canvas.removeNotify();
         frame.removeNotify();
         frame.setExtendedState( JFrame.NORMAL);
         //frame.setUndecorated(false);
         frame.addNotify();
         canvas.addNotify();
      }
      */
      
   }
   

   // Control
   public JFrame frame;
   public float winHeight;
   public float winWidth;
   public static GLU glu = new GLU();
   public String title = "";
   public boolean isMaximized = false;
   public boolean unDecorated = true;
   public boolean sendToNextScreen = false; 
}
