package org.json.zip;

public abstract class JSONzip implements None, PostMortem {
   public static final byte[] bcd = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 46, 45, 43, 69};
   public static final int end = 256;
   public static final int endOfNumber = bcd.length;
   public static final long int4 = 16L;
   public static final long int7 = 144L;
   public static final long int14 = 16528L;
   public static final boolean probe = false;
   public static final int zipEmptyObject = 0;
   public static final int zipEmptyArray = 1;
   public static final int zipTrue = 2;
   public static final int zipFalse = 3;
   public static final int zipNull = 4;
   public static final int zipObject = 5;
   public static final int zipArrayString = 6;
   public static final int zipArrayValue = 7;
   protected final Huff namehuff = new Huff(257);
   protected final Huff namehuffext = new Huff(257);
   protected final Keep namekeep = new Keep(9);
   protected final Huff stringhuff = new Huff(257);
   protected final Huff stringhuffext = new Huff(257);
   protected final Keep stringkeep = new Keep(11);
   protected final Keep valuekeep = new Keep(10);

   protected JSONzip() {
   }

   protected void generate() {
      this.namehuff.generate();
      this.namehuffext.generate();
      this.stringhuff.generate();
      this.stringhuffext.generate();
   }

   static void log() {
      log("\n");
   }

   static void log(int integer) {
      log(integer + " ");
   }

   static void log(int integer, int width) {
      if (width == 1) {
         log(integer);
      } else {
         log(integer + ":" + width + " ");
      }
   }

   static void log(String string) {
      System.out.print(string);
   }

   static void logchar(int integer, int width) {
      if (integer > 32 && integer <= 125) {
         log("'" + (char)integer + "':" + width + " ");
      } else {
         log(integer, width);
      }
   }

   @Override
   public boolean postMortem(PostMortem pm) {
      JSONzip that = (JSONzip)pm;
      return this.namehuff.postMortem(that.namehuff)
         && this.namekeep.postMortem(that.namekeep)
         && this.stringkeep.postMortem(that.stringkeep)
         && this.stringhuff.postMortem(that.stringhuff)
         && this.valuekeep.postMortem(that.valuekeep);
   }
}
