package com.mchange.util.impl;

import com.mchange.io.InputStreamUtils;
import com.mchange.io.OutputStreamUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;

public class SyncedProperties {
   private static final String[] SA_TEMPLATE = new String[0];
   private static final byte H_START_BYTE = 35;
   private static final byte[] H_LF_BYTES;
   private static final String ASCII = "8859_1";
   Properties props;
   byte[] headerBytes;
   File file;
   long last_mod = -1L;

   public SyncedProperties(File var1, String var2) throws IOException {
      this(var1, makeHeaderBytes(var2));
   }

   public SyncedProperties(File var1, String[] var2) throws IOException {
      this(var1, makeHeaderBytes(var2));
   }

   public SyncedProperties(File var1) throws IOException {
      this(var1, (byte[])null);
   }

   private SyncedProperties(File var1, byte[] var2) throws IOException {
      if (var1.exists()) {
         if (!var1.isFile()) {
            throw new IOException(var1.getPath() + ": Properties file can't be a directory or special file!");
         }

         if (var2 == null) {
            BufferedReader var3 = null;

            try {
               var3 = new BufferedReader(new InputStreamReader(new FileInputStream(var1)));
               LinkedList var4 = new LinkedList();
               String var5 = var3.readLine();

               while(var5.trim().equals("")) {
                  var5 = var3.readLine();
               }

               while(var5.charAt(0) == '#') {
                  var4.add(var5.substring(1).trim());
               }

               var2 = makeHeaderBytes(var4.toArray(SA_TEMPLATE));
            } finally {
               if (var3 != null) {
                  var3.close();
               }
            }
         }
      }

      if (!var1.canWrite()) {
         throw new IOException("Can't write to file " + var1.getPath());
      } else {
         this.props = new Properties();
         this.headerBytes = var2;
         this.file = var1;
         this.ensureUpToDate();
      }
   }

   public synchronized String getProperty(String var1) throws IOException {
      this.ensureUpToDate();
      return this.props.getProperty(var1);
   }

   public synchronized String getProperty(String var1, String var2) throws IOException {
      String var3 = this.props.getProperty(var1);
      return var3 == null ? var2 : var3;
   }

   public synchronized void put(String var1, String var2) throws IOException {
      this.ensureUpToDate();
      this.props.put(var1, var2);
      this.rewritePropsFile();
   }

   public synchronized void remove(String var1) throws IOException {
      this.ensureUpToDate();
      this.props.remove(var1);
      this.rewritePropsFile();
   }

   public synchronized void clear() throws IOException {
      this.ensureUpToDate();
      this.props.clear();
      this.rewritePropsFile();
   }

   public synchronized boolean contains(String var1) throws IOException {
      this.ensureUpToDate();
      return this.props.contains(var1);
   }

   public synchronized boolean containsKey(String var1) throws IOException {
      this.ensureUpToDate();
      return this.props.containsKey(var1);
   }

   public synchronized Enumeration elements() throws IOException {
      this.ensureUpToDate();
      return this.props.elements();
   }

   public synchronized Enumeration keys() throws IOException {
      this.ensureUpToDate();
      return this.props.keys();
   }

   public synchronized int size() throws IOException {
      this.ensureUpToDate();
      return this.props.size();
   }

   public synchronized boolean isEmpty() throws IOException {
      this.ensureUpToDate();
      return this.props.isEmpty();
   }

   private synchronized void ensureUpToDate() throws IOException {
      long var1 = this.file.lastModified();
      if (var1 > this.last_mod) {
         BufferedInputStream var3 = null;

         try {
            var3 = new BufferedInputStream(new FileInputStream(this.file));
            this.props.clear();
            this.props.load(var3);
            this.last_mod = var1;
         } finally {
            InputStreamUtils.attemptClose(var3);
         }
      }
   }

   private synchronized void rewritePropsFile() throws IOException {
      BufferedOutputStream var1 = null;

      try {
         var1 = new BufferedOutputStream(new FileOutputStream(this.file));
         if (this.headerBytes != null) {
            var1.write(this.headerBytes);
         }

         this.props.store(var1, null);
         var1.flush();
         this.last_mod = this.file.lastModified();
      } finally {
         OutputStreamUtils.attemptClose(var1);
      }
   }

   private static byte[] makeHeaderBytes(String[] var0) {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         int var2 = 0;

         for(int var3 = var0.length; var2 < var3; ++var2) {
            var1.write(35);
            var1.write(var0[var2].getBytes());
            var1.write(H_LF_BYTES);
         }

         return var1.toByteArray();
      } catch (IOException var4) {
         throw new InternalError("IOException working with ByteArrayOutputStream?!?");
      }
   }

   private static byte[] makeHeaderBytes(String var0) {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         var1.write(35);
         var1.write(var0.getBytes());
         var1.write(H_LF_BYTES);
         return var1.toByteArray();
      } catch (IOException var2) {
         throw new InternalError("IOException working with ByteArrayOutputStream?!?");
      }
   }

   static {
      try {
         H_LF_BYTES = System.getProperty("line.separator", "\r\n").getBytes("8859_1");
      } catch (UnsupportedEncodingException var1) {
         throw new InternalError("Encoding 8859_1 not supported ?!?");
      }
   }
}
