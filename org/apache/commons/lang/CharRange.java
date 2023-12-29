package org.apache.commons.lang;

import java.io.Serializable;

public final class CharRange implements Serializable {
   private static final long serialVersionUID = 8270183163158333422L;
   private final char start;
   private final char end;
   private final boolean negated;
   private transient String iToString;

   public CharRange(char ch) {
      this(ch, ch, false);
   }

   public CharRange(char ch, boolean negated) {
      this(ch, ch, negated);
   }

   public CharRange(char start, char end) {
      this(start, end, false);
   }

   public CharRange(char start, char end, boolean negated) {
      if (start > end) {
         char temp = start;
         start = end;
         end = temp;
      }

      this.start = start;
      this.end = end;
      this.negated = negated;
   }

   public char getStart() {
      return this.start;
   }

   public char getEnd() {
      return this.end;
   }

   public boolean isNegated() {
      return this.negated;
   }

   public boolean contains(char ch) {
      return (ch >= this.start && ch <= this.end) != this.negated;
   }

   public boolean contains(CharRange range) {
      if (range == null) {
         throw new IllegalArgumentException("The Range must not be null");
      } else if (this.negated) {
         if (range.negated) {
            return this.start >= range.start && this.end <= range.end;
         } else {
            return range.end < this.start || range.start > this.end;
         }
      } else if (range.negated) {
         return this.start == 0 && this.end == '\uffff';
      } else {
         return this.start <= range.start && this.end >= range.end;
      }
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CharRange)) {
         return false;
      } else {
         CharRange other = (CharRange)obj;
         return this.start == other.start && this.end == other.end && this.negated == other.negated;
      }
   }

   public int hashCode() {
      return 83 + this.start + 7 * this.end + (this.negated ? 1 : 0);
   }

   public String toString() {
      if (this.iToString == null) {
         StringBuffer buf = new StringBuffer(4);
         if (this.isNegated()) {
            buf.append('^');
         }

         buf.append(this.start);
         if (this.start != this.end) {
            buf.append('-');
            buf.append(this.end);
         }

         this.iToString = buf.toString();
      }

      return this.iToString;
   }
}
