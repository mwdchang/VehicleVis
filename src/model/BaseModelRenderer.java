package model;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.nio.IntBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import util.ALogger;
import util.DCCamera;
import util.DCUtil;
import util.DWin;
import util.GraphicUtil;
import util.MatrixUtil;
import util.ShaderObj;
import Jama.Matrix;
import TimingFrameExt.DCColourEval;

import com.jogamp.opengl.util.awt.TextRenderer;

import datastore.CacheManager;
import datastore.Const;
import datastore.HierarchyTable;
import datastore.MM;
import datastore.SSM;
import datastore.SchemeManager;
import exec.RenderTask;
import gui.DCTextPanel2;
import gui.Heatmap;
import gui.StatusWindow;

/////////////////////////////////////////////////////////////////////////////////
// This class provides basic function that should be shared among all model rendering classes
// The render and picking methods are declared abstract and must be implemented
/////////////////////////////////////////////////////////////////////////////////
public abstract class BaseModelRenderer implements RenderTask {
   
   public GLU glu = new GLU();
   public TextRenderer textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 22)); 
   
   public DCTextPanel2 dcTextPanel = new DCTextPanel2();
   

   
   
   // Flow effect filter
   public FrameBufferTexture glowTexture;
   public FrameBufferTexture outlineTexture1;
   public FrameBufferTexture outlineTexture2;
   
   
   
   
   @Override
   public abstract void render(GL2 gl2);

   @Override
   public abstract void picking(GL2 gl2, float px, float py);
   
   @Override
   public void init(GL2 gl2) {
      // Initialize modeling data
      try {
         CacheManager.instance(); // Initialize the manager
         MM.instance();
         MM.instance().loadModels();
         
         
         InitDualPeelingRenderTargets(gl2);
         
         gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
         BuildShaders(gl2);
         this.g_quadDisplayList = MakeFullScreenQuad(gl2);
         
         gl2.glDisable(GL2.GL_CULL_FACE);
         gl2.glDisable(GL2.GL_LIGHTING);
         gl2.glDisable(GL2.GL_NORMALIZE);         
         
         gl2.glGenQueries(1, g_queryId, 0);
         
         //DCTip.init(gl2);
         StatusWindow.init(gl2);
         
      } catch (Exception e) { 
         e.printStackTrace(); 
         System.exit(-1);
      }      
   }

   
   ////////////////////////////////////////////////////////////////////////////////  
   // Performs the basic transformation
   // Translation and rotation of object model
   ////////////////////////////////////////////////////////////////////////////////  
   public void basicTransform(GL2 gl2) {
      gl2.glTranslated(0, 0, 0);
      gl2.glRotated(SSM.rotateX, 1, 0, 0);
      gl2.glRotated(SSM.rotateY, 0, 1, 0);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   //
   ////////////////////////////////////////////////////////////////////////////////
   public void renderTextPolar(GL2 gl2, LensAttrib la, float angle, float scale, DCComponent comp, String s) {
      //gl2.glEnable(GL2.GL_BLEND);
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
      gl2.glBindTexture(GL2.GL_TEXTURE_2D, 0);
      float radius = la.magicLensRadius;
      float x;
      float y;
      x = radius * (float)Math.cos(angle);
      y = radius * (float)Math.sin(angle);
      textRenderer.beginRendering(SSM.windowWidth, SSM.windowHeight);
      Rectangle2D rect = textRenderer.getBounds(s);
      
      angle = angle*180.0f/3.14f;
      if (angle < 0) angle += 360.0f;
      
      //if (angle*180.0f/3.14f >=90 && angle*180.0f/3.14f < 270.0f) {
      if (angle >= 90.0f && angle < 270.0f) {
         x =  x -(float) rect.getWidth()*scale;
      }
      
      textRenderer.draw3D(s, la.magicLensX+(int)x, 
            (SSM.windowHeight - la.magicLensY)+(int)y, 
            0.0f, scale);      
      textRenderer.endRendering();
      gl2.glDisable(GL2.GL_TEXTURE_2D);          
      
      
      gl2.glLoadIdentity();
      gl2.glEnable(GL2.GL_BLEND);
      
      if (angle >= 90.0f && angle < 270.0f) {
         x -= comp.cchart.width;
      } else {
         x += rect.getWidth()*scale;
      } 
   
      gl2.glTranslated(la.magicLensX + x , 
                       (SSM.windowHeight - la.magicLensY) + y, 0);
      comp.cchart.renderImmediate(gl2);
      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Standard transformation for model rendering
   ////////////////////////////////////////////////////////////////////////////////
   public void setPerspectiveView(GL2 gl2) {
      float aspect = (float)SSM.windowWidth/ (float)SSM.windowHeight;
      GraphicUtil.setPerspectiveView(gl2, aspect, SSM.fov, 
            1, 1000, 
            DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
      
      //gl2.glRotated(SSM.instance().rotateX, 1, 0, 0);
      //gl2.glRotated(SSM.instance().rotateY, 0, 1, 0);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Perspective with near and far
   ////////////////////////////////////////////////////////////////////////////////
   public void setPerspectiveView(GL2 gl2, float near, float far) {
      float aspect = (float)SSM.windowWidth/ (float)SSM.windowHeight;
      GraphicUtil.setPerspectiveView(gl2, aspect, SSM.fov, 
            near, far, 
            DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
      
   }
   
   public void setOrthonormalView(GL2 gl2) {
      GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
   }
   
   
   public void setOrthonormalView(GL2 gl2, float minx, float maxx, float miny, float maxy) {
      GraphicUtil.setOrthonormalView(gl2, minx, maxx, miny, maxy, -10, 10);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Set the condition to pick in perspective mode
   ////////////////////////////////////////////////////////////////////////////////
   public void startPickingPerspective(GL2 gl2, IntBuffer buffer, float px, float py) {
      GraphicUtil.startPickingPerspective(gl2, buffer, 
            (int)px, (int)py, 
            SSM.windowWidth, SSM.windowHeight, SSM.fov, 
            DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Setup the viewport and the initial naming stack
   ////////////////////////////////////////////////////////////////////////////////
   public void startPickingOrtho(GL2 gl2, IntBuffer buffer, float px, float py) {
      GraphicUtil.startPickingOrtho(gl2, buffer, 
            (int)px, (int)py, 
            SSM.windowWidth, SSM.windowHeight);
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Calculate the hit
   ////////////////////////////////////////////////////////////////////////////////
   public Integer finishPicking(GL2 gl2, IntBuffer buffer) {
      return GraphicUtil.finishPicking(gl2, buffer);
   }   
   
   

   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset the groupOccurrence dataset 
   // using the updated date strings in SSM
   ////////////////////////////////////////////////////////////////////////////////
   public void resetData() {
      
      int startIdx = CacheManager.instance().getDateKey( SSM.startTimeFrame ) == null ? 0:
         CacheManager.instance().getDateKey( SSM.startTimeFrame );
      int endIdx   = CacheManager.instance().getDateKey( SSM.endTimeFrame) == null ? CacheManager.instance().timeLineSize:
         CacheManager.instance().getDateKey( SSM.endTimeFrame);
      
      SSM.startIdx = startIdx;
      SSM.endIdx = endIdx;

      
      
      

      
      
      DWin.instance().error(SSM.startMonth + " " + SSM.endMonth);
      DWin.instance().error("Starting indices: " + startIdx + " " + endIdx );
      
      
     
      
      // Set up the year data for the selected criteria
//      CacheManager.instance().filterYearData = 
//         CacheManager.instance().getFilterYearStatArray(
//               SSM.instance().manufactureAttrib.selected,
//               SSM.instance().makeAttrib.selected,
//               SSM.instance().modelAttrib.selected,
//               SSM.instance().yearAttrib.selected);
//     
      
      
      // Set up the month data for the selected criteria
//      CacheManager.instance().filterMonthData = 
//         CacheManager.instance().getFilterMonthlyStat(SSM.instance().startTimeFrame, SSM.instance().endTimeFrame, 
//               SSM.instance().manufactureAttrib.selected, 
//               SSM.instance().makeAttrib.selected, 
//               SSM.instance().modelAttrib.selected,
//               SSM.instance().yearAttrib.selected);
//      
      
      
      // Reset the occurrence frequency table
      SSM.maxOccurrence = 0;
      SSM.minOccurrence = 0;
      
      
      // Reset the global values
      if (SSM.useAggregate == false) {
          CacheManager.instance().groupOccurrence = 
            CacheManager.instance().getPartOccurrenceFilter(
               startIdx, endIdx, 
               SSM.startMonth,
               SSM.endMonth, 
               SSM.manufactureAttrib.selected, 
               SSM.makeAttrib.selected, 
               SSM.modelAttrib.selected,
               SSM.yearAttrib.selected);
      
          CacheManager.instance().c_groupOccurrence = 
            CacheManager.instance().getPartOccurrenceFilter(
               startIdx, endIdx, 
               SSM.startMonth,
               SSM.endMonth, 
               SSM.c_manufactureAttrib.selected, 
               SSM.c_makeAttrib.selected, 
               SSM.c_modelAttrib.selected,
               SSM.c_yearAttrib.selected);
         
          
          if (SSM.selectedGroup.size() > 0 && SSM.useLocalFocus == true) {
             CacheManager.instance().monthMaximum = 
               CacheManager.instance().getMonthMaximumSelected(
                     SSM.manufactureAttrib.selected, 
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);
             
             CacheManager.instance().c_monthMaximum = 
                CacheManager.instance().getMonthMaximumSelected(
                      SSM.c_manufactureAttrib.selected, 
                      SSM.c_makeAttrib.selected, 
                      SSM.c_modelAttrib.selected,
                      SSM.c_yearAttrib.selected);             
          } else {
             CacheManager.instance().monthMaximum = 
               CacheManager.instance().getMonthMaximum(
                     SSM.manufactureAttrib.selected, 
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);
             
             CacheManager.instance().c_monthMaximum = 
                CacheManager.instance().getMonthMaximum(
                      SSM.c_manufactureAttrib.selected, 
                      SSM.c_makeAttrib.selected, 
                      SSM.c_modelAttrib.selected,
                      SSM.c_yearAttrib.selected);             
          }
      } else {
          CacheManager.instance().groupOccurrence = 
            CacheManager.instance().getPartOccurrenceFilterAgg(
               startIdx, endIdx, 
               SSM.startMonth,
               SSM.endMonth, 
               SSM.manufactureAttrib.selected, 
               SSM.makeAttrib.selected, 
               SSM.modelAttrib.selected,
               SSM.yearAttrib.selected);
          
           CacheManager.instance().c_groupOccurrence = 
             CacheManager.instance().getPartOccurrenceFilterAgg(
               startIdx, endIdx, 
               SSM.startMonth,
               SSM.endMonth, 
               SSM.c_manufactureAttrib.selected, 
               SSM.c_makeAttrib.selected, 
               SSM.c_modelAttrib.selected,
               SSM.c_yearAttrib.selected);
       
           if (SSM.selectedGroup.size() > 0 && SSM.useLocalFocus) {
              CacheManager.instance().monthMaximum = 
                CacheManager.instance().getMonthMaximumSelectedAgg(
                      SSM.manufactureAttrib.selected, 
                      SSM.makeAttrib.selected, 
                      SSM.modelAttrib.selected,
                      SSM.yearAttrib.selected);
              
              CacheManager.instance().c_monthMaximum = 
                 CacheManager.instance().getMonthMaximumSelectedAgg(
                       SSM.c_manufactureAttrib.selected, 
                       SSM.c_makeAttrib.selected, 
                       SSM.c_modelAttrib.selected,
                       SSM.c_yearAttrib.selected);              
           } else {
              CacheManager.instance().monthMaximum = 
                CacheManager.instance().getMonthMaximumAgg(
                      SSM.manufactureAttrib.selected, 
                      SSM.makeAttrib.selected, 
                      SSM.modelAttrib.selected,
                      SSM.yearAttrib.selected);
              
              CacheManager.instance().c_monthMaximum = 
                 CacheManager.instance().getMonthMaximumAgg(
                       SSM.c_manufactureAttrib.selected, 
                       SSM.c_makeAttrib.selected, 
                       SSM.c_modelAttrib.selected,
                       SSM.c_yearAttrib.selected);              
           }
      }
      
      // Just checking out the month maximum
      //System.err.println("R Max: " + CacheManager.instance().monthMaximum);
      //System.err.println("C Max: " + CacheManager.instance().c_monthMaximum);
      
      
      // Pick up the related group(s) if there is a user
      // selected group in context
      if (SSM.selectedGroup.size() > 0 ) {
         Vector<Integer> t = new Vector<Integer>();
         t.addAll( SSM.selectedGroup.values());
         
         Vector<Integer> l1 = CacheManager.instance().getRelatedGroup(
               startIdx, endIdx,
               SSM.startMonth, SSM.endMonth,
               t,
               SSM.manufactureAttrib.selected,
               SSM.makeAttrib.selected,
               SSM.modelAttrib.selected,
               SSM.yearAttrib.selected);
         
          Vector<Integer> l2 = CacheManager.instance().getRelatedGroup(
               startIdx, endIdx,
               SSM.startMonth, SSM.endMonth,
               t,
               SSM.c_manufactureAttrib.selected,
               SSM.c_makeAttrib.selected,
               SSM.c_modelAttrib.selected,
               SSM.c_yearAttrib.selected);
          
          if (SSM.useComparisonMode == true) {
             for (int i=0; i < l2.size(); i++) {
                if ( ! l1.contains(l2.elementAt(i))) l1.add(l2.elementAt(i));
             }
          }
          SSM.relatedList = l1;   
      } else {
         // Remove all 
         SSM.relatedList.clear(); 
      }
      
   
      
      // Finding the maximum and minimum
      // If we are using the selected components as a base point, than the maximum is the total 
      // occurrences for the selected components, otherwise it is the 
      if (SSM.useLocalFocus && SSM.selectedGroup.size() >  0) {
         Vector<Integer> self = new Vector<Integer>();
         self.addAll(SSM.selectedGroup.values());
         
         Vector<Integer> selfAgg = HierarchyTable.instance().getAgg(SSM.selectedGroup);
         
         if (SSM.useAggregate) {
            SSM.maxOccurrence = CacheManager.instance().getCoOccurringAgg(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth, 
                  selfAgg, 
                  self, 
                  SSM.manufactureAttrib.selected,
                  SSM.makeAttrib.selected,
                  SSM.modelAttrib.selected,
                  SSM.yearAttrib.selected
                  );
            
            if (SSM.useComparisonMode == true) {
               SSM.maxOccurrence += CacheManager.instance().getCoOccurringAgg(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     selfAgg, 
                     self, 
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected,
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected
                     );
                
            }
         } else {
            SSM.maxOccurrence = CacheManager.instance().getCoOccurring(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth,
                  self, 
                  self, 
                  SSM.manufactureAttrib.selected,
                  SSM.makeAttrib.selected,
                  SSM.modelAttrib.selected,
                  SSM.yearAttrib.selected);
            
            if (SSM.useComparisonMode == true ) {
               SSM.maxOccurrence += CacheManager.instance().getCoOccurring(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth,
                     self, 
                     self, 
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected,
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected);
            }
         }
      } else {
         Hashtable<Integer, Integer> t;
         if (SSM.useComparisonMode == true) {
            t = DCUtil.mergeHash(
                  CacheManager.instance().groupOccurrence, 
                  CacheManager.instance().c_groupOccurrence);
         } else {
            t = CacheManager.instance().groupOccurrence;
         }
         
         Iterator<Integer> iter = t.values().iterator();
         while (iter.hasNext()) {
            Integer v = iter.next();
            if (SSM.maxOccurrence < v) SSM.maxOccurrence = v;
            if (SSM.minOccurrence > v) SSM.minOccurrence = v;
         }     
     }
      
      
      
      System.out.println("Resetting Model Renderer Data : " + SSM.startIdx + " " + SSM.endIdx);
      //System.out.println("Max occurrence : " + SSM.instance().maxOccurrence);
      //System.out.println("Min occurrence : " + SSM.instance().minOccurrence);
      //System.out.println("\n");
      
      
      // Calculate the colour for each component
      String[] clist = getComponentUnsorted( null ); //passing in null (no context)
      for (int i = 0; i < clist.length; i++) {
         String partName = clist[i];
         DCComponent comp = MM.currentModel.componentTable.get(partName);
         comp.hasContext = true;
         
         
         // Calc based on an frequency to intensity colour scale
         //Integer partId  = HierarchyTable.instance().getGroupId(comp.baseName).size() > 0 ? HierarchyTable.instance().getGroupId(comp.baseName).elementAt(0) : null;
         
         float occ  = 0;
         if (comp.id > 0) {
            if (SSM.useLocalFocus == true && SSM.selectedGroup.size() > 0) {
               Vector<Integer> self = new Vector<Integer>();
               Vector<Integer> selfAgg = HierarchyTable.instance().getAgg(comp.id);
               Vector<Integer> selected = new Vector<Integer>();
               
               self.add(comp.id);
               selected.addAll(SSM.selectedGroup.values());
               
               if (SSM.useAggregate) {
                  occ = CacheManager.instance().getCoOccurringAgg(
                        startIdx, endIdx, 
                        SSM.startMonth, SSM.endMonth, 
                        selfAgg, 
                        selected, 
                        SSM.manufactureAttrib.selected,
                        SSM.makeAttrib.selected,
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected
                        );
                  
                  if (SSM.useComparisonMode == true) {
                     occ += CacheManager.instance().getCoOccurringAgg(
                           startIdx, endIdx, 
                           SSM.startMonth, SSM.endMonth, 
                           selfAgg, 
                           selected, 
                           SSM.c_manufactureAttrib.selected,
                           SSM.c_makeAttrib.selected,
                           SSM.c_modelAttrib.selected,
                           SSM.c_yearAttrib.selected
                           );
                  }
               } else {
                  occ = CacheManager.instance().getCoOccurring(
                        startIdx, endIdx, 
                        SSM.startMonth, SSM.endMonth,
                        self, 
                        selected, 
                        SSM.manufactureAttrib.selected,
                        SSM.makeAttrib.selected,
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected);
                  
                  if (SSM.useComparisonMode == true) {
                     occ += CacheManager.instance().getCoOccurring(
                           startIdx, endIdx, 
                           SSM.startMonth, SSM.endMonth,
                           self, 
                           selected, 
                           SSM.c_manufactureAttrib.selected,
                           SSM.c_makeAttrib.selected,
                           SSM.c_modelAttrib.selected,
                           SSM.c_yearAttrib.selected);
                  }
                  
                  
               }               
            } else {
               if (null != CacheManager.instance().groupOccurrence.get(comp.id)) {
                  occ =  CacheManager.instance().groupOccurrence.get(comp.id);     
               }
               
               if (SSM.useComparisonMode == true) {
                  if (null != CacheManager.instance().c_groupOccurrence.get(comp.id)) {
                     occ += CacheManager.instance().c_groupOccurrence.get(comp.id);       
                  }
               }
            }
         } 
            
         
         // Check whether the component should take part at all in the visualization, there are several exclusion criteria
         // 1) if the comp.id < 0, this happens for parts of models that are not linked to the logical hierarchy
         // 2) if the overall occurrence over the period equals to 0, note this is the overall number, not the number we
         //    get based off selected groups
         if ( comp.id < 0 ) 
            comp.hasContext = false;
         
         if (SSM.useComparisonMode == true) {
            Integer t1 = CacheManager.instance().groupOccurrence.get(comp.id);
            Integer t2 = CacheManager.instance().c_groupOccurrence.get(comp.id);
            if ( (null == t1 || t1 == 0) && (null == t2 || t2 == 0)) comp.hasContext = false;
         } else {
            Integer t = CacheManager.instance().groupOccurrence.get(comp.id);
            if ( null == t || t == 0) comp.hasContext = false;
         }
         
         
         /*
         if (comp.id > 0 && CacheManager.instance().groupOccurrence.get(comp.id) != null) {
            occ = (float)CacheManager.instance().groupOccurrence.get(comp.id).intValue(); 
            
            // Agg up to parent part (if possible) when using aggregation mode
            if (SSM.instance().useAggregate == true) {
               Integer tmp = comp.id;
               while(true) {
                  tmp = HierarchyTable.instance().getParentId(tmp);   
                  
                  // If parent is not null, and the model contains a reference to the parent, agg up to the 
                  // parent occurrence instance
                  if (tmp == null || MM.currentModel.componentTableById.get(tmp) == null)  break;
                  occ = (float)CacheManager.instance().groupOccurrence.get(tmp);
               } // end while
            } // end if agg
         }
         */
         
         
         
         comp.active = true;
         if (SSM.useLocalFocus == true) {
            if (SSM.selectedGroup.size() > 0 && ! SSM.relatedList.contains(comp.id)) {
               comp.active= false;
            }
         }
         
         DCColour nextColour = SchemeManager.instance().getColour(comp.id, occ, SSM.maxOccurrence);
         
         // Adjust based on the current focus
         //Integer selected = SSM.selectedGroup;
         /*
         if (SSM.instance().useLocalFocus != true) {
            if (SSM.instance().useAggregate) {
               if (SSM.selectedGroup.size() > 0) {
                  Vector<Integer> v = HierarchyTable.instance().getAgg(SSM.selectedGroup);
                  if ( ! v.contains(comp.id)) {
                     nextColour = nextColour.adjustAlpha(0.3f);
                  }
               }
            } else {
               // If not the selected group, make it out of focus
               //if (selected != null && selected != comp.id)
               if (SSM.selectedGroup.size() > 0 && ! SSM.selectedGroup.contains(comp.id)) 
                  nextColour = nextColour.adjustAlpha(0.3f);
            }
         }
         */
         
         // Reset sparline for any colouring changes
         comp.cchart.colour = comp.colour;
         
         // Animator construction
         comp.canimator = PropertySetter.createAnimator(SSM.PART_CHANGE_DURATION, comp.colour , "colour", new DCColourEval(), comp.colour, nextColour);
      }
      
      MM.currentModel.startAnimation();
      
      
      // Log the changes
//      ALogger.instance().log("Selected Year :" + SSM.startYear + "-" + SSM.endYear);
//      ALogger.instance().log("Selected Month:" + SSM.startMonth + "-" + SSM.endMonth);
//      ALogger.instance().log("Selected Group:" + SSM.selectedGroup);
      
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset any graphic, user-interface or rendering related data
   // We do it here because it may require access to a OpenGL context, which 
   // is not directly(or easily) available in resetData() since they processed
   // in different threads
   ////////////////////////////////////////////////////////////////////////////////
   public void resetDataGL(GL2 gl2) {
      long t1 = System.currentTimeMillis();
      resetDataGL1(gl2);
      long t2 = System.currentTimeMillis();
      System.err.println("ResetData GL took ==> "+ (t2-t1));
   }
   public void resetDataGL1(GL2 gl2) {

         
      int startIdx = CacheManager.instance().getDateKey( SSM.startTimeFrame ) == null ? 0:
                     CacheManager.instance().getDateKey( SSM.startTimeFrame );
      
      //TODO: Need to fix this later, as dates may not be exact
      int endIdx   = CacheManager.instance().getDateKey( SSM.endTimeFrame) == null ? CacheManager.instance().timeLineSize:
                     CacheManager.instance().getDateKey( SSM.endTimeFrame );
      int size = (endIdx - startIdx) + 1;      
      
      int origin = CacheManager.timeLineStartYear;
      int sYear  = SSM.startYear;
      int eYear  = SSM.endYear; 
      int sMonth = SSM.startMonth;
      int eMonth = SSM.endMonth;      
      
      
      int chartStartIdx = startIdx;
      int chartEndIdx   = endIdx;
      int chartSize     = size;
      
      // Reassign the time-period for the graphs
      if (SSM.useFullTimeLine == true) {
         chartSize = CacheManager.instance().queryTable.size();      
         chartStartIdx = 0;
         chartEndIdx   = chartSize - 1;
      }      
      
      
      DWin.instance().debug("startIndex:" + startIdx + "  endIdnex:" + endIdx);
      
      
      if (SSM.selectedGroup.size() >= 0 ) {
         dcTextPanel.t1.documentList.clear();
         dcTextPanel.t2.documentList.clear();
         dcTextPanel.t2.tagList.clear();
         dcTextPanel.t2.tagList.clear();
         
         Vector<Integer> groupList = new Vector<Integer>();
         groupList.addAll(SSM.selectedGroup.values());
         /*
         if (SSM.instance().useAggregate) {
            groupList =  HierarchyTable.instance().getAgg(SSM.selectedGroup);
         } else {
            //groupList.add(SSM.selectedGroup);
            groupList.addAll(SSM.selectedGroup.values());
         }
         */
        
         if (SSM.useAggregate == true) {
            dcTextPanel.t1.documentList = CacheManager.instance().setDocumentDataAgg(
                  SSM.startTimeFrame,
                  SSM.endTimeFrame,
                  SSM.startMonth,
                  SSM.endMonth,
                  groupList,
                  SSM.t1Start);
            
            dcTextPanel.t2.documentList = CacheManager.instance().setDocumentDataAgg(
                  SSM.startTimeFrame,
                  SSM.endTimeFrame,
                  SSM.startMonth,
                  SSM.endMonth,
                  groupList,
                  SSM.t2Start);           
         } else {
            dcTextPanel.t1.documentList = CacheManager.instance().setDocumentData(
                  SSM.startTimeFrame,
                  SSM.endTimeFrame,
                  SSM.startMonth,
                  SSM.endMonth,
                  groupList,
                  SSM.t1Start);
            
            dcTextPanel.t2.documentList = CacheManager.instance().setDocumentData(
                  SSM.startTimeFrame,
                  SSM.endTimeFrame,
                  SSM.startMonth,
                  SSM.endMonth,
                  groupList,
                  SSM.t2Start);
         }
         
         DWin.instance().debug("Panel 1 size : " + dcTextPanel.t1.documentList.size());
         DWin.instance().debug("Panel 2 size : " + dcTextPanel.t2.documentList.size());
      } else {
         dcTextPanel.t1.tagList.clear();
         dcTextPanel.t2.tagList.clear();         
         dcTextPanel.t1.documentList.clear();
         dcTextPanel.t2.documentList.clear();
      }
      
      
      if (dcTextPanel.t1.documentList.size() >= 0) {
         dcTextPanel.t1.calculate();
         dcTextPanel.t1.renderToTexture(Color.BLACK);
         SSM.t1Height = dcTextPanel.t1.textPaneHeight;
      } 
      if (dcTextPanel.t2.documentList.size() >= 0) {
         dcTextPanel.t2.calculate();
         dcTextPanel.t2.renderToTexture(Color.BLACK);
         SSM.t2Height = dcTextPanel.t2.textPaneHeight;
      } 
      
      if (SSM.docAction == 1) {
         SSM.yoffset = SSM.t1Height + SSM.docHeight;
      }
      if (SSM.yoffset > dcTextPanel.t1.textPaneHeight + dcTextPanel.t2.textPaneHeight) {
         SSM.yoffset = SSM.docHeight;
      }
    
      
      float range = (CacheManager.timeLineEndYear - CacheManager.timeLineStartYear)+1;
      
      range = (SSM.endYear - SSM.startYear)+1; 
      
      
      String[] clist = getComponentUnsorted( null ); //passing in null (no context)         
      SSM.instance().segmentMax = 0;
      for (int i = 0; i < clist.length; i++) {
         DCComponent comp = MM.currentModel.componentTable.get( clist[i] );   
         
         if (comp.id < 0) continue;
         
         float data[] = new float[chartSize];
         float c_data[] = new float[chartSize];
         
         short h[] = new short[chartSize];
         
         float localMax = 0.0f;
         for (int idx = chartStartIdx; idx <= chartEndIdx; idx++) {
            float value = 0;
            float value2 = 0;
            
            if (SSM.useAggregate == false) {
               if (SSM.selectedGroup.size() > 0 && SSM.useLocalFocus == true) {
                  Vector<Integer> selectedGroup =  new Vector<Integer>();
                  selectedGroup.addAll( SSM.selectedGroup.values());
                  Vector<Integer> t = new Vector<Integer>();
                  t.add(comp.id);                  
                  
                  value = CacheManager.instance().getCoOccurring(
                        idx, 
                        t, 
                        selectedGroup, 
                        SSM.manufactureAttrib.selected,
                        SSM.makeAttrib.selected,
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected);
                  
                   value2 = CacheManager.instance().getCoOccurring(
                        idx, 
                        t, 
                        selectedGroup, 
                        SSM.c_manufactureAttrib.selected,
                        SSM.c_makeAttrib.selected,
                        SSM.c_modelAttrib.selected,
                        SSM.c_yearAttrib.selected);
                 
               } else {
                  value = CacheManager.instance().getOcc(idx, comp.id, 
                        SSM.manufactureAttrib.selected, 
                        SSM.makeAttrib.selected, 
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected);
                  
                  value2 = CacheManager.instance().getOcc(idx, comp.id, 
                        SSM.c_manufactureAttrib.selected, 
                        SSM.c_makeAttrib.selected, 
                        SSM.c_modelAttrib.selected,
                        SSM.c_yearAttrib.selected);
               }
            } else {
               if (SSM.selectedGroup.size() > 0 && SSM.useLocalFocus == true) {
                  Vector<Integer> selectedGroup =  new Vector<Integer>();
                  selectedGroup.addAll( SSM.selectedGroup.values());                  
                 
                  value = CacheManager.instance().getCoOccurringAgg(
                        idx, 
                        HierarchyTable.instance().getAgg(comp.id), 
                        selectedGroup,
                        SSM.manufactureAttrib.selected,
                        SSM.makeAttrib.selected,
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected);
                  
                  value2 = CacheManager.instance().getCoOccurringAgg(
                        idx, 
                        HierarchyTable.instance().getAgg(comp.id), 
                        selectedGroup,
                        SSM.c_manufactureAttrib.selected,
                        SSM.c_makeAttrib.selected,
                        SSM.c_modelAttrib.selected,
                        SSM.c_yearAttrib.selected);
               } else {
                  value = CacheManager.instance().getOccAgg(idx, comp.id, 
                        SSM.manufactureAttrib.selected, 
                        SSM.makeAttrib.selected, 
                        SSM.modelAttrib.selected,
                        SSM.yearAttrib.selected);              
                  
                  value2 = CacheManager.instance().getOccAgg(idx, comp.id, 
                        SSM.c_manufactureAttrib.selected, 
                        SSM.c_makeAttrib.selected, 
                        SSM.c_modelAttrib.selected,
                        SSM.c_yearAttrib.selected);              
               }
            }
            
            if (value > localMax) localMax = value;
            data[idx-chartStartIdx] = value;
            c_data[idx-chartStartIdx] = value2;
            
            int y = Integer.parseInt(CacheManager.instance().getTimeByIndex(idx).substring(0, 4));
            int m = Integer.parseInt(CacheManager.instance().getTimeByIndex(idx).substring(4, 6))-1;
            
            if (y >= SSM.startYear && y <= SSM.endYear) {
               if (m >= SSM.startMonth && m <= SSM.endMonth) {
                  h[idx-chartStartIdx] = 1;
               } else {
                  h[idx-chartStartIdx] = 0;
               }
            }
            
             
         }
         
         // # Segment restrictions
         int segSize = 0;
         if (data.length <= 24) {
            segSize = data.length;
         } else {
            segSize = 24;
         }
         
         
         
         
         comp.cchart.setHighlight(h);
         if (SSM.sparklineMode == 0) {
            comp.cchart.setData( DCUtil.getDeriv(data) );
         } else if (SSM.sparklineMode == 1){
            comp.cchart.setData( data ); // Original
         }
         comp.cchart.c_data = c_data;
         
         comp.cchart.setMaxValue(localMax);
         
         /*
         if (SSM.selectedGroup.size() > 0 && SSM.selectedGroup.contains(comp.id)) {
            comp.cchart.resize(SSM.sparkLineWidth, 10*range+comp.cchart.labelBuffer);
         } else {
            comp.cchart.resize(SSM.sparkLineWidth, 10*range+comp.cchart.labelBuffer);
         }
         */
         //comp.cchart.resize(SSM.sparkLineWidth, 15*range+comp.cchart.labelBuffer);
         comp.cchart.resize(SSM.sparkLineWidth, SSM.heatmapCellHeight*range+comp.cchart.labelBuffer);
         
         comp.cchart.createSegment( segSize ); // needs to go after setData and setHeight
         
         // Set it to the same colour as the component
         comp.cchart.colour = comp.colour;
         
         
         // Need to loop again to get the global maxima
         int start = sYear - origin; 
         int end   = (eYear - origin)+1;
         //System.out.println(sYear + "--" + origin);
         //System.out.println("start " + start +  " end " + end);
         for (int x=start; x < end; x++) {
            for (int y=sMonth; y <= eMonth; y++) {
               float tmp = 0;
               if (SSM.useComparisonMode == true) {
                  tmp = comp.cchart.data[12*x+y] + comp.cchart.c_data[12*x+y];  
               } else {
                  tmp = comp.cchart.data[12*x+y];
               }
               if ( SSM.instance().segmentMax < tmp) SSM.instance().segmentMax = tmp; 
            }
         }
      }
      
      
      
      // Reset the scroll labels
      /*
      label.clearMark();
      if (SSM.instance().manufactureAttrib.selected != null) 
         label.addMark(SSM.instance().manufactureAttrib.selected, Color.BLACK, labelFont, 1, 31);
      else
         label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 31);
      if (SSM.instance().makeAttrib.selected != null)
         label.addMark(SSM.instance().makeAttrib.selected, Color.BLACK, labelFont, 1, 21);
      else
         label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 21);
      if (SSM.instance().modelAttrib.selected != null)
         label.addMark(SSM.instance().modelAttrib.selected, Color.BLACK, labelFont, 1, 11);
      else
         label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 11);
      if (SSM.instance().yearAttrib.selected != null)
         label.addMark(SSM.instance().yearAttrib.selected, Color.BLACK, labelFont, 1, 1);
      else
         label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 1);
      label.renderToTexture(null);
      
      c_label.clearMark();
      if (SSM.instance().c_manufactureAttrib.selected != null)
         c_label.addMark(SSM.instance().c_manufactureAttrib.selected, Color.BLACK, labelFont, 1, 31);
      else
         c_label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 31);
         
      if (SSM.instance().c_makeAttrib.selected != null)
         c_label.addMark(SSM.instance().c_makeAttrib.selected, Color.BLACK, labelFont, 1, 21);
      else
         c_label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 21);
         
      if (SSM.instance().c_modelAttrib.selected != null)
         c_label.addMark(SSM.instance().c_modelAttrib.selected, Color.BLACK, labelFont, 1, 11);
      else
         c_label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 11);
         
      if (SSM.instance().c_yearAttrib.selected != null)
         c_label.addMark(SSM.instance().c_yearAttrib.selected, Color.BLACK, labelFont, 1, 1);
      else
         c_label.addMark("-- ALL --", Color.BLACK, labelFont, 1, 1);
      c_label.renderToTexture(null);      
      */
      
      
      // Update status
      StatusWindow.update();
   }
   
   
   
   
   public String getBaseName(String s) {
      if (s.indexOf("_") < 0) return s;
      return s.substring(0, s.indexOf('_')); 
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a list of components via whatever order the
   // enumeration returns them.
   // Don't really need GL2 ... but to keep the API consistent
   ////////////////////////////////////////////////////////////////////////////////
   public String[] getComponentUnsorted(GL2 gl2) {
      Enumeration<String> e = MM.currentModel.componentTable.keys();
      String list[] = new String[ MM.currentModel.componentTable.size()];
      int c = 0;
      while (e.hasMoreElements()) {
         list[c] = e.nextElement();
         c++;
      }
      return list;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a list of component sorted by their projected Y onto the near plane.
   // The projection point is based on the projection of the objects centroid
   ////////////////////////////////////////////////////////////////////////////////
   public String[] getComponentSortedByProjY(GL2 gl2) {
      Enumeration<String> e = MM.currentModel.componentTable.keys();
      String sortingList[] = new String[ MM.currentModel.componentTable.size()];
      
      int c = 0;
      while(e.hasMoreElements()) {
         sortingList[c] = e.nextElement();
         c++;
      }
      
      for (int i=0; i < sortingList.length; i++) {
         for (int j=0; j < sortingList.length-1; j++) {
            float d1 =  MM.currentModel.componentTable.get(sortingList[j]).projCenter.y;
            float d2 =  MM.currentModel.componentTable.get(sortingList[j+1]).projCenter.y;
            
            if (d1 < d2) {
               String tmp = sortingList[j];
               sortingList[j] = sortingList[j+1];
               sortingList[j+1] = tmp;
            }
         } 
      }
     
      return sortingList;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a list of components sorted by distance away from the origin
   ////////////////////////////////////////////////////////////////////////////////
   public String[] getComponentSortedByCentroid2(GL2 gl2) {
      Enumeration<String> e = MM.currentModel.componentTable.keys();
      String sortingList[] = new String[ MM.currentModel.componentTable.size()];
      
      int c = 0;
      while(e.hasMoreElements()) {
         sortingList[c] = e.nextElement();
         c++;
      }      
      
      
      for (int i=0; i < sortingList.length; i++) {
         for (int j=0; j < sortingList.length; j++) {
            float d1 = MM.currentModel.componentTable.get(sortingList[i]).center.sub(new DCTriple(0,0,0)).mag();
            float d2 = MM.currentModel.componentTable.get(sortingList[j]).center.sub(new DCTriple(0,0,0)).mag();
            if ( d1 < d2 ) {
               String tmp = sortingList[i];   
               sortingList[i] = sortingList[j];
               sortingList[j] = tmp;
            }
         }
      }
      
      return sortingList;      
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a list of component sorted by their distance away from the
   // eye position. This is a naive sort as it is based on the object centroid
   // which can be inaccurate if the components overlaps, or if the component 
   // has non-uniform distribution of polygons
   ////////////////////////////////////////////////////////////////////////////////
   public String[] getComponentSortedByCentroid(GL2 gl2) {
      Enumeration<String> e = MM.currentModel.componentTable.keys();
      String sortingList[] = new String[ MM.currentModel.componentTable.size()];
      
      // Get the modelView matrix from the openGL stack
      float modelV[] = new float[16];
      gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelV, 0);
      Matrix modelV4x4 = new Matrix(new double[][] {
            {modelV[0], modelV[4], modelV[8],  modelV[12]},
            {modelV[1], modelV[5], modelV[9],  modelV[13]},
            {modelV[2], modelV[6], modelV[10], modelV[14]},
            {modelV[3], modelV[7], modelV[11], modelV[15]}
      });
      Matrix modelV4x4Inv = modelV4x4.inverse();
      
      // Inverse transform the eye position to calculate the distance to the centroid
      double eye[] = { DCCamera.instance().eye.x, DCCamera.instance().eye.y, DCCamera.instance().eye.z, 1 };
      double eye_inv[] = MatrixUtil.multVector(modelV4x4Inv, eye);
      DCTriple eyeT = new DCTriple( (float)eye_inv[0], (float)eye_inv[1], (float)eye_inv[2]);
      
      int c = 0;
      while(e.hasMoreElements()) {
         sortingList[c] = e.nextElement();
         c++;
      }
      
      // Run-of-the mill bubble sort
      // Slow, but if we do get thousands of components there are better
      // problems to worry about...
      for (int i=0; i < sortingList.length; i++) {
         for (int j=0; j < sortingList.length; j++) {
            DCTriple centroid1 =  MM.currentModel.componentTable.get(sortingList[i]).center;   
            DCTriple centroid2 =  MM.currentModel.componentTable.get(sortingList[j]).center;   
            
            float d1 = eyeT.sub(centroid1).mag();
            float d2 = eyeT.sub(centroid2).mag();
            if (d1 < d2) {
               String tmp = sortingList[i];
               sortingList[i] = sortingList[j];
               sortingList[j] = tmp;
            }
         } 
      }
      return sortingList;
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Set the projected 3D coordinates in 2D space
   ////////////////////////////////////////////////////////////////////////////////
   public void setProjectedCoord(GL2 gl2) {
      float buffer[];
      float viewBuffer[], projBuffer[];
      
      buffer = new float[16];   
      gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
      projBuffer = buffer;
      
      buffer = new float[16];
      gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
      viewBuffer = buffer;
      
      int viewport[] = new int[4];
      gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);             
      
      float wincoord[] = new float[3];      
      
      String clist[] = this.getComponentUnsorted(gl2);
      for (int i=0; i < clist.length; i++) {
         DCComponent comp = MM.currentModel.componentTable.get(clist[i]);
         glu.gluProject(
               comp.center.x, 
               comp.center.y,
               comp.center.z,
               viewBuffer, 0,
               projBuffer, 0,
               viewport, 0,
               wincoord, 0
               );         
         comp.projCenter = new DCTriple( wincoord[0], wincoord[1], wincoord[2]);
      }
      
   }
   
   
   // Ported from JOGL demo pakcage 
   // Translated from C++ Version see below:
   //--------------------------------------------------------------------------------------
   // Order Independent Transparency with Dual Depth Peeling
   //
   // Author: Louis Bavoil
   // Email: sdkfeedback@nvidia.com
   //
   // Depth peeling is traditionally used to perform order independent transparency (OIT)
   // with N geometry passes for N transparency layers. Dual depth peeling enables peeling
   // N transparency layers in N/2+1 passes, by peeling from the front and the back
   // simultaneously using a min-max depth buffer. This sample performs either normal or
   // dual depth peeling and blends on the fly.
   //
   // Copyright (c) NVIDIA Corporation. All rights reserved.
   //--------------------------------------------------------------------------------------
     public void InitDualPeelingRenderTargets(GL2 gl) {
        DWin.instance().msg("SSM height is : " + SSM.windowHeight + " SSM width : " + SSM.windowWidth);
        
        gl.glGenTextures(2, g_dualDepthTexId, 0);
        gl.glGenTextures(2, g_dualFrontBlenderTexId, 0);
        gl.glGenTextures(2, g_dualBackTempTexId, 0);
        gl.glGenFramebuffers(1, g_dualPeelingSingleFboId, 0);
        for (int i = 0; i < 2; i++) {
           gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[i]);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S,  GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T,  GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
           
           //gl.glEnable( GL2.GL_PIXEL_UNPACK_BUFFER );
           gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_FLOAT_RG32_NV, 
                 SSM.windowWidth, SSM.windowHeight,
                 0,  GL2.GL_RGB,  GL2.GL_FLOAT, null);

           gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[i]);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
           gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_RGBA, 
                 SSM.windowWidth, SSM.windowHeight,
                 0,  GL2.GL_RGBA,  GL2.GL_FLOAT, null);

           gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[i]);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
           gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
           gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_RGBA, 
                 SSM.windowWidth, SSM.windowHeight,
                 0,  GL2.GL_RGBA,  GL2.GL_FLOAT, null);
        }

        gl.glGenTextures(1, g_dualBackBlenderTexId, 0);
        gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0]);
        gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
        gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
        gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0, GL2.GL_RGB, 
              SSM.windowWidth, SSM.windowHeight,
              0, GL2.GL_RGB, GL2.GL_FLOAT, null);

        gl.glGenFramebuffers(1, g_dualBackBlenderFboId, 0);
        gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, g_dualBackBlenderFboId[0]);
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0], 0);

        gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, g_dualPeelingSingleFboId[0]);

        int j = 0;
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT0,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[j], 0);
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT1,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[j], 0);
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT2,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[j], 0);

        j = 1;
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT3,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[j], 0);
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT4,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[j], 0);
        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT5,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[j], 0);

        gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT6,
              GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0], 0);
        
        
        // Back to normal ???? - DC
        gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, 0);
     }

     
     void DeleteDualPeelingRenderTargets(GL2 gl) {
        gl.glDeleteFramebuffers(1, g_dualBackBlenderFboId, 0);
        gl.glDeleteFramebuffers(1, g_dualPeelingSingleFboId, 0);
        gl.glDeleteTextures(2, g_dualDepthTexId, 0);
        gl.glDeleteTextures(2, g_dualFrontBlenderTexId, 0);
        gl.glDeleteTextures(2, g_dualBackTempTexId, 0);
        gl.glDeleteTextures(1, g_dualBackBlenderTexId, 0);
     }
     
     void BuildShaders(GL2 gl2) {
        System.err.println("\nloading shaders...\n");

        g_shaderDualInit = new ShaderObj();
        g_shaderDualInit.createShader(gl2, Const.SHADER_PATH+"dual_peeling_init_vertex.glsl", GL2.GL_VERTEX_SHADER);
        g_shaderDualInit.createShader(gl2, Const.SHADER_PATH+"dual_peeling_init_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
        g_shaderDualInit.createProgram(gl2);
        g_shaderDualInit.linkProgram(gl2);

        g_shaderDualPeel = new ShaderObj();
        g_shaderDualPeel.createShader(gl2, Const.SHADER_PATH+"shade_vertex.glsl", GL2.GL_VERTEX_SHADER);
        g_shaderDualPeel.createShader(gl2, Const.SHADER_PATH+"dual_peeling_peel_vertex.glsl", GL2.GL_VERTEX_SHADER);
        g_shaderDualPeel.createShader(gl2, Const.SHADER_PATH+"shade_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
        g_shaderDualPeel.createShader(gl2, Const.SHADER_PATH+"dual_peeling_peel_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
        g_shaderDualPeel.createProgram(gl2);
        g_shaderDualPeel.linkProgram(gl2);

        g_shaderDualBlend = new ShaderObj();
        g_shaderDualBlend.createShader(gl2, Const.SHADER_PATH+"dual_peeling_blend_vertex.glsl", GL2.GL_VERTEX_SHADER);
        g_shaderDualBlend.createShader(gl2, Const.SHADER_PATH+"dual_peeling_blend_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
        g_shaderDualBlend.createProgram(gl2);
        g_shaderDualBlend.linkProgram(gl2);

        g_shaderDualFinal = new ShaderObj();
        g_shaderDualFinal.createShader(gl2, Const.SHADER_PATH+"dual_peeling_final_vertex.glsl", GL2.GL_VERTEX_SHADER);
        g_shaderDualFinal.createShader(gl2, Const.SHADER_PATH+"dual_peeling_final_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
        g_shaderDualFinal.createProgram(gl2);
        g_shaderDualFinal.linkProgram(gl2);

     }     
     int MakeFullScreenQuad(GL2 gl) {
        GLU glu = GLU.createGLU(gl);

        //g_quadDisplayList = gl.glGenLists(1);
        int listId = gl.glGenLists(1);
        gl.glNewList(listId, GL2.GL_COMPILE);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0f, 1.0f, 0.0f, 1.0f);
        gl.glBegin(GL2.GL_QUADS);
        {
           gl.glVertex2f(0.0f, 0.0f); 
           gl.glVertex2f(1.0f, 0.0f);
           gl.glVertex2f(1.0f, 1.0f);
           gl.glVertex2f(0.0f, 1.0f);
        }
        gl.glEnd();
        gl.glPopMatrix();

        gl.glEndList();
        
        return listId;
     }     
     
     void ProcessDualPeeling(GL2 gl, int displayList) {
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);

        // ---------------------------------------------------------------------
        // 1. Initialize Min-Max Depth Buffer
        // ---------------------------------------------------------------------

        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, g_dualPeelingSingleFboId[0]);

        // Render targets 1 and 2 store the front and back colors
        // Clear to 0.0 and use MAX blending to filter written color
        // At most one front color and one back color can be written every pass
        gl.glDrawBuffers(2, g_drawBuffers, 1);
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        // Render target 0 stores (-minDepth, maxDepth, alphaMultiplier)
        gl.glDrawBuffer(g_drawBuffers[0]);
        gl.glClearColor(-MAX_DEPTH, -MAX_DEPTH, 0, 0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glBlendEquation(GL2.GL_MAX);

        g_shaderDualInit.bind(gl);
        DrawModel(gl);
        g_shaderDualInit.unbind(gl);

        // ---------------------------------------------------------------------
        // 2. Dual Depth Peeling + Blending
        // ---------------------------------------------------------------------

        // Since we cannot blend the back colors in the geometry passes,
        // we use another render target to do the alpha blending
        //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, g_dualBackBlenderFboId);
        gl.glDrawBuffer(g_drawBuffers[6]);
        //gl.glClearColor(g_backgroundColor[0], g_backgroundColor[1], g_backgroundColor[2], 0);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        


        currId = 0;
        for (int pass = 1; g_useOQ || pass < SSM.g_numPasses; pass++) {
           currId = pass % 2;
           int prevId = 1 - currId;
           int bufId = currId * 3;

           //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, g_dualPeelingFboId[currId]);

           gl.glDrawBuffers(2, g_drawBuffers, bufId+1);
           gl.glClearColor(0, 0, 0, 0);
           gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

           gl.glDrawBuffer(g_drawBuffers[bufId+0]);
           gl.glClearColor(-MAX_DEPTH, -MAX_DEPTH, 0, 0);
           gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

           // Render target 0: RG32F MAX blending
           // Render target 1: RGBA MAX blending
           // Render target 2: RGBA MAX blending
           gl.glDrawBuffers(3, g_drawBuffers, bufId+0);
           gl.glBlendEquation(GL2.GL_MAX);

           g_shaderDualPeel.bind(gl);
           g_shaderDualPeel.bindTextureRECT(gl,"DepthBlenderTex", g_dualDepthTexId[prevId], 0);
           g_shaderDualPeel.bindTextureRECT(gl,"FrontBlenderTex", g_dualFrontBlenderTexId[prevId], 1);
           //g_shaderDualPeel.setUniform(gl,"Alpha", g_opacity, 1);
           // Hackhack
           g_shaderDualPeel.setUniform1fv(gl, "Alpha", g_opacity);
           
           DrawModel(gl);
           g_shaderDualPeel.unbind(gl);

           // Full screen pass to alpha-blend the back color
           gl.glDrawBuffer(g_drawBuffers[6]);

           gl.glBlendEquation(GL2.GL_FUNC_ADD);
           gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

           if (g_useOQ) {
              gl.glBeginQuery(GL2.GL_SAMPLES_PASSED, g_queryId[0]);
           }

           g_shaderDualBlend.bind(gl);
           g_shaderDualBlend.bindTextureRECT(gl,"TempTex", g_dualBackTempTexId[currId], 0);
           gl.glCallList(displayList);
           g_shaderDualBlend.unbind(gl);

           if (g_useOQ) {
              gl.glEndQuery(GL2.GL_SAMPLES_PASSED);
              int[] sample_count = new int[]{0};
              gl.glGetQueryObjectuiv(g_queryId[0], GL2.GL_QUERY_RESULT, sample_count, 0);
              if (sample_count[0] == 0) {
                 break;
              }
           }
        }
        
        
        // Hackhack : Put this here for now
        // Draw silhouette where necessary
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);
        for (DCComponent comp : MM.currentModel.componentTable.values()) {
           //comp.renderBufferAdj(gl, DCColour.fromInt(100, 100, 100, 150));
           if (!comp.hasContext || !comp.active) {
              //comp.renderBufferAdj(gl, DCColour.fromInt(160, 160, 160, 50));
              comp.renderBufferAdj(gl, SchemeManager.silhouette_default);
              //comp.renderBuffer(gl, SchemeManager.comp_1);
           } else {
              //gl.glLineWidth(5.0f);
              //comp.renderBufferAdj(gl, comp.colour);
              //gl.glLineWidth(1.0f);
           }
        }          
        
        
        // If not using glow, then render a bounding box
        if (! SSM.useGlow && SSM.selectedGroup.size() > 0) {
           for (DCComponent comp : MM.currentModel.componentTable.values()) {
              if (SSM.selectedGroup.contains(comp.id)) {
                 comp.boundingBox.renderBoundingBox(gl, SchemeManager.selected);
              }
           }
        }
        
        
        
        ////////////////////////////////////////////////////////////////////////////////
        // Attempt to draw comparative result using the edge detectors
        // See also glow filter in ModelRenderer for alternative appraoch
        // 
        // This seems to result in a very cluttered looking render
        ////////////////////////////////////////////////////////////////////////////////
        /*
        if (SSM.instance().useComparisonMode == true) {
           gl.glLineWidth(1.2f);
           for (DCComponent comp : MM.currentModel.componentTable.values()) {
              if (comp.hasContext && comp.active) {
                 float v1 = CacheManager.instance().groupOccurrence.get(comp.id);
                 float v2 = CacheManager.instance().c_groupOccurrence.get(comp.id);
                 
                 if (v1 > v2) {
                    if (SSM.instance().useFlag)
                       comp.renderBufferAdj(gl, SchemeManager.comp_1);
                    else
                       //comp.renderSilhouette(gl, SchemeManager.comp_1);
                       comp.renderBufferSilhouette(gl, SchemeManager.comp_1);
                 } else if (v2 > v1){
                    if (SSM.instance().useFlag) 
                       comp.renderBufferAdj(gl, SchemeManager.comp_2);
                    else
                       //comp.renderSilhouette(gl, SchemeManager.comp_2);
                       comp.renderBufferSilhouette(gl, SchemeManager.comp_2);
                 } else {
                    if (SSM.instance().useFlag)
                       comp.renderBufferAdj(gl, SchemeManager.silhouette_default); 
                    else 
                       //comp.renderSilhouette(gl, SchemeManager.silhouette_default);
                       comp.renderBufferSilhouette(gl, SchemeManager.silhouette_default);
                 }
              }
           }
           gl.glLineWidth(1.0f);
        }
        */
        

        gl.glDisable(GL2.GL_BLEND);
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
      
     }     
     
     public void RenderDualPeeling(GL2 gl2, int displayList) {
        // ---------------------------------------------------------------------
        // 3. Final Pass
        // ---------------------------------------------------------------------


        gl2.glDrawBuffer(GL2.GL_BACK);

        g_shaderDualFinal.bind(gl2);
        g_shaderDualFinal.bindTextureRECT(gl2,"FrontBlenderTex", g_dualFrontBlenderTexId[currId], 1);
        g_shaderDualFinal.bindTextureRECT(gl2,"BackBlenderTex", g_dualBackBlenderTexId[0], 2);
        gl2.glCallList(displayList);
        g_shaderDualFinal.unbind(gl2);
       
     }
     
     
     public void RenderOITTexture(GL2 gl2) {
        gl2.glDisable(GL2.GL_BLEND);
        gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
        gl2.glDrawBuffer(GL2.GL_BACK);
        
        g_shaderDualFinal.bind(gl2);
           g_shaderDualFinal.bindTextureRECT(gl2,"FrontBlenderTex", g_dualFrontBlenderTexId[currId], 1);
           g_shaderDualFinal.bindTextureRECT(gl2,"BackBlenderTex", g_dualBackBlenderTexId[0], 2);
           gl2.glCallList(g_quadDisplayList);
        g_shaderDualFinal.unbind(gl2);
        
        // Hackhack : Put this here for now
        // Draw silhouette if necessary
        /*
        gl2.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glEnable(GL2.GL_BLEND);
        for (DCComponent comp : MM.currentModel.componentTable.values()) {
           if (SSM.relatedList.size() > 0 && SSM.relatedList.contains(comp.id) && !SSM.selectedGroup.contains(comp.id)){
              comp.renderBufferAdj(gl2, SchemeManager.colour_related.adjustAlpha(0.3f));   
           }
        }        
        */
    }
     
     public void DrawModel(GL2 gl2) {
        //gl2.glEnable(GL2.GL_CULL_FACE);
        
        for (DCComponent comp : MM.currentModel.componentTable.values()) {
           if (! comp.hasContext || ! comp.active) continue; // Dont' render if it does not have associated values
           
           //g_shaderDualPeel.setUniformf(gl2, "compColour", 0.85f, 0.85f, 0.85f, 0.3f);
           g_shaderDualPeel.setUniformf(gl2, "compColour", comp.colour.r, comp.colour.g, comp.colour.b, 0.7f);
           
           if (SSM.instance().useLight) 
              g_shaderDualPeel.setUniform1i(gl2, "useLight", 1);
           else
              g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
           
           // Hack Test
           if (SSM.useConstantAlpha == false) {
              g_opacity[0] = comp.colour.a;
              g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
           }
              
           gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, comp.vboOIT[0]);
           gl2.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, comp.vboOIT[1]);
           gl2.glVertexPointer(3, GL2.GL_FLOAT, 24, 0);
           gl2.glNormalPointer(GL2.GL_FLOAT, 24, 12);
           
           gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
           gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
           gl2.glDrawElements( GL2.GL_TRIANGLES, comp.faceList.size()*3, GL2.GL_UNSIGNED_INT, 0);
           gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0);
           gl2.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
           
           
           // Try to send in other special effects
           // Get the percentage differential between any two components
           if (SSM.useComparisonMode == true) {
              float v1max = 0, v2max = 0; 
              for (Integer v : CacheManager.instance().groupOccurrence.values()) v1max += v;
              for (Integer v : CacheManager.instance().c_groupOccurrence.values()) v2max += v;
              
              float v1 = CacheManager.instance().groupOccurrence.get(comp.id);
              float v2 = CacheManager.instance().c_groupOccurrence.get(comp.id);                  
              
              
              // Hack
              v1 /= v1max;
              v2 /= v2max;
              
              g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
              if (v1 > v2) {                   
                 double v = 0.4 + 0.6*(v1-v2)/v1;
                 gl2.glLineWidth(4.0f);
                 g_shaderDualPeel.setUniformf(gl2, "compColour", 
                       SchemeManager.comp_1.r,
                       SchemeManager.comp_1.g,
                       SchemeManager.comp_1.b,
                       (float)v);
                 comp.renderSilhouette(gl2, DCColour.fromInt(255, 0, 0, 255));
                 gl2.glLineWidth(1.0f);
              } else if (v1 < v2 ){
                 double v = 0.4 + 0.6*(v2-v1)/v2;
                 gl2.glLineWidth(4.0f);
                 g_shaderDualPeel.setUniformf(gl2, "compColour", 
                       SchemeManager.comp_2.r,
                       SchemeManager.comp_2.g,
                       SchemeManager.comp_2.b,
                       (float)v);
                 
                 comp.renderSilhouette(gl2, DCColour.fromInt(0, 255, 0, 255));
                 gl2.glLineWidth(1.0f);
              }
           }
           
           
           /* Test demo
           gl2.glLineWidth(6.0f);
              gl2.glColor4d(1, 0, 1, 1);
              //comp.renderEdgeWithNoAdjacent(gl2); 
              g_shaderDualPeel.setUniformf(gl2, "compColour", 1.0f, 0.0f,  0.0f, 1.0f);
              comp.renderSilhouette(gl2, DCColour.fromInt(255, 0, 0, 255));
           gl2.glLineWidth(1.0f);
           // End of trying to send in other special effects
            */
           
           
           
           //comp.renderBufferAdj(gl2, SchemeManager.silhouette_default);
           //if (SSM.selectedGroup.size() > 0 && SSM.selectedGroup.contains(comp.id)){
              //g_opacity[0] = comp.colour.a;
              //g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
              //g_shaderDualPeel.setUniformf(gl2, "compColour", SchemeManager.colour_blue.r, SchemeManager.colour_blue.g, SchemeManager.colour_blue.b, 1.0f);
              //g_shaderDualPeel.setUniform1fv(gl2, "Alpha", new float[]{1.0f});
              //g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
              //comp.boundingBox.renderBoundingBox(gl2);   
              //g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
           //} else if (SSM.relatedList.size() > 0 && SSM.relatedList.contains(comp.id)) {
              //g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
              //g_shaderDualPeel.setUniformf(gl2, "compColour", SchemeManager.colour_related.r, SchemeManager.colour_related.g, SchemeManager.colour_related.b, 1.0f);
              //comp.renderBufferAdj(gl2, SchemeManager.colour_related); 
           //}

        }
        SSM.g_numGeoPasses++;
     }     
     
     public int currId;
     public int[]  g_dualBackBlenderFboId = new int[1];
     public int[]  g_dualPeelingSingleFboId = new int[1];
     public int[]  g_dualDepthTexId = new int[2];
     public int[]  g_dualFrontBlenderTexId = new int[2];
     public int[]  g_dualBackTempTexId = new int[2];
     public int[]  g_dualBackBlenderTexId = new int[1]; 
     
     public int g_drawBuffers[] = {GL2.GL_COLOR_ATTACHMENT0,
           GL2.GL_COLOR_ATTACHMENT1,
           GL2.GL_COLOR_ATTACHMENT2,
           GL2.GL_COLOR_ATTACHMENT3,
           GL2.GL_COLOR_ATTACHMENT4,
           GL2.GL_COLOR_ATTACHMENT5,
           GL2.GL_COLOR_ATTACHMENT6
     };   
     
     
  	  public int g_quadDisplayList;
     public final static float MAX_DEPTH = 1.0f;  	  
//     public boolean g_useOQ = true;
     public boolean g_useOQ = false;
     public int[] g_queryId = new int[1];
//     public float[] g_opacity = new float[]{0.6f};     
     public float[] g_opacity = new float[]{0.5f};     
//     public int g_numPasses = 4;
//     public int g_numPasses = 8;
     
     
     public ShaderObj g_shaderDualInit;
     public ShaderObj g_shaderDualPeel;
     public ShaderObj g_shaderDualBlend;
     public ShaderObj g_shaderDualFinal;    
       
   
   
   
}
