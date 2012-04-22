package touch;

import java.util.Hashtable;
import java.util.Vector;

import util.DCCamera;
import util.DCUtil;

import datastore.SSM;
import exec.Event;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;

////////////////////////////////////////////////////////////////////////////////
// This class listens to TUIO events, it assumes that events are sent to the
// default TUIO port (3333).
//
////////////////////////////////////////////////////////////////////////////////
public class TUIOTest implements TuioListener {
   
   public static void main(String args[]) {
      TuioClient client = new TuioClient();
      client.addTuioListener(new TUIOTest());
      client.connect();
      while(true) {}
   }
   
   public static long EVENT_TAP  = 500;
   public static long EVENT_RING = 1000; 
   
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
   
   // Find how many "points" are activated over the element
   public int getElementPoint(int element) {
      int cnt = 0;
      for (WCursor w : eventTable.values()) {
         if (w.element == element) cnt++;   
      }
      return cnt;
   }
   
   
   
   Runnable tapCheck = new Runnable() {
      long interval = 800;
      public void run() {
         try {
            while(true) {
               for (int i=0; i < tapActionList.size(); i++) {
                  TapAction ta = tapActionList.elementAt(i);
                  int sx = (int) (ta.x * SSM.windowWidth);   
                  int sy = (int) (ta.y * SSM.windowHeight);   
                  
                  // Send a click event
                  if (ta.numTap == 1) {
                     SSM.mouseX = sx;
                     SSM.mouseY = sy;
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
               
               Thread.sleep(interval);
            }
         } catch(Exception e) { e.printStackTrace(); }
      }
   };
   

   
   ////////////////////////////////////////////////////////////////////////////////
   // Register the touch point, find out which ement is being
   // touched and add it to the event table
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void addTuioCursor(TuioCursor o) {
      System.err.println("=== Adding TUIO Cursor");
      
      int posX = (int)(o.getX()*(float)SSM.windowWidth);
      int posY = (int)(o.getY()*(float)SSM.windowHeight);
      
      if (Event.checkLens(posX, posY) == SSM.ELEMENT_LENS) {
         System.err.println("Add touch to lens");
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_LENS, o));
      } else if (Event.checkDocumentPanel(posX, posY) != SSM.ELEMENT_NONE) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_DOCUMENT, o));
      } else if (Event.checkSlider(posX, posY) != SSM.ELEMENT_NONE) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_FILTER, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().manufactureAttrib, SSM.ELEMENT_MANUFACTURE_SCROLL) == SSM.ELEMENT_MANUFACTURE_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_MANUFACTURE_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().makeAttrib, SSM.ELEMENT_MAKE_SCROLL) == SSM.ELEMENT_MAKE_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_MAKE_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().modelAttrib, SSM.ELEMENT_MODEL_SCROLL) == SSM.ELEMENT_MODEL_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_MODEL_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().yearAttrib, SSM.ELEMENT_YEAR_SCROLL) == SSM.ELEMENT_YEAR_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_YEAR_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_manufactureAttrib, SSM.ELEMENT_CMANUFACTURE_SCROLL) == SSM.ELEMENT_CMANUFACTURE_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_CMANUFACTURE_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_makeAttrib, SSM.ELEMENT_CMAKE_SCROLL) == SSM.ELEMENT_CMAKE_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_CMAKE_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_modelAttrib, SSM.ELEMENT_CMODEL_SCROLL) == SSM.ELEMENT_CMODEL_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_CMODEL_SCROLL, o));
      } else if (Event.checkScrollPanels(posX, posY, SSM.instance().c_yearAttrib, SSM.ELEMENT_CYEAR_SCROLL) == SSM.ELEMENT_CYEAR_SCROLL) {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_CYEAR_SCROLL, o));
      } else {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_NONE,  o));
      }
   }
   
   
   @Override
   public void removeTuioCursor(TuioCursor o) {
      System.err.println("=== Removing TUIO Cursor");
      WCursor w = eventTable.get(o.getSessionID());
      
      // Check if this is a swipe event
      if (w.cursor.getPath().size() > 1 && w.state == WCursor.STATE_SWIPE) {
         // Are they all in the same direction (more or less)?   
         float x1 = w.cursor.getPath().elementAt(0).getX();
         float x2 = w.cursor.getPath().elementAt(1).getX();
         float sign1 = x1-x2;
         float sign2;
         boolean sameDirection = true;
         
         for (int i=2; i < w.cursor.getPath().size(); i++) {
            x1 = x2;
            x2 = w.cursor.getPath().elementAt(i).getX();
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
      // Check if this is a tap event (approximate)
      } else if (w.cursor.getPath().size() < 2) {
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
      
      float ox = eventTable.get(o.getSessionID()).x;
      float oy = eventTable.get(o.getSessionID()).y;
      
      // Avoid jitter
      if (DCUtil.dist( (ox-o.getX()), (oy-o.getY())) < sensitivity) return;
      
      // Additional down-sampling
      if (wcursor.numUpdate % 3 != 0) return;
      
      
      System.err.println("=== Updating TUIO Cursor");
      
      

      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Check for multi touch (pinch/unpinch) events
      ////////////////////////////////////////////////////////////////////////////////
      if (eventTable.containsKey(o.getSessionID()) && eventTable.size() > 1) {
         
         // Check for multi touch
         float oldX = o.getPath().elementAt(o.getPath().size()-2).getX();
         float oldY = o.getPath().elementAt(o.getPath().size()-2).getY();
         
         float x = o.getX();
         float y = o.getY();
         
         int element = eventTable.get(o.getSessionID()).element;
         
         
         for (WCursor wcursor2 : eventTable.values()) {
            TuioCursor cursor = wcursor2.cursor;
            if (cursor.getSessionID() == o.getSessionID()) continue;
            float px = cursor.getX();
            float py = cursor.getY();
            
            double oldDistance = Math.sqrt( (oldX-px)*(oldX-px) + (oldY-py)*(oldY-py));
            double distance = Math.sqrt( (x-px)*(x-px) + (y-py)*(y-py));
            
            if (distance > oldDistance && element == wcursor2.element) {
               //System.out.println("possible pinch event");
               System.out.println("Moving away from " + cursor.getSessionID());   
               DCCamera.instance().move(1.5f);
               return;
            } else if (distance < oldDistance && element == wcursor2.element) {
               //System.out.println("possible pinch event");
               System.out.println("Moving closer from " + cursor.getSessionID());   
               DCCamera.instance().move(-1.5f);
               return;
            }
         }
      }
      
      ////////////////////////////////////////////////////////////////////////////////
      // Log possible swipe, just save it, we will process at the remove stage
      // The swipe event will be executed when the cursor is removed
      ////////////////////////////////////////////////////////////////////////////////
      if (DCUtil.dist( (wcursor.x - o.getX()), (wcursor.y - o.getY())) >  0.02 && wcursor.state != WCursor.STATE_MOVE) {
         System.out.println("Possible Swipe");
         WCursor newCursor = new WCursor(wcursor.element, o);
         newCursor.state = WCursor.STATE_SWIPE;
         eventTable.put(o.getSessionID(), newCursor);
         return;   
      }      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Execute any move event
      ////////////////////////////////////////////////////////////////////////////////
      if (wcursor.element == SSM.ELEMENT_NONE){
         System.out.println("Processing ELEMENT NONE move");
         WCursor newCursor = new WCursor(wcursor.element, o);
         newCursor.state = WCursor.STATE_MOVE;
         eventTable.put(o.getSessionID(), newCursor);
         Event.setCamera(o.getScreenX(SSM.windowWidth), o.getScreenY(SSM.windowHeight), (int)(ox*SSM.windowWidth), (int)(oy*SSM.windowHeight));     
      } else if (wcursor.element == SSM.ELEMENT_LENS){
         WCursor newCursor = new WCursor(wcursor.element, o);
         newCursor.state = WCursor.STATE_MOVE;
         eventTable.put(o.getSessionID(), newCursor);
         Event.moveLensTUIO(o.getScreenX(SSM.windowWidth), o.getScreenY(SSM.windowHeight), (int)(ox*SSM.windowWidth), (int)(oy*SSM.windowHeight));     
      }
      
   }
   
   
   @Override
   public void addTuioObject(TuioObject o) {
   }

   @Override
   public void updateTuioObject(TuioObject o) {
   }

}
