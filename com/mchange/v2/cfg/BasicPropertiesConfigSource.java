package com.mchange.v2.cfg;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

public final class BasicPropertiesConfigSource implements PropertiesConfigSource {
   @Override
   public PropertiesConfigSource.Parse propertiesFromSource(String var1) throws FileNotFoundException, Exception {
      InputStream var2 = MultiPropertiesConfig.class.getResourceAsStream(var1);
      if (var2 != null) {
         BufferedInputStream var3 = new BufferedInputStream(var2);
         Properties var4 = new Properties();
         LinkedList var5 = new LinkedList();

         try {
            var4.load(var3);
         } finally {
            try {
               if (var3 != null) {
                  var3.close();
               }
            } catch (IOException var12) {
               var5.add(
                  new DelayedLogItem(
                     DelayedLogItem.Level.WARNING, "An IOException occurred while closing InputStream from resource path '" + var1 + "'.", var12
                  )
               );
            }
         }

         return new PropertiesConfigSource.Parse(var4, var5);
      } else {
         throw new FileNotFoundException(String.format("Resource not found at path '%s'.", var1));
      }
   }
}
