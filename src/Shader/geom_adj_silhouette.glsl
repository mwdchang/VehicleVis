#version 150 

/*******************************************************************************
 Triangle adjacency silhouette shader
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
*******************************************************************************
*/


uniform vec3 eyePosition; 


 
//Specifies the input and output layout, for this shader the maximum is the triangle
//it self in line strip format ==> 6 vertices 
layout(triangles_adjacency) in;
layout(line_strip, max_vertices = 32) out;


////////////////////////////////////////////////////////////////////////////////
// 0) Calcuate the eye vector 
// 1) Check if central face is front or back facing
// 2) If front facing then check against the normals of the adjacency trangles
// 3) If adj trangle is back facing the extrude edge(s)
////////////////////////////////////////////////////////////////////////////////
void main(void) {

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


   // Calculate the "midpoint" of the central triangle, and use it 
   // to calcuate the eye vector
   vec3 midpoint = (v0+v2+v4)/3.0;
   vec3 eyeVec   = normalize(midpoint - eyePosition); 

   
   // Check if the central triangle is front or back facing
   float r = dot(normal_042, eyeVec);
   if (r < 0) return;


   // Check 021 
   midpoint = (v0+v2+v1)/3.0;
   eyeVec   = normalize(midpoint - eyePosition);
   if (dot(eyeVec, normal_021) < 0 ) {
     gl_Position = gl_in[0].gl_Position; EmitVertex();
     gl_Position = gl_in[2].gl_Position; EmitVertex();
     EndPrimitive();
   }

   // Check 243
   midpoint = (v2+v4+v3)/3.0;
   eyeVec   = normalize(midpoint - eyePosition);
   if (dot(eyeVec, normal_243) < 0 ) {
     gl_Position = gl_in[2].gl_Position; EmitVertex();
     gl_Position = gl_in[4].gl_Position; EmitVertex();
     EndPrimitive();
   }

   // Check 405
   midpoint = (v4+v0+v5)/3.0;
   eyeVec   = normalize(midpoint - eyePosition);
   if (dot(eyeVec, normal_405) < 0 ) {
     gl_Position = gl_in[4].gl_Position; EmitVertex();
     gl_Position = gl_in[0].gl_Position; EmitVertex();
     EndPrimitive();
   }


}
