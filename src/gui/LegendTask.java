package gui;

import java.nio.IntBuffer;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.GLBuffers;

import model.DCColour;

import datastore.CacheManager;
import datastore.SSM;
import datastore.SchemeManager;

import util.FontRenderer;
import util.GraphicUtil;

import exec.RenderTask;

/////////////////////////////////////////////////////////////////////////////////
// This class is responsible for rendering a legend for our colour scale.
// It is also capable of rendering a status line
/////////////////////////////////////////////////////////////////////////////////
public class LegendTask implements RenderTask {
   
   ////////////////////////////////////////////////////////////////////////////////
   // Draws a bucketed scale, with distinct colour and outline for each bucket
   ////////////////////////////////////////////////////////////////////////////////
   public void drawBucket(GL2 gl2) {
      int buckets = 9;
      float interval = 1.0f/9.0f;
      
      // Draws the distinct colours
      //gl2.glEnable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_QUADS);
      if (DIRECTION == HORIZONTAL) {
         for (float i=0; i < buckets; i++) {
            DCColour c = SchemeManager.instance().getColour(1, i*interval, 1.0f);
            gl2.glColor4fv(c.toArray(), 0);
            gl2.glVertex2d( startX + i*interval*width, startY);
            gl2.glVertex2d( startX + (i+1)*interval*width, startY);
            gl2.glVertex2d( startX + (i+1)*interval*width, startY+height);
            gl2.glVertex2d( startX + i*interval*width, startY+height);
         }
      } else {
         for (float i=0; i < buckets; i++) {
            DCColour c = SchemeManager.instance().getColour(1, i*interval, 1.0f);
            gl2.glColor4fv(c.toArray(), 0);
            gl2.glVertex2d( startX,             (int)(i*interval*height+startY));
            gl2.glVertex2d( startX + width,     (int)(i*interval*height+startY));
            gl2.glVertex2d( startX + width, (int)((i+1)*interval*height+startY));
            gl2.glVertex2d( startX,         (int)((i+1)*interval*height+startY));
         }
        
      }
      gl2.glEnd();
      
      
      // Draws the line separators
      gl2.glBegin(GL2.GL_LINES);
      gl2.glColor4d(0.4, 0.4, 0.4, 0.8);
      if (DIRECTION == HORIZONTAL) {
         for (float i=0; i < buckets; i++) {
            gl2.glVertex2d( startX + (i+1)*interval*width, startY);
            gl2.glVertex2d( startX + (i+1)*interval*width, startY+height);
         }
      } else {
         for (float i=0; i <= buckets; i++) {
            gl2.glVertex2d( startX ,        (i)*interval*height+startY);
            gl2.glVertex2d( startX + width, (i)*interval*height+startY);
         }
      }
      gl2.glEnd();
      

      
      
      
      // Draws an indicator of where the currently selected component is located
      /*
      if (SSM.instance().selectedGroup.size() > 0) {
         for (Integer key : SSM.instance().selectedGroup.keySet()) {
            float val = (float)CacheManager.instance().groupOccurrence.get(key)/(float)SSM.instance().maxOccurrence;
            int bucket = (int)Math.floor(val*SchemeManager.YellowOrangeRedPreset.length);
            bucket = bucket < 0 ? 0: bucket >= SchemeManager.YellowOrangeRedPreset.length ? (SchemeManager.YellowOrangeRedPreset.length-1) : bucket; 
            gl2.glLineWidth(2.0f);
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glColor4fv(SchemeManager.colour_blue.toArray(), 0);
               gl2.glVertex3d(startX + (bucket)*width*interval, startY, 0); 
               gl2.glVertex3d(startX + (bucket+1)*width*interval, startY, 0); 
               gl2.glVertex3d(startX + (bucket+1)*width*interval, startY+height, 0); 
               gl2.glVertex3d(startX + (bucket)*width*interval, startY+height, 0); 
            gl2.glEnd();           
            gl2.glLineWidth(1.0f);
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);         
         }
      }
      */
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Draws a continuous colour scale
   ////////////////////////////////////////////////////////////////////////////////
   public void drawContinuous(GL2 gl2) {
      gl2.glDisable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_QUAD_STRIP);
      
      // it is 1.01 because of floating point representation
      for (float val = 0.0f; val <= 1.01f; val+=0.1f) {
         DCColour c = SchemeManager.instance().getColour(1, val, 1.0f);        
         gl2.glColor4f(c.r, c.g, c.b, c.a);
         gl2.glVertex3d(startX + val*width,  startY, 0);
         gl2.glVertex3d(startX + val*width, startY+height, 0);
      }
      gl2.glEnd();      
      gl2.glEnable(GL2.GL_BLEND);
      
      
      // Draw an indicator of where the currently selected item located
      if (SSM.instance().selectedGroup.size() > 0 ) {
         float val = (float)CacheManager.instance().groupOccurrence.get(SSM.instance().selectedGroup)/(float)SSM.instance().maxOccurrence;
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glColor4fv(SchemeManager.colour_blue.toArray(), 0);
            gl2.glVertex3d(startX + val*width, startY, 0); 
            gl2.glVertex3d(startX + (val+0.03)*width, startY, 0); 
            gl2.glVertex3d(startX + (val+0.03)*width, startY+height, 0); 
            gl2.glVertex3d(startX + val*width, startY+height, 0); 
         gl2.glEnd();      
      }
         
   }

   
   @Override
   public void render(GL2 gl2) {
      GraphicUtil.setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight, -10, 10);
      
      this.startX = 30;
      this.startY = 30;
      /*
      this.startX = SSM.instance().windowWidth - this.width - 50;
      this.startY = SSM.instance().windowHeight - this.height - 20;
      */
      
      // Render the legend content, this will be either a 
      // continuous rendering, or render into buckets
      gl2.glDisable(GL2.GL_BLEND);
      if ( SSM.instance().colouringMethod == 4) {
         drawBucket(gl2);
      } else {
         drawContinuous(gl2);
      }
      
      // Draw a border around the legend to create a higher contrast
      gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glBegin(GL2.GL_LINE_LOOP);
         gl2.glColor4d(0.4, 0.4, 0.4, 0.8);
         gl2.glVertex2d(startX, startY);
         gl2.glVertex2d(startX+width, startY);
         gl2.glVertex2d(startX+width, startY+height);
         gl2.glVertex2d(startX, startY+height);
      gl2.glEnd();      
      
      // Draw a border around the legend
//      gl2.glBegin(GL2.GL_LINE_LOOP);
//         gl2.glColor4d(0, 0, 0, 1);
//         gl2.glVertex3d(startX, startY, 0);
//         gl2.glVertex3d(startX+width, startY, 0);
//         gl2.glVertex3d(startX+width, startY+height, 0);
//         gl2.glVertex3d(startX, startY+height, 0);
//      gl2.glEnd();
      
      if (DIRECTION == HORIZONTAL) {
         FontRenderer.instance().renderOnce(gl2, startX+width+2, startY-10, 0, SSM.instance().maxOccurrence+"");       
         FontRenderer.instance().renderOnce(gl2, startX-10, startY-10, 0, "0");       
      } else {
         FontRenderer.instance().renderOnce(gl2, startX+width+2, startY+height, 0, SSM.instance().maxOccurrence+"");       
         FontRenderer.instance().renderOnce(gl2, startX+width+2, startY-10, 0, "0");       
      }
      
   }

   
   @Override
   public void init(GL2 gl2) {
      if (this.DIRECTION == HORIZONTAL) {
         this.width = 200;
         this.height = 40;
      } else {
         this.width  =  40;
         this.height = 200;
      }
      /*
      this.startX = 50;
      this.startY = 50;
      this.width = 200;
      this.height = 40;
      */
      
   }

   @Override
   public void picking(GL2 gl2) {
//      
//      if (SSM.instance().colouringMethod != 4) return;
//      
//      // Quickie way to get out and save unnecessary rendering 
//      if (SSM.instance().l_mouseClicked == false) return;      
//      
//      IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
//      int buckets = 9;
//      float interval = 1.0f/9.0f;
//      
//      GraphicUtil.startPickingOrtho(gl2, buffer, SSM.instance().mouseX, SSM.instance().mouseY, SSM.instance().windowWidth, SSM.instance().windowHeight);
//      
//      for (float i=0; i < buckets; i++) {
//         gl2.glLoadName( (int)(i+1));
//         gl2.glPushMatrix();         
//         gl2.glBegin(GL2.GL_QUADS);
//            gl2.glVertex3d( startX + i*interval*width, startY, 0);
//            gl2.glVertex3d( startX + (i+1)*interval*width, startY, 0);
//            gl2.glVertex3d( startX + (i+1)*interval*width, startY+height, 0);
//            gl2.glVertex3d( startX + i*interval*width, startY+height, 0);
//         gl2.glEnd();
//         gl2.glPopMatrix();
//      }
//      Integer pick = GraphicUtil.finishPicking(gl2, buffer);
//      System.out.println("PIcked bucket is : " + pick);
   } 
   
   
   
   // startX and startY denotes the bottom left hand corner
   public int startX;
   public int startY;
   public int width;
   public int height;
   public static int VERTICAL   = 0; 
   public static int HORIZONTAL = 1;
   public static int DIRECTION = VERTICAL;
}
