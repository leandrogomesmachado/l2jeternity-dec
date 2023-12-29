package com.mchange.util.impl;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class QuotesAndWhitespaceTokenizer extends StringEnumerationHelperBase {
   Object current;
   LinkedList list = new LinkedList();

   public QuotesAndWhitespaceTokenizer(String var1) throws IllegalArgumentException {
      int var2 = 0;

      int var6;
      for(int var3 = var1.length(); var2 < var3; var2 = var6 + 1) {
         int var4 = var1.indexOf(34, var2);
         if (var4 < 0) {
            StringTokenizer var7 = new StringTokenizer(var1.substring(var2));
            if (var7.hasMoreTokens()) {
               this.list.add(var7);
            }
            break;
         }

         StringTokenizer var5 = new StringTokenizer(var1.substring(var2, var4));
         if (var5.hasMoreTokens()) {
            this.list.add(var5);
         }

         var6 = var1.indexOf(34, var4 + 1);
         if (var6 == -1) {
            throw new IllegalArgumentException("Badly quoted string: " + var1);
         }

         this.list.add(var1.substring(var4 + 1, var6));
      }

      this.advance();
   }

   @Override
   public synchronized boolean hasMoreStrings() {
      return this.current != null;
   }

   @Override
   public synchronized String nextString() {
      if (this.current instanceof String) {
         String var3 = (String)this.current;
         this.advance();
         return var3;
      } else {
         StringTokenizer var1 = (StringTokenizer)this.current;
         String var2 = var1.nextToken();
         if (!var1.hasMoreTokens()) {
            this.advance();
         }

         return var2;
      }
   }

   private void advance() {
      if (this.list.isEmpty()) {
         this.current = null;
      } else {
         this.current = this.list.getFirst();
         this.list.removeFirst();
      }
   }

   public static void main(String[] var0) {
      String var1 = "\t  \n\r";
      QuotesAndWhitespaceTokenizer var2 = new QuotesAndWhitespaceTokenizer(var1);

      while(var2.hasMoreStrings()) {
         System.out.println(var2.nextString());
      }
   }
}
