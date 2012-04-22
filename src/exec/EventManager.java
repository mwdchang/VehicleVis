package exec;

import gui.StatusWindow;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.event.MouseInputListener;

import model.DCTriple;
import model.LensAttrib;
import model.PaneAttrib;

import Jama.Matrix;

import util.ALogger;
import util.DCCamera;
import util.DCUtil;
import util.DWin;
import util.MatrixUtil;
import util.SerializeUtil;

import datastore.CacheManager;
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
         if (SSM.instance().controlKey == true) {
            Event.createLens(SSM.mouseX, SSM.mouseY);
         } else {
            SSM.pickPoints.add(new DCTriple(SSM.mouseX, SSM.mouseY, 0));
            SSM.instance().l_mouseClicked = true;
         }
      } else  if (e.getButton() == MouseEvent.BUTTON3){
         if (SSM.instance().controlKey == true) {
            Event.removeLens(SSM.mouseX, SSM.mouseY);
         } else  {
            SSM.instance().r_mouseClicked = true;
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
         
         // Check the document widget
         Event.checkDocumentPanel(e.getX(), e.getY());
         
         // For default filter
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().makeAttrib, SSM.ELEMENT_MAKE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().modelAttrib, SSM.ELEMENT_MODEL_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().yearAttrib, SSM.ELEMENT_YEAR_SCROLL);
         
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().c_manufactureAttrib, SSM.ELEMENT_CMANUFACTURE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().c_makeAttrib, SSM.ELEMENT_CMAKE_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().c_modelAttrib, SSM.ELEMENT_CMODEL_SCROLL);
         Event.checkScrollPanels(e.getX(), e.getY(), SSM.instance().c_yearAttrib, SSM.ELEMENT_CYEAR_SCROLL);
         
         // Check the ranged slider
         Event.checkSlider(e.getX(), e.getY());
         
         
//         float sl_anchorX = SSM.instance().saveLoadAnchorX;
//         float sl_anchorY = SSM.instance().saveLoadAnchorY;
//         if (DCUtil.between(mx, sl_anchorX, sl_anchorX+SSM.instance().scrollWidth)) {
//            if (DCUtil.between(my, sl_anchorY, sl_anchorY+SSM.instance().saveLoadHeight)) {
//               if (SSM.instance().saveLoadActive) {
//                  SSM.instance().topElement = SSM.ELEMENT_SAVELOAD_SCROLL;
//               }
//            }
//         }
         
         
 
         SSM.instance().l_mousePressed = true;
      } else if (e.getButton() == MouseEvent.BUTTON3) {
         Event.checkLens(e.getX(), e.getY());
         SSM.instance().r_mousePressed = true;
      }
         
   }
   

   
   @Override
   public void mouseReleased(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         SSM.instance().l_mousePressed = false;
         SSM.instance().clearLens();
      } else if (e.getButton() == MouseEvent.BUTTON3) {
         SSM.instance().r_mousePressed = false;
         SSM.instance().clearLens();
      }
      SSM.instance().topElement = SSM.ELEMENT_NONE;
      
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
         SSM.instance().shiftKey = true;
         return; 
      }
      if (e.isControlDown()) {
         SSM.instance().controlKey = true;
      }
      
      
      if (e.getKeyChar() == '1') {
         SSM.instance().chartMode = SSM.CHART_MODE_BY_MONTH_MAX;
      }
      if (e.getKeyChar() == '2') {
         SSM.instance().chartMode = SSM.CHART_MODE_BY_COMPONENT_MAX;
      }
      if (e.getKeyChar() == '3') {
         SSM.instance().chartMode = SSM.CHART_MODE_BY_GLOBAL_MAX;
      }
      if (e.getKeyChar() == 'd') {
         SSM.instance().use3DModel = ! SSM.instance().use3DModel;   
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1;
      }
      if (e.getKeyChar() == 's') {
         SSM.instance().useComparisonMode =! SSM.instance().useComparisonMode;   
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1;
         SSM.instance().refreshMagicLens = true;
      }
      if (e.getKeyChar() == 'a') {
         SSM.instance().useAggregate = ! SSM.instance().useAggregate;
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1;
         SSM.instance().refreshMagicLens = true;
      }
     
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Don't really need the stuff below. Most are either debugging or
      // experimental functions
      ////////////////////////////////////////////////////////////////////////////////
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
      if (e.getKeyChar() == '4') {
         SSM.instance().useLight = ! SSM.instance().useLight;   
      }
      if (e.getKeyChar() == 'w') {
         SSM.instance().useGlow = ! SSM.instance().useGlow;   
      }
      if (e.getKeyChar() == 'o') {
         SSM.instance().useDualDepthPeeling = ! SSM.instance().useDualDepthPeeling;
      }
      if (e.getKeyChar() == '9')  {
         SSM.instance().useConstantAlpha = ! SSM.instance().useConstantAlpha;   
      }
      if (e.getKeyChar() == 't') {
         SSM.instance().useFlag  = ! SSM.instance().useFlag;
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1;
         SSM.instance().refreshMagicLens = true;
      }
      if (e.getKeyChar() == 'f') {
         SSM.instance().useLocalFocus = ! SSM.instance().useLocalFocus;    
         SSM.instance().dirty = 1;
         SSM.instance().dirtyGL = 1;
         SSM.instance().refreshMagicLens = true;
      }
      if (e.getKeyChar() == ';') {
         SSM.instance().sortingMethod ++;
         SSM.instance().sortingMethod %= 3;
         SSM.instance().refreshMagicLens = true;
         SSM.instance().dirtyGL = 1;
      }
      if (e.getKeyChar() == 'c') {
         SSM.instance().colouringMethod ++;
         SSM.instance().colouringMethod %= 5;
         SSM.instance().refreshMagicLens = true;
         SSM.instance().dirty = 1; // Need to change this later
         SSM.instance().dirtyGL = 1;
      }
      if (e.getKeyChar() == 'v') {
         SSM.instance().colouringMethod --;
         if (SSM.instance().colouringMethod < 0) SSM.instance().colouringMethod = 4;
         SSM.instance().colouringMethod %= 5;
         SSM.instance().refreshMagicLens = true;
         SSM.instance().dirty = 1; // Need to change this later
         SSM.instance().dirtyGL = 1;
      }
      
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
         SSM.instance().occlusionLevel ++;
      }
      if (e.getKeyChar() == '[') {
         System.out.println("Decreasing occ level");
         SSM.instance().occlusionLevel --; 
      }
      if (e.getKeyChar() == 'l') {
         SSM.instance().showLabels = ! SSM.instance().showLabels;   
      }
      if (e.getKeyChar() == '?') {
         SSM.instance().timeFrameStatistics();   
      }
      
      // Turn on/off guides
      if (e.getKeyChar() == 'g') {
         SSM.instance().useGuide = ! SSM.instance().useGuide;      
      }
      // Show colour editor
      if (e.getKeyChar() == 'x') {
         CEditor.instance().setVisible( !CEditor.instance().isVisible() );   
      }
      // Print screen to file
      if (e.getKeyChar() == 'p') {
         SSM.instance().captureScreen = true;
      }
      // Swap model
      if (e.getKeyChar() == 'm') {
         MM.instance().nextModel();   
         SSM.instance().refreshMagicLens = true;
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
         SSM.instance().shiftKey = false;   
      }
      if ( ! e.isControlDown()) {
         SSM.instance().controlKey = false;   
      }
      
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
         ALogger.instance().cleanup();
         System.out.println("ESC exit...");
         System.exit(0);
      }
      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
         SSM.instance().docActive = ! SSM.instance().docActive;
         SSM.instance().resizePanel = 1;
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
      
      if (SSM.instance().lensSelected() == 0 &&
          SSM.instance().l_mousePressed &&
          SSM.instance().topElement == SSM.ELEMENT_NONE) {
         

         if ( SSM.instance().shiftKey && (SSM.oldMouseX > SSM.mouseX ||
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
         
         Event.setCamera(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
         SSM.instance().refreshMagicLens = true;
      }
      
      Event.checkGUIDrag(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
      
      
      // Moving the lens
      if (SSM.instance().l_mousePressed) {
         Event.moveLens(SSM.mouseX, SSM.mouseY, SSM.oldMouseX, SSM.oldMouseY);
      }
      
      // Resize the lens
      if (SSM.instance().r_mousePressed) {
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
            SSM.instance().refreshMagicLens = true;
            SSM.instance().refreshOITTexture = true;
         } else {
            DCCamera.instance().move(1.5f);
            SSM.instance().refreshMagicLens = true;
            SSM.instance().refreshOITTexture = true;
         }
      }       
      
   }   
   

}
