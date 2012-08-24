package datastore;

import model.DCColour;

/////////////////////////////////////////////////////////////////////////////////
// Used the define and pick scheme and colour
/////////////////////////////////////////////////////////////////////////////////
public class SchemeManager {
   
   public static void main(String args[]) {
      int buckets = 90;
      float interval = 1.0f/90.0f;
      
      // Draws the distinct colours
      for (float i=0; i < buckets; i++) {
         DCColour c = SchemeManager.instance().getColour(1, i*interval, 1.0f);
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Gateway method for getting a colour based on frequency
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour getColour(Integer partId, float occurrence, float maxOccurrence) {
      if (SSM.colouringMethod == 1) return intensityRamp05(partId, occurrence, maxOccurrence, Red); 
      if (SSM.colouringMethod == 2) return intensityRamp05(partId, occurrence, maxOccurrence, Set3);
      
      /*
      switch (SSM.instance().colouringMethod) {
         case 0: return intensityRamp01(partId, occurrence, maxOccurrence);
         case 1: return intensityRamp02(partId, occurrence, maxOccurrence);
         case 2: return intensityRamp03(partId, occurrence, maxOccurrence);
         case 3: return intensityRamp04(partId, occurrence, maxOccurrence);
         case 4: return intensityRamp05(partId, occurrence, maxOccurrence);
      }
      */
      
      return null;
   }
   
   public int getScaleSize() {
      if (SSM.colouringMethod == 1) return Red.length;
      if (SSM.colouringMethod == 2) return Set3.length;
      return 0;      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Blue hue based on intensity from with a value range of 0.8f
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp01(Integer partId, float occurrence, float maxOccurrence) {
      float intensity = 0.0f; 
      if (partId == null || occurrence <= 0.0f) {
         return new DCColour(0.8f, 0.8f, 0.8f, 0.4f);
      } 
      intensity = 0.2f + (occurrence/maxOccurrence)*0.8f;
      
      return new DCColour(0.0f, intensity, 1.0f - intensity, intensity*0.5f);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a piece-wise ramp
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp02(Integer partId, float occurrence, float maxOccurrence) {
      float intensity = 0.0f;
      float val = 0.0f;
      if (partId == null || occurrence <= 0.0f) {
         return new DCColour(0.8f, 0.8f, 0.8f, 0.4f);
      }
      intensity = (occurrence/maxOccurrence);
      
      if (intensity > 0.5f) {
         val = (intensity - 0.5f)/0.5f;    
         return new DCColour(val, 1.0f - val, 0.0f, val);
      } else {
         val = (intensity)/0.5f; 
         return new DCColour(0.0f, val, val, val);
      }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Return a bucketed distribution for the colour scale
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp04(Integer partId, float occurrence, float maxOccurrence) {
      float intensity = 0.0f;
      float val = 0.0f;
      if (partId == null ) {
         return new DCColour(0.8f, 0.8f, 0.8f, 0.4f);
      }
      intensity = (occurrence/maxOccurrence);
      
      // Check which 'bucket' this value falls into
      int bucket =   (int)Math.floor(intensity * YellowGreenBlue.length);
      
      // sanity check
      bucket = bucket < 0 ? 0: bucket >= YellowGreenBlue.length ? (YellowGreenBlue.length-1) : bucket;
      return YellowOrangeRedFixed[bucket]; 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the bucketed colour scale with preset alpha values
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp05(Integer partId, float occurrence, float maxOccurrence, DCColour scale[]) {
      float intensity = 0.0f;
      float val = 0.0f;
      if (partId == null || occurrence == 0) {
         return DCColour.fromInt(255, 255, 255, 255);
      }
      intensity = (occurrence/maxOccurrence);
      //int bucket =   (int)Math.floor(intensity * Red.length);
      int bucket =   (int)Math.floor(intensity * scale.length);
      
      // sanity check
      //bucket = bucket < 0 ? 0: bucket >= Red.length ? (Red.length-1) : bucket;
      //return Red[bucket]; 
      bucket = bucket < 0 ? 0: bucket >= scale.length ? (scale.length-1) : bucket;
      return scale[bucket]; 
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Using colour scale from the color brewer 2.0 website
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp03(Integer partId, float occurrence, float maxOccurrence) {
      DCColour start  = new DCColour(255, 237, 160, 128);
      DCColour middle = new DCColour(254, 178, 76, 128);
      DCColour end    = new DCColour(240, 59, 32, 128);
      
      float intensity = (occurrence/maxOccurrence);
      float r, g, b, a;
      
      if (intensity < 0.5f) {
         r = start.r + (middle.r-start.r)*(0.5f-(0.5f-intensity)); 
         g = start.g + (middle.g-start.g)*(0.5f-(0.5f-intensity)); 
         b = start.b + (middle.b-start.b)*(0.5f-(0.5f-intensity)); 
      } else {
         r = middle.r + (end.r - middle.r)*intensity;
         g = middle.g + (end.g - middle.g)*intensity;
         b = middle.b + (end.b - middle.b)*intensity;
      }
      
      // Lower threshold alpha
      a = 0.1f + 0.9f*intensity;
      return  new DCColour(r, g, b, a);
   }
   
   
   
   
   
   public static DCColour related = DCColour.fromInt(240, 140, 10, 200);
   //ROYALBLUE2: java.awt.Color[r=61,g=130,b=246]
   
   public static DCColour selected    = DCColour.fromInt(1, 71, 189, 175); 
   public static DCColour unselected  = DCColour.fromInt(128, 128, 128, 128); 
   
   public static DCColour sparkline_guideline = new DCColour(0.0, 0.0, 0.0, 0.2);
   
   public static DCColour car_normal1        = new DCColour(0.3, 0.3, 0.3, 0.1);     
   public static DCColour component_selected = new DCColour(1.0f, 0.8f, 0.0f, 0.5f);  
   
   public static DCColour inactive = DCColour.fromDouble(0.8, 0.8, 0.8, 0.8);
   
   public static DCColour silhouette_default = new DCColour(0.6f, 0.6f, 0.6f, 0.12);
   
   public static DCColour gl_clear = new DCColour(1.0, 1.0 ,1.0 ,0.0);
   public static DCColour not_used = new DCColour(0.0, 0.0, 0.0, 0.0);
   
   
   // Outline/silhouette colour for comparison mode
   //public static DCColour comp_1 = DCColour.fromInt(174, 141, 195, 200);
   //public static DCColour comp_2 = DCColour.fromInt(127, 191, 123, 200);
   public static DCColour comp_1 =  new DCColour(255, 20, 195, 200);
   public static DCColour comp_2 =  new DCColour(0, 102, 0, 200);   
   
   
   public static DCColour textPane_normal   = new DCColour(0.3, 0.3, 0.3, 1.0);
   
   public static SchemeManager instance() {
      if (inst == null) inst = new SchemeManager();
      return inst;
   }
   
   protected SchemeManager() {}
   private static SchemeManager inst;
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Colour templates from colour brewer
   // The alpha values are my own stuff...just a linear growth scale
   // http://colorbrewer2.org/
   ////////////////////////////////////////////////////////////////////////////////
   public static DCColour[] YellowGreenBlue = new DCColour[] {
      DCColour.fromInt(255, 255, 217, 25),
      DCColour.fromInt(237, 248, 217, 50),
      DCColour.fromInt(199, 233, 180, 75),
      DCColour.fromInt(127, 205, 187, 100),
      DCColour.fromInt(65, 182, 196, 125),
      DCColour.fromInt(29, 145, 192, 150),
      DCColour.fromInt(34, 96, 168, 175),
      DCColour.fromInt(37, 52, 148, 200),
      DCColour.fromInt(8, 29, 88, 225)
   };
   public static DCColour[] YellowOrangeRedFixed = new DCColour[] {
      DCColour.fromInt(255, 255, 204, 100), 
      DCColour.fromInt(255, 237, 160, 100), 
      DCColour.fromInt(254, 217, 118, 100),
      DCColour.fromInt(254, 178, 76, 100), 
      DCColour.fromInt(253, 141, 60, 100), 
      DCColour.fromInt(252, 78, 42, 100),
      DCColour.fromInt(227, 26, 28, 100), 
      DCColour.fromInt(189, 0, 38, 100), 
      DCColour.fromInt(128, 0, 38, 100)       
   };
   public static DCColour[] YellowOrangeRedPreset = new DCColour[] {
      DCColour.fromInt(255, 255, 204, 40), 
      DCColour.fromInt(255, 237, 160, 50), 
      DCColour.fromInt(254, 217, 118, 60),
      DCColour.fromInt(254, 178, 76, 70), 
      DCColour.fromInt(253, 141, 60, 80), 
      DCColour.fromInt(252, 78, 42, 90),
      DCColour.fromInt(227, 26, 28, 100), 
      DCColour.fromInt(189, 0, 38, 110), 
      DCColour.fromInt(128, 0, 38, 120)       
   };   
   
   
   
   
   public static DCColour[] Red = new DCColour[] {
      /* Red orange yellow */
      DCColour.fromInt(254, 240, 217, 100),      
      DCColour.fromInt(253, 212, 158, 110),      
      DCColour.fromInt(253, 187, 132, 120),      
      DCColour.fromInt(252, 141, 89, 140),      
      DCColour.fromInt(227, 74, 51, 170),      
      DCColour.fromInt(179, 0, 0, 200)      
   };
   
   public static DCColour[] Set3 = new DCColour[] {
      // Set3 qualitative colour from colourbrewer   
      DCColour.fromInt(141, 211, 199, 100),
      DCColour.fromInt(255, 255, 179, 110),
      DCColour.fromInt(190, 186, 218, 120),
      DCColour.fromInt(251, 128, 114, 140),
      DCColour.fromInt(128, 177, 211, 170),
      DCColour.fromInt(253, 180, 98, 200)
   };
   
   
   
}
