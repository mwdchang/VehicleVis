package model;

import java.awt.Color;
import java.awt.Font;
import java.nio.IntBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import touch.WCursor;
import util.DCCamera;
import util.GraphicUtil;
import util.TextureFont;
import TimingFrameExt.FloatEval;

import com.jogamp.opengl.util.GLBuffers;

import datastore.CacheManager;
import datastore.Const;
import datastore.HierarchyTable;
import datastore.MM;
import datastore.SSM;
import datastore.SchemeManager;
import exec.Event;
import gui.DCTip;
import gui.StatusWindow;

/////////////////////////////////////////////////////////////////////////////////
// This class is responsible for rendering the 3D part of the visualizaiton
/////////////////////////////////////////////////////////////////////////////////
public class ModelRenderer extends BaseModelRenderer {
   
   /* To keep compiler happy */
   private static final long serialVersionUID = 1L;
   
   public static GLU glu = new GLU();
   
   public float vpadding = 15.0f;
   public float rpadding = 30.0f;
   public float lpadding = 30.0f;
   public float spadding = 40.0f;
   
   
   public float OUTLINE_DOWN_SAMPLE = 1.5f;
   public float GLOW_DOWN_SAMPLE    = 1.5f;
   
  
   ////////////////////////////////////////////////////////////////////////////////
   // Default constructor
   ////////////////////////////////////////////////////////////////////////////////
   public ModelRenderer(){
      super();
   }     
   
   

   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Rendering
   ////////////////////////////////////////////////////////////////////////////////
   public void render(GL2 gl2) {
      gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
      // To avoid threading issues, lets
      // just put the update GL stuff here
      if (SSM.dirtyGL == 1) {
         resetDataGL(gl2);
         SSM.dirtyGL = 0;
      }
      
      //if (SSM.use3DModel == true) {
         this.renderIntegratedView(gl2);
      //} else {
      //   this.renderChartsOnly(gl2);
      //}
      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render a comparison outline
   ////////////////////////////////////////////////////////////////////////////////
   public void renderComparison(GL2 gl2) {
      if (SSM.useComparisonMode == true) {
         float size;
         float c;
         outlineTexture1.startRecording(gl2);
            setPerspectiveView(gl2);
            
            basicTransform(gl2);
            gl2.glClearColor(0, 0, 0, 0);
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            gl2.glDisable(GL2.GL_DEPTH_TEST);
            gl2.glEnable(GL2.GL_BLEND);
            
            c = 1;
            size = 2+MM.currentModel.componentTable.size();
            gl2.glPushMatrix();
            for (DCComponent comp : MM.currentModel.componentTable.values()) {
               
               if (comp.hasContext && comp.active && ! SSM.selectedGroup.contains(comp.id)) {
                  float v1 = CacheManager.instance().groupOccurrence.get(comp.id);
                  float v2 = CacheManager.instance().c_groupOccurrence.get(comp.id);                  
                  if (v1 > v2) {
                     double v = 0.4 + 0.6*(v1-v2)/v1;
                     comp.renderBuffer(gl2, DCColour.fromDouble(0.5, 0.0, v, 0.2), 2);
                  }
               }
               c ++;
            } // end for
            gl2.glPopMatrix();
         outlineTexture1.stopRecording(gl2);
         GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         outlineTexture1.renderComparison(gl2, 1.0f, SchemeManager.comp_1, SchemeManager.comp_2);
         
         
         outlineTexture2.startRecording(gl2);
            setPerspectiveView(gl2);
            basicTransform(gl2);
            gl2.glClearColor(0, 0, 0, 0);
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            gl2.glDisable(GL2.GL_DEPTH_TEST);
            gl2.glEnable(GL2.GL_BLEND);
            
            c = 1;
            size = 2+MM.currentModel.componentTable.size();
            gl2.glPushMatrix();
            for (DCComponent comp : MM.currentModel.componentTable.values()) {
               
               if (comp.hasContext && comp.active && ! SSM.selectedGroup.contains(comp.id)) {
                  float v1 = CacheManager.instance().groupOccurrence.get(comp.id);
                  float v2 = CacheManager.instance().c_groupOccurrence.get(comp.id);                  
                  if (v1 < v2) {
                     double v = 0.4 + 0.6*(v2-v1)/v2;
                     comp.renderBuffer(gl2, DCColour.fromDouble(0.0, 0.5, v, 0.2), 2);
                  }
               }
               c ++;
            } // end for
            gl2.glPopMatrix();

         outlineTexture2.stopRecording(gl2);
         GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         outlineTexture2.renderComparison(gl2, 1.0f, SchemeManager.comp_1, SchemeManager.comp_2);
      }      
   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Main rendering method
   ////////////////////////////////////////////////////////////////////////////////
   public void renderIntegratedView(GL2 gl2) {
      
      ////////////////////////////////////////////////////////////////////////////////
      // Need to re-adjust the buffer size if the screen size is changed
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.refreshOITBuffers == true) {
         this.DeleteDualPeelingRenderTargets(gl2);
         this.InitDualPeelingRenderTargets(gl2);
         SSM.refreshOITTexture = true;
         SSM.refreshOITBuffers = false;   
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Initialize the glowTexture
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.refreshGlowTexture == true) {
        glowTexture= new FrameBufferTexture();
        glowTexture.TEXTURE_SIZE_W = (int)((float)SSM.windowWidth/GLOW_DOWN_SAMPLE);
        glowTexture.TEXTURE_SIZE_H = (int)((float)SSM.windowHeight/GLOW_DOWN_SAMPLE);
        //glowTexture.TEXTURE_SIZE_W = SSM.instance().windowWidth /4;
        //glowTexture.TEXTURE_SIZE_H = SSM.instance().windowHeight/ 4;
        glowTexture.init(gl2);
        
        // Redo shader inits
        glowTexture.shader.createShader(gl2, Const.SHADER_PATH+"vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
        glowTexture.shader.createShader(gl2, Const.SHADER_PATH+"frag_fbt_white.glsl", GL2.GL_FRAGMENT_SHADER);
        glowTexture.shader.createProgram(gl2);
        
        gl2.glBindAttribLocation(glowTexture.shader.programID,  0, "in_position");
        gl2.glBindAttribLocation(glowTexture.shader.programID,  1, "in_colour");
        gl2.glBindAttribLocation(glowTexture.shader.programID,  2, "in_texcoord");      
        
        glowTexture.shader.linkProgram(gl2);
        glowTexture.shader.bindFragColour(gl2, "outColour");   
        
        SSM.refreshGlowTexture = false;
        
        
        // Down sample
        outlineTexture1 = new FrameBufferTexture();
        outlineTexture1.TEXTURE_SIZE_W = (int)((float)SSM.windowWidth / OUTLINE_DOWN_SAMPLE);
        outlineTexture1.TEXTURE_SIZE_H = (int)((float)SSM.windowHeight / OUTLINE_DOWN_SAMPLE);
        outlineTexture1.init(gl2);
        outlineTexture1.shader.createShader(gl2, Const.SHADER_PATH+"vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
        outlineTexture1.shader.createShader(gl2, Const.SHADER_PATH+"frag_fbt_white2.glsl", GL2.GL_FRAGMENT_SHADER);
        outlineTexture1.shader.createProgram(gl2);
        gl2.glBindAttribLocation(outlineTexture1.shader.programID, 0, "in_position");
        gl2.glBindAttribLocation(outlineTexture1.shader.programID, 1, "in_colour");
        gl2.glBindAttribLocation(outlineTexture1.shader.programID, 2, "in_texcoord");
        outlineTexture1.shader.linkProgram(gl2);
        outlineTexture1.shader.bindFragColour(gl2, "outColour");
        
        
        outlineTexture2 = new FrameBufferTexture();
        outlineTexture2.TEXTURE_SIZE_W = (int)((float)SSM.windowWidth / OUTLINE_DOWN_SAMPLE);
        outlineTexture2.TEXTURE_SIZE_H = (int)((float)SSM.windowHeight / OUTLINE_DOWN_SAMPLE);
        outlineTexture2.init(gl2);
        outlineTexture2.shader.createShader(gl2, Const.SHADER_PATH+"vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
        outlineTexture2.shader.createShader(gl2, Const.SHADER_PATH+"frag_fbt_white2.glsl", GL2.GL_FRAGMENT_SHADER);
        outlineTexture2.shader.createProgram(gl2);
        gl2.glBindAttribLocation(outlineTexture2.shader.programID, 0, "in_position");
        gl2.glBindAttribLocation(outlineTexture2.shader.programID, 1, "in_colour");
        gl2.glBindAttribLocation(outlineTexture2.shader.programID, 2, "in_texcoord");
        outlineTexture2.shader.linkProgram(gl2);
        outlineTexture2.shader.bindFragColour(gl2, "outColour");
       
     }      
      
    
      

      ////////////////////////////////////////////////////////////////////////////////
      // Render any scenes that we want to cache...ie: Lens, Filters
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.refreshMagicLens == true ||
         MM.currentModel.isAnimationRunning()) {
         for (int i=0;i < SSM.lensList.size(); i++) {
            LensAttrib la = SSM.lensList.elementAt(i);   
            //if (la.mlen == null) {
               la.mlen = new MagicLens();   
               la.mlen.init(gl2);
               la.displayList = this.MakeFullScreenQuad(gl2);
            //}
            if (la.t_top == null) {
               la.t_top = new TextureFont();   
               la.t_top.width  = 40;
               la.t_top.height = 40;
               la.l_top = -1;
            }
            if (la.t_bottom == null) {
               la.t_bottom = new TextureFont();    
               la.t_bottom.width  = 40;
               la.t_bottom.height = 40;
               la.l_bottom = -1;
            }
            
            
            
            setPerspectiveView(gl2, la.nearPlane, la.farPlane); {
               basicTransform(gl2);
               gl2.glEnable(GL2.GL_BLEND);
               this.ProcessDualPeeling(gl2, la.displayList);
            }
            
            la.mlen.startRecording(gl2); {
               //this.RenderDualPeeling(gl2, la.displayList);
               this.RenderDualPeeling(gl2, la.displayList);
               
               gl2.glEnable(GL2.GL_BLEND);
               gl2.glDisable(GL2.GL_DEPTH_TEST);
               setPerspectiveView(gl2, 0.01f, la.nearPlane); {
                  basicTransform(gl2);
                  renderSil(gl2);   
               }               
            } la.mlen.stopRecording(gl2);
            
            
            /*
            la.mlen.startRecording(gl2); {
               // Do not render again in dual depthing peeling mode, 
               // We already have transparency and it is too expensive
               if ( ! SSM.instance().useDualDepthPeeling) {
                  setPerspectiveView(gl2, la.nearPlane, la.farPlane); {
                     basicTransform(gl2);
                     gl2.glEnable(GL2.GL_BLEND);
                     renderColourRamp(gl2, la);
                  }
                  setPerspectiveView(gl2, 0.01f, la.nearPlane); {
                     basicTransform(gl2);
                     renderSil(gl2);   
                  }
               }
            } la.mlen.stopRecording(gl2);
            */
            
            
         }
         SSM.refreshMagicLens = false;
      }
      
    
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render the default scene we want to show
      ////////////////////////////////////////////////////////////////////////////////
      //setPerspectiveView(gl2, 40, 1000); {
      setPerspectiveView(gl2); {
         basicTransform(gl2);
         gl2.glEnable(GL2.GL_BLEND);
         
         if (SSM.useDualDepthPeeling) {
            
            // 1) Default, use dual peeling and render in colour ramp mode
            if (SSM.refreshOITTexture) {
               this.ProcessDualPeeling(gl2, this.g_quadDisplayList);
               this.RenderDualPeeling(gl2, this.g_quadDisplayList);
            } else {
               this.RenderOITTexture(gl2);   
            }
            
            // 2) Some funny stuff to try out...lets do face normals
            //this.renderFNormal(gl2);
            /*
            for (DCComponent comp: MM.currentModel.componentTable.values()) {
               comp.renderBuffer(gl2, DCColour.fromInt(200, 0, 0), DCComponent.GOOCH_SHADING);   
               gl2.glLineWidth(1.5f);
               comp.renderBufferAdj(gl2, DCColour.fromInt(0, 0, 0));
               gl2.glLineWidth(1.0f);
            }
            */
         } else {
            renderColourRamp(gl2, null);
         }
         setProjectedCoord(gl2);
         float coord[] = MM.currentModel.getMaxMinScreenX(gl2);
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render any filters we want to show
      ////////////////////////////////////////////////////////////////////////////////
      for (int i=0; i < SSM.lensList.size(); i++) {
         LensAttrib la = SSM.lensList.elementAt(i);
         setOrthonormalView(gl2); {
            if (la.mlen != null) {
              la.mlen.renderLens( gl2, la );
              // TODO: This is probably not very efficient, can we get away with just a single render for the entire render cycle at the end ???
              //this.renderComparison(gl2);
              
              /*
              if (la.tip.tf == null) la.tip.init(gl2);
              la.tip.clear();
              la.tip.addText(la.nearPlane+"");
              la.tip.setTip( 400, 400, SSM.windowWidth, SSM.windowHeight);                
              */
              /*
              la.tip.setTip( la.magicLensX,
                    SSM.windowHeight-la.magicLensY,
                    SSM.windowWidth, SSM.windowHeight);                
                    */
              //la.tip.render(gl2);
            }
         }
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Record glow effects and render to to a 1-1 square in ortho mode 
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.useGlow && SSM.selectedGroup.size() > 0) {
         glowTexture.startRecording(gl2); 
            setPerspectiveView(gl2); 
            basicTransform(gl2);
            gl2.glClearColor(1, 1, 1, 0);
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            gl2.glPushMatrix();
            for (DCComponent comp : MM.currentModel.componentTable.values()) {
               if ( SSM.selectedGroup.contains(comp.id) ) {
                  //gl2.glScaled(1.2, 1.2, 1.2);
                  //comp.renderBuffer(gl2, DCColour.fromInt(20, 20, 210));
                  comp.renderBuffer(gl2, SchemeManager.selected);
                  //gl2.glScaled(1.0/1.2, 1.0/1.2, 1.0/1.2);
               }
               
            }          
            gl2.glPopMatrix();
         glowTexture.stopRecording(gl2);
         
         GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         glowTexture.render(gl2, 2);
      }       
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render any 2D components
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.showLabels == true) {
         for (int i=0; i < SSM.lensList.size(); i++) {
            setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
               if (SSM.useCircularLabel == true) {
                  renderLabelCircular(gl2, SSM.lensList.elementAt(i));
               } else {
                  renderLabelBalanced(gl2, SSM.lensList.elementAt(i));
               }
            }
         }
      }
      
      
      // TODO : Move this out later...just to test if animation can work, probably have a flag and put in resetDataGL or soemthing
      if (SSM.resizePanel == 1) {
         if (dcTextPanel.animatorH != null) dcTextPanel.animatorH.stop();
         if (dcTextPanel.animatorW != null) dcTextPanel.animatorW.stop();
         
         float goalH = SSM.docActive ? SSM.docHeight : 0.0f;
         float goalW = SSM.docActive ? SSM.docWidth : 0.0f;
         
         dcTextPanel.animatorH = PropertySetter.createAnimator(600, dcTextPanel, "displayH", new FloatEval(), dcTextPanel.displayH, goalH);
         dcTextPanel.animatorW = PropertySetter.createAnimator(600, dcTextPanel, "displayW", new FloatEval(), dcTextPanel.displayW, goalW);
         dcTextPanel.animatorH.start();
         dcTextPanel.animatorW.start();
         SSM.resizePanel = 0;   
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Show the document viewer box
      ////////////////////////////////////////////////////////////////////////////////
      //if (SSM.instance().docVisible == true) {
      if (dcTextPanel.displayH >= 0.1) {
         setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
            dcTextPanel.render(gl2);
         }
      }
     
      
      ////////////////////////////////////////////////////////////////////////////////
      // Optional, debugging elements
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.useGuide == true) {
         
         setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
            StatusWindow.tf.anchorX = SSM.windowWidth - StatusWindow.tf.width;
            StatusWindow.tf.anchorY = 10; //StatusWindow.tf.height;
            StatusWindow.render(gl2);
            
            // Lets render the model bounding box in screen space
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl2.glColor4d(0, 0, 0, 1);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glVertex2d(MM.currentModel.minx, MM.currentModel.miny);
               gl2.glVertex2d(MM.currentModel.maxx, MM.currentModel.miny);
               gl2.glVertex2d(MM.currentModel.maxx, MM.currentModel.maxy);
               gl2.glVertex2d(MM.currentModel.minx, MM.currentModel.maxy);
            gl2.glEnd();
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            
            // Lets render a grid for aligning thingys
            GraphicUtil.drawGrid(gl2, 0, 0, SSM.windowWidth, SSM.windowHeight, 50);
            
         }
         
      }         
      
     
      ////////////////////////////////////////////////////////////////////////////////
      // Renders tooltips and scroll bar for the lens
      ////////////////////////////////////////////////////////////////////////////////
      setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
         //DCTip.render(gl2);
         for (DCTip tip: SSM.tooltips.values()) {
            tip.render(gl2);
         }
         
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         for (int i=0; i < SSM.lensList.size(); i++) {
            LensAttrib la = SSM.lensList.elementAt(i);   
            
            if (la.magicLensSelected == 1) {
               gl2.glColor4fv( SchemeManager.selected.toArray(), 0);
            } else {
               gl2.glColor4fv( SchemeManager.unselected.toArray(), 0);
            }
            
            GraphicUtil.drawArc(gl2, la.magicLensX, (SSM.windowHeight-la.magicLensY), 0, 
                  la.magicLensRadius, la.magicLensRadius+la.borderSize, 0, 360, 72, 1);
            
            // If the handle is selected, we rendered it in the selected colour
            // and make it slightly bigger so the users would know
            if (SSM.useTUIO == true) {
               if (SSM.lensList.elementAt(i).handleSelected) {
                  gl2.glColor4fv( SchemeManager.selected.toArray(), 0);
                  GraphicUtil.drawArc(gl2, la.magicLensX, (SSM.windowHeight-la.magicLensY), 0, 
                        la.magicLensRadius, la.magicLensRadius+47, 
                        la.handleAngle-1, la.handleAngle+31, 20);
               } else {
                  gl2.glColor4fv( SchemeManager.unselected.toArray(), 0);
                  GraphicUtil.drawArc(gl2, la.magicLensX, (SSM.windowHeight-la.magicLensY), 0, 
                        la.magicLensRadius, la.magicLensRadius+45, 
                        la.handleAngle, la.handleAngle+30, 20);
               }
                  
               // Draw a special marker if the near plane is at the minimum or at the maximum
               /*
               if (la.nearPlane <= 1.1) {
                  gl2.glColor4d(0.5, 0.5, 0.5, 0.6);
                  GraphicUtil.drawArc(gl2, la.magicLensX, (SSM.windowHeight-la.magicLensY), 0, 
                        la.magicLensRadius, la.magicLensRadius+40, 
                        la.nearPlane, la.nearPlane+1, 2);
                     
               }
               */
            }
         }
         
         /*
         for (int i=0; i < SSM.lensList.size(); i++) {
            LensAttrib la = SSM.lensList.elementAt(i);   
            if (la.mlen != null && la.magicLensSelected == 1 ) {
               gl2.glColor4d(0.7, 0.7, 0.7, 0.6);
               gl2.glBegin(GL2.GL_QUADS);
                  gl2.glVertex2d(la.magicLensX + la.magicLensRadius+10,   SSM.windowHeight-(la.magicLensY-100));
                  gl2.glVertex2d(la.magicLensX + la.magicLensRadius+20,   SSM.windowHeight-(la.magicLensY-100));
                  gl2.glVertex2d(la.magicLensX + la.magicLensRadius+20,   SSM.windowHeight-(la.magicLensY+100));
                  gl2.glVertex2d(la.magicLensX + la.magicLensRadius+10,   SSM.windowHeight-(la.magicLensY+100));
               gl2.glEnd();
            }
         }
         */
         
         // Render lens tip
         /*
         for (int i=0; i < SSM.lensList.size(); i++) {
            LensAttrib la = SSM.lensList.elementAt(i);         
            if (la.mlen != null) {
               if (la.tip.tf == null) la.tip.init(gl2);
               la.tip.clear();
               la.tip.addText(la.nearPlane+"");
               la.tip.tf.opacity = la.tip.opacity;
               //la.tip.setTip( 400, 400, SSM.windowWidth, SSM.windowHeight);                
               la.tip.setTip( la.magicLensX,
                     SSM.windowHeight-la.magicLensY,
                     SSM.windowWidth, SSM.windowHeight);                
               la.tip.render(gl2);               
            }
         }
         */
      } // end setOrtho
      
      

            
   }   
   
   
   
   public static float CHART_ANCHOR_X = 60;
   public static float CHART_ANCHOR_Y = 200;
   public static float CHART_GAP_W = 200;
   public static float CHART_GAP_H =  100;
   public static float CHART_NUM = 6;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render only the chart
   // No 3D model
   ////////////////////////////////////////////////////////////////////////////////
   public void renderChartsOnly(GL2 gl2) {
      String list[] = this.getComponentUnsorted(gl2);
      int startIdx = CacheManager.instance().getDateKey( SSM.startTimeFrame ) == null ? 0:
         CacheManager.instance().getDateKey( SSM.startTimeFrame );
      int endIdx   = CacheManager.instance().getDateKey( SSM.endTimeFrame) == null ? CacheManager.instance().timeLineSize:
         CacheManager.instance().getDateKey( SSM.endTimeFrame );      
     
      int counter = 0;
      Hashtable<String, String> uniqueTable = new Hashtable<String, String>();
      
      float startX = CHART_ANCHOR_X;
      float startY = CHART_ANCHOR_Y;
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render the heat maps
      ////////////////////////////////////////////////////////////////////////////////
      setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
         for (String key : list) {
            DCComponent comp = MM.currentModel.componentTable.get(key);
            
            if (comp.hasContext == false) continue;
            if (comp.id < 0) continue;
            
            
            // If local mode than don't render components that are not related
            comp.cchart.active = true;
            if (SSM.useLocalFocus == true) {
               if (SSM.selectedGroup.size() > 0 && ! SSM.relatedList.contains(comp.id))  {
                  comp.cchart.active = false;
               } else {
                  comp.cchart.active = true;   
               }
            }             
            

            // Check parent and model table capability in aggregation mode
            boolean skip = false;
            if (SSM.useAggregate == true) {
               Integer parentId = comp.id;
               while(true) {
                  parentId = HierarchyTable.instance().getParentId(parentId);   
                  
                  if (parentId == null) break;
                  if (MM.currentModel.componentTableById.get(parentId) != null) {
                     skip = true;
                     break;
                  }
               } // end while            
            } else {
               skip = false; 
            }
            if (skip == true) continue;
            
            
            
            
            if (uniqueTable.contains(comp.baseName)) continue;
            uniqueTable.put(comp.baseName, comp.baseName);
            
            int occ[] = this.getOccCounts(comp, startIdx, endIdx);
            
            comp.cchart.anchorX = startX;
            comp.cchart.anchorY = startY;
            comp.cchart.colour = comp.colour;
            comp.cchart.renderBorder(gl2);
            comp.cchart.render(gl2);
            
            comp.cchart.tf.width = comp.cchart.width;
            comp.cchart.tf.height = comp.cchart.height;
            comp.cchart.tf.anchorX = comp.cchart.anchorX;
            comp.cchart.tf.anchorY = comp.cchart.anchorY;
            if (SSM.useComparisonMode==true) {
               comp.cchart.setLabel(comp.baseName + " " + occ[2] + "/" + occ[0]);
            } else {
               comp.cchart.setLabel(comp.baseName + " " + (occ[2]+occ[3]) + "/" + (occ[0]+occ[1]));
            }
            
            //comp.cchart.setLabel(comp.baseName);
            comp.cchart.tf.render(gl2);
            counter += 1;
            
            startY += (comp.cchart.height + 10);
            if (startY > 700) {
               startX += 200;
               startY = CHART_ANCHOR_Y;
            }
            
         } // end for
      } // end ortho
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render the scrollable filters
      ////////////////////////////////////////////////////////////////////////////////
      //this.renderScrollFilter(gl2);
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Render the text panel
      ////////////////////////////////////////////////////////////////////////////////
      this.dcTextPanel.displayH = SSM.docHeight;
      this.dcTextPanel.displayW = SSM.docWidth;
      SSM.docAnchorX = SSM.windowWidth - 1.1f * SSM.docWidth;
      SSM.docActive = true;
      this.dcTextPanel.render(gl2);
      this.dcTextPanel.displayH = 0;
      this.dcTextPanel.displayW = 0;
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Renders a tool tip
      ////////////////////////////////////////////////////////////////////////////////
      setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight); {
         //DCTip.render(gl2);
         for (DCTip tip : SSM.tooltips.values()) {
            tip.render(gl2);
         }
      }      
   }
   
   public Integer pickingChartsOnly(GL2 gl2, float px, float py) {
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      this.startPickingOrtho(gl2, buffer, px, py);      
      
      String list[] = this.getComponentUnsorted(gl2);
      int counter = 0;
      Hashtable<String, String> uniqueTable = new Hashtable<String, String>();
      
      float startX = CHART_ANCHOR_X;
      float startY = CHART_ANCHOR_Y;      
      
      for (String key : list) {
         DCComponent comp = MM.currentModel.componentTable.get(key);
         
         
         if (comp.hasContext == false) continue;
         if (comp.id < 0) continue;
         
         // If local mode than don't render components that are not related
         comp.cchart.active = true;
         if (SSM.useLocalFocus == true) {
            if (SSM.selectedGroup.size() > 0 && ! SSM.relatedList.contains(comp.id))  {
               comp.cchart.active = false;
            } else {
               comp.cchart.active = true;   
            }
         }             
         

         // Check parent and model table capability in aggregation mode
         boolean skip = false;
         if (SSM.useAggregate == true) {
            Integer parentId = comp.id;
            while(true) {
               parentId = HierarchyTable.instance().getParentId(parentId);   
               
               if (parentId == null) break;
               if (MM.currentModel.componentTableById.get(parentId) != null) {
                  skip = true;
                  break;
               }
            } // end while            
         } else {
            skip = false; 
         }
         if (skip == true) continue;         
         
         
         if (uniqueTable.contains(comp.baseName)) continue;
         uniqueTable.put(comp.baseName, comp.baseName);
         
         comp.cchart.anchorX = startX;
         comp.cchart.anchorY = startY;
         gl2.glLoadName(comp.cchart.id);
         gl2.glPushMatrix();
            comp.cchart.renderBorder(gl2);
         gl2.glPopMatrix();   
         
         counter += 1;
         
         startY += (comp.cchart.height + 10);
         if (startY > 700) {
            startX += 200;
            startY = CHART_ANCHOR_Y;
         }
     
         
      } // end for
      
      return finishPicking(gl2, buffer);
   }
  
   
   

   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Rules
   //
   // 1) If object A is picked and A is currently selected, then de-select A
   // 2) If object A is picked and B is currently selected, then de-select A and select B
   // 3) If object A is picked and nothing is currently selected, then select A
   ////////////////////////////////////////////////////////////////////////////////
   public void picking(GL2 gl2, float px, float py) {
      // Quickie way to get out and save unnecessary rendering 
      if (SSM.l_mouseClicked == false) return;
System.out.println("Before ModelRenderer Picking : " + SSM.stopPicking);      
      if (SSM.stopPicking == 1) return;
System.out.println("After ModelRenderer Picking : " + SSM.stopPicking);      
      
      
      // Force trigger depth peel re-render on mouse press action
      SSM.refreshOITTexture = true;
      
      
      float mx = px;
      float my = SSM.windowHeight - py;
      
     
      // If is dirty then skip...something is already updating
      if (SSM.dirty == 1 || SSM.dirtyGL == 1) {
         System.out.println("........ dirty exit ........");
         return;
      }
      
      
      for (int i=0; i < SSM.lensList.size(); i++) {
         Integer obj = picking2DBalanced(gl2, SSM.lensList.elementAt(i), px, py);
         // Speical
         if (obj!= null && (obj == 9999 || obj == 8888)) {
            LensAttrib la = SSM.lensList.elementAt(i);
            System.out.println("Clicked either up or down lens");
            
            if (obj == 8888)
               la.start += la.numToDisplay;
            else if (obj == 9999)
               la.start -= la.numToDisplay;
            
            System.out.println("Lens attrib : " + la.start + " " + la.numToDisplay);
            return;
         }
         if (obj != null) break;
      }
      
      
      
      Integer obj = null;
      if (SSM.use3DModel == true) {
         // Check 3D first, then 2D
         obj = picking3D(gl2, px, py);
         if (obj == null) {
            for (int i=0; i < SSM.lensList.size(); i++) {
               obj = picking2DBalanced(gl2, SSM.lensList.elementAt(i), px, py);
               if (obj != null) break;
            }
         }
      } else {
         obj = pickingChartsOnly(gl2, px, py);
      }
      
      
      // This can be a select or a de-select
      /*
      if (obj == null) {
         SSM.selectedGroup.clear();
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1; // for the text panel
         SSM.instance().t1Start = 0;
         SSM.instance().t2Start = SSM.instance().globalFetchSize;
         SSM.instance().yoffset = SSM.instance().docHeight;
         SSM.instance().docMaxSize = 0;
         return;   
      }
      */
      
      
      if (obj != null) {
         
         // Disable any action if in local focus mode and 
         // the part clicked is not related nor selected
         if (SSM.useLocalFocus == true) {
            if (SSM.selectedGroup.size() > 0 &&  !SSM.relatedList.contains(obj)) return;
         }
         
         // If TUIO messages are present, then use toggle mode. 
         // Otherwise we have the mouse, and utilize the keyboard shift key modes
         if (SSM.useTUIO == true) {
            Event.handleTUIOSelect(obj);
         } else {
            Event.handleTUIOSelect(obj);
            //Event.handleMouseSelect(obj);
         }
         
      } else {
         if (SSM.useTUIO == false) return;
         
         // Check if we are hitting the bounding box of the objects
         // if we are than do nothing
         IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
         startPickingPerspective(gl2, buffer, px, py);
         gl2.glLoadName(987);
         gl2.glPushMatrix();
            basicTransform(gl2);
            MM.currentModel.mbox.renderBoundingBox(gl2); 
         gl2.glPopMatrix();   
         Integer obj2 = this.finishPicking(gl2, buffer);
         System.out.println("Obj 2: " + obj2);
         if (obj2 != null) return;
         System.out.println("Obj 2: " + obj2);
         
         
         boolean canCreateDocumentPanel = true;
         for (int i=0; i < SSM.lensList.size(); i++) {
            LensAttrib la = SSM.lensList.elementAt(i);
            float r = la.magicLensRadius;
            float xx = px - la.magicLensX;
            float yy = py - la.magicLensY;
            float d  = (float)Math.sqrt(xx*xx + yy*yy);
            if (d < r) canCreateDocumentPanel = false;
            
         }
         if (canCreateDocumentPanel) {
            if (SSM.docActive == false) {
               SSM.docActive = true;
               SSM.docAnchorX = mx;
               SSM.docAnchorY = my;
               SSM.resizePanel = 1;
            }
         }
         
         
         /* Old code to clear selection on selection of empty space
         SSM.selectedGroup.clear();
         SSM.dirty = 1;
         SSM.dirtyGL = 1; // for the text panel
         SSM.t1Start = 0;
         SSM.t2Start = SSM.globalFetchSize;
         SSM.yoffset = SSM.docHeight;
         SSM.docMaxSize = 0;
         */
      }
      
      
   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Picking with more or less balanced labels
   ////////////////////////////////////////////////////////////////////////////////
   public Integer picking2DBalanced(GL2 gl2, LensAttrib la, float px, float py) {
      //if (SSM.instance().l_mouseClicked == false) return;
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      this.startPickingOrtho(gl2, buffer, px, py);      
      
      String list[] = this.getComponentSortedByProjY(gl2);    
      float rightHeight = 0;
      float leftHeight = 0;
      
      
      
      Vector<DCComponent> rightList = new Vector<DCComponent>();
      Vector<DCComponent> leftList  = new Vector<DCComponent>();
      
      float lensRadius = la.magicLensRadius;
      float lensX = la.magicLensX;
      float lensY = la.magicLensY;
      
      
      
      // New padding, always apply outside of the 3D model, in addition 
      // position with respect to the radius size ie: do not go "insde" the circumference
      rpadding = Math.abs( MM.currentModel.maxx - lensX);
      lpadding = Math.abs( MM.currentModel.minx - lensX);
      if (rpadding > lensRadius) {
         rpadding -= lensRadius;
      } else {
         rpadding = vpadding;
      }
      if (lpadding > lensRadius) {
         lpadding -= lensRadius;
      } else { 
         lpadding = vpadding; 
      }
      
      // check if the paddings are out of bound (ie: when we are close up)
      // default the padding space to space padding
      if ( rpadding + lensX > SSM.windowWidth ) {
         rpadding = spadding;   
      }
      if (lensX - lpadding < 0) {
         lpadding = spadding;   
      }      
      
      Hashtable<String, String> tmp = new Hashtable<String, String>();
      
      // This need to be in order
      if (la.start > list.length) la.start -= la.numToDisplay; 
      if (la.start <= 0) la.start = 0;
      int laCnt = 0;
      
      // First filter into left and right list
      for (int i=0; i < list.length; i++) {
         DCComponent comp = MM.currentModel.componentTable.get(list[i]);
         // If the centroid point is before the lens's near plane, do not render the label
         if (DCCamera.instance().eye.sub(comp.center).mag() <= la.nearPlane) continue;
         
         // If the object has 0 occurrences, do not render the label
         if (comp.hasContext == false) continue;         
         
         // If local mode than don't render components that are not related
         comp.cchart.active = true;
         if (SSM.useLocalFocus == true) {
            if (SSM.selectedGroup.size() > 0 && ! SSM.relatedList.contains(comp.id)) {
               comp.cchart.active = false;   
            } else {
               comp.cchart.active = true;   
            }
         }
         
         
         
         // Check parent and model table capability in aggregation mode
         boolean skip = false;
         if (SSM.useAggregate == true) {
            Integer parentId = comp.id;
            while(true) {
               parentId = HierarchyTable.instance().getParentId(parentId);   
               
               if (parentId == null) break;
               if (MM.currentModel.componentTableById.get(parentId) != null) {
                  skip = true;
                  break;
               }
            } // end while            
         } else {
            skip = false; 
         }
         if (skip == true) continue;
         
         
         //float xx = comp.projCenter.x - lensX;
         //float yy = comp.projCenter.y - (SSM.windowHeight - lensY);
         float xx = la.zoomFactor*(comp.projCenter.x - lensX);
         float yy = la.zoomFactor*(comp.projCenter.y - (SSM.windowHeight - lensY));
         
         float c = (float)Math.sqrt(xx*xx + yy*yy);
         
         if ( c <= lensRadius ) {
            // If the base object is already registered, skip the rendering
            // so we don't draw so many guide lines
            if (tmp.contains(comp.baseName)) continue; 
            tmp.put(comp.baseName, comp.baseName);
            
            if (laCnt >= la.start && laCnt < la.start+la.numToDisplay) {
               this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
               //this.alternateSideLayout(comp, la, rightList, leftList, i);
               //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
            }
            laCnt ++;
            

            //this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
            //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
         }
      } // end for
      
      // Now actually render the labels
      rightHeight = Math.min((SSM.windowHeight-la.magicLensY) + (rightList.size()/2)*(SSM.sparkLineHeight + vpadding),
                              SSM.windowHeight-SSM.sparkLineHeight-vpadding);
      leftHeight  = Math.min((SSM.windowHeight-la.magicLensY) + (leftList.size()/2)*(SSM.sparkLineHeight + vpadding),
                              SSM.windowHeight-SSM.sparkLineHeight-vpadding); 
      
      
      // Right side
      for (int i=0; i < rightList.size(); i++) {
         DCComponent comp = rightList.elementAt(i);
         if (comp.id < 0) continue;
            
         String txt = comp.cname+"(" + CacheManager.instance().groupOccurrence.get(comp.id) + ")";
            
         // double size[] = FontRenderer.instance().getDimension(txt);      
         double size[] = GraphicUtil.getFontDim(txt);
         rightHeight -= Math.max(size[1], comp.cchart.height);
         rightHeight -= vpadding;
               
         comp.cchart.setAnchor(lensX+lensRadius+rpadding+spadding, rightHeight);
         gl2.glLoadName(comp.cchart.id);
         gl2.glPushMatrix();
            comp.cchart.renderBorder(gl2);
         gl2.glPopMatrix();
      }
      
      
      // Left Side
      for (int i=0; i < leftList.size(); i++) {
         DCComponent comp = leftList.elementAt(i);
         if (comp.id < 0) continue;
            
         String txt = comp.cname+"(" + CacheManager.instance().groupOccurrence.get(comp.id) + ")";
            
         //double size[] = FontRenderer.instance().getDimension(txt);      
         double size[] = GraphicUtil.getFontDim(txt);
         leftHeight -= Math.max(size[1], comp.cchart.height);
         leftHeight -= vpadding;
        
         //gl2.glTranslated(lensX-lensRadius-lpadding-comp.sparkLine.width, leftHeight, 0);
         comp.cchart.setAnchor(lensX-lensRadius-lpadding-comp.cchart.width-spadding, leftHeight);
         gl2.glLoadName(comp.cchart.id);
         gl2.glPushMatrix();
            comp.cchart.renderBorder(gl2);
         gl2.glPopMatrix();
      }      
      
      // Draw a down and up for scrolling
      gl2.glLoadName(9999);
      gl2.glPushMatrix();
      gl2.glBegin(GL2.GL_TRIANGLES); 
         gl2.glVertex2d(lensX-0.5*lensRadius, (SSM.windowHeight - lensY)+lensRadius+5);
         gl2.glVertex2d(lensX+0.5*lensRadius, (SSM.windowHeight - lensY)+lensRadius+5);
         gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)+lensRadius+25);
      gl2.glEnd();
      gl2.glPopMatrix();
      
      gl2.glLoadName(8888);
      gl2.glPushMatrix();
      gl2.glBegin(GL2.GL_TRIANGLES); 
         gl2.glVertex2d(lensX-0.5*lensRadius, (SSM.windowHeight - lensY)-lensRadius-5);
         gl2.glVertex2d(lensX+0.5*lensRadius, (SSM.windowHeight - lensY)-lensRadius-5);
         gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)-lensRadius-25);
      gl2.glEnd();      
      gl2.glPopMatrix();
      
      
      return finishPicking(gl2, buffer);
      
   }
   
   
   
   /////////////////////////////////////////////////////////////////////////////////       
   // Performs picking operation
   // Set state to select mode, then redraw the group components
   // Ported from NeHe site : http://nehe.gamedev.net
   /////////////////////////////////////////////////////////////////////////////////       
   public Integer picking3D(GL2 gl2, float px, float py) {
      // Quick way to get out and save some FPS from rendering useless cycles
      //if (SSM.instance().l_mouseClicked == false) return null;
      
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      
      
      // check if we are inside a lens, if we are, we want to use the lens' near and far plane 
      // instead of the default near and far plane
      LensAttrib la = null;
      LensAttrib clen = null;
      for (int i=0; i < SSM.lensList.size(); i++) {
         la = SSM.lensList.elementAt(i);
         float x = px - la.magicLensX;
         float y = py - la.magicLensY;
         float r = (float)la.magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);            
         if (d < r) {
            clen = la;
            break;
         }         
      }
      if (clen != null) {
         GraphicUtil.startPickingPerspective(gl2, buffer, 
               (int)px, (int)py, 
               SSM.windowWidth, SSM.windowHeight, SSM.fov, clen.nearPlane, clen.farPlane,
               DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());         
      } else {
         this.startPickingPerspective(gl2, buffer, px, py);
      }
      
      
      
      Enumeration<String> e = MM.currentModel.componentTable.keys();
      while (e.hasMoreElements()) {   
         //String partName = e.nextElement(); 
         String partName = e.nextElement(); 
         DCComponent comp = MM.currentModel.componentTable.get(partName);
         int partId = comp.id;
         
         
         // if partId is not registered just continue
         if (partId < 0) continue;
         
         // if the object has no context then continue
         if (comp.hasContext == false) continue;
         
         // if the object is inactive click through
         if (comp.active == false) continue;
         
         gl2.glLoadName(partId);
         gl2.glPushMatrix();
            // Make sure the transform is also in picking mode 
            basicTransform(gl2);
            if (MM.currentModel.componentTable.get(partName).level >= SSM.occlusionLevel) 
                MM.currentModel.componentTable.get(partName).renderBasicMesh(gl2);
            //model.componentTable.get(partName).boundingBox.renderBoundingBox(gl2);  
         gl2.glPopMatrix();
                     
      }
      return finishPicking(gl2, buffer);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Layout that places on either the left or the right side
   //    right and left are passed in containers
   ////////////////////////////////////////////////////////////////////////////////
   public void singleSideLayout(DCComponent comp, LensAttrib la, Vector<DCComponent> right, Vector<DCComponent> left, float paddings[]) {
      float lensRadius = la.magicLensRadius;
      float lensX = la.magicLensX;
      float lensY = la.magicLensY;
      float rpadding = paddings[0];
      float lpadding = paddings[1];
      
      if (rpadding < lpadding) {
         right.add( comp );
      } else {
         left.add( comp );
      }      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Layout that will place the chart on either side of the center of the lens
   // with respect to how far their projected coordinate is from the center
   //    right and left are passed in containers
   ////////////////////////////////////////////////////////////////////////////////
   public void dualSideLayout(DCComponent comp, LensAttrib la, Vector<DCComponent> right, Vector<DCComponent> left, float paddings[]) {
      float lensRadius = la.magicLensRadius;
      float lensX = la.magicLensX;
      float lensY = la.magicLensY;
      float rpadding = paddings[0];
      float lpadding = paddings[1];
      
      if (comp.projCenter.x >= lensX) {
         if (rpadding < 1.5*lensRadius)
            right.add( comp );
         else 
            left.add( comp );
      } else {
         if (lpadding < 1.5*lensRadius)
            left.add( comp );
         else
            right.add( comp );
      }      
   }
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Alternate between right and left
   ///////////////////////////////////////////////////////////////////////////////// 
   public void alternateSideLayout(DCComponent comp, LensAttrib la, Vector<DCComponent> right, Vector<DCComponent> left, int index) {
      if (index % 2 == 0) {
         right.add(comp);
      } else {
         left.add(comp); 
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the small chart/labels 
   ////////////////////////////////////////////////////////////////////////////////
   public void renderLabelBalanced(GL2 gl2, LensAttrib la) {
      String list[] = this.getComponentSortedByProjY(gl2);    
      float rightHeight = 0;
      float leftHeight = 0;
      
      int startIdx = CacheManager.instance().getDateKey( SSM.startTimeFrame ) == null ? 0:
         CacheManager.instance().getDateKey( SSM.startTimeFrame );
      int endIdx   = CacheManager.instance().getDateKey( SSM.endTimeFrame) == null ? CacheManager.instance().timeLineSize:
         CacheManager.instance().getDateKey( SSM.endTimeFrame );      
      
     
      Vector<DCComponent> rightList = new Vector<DCComponent>();
      Vector<DCComponent> leftList  = new Vector<DCComponent>();
      
      float lensRadius = la.magicLensRadius;
      float lensX = la.magicLensX;
      float lensY = la.magicLensY;
      
      
      // New padding, always apply outside of the 3D model, in addition 
      // position with respect to the radius size ie: do not go "inside" the circumference
      rpadding = Math.abs( MM.currentModel.maxx - lensX);
      lpadding = Math.abs( MM.currentModel.minx - lensX);
      if (rpadding > lensRadius) {
         rpadding -= lensRadius;
      } else  {
         rpadding = vpadding;
      }
      if (lpadding > lensRadius) {
         lpadding -= lensRadius;
      } else  {
         lpadding = vpadding;
      }
      
      // check if the paddings are out of bound (ie: when we are close up)
      // default the padding space to space padding
      if ( rpadding + lensX + SSM.sparkLineWidth > SSM.windowWidth ) {
         rpadding = spadding;   
      }
      if (lensX - lpadding - SSM.sparkLineWidth < 0) {
         lpadding = spadding;   
      }
      
      
      Hashtable<String, String> tmp = new Hashtable<String, String>();
      
      // This need to be in order
      if (la.start > list.length) la.start -= la.numToDisplay; 
      if (la.start <= 0) la.start = 0;
      int laCnt = 0;
      
      
      // First filter into left and right list
      for (int i=0; i < list.length; i++) {
         DCComponent comp = MM.currentModel.componentTable.get(list[i]);
         // If the centroid point is before the lens's near plane, do not render the label
         if (DCCamera.instance().eye.sub(comp.center).mag() <= la.nearPlane) continue;
         
         // If the object has 0 occurrences, do not render the label
         if (comp.hasContext == false) continue;
         
         
         // If local mode than don't render components that are not related
         comp.cchart.active = true;
         if (SSM.useLocalFocus == true) {
            if (SSM.selectedGroup.size() > 0 && ! SSM.relatedList.contains(comp.id))  {
               comp.cchart.active = false;
            } else {
               comp.cchart.active = true;   
            }
         } 
            
         
         
         // Check parent and model table capability in aggregation mode
         boolean skip = false;
         if (SSM.useAggregate == true) {
            Integer parentId = comp.id;
            while(true) {
               parentId = HierarchyTable.instance().getParentId(parentId);   
               
               if (parentId == null) break;
               if (MM.currentModel.componentTableById.get(parentId) != null) {
                  skip = true;
                  break;
               }
            } // end while            
         } else {
            skip = false; 
         }
         if (skip == true) continue;
             
         
         
         //float xx = comp.projCenter.x - lensX;
         //float yy = comp.projCenter.y - (SSM.windowHeight - lensY);
         float xx = la.zoomFactor*(comp.projCenter.x - lensX);
         float yy = la.zoomFactor*(comp.projCenter.y - (SSM.windowHeight - lensY));
         
         float c = (float)Math.sqrt(xx*xx + yy*yy);
         
         if ( c <= lensRadius ) {
            // If the base object is already registered, skip the rendering
            // so we don't draw so many guide lines
            if (tmp.contains(comp.baseName)) continue; 
            tmp.put(comp.baseName, comp.baseName);
            
            if (laCnt >= la.start && laCnt < (la.start+la.numToDisplay)) {
               this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
               //this.alternateSideLayout(comp, la, rightList, leftList, i);
               //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
            }
            laCnt ++;
            
               
            //this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
            //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
            
         }
      } // end for
      
      
      
      
      // Now actually render the labels
      rightHeight = Math.min((SSM.windowHeight-la.magicLensY) + (rightList.size()/2)*(SSM.sparkLineHeight + vpadding),
                              SSM.windowHeight-SSM.sparkLineHeight-vpadding);
      leftHeight  = Math.min((SSM.windowHeight-la.magicLensY) + (leftList.size()/2)*(SSM.sparkLineHeight + vpadding),
                              SSM.windowHeight-SSM.sparkLineHeight-vpadding); 
      
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ZERO);
      
      // Right side
      for (int i=0; i < rightList.size(); i++) {
         DCComponent comp = rightList.elementAt(i);
         if (comp.id < 0) continue;
         
            
         int occ = CacheManager.instance().groupOccurrence.get(comp.id); 
         int c_occ = CacheManager.instance().c_groupOccurrence.get(comp.id);
         
         int relatedOcc = 0;
         int relatedOccNew = 0;
         int c_relatedOccNew = 0;
         if (SSM.selectedGroup.size() >= 0 ) {
            
            if (SSM.useAggregate == true) {
               Vector<Integer> selectedGroup =  new Vector<Integer>();
               selectedGroup.addAll( SSM.selectedGroup.values());
            
               relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     HierarchyTable.instance().getAgg(comp.id),
                     selectedGroup,
                     SSM.manufactureAttrib.selected,
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);
               
               c_relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     HierarchyTable.instance().getAgg(comp.id),
                     selectedGroup,
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected, 
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected);
              
            } else {
               Vector<Integer> related =  new Vector<Integer>();
               related.addAll( SSM.selectedGroup.values());
               
               Vector<Integer> t = new Vector<Integer>();
               t.add(comp.id);
               
               relatedOccNew = CacheManager.instance().getCoOccurring(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     t,
                     related,
                     SSM.manufactureAttrib.selected,
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);              
               
               c_relatedOccNew = CacheManager.instance().getCoOccurring(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     t,
                     related,
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected, 
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected);              
              
            }
         }
         String txt = "";
         if (SSM.useComparisonMode == true) {
            //txt = comp.baseName+"(" + (relatedOccNew+c_relatedOccNew) + "/" + relatedOcc + "/" + (c_occ+occ) + ")";
            txt = comp.baseName+" (" + (relatedOccNew+c_relatedOccNew) + "/" + (c_occ+occ) + ")";
         } else {
            //txt = comp.baseName+"(" + relatedOccNew + "/" + relatedOcc + "/" + occ + ")";
            txt = comp.baseName+" (" + relatedOccNew + "/" + occ + ")";
         }
            
         double size[] = GraphicUtil.getFontDim(txt);
         rightHeight -= Math.max(size[1], comp.cchart.height);
         rightHeight -= vpadding;
               
               
         //FontRenderer.instance().setColour(SchemeManager.font_default);
         //comp.sparkLine.label = txt;
         comp.cchart.setAnchor(lensX+lensRadius+rpadding+spadding, rightHeight);
         comp.cchart.colour = comp.colour;
         comp.cchart.render(gl2);
         
         comp.cchart.tf.width = comp.cchart.width;
         comp.cchart.tf.height = comp.cchart.height;
         comp.cchart.tf.anchorX = comp.cchart.anchorX;
         comp.cchart.tf.anchorY = comp.cchart.anchorY;
         
         comp.cchart.setLabel(txt);
         comp.cchart.tf.render(gl2);
         //comp.cchart.tf.renderBorder(gl2);
         
         
         // doodle
         float doodleY = comp.projCenter.y - (SSM.windowHeight - lensY);
         float doodleAngle = (float)Math.asin( doodleY/lensRadius);
         
         float edgeX = 1.0f*lensRadius*(float)Math.cos(doodleAngle);
         
         if (SSM.selectedGroup.size() > 0  && SSM.selectedGroup.contains(comp.cchart.id)){
            gl2.glColor4fv( SchemeManager.selected.toArray(), 0);
         } else if (SSM.relatedList != null && SSM.relatedList.contains(comp.id)) { 
            gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
         } else {
            if (comp.cchart.active) 
               gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
            else
               gl2.glColor4fv( DCColour.fromDouble(0.8, 0.8, 0.8, 0.8).toArray(), 0);
            
         }
         
         gl2.glLineWidth(2.0f);
         gl2.glLineStipple(2, SSM.stipplePattern);
         if (SSM.useStipple) gl2.glEnable(GL2.GL_LINE_STIPPLE);
         gl2.glBegin(GL2.GL_LINES);
            float tx = lensX  - (lensX  - comp.projCenter.x)*la.zoomFactor;
            float ty = (SSM.windowHeight-lensY)  - ((SSM.windowHeight-lensY) - comp.projCenter.y)*la.zoomFactor;
            
                        
            gl2.glVertex2d( tx, ty);
            gl2.glVertex2d( lensX + edgeX, comp.projCenter.y);
            
            // Original
            //gl2.glVertex2d( comp.projCenter.x, comp.projCenter.y);
            //gl2.glVertex2d( lensX + edgeX, comp.projCenter.y);
            
            gl2.glVertex2d( lensX + edgeX, comp.projCenter.y);
            gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*comp.cchart.height);
            
            // Connect the line to the center
            gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d(lensX+lensRadius + rpadding+spadding-1, rightHeight + 0.5*comp.cchart.height);
            
            
            // Connect the line to the top and the bottom
            /*
            gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d(lensX+lensRadius + rpadding+spadding, rightHeight + comp.cchart.height);
            gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d(lensX+lensRadius + rpadding+spadding, rightHeight );
            */
         gl2.glEnd();
         gl2.glLineWidth(1.0f);
         gl2.glDisable(GL2.GL_LINE_STIPPLE);
      }
      
      
      // Left Side
      for (int i=0; i < leftList.size(); i++) {
         DCComponent comp = leftList.elementAt(i);
         if (comp.id < 0) continue;
            
         int occ = CacheManager.instance().groupOccurrence.get(comp.id); 
         int c_occ = CacheManager.instance().c_groupOccurrence.get(comp.id);
         
         int relatedOcc = 0;
         int relatedOccNew = 0;
         int c_relatedOccNew = 0;
         
         if (SSM.selectedGroup.size() >= 0 ) {
            
            if (SSM.useAggregate == true) {
               Vector<Integer> selectedGroup =  new Vector<Integer>();
               selectedGroup.addAll( SSM.selectedGroup.values());
               
               relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     HierarchyTable.instance().getAgg(comp.id),
                     selectedGroup,
                     SSM.manufactureAttrib.selected,
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);               
               
               c_relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     HierarchyTable.instance().getAgg(comp.id),
                     selectedGroup,
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected, 
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected);               
               
            } else {
               Vector<Integer> related =  new Vector<Integer>();
               related.addAll(SSM.selectedGroup.keySet());
               Vector<Integer> t = new Vector<Integer>();
               t.add(comp.id);
               
               relatedOccNew = CacheManager.instance().getCoOccurring(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     t,
                     related,
                     SSM.manufactureAttrib.selected,
                     SSM.makeAttrib.selected, 
                     SSM.modelAttrib.selected,
                     SSM.yearAttrib.selected);                   
               
               c_relatedOccNew = CacheManager.instance().getCoOccurring(
                     startIdx, endIdx, 
                     SSM.startMonth, SSM.endMonth, 
                     t,
                     related,
                     SSM.c_manufactureAttrib.selected,
                     SSM.c_makeAttrib.selected, 
                     SSM.c_modelAttrib.selected,
                     SSM.c_yearAttrib.selected);                   
              
            }
         }
         String txt = "";
         if (SSM.useComparisonMode == true) {
            //txt = comp.baseName+"(" + (relatedOccNew+c_relatedOccNew) + "/" + relatedOcc + "/" + (occ+c_occ) + ")";
            txt = comp.baseName+" (" + (relatedOccNew+c_relatedOccNew) + "/" + (occ+c_occ) + ")";
         } else {
            //txt = comp.baseName+"(" + relatedOccNew + "/" + relatedOcc + "/" + occ + ")";
            txt = comp.baseName+" (" + relatedOccNew + "/" + occ + ")";
         }
            
            
         double size[] = GraphicUtil.getFontDim(txt);
         leftHeight -= Math.max(size[1], comp.cchart.height);
         leftHeight -= vpadding;
        
         comp.cchart.setAnchor( lensX-lensRadius-lpadding-comp.cchart.width-spadding, leftHeight);
         comp.cchart.colour = comp.colour;
         comp.cchart.render(gl2);
         
         comp.cchart.tf.width = comp.cchart.width;
         comp.cchart.tf.height = comp.cchart.height;
         comp.cchart.tf.anchorX = comp.cchart.anchorX;
         comp.cchart.tf.anchorY = comp.cchart.anchorY;         
         comp.cchart.setLabel(txt);
         comp.cchart.tf.render(gl2);
         
            
         // Doodle
         float doodleY = comp.projCenter.y - (SSM.windowHeight - lensY);
         float doodleAngle = (float)Math.asin( doodleY/lensRadius);
         float edgeX = 1.0f*lensRadius*(float)Math.cos(doodleAngle);
         
         if (SSM.selectedGroup.size() > 0 && SSM.selectedGroup.contains(comp.cchart.id)){
            gl2.glColor4fv( SchemeManager.selected.toArray(), 0);
         } else if (SSM.relatedList != null && SSM.relatedList.contains(comp.id)) {   
            gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
         } else {
            if (comp.cchart.active)
               gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
            else 
               gl2.glColor4fv( DCColour.fromDouble(0.8, 0.8, 0.8, 0.8).toArray(), 0);
         }         
         
         
         gl2.glLineStipple(2, SSM.stipplePattern);
         if (SSM.useStipple) gl2.glEnable(GL2.GL_LINE_STIPPLE);
         gl2.glLineWidth(2.0f);
         gl2.glBegin(GL2.GL_LINES);
            float tx = lensX  - (lensX  - comp.projCenter.x)*la.zoomFactor;
            float ty = (SSM.windowHeight-lensY)  - ((SSM.windowHeight-lensY) - comp.projCenter.y)*la.zoomFactor;
            
            gl2.glVertex2d( tx, ty );
            gl2.glVertex2d( lensX - edgeX, comp.projCenter.y);
            
            // Original
            //gl2.glVertex2d( comp.projCenter.x, comp.projCenter.y);
            //gl2.glVertex2d( lensX - edgeX, comp.projCenter.y);
            
            gl2.glVertex2d( lensX - edgeX, comp.projCenter.y);
            gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*comp.cchart.height);
            
            // Connect the line to the centre
            gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d( lensX-lensRadius - lpadding-spadding+1, leftHeight + 0.5*comp.cchart.height);
            
            // Connect the line to the top and bottom
            /*
            gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d( lensX-lensRadius - lpadding-spadding, leftHeight + comp.cchart.height);
            gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*comp.cchart.height);
            gl2.glVertex2d( lensX-lensRadius - lpadding-spadding, leftHeight);
            */
         gl2.glEnd();
         gl2.glLineWidth(1.0f);
         gl2.glDisable(GL2.GL_LINE_STIPPLE);
      }
      
      
      
      for (DCTriple point: SSM.hoverPoints.values()) {
         Integer obj = this.pickingCircleLabel(gl2, la, point.x, point.y);
         float x = (float)point.x - lensX;
         float y = (float)point.y - lensY;
         float r = (float)lensRadius;
         float d = (float)Math.sqrt(x*x + y*y);
         //System.out.println(".....debugging...." + lensX + " " + lensY + " " + point.x + " " + point.y);
         //System.out.println(".....debugging...." + x + " " + y + " " + r + " " + d);
         if ( d <= r ) {
            la.magicLensSelected = 1;
            if ( la.borderSize < la.borderSizeSelected ) {
               la.selectAnimator = PropertySetter.createAnimator(200, la, "borderSize", new FloatEval(), la.borderSizeSelected);
               la.selectAnimator.start();
            }            
            
         } else {
            la.magicLensSelected = 0;
            if (la.borderSize > la.borderSizeNormal) {
               la.deSelectAnimator = PropertySetter.createAnimator(200, la, "borderSize", new FloatEval(), la.borderSizeNormal);
               la.deSelectAnimator.start();
            }
         }
         if (obj != null) {
            la.magicLensSelected = 1;
            SSM.topElement = SSM.ELEMENT_LENS;
         } 
      }
      
      
      
      if (la.start >= la.numToDisplay) {
         la.renderTop = true;
      } else {
         la.renderTop = false;
      }
     
      if (la.start+la.numToDisplay < laCnt) {
         la.renderBottom = true;
      } else {
         la.renderBottom = false;
      }
      
      double arrowWidth = Math.min(30, Math.max(15, 0.5*lensRadius));
      double arrowHeight = 30;
      if (la.renderTop) {
         if (la.magicLensSelected == 1)  {
            gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
         } else {
            gl2.glColor4fv(SchemeManager.unselected.toArray(), 0);
         }
         gl2.glBegin(GL2.GL_TRIANGLES); 
            gl2.glVertex2d(lensX-arrowWidth, (SSM.windowHeight - lensY)+lensRadius+5);
            gl2.glVertex2d(lensX+arrowWidth, (SSM.windowHeight - lensY)+lensRadius+5);
            gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)+lensRadius+arrowHeight);
         gl2.glEnd();
         
         if (la.start != la.l_top) {
            la.t_top.clearMark();
            la.t_top.addMark(la.start+"", Color.BLACK, new Font("Arial", Font.BOLD, 14), 0, 5, true);
            la.t_top.anchorX = lensX-8;
            la.t_top.anchorY = (SSM.windowHeight-lensY)+lensRadius+5;
            la.t_top.dirty = 1;
         }
         la.t_top.render(gl2);
         
      }
      if (la.renderBottom) {
         if (la.magicLensSelected == 1)  {
            gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
         } else {
            gl2.glColor4fv(SchemeManager.unselected.toArray(), 0);
         }
         gl2.glBegin(GL2.GL_TRIANGLES); 
            gl2.glVertex2d(lensX-arrowWidth, (SSM.windowHeight - lensY)-lensRadius-5);
            gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)-lensRadius-arrowHeight);
            gl2.glVertex2d(lensX+arrowWidth, (SSM.windowHeight - lensY)-lensRadius-5);
         gl2.glEnd();
         
         if ( (laCnt - (la.start+la.numToDisplay)) != la.l_bottom){
            la.t_bottom.clearMark();
            la.t_bottom.addMark((laCnt-(la.start+la.numToDisplay))+"", Color.BLACK, new Font("Arial", Font.BOLD, 14), 0, 5, true);
            la.t_bottom.anchorX = lensX-8;
            la.t_bottom.anchorY = (SSM.windowHeight-lensY)-lensRadius-23;
            la.t_bottom.dirty = 1;
         }
         la.t_bottom.render(gl2);
      }
  
   }
   
   
   
   
   
   public Integer pickingCircleLabel(GL2 gl2, LensAttrib la, float px, float py)  {
      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      this.startPickingOrtho(gl2, buffer, px, py);      
      
      float lensX = la.magicLensX;
      float lensY = la.magicLensY;
      float lensRadius = la.magicLensRadius;
      
      // Draw a down and up for scrolling
      double arrowWidth = Math.min(30, Math.max(15, 0.5*lensRadius));
      //double arrowWidth = Math.min(50, Math.max(20, 0.5*lensRadius));
      double arrowHeight = 30;
      if (la.renderTop) {
         gl2.glLoadName(9999);
         gl2.glPushMatrix();
         gl2.glBegin(GL2.GL_TRIANGLES); 
            gl2.glVertex2d(lensX-arrowWidth, (SSM.windowHeight - lensY)+lensRadius+5);
            gl2.glVertex2d(lensX+arrowWidth, (SSM.windowHeight - lensY)+lensRadius+5);
            gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)+lensRadius+arrowHeight);
         gl2.glEnd();
         gl2.glPopMatrix();
      }
      
      if (la.renderBottom){ 
         gl2.glLoadName(8888);
         gl2.glPushMatrix();
         gl2.glBegin(GL2.GL_TRIANGLES); 
            gl2.glVertex2d(lensX-arrowWidth, (SSM.windowHeight - lensY)-lensRadius-5);
            gl2.glVertex2d(lensX+arrowWidth, (SSM.windowHeight - lensY)-lensRadius-5);
            gl2.glVertex2d(lensX, (SSM.windowHeight - lensY)-lensRadius-arrowHeight);
         gl2.glEnd();      
         gl2.glPopMatrix();
      }
     
      return this.finishPicking(gl2, buffer);
   }
   
   

   ////////////////////////////////////////////////////////////////////////////////
   // Render labels/sparklines in a circular pattern around the
   // magic lens
   ////////////////////////////////////////////////////////////////////////////////
   public void renderLabelCircular(GL2 gl2, LensAttrib la) {
      String list[] = this.getComponentUnsorted(gl2);
      for (int i=0; i < list.length; i++) {
         DCComponent comp = MM.currentModel.componentTable.get(list[i]);
         
         float xx = comp.projCenter.x - la.magicLensX;
         float yy = comp.projCenter.y - (SSM.windowHeight - la.magicLensY);
         float c = (float)Math.sqrt(xx*xx + yy*yy);
         
         double angle = 0.0f;
         angle = Math.atan2( yy, xx+0.000001);
         //angle = xx/yy;
         
         if ( c <= la.magicLensRadius ) {
         	/*
            Vector<Integer> ids = HierarchyTable.instance().getGroupId(comp.baseName);
            if (ids == null || ids.size() == 0) continue;
            Integer id = ids.elementAt(0);
            */
         	if (comp.id < 0) continue;
            
            float scale = 1.0f; // + (float)CacheManager.instance().groupOccurrence.get(comp.id)/(float)SSM.maxOccurrence;
            textRenderer.setColor( 0.0f, 0.5f, 2.0f, 1.0f);
            this.renderTextPolar(gl2, la, (float)angle, scale, comp, comp.cname+"(" + CacheManager.instance().groupOccurrence.get(comp.id) + ")");
         }
      }
   }
   

      
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render components in a single colour
   ////////////////////////////////////////////////////////////////////////////////
   public void renderNormal(GL2 gl2) {
      gl2.glPushMatrix();
         gl2.glEnable(GL2.GL_DEPTH_TEST);
         gl2.glDisable(GL2.GL_BLEND);
         String[] clist = this.getComponentSortedByCentroid2(gl2);
         for (int cidx = 0; cidx < clist.length; cidx++) {
            String partName  = clist[cidx];
            DCComponent modelComp = MM.currentModel.componentTable.get(partName);         
            modelComp.renderBufferToon(gl2);
            //modelComp.renderBuffer(gl2);
         }
      gl2.glPopMatrix();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render 'silhouette'...Why? because I can...that's why
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSil(GL2 gl2) {
      String[] clist = this.getComponentSortedByCentroid(gl2);
      for (int cidx = 0; cidx < clist.length; cidx++) {
         DCComponent modelComp = MM.currentModel.componentTable.get( clist[cidx] );
         //modelComp.renderTriangleWithAdj(gl2);
         modelComp.renderBufferAdj(gl2, null);
      }
      //gl2.glUseProgram(0);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders a face normal visualization...more for debugging polygons than
   // any actual use
   ////////////////////////////////////////////////////////////////////////////////
   public void renderFNormal(GL2 gl2) {
      String clist[] = this.getComponentUnsorted(gl2);      
      for (int i=0; i < clist.length; i++) {
         DCComponent c = MM.currentModel.componentTable.get(clist[i]);        
         c.renderFNormal(gl2);
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders a per vertex normal visualization ... for debugging
   ////////////////////////////////////////////////////////////////////////////////
   public void renderVNormal(GL2 gl2) {
      String clist[] = this.getComponentUnsorted(gl2);      
      for (int i=0; i < clist.length; i++) {
         DCComponent c = MM.currentModel.componentTable.get(clist[i]);        
         c.renderVNormal(gl2);
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render silhouette with the line width proportional to the frequence of occurrences
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSilLineWidth(GL2 gl2) {
      String[] clist = this.getComponentSortedByCentroid2(gl2);   
      gl2.glLineWidth( 0.5f);
      for (int i=0; i < clist.length; i++) {
         DCComponent c = MM.currentModel.componentTable.get( clist[i] );   
         //gl2.glLineWidth( 0.5f + 4.0f*c.occurrence/(float)this.maxOccurrence); 
         c.renderBufferAdj(gl2, c.silhouetteColour);
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just for fun...use as a minimal rendering example
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBoundingBox(GL2 gl2) {
      String[] clist = this.getComponentSortedByCentroid(gl2);
      for (int i = 0; i < clist.length; i++) {
         DCComponent c = MM.currentModel.componentTable.get( clist[i] );   
         c.boundingBox.renderBoundingBox(gl2);
      }
   }
      
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Render each component based up frequency of occurrences
   ///////////////////////////////////////////////////////////////////////////////// 
   public void renderColourRamp(GL2 gl2, LensAttrib la) {
      // Render the scene
      //gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glPushMatrix();
      
      //String[] clist = this.getComponentSortedByCentroid(gl2);
      String[] clist = null; 
      switch (SSM.sortingMethod) {
         case 0: clist = this.getComponentSortedByCentroid2(gl2); break;
         case 1: clist = this.getComponentSortedByCentroid(gl2); break;
         case 2: clist = this.getComponentUnsorted(gl2); break;
         default:
            clist = this.getComponentSortedByCentroid2(gl2);
      }
      for (int cidx = 0; cidx < clist.length; cidx++) {
         String partName  = clist[cidx];
         

         DCComponent modelComp = MM.currentModel.componentTable.get(partName);         
         
         // Test
         if (la != null)
            if (DCCamera.instance().eye.sub(modelComp.center).mag() <= la.nearPlane) continue;
         
         
	   
         if ( SSM.selectedGroup.size() > 0 && SSM.selectedGroup.contains(modelComp.id)) {
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl2.glColor4fv(SchemeManager.component_selected.toArray(), 0);
            
            gl2.glPushMatrix();
               // Render the component (selected)
               gl2.glUseProgram(0);
               modelComp.renderBuffer( gl2, modelComp.colour );
               gl2.glLineWidth(1.5f);
               modelComp.renderBufferAdj(gl2, SchemeManager.selected);
               
               modelComp.boundingBox.renderBoundingBox(gl2);
               gl2.glLineWidth(0.5f);
               
               if (SSM.renderSihoulette) {
                  modelComp.renderBufferAdj(gl2, null);
               }
            gl2.glPopMatrix();
            continue;
         } 
         
         // TODO:Highlight the related groups - temporary test, should move to better id/string check structure
         if (SSM.relatedList.size() > 0 ) {
            boolean done = false;   
            for (int i=0; i < SSM.relatedList.size(); i++) {
               //if ( HierarchyTable.instance().partTable.get(SSM.instance().relatedList.elementAt(i)).contains(modelComp.baseName)) {
            	if ( modelComp.id == SSM.relatedList.elementAt(i)) {
                  done = true;   
                  gl2.glPushMatrix();
                     //modelComp.boundingBox.renderBoundingBox(gl2, DCColour.fromInt(0, 255, 255, 128));
                     //modelComp.boundingBox.renderBoundingBox(gl2, DCColour.fromInt(0, 255, 255, 128));
                     modelComp.renderBufferAdj(gl2, SchemeManager.related.adjustAlpha(0.5f));
                  gl2.glPopMatrix();
                  break;
               }
            }
            //if (done == true) continue;
         }
         
         
         gl2.glPushMatrix();
            //gl2.glScalef( 1.0f/modelComp.scaleFactor.x, 1.0f/modelComp.scaleFactor.x, 1.0f/modelComp.scaleFactor.x);
            if (! modelComp.hasContext || ! modelComp.active) {
               gl2.glLineWidth(0.5f);
               modelComp.renderBufferAdj(gl2, null);
            } else {
               gl2.glUseProgram(0);
               //modelComp.renderBuffer(gl2, modelComp.colour);
               gl2.glEnable(GL2.GL_BLEND);
               //modelComp.renderBufferVaryLight(gl2, modelComp.colour);
               modelComp.renderBuffer(gl2, modelComp.colour);
               //modelComp.renderFNormal(gl2);
               
               if (SSM.renderSihoulette) {
                  gl2.glLineWidth(0.5f);
                  modelComp.renderBufferAdj(gl2, null);
               }
            }
         gl2.glPopMatrix();
      
      } // end while
      gl2.glPopMatrix();
      //gl2.glEnable(GL2.GL_DEPTH_TEST);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Init vert,geom and frag shaders
   // Init model and model component
   ////////////////////////////////////////////////////////////////////////////////
   public void init(GL2 gl2) {
      super.init(gl2);
      dcTextPanel.init(gl2);
      

     
      
      MM.instance().initGPU(gl2);
      SSM.dirty = 1;
      SSM.dirtyGL = 1;
      
      
      this.resetData();
   }
   
   
   
   

   
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // res[0] -> occ
   // res[1] -> c_occ
   // res[2] -> relatedOcc
   // res[3] -> c_relatedOcc
   ////////////////////////////////////////////////////////////////////////////////
   public int[] getOccCounts(DCComponent comp, int startIdx, int endIdx) {
      int res[] = new int[4];   

      res[0] = CacheManager.instance().groupOccurrence.get(comp.id); 
      res[1] = CacheManager.instance().c_groupOccurrence.get(comp.id);      
      
      
      if (SSM.selectedGroup.size() > 0 ) {
         if (SSM.useAggregate == true) {
            Vector<Integer> selectedGroup =  new Vector<Integer>();
            selectedGroup.addAll( SSM.selectedGroup.values());
         
            res[2] = CacheManager.instance().getCoOccurringAgg(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth, 
                  HierarchyTable.instance().getAgg(comp.id),
                  selectedGroup,
                  SSM.manufactureAttrib.selected,
                  SSM.makeAttrib.selected, 
                  SSM.modelAttrib.selected,
                  SSM.yearAttrib.selected);
            
            res[3] = CacheManager.instance().getCoOccurringAgg(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth, 
                  HierarchyTable.instance().getAgg(comp.id),
                  selectedGroup,
                  SSM.c_manufactureAttrib.selected,
                  SSM.c_makeAttrib.selected, 
                  SSM.c_modelAttrib.selected,
                  SSM.c_yearAttrib.selected);
           
         } else {
            Vector<Integer> related =  new Vector<Integer>();
            related.addAll( SSM.selectedGroup.values());
            
            Vector<Integer> t = new Vector<Integer>();
            t.add(comp.id);
            
            res[2] = CacheManager.instance().getCoOccurring(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth, 
                  t,
                  related,
                  SSM.manufactureAttrib.selected,
                  SSM.makeAttrib.selected, 
                  SSM.modelAttrib.selected,
                  SSM.yearAttrib.selected);              
            
            res[3] = CacheManager.instance().getCoOccurring(
                  startIdx, endIdx, 
                  SSM.startMonth, SSM.endMonth, 
                  t,
                  related,
                  SSM.c_manufactureAttrib.selected,
                  SSM.c_makeAttrib.selected, 
                  SSM.c_modelAttrib.selected,
                  SSM.c_yearAttrib.selected);              
         }
      }
      return res;
   }
   
   
}

