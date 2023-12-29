package com.mysql.cj.conf;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.util.StringUtils;
import java.util.Arrays;

public class BooleanPropertyDefinition extends AbstractPropertyDefinition<Boolean> {
   private static final long serialVersionUID = -7288366734350231540L;

   public BooleanPropertyDefinition(
      String name,
      String alias,
      Boolean defaultValue,
      boolean isRuntimeModifiable,
      String description,
      String sinceVersion,
      String category,
      int orderInCategory
   ) {
      super(name, alias, defaultValue, isRuntimeModifiable, description, sinceVersion, category, orderInCategory);
   }

   @Override
   public String[] getAllowableValues() {
      return Arrays.stream(BooleanPropertyDefinition.AllowableValues.values()).map(Enum::toString).toArray(x$0 -> new String[x$0]);
   }

   public Boolean parseObject(String value, ExceptionInterceptor exceptionInterceptor) {
      try {
         return BooleanPropertyDefinition.AllowableValues.valueOf(value.toUpperCase()).asBoolean();
      } catch (Exception var4) {
         throw ExceptionFactory.createException(
            Messages.getString(
               "PropertyDefinition.1",
               new Object[]{this.getName(), StringUtils.stringArrayToString(this.getAllowableValues(), "'", "', '", "' or '", "'"), value}
            ),
            var4,
            exceptionInterceptor
         );
      }
   }

   @Override
   public RuntimeProperty<Boolean> createRuntimeProperty() {
      return new BooleanProperty(this);
   }

   public static enum AllowableValues {
      TRUE(true),
      FALSE(false),
      YES(true),
      NO(false);

      private boolean asBoolean;

      private AllowableValues(boolean booleanValue) {
         this.asBoolean = booleanValue;
      }

      public boolean asBoolean() {
         return this.asBoolean;
      }
   }
}
