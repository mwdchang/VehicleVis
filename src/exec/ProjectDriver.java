package exec;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;

import TUIO.TuioClient;

import touch.TUIOListener;
import util.ALogger;
import util.DWin;
import datastore.CacheManager;
import datastore.Const;
import datastore.SSM;

/////////////////////////////////////////////////////////////////////////////////
// Main driver for project
// Initializes the graphic and logical entities 
/////////////////////////////////////////////////////////////////////////////////
public class ProjectDriver {
   
   public static void main(String args[]) {
      Const.doRunTimeCheck();
      SSM.instance();
      
      
      
      // Get the on-disk and run time database cache out of the way....
      CacheManager.instance(); 
      
      // There are some parameters to parse...
      if (args.length > 0) {
         if (args.length == 2) {
            System.out.println("getting dates");
            CacheManager.timeLineStartYear = Integer.parseInt(args[0]);   
            CacheManager.timeLineEndYear   = Integer.parseInt(args[1]);
         } else if (args.length == 3) {    
            System.out.println("getting dates and filters");
            CacheManager.timeLineStartYear = Integer.parseInt(args[0]);   
            CacheManager.timeLineEndYear   = Integer.parseInt(args[1]);
            CacheManager.mfrFilter = args[2];
         } else {
            System.out.println("Parameters are : startYear endYear");
            System.exit(0);   
         }
      }
      
      
      CacheManager.instance().initSystem();
      
      
      // Fetch and create profile
      //GLProfile profile = GLProfile.get(GLProfile.GL4);
      //System.out.println(GLProfile.glAvailabilityToString());
      //GLProfile profile = GLProfile.get(GLProfile.GL3bc);
      GLProfile profile = GLProfile.getMaxProgrammable();
     
      GLCapabilities capabilities = new GLCapabilities(profile);
      capabilities.setHardwareAccelerated(true);
      
      
      // Create the rendering items
      GLRenderer renderer = new GLRenderer();
      
      
      
      // Create event manager instance
      EventManager eventManager = new EventManager();
      
      // Create a canvas for rendering  
      GLCanvas canvas = new GLCanvas(capabilities);
      canvas.setAutoSwapBufferMode(false); // Turn off autoswap
      canvas.setSize(900, 900);
      canvas.addGLEventListener( renderer );
      
          
      // Register event listeners
      if (SSM.useTUIO == true) {
         TuioClient client = new TuioClient();
         client.addTuioListener(new TUIOListener());
         client.connect();
DWin.instance().debug("TUIO is true");         
      } else {
         canvas.addMouseListener(eventManager);
         canvas.addMouseMotionListener(eventManager);
         canvas.addMouseWheelListener(eventManager);
      }
      
      // Key listener is always enabled
      canvas.addKeyListener(eventManager);
      
      
      // Create a Java rendering context
      frame = new JFrame("");
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(canvas, BorderLayout.CENTER);
      
      //frame.getContentPane().add(canvas);
      frame.getContentPane().add(panel);
      frame.setSize( frame.getContentPane().getPreferredSize() );
      
      if (SSM.useFullScreen == true) {
         frame.setUndecorated(true);
         frame.setExtendedState( frame.getExtendedState() | JFrame.MAXIMIZED_BOTH );
      }
      
      
      // Hack test
      /////////////////////////////////////////////////////////////////////////
      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension sc = tk.getScreenSize();
      frame.setLocation((int)sc.getWidth()+1, 0); // Just to the next screen
      //frame.setUndecorated(true);
      frame.setExtendedState( frame.getExtendedState() | JFrame.MAXIMIZED_BOTH );
      /////////////////////////////////////////////////////////////////////////
      // End Hack test
      
      
      // Hack to get rid of cursor
      /////////////////////////////////////////////////////////////////////////
      if (SSM.useTUIO == true) {
         Cursor nullCursor = null;
         Dimension d = tk.getBestCursorSize(1, 1);
         if ( (d.width|d.height) != 0) {
            nullCursor = tk.createCustomCursor(
               new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB),
               new Point(0,0), "nullCursor");
         }
         frame.setCursor(nullCursor); 
      }
      /////////////////////////////////////////////////////////////////////////
      // End Hack to get rid of cursor
      
      
      frame.setVisible(true);
      //frame.requestFocus();
      
      
      canvas.requestFocusInWindow();
      canvas.requestFocus();
      
      // Register window close event
      frame.addWindowListener(new WindowAdapter() { 
         public void windowClosing(WindowEvent e) {
            System.out.println("Shutting down....");
            ALogger.instance().cleanup();
            System.exit(0); 
         }
      });
      
      
     
      //TODO: Create a timer to control update rate and rendering frame rate ?????
      // Start main loop
      long updateFrequency = 1000;
      long currentTime = 0;
      long lastTime = System.currentTimeMillis();
      int fps = 0;
      
      while (true) {
         canvas.update(canvas.getGraphics());
         
         currentTime = System.currentTimeMillis();
         fps++;
         if (currentTime - lastTime >= updateFrequency) {
            //frame.setTitle(" Project V5.0 - FPS: " + fps);
            frame.setTitle("Daniel");
            fps = 0;
            lastTime = currentTime; 
         }
         // Clear any non-lasting mouse status
         //SSM.instance().mouseState &= SSM.MOUSE_CLEAR;
      }
      
   }
   
   // Default constructor
   public ProjectDriver() {
   }
   
   public static JFrame frame;
}
