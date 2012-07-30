package model;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import TimingFrameExt.FloatEval;

import gui.DCTip;
import util.TextureFont;
import datastore.SSM;

/////////////////////////////////////////////////////////////////////////////////
// A class to hold lens attribute - for supporting
// multiple lens simuultaneously.
//
// Each lens will have to register an effect (magicLensType), which will have to
// correspond to a MagicLens recording instance.
// 
/////////////////////////////////////////////////////////////////////////////////
public class LensAttrib {

   public LensAttrib() {
      magicLensX = 0;
      magicLensY = 0;
      magicLensSelected = 0;
      magicLensRadius = 100;
      magicLensType = 0;
   }
   
   public LensAttrib(int mx, int my) {
      magicLensX = mx;
      magicLensY = my;
      magicLensRadius = 100;
      magicLensType = 0;
   }
   
   public LensAttrib(int mx, int my, float r) {
      magicLensX = mx;
      magicLensY = my;
      magicLensRadius = r;
      magicLensType = 0;
   }
   
   public LensAttrib(int mx, int my, float r, int t) {
      magicLensX = mx;
      magicLensY = my;
      magicLensRadius = r;
      magicLensType = t;
   }
   
   @Override
   public boolean equals(Object o) {
      return ((LensAttrib)o).magicLensX == magicLensX && ((LensAttrib)o).magicLensY == magicLensY;   
   }

   
   // Attributes
   public int magicLensX;
   public int magicLensY;
   public int magicLensSelected;
   public float magicLensRadius;
   public int magicLensType;
   
   public boolean handleSelected = false;
   public boolean rimSelected = false;
   
   
   // This really shouldn't be here because we created
   // a circular dependency...but works for now
   public MagicLens mlen;
   
   public float zoomFactor = 1.0f;
   public float nearPlane = SSM.nearPlane;
   public float farPlane  = SSM.farPlane;
   
   public float handleAngle = 1.0f;
   
   // Number of labels to display
   public int start = 0;
   public final int numToDisplay = 8;
   public boolean renderTop    = false;
   public boolean renderBottom = false; 
   
   public TextureFont t_top;
   public TextureFont t_bottom;
   public int l_top;
   public int l_bottom;
   
   public int displayList;
   
   public float borderSize = 2;
   public float borderSizeNormal   = 2;
   public float borderSizeSelected = 4;
   public float getBorderSize() { return borderSize; }
   public void setBorderSize( float v ) { borderSize = v; }
   
   
   public DCTip tip = new DCTip(); // Show interactive information
   
   //public Animator selectAnimator = PropertySetter.createAnimator(300, this, "borderSize", new FloatEval(), borderSizeSelected);
   //public Animator deSelectAnimator = PropertySetter.createAnimator(300, this, "borderSize", new FloatEval(), borderSizeNormal);
   
   public Animator selectAnimator   = null;
   public Animator deSelectAnimator = null;
   
   // Types
   public static int LENS_NORMAL = 0;
   public static int LENS_DEPTH  = 1;
   
   
   // To keep track of where the finger is touching for TUIO
   // This is not used for desktop applications
   public int offsetX = 0;
   public int offsetY = 0;
   
   
   
}
