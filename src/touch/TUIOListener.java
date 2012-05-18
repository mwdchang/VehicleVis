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
   
//   public static void main(String args[]) {
//      TuioClient client = new TuioClient();
//      client.addTuioListener(new TUIOListener());
//      client.connect();
//      while(true) {}
//   }
   
   
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
   public static float TOO_CLOSE        = 0.008f;
   
   public Hashtable<Long, WCursor> eventTable = new Hashtable<Long, WCursor>();
   public Vector<TapAction> tapActionList = new Vector<TapAction>();
   
   public float sensitivity = 0;
   
   public TUIOListener() {
      super();   
      sensitivity = Float.valueOf(System.getProperty("TUIOSensitivity", "0.002")); 
      System.out.println("TUIO Sensitivity : " + sensitivity);
      System.out.println("Starting timer thread");
      Thread t1 = new Thread(tapCheck);
      t1.start();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Find the distance between two points
   ////////////////////////////////////////////////////////////////////////////////
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
      
      for (WCursor k : eventTable.values()) {
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element) {
            if (distancePixel(w, k) <= pixelHigh && distancePixel(w, k) >= pixelLow) result.add(k);
         }
          
      }
      return result;
   }
   
   
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check deferred events, such as taps and double taps (They are initiated after 
   // the physical action has taken place)
   // Use the interval parameter to tune for hardware differences
   //
   // Evoluce2 : ~800 
   ////////////////////////////////////////////////////////////////////////////////
   Runnable tapCheck = new Runnable() {
      public void run() {
         try {
            while(true) {
               for (int i=0; i < tapActionList.size(); i++) {
                  TapAction ta = tapActionList.elementAt(i);
                  int sx = (int) (ta.x * SSM.windowWidth);   
                  int sy = (int) (ta.y * SSM.windowHeight);   
                  
                  // Send a click event
                  if (ta.numTap == 1) {
                     SSM.pickPoints.add(new DCTriple(sx, sy, 0));
                     SSM.l_mouseClicked = true;
                  }
//                  if (ta.numTap == 2) {
//                     if (Event.isEmptySpace(sx, sy)) {
//                        Event.createLens(sx, sy);
//                     } else if (Event.checkLens(sx, sy) == SSM.ELEMENT_LENS) {
//                        Event.removeLens(sx, sy);   
//                     }
//                  }
                  
                  /*
                  if (ta.numTap == 2) {
                     if (Event.checkDocumentPanel(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkLens(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkSlider(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_NONE) {
                        System.out.println("Trying to create lens");
                        Event.createLens(sx, sy);
                     } else if (Event.checkLens(sx, sy) == SSM.ELEMENT_LENS) {
                        System.out.println("Trying to remove lens");
                        Event.removeLens(sx, sy);   
                     }
                  }
                  */
                  System.err.println("Processing taps " + ta.numTap);
               }
               tapActionList.clear();
               
               Thread.sleep(REFRESH_INTERVAL);
            }
         } catch(Exception e) { e.printStackTrace(); }
      }
   };
   

   
   ////////////////////////////////////////////////////////////////////////////////
   // Register the touch point, find out which element is being
   // touched and add it to the event table
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void addTuioCursor(TuioCursor o) {
      
      int posX = (int)(o.getX()*(float)SSM.windowWidth);
      int posY = (int)(o.getY()*(float)SSM.windowHeight);
      
      //SSM.dragPoints.add(new DCTriple(posX,  posY, 0));
      SSM.dragPoints.put(o.getSessionID(), new DCTriple(posX, posY, 0));
      
      WCursor w;
      if (Event.checkLens(posX, posY) == SSM.ELEMENT_LENS) {
         System.err.println("Add touch to lens");
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
      } else {
         w = new WCursor(SSM.ELEMENT_NONE, o);
      }
      
      
      // Damping...if there are any points that are too close together, 
      // throw them out because they will screw up the touch point calculations
      // Vector<WCursor> nearCursor = this.findSimilarCursor(w, 0, TOO_CLOSE);
      Vector<WCursor> nearCursor = this.findSimilarCursorPixel(w, 0, 30);
      if (nearCursor.size() > 0) {
         System.out.println("Ignoring too close events.....");
         return;
      }
      
      
      System.err.println("=== Adding TUIO Cursor");
      w.sessionID = o.getSessionID();
      w.points.add(o.getPosition());
      eventTable.put(w.sessionID, w);
      
      
      /*
      Vector<WCursor> addList = this.findSimilarCursor(w);
      if (addList.size() > 0) {
         for (int i=0; i < addList.size(); i++) {
            WCursor wPrime = addList.elementAt(i);
            // This can either toggle the lens or the document panel
            if (wPrime.element == w.element) {
               float dist = this.distance(w.cursor.getPosition(), wPrime.cursor.getPosition());
               if (dist >= 0.2f ) {
                  System.out.println("This should activate lens");
               } else if (dist < 0.1f) {
                  System.out.println("This should toggle the document panel");
               }
            }
         }
      } // end if
      */
      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Remove the TUIO cursor from the event table
   // Note for events classified as "Taps" we store them seperately
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void removeTuioCursor(TuioCursor o) {
      WCursor w = eventTable.get(o.getSessionID());
      if (w == null) return;
      
      System.out.println("=== Removing TUIO Cursor " + o.getPath().size());
      
      SSM.dragPoints.remove(o.getSessionID());
      SSM.checkDragEvent = true;
      
      
      // Are there too many fingers down ? We only support 2 touch gestures, so if
      // there are too many lets just remove them all
      Vector<WCursor> sim = this.findSimilarCursorPixel(w, 0, 250);
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
      //if (w.state != WCursor.STATE_SWIPE && w.state != WCursor.STATE_MOVE && w.element == SSM.ELEMENT_NONE) {
      if (w.state == WCursor.STATE_NOTHING && w.element == SSM.ELEMENT_NONE) {
         
         Vector<WCursor> doc = this.findSimilarCursor(w, 0.02f, 0.05f);
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
         Vector<WCursor> len = this.findSimilarCursor(w, 0.06f, 0.25f);
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
      
      // Check to see if we are de-activating the lens
      if (w.state != WCursor.STATE_SWIPE && w.state != WCursor.STATE_MOVE && w.element == SSM.ELEMENT_DOCUMENT) {
         Vector<WCursor> doc = this.findSimilarCursor(w, 0.02f, 0.05f);
         if (doc.size() == 1) {
            System.out.print("Removing document panel");
            SSM.docActive = false;
            SSM.resizePanel = 1;
            eventTable.remove(o.getSessionID());
            return;
         }
      }
      
      /*
      if (w.state != WCursor.STATE_SWIPE && w.state != WCursor.STATE_MOVE && w.element == SSM.ELEMENT_LENS) {
         Vector<WCursor> len = this.findSimilarCursor(w, 0.01f, 0.4f);
         if (len.size() == 1) {
            System.out.print("Removing lens");
            Event.removeLens((int)(w.x*SSM.windowWidth), (int)(w.y*SSM.windowHeight));
         }
         eventTable.remove(o.getSessionID());
         return;
      }
      */
      
      
      
      // Check if this is a swipe event
      if (w.points.size() > 1 && w.state == WCursor.STATE_SWIPE) {
         if (Math.abs(w.x - w.points.elementAt(0).getX()) > Math.abs(w.y - w.points.elementAt(0).getY())) {
            // Are they all in the same direction (more or less)?   
            float x1 = w.points.elementAt(0).getX();
            float x2 = w.points.elementAt(1).getX();
            float sign1 = x1-x2;
            float sign2;
            boolean sameDirection = true;
            for (int i=2; i < w.points.size(); i++) {
               x1 = x2;
               x2 = w.points.elementAt(i).getX();
               sign2 = x1-x2;
               if (sign2*sign1 < 0) { 
                  sameDirection = false; 
                  break; 
               }
            }
            /*
            if (sameDirection == true) {
               System.out.println("Horizontal Swipe detected...");   
               SSM.colouringMethod++;
               SSM.colouringMethod %= 5;
            }
            */
         } else {
            // Are they all in the same direction (more or less)?   
            float y1 = w.points.elementAt(0).getY();
            float y2 = w.points.elementAt(1).getY();
            float sign1 = y1-y2;
            float sign2;
            boolean sameDirection = true;
            for (int i=2; i < w.points.size(); i++) {
               y1 = y2;
               y2 = w.points.elementAt(i).getY();
               sign2 = y1-y2;
               if (sign2*sign1 < 0) { 
                  sameDirection = false; 
                  break; 
               }
            }
            /*
            if (sameDirection == true) {
               System.out.println("Vertical Swipe detected...");   
               SSM.colouringMethod++;
               SSM.colouringMethod %= 5;
            }
            */
         }
      // Check if this is a tap event (approximate)
      } else if (w.points.size() < 2) {
        synchronized(tapActionList) {
           boolean found = false;
            for (int i=0; i < tapActionList.size(); i++) {
              TapAction ta = tapActionList.elementAt(i);   
              if (DCUtil.dist( (w.x-ta.x), (w.y-ta.y)) < 0.05)  {
                 System.out.println("Registering multi-tap");
                 ta.numTap++;   
                 found = true;
                 break;
              }
            }
            if (found == false) {
               System.out.println("Adding TapAction...");
               tapActionList.add(new TapAction(w.x, w.y, w.element));   
            }
         }
      }
      eventTable.remove(o.getSessionID());
   }


   @Override
   public void refresh(TuioTime o) {
   }


   @Override
   public void removeTuioObject(TuioObject o) {
      System.err.println("=== Removing TUIO Object");
      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
   }

   
   
   
   @Override
   public void updateTuioCursor(TuioCursor o) {
      // Sanity check, ignore small changes (evoluce table seem to send out crap changes)
      WCursor wcursor = eventTable.get(o.getSessionID());
      if (wcursor == null) return;
      
      // Wait for the cursor to stablize before allowing update
      if (o.getTuioTime().getTotalMilliseconds() - wcursor.timestamp < 100) {
         System.out.println("Enforcing stablization delay");   
         return;
      }
      
     
      wcursor.numUpdate++;
      
      float ox = o.getX();
      float oy = o.getY();
      SSM.dragPoints.put(o.getSessionID(), new DCTriple(ox*SSM.windowWidth, oy*SSM.windowHeight, 0));
      
      // Avoid jitter
      //if (DCUtil.dist( (ox-wcursor.x), (oy-wcursor.y)) < sensitivity) return;
      int xx1 = (int)(ox*SSM.windowWidth);
      int yy1 = (int)(oy*SSM.windowHeight);
      int xx2 = (int)(wcursor.x*SSM.windowWidth);
      int yy2 = (int)(wcursor.y*SSM.windowHeight);
      
      if (DCUtil.dist( (xx1-xx2), (yy1-yy2)) < 10) {
         //System.out.println("Ignoring update too close");      
         return;
      }
      
      
      // Additional down-sampling
      //if (wcursor.numUpdate % DOWNSAMPLE_RATE != 0) return;
      
      
      
      System.err.println("=== Updating TUIO Cursor");
      wcursor.points.add( o.getPosition() );
      
      //SSM.dragPoints.add(new DCTriple(ox*SSM.windowWidth, ox*SSM.windowHeight, 0));
          
      
      // There are 2 other points
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
      
      // There is another touch/gesture over the
      // same element
      //if (findSimilarCursor(wcursor) != null) {
      if (findSimilarCursor(wcursor).size() == 1) {
         // Check for pinch and spread events
         WCursor simCursor   = findSimilarCursor(wcursor).elementAt(0);
         TuioPoint point     = wcursor.points.lastElement();
         TuioPoint oldPoint  = wcursor.points.elementAt( wcursor.points.size()-2);
         
         
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
            if (wcursor.element == SSM.ELEMENT_LENS) {
               Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(oldPoint.getX()*SSM.windowWidth), (int)(oldPoint.getY()*SSM.windowHeight));
            } else if (wcursor.element == SSM.ELEMENT_FILTER){     
            } else {
               DCCamera.instance().move(1.5f);
            }
            return;            
         } else {
            if (wcursor.element == SSM.ELEMENT_LENS) {
               Event.resizeLens( (int)(point.getX()*SSM.windowWidth), (int)(point.getY()*SSM.windowHeight), (int)(oldPoint.getX()*SSM.windowWidth), (int)(oldPoint.getY()*SSM.windowHeight));
            } else if (wcursor.element == SSM.ELEMENT_FILTER){     
            } else {
               DCCamera.instance().move(-1.5f);
            }
            return;
         }
      }

      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Log possible swipe, just save it, we will process at the remove stage
      // The swipe event will be executed when the cursor is removed
      ////////////////////////////////////////////////////////////////////////////////
      if (DCUtil.dist( (wcursor.x - o.getX()), (wcursor.y - o.getY())) >  0.03 && wcursor.state != WCursor.STATE_MOVE) {
         System.out.println("Possible Swipe");
         wcursor.state = WCursor.STATE_SWIPE;
         wcursor.x = o.getX();
         wcursor.y = o.getY();
         return;   
      }      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Execute any move event
      ////////////////////////////////////////////////////////////////////////////////
      int x1 = (int)(o.getX()*SSM.windowWidth);
      int y1 = (int)(o.getY()*SSM.windowHeight);
      int x2 = (int)(wcursor.x*SSM.windowWidth);
      int y2 = (int)(wcursor.y*SSM.windowHeight);
      if (wcursor.element == SSM.ELEMENT_NONE){
         System.out.println("Processing ELEMENT NONE move");
         wcursor.state = WCursor.STATE_MOVE;
         wcursor.x = o.getX();
         wcursor.y = o.getY();
         Event.setCamera(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_LENS){
         wcursor.state = WCursor.STATE_MOVE;
         wcursor.x = o.getX();
         wcursor.y = o.getY();
         Event.moveLensTUIO(x1, y1, x2, y2);
      } else if (wcursor.element == SSM.ELEMENT_DOCUMENT) {
         wcursor.state = WCursor.STATE_MOVE;
         wcursor.x = o.getX();
         wcursor.y = o.getY();
         Event.dragDocumentPanel(x1, y1, x2, y2);
      }
      
   }
   
   
   @Override
   public void addTuioObject(TuioObject o) {
   }

   @Override
   public void updateTuioObject(TuioObject o) {
   }

}
