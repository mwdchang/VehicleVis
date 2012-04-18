package gui;

import java.awt.Color;
import java.awt.Font;

import javax.media.opengl.GL2;

import datastore.SSM;

import util.DCCamera;
import util.DCUtil;
import util.TextureFont;

/////////////////////////////////////////////////////////////////////////////////
// Basically just a window containing text that can be turned on or off
/////////////////////////////////////////////////////////////////////////////////
public class StatusWindow {
   
   public static void init(GL2 gl2) {
      tf = new TextureFont();
      tf.height = 600;
      tf.width  = 350;
   }
   
   public static void render(GL2 gl2) {
      // Render the box
      gl2.glColor4d(0.6, 0.6, 0.6, 0.4);
      gl2.glBegin(GL2.GL_QUADS);
         gl2.glVertex2d(tf.anchorX, tf.anchorY);
         gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY);
         gl2.glVertex2d(tf.anchorX+tf.width, tf.anchorY+tf.height);
         gl2.glVertex2d(tf.anchorX, tf.anchorY+tf.height);
      gl2.glEnd();
      tf.render(gl2);
   }
   
   public static void clear() {
      tf.clearMark();
   }
   
   public static void addText(String txt) {
      tf.addMark( txt, c, f, 5, tf.height-20-tf.marks.size()*buffer);
   }   
   
   public static String convert(boolean b) {
      return b? "On" : "Off";
   }
   
   public static void update() {
      clear();
      addText("Depth Peeling  : " + convert(SSM.instance().useDualDepthPeeling));
      addText("Glow Filter    : " + convert(SSM.instance().useGlow));
      addText("Colour Method  : " + SSM.instance().colouringMethod);
      addText("Showing Label  : " + convert(SSM.instance().showLabels));
      addText("Use Full Time  : " + convert(SSM.instance().useFullTimeLine));
      addText("Constant Alpha : " + convert(SSM.instance().useConstantAlpha)); 
      addText("Local mode     : " + convert(SSM.instance().useLocalFocus));
      addText("Compare mode   : " + convert(SSM.instance().useComparisonMode));
      addText("Temp Flag      : " + convert(SSM.instance().useFlag));
      addText("OIT Pass       : " + SSM.g_numPasses);
      
      switch (SSM.instance().chartMode) {
      case SSM.CHART_MODE_BY_MONTH_MAX: { addText("Chart Mode      : Month Max"); break; }
      case SSM.CHART_MODE_BY_GLOBAL_MAX: { addText("Chart Mode      : Global Max"); break; }
      case SSM.CHART_MODE_BY_COMPONENT_MAX: { addText("Chart Mode      : Component Max"); break; }
      }
      
      addText("================="); 
      addText("Num selected   : " + SSM.instance().selectedGroup.size());
      addText("Num related    : " + SSM.instance().relatedList.size());
      addText("================="); 
      //addText("Camera: " + DCCamera.instance().eye);
      
      
   }
   
   public static TextureFont tf;
   public static Font f  =  DCUtil.loadFont("din1451m.ttf", Font.PLAIN, 12f);
   public static Color c = Color.BLACK;
   public static float tipX;
   public static float tipY;
   public static float TIP_LENGTH = 30;
   public static boolean visible = false;
   public static float buffer = 15;   
}
