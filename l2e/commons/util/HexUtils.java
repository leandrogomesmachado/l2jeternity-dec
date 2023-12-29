package l2e.commons.util;

import java.util.Arrays;

public class HexUtils {
   private static final char[] _NIBBLE_CHAR_LOOKUP = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private static final char[] _NEW_LINE_CHARS = System.getProperty("line.separator").toCharArray();
   private static final int _HEX_ED_BPL = 16;
   private static final int _HEX_ED_CPB = 2;

   public static char[] b2HexChars(byte data) {
      return b2HexChars(data, null, 0);
   }

   public static char[] b2HexChars(byte data, char[] dstHexChars, int dstOffset) {
      if (dstHexChars == null) {
         dstHexChars = new char[2];
         dstOffset = 0;
      }

      dstHexChars[dstOffset] = _NIBBLE_CHAR_LOOKUP[(data & 240) >> 4];
      dstHexChars[dstOffset + 1] = _NIBBLE_CHAR_LOOKUP[data & 15];
      return dstHexChars;
   }

   public static char[] int2HexChars(int data) {
      return int2HexChars(data, new char[8], 0);
   }

   public static char[] int2HexChars(int data, char[] dstHexChars, int dstOffset) {
      if (dstHexChars == null) {
         dstHexChars = new char[8];
         dstOffset = 0;
      }

      b2HexChars((byte)((data & 0xFF000000) >> 24), dstHexChars, dstOffset);
      b2HexChars((byte)((data & 0xFF0000) >> 16), dstHexChars, dstOffset + 2);
      b2HexChars((byte)((data & 0xFF00) >> 8), dstHexChars, dstOffset + 4);
      b2HexChars((byte)(data & 0xFF), dstHexChars, dstOffset + 6);
      return dstHexChars;
   }

   public static char[] bArr2HexChars(byte[] data, int offset, int len) {
      return bArr2HexChars(data, offset, len, null, 0);
   }

   public static char[] bArr2HexChars(byte[] data, int offset, int len, char[] dstHexChars, int dstOffset) {
      if (dstHexChars == null) {
         dstHexChars = new char[len * 2];
         dstOffset = 0;
      }

      int dataIdx = offset;

      for(int charsIdx = dstOffset; dataIdx < len + offset; ++charsIdx) {
         dstHexChars[charsIdx] = _NIBBLE_CHAR_LOOKUP[(data[dataIdx] & 240) >> 4];
         dstHexChars[charsIdx] = _NIBBLE_CHAR_LOOKUP[data[dataIdx] & 15];
         ++dataIdx;
         ++charsIdx;
      }

      return dstHexChars;
   }

   public static char[] bArr2AsciiChars(byte[] data, int offset, int len) {
      return bArr2AsciiChars(data, offset, len, new char[len], 0);
   }

   public static char[] bArr2AsciiChars(byte[] data, int offset, int len, char[] dstAsciiChars, int dstOffset) {
      if (dstAsciiChars == null) {
         dstAsciiChars = new char[len];
         dstOffset = 0;
      }

      int dataIdx = offset;

      for(int charsIdx = dstOffset; dataIdx < len + offset; ++charsIdx) {
         if (data[dataIdx] > 31 && data[dataIdx] < 128) {
            dstAsciiChars[charsIdx] = (char)data[dataIdx];
         } else {
            dstAsciiChars[charsIdx] = '.';
         }

         ++dataIdx;
      }

      return dstAsciiChars;
   }

   public static char[] bArr2HexEdChars(byte[] data, int len) {
      int lineLength = 58 + _NEW_LINE_CHARS.length;
      int lenBplMod = len % 16;
      int numLines;
      char[] textData;
      if (lenBplMod == 0) {
         numLines = len / 16;
         textData = new char[lineLength * numLines - _NEW_LINE_CHARS.length];
      } else {
         numLines = len / 16 + 1;
         textData = new char[lineLength * numLines - (16 - lenBplMod) - _NEW_LINE_CHARS.length];
      }

      for(int i = 0; i < numLines; ++i) {
         int dataOffset = i * 16;
         int dataLen = Math.min(len - dataOffset, 16);
         int lineStart = i * lineLength;
         int lineHexDataStart = lineStart + 9;
         int lineAsciiDataStart = lineHexDataStart + 32 + 1;
         int2HexChars(dataOffset, textData, lineStart);
         textData[lineHexDataStart - 1] = ' ';
         bArr2HexChars(data, dataOffset, dataLen, textData, lineHexDataStart);
         bArr2AsciiChars(data, dataOffset, dataLen, textData, lineAsciiDataStart);
         if (i < numLines - 1) {
            textData[lineAsciiDataStart - 1] = ' ';
            System.arraycopy(_NEW_LINE_CHARS, 0, textData, lineAsciiDataStart + 16, _NEW_LINE_CHARS.length);
         } else if (dataLen < 16) {
            int lineHexDataEnd = lineHexDataStart + dataLen * 2;
            Arrays.fill(textData, lineHexDataEnd, lineHexDataEnd + (16 - dataLen) * 2 + 1, ' ');
         } else {
            textData[lineAsciiDataStart - 1] = ' ';
         }
      }

      return textData;
   }
}
