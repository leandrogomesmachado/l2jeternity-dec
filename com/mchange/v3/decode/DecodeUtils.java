package com.mchange.v3.decode;

import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class DecodeUtils {
   public static final String DECODER_CLASS_DOT_KEY = ".decoderClass";
   public static final String DECODER_CLASS_NO_DOT_KEY = "decoderClass";
   private static final Object[] DECODER_CLASS_DOT_KEY_OBJ_ARRAY = new Object[]{".decoderClass"};
   private static final Object[] DECODER_CLASS_NO_DOT_KEY_OBJ_ARRAY = new Object[]{"decoderClass"};
   private static final MLogger logger = MLog.getLogger(DecodeUtils.class);
   private static final List<DecoderFinder> finders;
   private static final String[] finderClassNames = new String[]{"com.mchange.sc.v1.decode.ScalaMapDecoderFinder"};

   static final String findDecoderClassName(Object var0) throws CannotDecodeException {
      for(DecoderFinder var2 : finders) {
         String var3 = var2.decoderClassName(var0);
         if (var3 != null) {
            return var3;
         }
      }

      throw new CannotDecodeException("Could not find a decoder class name for object: " + var0);
   }

   public static Object decode(String var0, Object var1) throws CannotDecodeException {
      try {
         Class var2 = Class.forName(var0);
         Decoder var3 = (Decoder)var2.newInstance();
         return var3.decode(var1);
      } catch (Exception var4) {
         throw new CannotDecodeException("An exception occurred while attempting to decode " + var1, var4);
      }
   }

   public static Object decode(Object var0) throws CannotDecodeException {
      return decode(findDecoderClassName(var0), var0);
   }

   private DecodeUtils() {
   }

   static {
      LinkedList var0 = new LinkedList();
      var0.add(new DecodeUtils.JavaMapDecoderFinder());
      int var1 = 0;

      for(int var2 = finderClassNames.length; var1 < var2; ++var1) {
         try {
            var0.add((DecoderFinder)Class.forName(finderClassNames[var1]).newInstance());
         } catch (Exception var4) {
            if (logger.isLoggable(MLevel.INFO)) {
               logger.log(MLevel.INFO, "Could not load DecoderFinder '" + finderClassNames[var1] + "'", (Throwable)var4);
            }
         }
      }

      finders = Collections.unmodifiableList(var0);
   }

   static class JavaMapDecoderFinder implements DecoderFinder {
      @Override
      public String decoderClassName(Object var1) throws CannotDecodeException {
         if (var1 instanceof Map) {
            String var2 = null;
            Map var3 = (Map)var1;
            var2 = (String)var3.get(".decoderClass");
            if (var2 == null) {
               var2 = (String)var3.get("decoderClass");
            }

            if (var2 == null) {
               throw new CannotDecodeException("Could not find the decoder class for java.util.Map: " + var1);
            } else {
               return var2;
            }
         } else {
            return null;
         }
      }
   }
}
