package com.mysql.cj.conf;

import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;

public class IntegerPropertyDefinition extends AbstractPropertyDefinition<Integer> {
   private static final long serialVersionUID = 4151893695173946081L;
   protected int multiplier = 1;

   public IntegerPropertyDefinition(
      String name, String alias, int defaultValue, boolean isRuntimeModifiable, String description, String sinceVersion, String category, int orderInCategory
   ) {
      super(name, alias, defaultValue, isRuntimeModifiable, description, sinceVersion, category, orderInCategory);
   }

   public IntegerPropertyDefinition(
      String name,
      String alias,
      int defaultValue,
      boolean isRuntimeModifiable,
      String description,
      String sinceVersion,
      String category,
      int orderInCategory,
      int lowerBound,
      int upperBound
   ) {
      super(name, alias, defaultValue, isRuntimeModifiable, description, sinceVersion, category, orderInCategory, lowerBound, upperBound);
   }

   @Override
   public boolean isRangeBased() {
      return this.getUpperBound() != this.getLowerBound();
   }

   public Integer parseObject(String value, ExceptionInterceptor exceptionInterceptor) {
      try {
         int intValue = (int)(Double.valueOf(value) * (double)this.multiplier);
         return intValue;
      } catch (NumberFormatException var4) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class,
            "The connection property '" + this.getName() + "' only accepts integer values. The value '" + value + "' can not be converted to an integer.",
            exceptionInterceptor
         );
      }
   }

   @Override
   public RuntimeProperty<Integer> createRuntimeProperty() {
      return new IntegerProperty(this);
   }
}
