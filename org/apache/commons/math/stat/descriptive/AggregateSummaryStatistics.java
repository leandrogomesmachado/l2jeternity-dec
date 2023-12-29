package org.apache.commons.math.stat.descriptive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class AggregateSummaryStatistics implements StatisticalSummary, Serializable {
   private static final long serialVersionUID = -8207112444016386906L;
   private final SummaryStatistics statisticsPrototype;
   private final SummaryStatistics statistics;

   public AggregateSummaryStatistics() {
      this(new SummaryStatistics());
   }

   public AggregateSummaryStatistics(SummaryStatistics prototypeStatistics) {
      this(prototypeStatistics, prototypeStatistics == null ? null : new SummaryStatistics(prototypeStatistics));
   }

   public AggregateSummaryStatistics(SummaryStatistics prototypeStatistics, SummaryStatistics initialStatistics) {
      this.statisticsPrototype = prototypeStatistics == null ? new SummaryStatistics() : prototypeStatistics;
      this.statistics = initialStatistics == null ? new SummaryStatistics() : initialStatistics;
   }

   @Override
   public double getMax() {
      synchronized(this.statistics) {
         return this.statistics.getMax();
      }
   }

   @Override
   public double getMean() {
      synchronized(this.statistics) {
         return this.statistics.getMean();
      }
   }

   @Override
   public double getMin() {
      synchronized(this.statistics) {
         return this.statistics.getMin();
      }
   }

   @Override
   public long getN() {
      synchronized(this.statistics) {
         return this.statistics.getN();
      }
   }

   @Override
   public double getStandardDeviation() {
      synchronized(this.statistics) {
         return this.statistics.getStandardDeviation();
      }
   }

   @Override
   public double getSum() {
      synchronized(this.statistics) {
         return this.statistics.getSum();
      }
   }

   @Override
   public double getVariance() {
      synchronized(this.statistics) {
         return this.statistics.getVariance();
      }
   }

   public double getSumOfLogs() {
      synchronized(this.statistics) {
         return this.statistics.getSumOfLogs();
      }
   }

   public double getGeometricMean() {
      synchronized(this.statistics) {
         return this.statistics.getGeometricMean();
      }
   }

   public double getSumsq() {
      synchronized(this.statistics) {
         return this.statistics.getSumsq();
      }
   }

   public double getSecondMoment() {
      synchronized(this.statistics) {
         return this.statistics.getSecondMoment();
      }
   }

   public StatisticalSummary getSummary() {
      synchronized(this.statistics) {
         return new StatisticalSummaryValues(this.getMean(), this.getVariance(), this.getN(), this.getMax(), this.getMin(), this.getSum());
      }
   }

   public SummaryStatistics createContributingStatistics() {
      SummaryStatistics contributingStatistics = new AggregateSummaryStatistics.AggregatingSummaryStatistics(this.statistics);
      SummaryStatistics.copy(this.statisticsPrototype, contributingStatistics);
      return contributingStatistics;
   }

   public static StatisticalSummaryValues aggregate(Collection<SummaryStatistics> statistics) {
      if (statistics == null) {
         return null;
      } else {
         Iterator<SummaryStatistics> iterator = statistics.iterator();
         if (!iterator.hasNext()) {
            return null;
         } else {
            SummaryStatistics current = iterator.next();
            long n = current.getN();
            double min = current.getMin();
            double sum = current.getSum();
            double max = current.getMax();
            double m2 = current.getSecondMoment();

            double mean;
            double oldN;
            double curN;
            double meanDiff;
            for(mean = current.getMean(); iterator.hasNext(); m2 = m2 + current.getSecondMoment() + meanDiff * meanDiff * oldN * curN / (double)n) {
               current = iterator.next();
               if (current.getMin() < min || Double.isNaN(min)) {
                  min = current.getMin();
               }

               if (current.getMax() > max || Double.isNaN(max)) {
                  max = current.getMax();
               }

               sum += current.getSum();
               oldN = (double)n;
               curN = (double)current.getN();
               n = (long)((double)n + curN);
               meanDiff = current.getMean() - mean;
               mean = sum / (double)n;
            }

            if (n == 0L) {
               oldN = Double.NaN;
            } else if (n == 1L) {
               oldN = 0.0;
            } else {
               oldN = m2 / (double)(n - 1L);
            }

            return new StatisticalSummaryValues(mean, oldN, n, max, min, sum);
         }
      }
   }

   private static class AggregatingSummaryStatistics extends SummaryStatistics {
      private static final long serialVersionUID = 1L;
      private final SummaryStatistics aggregateStatistics;

      public AggregatingSummaryStatistics(SummaryStatistics aggregateStatistics) {
         this.aggregateStatistics = aggregateStatistics;
      }

      @Override
      public void addValue(double value) {
         super.addValue(value);
         synchronized(this.aggregateStatistics) {
            this.aggregateStatistics.addValue(value);
         }
      }

      @Override
      public boolean equals(Object object) {
         if (object == this) {
            return true;
         } else if (!(object instanceof AggregateSummaryStatistics.AggregatingSummaryStatistics)) {
            return false;
         } else {
            AggregateSummaryStatistics.AggregatingSummaryStatistics stat = (AggregateSummaryStatistics.AggregatingSummaryStatistics)object;
            return super.equals(stat) && this.aggregateStatistics.equals(stat.aggregateStatistics);
         }
      }

      @Override
      public int hashCode() {
         return 123 + super.hashCode() + this.aggregateStatistics.hashCode();
      }
   }
}
