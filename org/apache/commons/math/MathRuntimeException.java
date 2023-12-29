package org.apache.commons.math;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.apache.commons.math.exception.MathThrowable;
import org.apache.commons.math.exception.util.DummyLocalizable;
import org.apache.commons.math.exception.util.Localizable;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class MathRuntimeException extends RuntimeException implements MathThrowable {
   private static final long serialVersionUID = 9058794795027570002L;
   private final Localizable pattern;
   private final Object[] arguments;

   @Deprecated
   public MathRuntimeException(String pattern, Object... arguments) {
      this(new DummyLocalizable(pattern), arguments);
   }

   public MathRuntimeException(Localizable pattern, Object... arguments) {
      this.pattern = pattern;
      this.arguments = arguments == null ? new Object[0] : arguments.clone();
   }

   public MathRuntimeException(Throwable rootCause) {
      super(rootCause);
      this.pattern = LocalizedFormats.SIMPLE_MESSAGE;
      this.arguments = new Object[]{rootCause == null ? "" : rootCause.getMessage()};
   }

   @Deprecated
   public MathRuntimeException(Throwable rootCause, String pattern, Object... arguments) {
      this(rootCause, new DummyLocalizable(pattern), arguments);
   }

   public MathRuntimeException(Throwable rootCause, Localizable pattern, Object... arguments) {
      super(rootCause);
      this.pattern = pattern;
      this.arguments = arguments == null ? new Object[0] : arguments.clone();
   }

   private static String buildMessage(Locale locale, Localizable pattern, Object... arguments) {
      return new MessageFormat(pattern.getLocalizedString(locale), locale).format(arguments);
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
      return this.pattern != null ? buildMessage(locale, this.pattern, this.arguments) : "";
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

   @Deprecated
   public static ArithmeticException createArithmeticException(String pattern, Object... arguments) {
      return createArithmeticException(new DummyLocalizable(pattern), arguments);
   }

   public static ArithmeticException createArithmeticException(final Localizable pattern, final Object... arguments) {
      return new ArithmeticException() {
         private static final long serialVersionUID = 5305498554076846637L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static ArrayIndexOutOfBoundsException createArrayIndexOutOfBoundsException(String pattern, Object... arguments) {
      return createArrayIndexOutOfBoundsException(new DummyLocalizable(pattern), arguments);
   }

   public static ArrayIndexOutOfBoundsException createArrayIndexOutOfBoundsException(final Localizable pattern, final Object... arguments) {
      return new ArrayIndexOutOfBoundsException() {
         private static final long serialVersionUID = 6718518191249632175L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static EOFException createEOFException(String pattern, Object... arguments) {
      return createEOFException(new DummyLocalizable(pattern), arguments);
   }

   public static EOFException createEOFException(final Localizable pattern, final Object... arguments) {
      return new EOFException() {
         private static final long serialVersionUID = 6067985859347601503L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   public static IOException createIOException(Throwable rootCause) {
      IOException ioe = new IOException(rootCause.getLocalizedMessage());
      ioe.initCause(rootCause);
      return ioe;
   }

   @Deprecated
   public static IllegalArgumentException createIllegalArgumentException(String pattern, Object... arguments) {
      return createIllegalArgumentException(new DummyLocalizable(pattern), arguments);
   }

   public static IllegalArgumentException createIllegalArgumentException(final Localizable pattern, final Object... arguments) {
      return new IllegalArgumentException() {
         private static final long serialVersionUID = -4284649691002411505L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   public static IllegalArgumentException createIllegalArgumentException(Throwable rootCause) {
      IllegalArgumentException iae = new IllegalArgumentException(rootCause.getLocalizedMessage());
      iae.initCause(rootCause);
      return iae;
   }

   @Deprecated
   public static IllegalStateException createIllegalStateException(String pattern, Object... arguments) {
      return createIllegalStateException(new DummyLocalizable(pattern), arguments);
   }

   public static IllegalStateException createIllegalStateException(final Localizable pattern, final Object... arguments) {
      return new IllegalStateException() {
         private static final long serialVersionUID = 6880901520234515725L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static ConcurrentModificationException createConcurrentModificationException(String pattern, Object... arguments) {
      return createConcurrentModificationException(new DummyLocalizable(pattern), arguments);
   }

   public static ConcurrentModificationException createConcurrentModificationException(final Localizable pattern, final Object... arguments) {
      return new ConcurrentModificationException() {
         private static final long serialVersionUID = -1878427236170442052L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static NoSuchElementException createNoSuchElementException(String pattern, Object... arguments) {
      return createNoSuchElementException(new DummyLocalizable(pattern), arguments);
   }

   public static NoSuchElementException createNoSuchElementException(final Localizable pattern, final Object... arguments) {
      return new NoSuchElementException() {
         private static final long serialVersionUID = 1632410088350355086L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static UnsupportedOperationException createUnsupportedOperationException(final Localizable pattern, final Object... arguments) {
      return new UnsupportedOperationException() {
         private static final long serialVersionUID = -4284649691002411505L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static NullPointerException createNullPointerException(String pattern, Object... arguments) {
      return createNullPointerException(new DummyLocalizable(pattern), arguments);
   }

   @Deprecated
   public static NullPointerException createNullPointerException(final Localizable pattern, final Object... arguments) {
      return new NullPointerException() {
         private static final long serialVersionUID = 451965530686593945L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   @Deprecated
   public static ParseException createParseException(int offset, String pattern, Object... arguments) {
      return createParseException(offset, new DummyLocalizable(pattern), arguments);
   }

   public static ParseException createParseException(int offset, final Localizable pattern, final Object... arguments) {
      return new ParseException(null, offset) {
         private static final long serialVersionUID = 8153587599409010120L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, pattern, arguments);
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), pattern, arguments);
         }
      };
   }

   public static RuntimeException createInternalError(Throwable cause) {
      String argument = "https://issues.apache.org/jira/browse/MATH";
      return new RuntimeException(cause) {
         private static final long serialVersionUID = -201865440834027016L;

         @Override
         public String getMessage() {
            return MathRuntimeException.buildMessage(Locale.US, LocalizedFormats.INTERNAL_ERROR, "https://issues.apache.org/jira/browse/MATH");
         }

         @Override
         public String getLocalizedMessage() {
            return MathRuntimeException.buildMessage(Locale.getDefault(), LocalizedFormats.INTERNAL_ERROR, "https://issues.apache.org/jira/browse/MATH");
         }
      };
   }
}
