package touch;

import java.util.Hashtable;
import java.util.Vector;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import model.DCTriple;
import model.LensAttrib;

import util.DCCamera;
import util.DCUtil;
import util.DWin;

import datastore.SSM;
import exec.Event;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioPoint;
import TUIO.TuioTime;
import TimingFrameExt.FloatEval;

////////////////////////////////////////////////////////////////////////////////
// This class listens to TUIO events, it assumes that events are sent to the
// default TUIO port (3333).
//
// Immediate on touch events refers to events that are execute as the updates are coming in
// Immediate on remove events refers to events that are execute after the gesture is completed
// Deferred events refers to events that are not natively supported and are buffered by our software
//
////////////////////////////////////////////////////////////////////////////////
public class TUIOListener implements TuioListener {
   
   
   public static long  REFRESH_INTERVAL = SSM.refreshRate;
   public static int   DOWNSAMPLE_RATE  = SSM.downsampleRate;
   public static float NEAR_THRESHOLD   = SSM.nearThreshold;

   public Hashtable<Long, WCursor> eventTable = new Hashtable<Long, WCursor>();
   
   public Hashtable<Long, WCursor> deadzone = new Hashtable<Long, WCursor>();
   
   
   public float sensitivity = 0;
   
   public TUIOListener() {
      super();   
      sensitivity = Float.valueOf(System.getProperty("TUIOSensitivity", "0.002")); 
      System.out.println("TUIO Sensitivity : " + sensitivity);
      System.out.println("Starting timer thread");
      Thread t1 = new Thread(update);
      t1.start();
      //Thread t2 = new Thread(delayTaps);
      //t2.start();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Find the distance between two points
   ////////////////////////////////////////////////////////////////////////////////
   public double dist(double x1, double y1, double x2, double y2, double w, double h) {
      return Math.sqrt((x1-x2)*(x1-x2)*w*w + (y1-y2)*(y1-y2)*h*h);    
   }
   public float distance(WCursor w1, WCursor w2) {
      return (float)DCUtil.dist((w1.x-w2.x), (w1.y-w2.y));
   }
   
   public float distancePixel(WCursor w1, WCursor w2) {
      int x1 = (int)(w1.x * SSM.windowWidth);   
      int x2 = (int)(w2.x * SSM.windowWidth);
      int y1 = (int)(w1.y * SSM.windowHeight);
      int y2 = (int)(w2.y * SSM.windowHeight);
      
      return (float)DCUtil.dist( (x1-x2), (y1-y2));
   }
   
   public float distance(TuioPoint p1, TuioPoint p2) {
      return (float)DCUtil.dist((p1.getX() - p2.getX()), (p1.getY()-p2.getY()));
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the first similar cursor (over the same element) that is 
   // not itself
   // TODO: Probably want distance as well
   ////////////////////////////////////////////////////////////////////////////////
   public Vector<WCursor> findSimilarCursorPixel(WCursor w, int state, int pixelLow, int pixelHigh) {
      Vector<WCursor> result = new Vector<WCursor>();
      for (WCursor k : eventTable.values()) {
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element && k.state == state) {
            if (distancePixel(w, k) <= pixelHigh && distancePixel(w, k) >= pixelLow) result.add(k);
         }
      }
      return result;
   }
   
   public Vector<WCursor> findSimilarCursor(WCursor w) {
      Vector<WCursor> result = new Vector<WCursor>();
      for (WCursor k : eventTable.values()) {
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element && distance(k, w) < NEAR_THRESHOLD) result.add(k);
      }
      return result;
   }
   
   public Vector<WCursor> findSimilarCursor(WCursor w, float low, float high) {
      Vector<WCursor> result = new Vector<WCursor>();
      for (WCursor k : eventTable.values()) {
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element && distance(k, w) <= high && distance(k, w) >= low) result.add(k);
      }
      return result;
   }
   
   
   public Vector<WCursor> findSimilarCursorPixel(WCursor w, int pixelLow, int pixelHigh, boolean includeSelf) {
      Vector<WCursor> result = new Vector<WCursor>();
      
      int c = 0;
      for (WCursor k : eventTable.values()) {
         c++;
         if (k.sessionID == w.sessionID && includeSelf == false) continue;   
         if (k.element == w.element) {
            //System.out.println(k.sessionID + " " + w.sessionID+ " " + distancePixel(k, w));
            if (distancePixel(w, k) <= pixelHigh && distancePixel(w, k) >= pixelLow) result.add(k);
         }
      }
      return result;
   }
   
   public Vector<WCursor> findSimilarCursorPixel(WCursor w, int pixelLow, int pixelHigh) {
      return findSimilarCursorPixel(w, pixelLow, pixelHigh, false);
      /*
      Vector<WCursor> result = new Vector<WCursor>();
      
      int c = 0;
      for (WCursor k : eventTable.values()) {
         c++;
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element) {
            System.out.println(k.sessionID + " " + w.sessionID+ " " + distancePixel(k, w));
            if (distancePixel(w, k) <= pixelHigh && distancePixel(w, k) >= pixelLow) result.add(k);
         }
      }
      return result;
      */
   }
   
   

   
   ////////////////////////////////////////////////////////////////////////////////
   // Register the touch point, find out which element is being
   // touched and add it to the event table
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void addTuioCursor(TuioCursor o) {
      
      int posX = (int)(o.getX()*(float)SSM.windowWidth);
      int posY = (int)(o.getY()*(float)SSM.windowHeight);
      
      
      WCursor w;
      if (Event.checkLens(posX, posY) == SSM.ELEMENT_LENS) {
         w = new WCursor(SSM.ELEMENT_LENS, o);
      } else if (Event.checkLens(posX, posY) == SSM.ELEMENT_LENS_RIM) {    
         w = new WCursor(SSM.ELEMENT_LENS_RIM, o);
      } else if (Event.checkLensHandle(posX, posY) == SSM.ELEMENT_LENS_HANDLE) {
         w = new WCursor(SSM.ELEMENT_LENS_HANDLE, o);
      } else if (Event.checkDocumentPanel(posX, posY) != SSM.ELEMENT_NONE) {
         w = new WCursor(SSM.ELEMENT_DOCUMENT, o);
      } else if (Event.checkSlider(posX, posY) != SSM.ELEMENT_NONE) {
         w = new WCursor(SSM.ELEMENT_FILTER, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MANUFACTURE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_MAKE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MAKE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_MODEL_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MODEL_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_YEAR_SCROLL) {
         w = new WCursor(SSM.ELEMENT_YEAR_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.c_manufactureAttrib, SSM.ELEMENT_CMANUFACTURE_SCROLL) == SSM.ELEMENT_CMANUFACTURE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMANUFACTURE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.c_makeAttrib, SSM.ELEMENT_CMAKE_SCROLL) == SSM.ELEMENT_CMAKE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMAKE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.c_modelAttrib, SSM.ELEMENT_CMODEL_SCROLL) == SSM.ELEMENT_CMODEL_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMODEL_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.c_yearAttrib, SSM.ELEMENT_CYEAR_SCROLL) == SSM.ELEMENT_CYEAR_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CYEAR_SCROLL, o);
      } else if (Event.checkScenario(posX, posY) == SSM.ELEMENT_SCENARIO) {
         w = new WCursor(SSM.ELEMENT_SCENARIO, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.perspectiveAttrib, SSM.ELEMENT_PERSPECTIVE_SCROLL) == SSM.ELEMENT_PERSPECTIVE_SCROLL) {    
         w = new WCursor(SSM.ELEMENT_PERSPECTIVE_SCROLL, o);
      } else {
         w = new WCursor(SSM.ELEMENT_NONE, o);
      }
      
      
      
      float width =  SSM.windowWidth;
      float height = SSM.windowHeight;
      
      if (findSimilarCursorPixel(w, 0, 300).size() >= 2) return;
      
      
      synchronized(eventTable) {
         for (WCursor wc : eventTable.values()) {
            if (wc.sessionID == w.sessionID) continue;
            
            // 2) Remove new touch points that are way too close in terms of time and distance
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 30) {
               if (Math.abs( wc.timestamp-w.timestamp) < 100) {
                  if (wc.element != SSM.ELEMENT_DOCUMENT && wc.element != SSM.ELEMENT_LENS && wc.element != SSM.ELEMENT_LENS_RIM) {
                     System.err.println("H2 " + eventTable.size());
                     return;   
                  }
               }
            }
            
            
            // 3) Remove new touch points if there are move points in the vicinity
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 500 &&
                dist(wc.x, wc.y, w.x, w.y, width, height) > 20) {
               if (wc.state == WCursor.STATE_MOVE && wc.element != SSM.ELEMENT_DOCUMENT && wc.element != SSM.ELEMENT_LENS && wc.element != SSM.ELEMENT_LENS_RIM) {
                  System.err.println("H3 " + eventTable.size());
                  return;   
               }
            }
            
            // Clear a zone around the lens to prevent accidental triggers
            if (wc.element == SSM.ELEMENT_LENS || wc.element == SSM.ELEMENT_LENS_RIM) {
               LensAttrib la = wc.lensReference;       
               if (la != null) {
                  float dist = (float)Math.sqrt((posX - la.magicLensX)*(posX - la.magicLensX)  + (posY - la.magicLensY)*(posY - la.magicLensY));       
                  if (dist < la.magicLensRadius+80) {
                     System.err.println("H3 Lens " + eventTable.size());    
                     return;
                  }
               }
            }
            
            // Clear a zone around the document panel to prevent accidental triggers
            if (wc.element == SSM.ELEMENT_DOCUMENT) {
               if (DCUtil.between(posX, SSM.docAnchorX-40, SSM.docAnchorX+SSM.docWidth+40)) {
                  if (DCUtil.between((SSM.windowHeight-posY), SSM.docAnchorY-40, SSM.docAnchorY+SSM.docHeight+40)) {
                     System.err.println("H3 Document " + eventTable.size());
                     return;
                  }
               }
            }
            
            
         }
            
         // 3.1) Remove new touch points if it overflows the Zone maximum
         // There should be a maximum of two touch points for document zone and
         // the lens zone
         if (w.element == SSM.ELEMENT_LENS || w.element == SSM.ELEMENT_LENS_RIM) {
            if (this.findSimilarCursorPixel(w, 0, 999).size() >= 1) {
               System.err.println("H3.1 " + eventTable.size());
               return;
            }
         } else if (w.element == SSM.ELEMENT_DOCUMENT) {
            if (this.findSimilarCursorPixel(w, 0, 999).size() >= 1) {
               System.err.println("H3.1 " + eventTable.size());
               return;
            }
         } else if (w.element == SSM.ELEMENT_NONE) {
            if (this.findSimilarCursorPixel(w, 0, 500).size() >= 2) {
               System.err.println("H3.1 " + eventTable.size());
               return;
            }
         }
         
         
         // Check if a deadzone exist for the wcursor, a deadzone is an
         // area that has just been deactivated (remove cursor) in a 
         // non moveable zone...we restrict this so the action does not
         // try to repeat itself
         if (w.element != SSM.ELEMENT_LENS && w.element != SSM.ELEMENT_LENS_RIM && w.element != SSM.ELEMENT_DOCUMENT && w.state != WCursor.STATE_MOVE) {
            for (WCursor deadCursor : deadzone.values()) {
               System.err.println( w.x*width + " " + w.y*height + " | " + deadCursor.x*width + " " + deadCursor.y*height);
               if ( this.dist(deadCursor.x*width, deadCursor.y*height, w.x*width, w.y*height, 1, 1) < 200) {
                  System.err.println("HX.1 ");   
                  return;
               }
            }
         }
         
         
         // If the type is lens, register which lens
         if (w.element == SSM.ELEMENT_LENS || w.element == SSM.ELEMENT_LENS_RIM) {
            for (int i=0; i < SSM.lensList.size(); i++) {
               float x = (float)w.x*width - (float)SSM.lensList.elementAt(i).magicLensX;
               float y = (float)w.y*height - (float)SSM.lensList.elementAt(i).magicLensY;
               float r = (float)SSM.lensList.elementAt(i).magicLensRadius;
               float d = (float)Math.sqrt(x*x + y*y);
               
               if (d <= (r+LensAttrib.errorRange)) {
                  SSM.lensList.elementAt(i).magicLensSelected = 1;
                  SSM.topElement = SSM.ELEMENT_LENS;
                  w.lensReference = SSM.lensList.elementAt(i);
                  w.lensIndex = i;
                  w.lensReference.offsetX = (int)x;
                  w.lensReference.offsetY = (int)y;
                  break;
               }
            }              
         }
         
         
         
         // Register a point for the filter widget dragging
         SSM.dragPoints.put(o.getSessionID(), new DCTriple(posX, posY, 0));
         
         // Register a point for hover effects
         // synchronized(SSM.hoverPoints) { SSM.hoverPoints.put(o.getSessionID(), new DCTriple(posX, posY, 0)); }
         System.err.println("=== Adding TUIO Cursor [" + o.getSessionID() + "," + w.element+"] " + w.x + " " + w.y);
         eventTable.put(o.getSessionID(), w);
         
         // Register a touch point
         synchronized(SSM.touchPoint) { SSM.touchPoint.put(o.getSessionID(), w); }
      }      
      
      
   }
   
   
   

   
   
   @Override
   public void updateTuioCursor(TuioCursor o) {
      // Sanity check, ignore small changes (evoluce table seem to send out crap changes)
      WCursor wcursor = eventTable.get(o.getSessionID());
      if (wcursor == null) return;
      
      
      float width  = SSM.windowWidth;
      float height = SSM.windowHeight;
      // 1) Remove touch point jitters
      if (dist(wcursor.x, wcursor.y, o.getX(), o.getY(), width, height) < 1.0 && wcursor.element != SSM.ELEMENT_LENS_RIM && wcursor.element != SSM.ELEMENT_LENS_HANDLE && wcursor.element != SSM.ELEMENT_DOCUMENT) {
         System.err.println("H1 " + eventTable.size());
         return;   
      }
      if (dist(wcursor.x, wcursor.y, o.getX(), o.getY(), width, height) < 4.0 && wcursor.element == SSM.ELEMENT_DOCUMENT) {
         System.err.println("H1 " + eventTable.size());
         return;   
      }
      
      
      // 4) Reinforce intention to actually move
      if (wcursor.numUpdate < 1 && wcursor.element != SSM.ELEMENT_LENS && wcursor.element != SSM.ELEMENT_LENS_RIM && wcursor.element != SSM.ELEMENT_LENS_HANDLE && wcursor.element != SSM.ELEMENT_DOCUMENT) {
         //if ( o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 350)  {
            if (dist(wcursor.x, wcursor.y, o.getX(), o.getY(), width, height) < 20) {
               System.err.println("H4 " + eventTable.size());
               return;   
            }
         //}
      } 
      // 4.1) Lens Update: Reinforce intention to actually move for lens
      if (wcursor.numUpdate < 1 && (wcursor.element == SSM.ELEMENT_LENS || wcursor.element == SSM.ELEMENT_LENS_RIM)) {
         if ( o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 200)  {
            System.err.println("H4.1 " + eventTable.size());
            return;
         }
      }
      
      // 4.2) Document Update: check if intention is to scroll the document, or move the panel
      /*
      if (wcursor.numUpdate < 1 && wcursor.element == SSM.ELEMENT_DOCUMENT) {
         if ( o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 200)  {
            wcursor.intention = WCursor.SCROLL_ELEMENT;
         } else {
            wcursor.intention = WCursor.MOVE_ELEMENTu
         }
      }
      */
      
      
      float ox = o.getX();
      float oy = o.getY();
      SSM.dragPoints.put(o.getSessionID(), new DCTriple(ox*SSM.windowWidth, oy*SSM.windowHeight, 0));
      
      int x1 = (int)(o.getX()*SSM.windowWidth);
      int y1 = (int)(o.getY()*SSM.windowHeight);
      int x2 = (int)(wcursor.x*SSM.windowWidth);
      int y2 = (int)(wcursor.y*SSM.windowHeight);
      
      
      System.err.println("=== Updating TUIO Cursor : [" + o.getSessionID() + "] ");
      wcursor.points.add( o.getPosition() );
      wcursor.numUpdate++;
      wcursor.updTimestamp = System.currentTimeMillis();
      
      wcursor.oldX = wcursor.x;
      wcursor.oldY = wcursor.y;
      wcursor.x = o.getX();
      wcursor.y = o.getY();
      
      if (wcursor.state == WCursor.STATE_HOLD) return; 
      
      // Before setting the state to state move, check to see if this might be a
      // swipe event
      if ( dist(x1, y1, x2, y2, 1, 1) > 11 && wcursor.state != WCursor.STATE_MOVE && o.getTuioTime().getTotalMilliseconds() - wcursor.updTimestamp < 80) {
         wcursor.swipeCount ++;
         wcursor.state = WCursor.STATE_SWIPE;
         
         // Check the approximate direction of the swipe
         if ( Math.abs(x2-x1) - Math.abs(y2-y1) > 0) {  
            // Horizontal   
            if (x1 - x2 > 0) wcursor.swipeDirection = WCursor.LEFT;
            else wcursor.swipeDirection = WCursor.RIGHT;
         } else {
            // Vertical   
            if (y1 - y2 > 0) wcursor.swipeDirection = WCursor.DOWN;
            else wcursor.swipeDirection = WCursor.UP;
         }
         
         return;
      } else {
         wcursor.swipeCount = 0;
         wcursor.state = WCursor.STATE_MOVE;
      }
      
      wcursor.updTimestamp = o.getTuioTime().getTotalMilliseconds();
      
      
      
         
      // Register a touch point
      synchronized(SSM.touchPoint) { SSM.touchPoint.put(o.getSessionID(), wcursor);}
      
      
      
      
      // There is another touch/gesture over the
      // same element
      //if (findSimilarCursor(wcursor) != null) {
      //System.out.println("Similar cursors = " + findSimilarCursorPixel(wcursor, 0, 500));
      Vector<WCursor> simCursor = findSimilarCursorPixel(wcursor, 0, 500);
      if (simCursor.size() == 1) {
         WCursor sim = simCursor.elementAt(0);   
         int simX = (int)(sim.x * width);
         int simY = (int)(sim.y * height);
         
         double oldDistance = dist(simX, simY, x2, y2, 1, 1);
         double newDistance = dist(simX, simY, x1, y1, 1, 1);
         
         if (oldDistance < newDistance) {
System.out.println("Spread detected");    
            if (wcursor.element == SSM.ELEMENT_NONE) {
               double dist = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
               if (dist < 15) return;
               DCCamera.instance().move(1.1f);
            }
         } else if (oldDistance > newDistance) {
System.out.println("Pinch detected");
            if (wcursor.element == SSM.ELEMENT_NONE) {
               double dist = DCCamera.instance().eye.sub(new DCTriple(0,0,0)).mag();
               if (dist > 90) return;
               DCCamera.instance().move(-1.1f);
            }
         }
         
         return;
      }
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Execute any move event
      ////////////////////////////////////////////////////////////////////////////////
      if (wcursor.numUpdate < 2) return; // Lag the update a bit to further distinguish swipe and move events
      
      if (wcursor.element == SSM.ELEMENT_NONE){
         Event.setCameraTUIO(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_LENS || wcursor.element == SSM.ELEMENT_LENS_RIM){
         Event.moveLensTUIO(x1, y1, x2, y2, wcursor);
         if (wcursor.lensReference == null) {
            eventTable.remove(wcursor.sessionID);
            DWin.instance().debug("Removed a lens");   
         }
      } else if (wcursor.element == SSM.ELEMENT_LENS_HANDLE) {
         Event.moveLensHandle(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_DOCUMENT) {
         Event.checkDocumentScrollTUIO(x1, y1, (y2-y1));
         Event.dragDocumentPanelTUIO(x1, y1, x2, y2);
         /*
         if (wcursor.intention == WCursor.MOVE_ELEMENT)
            Event.dragDocumentPanel(x1, y1, x2, y2);
         else if (wcursor.intention == WCursor.SCROLL_ELEMENT)
            Event.checkDocumentScroll(x1, y1, (y2-y1));
         */
      } else if (wcursor.element == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_CMANUFACTURE_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_MAKE_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_CMAKE_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_MODEL_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_CMODEL_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_YEAR_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_YEAR_SCROLL) {
         Event.checkGUIDrag(x1, y1, x2, y2);
      }
   }
   
      
   ////////////////////////////////////////////////////////////////////////////////
   // Remove the TUIO cursor from the event table
   // Note for events classified as "Taps" we store them seperately
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void removeTuioCursor(TuioCursor o) {
      SSM.dragPoints.remove(o.getSessionID());
      
      // Remove a touch point - since removal of nothing is nothing, we will just
      // remove at the top for simplicity sake rather than removal at every single 
      // conditions that we have
      synchronized(SSM.touchPoint)  { SSM.touchPoint.remove(o.getSessionID()); }
      synchronized(SSM.hoverPoints) { SSM.hoverPoints.remove(o.getSessionID()); }
      synchronized(SSM.tooltips) { SSM.tooltips.remove(o.getSessionID()); }
      
      WCursor w = eventTable.get(o.getSessionID());
      if (w == null) return;
      
      
      System.err.println("=== Removing TUIO Cursor [" + o.getSessionID() + "] " + w.points.size());
      
      SSM.checkDragEvent = true;
      
      // Create a dead zone
      w.endTimestamp = System.currentTimeMillis();
      //if ( w.element != SSM.ELEMENT_DOCUMENT && w.element != SSM.ELEMENT_LENS) {
         deadzone.put(w.sessionID, w);   
      //}
      
      
      // Are there too many fingers down ? We only support 2 touch gestures, so if
      // there are too many lets just remove them all
//      Vector<WCursor> sim = this.findSimilarCursorPixel(w, 0, 300);
//      if (sim.size() > 1) {
//         System.out.println("Killing extra touch points");
//         for (int i=0; i < sim.size(); i++) {
//            eventTable.remove(sim.elementAt(i).sessionID);
//         }
//         eventTable.remove(o.getSessionID());
//         return; 
//      }
      
      
      
      
      // Check to see if we are activating lens or the document panel
      // If there are exactly 2 points currently, and they are sufficiently close to each other, and 
      // they are over the same type of element then create a document panel
      //if (w.state == WCursor.STATE_NOTHING && w.element == SSM.ELEMENT_NONE) {
      if ( (w.state == WCursor.STATE_HOLD) && w.element == SSM.ELEMENT_NONE) {
         Vector<WCursor> len = this.findSimilarCursorPixel(w, 100, 500);
         if (len.size() == 1) {
            if (len.size() == 1 && (len.elementAt(0).state == WCursor.STATE_HOLD)) {
               // Adjust the lens coordinate such that the 2 points are on the circumference of the lens
               System.out.print("activate lens");   
               float xx = w.x*SSM.windowWidth;
               float yy = w.y*SSM.windowHeight;
               
               float xx2 = len.elementAt(0).x * SSM.windowWidth;
               float yy2 = len.elementAt(0).y * SSM.windowHeight;
               
               float r = (float)Math.sqrt((xx-xx2)*(xx-xx2)+(yy-yy2)*(yy-yy2))/2.0f;
               float cx = (xx+xx2)/2;
               float cy = (yy+yy2)/2;
               
               Event.createLens( (int)cx, (int)cy, r);
            } 
            eventTable.remove(o.getSessionID());
            
            // also remove the other one to avoid triggering a tap event
            eventTable.remove(len.elementAt(0).sessionID);
            return; 
         }
      }
      
      

      
      
      
      // Check if this is a swipe event
      if (w.swipeCount > 1) {
         if (w.swipeDirection == WCursor.DOWN || w.swipeDirection == WCursor.UP) {
            SSM.selectedGroup.clear();
            SSM.dirty = 1;
            SSM.dirtyGL = 1;
         } else {
            if (SSM.hidePanel == true) Event.showPanel();
            else Event.hidePanel();
         }
      } else if (w.points.size() < 4 && findSimilarCursorPixel(w, 0, 400).size() == 0) {
         // Only clickable elements can send a tap event
         if (w.element == SSM.ELEMENT_NONE || w.element == SSM.ELEMENT_LENS || w.element == SSM.ELEMENT_LENS_RIM ||
             w.element == SSM.ELEMENT_DOCUMENT ||
             w.element == SSM.ELEMENT_SCENARIO ||
             w.element == SSM.ELEMENT_FILTER ||
             w.element == SSM.ELEMENT_MANUFACTURE_SCROLL|| w.element == SSM.ELEMENT_CMANUFACTURE_SCROLL || 
             w.element == SSM.ELEMENT_MAKE_SCROLL || w.element == SSM.ELEMENT_CMAKE_SCROLL ||
             w.element == SSM.ELEMENT_MODEL_SCROLL || w.element == SSM.ELEMENT_CMODEL_SCROLL ||
             w.element == SSM.ELEMENT_YEAR_SCROLL || w.element == SSM.ELEMENT_CYEAR_SCROLL  ||
             w.element == SSM.ELEMENT_PERSPECTIVE_SCROLL) {
            
            
            // Only set a tap if the touch point is "fresh", that is, the touch points are
            // within certain time limits
            //if (o.getTuioTime().getTotalMilliseconds() - w.timestamp < 1000) {
               System.out.println("\tSending out tap event");
               
               if (w.endTimestamp - w.startTimestamp > SSM.HOLD_DELAY) {
                  if (w.element == SSM.ELEMENT_DOCUMENT && SSM.docActive == true) {
                     SSM.docActive = false;
                     SSM.resizePanel = 1;
                  } else {
                     // massive hack, a negative number in the z denotes it is a document picking
                     SSM.pickPoints.add(new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, -1));
                     SSM.l_mouseClicked = true;
                  }
               } else {
                  SSM.pickPoints.add(new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                  SSM.l_mouseClicked = true;
               }
               
            //}
            
         }
         
      }
      
      
      eventTable.remove(o.getSessionID());
      
      
      // Programmably reset the lens selected stata based on remaining points
      SSM.clearLens();
      
      
   }



  public Vector<WCursor> tapPoints = new Vector<WCursor>();
  public Runnable delayTaps = new Runnable() {
     public void run() {
        while(true) {
           try {
              synchronized( tapPoints ) {
                 for (int i=0; i < tapPoints.size(); i++) {
                    WCursor w = tapPoints.elementAt(i);    
                    if (System.currentTimeMillis() - w.endTimestamp > 250) {
                       if (w.tapCount == 1) {
                          if (w.element == SSM.ELEMENT_DOCUMENT && SSM.docActive == true) {
                             SSM.docActive = false;
                             SSM.resizePanel = 1;
                          } else {
                             SSM.pickPoints.add(new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                             SSM.l_mouseClicked = true;
                          }     
                       } else {
                          SSM.selectedGroup.clear();
                          SSM.dirty = 1;
                          SSM.dirtyGL = 1;
                       }
                       tapPoints.remove(i);
                       break;
                    }
                 } // end for
              } 
              
              Thread.sleep(100);
           } catch (Exception e) {}
        } // end while
     }
  };
   
   
   public Runnable update = new Runnable() {
      public void run() {
         while(true) {
            try {
               synchronized(eventTable) {
                  for (WCursor w : eventTable.values()) {
                     if ( Math.abs(w.timestamp - w.cursor.getTuioTime().getTotalMilliseconds()) > SSM.HOLD_DELAY && w.state == WCursor.STATE_NOTHING) {
                        w.state = WCursor.STATE_HOLD;   
                        SSM.hoverPoints.put(w.sessionID, new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                     } else if (w.state == WCursor.STATE_HOLD){
                        SSM.hoverPoints.put(w.sessionID, new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                     }
                  }
               }
               Thread.sleep(30);
               
               // Clearn up deadzones
               for (WCursor w : deadzone.values()) {
                  if (System.currentTimeMillis() - w.endTimestamp >= 600) {
                     System.out.println("Cleaning up deadzones");
                     deadzone.remove(w.sessionID);   
                  }
               }
            } catch (Exception e) {}
         }
      }
   };
   

   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   public void removeTuioObject(TuioObject arg0) {}
   public void updateTuioObject(TuioObject arg0) {}   
}