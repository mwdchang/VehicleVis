package TimingFrameExt;

import org.jdesktop.animation.timing.interpolation.Evaluator;

/////////////////////////////////////////////////////////////////////////////////
// Evaluates and interpolates a double value
/////////////////////////////////////////////////////////////////////////////////
public class DoubleEval extends Evaluator<Double> {

   @Override
   public Double evaluate(Double v0, Double v1, float fraction) {
      return (v0.doubleValue() + (v1.doubleValue() - v0.doubleValue()) * fraction);
   }

}
