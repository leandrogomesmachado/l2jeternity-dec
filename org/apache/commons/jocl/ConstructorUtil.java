package org.apache.commons.jocl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorUtil {
   public static Constructor getConstructor(Class type, Class[] argTypes) {
      if (null != type && null != argTypes) {
         Constructor ctor = null;

         try {
            ctor = type.getConstructor(argTypes);
         } catch (Exception var8) {
            ctor = null;
         }

         if (null == ctor) {
            Constructor[] ctors = type.getConstructors();

            for(int i = 0; i < ctors.length; ++i) {
               Class[] paramtypes = ctors[i].getParameterTypes();
               if (paramtypes.length == argTypes.length) {
                  boolean canuse = true;

                  for(int j = 0; j < paramtypes.length; ++j) {
                     if (!paramtypes[j].isAssignableFrom(argTypes[j])) {
                        canuse = false;
                        break;
                     }
                  }

                  if (canuse) {
                     ctor = ctors[i];
                     break;
                  }
               }
            }
         }

         return ctor;
      } else {
         throw new NullPointerException();
      }
   }

   public static Object invokeConstructor(Class type, Class[] argTypes, Object[] argValues) throws InstantiationException, IllegalAccessException, InvocationTargetException {
      return getConstructor(type, argTypes).newInstance(argValues);
   }
}
