#version 150

in vec4 in_colour;
in vec2 in_texcoord;
in vec3 in_position;

uniform mat4 projection_matrix;
uniform mat4 modelview_matrix;

out vec4 pass_colour;
out vec2 pass_texcoord;

void main() {
   gl_Position = projection_matrix * modelview_matrix * vec4(in_position, 1.0);
   pass_colour   = in_colour;
   pass_texcoord = in_texcoord; 
}
