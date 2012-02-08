#version 150

////////////////////////////////////////////////////////////////////////////////
// This shader passes all vertex to the geometry shader
////////////////////////////////////////////////////////////////////////////////

in vec3 in_position;


uniform mat4 projection_matrix;
uniform mat4 modelview_matrix;


void main() {
   gl_Position = projection_matrix * modelview_matrix * vec4(in_position, 1.0);
}
