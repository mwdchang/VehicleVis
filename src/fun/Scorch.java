package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import TUIO.TuioTime;

////////////////////////////////////////////////////////////////////////////////
// Similar to scorched earth but with TUIO?
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
   }

   @Override
   public void removeTuioCursor(TuioCursor tc) {
      double x = tc.getX() * this.winWidth;
      double y = this.winHeight - tc.getY() * this.winHeight;
      points.add(new DCTriple(x, y, 0));
   }

   @Override
   public void updateTuioCursor(TuioCursor tc) {
   }


   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      
      GraphicUtil.setOrthonormalView(gl2, 0, a.getWidth(), 0, a.getHeight(), -10, 10);
      
      gl2.glColor4d(1, 1, 1, 1);
      gl2.glBegin(GL2.GL_LINES);
         gl2.glVertex2d(0, 0);
         gl2.glVertex2d(10, 10);
      gl2.glEnd();
      
      for (int i=0; i < points.size(); i++) {
         DCTriple p = points.elementAt(i);
         GraphicUtil.drawPie(gl2, p.x, p.y, 0, 10, 0, 360, 10);
      }
      
   }
   
   @Override
   public void init(GLAutoDrawable a) {
      this.gl2 = a.getGL().getGL2();
      super.init(a);   
      this.canvas.addKeyListener(this);
   }
   
   
   // Just testing
   Vector<DCTriple> points = new Vector<DCTriple>();

   
   // Unused
   public void removeTuioObject(TuioObject arg0) { }
   public void updateTuioObject(TuioObject arg0) {}
   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   public void keyTyped(KeyEvent arg0) {}
   public void keyPressed(KeyEvent arg0) {}
   
   public GL2 gl2;
}
