package model;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import util.BSPTree;
import util.GraphicUtil;

public class DCModel extends DCObj {
   
//   public static void main(String args[]) {
//      try {
//         DCModel model = WaveFrontLoader.loadWaveFrontObj(Const.MODEL_FILE);   
//         model.init();
//      } catch (Exception e) {
//         e.printStackTrace(); 
//      }
//   }
//   
        
   ////////////////////////////////////////////////////////////////////////////////
   // Default DCModel
   ////////////////////////////////////////////////////////////////////////////////
   public DCModel(){  
      // Initialize component tables
      //components = new Hashtable<String, Vector<DCFace>>();
      componentTable = new Hashtable<String, DCComponent>();
      componentTableById = new Hashtable<Integer, String>();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Resize portion to size
   ////////////////////////////////////////////////////////////////////////////////
   public void resize(float size) {
      System.out.println("Resizeing model ... " + size);
      float g_maxx, g_maxy, g_maxz;
      float g_minx, g_miny, g_minz;
      g_maxx = g_maxy = g_maxz = g_minx = g_miny = g_minz = 0.0f;
      
      // Get the global min and max to calculate expansion/contraction ratio
      Enumeration<String> e = componentTable.keys();
      while (e.hasMoreElements()) {
         DCComponent comp = componentTable.get( e.nextElement() );
         if (comp.boundingBox.minx < g_minx) g_minx = comp.boundingBox.minx;
         if (comp.boundingBox.miny < g_miny) g_miny = comp.boundingBox.miny;
         if (comp.boundingBox.minz < g_minz) g_minz = comp.boundingBox.minz;
         
         if (comp.boundingBox.maxx > g_maxx) g_maxx = comp.boundingBox.maxx;
         if (comp.boundingBox.maxy > g_maxy) g_maxy = comp.boundingBox.maxy;
         if (comp.boundingBox.maxz > g_maxz) g_maxz = comp.boundingBox.maxz;
      }
      
      float sizeX = g_maxx - g_minx;
      float sizeY = g_maxy - g_miny;
      float sizeZ = g_maxz - g_minz;
      float factor = size / ((sizeX + sizeY + sizeZ)/3.0f);
      
      
      // Enumerate through the components and call individual resize
      e = componentTable.keys();
      while (e.hasMoreElements()) {
         DCComponent comp = componentTable.get( e.nextElement() );
         comp.resize( factor );
      }
      
      // Initialize the AABB
      mbox.maxx = g_maxx*factor;
      mbox.maxy = g_maxy*factor;
      mbox.maxz = g_maxz*factor;
      
      mbox.minx = g_minx*factor;
      mbox.miny = g_miny*factor;
      mbox.minz = g_minz*factor;
   }
   
   
   
   // Initialize various other information not provided in the model file
   public void init() {
   	// 1st pass to calc attributes
      Enumeration<String> e = componentTable.keys();         
      while (e.hasMoreElements()) {
         String compStr = e.nextElement();
         componentTable.get(compStr).recalculateAttribute();
      }
      
      // Calculate a level based on occlusion bounding boxes
      Enumeration<String> occ1 = componentTable.keys();
      while (occ1.hasMoreElements()) {
         DCComponent comp1 = componentTable.get( occ1.nextElement() );   
         Enumeration<String> occ2 = componentTable.keys();
         while (occ2.hasMoreElements()) {
            DCComponent comp2 = componentTable.get( occ2.nextElement() );   
            if (comp1.cname.equals(comp2.cname)) continue; // don't count self  
            
            if (comp1.boundingBox.contains(comp2.boundingBox)) {
               comp2.level ++;      
            }
            
         }
      }
      
      Enumeration<String> ee = componentTable.keys();
      while (ee.hasMoreElements()) {
         DCComponent comp = componentTable.get( ee.nextElement());          
         System.out.println(comp.cname + " " + comp.level); 
      }
      
      // Create a secondary table, indexed by the integer group id, this is just to do a faster
      // search, we will use the primary componentTable for all other work
      ee = componentTable.keys();
      while (ee.hasMoreElements()) {
         DCComponent comp = componentTable.get(ee.nextElement());   
         componentTableById.put(comp.id, comp.baseName);
      }
     
      
      
      
      // Calculate complete occlusion
      /*
      System.out.println("Calc occlusion list");
      Enumeration<String> e1 = componentTable.keys();         
      
      while (e1.hasMoreElements()) {
         String base = e1.nextElement();
         DCComponent baseComp = componentTable.get(base);
//         System.out.println( base + " " + baseComp.boundingBox);
         
         Enumeration<String> e2 = componentTable.keys();         
         while (e2.hasMoreElements()) {
            String tmp = e2.nextElement();
            DCComponent tmpComp = componentTable.get(tmp);
            if (base.equals(tmp)) continue; // Do not do self include
            if ( baseComp.boundingBox.contains(tmpComp.boundingBox)) {
               System.out.println(base + " completedly occludes " + tmp);
               baseComp.occlusionList.add(tmpComp);              
            }
         }
      }
      */
   }
   
   

   // Slow slow slow
   public float[] getMaxMinScreenX(GL2 gl2) {
      //float maxx = Float.MIN_VALUE;
      //float minx = Float.MAX_VALUE;
      
      maxx = Float.MIN_VALUE;
      minx = Float.MAX_VALUE;
      
      float coord[];
      
      // "lower"
      coord = GraphicUtil.getProject(gl2, mbox.minx, mbox.miny, mbox.minz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.minx, mbox.miny, mbox.maxz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.maxx, mbox.miny, mbox.maxz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.maxx, mbox.miny, mbox.minz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      // "upper"
      coord = GraphicUtil.getProject(gl2, mbox.minx, mbox.maxy, mbox.minz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.minx, mbox.maxy, mbox.maxz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.maxx, mbox.maxy, mbox.maxz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      coord = GraphicUtil.getProject(gl2, mbox.maxx, mbox.maxy, mbox.minz);
      if (coord[0] > maxx) maxx = coord[0];
      if (coord[0] < minx) minx = coord[0];
//      System.out.println("\t\t" + coord[0]);
      
      return new float[]{maxx, minx};
   }
   
//   public void setProjectedCoord(GL2 gl2) {
//      float buffer[];
//      float viewBuffer[], projBuffer[];
//      
//      buffer = new float[16];   
//      gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
//      projBuffer = buffer;
//      
//      buffer = new float[16];
//      gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
//      viewBuffer = buffer;
//      
//      int viewport[] = new int[4];
//      gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);             
//      
//      float wincoord[] = new float[3];      
//      
//      glu.gluProject(
//            mbox.maxx,
//            mbox.maxy,
//            mbox.maxz,
//            viewBuffer, 0,
//            projBuffer, 0,
//            viewport, 0,
//            wincoord, 0
//            );         
//      projbox.maxx = wincoord[0];
//      projbox.maxy = wincoord[1];
//      projbox.maxz = wincoord[2];
//      
//      
//      glu.gluProject(
//            mbox.minx,
//            mbox.miny,
//            mbox.minz,
//            viewBuffer, 0,
//            projBuffer, 0,
//            viewport, 0,
//            wincoord, 0
//            );         
//      projbox.minx = wincoord[0];
//      projbox.miny = wincoord[1];
//      projbox.minz = wincoord[2];
//      
//   }
   
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Kick off animation
   ////////////////////////////////////////////////////////////////////////////////
   public void startAnimation() {
      Enumeration<String> s = componentTable.keys();
      while (s.hasMoreElements()) {
         String k = s.nextElement();
         DCComponent c = componentTable.get(k);
         if (c.canimator != null) {
            c.canimator.start();   
         }
//         if (c.lineAnimator != null) {
//            c.lineAnimator.start(); 
//         }
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Stop animation
   ////////////////////////////////////////////////////////////////////////////////
   public void stopAnimation() {
      Enumeration<String> s = componentTable.keys();
      while (s.hasMoreElements()) {
         String k = s.nextElement();
         DCComponent c = componentTable.get(k);
         if (c.canimator != null) {
            c.canimator.stop();   
         }
//         if (c.lineAnimator != null) {
//            c.lineAnimator.stop();
//         }
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check if any component animator is running
   ////////////////////////////////////////////////////////////////////////////////
   public boolean isAnimationRunning() {
      Enumeration<String> s = componentTable.keys();
      while (s.hasMoreElements()) {
         String k = s.nextElement();
         DCComponent c = componentTable.get(k);
         if (c.canimator != null)
            if (c.canimator.isRunning()) return true;
//         if (c.lineAnimator != null)
//            if (c.lineAnimator.isRunning()) return true;
      }
      return false;  
   }
   

   
   // Debugging
   public void printInfo() {
//      Enumeration<String> e = components.keys();
//      while (e.hasMoreElements()) {
//         String key = e.nextElement();
//         Vector<DCFace> tmp = components.get(key);
//         System.out.println( "[" + key + "] ==> " + tmp.size());
//      }
   }
   
   
   public int getTotalVertices() {
      int result = 0;
      for (DCComponent comp: componentTable.values()) {
         result +=  comp.faceList.size()*3;   
      }
      return result;
   }
   

   
   // A name to component table
   public Hashtable<String, DCComponent> componentTable = null;
   
   // A id to component table
   public Hashtable<Integer, String> componentTableById = null;
   
   public BSPTree root;
   public DCBoundingBox mbox = new DCBoundingBox();
   public GLU glu = new GLU();
   
   
   // Keep a reference to the currently selected component, for ease of access
   public static DCComponent currentComponent = null;
   
   
   // Projection variables
   public float maxx = 0;
   public float minx = 0;
}
