#version 150 

/*******************************************************************************
 Triangle adjacency edge shader
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


   // Ignore really small triangles ... this does not work in general, 
   // but should work for the vehicle models we have
   //vec3 c = cross(v4-v0, v2-v0);
   //float cmag = c.x*c.x + c.y*c.y + c.z*c.z;
   //if (cmag < 0.01) return;


   // Ignore back faces
   //vec3 midpoint = (v0+v2+v4)/3.0;
   //if (dot(normal_042, normalize( midpoint - eyePosition)) > 0) return;


   

   // dot product guard ?

   // Check if the normals are facing the same direction
   // check 021
   if ( dot(normal_042, normal_021) < 0.0) {
     // original
     gl_Position = gl_in[0].gl_Position; EmitVertex();
     gl_Position = gl_in[2].gl_Position; EmitVertex();

     // modified to extrude normal a bit
     //gl_Position = gl_in[0].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();
     //gl_Position = gl_in[2].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();
     EndPrimitive();
   }

   // check 243
   if ( dot(normal_042, normal_243) < 0.0) {
     // original
     gl_Position = gl_in[2].gl_Position; EmitVertex();
     gl_Position = gl_in[4].gl_Position; EmitVertex();

     // modified to extrude normal a bit
     //gl_Position = gl_in[2].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();
     //gl_Position = gl_in[4].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();

     EndPrimitive();
   }
   

   // check 405
   if ( dot(normal_042, normal_405) < 0.0) {
     // original  
     gl_Position = gl_in[4].gl_Position; EmitVertex();
     gl_Position = gl_in[0].gl_Position; EmitVertex();

     // modified to extrude normal a bit
     //gl_Position = gl_in[4].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();
     //gl_Position = gl_in[0].gl_Position + 0.2*vec4(normal_042, 0); EmitVertex();

     EndPrimitive();
   }



}
