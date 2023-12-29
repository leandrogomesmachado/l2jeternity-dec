package com.mchange.util.impl;

import com.mchange.lang.ByteUtils;
import com.mchange.util.PasswordManager;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HexAsciiMD5PropertiesPasswordManager implements PasswordManager {
   private static final String DIGEST_ALGORITHM = "MD5";
   private static final String PASSWORD_ENCODING = "8859_1";
   private static final String DEF_PASSWORD_PROP_PFX = "password";
   private static final String DEF_HEADER = "com.mchange.util.impl.HexAsciiMD5PropertiesPasswordManager data";
   private static final boolean DEBUG = true;
   SyncedProperties props;
   String pfx;
   MessageDigest md;

   public HexAsciiMD5PropertiesPasswordManager(File var1, String var2, String[] var3) throws IOException {
      this(new SyncedProperties(var1, var3), var2);
   }

   public HexAsciiMD5PropertiesPasswordManager(File var1, String var2, String var3) throws IOException {
      this(new SyncedProperties(var1, var3), var2);
   }

   public HexAsciiMD5PropertiesPasswordManager(File var1) throws IOException {
      this(var1, "password", "com.mchange.util.impl.HexAsciiMD5PropertiesPasswordManager data");
   }

   private HexAsciiMD5PropertiesPasswordManager(SyncedProperties var1, String var2) throws IOException {
      try {
         this.props = var1;
         this.pfx = var2;
         this.md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException var4) {
         throw new InternalError("MD5 is not supported???");
      }
   }

   @Override
   public synchronized boolean validate(String var1, String var2) throws IOException {
      try {
         String var3 = this.props.getProperty(this.pfx != null ? this.pfx + '.' + var1 : var1);
         byte[] var4 = ByteUtils.fromHexAscii(var3);
         byte[] var5 = this.md.digest(var2.getBytes("8859_1"));
         return Arrays.equals(var4, var5);
      } catch (NumberFormatException var6) {
         throw new IOException("Password file corrupted! [contains invalid hex ascii string]");
      } catch (UnsupportedEncodingException var7) {
         var7.printStackTrace();
         throw new InternalError("8859_1is an unsupported encoding???");
      }
   }

   @Override
   public synchronized boolean updatePassword(String var1, String var2, String var3) throws IOException {
      if (!this.validate(var1, var2)) {
         return false;
      } else {
         this.props.put(this.pfx + '.' + var1, ByteUtils.toHexAscii(this.md.digest(var3.getBytes("8859_1"))));
         return true;
      }
   }
}
