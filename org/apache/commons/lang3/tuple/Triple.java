package org.apache.commons.lang3.tuple;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.builder.CompareToBuilder;

public abstract class Triple<L, M, R> implements Comparable<Triple<L, M, R>>, Serializable {
   private static final long serialVersionUID = 1L;

   public static <L, M, R> Triple<L, M, R> of(L left, M middle, R right) {
      return new ImmutableTriple<>(left, middle, right);
   }

   public abstract L getLeft();

   public abstract M getMiddle();

   public abstract R getRight();

   public int compareTo(Triple<L, M, R> other) {
      return new CompareToBuilder()
         .append(this.getLeft(), other.getLeft())
         .append(this.getMiddle(), other.getMiddle())
         .append(this.getRight(), other.getRight())
         .toComparison();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Triple)) {
         return false;
      } else {
         Triple<?, ?, ?> other = (Triple)obj;
         return Objects.equals(this.getLeft(), other.getLeft())
            && Objects.equals(this.getMiddle(), other.getMiddle())
            && Objects.equals(this.getRight(), other.getRight());
      }
   }

   @Override
   public int hashCode() {
      return (this.getLeft() == null ? 0 : this.getLeft().hashCode())
         ^ (this.getMiddle() == null ? 0 : this.getMiddle().hashCode())
         ^ (this.getRight() == null ? 0 : this.getRight().hashCode());
   }

   @Override
   public String toString() {
      return "(" + this.getLeft() + "," + this.getMiddle() + "," + this.getRight() + ")";
   }

   public String toString(String format) {
      return String.format(format, this.getLeft(), this.getMiddle(), this.getRight());
   }
}
