package touch;

import java.util.Hashtable;

import util.DCUtil;

import datastore.SSM;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;

public class TUIOTest implements TuioListener {
   
   public static void main(String args[]) {
      TuioClient client = new TuioClient();
      client.addTuioListener(new TUIOTest());
      client.connect();
      while(true) {}
   }
   
   public Hashtable<Long, WCursor> eventTable = new Hashtable<Long, WCursor>();
   public float sensitivity = 0;
   
   public TUIOTest() {
      super();   
      sensitivity = Float.valueOf(System.getProperty("TUIOSensitivity", "0.002")); 
      System.out.println("TUIO Sensitivity : " + sensitivity);
   }
   

   @Override
   public void addTuioCursor(TuioCursor o) {
      System.err.println("=== Adding TUIO Cursor");
      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
      
      if (o.getY() > 0.5) { 
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_MAKE_SCROLL, o.getX(), o.getY(), o));
      } else {
         eventTable.put(o.getSessionID(), new WCursor(SSM.ELEMENT_MANUFACTURE_SCROLL, o.getX(), o.getY(), o));
      }
   
   }
   
   @Override
   public void removeTuioCursor(TuioCursor o) {
      System.err.println("=== Removing TUIO Cursor");
      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
      eventTable.remove(o.getSessionID());
   }


   @Override
   public void refresh(TuioTime o) {
      //System.err.println("TUIO Refresh");
      //System.out.println(o);
   }


   @Override
   public void removeTuioObject(TuioObject o) {
      System.err.println("=== Removing TUIO Object");
      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
   }

   @Override
   public void updateTuioCursor(TuioCursor o) {
      // Sanity check, ignore small changes (evoluce table seem to send out crap changes)
      float ox = eventTable.get(o.getSessionID()).x;
      float oy = eventTable.get(o.getSessionID()).y;
      if (DCUtil.dist( (ox-o.getX()), (oy-o.getY())) < sensitivity) return;
      
      
      
      System.err.println("=== Updating TUIO Cursor");
      System.out.println(o.getSessionID() + "\t" + o.getX() + "\t" + o.getY());
      
      boolean eventHandled = false;
      if (eventTable.containsKey(o.getSessionID()) && eventTable.size() > 1) {
         
         // Check for multi touch
         float oldX = o.getPath().elementAt(o.getPath().size()-2).getX();
         float oldY = o.getPath().elementAt(o.getPath().size()-2).getY();
         
         float x = o.getX();
         float y = o.getY();
         
         int element = eventTable.get(o.getSessionID()).element;
         
         
         // Check for multi touch (pinch/unpinch) events
         for (WCursor wcursor : eventTable.values()) {
            TuioCursor cursor = wcursor.cursor;
            if (cursor.getSessionID() == o.getSessionID()) continue;
            float px = cursor.getX();
            float py = cursor.getY();
            
            double oldDistance = Math.sqrt( (oldX-px)*(oldX-px) + (oldY-py)*(oldY-py));
            double distance = Math.sqrt( (x-px)*(x-px) + (y-py)*(y-py));
            
            if (distance > oldDistance && element == wcursor.element) {
               //System.out.println("possible pinch event");
               System.out.println("Moving away from " + cursor.getSessionID());   
               return;
            } else if (distance < oldDistance && element == wcursor.element) {
               //System.out.println("possible pinch event");
               System.out.println("Moving closer from " + cursor.getSessionID());   
               return;
            }
         }
      }
      
      
      // We cannot detect any multi gesture event, so check
      // if this can fit under any single touch gesture events
      System.out.println("Check single touch event");
      if (ox > o.getX()) System.out.println("Swipe Left");
      if (ox < o.getX()) System.out.println("Swipe Right");
      if (oy > o.getY()) System.out.println("Swipe Up");
      if (oy < o.getY()) System.out.println("Swipe Down");
      
      int originElement = eventTable.get(o.getSessionID()).element;
      eventTable.put(o.getSessionID(), new WCursor(originElement, o.getX(), o.getY(), o));
   }
   
   
   @Override
   public void addTuioObject(TuioObject o) {
   }

   @Override
   public void updateTuioObject(TuioObject o) {
   }

}
