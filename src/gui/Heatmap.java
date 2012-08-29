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
      
      this.labelBuffer = getHardFontHeight( smallFont );
   }
   
   
   // hack
   public float getHardFontHeight(Font f) {
      float size = f.getSize();
      return size * 1.02f;
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
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Hack for component max and min
      // The max for non-comparison view is just the max of data[]
      // The max for comparison is of data[i]+c_data[i]
      //
      // Consider vehicle 1 : [1, 10, 100]
      //          vehicle 2 : [100, 10, 1]
      // 
      // The component max for non-comparison is 100,
      // while the comparison max is 101, NOT 200
      ////////////////////////////////////////////////////////////////////////////////
      maxValue = Float.MIN_VALUE;
      minValue = Float.MAX_VALUE;
      for (int i=start; i < end; i++) {
         for (int j=sMonth; j <= eMonth; j++) {
            if (SSM.useComparisonMode == true) {
               if ( data[12*i+j] + c_data[12*i+j] > maxValue) maxValue = (c_data[12*i+j] + data[12*i+j]);
            } else {
               if ( data[12*i+j] > maxValue) maxValue = data[12*i+j];
            }
         }
      }
      
      for (int i=start; i < end; i++) {
         float tmpX = 0;
         float tmpY = i-start;
         for (int j=sMonth; j <= eMonth; j++) {
            float v = data[12*i + j];      
            float vOrig = data[12*i + j];      
            
            float c_v = c_data[12*i + j];
            float c_vOrig = c_data[12*i + j];
            
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
                  c_max = 0;
                  //c_max = this.c_maxValue;
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
                        tip.addText("# Occurrence: " + (int)(c_v), SchemeManager.comp_2);
                        tip.addText("# Occurrence: " + (int)(v), SchemeManager.comp_1);
                     } else {
                        tip.addText("# Occurrence: " + (int)v);
                     }
                     tip.setTip( point.x,
                           SSM.windowHeight-point.y,
                           SSM.windowWidth, SSM.windowHeight);   
                     tip.xIndex = i;
                     tip.yIndex = j;
                  }
               }
            } // end while
            
            
            
            // Render an out line to separate the grids
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glLineWidth(1.0f);
               
               if (SSM.useComparisonMode == true) {
                  // Hack
                  float v1max = 0, v2max = 0; 
                  for (Integer val : CacheManager.instance().groupOccurrence.values()) v1max += val;
                  for (Integer val : CacheManager.instance().c_groupOccurrence.values()) v2max += val;                  
                  v /= v1max;
                  c_v /= v2max;
                  
                  
                  if (v > c_v) {
                     float alpha = 0.4f + 0.6f*(v-c_v)/v;
                     gl2.glColor4f( SchemeManager.comp_1.r, SchemeManager.comp_1.g, SchemeManager.comp_1.b, alpha);
                  } else if (v < c_v) {
                     float alpha = 0.4f + 0.6f*(c_v-v)/c_v;
                     gl2.glColor4f( SchemeManager.comp_2.r, SchemeManager.comp_2.g, SchemeManager.comp_2.b, alpha);
                  } else {
                     gl2.glColor4fv(SchemeManager.silhouette_default.adjustAlpha(0.5f).toArray(), 0);
                  }
               } else {
                  gl2.glColor4fv(SchemeManager.silhouette_default.adjustAlpha(0.5f).toArray(), 0);
               }
               
               for (DCTip tip : SSM.tooltips.values()) {
                  if (tip.visible == true && i==tip.xIndex && j==tip.yIndex){
                     gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
                     break;
                  }
               } // end for
              
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth),     (int)(anchorY+height-tmpY*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth), (int)(anchorY+height-tmpY*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth), (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer));
               gl2.glVertex2i((int)(anchorX + tmpX*blockWidth),     (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer));
            gl2.glEnd();
            
            // Render the individual grid
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl2.glDisable(GL2.GL_BLEND);
            gl2.glBegin(GL2.GL_QUADS);
               if (SSM.useComparisonMode == true) {
                  gl2.glColor4fv(SchemeManager.instance().getColour(0, vOrig+c_vOrig, max+c_max).toArray(), 0);
                  gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+2),     (int)(anchorY+height-tmpY*blockHeight-labelBuffer-2));
                  gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-2), (int)(anchorY+height-tmpY*blockHeight-labelBuffer-2));
                  gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-2), (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+2));
                  gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+2),     (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+2));
               } else {
                  gl2.glColor4fv(SchemeManager.instance().getColour(0, vOrig, max).toArray(), 0);
                  gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+1),     (int)(anchorY+height-tmpY*blockHeight-labelBuffer-1));
                  gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-1), (int)(anchorY+height-tmpY*blockHeight-labelBuffer-1));
                  gl2.glVertex2i((int)(anchorX + (1+tmpX)*blockWidth-1), (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+1));
                  gl2.glVertex2i((int)(anchorX + tmpX*blockWidth+1),     (int)(anchorY+height-(1+tmpY)*blockHeight-labelBuffer+1));
               }
            gl2.glEnd();
            gl2.glEnable(GL2.GL_BLEND);
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
      //if (tf.marks.size() == 0 || ! tf.marks.elementAt(0).str.equalsIgnoreCase(label)) {
         tf.clearMark();
         //tf.addMark(label, labelColour, new Font("Consolas", Font.PLAIN, 11), 1, height-labelBuffer+5);
         if (active) 
            tf.addMark(label, labelColour, smallFont, 1, height-labelBuffer+1);
         else 
            tf.addMark(label, labelColourInactive, smallFont, 1, height-labelBuffer+1);
      //}
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
   
   
   public static Font  smallFont  = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 16);
   public float blockHeight = 3;
   public float blockWidth  = 8;
 

   
   

}

