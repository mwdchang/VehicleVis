package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Hashtable;
import org.jfugue.*;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import model.DCTriple;

import test.JOGLBase;
import util.GraphicUtil;

public class ForcePad1 extends JOGLBase implements KeyListener {
   
   
   public final float MAX_CORNER_PRESSURE = 100;

   public static void main(String args[]) {
      ForcePad1 fp = new ForcePad1();
      fp.unDecorated = false;
      fp.sendToNextScreen = true;
      fp.run("Test ForcePAD", 800, 800);
   }
   
   public ForcePad1() {
      bottomRight = bottomLeft = topRight = topLeft = 0.0f;
      FPClient c = new FPClient();
      Thread t = new Thread(c);
      t.start();
   }
   
   Player jplayer = new Player();

   @Override
   public void display(GLAutoDrawable a) {
      GL2 gl2 = a.getGL().getGL2();
      this.basicClear(gl2);
      
      GraphicUtil.setOrthonormalView(gl2, 0, winWidth, 0, winHeight, -10, 10 );
      
      
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      //gl2.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glColor4d(1, 0, 0, bottomLeft/MAX_CORNER_PRESSURE);
         gl2.glVertex2d(0, 0);
         
         gl2.glColor4d(0, 1, 0, bottomRight/MAX_CORNER_PRESSURE);
         gl2.glVertex2d(winWidth, 0);
         
         gl2.glColor4d(0, 0, 1, topRight/MAX_CORNER_PRESSURE);
         gl2.glVertex2d(winWidth, winHeight);
         
         gl2.glColor4d(0, 1, 1, topLeft/MAX_CORNER_PRESSURE);
         gl2.glVertex2d(0, winHeight);
      gl2.glEnd();
      
      /*
      for (int i=0; i < events.size(); i++) {
         TPoint p = events.elementAt(i);
         gl2.glColor4d(p.f/500.0f, (500.0-p.f)/500.0, 0, p.z/2000);
         GraphicUtil.drawPie(gl2, p.x*winWidth, p.y*winHeight, 0, 15, 0, 350, 36);
      }
      */
      
      for (TPoint p : events.values()) {
         gl2.glColor4d(p.f/500.0f, (500.0-p.f)/500.0, 0, 0.5);
         if (p.z > 0) {
            GraphicUtil.drawPie(gl2, p.x*winWidth, p.y*winHeight, 0, p.f, 0, 360, 36);
         }
      }
      synchronized( events.values() ) {
         for (TPoint p : events.values()) {
            p.z --;   
            //if (p.z < 0) events.remove(p.id);
         }
      }
      
   }
   
   @Override
   public void init(GLAutoDrawable a) {
     super.init(a);
     this.canvas.addKeyListener(this);
     
     GL2 gl2 = a.getGL().getGL2();   
     this.winHeight = a.getHeight();
     this.winWidth = a.getWidth();
     
   }
   
   
   @Override
   public void keyReleased(KeyEvent e) {
      this.registerStandardExit(e);   
   }
   
   
   // Blah
   public class FPClient implements Runnable {

      Socket requestSocket;
      BufferedReader in;
      String message;
      
      int portNum = 55555;      
      

      @Override
      public void run() {
         // TODO Auto-generated method stub
         try {
            //1. creating a socket to connect to the server
            requestSocket = new Socket("localhost", portNum);
            System.out.println("Connected to localhost in port" +  portNum);
            
            
            //2. get Input and Output streams
            in = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
            
            //3: Communicating with the server
            String line = "";
            while (requestSocket.isConnected()) {
               line = in.readLine();
               if (line.equals("Q")) break;
               
               String[] parts = line.split("\\|");
               
               int  id = Integer.parseInt(parts[0]);
               float x = Float.parseFloat(parts[1]);
               float y = Float.parseFloat(parts[2]);
               float f = Float.parseFloat(parts[3]);
               
               bottomLeft  = Float.parseFloat(parts[4]);
               topLeft     = Float.parseFloat(parts[5]);
               bottomRight = Float.parseFloat(parts[6]);
               topRight    = Float.parseFloat(parts[7]);
               
               bottomLeft  = Math.min(MAX_CORNER_PRESSURE, Math.max(0, bottomLeft));
               bottomRight = Math.min(MAX_CORNER_PRESSURE, Math.max(0, bottomRight));
               topLeft     = Math.min(MAX_CORNER_PRESSURE, Math.max(0, topLeft));
               topRight    = Math.min(MAX_CORNER_PRESSURE, Math.max(0, topRight));
               
               System.out.println(bottomLeft + " " + bottomRight + " " + topLeft + " " + topRight);
               /*
               int c = 65 + (int)(x*7.0f);
               jplayer.play( (char)c +"");
               
               System.out.println( (char)c );
               */
               
               synchronized(events) {
                  //events.add(new TPoint(x, y, 2000, f));
                  events.put(id, new TPoint(id, x, y, 2000, f));
               }
               
            }
            
            System.out.println("Quitting...");
         } catch( Exception e) {
            e.printStackTrace();
         }         
      }
      
   }
   
   
   public float bottomRight, bottomLeft, topRight, topLeft;
   
   public Hashtable<Integer, TPoint> events = new Hashtable<Integer, TPoint>();
   public class TPoint {
      public TPoint(int i, float a, float b, float c, float d) {
         id = i;
         x = a;
         y = b;
         z = c;
         f = d;
      }
      float x, y, z, f;   
      int id;
   }



   public void keyPressed(KeyEvent arg0) {}
   public void keyTyped(KeyEvent arg0) {}
}
