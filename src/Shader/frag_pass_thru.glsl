#version 150

////////////////////////////////////////////////////////////////////////////////
// This is a simple pass thru fragment shader/
////////////////////////////////////////////////////////////////////////////////

precision highp float;


in vec4 pass_colour;
in vec2 pass_texcoord; 
in vec3 pass_normal;
in vec3 pass_position;

out vec4 outColour;



void main() {
   outColour = pass_colour;
}

