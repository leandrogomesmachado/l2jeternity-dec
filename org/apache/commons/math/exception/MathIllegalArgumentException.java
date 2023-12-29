package org.apache.commons.math.exception;

import java.util.Locale;
import org.apache.commons.math.exception.util.ArgUtils;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.MessageFactory;

public class MathIllegalArgumentException extends IllegalArgumentException implements MathThrowable {
   private static final long serialVersionUID = -6024911025449780478L;
   private final Localizable specific;
   private final Localizable general;
   private final Object[] arguments;

   protected MathIllegalArgumentException(Localizable specific, Localizable general, Object... args) {
      this.specific = specific;
      this.general = general;
      this.arguments = ArgUtils.flatten(args);
   }

   protected MathIllegalArgumentException(Localizable general, Object... args) {
      this(null, general, args);
   }

   @Override
   public Localizable getSpecificPattern() {
      return this.specific;
   }

   @Override
   public Localizable getGeneralPattern() {
      return this.general;
   }

   @Override
   public Object[] getArguments() {
      return this.arguments.clone();
   }

   @Override
   public String getMessage(Locale locale) {
      return MessageFactory.buildMessage(locale, this.specific, this.general, this.arguments);
   }

   @Override
   public String getMessage() {
      return this.getMessage(Locale.US);
   }

   @Override
   public String getLocalizedMessage() {
      return this.getMessage(Locale.getDefault());
   }
}
