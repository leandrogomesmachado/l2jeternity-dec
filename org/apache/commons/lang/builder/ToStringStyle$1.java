package org.apache.commons.lang.builder;

import java.util.HashSet;

class ToStringStyle$1 extends ThreadLocal {
   protected Object initialValue() {
      return new HashSet();
   }
}
