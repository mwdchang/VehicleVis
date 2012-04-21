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
               /*
               Vector<Long> removal = new Vector<Long>();
               for (WCursor w : eventTable.values()) {
                  if (w.cursor.getPath().size() > 3) continue;
                  System.out.println("TAPS:" + w.tap);
                  removal.add(w.cursor.getSessionID());
               }
               for (int i=0; i < removal.size(); i++) {
                  eventTable.remove(removal.elementAt(i));
               }
               */
               
               // First increment all the events
               /*
               for (WCursor w : eventTable.values()) {
                  w.holdTime += interval;
               }
               for (WCursor w : eventTable.values()) {
                  if (w.element == SSM.ELEMENT_NONE && w.holdTime > EVENT_RING) {
                     w.element = SSM.ELEMENT_LENS;
                     Event.createLens((int)(w.x*SSM.windowWidth), (int)(w.y*SSM.windowHeight));
                  }
               }               
               for (WCursor w : eventTable.values()) {
                  if (w.element == SSM.ELEMENT_LENS && w.holdTime > EVENT_RING) {
                     Event.removeLens((int)(w.x*SSM.windowWidth), (int)(w.y*SSM.windowHeight));
                  }
               }
               */
               
               Thread.sleep(interval);
            }
         } catch(Exception e) { e.printStackTrace(); }
      }
   };
   

   @Override
   public void addTuioCursor(TuioCursor o) {
      System.err.println("=== Adding TUIO Cursor");
//      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
      
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
      //System.err.println("=== Removing TUIO Cursor");
      
      // Check if this is a tap event (approximate)
      WCursor w = eventTable.get(o.getSessionID());
      if (w.cursor.getPath().size() < 3) {
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
         // Probably a tap, but is this a single or multi tap ?
         /*
         for (WCursor w2 : eventTable.values()) {
            if (w2.cursor.getSessionID() == w.cursor.getSessionID()) continue;      
            // There is another tap nearby, this is probably a double tap, remove the current one
            if (DCUtil.dist( (w.x-w2.x), (w.y-w2.y)) < 0.05)  {
               w2.tap++;   
               return;
            } 
         }
         w.tap++;
         */
      }
      
      eventTable.remove(o.getSessionID());
      
      /*
      int num  = getElementPoint(eventTable.get(o.getSessionID()).element);
      System.out.println("Total TUIO Time : " + time);
      if (num == 1 && time < EVENT_TAP) {
         SSM.mouseX = o.getScreenX(SSM.windowWidth);
         SSM.mouseY = o.getScreenY(SSM.windowHeight);
         SSM.instance().l_mouseClicked = true;
      }
      eventTable.remove(o.getSessionID());
      */
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
      float ox = eventTable.get(o.getSessionID()).x;
      float oy = eventTable.get(o.getSessionID()).y;
      if (DCUtil.dist( (ox-o.getX()), (oy-o.getY())) < sensitivity) return;
      
      
      System.err.println("=== Updating TUIO Cursor");
//      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
      
      boolean eventHandled = false;
      if (eventTable.containsKey(o.getSessionID()) && eventTable.size() > 1) {
         
         // Check for multi touch
         float oldX = o.getPath().elementAt(o.getPath().size()-2).getX();
         float oldY = o.getPath().elementAt(o.getPath().size()-2).getY();
         
         float x = o.getX();
         float y = o.getY();
         
         int element = eventTable.get(o.getSessionID()).element;
         
         
         // Check for multi touch (pinch/unpinch) events
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
      
      // We cannot detect any multi gesture event, so check
      // if this can fit under any single touch gesture events
      System.out.println("Check single touch event");
      if (wcursor.element == SSM.ELEMENT_NONE){
         Event.setCamera(o.getScreenX(SSM.windowWidth), o.getScreenY(SSM.windowHeight), (int)(ox*SSM.windowWidth), (int)(oy*SSM.windowHeight));     
      } else if (wcursor.element == SSM.ELEMENT_LENS){
         Event.moveLensTUIO(o.getScreenX(SSM.windowWidth), o.getScreenY(SSM.windowHeight), (int)(ox*SSM.windowWidth), (int)(oy*SSM.windowHeight));     
      }
      
      int originElement = wcursor.element;
      eventTable.put(o.getSessionID(), new WCursor(originElement, o));
   }
   
   
   @Override
   public void addTuioObject(TuioObject o) {
   }

   @Override
   public void updateTuioObject(TuioObject o) {
   }

}
