package TimingFrameExt;

import org.jdesktop.animation.timing.interpolation.Evaluator;
import model.DCColour;
public class DCColourEval extends Evaluator<DCColour> {

   @Override
   public DCColour evaluate(DCColour v0, DCColour v1, float fraction) {
      return new DCColour(
         v0.r + (v1.r - v0.r)*fraction,
         v0.g + (v1.g - v0.g)*fraction,
         v0.b + (v1.b - v0.b)*fraction,
         v0.a + (v1.a - v0.a)*fraction
      );
   }


}
