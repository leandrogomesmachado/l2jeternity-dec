package org.apache.commons.math;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;
import org.apache.commons.math.exception.MathThrowable;
import org.apache.commons.math.exception.util.DummyLocalizable;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class MathException extends Exception implements MathThrowable {
   private static final long serialVersionUID = 7428019509644517071L;
   private final Localizable pattern;
   private final Object[] arguments;

   public MathException() {
      this.pattern = LocalizedFormats.SIMPLE_MESSAGE;
      this.arguments = new Object[]{""};
   }

   @Deprecated
   public MathException(String pattern, Object... arguments) {
      this(new DummyLocalizable(pattern), arguments);
   }

   public MathException(Localizable pattern, Object... arguments) {
      this.pattern = pattern;
      this.arguments = arguments == null ? new Object[0] : arguments.clone();
   }

   public MathException(Throwable rootCause) {
      super(rootCause);
      this.pattern = LocalizedFormats.SIMPLE_MESSAGE;
      this.arguments = new Object[]{rootCause == null ? "" : rootCause.getMessage()};
   }

   @Deprecated
   public MathException(Throwable rootCause, String pattern, Object... arguments) {
      this(rootCause, new DummyLocalizable(pattern), arguments);
   }

   public MathException(Throwable rootCause, Localizable pattern, Object... arguments) {
      super(rootCause);
      this.pattern = pattern;
      this.arguments = arguments == null ? new Object[0] : arguments.clone();
   }

   @Deprecated
   public String getPattern() {
      return this.pattern.getSourceString();
   }

   @Override
   public Localizable getSpecificPattern() {
      return null;
   }

   @Override
   public Localizable getGeneralPattern() {
      return this.pattern;
   }

   @Override
   public Object[] getArguments() {
      return this.arguments.clone();
   }

   @Override
   public String getMessage(Locale locale) {
      return this.pattern != null ? new MessageFormat(this.pattern.getLocalizedString(locale), locale).format(this.arguments) : "";
   }

   @Override
   public String getMessage() {
      return this.getMessage(Locale.US);
   }

   @Override
   public String getLocalizedMessage() {
      return this.getMessage(Locale.getDefault());
   }

   @Override
   public void printStackTrace() {
      this.printStackTrace(System.err);
   }

   @Override
   public void printStackTrace(PrintStream out) {
      synchronized(out) {
         PrintWriter pw = new PrintWriter(out, false);
         this.printStackTrace(pw);
         pw.flush();
      }
   }
}
