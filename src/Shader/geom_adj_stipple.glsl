#version 150 

/*******************************************************************************
 Triangle adjacency edge shader with stippling (creates dashed lines)
 Given triangle 024 and adjacency 1,3,5. Emit an edge if the face normals are in
 different directions

 Triangle(0, 4, 2) => Main tri
 Triangle(0, 2, 1), Triangle(2, 4, 3), Triangle(4, 0, 5) ==> Adj tris


       1 _____2______3
         \ A  /\ A  /
          \  /  \  /
           \/____\/
          0 \ A  / 4
             \  /
              \/
              5

 Becareful when dealing with w's

 A line segment, given 2 points A and B can be defined as: A + (B-A) * f
 Where f is between [0,1], if we break f into a discrete # of segments
 we can simulte a stippling effect. As well we can calculate the magnitude
 so the stippling does not affect smaller line segments.


 Note: A tad slow..., need to keep the segment lower, or precompute the magnitude 
*******************************************************************************
*/




 
//Specifies the input and output layout, for this shader the maximum is the triangle
//it self in line strip format ==> 6 vertices 
layout(triangles_adjacency) in;
layout(line_strip, max_vertices = 32) out;



////////////////////////////////////////////////////////////////////////////////
// Sub-divide a single line segment into multiple segments stipples
////////////////////////////////////////////////////////////////////////////////
void computeStipple(in vec4 p1, in vec4 p2, in int segment) {
   vec4 f = (p2-p1);
   f.w = 1;

   // Cap a maxima length
   if ( length(f) < 3.0) {
      gl_Position = p1; EmitVertex();
      gl_Position = p2; EmitVertex();
      EndPrimitive();
      return;
   }

   // Break the lines into stipples
   for (int i=0; i < segment; i++) {
      vec4 p1d = p1 + (i*f)/segment;
      vec4 p2d = p1 + ((i+1)*f)/segment;
      if (  mod( i, 2) == 0) {
         gl_Position = p1d; EmitVertex();
         gl_Position = p2d; EmitVertex();
         EndPrimitive();
      }
   }

}


////////////////////////////////////////////////////////////////////////////////
// Main method 
////////////////////////////////////////////////////////////////////////////////
void main(void) {
   // Pass through the colours


   // Grab the vertices
   vec3 v0 = gl_in[0].gl_Position.xyz;
   vec3 v1 = gl_in[1].gl_Position.xyz;
   vec3 v2 = gl_in[2].gl_Position.xyz;
   vec3 v3 = gl_in[3].gl_Position.xyz;
   vec3 v4 = gl_in[4].gl_Position.xyz;
   vec3 v5 = gl_in[5].gl_Position.xyz;


   // Get the normal for the 4 triangles in play
   vec3 normal_042 = normalize(cross(v4-v0, v2-v0));
   vec3 normal_021 = normalize(cross(v2-v0, v1-v0));
   vec3 normal_243 = normalize(cross(v4-v2, v3-v2));
   vec3 normal_405 = normalize(cross(v0-v4, v5-v4));

   

   // dot product guard ?

   // Check if the normals are facing the same direction
   // check 021
   if ( dot(normal_042, normal_021) < 0.5) {
     vec4 p1 = gl_in[0].gl_Position; 
     vec4 p2 = gl_in[2].gl_Position; 
     computeStipple(p1, p2, 6);
   }

   // check 243
   if ( dot(normal_042, normal_243) < 0.5) {
     vec4 p1 = gl_in[2].gl_Position; 
     vec4 p2 = gl_in[4].gl_Position; 
     computeStipple(p1, p2, 6);
   }
   

   // check 405
   if ( dot(normal_042, normal_405) < 0.5) {
     vec4 p1 = gl_in[4].gl_Position;
     vec4 p2 = gl_in[0].gl_Position;
     computeStipple(p1, p2, 6);
   }



}
