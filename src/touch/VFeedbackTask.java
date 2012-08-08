package touch;

import javax.media.opengl.GL2;

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
            if (p.state == WCursor.STATE_NOTHING || p.state == WCursor.STATE_HOLD) {
               long diff = System.currentTimeMillis() - p.startTimestamp;
               if (diff >= 360) diff = 360;
               //if (diff < 100) continue;
               double angle = diff*360.0/SSM.HOLD_DELAY;
               
               gl2.glColor4d(0, 0.35, 0.45, 0.5*angle/360.0);
               GraphicUtil.drawArc(gl2, p.x*SSM.windowWidth, (1.0-p.y)*SSM.windowHeight, 0, 13*1.7, 15*1.7, 
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
   } 

   @Override
   public void init(GL2 gl2) {
   }

   @Override
   public void picking(GL2 gl2, float px, float py, float pz) {
      // There shouldn't be any interactions for feedback tasks
   }

}
