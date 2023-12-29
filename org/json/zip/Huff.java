package org.json.zip;

import org.json.JSONException;

public class Huff implements None, PostMortem {
   private final int domain;
   public static final int education = 1000000;
   private final Huff.Symbol[] symbols;
   private Huff.Symbol table;
   private int toLearn;
   private boolean upToDate = false;
   private int width;

   public Huff(int domain) {
      this.domain = domain;
      this.toLearn = 1000000;
      int length = domain * 2 - 1;
      this.symbols = new Huff.Symbol[length];

      for(int i = 0; i < domain; ++i) {
         this.symbols[i] = new Huff.Symbol(i);
      }

      for(int i = domain; i < length; ++i) {
         this.symbols[i] = new Huff.Symbol(-1);
      }
   }

   public void generate() {
      if (!this.upToDate) {
         Huff.Symbol head = this.symbols[0];
         Huff.Symbol previous = head;
         this.table = null;
         head.next = null;

         for(int i = 1; i < this.domain; ++i) {
            Huff.Symbol symbol = this.symbols[i];
            if (symbol.weight < head.weight) {
               symbol.next = head;
               head = symbol;
            } else {
               if (symbol.weight < previous.weight) {
                  previous = head;
               }

               while(true) {
                  Huff.Symbol next = previous.next;
                  if (next == null || symbol.weight < next.weight) {
                     symbol.next = next;
                     previous.next = symbol;
                     previous = symbol;
                     break;
                  }

                  previous = next;
               }
            }
         }

         int avail = this.domain;
         previous = head;

         while(true) {
            Huff.Symbol first = head;
            Huff.Symbol second = head.next;
            head = second.next;
            Huff.Symbol symbol = this.symbols[avail];
            ++avail;
            symbol.weight = first.weight + second.weight;
            symbol.zero = first;
            symbol.one = second;
            symbol.back = null;
            first.back = symbol;
            second.back = symbol;
            if (head == null) {
               this.table = symbol;
               this.upToDate = true;
               break;
            }

            if (symbol.weight < head.weight) {
               symbol.next = head;
               head = symbol;
               previous = symbol;
            } else {
               while(true) {
                  Huff.Symbol next = previous.next;
                  if (next == null || symbol.weight < next.weight) {
                     symbol.next = next;
                     previous.next = symbol;
                     previous = symbol;
                     break;
                  }

                  previous = next;
               }
            }
         }
      }
   }

   private boolean postMortem(int integer) {
      int[] bits = new int[this.domain];
      Huff.Symbol symbol = this.symbols[integer];
      if (symbol.integer != integer) {
         return false;
      } else {
         int i = 0;

         while(true) {
            Huff.Symbol back = symbol.back;
            if (back == null) {
               if (symbol != this.table) {
                  return false;
               } else {
                  this.width = 0;

                  for(symbol = this.table; symbol.integer == -1; symbol = bits[i] != 0 ? symbol.one : symbol.zero) {
                     --i;
                  }

                  return symbol.integer == integer && i == 0;
               }
            }

            if (back.zero == symbol) {
               bits[i] = 0;
            } else {
               if (back.one != symbol) {
                  return false;
               }

               bits[i] = 1;
            }

            ++i;
            symbol = back;
         }
      }
   }

   @Override
   public boolean postMortem(PostMortem pm) {
      for(int integer = 0; integer < this.domain; ++integer) {
         if (!this.postMortem(integer)) {
            JSONzip.log("\nBad huff ");
            JSONzip.logchar(integer, integer);
            return false;
         }
      }

      return this.table.postMortem(((Huff)pm).table);
   }

   public int read(BitReader bitreader) throws JSONException {
      try {
         this.width = 0;

         Huff.Symbol symbol;
         for(symbol = this.table; symbol.integer == -1; symbol = bitreader.bit() ? symbol.one : symbol.zero) {
            ++this.width;
         }

         this.tick(symbol.integer);
         return symbol.integer;
      } catch (Throwable var3) {
         throw new JSONException(var3);
      }
   }

   public void tick(int value) {
      if (this.toLearn > 0) {
         --this.toLearn;
         ++this.symbols[value].weight;
         this.upToDate = false;
      }
   }

   private void write(Huff.Symbol symbol, BitWriter bitwriter) throws JSONException {
      try {
         Huff.Symbol back = symbol.back;
         if (back != null) {
            ++this.width;
            this.write(back, bitwriter);
            if (back.zero == symbol) {
               bitwriter.zero();
            } else {
               bitwriter.one();
            }
         }
      } catch (Throwable var4) {
         throw new JSONException(var4);
      }
   }

   public void write(int value, BitWriter bitwriter) throws JSONException {
      this.width = 0;
      this.write(this.symbols[value], bitwriter);
      this.tick(value);
   }

   private static class Symbol implements PostMortem {
      public Huff.Symbol back;
      public Huff.Symbol next;
      public Huff.Symbol zero;
      public Huff.Symbol one;
      public final int integer;
      public long weight;

      public Symbol(int integer) {
         this.integer = integer;
         this.weight = 0L;
         this.next = null;
         this.back = null;
         this.one = null;
         this.zero = null;
      }

      @Override
      public boolean postMortem(PostMortem pm) {
         boolean result = true;
         Huff.Symbol that = (Huff.Symbol)pm;
         if (this.integer == that.integer && this.weight == that.weight) {
            if (this.back == null != (that.back == null)) {
               return false;
            } else {
               Huff.Symbol zero = this.zero;
               Huff.Symbol one = this.one;
               if (zero == null) {
                  if (that.zero != null) {
                     return false;
                  }
               } else {
                  result = zero.postMortem(that.zero);
               }

               if (one == null) {
                  if (that.one != null) {
                     return false;
                  }
               } else {
                  result = one.postMortem(that.one);
               }

               return result;
            }
         } else {
            return false;
         }
      }
   }
}
