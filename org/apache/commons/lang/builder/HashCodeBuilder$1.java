package org.apache.commons.lang.builder;

import java.util.HashSet;

class HashCodeBuilder$1 extends ThreadLocal {
   protected Object initialValue() {
      return new HashSet();
   }
}
