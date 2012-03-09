package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Vector;

import model.DCColour;

import util.DCUtil;
import util.DWin;
import util.StringUtil;

import com.jogamp.opengl.util.awt.TextureRenderer;

import datastore.CacheManager;
import datastore.HierarchyTable;
import datastore.SSM;
import datastore.SchemeManager;
import db.DCDoc;
import db.DCTag;


/////////////////////////////////////////////////////////////////////////////////
// Text pane is a self contained texture that holds text information
/////////////////////////////////////////////////////////////////////////////////
public class TextPane {
   
   public TextPane() {
      // This is not the "actual" constructor, but we will use this to get a
      // graphics context, which we will use to get the fontmetrics
      texture = new TextureRenderer((int)textPaneWidth, (int)textPaneHeight, true, true);
      g2d = texture.createGraphics();
      g2d.setFont(fontArial);
      fm = g2d.getFontMetrics();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Split each document into tokens and calculate the position
   ////////////////////////////////////////////////////////////////////////////////
   public void calculate() {
      float tokenHeight = fontHeight; // just testing
      float ycursor = 0;
      float xcursor = 0;
      
      // Just exit if nothing is selected
      if (SSM.instance().selectedGroup.size() <= 0) return;
      
      
      Vector<Integer> keyList = new Vector<Integer>();
      if (SSM.instance().useAggregate == true) {
         keyList = HierarchyTable.instance().getAgg(SSM.instance().selectedGroup);
      } else {
         //keyList.add( SSM.instance().selectedGroup );   
         keyList.addAll( SSM.instance().selectedGroup.values() );   
      }
      
      
      tagList.clear();
      
      for (int i=0; i < documentList.size(); i++) {
         ycursor += tokenHeight;
         xcursor = 0;
         DCDoc d = documentList.elementAt(i);       
         
         String idstr = "[" + DCUtil.formatDateStr(d.datea) + "] " + d.mfr + " -" + d.make + " -" + d.model + " -" + d.year ;
         //String idstr = "[" + d.docId + " >" + d.mfr + " >" + d.make + " >" + d.model + " >" + d.year +  "] ";
         
         
         float tmp0 = fm.stringWidth(idstr);
         Tag tag = new Tag(xcursor, ycursor, tokenHeight, idstr);
         tag.fn = fontArialBold;
         
         // Text colour with accordance to comparison mode ?
         if (SSM.instance().useComparisonMode == true) {
            String s = SSM.instance().manufactureAttrib.selected == null? "" : SSM.instance().manufactureAttrib.selected;  
            s += SSM.instance().makeAttrib.selected  == null? "" : SSM.instance().makeAttrib.selected;
            s += SSM.instance().modelAttrib.selected == null? "" : SSM.instance().modelAttrib.selected;
            s += SSM.instance().yearAttrib.selected  == null? "" : SSM.instance().yearAttrib.selected;
            String c = d.mfr + d.make + d.model + d.year;
            
//            System.out.println("In Text Panel : " + s + "<>" + c);
            if (s != null && c.contains(s)) {
               tag.c = SchemeManager.comp_1;   
            } else {
               tag.c = SchemeManager.comp_2; 
            }
            
         }
         tagList.add(tag);
         ycursor += tokenHeight;
         //xcursor += tmp0;
         
         
         Vector<String> s = StringUtil.splitSpace(d.txt);
         Vector<DCTag> tlist = CacheManager.instance().tagTable.get(d.docId);            
         
         // Token placement
         for (int j=0; j < s.size(); j++) {
            String str = s.elementAt(j) +  " "; // Buffer an additional space
            float tmp = fm.stringWidth(str);
            if (xcursor + tmp > textPaneWidth) {
               xcursor = 0;
               ycursor += tokenHeight;
            }
            Tag t = new Tag(xcursor, ycursor, tokenHeight, str);
            
            // Check highlighting semantics
            for (int x=0; x < tlist.size(); x++) {
               if (tlist.elementAt(x).contains(j)) {
                  //if (tlist.elementAt(x).groupId == SSM.instance().selectedGroup) {
                  if (keyList.contains(tlist.elementAt(x).groupId)) {
                     //t.c = SchemeManager.textPane_selected;
                     t.c = SchemeManager.colour_blue;
                  } else {
                     t.c = SchemeManager.colour_related;
                  }
                  //t.fn = fontArialBold;
               }
            }
            
            tagList.add(t);
            xcursor += tmp;
         }
         ycursor += tokenHeight;
      }
      
      // Adjust the Y with respect to the ycurosr 
      // We are going to fit it all in even it means
      // expanding the default values
      textPaneHeight = ycursor;
      
      
      textPaneHeight = Math.max(1, textPaneHeight); // prevent JOGL from complaining about 0 size texture
      textPaneWidth =  Math.max(1, textPaneWidth);  // prevent JOGL from complaining about 0 size texture
      
      DWin.instance().msg("Panel height is : " + textPaneHeight);
      if (textPaneHeight > 8192) {
         DWin.instance().error("Cannot support texture height > 8129");   
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the contents of the taglist to a GL texture
   ////////////////////////////////////////////////////////////////////////////////
   public void renderToTexture(Color c) {
      // Recreate the textureRenderer to make sure we have the write context  
      texture = null;
      texture = new TextureRenderer((int)textPaneWidth, (int)textPaneHeight, true, true);
      g2d = texture.createGraphics();
      //g2d.setFont(fontArial);
      
      // Setup anti-aliasing fonts
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.setColor(c);
      
      // Render the tags
      for (int i=0; i < tagList.size(); i++) {
         Tag t = tagList.elementAt(i);
         if (t.fn != null) {
            g2d.setFont(t.fn);
         } else {
            g2d.setFont(fontArial); 
         }
         g2d.setColor(t.c.convertToAWT());
         g2d.drawString(t.s, t.x, t.y);
      }
   }
   
   
   // Default
   //public float textPaneWidth = 300.0f;
   public float textPaneWidth = 450.0f;
   public float textPaneHeight = 900.0f;
   public float fontHeight = 14.0f;

   public TextureRenderer texture; 
   public Graphics2D g2d;
   public static Font fontArial = new Font( "Arial", Font.PLAIN, 12);   
   public static Font fontArialBold = new Font( "Arial", Font.BOLD, 12);
   public FontMetrics fm;
   
   // documentList holds the original document, tag list hods the actual rendering components
   public Vector<DCDoc> documentList = new Vector<DCDoc>();
   public Vector<Tag> tagList = new Vector<Tag>();
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Inner class to hold position and other attributes includeing colour and bounding box
   ////////////////////////////////////////////////////////////////////////////////
   class Tag {
      public Tag(float _x, float _y, float _yp, String _s) {
         x = _x;   
         y = _y;
         s = _s;
         yPrime = _yp;
         
         // Derived 
         c = SchemeManager.textPane_normal;
         sClean = StringUtil.cleanString(_s);
      }
      public String toString() {
         return "[" + x + ", " + y + "] " + s;  
      }
      float x; 
      float y;
      float yPrime;
      String s;      
      String sClean;
      DCColour c;
      Font fn = null;
   }
   
   
}
