package com.mchange.v2.util;

import com.mchange.v1.util.WrapperIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PatternReplacementMap {
   List mappings = new LinkedList();

   public synchronized void addMapping(Pattern var1, String var2) {
      this.mappings.add(new PatternReplacementMap.Mapping(var1, var2));
   }

   public synchronized void removeMapping(Pattern var1) {
      int var2 = 0;

      for(int var3 = this.mappings.size(); var2 < var3; ++var2) {
         if (((PatternReplacementMap.Mapping)this.mappings.get(var2)).getPattern().equals(var1)) {
            this.mappings.remove(var2);
         }
      }
   }

   public synchronized Iterator patterns() {
      return new WrapperIterator(this.mappings.iterator(), true) {
         @Override
         protected Object transformObject(Object var1) {
            PatternReplacementMap.Mapping var2 = (PatternReplacementMap.Mapping)var1;
            return var2.getPattern();
         }
      };
   }

   public synchronized int size() {
      return this.mappings.size();
   }

   public synchronized String attemptReplace(String var1) {
      String var2 = null;

      for(PatternReplacementMap.Mapping var4 : this.mappings) {
         Matcher var5 = var4.getPattern().matcher(var1);
         if (var5.matches()) {
            var2 = var5.replaceAll(var4.getReplacement());
            break;
         }
      }

      return var2;
   }

   private static final class Mapping {
      Pattern pattern;
      String replacement;

      public Pattern getPattern() {
         return this.pattern;
      }

      public String getReplacement() {
         return this.replacement;
      }

      public Mapping(Pattern var1, String var2) {
         this.pattern = var1;
         this.replacement = var2;
      }
   }
}
