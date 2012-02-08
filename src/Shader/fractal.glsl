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


