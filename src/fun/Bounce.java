package fun;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.GLBuffers;

import datastore.SSM;

import model.DCColour;
import model.DCTriple;

import test.JOGLBase;
import touch.WCursor;
import util.DCUtil;
import util.GraphicUtil;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;

////////////////////////////////////////////////////////////////////////////////
// Pong game for TUIO based multi touch systems
////////////////////////////////////////////////////////////////////////////////
public class Bounce extends JOGLBase implements TuioListener, KeyListener {

   public static void main(String[] args) {
      TuioClient tc = new TuioClient();   
      Bounce tune = new Bounce();
      tc.addTuioListener(tune);
      tc.connect();
      
      
      tune.unDecorated = true;
      tune.isMaximized = true;
      tune.sendToNextScreen = true;
      tune.run("TUNE TUIO", 800, 800);
   }
   
   
   public Bounce() {
      Thread t1 = new Thread(update);
      t1.start();         
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Calculate the distance between (x1, y1) and (x2, y2) with w and h as weight
   // modifiers
   ////////////////////////////////////////////////////////////////////////////////
   public double dist(double x1, double y1, double x2, double y2, double w, double h) {
      return Math.sqrt((x1-x2)*(x1-x2)*w*w + (y1-y2)*(y1-y2)*h*h);    
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
         ball.position = new DCTriple( width/2, height/2, 0);
         ball.direction = new DCTriple( 1, 0, 0);
      }
      if (e.getKeyChar() == 'p') {
         doScreenCapture = true;
      }
   }
   
   
   public void renderPaddle(GL2 gl2, Paddle paddle, DCColour c) {
      if (paddle != null) {
         synchronized(paddle) {
            if (paddle.connected == true) {
               for (int j=0; j < 2; j++) {
                  DCTriple s[] = new DCTriple[num_segment];
                  for (int i=0; i < num_segment; i++) {
                     s[i] = new DCTriple(paddle.segment[i]);
                  }
                  for (int i=1; i < (num_segment-1); i++) {
                     s[i].x += (float)(Math.random()*14 - 7);   
                     s[i].y += (float)(Math.random()*14 - 7);   
                  }
                  
                  gl2.glLineWidth(3.0f);
                  gl2.glBegin(GL2.GL_LINES);
                  gl2.glColor4fv(c.toArray(), 0);
                  //gl2.glColor4d(0, 0.4, 0.8, 0.8);
                  for (int i=0; i < (num_segment-1); i++) {
                     gl2.glVertex2d( s[i].x, s[i].y);
                     gl2.glVertex2d( s[i+1].x, s[i+1].y);
                  }
                  gl2.glEnd();
                  gl2.glLineWidth(1.0f);
               }
            } else {
               DCTriple dir = (paddle.p2.sub(paddle.p1));
               dir.normalize();
               DCTriple endPoint = paddle.p1.add(dir.mult((float)paddle.connectedCounter));
               gl2.glLineWidth(3.0f);
               gl2.glColor4fv(c.toArray(), 0);
               gl2.glBegin(GL2.GL_LINES);
                  gl2.glVertex2d( paddle.p1.x, paddle.p1.y);
                  gl2.glVertex2d( endPoint.x, endPoint.y);
               gl2.glEnd();
               gl2.glLineWidth(1.0f);
               if (endPoint.sub(paddle.p1).mag2() > paddle.p2.sub(paddle.p1).mag2()) {
                  paddle.connected = true;   
               }
            }
            
            // Check the normals
            gl2.glBegin(GL2.GL_LINES);
               gl2.glColor4d(1, 0, 1, 1);
               gl2.glVertex2d( paddle.centre.x, paddle.centre.y);
               gl2.glVertex2d( paddle.centre.x + 40*paddle.normal.x, paddle.centre.y + 40*paddle.normal.y);
            gl2.glEnd();
         }
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render loop
   ////////////////////////////////////////////////////////////////////////////////
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
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Draw the play zone
      ////////////////////////////////////////////////////////////////////////////////
      
      gl2.glColor4d(0.5, 0.5, 0.5, 0.8);
      gl2.glBegin(GL2.GL_LINES);
         gl2.glVertex2d(playZoneWidth, 0);
         gl2.glVertex2d(playZoneWidth, height);
         
         gl2.glVertex2d(playZoneWidth+3, 0);
         gl2.glVertex2d(playZoneWidth+3, height);
         
         gl2.glVertex2d(width-playZoneWidth, 0);
         gl2.glVertex2d(width-playZoneWidth, height);
         
         gl2.glVertex2d(width-playZoneWidth-3, 0);
         gl2.glVertex2d(width-playZoneWidth-3, height);
      gl2.glEnd();
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Draw the touch markers, each marker is drawn as concentric ellipses
      ////////////////////////////////////////////////////////////////////////////////
      for (WCursor wc : pointsPlayer1.values()) {
         float x = wc.x * width; 
         float y = wc.y * height; 
         
         gl2.glColor4d(0, 0.1, 0.2, 0.5);
         for (int i=0; i < 15; i++) {
            GraphicUtil.drawPie(gl2, x, (height-y), 0, (i+1)*2, 0, 360, 36);  
         }
      }
      for (WCursor wc : pointsPlayer2.values()) {
         float x = wc.x * width; 
         float y = wc.y * height; 
         gl2.glColor4d(0, 0.2, 0.1, 0.5);
         for (int i=0; i < 15; i++) {
            GraphicUtil.drawPie(gl2, x, (height-y), 0, (i+1)*2, 0, 360, 36);  
         }         
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Draw the paddles
      ////////////////////////////////////////////////////////////////////////////////
      this.renderPaddle(gl2, player1, DCColour.fromFloat(0.0f, 0.4f, 0.8f, 0.8f));
      this.renderPaddle(gl2, player2, DCColour.fromFloat(0.0f, 0.8f, 0.4f, 0.8f));
     
      
      ////////////////////////////////////////////////////////////////////////////////
      // Draw the ball
      ////////////////////////////////////////////////////////////////////////////////
      //gl2.glColor4d(0, 1, 1, 1);
      //GraphicUtil.drawPie(gl2, ball.position.x, ball.position.y, 0, 10, 0, 360, 12); 
      
      gl2.glEnable(GL2.GL_TEXTURE_2D);
      this.drawFragment(gl2, 0.8, 1, 0.2, 0.5);
      
      
      // Check if screen capture is requested
      if (doScreenCapture) { 
         GraphicUtil.screenCap(gl2, "Bounce Capture.png");
         doScreenCapture = false;
      }
   }

   @Override 
   public void init(GLAutoDrawable a) {
      super.init(a);
      this.canvas.addKeyListener(this);
      
      width = a.getWidth();
      height = a.getHeight();
      
      GL2 gl2 = a.getGL().getGL2();
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      
      ball.position = new DCTriple(400, 400, 0);
      blurTexture(gl2);
      initFragment();
      
      // Dummy data
      /*
      player1 = new Paddle();
      player1.p1 = new DCTriple(60, 500, 0);
      player1.p2 = new DCTriple(150, 600, 0);
      player1.calc();
      WCursor p1cursor1 = new WCursor(0);
      WCursor p1cursor2 = new WCursor(0);
      p1cursor1.x = (float)player1.p1.x / (float)width; 
      p1cursor1.y = (float)(height-player1.p1.y) / (float)height; 
      p1cursor2.x = (float)player1.p2.x / (float)width; 
      p1cursor2.y = (float)(height-player1.p2.y) / (float)height; 
      pointsPlayer1.put(1L, p1cursor1);
      pointsPlayer1.put(2L, p1cursor2);
      
      player2 = new Paddle();
      player2.p1 = new DCTriple(1500, 200, 0);
      player2.p2 = new DCTriple(1600, 630, 0);
      player2.calc();
      WCursor p2cursor1 = new WCursor(0);
      WCursor p2cursor2 = new WCursor(0);
      p2cursor1.x = (float)player2.p1.x / (float)width; 
      p2cursor1.y = (float)(height-player2.p1.y) / (float)height; 
      p2cursor2.x = (float)player2.p2.x / (float)width; 
      p2cursor2.y = (float)(height-player2.p2.y) / (float)height; 
      pointsPlayer2.put(1L, p2cursor1);
      pointsPlayer2.put(2L, p2cursor2);
      */
     
   }
   
   
   @Override
   public void addTuioCursor(TuioCursor t) {
      WCursor w = new WCursor(SSM.ELEMENT_NONE, t);;
      
      int p1ZoneCounter = 0;
      int p2ZoneCounter = 0;
      
      if (w.x * width > playZoneWidth && w.x * width < (width - playZoneWidth)) return;
      
      if (w.x * width <= playZoneWidth) {
         synchronized(pointsPlayer1) {
            for (WCursor wc : pointsPlayer1.values()) {
               // 2) Remove new touch points that are way too close in terms of time and distance
               if (dist(wc.x, wc.y, w.x, w.y, width, height) < 30) {
                  if (Math.abs( wc.timestamp-w.timestamp) < 100) return;   
               }
               
               // Enforce max touch points per zone
               p1ZoneCounter++;
            }
            if (p1ZoneCounter >= 2) return;
            pointsPlayer1.put(t.getSessionID(), w);
         }
      } else if (w.x * width >= (width-playZoneWidth)) {
         synchronized(pointsPlayer2) {
            for (WCursor wc : pointsPlayer2.values()) {
               // 2) Remove new touch points that are way too close in terms of time and distance
               if (dist(wc.x, wc.y, w.x, w.y, width, height) < 30) {
                  if (Math.abs( wc.timestamp-w.timestamp) < 100) return;   
               }
               
               // Enforce max touch points per zone
               p2ZoneCounter++;
            }
            if (p2ZoneCounter >= 2) return;
            pointsPlayer2.put(t.getSessionID(), w);
         }
      }
      
   }
   
   
   
   
   @Override
   public void updateTuioCursor(TuioCursor t) {
      WCursor w = pointsPlayer1.get(t.getSessionID());
      if (w == null) w = pointsPlayer2.get(t.getSessionID());
      if (w == null) return;
      
      
      // 1) Remove touch point jitters
      //if ( t.getTuioTime().getTotalMilliseconds() - w.timestamp < 300)  {
      if (dist(w.x, w.y, t.getX(), t.getY(), width, height) < 25) {
         System.err.println("H1 ");
         return;   
      } 
      
      synchronized(pointsPlayer1) {
         pointsPlayer1.remove(t.getSessionID());
         if (pointsPlayer1.size() < 2) player1 = null;
      }
      synchronized(pointsPlayer2) {
         pointsPlayer2.remove(t.getSessionID());
         if (pointsPlayer2.size() < 2) player2 = null;
      }
         
   }
   

   @Override
   public void removeTuioCursor(TuioCursor t) {
      WCursor wc = pointsPlayer1.get(t.getSessionID());
      if (wc == null) wc = pointsPlayer2.get(t.getSessionID());
      if (wc == null) return;
      
      System.out.println("Removing TUIO Cursor " + wc.numUpdate);
      
      synchronized(pointsPlayer1) {
         pointsPlayer1.remove(t.getSessionID());
         if (pointsPlayer1.size() < 2) player1 = null;
      }
      synchronized(pointsPlayer2) {
         pointsPlayer2.remove(t.getSessionID());
         if (pointsPlayer2.size() < 2) player2 = null;
      }
     
   }

   
   // Not used
   public void keyTyped(KeyEvent arg0) {}
   public void keyReleased(KeyEvent arg0) {}
   public void updateTuioObject(TuioObject arg0) {}
   public void removeTuioObject(TuioObject arg0) {}
   public void addTuioObject(TuioObject arg0) {}
   public void refresh(TuioTime arg0) {}
   
   public int pAdded = 0;
   public int pUpdated = 0;
   public int pRemoved = 0;
   
   //public Hashtable<Long, WCursor> points = new Hashtable<Long, WCursor>();
   public Hashtable<Long, WCursor> pointsPlayer1 = new Hashtable<Long, WCursor>();
   public Hashtable<Long, WCursor> pointsPlayer2 = new Hashtable<Long, WCursor>();
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Update thread, this is the meat of the main game logic
   ////////////////////////////////////////////////////////////////////////////////
   public Runnable update = new Runnable() {
      public void run() {
         try {
            while(true) {
               // Check if player1 paddle is there
               if (player1 == null && pointsPlayer1.size() == 2) {
                  player1 = new Paddle();   
                  synchronized(player1) {
                     Vector<WCursor> v = new Vector<WCursor>();
                     v.addAll(pointsPlayer1.values());
                     player1.p1 = new DCTriple( v.elementAt(0).x * width, (1.0-v.elementAt(0).y)*height, 0);
                     player1.p2 = new DCTriple( v.elementAt(1).x * width, (1.0-v.elementAt(1).y)*height, 0);
                     player1.calc();
                  }
               }
               if (player2 == null && pointsPlayer2.size() == 2) {
                  player2 = new Paddle();   
                  synchronized(player2) {
                     Vector<WCursor> v = new Vector<WCursor>();
                     v.addAll(pointsPlayer2.values());
                     player2.p1 = new DCTriple( v.elementAt(0).x * width, (1.0-v.elementAt(0).y)*height, 0);
                     player2.p2 = new DCTriple( v.elementAt(1).x * width, (1.0-v.elementAt(1).y)*height, 0);
                     player2.calc();
                  }
               }
               
               
               // Update the ball position
               ball.position = ball.position.add( ball.direction.mult(ball.velocity) );
               
               // Check for collision
               // check against player 1
               DCTriple start = ball.position;
               DCTriple end   = ball.position.add(ball.direction.mult(ball.velocity));
               
               if (player1 != null && player1.connected == false) player1.connectedCounter++;
               if (player2 != null && player2.connected == false) player2.connectedCounter++;
               
               if (player1 != null && player1.connected) {
                  synchronized(player1) {
                     DCTriple hitP1 = DCUtil.intersectLine2D(start, end, player1.p1, player1.p2); 
                     if (hitP1 != null)  { 
                        float dot = ball.direction.dot(player1.normal);
                        ball.direction = (player1.normal.mult(-2*dot)).add(ball.direction);
                        ball.direction.normalize();
                     }
                  }
               }
               if (player2 != null && player2.connected) {
                  synchronized(player2) {
                     DCTriple hitP2 = DCUtil.intersectLine2D(start, end, player2.p1, player2.p2); 
                     if (hitP2 != null) {
                        float dot = ball.direction.dot(player2.normal);
                        ball.direction = (player2.normal.mult(-2*dot)).add(ball.direction);
                        ball.direction.normalize();
                     }
                  }
               }
               
               
               // Check against top and bottom
               if (ball.position.y < padding) ball.direction.y *= -1;
               if (ball.position.y > (height-padding)) ball.direction.y *= -1;
               
               
               Thread.sleep(10);   
            }
         } catch (Exception e) {}
      }
   };
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create a fuzzy ball gradient texture
   ////////////////////////////////////////////////////////////////////////////////
   public void blurTexture(GL2 gl2) {
      int h = 256;
      int w = 256;
      int h2 = 128;
      int w2 = 128;
      int channel = 4;
      ByteBuffer b = GLBuffers.newDirectByteBuffer(h*w*channel);
      
      for (int x=0; x < w; x++) {
         for (int y=0; y < h; y++) {
            int d = (int)Math.sqrt(  (x-w2)*(x-w2) + (y-h2)*(y-h2) );   
            int c = 255-(d*2);
            if (c < 0) c = 0;
            b.put((byte)c);
            b.put((byte)c);
            b.put((byte)c);
            b.put((byte)c);
         }
      }
      b.flip();
      
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
      gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
      gl2.glTexImage2D(GL2.GL_TEXTURE_2D, 0, 3, w, h, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, b);
   }   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Draw the particle effects
   ////////////////////////////////////////////////////////////////////////////////
   public void drawFragment(GL2 gl2, double r, double g, double b, double a) {
      double size; // = 0.05f;
      double slowX = 0.99f;
      double slowY = 0.99f;
      double slowZ = 0.99f;

      for (int i=0; i < fragmentSize; i++) {
         size = f[i].life * 30;
         gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
            //gl2.glColor4d(r, g-f[i].life, b, a);
            gl2.glColor4d(r-f[i].life, g-f[i].life, b-f[i].life, a);
            gl2.glTexCoord2f(1,1); gl2.glVertex3d(f[i].x + size, f[i].y + size, 0);
            gl2.glTexCoord2f(0,1); gl2.glVertex3d(f[i].x - size, f[i].y + size, 0);
            gl2.glTexCoord2f(1,0); gl2.glVertex3d(f[i].x + size, f[i].y - size, 0);
            gl2.glTexCoord2f(0,0); gl2.glVertex3d(f[i].x - size, f[i].y - size, 0);
         gl2.glEnd();

         f[i].x += f[i].dx/18;
         f[i].y += f[i].dy/18;

         f[i].dx *= slowX;
         f[i].dy *= slowY;
         f[i].life -= f[i].fadespeed;


         if (f[i].life < 0.05f) {       
            double velocity = Math.random()*5 + 0.001;            
            double angle    = Math.random()*360 * Math.PI / 180.0;
            
            f[i] = new Fragment();
            f[i].x = ball.position.x;
            f[i].y = ball.position.y;
            f[i].z = 0;
            
            f[i].dx = Math.cos(angle)*velocity;
            f[i].dy = Math.sin(angle)*velocity;
            f[i].dz = 0;
            
            f[i].life = 1.0;
            f[i].fadespeed = Math.random()/50.5;
         }
      }
   }   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize particles
   ////////////////////////////////////////////////////////////////////////////////
   public void initFragment() {
      for (int i=0; i < fragmentSize; i++) {
         double velocity = Math.random()*5 + 0.001;            
         double angle    = Math.random()*360 * Math.PI / 180.0;
         
         f[i] = new Fragment();
         f[i].x = ball.position.x;
         f[i].y = ball.position.y;
         f[i].z = 0;
         
         f[i].dx = Math.cos(angle)*velocity;
         f[i].dy = Math.sin(angle)*velocity;
         f[i].dz = Math.random();
         
         f[i].life = 0.0;
         f[i].fadespeed = Math.random()/6.0;
      }
   }   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // A placeholder to represent paddle
   ////////////////////////////////////////////////////////////////////////////////
   public class Paddle {
      public Paddle() {
         p1 = new DCTriple();
         p2 = new DCTriple();
      }
      
      public synchronized void calc() {
         connected = false;
         connectedCounter = 0;
         DCTriple direction = (p2.sub(p1));
         centre = p1.add( direction.mult(0.5f) );
         
         // Explicit knowledge that nomal's x should be positive
         normal = new DCTriple(-direction.y, direction.x, 0);
         normal.normalize();
         if (p1.x <= playZoneWidth && normal.x < 0) {
            normal = normal.mult(-1.0f);
         }
         if (p1.x >= (width - playZoneWidth) && normal.x > 0) {
            normal = normal.mult(-1.0f); 
         }
         
         DCTriple dir = p2.sub(p1);
         for (int i=0; i < num_segment; i++) {
            segment[i] = p1.add( dir.mult( (float)i / (float)(num_segment-1)));
         }
         
      }
      
      public DCTriple centre;
      public DCTriple normal;
      public DCTriple p1;
      public DCTriple p2;
      public DCTriple segment[] = new DCTriple[num_segment];
      
      public boolean connected = false;
      public int     connectedCounter = 0;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Place holder for the ball centre
   ////////////////////////////////////////////////////////////////////////////////
   public class Ball {
      public Ball() {
         position = new DCTriple();
         direction = new DCTriple(1, 0, 0);
         velocity = 3.0f;
      }
      public DCTriple position;
      public DCTriple direction;
      public float velocity = 1.0f;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Ball fragment particles 
   ////////////////////////////////////////////////////////////////////////////////
   public class Fragment {
      double x;
      double y;
      double z;
      
      double dx;
      double dy;
      double dz;
      
      double life;
      double fadespeed;
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Power
   ////////////////////////////////////////////////////////////////////////////////
   public class Power {
      public PType ptype;
   }
   
   
   public enum PType {
      CHANGE_DIRECTION, SPEED_UP, SLOW_DOWN, BREAK_PADDLE
   };
   
   
   // Game object declarations
   public Paddle player1 = null; 
   public Paddle player2 = null;
   public Ball ball = new Ball();
   public Fragment f[] = new Fragment[fragmentSize];   
   
   // Environment
   public static int playZoneWidth = 300;
   public static int padding = 10;
   public static int fragmentSize = 200;
   public static int height, width;
   public static int num_segment = 15;
   
   public static boolean doScreenCapture = false;
   public static float connectSpeed = 16.0f;
}
