package gui;

import java.awt.Font;
import java.util.Enumeration;
import javax.media.opengl.GL2;

import datastore.CacheManager;
import datastore.Const;
import datastore.SSM;
import datastore.SchemeManager;

import model.DCColour;
import model.DCTriple;

import util.DCUtil;

/////////////////////////////////////////////////////////////////////////////////
// Implements a heatmap chart
/////////////////////////////////////////////////////////////////////////////////
public class Heatmap extends ComponentChart {

   public Heatmap() {
   }
   
   public Heatmap(float _data[], float _w, float _h) {
      data = _data;
      width = _w;
      height = _h;
   }
   
   
   public void render(GL2 gl2) {
      renderSelected(gl2);   
      
      
      // Draw the selected/related components
      if (SSM.selectedGroup.size() > 0 && SSM.selectedGroup.contains(this.id)) {
         gl2.glLineWidth(3.0f);
         renderBorder(gl2, SchemeManager.selected, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      } else if (SSM.relatedList != null && SSM.relatedList.contains(this.id))  {
         gl2.glLineWidth(1.0f);
         //renderBorder(gl2, SchemeManager.related, GL2.GL_LINE);
         renderBorder(gl2, SchemeManager.sparkline_guideline, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      } else {
         if (active) {
            renderBorder(gl2, SchemeManager.sparkline_guideline, GL2.GL_LINE);
         } else {
            renderBorder(gl2, DCColour.fromDouble(0.8, 0.8, 0.8, 0.8), GL2.GL_LINE);
         }
      }
   }
   
   
   public void renderSelected(GL2 gl2) {
      // Hack slow
      calcMaxMin();
      
      // Draw a shaded grey as the back drop
      /*
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      if (active) {
         //gl2.glDisable(GL2.GL_BLEND);
         gl2.glEnable(GL2.GL_BLEND);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         renderBorder(gl2, DCColour.fromDouble(0.9, 0.9, 0.9, 0.5), GL2.GL_FILL);
         //renderBorder(gl2, DCColour.fromDouble(0.0, 0.8, 0.0, 0.2), GL2.GL_FILL);
      } else {
         gl2.glEnable(GL2.GL_BLEND);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         renderBorder(gl2, DCColour.fromDouble(0.4, 0.4, 0.4, 0.6), GL2.GL_FILL);
      }
      */
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         
      int origin = CacheManager.timeLineStartYear;
      int sYear  = SSM.startYear;
      int eYear  = SSM.endYear; 
      int sMonth = SSM.startMonth;
      int eMonth = SSM.endMonth;
      
      
      // If < 0 then something bad happened...
      int start = sYear - origin; 
      int end   = (eYear - origin)+1;
      
      blockWidth  = width / (1+eMonth-sMonth);
      blockHeight = (height-labelBuffer) / (1+eYear-sYear);
      
      // Hack for max and min
      maxValue = Float.MIN_VALUE;
      minValue = Float.MAX_VALUE;
      for (int i=start; i < end; i++) {
         for (int j=sMonth; j <= eMonth; j++) {
            if ( data[12*i+j] > maxValue) maxValue = data[12*i+j];
            if ( c_data[12*i+j] > c_maxValue) c_maxValue = c_data[12*i+j];
         }
      }
      
      for (int i=start; i < end; i++) {
         float tmpX = 0;
         float tmpY = i-start;
         for (int j=sMonth; j <= eMonth; j++) {
            float v = data[12*i + j];      
            float c_v = c_data[12*i + j];
            
            float max = 0;
            float c_max = 0;
            
            switch(SSM.chartMode) {
               case SSM.CHART_MODE_BY_MONTH_MAX: {
                  // month_score % max_month
                  max = CacheManager.instance().monthMaximum.elementAt(12*i+j);
                  c_max = CacheManager.instance().c_monthMaximum.elementAt(12*i + j);
                  break;
               }
               case SSM.CHART_MODE_BY_COMPONENT_MAX: {
                  // month_score % max_component
                  max = this.maxValue;
                  c_max = this.c_maxValue;
                  break;
               }
               case SSM.CHART_MODE_BY_GLOBAL_MAX: {
                  max = SSM.instance().segmentMax;
                  c_max = 0; // segMax includes comparative sums
                  break;
               }
            }
            
            
            Enumeration<Long> enumeration = SSM.hoverPoints.keys();
            while(enumeration.hasMoreElements()) {
               Long session = enumeration.nextElement();      
               DCTriple point = SSM.hoverPoints.get(session);
               
               if ( DCUtil.between(point.x, anchorX+tmpX*blockWidth, anchorX+(1+tmpX)*blockWidth)) {
                  if (DCUtil.between(SSM.windowHeight - point.y, anchorY+height-(1+tmpY)*blockHeight-labelBuffer, anchorY+height-tmpY*blockHeight-labelBuffer)) {
                     if (SSM.tooltips.get(session) == null) {
                        DCTip tip = new DCTip();   
                        tip.init(gl2);
                        SSM.tooltips.put(session, tip);
                        System.out.println("debug");
                     }
                     DCTip  tip = SSM.tooltips.get(session);
                     tip.visible = true;
                     tip.clear();
                     int cYear  = (int)((tmpY) + SSM.startYear); 
                     int cMonth = (int)((1+tmpX) + SSM.startMonth);
                     tip.addText("Time:" + DCTip.translateTable.get(cMonth+"") + "-" + cYear); 
                     if (SSM.useComparisonMode == true) {
                        tip.addText("Value:" + (int)(v));
                        tip.addText("Value:" + (int)(c_v));
                     } else {
                        tip.addText("Value:" + (int)v);
                     }
                     tip.setTip( point.x,
                           SSM.windowHeight-point.y,
                           SSM.windowWidth, SSM.windowHeight);   
                     tip.xIndex = i;
                     tip.yIndex = j;
                  }
               }
            } // end while
            
            
            // Tool Tip !
            /*
            if ( DCUtil.between(SSM.mouseX, anchorX+tmpX*blockWidth, anchorX+(1+tmpX)*blockWidth)) {
               if (DCUtil.between(SSM.windowHeight - SSM.mouseY, anchorY+height-(1+tmpY)*blockHeight-labelBuffer, anchorY+height-tmpY*blockHeight-labelBuffer)) {
                  DCTip.visible = true;
                  DCTip.clear();
                  int cYear  = (int)((tmpY) + SSM.instance().startYear); 
                  int cMonth = (int)((1+tmpX) + SSM.instance().startMonth);
                  
                  DCTip.addText("Time:" + DCTip.translateTable.get(cMonth+"") + "-" + cYear); 
                  if (SSM.instance().useComparisonMode == true) {
                     DCTip.addText("Value:" + (int)(v));
                     DCTip.addText("Value:" + (int)(c_v));
                  } else {
                     DCTip.addText("Value:" + (int)v);
                  }
                  DCTip.setTip( SSM.instance().mouseX, 
                        (SSM.windowHeight-SSM.instance().mouseY), 
                        SSM.windowWidth, SSM.instance().windowHeight);   
                  
                  SSM.instance().selectedX = i;
                  SSM.instance().selectedY = j;
               }
            }
            */
            
            
            // Render an out line to separate the grids
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl2.glBegin(GL2.GL_QUADS);
               for (DCTip tip : SSM.tooltips.values()) {
                  if (tip.visible == true && i==tip.xIndex && j==tip.yIndex){
                     gl2.glLineWidth(2.0f);
                     gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
                  } else {
                     gl2.glLineWidth(1.0f);
                     if (SSM.useComparisonMode == true) {
                        if (v > c_v) {
                           gl2.glColor4fv(SchemeManager.comp_1.toArray(), 0);
                        } else if (v < c_v) {
                           gl2.glColor4fv(SchemeManager.comp_2.toArray(), 0);
                        } else {
                           gl2.glColor4fv(SchemeManager.silhouette_default.adjustAlpha(0.5f).toArray(), 0);
                        }
                     } else {
                        gl2.glColor4fv(SchemeManager.silhouette_default.adjustAlpha(0.5f).toArray(), 0);
                     }
                  } // end if tip visible
               }
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth),     (int)(anchorY+height-tmpY*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth), (int)(anchorY+height-tmpY*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth), (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth),     (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer));
            gl2.glEnd();
            
            // Render the individual grid
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl2.glBegin(GL2.GL_QUADS);
               if (SSM.useComparisonMode == true) {
                  gl2.glColor4fv(SchemeManager.instance().getColour(0, v+c_v, max+c_max).toArray(), 0);
               } else {
                  gl2.glColor4fv(SchemeManager.instance().getColour(0, v, max).toArray(), 0);
               }
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+1),     (int)(anchorY+height-tmpY*blockHeight-labelBuffer-1));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-1), (int)(anchorY+height-tmpY*blockHeight-labelBuffer-1));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-1), (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+1));
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+1),     (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+1));
            gl2.glEnd();
            tmpX++;
         }
      }
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
   }
   
   
   
   public void renderAll(GL2 gl2) {
      // Hack slow
      calcMaxMin();
      
      // Draw a shaded grey as the back drop
      renderBorder(gl2, DCColour.fromDouble(0.9, 0.9, 0.9, 0.3), GL2.GL_FILL);
      
      
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      gl2.glDisable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_QUADS);
      for (int i=0; i < data.length; i++) {
         float tmpX = i % 12;
         float tmpY = (float) Math.floor((float)i/12.0f);
         float gMax = CacheManager.instance().monthMaximum.elementAt(i);
         
         //gl2.glColor4fv(SchemeManager.instance().getColour(0, data[i], maxValue).toArray(), 0);
         gl2.glColor4fv(SchemeManager.instance().getColour(0, data[i], gMax).toArray(), 0);
         
         gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-tmpY*blockHeight-labelBuffer);
         gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-tmpY*blockHeight-labelBuffer);
         gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
         gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
      }
      gl2.glEnd();
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      
      
      
      // Draw the selected time period
      float startX = SSM.startMonth;
      float endX = SSM.endMonth+1;
      
      float startY = SSM.startYear - CacheManager.timeLineStartYear;
      float endY   = 1+SSM.endYear - CacheManager.timeLineStartYear;
      
      float tmpY = (float) Math.floor(data.length/12.0f);
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
      gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2f(anchorX + startX*blockWidth, anchorY+height - startY*blockHeight - labelBuffer);
         gl2.glVertex2f(anchorX + endX*blockWidth,   anchorY+height - startY*blockHeight - labelBuffer);
         gl2.glVertex2f(anchorX + endX*blockWidth,   anchorY+height - endY*blockHeight - labelBuffer);
         gl2.glVertex2f(anchorX + startX*blockWidth, anchorY+height - endY*blockHeight - labelBuffer);
      gl2.glEnd();
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      
      
      
      
      // Draw the selected/related components
      /*
      if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(this.id)) {
         gl2.glLineWidth(1.5f);
         renderBorder(gl2, SchemeManager.colour_blue, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      } else if (SSM.instance().relatedList != null && SSM.instance().relatedList.contains(this.id))  {
         gl2.glLineWidth(1.5f);
         renderBorder(gl2, SchemeManager.colour_related, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      }      
      */
      
      
   }
   
   
   public void setLabel(String label) {
      if (tf.marks.size() == 0 || ! tf.marks.elementAt(0).str.equalsIgnoreCase(label)) {
         tf.clearMark();
         //tf.addMark(label, labelColour, new Font("Consolas", Font.PLAIN, 11), 1, height-labelBuffer+5);
         if (active) 
            tf.addMark(label, labelColour, smallFont, 1, height-labelBuffer+5);
         else 
            tf.addMark(label, labelColourInactive, smallFont, 1, height-labelBuffer+5);
      }
   }   
   
   
   public void renderBorder(GL2 gl2, DCColour c, int mode) {
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, mode);
      gl2.glColor4fv(c.toArray(), 0);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2i((int)(anchorX+0.0f-1),  (int)(anchorY+0.0f-1));
         gl2.glVertex2i((int)(anchorX+width+1), (int)(anchorY+0.0f-1));
         gl2.glVertex2i((int)(anchorX+width+1), (int)(anchorY+height+1));
         gl2.glVertex2i((int)(anchorX+0.0f-1),  (int)(anchorY+height+1));
      gl2.glEnd();       
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
   }   
   public void renderBorder(GL2 gl2) {
      renderBorder(gl2, DCColour.fromDouble(0.5, 0.5, 0.5, 0.5), GL2.GL_FILL);
   }
   
   
   // Not used
   public void createSegment(int seg) { }
   public void setHighlight(short data[]){}
   public void renderImmediate(GL2 gl2) {}
   
   
   public static Font  smallFont  = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 14);
   public float blockHeight = 3;
   public float blockWidth  = 8;
 
   public static float labelBuffer = 20.0f;
   
   

}

