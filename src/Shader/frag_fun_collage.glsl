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
uniform sampler2D tex2;


// Passed in from vertex shader
in vec4 pass_colour;
in vec2 pass_texcoord; 


// Out parameter
out vec4 outColour;


void main(void) {

   vec4 c = vec4(0,0,0,0);
   vec2 offset;
   int cnt = 0;

   
   outColour.rgba = texture2D(tex, pass_texcoord.xy).rgba;
   
   //if (outColour.g >= 0.99 && outColour.b == 0 && outColour.r == 0) {
   if (outColour.g >= 0.9 && outColour.r < 0.1 && outColour.b < 0.1) {
      vec2 temp = pass_texcoord.xy;
      temp.y = 1.0 - temp.y;
      outColour.rgba = texture2D(tex2, temp).rgba;
   }
   
   //outColour.rgba = texture2D(tex2, pass_texcoord.xy).rgba;
   //outColour.rgba = texture2D(tex2, pass_texcoord.xy).rgba;

}


