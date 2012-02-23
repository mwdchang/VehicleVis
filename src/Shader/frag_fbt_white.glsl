#version 150

////////////////////////////////////////////////////////////////////////////////
//
// This shader creates a 'glow' or 'halo' effect around the edge of coloured
// pixels. This particular implementation is desgined to blend against a w
// white (1,1,1,1) background. 
//
// tex        - the texture, most likely from the frame buffer
// height     - texture height
// width      - texture width
// useAverage - if 0 then return an unblurred colour
// sampleRate - controls where to sample. A sampleRate of 1 samples the exact textile, > 1 
//              gives a coarser and more halo'y effect
//
////////////////////////////////////////////////////////////////////////////////

precision highp float;

// User defined params
uniform sampler2D tex;
uniform int height;
uniform int width;
uniform int useAverage;
uniform float sampleRate;


// Passed in from vertex shader
in vec4 pass_colour;
in vec2 pass_texcoord; 


// Out parameter
out vec4 outColour;


void main(void) {

   vec4 c = vec4(0,0,0,0);
   vec2 offset;
   float distW = sampleRate/width;
   float distH = sampleRate/height;
   int cnt;
   

   ////////////////////////////////////////////////////////////////////////////////
   // We only want to blend the edges, so if the pixel
   // contains some value, then we want to discard it
   // and return transparent pixel
   ////////////////////////////////////////////////////////////////////////////////
   vec4 texC = texture2D(tex, pass_texcoord.xy).rgba;
   if (texC.r <= 0.9 || texC.g <= 0.9 || texC.b <= 0.9) {
      outColour = vec4(0, 0, 0, 0);
      return; 
   }
   
   

   // This part is a tad slow .... what can we do to optimiz it ? using a matrix ??
   for (int y=-2; y <=2; y++) {
      for (int x=-2; x <=2; x++) {
         offset.x = 1.0 * x * distW;
         offset.y = 1.0 * y * distH;
         c +=  texture2D( tex , pass_texcoord.xy + offset).rgba;
      }
   }
   c /= 25;  // 5x5 

   outColour = c; 

   // Sanity check
   if (outColour.r+outColour.g+outColour.b > 2.7)
      outColour.a = 0.0;
   else
      outColour.a = 0.8;


   // check what exact colour we want
   if (useAverage == 0) {
      if (outColour.r+outColour.g+outColour.b < 2.7) 
         outColour.rgba = vec4(0, 1, 0, 0.8);
   }
       

}


