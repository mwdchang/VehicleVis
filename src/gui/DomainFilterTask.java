package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.opengl.GL2;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import TimingFrameExt.FloatEval;

import model.DCColour;
import model.PaneAttrib;

import datastore.CacheManager;
import datastore.Const;
import datastore.SSM;
import datastore.SchemeManager;
import db.QueryObj;

import util.DCUtil;
import util.GraphicUtil;
import util.TextureFont;

import exec.RenderTask;

////////////////////////////////////////////////////////////////////////////////
// Contains domain specific filters
////////////////////////////////////////////////////////////////////////////////
public class DomainFilterTask implements RenderTask {
   
   // Data filter combo boxes
   public TextureFont label;
   public TextureFont filterTexture;
   public DCScrollPane manufactureScroll; 
   public DCScrollPane makeScroll;
   public DCScrollPane modelScroll;
   public DCScrollPane yearScroll;
   
   public TextureFont c_label;
   public TextureFont c_filterTexture;
   public DCScrollPane c_manufactureScroll; 
   public DCScrollPane c_makeScroll;
   public DCScrollPane c_modelScroll;
   public DCScrollPane c_yearScroll;   
   
   // Aggregation switch
   public DCSwitch aggSwitch;
   
   // For perspective
   public TextureFont  perspectiveLabel;
   public DCScrollPane perspectiveScroll;
   
   public static Font labelFont = DCUtil.loadFont(Const.FONT_PATH+"din1451m.ttf", Font.PLAIN, 12f);
   

   @Override
   public void render(GL2 gl2) {
      setPositions();
      checkComparisonMode();
      renderSwitch(gl2);
      renderScrollFilter(gl2);
   }
   
   public void setPositions() {
      perspectiveScroll.anchorX = SSM.perspectiveAttrib.anchorX;
      perspectiveScroll.anchorY = SSM.perspectiveAttrib.anchorY;
      
      manufactureScroll.anchorX = SSM.manufactureAttrib.anchorX;
      manufactureScroll.anchorY = SSM.manufactureAttrib.anchorY;
      
      makeScroll.anchorX = SSM.makeAttrib.anchorX;
      makeScroll.anchorY = SSM.makeAttrib.anchorY;
      
      modelScroll.anchorX = SSM.modelAttrib.anchorX;
      modelScroll.anchorY = SSM.modelAttrib.anchorY;
      
      yearScroll.anchorX = SSM.yearAttrib.anchorX;
      yearScroll.anchorY = SSM.yearAttrib.anchorY;
      
      c_manufactureScroll.anchorX = SSM.c_manufactureAttrib.anchorX;
      c_manufactureScroll.anchorY = SSM.c_manufactureAttrib.anchorY;
      
      c_makeScroll.anchorX = SSM.c_makeAttrib.anchorX;
      c_makeScroll.anchorY = SSM.c_makeAttrib.anchorY;
      
      c_modelScroll.anchorX = SSM.c_modelAttrib.anchorX;
      c_modelScroll.anchorY = SSM.c_modelAttrib.anchorY;
      
      c_yearScroll.anchorX = SSM.c_yearAttrib.anchorX;
      c_yearScroll.anchorY = SSM.c_yearAttrib.anchorY;
      
      aggSwitch.anchorX = SSM.aggregationAnchorX;
      aggSwitch.anchorY = SSM.aggregationAnchorY;
   }
   

   @Override
   public void init(GL2 gl2) {
      
      perspectiveScroll = new DCScrollPane("Perspective");
      perspectiveScroll.depth = 0.5f;
      perspectiveScroll.calculate();
      perspectiveScroll.renderToTexture(null);
      perspectiveScroll.width =  SSM.perspectiveAttrib.width;
      perspectiveScroll.texPanelWidth = SSM.perspectiveAttrib.width;
      SSM.perspectiveAttrib.selected = "Month";
      SSM.perspectiveAttrib.hasAllSelection = false;
      
      perspectiveLabel = new TextureFont();
      perspectiveLabel.height = 20;
      perspectiveLabel.width = 100;
      perspectiveLabel.addMark("Perspective", Color.black, labelFont, 20, 10);
      perspectiveLabel.renderToTexture(null);

      
      
      label = new TextureFont();
      label.height = 45;
      label.width = 120;
      label.addMark("label", Color.black, labelFont, 1, 1);
      label.renderToTexture(null);
      
      filterTexture = new TextureFont();
      filterTexture.height = 20;
      filterTexture.width = 120;
      filterTexture.addMark("Vehicle 1", Color.BLACK, labelFont, 1, 1);
      filterTexture.renderToTexture(null);
      
      
      manufactureScroll = new DCScrollPane("MFR");
      manufactureScroll.depth = 0.5f;
      manufactureScroll.calculate();
      manufactureScroll.renderToTexture(null);
      
      makeScroll = new DCScrollPane("MAKE");
      makeScroll.depth = 0.5f;
      makeScroll.calculate();
      makeScroll.renderToTexture(null);
      
      modelScroll = new DCScrollPane("MODEL");
      modelScroll.depth = 0.5f;
      modelScroll.calculate();
      modelScroll.renderToTexture(null);
      
      yearScroll = new DCScrollPane("YEAR");
      yearScroll.depth = 0.5f;
      yearScroll.calculate();
      yearScroll.renderToTexture(null);
      
      
      
      c_label = new TextureFont();
      c_label.height = 45;
      c_label.width = 100;
      c_label.addMark("clabel", Color.black, labelFont, 1, 1);
      c_label.renderToTexture(null);
      
      c_filterTexture = new TextureFont();
      c_filterTexture.height = 20;
      c_filterTexture.width = 120;
      c_filterTexture.addMark("Vehicle 2", Color.BLACK, labelFont, 1, 1);
      c_filterTexture.renderToTexture(null);
      
      c_manufactureScroll = new DCScrollPane("MFR");
      c_manufactureScroll.direction = DCScrollPane.UP;
      c_manufactureScroll.calculate();
      c_manufactureScroll.renderToTexture(null);
      SSM.c_manufactureAttrib.selected = "None";
      SSM.c_manufactureAttrib.hasNoneSelection = true;
      
      c_makeScroll = new DCScrollPane("MAKE");
      c_makeScroll.direction = DCScrollPane.UP;
      c_makeScroll.calculate();
      c_makeScroll.renderToTexture(null);
      
      c_modelScroll = new DCScrollPane("MODEL");
      c_modelScroll.direction = DCScrollPane.UP;
      c_modelScroll.calculate();
      c_modelScroll.renderToTexture(null);
      
      c_yearScroll = new DCScrollPane("YEAR");
      c_yearScroll.direction = DCScrollPane.UP;
      c_yearScroll.calculate();
      c_yearScroll.renderToTexture(null);
      
      
      // Resize the width
      manufactureScroll.width = SSM.manufactureAttrib.width;
      manufactureScroll.texPanelWidth = SSM.manufactureAttrib.width;
      makeScroll.width = SSM.makeAttrib.width;
      makeScroll.texPanelWidth = SSM.makeAttrib.width;
      modelScroll.width = SSM.modelAttrib.width;
      modelScroll.texPanelWidth = SSM.modelAttrib.width;
      yearScroll.width = SSM.yearAttrib.width;
      yearScroll.texPanelWidth = SSM.yearAttrib.width;
      
      c_manufactureScroll.width = SSM.c_manufactureAttrib.width;
      c_manufactureScroll.texPanelWidth = SSM.c_manufactureAttrib.width;
      c_makeScroll.width = SSM.c_makeAttrib.width;
      c_makeScroll.texPanelWidth = SSM.c_makeAttrib.width;
      c_modelScroll.width = SSM.c_modelAttrib.width;
      c_modelScroll.texPanelWidth = SSM.c_modelAttrib.width;
      c_yearScroll.width = SSM.c_yearAttrib.width;
      c_yearScroll.texPanelWidth = SSM.c_yearAttrib.width;  
      
      
      aggSwitch = new DCSwitch();
      aggSwitch.setLabel("Aggregation");
      
      setPositions();
   }
   

   @Override
   public void picking(GL2 gl2, float px, float py) {
      if (SSM.l_mouseClicked == false) return;
      
      float mx = px;
      float my = SSM.windowHeight - py;
      
      // Check if any one of the master scrollpane buttons are pressed
      /*
      if (DCUtil.between(mx, filterTexture.anchorX, filterTexture.anchorX+filterTexture.width))  {
         if (DCUtil.between(my, filterTexture.anchorY, filterTexture.anchorY+filterTexture.height)) {
            System.err.println("Clicked on master control");   
            this.manufactureScroll.masterVisible = ! this.manufactureScroll.masterVisible;
            this.makeScroll.masterVisible = ! this.makeScroll.masterVisible;
            this.modelScroll.masterVisible = ! this.modelScroll.masterVisible;
            this.yearScroll.masterVisible = ! this.yearScroll.masterVisible;
            
            if (this.manufactureScroll.masterVisible) {
               this.filterTexture.clearMark();
               this.filterTexture.addMark("Filter 1", Color.BLACK, labelFont, 1, 1);
               this.filterTexture.renderToTexture(null);
            } else {
               this.filterTexture.clearMark();
               this.filterTexture.addMark("Filter 1", Color.BLACK, labelFont, 1, 1);
               this.filterTexture.renderToTexture(null);
            }
            
            SSM.stopPicking = 1;
            return;
         }
      }
      
      if (DCUtil.between(mx, c_filterTexture.anchorX, c_filterTexture.anchorX+c_filterTexture.width))  {
         if (DCUtil.between(my, c_filterTexture.anchorY, c_filterTexture.anchorY+c_filterTexture.height)) {
            System.err.println("Clicked on c master control");   
            this.c_manufactureScroll.masterVisible = ! this.c_manufactureScroll.masterVisible;
            this.c_makeScroll.masterVisible = ! this.c_makeScroll.masterVisible;
            this.c_modelScroll.masterVisible = ! this.c_modelScroll.masterVisible;
            this.c_yearScroll.masterVisible = ! this.c_yearScroll.masterVisible;
            
            if (this.c_manufactureScroll.masterVisible) {
               this.c_filterTexture.clearMark();
               this.c_filterTexture.addMark("Filter 2", Color.BLACK, labelFont, 1, 1);
               this.c_filterTexture.renderToTexture(null);
            } else {
               this.c_filterTexture.clearMark();
               this.c_filterTexture.addMark("Filter 2", Color.BLACK, labelFont, 1, 1);
               this.c_filterTexture.renderToTexture(null);
            }
            
            SSM.stopPicking = 1;
            return;
         }
      }
      */
      
      ////////////////////////////////////////////////////////////////////////////////
      // Check the UI elements first
      // The GUIs are implicitly layered, so the top layered gui elements are processed 
      // first
      ////////////////////////////////////////////////////////////////////////////////
      
      // Handle perspective
      SSM.stopPicking = pickingScrollPane(mx, my,perspectiveScroll, SSM.perspectiveAttrib); 
      if (SSM.perspectiveAttrib.selected != null) {
         if (SSM.perspectiveAttrib.selected.equalsIgnoreCase("Month")) {
            SSM.chartMode = SSM.CHART_MODE_BY_MONTH_MAX;
            System.out.println("Switching to month");
         } else if (SSM.perspectiveAttrib.selected.equalsIgnoreCase("Component")) {
            SSM.chartMode = SSM.CHART_MODE_BY_COMPONENT_MAX;
            System.out.println("Switching to component");
         } else if (SSM.perspectiveAttrib.selected.equalsIgnoreCase("Global")) {
            SSM.chartMode = SSM.CHART_MODE_BY_GLOBAL_MAX;
            System.out.println("Switching to global");
         }
      }
      if (SSM.stopPicking != 0) return;
      
      
      
      // Handle vehicle manufacturers
      SSM.stopPicking = pickingScrollPane(mx, my, manufactureScroll, SSM.manufactureAttrib, 
            makeScroll, SSM.makeAttrib,     // level 1
            modelScroll, SSM.modelAttrib,   // level 2
            yearScroll,  SSM.yearAttrib     // level 3
      ); if (SSM.stopPicking != 0) return;
      SSM.stopPicking = pickingScrollPane(mx, my, c_manufactureScroll, SSM.c_manufactureAttrib, 
            c_makeScroll, SSM.c_makeAttrib,     // level 1
            c_modelScroll, SSM.c_modelAttrib,   // level 2
            c_yearScroll,  SSM.c_yearAttrib     // level 3
      );
      if (SSM.c_manufactureAttrib.selected != null && SSM.c_manufactureAttrib.selected.equals("None")) {
         SSM.useComparisonMode = false;
      } else {
         SSM.useComparisonMode = true;
      }
      if (SSM.stopPicking != 0) return;
      
      
      // Handling vehicle make
      SSM.stopPicking = pickingScrollPane(mx, my, makeScroll, SSM.makeAttrib, 
            modelScroll, SSM.modelAttrib,   // level 2
            yearScroll, SSM.yearAttrib      // level 3
      ); if (SSM.stopPicking != 0) return;
      SSM.stopPicking = pickingScrollPane(mx, my, c_makeScroll, SSM.c_makeAttrib, 
            c_modelScroll, SSM.c_modelAttrib,   // level 2
            c_yearScroll, SSM.c_yearAttrib      // level 3
      ); if (SSM.stopPicking != 0) return; 
      
      
      // Handling vehicle model
      SSM.stopPicking = pickingScrollPane(mx, my, modelScroll, SSM.modelAttrib,
            yearScroll, SSM.yearAttrib      // level 3
      ); if (SSM.stopPicking != 0) return;
      SSM.stopPicking = pickingScrollPane(mx, my, c_modelScroll, SSM.c_modelAttrib,
            c_yearScroll, SSM.c_yearAttrib      // level 3
      ); if (SSM.stopPicking != 0) return;
      
      
      // Handling vehicle year
      SSM.stopPicking = pickingScrollPane(mx, my, yearScroll, SSM.yearAttrib);
      if (SSM.stopPicking != 0) return;
      SSM.stopPicking = pickingScrollPane(mx, my, c_yearScroll, SSM.c_yearAttrib);
      if (SSM.stopPicking != 0) return;
      
      
      // Transitions
      scrollPaneTransition(mx, my, manufactureScroll, SSM.manufactureAttrib);
      scrollPaneTransition(mx, my, makeScroll, SSM.makeAttrib);
      scrollPaneTransition(mx, my, modelScroll, SSM.modelAttrib);
      scrollPaneTransition(mx, my, yearScroll, SSM.yearAttrib);
      
      scrollPaneTransition(mx, my, c_manufactureScroll, SSM.c_manufactureAttrib);
      scrollPaneTransition(mx, my, c_makeScroll, SSM.c_makeAttrib);
      scrollPaneTransition(mx, my, c_modelScroll, SSM.c_modelAttrib);
      scrollPaneTransition(mx, my, c_yearScroll, SSM.c_yearAttrib);      
      
      scrollPaneTransition(mx, my, perspectiveScroll, SSM.perspectiveAttrib);
      
      
      
      // Now test the switch intersection
      if (DCUtil.between(mx, SSM.aggregationAnchorX+aggSwitch.width, SSM.aggregationAnchorX+aggSwitch.width+aggSwitch.buttonWidth)) {
         if (DCUtil.between(my, SSM.aggregationAnchorY, SSM.aggregationAnchorY+aggSwitch.height)) {
            System.out.println("Hit a switch...");
            aggSwitch.state = ! aggSwitch.state;
            if (aggSwitch.state == true) { SSM.useAggregate = true; }
            else { SSM.useAggregate = false; }
         }
      }
      
      
   }
   
   
   public void resetData() {
      manufactureScroll.tagList.clear();
      makeScroll.tagList.clear();
      modelScroll.tagList.clear();
      yearScroll.tagList.clear();
      
      c_manufactureScroll.tagList.clear();
      c_makeScroll.tagList.clear();
      c_modelScroll.tagList.clear();
      c_yearScroll.tagList.clear();      
      
      perspectiveScroll.tagList.clear();
      
      int startIdx = SSM.startIdx;
      int endIdx   = SSM.endIdx;
      
      // Set up the text for perspective
      Hashtable<String, Integer> perspectiveHash = new Hashtable<String, Integer>(); //blah
      perspectiveHash.put("Month", 0);
      perspectiveHash.put("Component", 0);
      perspectiveHash.put("Global", 0);
      this.resetPane(perspectiveHash, perspectiveScroll, SSM.perspectiveAttrib);
      
      
      // Set up default 
      Hashtable<String, Integer> manufactureHash = this.getHierFilter(startIdx, endIdx);
      //DCUtil.removeLowerBound(manufactureHash, 100);
      this.resetPane(manufactureHash, manufactureScroll, SSM.manufactureAttrib);
      
      Hashtable<String, Integer> makeHash = this.getHierFilter(startIdx, endIdx, manufactureScroll);
      //DCUtil.removeLowerBound(makeHash, 20);
      this.resetPane(makeHash, makeScroll, SSM.makeAttrib);
      
      Hashtable<String, Integer> modelHash = this.getHierFilter(startIdx, endIdx, manufactureScroll, makeScroll);
      //DCUtil.removeLowerBound(modelHash, 20);
      this.resetPane(modelHash, modelScroll, SSM.modelAttrib);
      
      Hashtable<String, Integer> yearHash = this.getHierFilter(startIdx, endIdx, manufactureScroll, makeScroll, modelScroll);
      //DCUtil.removeLowerBound(yearHash, 20);
      this.resetPane(yearHash, yearScroll, SSM.yearAttrib);

            
      
      // Set up the comparisons
      Hashtable<String, Integer> c_manufactureHash = this.getHierFilter(startIdx, endIdx);
      //DCUtil.removeLowerBound(c_manufactureHash, 100);
      this.resetPane(c_manufactureHash, c_manufactureScroll, SSM.c_manufactureAttrib);
      
      Hashtable<String, Integer> c_makeHash = this.getHierFilter(startIdx, endIdx, c_manufactureScroll);
      //DCUtil.removeLowerBound(c_makeHash, 20);
      this.resetPane(c_makeHash, c_makeScroll, SSM.c_makeAttrib);
      
      Hashtable<String, Integer> c_modelHash = this.getHierFilter(startIdx, endIdx, c_manufactureScroll, c_makeScroll);
      //DCUtil.removeLowerBound(c_modelHash, 20);
      this.resetPane(c_modelHash, c_modelScroll, SSM.c_modelAttrib);
      
      Hashtable<String, Integer> c_yearHash = this.getHierFilter(startIdx, endIdx, c_manufactureScroll, c_makeScroll, c_modelScroll);
      //DCUtil.removeLowerBound(c_yearHash, 20);
      this.resetPane(c_yearHash, c_yearScroll, SSM.c_yearAttrib);
      
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset a scroll-able panel
   ////////////////////////////////////////////////////////////////////////////////
   public void resetPane(Hashtable<String, Integer> table, DCScrollPane widget, PaneAttrib attrib) {
      int counter = 0;
      int prev= -1;
      
      // Order matters : None = 0, All = 1;
      if (attrib.hasNoneSelection == true) {
         widget.tagList.add( new GTag(10.0f, (counter+1)*DCScrollPane.spacing, counter*DCScrollPane.spacing, "None", "None", -1));
         counter++;
      }
      if (attrib.hasAllSelection == true) {
         widget.tagList.add( new GTag(10.0f, (counter+1)*DCScrollPane.spacing, counter*DCScrollPane.spacing, "All", "All", -1));
         counter++;
      }
      
      
      Vector<String> list = new Vector<String>();
      
      // Add to list and also calculate the max per the category
      int max = 0;
      for (String s : table.keySet()) {
         list.add( s );  
         if (max < table.get(s) ) max = table.get(s);
      }
      widget.maxValue = max;
      
      Collections.sort(list);
      for (int i=0; i < list.size(); i++) {
         String txt = "";
         String s = list.elementAt(i);
         
         // HACK
         String s2 = "";
         if (s.length() > 20) {
            s2 = s.substring(0, 20);
            txt = s2 + " (" + table.get(s) + ")";
         } else {
            txt = s + " (" + table.get(s) + ")";
         }
         
         GTag t = new GTag(10.0f, (counter+1)*DCScrollPane.spacing, counter*DCScrollPane.spacing, txt, s, table.get(s));
         if (t.val.equals(attrib.selected)) {
            prev= i;
            widget.currentStr = t.val;
         }
         widget.tagList.add( t );
         counter++;   
      }
      widget.texPanelHeight = widget.tagList.lastElement().y;
      attrib.textureHeight = widget.texPanelHeight;
      attrib.height = Math.min(attrib.textureHeight, SSM.defaultScrollHeight);
      
      if (widget.height > 0) widget.height = attrib.height;
      widget.dirty = true;       
      if (prev < 0) {
         
         // If has None option
         if (attrib.hasNoneSelection == true && attrib.hasAllSelection == true) {
//System.out.println("blah blah blah >> " + attrib.selected);            
            // All
            if (attrib.selected == null) {
               widget.current = 1;   
               widget.currentStr = widget.tagList.elementAt(1).val;
               attrib.selected = null;
               attrib.yOffset = attrib.height;
            } else {
               widget.current = 0;   
               widget.currentStr = widget.tagList.elementAt(0).val;
               attrib.yOffset = attrib.height;
            }
         } else {
            widget.current = 0;   
            widget.currentStr = widget.tagList.elementAt(0).val;
            attrib.selected = null;
            attrib.yOffset = attrib.height;
         }
      }
      
      // Final adjustment
      //if (prev>= 0 && defaultSelection == true) {
      if (prev>= 0 ) {
         float tempY = 0;
         float additionalSpacing = 0;
         if (attrib.hasAllSelection == true) additionalSpacing += DCScrollPane.spacing;
         tempY = widget.tagList.elementAt(prev).y + additionalSpacing ;
         
         //   tempY = widget.tagList.elementAt(prev).y + DCScrollPane.spacing;
         //else
         //   tempY = widget.tagList.elementAt(prev).y;
         attrib.yOffset = Math.max( tempY, attrib.height);
      }
      
      // If the list only contains one element ==> IE: "ALL", then that means we did not select an item
      // in the upper hierarchy somewhere. So this means that we might as well hide it. 
      // We will still do the calculations above, just so everything is synchronized
      //if (widget.tagList.size() == 1 ) {
      if (list.size() == 0 ) {
         widget.visible = false;   
         widget.height = 0.0f;
         attrib.height = 0.0f;
         attrib.active = false;
      } else {
         widget.visible = true;
      }
      //System.out.println(widget.label + " " + widget.tagList.size());
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get a list of available filters, based on hierarchical order
   //  - startIdx : starting time frame
   //  - endIdx   : ending time frame
   //  - ancestor : higher level options that is applied to the current filter 
   ////////////////////////////////////////////////////////////////////////////////
   public Hashtable<String, Integer> getHierFilter(int startIdx, int endIdx, Object... ancestor) {
      Hashtable<String, Integer> result = new Hashtable<String, Integer>();
      
      for (int i=startIdx; i <= endIdx; i++) {
         String dtStr = CacheManager.instance().getTimeByIndex(i); 
         int month = Integer.parseInt(dtStr.substring(4,6)) - 1;
         if (month < SSM.startMonth || month > SSM.endMonth) continue;      
         
         QueryObj root = CacheManager.instance().queryTableU.elementAt(i);
         QueryObj qobj = root;
         QueryObj current = root;
         
         for (int j=0; j < ancestor.length; j++) {
            qobj = current.get( ((DCScrollPane)ancestor[j]).currentStr);
            if (qobj == null) break;
            current = qobj;
         }
         if (qobj == null) continue;
         
         for (String s: qobj.children.keySet()) {
            int count = qobj.children.get(s).count;
            if (result.containsKey(s)) {
               int prev = result.get(s);
               result.put(s, count+prev);
            } else {
               result.put(s, count);
            }
         } // end for qobj
         
      }
      
      return result;
   }   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Hack: Just to test if this works
   // Check if the comparison mode is active
   // if comparisonMode == true  then adjust the anchorX and anchorY to show the comparison dropdown
   // if comparisonMode == false then hide the comparison mode
   ////////////////////////////////////////////////////////////////////////////////
   public void checkComparisonMode() {
      /*
      SSM.c_manufactureAttrib.anchorY = 80;
      SSM.c_makeAttrib.anchorY = 80;
      SSM.c_modelAttrib.anchorY = 80;
      SSM.c_yearAttrib.anchorY = 80;
      
      SSM.manufactureAttrib.anchorY = 50;
      SSM.makeAttrib.anchorY = 50;
      SSM.modelAttrib.anchorY = 50;
      SSM.yearAttrib.anchorY = 50;
      */
      
      manufactureScroll.anchorY = SSM.manufactureAttrib.anchorY;
      makeScroll.anchorY  = SSM.makeAttrib.anchorY;
      modelScroll.anchorY = SSM.modelAttrib.anchorY;
      yearScroll.anchorY  = SSM.yearAttrib.anchorY;
      c_manufactureScroll.anchorY = SSM.c_manufactureAttrib.anchorY;
      c_makeScroll.anchorY  = SSM.c_makeAttrib.anchorY;
      c_modelScroll.anchorY = SSM.c_modelAttrib.anchorY;
      c_yearScroll.anchorY  = SSM.c_yearAttrib.anchorY;
   }    
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Handles the animated transition for scrolling panels, specifically those
   // in the hierarchical search filter
   ////////////////////////////////////////////////////////////////////////////////
   public void scrollPaneTransition(float mx, float my, DCScrollPane widget, PaneAttrib attrib ) {
      if (DCUtil.between(mx, widget.anchorX, widget.anchorX+SSM.scrollWidth)) {
         if (DCUtil.between(my, widget.anchorY-20, widget.anchorY)) {
            attrib.active = ! attrib.active;
            SSM.stopPicking = 1;
            
            if (attrib.active) {
               widget.animator = PropertySetter.createAnimator(SSM.SCROLL_DURATION, widget, "height", new FloatEval(), widget.height, attrib.height); 
               widget.animator.start();
            } else {
               widget.animator = PropertySetter.createAnimator(SSM.SCROLL_DURATION, widget, "height", new FloatEval(), widget.height, 0.0f); 
               widget.animator.start();
            }
         }
      }   
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Handles select action for hierarchical scrolling panel filters
   ////////////////////////////////////////////////////////////////////////////////
   public int pickingScrollPane(float mx, float my, DCScrollPane widget, PaneAttrib attrib, Object ...childrenPair) {
      if (DCUtil.between(mx, attrib.anchorX, attrib.anchorX+SSM.scrollWidth)) {
         //if (DCUtil.between(my, attrib.anchorY, attrib.anchorY+attrib.height)) {
         boolean yCheck = false;
         if (widget.direction == DCScrollPane.UP ) {
            yCheck = DCUtil.between(my, attrib.anchorY, attrib.anchorY+widget.height); 
         } else {
            yCheck = DCUtil.between(my, attrib.anchorY-20-widget.height, attrib.anchorY-20);
            System.out.println( (attrib.anchorY-20-widget.height) + " " + (attrib.anchorY-20));
         }
                  
         //if (DCUtil.between(my, attrib.anchorY, attrib.anchorY+widget.height)) {
         if (yCheck) {
            
            // 1) Calculate the texture coordinate
            float texX = mx - attrib.anchorX;
            float texY = 0; 
            if (widget.direction == DCScrollPane.UP)
               texY = my - attrib.anchorY;
            else
               texY = widget.height - Math.abs(my - (widget.anchorY-20));
            
            // 2) Adjust for Y-offset
            texY = attrib.yOffset - (texY);
            System.out.println("Tex : " + texX + " " + texY);
            
            for (int i=0; i < widget.tagList.size(); i++) {
               GTag t = widget.tagList.elementAt(i);                
               // Window system is upside down
               if (texY >= t.yPrime && texY <= t.y) {
                  SSM.dirtyDateFilter = 1;
                  widget.current = i; 
                  widget.currentStr = t.val;
                  widget.dirty  = true;
                  
                  SSM.dirty = 1;
                  SSM.dirtyGL = 1;
                  SSM.refreshMagicLens = true;
                  
                  /*
                  if (attrib.hasAllSelection == false) {
                     attrib.selected = t.val; 
                  } else {
                     attrib.selected = i==0? null:t.val; 
                  }
                  */
                  if (t.val.equals("All")) {
                     attrib.selected = null;
                  } else {
                     attrib.selected = t.val;
                  }
                  
System.out.println(">>>>>>>>>>>>>>>> " + i + " " + t.val);                  
                  
                  // Clear the children
                  for (int j=0; j < childrenPair.length; j+=2) {
                     ((DCScrollPane)childrenPair[j]).current = 0;
                     ((PaneAttrib)childrenPair[j+1]).selected = null;
                  }
                  System.out.println(widget.label + " >>>>>>>>>>>>>" + i);
                  break;
               }
            }            
            return 1;
         }
      } 
      return 0;
   }
   
   
   
   public void renderSwitch(GL2 gl2) {
      aggSwitch.render(gl2);
      
      
      perspectiveScroll.masterVisible = true;
      perspectiveScroll.visible = true;
      perspectiveScroll.yoffset = SSM.perspectiveAttrib.yOffset;
      perspectiveScroll.render(gl2);
      
      perspectiveLabel.anchorX = SSM.perspectiveAttrib.anchorX - perspectiveLabel.width;
      perspectiveLabel.anchorY = SSM.perspectiveAttrib.anchorY - 20;
      perspectiveLabel.render(gl2);      
   }
   
   
   
   public void renderScrollFilter(GL2 gl2) {
      ////////////////////////////////////////////////////////////////////////////////
      // Rener the combo boxes
      ////////////////////////////////////////////////////////////////////////////////
      GraphicUtil.setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight, -10, 10); {
      //setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
         // Update the yoffset before rendering
         gl2.glEnable(GL2.GL_DEPTH_TEST);
         manufactureScroll.yoffset = SSM.manufactureAttrib.yOffset;
         makeScroll.yoffset  = SSM.makeAttrib.yOffset;
         modelScroll.yoffset = SSM.modelAttrib.yOffset;
         yearScroll.yoffset  = SSM.yearAttrib.yOffset;
         
         manufactureScroll.render(gl2);   
         makeScroll.render(gl2);
         modelScroll.render(gl2);
         yearScroll.render(gl2);
         
         
         c_manufactureScroll.yoffset = SSM.c_manufactureAttrib.yOffset;
         c_makeScroll.yoffset  = SSM.c_makeAttrib.yOffset;
         c_modelScroll.yoffset = SSM.c_modelAttrib.yOffset;
         c_yearScroll.yoffset  = SSM.c_yearAttrib.yOffset;
         
         c_manufactureScroll.render(gl2);   
         c_makeScroll.render(gl2);
         c_modelScroll.render(gl2);
         c_yearScroll.render(gl2);
         
         

         
         
         
         
         gl2.glDisable(GL2.GL_DEPTH_TEST);
         
         float ax;
         float ay;
         
         gl2.glEnable(GL2.GL_BLEND);
//         GraphicUtil.drawRoundedRect(gl2, SSM.filterControlAnchorX+30, manufactureScroll.anchorY - 10, 0, 
//               60, 10, 8, 6,
//               DCColour.fromDouble(0.68, 0.68, 0.68, 0.65).toArray(), 
//               DCColour.fromDouble(0.77, 0.77, 0.77, 0.65).toArray());
         
         filterTexture.anchorX = SSM.filterControlAnchorX;
         filterTexture.anchorY = manufactureScroll.anchorY - 15;
         filterTexture.render(gl2);
         
//         GraphicUtil.drawRoundedRect(gl2, SSM.c_filterControlAnchorX+30, c_manufactureScroll.anchorY - 10, 0, 
//               60, 10, 8, 6,
//               DCColour.fromDouble(0.68, 0.68, 0.68, 0.65).toArray(), 
//               DCColour.fromDouble(0.77, 0.77, 0.77, 0.65).toArray());
               
         c_filterTexture.anchorX = SSM.c_filterControlAnchorX;
         c_filterTexture.anchorY = c_manufactureScroll.anchorY - 15;
         c_filterTexture.render(gl2);
         
         
         // Draw in indicator so the users will known which colour is associated with 
         // which selection
         ax = SSM.filterControlAnchorX-20;
         ay = manufactureScroll.anchorY-20;
         gl2.glColor4fv(SchemeManager.comp_1.toArray(), 0);
         gl2.glBegin(GL2.GL_QUADS);    
            gl2.glVertex2f(ax, ay+3);
            gl2.glVertex2f(ax+15, ay+3);
            gl2.glVertex2f(ax+15, ay+20-3);
            gl2.glVertex2f(ax, ay+20-3);
         gl2.glEnd();
         
         ax = SSM.c_filterControlAnchorX-20;
         ay = c_manufactureScroll.anchorY-20;
         gl2.glColor4fv(SchemeManager.comp_2.toArray(), 0);
         gl2.glBegin(GL2.GL_QUADS);
            gl2.glVertex2f(ax, ay+3);
            gl2.glVertex2f(ax+15, ay+3);
            gl2.glVertex2f(ax+15, ay+20-3);
            gl2.glVertex2f(ax, ay+20-3);
         gl2.glEnd();
      }      
   }   
   
   
}
