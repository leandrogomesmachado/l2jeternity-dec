package org.apache.commons.math.estimation;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class SimpleEstimationProblem implements EstimationProblem {
   private final List<EstimatedParameter> parameters = new ArrayList<>();
   private final List<WeightedMeasurement> measurements = new ArrayList<>();

   @Override
   public EstimatedParameter[] getAllParameters() {
      return this.parameters.toArray(new EstimatedParameter[this.parameters.size()]);
   }

   @Override
   public EstimatedParameter[] getUnboundParameters() {
      List<EstimatedParameter> unbound = new ArrayList<>(this.parameters.size());

      for(EstimatedParameter p : this.parameters) {
         if (!p.isBound()) {
            unbound.add(p);
         }
      }

      return unbound.toArray(new EstimatedParameter[unbound.size()]);
   }

   @Override
   public WeightedMeasurement[] getMeasurements() {
      return this.measurements.toArray(new WeightedMeasurement[this.measurements.size()]);
   }

   protected void addParameter(EstimatedParameter p) {
      this.parameters.add(p);
   }

   protected void addMeasurement(WeightedMeasurement m) {
      this.measurements.add(m);
   }
}
