package l2e.commons.math.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.commons.util.Rnd;

public class RndSelector<E> {
   private int totalWeight = 0;
   private final List<RndSelector<E>.RndNode<E>> nodes;

   public RndSelector() {
      this.nodes = new ArrayList<>();
   }

   public RndSelector(int initialCapacity) {
      this.nodes = new ArrayList<>(initialCapacity);
   }

   public void add(E value, int weight) {
      if (value != null && weight > 0) {
         this.totalWeight += weight;
         this.nodes.add(new RndSelector.RndNode<>(value, weight));
      }
   }

   public E chance(int maxWeight) {
      if (maxWeight <= 0) {
         return null;
      } else {
         Collections.sort(this.nodes);
         int r = Rnd.get(maxWeight);
         int weight = 0;

         for(int i = 0; i < this.nodes.size(); ++i) {
            if ((weight += this.nodes.get(i).weight) > r) {
               return (E)this.nodes.get(i).value;
            }
         }

         return null;
      }
   }

   public E chance() {
      return this.chance(100);
   }

   public E select() {
      return this.chance(this.totalWeight);
   }

   public void clear() {
      this.totalWeight = 0;
      this.nodes.clear();
   }

   private class RndNode<T> implements Comparable<RndSelector<E>.RndNode<T>> {
      private final T value;
      private final int weight;

      public RndNode(T value, int weight) {
         this.value = value;
         this.weight = weight;
      }

      public int compareTo(RndSelector<E>.RndNode<T> o) {
         return this.weight - this.weight;
      }
   }
}
