package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import datastore.SSM;

import model.DCTriple;

import test.JOGLBase;
import touch.WCursor;
import util.GraphicUtil;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;

public class Bounce extends JOGLBase implements TuioListener, KeyListener {

   public static void main(String[] args) {
      TuioClient tc = new TuioClient();   
      Bounce tune = new Bounce();
      tc.addTuioListener(tune);
      tc.connect();
      
      tune.unDecorated = false;
      tune.run("TUNE TUIO", 800, 800);
   }
   
   public Bounce() {
      Thread t1 = new Thread(clearDeadzone);
      t1.start();         
   }
   
   
   @Override
   public void keyPressed(KeyEvent e) {
      this.registerStandardExit(e);
      
      if (e.getKeyChar() == 'r') {
         System.err.println("Stats");   
         System.err.println("Points added :" + pAdded);
         System.err.println("Points updated:" + pUpdated);
         System.err.println("Points removed:" + pRemoved);
         System.err.println("");
         pAdded = 0;
         pUpdated = 0;
         pRemoved = 0;
         synchronized(points){ points.clear(); }
         synchronized(trail) { trail.clear();  }
         synchronized(start) { start.clear();  }
         synchronized(end) { end.clear(); }
      }
   }
   
   
   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      
      width = a.getWidth();
      height = a.getHeight();
      
      this.basicClear(gl2);
      GraphicUtil.setOrthonormalView(gl2, 0, width, 0, height, -10, 10);
      gl2.glLoadIdentity();
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      gl2.glColor4d(0, 0, 1, 1);
      gl2.glPointSize(7.5f);
      gl2.glBegin(GL2.GL_POINTS);
      for (WCursor wc : points.values()) {
         float x = wc.x * width; 
         float y = wc.y * height; 
         gl2.glVertex2d( x, (height-y) );
      }
      gl2.glEnd();
      
      gl2.glColor4d(0, 1, 1.0, 1);
      gl2.glPointSize(2.0f);
      gl2.glBegin(GL2.GL_POINTS);
      for (int i=0; i < trail.size(); i++) {
         gl2.glVertex3dv( trail.elementAt(i).toArray3d(), 0 );
      }
      gl2.glEnd();
      
      gl2.glColor4d(0, 1, 0, 1);
      gl2.glPointSize(5.5f);
      gl2.glBegin(GL2.GL_POINTS);
      for (int i=0; i < start.size(); i++) {
         gl2.glVertex3dv( start.elementAt(i).toArray3d(), 0 );
      }
      gl2.glEnd();
      
      gl2.glColor4d(1, 0, 0, 1);
      gl2.glPointSize(5.5f);
      gl2.glBegin(GL2.GL_POINTS);
      for (int i=0; i < end.size(); i++) {
         gl2.glVertex3dv( end.elementAt(i).toArray3d(), 0 );
      }
      gl2.glEnd();
     
      /*
      gl2.glColor4d(1, 1, 0, 1);
      gl2.glBegin(GL2.GL_TRIANGLES);
         gl2.glVertex3d(0, 0, 0);
         gl2.glVertex3d(200, 0, 0);
         gl2.glVertex3d(200, 200, 0);
      gl2.glEnd();
      */
   }

   @Override 
   public void init(GLAutoDrawable a) {
      super.init(a);
      this.canvas.addKeyListener(this);
   }
   
   
   @Override
   public void addTuioCursor(TuioCursor t) {
      DCTriple p = new DCTriple( t.getX()*width, (1.0f-t.getY())*height, 0 );
      WCursor w = new WCursor(SSM.ELEMENT_NONE, t);;
      
      synchronized(points) {
         for (WCursor wc : points.values()) {
            // 2) Remove new touch points that are way too close in terms of time and distance
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 30) {
               if (Math.abs( wc.timestamp-w.timestamp) < 100) {
                  System.err.println("H2 " + points.size());
                  return;   
               }
            }
            
            // 3) Remove new touch points if there are move points in the vicinity
            if (dist(wc.x, wc.y, w.x, w.y, width, height) < 500 &&
                dist(wc.x, wc.y, w.x, w.y, width, height) > 20) {
               if (wc.state == WCursor.STATE_MOVE) {
                  System.err.println("H3 " + points.size());
                  return;   
               }
            }
            
            // 4) Remove any deadzone points
            for (int i=0; i < deadzone.size(); i++) {
               float x = w.x*width;
               float y = (1.0f-w.y)*height;
               DCTriple zone = deadzone.elementAt(i);
               if (dist(x,y, zone.x, zone.y, 1, 1) < 50) {
                  System.err.println("H4 " + points.size());      
                  return;
               }
            }
         }
         
         System.out.println("Path length : " + t.getPath().size());
         
         points.put(t.getSessionID(), w);
         pAdded ++;
      }
      synchronized(trail) { trail.add(p); start.add(p);}
   }
   
   
   public double dist(double x1, double y1, double x2, double y2, double w, double h) {
      return Math.sqrt((x1-x2)*(x1-x2)*w*w + (y1-y2)*(y1-y2)*h*h);    
   }
   
   @Override
   public void updateTuioCursor(TuioCursor t) {
      DCTriple p = new DCTriple( t.getX()*width, (1.0f-t.getY())*height, 0 );
      WCursor w = points.get(t.getSessionID());
      if (w == null) return;
      
      
      // 1) Remove touch point jitters
      //if ( t.getTuioTime().getTotalMilliseconds() - w.timestamp < 300)  {
      if (dist(w.x, w.y, t.getX(), t.getY(), width, height) < 3) {
            System.err.println("H1 " + points.size());
            return;   
      }
      //}
      
      // 4) Reinforce intention to actually move
      if (w.numUpdate < 1) {
         if ( t.getTuioTime().getTotalMilliseconds() - w.timestamp < 600)  {
         if (dist(w.x, w.y, t.getX(), t.getY(), width, height) < 20) {
            System.err.println("H4 " + points.size());
            return;   
         }
         }
      }
      
      
      
      w.x = t.getX();
      w.y = t.getY();
      w.numUpdate ++;
      w.state = WCursor.STATE_MOVE;
      
      synchronized(points) {
         //points.put(t.getSessionID(), p);
         pUpdated ++;
      }
      synchronized(trail) { trail.add(p); }
   }

   @Override
   public void removeTuioCursor(TuioCursor t) {
      WCursor wc = points.get(t.getSessionID());
      if (wc == null) return;
      
      System.out.println("Removing TUIO Cursor " + wc.numUpdate);
      DCTriple p; 
      if (wc.numUpdate > 0) {
         p = new DCTriple( t.getX()*width, (1.0f-t.getY())*height, 0 );
      } else {
         p = new DCTriple( wc.x*width, (1.0f-wc.y)*height, 0 );
      }
      synchronized(points) {
         points.remove( t.getSessionID() );
         deadzone.add( p );
         end.add(p);
         pRemoved ++;
      }
   }
   


   
   public void keyTyped(KeyEvent arg0) {}
   public void keyReleased(KeyEvent arg0) {}
   public void updateTuioObject(TuioObject arg0) {}
   public void removeTuioObject(TuioObject arg0) {}
   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   
   public int height, width;
   
   public int pAdded = 0;
   public int pUpdated = 0;
   public int pRemoved = 0;
   
   public Hashtable<Long, WCursor> points = new Hashtable<Long, WCursor>();
   public Vector<DCTriple> trail = new Vector<DCTriple>();
   public Vector<DCTriple> start = new Vector<DCTriple>();
   public Vector<DCTriple> end   = new Vector<DCTriple>();
   
   
   // A deadzone is an area that cannot be used for a certain time
   // period, we use this for to prevent add event from triggering
   // when a remove event is fired (due to jitter from finger)
   public Vector<DCTriple> deadzone = new Vector<DCTriple>();
   public Runnable clearDeadzone = new Runnable() {
      public void run() {
         try {
            while(true) {
               Thread.sleep(100);   
               synchronized(clearDeadzone) { deadzone.clear(); }
            }
         } catch (Exception e) {}
      }
   };
   
   

}
