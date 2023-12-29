package javax.mail;

import java.util.Vector;

public class FetchProfile {
   private Vector specials = null;
   private Vector headers = null;

   public void add(FetchProfile.Item item) {
      if (this.specials == null) {
         this.specials = new Vector();
      }

      this.specials.addElement(item);
   }

   public void add(String headerName) {
      if (this.headers == null) {
         this.headers = new Vector();
      }

      this.headers.addElement(headerName);
   }

   public boolean contains(FetchProfile.Item item) {
      return this.specials != null && this.specials.contains(item);
   }

   public boolean contains(String headerName) {
      return this.headers != null && this.headers.contains(headerName);
   }

   public FetchProfile.Item[] getItems() {
      if (this.specials == null) {
         return new FetchProfile.Item[0];
      } else {
         FetchProfile.Item[] s = new FetchProfile.Item[this.specials.size()];
         this.specials.copyInto(s);
         return s;
      }
   }

   public String[] getHeaderNames() {
      if (this.headers == null) {
         return new String[0];
      } else {
         String[] s = new String[this.headers.size()];
         this.headers.copyInto(s);
         return s;
      }
   }

   public static class Item {
      public static final FetchProfile.Item ENVELOPE = new FetchProfile.Item("ENVELOPE");
      public static final FetchProfile.Item CONTENT_INFO = new FetchProfile.Item("CONTENT_INFO");
      public static final FetchProfile.Item SIZE = new FetchProfile.Item("SIZE");
      public static final FetchProfile.Item FLAGS = new FetchProfile.Item("FLAGS");
      private String name;

      protected Item(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.getClass().getName() + "[" + this.name + "]";
      }
   }
}
