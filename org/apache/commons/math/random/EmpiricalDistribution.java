package org.apache.commons.math.random;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public interface EmpiricalDistribution {
   void load(double[] var1);

   void load(File var1) throws IOException;

   void load(URL var1) throws IOException;

   double getNextValue() throws IllegalStateException;

   StatisticalSummary getSampleStats() throws IllegalStateException;

   boolean isLoaded();

   int getBinCount();

   List<SummaryStatistics> getBinStats();

   double[] getUpperBounds();
}
