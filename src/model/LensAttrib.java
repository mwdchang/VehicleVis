package model;

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

   
   // Attributes
   public int magicLensX;
   public int magicLensY;
   public int magicLensSelected;
   public float magicLensRadius;
   public int magicLensType;
   
   
   // This really shouldn't be here because we created
   // a circular dependency...but works for now
   public MagicLens mlen;
   
   
   public float nearPlane = SSM.nearPlane;
   public float farPlane  = SSM.farPlane;
   
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
   
   public DCTip tip = new DCTip(); // Show interactive information
   
   
   // Types
   public static int LENS_NORMAL = 0;
   public static int LENS_DEPTH  = 1;
   
   
}
