package com.mchange.v2.io;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class IndentedWriter extends FilterWriter {
   static final String EOL;
   int indent_level = 0;
   boolean at_line_start = true;

   public IndentedWriter(Writer var1) {
      super(var1);
   }

   private boolean isEol(char var1) {
      return var1 == '\r' || var1 == '\n';
   }

   public void upIndent() {
      ++this.indent_level;
   }

   public void downIndent() {
      --this.indent_level;
   }

   @Override
   public void write(int var1) throws IOException {
      this.out.write(var1);
      this.at_line_start = this.isEol((char)var1);
   }

   @Override
   public void write(char[] var1, int var2, int var3) throws IOException {
      this.out.write(var1, var2, var3);
      this.at_line_start = this.isEol(var1[var2 + var3 - 1]);
   }

   @Override
   public void write(String var1, int var2, int var3) throws IOException {
      if (var3 > 0) {
         this.out.write(var1, var2, var3);
         this.at_line_start = this.isEol(var1.charAt(var2 + var3 - 1));
      }
   }

   private void printIndent() throws IOException {
      for(int var1 = 0; var1 < this.indent_level; ++var1) {
         this.out.write(9);
      }
   }

   public void print(String var1) throws IOException {
      if (this.at_line_start) {
         this.printIndent();
      }

      this.out.write(var1);
      char var2 = var1.charAt(var1.length() - 1);
      this.at_line_start = this.isEol(var2);
   }

   public void println(String var1) throws IOException {
      if (this.at_line_start) {
         this.printIndent();
      }

      this.out.write(var1);
      this.out.write(EOL);
      this.at_line_start = true;
   }

   public void print(boolean var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(byte var1) throws IOException {
      this.print(String.valueOf((int)var1));
   }

   public void print(char var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(short var1) throws IOException {
      this.print(String.valueOf((int)var1));
   }

   public void print(int var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(long var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(float var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(double var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void print(Object var1) throws IOException {
      this.print(String.valueOf(var1));
   }

   public void println(boolean var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(byte var1) throws IOException {
      this.println(String.valueOf((int)var1));
   }

   public void println(char var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(short var1) throws IOException {
      this.println(String.valueOf((int)var1));
   }

   public void println(int var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(long var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(float var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(double var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println(Object var1) throws IOException {
      this.println(String.valueOf(var1));
   }

   public void println() throws IOException {
      this.println("");
   }

   static {
      String var0 = System.getProperty("line.separator");
      EOL = var0 != null ? var0 : "\r\n";
   }
}
