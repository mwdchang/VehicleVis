#version 150

////////////////////////////////////////////////////////////////////////////////
// 
// Sobel filter edges
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
uniform int height;
uniform int width;
uniform int useAverage;
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


   vec4 s00 = texture2D( tex, pass_texcoord.xy + vec2(-distW, -distH)).rgba;
   vec4 s01 = texture2D( tex, pass_texcoord.xy + vec2(     0, -distH)).rgba;
   vec4 s02 = texture2D( tex, pass_texcoord.xy + vec2( distW, -distH)).rgba;

   vec4 s10 = texture2D( tex, pass_texcoord.xy + vec2(-distW, 0)).rgba;
   vec4 s12 = texture2D( tex, pass_texcoord.xy + vec2( distW, 0)).rgba;

   vec4 s20 = texture2D( tex, pass_texcoord.xy + vec2(-distW, distH)).rgba;
   vec4 s21 = texture2D( tex, pass_texcoord.xy + vec2(     0, distH)).rgba;
   vec4 s22 = texture2D( tex, pass_texcoord.xy + vec2( distW, distH)).rgba;

   vec4 X = 3*s00 + 10*s10 + 3*s20 - 3*s02 - 10*s12 - 3*s22;
   vec4 Y = 3*s00 + 10*s01 + 3*s02 - 3*s20 - 10*s21 - 3*s22;

   float test = sqrt(X.r*X.r + X.g*X.g + X.b*X.b + X.a*X.a +
                     Y.r*Y.r + Y.g*Y.g + Y.b*Y.b + Y.a*Y.a);
//   float test = sqrt(X.a*X.a + Y.a*Y.a);


   // The original colour at point, we will decode the value
   vec4 pointColour = texture2D( tex, pass_texcoord.xy).rgba;


   //////////////////////////////////////////////////////////////////////////////// 
   // RGBA is used to encode other values
   // R/G are switches to see which colour to use
   // B is the strength of the colour
   // A is used to differentiate edges (see above)
   //////////////////////////////////////////////////////////////////////////////// 
   if (test > 0.01) {
      //outColour.rgb = texture2D( tex, pass_texcoord.xy).rgb;

      if (pointColour.r > 0) {
         outColour.rgba = vec4(1.0, 0.0, 0.0, pointColour.b);
      } else if (pointColour.g > 0) {
         outColour.rgba = vec4(0.0, 0.0, 1.0, pointColour.b);
      } else {
         outColour.rgba = vec4(0, 0, 0, 0.0);
      }

      //outColour.rgb = vec3(1, 0, 0);
      //outColour.a = 0.6;
      //outColour = texture2D( tex, pass_texcoord.xy).rgba;
   } else {
      //float alpha = texture2D(tex, pass_texcoord.xy).a;
      //outColour.rgba = texture2D( tex, pass_texcoord.xy).rgba;
      outColour.rgba = vec4(0, 0, 0.0, 0.0);
      //outColour.a = 1.0;
      //outColour = texture2D( tex, pass_texcoord.xy).rgba;
   }





   // Default texture thingy
   /*
   if ( width  > 0 && height > 0 && sampleRate > 0) {
      outColour.rgba = texture2D( tex, pass_texcoord.xy).rgba;
   }
   */
   

   

   ////////////////////////////////////////////////////////////////////////////////
   // We only want to blend the edges, so if the pixel
   // contains some value, then we want to discard it
   // and return transparent pixel
   ////////////////////////////////////////////////////////////////////////////////
   /*
   vec4 texC = texture2D(tex, pass_texcoord.xy).rgba;
   if (texC.r <= 0.9 || texC.g <= 0.9 || texC.b <= 0.9) {
      outColour = vec4(0, 0, 0, 0);
      return; 
   }
   
   
   // This part is a tad slow .... what can we do to optimiz it ? using a matrix ??
   for (int y=-1; y <=1; y++) {
      for (int x=-1; x <=1; x++) {
         offset.x = 1.0 * x * distW;
         offset.y = 1.0 * y * distH;
         vec4 current =  texture2D( tex , pass_texcoord.xy + offset).rgba;
         if ( ! (current.r > 0.9 && current.g > 0.9 && current.b > 0.9) ) {
            c += current;
            cnt ++;
         }
      }
   }

   // if count is 0 then it should be all white, so return (0,0,0,0)
   if (cnt < 1) {
      outColour = vec4(0, 0, 0, 0);
      return;
   }
   c /= cnt; 
   outColour = c; 

   // Sanity check
   if (outColour.r+outColour.g+outColour.b > 2.7) {
      outColour.a = 0.0;
   } else {
      outColour.a = 0.8;
   }
   */
       

}


