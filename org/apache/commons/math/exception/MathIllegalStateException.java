package org.apache.commons.math.exception;

import java.util.Locale;
import org.apache.commons.math.exception.util.ArgUtils;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.MessageFactory;

public class MathIllegalStateException extends IllegalStateException implements MathThrowable {
   private static final long serialVersionUID = -6024911025449780478L;
   private final Localizable specific;
   private final Localizable general;
   private final Object[] arguments;

   public MathIllegalStateException(Localizable specific, Localizable general, Object... args) {
      this(null, specific, general, args);
   }

   public MathIllegalStateException(Throwable cause, Localizable specific, Localizable general, Object... args) {
      super(cause);
      this.specific = specific;
      this.general = general;
      this.arguments = ArgUtils.flatten(args);
   }

   public MathIllegalStateException(Localizable general, Object... args) {
      this(null, null, general, args);
   }

   public MathIllegalStateException(Throwable cause, Localizable general, Object... args) {
      this(cause, null, general, args);
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
