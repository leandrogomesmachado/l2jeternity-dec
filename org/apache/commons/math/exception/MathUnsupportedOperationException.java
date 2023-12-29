package org.apache.commons.math.exception;

import java.util.Locale;
import org.apache.commons.math.exception.util.ArgUtils;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.exception.util.MessageFactory;

public class MathUnsupportedOperationException extends UnsupportedOperationException implements MathThrowable {
   private static final long serialVersionUID = -6024911025449780478L;
   private final Localizable specific;
   private final Object[] arguments;

   public MathUnsupportedOperationException(Object... args) {
      this(null, args);
   }

   public MathUnsupportedOperationException(Localizable specific, Object... args) {
      this.specific = specific;
      this.arguments = ArgUtils.flatten(args);
   }

   @Override
   public Localizable getSpecificPattern() {
      return this.specific;
   }

   @Override
   public Localizable getGeneralPattern() {
      return LocalizedFormats.UNSUPPORTED_OPERATION;
   }

   @Override
   public Object[] getArguments() {
      return this.arguments.clone();
   }

   @Override
   public String getMessage(Locale locale) {
      return MessageFactory.buildMessage(locale, this.specific, LocalizedFormats.UNSUPPORTED_OPERATION, this.arguments);
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
