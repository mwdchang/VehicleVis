#version 150

////////////////////////////////////////////////////////////////////////////////
//
// This shader creates a 'magic' lens effect
// by texturing on top of an existing frame buffer.
// It draws a circle by default given a uniform radius length of magicLensRadius
// at location (smouseX, smouseY)
//
////////////////////////////////////////////////////////////////////////////////

precision highp float;


uniform sampler2D tex;
uniform int smouseX;
uniform int smouseY;
uniform int areaHeight;
uniform int areaWidth;
uniform float magicLensRadius;
uniform int magicLensSelected;
uniform int useTexture;
uniform vec4 lensColour;
uniform float zoomFactor;


in vec4 pass_colour;
in vec2 pass_texcoord; 


out vec4 outColour;

// gl_FragCoord is the screen coordinate !!!
// find out how to pass actual ST values
void main() {

   if (useTexture > 0) {
      // Test zoom
      vec2 test = vec2( float(smouseX)/float(areaWidth), float(smouseY)/float(areaHeight));
      outColour = texture2D( tex, (test - (test-pass_texcoord.xy)/zoomFactor)).rgba;

      // Original Unaltered
      //outColour = texture2D( tex, pass_texcoord.xy).rgba;
      outColour.a = 1.0f;
   } else {
      outColour = vec4(0,0,0,0);
   }
  
  
  // Hack hack - for OIT rendering
  //outColour = vec4(0,0,0,0);
  
   // Start of some sobel stuff
/*   
   float ix = 1.0/1024.0;
   float iy = 1.0/1024.0;
  
   vec4 s00 = texture2D( tex, pass_texcoord.xy + vec2(-ix, -iy));
   vec4 s01 = texture2D( tex, pass_texcoord.xy + vec2(  0, -iy));
   vec4 s02 = texture2D( tex, pass_texcoord.xy + vec2( ix, -iy));
  
   vec4 s10 = texture2D( tex, pass_texcoord.xy + vec2(-ix, 0));
   vec4 s12 = texture2D( tex, pass_texcoord.xy + vec2( ix, 0));
  
   vec4 s20 = texture2D( tex, pass_texcoord.xy + vec2( -ix, iy));
   vec4 s21 = texture2D( tex, pass_texcoord.xy + vec2(   0, iy));
   vec4 s22 = texture2D( tex, pass_texcoord.xy + vec2(  ix, iy));
  
   vec4 sobelX = 3*s00 + 10*s10 + 3*s20 - 3*s02 - 10*s12 - 3*s22;
   vec4 sobelY = 3*s00 + 10*s01 + 3*s02 - 3*s20 - 10*s21 - 3*s22;
   
   
   
   outColour.rgb = sqrt(sobelX.rgb*sobelX.rgb + sobelY.rgb*sobelY.rgb);
   outColour.a = 1.0;
 */  
   // end of sobel stuff
  
  
  
  

   vec2 c = vec2( gl_FragCoord.s, gl_FragCoord.t);
   float hypo = sqrt((c.x-smouseX)*(c.x-smouseX) + (c.y-smouseY)*(c.y-smouseY));

   if (  hypo > magicLensRadius) {
      //outColour = vec4(1,0,0,1);
      outColour = vec4(0,0,0,0);
   } else if ( hypo > magicLensRadius-5 && hypo <= magicLensRadius) {
      outColour.rgba = lensColour;
      /*
      if (magicLensSelected == 1)
	      outColour = vec4(0.2,0.2,0.9,0.5);
	  else    
	      outColour = vec4(0.5,0.5,0.5,0.5);
	  */     
   }

   //outColour = pass_colour;
   //outColour = vec4(1.0f, 0.0f, 0.0f, 1.0f);
   return;

  
   vec2 coord = vec2( gl_FragCoord.s, gl_FragCoord.t);
  
  
}

