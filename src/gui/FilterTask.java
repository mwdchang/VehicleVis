package gui;

import java.nio.IntBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import model.DCTriple;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import util.DCUtil;
import util.DWin;
import TimingFrameExt.DoubleEval;
import TimingFrameExt.SliderEval;

import com.jogamp.opengl.util.GLBuffers;

import datastore.CacheManager;
import datastore.SSM;
import datastore.SchemeManager;
import exec.RenderTask;

/////////////////////////////////////////////////////////////////////////////////
// Controls the two slider widgets, a yearly slider and a monthly slider to 
// control the date range
/////////////////////////////////////////////////////////////////////////////////
public class FilterTask implements RenderTask {
   
   
   public boolean deferredRefresh = false;
   
   ////////////////////////////////////////////////////////////////////////////////
   // Position various sliders and render
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void render(GL2 gl2) {
      
      // test resetting so colour picking will pick up the changes     
      yearSlider.barColour = SchemeManager.selected;
      monthSlider.barColour = SchemeManager.selected;
      
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0, SSM.windowWidth, 0, SSM.windowHeight, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      
      
      // Set the anchor here so it is with respect to the ortho
      yearSlider.anchorX = SSM.instance().getYearAnchorX();
      yearSlider.anchorY = SSM.instance().getYearAnchorY();
      
      yearSlider.tf.anchorX = yearSlider.anchorX;
      yearSlider.tf.anchorY = yearSlider.anchorY-12;
      
      monthSlider.anchorX = SSM.instance().getMonthAnchorX();
      monthSlider.anchorY = SSM.instance().getMonthAnchorY();
      monthSlider.tf.anchorX = monthSlider.anchorX;
      monthSlider.tf.anchorY = monthSlider.anchorY-12;
      
      
      
      gl2.glDisable(GL2.GL_LIGHTING);
      
      
      
      // Render the year slider filter
      gl2.glLoadIdentity();
      yearSlider.render(gl2);
      
      SSM.yearLow[0] = new DCTriple(yearSlider.anchorX+yearSlider.lowIdx * yearSlider.interval - yearSlider.markerSize, yearSlider.anchorY-yearSlider.markerSize, 0.0);
      SSM.yearLow[1] = new DCTriple(yearSlider.anchorX+yearSlider.lowIdx * yearSlider.interval, yearSlider.anchorY-yearSlider.markerSize, 0);
      SSM.yearLow[2] = new DCTriple(yearSlider.anchorX+yearSlider.lowIdx * yearSlider.interval, yearSlider.anchorY, 0);
      SSM.yearHigh[0] = new DCTriple( yearSlider.anchorX+(yearSlider.highIdx+1) * yearSlider.interval + yearSlider.markerSize, yearSlider.anchorY-yearSlider.markerSize, 0);
      SSM.yearHigh[1] = new DCTriple( yearSlider.anchorX+(yearSlider.highIdx+1) * yearSlider.interval, yearSlider.anchorY, 0);
      SSM.yearHigh[2] = new DCTriple( yearSlider.anchorX+(yearSlider.highIdx+1) * yearSlider.interval, yearSlider.anchorY-yearSlider.markerSize, 0);      
      
      // Render the month slider filter
      gl2.glLoadIdentity();
      monthSlider.render(gl2);
      
      SSM.monthLow[0] = new DCTriple(monthSlider.anchorX+monthSlider.lowIdx * monthSlider.interval - monthSlider.markerSize, monthSlider.anchorY-monthSlider.markerSize, 0.0);
      SSM.monthLow[1] = new DCTriple(monthSlider.anchorX+monthSlider.lowIdx * monthSlider.interval, monthSlider.anchorY-monthSlider.markerSize, 0);
      SSM.monthLow[2] = new DCTriple(monthSlider.anchorX+monthSlider.lowIdx * monthSlider.interval, monthSlider.anchorY, 0);
      SSM.monthHigh[0] = new DCTriple( monthSlider.anchorX+(monthSlider.highIdx+1) * monthSlider.interval + monthSlider.markerSize, monthSlider.anchorY-monthSlider.markerSize, 0);
      SSM.monthHigh[1] = new DCTriple( monthSlider.anchorX+(monthSlider.highIdx+1) * monthSlider.interval, monthSlider.anchorY, 0);
      SSM.monthHigh[2] = new DCTriple( monthSlider.anchorX+(monthSlider.highIdx+1) * monthSlider.interval, monthSlider.anchorY-monthSlider.markerSize, 0);      
      
      monthSlider.tf.render(gl2);
      yearSlider.tf.render(gl2);
      
      /*
      if (CacheManager.instance().filterMonthData != null) {
         monthSlider.renderData(gl2, CacheManager.instance().filterMonthData);
      }
      */
      
      
      // I don't know why the year slider disappears, just render it one more time :(
      //gl2.glLoadIdentity();
      //yearSlider.render(gl2);
      
         
   }
   
   
   
   
   @Override
   public void init(GL2 gl2) {
      yearSlider  = new DCRSlider();
      monthSlider = new DCRSlider();
      
      // Give month slider a translation table to convert 
      // integer month to string literals
      monthSlider.keyTranslation = DCUtil.getMonthTranslationTable();
      
      
      // Override default interval amount
      yearSlider.interval = 45;
      yearSlider.markerSize = 40;
      monthSlider.interval = 45;
      monthSlider.markerSize = 40;
      
      
      yearSlider.barColour = SchemeManager.selected;
      monthSlider.barColour = SchemeManager.selected;
      
      // Set the bottom-left of the year range slider
      yearSlider.anchorX = 80;
      yearSlider.anchorY = SSM.windowHeight - 150;
      
      // Set the bottom left of the month slider
      monthSlider.anchorX = 80;
      monthSlider.anchorY = SSM.windowHeight - 200;
      
      monthSlider.createTexture();
      yearSlider.createTexture();
      
      setYearData();
      setMonthData();
      
      // Reset
      unfocus();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset the year slider
   ////////////////////////////////////////////////////////////////////////////////
   public void setYearData() {
      
      Vector<DCPair> yearVolume = CacheManager.instance().getFilterYearlyStat(
            SSM.manufactureAttrib.selected, 
            SSM.makeAttrib.selected, 
            SSM.modelAttrib.selected,
            SSM.yearAttrib.selected);
      
      Vector<DCPair> c_yearVolume = CacheManager.instance().getFilterYearlyStat(
            SSM.c_manufactureAttrib.selected, 
            SSM.c_makeAttrib.selected, 
            SSM.c_modelAttrib.selected,
            SSM.c_yearAttrib.selected);
      
      // Sanity check, the year time lines should always have the same size
      // regardless of selection criterion
      if (yearVolume.size() != c_yearVolume.size()) {
         System.err.println("Failed regular_year vs compare_year sanity check");
         System.exit(0);
      }
      
      
      yearData = new DCPair[ yearVolume.size()];
      double max = 0;
      
      // When in comparison mode, sum up the regular year volume and the comparison year volume
      // otherwise just use the regular year volume
      if (SSM.useComparisonMode == false) {
         for (int i=0; i < yearVolume.size(); i++) {
            double volume = yearVolume.elementAt(i).value;
            yearData[i] = new DCPair( yearVolume.elementAt(i).key, volume);
            if (max < volume) max = volume;
         }
      } else {
         for (int i=0; i < yearVolume.size(); i++) {
            double volume = yearVolume.elementAt(i).value + c_yearVolume.elementAt(i).value;
            yearData[i] = new DCPair( yearVolume.elementAt(i).key, volume);
            if (max < volume) max = volume;
         }
      }
      
      yearSlider.tempMaxValue = max;
      yearSlider.tempData = yearData;
      
      if (yearSlider.data != null) {
         yearAnimator = PropertySetter.createAnimator(SSM.TIME_CHANGE_DURATION, yearSlider, "data", new SliderEval(), yearSlider.data, yearData);
         yearAnimator2 = PropertySetter.createAnimator(SSM.TIME_CHANGE_DURATION, yearSlider, "maxValue", new DoubleEval(), yearSlider.maxValue, (double)max);
         yearAnimator.start();
         yearAnimator2.start();
      } else {
         yearSlider.maxValue = max;
         yearSlider.data = yearData;
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset the month slider
   ////////////////////////////////////////////////////////////////////////////////
   public void setMonthData() {
      double max = 0;
      int[] monthVolume = CacheManager.instance().getFilterMonthlyStat(SSM.startTimeFrame, SSM.endTimeFrame, 
            SSM.manufactureAttrib.selected,
            SSM.makeAttrib.selected,
            SSM.modelAttrib.selected,
            SSM.yearAttrib.selected); 
      
      int[] c_monthVolume = CacheManager.instance().getFilterMonthlyStat(SSM.startTimeFrame, SSM.endTimeFrame, 
            SSM.c_manufactureAttrib.selected,
            SSM.c_makeAttrib.selected,
            SSM.c_modelAttrib.selected,
            SSM.c_yearAttrib.selected); 
     
      monthData = new DCPair[monthVolume.length];
      
      if (SSM.useComparisonMode == false) { 
         for (int i=0; i < monthVolume.length; i++) {
            monthData[i] = new DCPair((i+1)+"", monthVolume[i]);   
            if (max < monthVolume[i]) max = monthVolume[i];
         }
      } else {
         for (int i=0; i < monthVolume.length; i++) {
            double volume = monthVolume[i] + c_monthVolume[i];   
            monthData[i] = new DCPair((i+1)+"", volume);
            if (max < volume) max = volume;
         }
      }
      monthSlider.tempMaxValue = max;
      monthSlider.tempData = monthData;
      
      if (monthSlider.data != null) {
         monthAnimator = PropertySetter.createAnimator(SSM.TIME_CHANGE_DURATION, monthSlider, "data", new SliderEval(), monthSlider.data, monthData);
         monthAnimator2 = PropertySetter.createAnimator(SSM.TIME_CHANGE_DURATION, monthSlider, "maxValue", new DoubleEval(), monthSlider.maxValue, (double)max);
         monthAnimator.start();
         monthAnimator2.start();
      } else {
         monthSlider.data = monthData;
         monthSlider.maxValue = max;
      }
      
   }
   

   ////////////////////////////////////////////////////////////////////////////////
   // Position the select-able parts and render
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void picking(GL2 gl2, float px, float py) {
      //pickSliderIndicator(gl2, px, py);
      pickingSlider2(gl2, px, py);
   }
   
   
   // Just to check if the indicators is being dragged
   public void pickingDrag(GL2 gl2, float px, float py) {
      int x = (int)px;
      int y = SSM.windowHeight - (int)py;
      DCTriple point = new DCTriple(x, y, 0);
      boolean selected = false;
      
      // Check the interactive markers
      if (DCUtil.pointInTriangle(point, SSM.yearHigh[0], SSM.yearHigh[1], SSM.yearHigh[2])) {
         deferredRefresh = true;
         yearSlider.isSelected = true;
         yearSlider.sitem = 2;
         yearSlider.anchor = px;
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.yearLow[0], SSM.yearLow[1], SSM.yearLow[2])) {
         deferredRefresh = true;
         yearSlider.isSelected = true;
         yearSlider.sitem = 1;
         yearSlider.anchor = px;
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.monthHigh[0], SSM.monthHigh[1], SSM.monthHigh[2])) {
         deferredRefresh = true;
         monthSlider.isSelected = true; 
         monthSlider.sitem = 2;
         monthSlider.anchor = px; 
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.monthLow[0], SSM.monthLow[1], SSM.monthLow[2])) {
         deferredRefresh = true;
         monthSlider.isSelected = true; 
         monthSlider.sitem = 1;
         monthSlider.anchor = px; 
         selected = true;
      }
      
      // If something is selected, than flag the necessary updates
      if (selected == true) {
         SSM.topElement = SSM.ELEMENT_FILTER;
         SSM.globalFetchIdx = 0;
         SSM.docStartIdx = 0;
         yearSlider.createTexture();
         monthSlider.createTexture();
      }
    
   }
   
   
   // Picking ... for "clicking" action
   public void pickingSlider2(GL2 gl2, float px, float py) {
      if (SSM.stopPicking == 1) return;
      
      int x = (int)px;
      int y = SSM.windowHeight - (int)py;
      
      
      DCTriple point = new DCTriple(x, y, 0);
      boolean selected = false;
      
      // Check the interactive markers
      if (DCUtil.pointInTriangle(point, SSM.yearHigh[0], SSM.yearHigh[1], SSM.yearHigh[2])) {
         deferredRefresh = true;
         yearSlider.isSelected = true;
         yearSlider.sitem = 2;
         yearSlider.anchor = px;
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.yearLow[0], SSM.yearLow[1], SSM.yearLow[2])) {
         deferredRefresh = true;
         yearSlider.isSelected = true;
         yearSlider.sitem = 1;
         yearSlider.anchor = px;
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.monthHigh[0], SSM.monthHigh[1], SSM.monthHigh[2])) {
         deferredRefresh = true;
         monthSlider.isSelected = true; 
         monthSlider.sitem = 2;
         monthSlider.anchor = px; 
         selected = true;
      }
      if (DCUtil.pointInTriangle(point, SSM.monthLow[0], SSM.monthLow[1], SSM.monthLow[2])) {
         deferredRefresh = true;
         monthSlider.isSelected = true; 
         monthSlider.sitem = 1;
         monthSlider.anchor = px; 
         selected = true;
      }
      
      
      // Now check against individual bars
      float yf_anchorX = SSM.instance().getYearAnchorX();
      float yf_anchorY = SSM.instance().getYearAnchorY();      
      float yf_width   = (CacheManager.instance().timeLineSize/12)*SSM.instance().rangeFilterWidth;
      if (DCUtil.between(x, yf_anchorX, yf_anchorX + (CacheManager.instance().timeLineSize/(float)12)*SSM.instance().rangeFilterWidth)) {
         if (DCUtil.between(y, yf_anchorY-15, yf_anchorY+SSM.instance().rangeFilterHeight)) {
System.out.println("Selected something on the year bar");            
            float rawIdx = (float)(CacheManager.instance().timeLineSize/12)*( ((float)x - yf_anchorX)/ yf_width);
            int idx = (int)Math.floor(rawIdx);
            SSM.stopPicking = 1;
            
            // if we selected ourselves, high light all the year
            if (yearSlider.lowIdx == idx && yearSlider.highIdx == idx) {
               monthSlider.lowIdx = 0;
               monthSlider.highIdx = 11;
            }
            yearSlider.lowIdx = idx;
            yearSlider.highIdx = idx;
            deferredRefresh = true;            
            selected = true;
         }
      }      
      
      float mf_anchorX = SSM.instance().getMonthAnchorX();
      float mf_anchorY = SSM.instance().getMonthAnchorY();
      // Always 12 month
      if (DCUtil.between(x, mf_anchorX, mf_anchorX + 12*SSM.instance().rangeFilterWidth)) {
         if (DCUtil.between(y, mf_anchorY-15, mf_anchorY+SSM.instance().rangeFilterHeight)) {
            float rawIdx = (float)12* ( (float)x - yf_anchorX)/ (12*SSM.instance().rangeFilterWidth);
            int idx = (int)Math.floor(rawIdx);
            
            SSM.stopPicking = 1;
            monthSlider.lowIdx = idx;
            monthSlider.highIdx = idx;
            deferredRefresh = true;
            selected = true;
         }
      }       
      
      
      // If something is selected, than flag the necessary updates
      if (selected == true) {
         SSM.topElement = SSM.ELEMENT_FILTER;
         SSM.globalFetchIdx = 0;
         SSM.docStartIdx = 0;
         yearSlider.createTexture();
         monthSlider.createTexture();
      }
      
      
   }
   
   
   
   public void pickSliderIndicator(GL2 gl2, float px, float py) {
      //if (SSM.instance().l_mousePressed == false) return;
      if (SSM.stopPicking == 1) return;
      
      
      int hits;
      IntBuffer buffer = (IntBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
      IntBuffer viewport =  (IntBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 4); 
      gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport);
      gl2.glSelectBuffer(512, buffer);
      
      gl2.glRenderMode(GL2.GL_SELECT);
      gl2.glInitNames();
      gl2.glPushName(0); // At least one 

      // Set up the environment as if rendering
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glPushMatrix();
         gl2.glLoadIdentity();
//         mouseY = viewport.get(3) - mouseY;
         glu.gluPickMatrix((float)px, (float)(viewport.get(3) - py), 1.0f, 1.0f, viewport);
         SSM.instance();
         SSM.instance();
         gl2.glOrtho(0, SSM.windowWidth, 0, SSM.windowHeight, -10, 10);
         gl2.glMatrixMode(GL2.GL_MODELVIEW);
         gl2.glLoadIdentity();      
         
                          
         gl2.glLoadName(101);
         gl2.glPushMatrix();
            yearSlider.renderMarkerLow(gl2);
         gl2.glPopMatrix();   
         
         gl2.glLoadName(102);
         gl2.glPushMatrix();
            yearSlider.renderMarkerHigh(gl2);
         gl2.glPopMatrix();  
         for (int i=0; i < yearSlider.data.length; i++) {
            gl2.glLoadName(1000+i);
            gl2.glPushMatrix();
               yearSlider.renderQuad(gl2, yearSlider.data[i], null, 1, i);   
            gl2.glPopMatrix();
         }         
         
 
                  
         
         
         gl2.glLoadIdentity();
         gl2.glLoadName(201);
         gl2.glPushMatrix();
            monthSlider.renderMarkerLow(gl2);
         gl2.glPopMatrix();
         
         gl2.glLoadName(202);
         gl2.glPushMatrix();
            monthSlider.renderMarkerHigh(gl2);
         gl2.glPopMatrix();
         for (int i=0; i < monthSlider.data.length; i++) {
            gl2.glLoadName(2000+i);
            gl2.glPushMatrix();
               monthSlider.renderQuad(gl2, monthSlider.data[i], null, 1, i);   
            gl2.glPopMatrix();
         }          
      
         gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glPopMatrix();
      
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      hits = gl2.glRenderMode(GL2.GL_RENDER);
      
      if (hits > 0) {
         System.out.println("hit in filter");
         
         int choose = buffer.get(3);
         int depth  = buffer.get(1);
         
         for (int idx = 1; idx < hits; idx++) {
            if(buffer.get(idx*4+1) < depth) {
               choose = buffer.get(idx*4+3);
               depth  = buffer.get(idx*4+1);
            }
         }
         
         if (choose < 200) {
            System.out.println("Hit in year marker " + px);
            deferredRefresh = true;
            yearSlider.isSelected = true;
            yearSlider.sitem = choose%100;
            yearSlider.anchor = px;
         } else if (choose < 300) {
            System.out.println("Hit in month marker " + px);
            deferredRefresh = true;
            monthSlider.isSelected = true; 
            monthSlider.sitem = choose%100;
            monthSlider.anchor = px;
         } else if (choose < 2000) {
            SSM.stopPicking = 1;
            int index = choose%1000;
            
            // if we selected ourselves, high light all the year
            if (yearSlider.lowIdx == index && yearSlider.highIdx == index) {
               monthSlider.lowIdx = 0;
               monthSlider.highIdx = 11;
            }
            
            yearSlider.lowIdx = index;
            yearSlider.highIdx = index;
            deferredRefresh = true;
         } else if (choose < 3000) {
            SSM.stopPicking = 1;
            int index = choose%1000;
            monthSlider.lowIdx = index;
            monthSlider.highIdx = index;
            deferredRefresh = true;
         }
         
         SSM.topElement = SSM.ELEMENT_FILTER;
         SSM.globalFetchIdx = 0;
         SSM.docStartIdx = 0;
         
         yearSlider.createTexture();
         monthSlider.createTexture();
      }      
      
   }
   
   
   public boolean update(double newAnchor) {
      boolean refresh = false; 
      if (yearSlider.isSelected) {
         if (yearSlider.update(newAnchor) == true) refresh=true;   
         yearSlider.anchor = newAnchor;
      }
      if (monthSlider.isSelected) {
         if (monthSlider.update(newAnchor) == true) refresh=true;
         monthSlider.anchor = newAnchor;
      }
      
      
      
      return refresh;
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Restore/Reset year and month slider state using the SSM data
   ////////////////////////////////////////////////////////////////////////////////
   public void loadFromSSM() {
System.out.println("Restore from SSM");      
System.out.println("M " + SSM.startMonth );
System.out.println("M " + SSM.endMonth );

   	for (int i=0; i < yearData.length; i++) {
   	   if (SSM.startYear == Integer.parseInt(yearData[i].key)) yearSlider.lowIdx = i; 	
   	   if (SSM.endYear == Integer.parseInt(yearData[i].key)) yearSlider.highIdx = i; 	
   	}
   	monthSlider.lowIdx = SSM.startMonth;
   	monthSlider.highIdx = SSM.endMonth;
   	
   	setYearData();
      setMonthData();
      monthSlider.isSelected = false;
      monthSlider.highIdx = (int)monthSlider.highIdx;
      monthSlider.lowIdx = (int)monthSlider.lowIdx;
      monthSlider.sitem = -1;      
                 
      yearSlider.createTexture();
      monthSlider.createTexture();   	
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // When user stops dragging the markers
   ////////////////////////////////////////////////////////////////////////////////
   public void unfocus() {
System.out.println("In Unfocus");
      yearSlider.isSelected = false;
      yearSlider.highIdx = (int)yearSlider.highIdx;
      yearSlider.lowIdx = (int)yearSlider.lowIdx;
      yearSlider.sitem = -1;
      

      
      
      // Setup the new focus time-frame
      // TODO: Holy cow...this is ugly, should really do something about it !!!
      String str_lowIdx = yearData[ (int)yearSlider.lowIdx ].key +
                          monthData[ (int)monthSlider.lowIdx ].key +
                          "01"; 
      String str_highIdx = yearData[ (int)yearSlider.highIdx ].key +
                           monthData[ (int)monthSlider.highIdx ].key + 
                           getDaysInMonth(  new Integer(yearData[(int)yearSlider.highIdx].key),
                                            (int)monthSlider.highIdx);
      
      
      // Hack: set the lowIdx and highIndex to the end of year, always - for more consistent sparkline 
      str_lowIdx  = yearData[ (int)yearSlider.lowIdx ].key + "0101";
      str_highIdx = yearData[ (int)yearSlider.highIdx ].key + "1231";
      
      
      
      // Do not do unnecessary updates
      if (SSM.startMonth != (int)monthSlider.lowIdx ||
          SSM.endMonth != (int)monthSlider.highIdx ||
          SSM.startYear != Integer.parseInt(yearData[ (int)yearSlider.lowIdx ].key) ||
          SSM.endYear != Integer.parseInt(yearData[ (int)yearSlider.highIdx ].key)) {
         SSM.dirty = 1;
         SSM.dirtyGL = 1;
      }
      DWin.instance().msg("New data rage " + str_lowIdx + " to " + str_highIdx);
      SSM.startTimeFrame = str_lowIdx;
      SSM.endTimeFrame   = str_highIdx;
      SSM.startMonth = (int)monthSlider.lowIdx;
      SSM.endMonth = (int)monthSlider.highIdx;
      SSM.startYear = Integer.parseInt(yearData[ (int)yearSlider.lowIdx ].key);
      SSM.endYear = Integer.parseInt(yearData[ (int)yearSlider.highIdx ].key);
                 
      setMonthData();
      
      monthSlider.isSelected = false;
      monthSlider.highIdx = (int)monthSlider.highIdx;
      monthSlider.lowIdx = (int)monthSlider.lowIdx;
      monthSlider.sitem = -1;      
                 
      yearSlider.createTexture();
      monthSlider.createTexture();
      


   }
   
   
   // Get the number of days in a month that is 
   // sensitive to leap year
   public int getDaysInMonth(int year, int month) {
      Calendar cal = new GregorianCalendar(year, month, 1);
      return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
   }
   
   
   
   public DCPair[] yearData;
   public DCPair[] monthData;
   
   public GLU glu = new GLU(); // for picking matrix
   public DCRSlider yearSlider;
   public DCRSlider monthSlider;
    
   
   // Animation
   public Animator monthAnimator;
   public Animator monthAnimator2;
   public Animator yearAnimator;
   public Animator yearAnimator2;
}
