package com.mysql.cj.conf;

import com.mysql.cj.exceptions.ExceptionInterceptor;

public class StringPropertyDefinition extends AbstractPropertyDefinition<String> {
   private static final long serialVersionUID = 8228934389127796555L;

   public StringPropertyDefinition(
      String name,
      String alias,
      String defaultValue,
      boolean isRuntimeModifiable,
      String description,
      String sinceVersion,
      String category,
      int orderInCategory
   ) {
      super(name, alias, defaultValue, isRuntimeModifiable, description, sinceVersion, category, orderInCategory);
   }

   public String parseObject(String value, ExceptionInterceptor exceptionInterceptor) {
      return value;
   }

   @Override
   public RuntimeProperty<String> createRuntimeProperty() {
      return new StringProperty(this);
   }
}
