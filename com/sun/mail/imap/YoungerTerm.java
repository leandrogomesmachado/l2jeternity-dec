package com.sun.mail.imap;

import java.util.Date;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

public final class YoungerTerm extends SearchTerm {
   private int interval;
   private static final long serialVersionUID = 1592714210688163496L;

   public YoungerTerm(int interval) {
      this.interval = interval;
   }

   public int getInterval() {
      return this.interval;
   }

   @Override
   public boolean match(Message msg) {
      Date d;
      try {
         d = msg.getReceivedDate();
      } catch (Exception var4) {
         return false;
      }

      if (d == null) {
         return false;
      } else {
         return d.getTime() >= System.currentTimeMillis() - (long)this.interval * 1000L;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof YoungerTerm)) {
         return false;
      } else {
         return this.interval == ((YoungerTerm)obj).interval;
      }
   }

   @Override
   public int hashCode() {
      return this.interval;
   }
}
