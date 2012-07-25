package exec;

import gui.DCTip;
import gui.StatusWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import TimingFrameExt.FloatEval;

import model.DCTriple;
import util.ALogger;
import util.DCCamera;
import datastore.MM;
import datastore.SSM;

/////////////////////////////////////////////////////////////////////////////////
// Class to handle input events
/////////////////////////////////////////////////////////////////////////////////
public class EventManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

 
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // From MouseListener
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         if (SSM.controlKey == true) {
            Event.createLens(SSM.mouseX, SSM.mouseY);
         } else {
            SSM.pickPoints.add(new DCTriple(SSM.mouseX, SSM.mouseY, 0));
            SSM.l_mouseClicked = true;
         }
      } else  if (e.getButton() == MouseEvent.BUTTON3){
         if (SSM.controlKey == true) {
            Event.removeLens(SSM.mouseX, SSM.mouseY);
         } else  {
            SSM.r_mouseClicked = true;
         }
      } // end if 
   }

   
   
   @Override
   public void mouseEntered(MouseEvent e) {
   }

   
   @Override
   public void mouseExited(MouseEvent e) {
   }

   
   
   @Override
   public void mousePressed(MouseEvent e) {
      
      if (e.getButton() == MouseEvent.BUTTON1) {
         
         // Check the lens
         Event.checkLens(e.getX(), e.getY());
         Event.checkLensHandle(e.getX(), e.getY());
         
         // Check the document widget
         Event.checkDocumentPanel(e.getX(), e.getY());
         
         // For default filter
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.makeAttrib, SSM.ELEMENT_MAKE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.modelAttrib, SSM.ELEMENT_MODEL_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.yearAttrib, SSM.ELEMENT_YEAR_SCROLL);
         
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.c_manufactureAttrib, SSM.ELEMENT_CMANUFACTURE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.c_makeAttrib, SSM.ELEMENT_CMAKE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.c_modelAttrib, SSM.ELEMENT_CMODEL_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.c_yearAttrib, SSM.ELEMENT_CYEAR_SCROLL);
         
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.perspectiveAttrib, SSM.ELEMENT_PERSPECTIVE_SCROLL);
         
         // Check the ranged slider
         Event.checkSlider(e.getX(), e.getY());
         
         // Comment this out if not using scenarios
         Event.checkScenario(e.getX(), e.getY());
         
         
//         float sl_anchorX = SSM.instance().saveLoadAnchorX;
//         float sl_anchorY = SSM.instance().saveLoadAnchorY;
//         if (DCUtil.between(mx, sl_anchorX, sl_anchorX+SSM.instance().scrollWidth)) {
//            if (DCUtil.between(my, sl_anchorY, sl_anchorY+SSM.instance().saveLoadHeight)) {
//               if (SSM.instance().saveLoadActive) {
//                  SSM.instance().topElement = SSM.ELEMENT_SAVELOAD_SCROLL;
//               }
//            }
//         }
         
         if (SSM.dragPoints.get(999L) == null) {
            SSM.dragPoints.put(999L, new DCTriple());   
         }
         DCTriple d = SSM.dragPoints.get(999L);
         d.x = e.getX();
         d.y = e.getY();         
         
 
         SSM.l_mousePressed = true;
      } else if (e.getButton() == MouseEvent.BUTTON3) {
         Event.checkLens(e.getX(), e.getY());
         SSM.r_mousePressed = true;
      }
         
   }
   

   
   @Override
   public void mouseReleased(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         SSM.l_mousePressed = false;
         SSM.clearLens();
         SSM.dragPoints.clear();
      } else if (e.getButton() == MouseEvent.BUTTON3) {
         SSM.r_mousePressed = false;
         SSM.clearLens();
         SSM.dragPoints.clear();
      }
      SSM.topElement = SSM.ELEMENT_NONE;
      
      
   }

   
   ////////////////////////////////////////////////////////////////////////////////
   // From KeyListener
   // 
   //  [ - occ             ... not really used
   //  ] - occ             ... not really used
   // 
   //  i - info dump       ... keep
   //  g - guide lines     ... keep
   //  x - CEditor         ... keep
   //  p - screen capture  ... keep
   //  r - reset           ... keep
   //  m - swap models     ... keep
   //  ; - swap sorting    ... keep
   //  w - swap glow filter
   //  o - swap depth-peel/normal rendering
   //  f - switch focus mode
   //  d - switch whether to use 3D
   //
   //  c - alternates colouring 
   //  a - switch aggregation
   //  o - use dual depth peeling
   //  1 - swaps between constant and preset alpha
   //
   //  j - sparkline width--
   //  k - sparkline width++
   //  0 - reverse colour scale alpha
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void keyPressed(KeyEvent e) {
      
      // Special meta
      if (e.isShiftDown()) {
         SSM.shiftKey = true;
         return; 
      }
      if (e.isControlDown()) {
         SSM.controlKey = true;
      }
      
      
      // Regular key presses 
      if (SSM.controlKey == false)  {
         if (e.getKeyChar() == '1') {
            SSM.chartMode = SSM.CHART_MODE_BY_MONTH_MAX;
         }
         if (e.getKeyChar() == '2') {
            SSM.chartMode = SSM.CHART_MODE_BY_COMPONENT_MAX;
         }
         if (e.getKeyChar() == '3') {
            SSM.chartMode = SSM.CHART_MODE_BY_GLOBAL_MAX;
         }
         if (e.getKeyChar() == 'd') {
            SSM.use3DModel = ! SSM.use3DModel;   
            SSM.dirty = 1;
            SSM.dirtyGL = 1;
         }
         if (e.getKeyChar() == 's') {
            SSM.useComparisonMode =! SSM.useComparisonMode;   
            SSM.dirty = 1;
            SSM.dirtyGL = 1;
            SSM.refreshMagicLens = true;
            SSM.dirtyDateFilter = 1;
         }
         if (e.getKeyChar() == 'a') {
            SSM.useAggregate = ! SSM.useAggregate;
            SSM.dirty = 1;
            SSM.dirtyGL = 1;
            SSM.refreshMagicLens = true;
         }
      }
       
     
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Don't really need the stuff below. Most are either debugging or
      // experimental functions. Note we use the control modifier key
      // 1 => Stippling
      // 2 => Lighting 
      // 3 => Depth Peeling
      // 4 => Heatmap Widgets
      ////////////////////////////////////////////////////////////////////////////////
      if (SSM.controlKey) {
         if (e.getKeyCode() == '1') {
            SSM.useStipple = ! SSM.useStipple;   
         }
         if (e.getKeyChar() == '2') {
            SSM.useLight = ! SSM.useLight;   
         }
         if (e.getKeyChar() == '3') {
            SSM.useDualDepthPeeling = ! SSM.useDualDepthPeeling;
         }
         if (e.getKeyChar() == '4') {
            SSM.showLabels = ! SSM.showLabels;   
         }
      }
      
      if (e.getKeyCode() == KeyEvent.VK_LEFT) {
         Event.hidePanel();   
      } 
      if (e.getKeyCode() == KeyEvent.VK_RIGHT){
         Event.showPanel();   
      }
      
      if (e.getKeyChar() == 'w') {
         SSM.useGlow = ! SSM.useGlow;   
      }
      if (e.getKeyCode() == KeyEvent.VK_UP) {
         System.out.println("Up Arrow");
         SSM.g_numPasses ++;   
      }
      if (e.getKeyCode() == KeyEvent.VK_DOWN){
         System.out.println("Down Arrow");
         SSM.g_numPasses --;   
      }
      // Massive hack hack to get high reslution screen
      if (e.getKeyChar() == '+') {
         ProjectDriver.frame.setBounds(0, 0, 3000, 3000);
      }
      if (e.getKeyChar() == '-') {
         ProjectDriver.frame.setBounds(0, 0, 900, 900);
      }
      if (e.getKeyChar() == '9')  {
         SSM.useConstantAlpha = ! SSM.useConstantAlpha;   
      }
      if (e.getKeyChar() == 't') {
         SSM.useFlag  = ! SSM.useFlag;
         SSM.dirty = 1;
         SSM.dirtyGL = 1;
         SSM.refreshMagicLens = true;
      }
//      if (e.getKeyChar() == 'f') {
//         SSM.useLocalFocus = ! SSM.useLocalFocus;    
//         SSM.dirty = 1;
//         SSM.dirtyGL = 1;
//         SSM.refreshMagicLens = true;
//      }
//      if (e.getKeyChar() == ';') {
//         SSM.sortingMethod ++;
//         SSM.sortingMethod %= 3;
//         SSM.refreshMagicLens = true;
//         SSM.dirtyGL = 1;
//      }
//      if (e.getKeyChar() == 'c') {
//         SSM.colouringMethod ++;
//         SSM.colouringMethod %= 5;
//         SSM.refreshMagicLens = true;
//         SSM.dirty = 1; // Need to change this later
//         SSM.dirtyGL = 1;
//      }
//      if (e.getKeyChar() == 'v') {
//         SSM.colouringMethod --;
//         if (SSM.colouringMethod < 0) SSM.colouringMethod = 4;
//         SSM.colouringMethod %= 5;
//         SSM.refreshMagicLens = true;
//         SSM.dirty = 1; // Need to change this later
//         SSM.dirtyGL = 1;
//      }
      
      // Camera move
//      if (e.getKeyChar() == 'w') {
//         DCCamera.instance().move(0.3f);
//      }
//      if (e.getKeyChar() == 's') {
//         DCCamera.instance().move(-0.3f);
//      }
//      
      if (e.getKeyChar() == ']') {
         System.out.println("Increasing occ level");
         SSM.occlusionLevel ++;
      }
      if (e.getKeyChar() == '[') {
         System.out.println("Decreasing occ level");
         SSM.occlusionLevel --; 
      }
      if (e.getKeyChar() == '?') {
         SSM.instance().timeFrameStatistics();   
      }
      if (e.getKeyChar() == 'g') {
         SSM.useGuide = ! SSM.useGuide;      
      }
      
      // Show colour editor
      if (e.getKeyChar() == 'x') {
         CEditor.instance().setVisible( !CEditor.instance().isVisible() );   
      }
      // Print screen to file
      if (e.getKeyChar() == 'p') {
         SSM.captureScreen = true;
      }
      // Swap model
      if (e.getKeyChar() == 'm') {
         MM.instance().nextModel();   
         SSM.refreshMagicLens = true;
         System.out.println("Current instance is " + MM.instance().modelIndex);
      }
      // Camera reset
      if (e.getKeyChar() == 'r') {
         DCCamera.instance().reset();
         SSM.instance().reset();
      }
      
      // Update status
      StatusWindow.update();
   }

   
   
   @Override
   public void keyReleased(KeyEvent e) {
      
      if ( ! e.isShiftDown()) {
         SSM.shiftKey = false;   
      }
      if ( ! e.isControlDown()) {
         SSM.controlKey = false;   
      }
      
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
         ALogger.instance().cleanup();
         System.out.println("ESC exit...");
         System.exit(0);
      }
      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
         SSM.docActive = ! SSM.docActive;
         SSM.resizePanel = 1;
      }
   }
   

   @Override
   public void keyTyped(KeyEvent arg0) {
   }

   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // From MouseMotionLisener
   // 
   // We have an implict precedence order of handling event, from highest to lowest
   // - document viewer
   // - magic lens
   // - gui slider
   // - camera rotation
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void mouseDragged(MouseEvent e) {
      SSM.oldMouseX = SSM.mouseX;  
      SSM.oldMouseY = SSM.mouseY;  
      SSM.mouseX = e.getX();
      SSM.mouseY = e.getY();       
      
      if (SSM.dragPoints.get(999L) == null) {
         SSM.dragPoints.put(999L, new DCTriple());   
      }
      DCTriple d = SSM.dragPoints.get(999L);
      d.x = e.getX();
      d.y = e.getY();
      
      // Is this really necessary ???
      if (SSM.hoverPoints.get(999L) == null) {
         SSM.hoverPoints.put(999L, new DCTriple());
      }
      DCTriple p = SSM.hoverPoints.get(999L);
      p.x = e.getX();
      p.y = e.getY();      
      
      if (SSM.lensSelected() == 0 &&
          SSM.l_mousePressed &&
          SSM.topElement == SSM.ELEMENT_NONE) {
         

         if ( SSM.shiftKey && (SSM.oldMouseX > SSM.mouseX ||
              SSM.oldMouseX < SSM.mouseX)) {
            float val = (float)(SSM.mouseX - SSM.oldMouseX);
            SSM.rotateY += val;
            System.out.println("Shift + X");
            return;
         }
         
         
         /* Ignore the Y rotation
         if ( this.shiftKeyPressed && (SSM.instance().oldMouseY > SSM.instance().mouseY ||
              SSM.instance().oldMouseY < SSM.instance().mouseY)) {
            float val = (float)(SSM.instance().mouseY - SSM.instance().oldMouseY);
            SSM.instance().rotateX += val;
            System.out.println("Shift + Y");
            return;
         }
         */
         
         //Event.setCamera(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
         Event.setCameraTUIO(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
         SSM.refreshMagicLens = true;
      }
      
      Event.checkGUIDrag(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
      
      
      // Moving the lens
      if (SSM.l_mousePressed) {
         Event.moveLens(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
         Event.moveLensHandle(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
      }
      
      // Resize the lens
      if (SSM.r_mousePressed) {
         Event.resizeLens(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
      }
   }
   

   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Update the mouse pointer position, and the previous mouse pointer position
   // 
   // Also any "hovering" effects (Which may need to be removed for touch surface
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void mouseMoved(MouseEvent e) {
      SSM.oldMouseX = SSM.mouseX;  
      SSM.oldMouseY = SSM.mouseY;  
      SSM.mouseX = e.getX();
      SSM.mouseY = e.getY();
      
      if (SSM.hoverPoints.get(999L) == null) {
         SSM.hoverPoints.put(999L, new DCTriple());
      }
      DCTriple p = SSM.hoverPoints.get(999L);
      p.x = e.getX();
      p.y = e.getY();
      
      for (DCTip tip : SSM.tooltips.values()) {
         tip.visible = false;
      }
   }
   
   
   
   
   @Override
   public void mouseWheelMoved(MouseWheelEvent e) {
      int flag = 0;
     
      flag = Event.scrollLens(e.getX(), e.getY(), e.getUnitsToScroll());
      if (flag == 1) return;
      
      // Allow the mouse wheel to control the text panel scroll
      if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL && Event.inDocContext(e.getX(), e.getY())) {
         Event.checkDocumentScroll(e.getX(), e.getY(), e.getUnitsToScroll());
         return;
      }
      
      // 3D manipulation should be checked last
      if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL && ! Event.inDocContext(e.getX(), e.getY())) {
         if (e.getUnitsToScroll() > 0) {
            DCCamera.instance().move(-1.5f);
            SSM.refreshMagicLens = true;
            SSM.refreshOITTexture = true;
         } else {
            DCCamera.instance().move(1.5f);
            SSM.refreshMagicLens = true;
            SSM.refreshOITTexture = true;
         }
      }       
      
   }   
   

}
