////////////////////////////////////////////////////////////////////////////////
// Just for the fun of it...
//
// Fractal fragment shader
// Ported this from my C++ code, which renders the fractal to a 
// openGL texture, using the shader in place of texture should
// off load the CPU computation and get better performance
////////////////////////////////////////////////////////////////////////////////

// Need to have GLSL4.0 or enable this extension
// to use double precision numbers
#extension GL_ARB_gpu_shader_fp64: enable

// Window width and height
uniform int width;
uniform int height;
uniform int flag;

// Number of iterations
uniform int iter;


// Controls the drawing region
uniform float cminX;
uniform float cminY;
uniform float cmaxX;
uniform float cmaxY;


uniform float juliaReal;
uniform float juliaImaginary;


// Complex number ADD operation
vec2 cadd(vec2 a, vec2 b) {
   return vec2(a.x+b.x, a.y+b.y);
}

// Complex number SUB operation
vec2 csub(vec2 a, vec2 b) {
   return vec2(a.x-b.x, a.y-b.y);
}

// Complex number MULT operation
vec2 cmult(vec2 a, vec2 b) {
   return vec2(
      a.x*b.x - a.y*b.y,
      a.x*b.y + a.y*b.x);
}

// Complex number MODULUS operation
float cmodulus(vec2 v) {
   return sqrt( v.x*v.x + v.y*v.y);
}


// Generate a mandelbrod fractal
int c_mandelbrot(float x, float y, int iter) {
   // Complex component
   vec2 cz = vec2(0.0, 0.0);
   vec2 cc = vec2(x, y);

   int n = 0;
   while (n < iter && cmodulus( cz ) < 4.0) {
      cz = cmult(cz, cz);
      cz = cadd(cz, cc);
      n++;
   }
   return n >= iter ? 0:n;
}


int c_multibrot(float x, float y, int iter) {
   vec2 cz = vec2(0.0, 0.0);
   vec2 cc = vec2(x, y);

   int n = 0;
   while (n < iter && cmodulus( cz ) < 4.0) {
      cz = cmult(cz, cz);
      cz = cmult(cz, cz);
      cz = cadd(cz, cc);
      n++;
   }
   return n >= iter ? 0:n;
}




// Generate a julia fractal
int c_julia(float x, float y, int iter) {
   vec2 cz = vec2(x, y);
   //vec2 cc = vec2(0.32, 0.5);
   //vec2 cc = vec2(0.35, 0.5);
   vec2 cc = vec2(juliaReal, juliaImaginary);
   

   int n = 0;
   while (n < iter && cmodulus( cz ) < 4.0) {
      cz = cmult(cz, cz);
      cz = cadd(cz, cc);
      n++;
   }

   return n > iter ? 0:n;
}




void main(void) {

   float xdist = (cmaxX-cminX)/width;
   float ydist = (cmaxY-cminY)/height;

   float X = cminX + gl_FragCoord.x * xdist;
   float Y = cminY + gl_FragCoord.y * ydist;

   int result = 0;
   if (flag == 0)
      result = c_multibrot(X, Y, iter);
   else if (flag == 1)
      result = c_mandelbrot(X, Y, iter);
   else 
      result = c_julia(X, Y, iter);
   
   //gl_FragColor.rgb = vec3( 0.5*gl_FragCoord.x/width, 0.5*gl_FragCoord.y/height, 0);
   if (result > 2) {
      gl_FragColor.rgb += vec3(
         //(float)result / 1024.0,
         log(result)/2.0,
         mod((float)result, 85)/85.0,
         juliaReal/juliaImaginary
         //0.1
      );
      /*
      gl_FragColor.rgb += vec3(
         (float)result / 1024.0,
         mod((float)result, 85)/85.0,
         mod((float)result, 20)/20.0
      );
      */
   } else {
      gl_FragColor.rgb = vec3(0, 0, 0);
   }

   gl_FragColor.a = 1.0;

}


////////////////////////////////////////////////////////////////////////////////
// Just a test - graident with coords
////////////////////////////////////////////////////////////////////////////////
/*
void main(void) {
   gl_FragColor.rgb = vec3( gl_FragCoord.x/600, gl_FragCoord.y/600, 0);
   gl_FragColor.a = 1.0;
}
*/
