package com.mchange.v2.beans.swing;

import java.beans.IntrospectionException;
import javax.swing.AbstractButton;

public final class BoundButtonUtils {
   public static void bindToSetProperty(AbstractButton[] var0, Object[] var1, Object var2, String var3) throws IntrospectionException {
      SetPropertyElementBoundButtonModel.bind(var0, var1, var2, var3);
   }

   public static void bindAsRadioButtonsToProperty(AbstractButton[] var0, Object[] var1, Object var2, String var3) throws IntrospectionException {
      PropertyBoundButtonGroup var4 = new PropertyBoundButtonGroup(var2, var3);

      for(int var5 = 0; var5 < var0.length; ++var5) {
         var4.add(var0[var5], var1[var5]);
      }
   }
}
