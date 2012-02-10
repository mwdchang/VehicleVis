package gui;

import java.nio.IntBuffer;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import model.DCColour;

import util.DCUtil;
import util.DWin;
import util.FontRenderer;

import TimingFrameExt.DoubleEval;
import TimingFrameExt.SliderEval;

import com.jogamp.opengl.util.GLBuffers;

import datastore.CacheManager;
import datastore.SSM;
import datastore.SchemeManager;
import db.DBWrapper;
import exec.RenderTask;

/////////////////////////////////////////////////////////////////////////////////
// Controls the two slider widgets, a yearly slider and a monthly slider to 
// control the date range
/////////////////////////////////////////////////////////////////////////////////
public class FilterTask implements RenderTask {
   
   public static float offsetX = 30.0f;
   
   ////////////////////////////////////////////////////////////////////////////////
   // Position various sliders and render
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void render(GL2 gl2) {
      gl2.glMatrixMode(GL2.GL_PROJECTION);
      gl2.glLoadIdentity();
      gl2.glOrtho(0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight, -10, 10);
      gl2.glMatrixMode(GL2.GL_MODELVIEW);
      gl2.glLoadIdentity();
      
      
      // Set the anchor here so it is with respect to the ortho
      yearSlider.anchorX = 30;
      yearSlider.anchorY = SSM.instance().windowHeight - 80;
      yearSlider.tf.anchorX = yearSlider.anchorX;
      yearSlider.tf.anchorY = yearSlider.anchorY-12;
      
      monthSlider.anchorX = 30;
      monthSlider.anchorY = SSM.instance().windowHeight - 170;
      monthSlider.tf.anchorX = monthSlider.anchorX;
      monthSlider.tf.anchorY = monthSlider.anchorY-12;
      
      
      
      gl2.glDisable(GL2.GL_LIGHTING);
      
      
      
      // Render the year slider filter
      gl2.glLoadIdentity();
      yearSlider.render(gl2);
      
      /*
      if (CacheManager.instance().filterYearData != null) {
         yearSlider.renderData(gl2, CacheManager.instance().filterYearData);  
      }
      */
      
      // Render the month slider filter
      gl2.glLoadIdentity();
      monthSlider.render(gl2);
      
      
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
      yearSlider.interval = 40;
      monthSlider.interval = 40;
      
      //yearSlider.barColour = new DCColour(0.9f, 0.3f, 0.3f, 0.6f);
      //monthSlider.barColour = new DCColour(0.9f, 0.3f, 0.3f, 0.6f);
      yearSlider.barColour = SchemeManager.range_selected;
      monthSlider.barColour = SchemeManager.range_selected;
      
      // Set the bottom-left of the year range slider
      yearSlider.anchorX = 80;
      yearSlider.anchorY = SSM.instance().windowHeight - 150;
      
      // Set the bottom left of the month slider
      monthSlider.anchorX = 80;
      monthSlider.anchorY = SSM.instance().windowHeight - 200;
      
      setYearData();
      setMonthData();
      
      // Reset
      unfocus();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset the year slider
   ////////////////////////////////////////////////////////////////////////////////
   public void setYearData() {
      // Get all 
      //Vector<DCPair> yV = CacheManager.instance().getFilterYearlyStat(null, null, null);
      Vector<DCPair> yV = CacheManager.instance().getFilterYearlyStat(
            SSM.instance().manufactureAttrib.selected, 
            SSM.instance().makeAttrib.selected, 
            SSM.instance().modelAttrib.selected);
      
      yearData = new DCPair[ yV.size()];
      double max = 0;
      for (int i=0; i < yV.size(); i++) {
         yearData[i] = new DCPair( yV.elementAt(i).key, yV.elementAt(i).value);         
         if (max < yV.elementAt(i).value) max = yV.elementAt(i).value;
      }
      yearSlider.tempMaxValue = max;
      yearSlider.tempData = yearData;
      
      if (yearSlider.data != null) {
         yearAnimator = PropertySetter.createAnimator(1200, yearSlider, "data", new SliderEval(), yearSlider.data, yearData);
         yearAnimator2 = PropertySetter.createAnimator(1200, yearSlider, "maxValue", new DoubleEval(), yearSlider.maxValue, (double)max);
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
      int max = 0;
      //int[] mdata = CacheManager.instance().getFilterMonthlyStat(SSM.instance().startTimeFrame, SSM.instance().endTimeFrame, null, null, null);
      int[] mdata = CacheManager.instance().getFilterMonthlyStat(SSM.instance().startTimeFrame, SSM.instance().endTimeFrame, 
            SSM.instance().manufactureAttrib.selected,
            SSM.instance().makeAttrib.selected,
            SSM.instance().modelAttrib.selected); 
      
      monthData = new DCPair[mdata.length];
      for (int i=0; i < mdata.length; i++) {
         monthData[i] = new DCPair((i+1)+"", mdata[i]);   
         if (max < mdata[i]) max = mdata[i];
      }
      
      monthSlider.tempMaxValue = max;
      monthSlider.tempData = monthData;
      
      if (monthSlider.data != null) {
         monthAnimator = PropertySetter.createAnimator(1200, monthSlider, "data", new SliderEval(), monthSlider.data, monthData);
         monthAnimator2 = PropertySetter.createAnimator(1200, monthSlider, "maxValue", new DoubleEval(), monthSlider.maxValue, (double)max);
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
   public void picking(GL2 gl2) {
      pickSliderIndicator(gl2);
   }
   
   
   
   
   public void pickSliderIndicator(GL2 gl2) {
      
      if (SSM.instance().l_mouseClicked == false) return;
      
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
         glu.gluPickMatrix((float)SSM.instance().mouseX, (float)(viewport.get(3) - SSM.instance().mouseY), 1.0f, 1.0f, viewport);
         gl2.glOrtho(0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight, -10, 10);
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
      
//      System.out.println("Hits = " + hits);
      if (hits > 0) {
         int choose = buffer.get(3);
         int depth  = buffer.get(1);
         
         for (int idx = 1; idx < hits; idx++) {
            if(buffer.get(idx*4+1) < depth) {
               choose = buffer.get(idx*4+3);
               depth  = buffer.get(idx*4+1);
            }
         }
         
         if (choose < 200) {
            yearSlider.isSelected = true;
            yearSlider.sitem = choose%100;
            yearSlider.anchor = SSM.instance().mouseX;
         } else if (choose < 300) {
            monthSlider.isSelected = true; 
            monthSlider.sitem = choose%100;
            monthSlider.anchor = SSM.instance().mouseX;
         } else if (choose < 2000) {
            int index = choose%1000;
            
            System.out.println("................................." + yearSlider.lowIdx + " " + yearSlider.highIdx);
            
            // if we selected ourselves, high light all the year
            if (yearSlider.lowIdx == index && yearSlider.highIdx == index) {
               monthSlider.lowIdx = 0;
               monthSlider.highIdx = 11;
            }
            
            yearSlider.lowIdx = index;
            yearSlider.highIdx = index;
         } else if (choose < 3000) {
            int index = choose%1000;
            monthSlider.lowIdx = index;
            monthSlider.highIdx = index;
         }
         
         SSM.instance().currentFocusLayer = SSM.UI_LAYER;
         DWin.instance().debug("Switching to UI_LAYER");
         
         SSM.instance().globalFetchIdx = 0;
         SSM.instance().docStartIdx = 0;
         
         //System.out.println(  "Item is : " + choose + " mouseX is " + SSM.instance().mouseX);
         yearSlider.createTexture();
         monthSlider.createTexture();
      }      
      
   }
   
   
   public void update(double newAnchor) {
      if (yearSlider.isSelected) {
         yearSlider.update(newAnchor);   
         yearSlider.anchor = newAnchor;
      }
      if (monthSlider.isSelected) {
         monthSlider.update(newAnchor);
         monthSlider.anchor = newAnchor;
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Restore/Reset year and month slider state using the SSM data
   ////////////////////////////////////////////////////////////////////////////////
   public void loadFromSSM() {
   	for (int i=0; i < yearData.length; i++) {
   	   if (SSM.instance().startYear == Integer.parseInt(yearData[i].key)) yearSlider.lowIdx = i; 	
   	   if (SSM.instance().endYear == Integer.parseInt(yearData[i].key)) yearSlider.highIdx = i; 	
   	}
   	monthSlider.lowIdx = SSM.instance().startMonth;
   	monthSlider.highIdx = SSM.instance().endMonth;
   	
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
      
      
      
      DWin.instance().msg("New data rage " + str_lowIdx + " to " + str_highIdx);
      
      
      // Pray....may need to redo occurrence table in CacheManager 
      SSM.instance().startTimeFrame = str_lowIdx;
      SSM.instance().endTimeFrame   = str_highIdx;
      SSM.instance().dirty = 1;
      SSM.instance().dirtyGL = 1;
      SSM.instance().startMonth = (int)monthSlider.lowIdx;
      SSM.instance().endMonth = (int)monthSlider.highIdx;
      SSM.instance().startYear = Integer.parseInt(yearData[ (int)yearSlider.lowIdx ].key);
      SSM.instance().endYear = Integer.parseInt(yearData[ (int)yearSlider.highIdx ].key);
                 
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
   
   // Is there a native java function that returns month String literals ????
//   public String getMonthLiteral(int i) {
//     switch(i) {
//     case 1: return "Jan";
//     case 2: return "Feb";
//     case 3: return "Mar";
//     case 4: return "Apr";
//     case 5: return "May";
//     case 6: return "Jun";
//     case 7: return "Jul";
//     case 8: return "Aug";
//     case 9: return "Sep";
//     case 10: return "Oct";
//     case 11: return "Nov";
//     case 12: return "Dec";
//     default: 
//        return "Doh";
//     }
//   }
   
   
   
   // Based on year
   //public Vector<DCPair> year = new Vector<DCPair>();
   
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
