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

import Jama.Matrix;

import util.ALogger;
import util.DCCamera;
import util.DCUtil;
import util.DWin;
import util.MatrixUtil;
import util.SerializeUtil;

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
         SSM.instance().l_mouseClicked = true;
         if (e.getClickCount() == 2) {
            System.out.println("Left button double clickec");
            LensAttrib la = new LensAttrib( SSM.instance().mouseX, SSM.instance().mouseY, 100.0f, 0);      
            la.magicLensType = LensAttrib.LENS_DEPTH;
            SSM.instance().lensList.add( la );
            SSM.instance().refreshMagicLens = true;
         }
      } else  if (e.getButton() == MouseEvent.BUTTON3){
         SSM.instance().r_mouseClicked = true;
         if (e.getClickCount() == 2) {
            System.out.println("Right button double clickec");
            
            // TODO: This is a bit buggy due to the removal while still iterating the list
            for (int i=0; i < SSM.instance().lensList.size(); i++) {
               float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
               float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
               float r = (float)SSM.instance().lensList.elementAt(i).magicLensRadius;
               float d = (float)Math.sqrt(x*x + y*y);            
               if (d < r) {
                  SSM.instance().lensList.remove(i);   
               }
               
            }
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
      lastPressX = SSM.instance().mouseX;
      lastPressY = SSM.instance().mouseY;
      
      // Check to see if the document viewer is pressed
//      if (e.getButton() == MouseEvent.BUTTON1){
//         if (SSM.instance().inDocFooter()) {
//            SSM.instance().nextDoc();   
//         } else if (SSM.instance().inDocHeader()) {
//            SSM.instance().previousDoc();   
//         } 
//         
//      }
      
      
      if (e.getButton() == MouseEvent.BUTTON1) {
         for (int i=0; i < SSM.instance().lensList.size(); i++) {
            float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
            float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
            float r = (float)SSM.instance().lensList.elementAt(i).magicLensRadius;
            float d = (float)Math.sqrt(x*x + y*y);
            
            if (d >= r-10.0 && d <= r) {
               SSM.instance().lensList.elementAt(i).magicLensSelected = 1;
               SSM.instance().topElement = SSM.ELEMENT_LENS;
            }
         }
         
         
         float mx = SSM.instance().mouseX;
         float my = SSM.instance().windowHeight - SSM.instance().mouseY;
         float anchorX = SSM.instance().docAnchorX;
         float anchorY = SSM.instance().docAnchorY;
         float docWidth  = SSM.instance().docWidth;
         float docHeight = SSM.instance().docHeight;
         float padding   = SSM.instance().docPadding;
         
         if (DCUtil.between(mx, anchorX-padding, anchorX) || DCUtil.between(mx, anchorX+docWidth, anchorX+docWidth+padding)) {
            if (DCUtil.between(my, anchorY-padding, anchorY+docHeight+padding)) {
               SSM.instance().topElement = SSM.ELEMENT_DOCUMENT;   
            }
         }
         
         if (DCUtil.between(my, anchorY-padding, anchorY) || DCUtil.between(my, anchorY+docHeight, anchorY+docHeight+padding)) {
            System.out.println("blah");
            if (DCUtil.between(mx, anchorX-padding, anchorX+docWidth+padding)) {
               SSM.instance().topElement = SSM.ELEMENT_DOCUMENT;   
            }
         }
         
         float mf_anchorX = SSM.instance().manufactureAnchorX;
         float mf_anchorY = SSM.instance().manufactureAnchorY;
         if (DCUtil.between(mx, mf_anchorX, mf_anchorX+SSM.instance().scrollWidth)) {
            if (DCUtil.between(my, mf_anchorY, mf_anchorY+SSM.instance().manufactureHeight)) {
               if (SSM.instance().manufactureActive) {
                  SSM.instance().topElement = SSM.ELEMENT_MANUFACTURE_SCROLL;
               }
            }
         }
         
         float mk_anchorX = SSM.instance().makeAnchorX;
         float mk_anchorY = SSM.instance().makeAnchorY;
         if (DCUtil.between(mx, mk_anchorX, mk_anchorX+SSM.instance().scrollWidth)) {
            if (DCUtil.between(my, mk_anchorY, mk_anchorY+SSM.instance().makeHeight)) {
               if (SSM.instance().makeActive) {
                  SSM.instance().topElement = SSM.ELEMENT_MAKE_SCROLL;
               }
            }
         }
         
         float md_anchorX = SSM.instance().modelAnchorX;
         float md_anchorY = SSM.instance().modelAnchorY;
         if (DCUtil.between(mx, md_anchorX, md_anchorX+SSM.instance().scrollWidth)) {
            if (DCUtil.between(my, md_anchorY, md_anchorY+SSM.instance().modelHeight)) {
               if (SSM.instance().modelActive) {
                  SSM.instance().topElement = SSM.ELEMENT_MODEL_SCROLL;
               }
            }
         }
         
         float sl_anchorX = SSM.instance().saveLoadAnchorX;
         float sl_anchorY = SSM.instance().saveLoadAnchorY;
         if (DCUtil.between(mx, sl_anchorX, sl_anchorX+SSM.instance().scrollWidth)) {
            if (DCUtil.between(my, sl_anchorY, sl_anchorY+SSM.instance().saveLoadHeight)) {
               if (SSM.instance().saveLoadActive) {
                  SSM.instance().topElement = SSM.ELEMENT_SAVELOAD_SCROLL;
               }
            }
         }
         
         /*
         if ( (mx >= SSM.instance().docAnchorX && mx <= SSM.instance().docAnchorX+SSM.instance().docPadding) ||
              (mx >= SSM.instance().docAnchorX+SSM.instance().docWidth-SSM.instance().docPadding && mx <= SSM.instance().docAnchorX + SSM.instance().docWidth) ) {
            if (my >= SSM.instance().docAnchorY && my <= SSM.instance().docAnchorY + SSM.instance().docHeight) {
               SSM.instance().topElement = SSM.ELEMENT_DOCUMENT;   
            }
         }
         
         if ( (my >= SSM.instance().docAnchorY && my <= SSM.instance().docAnchorY + SSM.instance().docPadding) || 
              (my >= SSM.instance().docAnchorY+SSM.instance().docHeight-SSM.instance().docPadding && my <= SSM.instance().docAnchorY + SSM.instance().docHeight)) {
            if (mx >= SSM.instance().docAnchorX && mx <= SSM.instance().docAnchorX+SSM.instance().docWidth) {
               SSM.instance().topElement = SSM.ELEMENT_DOCUMENT;   
            }
         }
         */
      
         
         
         //SSM.instance().mouseState |= SSM.MOUSE_LPRESS;
         SSM.instance().l_mousePressed = true;
      } else if (e.getButton() == MouseEvent.BUTTON3) {
         for (int i=0; i < SSM.instance().lensList.size(); i++) {
            float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
            float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
            float r = (float)SSM.instance().lensList.elementAt(i).magicLensRadius;
            float d = (float)Math.sqrt(x*x + y*y);            
            
            if (d >= r-10.0 && d <= r) {
               SSM.instance().lensList.elementAt(i).magicLensSelected = 1;
               SSM.instance().topElement = SSM.ELEMENT_LENS;
            }
         }
         
         SSM.instance().r_mousePressed = true;
      }
         
   }
   

   @Override
   public void mouseReleased(MouseEvent e) {
      //System.out.println("Mouse Released...");
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
      
     
      if (e.getKeyChar() == 'w') {
         SSM.instance().useGlow = ! SSM.instance().useGlow;   
      }
      
      if (e.getKeyChar() == 'o') {
         SSM.instance().useDualDepthPeeling = ! SSM.instance().useDualDepthPeeling;
      }
      if (e.getKeyChar() == '1')  {
         SSM.instance().useConstantAlpha = ! SSM.instance().useConstantAlpha;   
      }
      
      if (e.getKeyChar() == 'a') {
         SSM.instance().useAggregate = ! SSM.instance().useAggregate;
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
      
      if (e.getKeyChar() == '0') {
         SSM.instance().sparklineMode = 0;   
         SSM.instance().dirtyGL = 1;
      }
      if (e.getKeyChar() == '1') {
         SSM.instance().sparklineMode = 1;   
         SSM.instance().dirtyGL = 1;
      }
      
      if (e.getKeyChar() == '?') {
         SSM.instance().timeFrameStatistics();   
      }
      
      
      // information
      if (e.getKeyChar() == 'i') {
        
        DWin.instance().error("________________________________________");
        DWin.instance().error("Selected :" + SSM.instance().selectedGroup);   
        DWin.instance().error("SSM starting year :" + SSM.instance().startYear);   
        DWin.instance().error("SSM ending   year :" + SSM.instance().endYear);   
        DWin.instance().error("SSM starting month :" + SSM.instance().startMonth);   
        DWin.instance().error("SSM ending month   :" + SSM.instance().endMonth);   
        DWin.instance().error("SSM selected manufacture : " + SSM.instance().selectedManufacture);
        DWin.instance().error("SSM selected make: " + SSM.instance().selectedMake);
        DWin.instance().error("SSM selected model: " + SSM.instance().selectedModel);
        DWin.instance().error("________________________________________");
        DWin.instance().error("Total Vertices : "  + MM.currentModel.getTotalVertices());
        
        
      }
      
      // Turn on/off guides
      if (e.getKeyChar() == 'g') {
         SSM.instance().useGuide = ! SSM.instance().useGuide;      
      }
//      if (e.getKeyChar() == 'c') {
//         SSM.instance().useCircularLabel = ! SSM.instance().useCircularLabel;
//      }
      if (e.getKeyChar() == 'x') {
         CEditor.instance().setVisible( !CEditor.instance().isVisible() );   
      }
      
      // vim key binding....out of keys...doh ! 
      if (e.getKeyChar() == 'k') {
         SSM.instance().sparkLineWidth ++;   
         SSM.instance().dirtyGL = 1;
      }
      if (e.getKeyChar() == 'j') {
         SSM.instance().sparkLineWidth --;
         SSM.instance().dirtyGL = 1;
      }
      
      if (e.getKeyChar() == 'p') {
         SSM.instance().captureScreen = true;
      }
      
      
      if (e.getKeyChar() == 'm') {
         MM.instance().nextModel();   
         SSM.instance().refreshMagicLens = true;
         System.out.println("Current instance is " + MM.instance().modelIndex);
      }
      
      if (e.getKeyChar() == '0') {
         SSM.instance().colourRampReverseAlpha = ! SSM.instance().colourRampReverseAlpha;
         SSM.instance().dirtyGL = 1;
         SSM.instance().refreshMagicLens = true;
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
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
         ALogger.instance().cleanup();
         System.out.println("ESC exit...");
         System.exit(0);
      }
      if (e.getKeyCode() == e.VK_SPACE) {
         SSM.instance().docActive = ! SSM.instance().docActive;
         SSM.instance().resizePanel = 1;
      }
      /*
      if (e.getKeyCode() == KeyEvent.VK_1){
         SSM.instance().renderSihoulette = false;   
         SSM.instance().useLight = true;
      } else if (e.getKeyCode() == KeyEvent.VK_2){
         SSM.instance().renderSihoulette = false; 
         SSM.instance().useLight = false;
      }
      */
      if (e.getKeyChar() == KeyEvent.VK_SHIFT) {
         shiftKeyPressed = false;   
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
      SSM.instance().oldMouseX = SSM.instance().mouseX;  
      SSM.instance().oldMouseY = SSM.instance().mouseY;  
      SSM.instance().mouseX = e.getX();
      SSM.instance().mouseY = e.getY();       
      
      // Just a proof of concept of draggable text scrolling
      if (SSM.instance().inDocContext()) {
         float val = SSM.instance().oldMouseY - SSM.instance().mouseY;
         
         // Lock controls when the total panel height is less than that of the display height
         if (SSM.instance().t1Height + SSM.instance().t2Height <= SSM.instance().docHeight) return;
         
         if (val < 0) {
            // Prevent underflow
            if (SSM.instance().yoffset <= SSM.instance().docHeight) return;
            
            if (SSM.instance().yoffset <= SSM.instance().t1Height && SSM.instance().t1Start > 0 ) {
               System.out.println("Getting previous text");               
               //SSM.instance().yoffset -= SSM.instance().t1Height;
               SSM.instance().t1Start = Math.max(0, SSM.instance().t1Start - SSM.instance().globalFetchSize);
               SSM.instance().t2Start = Math.max(SSM.instance().globalFetchSize, SSM.instance().t2Start - SSM.instance().globalFetchSize);
               SSM.instance().docAction = 1;   
               SSM.instance().dirtyGL = 1;
            } else {
               SSM.instance().yoffset += val;
            }
            
//            if (yoffset <= t1.textPaneHeight) {
//               System.out.println("Switch...");
//               yoffset += t3.textPaneHeight;
//            }
         } else {
            // Check to see if T2 even exist
            if (SSM.instance().yoffset > SSM.instance().t1Height && SSM.instance().t2Height <= 0) return;
            
            // Check to see if we have run off the number allocated for the period
            if (SSM.instance().t2Start + SSM.instance().globalFetchSize > SSM.instance().docMaxSize) {
               if (SSM.instance().yoffset >= SSM.instance().t1Height + SSM.instance().t2Height)
                  return;
            }
            
            if (SSM.instance().yoffset - SSM.instance().docHeight > SSM.instance().t1Height) {
               
               
               System.out.println("Getting next text");
               SSM.instance().yoffset -= SSM.instance().t1Height;
               SSM.instance().t1Start += SSM.instance().globalFetchSize;
               SSM.instance().t2Start += SSM.instance().globalFetchSize;
               SSM.instance().docAction = 2;   
               SSM.instance().dirtyGL = 1;
            } else {
               SSM.instance().yoffset += val;
            }
//            if (yoffset - displayH > t1.textPaneHeight) {
//               System.out.println("Switch...");
//               yoffset -= t1.textPaneHeight;
//            }
         }
         
         /*
         if (lastPressY - SSM.instance().mouseY < -10) {
            lastPressY = SSM.instance().mouseY;
            SSM.instance().nextDoc();
         }
         if (lastPressY - SSM.instance().mouseY > 10) {
            lastPressY = SSM.instance().mouseY;
            SSM.instance().previousDoc();
         }
         */
         return;
      }
      
      
      if (SSM.instance().currentFocusLayer == 0 &&
          SSM.instance().lensSelected() == 0 &&
          SSM.instance().l_mousePressed &&
          SSM.instance().topElement == SSM.ELEMENT_NONE) {
         
         double basis[][] = {
            { DCCamera.instance().right.x, DCCamera.instance().right.y, DCCamera.instance().right.z, 0 },      
            { DCCamera.instance().up.x, DCCamera.instance().up.y, DCCamera.instance().up.z, 0 },      
            { DCCamera.instance().look.x, DCCamera.instance().look.y, DCCamera.instance().look.z, 0 },      
            { 0, 0, 0, 1}
         };
         Matrix m_basis      = new Matrix(basis);
         Matrix m_basisT     = m_basis.inverse();
         
         if ( SSM.instance().oldMouseX > SSM.instance().mouseX ||
              SSM.instance().oldMouseX < SSM.instance().mouseX) {
            float factor; 
            if (SSM.instance().useDualDepthPeeling) {
               factor= SSM.instance().oldMouseX > SSM.instance().mouseX ? -3.0f : 3.0f; 
            } else {
               factor= SSM.instance().oldMouseX > SSM.instance().mouseX ? -1.0f : 1.0f; 
            }
            Matrix m_rotation    = MatrixUtil.rotationMatrix(factor*2.0, "Y");
            Matrix m = m_basisT.times(m_rotation).times(m_basis);
            double[] newPosition = MatrixUtil.multVector(m, DCCamera.instance().eye.toArrayd());
            DCCamera.instance().eye = new DCTriple(newPosition);
            DCTriple newDir = new DCTriple(0.0f, 0.0f, 0.0f).sub(DCCamera.instance().eye);
            newDir.normalize();
            DCCamera.instance().look = new DCTriple(newDir);
            DCCamera.instance().right = DCCamera.instance().look.cross(DCCamera.instance().up);
            DCCamera.instance().right.normalize();
            
            SSM.instance().refreshOITTexture = true;
         }
         
         if ( SSM.instance().oldMouseY > SSM.instance().mouseY ||
              SSM.instance().oldMouseY < SSM.instance().mouseY) {
            float factor;
            if (SSM.instance().useDualDepthPeeling) {
               factor = SSM.instance().oldMouseY > SSM.instance().mouseY ? -3.0f : 3.0f; 
            } else {
               factor = SSM.instance().oldMouseY > SSM.instance().mouseY ? -1.0f : 1.0f; 
            }
            Matrix m_rotation    = MatrixUtil.rotationMatrix(factor*2.0, "X");
            Matrix m = m_basisT.times(m_rotation).times(m_basis);
            double[] newPosition = MatrixUtil.multVector(m, DCCamera.instance().eye.toArrayd());
            DCCamera.instance().eye = new DCTriple(newPosition);
            DCTriple newDir = new DCTriple(0.0f, 0.0f, 0.0f).sub(DCCamera.instance().eye);
            newDir.normalize();
            DCCamera.instance().look = new DCTriple(newDir);
            DCCamera.instance().up = DCCamera.instance().look.cross(DCCamera.instance().right).mult(-1.0f);
            DCCamera.instance().up.normalize();
            
            SSM.instance().refreshOITTexture = true;
         }
         SSM.instance().refreshMagicLens = true;
      }
      
      
      // Check the top level UI elements
      if (SSM.instance().topElement == SSM.ELEMENT_DOCUMENT) {
         SSM.instance().docAnchorX += (SSM.instance().mouseX - SSM.instance().oldMouseX);   
         SSM.instance().docAnchorY -= (SSM.instance().mouseY - SSM.instance().oldMouseY);   
      } else if (SSM.instance().topElement == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         SSM.instance().manufactureYOffset -= (SSM.instance().mouseY - SSM.instance().oldMouseY);   
         if (SSM.instance().manufactureYOffset < SSM.instance().manufactureHeight)
            SSM.instance().manufactureYOffset = SSM.instance().manufactureHeight;
         if (SSM.instance().manufactureYOffset > SSM.instance().manufactureTexHeight)
            SSM.instance().manufactureYOffset = SSM.instance().manufactureTexHeight;
      } else if (SSM.instance().topElement == SSM.ELEMENT_MAKE_SCROLL) {
         SSM.instance().makeYOffset -= (SSM.instance().mouseY - SSM.instance().oldMouseY);   
         if (SSM.instance().makeYOffset < SSM.instance().makeHeight)
            SSM.instance().makeYOffset = SSM.instance().makeHeight;
         if (SSM.instance().makeYOffset > SSM.instance().makeTexHeight)
            SSM.instance().makeYOffset = SSM.instance().makeTexHeight;
      } else if (SSM.instance().topElement == SSM.ELEMENT_MODEL_SCROLL)  {
         SSM.instance().modelYOffset -= (SSM.instance().mouseY - SSM.instance().oldMouseY);   
         if (SSM.instance().modelYOffset < SSM.instance().modelHeight)
            SSM.instance().modelYOffset = SSM.instance().modelHeight;
         if (SSM.instance().modelYOffset > SSM.instance().modelTexHeight)
            SSM.instance().modelYOffset = SSM.instance().modelTexHeight;
      } else if (SSM.instance().topElement == SSM.ELEMENT_SAVELOAD_SCROLL) {
         SSM.instance().saveLoadYOffset -= (SSM.instance().mouseY - SSM.instance().oldMouseY);   
         if (SSM.instance().saveLoadYOffset < SSM.instance().saveLoadHeight)
            SSM.instance().saveLoadYOffset = SSM.instance().saveLoadHeight;
         if (SSM.instance().saveLoadYOffset > SSM.instance().saveLoadTexHeight)
            SSM.instance().saveLoadYOffset = SSM.instance().saveLoadTexHeight;
      }
      
      
      // Moving the lens
      if (SSM.instance().l_mousePressed) {
         for (int i=0; i < SSM.instance().lensList.size(); i++) {
            if (SSM.instance().lensList.elementAt(i).magicLensSelected == 1) {
               SSM.instance().lensList.elementAt(i).magicLensX += (SSM.instance().mouseX - SSM.instance().oldMouseX);   
               SSM.instance().lensList.elementAt(i).magicLensY += (SSM.instance().mouseY - SSM.instance().oldMouseY);   
            }
         }
      }
      
      // Resize the lens
      if (SSM.instance().r_mousePressed) {
         for (int i=0; i < SSM.instance().lensList.size(); i++) {
            if (SSM.instance().lensList.elementAt(i).magicLensSelected == 1) {
               float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
               float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
               float d = (float)Math.sqrt(x*x + y*y);         
               SSM.instance().lensList.elementAt(i).magicLensRadius = d;  
            }
         }
      }
      
      
      
   }
   

   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Update the mouse pointer position, and the previous mouse pointer position
   // 
   // Also any "hovering" effects (Which may need to be removed for touch surface
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void mouseMoved(MouseEvent e) {
      SSM.instance().oldMouseX = SSM.instance().mouseX;  
      SSM.instance().oldMouseY = SSM.instance().mouseY;  
      SSM.instance().mouseX = e.getX();
      SSM.instance().mouseY = e.getY();
      
      for (int i=0; i < SSM.instance().lensList.size(); i++) {
         float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
         float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
         float r = (float)SSM.instance().lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);
         if ( d <= r ) {
            SSM.instance().lensList.elementAt(i).magicLensSelected = 1;
         } else {
            SSM.instance().lensList.elementAt(i).magicLensSelected = 0;
         }
      }
   }
   
   
   
   
   @Override
   public void mouseWheelMoved(MouseWheelEvent e) {
      int flag = 0;
      for (int i=0; i < SSM.instance().lensList.size(); i++) {
         float x = (float)SSM.instance().mouseX - (float)SSM.instance().lensList.elementAt(i).magicLensX;
         float y = (float)SSM.instance().mouseY - (float)SSM.instance().lensList.elementAt(i).magicLensY;
         float r = (float)SSM.instance().lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);
         
         if ( d <= r ) {
            flag = 1;
            LensAttrib la = SSM.instance().lensList.elementAt(i);
            
            if (la != null ) {
               if (e.getUnitsToScroll() < 0) {
                  double totalD = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
                  double remainD = totalD - la.nearPlane;
                  double advD    = Math.max(0.3f, remainD*0.05);
                  
                  if (la.nearPlane + advD < totalD)
                     la.nearPlane += advD;
               } else {
                  double totalD = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
                  double remainD = totalD - la.nearPlane;
                  double advD    = Math.max(0.3f, remainD*0.05);
                  
                  if (la.nearPlane - advD > 0) 
                     la.nearPlane -= advD;               
               }
               SSM.instance().refreshMagicLens = true;
            }            
         }
      }      
      if (flag == 1) return;
      
      
      if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
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
   
   public boolean shiftKeyPressed = false;
   public float lastPressX;
   public float lastPressY;


}
