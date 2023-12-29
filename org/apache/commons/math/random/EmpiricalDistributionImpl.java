package org.apache.commons.math.random;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.util.FastMath;

public class EmpiricalDistributionImpl implements Serializable, EmpiricalDistribution {
   private static final long serialVersionUID = 5729073523949762654L;
   private final List<SummaryStatistics> binStats;
   private SummaryStatistics sampleStats = null;
   private double max = Double.NEGATIVE_INFINITY;
   private double min = Double.POSITIVE_INFINITY;
   private double delta = 0.0;
   private final int binCount;
   private boolean loaded = false;
   private double[] upperBounds = null;
   private final RandomData randomData = new RandomDataImpl();

   public EmpiricalDistributionImpl() {
      this.binCount = 1000;
      this.binStats = new ArrayList<>();
   }

   public EmpiricalDistributionImpl(int binCount) {
      this.binCount = binCount;
      this.binStats = new ArrayList<>();
   }

   @Override
   public void load(double[] in) {
      EmpiricalDistributionImpl.DataAdapter da = new EmpiricalDistributionImpl.ArrayDataAdapter(in);

      try {
         da.computeStats();
         this.fillBinStats(in);
      } catch (IOException var4) {
         throw new MathRuntimeException(var4);
      }

      this.loaded = true;
   }

   @Override
   public void load(URL url) throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

      try {
         EmpiricalDistributionImpl.DataAdapter da = new EmpiricalDistributionImpl.StreamDataAdapter(in);
         da.computeStats();
         if (this.sampleStats.getN() == 0L) {
            throw MathRuntimeException.createEOFException(LocalizedFormats.URL_CONTAINS_NO_DATA, url);
         }

         in = new BufferedReader(new InputStreamReader(url.openStream()));
         this.fillBinStats(in);
         this.loaded = true;
      } finally {
         try {
            in.close();
         } catch (IOException var9) {
         }
      }
   }

   @Override
   public void load(File file) throws IOException {
      BufferedReader in = new BufferedReader(new FileReader(file));

      try {
         EmpiricalDistributionImpl.DataAdapter da = new EmpiricalDistributionImpl.StreamDataAdapter(in);
         da.computeStats();
         in = new BufferedReader(new FileReader(file));
         this.fillBinStats(in);
         this.loaded = true;
      } finally {
         try {
            in.close();
         } catch (IOException var9) {
         }
      }
   }

   private void fillBinStats(Object in) throws IOException {
      this.min = this.sampleStats.getMin();
      this.max = this.sampleStats.getMax();
      this.delta = (this.max - this.min) / Double.valueOf((double)this.binCount);
      if (!this.binStats.isEmpty()) {
         this.binStats.clear();
      }

      for(int i = 0; i < this.binCount; ++i) {
         SummaryStatistics stats = new SummaryStatistics();
         this.binStats.add(i, stats);
      }

      EmpiricalDistributionImpl.DataAdapterFactory aFactory = new EmpiricalDistributionImpl.DataAdapterFactory();
      EmpiricalDistributionImpl.DataAdapter da = aFactory.getAdapter(in);
      da.computeBinStats();
      this.upperBounds = new double[this.binCount];
      this.upperBounds[0] = (double)this.binStats.get(0).getN() / (double)this.sampleStats.getN();

      for(int i = 1; i < this.binCount - 1; ++i) {
         this.upperBounds[i] = this.upperBounds[i - 1] + (double)this.binStats.get(i).getN() / (double)this.sampleStats.getN();
      }

      this.upperBounds[this.binCount - 1] = 1.0;
   }

   private int findBin(double value) {
      return FastMath.min(FastMath.max((int)FastMath.ceil((value - this.min) / this.delta) - 1, 0), this.binCount - 1);
   }

   @Override
   public double getNextValue() throws IllegalStateException {
      if (!this.loaded) {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.DISTRIBUTION_NOT_LOADED);
      } else {
         double x = FastMath.random();

         for(int i = 0; i < this.binCount; ++i) {
            if (x <= this.upperBounds[i]) {
               SummaryStatistics stats = this.binStats.get(i);
               if (stats.getN() > 0L) {
                  if (stats.getStandardDeviation() > 0.0) {
                     return this.randomData.nextGaussian(stats.getMean(), stats.getStandardDeviation());
                  }

                  return stats.getMean();
               }
            }
         }

         throw new MathRuntimeException(LocalizedFormats.NO_BIN_SELECTED);
      }
   }

   @Override
   public StatisticalSummary getSampleStats() {
      return this.sampleStats;
   }

   @Override
   public int getBinCount() {
      return this.binCount;
   }

   @Override
   public List<SummaryStatistics> getBinStats() {
      return this.binStats;
   }

   @Override
   public double[] getUpperBounds() {
      double[] binUpperBounds = new double[this.binCount];
      binUpperBounds[0] = this.min + this.delta;

      for(int i = 1; i < this.binCount - 1; ++i) {
         binUpperBounds[i] = binUpperBounds[i - 1] + this.delta;
      }

      binUpperBounds[this.binCount - 1] = this.max;
      return binUpperBounds;
   }

   public double[] getGeneratorUpperBounds() {
      int len = this.upperBounds.length;
      double[] out = new double[len];
      System.arraycopy(this.upperBounds, 0, out, 0, len);
      return out;
   }

   @Override
   public boolean isLoaded() {
      return this.loaded;
   }

   private class ArrayDataAdapter extends EmpiricalDistributionImpl.DataAdapter {
      private double[] inputArray;

      public ArrayDataAdapter(double[] in) {
         this.inputArray = in;
      }

      @Override
      public void computeStats() throws IOException {
         EmpiricalDistributionImpl.this.sampleStats = new SummaryStatistics();

         for(int i = 0; i < this.inputArray.length; ++i) {
            EmpiricalDistributionImpl.this.sampleStats.addValue(this.inputArray[i]);
         }
      }

      @Override
      public void computeBinStats() throws IOException {
         for(int i = 0; i < this.inputArray.length; ++i) {
            SummaryStatistics stats = EmpiricalDistributionImpl.this.binStats.get(EmpiricalDistributionImpl.this.findBin(this.inputArray[i]));
            stats.addValue(this.inputArray[i]);
         }
      }
   }

   private abstract class DataAdapter {
      private DataAdapter() {
      }

      public abstract void computeBinStats() throws IOException;

      public abstract void computeStats() throws IOException;
   }

   private class DataAdapterFactory {
      private DataAdapterFactory() {
      }

      public EmpiricalDistributionImpl.DataAdapter getAdapter(Object in) {
         if (in instanceof BufferedReader) {
            BufferedReader inputStream = (BufferedReader)in;
            return EmpiricalDistributionImpl.this.new StreamDataAdapter(inputStream);
         } else if (in instanceof double[]) {
            double[] inputArray = (double[])in;
            return EmpiricalDistributionImpl.this.new ArrayDataAdapter(inputArray);
         } else {
            throw MathRuntimeException.createIllegalArgumentException(
               LocalizedFormats.INPUT_DATA_FROM_UNSUPPORTED_DATASOURCE, in.getClass().getName(), BufferedReader.class.getName(), double[].class.getName()
            );
         }
      }
   }

   private class StreamDataAdapter extends EmpiricalDistributionImpl.DataAdapter {
      private BufferedReader inputStream;

      public StreamDataAdapter(BufferedReader in) {
         this.inputStream = in;
      }

      @Override
      public void computeBinStats() throws IOException {
         String str = null;
         double val = 0.0;

         while((str = this.inputStream.readLine()) != null) {
            val = Double.parseDouble(str);
            SummaryStatistics stats = EmpiricalDistributionImpl.this.binStats.get(EmpiricalDistributionImpl.this.findBin(val));
            stats.addValue(val);
         }

         this.inputStream.close();
         this.inputStream = null;
      }

      @Override
      public void computeStats() throws IOException {
         String str = null;
         double val = 0.0;
         EmpiricalDistributionImpl.this.sampleStats = new SummaryStatistics();

         while((str = this.inputStream.readLine()) != null) {
            val = Double.valueOf(str);
            EmpiricalDistributionImpl.this.sampleStats.addValue(val);
         }

         this.inputStream.close();
         this.inputStream = null;
      }
   }
}
