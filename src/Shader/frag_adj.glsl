#version 150

////////////////////////////////////////////////////////////////////////////////
// This shader renders the vertex based on comp_colourAdj
////////////////////////////////////////////////////////////////////////////////


precision highp float;

uniform int  modeAdj;
uniform vec4 comp_colourAdj; 

out vec4 outColour;

void main() {
   outColour = comp_colourAdj;
   return;
}

