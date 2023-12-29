package org.utils;

import java.util.ArrayList;
import java.util.Iterator;

public final class NetList extends ArrayList<Net> {
   private static final long serialVersionUID = 4266033257195615387L;

   public boolean matches(String address) {
      for(Net net : this) {
         if (net.matches(address)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator<Net> itr = this.iterator();

      while(itr.hasNext()) {
         sb.append(itr.next());
         if (itr.hasNext()) {
            sb.append(',');
         }
      }

      return sb.toString();
   }
}
