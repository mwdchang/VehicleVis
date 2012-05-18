package exec;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import model.DCTriple;
import model.LensAttrib;
import model.PaneAttrib;
import util.DCCamera;
import util.DCUtil;
import util.DWin;
import util.MatrixUtil;
import Jama.Matrix;
import TimingFrameExt.DCColourEval;
import TimingFrameExt.FloatEval;
import datastore.CacheManager;
import datastore.SSM;


////////////////////////////////////////////////////////////////////////////////
// Holds methods to handle discrete events
////////////////////////////////////////////////////////////////////////////////
public class Event {
   ////////////////////////////////////////////////////////////////////////////////
   // Creates a lens at (posX, posY)
   ////////////////////////////////////////////////////////////////////////////////
   public static void createLens(int posX, int posY) {
      LensAttrib la = new LensAttrib( posX, posY, 200.0f, 0);      
      la.magicLensType = LensAttrib.LENS_DEPTH;
      SSM.lensList.add( la );
      SSM.refreshMagicLens = true;
   }
   
   public static void createLens(int posX, int posY, float r) {
      // Hackhack: limit the amount of lens to 3 for now
      if (SSM.lensList.size() >= 2) return;
      
      LensAttrib la = new LensAttrib( posX, posY, r, 0);      
      la.magicLensType = LensAttrib.LENS_DEPTH;
      SSM.lensList.add( la );
      SSM.refreshMagicLens = true;
      
      
      // Debug
      DWin.instance().msg("Lens : " + posX + " " + posY + " " + r);
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Remove a lens at (posX, posY)
   ////////////////////////////////////////////////////////////////////////////////
   public static void removeLens(int posX, int posY) {
      // TODO: This is a bit buggy due to the removal while still iterating the list
      for (int i=0; i < SSM.lensList.size(); i++) {
         float x = (float)posX - (float)SSM.lensList.elementAt(i).magicLensX;
         float y = (float)posY - (float)SSM.lensList.elementAt(i).magicLensY;
         float r = (float)SSM.lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);            
         if (d < r) {
            SSM.lensList.remove(i);   
         }
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Move lens by delta
   ////////////////////////////////////////////////////////////////////////////////
   public static void moveLens(int posX, int posY, int oldPosX, int oldPosY) {
      for (int i=0; i < SSM.lensList.size(); i++) {
         if (SSM.lensList.elementAt(i).magicLensSelected == 1) {
            SSM.lensList.elementAt(i).magicLensX += (posX - oldPosX);   
            SSM.lensList.elementAt(i).magicLensY += (posY - oldPosY);   
            SSM.lensList.elementAt(i).start = 0;
         }
      }      
   }
   
   public static void moveLensTUIO(int posX, int posY, int oldPosX, int oldPosY) {
      for (int i=0; i < SSM.lensList.size(); i++) {
         float x = (float)posX - (float)SSM.lensList.elementAt(i).magicLensX;
         float y = (float)posY - (float)SSM.lensList.elementAt(i).magicLensY;
         float r = (float)SSM.lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);            
         if (d < r) {
            SSM.lensList.elementAt(i).magicLensX += posX - oldPosX;   
            SSM.lensList.elementAt(i).magicLensY += posY - oldPosY;   
            SSM.lensList.elementAt(i).start = 0;
         }
      }      
   }
  
   ////////////////////////////////////////////////////////////////////////////////
   // Resize lens by delta
   ////////////////////////////////////////////////////////////////////////////////
   public static void resizeLens(int posX, int posY, int oldPosX, int oldPosY) {
      for (int i=0; i < SSM.lensList.size(); i++) {
         if (SSM.lensList.elementAt(i).magicLensSelected == 1) {
            float x = (float)posX - (float)SSM.lensList.elementAt(i).magicLensX;
            float y = (float)posY - (float)SSM.lensList.elementAt(i).magicLensY;
            float d = (float)Math.sqrt(x*x + y*y);         
            
            // Test
            if (d < 50) {
               SSM.lensList.remove(i);
               break;
            }
            SSM.lensList.elementAt(i).magicLensRadius = d;  
         }
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Change lens cutting plane
   ////////////////////////////////////////////////////////////////////////////////
   public static int scrollLens(int posX, int posY, int unit) {
      int flag = 0;
      long systemTime = System.currentTimeMillis();
      for (int i=0; i < SSM.lensList.size(); i++) {
         float x = (float)posX - (float)SSM.lensList.elementAt(i).magicLensX;
         float y = (float)posY - (float)SSM.lensList.elementAt(i).magicLensY;
         float r = (float)SSM.lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);
         
         if ( d <= r ) {
            flag = 1;
            LensAttrib la = SSM.lensList.elementAt(i);
            
            // Reset/update the tool-tip
            if (la.tip.opacityAnimator != null) {
               if (la.tip.opacityAnimator.isRunning()) la.tip.opacityAnimator.stop();   
            }
            la.tip.lastUpdateTime = systemTime;
            la.tip.visible = true;
            la.tip.opacity = 0.7f; 
            la.tip.opacityAnimator = PropertySetter.createAnimator(SSM.FADE_DURATION, la.tip , "opacity", new FloatEval(), la.tip.opacity, 0.0f);
            la.tip.opacityAnimator.start();
            //la.tip.clear();
            //la.tip.addText( la.nearPlane +"");
            
            
            if (la != null ) {
               if (unit < 0) {
                  double totalD = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
                  double remainD = totalD - la.nearPlane;
                  double advD    = Math.max(0.3f, remainD*0.05);
                  if (la.nearPlane + advD < totalD)
                     la.nearPlane += advD;
System.out.println(">Near plane: " + la.nearPlane);                  
               } else {
                  double totalD = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
                  double remainD = totalD - la.nearPlane;
                  double advD    = Math.max(0.3f, remainD*0.05);
                  if (la.nearPlane - advD > 0) 
                     la.nearPlane -= advD;               
System.out.println("<Near plane: " + la.nearPlane);                  
               }
               SSM.refreshMagicLens = true;
            }            
         }
      }        
      return flag;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Change camera position
   ////////////////////////////////////////////////////////////////////////////////
   public static void setCamera(int posX, int posY, int oldPosX, int oldPosY) {
      double basis[][] = {
            { DCCamera.instance().right.x, DCCamera.instance().right.y, DCCamera.instance().right.z, 0 },      
            { DCCamera.instance().up.x, DCCamera.instance().up.y, DCCamera.instance().up.z, 0 },      
            { DCCamera.instance().look.x, DCCamera.instance().look.y, DCCamera.instance().look.z, 0 },      
            { 0, 0, 0, 1}
         };
      Matrix m_basis      = new Matrix(basis);
      Matrix m_basisT     = m_basis.inverse();         

      if ( oldPosX != posX ) {
         float factor; 
         if (SSM.useDualDepthPeeling) {
            factor= oldPosX > posX ? -3.0f : 3.0f; 
         } else {
            factor= oldPosX > posX ? -1.0f : 1.0f; 
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
         
         SSM.refreshOITTexture = true;
         SSM.refreshMagicLens = true;
      }
      
      if ( oldPosY != posY) {
         float factor;
         if (SSM.useDualDepthPeeling) {
            factor = oldPosY > posY ? -3.0f : 3.0f; 
         } else {
            factor = oldPosY > posY ? -1.0f : 1.0f; 
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
         
         SSM.refreshOITTexture = true;
         SSM.refreshMagicLens = true;
      }      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Scroll filter panel
   ////////////////////////////////////////////////////////////////////////////////
   public static void setScrollPanelOffset(PaneAttrib attrib, int posY, int oldPosY) {
      attrib.yOffset -= (posY - oldPosY);   
      if (attrib.yOffset < attrib.height)
         attrib.yOffset = attrib.height;
      if (attrib.yOffset > attrib.textureHeight)
         attrib.yOffset = attrib.textureHeight;   
   }   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check drag movement against GUI elements
   ////////////////////////////////////////////////////////////////////////////////
   public static void checkGUIDrag(int posX, int posY, int oldPosX, int oldPosY) {
      // Check the top level UI elements
      if (SSM.topElement == SSM.ELEMENT_DOCUMENT) {
         SSM.docAnchorX += (posX - oldPosX);   
         SSM.docAnchorY -= (posY - oldPosY);   
      // For default filter   
      } else if (SSM.topElement == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         setScrollPanelOffset(SSM.manufactureAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_MAKE_SCROLL) {
         setScrollPanelOffset(SSM.makeAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_MODEL_SCROLL)  {
         setScrollPanelOffset(SSM.modelAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_YEAR_SCROLL)  {
         setScrollPanelOffset(SSM.yearAttrib, posY, oldPosY);
      // For comparisons   
      } else if (SSM.topElement == SSM.ELEMENT_CMANUFACTURE_SCROLL) {
         setScrollPanelOffset(SSM.c_manufactureAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_CMAKE_SCROLL) {
         setScrollPanelOffset(SSM.c_makeAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_CMODEL_SCROLL)  {
         setScrollPanelOffset(SSM.c_modelAttrib, posY, oldPosY);
      } else if (SSM.topElement == SSM.ELEMENT_CYEAR_SCROLL)  {
         setScrollPanelOffset(SSM.c_yearAttrib, posY, oldPosY);
      // Save and load stuff         
      } else if (SSM.topElement == SSM.ELEMENT_SAVELOAD_SCROLL) {
         SSM.saveLoadYOffset -= (SSM.mouseY - SSM.oldMouseY);   
         if (SSM.saveLoadYOffset < SSM.saveLoadHeight)
            SSM.saveLoadYOffset = SSM.saveLoadHeight;
         if (SSM.saveLoadYOffset > SSM.saveLoadTexHeight)
            SSM.saveLoadYOffset = SSM.saveLoadTexHeight;
      }      
   }
   
   
   public static void dragDocumentPanel(int posX, int posY, int oldPosX, int oldPosY) {
      SSM.docAnchorX += (posX - oldPosX);   
      SSM.docAnchorY -= (posY - oldPosY);   
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Scroll document panel
   ////////////////////////////////////////////////////////////////////////////////
   public static void checkDocumentScroll(int posX, int posY, int unit) {
      if (unit < 0) {
         // Prevent underflow
         if (SSM.yoffset <= SSM.docHeight) return;
         
         if (SSM.yoffset <= SSM.t1Height && SSM.t1Start > 0 ) {
            SSM.t1Start = Math.max(0, SSM.t1Start - SSM.globalFetchSize);
            SSM.t2Start = Math.max(SSM.globalFetchSize, SSM.t2Start - SSM.globalFetchSize);
            SSM.docAction = 1;   
            SSM.dirtyGL = 1;
         } else {
            SSM.yoffset += unit*5;
         }            
      } else {
         if (SSM.yoffset > SSM.t1Height && SSM.t2Height <= 0) return;
         
         // Check to see if we have run off the number allocated for the period
         if (SSM.t2Start + SSM.globalFetchSize > SSM.docMaxSize) {
            if (SSM.yoffset >= SSM.t1Height + SSM.t2Height)
               return;
         }
         
         if (SSM.yoffset - SSM.docHeight > SSM.t1Height) {
            SSM.yoffset -= SSM.t1Height;
            SSM.t1Start += SSM.globalFetchSize;
            SSM.t2Start += SSM.globalFetchSize;
            SSM.docAction = 2;   
            SSM.dirtyGL = 1;
         } else {
            SSM.yoffset += unit*5;
         }            
      }      
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check to see if the mouse cursor is in the area where
   // the text is drawn
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean inDocContext(int posX, int posY) {
      if ( ! SSM.docActive ) return false;
      float mX = posX;
      float mY = SSM.windowHeight - posY;
      if (DCUtil.between( mX, SSM.docAnchorX, SSM.docAnchorX+SSM.docWidth)) {
         if (DCUtil.between( mY, SSM.docAnchorY, SSM.docAnchorY+SSM.docHeight)) {
            return true;   
         }
      }
      return false;
   }  
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Set the top element to id if mouse is clicked over the panel
   ////////////////////////////////////////////////////////////////////////////////
   public static int checkScrollPanels(int posX, int posY, PaneAttrib attrib, int id) {
      float mx = posX;
      float my = SSM.windowHeight - posY;
      
      float anchorX = attrib.anchorX;
      float anchorY = attrib.anchorY;
      
      if (DCUtil.between(mx, anchorX, anchorX+SSM.scrollWidth)) {
         if (attrib.direction == 1) {
            if (DCUtil.between(my, anchorY-20, anchorY+attrib.height)) {
               if (attrib.active) SSM.topElement = id;
               return id;
            }
         } else {
            if (DCUtil.between(my, anchorY-20-attrib.height, anchorY)) {
               if (attrib.active) SSM.topElement = id;
               return id;
            }
         }
      }
      return SSM.ELEMENT_NONE;
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check to see if the point (posX, posY) is in the document panel
   ////////////////////////////////////////////////////////////////////////////////
   public static int checkDocumentPanel(int posX, int posY) {
     // Detecting the document text area
     if (Event.inDocContext(posX, posY)) {
        SSM.topElement = SSM.ELEMENT_DOCUMENT;   
        return SSM.ELEMENT_DOCUMENT;
     }
     float mx = posX;
     float my = SSM.windowHeight - posY;
     float anchorX = SSM.docAnchorX;
     float anchorY = SSM.docAnchorY;
     float docWidth  = SSM.docWidth;
     float docHeight = SSM.docHeight;
     float padding   = SSM.docPadding;         
     // Detecting the document borders
     if (DCUtil.between(mx, anchorX-padding, anchorX) || DCUtil.between(mx, anchorX+docWidth, anchorX+docWidth+padding)) {
        if (DCUtil.between(my, anchorY-padding, anchorY+docHeight+padding)) {
           SSM.topElement = SSM.ELEMENT_DOCUMENT;   
           return SSM.ELEMENT_DOCUMENT;
        }
     }
     if (DCUtil.between(my, anchorY-padding, anchorY) || DCUtil.between(my, anchorY+docHeight, anchorY+docHeight+padding)) {
        if (DCUtil.between(mx, anchorX-padding, anchorX+docWidth+padding)) {
           SSM.topElement = SSM.ELEMENT_DOCUMENT;   
           return SSM.ELEMENT_DOCUMENT;
        }
     }     
     return SSM.ELEMENT_NONE;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check to see if the point (posX, posY) is in any of the magic lens
   ////////////////////////////////////////////////////////////////////////////////
   public static int checkLens(int posX, int posY) {
      float mx = posX;
      float my = posY;
      
      
      for (int i=0; i < SSM.lensList.size(); i++) {
         float x = (float)mx - (float)SSM.lensList.elementAt(i).magicLensX;
         float y = (float)my - (float)SSM.lensList.elementAt(i).magicLensY;
         float r = (float)SSM.lensList.elementAt(i).magicLensRadius;
         float d = (float)Math.sqrt(x*x + y*y);
         
         if (d <= r) {
            SSM.lensList.elementAt(i).magicLensSelected = 1;
            SSM.topElement = SSM.ELEMENT_LENS;
System.out.println("=============================================> Found Lens");            
            return SSM.ELEMENT_LENS;
         }
      }      
      return SSM.ELEMENT_NONE; 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   //
   ////////////////////////////////////////////////////////////////////////////////
   public static int checkSlider(int posX, int posY) {
      float mx = posX;
      float my = SSM.windowHeight - posY;      
      
      float yf_anchorX = SSM.instance().getYearAnchorX();
      float yf_anchorY = SSM.instance().getYearAnchorY();
      if (DCUtil.between(mx, yf_anchorX, yf_anchorX + (CacheManager.instance().timeLineSize/12)*SSM.instance().rangeFilterWidth)) {
         if (DCUtil.between(my, yf_anchorY-15, yf_anchorY+SSM.instance().rangeFilterHeight)) {
            SSM.topElement = SSM.ELEMENT_FILTER;
            return SSM.ELEMENT_FILTER;
         }
      }
      
      float mf_anchorX = SSM.instance().getMonthAnchorX();
      float mf_anchorY = SSM.instance().getMonthAnchorY();
      // Always 12 month
      if (DCUtil.between(mx, mf_anchorX, mf_anchorX + 12*SSM.instance().rangeFilterWidth)) {
         if (DCUtil.between(my, mf_anchorY-15, mf_anchorY+SSM.instance().rangeFilterHeight)) {
            SSM.topElement = SSM.ELEMENT_FILTER;
            return SSM.ELEMENT_FILTER;
         }
      }      
      
      // Check the markers
      
      DCTriple point = new DCTriple(mx, my, 0);
      if (DCUtil.pointInTriangle(point, SSM.yearHigh[0], SSM.yearHigh[1], SSM.yearHigh[2])) return SSM.ELEMENT_FILTER;
      if (DCUtil.pointInTriangle(point, SSM.yearLow[0], SSM.yearLow[1], SSM.yearLow[2])) return SSM.ELEMENT_FILTER;
      if (DCUtil.pointInTriangle(point, SSM.monthHigh[0], SSM.monthHigh[1], SSM.monthHigh[2])) return SSM.ELEMENT_FILTER;
      if (DCUtil.pointInTriangle(point, SSM.monthLow[0], SSM.monthLow[1], SSM.monthLow[2])) return SSM.ELEMENT_FILTER;
      
      
      
      return SSM.ELEMENT_NONE; 
   }
   
   public static boolean isEmptySpace(int sx, int sy) {
      if (Event.checkDocumentPanel(sx, sy) == SSM.ELEMENT_NONE &&
            Event.checkLens(sx, sy) == SSM.ELEMENT_NONE &&
            Event.checkSlider(sx, sy) == SSM.ELEMENT_NONE &&
            Event.checkScrollPanels(sx, sy, SSM.manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_NONE &&
            Event.checkScrollPanels(sx, sy, SSM.makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_NONE &&
            Event.checkScrollPanels(sx, sy, SSM.modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_NONE &&
            Event.checkScrollPanels(sx, sy, SSM.yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_NONE) {      
         return true;
      }
      return false;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Selection semantic with the mouse
   ////////////////////////////////////////////////////////////////////////////////
   public static void handleMouseSelect(Integer obj) {
      
      if (SSM.selectedGroup.size() > 0 ) {
         // If control key is not held down, clear
         if ( ! SSM.shiftKey) {
            SSM.selectedGroup.clear();   
         }
         
         if (SSM.selectedGroup.contains(obj)) {
            SSM.selectedGroup.remove(obj);
         } else {
            SSM.selectedGroup.put(obj, obj);
         }
         
         SSM.dirty = 1;
         SSM.dirtyGL = 1; // for the text panel
         SSM.t1Start = 0;
         SSM.t2Start = SSM.globalFetchSize;
         SSM.yoffset = SSM.docHeight;
         SSM.docMaxSize = 0;
         for (Integer key : SSM.selectedGroup.keySet()) {
            SSM.docMaxSize += CacheManager.instance().groupOccurrence.get( key );
         }
      } else {
         SSM.selectedGroup.put(obj,obj);
         SSM.dirty = 1;
         SSM.dirtyGL = 1; // for the text panel
         SSM.t1Start = 0;
         SSM.t2Start = SSM.globalFetchSize;
         SSM.yoffset = SSM.docHeight;
         SSM.docMaxSize = 0;
         for (Integer key : SSM.selectedGroup.keySet()) {
            SSM.docMaxSize += CacheManager.instance().groupOccurrence.get( key );
         }
     }
      
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Selection semantic with TUIO protocol 
   // Toggle, selections are either on or off
   ////////////////////////////////////////////////////////////////////////////////
   public static void handleTUIOSelect(Integer obj) {
      if (SSM.selectedGroup.contains(obj)) {
         SSM.selectedGroup.remove(obj);
      } else {
         SSM.selectedGroup.put(obj, obj);
      }
      
      SSM.dirty = 1;
      SSM.dirtyGL = 1; // for the text panel
      SSM.t1Start = 0;
      SSM.t2Start = SSM.globalFetchSize;
      SSM.yoffset = SSM.docHeight;
      SSM.docMaxSize = 0;
      for (Integer key : SSM.selectedGroup.keySet()) {
         SSM.docMaxSize += CacheManager.instance().groupOccurrence.get( key );
      }      
   }
   
   
   
}
