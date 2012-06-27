package touch;

import java.util.Hashtable;
import java.util.Vector;

import model.DCTriple;

import util.DCCamera;
import util.DCUtil;

import datastore.SSM;
import exec.Event;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioPoint;
import TUIO.TuioTime;

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
   
   // Variables that may be hardware dependent
   // Some sensors are too sensitive and need to be toned down in order for gestures to work,
   // others have difference screen sizes and have a different metrics of what "near" means
   // ... etc etc
   /*
   public static long  REFRESH_INTERVAL = 800;  // Thread refresh every 800 milliseconds
   public static int   DOWNSAMPLE_RATE = 3;     // Sample every 3 update messages
   public static float NEAR_THRESHOLD = 0.2f;   // "Near" items are <= 0.2 normalized distance 
   */
   
   public static long  REFRESH_INTERVAL = SSM.refreshRate;
   public static int   DOWNSAMPLE_RATE  = SSM.downsampleRate;
   public static float NEAR_THRESHOLD   = SSM.nearThreshold;

   public Hashtable<Long, WCursor> eventTable = new Hashtable<Long, WCursor>();
   public Vector<TapAction> tapActionList = new Vector<TapAction>();
   
   public float sensitivity = 0;
   
   public TUIOListener() {
      super();   
      sensitivity = Float.valueOf(System.getProperty("TUIOSensitivity", "0.002")); 
      System.out.println("TUIO Sensitivity : " + sensitivity);
      System.out.println("Starting timer thread");
      Thread t1 = new Thread(update);
      t1.start();
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
   
   
   public Vector<WCursor> findSimilarCursorPixel(WCursor w, int pixelLow, int pixelHigh) {
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
      synchronized(eventTable) {
         for (WCursor wc : eventTable.values()) {
            if (wc.sessionID == w.sessionID) continue;
            // 2) Remove new touch points that are way too close in terms of time and distance
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 30) {
               if (Math.abs( wc.timestamp-w.timestamp) < 100) {
                  System.err.println("H2 " + eventTable.size());
                  return;   
               }
            }
            
            // 3) Remove new touch points if there are move points in the vicinity
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 500 &&
                dist(wc.x, wc.y, w.x, w.y, width, height) > 20) {
               //if (wc.state == WCursor.STATE_MOVE) {
               if (wc.state == WCursor.STATE_MOVE && wc.element != SSM.ELEMENT_DOCUMENT && wc.element != SSM.ELEMENT_LENS) {
                  System.err.println("H3 " + eventTable.size());
                  return;   
               }
            }
         }
         
         // Register a point for the filter widget dragging
         SSM.dragPoints.put(o.getSessionID(), new DCTriple(posX, posY, 0));
         
         // Register a point for hover effects
         // synchronized(SSM.hoverPoints) { SSM.hoverPoints.put(o.getSessionID(), new DCTriple(posX, posY, 0)); }
         
         eventTable.put(o.getSessionID(), w);
         
         // Register a touch point
         synchronized(SSM.touchPoint) { SSM.touchPoint.put(o.getSessionID(), w); }
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
      
      
      System.err.println("=== Removing TUIO Cursor " + o.getPath().size());
      
      
      SSM.checkDragEvent = true;
      
      
      // Are there too many fingers down ? We only support 2 touch gestures, so if
      // there are too many lets just remove them all
      Vector<WCursor> sim = this.findSimilarCursorPixel(w, 0, 300);
      if (sim.size() > 1) {
         System.out.println("Killing extra touch points");
         for (int i=0; i < sim.size(); i++) {
            eventTable.remove(sim.elementAt(i).sessionID);
         }
         eventTable.remove(o.getSessionID());
         return; 
      }
      
      
      
      // Check to see if we are activating lens or the document panel
      // If there are exactly 2 points currently, and they are sufficiently close to each other, and 
      // they are over the same type of element then create a document panel
      if (w.state == WCursor.STATE_NOTHING && w.element == SSM.ELEMENT_NONE) {
         
         Vector<WCursor> doc = this.findSimilarCursorPixel(w, 0, 60);
         if (doc.size() > 0) {
            if (doc.size() == 1 && doc.elementAt(0).state == WCursor.STATE_NOTHING && SSM.docActive == false) {
               System.out.print("activate document panel");   
               SSM.docActive = true;
               SSM.docAnchorX = w.x * SSM.windowWidth;
               SSM.docAnchorY = SSM.windowHeight - (w.y * SSM.windowHeight);
               SSM.resizePanel = 1;
            } 
            eventTable.remove(o.getSessionID());
            return; 
         }
         Vector<WCursor> len = this.findSimilarCursorPixel(w, 100, 500);
         if (len.size() > 0) {
            if (len.size() == 1 && len.elementAt(0).state == WCursor.STATE_NOTHING) {
               // Adjust the lens coordinate such that the 2 points are on the circumference of the lens
               System.out.print("activate lens");   
               float xx = w.x*SSM.windowWidth;
               float yy = w.y*SSM.windowHeight;
               
               float xx2 = len.elementAt(0).x * SSM.windowWidth;
               float yy2 = len.elementAt(0).y * SSM.windowHeight;
               
               float r = (float)Math.sqrt((xx-xx2)*(xx-xx2)+(yy-yy2)*(yy-yy2))/2.0f;
               float cx = (xx+xx2)/2;
               float cy = (yy+yy2)/2;
               
               //Event.createLens( (int)(w.x*SSM.windowWidth), (int)(w.y*SSM.windowHeight));
               Event.createLens( (int)cx, (int)cy, r);
            } 
            eventTable.remove(o.getSessionID());
            return; 
         }
      }
      
      
      // Check to see if we are de-activating document
      if (w.swipeCount == 0 && w.state != WCursor.STATE_MOVE && w.element == SSM.ELEMENT_DOCUMENT) {
         Vector<WCursor> doc = this.findSimilarCursorPixel(w, 0, 50);
         if (doc.size() == 1) {
            System.out.print("Removing document panel");
            SSM.docActive = false;
            SSM.resizePanel = 1;
            eventTable.remove(o.getSessionID());
            return;
         }
      }
      
      
      
      // Check if this is a swipe event
//      if (w.points.size() > 1 && w.state == WCursor.STATE_SWIPE) {
      if (w.swipeCount > 1) {
         SSM.chartMode ++;
         if (SSM.chartMode > 3) SSM.chartMode = 1;
         
         
//         if (Math.abs(w.x - w.points.elementAt(0).getX()) > Math.abs(w.y - w.points.elementAt(0).getY())) {
//            // Are they all in the same direction (more or less)?   
//            float x1 = w.points.elementAt(0).getX();
//            float x2 = w.points.elementAt(1).getX();
//            float sign1 = x1-x2;
//            float sign2;
//            boolean sameDirection = true;
//            for (int i=2; i < w.points.size(); i++) {
//               x1 = x2;
//               x2 = w.points.elementAt(i).getX();
//               sign2 = x1-x2;
//               if (sign2*sign1 < 0) { 
//                  sameDirection = false; 
//                  break; 
//               }
//            }
//         } else {
//            // Are they all in the same direction (more or less)?   
//            float y1 = w.points.elementAt(0).getY();
//            float y2 = w.points.elementAt(1).getY();
//            float sign1 = y1-y2;
//            float sign2;
//            boolean sameDirection = true;
//            for (int i=2; i < w.points.size(); i++) {
//               y1 = y2;
//               y2 = w.points.elementAt(i).getY();
//               sign2 = y1-y2;
//               if (sign2*sign1 < 0) { 
//                  sameDirection = false; 
//                  break; 
//               }
//            }
//         }
      // Check if this is a tap event (approximate)
      } else if (w.points.size() < 3) {
         // Only clickable elements can send a tap event
         if (w.element == SSM.ELEMENT_NONE || w.element == SSM.ELEMENT_LENS || 
             w.element == SSM.ELEMENT_SCENARIO ||
             w.element == SSM.ELEMENT_MANUFACTURE_SCROLL|| w.element == SSM.ELEMENT_CMANUFACTURE_SCROLL || 
             w.element == SSM.ELEMENT_MAKE_SCROLL || w.element == SSM.ELEMENT_CMAKE_SCROLL ||
             w.element == SSM.ELEMENT_MODEL_SCROLL || w.element == SSM.ELEMENT_CMODEL_SCROLL ||
             w.element == SSM.ELEMENT_YEAR_SCROLL || w.element == SSM.ELEMENT_CYEAR_SCROLL  ||
             w.element == SSM.ELEMENT_PERSPECTIVE_SCROLL) {
            
            // Only set a tap if the touch point is "fresh", that is, the touch points are
            // within certain time limits
            if (o.getTuioTime().getTotalMilliseconds() - w.timestamp < 1000) {
               System.out.println("\tSending out tap event");
               SSM.pickPoints.add(new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
               SSM.l_mouseClicked = true;
            }
         }
         
      }
      eventTable.remove(o.getSessionID());
      
      
      // Programmably reset the lens selected stata based on remaining points
      SSM.clearLens();
      
      
   }



   
   
   @Override
   public void updateTuioCursor(TuioCursor o) {
      // Sanity check, ignore small changes (evoluce table seem to send out crap changes)
      WCursor wcursor = eventTable.get(o.getSessionID());
      if (wcursor == null) return;
      
      float width  = SSM.windowWidth;
      float height = SSM.windowHeight;
      // 1) Remove touch point jitters
      //if ( t.getTuioTime().getTotalMilliseconds() - w.timestamp < 300)  {
      if (dist(wcursor.x, wcursor.y, o.getX(), o.getY(), width, height) < 2) {
         System.err.println("H1 " + eventTable.size());
         return;   
      }
      //}
      
      
      // 4) Reinforce intention to actually move
      if (wcursor.numUpdate < 1 && wcursor.element != SSM.ELEMENT_LENS) {
         if ( o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 600)  {
            if (dist(wcursor.x, wcursor.y, o.getX(), o.getY(), width, height) < 20) {
               System.err.println("H4 " + eventTable.size());
               return;   
            }
         }
      } 
      // 4.1) Lens Update: Reinforce intention to actually move for lens
      if (wcursor.numUpdate < 1 && wcursor.element == SSM.ELEMENT_LENS) {
         if ( o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 400)  {
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
      
      
      System.err.println("=== Updating TUIO Cursor : " + dist(x1, y1, x2, y2, 1, 1));
      wcursor.points.add( o.getPosition() );
      wcursor.numUpdate++;
      
      wcursor.oldX = wcursor.x;
      wcursor.oldY = wcursor.y;
      wcursor.x = o.getX();
      wcursor.y = o.getY();
      
      if (wcursor.state == WCursor.STATE_HOLD) return; 
      
      // Before setting the state to state move, check to see if this might be a
      // swipe event
      if ( dist(x1, y1, x2, y2, 1, 1) > 15 && wcursor.state != WCursor.STATE_MOVE) {
         wcursor.swipeCount ++;
         return;
      } else {
         wcursor.swipeCount = 0;
      }
      
      
      
      wcursor.state = WCursor.STATE_MOVE;
         
      // Register a touch point
      synchronized(SSM.touchPoint) { SSM.touchPoint.put(o.getSessionID(), wcursor);}
      
      
      
      
      // There are 2 other points
      /*
      if (findSimilarCursor(wcursor).size() == 2) {
         Vector<WCursor> simCursor = findSimilarCursor(wcursor);
         TuioPoint point     = wcursor.points.lastElement();
         TuioPoint oldPoint  = wcursor.points.elementAt( wcursor.points.size()-2);         
         
         // They are all LENs
         System.out.println("Detected 3 on same element");
         if (wcursor.element == SSM.ELEMENT_LENS) {
            System.out.println("Changing lens depth");
            float direction = -(point.getY() - oldPoint.getY());
            Event.scrollLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), direction > 0 ? 1 : -1);
            return;
         }
      }
      */
      
      // There is another touch/gesture over the
      // same element
      //if (findSimilarCursor(wcursor) != null) {
      //System.out.println("Similar cursors = " + findSimilarCursorPixel(wcursor, 0, 500));
      Vector<WCursor> simCursor = findSimilarCursorPixel(wcursor, 0, 500);
      if (simCursor.size() == 1) {
System.out.println("Checking 2 finger gestures...");         
         WCursor sim = simCursor.elementAt(0);   
         int simX = (int)(sim.x * width);
         int simY = (int)(sim.y * height);
         
         double oldDistance = dist(simX, simY, x2, y2, 1, 1);
         double newDistance = dist(simX, simY, x1, y1, 1, 1);
         
         double vDir1 = (wcursor.y - wcursor.oldY)*height;
         double vDir2 = (sim.y - sim.oldY)*height;         
         
         // Check if there is a two-finger drag going on,
         // To check for two finger drag, we check that the 
         // two touch points are sufficiently close, are both in move state, and are moving in
         // the same direction
         //if (newDistance <= 150 && wcursor.element == SSM.ELEMENT_DOCUMENT) {               // 1) Sufficiently close
         if ( newDistance <= 150 ) {
System.out.println("Checking 50");
            if (wcursor.state == WCursor.STATE_MOVE && sim.state == WCursor.STATE_MOVE) {  // 2) Same state 
System.out.println("Checking 2 finger drag : " + vDir1 + " " + vDir2);
               if ( vDir1 * vDir2 > 0 ) {                                                  // 3) Same direction of movement ( ++ | -- )
System.out.println("2 Finger Drag");   
                  if (wcursor.element == SSM.ELEMENT_DOCUMENT) {
                     Event.checkDocumentScroll(x1, y1, (y2-y1));
                  } else if (wcursor.element == SSM.ELEMENT_LENS) {
                     int direction = (y2 > y1)? 1 : -1;
                     Event.scrollLens(x1, y1, direction);
                  }
                  return;
               }
            }
         }         
         
         
         if (oldDistance < newDistance) {
System.out.println("Spread detected");    
            if (wcursor.element == SSM.ELEMENT_LENS) {
               Event.resizeLens( x1, y1, (int)(2));
            } else if (wcursor.element == SSM.ELEMENT_NONE) {
               DCCamera.instance().move(1.5f);
            }
         } else if (oldDistance > newDistance) {
System.out.println("Pinch detected");
            if (wcursor.element == SSM.ELEMENT_LENS) {
               Event.resizeLens( x1, y1, (int)(-2));
            } else if (wcursor.element == SSM.ELEMENT_NONE) {
               DCCamera.instance().move(-1.5f);
            }
         }
         

         
         return;
      }
      
      
      /*
      if (findSimilarCursorPixel(wcursor, 0, 500).size() == 1) {
         // Check for pinch and spread events
         WCursor simCursor   = findSimilarCursorPixel(wcursor, 0, 500).elementAt(0);
         TuioPoint point     = wcursor.points.lastElement();
         TuioPoint oldPoint  = wcursor.points.elementAt( wcursor.points.size()-1);
         
         
         float distance = this.distance(simCursor.points.lastElement(), point);
         float oldDistance = this.distance(simCursor.points.lastElement(), oldPoint);
         
         // Check document
         if (wcursor.element == SSM.ELEMENT_DOCUMENT) {
            float direction = -(point.getY() - oldPoint.getY());
            Event.checkDocumentScroll(0, 0, (int)(direction*SSM.windowHeight));
            return;
         }
         
         // Check others
         if (distance > oldDistance) {
            System.out.println("Spread Event...");
            
            if (wcursor.element == SSM.ELEMENT_LENS) {
               //Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(oldPoint.getX()*SSM.windowWidth), (int)(oldPoint.getY()*SSM.windowHeight));
               Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(2));
            } else if (wcursor.element == SSM.ELEMENT_FILTER){     
            } else {
               DCCamera.instance().move(1.5f);
            }
            return;            
         } else {
            System.out.println("Pinch Event...");
            
            if (wcursor.element == SSM.ELEMENT_LENS) {
               //Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(oldPoint.getX()*SSM.windowWidth), (int)(oldPoint.getY()*SSM.windowHeight));
               Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(-2));
            } else if (wcursor.element == SSM.ELEMENT_FILTER){     
            } else {
               DCCamera.instance().move(-1.5f);
            }
            return;
         }
      }
      */

      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Execute any move event
      ////////////////////////////////////////////////////////////////////////////////
      if (wcursor.element == SSM.ELEMENT_NONE){
         Event.setCameraTUIO(x1, y1, x2, y2);
         //Event.setCamera(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_LENS){
         Event.moveLensTUIO(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_DOCUMENT) {
         Event.dragDocumentPanel(x1, y1, x2, y2);
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
   
   
   public Runnable update = new Runnable() {
      public void run() {
         while(true) {
            try {
               synchronized(eventTable) {
                  for (WCursor w : eventTable.values()) {
System.out.println("checking " + Math.abs(w.timestamp - w.cursor.getTuioTime().getTotalMilliseconds()));                        
                     if ( Math.abs(w.timestamp - w.cursor.getTuioTime().getTotalMilliseconds()) > 400 && w.state == WCursor.STATE_NOTHING) {
System.out.println("Changing to state hold " + Math.abs(w.timestamp - w.cursor.getTuioTime().getTotalMilliseconds()));                        
                        w.state = WCursor.STATE_HOLD;   
                        SSM.hoverPoints.put(w.sessionID, new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                     } else if (w.state == WCursor.STATE_HOLD){
                        SSM.hoverPoints.put(w.sessionID, new DCTriple(w.x*SSM.windowWidth, w.y*SSM.windowHeight, 0));
                     }
                  }
               }
               Thread.sleep(20);
            } catch (Exception e) {}
         }
      }
   };
   

   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   public void removeTuioObject(TuioObject arg0) {}
   public void updateTuioObject(TuioObject arg0) {}   
}