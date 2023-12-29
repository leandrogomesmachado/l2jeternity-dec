package l2e.commons.util;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public final class Rnd {
   protected static final Rnd.L2Random RND = new Rnd.L2Random();
   private static final long ADDEND = 11L;
   private static final long MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final Rnd.RandomContainer rnd = newInstance(Rnd.RandomType.UNSECURE_THREAD_LOCAL);
   protected static volatile long SEED_UNIQUIFIER = 8682522807148012L;

   public static final Random directRandom() {
      return rnd.directRandom();
   }

   public static final double get() {
      return rnd.nextDouble();
   }

   public static final int get(int n) {
      return rnd.get(n);
   }

   public static final int get(int min, int max) {
      return rnd.get(min, max);
   }

   public static final long get(long min, long max) {
      return rnd.get(min, max);
   }

   public static final Rnd.RandomContainer newInstance(Rnd.RandomType type) {
      switch(type) {
         case UNSECURE_ATOMIC:
            return new Rnd.RandomContainer(new Random());
         case UNSECURE_VOLATILE:
            return new Rnd.RandomContainer(new Rnd.NonAtomicRandom());
         case UNSECURE_THREAD_LOCAL:
            return new Rnd.RandomContainer(new Rnd.ThreadLocalRandom());
         case SECURE:
            return new Rnd.RandomContainer(new SecureRandom());
         default:
            throw new IllegalArgumentException();
      }
   }

   public static final boolean nextBoolean() {
      return rnd.nextBoolean();
   }

   public static final void nextBytes(byte[] array) {
      rnd.nextBytes(array);
   }

   public static final double nextDouble() {
      return rnd.nextDouble();
   }

   public static final float nextFloat() {
      return rnd.nextFloat();
   }

   public static final double nextGaussian() {
      return rnd.nextGaussian();
   }

   public static final int nextInt() {
      return rnd.nextInt();
   }

   public static final int nextInt(int n) {
      return get(n);
   }

   public static final long nextLong() {
      return rnd.nextLong();
   }

   public static final boolean getChance(int n) {
      return n > RND.nextInt() * 100;
   }

   public static boolean calcChance(int chance, int maxChance) {
      return chance > RND.nextInt(maxChance);
   }

   public static boolean calcChance(double chance, int maxChance) {
      return chance > RND.nextDouble((double)maxChance);
   }

   public static boolean calcChance(double chance) {
      return chance > RND.nextDouble();
   }

   public static boolean chance(int chance) {
      return chance >= 1 && (chance > 99 || RND.nextInt(99) + 1 <= chance);
   }

   public static boolean chance(double chance) {
      return RND.nextDouble() <= chance / 100.0;
   }

   public static int get(int[] list) {
      return list.length == 0 ? 0 : list[get(list.length)];
   }

   public static <E> E get(List<E> list) {
      return list.size() == 0 ? null : list.get(get(list.size()));
   }

   public static <E> E get(E[] list) {
      return list.length == 0 ? null : list[get(list.length)];
   }

   protected static final class L2Random extends Random {
      private static final long serialVersionUID = 2089256427272977088L;
      private static final long multiplier = 25214903917L;
      private static final long addend = 11L;
      private static final long mask = 281474976710655L;
      private long seed;

      @Override
      public synchronized void setSeed(long newSeed) {
         this.seed = (newSeed ^ 25214903917L) & 281474976710655L;
         super.setSeed(newSeed);
      }

      @Override
      protected int next(int bits) {
         long nextseed = this.seed = this.seed * 25214903917L + 11L & 281474976710655L;
         return (int)(nextseed >>> 48 - bits);
      }

      @Override
      public double nextDouble() {
         return (double)this.next(31) / 2.147483647E9;
      }

      public double nextDouble(double n) {
         return (double)(this.next(31) - 1) / 2.147483647E9 * n;
      }

      @Override
      public int nextInt(int n) {
         return (int)((double)(this.next(31) - 1) / 2.147483647E9 * (double)n);
      }
   }

   public static final class NonAtomicRandom extends Random {
      private static final long serialVersionUID = 1L;
      private volatile long _seed;

      public NonAtomicRandom() {
         this(++Rnd.SEED_UNIQUIFIER + System.nanoTime());
      }

      public NonAtomicRandom(long seed) {
         this.setSeed(seed);
      }

      @Override
      public final int next(int bits) {
         return (int)((this._seed = this._seed * 25214903917L + 11L & 281474976710655L) >>> 48 - bits);
      }

      @Override
      public final void setSeed(long seed) {
         this._seed = (seed ^ 25214903917L) & 281474976710655L;
      }
   }

   public static final class RandomContainer {
      private final Random _random;

      protected RandomContainer(Random random) {
         this._random = random;
      }

      public final Random directRandom() {
         return this._random;
      }

      public final double get() {
         return this._random.nextDouble();
      }

      public final int get(int n) {
         return (int)(this._random.nextDouble() * (double)n);
      }

      public final int get(int min, int max) {
         return min + (int)(this._random.nextDouble() * (double)(max - min + 1));
      }

      public final long get(long min, long max) {
         return min + (long)(this._random.nextDouble() * (double)(max - min + 1L));
      }

      public final boolean nextBoolean() {
         return this._random.nextBoolean();
      }

      public final void nextBytes(byte[] array) {
         this._random.nextBytes(array);
      }

      public final double nextDouble() {
         return this._random.nextDouble();
      }

      public final float nextFloat() {
         return this._random.nextFloat();
      }

      public final double nextGaussian() {
         return this._random.nextGaussian();
      }

      public final int nextInt() {
         return this._random.nextInt();
      }

      public final long nextLong() {
         return this._random.nextLong();
      }
   }

   public static enum RandomType {
      SECURE,
      UNSECURE_ATOMIC,
      UNSECURE_THREAD_LOCAL,
      UNSECURE_VOLATILE;
   }

   public static final class ThreadLocalRandom extends Random {
      private static final long serialVersionUID = 1L;
      private final ThreadLocal<Rnd.ThreadLocalRandom.Seed> _seedLocal;

      public ThreadLocalRandom() {
         this._seedLocal = new ThreadLocal<Rnd.ThreadLocalRandom.Seed>() {
            public final Rnd.ThreadLocalRandom.Seed initialValue() {
               return new Rnd.ThreadLocalRandom.Seed(++Rnd.SEED_UNIQUIFIER + System.nanoTime());
            }
         };
      }

      public ThreadLocalRandom(final long seed) {
         this._seedLocal = new ThreadLocal<Rnd.ThreadLocalRandom.Seed>() {
            public final Rnd.ThreadLocalRandom.Seed initialValue() {
               return new Rnd.ThreadLocalRandom.Seed(seed);
            }
         };
      }

      @Override
      public final int next(int bits) {
         return this._seedLocal.get().next(bits);
      }

      @Override
      public final void setSeed(long seed) {
         if (this._seedLocal != null) {
            this._seedLocal.get().setSeed(seed);
         }
      }

      private static final class Seed {
         long _seed;

         Seed(long seed) {
            this.setSeed(seed);
         }

         final int next(int bits) {
            return (int)((this._seed = this._seed * 25214903917L + 11L & 281474976710655L) >>> 48 - bits);
         }

         final void setSeed(long seed) {
            this._seed = (seed ^ 25214903917L) & 281474976710655L;
         }
      }
   }
}
