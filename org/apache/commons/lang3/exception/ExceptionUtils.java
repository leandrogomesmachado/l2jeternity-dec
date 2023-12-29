package org.apache.commons.lang3.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class ExceptionUtils {
   static final String WRAPPED_MARKER = " [wrapped] ";
   private static final String[] CAUSE_METHOD_NAMES = new String[]{
      "getCause",
      "getNextException",
      "getTargetException",
      "getException",
      "getSourceException",
      "getRootCause",
      "getCausedByException",
      "getNested",
      "getLinkedException",
      "getNestedException",
      "getLinkedCause",
      "getThrowable"
   };

   @Deprecated
   public static String[] getDefaultCauseMethodNames() {
      return ArrayUtils.clone(CAUSE_METHOD_NAMES);
   }

   @Deprecated
   public static Throwable getCause(Throwable throwable) {
      return getCause(throwable, null);
   }

   @Deprecated
   public static Throwable getCause(Throwable throwable, String[] methodNames) {
      if (throwable == null) {
         return null;
      } else {
         if (methodNames == null) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
               return cause;
            }

            methodNames = CAUSE_METHOD_NAMES;
         }

         for(String methodName : methodNames) {
            if (methodName != null) {
               Throwable legacyCause = getCauseUsingMethodName(throwable, methodName);
               if (legacyCause != null) {
                  return legacyCause;
               }
            }
         }

         return null;
      }
   }

   public static Throwable getRootCause(Throwable throwable) {
      List<Throwable> list = getThrowableList(throwable);
      return list.size() < 2 ? null : list.get(list.size() - 1);
   }

   private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
      Method method = null;

      try {
         method = throwable.getClass().getMethod(methodName);
      } catch (SecurityException | NoSuchMethodException var4) {
      }

      if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
         try {
            return (Throwable)method.invoke(throwable);
         } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException var5) {
         }
      }

      return null;
   }

   public static int getThrowableCount(Throwable throwable) {
      return getThrowableList(throwable).size();
   }

   public static Throwable[] getThrowables(Throwable throwable) {
      List<Throwable> list = getThrowableList(throwable);
      return list.toArray(new Throwable[list.size()]);
   }

   public static List<Throwable> getThrowableList(Throwable throwable) {
      List<Throwable> list;
      for(list = new ArrayList<>(); throwable != null && !list.contains(throwable); throwable = throwable.getCause()) {
         list.add(throwable);
      }

      return list;
   }

   public static int indexOfThrowable(Throwable throwable, Class<?> clazz) {
      return indexOf(throwable, clazz, 0, false);
   }

   public static int indexOfThrowable(Throwable throwable, Class<?> clazz, int fromIndex) {
      return indexOf(throwable, clazz, fromIndex, false);
   }

   public static int indexOfType(Throwable throwable, Class<?> type) {
      return indexOf(throwable, type, 0, true);
   }

   public static int indexOfType(Throwable throwable, Class<?> type, int fromIndex) {
      return indexOf(throwable, type, fromIndex, true);
   }

   private static int indexOf(Throwable throwable, Class<?> type, int fromIndex, boolean subclass) {
      if (throwable != null && type != null) {
         if (fromIndex < 0) {
            fromIndex = 0;
         }

         Throwable[] throwables = getThrowables(throwable);
         if (fromIndex >= throwables.length) {
            return -1;
         } else {
            if (subclass) {
               for(int i = fromIndex; i < throwables.length; ++i) {
                  if (type.isAssignableFrom(throwables[i].getClass())) {
                     return i;
                  }
               }
            } else {
               for(int i = fromIndex; i < throwables.length; ++i) {
                  if (type.equals(throwables[i].getClass())) {
                     return i;
                  }
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   public static void printRootCauseStackTrace(Throwable throwable) {
      printRootCauseStackTrace(throwable, System.err);
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintStream stream) {
      if (throwable != null) {
         Validate.isTrue(stream != null, "The PrintStream must not be null");
         String[] trace = getRootCauseStackTrace(throwable);

         for(String element : trace) {
            stream.println(element);
         }

         stream.flush();
      }
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintWriter writer) {
      if (throwable != null) {
         Validate.isTrue(writer != null, "The PrintWriter must not be null");
         String[] trace = getRootCauseStackTrace(throwable);

         for(String element : trace) {
            writer.println(element);
         }

         writer.flush();
      }
   }

   public static String[] getRootCauseStackTrace(Throwable throwable) {
      if (throwable == null) {
         return ArrayUtils.EMPTY_STRING_ARRAY;
      } else {
         Throwable[] throwables = getThrowables(throwable);
         int count = throwables.length;
         List<String> frames = new ArrayList<>();
         List<String> nextTrace = getStackFrameList(throwables[count - 1]);

         List<String> trace;
         for(int i = count; --i >= 0; frames.addAll(trace)) {
            trace = nextTrace;
            if (i != 0) {
               nextTrace = getStackFrameList(throwables[i - 1]);
               removeCommonFrames(trace, nextTrace);
            }

            if (i == count - 1) {
               frames.add(throwables[i].toString());
            } else {
               frames.add(" [wrapped] " + throwables[i].toString());
            }
         }

         return frames.toArray(new String[frames.size()]);
      }
   }

   public static void removeCommonFrames(List<String> causeFrames, List<String> wrapperFrames) {
      if (causeFrames != null && wrapperFrames != null) {
         int causeFrameIndex = causeFrames.size() - 1;

         for(int wrapperFrameIndex = wrapperFrames.size() - 1; causeFrameIndex >= 0 && wrapperFrameIndex >= 0; --wrapperFrameIndex) {
            String causeFrame = causeFrames.get(causeFrameIndex);
            String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
               causeFrames.remove(causeFrameIndex);
            }

            --causeFrameIndex;
         }
      } else {
         throw new IllegalArgumentException("The List must not be null");
      }
   }

   public static String getStackTrace(Throwable throwable) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      throwable.printStackTrace(pw);
      return sw.getBuffer().toString();
   }

   public static String[] getStackFrames(Throwable throwable) {
      return throwable == null ? ArrayUtils.EMPTY_STRING_ARRAY : getStackFrames(getStackTrace(throwable));
   }

   static String[] getStackFrames(String stackTrace) {
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      List<String> list = new ArrayList<>();

      while(frames.hasMoreTokens()) {
         list.add(frames.nextToken());
      }

      return list.toArray(new String[list.size()]);
   }

   static List<String> getStackFrameList(Throwable t) {
      String stackTrace = getStackTrace(t);
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      List<String> list = new ArrayList<>();
      boolean traceStarted = false;

      while(frames.hasMoreTokens()) {
         String token = frames.nextToken();
         int at = token.indexOf("at");
         if (at != -1 && token.substring(0, at).trim().isEmpty()) {
            traceStarted = true;
            list.add(token);
         } else if (traceStarted) {
            break;
         }
      }

      return list;
   }

   public static String getMessage(Throwable th) {
      if (th == null) {
         return "";
      } else {
         String clsName = ClassUtils.getShortClassName(th, null);
         String msg = th.getMessage();
         return clsName + ": " + StringUtils.defaultString(msg);
      }
   }

   public static String getRootCauseMessage(Throwable th) {
      Throwable root = getRootCause(th);
      root = root == null ? th : root;
      return getMessage(root);
   }

   public static <R> R rethrow(Throwable throwable) {
      return typeErasure(throwable);
   }

   private static <R, T extends Throwable> R typeErasure(Throwable throwable) throws T {
      throw throwable;
   }

   public static <R> R wrapAndThrow(Throwable throwable) {
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      } else if (throwable instanceof Error) {
         throw (Error)throwable;
      } else {
         throw new UndeclaredThrowableException(throwable);
      }
   }

   public static boolean hasCause(Throwable chain, Class<? extends Throwable> type) {
      if (chain instanceof UndeclaredThrowableException) {
         chain = chain.getCause();
      }

      return type.isInstance(chain);
   }
}
