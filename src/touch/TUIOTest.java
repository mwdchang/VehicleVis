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
// Immediate on touch events
//    Move, Hold+Move, Pinch, Spread
// Immediate on remove events
//    Swipe
// Deferred events
//    Tap, Double-tap
//
////////////////////////////////////////////////////////////////////////////////
public class TUIOTest implements TuioListener {
   
   public static void main(String args[]) {
      TuioClient client = new TuioClient();
      client.addTuioListener(new TUIOTest());
      client.connect();
      while(true) {}
   }
   
   
   // Variables that may be hardware dependent
   // Some sensors are too sensitive and need to be toned down in order for gestures to work,
   // others have difference screen sizes and have a different metrics of what "near" means
   // ... etc etc
   public static long  REFRESH_INTERVAL = 800;  // Thread refresh every 800 milliseconds
   public static int   DOWNSAMPLE_RATE = 3;     // Sample every 3 update messages
   public static float NEAR_THRESHOLD = 0.2f;   // "Near" items are <= 0.2 normalized distance 
   
   
   public Hashtable<Long, WCursor> eventTable = new Hashtable<Long, WCursor>();
   public Vector<TapAction> tapActionList = new Vector<TapAction>();
   
   public float sensitivity = 0;
   
   public TUIOTest() {
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
   public float distance(TuioPoint p1, TuioPoint p2) {
      return (float)DCUtil.dist((p1.getX() - p2.getX()), (p1.getY()-p2.getY()));
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the first similar cursor (over the same element) that is 
   // not itself
   // TODO: Probably want distance as well
   ////////////////////////////////////////////////////////////////////////////////
   public WCursor findSimilarCursor(WCursor w) {
      for (WCursor k : eventTable.values()) {
         if (k.sessionID == w.sessionID) continue;   
         if (k.element == w.element && distance(k, w) < NEAR_THRESHOLD) return k;
      }
      return null;
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
                     SSM.instance().l_mouseClicked = true;
                  }
                  if (ta.numTap == 2) {
                     if (Event.checkDocumentPanel(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkLens(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkSlider(sx, sy) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.instance().manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.instance().makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.instance().modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_NONE &&
                         Event.checkScrollPanels(sx, sy, SSM.instance().yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_NONE) {
                        System.out.println("Trying to create lens");
                        Event.createLens(sx, sy);
                     } else if (Event.checkLens(sx, sy) == SSM.ELEMENT_LENS) {
                        System.out.println("Trying to remove lens");
                        Event.removeLens(sx, sy);   
                     }
                  }
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
      System.err.println("=== Adding TUIO Cursor");
      
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
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MANUFACTURE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_MAKE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MAKE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_MODEL_SCROLL) {
         w = new WCursor(SSM.ELEMENT_MODEL_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_YEAR_SCROLL) {
         w = new WCursor(SSM.ELEMENT_YEAR_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_manufactureAttrib, SSM.ELEMENT_CMANUFACTURE_SCROLL) == SSM.ELEMENT_CMANUFACTURE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMANUFACTURE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_makeAttrib, SSM.ELEMENT_CMAKE_SCROLL) == SSM.ELEMENT_CMAKE_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMAKE_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_modelAttrib, SSM.ELEMENT_CMODEL_SCROLL) == SSM.ELEMENT_CMODEL_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CMODEL_SCROLL, o);
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_yearAttrib, SSM.ELEMENT_CYEAR_SCROLL) == SSM.ELEMENT_CYEAR_SCROLL) {
         w = new WCursor(SSM.ELEMENT_CYEAR_SCROLL, o);
      } else {
         w = new WCursor(SSM.ELEMENT_NONE, o);
      }
      
      w.sessionID = o.getSessionID();
      w.points.add(o.getPosition());
      eventTable.put(w.sessionID, w);
      
      
      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Remove the TUIO cursor from the event table
   // Note for events classified as "Taps" we store them seperately
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void removeTuioCursor(TuioCursor o) {
      System.out.println("=== Removing TUIO Cursor " + o.getPath().size());
      WCursor w = eventTable.get(o.getSessionID());
      
      SSM.dragPoints.remove(o.getSessionID());
      SSM.instance().checkDragEvent = true;
      
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
            if (sameDirection == true) {
               System.out.println("Horizontal Swipe detected...");   
               SSM.instance().colouringMethod++;
               SSM.instance().colouringMethod %= 5;
            }
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
            if (sameDirection == true) {
               System.out.println("Vertical Swipe detected...");   
               SSM.instance().colouringMethod++;
               SSM.instance().colouringMethod %= 5;
            }
           
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
               tapActionList.add(new TapAction(w.x, w.y));   
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
      wcursor.numUpdate++;
      
      float ox = o.getX();
      float oy = o.getY();
      SSM.dragPoints.put(o.getSessionID(), new DCTriple(ox*SSM.windowWidth, oy*SSM.windowHeight, 0));
      
      // Avoid jitter
      if (DCUtil.dist( (ox-wcursor.x), (oy-wcursor.y)) < sensitivity) return;
      
      // Additional down-sampling
      if (wcursor.numUpdate % DOWNSAMPLE_RATE != 0) return;
      
      System.err.println("=== Updating TUIO Cursor");
      wcursor.points.add( o.getPosition() );
      
      //SSM.dragPoints.add(new DCTriple(ox*SSM.windowWidth, ox*SSM.windowHeight, 0));
          
      
      
      // There is another touch/gesture over the
      // same element
      if (findSimilarCursor(wcursor) != null) {
         // Check for pinch and spread events
         WCursor simCursor = findSimilarCursor(wcursor);
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
