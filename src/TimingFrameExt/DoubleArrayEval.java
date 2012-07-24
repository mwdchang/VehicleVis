package TimingFrameExt;

import org.jdesktop.animation.timing.interpolation.Evaluator;

public class DoubleArrayEval extends Evaluator<double[]> {

   @Override
   public double[] evaluate(double[] v0, double[] v1, float fraction) {
      double result[] = new double[ v0.length ];
      for (int i=0; i < v0.length; i++) {
         result[i] = v0[i] + (v1[i] - v0[i])*fraction;   
      }
      return result;
   }

}
