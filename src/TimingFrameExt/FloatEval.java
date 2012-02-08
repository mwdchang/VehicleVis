package TimingFrameExt;

import org.jdesktop.animation.timing.interpolation.Evaluator;

public class FloatEval extends Evaluator<Float> {

   @Override
   public Float evaluate(Float v0, Float v1, float fraction) {
      return v0.floatValue() + ( v1.floatValue() - v0.floatValue()) * fraction;
   }

}
