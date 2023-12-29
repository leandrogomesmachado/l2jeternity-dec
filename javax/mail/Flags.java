package javax.mail;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

public class Flags implements Cloneable, Serializable {
   private int system_flags = 0;
   private Hashtable user_flags = null;
   private static final int ANSWERED_BIT = 1;
   private static final int DELETED_BIT = 2;
   private static final int DRAFT_BIT = 4;
   private static final int FLAGGED_BIT = 8;
   private static final int RECENT_BIT = 16;
   private static final int SEEN_BIT = 32;
   private static final int USER_BIT = Integer.MIN_VALUE;
   private static final long serialVersionUID = 6243590407214169028L;

   public Flags() {
   }

   public Flags(Flags flags) {
      this.system_flags = flags.system_flags;
      if (flags.user_flags != null) {
         this.user_flags = (Hashtable)flags.user_flags.clone();
      }
   }

   public Flags(Flags.Flag flag) {
      this.system_flags |= flag.bit;
   }

   public Flags(String flag) {
      this.user_flags = new Hashtable(1);
      this.user_flags.put(flag.toLowerCase(Locale.ENGLISH), flag);
   }

   public void add(Flags.Flag flag) {
      this.system_flags |= flag.bit;
   }

   public void add(String flag) {
      if (this.user_flags == null) {
         this.user_flags = new Hashtable(1);
      }

      this.user_flags.put(flag.toLowerCase(Locale.ENGLISH), flag);
   }

   public void add(Flags f) {
      this.system_flags |= f.system_flags;
      if (f.user_flags != null) {
         if (this.user_flags == null) {
            this.user_flags = new Hashtable(1);
         }

         Enumeration e = f.user_flags.keys();

         while(e.hasMoreElements()) {
            String s = (String)e.nextElement();
            this.user_flags.put(s, f.user_flags.get(s));
         }
      }
   }

   public void remove(Flags.Flag flag) {
      this.system_flags &= ~flag.bit;
   }

   public void remove(String flag) {
      if (this.user_flags != null) {
         this.user_flags.remove(flag.toLowerCase(Locale.ENGLISH));
      }
   }

   public void remove(Flags f) {
      this.system_flags &= ~f.system_flags;
      if (f.user_flags != null) {
         if (this.user_flags == null) {
            return;
         }

         Enumeration e = f.user_flags.keys();

         while(e.hasMoreElements()) {
            this.user_flags.remove(e.nextElement());
         }
      }
   }

   public boolean contains(Flags.Flag flag) {
      return (this.system_flags & flag.bit) != 0;
   }

   public boolean contains(String flag) {
      return this.user_flags == null ? false : this.user_flags.containsKey(flag.toLowerCase(Locale.ENGLISH));
   }

   public boolean contains(Flags f) {
      if ((f.system_flags & this.system_flags) != f.system_flags) {
         return false;
      } else {
         if (f.user_flags != null) {
            if (this.user_flags == null) {
               return false;
            }

            Enumeration e = f.user_flags.keys();

            while(e.hasMoreElements()) {
               if (!this.user_flags.containsKey(e.nextElement())) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Flags)) {
         return false;
      } else {
         Flags f = (Flags)obj;
         if (f.system_flags != this.system_flags) {
            return false;
         } else if (f.user_flags == null && this.user_flags == null) {
            return true;
         } else if (f.user_flags != null && this.user_flags != null && f.user_flags.size() == this.user_flags.size()) {
            Enumeration e = f.user_flags.keys();

            while(e.hasMoreElements()) {
               if (!this.user_flags.containsKey(e.nextElement())) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int hash = this.system_flags;
      if (this.user_flags != null) {
         Enumeration e = this.user_flags.keys();

         while(e.hasMoreElements()) {
            hash += ((String)e.nextElement()).hashCode();
         }
      }

      return hash;
   }

   public Flags.Flag[] getSystemFlags() {
      Vector v = new Vector();
      if ((this.system_flags & 1) != 0) {
         v.addElement(Flags.Flag.ANSWERED);
      }

      if ((this.system_flags & 2) != 0) {
         v.addElement(Flags.Flag.DELETED);
      }

      if ((this.system_flags & 4) != 0) {
         v.addElement(Flags.Flag.DRAFT);
      }

      if ((this.system_flags & 8) != 0) {
         v.addElement(Flags.Flag.FLAGGED);
      }

      if ((this.system_flags & 16) != 0) {
         v.addElement(Flags.Flag.RECENT);
      }

      if ((this.system_flags & 32) != 0) {
         v.addElement(Flags.Flag.SEEN);
      }

      if ((this.system_flags & -2147483648) != 0) {
         v.addElement(Flags.Flag.USER);
      }

      Flags.Flag[] f = new Flags.Flag[v.size()];
      v.copyInto(f);
      return f;
   }

   public String[] getUserFlags() {
      Vector v = new Vector();
      if (this.user_flags != null) {
         Enumeration e = this.user_flags.elements();

         while(e.hasMoreElements()) {
            v.addElement(e.nextElement());
         }
      }

      String[] f = new String[v.size()];
      v.copyInto(f);
      return f;
   }

   @Override
   public Object clone() {
      Flags f = null;

      try {
         f = (Flags)super.clone();
      } catch (CloneNotSupportedException var3) {
      }

      if (this.user_flags != null) {
         f.user_flags = (Hashtable)this.user_flags.clone();
      }

      return f;
   }

   public static final class Flag {
      public static final Flags.Flag ANSWERED = new Flags.Flag(1);
      public static final Flags.Flag DELETED = new Flags.Flag(2);
      public static final Flags.Flag DRAFT = new Flags.Flag(4);
      public static final Flags.Flag FLAGGED = new Flags.Flag(8);
      public static final Flags.Flag RECENT = new Flags.Flag(16);
      public static final Flags.Flag SEEN = new Flags.Flag(32);
      public static final Flags.Flag USER = new Flags.Flag(Integer.MIN_VALUE);
      private int bit;

      private Flag(int bit) {
         this.bit = bit;
      }
   }
}
