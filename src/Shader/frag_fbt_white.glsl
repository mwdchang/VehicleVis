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
// sampleRate - controls where to sample. A sampleRate of 1 samples the exact textile, > 1 
//              gives a coarser and more halo'y effect
//
////////////////////////////////////////////////////////////////////////////////

precision highp float;

// User defined params
uniform sampler2D tex;
uniform int height;
uniform int width;
uniform float sampleRate;

uniform vec4 hardColour;


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
   int cnt = 0;
   

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
         vec4 current =  texture2D( tex , pass_texcoord.xy + offset).rgba;
         c += current;
         cnt ++;
      }
   }

   // if count is 0 then it should be all white, so return (0,0,0,0)
   if (cnt < 1) {
      outColour = vec4(0, 0, 0, 0);
      return;
   }
   c /= (cnt);  // 5x5 
   outColour = c; 


   // Sanity check
   if (outColour.r+outColour.g+outColour.b > 2.7) {
      outColour.a = 0.0;
   } else {
      // Darken the rgb values to make it more distinguished on a white back drop
      outColour.rg /= 1.5;
      outColour.b *= 1.2;
      outColour.a = 0.8;
   }



}


