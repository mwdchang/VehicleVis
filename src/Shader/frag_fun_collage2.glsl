#version 150

////////////////////////////////////////////////////////////////////////////////
// 
// Sobel filter edges. This shader uses a variation of the standard sobel filter
// kernel
//
// H [ 3 10   3;  0, 0,  0; -3, -10, -3]
// V [ 3, 0, -3; 10, 0, 10;  3,   0, -3]
//
// tex        - the texture, most likely from the frame buffer
// height     - texture height
// width      - texture width
// sampleRate - controls where to sample. A sampleRate of 1 samples the exact textile, > 1 
//              gives a coarser and more halo'y effect
////////////////////////////////////////////////////////////////////////////////

precision highp float;

// User defined params
uniform sampler2D tex;


// Passed in from vertex shader
in vec4 pass_colour;
in vec2 pass_texcoord; 


// Out parameter
out vec4 outColour;


void main(void) {
   vec2 temp = pass_texcoord.xy;
   temp.y = 1 - temp.y;
   outColour.rgba = texture2D(tex, temp).rgba;
   /*
   if (outColour.r == 0 && outColour.g == 0 && outColour.b == 0) {
      outColour.r = 0.9;
      outColour.g = 0.9;
      outColour.b = 0.9;
   }
   */
}


