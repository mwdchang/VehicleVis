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

// Window width and height
uniform int width;
uniform int height;
uniform int flag;

// Number of iterations
uniform int iter;


// Controls the drawing region
uniform float cminX1;
uniform float cminX2;

uniform float cminY1;
uniform float cminY2;

uniform float cmaxX1;
uniform float cmaxX2;

uniform float cmaxY1;
uniform float cmaxY2;

uniform float juliaReal;
uniform float juliaImaginary;


// Simulate a double with 2 floats
// From http://www.thasler.org/blog/?p=93

vec2 ds_set(float a) {
   vec2 z;
   z.x = a;
   z.y = 0.0;
   return z;
}

// Substract: res = ds_add(a, b) => res = a + b
vec2 ds_add (vec2 dsa, vec2 dsb) {
   vec2 dsc;
   float t1, t2, e;

   t1 = dsa.x + dsb.x;
   e = t1 - dsa.x;
   t2 = ((dsb.x - e) + (dsa.x - (t1 - e))) + dsa.y + dsb.y;

   dsc.x = t1 + t2;
   dsc.y = t2 - (dsc.x - t1);
   return dsc;
}


// Substract: res = ds_sub(a, b) => res = a - b
vec2 ds_sub (vec2 dsa, vec2 dsb) {
   vec2 dsc;
   float e, t1, t2;

   t1 = dsa.x - dsb.x;
   e = t1 - dsa.x;
   t2 = ((-dsb.x - e) + (dsa.x - (t1 - e))) + dsa.y - dsb.y;

   dsc.x = t1 + t2;
   dsc.y = t2 - (dsc.x - t1);
   return dsc;
}


float ds_compare(vec2 dsa, vec2 dsb) {
 if (dsa.x < dsb.x) return -1.;
 else if (dsa.x == dsb.x) 
	{
	if (dsa.y < dsb.y) return -1.;
	else if (dsa.y == dsb.y) return 0.;
	else return 1.;
	}
 else return 1.;
}



// Multiply: res = ds_mul(a, b) => res = a * b
vec2 ds_mul (vec2 dsa, vec2 dsb) {
   vec2 dsc;
   float c11, c21, c2, e, t1, t2;
   float a1, a2, b1, b2, cona, conb, split = 8193.;

   cona = dsa.x * split;
   conb = dsb.x * split;
   a1 = cona - (cona - dsa.x);
   b1 = conb - (conb - dsb.x);
   a2 = dsa.x - a1;
   b2 = dsb.x - b1;

   c11 = dsa.x * dsb.x;
   c21 = a2 * b2 + (a2 * b1 + (a1 * b2 + (a1 * b1 - c11)));

   c2 = dsa.x * dsb.y + dsa.y * dsb.x;

   t1 = c11 + c2;
   e = t1 - c11;
   t2 = dsa.y * dsb.y + ((c2 - e) + (c11 - (t1 - e))) + c21;
   
   dsc.x = t1 + t2;
   dsc.y = t2 - (dsc.x - t1);
 
   return dsc;
}




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
/*
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
*/




// Generate a julia fractal
//int c_julia(float x, float y, int iter) {
int c_julia(vec2 x, vec2 y, int iter) {
   vec2 czX = x;
   vec2 czY = y;

   vec2 ccX = ds_set(juliaReal);
   vec2 ccY = ds_set(juliaImaginary);

   //vec2 cc = vec2(0.35, 0.5);

   int n = 0;
   while (n < iter ) {
      vec2 tX = ds_sub(ds_mul(czX, czX), ds_mul(czY, czY));
      vec2 tY = ds_add(ds_mul(czX, czY), ds_mul(czY, czX));
      czX = tX;
      czY = tY;

      czX = ds_add(czX, ccX);
      czY = ds_add(czY, ccY);

      if ( ds_compare( ds_add( ds_mul(czX, czX), ds_mul(czY, czY)), ds_set(16.0)) > 0)
         break;


      //cz = cmult(cz, cz);
      //cz = cadd(cz, cc);
      n++;
   }

   return n > iter ? 0:n;
}




void main(void) {

//   float xdist = (cmaxX-cminX)/width;
//   float ydist = (cmaxY-cminY)/height;

   vec2 xdist = ds_mul( ds_sub( vec2(cmaxX1, cmaxX2), vec2(cminX1, cminX2)), ds_set(1./width));
   vec2 ydist = ds_mul( ds_sub( vec2(cmaxY1, cmaxY2), vec2(cminY1, cminY2)), ds_set(1./height));

   vec2 X = ds_add(vec2(cminX1, cminX2), ds_mul( ds_set(gl_FragCoord.x), xdist));
   vec2 Y = ds_add(vec2(cminY1, cminY2), ds_mul( ds_set(gl_FragCoord.y), ydist));

//   float X = cminX + gl_FragCoord.x * xdist;
//   float Y = cminY + gl_FragCoord.y * ydist;

   int result = 0;
   if (flag == 0)
      result = c_julia(X, Y, iter);
   else if (flag == 1)
      result = c_julia(X, Y, iter);
   else 
      result = c_julia(X, Y, iter);
   
   //gl_FragColor.rgb = vec3( 0.5*gl_FragCoord.x/width, 0.5*gl_FragCoord.y/height, 0);
   if (result > 1) {
      /*
      gl_FragColor.rgb += vec3(
         log(result)/5.0,
         mod((float)result, 85)/85.0,
         juliaImaginary
      );
      */
      gl_FragColor.rgb = vec3(
         cos(0.3005*result),
         -cos(0.08*result+1)/2.0,
         log(result)/3.0 
      );
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
