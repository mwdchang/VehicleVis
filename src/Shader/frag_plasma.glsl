////////////////////////////////////////////////////////////////////////////////
// Just for the fun of it...
//
////////////////////////////////////////////////////////////////////////////////

uniform float width;
uniform float height;

uniform float a;
uniform float b;
uniform float c;


float hyp(float a, float b) {
   return sqrt(a*a + b*b);
}

void main(void) {

   float x = gl_FragCoord.x;
   float y = gl_FragCoord.y;

/*
   x /= 10.0;
   y /= 10.0;
*/


   float midx = width / 2.0;
   float midy = height / 2.0;



   // I can't remember where I got this formula
   // but it seems to work rather nicely ....
   float val = 15 + 8 * (
       (1 + sin (
        + (sin ( (x+y)/a))
        + (sin (y / b))
        + (cos (hyp(x - midx, y - midy)/c)))
        ));

   val *= 2.2;


   //gl_FragColor.rgba = vec4( val/150.0, val/115.0, val/50.0, 0.4);
   gl_FragColor.rgba = vec4( 0.2, val/115.0, val/50.0, 0.4);



}


