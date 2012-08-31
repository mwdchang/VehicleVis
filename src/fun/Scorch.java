package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import model.DCTriple;

import test.JOGLBase;
import util.GraphicUtil;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioPoint;
import TUIO.TuioTime;

////////////////////////////////////////////////////////////////////////////////
// Similar to scorched earth but with TUIO? Well...I guess not eh?
////////////////////////////////////////////////////////////////////////////////
public class Scorch extends JOGLBase implements TuioListener, KeyListener {
   
   public static void main(String[] args) {
      Scorch scorch = new Scorch();
      
      // Start TUIO Client
      TuioClient client = new TuioClient();
      client.addTuioListener(scorch);
      client.connect();
      
      // This needs to go last
      scorch.isMaximized = true;
      scorch.run("Scorch", 600, 600);
   }

   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);
   }

   @Override
   public void addTuioCursor(TuioCursor tc) {
      Cursor c = new Cursor();
      CursorPoint cp = new CursorPoint();
      cp.x = tc.getX() * this.winWidth;
      cp.y = this.winHeight - tc.getY()*this.winHeight;
      cp.z = 0;
      cp.time = tc.getTuioTime().getTotalMilliseconds();
      c.id = tc.getSessionID();
      c.path.add(cp);
      
      eventTable.put(c.id, c);
   }
   
   @Override
   public void updateTuioCursor(TuioCursor tc) {
      long id = tc.getSessionID();
      if (eventTable.get(id) == null) return;
      
      CursorPoint cp = new CursorPoint();
      cp.x = tc.getX() * this.winWidth;
      cp.y = this.winHeight - tc.getY()*this.winHeight;
      cp.z = 0;
      cp.time = tc.getTuioTime().getTotalMilliseconds();
      
      CursorPoint old = eventTable.get(id).path.lastElement();
      
      // Prevent update points that are too close
      if ( Math.sqrt( (old.x - cp.x)*(old.x - cp.x) + (old.y - cp.y)*(old.y - cp.y)) < 5) return;
     
      eventTable.get(id).path.add(cp);
      
      
   }

   @Override
   public void removeTuioCursor(TuioCursor tc) {
      long id = tc.getSessionID();
      if (eventTable.get(id) == null) return;
      
      Cursor c = eventTable.get(id);
      
      if (c.path.size() < 2) return;
      
      CursorPoint p1 = c.path.elementAt( c.path.size() - 1);
      CursorPoint p2 = c.path.elementAt( c.path.size() - 2);
      
      
      Cluster cluster = new Cluster();
      cluster.x = p1.x;
      cluster.y = p1.y;
      cluster.z = 0;
      cluster.dx = (p1.x - p2.x)/25.0;
      cluster.dy = (p1.y - p2.y)/25.0;
      cluster.dz = 0;
      
      cluster.angle = 0;
      cluster.dangle = -0.125 + Math.random()/4;
      
      synchronized( gpoints ) {
         gpoints.add(cluster);
      }
      
      eventTable.remove(id);
   }



   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      gl2.glClearColor(1, 1, 1, 1);
      this.basicClear(gl2);
      
      GraphicUtil.setOrthonormalView(gl2, 0, a.getWidth(), 0, a.getHeight(), -10, 10);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      
      // Drawing phase
      for (int i=0; i < gpoints.size(); i++) {
         Cluster c = gpoints.elementAt(i);
         gl2.glPushMatrix();
            gl2.glTranslated(c.x, c.y, 0);
            gl2.glRotated(c.angle, 0, 0, 1);
            if (c.dy > 0) {
               // Happy Face
               gl2.glColor4d(1, 1, 0, 1);
               GraphicUtil.drawPie(gl2, 0, 0, 0, 50, 0, 360, 30);
               gl2.glColor4d(0, 0, 0, 1);
               GraphicUtil.drawArc(gl2, 0, 5, 0, 36, 40, 210, 330, 30);
               GraphicUtil.drawPie(gl2, -12, +14, 0, 7, 0, 360, 30);
               GraphicUtil.drawPie(gl2, +12, +14, 0, 7, 0, 360, 30);
            } else if (c.dy < 0){
               // Shock Face
               gl2.glColor4d(1, 1, 0, 1);
               GraphicUtil.drawPie(gl2, 0, 0, 0, 50, 0, 360, 30);
               gl2.glColor4d(0, 0, 0, 1);
               GraphicUtil.drawArc(gl2, -24, +20, 0, 22, 24, 0, 360, 30);
               GraphicUtil.drawArc(gl2, +24, +20, 0, 22, 24, 0, 360, 30);
               gl2.glColor4d(1, 1, 1, 1);
               GraphicUtil.drawPie(gl2, -24, +20, 0, 22, 0, 360, 30);
               GraphicUtil.drawPie(gl2, +24, +20, 0, 22, 0, 360, 30);
               gl2.glColor4d(0, 0, 0, 1);
               GraphicUtil.drawPie(gl2, -24, +20, 0, 4, 0, 360, 10);
               GraphicUtil.drawPie(gl2, +24, +20, 0, 4, 0, 360, 10);
               GraphicUtil.drawQuadFan(gl2, 0, -22, 0, 9, 3);
            } else {
               // XP Face
               gl2.glColor4d(1, 1, 0, 1);
               GraphicUtil.drawPie(gl2, 0, 0, 0, 50, 0, 360, 30);
               gl2.glColor4d(0, 0, 0, 1);
               gl2.glLineWidth(5.0f);
               gl2.glBegin(GL2.GL_LINES);
                  gl2.glVertex2d(-25, 20);
                  gl2.glVertex2d( 25, 0);
                  
                  gl2.glVertex2d(-25,  0);
                  gl2.glVertex2d( 25,  20);
                  
                  gl2.glVertex2d( -25,  -10);
                  gl2.glVertex2d(  25,  -10);
                  
               gl2.glEnd(); 
               gl2.glLineWidth(1.0f);
               GraphicUtil.drawArc(gl2, 5, -10, 0, 15, 19, 180, 360, 18, 1);
               gl2.glColor4d(1, 0, 0, 1); 
               GraphicUtil.drawPie(gl2, 5, -11, 0, 14, 180, 360, 18 );
               
            }
         gl2.glPopMatrix();
         
         
      }
      
      // Update phase
      for (int i=0; i < gpoints.size(); i++) {
         gpoints.elementAt(i).y += gpoints.elementAt(i).dy;
         gpoints.elementAt(i).x += gpoints.elementAt(i).dx;
         gpoints.elementAt(i).angle += gpoints.elementAt(i).dangle;
         gpoints.elementAt(i).dy -= 0.0008;  // acceleration 
         
         if (gpoints.elementAt(i).x <=50) gpoints.elementAt(i).dx = - gpoints.elementAt(i).dx;
         if (gpoints.elementAt(i).x >= this.winWidth-50) gpoints.elementAt(i).dx = - gpoints.elementAt(i).dx;
         
         if (gpoints.elementAt(i).y <= 50) {
            gpoints.elementAt(i).y = 50;
            gpoints.elementAt(i).dy = 0;
            gpoints.elementAt(i).dx = 0;
            gpoints.elementAt(i).dangle = 0;
            gpoints.elementAt(i).idleCount ++;
         }
         // Terminal velocity
         //if (gpoints.elementAt(i).dy < -0.5) gpoints.elementAt(i).dy = -0.5;
         
      }
      
      // Remove phase
      Iterator<Cluster> clusterIter = gpoints.iterator();
      while (clusterIter.hasNext()) {
         //if (clusterIter.next().y < 0) clusterIter.remove();
         if (clusterIter.next().idleCount > 1500) clusterIter.remove();
      }
      
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      this.gl2 = a.getGL().getGL2();
      super.init(a);   
      this.canvas.addKeyListener(this);
   }
   
   
   // Just testing
   Vector<Cluster> gpoints = new Vector<Cluster>();
   
   
   public class Cluster {
      double x, y, z;
      double dx, dy, dz;
      double angle;
      double dangle;
      long idleCount = 0;
   }
   
   // Light weight cursors for graphics
   public class CursorPoint {
      double x;
      double y;
      double z;
      long time;
   }
   public class Cursor {
      long id;
      Vector<CursorPoint> path = new Vector<CursorPoint>();
   }
   public Hashtable<Long, Cursor> eventTable = new Hashtable<Long, Cursor>();

   
   // Unused
   public void removeTuioObject(TuioObject arg0) { }
   public void updateTuioObject(TuioObject arg0) {}
   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   public void keyTyped(KeyEvent arg0) {}
   public void keyPressed(KeyEvent arg0) {}
   
   public GL2 gl2;
}
