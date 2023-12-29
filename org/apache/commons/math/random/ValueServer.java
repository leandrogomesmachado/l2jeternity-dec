package org.apache.commons.math.random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class ValueServer {
   public static final int DIGEST_MODE = 0;
   public static final int REPLAY_MODE = 1;
   public static final int UNIFORM_MODE = 2;
   public static final int EXPONENTIAL_MODE = 3;
   public static final int GAUSSIAN_MODE = 4;
   public static final int CONSTANT_MODE = 5;
   private int mode = 5;
   private URL valuesFileURL = null;
   private double mu = 0.0;
   private double sigma = 0.0;
   private EmpiricalDistribution empiricalDistribution = null;
   private BufferedReader filePointer = null;
   private final RandomData randomData;

   public ValueServer() {
      this.randomData = new RandomDataImpl();
   }

   public ValueServer(RandomData randomData) {
      this.randomData = randomData;
   }

   public double getNext() throws IOException {
      switch(this.mode) {
         case 0:
            return this.getNextDigest();
         case 1:
            return this.getNextReplay();
         case 2:
            return this.getNextUniform();
         case 3:
            return this.getNextExponential();
         case 4:
            return this.getNextGaussian();
         case 5:
            return this.mu;
         default:
            throw MathRuntimeException.createIllegalStateException(
               LocalizedFormats.UNKNOWN_MODE,
               this.mode,
               "DIGEST_MODE",
               0,
               "REPLAY_MODE",
               1,
               "UNIFORM_MODE",
               2,
               "EXPONENTIAL_MODE",
               3,
               "GAUSSIAN_MODE",
               4,
               "CONSTANT_MODE",
               5
            );
      }
   }

   public void fill(double[] values) throws IOException {
      for(int i = 0; i < values.length; ++i) {
         values[i] = this.getNext();
      }
   }

   public double[] fill(int length) throws IOException {
      double[] out = new double[length];

      for(int i = 0; i < length; ++i) {
         out[i] = this.getNext();
      }

      return out;
   }

   public void computeDistribution() throws IOException {
      this.empiricalDistribution = new EmpiricalDistributionImpl();
      this.empiricalDistribution.load(this.valuesFileURL);
   }

   public void computeDistribution(int binCount) throws IOException {
      this.empiricalDistribution = new EmpiricalDistributionImpl(binCount);
      this.empiricalDistribution.load(this.valuesFileURL);
      this.mu = this.empiricalDistribution.getSampleStats().getMean();
      this.sigma = this.empiricalDistribution.getSampleStats().getStandardDeviation();
   }

   public int getMode() {
      return this.mode;
   }

   public void setMode(int mode) {
      this.mode = mode;
   }

   public URL getValuesFileURL() {
      return this.valuesFileURL;
   }

   public void setValuesFileURL(String url) throws MalformedURLException {
      this.valuesFileURL = new URL(url);
   }

   public void setValuesFileURL(URL url) {
      this.valuesFileURL = url;
   }

   public EmpiricalDistribution getEmpiricalDistribution() {
      return this.empiricalDistribution;
   }

   public void resetReplayFile() throws IOException {
      if (this.filePointer != null) {
         try {
            this.filePointer.close();
            this.filePointer = null;
         } catch (IOException var2) {
         }
      }

      this.filePointer = new BufferedReader(new InputStreamReader(this.valuesFileURL.openStream()));
   }

   public void closeReplayFile() throws IOException {
      if (this.filePointer != null) {
         this.filePointer.close();
         this.filePointer = null;
      }
   }

   public double getMu() {
      return this.mu;
   }

   public void setMu(double mu) {
      this.mu = mu;
   }

   public double getSigma() {
      return this.sigma;
   }

   public void setSigma(double sigma) {
      this.sigma = sigma;
   }

   private double getNextDigest() {
      if (this.empiricalDistribution != null && this.empiricalDistribution.getBinStats().size() != 0) {
         return this.empiricalDistribution.getNextValue();
      } else {
         throw MathRuntimeException.createIllegalStateException(LocalizedFormats.DIGEST_NOT_INITIALIZED);
      }
   }

   private double getNextReplay() throws IOException {
      String str = null;
      if (this.filePointer == null) {
         this.resetReplayFile();
      }

      if ((str = this.filePointer.readLine()) == null) {
         this.closeReplayFile();
         this.resetReplayFile();
         if ((str = this.filePointer.readLine()) == null) {
            throw MathRuntimeException.createEOFException(LocalizedFormats.URL_CONTAINS_NO_DATA, this.valuesFileURL);
         }
      }

      return Double.valueOf(str);
   }

   private double getNextUniform() {
      return this.randomData.nextUniform(0.0, 2.0 * this.mu);
   }

   private double getNextExponential() {
      return this.randomData.nextExponential(this.mu);
   }

   private double getNextGaussian() {
      return this.randomData.nextGaussian(this.mu, this.sigma);
   }
}
