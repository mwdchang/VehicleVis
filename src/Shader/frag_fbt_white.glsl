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
   float distW = 1.0/width;
   float distH = 1.0/height;
   int cnt;
   
   
   // If has some value, make it transparent
   vec4 texC = texture2D(tex, pass_texcoord.xy).rgba;
   if (texC.r > 0.1 || texC.g > 0.1 || texC.b > 0.1) {
      outColour = vec4(0,0,0,0.0);
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
   //c /= 25;
   c /= 81;

   //outColour = 0.6*texture2D(tex, pass_texcoord.xy).rgba + 0.4*c; 
   outColour = c; 
   //outColour.rgb += 0.4*texture2D(tex, pass_texcoord.xy).rgb;

   if (outColour.r+outColour.g+outColour.b < 0.3)
      outColour.a = 0.0;
   else
      outColour.a = 0.5;

   //outColour.a = 1.0;
  
   //outColour.r = 0.4;




}


