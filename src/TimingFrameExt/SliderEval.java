package TimingFrameExt;

import gui.DCPair;

import org.jdesktop.animation.timing.interpolation.Evaluator;

/////////////////////////////////////////////////////////////////////////////////
// Assumes chronological order
/////////////////////////////////////////////////////////////////////////////////
public class SliderEval extends Evaluator<DCPair[]>{

   @Override
   public DCPair[] evaluate(DCPair[] v0, DCPair[] v1, float fraction) {
      /*
      if (v0 == null) { System.out.println("Dho 1"); System.exit(0); }
      if (v1 == null) { System.out.println("Dho 2"); System.exit(0); }
      */
      
      // If the # gets bigger, we need to extend the array eh ?
      int size = v1.length;
      DCPair r[] = new DCPair[ size ]; 
      
      for (int i=0; i < size; i++) {
         if (i < v0.length) {
            r[i] = new DCPair("", 0.0);
            r[i].key = v1[i].key;
            r[i].value = v0[i].value + (v1[i].value - v0[i].value)*fraction;
         } else {
            r[i] = new DCPair("", 0.0);
            r[i].key = v1[i].key;
            r[i].value = v1[i].value*fraction;
         }
      }
      return r;
   }

}
