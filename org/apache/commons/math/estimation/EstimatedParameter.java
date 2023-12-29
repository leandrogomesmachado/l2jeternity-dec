package org.apache.commons.math.estimation;

import java.io.Serializable;

@Deprecated
public class EstimatedParameter implements Serializable {
   private static final long serialVersionUID = -555440800213416949L;
   protected double estimate;
   private final String name;
   private boolean bound;

   public EstimatedParameter(String name, double firstEstimate) {
      this.name = name;
      this.estimate = firstEstimate;
      this.bound = false;
   }

   public EstimatedParameter(String name, double firstEstimate, boolean bound) {
      this.name = name;
      this.estimate = firstEstimate;
      this.bound = bound;
   }

   public EstimatedParameter(EstimatedParameter parameter) {
      this.name = parameter.name;
      this.estimate = parameter.estimate;
      this.bound = parameter.bound;
   }

   public void setEstimate(double estimate) {
      this.estimate = estimate;
   }

   public double getEstimate() {
      return this.estimate;
   }

   public String getName() {
      return this.name;
   }

   public void setBound(boolean bound) {
      this.bound = bound;
   }

   public boolean isBound() {
      return this.bound;
   }
}
