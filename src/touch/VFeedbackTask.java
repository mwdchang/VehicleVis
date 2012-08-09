package touch;

import java.util.Iterator;

import javax.media.opengl.GL2;

import model.DCTriple;

import util.GraphicUtil;
import datastore.SSM;

import exec.RenderTask;

////////////////////////////////////////////////////////////////////////////////
// For rendering visual feedbacks in TUIO mode
////////////////////////////////////////////////////////////////////////////////
public class VFeedbackTask implements RenderTask {

   @Override
   public void render(GL2 gl2) {
      if (SSM.useTUIO == false) return;
      ////////////////////////////////////////////////////////////////////////////////
      // Draw any active touch points
      ////////////////////////////////////////////////////////////////////////////////
      gl2.glDisable(GL2.GL_LIGHTING);
      gl2.glDisable(GL2.GL_TEXTURE_2D);
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      GraphicUtil.setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight, -10, 10);
      synchronized(SSM.touchPoint) {
         
         for (WCursor p : SSM.touchPoint.values()) {
            gl2.glColor4d(0, 0.35, 0.45, 0.12);
            for (int i = 0; i < 10; i++) {
               GraphicUtil.drawPie(gl2, p.x*SSM.windowWidth, (1.0-p.y)*SSM.windowHeight, 0, (i+1)*1.7, 0, 360, 36);   
            }
            // Start counting
            if (p.element == SSM.ELEMENT_LENS) continue;
            if (p.element == SSM.ELEMENT_LENS_HANDLE) continue;
            if (p.element == SSM.ELEMENT_LENS_RIM) continue; 
            if (p.element == SSM.ELEMENT_MANUFACTURE_SCROLL) continue;
            if (p.element == SSM.ELEMENT_MAKE_SCROLL) continue;
            if (p.element == SSM.ELEMENT_MODEL_SCROLL) continue;
            if (p.element == SSM.ELEMENT_YEAR_SCROLL) continue;
            if (p.element == SSM.ELEMENT_CMANUFACTURE_SCROLL) continue;
            if (p.element == SSM.ELEMENT_CMAKE_SCROLL) continue;
            if (p.element == SSM.ELEMENT_CMODEL_SCROLL) continue;
            if (p.element == SSM.ELEMENT_CYEAR_SCROLL) continue;
            if (p.element == SSM.ELEMENT_FILTER)continue; 
            
            if (p.state == WCursor.STATE_NOTHING || p.state == WCursor.STATE_HOLD) {
               long diff = System.currentTimeMillis() - p.startTimestamp;
               if (diff >= 360) diff = 360;
               //if (diff < 100) continue;
               double angle = diff*360.0/SSM.HOLD_DELAY;
               
               gl2.glColor4d(0, 0.35, 0.45, 0.5*angle/360.0);
               GraphicUtil.drawArc(gl2, p.x*SSM.windowWidth, (1.0-p.y)*SSM.windowHeight, 9.9, 
                     diff==360?13*1.7:15*1.7, diff==360?15*1.7:17*1.7, 
                     0, angle, 36);   
            }

            // Trail
            int c = 0;
            for (int idx=(p.points.size()-1); idx > 0; idx -=2) {
               c++;   
               if (c > 15) break;
               if (idx < 0) break;
               GraphicUtil.drawPie(gl2, p.points.elementAt(idx).getX()*SSM.windowWidth, (1.0-p.points.elementAt(idx).getY())*SSM.windowHeight, 0, 5, 0, 360, 10);   
            }
         }
      } // end synchronize
      
      // Draw wait markers
      if (SSM.waitMarker != null) {
         gl2.glColor4d(1, 1, 1, 0.6*SSM.waitMarker.z/20.0);
         GraphicUtil.drawPie(gl2, SSM.waitMarker.x, SSM.waitMarker.y, 9.9, 
               33, 0, 360, 36);
         gl2.glColor4d(0, 0, 0, 0.6*SSM.waitMarker.z/20.0);
         GraphicUtil.drawArc(gl2, SSM.waitMarker.x, SSM.waitMarker.y, 9.9, 
               30, 33, 0, 360, 36, 1);
         
         GraphicUtil.drawArc(gl2, SSM.waitMarker.x, SSM.waitMarker.y, 9.9, 
               0, 5, 0, 360, 36, 1);
         
         gl2.glLineWidth(4.0f);
         gl2.glBegin(GL2.GL_LINES);
            gl2.glVertex3d(SSM.waitMarker.x, SSM.waitMarker.y, 9.9);
            gl2.glVertex3d(SSM.waitMarker.x, SSM.waitMarker.y+25, 9.9);
            
            gl2.glVertex3d(SSM.waitMarker.x, SSM.waitMarker.y, 9.9);
            gl2.glVertex3d(SSM.waitMarker.x+15, SSM.waitMarker.y, 9.9);
         gl2.glEnd();
         gl2.glLineWidth(1.0f);
         SSM.waitMarker.z -= 1;
         if ( SSM.waitMarker.z <= 0) SSM.waitMarker = null;
      }      
      
      // Draw the points that are no longer invalid
      synchronized( SSM.invalidPoint ) {
         for (int i=0; i < SSM.invalidPoint.size(); i++) {
            gl2.glColor4d(0, 0.35, 0.45, 0.5);
            SSM.invalidPoint.elementAt(i).z -= 0.5;
            DCTriple t = SSM.invalidPoint.elementAt(i);
            GraphicUtil.drawArc(gl2, t.x, t.y, 9.9, 
               t.z, t.z+1, 0, 360, 36, 1);
         }
         
         Iterator<DCTriple> iter = SSM.invalidPoint.iterator();
         while (iter.hasNext()) {
            if (iter.next().z <= 0) iter.remove();   
         }
         
      }
   } 

   @Override
   public void init(GL2 gl2) {
   }

   @Override
   public void picking(GL2 gl2, float px, float py, float pz) {
      // There shouldn't be any interactions for feedback tasks
   }

}
