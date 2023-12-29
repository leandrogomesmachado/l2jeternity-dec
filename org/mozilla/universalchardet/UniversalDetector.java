package org.mozilla.universalchardet;

import java.io.FileInputStream;
import org.mozilla.universalchardet.prober.CharsetProber;
import org.mozilla.universalchardet.prober.EscCharsetProber;
import org.mozilla.universalchardet.prober.Latin1Prober;
import org.mozilla.universalchardet.prober.MBCSGroupProber;
import org.mozilla.universalchardet.prober.SBCSGroupProber;

public class UniversalDetector {
   public static final float SHORTCUT_THRESHOLD = 0.95F;
   public static final float MINIMUM_THRESHOLD = 0.2F;
   private UniversalDetector.InputState inputState;
   private boolean done;
   private boolean start;
   private boolean gotData;
   private byte lastChar;
   private String detectedCharset;
   private CharsetProber[] probers;
   private CharsetProber escCharsetProber;
   private CharsetListener listener;

   public UniversalDetector(CharsetListener var1) {
      this.listener = var1;
      this.escCharsetProber = null;
      this.probers = new CharsetProber[3];

      for(int var2 = 0; var2 < this.probers.length; ++var2) {
         this.probers[var2] = null;
      }

      this.reset();
   }

   public boolean isDone() {
      return this.done;
   }

   public String getDetectedCharset() {
      return this.detectedCharset;
   }

   public void setListener(CharsetListener var1) {
      this.listener = var1;
   }

   public CharsetListener getListener() {
      return this.listener;
   }

   public void handleData(byte[] var1, int var2, int var3) {
      if (!this.done) {
         if (var3 > 0) {
            this.gotData = true;
         }

         if (this.start) {
            this.start = false;
            if (var3 > 3) {
               int var4 = var1[var2] & 255;
               int var5 = var1[var2 + 1] & 255;
               int var6 = var1[var2 + 2] & 255;
               int var7 = var1[var2 + 3] & 255;
               switch(var4) {
                  case 0:
                     if (var5 == 0 && var6 == 254 && var7 == 255) {
                        this.detectedCharset = Constants.CHARSET_UTF_32BE;
                     } else if (var5 == 0 && var6 == 255 && var7 == 254) {
                        this.detectedCharset = Constants.CHARSET_X_ISO_10646_UCS_4_2143;
                     }
                     break;
                  case 239:
                     if (var5 == 187 && var6 == 191) {
                        this.detectedCharset = Constants.CHARSET_UTF_8;
                     }
                     break;
                  case 254:
                     if (var5 == 255 && var6 == 0 && var7 == 0) {
                        this.detectedCharset = Constants.CHARSET_X_ISO_10646_UCS_4_3412;
                     } else if (var5 == 255) {
                        this.detectedCharset = Constants.CHARSET_UTF_16BE;
                     }
                     break;
                  case 255:
                     if (var5 == 254 && var6 == 0 && var7 == 0) {
                        this.detectedCharset = Constants.CHARSET_UTF_32LE;
                     } else if (var5 == 254) {
                        this.detectedCharset = Constants.CHARSET_UTF_16LE;
                     }
               }

               if (this.detectedCharset != null) {
                  this.done = true;
                  return;
               }
            }
         }

         int var8 = var2 + var3;

         for(int var9 = var2; var9 < var8; ++var9) {
            int var12 = var1[var9] & 255;
            if ((var12 & 128) != 0 && var12 != 160) {
               if (this.inputState != UniversalDetector.InputState.HIGHBYTE) {
                  this.inputState = UniversalDetector.InputState.HIGHBYTE;
                  if (this.escCharsetProber != null) {
                     this.escCharsetProber = null;
                  }

                  if (this.probers[0] == null) {
                     this.probers[0] = new MBCSGroupProber();
                  }

                  if (this.probers[1] == null) {
                     this.probers[1] = new SBCSGroupProber();
                  }

                  if (this.probers[2] == null) {
                     this.probers[2] = new Latin1Prober();
                  }
               }
            } else {
               if (this.inputState == UniversalDetector.InputState.PURE_ASCII && (var12 == 27 || var12 == 123 && this.lastChar == 126)) {
                  this.inputState = UniversalDetector.InputState.ESC_ASCII;
               }

               this.lastChar = var1[var9];
            }
         }

         if (this.inputState == UniversalDetector.InputState.ESC_ASCII) {
            if (this.escCharsetProber == null) {
               this.escCharsetProber = new EscCharsetProber();
            }

            CharsetProber.ProbingState var10 = this.escCharsetProber.handleData(var1, var2, var3);
            if (var10 == CharsetProber.ProbingState.FOUND_IT) {
               this.done = true;
               this.detectedCharset = this.escCharsetProber.getCharSetName();
            }
         } else if (this.inputState == UniversalDetector.InputState.HIGHBYTE) {
            for(int var13 = 0; var13 < this.probers.length; ++var13) {
               CharsetProber.ProbingState var11 = this.probers[var13].handleData(var1, var2, var3);
               if (var11 == CharsetProber.ProbingState.FOUND_IT) {
                  this.done = true;
                  this.detectedCharset = this.probers[var13].getCharSetName();
                  return;
               }
            }
         }
      }
   }

   public void dataEnd() {
      if (this.gotData) {
         if (this.detectedCharset != null) {
            this.done = true;
            if (this.listener != null) {
               this.listener.report(this.detectedCharset);
            }
         } else {
            if (this.inputState == UniversalDetector.InputState.HIGHBYTE) {
               float var2 = 0.0F;
               int var3 = 0;

               for(int var4 = 0; var4 < this.probers.length; ++var4) {
                  float var1 = this.probers[var4].getConfidence();
                  if (var1 > var2) {
                     var2 = var1;
                     var3 = var4;
                  }
               }

               if (var2 > 0.2F) {
                  this.detectedCharset = this.probers[var3].getCharSetName();
                  if (this.listener != null) {
                     this.listener.report(this.detectedCharset);
                  }
               }
            } else if (this.inputState == UniversalDetector.InputState.ESC_ASCII) {
            }
         }
      }
   }

   public void reset() {
      this.done = false;
      this.start = true;
      this.detectedCharset = null;
      this.gotData = false;
      this.inputState = UniversalDetector.InputState.PURE_ASCII;
      this.lastChar = 0;
      if (this.escCharsetProber != null) {
         this.escCharsetProber.reset();
      }

      for(int var1 = 0; var1 < this.probers.length; ++var1) {
         if (this.probers[var1] != null) {
            this.probers[var1].reset();
         }
      }
   }

   public static void main(String[] var0) throws Exception {
      if (var0.length != 1) {
         System.out.println("USAGE: java UniversalDetector filename");
      } else {
         UniversalDetector var1 = new UniversalDetector(new CharsetListener() {
            @Override
            public void report(String var1) {
               System.out.println("charset = " + var1);
            }
         });
         byte[] var2 = new byte[4096];
         FileInputStream var3 = new FileInputStream(var0[0]);

         int var4;
         while((var4 = var3.read(var2)) > 0 && !var1.isDone()) {
            var1.handleData(var2, 0, var4);
         }

         var1.dataEnd();
      }
   }

   public static enum InputState {
      PURE_ASCII,
      ESC_ASCII,
      HIGHBYTE;
   }
}
