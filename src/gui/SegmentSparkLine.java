package gui;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import datastore.SSM;
import datastore.SchemeManager;

import util.FontRenderer;
import util.TextureFont;

import model.DCColour;

/////////////////////////////////////////////////////////////////////////////////
// Specialized SparkLine implementation that allows parts to be highlighted
/////////////////////////////////////////////////////////////////////////////////
public class SegmentSparkLine extends ComponentChart {
   
   /*
   public static void main(String args[]) {
      SegmentSparkLine spl = new SegmentSparkLine();
      float data[] = new float[]{ 1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  1.0f, 2.0f, 3.0f, 4.0f, 5.0f,
                                  31.0f};
      spl.data = data;
      spl.createSegment(25);
   }*/
   
   public SegmentSparkLine() {
   }
   
   public SegmentSparkLine(float[] d) {
      data = d;
   }
   
   public SegmentSparkLine(float[] d, float w, float h) {
      data = d;
      width = w;
      height = h;
   }
   
   
   public void setLabel(String label) {
      if (tf.marks.size() == 0 || ! tf.marks.elementAt(0).str.equalsIgnoreCase(label)) {
         tf.clearMark();
         tf.addMark(label, labelColour, new Font("Arial", Font.PLAIN, 12), 2, 2);
         //System.out.println("Setting sparkline label: " + label);
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////  
   // Create a segment based on the 
   ////////////////////////////////////////////////////////////////////////////////  
   public void createSegment(int segment) {
      if (segment > data.length) {
         //System.err.println("No support yet for segment > data.length...");
         segment = data.length;
      }
      
      double dataWidth = (float)data.length / (float)segment;
      double interval = (float)width / (float)(segment); // size of bars
      
      int start = 0;
      double leftover = 0.0f;
      segData = new float[segment];
      
      //System.out.println("Data length : " + data.length);
      
      float max = 0;
      float min = 0; 
      for (int i=0; i < segment; i++) {
         //System.out.println("segment " + i + " " + start + " L=" + leftover);
         
         float dw = (float)dataWidth;
         float value = 0.0f;
         // check leftover
         if (leftover > 0) {
            value += data[start]*leftover;   
            dw -= leftover;
            start ++;
            leftover = 0;
         }
         while (Math.abs(dw) > SSM.instance().EPS) {
            if (dw - 1.0f >= 0) {
               dw -= 1.0f;   
               value +=  data[ start ];
               start ++;
            } else {
               leftover = (1.0f - dw); 
               value += data[start]*dw;
               dw = 0.0f;
            }
            if (start == data.length-1) dw = 0.0f;
         }
         
         //System.out.println(value + " --> " + value/dataWidth + " [" + this.maxValue + "]");
         segData[i] = (float)value/(float)dataWidth;
         if (max < segData[i]) max = segData[i];
         if (min > segData[i]) min = segData[i];
      }
      this.minValue = min;
      this.maxValue = max;
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Assumes that it spans at least one bar
   ////////////////////////////////////////////////////////////////////////////////
   public void renderImmediateWeighted2() {
      int segment = 5;                                   // number of bars
      float interval = (float)width / (float)(segment-1); // size of bars
      
      float dataWidth = (float)data.length / (float)segment;
      System.out.println("Data width : " + dataWidth);
      
      int start = 0;
      float leftover = 0.0f;
      for (int i=0; i < segment; i++) {
         float dw = dataWidth;
         float value = 0.0f;
         
         // check leftover
         if (leftover > 0) {
            //System.out.println("boo");
            value += data[start]*leftover;   
            dw -= leftover;
            start ++;
            leftover = 0;
         }
         while (dw > 0) {
            //System.out.println(dw + " " + value + " " + start);
            if (dw - 1.0f >= 0) {
               dw -= 1.0f;   
               value +=  data[ start ];
               start ++;
            } else {
               leftover = (1.0f - dw); 
               value += data[start]*dw;
               dw = 0.0f;
            }
         }
         //System.out.println("Value is : " + value);
      }
   }
   
   
   
   public void render(GL2 gl2) {
      if (SSM.instance().sparklineMode == 0) {
         renderSegmentLine(gl2);   
      } else {
         renderSegment(gl2);   
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render a sparkline as series of line segments
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSegmentLine(GL2 gl2) {
      if (segData == null) return;
      float interval = (float)width/(float)(segData.length); 
      float d = (float)data.length/(float)segData.length;
      float floor = Math.abs(minValue)/(maxValue-minValue)*height;
      
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_LINES);
      for (int i=0; i < segData.length-1; i++) {
         float val = segData[i] / (maxValue - minValue); 
         float val2 = segData[i+1] / (maxValue - minValue); 
         
         if (highlight[ (int)Math.floor(i*d)] == 1) {
            gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
         } else {
            //gl2.glColor4fv(SchemeManager.sparkline_normal.toArray(), 0);
         	gl2.glColor4fv(colour.toArray(), 0);
         }
         
         gl2.glVertex2d(anchorX+interval * i, anchorY+floor+val*height);     
         gl2.glVertex2d(anchorX+interval * (i+1), anchorY+floor+val2*height);     
         
      }
      gl2.glVertex2d(anchorX, anchorY+floor); 
      gl2.glVertex2d(anchorX+width, anchorY+floor); 
      gl2.glEnd();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render shortened sparkline graph
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSegment(GL2 gl2) {
      if (segData == null) return;
      
      float interval = (float)width/(float)(segData.length); 
      float d = (float)data.length/(float)segData.length;
      
      
      //gl2.glBegin(GL2.GL_LINE_STRIP);
      renderBorder(gl2);
      //gl2.glEnable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_QUADS);
      for (int i=0; i < segData.length; i++) {
         if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(this.id)) {
            if (highlight[ (int)Math.floor(i*d)] == 1) {
               gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
            } else {
               //gl2.glColor4fv(SchemeManager.sparkline_normal.toArray(), 0);
            	gl2.glColor4fv(colour.toArray(), 0);
            }
         } else {
            if (highlight[ (int)Math.floor(i*d)] == 1) {
               gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
            } else {
               //gl2.glColor4fv(SchemeManager.sparkline_normal.toArray(), 0);
            	gl2.glColor4fv(colour.toArray(), 0);
            }
         }
         gl2.glVertex2d(anchorX+interval * i, anchorY+(segData[i]/maxValue)*height);     
         gl2.glVertex2d(anchorX+interval * i, anchorY+0);
         gl2.glVertex2d(anchorX+interval * (i+1), anchorY+0);
         gl2.glVertex2d(anchorX+interval * (i+1), anchorY+(segData[i]/maxValue)*height);     
      }
      gl2.glEnd();
      
      //renderBorder(gl2);
      if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(this.id)) {
         gl2.glLineWidth(1.5f);
         renderBorder(gl2, SchemeManager.selected, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      } else if (SSM.instance().relatedList != null && SSM.instance().relatedList.contains(this.id))  {
         gl2.glLineWidth(1.5f);
         renderBorder(gl2, SchemeManager.related, GL2.GL_LINE);
         gl2.glLineWidth(0.5f);
      }
      
   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders a backdrop
   ////////////////////////////////////////////////////////////////////////////////
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
   }
   
   
   
   @Override
   public void renderImmediate(GL2 gl2) {
      float interval = width / (float)(data.length-1);   
      
      gl2.glBegin(GL2.GL_LINE_STRIP);
      for (int i=0; i < data.length; i++) {
         float h = data[i]/maxValue*height;
         if (highlight != null && highlight[i] == 1) {
            gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
         } else {
            gl2.glColor4fv(SchemeManager.unselected.toArray(), 0);
         }
         gl2.glVertex2f( i*interval, h);
         //gl2.glVertex2f( i*interval, 0);
      }
      gl2.glEnd();
      
      
      gl2.glBegin(GL2.GL_LINE_LOOP);
         gl2.glVertex2f(0.0f, 0.0f);
         gl2.glVertex2f(width, 0.0f);
         gl2.glVertex2f(width, height);
         gl2.glVertex2f(0.0f,  height);
      gl2.glEnd();      
      
   }
   
   
   public void setHighlight(short[] s) {
      highlight = s;   
   }
   
   
   
   
   // Which point to highlight 
   public int sampleRate = 1;
   
   public short highlight[];
   //public DCColour hColour = SchemeManager.sparkline_hilight; 
   //public DCColour nColour = SchemeManager.sparkline_normal;
   public float segData[];
   
   //public DCColour colour = new DCColour();
   
   
   // For labels and font
   //public static Color labelColour = Color.BLACK;
   //public static Font  labelFont  = new Font("Arial", Font.PLAIN, 12);
   //public TextureFont tf = new TextureFont();
   
   
   
}
