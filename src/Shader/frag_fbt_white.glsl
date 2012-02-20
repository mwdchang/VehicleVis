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
uniform int height;
uniform int width;

uniform float sampleRate;


in vec4 pass_colour;
in vec2 pass_texcoord; 


out vec4 outColour;


void main(void) {
   float bloom[25];  
   bloom[0] = 1;
   bloom[1] = 4;
   bloom[2] = 7;
   bloom[3] = 4;
   bloom[4] = 1;

   bloom[5] = 4;
   bloom[6] = 16;
   bloom[7] = 26;
   bloom[8] = 16;
   bloom[9] = 4;

   bloom[10] = 7;
   bloom[11] = 26;
   bloom[12] = 41;
   bloom[13] = 26;
   bloom[14] = 7;
   
   bloom[15] = 4;
   bloom[16] = 16;
   bloom[17] = 26;
   bloom[18] = 16;
   bloom[19] = 4;

   bloom[20] = 1;
   bloom[21] = 4;
   bloom[22] = 7;
   bloom[23] = 4;
   bloom[24] = 1;

   vec4 c = vec4(0,0,0,0);
   vec2 offset;
   float distW = sampleRate/width;
   float distH = sampleRate/height;
   int cnt;
   
   
   // If has some value, make it transparent
   vec4 texC = texture2D(tex, pass_texcoord.xy).rgba;
   if (texC.r <= 0.9 || texC.g <= 0.9 || texC.b <= 0.9) {
      outColour = vec4(0, 0, 0, 0);
      return; 
   }
   
   
   


   for (int y=-4; y <=4; y++) {
      for (int x=-4; x <=4; x++) {
         offset.x = 1.0 * x * distW;
         offset.y = 1.0 * y * distH;
         //c += bloom[cnt] * texture2D( tex , pass_texcoord.xy + offset).rgba;
         c +=  texture2D( tex , pass_texcoord.xy + offset).rgba;
         
         // Inverse blend - WTF !!!
         //c += (vec4(1,1,1,1) - bloom[cnt] * texture2D( tex , pass_texcoord.xy + offset).rgba);
         //c += (vec4(1,1,1,1) - texture2D( tex , pass_texcoord.xy + offset).rgba);
      }
   }
   c /= 81;

   outColour = c; 

   if (outColour.r+outColour.g+outColour.b > 2.7)
      outColour.a = 0.0;
   else
      outColour.a = 0.8;

   
   //outColour.rgb = vec3(1) - outColour.rgb;




}


