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
      switch (SSM.instance().colouringMethod) {
         case 0: return intensityRamp01(partId, occurrence, maxOccurrence);
         case 1: return intensityRamp02(partId, occurrence, maxOccurrence);
         case 2: return intensityRamp03(partId, occurrence, maxOccurrence);
         case 3: return intensityRamp04(partId, occurrence, maxOccurrence);
         case 4: return intensityRamp05(partId, occurrence, maxOccurrence);
      }
      return null;
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
      
//      return new DCColour(0.0f, intensity, 1.0f, intensity*0.5f);
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
      
      // Boot strap engine
      //if ( partId == 117 ) { 
      //   System.out.println("Intensity: " + intensity + "  MaxOccurrence : " + maxOccurrence);
      //}
      
      // Check which 'bucket' this value falls into
      // multiply by the size of the scale, then floor the result
      //int bucket =   (int)Math.floor(intensity * YellowGreenBlue.length-1);
      //int bucket =   (int)Math.round(intensity * YellowGreenBlue.length-1);
      int bucket =   (int)Math.floor(intensity * YellowGreenBlue.length);
      
      // sanity check
      //bucket = bucket < 0 ? 0: bucket;
      bucket = bucket < 0 ? 0: bucket >= YellowGreenBlue.length ? (YellowGreenBlue.length-1) : bucket;
      
      return YellowOrangeRedFixed[bucket]; 
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the bucketed colour scale with preset alpha values
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp05(Integer partId, float occurrence, float maxOccurrence) {
      float intensity = 0.0f;
      float val = 0.0f;
      if (partId == null ) {
         return new DCColour(0.8f, 0.8f, 0.8f, 0.4f);
      }
      intensity = (occurrence/maxOccurrence);
      //int bucket =   (int)Math.round(intensity * YellowGreenBlue.length);
      int bucket =   (int)Math.floor(intensity * YellowGreenBlue.length);
      
      
      //System.out.println( intensity + " " + bucket );
      
      // sanity check
      bucket = bucket < 0 ? 0: bucket >= YellowGreenBlue.length ? (YellowGreenBlue.length-1) : bucket;
      return YellowOrangeRedPreset[bucket]; 
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Using colour scale from the color brewer 2.0 website
   ////////////////////////////////////////////////////////////////////////////////
   public DCColour intensityRamp03(Integer partId, float occurrence, float maxOccurrence) {
      DCColour start  = new DCColour(255, 237, 160, 128);
      DCColour middle = new DCColour(254, 178, 76, 128);
      DCColour end    = new DCColour(240, 59, 32, 128);
      /*
      DCColour start = colour_blue;
      DCColour middle = colour_green;
      DCColour end  = colour_red;
      */
      
      float intensity = (occurrence/maxOccurrence);
      float r, g, b, a;
      
      if (intensity < 0.5f) {
         
         r = start.r + (middle.r-start.r)*(0.5f-(0.5f-intensity)); 
         g = start.g + (middle.g-start.g)*(0.5f-(0.5f-intensity)); 
         b = start.b + (middle.b-start.b)*(0.5f-(0.5f-intensity)); 
         
         /*
         r = start.r + (middle.r-start.r)*(intensity/0.5f); 
         g = start.g + (middle.g-start.g)*(intensity/0.5f); 
         b = start.b + (middle.b-start.b)*(intensity/0.5f); 
         */
      } else {
         r = middle.r + (end.r - middle.r)*intensity;
         g = middle.g + (end.g - middle.g)*intensity;
         b = middle.b + (end.b - middle.b)*intensity;
         /*
         r = middle.r + (end.r - middle.r)*(intensity-0.5f)/0.5f;
         g = middle.g + (end.g - middle.g)*(intensity-0.5f)/0.5f;
         b = middle.b + (end.b - middle.b)*(intensity-0.5f)/0.5f;
         */
      }
      
      // Lower threshold alpha
      a = 0.1f + 0.9f*intensity;
      
      // Unaltered alpha
      //a = intensity;
      
      // Constant alpha
      //a = 0.8f;
      return  new DCColour(r, g, b, a);
   }
   
   
   
   
   // Default colour and stuff
   public static DCColour colour_red    = new DCColour(1, 0, 0, 0.5); 
   public static DCColour colour_green  = new DCColour(0, 1, 0, 0.5); 
   public static DCColour colour_blue   = new DCColour(0.1, 0.1, 0.9, 0.5); 
   
   public static DCColour colour_related = new DCColour(0.1, 0.4, 0.8, 0.5);
   
   public static DCColour range_selected = colour_blue; 
   public static DCColour range_normal   = new DCColour(0.5, 0.5, 0.5, 0.5);
   
   public static DCColour sparkline_guideline = new DCColour(0.6, 0.6, 0.6, 0.6);
   
   public static DCColour car_normal1        = new DCColour(0.3, 0.3, 0.3, 0.1);     
   public static DCColour component_selected = new DCColour(1.0f, 0.8f, 0.0f, 0.5f);  
   
   
   public static DCColour silhouette_default = new DCColour(0.5f, 0.5f, 0.5f, 0.15);
   
   public static DCColour gl_clear = new DCColour(1.0, 1.0 ,1.0 ,0.0);
   public static DCColour not_used = new DCColour(0.0, 0.0, 0.0, 0.0);
   
   
   // Outline/silhouette colour for comparison mode
   public static DCColour comp_1 = new DCColour(1.0, 1.0, 0.0, 0.5);
   public static DCColour comp_2 = new DCColour(0.0, 1.0, 1.0, 0.5);
   
   
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
      DCColour.fromInt(255, 255, 204, 20), 
      DCColour.fromInt(255, 237, 160, 40), 
      DCColour.fromInt(254, 217, 118, 60),
      DCColour.fromInt(254, 178, 76, 80), 
      DCColour.fromInt(253, 141, 60, 100), 
      DCColour.fromInt(252, 78, 42, 120),
      DCColour.fromInt(227, 26, 28, 140), 
      DCColour.fromInt(189, 0, 38, 160), 
      DCColour.fromInt(128, 0, 38, 180)       
   };   
   public static DCColour[] Red = new DCColour[] {
      DCColour.fromInt(255, 245, 240, 20),
      DCColour.fromInt(254, 224, 210, 40),
      DCColour.fromInt(252, 187, 161, 60),
      DCColour.fromInt(252, 146, 114, 80),
      DCColour.fromInt(251, 106, 74, 100),
      DCColour.fromInt(239, 59, 44, 120),
      DCColour.fromInt(203, 24, 29, 140),
      DCColour.fromInt(165, 15, 21, 160),
      DCColour.fromInt(103, 0, 13, 180)      
   };
   
   
}
