#version 150

////////////////////////////////////////////////////////////////////////////////
// This is a simple pass through shader that takes in
// vertex, normal, colour and texcoord information and pass them forward
// into the pipeline.
// 
// in_colour   - rgba colour
// in_texcoord - uv coord
// in_position - xyz
// in_normal   - xyz
//
// In addition colour can be over written via the uniform parameter
//
////////////////////////////////////////////////////////////////////////////////


in vec4 in_colour;
in vec2 in_texcoord;
in vec3 in_position;
in vec3 in_normal;

uniform mat4 projection_matrix;
uniform mat4 modelview_matrix;
uniform vec4 comp_colour;
uniform int  mode;
uniform vec3 lightPos;

out vec3 pass_position;
out vec3 pass_normal;
out vec4 pass_colour;
out vec2 pass_texcoord;

uniform sampler1D texMap;

void main() {
   gl_Position = projection_matrix * modelview_matrix * vec4(in_position, 1.0);
   //pass_colour   = in_colour;


   // Fake light calculations (per vertex)
   vec4 ambient = vec4(0.2, 0.2, 0.2, 0.0);
   vec3 light_pos = vec3(0, 100, 0);

   //vec3 light_pos = vec3(100, 100, 0);

   // Original
   vec3 L = normalize(light_pos - in_position);

   // Set the light to be perpendicular to the surface or pixel <== oh crap...this will give me a "1"
   //vec3 L = normalize(pass_position + pass_normal);


   float diffuse = max(dot(in_normal, L), 0);

   if (mode == 0) {
      ////////////////////////////////////////////////////////////////////////////////
      // Grey colour rendering with fixed light position at (0, 100, 0)
      ////////////////////////////////////////////////////////////////////////////////
      pass_colour   = ambient + vec4(0.5, 0.5, 0.5, 1.0)*diffuse;

   } else if (mode == 1) {
      ////////////////////////////////////////////////////////////////////////////////
      // Modified light (no specular) with fixed light position at (0, 100, 0);
      ////////////////////////////////////////////////////////////////////////////////
      pass_colour.rgb  = ambient.rgb + comp_colour.rgb*diffuse;
      pass_colour.a = comp_colour.a;

   } else if (mode == 2) {
      ////////////////////////////////////////////////////////////////////////////////
      // Solid colour rendering
      ////////////////////////////////////////////////////////////////////////////////
      pass_colour = comp_colour;

   } else if (mode == 3) {
      ////////////////////////////////////////////////////////////////////////////////
      // Modified light (no specular) with variable light position passed in by the user
      ////////////////////////////////////////////////////////////////////////////////
      vec3 L2 = normalize(lightPos - in_position);
      float diffuse2 = max(dot(in_normal, L2), 0);
      pass_colour.rgb  = ambient.rgb + comp_colour.rgb*diffuse2;
      pass_colour.a = comp_colour.a;

   } else if (mode == 4) {
      ////////////////////////////////////////////////////////////////////////////////
      // Just for fun - Emulate a toon shading
      ////////////////////////////////////////////////////////////////////////////////
      vec3 n      = in_normal;
      vec3 L2     = vec3(100, 100, 0);
      vec3 L2D    = normalize(L2 - in_position);
      float cellS = max(dot(n, L2D), 0);
      
      pass_colour = texture( texMap, cellS);
      pass_colour.a = 1.0;
   } else if (mode == 5) {
      ////////////////////////////////////////////////////////////////////////////////
      // Gooch NPR Shading emulation
      ////////////////////////////////////////////////////////////////////////////////
      float warm_coeff = 0.1;
      float cold_coeff = 0.9;
      float kd = 0.6;

      vec3 warm_colour = vec3(0.7, 0.5, 0.0);
      vec3 cold_colour = vec3(0.0, 0.2, 0.9);

      warm_colour += warm_coeff*kd;
      cold_colour += cold_coeff*kd;

      float W = (1 + max(dot(in_normal, L), 0))/2;
      float C = 1.0 - (1+max(dot(in_normal, L), 0))/2;


      pass_colour.rgb = (W*warm_colour + C*cold_colour);
      pass_colour.a = 1.0;


   }
     

   pass_texcoord = in_texcoord; 
   pass_normal   = in_normal;
   pass_position = in_position;
}
