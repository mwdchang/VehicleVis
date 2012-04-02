package datastore;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.media.opengl.GL2;

import util.DWin;


import model.DCModel;
import model.PlyLoader;
import model.WaveFrontLoader;

/////////////////////////////////////////////////////////////////////////////////
// Model Manager
//    Holds and manages all 3D models used in the project
/////////////////////////////////////////////////////////////////////////////////
public class MM {
   
   public static void main(String args[]) {
      MM.instance();
   }
   
   
   
   // List of all model files
   //static String[] models = { "Foobar.obj", "Debug.obj", "demoModel.obj" };
//   static String[] models = { "Foobar.obj", "demoModel.obj" };
//   static String[] models = { "Foobar.obj", "newBMW.obj" };
//   static String[] models = { "newBMW.obj", "bunny.ply"};
   static String[] models = { "newBMW_Mar.obj" };
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Load and initialize the models and calculable geometry
   ////////////////////////////////////////////////////////////////////////////////
   public void loadModels() {
      try {
         modelList = new DCModel[models.length];
         for (int i=0; i < models.length; i++) {
            
            if (models[i].endsWith(".obj")) {
               modelList[i] = WaveFrontLoader.loadWaveFrontObj(Const.MODEL_PATH + models[i]);
            } else if (models[i].endsWith(".ply")) {
               modelList[i] = PlyLoader.loadPly(Const.MODEL_PATH + models[i]); 
            } else {
               System.err.println("Unsupported format :" + models[i]);    
               System.exit(0);
            }
            System.out.println("Loading......" + models[i]);
            initModelData( modelList[i]);
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0); 
      }
      currentModel = modelList[0]; 
      modelIndex = 0;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize GPU related things 
   ////////////////////////////////////////////////////////////////////////////////
   public void initGPU(GL2 gl2) {
      for (int i=0; i < models.length; i++) {
         initModelGraphics(gl2, modelList[i]);
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize the VBO buffers
   ////////////////////////////////////////////////////////////////////////////////
   public void initModelGraphics(GL2 gl2, DCModel model) {
      Enumeration<String> keys = model.componentTable.keys();
      while(keys.hasMoreElements()) {
         String k = keys.nextElement();
         //DWin.instance().debug(k + " Creating vbo for adjacency (6v) format");
         model.componentTable.get(k).createBuffersAdj(gl2);
         //DWin.instance().debug(k + " Creating vbo for normal format");
         model.componentTable.get(k).createBuffers(gl2);
         
         // OIT
         model.componentTable.get(k).createBuffersOIT(gl2);
      }
      System.out.println("Trying to clean up......calling GC");
      System.gc();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Make sure the geometry are ready to use
   ////////////////////////////////////////////////////////////////////////////////
   public void initModelData(DCModel model) {
      model.resize(10.0f);
      model.init();
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Swap to the next model
   ////////////////////////////////////////////////////////////////////////////////
   public void nextModel() {
      modelIndex = (++modelIndex ) % models.length;
      currentModel = modelList[ modelIndex ];
   }
   
   
   public static MM instance() {
      if (inst == null) inst = new MM();      
      return inst;
   }
   
   
   // Constructor
   protected MM() {
      //loadModels();   
   }
   

   private static MM inst;
   public DCModel modelList[];
   public static DCModel currentModel;
   public int modelIndex = 0;
}
