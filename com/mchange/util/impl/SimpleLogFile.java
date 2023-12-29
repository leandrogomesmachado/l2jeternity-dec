package com.mchange.util.impl;

import com.mchange.util.MessageLogger;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class SimpleLogFile implements MessageLogger {
   PrintWriter logWriter;
   DateFormat df = DateFormat.getDateTimeInstance(3, 3);

   public SimpleLogFile(File var1, String var2) throws UnsupportedEncodingException, IOException {
      this.logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(var1.getAbsolutePath(), true), var2)), true);
   }

   public SimpleLogFile(File var1) throws IOException {
      this.logWriter = new PrintWriter(new BufferedOutputStream(new FileOutputStream(var1.getAbsolutePath(), true)), true);
   }

   @Override
   public synchronized void log(String var1) throws IOException {
      this.logMessage(var1);
      this.flush();
   }

   @Override
   public synchronized void log(Throwable var1, String var2) throws IOException {
      this.logMessage(var2);
      var1.printStackTrace(this.logWriter);
      this.flush();
   }

   private void logMessage(String var1) {
      this.logWriter.println(this.df.format(new Date()) + " -- " + var1);
   }

   private void flush() {
      this.logWriter.flush();
   }

   public synchronized void close() {
      this.logWriter.close();
   }

   @Override
   public void finalize() {
      this.close();
   }
}
