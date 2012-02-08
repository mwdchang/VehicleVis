#version 150

in vec4 in_colour;
in vec2 in_texcoord;
in vec3 in_position;
in vec3 in_normal;

uniform mat4 projection_matrix;
uniform mat4 modelview_matrix;
uniform vec4 comp_colour;
uniform int  mode;
uniform vec3 lightPos;
uniform sampler1D texMap;

out vec3 pass_position;
out vec3 pass_normal;
out vec4 pass_colour;
out vec2 pass_texcoord;


// NPR param
float kappa;
float alpha, tau, beta, gamma, lambda, mu, chi;



float u(vec3 n, vec3 l, vec3 v) {
   vec3 h = normalize(l+v);
   float eta = dot(l, v) * 0.5 + 0.5;
   float S_l; 
   if (lambda >= 0) {
      S_l =  1 /  (1.0-lambda)*eta+(1-eta) ;
   } else {
      S_l =  1 /  ((1.0+lambda)*eta+(1-eta)) ;
   }

   vec3 tmp = in_position;
   tmp.x -= 0.01;

   vec3 t = cross(in_position, tmp);
   t = normalize(t);
   vec3 b = cross(t, in_position);
   b = normalize(b);

   
   vec3 ht = dot(h, t)*t;
   vec3 hb = dot(h, b)*b;
   vec3 hn = dot(h, n)*n;

   h = normalize( S_l*ht + 1/S_l*hb + hn);
   v = reflect(-l, h);
   vec3 r = reflect(-v, n);
   vec3 d = normalize( (1-alpha)*n + alpha*r);

   tau += mu*tanh( kappa*chi);
   return clamp( acos(dot(d, l))-tau, 0, 3.1416);
}


float I(float x) {
   return pow(max(beta+(1-beta)*cos(x), 0), gamma);
}




void main() {
   gl_Position = projection_matrix * modelview_matrix * vec4(in_position, 1.0);


   alpha = 0.5;   // specularity
   tau = 2.5;    // extent
   beta = 0.5;    // c 
   gamma = 1.0;   // f,c 
   
   lambda = -0.5;  // material anisotropy
   mu = 0.3;      // surface enhancement
   chi = 30;     // concave convex transition
   
   kappa = 0.2;



   pass_colour = vec4( comp_colour.rgb, I(u(in_normal,  vec3(1, 0, 0),  in_position) ));
   
   //pass_colour = comp_colour;
   pass_texcoord = in_texcoord;
   pass_normal = in_normal;
   pass_position = in_position;
}


