package org.apache.commons.math.stat;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class Frequency implements Serializable {
   private static final long serialVersionUID = -3845586908418844111L;
   private final TreeMap<Comparable<?>, Long> freqTable;

   public Frequency() {
      this.freqTable = new TreeMap<>();
   }

   public Frequency(Comparator<?> comparator) {
      this.freqTable = new TreeMap<>(comparator);
   }

   @Override
   public String toString() {
      NumberFormat nf = NumberFormat.getPercentInstance();
      StringBuilder outBuffer = new StringBuilder();
      outBuffer.append("Value \t Freq. \t Pct. \t Cum Pct. \n");

      for(Comparable<?> value : this.freqTable.keySet()) {
         outBuffer.append(value);
         outBuffer.append('\t');
         outBuffer.append(this.getCount(value));
         outBuffer.append('\t');
         outBuffer.append(nf.format(this.getPct(value)));
         outBuffer.append('\t');
         outBuffer.append(nf.format(this.getCumPct(value)));
         outBuffer.append('\n');
      }

      return outBuffer.toString();
   }

   @Deprecated
   public void addValue(Object v) {
      if (v instanceof Comparable) {
         this.addValue((Comparable<?>)v);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CLASS_DOESNT_IMPLEMENT_COMPARABLE, v.getClass().getName());
      }
   }

   public void addValue(Comparable<?> v) {
      Comparable<?> obj = v;
      if (v instanceof Integer) {
         obj = ((Integer)v).longValue();
      }

      try {
         Long count = this.freqTable.get(obj);
         if (count == null) {
            this.freqTable.put(obj, 1L);
         } else {
            this.freqTable.put(obj, count + 1L);
         }
      } catch (ClassCastException var4) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INSTANCES_NOT_COMPARABLE_TO_EXISTING_VALUES, v.getClass().getName());
      }
   }

   public void addValue(int v) {
      this.addValue((Comparable<?>)Long.valueOf((long)v));
   }

   @Deprecated
   public void addValue(Integer v) {
      this.addValue((Comparable<?>)Long.valueOf(v.longValue()));
   }

   public void addValue(long v) {
      this.addValue((Comparable<?>)Long.valueOf(v));
   }

   public void addValue(char v) {
      this.addValue((Comparable<?>)Character.valueOf(v));
   }

   public void clear() {
      this.freqTable.clear();
   }

   public Iterator<Comparable<?>> valuesIterator() {
      return this.freqTable.keySet().iterator();
   }

   public long getSumFreq() {
      long result = 0L;
      Iterator<Long> iterator = this.freqTable.values().iterator();

      while(iterator.hasNext()) {
         result += iterator.next();
      }

      return result;
   }

   @Deprecated
   public long getCount(Object v) {
      return this.getCount((Comparable<?>)v);
   }

   public long getCount(Comparable<?> v) {
      if (v instanceof Integer) {
         return this.getCount(((Integer)v).longValue());
      } else {
         long result = 0L;

         try {
            Long count = this.freqTable.get(v);
            if (count != null) {
               result = count;
            }
         } catch (ClassCastException var5) {
         }

         return result;
      }
   }

   public long getCount(int v) {
      return this.getCount((Comparable<?>)Long.valueOf((long)v));
   }

   public long getCount(long v) {
      return this.getCount((Comparable<?>)Long.valueOf(v));
   }

   public long getCount(char v) {
      return this.getCount((Comparable<?>)Character.valueOf(v));
   }

   public int getUniqueCount() {
      return this.freqTable.keySet().size();
   }

   @Deprecated
   public double getPct(Object v) {
      return this.getPct((Comparable<?>)v);
   }

   public double getPct(Comparable<?> v) {
      long sumFreq = this.getSumFreq();
      return sumFreq == 0L ? Double.NaN : (double)this.getCount(v) / (double)sumFreq;
   }

   public double getPct(int v) {
      return this.getPct((Comparable<?>)Long.valueOf((long)v));
   }

   public double getPct(long v) {
      return this.getPct((Comparable<?>)Long.valueOf(v));
   }

   public double getPct(char v) {
      return this.getPct((Comparable<?>)Character.valueOf(v));
   }

   @Deprecated
   public long getCumFreq(Object v) {
      return this.getCumFreq((Comparable<?>)v);
   }

   public long getCumFreq(Comparable<?> v) {
      if (this.getSumFreq() == 0L) {
         return 0L;
      } else if (v instanceof Integer) {
         return this.getCumFreq(((Integer)v).longValue());
      } else {
         Comparator<Comparable<?>> c = (Comparator<Comparable<?>>)this.freqTable.comparator();
         if (c == null) {
            c = new Frequency.NaturalComparator();
         }

         long result = 0L;

         try {
            Long value = this.freqTable.get(v);
            if (value != null) {
               result = value;
            }
         } catch (ClassCastException var7) {
            return result;
         }

         if (c.compare(v, this.freqTable.firstKey()) < 0) {
            return 0L;
         } else if (c.compare(v, this.freqTable.lastKey()) >= 0) {
            return this.getSumFreq();
         } else {
            Comparable<?> nextValue;
            for(Iterator<Comparable<?>> values = this.valuesIterator(); values.hasNext(); result += this.getCount(nextValue)) {
               nextValue = values.next();
               if (c.compare(v, nextValue) <= 0) {
                  return result;
               }
            }

            return result;
         }
      }
   }

   public long getCumFreq(int v) {
      return this.getCumFreq((Comparable<?>)Long.valueOf((long)v));
   }

   public long getCumFreq(long v) {
      return this.getCumFreq((Comparable<?>)Long.valueOf(v));
   }

   public long getCumFreq(char v) {
      return this.getCumFreq((Comparable<?>)Character.valueOf(v));
   }

   @Deprecated
   public double getCumPct(Object v) {
      return this.getCumPct((Comparable<?>)v);
   }

   public double getCumPct(Comparable<?> v) {
      long sumFreq = this.getSumFreq();
      return sumFreq == 0L ? Double.NaN : (double)this.getCumFreq(v) / (double)sumFreq;
   }

   public double getCumPct(int v) {
      return this.getCumPct((Comparable<?>)Long.valueOf((long)v));
   }

   public double getCumPct(long v) {
      return this.getCumPct((Comparable<?>)Long.valueOf(v));
   }

   public double getCumPct(char v) {
      return this.getCumPct((Comparable<?>)Character.valueOf(v));
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      return 31 * result + (this.freqTable == null ? 0 : this.freqTable.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof Frequency)) {
         return false;
      } else {
         Frequency other = (Frequency)obj;
         if (this.freqTable == null) {
            if (other.freqTable != null) {
               return false;
            }
         } else if (!this.freqTable.equals(other.freqTable)) {
            return false;
         }

         return true;
      }
   }

   private static class NaturalComparator<T extends Comparable<T>> implements Comparator<Comparable<T>>, Serializable {
      private static final long serialVersionUID = -3852193713161395148L;

      private NaturalComparator() {
      }

      public int compare(Comparable<T> o1, Comparable<T> o2) {
         return o1.compareTo((T)o2);
      }
   }
}
