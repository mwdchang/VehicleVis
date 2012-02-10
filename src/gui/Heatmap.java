package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import javax.media.opengl.GL2;

import datastore.CacheManager;
import datastore.SSM;
import datastore.SchemeManager;

import model.DCColour;

import util.DCUtil;
import util.TextureFont;
import util.TextureFont.Mark;

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
      if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(this.id)) {
         gl2.glLineWidth(2.0f);
         renderBorder(gl2, SchemeManager.colour_blue, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      } else if (SSM.instance().relatedList != null && SSM.instance().relatedList.contains(this.id))  {
         gl2.glLineWidth(2.0f);
         renderBorder(gl2, SchemeManager.colour_related, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      }        
   }
   
   
   public void renderSelected(GL2 gl2) {
      // Hack slow
      calcMaxMin();
      
      // Draw a shaded grey as the back drop
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      if (active) {
         gl2.glDisable(GL2.GL_BLEND);
         renderBorder(gl2, DCColour.fromDouble(0.9, 0.9, 0.9, 0.2), GL2.GL_FILL);
      } else {
         gl2.glEnable(GL2.GL_BLEND);
         gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
         renderBorder(gl2, DCColour.fromDouble(0.4, 0.4, 0.4, 0.6), GL2.GL_FILL);
      }
      
      int origin = CacheManager.instance().timeLineStartYear;
      int sYear  = SSM.instance().startYear;
      int eYear  = SSM.instance().endYear; 
      int sMonth = SSM.instance().startMonth;
      int eMonth = SSM.instance().endMonth;
      
      
      // If < 0 then something bad happened...
      int start = sYear - origin; 
      int end   = (eYear - origin)+1;
      
      blockWidth  = width / (1+eMonth-sMonth);
      blockHeight = (height-labelBuffer) / (1+eYear-sYear);
      
      for (int i=start; i < end; i++) {
         float tmpX = 0;
         float tmpY = i-start;
         for (int j=sMonth; j <= eMonth; j++) {
            float v = data[12*i + j];      
            float gMax = CacheManager.instance().monthMaximum.elementAt(12*i+j);
            
            // Tool Tip !
            if ( DCUtil.between(SSM.instance().mouseX, anchorX+tmpX*blockWidth, anchorX+(1+tmpX)*blockWidth)) {
               if (DCUtil.between(SSM.instance().windowHeight - SSM.instance().mouseY, anchorY+height-(1+tmpY)*blockHeight-labelBuffer, anchorY+height-tmpY*blockHeight-labelBuffer)) {
                  DCTip.visible = true;
                  DCTip.clear();
                  //DCTip.addText("Year:" +  (int)((tmpY) + SSM.instance().startYear));
                  //DCTip.addText("Month:" + (int)((1+tmpX) + SSM.instance().startMonth));
                  int cYear  = (int)((tmpY) + SSM.instance().startYear); 
                  int cMonth = (int)((1+tmpX) + SSM.instance().startMonth);
                  
                  DCTip.addText("Time:" + DCTip.translateTable.get(cMonth+"") + "-" + cYear); 
                  DCTip.addText("Value:" + (int)v);
                  DCTip.setTip( SSM.instance().mouseX, 
                        (SSM.instance().windowHeight-SSM.instance().mouseY), 
                        SSM.instance().windowWidth, SSM.instance().windowHeight);   
               }
            }
            
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glColor4fv(SchemeManager.instance().getColour(0, v, gMax).toArray(), 0);
               gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-tmpY*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-tmpY*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
            gl2.glEnd();
            gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
            gl2.glBegin(GL2.GL_QUADS);
               gl2.glColor4fv(SchemeManager.silhouette_default.toArray(), 0);
               gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-tmpY*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-tmpY*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + (1+tmpX)*blockWidth, anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
               gl2.glVertex2f(anchorX + tmpX*blockWidth,     anchorY+height-(1+tmpY)*blockHeight-labelBuffer);
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
      float startX = SSM.instance().startMonth;
      float endX = SSM.instance().endMonth+1;
      
      float startY = SSM.instance().startYear - CacheManager.timeLineStartYear;
      float endY   = 1+SSM.instance().endYear - CacheManager.timeLineStartYear;
      
      float tmpY = (float) Math.floor(data.length/12.0f);
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
      gl2.glColor4fv(SchemeManager.colour_blue.toArray(), 0);
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
         tf.addMark(label, labelColour, new Font("Consolas", Font.PLAIN, 11), 1, height-labelBuffer+5);
         //tf.addMark(label, labelColour, new Font("Arial", Font.PLAIN, 12), 150, 2);
         //System.out.println("Setting sparkline label: " + label);
         
         /*
         Hashtable<String, String> translation = DCUtil.getMonthTranslationTable();
         int layers = (int) Math.floor(data.length/12.0f);
         for (int i=0; i < 12; i++) {
            String s = (i+1) +"";
            //tf.addMark( translation.get(s).substring(0,1), labelColour, smallFont, i*blockWidth, height - layers*blockHeight - 10);   
            tf.addMark(translation.get(s).substring(0,1), labelColour, smallFont, i*blockWidth, layers*blockHeight+10, false);
         }
         */
         
         //int yearStart = SSM.instance().startYear;
         //int yearEnd   = SSM.instance().endYear;
         /*
         int yearStart = CacheManager.timeLineStartYear;
         int yearEnd = CacheManager.timeLineEndYear;
         for (int i=0; i < (yearEnd-yearStart)+1; i++) {
            if (i == 0 || i == (yearEnd-yearStart)) {
               String s = (yearStart+i)+"";   
               tf.addMark(s, labelColour, smallFont, 12*blockWidth, height-(i+1)*blockHeight-labelBuffer, false);
            }
         }
         */
      }
      
      
      // Also add the axis labels for the heat map
      //int layers = (int) Math.floor(data.length/12.0f);
      //for (int i=0; i < layers; i++) {
      //   String s = (i+1)+"";
      //   tf.addMark( translation.get(s), labelColour, smallFont, 150, height - i*blockHeight);   
      //}
   }   
   
   public void renderBorder(GL2 gl2, DCColour c, int mode) {
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, mode);
      gl2.glColor4fv(c.toArray(), 0);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2f(anchorX+0.0f, anchorY+0.0f);
         gl2.glVertex2f(anchorX+width, anchorY+0.0f);
         gl2.glVertex2f(anchorX+width, anchorY+height);
         gl2.glVertex2f(anchorX+0.0f,  anchorY+height);
      gl2.glEnd();       
      gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
   }   
   public void renderBorder(GL2 gl2) {
      renderBorder(gl2, DCColour.fromDouble(0.5, 0.5, 0.5, 0.5), GL2.GL_FILL);
      /*
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2f(anchorX, anchorY);
         gl2.glVertex2f(anchorX+width, anchorY);
         gl2.glVertex2f(anchorX+width, anchorY+height);
         gl2.glVertex2f(anchorX, anchorY+height);
      gl2.glEnd();;   
      */
   }
   
   
   // Not used
   public void createSegment(int seg) { }
   public void setHighlight(short data[]){}
   public void renderImmediate(GL2 gl2) {}
   
   
   //public static Font  smallFont  = new Font("Courier", Font.PLAIN, 5);
   //public static Font  smallFont  = new Font("Tahoma", Font.PLAIN, 8);
   public static Font  smallFont  = new Font("Consolas", Font.PLAIN, 10);
   public float blockHeight = 3;
   public float blockWidth  = 8;
 
   public float labelBuffer = 16.0f;

}

