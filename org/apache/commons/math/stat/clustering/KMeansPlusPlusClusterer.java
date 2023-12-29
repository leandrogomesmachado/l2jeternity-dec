package org.apache.commons.math.stat.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.stat.descriptive.moment.Variance;

public class KMeansPlusPlusClusterer<T extends Clusterable<T>> {
   private final Random random;
   private final KMeansPlusPlusClusterer.EmptyClusterStrategy emptyStrategy;

   public KMeansPlusPlusClusterer(Random random) {
      this(random, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
   }

   public KMeansPlusPlusClusterer(Random random, KMeansPlusPlusClusterer.EmptyClusterStrategy emptyStrategy) {
      this.random = random;
      this.emptyStrategy = emptyStrategy;
   }

   public List<Cluster<T>> cluster(Collection<T> points, int k, int maxIterations) {
      List<Cluster<T>> clusters = chooseInitialCenters(points, k, this.random);
      assignPointsToClusters(clusters, points);
      int max = maxIterations < 0 ? Integer.MAX_VALUE : maxIterations;

      for(int count = 0; count < max; ++count) {
         boolean clusteringChanged = false;
         List<Cluster<T>> newClusters = new ArrayList<>();

         for(Cluster<T> cluster : clusters) {
            T newCenter;
            if (cluster.getPoints().isEmpty()) {
               switch(this.emptyStrategy) {
                  case LARGEST_VARIANCE:
                     newCenter = this.getPointFromLargestVarianceCluster(clusters);
                     break;
                  case LARGEST_POINTS_NUMBER:
                     newCenter = this.getPointFromLargestNumberCluster(clusters);
                     break;
                  case FARTHEST_POINT:
                     newCenter = this.getFarthestPoint(clusters);
                     break;
                  default:
                     throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
               }

               clusteringChanged = true;
            } else {
               newCenter = cluster.getCenter().centroidOf(cluster.getPoints());
               if (!newCenter.equals(cluster.getCenter())) {
                  clusteringChanged = true;
               }
            }

            newClusters.add(new Cluster<>(newCenter));
         }

         if (!clusteringChanged) {
            return clusters;
         }

         assignPointsToClusters(newClusters, points);
         clusters = newClusters;
      }

      return clusters;
   }

   private static <T extends Clusterable<T>> void assignPointsToClusters(Collection<Cluster<T>> clusters, Collection<T> points) {
      for(T p : points) {
         Cluster<T> cluster = getNearestCluster(clusters, p);
         cluster.addPoint(p);
      }
   }

   private static <T extends Clusterable<T>> List<Cluster<T>> chooseInitialCenters(Collection<T> points, int k, Random random) {
      List<T> pointSet = new ArrayList<>(points);
      List<Cluster<T>> resultSet = new ArrayList<>();
      T firstPoint = pointSet.remove(random.nextInt(pointSet.size()));
      resultSet.add(new Cluster<>(firstPoint));
      double[] dx2 = new double[pointSet.size()];

      while(resultSet.size() < k) {
         int sum = 0;

         for(int i = 0; i < pointSet.size(); ++i) {
            T p = pointSet.get(i);
            Cluster<T> nearest = getNearestCluster(resultSet, p);
            double d = p.distanceFrom(nearest.getCenter());
            sum = (int)((double)sum + d * d);
            dx2[i] = (double)sum;
         }

         double r = random.nextDouble() * (double)sum;

         for(int i = 0; i < dx2.length; ++i) {
            if (dx2[i] >= r) {
               T p = pointSet.remove(i);
               resultSet.add(new Cluster<>(p));
               break;
            }
         }
      }

      return resultSet;
   }

   private T getPointFromLargestVarianceCluster(Collection<Cluster<T>> clusters) {
      double maxVariance = Double.NEGATIVE_INFINITY;
      Cluster<T> selected = null;

      for(Cluster<T> cluster : clusters) {
         if (!cluster.getPoints().isEmpty()) {
            T center = cluster.getCenter();
            Variance stat = new Variance();

            for(T point : cluster.getPoints()) {
               stat.increment(point.distanceFrom(center));
            }

            double variance = stat.getResult();
            if (variance > maxVariance) {
               maxVariance = variance;
               selected = cluster;
            }
         }
      }

      if (selected == null) {
         throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
      } else {
         List<T> selectedPoints = selected.getPoints();
         return selectedPoints.remove(this.random.nextInt(selectedPoints.size()));
      }
   }

   private T getPointFromLargestNumberCluster(Collection<Cluster<T>> clusters) {
      int maxNumber = 0;
      Cluster<T> selected = null;

      for(Cluster<T> cluster : clusters) {
         int number = cluster.getPoints().size();
         if (number > maxNumber) {
            maxNumber = number;
            selected = cluster;
         }
      }

      if (selected == null) {
         throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
      } else {
         List<T> selectedPoints = selected.getPoints();
         return selectedPoints.remove(this.random.nextInt(selectedPoints.size()));
      }
   }

   private T getFarthestPoint(Collection<Cluster<T>> clusters) {
      double maxDistance = Double.NEGATIVE_INFINITY;
      Cluster<T> selectedCluster = null;
      int selectedPoint = -1;

      for(Cluster<T> cluster : clusters) {
         T center = cluster.getCenter();
         List<T> points = cluster.getPoints();

         for(int i = 0; i < points.size(); ++i) {
            double distance = points.get(i).distanceFrom(center);
            if (distance > maxDistance) {
               maxDistance = distance;
               selectedCluster = cluster;
               selectedPoint = i;
            }
         }
      }

      if (selectedCluster == null) {
         throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
      } else {
         return selectedCluster.getPoints().remove(selectedPoint);
      }
   }

   private static <T extends Clusterable<T>> Cluster<T> getNearestCluster(Collection<Cluster<T>> clusters, T point) {
      double minDistance = Double.MAX_VALUE;
      Cluster<T> minCluster = null;

      for(Cluster<T> c : clusters) {
         double distance = point.distanceFrom(c.getCenter());
         if (distance < minDistance) {
            minDistance = distance;
            minCluster = c;
         }
      }

      return minCluster;
   }

   public static enum EmptyClusterStrategy {
      LARGEST_VARIANCE,
      LARGEST_POINTS_NUMBER,
      FARTHEST_POINT,
      ERROR;
   }
}
