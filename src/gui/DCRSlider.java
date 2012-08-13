package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import javax.media.opengl.GL2;

import util.DCUtil;
import util.TextureFont;

import datastore.SSM;
import datastore.SchemeManager;

import model.DCColour;

/////////////////////////////////////////////////////////////////////////////////
// A stand-alone DIY slider widget
// with 'scented' bars showing the aggregated value at each slider point.
/////////////////////////////////////////////////////////////////////////////////
public class DCRSlider {
   
   public DCRSlider() {
      interval = 25.0; // Gets over-written if using FilterTask
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render sub graph
   ////////////////////////////////////////////////////////////////////////////////
   public void renderData(GL2 gl2, int[] data) {
      gl2.glColor4d(0.0, 0.0, 0.0, 1.0);
      gl2.glPushMatrix();
      for (int i=0; i < data.length; i++) {
         double value = data[i] * height/maxValue;
         gl2.glBegin(GL2.GL_LINES);
            gl2.glVertex2d((float)i*interval+anchorX, anchorY+value);  
            gl2.glVertex2d((float)i*interval+anchorX+interval, anchorY+value);
         gl2.glEnd();
      }
      gl2.glPopMatrix();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the entire data range
   ////////////////////////////////////////////////////////////////////////////////
   public void render(GL2 gl2) {
      gl2.glEnable(GL2.GL_BLEND);
      gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      //gl2.glDisable(GL2.GL_DEPTH_TEST);
      //gl2.glDisable(GL2.GL_TEXTURE2);
      renderMarkerLow(gl2);
      renderMarkerHigh(gl2);
      gl2.glPushMatrix();
      for (int i=0; i < data.length; i++) {
         if (i >= lowIdx && i <= highIdx) {
            if (barColour == null) {
               gl2.glColor4fv( SchemeManager.selected.toArray(), 0);
            } else {
               gl2.glColor4fv(barColour.toArray(), 0);
            }
         } else {
               gl2.glColor4fv( SchemeManager.unselected.toArray(), 0);
         }
         //renderQuad(gl2, data[i], null, 1, i);   
         renderQuadInterpolate(gl2, barHeight[i], null, 1, i);   
      }
      gl2.glPopMatrix();
      
      // Render the element labels
      //tf.render(gl2);
      
      // Debugging infomration for space layout alignment
      if (SSM.instance().useGuide == true)  {
         gl2.glBegin(GL2.GL_LINE_LOOP);
            gl2.glColor4d(0.5, 0.5, 0.5, 0.5);
            gl2.glVertex3d(anchorX, anchorY, 0);
            gl2.glVertex3d(anchorX+data.length*interval, anchorY, 0);
            gl2.glVertex3d(anchorX+data.length*interval, anchorY+height, 0);
            gl2.glVertex3d(anchorX, anchorY+height, 0);
         gl2.glEnd();
      }
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render a single quad
   // Note: DCColour c is not used
   ////////////////////////////////////////////////////////////////////////////////
   public void renderQuad(GL2 gl2, DCPair p, DCColour c, int style, int index) {
      double value = p.value;
      float idx = (float)index;
      
      value = value*height/maxValue;
      
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2d(idx*interval+anchorX, anchorY); 
         gl2.glVertex2d(idx*interval+interval+anchorX, anchorY);  
         gl2.glVertex2d(idx*interval+anchorX+interval, anchorY+value);
         gl2.glVertex2d(idx*interval+anchorX, anchorY+value);
      gl2.glEnd();
      
      if (style == 1) {
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
         gl2.glColor4d( 0, 0, 0, 1);
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex2d(idx*interval+anchorX, anchorY); 
            gl2.glVertex2d(idx*interval+anchorX+interval, anchorY);  
            gl2.glVertex2d(idx*interval+anchorX+interval, anchorY+value);
            gl2.glVertex2d(idx*interval+anchorX, anchorY+value);
         gl2.glEnd();
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      }
      
   }
   
   public void renderQuadInterpolate(GL2 gl2, double bHeight, DCColour c, int style, int index) {
      float idx = (float)index;
      
      
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2d(idx*interval+anchorX, anchorY); 
         gl2.glVertex2d(idx*interval+interval+anchorX, anchorY);  
         gl2.glVertex2d(idx*interval+anchorX+interval, anchorY+bHeight);
         gl2.glVertex2d(idx*interval+anchorX, anchorY+bHeight);
      gl2.glEnd();
      
      if (style == 1) {
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
         gl2.glColor4d( 0, 0, 0, 1);
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex2d(idx*interval+anchorX, anchorY); 
            gl2.glVertex2d(idx*interval+anchorX+interval, anchorY);  
            gl2.glVertex2d(idx*interval+anchorX+interval, anchorY+bHeight);
            gl2.glVertex2d(idx*interval+anchorX, anchorY+bHeight);
         gl2.glEnd();
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
      }
      
   }
  
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders the left marker
   ////////////////////////////////////////////////////////////////////////////////
   public void renderMarkerLow(GL2 gl2) {
      // Low marker
      if (this.isSelected == true)
         gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
      else 
         gl2.glColor4fv(SchemeManager.unselected.toArray(), 0);
         //gl2.glColor4d(0.5, 0.5, 0.5, 1.0);
      
      gl2.glBegin(GL2.GL_TRIANGLES);     
         gl2.glVertex2d(anchorX+lowIdx * interval - markerSize, anchorY-markerSize);
         gl2.glVertex2d(anchorX+lowIdx * interval, anchorY-markerSize);
         gl2.glVertex2d(anchorX+lowIdx * interval, anchorY);
      gl2.glEnd();
   }
     
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders the right marker
   ////////////////////////////////////////////////////////////////////////////////
   public void renderMarkerHigh(GL2 gl2) {
      // High marker
      if (this.isSelected == true)
         gl2.glColor4fv(SchemeManager.selected.toArray(), 0);
      else
         gl2.glColor4fv(SchemeManager.unselected.toArray(), 0);
      
      gl2.glBegin(GL2.GL_TRIANGLES);     
         gl2.glVertex2d( anchorX+(highIdx+1) * interval + markerSize, anchorY-markerSize);
         gl2.glVertex2d( anchorX+(highIdx+1) * interval, anchorY);
         gl2.glVertex2d( anchorX+(highIdx+1) * interval, anchorY-markerSize);
      gl2.glEnd();
   }
  
   
   ////////////////////////////////////////////////////////////////////////////////
   // Update the low and high slider indicator position
   // anchor position refer to the last mouse interaction with
   // either the low or the high slider indicator
   ////////////////////////////////////////////////////////////////////////////////
   public boolean update(double newAnchor) {
      if (! isSelected) return false;
      
      int oldH = (int)highIdx;
      int oldL = (int)lowIdx;
      
      
      if (sitem == 2) {
         highIdx += (newAnchor-anchor)/interval;   
         if (highIdx >= data.length-1) highIdx = data.length-1;
         if (highIdx <= lowIdx) highIdx = lowIdx;
         
         // sanity check condition
         if (highIdx < 0) highIdx = 0;
      } 
      if (sitem == 1) {
         lowIdx += (newAnchor-anchor)/interval;   
         if (lowIdx <= 0) lowIdx = 0;
         if (lowIdx >= highIdx) lowIdx = highIdx;
         
         // sanity check condition
         if (lowIdx >= data.length-1) lowIdx = data.length-1;
      } 
      
      //System.out.println(oldH + " " + (int)highIdx);
      
      if (oldH != (int)highIdx || oldL != (int)lowIdx) {
         System.out.println("SHould have an update here ....");
         return true;
      }
      
      return false;
   }
   
   
   
   // Just for the animator reflections, we don't
   // really need this with public scooped variables
   public void setData(DCPair[] d) { data = d; }
   public DCPair[] getData() { return data; }
   
   public void setMaxValue(Double d) {
      maxValue = d.doubleValue();      
   }
   public Double getMaxValue() {
      return maxValue;   
   }
   
   public double barHeight[];
   public void setBarHeight( double[] v) { barHeight = v; }
   public double[] getBarHeight() { return barHeight; }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create, or recreate the texture representing the 
   // labelling for this gui element
   ////////////////////////////////////////////////////////////////////////////////
   public void createTexture() {
   	if (tempData == null) return;
   	tf.clearMark();
   	
   	// The dimension of the texture is bigger than the element to
   	// account for text labels
   	tf.width = ((float)tempData.length) * (float)this.interval;
   	tf.height = (float)this.height + 30;
   	
   	// Add the value labels (total amounts for each time period)
   	for (int i=0; i < tempData.length; i++) {
   		float v = (float)(tempData[i].value*height/tempMaxValue);
   		String s = tempData[i].value > 1000 ? DCUtil.abbreviation((int)tempData[i].value) : (int)tempData[i].value+"";
   	   tf.addMark( s, labelColour, labelFont, (float)(3+(i*interval)), v+15, false);	
   	}
   	
   	// Add the key labels (time period names)
   	for (int i=0; i < tempData.length; i++) {
   	   String s = tempData[i].key; 
   	   if (keyTranslation != null && keyTranslation.get(tempData[i].key) != null) {
            s = keyTranslation.get( tempData[i].key );
   	   }
   	   tf.addMark( s, labelColour, labelFont, (float)(10+(i*interval)), 3, false);   
   	}
   	tf.renderToTexture(null);
   }
   
   
   public double tempMaxValue = 1.0; // Hack to have a maximum for the texture while the actual max value is in animated transition
   public DCPair[] tempData;         // Hack to have a maximum for the texture while the actual max value is in animated transition
   
   public double maxValue = 1.0;
   public double height = SSM.instance().rangeFilterHeight;
   
   public DCPair[] data;      // Primary Data
   public DCPair[] subData;   // Secondary Data
   public double hValue[];   // The actual height positions
   
   //public String labelTxt;
   public double interval;
   public double markerSize = 17;
   public double lowIdx = 0;
   public double highIdx = 0;
  
   public DCColour barColour;
   
   // For calculations
   public double anchor = 0;
   public int sitem = 0;
   public boolean isSelected = false;
   
   public float anchorX = 0; // left
   public float anchorY = 0; // bottom
   
   
   // Translate the key into display-able values
   public Hashtable<String, String> keyTranslation = null;
   
   
   // Font as textured quads
   public static Color labelColour = Color.BLACK;
   //public static Font labelFont = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 12f);   
   public static Font labelFont = new Font("Arial", Font.PLAIN, 10);
   public TextureFont tf = new TextureFont();   
   
}

