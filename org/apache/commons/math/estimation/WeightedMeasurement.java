package org.apache.commons.math.estimation;

import java.io.Serializable;

@Deprecated
public abstract class WeightedMeasurement implements Serializable {
   private static final long serialVersionUID = 4360046376796901941L;
   private final double weight;
   private final double measuredValue;
   private boolean ignored;

   public WeightedMeasurement(double weight, double measuredValue) {
      this.weight = weight;
      this.measuredValue = measuredValue;
      this.ignored = false;
   }

   public WeightedMeasurement(double weight, double measuredValue, boolean ignored) {
      this.weight = weight;
      this.measuredValue = measuredValue;
      this.ignored = ignored;
   }

   public double getWeight() {
      return this.weight;
   }

   public double getMeasuredValue() {
      return this.measuredValue;
   }

   public double getResidual() {
      return this.measuredValue - this.getTheoreticalValue();
   }

   public abstract double getTheoreticalValue();

   public abstract double getPartial(EstimatedParameter var1);

   public void setIgnored(boolean ignored) {
      this.ignored = ignored;
   }

   public boolean isIgnored() {
      return this.ignored;
   }
}
