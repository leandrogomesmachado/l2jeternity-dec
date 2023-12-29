package org.eclipse.jdt.internal.compiler.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import javax.tools.FileObject;

public final class Util {
   public static String LINE_SEPARATOR = System.getProperty("line.separator");

   public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding) throws IOException {
      Charset charset = null;

      try {
         charset = Charset.forName(encoding);
      } catch (IllegalCharsetNameException var7) {
         System.err.println("Illegal charset name : " + encoding);
         return null;
      } catch (UnsupportedCharsetException var8) {
         System.err.println("Unsupported charset : " + encoding);
         return null;
      }

      CharsetDecoder charsetDecoder = charset.newDecoder();
      charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
      byte[] contents = org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsByteArray(stream, length);
      ByteBuffer byteBuffer = ByteBuffer.allocate(contents.length);
      byteBuffer.put(contents);
      ((Buffer)byteBuffer).flip();
      return charsetDecoder.decode(byteBuffer).array();
   }

   public static CharSequence getCharContents(FileObject fileObject, boolean ignoreEncodingErrors, byte[] contents, String encoding) throws IOException {
      if (contents == null) {
         return null;
      } else {
         Charset charset = null;

         try {
            charset = Charset.forName(encoding);
         } catch (IllegalCharsetNameException var13) {
            System.err.println("Illegal charset name : " + encoding);
            return null;
         } catch (UnsupportedCharsetException var14) {
            System.err.println("Unsupported charset : " + encoding);
            return null;
         }

         CharsetDecoder charsetDecoder = charset.newDecoder();
         ByteBuffer byteBuffer = ByteBuffer.allocate(contents.length);
         byteBuffer.put(contents);
         ((Buffer)byteBuffer).flip();
         if (ignoreEncodingErrors) {
            charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            return charsetDecoder.decode(byteBuffer);
         } else {
            charsetDecoder.onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer out = CharBuffer.allocate(contents.length);
            CoderResult result = null;
            String replacement = charsetDecoder.replacement();
            int replacementLength = replacement.length();
            Util.EncodingErrorCollector collector = null;

            while(true) {
               result = charsetDecoder.decode(byteBuffer, out, true);
               if (!result.isMalformed() && !result.isUnmappable()) {
                  if (!result.isOverflow()) {
                     ((Buffer)out).flip();
                     if (collector != null) {
                        collector.reportAllEncodingErrors(out.toString());
                     }

                     return out;
                  }

                  CharBuffer temp = CharBuffer.allocate(out.capacity() * 2);
                  ((Buffer)out).flip();
                  temp.put(out);
                  out = temp;
               } else {
                  if (collector == null) {
                     collector = new Util.EncodingErrorCollector(fileObject, encoding);
                  }

                  reportEncodingError(collector, out.position(), result.length());
                  if (out.position() + replacementLength >= out.capacity()) {
                     CharBuffer temp = CharBuffer.allocate(out.capacity() * 2);
                     ((Buffer)out).flip();
                     temp.put(out);
                     out = temp;
                  }

                  out.append(replacement);
                  ((Buffer)byteBuffer).position(byteBuffer.position() + result.length());
               }
            }
         }
      }
   }

   private static void reportEncodingError(Util.EncodingErrorCollector collector, int position, int length) {
      collector.collect(position, -length);
   }

   public static class EncodingError {
      int position;
      int length;

      public EncodingError(int position, int length) {
         this.position = position;
         this.length = length;
      }

      public String getSource(char[] unitSource) {
         int startPosition = this.position;
         int endPosition = this.position + this.length - 1;
         if (startPosition <= endPosition && (startPosition >= 0 || endPosition >= 0) && unitSource.length != 0) {
            StringBuffer errorBuffer = new StringBuffer();
            errorBuffer.append('\t');
            int sourceLength = unitSource.length;
            int begin = startPosition >= sourceLength ? sourceLength - 1 : startPosition;

            char c;
            while(begin > 0 && (c = unitSource[begin - 1]) != '\n' && c != '\r') {
               --begin;
            }

            int end = endPosition >= sourceLength ? sourceLength - 1 : endPosition;

            while(end + 1 < sourceLength && (c = unitSource[end + 1]) != '\r' && c != '\n') {
               ++end;
            }

            while((c = unitSource[begin]) == ' ' || c == '\t') {
               ++begin;
            }

            errorBuffer.append(unitSource, begin, end - begin + 1);
            errorBuffer.append(Util.LINE_SEPARATOR).append("\t");

            for(int i = begin; i < startPosition; ++i) {
               errorBuffer.append((char)(unitSource[i] == '\t' ? '\t' : ' '));
            }

            for(int i = startPosition; i <= (endPosition >= sourceLength ? sourceLength - 1 : endPosition); ++i) {
               errorBuffer.append('^');
            }

            return errorBuffer.toString();
         } else {
            return "No source available";
         }
      }
   }

   public static class EncodingErrorCollector {
      ArrayList<Util.EncodingError> encodingErrors = new ArrayList<>();
      FileObject fileObject;
      String encoding;

      public EncodingErrorCollector(FileObject fileObject, String encoding) {
         this.fileObject = fileObject;
         this.encoding = encoding;
      }

      public void collect(int position, int length) {
         this.encodingErrors.add(new Util.EncodingError(position, length));
      }

      public void reportAllEncodingErrors(String string) {
         char[] unitSource = string.toCharArray();

         for(Util.EncodingError error : this.encodingErrors) {
            System.err.println(this.fileObject.getName() + " Unmappable character for encoding " + this.encoding);
            System.err.println(error.getSource(unitSource));
         }
      }
   }
}
