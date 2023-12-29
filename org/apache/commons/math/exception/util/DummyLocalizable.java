package org.apache.commons.math.exception.util;

import java.util.Locale;

public class DummyLocalizable implements Localizable {
   private static final long serialVersionUID = 8843275624471387299L;
   private final String source;

   public DummyLocalizable(String source) {
      this.source = source;
   }

   @Override
   public String getSourceString() {
      return this.source;
   }

   @Override
   public String getLocalizedString(Locale locale) {
      return this.source;
   }

   @Override
   public String toString() {
      return this.source;
   }
}
