package org.apache.commons.math;

public interface FieldElement<T> {
   T add(T var1);

   T subtract(T var1);

   T multiply(T var1);

   T divide(T var1) throws ArithmeticException;

   Field<T> getField();
}
