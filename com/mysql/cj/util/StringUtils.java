package com.mysql.cj.util;

import com.mysql.cj.Messages;
import com.mysql.cj.ServerVersion;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.NumberOutOfRange;
import com.mysql.cj.exceptions.WrongArgumentException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__ALL = Collections.unmodifiableSet(EnumSet.allOf(StringUtils.SearchMode.class));
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__MRK_COM_WS = Collections.unmodifiableSet(
      EnumSet.of(
         StringUtils.SearchMode.SKIP_BETWEEN_MARKERS,
         StringUtils.SearchMode.SKIP_BLOCK_COMMENTS,
         StringUtils.SearchMode.SKIP_LINE_COMMENTS,
         StringUtils.SearchMode.SKIP_WHITE_SPACE
      )
   );
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__BSESC_COM_WS = Collections.unmodifiableSet(
      EnumSet.of(
         StringUtils.SearchMode.ALLOW_BACKSLASH_ESCAPE,
         StringUtils.SearchMode.SKIP_BLOCK_COMMENTS,
         StringUtils.SearchMode.SKIP_LINE_COMMENTS,
         StringUtils.SearchMode.SKIP_WHITE_SPACE
      )
   );
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__BSESC_MRK_WS = Collections.unmodifiableSet(
      EnumSet.of(StringUtils.SearchMode.ALLOW_BACKSLASH_ESCAPE, StringUtils.SearchMode.SKIP_BETWEEN_MARKERS, StringUtils.SearchMode.SKIP_WHITE_SPACE)
   );
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__COM_WS = Collections.unmodifiableSet(
      EnumSet.of(StringUtils.SearchMode.SKIP_BLOCK_COMMENTS, StringUtils.SearchMode.SKIP_LINE_COMMENTS, StringUtils.SearchMode.SKIP_WHITE_SPACE)
   );
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__MRK_WS = Collections.unmodifiableSet(
      EnumSet.of(StringUtils.SearchMode.SKIP_BETWEEN_MARKERS, StringUtils.SearchMode.SKIP_WHITE_SPACE)
   );
   public static final Set<StringUtils.SearchMode> SEARCH_MODE__NONE = Collections.unmodifiableSet(EnumSet.noneOf(StringUtils.SearchMode.class));
   private static final int NON_COMMENTS_MYSQL_VERSION_REF_LENGTH = 5;
   private static final int WILD_COMPARE_MATCH = 0;
   private static final int WILD_COMPARE_CONTINUE_WITH_WILD = 1;
   private static final int WILD_COMPARE_NO_MATCH = -1;
   static final char WILDCARD_MANY = '%';
   static final char WILDCARD_ONE = '_';
   static final char WILDCARD_ESCAPE = '\\';
   private static final String VALID_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@";
   private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
   static final char[] EMPTY_SPACE = new char[255];

   public static String dumpAsHex(byte[] byteBuffer, int length) {
      length = Math.min(length, byteBuffer.length);
      StringBuilder fullOutBuilder = new StringBuilder(length * 4);
      StringBuilder asciiOutBuilder = new StringBuilder(16);
      int p = 0;

      for(int l = 0; p < length; l = 0) {
         while(l < 8 && p < length) {
            int asInt = byteBuffer[p] & 255;
            if (asInt < 16) {
               fullOutBuilder.append("0");
            }

            fullOutBuilder.append(Integer.toHexString(asInt)).append(" ");
            asciiOutBuilder.append(" ").append(asInt >= 32 && asInt < 127 ? (char)asInt : ".");
            ++p;
            ++l;
         }

         while(l < 8) {
            fullOutBuilder.append("   ");
            ++l;
         }

         fullOutBuilder.append("   ").append((CharSequence)asciiOutBuilder).append(System.lineSeparator());
         asciiOutBuilder.setLength(0);
      }

      return fullOutBuilder.toString();
   }

   public static String toHexString(byte[] byteBuffer, int length) {
      length = Math.min(length, byteBuffer.length);
      StringBuilder outputBuilder = new StringBuilder(length * 2);

      for(int i = 0; i < length; ++i) {
         int asInt = byteBuffer[i] & 255;
         if (asInt < 16) {
            outputBuilder.append("0");
         }

         outputBuilder.append(Integer.toHexString(asInt));
      }

      return outputBuilder.toString();
   }

   private static boolean endsWith(byte[] dataFrom, String suffix) {
      for(int i = 1; i <= suffix.length(); ++i) {
         int dfOffset = dataFrom.length - i;
         int suffixOffset = suffix.length() - i;
         if (dataFrom[dfOffset] != suffix.charAt(suffixOffset)) {
            return false;
         }
      }

      return true;
   }

   public static char firstNonWsCharUc(String searchIn) {
      return firstNonWsCharUc(searchIn, 0);
   }

   public static char firstNonWsCharUc(String searchIn, int startAt) {
      if (searchIn == null) {
         return '\u0000';
      } else {
         int length = searchIn.length();

         for(int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (!Character.isWhitespace(c)) {
               return Character.toUpperCase(c);
            }
         }

         return '\u0000';
      }
   }

   public static char firstAlphaCharUc(String searchIn, int startAt) {
      if (searchIn == null) {
         return '\u0000';
      } else {
         int length = searchIn.length();

         for(int i = startAt; i < length; ++i) {
            char c = searchIn.charAt(i);
            if (Character.isLetter(c)) {
               return Character.toUpperCase(c);
            }
         }

         return '\u0000';
      }
   }

   public static String fixDecimalExponent(String dString) {
      int ePos = dString.indexOf(69);
      if (ePos == -1) {
         ePos = dString.indexOf(101);
      }

      if (ePos != -1 && dString.length() > ePos + 1) {
         char maybeMinusChar = dString.charAt(ePos + 1);
         if (maybeMinusChar != '-' && maybeMinusChar != '+') {
            StringBuilder strBuilder = new StringBuilder(dString.length() + 1);
            strBuilder.append(dString.substring(0, ePos + 1));
            strBuilder.append('+');
            strBuilder.append(dString.substring(ePos + 1, dString.length()));
            dString = strBuilder.toString();
         }
      }

      return dString;
   }

   public static byte[] getBytes(String s, String encoding) {
      if (encoding == null) {
         return getBytes(s);
      } else {
         try {
            return s.getBytes(encoding);
         } catch (UnsupportedEncodingException var3) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}), var3
            );
         }
      }
   }

   public static byte[] getBytesWrapped(String s, char beginWrap, char endWrap, String encoding) {
      byte[] b;
      if (encoding == null) {
         StringBuilder strBuilder = new StringBuilder(s.length() + 2);
         strBuilder.append(beginWrap);
         strBuilder.append(s);
         strBuilder.append(endWrap);
         b = getBytes(strBuilder.toString());
      } else {
         StringBuilder strBuilder = new StringBuilder(s.length() + 2);
         strBuilder.append(beginWrap);
         strBuilder.append(s);
         strBuilder.append(endWrap);
         s = strBuilder.toString();
         b = getBytes(s, encoding);
      }

      return b;
   }

   public static int getInt(byte[] buf) throws NumberFormatException {
      return getInt(buf, 0, buf.length);
   }

   public static int getInt(byte[] buf, int offset, int endpos) throws NumberFormatException {
      long l = getLong(buf, offset, endpos);
      if (l >= -2147483648L && l <= 2147483647L) {
         return (int)l;
      } else {
         throw new NumberOutOfRange(Messages.getString("StringUtils.badIntFormat", new Object[]{toString(buf, offset, endpos - offset)}));
      }
   }

   public static long getLong(byte[] buf) throws NumberFormatException {
      return getLong(buf, 0, buf.length);
   }

   public static long getLong(byte[] buf, int offset, int endpos) throws NumberFormatException {
      int base = 10;
      int s = offset;

      while(s < endpos && Character.isWhitespace((char)buf[s])) {
         ++s;
      }

      if (s == endpos) {
         throw new NumberFormatException(toString(buf));
      } else {
         boolean negative = false;
         if ((char)buf[s] == '-') {
            negative = true;
            ++s;
         } else if ((char)buf[s] == '+') {
            ++s;
         }

         int save = s;
         long cutoff = Long.MAX_VALUE / (long)base;
         long cutlim = (long)((int)(Long.MAX_VALUE % (long)base));
         if (negative) {
            ++cutlim;
         }

         boolean overflow = false;

         long i;
         for(i = 0L; s < endpos; ++s) {
            char c = (char)buf[s];
            if (c >= '0' && c <= '9') {
               c = (char)(c - '0');
            } else {
               if (!Character.isLetter(c)) {
                  break;
               }

               c = (char)(Character.toUpperCase(c) - 'A' + 10);
            }

            if (c >= base) {
               break;
            }

            if (i <= cutoff && (i != cutoff || (long)c <= cutlim)) {
               i *= (long)base;
               i += (long)c;
            } else {
               overflow = true;
            }
         }

         if (s == save) {
            throw new NumberFormatException(Messages.getString("StringUtils.badIntFormat", new Object[]{toString(buf, offset, endpos - offset)}));
         } else if (overflow) {
            throw new NumberOutOfRange(Messages.getString("StringUtils.badIntFormat", new Object[]{toString(buf, offset, endpos - offset)}));
         } else {
            return negative ? -i : i;
         }
      }
   }

   public static int indexOfIgnoreCase(String searchIn, String searchFor) {
      return indexOfIgnoreCase(0, searchIn, searchFor);
   }

   public static int indexOfIgnoreCase(int startingPosition, String searchIn, String searchFor) {
      if (searchIn != null && searchFor != null) {
         int searchInLength = searchIn.length();
         int searchForLength = searchFor.length();
         int stopSearchingAt = searchInLength - searchForLength;
         if (startingPosition <= stopSearchingAt && searchForLength != 0) {
            char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
            char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));

            for(int i = startingPosition; i <= stopSearchingAt; ++i) {
               if (isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                  do {
                     ++i;
                  } while(i <= stopSearchingAt && isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc));
               }

               if (i <= stopSearchingAt && startsWithIgnoreCase(searchIn, i, searchFor)) {
                  return i;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static int indexOfIgnoreCase(
      int startingPosition, String searchIn, String[] searchForSequence, String openingMarkers, String closingMarkers, Set<StringUtils.SearchMode> searchMode
   ) {
      if (searchIn != null && searchForSequence != null) {
         int searchInLength = searchIn.length();
         int searchForLength = 0;

         for(String searchForPart : searchForSequence) {
            searchForLength += searchForPart.length();
         }

         if (searchForLength == 0) {
            return -1;
         } else {
            int searchForWordsCount = searchForSequence.length;
            searchForLength += searchForWordsCount > 0 ? searchForWordsCount - 1 : 0;
            int stopSearchingAt = searchInLength - searchForLength;
            if (startingPosition > stopSearchingAt) {
               return -1;
            } else if (!searchMode.contains(StringUtils.SearchMode.SKIP_BETWEEN_MARKERS)
               || openingMarkers != null && closingMarkers != null && openingMarkers.length() == closingMarkers.length()) {
               if (Character.isWhitespace(searchForSequence[0].charAt(0)) && searchMode.contains(StringUtils.SearchMode.SKIP_WHITE_SPACE)) {
                  searchMode = EnumSet.copyOf(searchMode);
                  searchMode.remove(StringUtils.SearchMode.SKIP_WHITE_SPACE);
               }

               Set<StringUtils.SearchMode> searchMode2 = EnumSet.of(StringUtils.SearchMode.SKIP_WHITE_SPACE);
               searchMode2.addAll(searchMode);
               searchMode2.remove(StringUtils.SearchMode.SKIP_BETWEEN_MARKERS);

               for(int positionOfFirstWord = startingPosition; positionOfFirstWord <= stopSearchingAt; ++positionOfFirstWord) {
                  positionOfFirstWord = indexOfIgnoreCase(positionOfFirstWord, searchIn, searchForSequence[0], openingMarkers, closingMarkers, searchMode);
                  if (positionOfFirstWord == -1 || positionOfFirstWord > stopSearchingAt) {
                     return -1;
                  }

                  int startingPositionForNextWord = positionOfFirstWord + searchForSequence[0].length();
                  int wc = 0;
                  boolean match = true;

                  while(++wc < searchForWordsCount && match) {
                     int positionOfNextWord = indexOfNextChar(startingPositionForNextWord, searchInLength - 1, searchIn, null, null, null, searchMode2);
                     if (startingPositionForNextWord != positionOfNextWord && startsWithIgnoreCase(searchIn, positionOfNextWord, searchForSequence[wc])) {
                        startingPositionForNextWord = positionOfNextWord + searchForSequence[wc].length();
                     } else {
                        match = false;
                     }
                  }

                  if (match) {
                     return positionOfFirstWord;
                  }
               }

               return -1;
            } else {
               throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[]{openingMarkers, closingMarkers}));
            }
         }
      } else {
         return -1;
      }
   }

   public static int indexOfIgnoreCase(
      int startingPosition, String searchIn, String searchFor, String openingMarkers, String closingMarkers, Set<StringUtils.SearchMode> searchMode
   ) {
      return indexOfIgnoreCase(startingPosition, searchIn, searchFor, openingMarkers, closingMarkers, "", searchMode);
   }

   public static int indexOfIgnoreCase(
      int startingPosition,
      String searchIn,
      String searchFor,
      String openingMarkers,
      String closingMarkers,
      String overridingMarkers,
      Set<StringUtils.SearchMode> searchMode
   ) {
      if (searchIn != null && searchFor != null) {
         int searchInLength = searchIn.length();
         int searchForLength = searchFor.length();
         int stopSearchingAt = searchInLength - searchForLength;
         if (startingPosition <= stopSearchingAt && searchForLength != 0) {
            if (searchMode.contains(StringUtils.SearchMode.SKIP_BETWEEN_MARKERS)) {
               if (openingMarkers == null || closingMarkers == null || openingMarkers.length() != closingMarkers.length()) {
                  throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[]{openingMarkers, closingMarkers}));
               }

               if (overridingMarkers == null) {
                  throw new IllegalArgumentException(Messages.getString("StringUtils.16", new String[]{overridingMarkers, openingMarkers}));
               }

               for(char c : overridingMarkers.toCharArray()) {
                  if (openingMarkers.indexOf(c) == -1) {
                     throw new IllegalArgumentException(Messages.getString("StringUtils.16", new String[]{overridingMarkers, openingMarkers}));
                  }
               }
            }

            char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
            char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
            if (Character.isWhitespace(firstCharOfSearchForLc) && searchMode.contains(StringUtils.SearchMode.SKIP_WHITE_SPACE)) {
               searchMode = EnumSet.copyOf(searchMode);
               searchMode.remove(StringUtils.SearchMode.SKIP_WHITE_SPACE);
            }

            for(int i = startingPosition; i <= stopSearchingAt; ++i) {
               i = indexOfNextChar(i, stopSearchingAt, searchIn, openingMarkers, closingMarkers, overridingMarkers, searchMode);
               if (i == -1) {
                  return -1;
               }

               char c = searchIn.charAt(i);
               if (isCharEqualIgnoreCase(c, firstCharOfSearchForUc, firstCharOfSearchForLc) && startsWithIgnoreCase(searchIn, i, searchFor)) {
                  return i;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static int indexOfNextChar(
      int startingPosition,
      int stopPosition,
      String searchIn,
      String openingMarkers,
      String closingMarkers,
      String overridingMarkers,
      Set<StringUtils.SearchMode> searchMode
   ) {
      if (searchIn == null) {
         return -1;
      } else {
         int searchInLength = searchIn.length();
         if (startingPosition >= searchInLength) {
            return -1;
         } else {
            char c0 = '\u0000';
            char c1 = searchIn.charAt(startingPosition);
            char c2 = startingPosition + 1 < searchInLength ? searchIn.charAt(startingPosition + 1) : 0;

            for(int i = startingPosition; i <= stopPosition; ++i) {
               c0 = c1;
               c1 = c2;
               c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
               boolean dashDashCommentImmediateEnd = false;
               int markerIndex = -1;
               if (searchMode.contains(StringUtils.SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                  ++i;
                  c1 = c2;
                  c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
               } else if (searchMode.contains(StringUtils.SearchMode.SKIP_BETWEEN_MARKERS) && (markerIndex = openingMarkers.indexOf(c0)) != -1) {
                  int nestedMarkersCount = 0;
                  char openingMarker = c0;
                  char closingMarker = closingMarkers.charAt(markerIndex);
                  boolean outerIsAnOverridingMarker = overridingMarkers.indexOf(c0) != -1;

                  while(true) {
                     ++i;
                     if (i > stopPosition || (c0 = searchIn.charAt(i)) == closingMarker && nestedMarkersCount == 0) {
                        c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0;
                        c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
                        break;
                     }

                     if (!outerIsAnOverridingMarker && overridingMarkers.indexOf(c0) != -1) {
                        int overridingMarkerIndex = openingMarkers.indexOf(c0);
                        int overridingNestedMarkersCount = 0;
                        char overridingOpeningMarker = c0;
                        char overridingClosingMarker = closingMarkers.charAt(overridingMarkerIndex);

                        while(true) {
                           ++i;
                           if (i > stopPosition || (c0 = searchIn.charAt(i)) == overridingClosingMarker && overridingNestedMarkersCount == 0) {
                              break;
                           }

                           if (c0 == overridingOpeningMarker) {
                              ++overridingNestedMarkersCount;
                           } else if (c0 == overridingClosingMarker) {
                              --overridingNestedMarkersCount;
                           } else if (searchMode.contains(StringUtils.SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                              ++i;
                           }
                        }
                     } else if (c0 == openingMarker) {
                        ++nestedMarkersCount;
                     } else if (c0 == closingMarker) {
                        --nestedMarkersCount;
                     } else if (searchMode.contains(StringUtils.SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                        ++i;
                     }
                  }
               } else if (searchMode.contains(StringUtils.SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '/' && c1 == '*') {
                  if (c2 != '!') {
                     ++i;

                     do {
                        ++i;
                     } while(i <= stopPosition && (searchIn.charAt(i) != '*' || (i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0) != '/'));

                     ++i;
                  } else {
                     ++i;
                     ++i;
                     int j = 1;

                     while(j <= 5 && i + j < searchInLength && Character.isDigit(searchIn.charAt(i + j))) {
                        ++j;
                     }

                     if (j == 5) {
                        i += 5;
                     }
                  }

                  c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0;
                  c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
               } else if (searchMode.contains(StringUtils.SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '*' && c1 == '/') {
                  ++i;
                  c1 = c2;
                  c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
               } else if (searchMode.contains(StringUtils.SearchMode.SKIP_LINE_COMMENTS)
                  && (c0 == '-' && c1 == '-' && (Character.isWhitespace(c2) || (dashDashCommentImmediateEnd = c2 == ';') || c2 == 0) || c0 == '#')) {
                  if (dashDashCommentImmediateEnd) {
                     ++i;
                     ++i;
                     c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0;
                     c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
                  } else {
                     do {
                        ++i;
                     } while(i <= stopPosition && (c0 = searchIn.charAt(i)) != '\n' && c0 != '\r');

                     c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0;
                     if (c0 == '\r' && c1 == '\n') {
                        ++i;
                        c1 = i + 1 < searchInLength ? searchIn.charAt(i + 1) : 0;
                     }

                     c2 = i + 2 < searchInLength ? searchIn.charAt(i + 2) : 0;
                  }
               } else if (!searchMode.contains(StringUtils.SearchMode.SKIP_WHITE_SPACE) || !Character.isWhitespace(c0)) {
                  return i;
               }
            }

            return -1;
         }
      }
   }

   private static boolean isCharAtPosNotEqualIgnoreCase(String searchIn, int pos, char firstCharOfSearchForUc, char firstCharOfSearchForLc) {
      return Character.toLowerCase(searchIn.charAt(pos)) != firstCharOfSearchForLc && Character.toUpperCase(searchIn.charAt(pos)) != firstCharOfSearchForUc;
   }

   private static boolean isCharEqualIgnoreCase(char charToCompare, char compareToCharUC, char compareToCharLC) {
      return Character.toLowerCase(charToCompare) == compareToCharLC || Character.toUpperCase(charToCompare) == compareToCharUC;
   }

   public static List<String> split(String stringToSplit, String delimiter, boolean trim) {
      if (stringToSplit == null) {
         return new ArrayList<>();
      } else if (delimiter == null) {
         throw new IllegalArgumentException();
      } else {
         String[] tokens = stringToSplit.split(delimiter, -1);
         Stream<String> tokensStream = Arrays.asList(tokens).stream();
         if (trim) {
            tokensStream = tokensStream.map(String::trim);
         }

         return tokensStream.collect(Collectors.toList());
      }
   }

   public static List<String> split(String stringToSplit, String delimiter, String openingMarkers, String closingMarkers, boolean trim) {
      return split(stringToSplit, delimiter, openingMarkers, closingMarkers, "", trim);
   }

   public static List<String> split(
      String stringToSplit, String delimiter, String openingMarkers, String closingMarkers, boolean trim, Set<StringUtils.SearchMode> searchMode
   ) {
      return split(stringToSplit, delimiter, openingMarkers, closingMarkers, "", trim, searchMode);
   }

   public static List<String> split(
      String stringToSplit, String delimiter, String openingMarkers, String closingMarkers, String overridingMarkers, boolean trim
   ) {
      return split(stringToSplit, delimiter, openingMarkers, closingMarkers, overridingMarkers, trim, SEARCH_MODE__MRK_COM_WS);
   }

   public static List<String> split(
      String stringToSplit,
      String delimiter,
      String openingMarkers,
      String closingMarkers,
      String overridingMarkers,
      boolean trim,
      Set<StringUtils.SearchMode> searchMode
   ) {
      if (stringToSplit == null) {
         return new ArrayList<>();
      } else if (delimiter == null) {
         throw new IllegalArgumentException();
      } else {
         int delimPos = 0;
         int currentPos = 0;

         List<String> splitTokens;
         for(splitTokens = new ArrayList<>();
            (delimPos = indexOfIgnoreCase(currentPos, stringToSplit, delimiter, openingMarkers, closingMarkers, overridingMarkers, searchMode)) != true;
            currentPos = delimPos + delimiter.length()
         ) {
            String token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
               token = token.trim();
            }

            splitTokens.add(token);
         }

         String token = stringToSplit.substring(currentPos);
         if (trim) {
            token = token.trim();
         }

         splitTokens.add(token);
         return splitTokens;
      }
   }

   private static boolean startsWith(byte[] dataFrom, String chars) {
      int charsLength = chars.length();
      if (dataFrom.length < charsLength) {
         return false;
      } else {
         for(int i = 0; i < charsLength; ++i) {
            if (dataFrom[i] != chars.charAt(i)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
      return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
   }

   public static boolean startsWithIgnoreCase(String searchIn, String searchFor) {
      return startsWithIgnoreCase(searchIn, 0, searchFor);
   }

   public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(String searchIn, String searchFor) {
      if (searchIn == null) {
         return searchFor == null;
      } else {
         int beginPos = 0;

         for(int inLength = searchIn.length(); beginPos < inLength; ++beginPos) {
            char c = searchIn.charAt(beginPos);
            if (Character.isLetterOrDigit(c)) {
               break;
            }
         }

         return startsWithIgnoreCase(searchIn, beginPos, searchFor);
      }
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
      return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
   }

   public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
      if (searchIn == null) {
         return searchFor == null;
      } else {
         int inLength = searchIn.length();

         while(beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos))) {
            ++beginPos;
         }

         return startsWithIgnoreCase(searchIn, beginPos, searchFor);
      }
   }

   public static int startsWithIgnoreCaseAndWs(String searchIn, String[] searchFor) {
      for(int i = 0; i < searchFor.length; ++i) {
         if (startsWithIgnoreCaseAndWs(searchIn, searchFor[i], 0)) {
            return i;
         }
      }

      return -1;
   }

   public static byte[] stripEnclosure(byte[] source, String prefix, String suffix) {
      if (source.length >= prefix.length() + suffix.length() && startsWith(source, prefix) && endsWith(source, suffix)) {
         int totalToStrip = prefix.length() + suffix.length();
         int enclosedLength = source.length - totalToStrip;
         byte[] enclosed = new byte[enclosedLength];
         int startPos = prefix.length();
         int numToCopy = enclosed.length;
         System.arraycopy(source, startPos, enclosed, 0, numToCopy);
         return enclosed;
      } else {
         return source;
      }
   }

   public static String toAsciiString(byte[] buffer) {
      return toAsciiString(buffer, 0, buffer.length);
   }

   public static String toAsciiString(byte[] buffer, int startPos, int length) {
      char[] charArray = new char[length];
      int readpoint = startPos;

      for(int i = 0; i < length; ++i) {
         charArray[i] = (char)buffer[readpoint];
         ++readpoint;
      }

      return new String(charArray);
   }

   public static boolean wildCompareIgnoreCase(String searchIn, String searchFor) {
      return wildCompareInternal(searchIn, searchFor) == 0;
   }

   private static int wildCompareInternal(String searchIn, String searchFor) {
      if (searchIn == null || searchFor == null) {
         return -1;
      } else if (searchFor.equals("%")) {
         return 0;
      } else {
         int searchForPos = 0;
         int searchForEnd = searchFor.length();
         int searchInPos = 0;
         int searchInEnd = searchIn.length();
         int result = -1;

         while(searchForPos != searchForEnd) {
            while(searchFor.charAt(searchForPos) != '%' && searchFor.charAt(searchForPos) != '_') {
               if (searchFor.charAt(searchForPos) == '\\' && searchForPos + 1 != searchForEnd) {
                  ++searchForPos;
               }

               if (searchInPos == searchInEnd
                  || Character.toUpperCase(searchFor.charAt(searchForPos++)) != Character.toUpperCase(searchIn.charAt(searchInPos++))) {
                  return 1;
               }

               if (searchForPos == searchForEnd) {
                  return searchInPos != searchInEnd ? 1 : 0;
               }

               result = 1;
            }

            if (searchFor.charAt(searchForPos) == '_') {
               do {
                  if (searchInPos == searchInEnd) {
                     return result;
                  }

                  ++searchInPos;
                  ++searchForPos;
               } while(searchForPos < searchForEnd && searchFor.charAt(searchForPos) == '_');

               if (searchForPos == searchForEnd) {
                  break;
               }
            }

            if (searchFor.charAt(searchForPos) == '%') {
               ++searchForPos;

               for(; searchForPos != searchForEnd; ++searchForPos) {
                  if (searchFor.charAt(searchForPos) != '%') {
                     if (searchFor.charAt(searchForPos) != '_') {
                        break;
                     }

                     if (searchInPos == searchInEnd) {
                        return -1;
                     }

                     ++searchInPos;
                  }
               }

               if (searchForPos == searchForEnd) {
                  return 0;
               }

               if (searchInPos == searchInEnd) {
                  return -1;
               }

               char cmp;
               if ((cmp = searchFor.charAt(searchForPos)) == '\\' && searchForPos + 1 != searchForEnd) {
                  cmp = searchFor.charAt(++searchForPos);
               }

               ++searchForPos;

               while(true) {
                  while(searchInPos == searchInEnd || Character.toUpperCase(searchIn.charAt(searchInPos)) == Character.toUpperCase(cmp)) {
                     if (searchInPos++ == searchInEnd) {
                        return -1;
                     }

                     int tmp = wildCompareInternal(searchIn.substring(searchInPos), searchFor.substring(searchForPos));
                     if (tmp <= 0) {
                        return tmp;
                     }

                     if (searchInPos == searchInEnd) {
                        return -1;
                     }
                  }

                  ++searchInPos;
               }
            }
         }

         return searchInPos != searchInEnd ? 1 : 0;
      }
   }

   public static int lastIndexOf(byte[] s, char c) {
      if (s == null) {
         return -1;
      } else {
         for(int i = s.length - 1; i >= 0; --i) {
            if (s[i] == c) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int indexOf(byte[] s, char c) {
      if (s == null) {
         return -1;
      } else {
         int length = s.length;

         for(int i = 0; i < length; ++i) {
            if (s[i] == c) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean isNullOrEmpty(String toTest) {
      return toTest == null || toTest.isEmpty();
   }

   public static String stripComments(
      String src,
      String stringOpens,
      String stringCloses,
      boolean slashStarComments,
      boolean slashSlashComments,
      boolean hashComments,
      boolean dashDashComments
   ) {
      if (src == null) {
         return null;
      } else {
         StringBuilder strBuilder = new StringBuilder(src.length());
         StringReader sourceReader = new StringReader(src);
         int contextMarker = 0;
         boolean escaped = false;
         int markerTypeFound = -1;
         int ind = 0;
         int currentChar = 0;

         try {
            label141:
            while((currentChar = sourceReader.read()) != -1) {
               if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
                  contextMarker = 0;
                  markerTypeFound = -1;
               } else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped && contextMarker == 0) {
                  markerTypeFound = ind;
                  contextMarker = currentChar;
               }

               if (contextMarker == 0 && currentChar == 47 && (slashSlashComments || slashStarComments)) {
                  currentChar = sourceReader.read();
                  if (currentChar == 42 && slashStarComments) {
                     for(int prevChar = 0; (currentChar = sourceReader.read()) != 47 || prevChar != 42; prevChar = currentChar) {
                        if (currentChar == 13) {
                           currentChar = sourceReader.read();
                           if (currentChar == 10) {
                              currentChar = sourceReader.read();
                           }
                        } else if (currentChar == 10) {
                           currentChar = sourceReader.read();
                        }

                        if (currentChar < 0) {
                           continue label141;
                        }
                     }
                     continue;
                  }

                  if (currentChar == 47 && slashSlashComments) {
                     while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                     }
                  }
               } else if (contextMarker == 0 && currentChar == 35 && hashComments) {
                  while((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {
                  }
               } else {
                  label120:
                  if (contextMarker == 0 && currentChar == 45 && dashDashComments) {
                     currentChar = sourceReader.read();
                     if (currentChar != -1 && currentChar == 45) {
                        while(true) {
                           if ((currentChar = sourceReader.read()) == 10 || currentChar == 13 || currentChar < 0) {
                              break label120;
                           }
                        }
                     }

                     strBuilder.append('-');
                     if (currentChar != -1) {
                        strBuilder.append((char)currentChar);
                     }
                     continue;
                  }
               }

               if (currentChar != -1) {
                  strBuilder.append((char)currentChar);
               }
            }
         } catch (IOException var15) {
         }

         return strBuilder.toString();
      }
   }

   public static String sanitizeProcOrFuncName(String src) {
      return src != null && !src.equals("%") ? src : null;
   }

   public static List<String> splitDBdotName(String source, String catalog, String quoteId, boolean isNoBslashEscSet) {
      if (source != null && !source.equals("%")) {
         int dotIndex = -1;
         if (" ".equals(quoteId)) {
            dotIndex = source.indexOf(".");
         } else {
            dotIndex = indexOfIgnoreCase(0, source, ".", quoteId, quoteId, isNoBslashEscSet ? SEARCH_MODE__MRK_WS : SEARCH_MODE__BSESC_MRK_WS);
         }

         String database = catalog;
         String entityName;
         if (dotIndex != -1) {
            database = unQuoteIdentifier(source.substring(0, dotIndex), quoteId);
            entityName = unQuoteIdentifier(source.substring(dotIndex + 1), quoteId);
         } else {
            entityName = unQuoteIdentifier(source, quoteId);
         }

         return Arrays.asList(database, entityName);
      } else {
         return Collections.emptyList();
      }
   }

   public static boolean isEmptyOrWhitespaceOnly(String str) {
      if (str != null && str.length() != 0) {
         int length = str.length();

         for(int i = 0; i < length; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static String escapeQuote(String src, String quotChar) {
      if (src == null) {
         return null;
      } else {
         src = toString(stripEnclosure(src.getBytes(), quotChar, quotChar));
         int lastNdx = src.indexOf(quotChar);
         String tmpSrc = src.substring(0, lastNdx);
         tmpSrc = tmpSrc + quotChar + quotChar;
         String tmpRest = src.substring(lastNdx + 1, src.length());

         for(int var6 = tmpRest.indexOf(quotChar); var6 > -1; var6 = tmpRest.indexOf(quotChar)) {
            tmpSrc = tmpSrc + tmpRest.substring(0, var6);
            tmpSrc = tmpSrc + quotChar + quotChar;
            tmpRest = tmpRest.substring(var6 + 1, tmpRest.length());
         }

         return tmpSrc + tmpRest;
      }
   }

   public static String quoteIdentifier(String identifier, String quoteChar, boolean isPedantic) {
      if (identifier == null) {
         return null;
      } else {
         identifier = identifier.trim();
         int quoteCharLength = quoteChar.length();
         if (quoteCharLength == 0) {
            return identifier;
         } else {
            if (!isPedantic && identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
               String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);

               int quoteCharPos;
               int quoteCharNextPosition;
               for(quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
                  quoteCharPos >= 0;
                  quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength)
               ) {
                  int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                  quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                  if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                     break;
                  }
               }

               if (quoteCharPos < 0) {
                  return identifier;
               }
            }

            return quoteChar + identifier.replaceAll(quoteChar, quoteChar + quoteChar) + quoteChar;
         }
      }
   }

   public static String quoteIdentifier(String identifier, boolean isPedantic) {
      return quoteIdentifier(identifier, "`", isPedantic);
   }

   public static String unQuoteIdentifier(String identifier, String quoteChar) {
      if (identifier == null) {
         return null;
      } else {
         identifier = identifier.trim();
         int quoteCharLength = quoteChar.length();
         if (quoteCharLength == 0) {
            return identifier;
         } else if (identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
            String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);

            int quoteCharNextPosition;
            for(int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar);
               quoteCharPos >= 0;
               quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength)
            ) {
               int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
               quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
               if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                  return identifier;
               }
            }

            return identifier.substring(quoteCharLength, identifier.length() - quoteCharLength).replaceAll(quoteChar + quoteChar, quoteChar);
         } else {
            return identifier;
         }
      }
   }

   public static int indexOfQuoteDoubleAware(String searchIn, String quoteChar, int startFrom) {
      if (searchIn != null && quoteChar != null && quoteChar.length() != 0 && startFrom <= searchIn.length()) {
         int lastIndex = searchIn.length() - 1;
         int beginPos = startFrom;
         int pos = -1;
         boolean next = true;

         while(next) {
            pos = searchIn.indexOf(quoteChar, beginPos);
            if (pos != -1 && pos != lastIndex && searchIn.startsWith(quoteChar, pos + 1)) {
               beginPos = pos + 2;
            } else {
               next = false;
            }
         }

         return pos;
      } else {
         return -1;
      }
   }

   public static String toString(byte[] value, int offset, int length, String encoding) {
      if (encoding != null && !"null".equalsIgnoreCase(encoding)) {
         try {
            return new String(value, offset, length, encoding);
         } catch (UnsupportedEncodingException var5) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}), var5
            );
         }
      } else {
         return new String(value, offset, length);
      }
   }

   public static String toString(byte[] value, String encoding) {
      if (encoding == null) {
         return new String(value);
      } else {
         try {
            return new String(value, encoding);
         } catch (UnsupportedEncodingException var3) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}), var3
            );
         }
      }
   }

   public static String toString(byte[] value, int offset, int length) {
      return new String(value, offset, length);
   }

   public static String toString(byte[] value) {
      return new String(value);
   }

   public static byte[] getBytes(char[] value) {
      return getBytes(value, 0, value.length);
   }

   public static byte[] getBytes(char[] c, String encoding) {
      return getBytes(c, 0, c.length, encoding);
   }

   public static byte[] getBytes(char[] value, int offset, int length) {
      return getBytes(value, offset, length, null);
   }

   public static byte[] getBytes(char[] value, int offset, int length, String encoding) {
      Charset cs;
      try {
         if (encoding == null) {
            cs = Charset.defaultCharset();
         } else {
            cs = Charset.forName(encoding);
         }
      } catch (UnsupportedCharsetException var8) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}), var8
         );
      }

      ByteBuffer buf = cs.encode(CharBuffer.wrap(value, offset, length));
      int encodedLen = buf.limit();
      byte[] asBytes = new byte[encodedLen];
      buf.get(asBytes, 0, encodedLen);
      return asBytes;
   }

   public static byte[] getBytes(String value) {
      return value.getBytes();
   }

   public static byte[] getBytes(String value, int offset, int length) {
      return value.substring(offset, offset + length).getBytes();
   }

   public static byte[] getBytes(String value, int offset, int length, String encoding) {
      if (encoding == null) {
         return getBytes(value, offset, length);
      } else {
         try {
            return value.substring(offset, offset + length).getBytes(encoding);
         } catch (UnsupportedEncodingException var5) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("StringUtils.0", new Object[]{encoding}), var5
            );
         }
      }
   }

   public static final boolean isValidIdChar(char c) {
      return "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@".indexOf(c) != -1;
   }

   public static void appendAsHex(StringBuilder builder, byte[] bytes) {
      builder.append("0x");

      for(byte b : bytes) {
         builder.append(HEX_DIGITS[b >>> 4 & 15]).append(HEX_DIGITS[b & 15]);
      }
   }

   public static void appendAsHex(StringBuilder builder, int value) {
      if (value == 0) {
         builder.append("0x0");
      } else {
         int shift = 32;
         boolean nonZeroFound = false;
         builder.append("0x");

         do {
            shift -= 4;
            byte nibble = (byte)(value >>> shift & 15);
            if (nonZeroFound) {
               builder.append(HEX_DIGITS[nibble]);
            } else if (nibble != 0) {
               builder.append(HEX_DIGITS[nibble]);
               nonZeroFound = true;
            }
         } while(shift != 0);
      }
   }

   public static byte[] getBytesNullTerminated(String value, String encoding) {
      Charset cs = Charset.forName(encoding);
      ByteBuffer buf = cs.encode(value);
      int encodedLen = buf.limit();
      byte[] asBytes = new byte[encodedLen + 1];
      buf.get(asBytes, 0, encodedLen);
      asBytes[encodedLen] = 0;
      return asBytes;
   }

   public static boolean canHandleAsServerPreparedStatementNoCache(String sql, ServerVersion serverVersion) {
      if (startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
         return false;
      } else {
         boolean canHandleAsStatement = true;
         if (startsWithIgnoreCaseAndWs(sql, "XA ")) {
            canHandleAsStatement = false;
         } else if (startsWithIgnoreCaseAndWs(sql, "CREATE TABLE")) {
            canHandleAsStatement = false;
         } else if (startsWithIgnoreCaseAndWs(sql, "DO")) {
            canHandleAsStatement = false;
         } else if (startsWithIgnoreCaseAndWs(sql, "SET")) {
            canHandleAsStatement = false;
         } else if (startsWithIgnoreCaseAndWs(sql, "SHOW WARNINGS") && serverVersion.meetsMinimum(ServerVersion.parseVersion("5.7.2"))) {
            canHandleAsStatement = false;
         } else if (sql.startsWith("/* ping */")) {
            canHandleAsStatement = false;
         }

         return canHandleAsStatement;
      }
   }

   public static String padString(String stringVal, int requiredLength) {
      int currentLength = stringVal.length();
      int difference = requiredLength - currentLength;
      if (difference > 0) {
         StringBuilder paddedBuf = new StringBuilder(requiredLength);
         paddedBuf.append(stringVal);
         paddedBuf.append(EMPTY_SPACE, 0, difference);
         return paddedBuf.toString();
      } else {
         return stringVal;
      }
   }

   public static int safeIntParse(String intAsString) {
      try {
         return Integer.parseInt(intAsString);
      } catch (NumberFormatException var2) {
         return 0;
      }
   }

   public static boolean isStrictlyNumeric(CharSequence cs) {
      if (cs != null && cs.length() != 0) {
         for(int i = 0; i < cs.length(); ++i) {
            if (!Character.isDigit(cs.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static String safeTrim(String toTrim) {
      return isNullOrEmpty(toTrim) ? toTrim : toTrim.trim();
   }

   public static String stringArrayToString(String[] elems, String prefix, String midDelimiter, String lastDelimiter, String suffix) {
      StringBuilder valuesString = new StringBuilder();
      if (elems.length > 1) {
         valuesString.append(Arrays.stream(elems).limit((long)(elems.length - 1)).collect(Collectors.joining(midDelimiter, prefix, lastDelimiter)));
      } else {
         valuesString.append(prefix);
      }

      valuesString.append(elems[elems.length - 1]).append(suffix);
      return valuesString.toString();
   }

   public static final void escapeblockFast(byte[] buf, ByteArrayOutputStream bytesOut, int size, boolean useAnsiMode) {
      int lastwritten = 0;

      for(int i = 0; i < size; ++i) {
         byte b = buf[i];
         if (b == 0) {
            if (i > lastwritten) {
               bytesOut.write(buf, lastwritten, i - lastwritten);
            }

            bytesOut.write(92);
            bytesOut.write(48);
            lastwritten = i + 1;
         } else if (b == 92 || b == 39 || !useAnsiMode && b == 34) {
            if (i > lastwritten) {
               bytesOut.write(buf, lastwritten, i - lastwritten);
            }

            bytesOut.write(92);
            lastwritten = i;
         }
      }

      if (lastwritten < size) {
         bytesOut.write(buf, lastwritten, size - lastwritten);
      }
   }

   public static boolean hasWildcards(String src) {
      return indexOfIgnoreCase(0, src, "%") > -1 || indexOfIgnoreCase(0, src, "_") > -1;
   }

   public static String getUniqueSavepointId() {
      String uuid = UUID.randomUUID().toString();
      return uuid.replaceAll("-", "_");
   }

   public static String joinWithSerialComma(List<?> elements) {
      if (elements != null && elements.size() != 0) {
         if (elements.size() == 1) {
            return elements.get(0).toString();
         } else {
            return elements.size() == 2
               ? elements.get(0) + " and " + elements.get(1)
               : (String)elements.subList(0, elements.size() - 1).stream().map(Object::toString).collect(Collectors.joining(", ", "", ", and "))
                  + elements.get(elements.size() - 1).toString();
         }
      } else {
         return "";
      }
   }

   static {
      for(int i = 0; i < EMPTY_SPACE.length; ++i) {
         EMPTY_SPACE[i] = ' ';
      }
   }

   public static enum SearchMode {
      ALLOW_BACKSLASH_ESCAPE,
      SKIP_BETWEEN_MARKERS,
      SKIP_BLOCK_COMMENTS,
      SKIP_LINE_COMMENTS,
      SKIP_WHITE_SPACE;
   }
}
