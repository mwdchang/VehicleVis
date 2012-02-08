package exec;

import gui.FilterTask;
import gui.LegendTask;
import gui.SaveLoadTask;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.ModelRenderer;

import util.ALogger;
import util.DWin;
import datastore.CacheManager;
import datastore.SSM;

/////////////////////////////////////////////////////////////////////////////////
// Main driver for project
// Initializes the graphic and logical entities 
/////////////////////////////////////////////////////////////////////////////////
public class ProjectDriver {
   
   public static void main(String args[]) {
      
      // Get the on-disk and run time database cache out of the way....
      CacheManager.instance(); 
      CacheManager.instance().initSystem();
      
      
      // Fetch and create profile
      //GLProfile profile = GLProfile.get(GLProfile.GL4);
      //System.out.println(GLProfile.glAvailabilityToString());
      //GLProfile profile = GLProfile.get(GLProfile.GL3bc);
      GLProfile profile = GLProfile.getMaxProgrammable();
      DWin.instance().debug(GLProfile.glAvailabilityToString());
      
      GLCapabilities capabilities = new GLCapabilities(profile);
      capabilities.setHardwareAccelerated(true);
      
      
      // Create the rendering items
      GLRenderer renderer = new GLRenderer();
      
      // Add the 3D model viewer
      ModelRenderer model_renderer = new ModelRenderer();
      renderer.renderTaskList.add(model_renderer);
      
      // Add a filtering task
      FilterTask filter_task = new FilterTask();
      renderer.renderTaskList.add( filter_task );
      
      // Add the legend rendering task
      LegendTask legend_task = new LegendTask();
      renderer.renderTaskList.add( legend_task);
      
      // Add save/load
      SaveLoadTask save_task = new SaveLoadTask();
      renderer.renderTaskList.add( save_task );
      
      
      // Create event manager instance
      EventManager eventManager = new EventManager();
      
      // Create a canvas for rendering  
      GLCanvas canvas = new GLCanvas(capabilities);
      canvas.setAutoSwapBufferMode(false); // Turn off autoswap
      canvas.setSize(900, 900);
      canvas.addGLEventListener( renderer );
      
          
      // Register event listeners
      canvas.addKeyListener(eventManager);
      canvas.addMouseListener(eventManager);
      canvas.addMouseMotionListener(eventManager);
      canvas.addMouseWheelListener(eventManager);
      
      // Create a Java rendering context
      JFrame frame = new JFrame("");
      
      JPanel panel = new JPanel(new BorderLayout());
      
      panel.add(canvas, BorderLayout.CENTER);
      
      //frame.getContentPane().add(canvas);
      frame.getContentPane().add(panel);
      frame.setSize( frame.getContentPane().getPreferredSize() );
      frame.setUndecorated(false);
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
         // Temporary update logic goes in here
         if (SSM.instance().dirty == 1) {
            model_renderer.resetData();
            
            // Reset panel if necessary
            SSM.instance().dirty = 0;
         }
         
         if (SSM.instance().currentFocusLayer == SSM.UI_LAYER) {
            filter_task.update(SSM.instance().mouseX);   
         }
         
         // Trigger the range slider to update, under two conditions
         // 1) The user drags the date slider indicator
         // 2) The user loads a saved state
         if (SSM.instance().l_mousePressed == false &&
             SSM.instance().currentFocusLayer == SSM.UI_LAYER) {
            SSM.instance().currentFocusLayer = 0;
            filter_task.unfocus();
            SSM.instance().l_mousePressed = false; 
            //SSM.instance().currentState = SSM.STATE_NORMAL;
            //SSM.instance().currentFocusLayer = 0;
         }
         if (SSM.instance().dirtyLoad == 1) {
         	SSM.instance().dirtyLoad = 0;
         	filter_task.loadFromSSM();
         }
         if (SSM.instance().dirtyDateFilter == 1) {
            SSM.instance().dirtyDateFilter = 0;
            filter_task.loadFromSSM(); 
         }
         
         canvas.update(canvas.getGraphics());
         
         currentTime = System.currentTimeMillis();
         fps++;
         if (currentTime - lastTime >= updateFrequency) {
            frame.setTitle(" Project V5.0 - FPS: " + fps);
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
}
