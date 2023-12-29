package org.apache.commons.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtils {
   public static final String EMPTY = "";
   public static final int INDEX_NOT_FOUND = -1;
   private static final int PAD_LIMIT = 8192;

   public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
   }

   public static boolean isNotEmpty(String str) {
      return !isEmpty(str);
   }

   public static boolean isBlank(String str) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         for(int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean isNotBlank(String str) {
      return !isBlank(str);
   }

   /** @deprecated */
   public static String clean(String str) {
      return str == null ? "" : str.trim();
   }

   public static String trim(String str) {
      return str == null ? null : str.trim();
   }

   public static String trimToNull(String str) {
      String ts = trim(str);
      return isEmpty(ts) ? null : ts;
   }

   public static String trimToEmpty(String str) {
      return str == null ? "" : str.trim();
   }

   public static String strip(String str) {
      return strip(str, null);
   }

   public static String stripToNull(String str) {
      if (str == null) {
         return null;
      } else {
         str = strip(str, null);
         return str.length() == 0 ? null : str;
      }
   }

   public static String stripToEmpty(String str) {
      return str == null ? "" : strip(str, null);
   }

   public static String strip(String str, String stripChars) {
      if (isEmpty(str)) {
         return str;
      } else {
         str = stripStart(str, stripChars);
         return stripEnd(str, stripChars);
      }
   }

   public static String stripStart(String str, String stripChars) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         int start = 0;
         if (stripChars == null) {
            while(start != strLen && Character.isWhitespace(str.charAt(start))) {
               ++start;
            }
         } else {
            if (stripChars.length() == 0) {
               return str;
            }

            while(start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
               ++start;
            }
         }

         return str.substring(start);
      } else {
         return str;
      }
   }

   public static String stripEnd(String str, String stripChars) {
      int end;
      if (str != null && (end = str.length()) != 0) {
         if (stripChars == null) {
            while(end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
               --end;
            }
         } else {
            if (stripChars.length() == 0) {
               return str;
            }

            while(end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
               --end;
            }
         }

         return str.substring(0, end);
      } else {
         return str;
      }
   }

   public static String[] stripAll(String[] strs) {
      return stripAll(strs, null);
   }

   public static String[] stripAll(String[] strs, String stripChars) {
      int strsLen;
      if (strs != null && (strsLen = strs.length) != 0) {
         String[] newArr = new String[strsLen];

         for(int i = 0; i < strsLen; ++i) {
            newArr[i] = strip(strs[i], stripChars);
         }

         return newArr;
      } else {
         return strs;
      }
   }

   public static boolean equals(String str1, String str2) {
      return str1 == null ? str2 == null : str1.equals(str2);
   }

   public static boolean equalsIgnoreCase(String str1, String str2) {
      return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
   }

   public static int indexOf(String str, char searchChar) {
      return isEmpty(str) ? -1 : str.indexOf(searchChar);
   }

   public static int indexOf(String str, char searchChar, int startPos) {
      return isEmpty(str) ? -1 : str.indexOf(searchChar, startPos);
   }

   public static int indexOf(String str, String searchStr) {
      return str != null && searchStr != null ? str.indexOf(searchStr) : -1;
   }

   public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
      if (str == null || searchStr == null || ordinal <= 0) {
         return -1;
      } else if (searchStr.length() == 0) {
         return 0;
      } else {
         int found = 0;
         int index = -1;

         do {
            index = str.indexOf(searchStr, index + 1);
            if (index < 0) {
               return index;
            }
         } while(++found < ordinal);

         return index;
      }
   }

   public static int indexOf(String str, String searchStr, int startPos) {
      if (str == null || searchStr == null) {
         return -1;
      } else {
         return searchStr.length() == 0 && startPos >= str.length() ? str.length() : str.indexOf(searchStr, startPos);
      }
   }

   public static int lastIndexOf(String str, char searchChar) {
      return isEmpty(str) ? -1 : str.lastIndexOf(searchChar);
   }

   public static int lastIndexOf(String str, char searchChar, int startPos) {
      return isEmpty(str) ? -1 : str.lastIndexOf(searchChar, startPos);
   }

   public static int lastIndexOf(String str, String searchStr) {
      return str != null && searchStr != null ? str.lastIndexOf(searchStr) : -1;
   }

   public static int lastIndexOf(String str, String searchStr, int startPos) {
      return str != null && searchStr != null ? str.lastIndexOf(searchStr, startPos) : -1;
   }

   public static boolean contains(String str, char searchChar) {
      if (isEmpty(str)) {
         return false;
      } else {
         return str.indexOf(searchChar) >= 0;
      }
   }

   public static boolean contains(String str, String searchStr) {
      if (str != null && searchStr != null) {
         return str.indexOf(searchStr) >= 0;
      } else {
         return false;
      }
   }

   public static boolean containsIgnoreCase(String str, String searchStr) {
      return str != null && searchStr != null ? contains(str.toUpperCase(), searchStr.toUpperCase()) : false;
   }

   public static int indexOfAny(String str, char[] searchChars) {
      if (!isEmpty(str) && !ArrayUtils.isEmpty(searchChars)) {
         for(int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);

            for(int j = 0; j < searchChars.length; ++j) {
               if (searchChars[j] == ch) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int indexOfAny(String str, String searchChars) {
      return !isEmpty(str) && !isEmpty(searchChars) ? indexOfAny(str, searchChars.toCharArray()) : -1;
   }

   public static boolean containsAny(String str, char[] searchChars) {
      if (str != null && str.length() != 0 && searchChars != null && searchChars.length != 0) {
         for(int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);

            for(int j = 0; j < searchChars.length; ++j) {
               if (searchChars[j] == ch) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean containsAny(String str, String searchChars) {
      return searchChars == null ? false : containsAny(str, searchChars.toCharArray());
   }

   public static int indexOfAnyBut(String str, char[] searchChars) {
      if (!isEmpty(str) && !ArrayUtils.isEmpty(searchChars)) {
         label27:
         for(int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);

            for(int j = 0; j < searchChars.length; ++j) {
               if (searchChars[j] == ch) {
                  continue label27;
               }
            }

            return i;
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int indexOfAnyBut(String str, String searchChars) {
      if (!isEmpty(str) && !isEmpty(searchChars)) {
         for(int i = 0; i < str.length(); ++i) {
            if (searchChars.indexOf(str.charAt(i)) < 0) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static boolean containsOnly(String str, char[] valid) {
      if (valid == null || str == null) {
         return false;
      } else if (str.length() == 0) {
         return true;
      } else if (valid.length == 0) {
         return false;
      } else {
         return indexOfAnyBut(str, valid) == -1;
      }
   }

   public static boolean containsOnly(String str, String validChars) {
      return str != null && validChars != null ? containsOnly(str, validChars.toCharArray()) : false;
   }

   public static boolean containsNone(String str, char[] invalidChars) {
      if (str != null && invalidChars != null) {
         int strSize = str.length();
         int validSize = invalidChars.length;

         for(int i = 0; i < strSize; ++i) {
            char ch = str.charAt(i);

            for(int j = 0; j < validSize; ++j) {
               if (invalidChars[j] == ch) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static boolean containsNone(String str, String invalidChars) {
      return str != null && invalidChars != null ? containsNone(str, invalidChars.toCharArray()) : true;
   }

   public static int indexOfAny(String str, String[] searchStrs) {
      if (str != null && searchStrs != null) {
         int sz = searchStrs.length;
         int ret = Integer.MAX_VALUE;
         int tmp = 0;

         for(int i = 0; i < sz; ++i) {
            String search = searchStrs[i];
            if (search != null) {
               tmp = str.indexOf(search);
               if (tmp != -1 && tmp < ret) {
                  ret = tmp;
               }
            }
         }

         return ret == Integer.MAX_VALUE ? -1 : ret;
      } else {
         return -1;
      }
   }

   public static int lastIndexOfAny(String str, String[] searchStrs) {
      if (str != null && searchStrs != null) {
         int sz = searchStrs.length;
         int ret = -1;
         int tmp = 0;

         for(int i = 0; i < sz; ++i) {
            String search = searchStrs[i];
            if (search != null) {
               tmp = str.lastIndexOf(search);
               if (tmp > ret) {
                  ret = tmp;
               }
            }
         }

         return ret;
      } else {
         return -1;
      }
   }

   public static String substring(String str, int start) {
      if (str == null) {
         return null;
      } else {
         if (start < 0) {
            start += str.length();
         }

         if (start < 0) {
            start = 0;
         }

         return start > str.length() ? "" : str.substring(start);
      }
   }

   public static String substring(String str, int start, int end) {
      if (str == null) {
         return null;
      } else {
         if (end < 0) {
            end += str.length();
         }

         if (start < 0) {
            start += str.length();
         }

         if (end > str.length()) {
            end = str.length();
         }

         if (start > end) {
            return "";
         } else {
            if (start < 0) {
               start = 0;
            }

            if (end < 0) {
               end = 0;
            }

            return str.substring(start, end);
         }
      }
   }

   public static String left(String str, int len) {
      if (str == null) {
         return null;
      } else if (len < 0) {
         return "";
      } else {
         return str.length() <= len ? str : str.substring(0, len);
      }
   }

   public static String right(String str, int len) {
      if (str == null) {
         return null;
      } else if (len < 0) {
         return "";
      } else {
         return str.length() <= len ? str : str.substring(str.length() - len);
      }
   }

   public static String mid(String str, int pos, int len) {
      if (str == null) {
         return null;
      } else if (len >= 0 && pos <= str.length()) {
         if (pos < 0) {
            pos = 0;
         }

         return str.length() <= pos + len ? str.substring(pos) : str.substring(pos, pos + len);
      } else {
         return "";
      }
   }

   public static String substringBefore(String str, String separator) {
      if (isEmpty(str) || separator == null) {
         return str;
      } else if (separator.length() == 0) {
         return "";
      } else {
         int pos = str.indexOf(separator);
         return pos == -1 ? str : str.substring(0, pos);
      }
   }

   public static String substringAfter(String str, String separator) {
      if (isEmpty(str)) {
         return str;
      } else if (separator == null) {
         return "";
      } else {
         int pos = str.indexOf(separator);
         return pos == -1 ? "" : str.substring(pos + separator.length());
      }
   }

   public static String substringBeforeLast(String str, String separator) {
      if (!isEmpty(str) && !isEmpty(separator)) {
         int pos = str.lastIndexOf(separator);
         return pos == -1 ? str : str.substring(0, pos);
      } else {
         return str;
      }
   }

   public static String substringAfterLast(String str, String separator) {
      if (isEmpty(str)) {
         return str;
      } else if (isEmpty(separator)) {
         return "";
      } else {
         int pos = str.lastIndexOf(separator);
         return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
      }
   }

   public static String substringBetween(String str, String tag) {
      return substringBetween(str, tag, tag);
   }

   public static String substringBetween(String str, String open, String close) {
      if (str != null && open != null && close != null) {
         int start = str.indexOf(open);
         if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
               return str.substring(start + open.length(), end);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static String[] substringsBetween(String str, String open, String close) {
      if (str != null && !isEmpty(open) && !isEmpty(close)) {
         int strLen = str.length();
         if (strLen == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
         } else {
            int closeLen = close.length();
            int openLen = open.length();
            List list = new ArrayList();

            int end;
            for(int pos = 0; pos < strLen - closeLen; pos = end + closeLen) {
               int start = str.indexOf(open, pos);
               if (start < 0) {
                  break;
               }

               start += openLen;
               end = str.indexOf(close, start);
               if (end < 0) {
                  break;
               }

               list.add(str.substring(start, end));
            }

            return list.isEmpty() ? null : list.toArray(new String[list.size()]);
         }
      } else {
         return null;
      }
   }

   /** @deprecated */
   public static String getNestedString(String str, String tag) {
      return substringBetween(str, tag, tag);
   }

   /** @deprecated */
   public static String getNestedString(String str, String open, String close) {
      return substringBetween(str, open, close);
   }

   public static String[] split(String str) {
      return split(str, null, -1);
   }

   public static String[] split(String str, char separatorChar) {
      return splitWorker(str, separatorChar, false);
   }

   public static String[] split(String str, String separatorChars) {
      return splitWorker(str, separatorChars, -1, false);
   }

   public static String[] split(String str, String separatorChars, int max) {
      return splitWorker(str, separatorChars, max, false);
   }

   public static String[] splitByWholeSeparator(String str, String separator) {
      return splitByWholeSeparatorWorker(str, separator, -1, false);
   }

   public static String[] splitByWholeSeparator(String str, String separator, int max) {
      return splitByWholeSeparatorWorker(str, separator, max, false);
   }

   public static String[] splitByWholeSeparatorPreserveAllTokens(String str, String separator) {
      return splitByWholeSeparatorWorker(str, separator, -1, true);
   }

   public static String[] splitByWholeSeparatorPreserveAllTokens(String str, String separator, int max) {
      return splitByWholeSeparatorWorker(str, separator, max, true);
   }

   private static String[] splitByWholeSeparatorWorker(String str, String separator, int max, boolean preserveAllTokens) {
      if (str == null) {
         return null;
      } else {
         int len = str.length();
         if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
         } else if (separator != null && !"".equals(separator)) {
            int separatorLength = separator.length();
            ArrayList substrings = new ArrayList();
            int numberOfSubstrings = 0;
            int beg = 0;
            int end = 0;

            while(end < len) {
               end = str.indexOf(separator, beg);
               if (end > -1) {
                  if (end > beg) {
                     if (++numberOfSubstrings == max) {
                        end = len;
                        substrings.add(str.substring(beg));
                     } else {
                        substrings.add(str.substring(beg, end));
                        beg = end + separatorLength;
                     }
                  } else {
                     if (preserveAllTokens) {
                        if (++numberOfSubstrings == max) {
                           end = len;
                           substrings.add(str.substring(beg));
                        } else {
                           substrings.add("");
                        }
                     }

                     beg = end + separatorLength;
                  }
               } else {
                  substrings.add(str.substring(beg));
                  end = len;
               }
            }

            return substrings.toArray(new String[substrings.size()]);
         } else {
            return splitWorker(str, null, max, preserveAllTokens);
         }
      }
   }

   public static String[] splitPreserveAllTokens(String str) {
      return splitWorker(str, null, -1, true);
   }

   public static String[] splitPreserveAllTokens(String str, char separatorChar) {
      return splitWorker(str, separatorChar, true);
   }

   private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
      if (str == null) {
         return null;
      } else {
         int len = str.length();
         if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
         } else {
            List list = new ArrayList();
            int i = 0;
            int start = 0;
            boolean match = false;
            boolean lastMatch = false;

            while(i < len) {
               if (str.charAt(i) == separatorChar) {
                  if (match || preserveAllTokens) {
                     list.add(str.substring(start, i));
                     match = false;
                     lastMatch = true;
                  }

                  start = ++i;
               } else {
                  lastMatch = false;
                  match = true;
                  ++i;
               }
            }

            if (match || preserveAllTokens && lastMatch) {
               list.add(str.substring(start, i));
            }

            return list.toArray(new String[list.size()]);
         }
      }
   }

   public static String[] splitPreserveAllTokens(String str, String separatorChars) {
      return splitWorker(str, separatorChars, -1, true);
   }

   public static String[] splitPreserveAllTokens(String str, String separatorChars, int max) {
      return splitWorker(str, separatorChars, max, true);
   }

   private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
      if (str == null) {
         return null;
      } else {
         int len = str.length();
         if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
         } else {
            List list = new ArrayList();
            int sizePlus1 = 1;
            int i = 0;
            int start = 0;
            boolean match = false;
            boolean lastMatch = false;
            if (separatorChars == null) {
               while(i < len) {
                  if (Character.isWhitespace(str.charAt(i))) {
                     if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                           i = len;
                           lastMatch = false;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                     }

                     start = ++i;
                  } else {
                     lastMatch = false;
                     match = true;
                     ++i;
                  }
               }
            } else if (separatorChars.length() == 1) {
               char sep = separatorChars.charAt(0);

               while(i < len) {
                  if (str.charAt(i) == sep) {
                     if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                           i = len;
                           lastMatch = false;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                     }

                     start = ++i;
                  } else {
                     lastMatch = false;
                     match = true;
                     ++i;
                  }
               }
            } else {
               while(i < len) {
                  if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                     if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                           i = len;
                           lastMatch = false;
                        }

                        list.add(str.substring(start, i));
                        match = false;
                     }

                     start = ++i;
                  } else {
                     lastMatch = false;
                     match = true;
                     ++i;
                  }
               }
            }

            if (match || preserveAllTokens && lastMatch) {
               list.add(str.substring(start, i));
            }

            return list.toArray(new String[list.size()]);
         }
      }
   }

   public static String[] splitByCharacterType(String str) {
      return splitByCharacterType(str, false);
   }

   public static String[] splitByCharacterTypeCamelCase(String str) {
      return splitByCharacterType(str, true);
   }

   private static String[] splitByCharacterType(String str, boolean camelCase) {
      if (str == null) {
         return null;
      } else if (str.length() == 0) {
         return ArrayUtils.EMPTY_STRING_ARRAY;
      } else {
         char[] c = str.toCharArray();
         List list = new ArrayList();
         int tokenStart = 0;
         int currentType = Character.getType(c[tokenStart]);

         for(int pos = tokenStart + 1; pos < c.length; ++pos) {
            int type = Character.getType(c[pos]);
            if (type != currentType) {
               if (camelCase && type == 2 && currentType == 1) {
                  int newTokenStart = pos - 1;
                  if (newTokenStart != tokenStart) {
                     list.add(new String(c, tokenStart, newTokenStart - tokenStart));
                     tokenStart = newTokenStart;
                  }
               } else {
                  list.add(new String(c, tokenStart, pos - tokenStart));
                  tokenStart = pos;
               }

               currentType = type;
            }
         }

         list.add(new String(c, tokenStart, c.length - tokenStart));
         return list.toArray(new String[list.size()]);
      }
   }

   /** @deprecated */
   public static String concatenate(Object[] array) {
      return join(array, null);
   }

   public static String join(Object[] array) {
      return join(array, null);
   }

   public static String join(Object[] array, char separator) {
      return array == null ? null : join(array, separator, 0, array.length);
   }

   public static String join(Object[] array, char separator, int startIndex, int endIndex) {
      if (array == null) {
         return null;
      } else {
         int bufSize = endIndex - startIndex;
         if (bufSize <= 0) {
            return "";
         } else {
            bufSize *= (array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1;
            StringBuffer buf = new StringBuffer(bufSize);

            for(int i = startIndex; i < endIndex; ++i) {
               if (i > startIndex) {
                  buf.append(separator);
               }

               if (array[i] != null) {
                  buf.append(array[i]);
               }
            }

            return buf.toString();
         }
      }
   }

   public static String join(Object[] array, String separator) {
      return array == null ? null : join(array, separator, 0, array.length);
   }

   public static String join(Object[] array, String separator, int startIndex, int endIndex) {
      if (array == null) {
         return null;
      } else {
         if (separator == null) {
            separator = "";
         }

         int bufSize = endIndex - startIndex;
         if (bufSize <= 0) {
            return "";
         } else {
            bufSize *= (array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length();
            StringBuffer buf = new StringBuffer(bufSize);

            for(int i = startIndex; i < endIndex; ++i) {
               if (i > startIndex) {
                  buf.append(separator);
               }

               if (array[i] != null) {
                  buf.append(array[i]);
               }
            }

            return buf.toString();
         }
      }
   }

   public static String join(Iterator iterator, char separator) {
      if (iterator == null) {
         return null;
      } else if (!iterator.hasNext()) {
         return "";
      } else {
         Object first = iterator.next();
         if (!iterator.hasNext()) {
            return ObjectUtils.toString(first);
         } else {
            StringBuffer buf = new StringBuffer(256);
            if (first != null) {
               buf.append(first);
            }

            while(iterator.hasNext()) {
               buf.append(separator);
               Object obj = iterator.next();
               if (obj != null) {
                  buf.append(obj);
               }
            }

            return buf.toString();
         }
      }
   }

   public static String join(Iterator iterator, String separator) {
      if (iterator == null) {
         return null;
      } else if (!iterator.hasNext()) {
         return "";
      } else {
         Object first = iterator.next();
         if (!iterator.hasNext()) {
            return ObjectUtils.toString(first);
         } else {
            StringBuffer buf = new StringBuffer(256);
            if (first != null) {
               buf.append(first);
            }

            while(iterator.hasNext()) {
               if (separator != null) {
                  buf.append(separator);
               }

               Object obj = iterator.next();
               if (obj != null) {
                  buf.append(obj);
               }
            }

            return buf.toString();
         }
      }
   }

   public static String join(Collection collection, char separator) {
      return collection == null ? null : join(collection.iterator(), separator);
   }

   public static String join(Collection collection, String separator) {
      return collection == null ? null : join(collection.iterator(), separator);
   }

   /** @deprecated */
   public static String deleteSpaces(String str) {
      return str == null ? null : CharSetUtils.delete(str, " \t\r\n\b");
   }

   public static String deleteWhitespace(String str) {
      if (isEmpty(str)) {
         return str;
      } else {
         int sz = str.length();
         char[] chs = new char[sz];
         int count = 0;

         for(int i = 0; i < sz; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               chs[count++] = str.charAt(i);
            }
         }

         return count == sz ? str : new String(chs, 0, count);
      }
   }

   public static String removeStart(String str, String remove) {
      if (isEmpty(str) || isEmpty(remove)) {
         return str;
      } else {
         return str.startsWith(remove) ? str.substring(remove.length()) : str;
      }
   }

   public static String removeStartIgnoreCase(String str, String remove) {
      if (isEmpty(str) || isEmpty(remove)) {
         return str;
      } else {
         return startsWithIgnoreCase(str, remove) ? str.substring(remove.length()) : str;
      }
   }

   public static String removeEnd(String str, String remove) {
      if (isEmpty(str) || isEmpty(remove)) {
         return str;
      } else {
         return str.endsWith(remove) ? str.substring(0, str.length() - remove.length()) : str;
      }
   }

   public static String removeEndIgnoreCase(String str, String remove) {
      if (isEmpty(str) || isEmpty(remove)) {
         return str;
      } else {
         return endsWithIgnoreCase(str, remove) ? str.substring(0, str.length() - remove.length()) : str;
      }
   }

   public static String remove(String str, String remove) {
      return !isEmpty(str) && !isEmpty(remove) ? replace(str, remove, "", -1) : str;
   }

   public static String remove(String str, char remove) {
      if (!isEmpty(str) && str.indexOf(remove) != -1) {
         char[] chars = str.toCharArray();
         int pos = 0;

         for(int i = 0; i < chars.length; ++i) {
            if (chars[i] != remove) {
               chars[pos++] = chars[i];
            }
         }

         return new String(chars, 0, pos);
      } else {
         return str;
      }
   }

   public static String replaceOnce(String text, String searchString, String replacement) {
      return replace(text, searchString, replacement, 1);
   }

   public static String replace(String text, String searchString, String replacement) {
      return replace(text, searchString, replacement, -1);
   }

   public static String replace(String text, String searchString, String replacement, int max) {
      if (!isEmpty(text) && !isEmpty(searchString) && replacement != null && max != 0) {
         int start = 0;
         int end = text.indexOf(searchString, start);
         if (end == -1) {
            return text;
         } else {
            int replLength = searchString.length();
            int increase = replacement.length() - replLength;
            increase = increase < 0 ? 0 : increase;
            increase *= max < 0 ? 16 : (max > 64 ? 64 : max);

            StringBuffer buf;
            for(buf = new StringBuffer(text.length() + increase); end != -1; end = text.indexOf(searchString, start)) {
               buf.append(text.substring(start, end)).append(replacement);
               start = end + replLength;
               if (--max == 0) {
                  break;
               }
            }

            buf.append(text.substring(start));
            return buf.toString();
         }
      } else {
         return text;
      }
   }

   public static String replaceEach(String text, String[] searchList, String[] replacementList) {
      return replaceEach(text, searchList, replacementList, false, 0);
   }

   public static String replaceEachRepeatedly(String text, String[] searchList, String[] replacementList) {
      int timeToLive = searchList == null ? 0 : searchList.length;
      return replaceEach(text, searchList, replacementList, true, timeToLive);
   }

   private static String replaceEach(String text, String[] searchList, String[] replacementList, boolean repeat, int timeToLive) {
      if (text != null && text.length() != 0 && searchList != null && searchList.length != 0 && replacementList != null && replacementList.length != 0) {
         if (timeToLive < 0) {
            throw new IllegalStateException("TimeToLive of " + timeToLive + " is less than 0: " + text);
         } else {
            int searchLength = searchList.length;
            int replacementLength = replacementList.length;
            if (searchLength != replacementLength) {
               throw new IllegalArgumentException("Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength);
            } else {
               boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
               int textIndex = -1;
               int replaceIndex = -1;
               int tempIndex = -1;

               for(int i = 0; i < searchLength; ++i) {
                  if (!noMoreMatchesForReplIndex[i] && searchList[i] != null && searchList[i].length() != 0 && replacementList[i] != null) {
                     tempIndex = text.indexOf(searchList[i]);
                     if (tempIndex == -1) {
                        noMoreMatchesForReplIndex[i] = true;
                     } else if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                     }
                  }
               }

               if (textIndex == -1) {
                  return text;
               } else {
                  int var19 = 0;
                  int increase = 0;

                  for(int i = 0; i < searchList.length; ++i) {
                     int greater = replacementList[i].length() - searchList[i].length();
                     if (greater > 0) {
                        increase += 3 * greater;
                     }
                  }

                  increase = Math.min(increase, text.length() / 5);
                  StringBuffer buf = new StringBuffer(text.length() + increase);

                  while(textIndex != -1) {
                     for(int i = var19; i < textIndex; ++i) {
                        buf.append(text.charAt(i));
                     }

                     buf.append(replacementList[replaceIndex]);
                     var19 = textIndex + searchList[replaceIndex].length();
                     textIndex = -1;
                     replaceIndex = -1;
                     tempIndex = -1;

                     for(int i = 0; i < searchLength; ++i) {
                        if (!noMoreMatchesForReplIndex[i] && searchList[i] != null && searchList[i].length() != 0 && replacementList[i] != null) {
                           tempIndex = text.indexOf(searchList[i], var19);
                           if (tempIndex == -1) {
                              noMoreMatchesForReplIndex[i] = true;
                           } else if (textIndex == -1 || tempIndex < textIndex) {
                              textIndex = tempIndex;
                              replaceIndex = i;
                           }
                        }
                     }
                  }

                  int textLength = text.length();

                  for(int i = var19; i < textLength; ++i) {
                     buf.append(text.charAt(i));
                  }

                  String result = buf.toString();
                  return !repeat ? result : replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
               }
            }
         }
      } else {
         return text;
      }
   }

   public static String replaceChars(String str, char searchChar, char replaceChar) {
      return str == null ? null : str.replace(searchChar, replaceChar);
   }

   public static String replaceChars(String str, String searchChars, String replaceChars) {
      if (!isEmpty(str) && !isEmpty(searchChars)) {
         if (replaceChars == null) {
            replaceChars = "";
         }

         boolean modified = false;
         int replaceCharsLength = replaceChars.length();
         int strLength = str.length();
         StringBuffer buf = new StringBuffer(strLength);

         for(int i = 0; i < strLength; ++i) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if (index >= 0) {
               modified = true;
               if (index < replaceCharsLength) {
                  buf.append(replaceChars.charAt(index));
               }
            } else {
               buf.append(ch);
            }
         }

         return modified ? buf.toString() : str;
      } else {
         return str;
      }
   }

   /** @deprecated */
   public static String overlayString(String text, String overlay, int start, int end) {
      return new StringBuffer(start + overlay.length() + text.length() - end + 1)
         .append(text.substring(0, start))
         .append(overlay)
         .append(text.substring(end))
         .toString();
   }

   public static String overlay(String str, String overlay, int start, int end) {
      if (str == null) {
         return null;
      } else {
         if (overlay == null) {
            overlay = "";
         }

         int len = str.length();
         if (start < 0) {
            start = 0;
         }

         if (start > len) {
            start = len;
         }

         if (end < 0) {
            end = 0;
         }

         if (end > len) {
            end = len;
         }

         if (start > end) {
            int temp = start;
            start = end;
            end = temp;
         }

         return new StringBuffer(len + start - end + overlay.length() + 1)
            .append(str.substring(0, start))
            .append(overlay)
            .append(str.substring(end))
            .toString();
      }
   }

   public static String chomp(String str) {
      if (isEmpty(str)) {
         return str;
      } else if (str.length() == 1) {
         char ch = str.charAt(0);
         return ch != '\r' && ch != '\n' ? str : "";
      } else {
         int lastIdx = str.length() - 1;
         char last = str.charAt(lastIdx);
         if (last == '\n') {
            if (str.charAt(lastIdx - 1) == '\r') {
               --lastIdx;
            }
         } else if (last != '\r') {
            ++lastIdx;
         }

         return str.substring(0, lastIdx);
      }
   }

   public static String chomp(String str, String separator) {
      if (isEmpty(str) || separator == null) {
         return str;
      } else {
         return str.endsWith(separator) ? str.substring(0, str.length() - separator.length()) : str;
      }
   }

   /** @deprecated */
   public static String chompLast(String str) {
      return chompLast(str, "\n");
   }

   /** @deprecated */
   public static String chompLast(String str, String sep) {
      if (str.length() == 0) {
         return str;
      } else {
         String sub = str.substring(str.length() - sep.length());
         return sep.equals(sub) ? str.substring(0, str.length() - sep.length()) : str;
      }
   }

   /** @deprecated */
   public static String getChomp(String str, String sep) {
      int idx = str.lastIndexOf(sep);
      if (idx == str.length() - sep.length()) {
         return sep;
      } else {
         return idx != -1 ? str.substring(idx) : "";
      }
   }

   /** @deprecated */
   public static String prechomp(String str, String sep) {
      int idx = str.indexOf(sep);
      return idx == -1 ? str : str.substring(idx + sep.length());
   }

   /** @deprecated */
   public static String getPrechomp(String str, String sep) {
      int idx = str.indexOf(sep);
      return idx == -1 ? "" : str.substring(0, idx + sep.length());
   }

   public static String chop(String str) {
      if (str == null) {
         return null;
      } else {
         int strLen = str.length();
         if (strLen < 2) {
            return "";
         } else {
            int lastIdx = strLen - 1;
            String ret = str.substring(0, lastIdx);
            char last = str.charAt(lastIdx);
            return last == '\n' && ret.charAt(lastIdx - 1) == '\r' ? ret.substring(0, lastIdx - 1) : ret;
         }
      }
   }

   /** @deprecated */
   public static String chopNewline(String str) {
      int lastIdx = str.length() - 1;
      if (lastIdx <= 0) {
         return "";
      } else {
         char last = str.charAt(lastIdx);
         if (last == '\n') {
            if (str.charAt(lastIdx - 1) == '\r') {
               --lastIdx;
            }
         } else {
            ++lastIdx;
         }

         return str.substring(0, lastIdx);
      }
   }

   /** @deprecated */
   public static String escape(String str) {
      return StringEscapeUtils.escapeJava(str);
   }

   public static String repeat(String str, int repeat) {
      if (str == null) {
         return null;
      } else if (repeat <= 0) {
         return "";
      } else {
         int inputLength = str.length();
         if (repeat == 1 || inputLength == 0) {
            return str;
         } else if (inputLength == 1 && repeat <= 8192) {
            return padding(repeat, str.charAt(0));
         } else {
            int outputLength = inputLength * repeat;
            switch(inputLength) {
               case 1:
                  char ch = str.charAt(0);
                  char[] output1 = new char[outputLength];

                  for(int i = repeat - 1; i >= 0; --i) {
                     output1[i] = ch;
                  }

                  return new String(output1);
               case 2:
                  char ch0 = str.charAt(0);
                  char ch1 = str.charAt(1);
                  char[] output2 = new char[outputLength];

                  for(int i = repeat * 2 - 2; i >= 0; --i) {
                     output2[i] = ch0;
                     output2[i + 1] = ch1;
                     --i;
                  }

                  return new String(output2);
               default:
                  StringBuffer buf = new StringBuffer(outputLength);

                  for(int i = 0; i < repeat; ++i) {
                     buf.append(str);
                  }

                  return buf.toString();
            }
         }
      }
   }

   private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
      if (repeat < 0) {
         throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
      } else {
         char[] buf = new char[repeat];

         for(int i = 0; i < buf.length; ++i) {
            buf[i] = padChar;
         }

         return new String(buf);
      }
   }

   public static String rightPad(String str, int size) {
      return rightPad(str, size, ' ');
   }

   public static String rightPad(String str, int size, char padChar) {
      if (str == null) {
         return null;
      } else {
         int pads = size - str.length();
         if (pads <= 0) {
            return str;
         } else {
            return pads > 8192 ? rightPad(str, size, String.valueOf(padChar)) : str.concat(padding(pads, padChar));
         }
      }
   }

   public static String rightPad(String str, int size, String padStr) {
      if (str == null) {
         return null;
      } else {
         if (isEmpty(padStr)) {
            padStr = " ";
         }

         int padLen = padStr.length();
         int strLen = str.length();
         int pads = size - strLen;
         if (pads <= 0) {
            return str;
         } else if (padLen == 1 && pads <= 8192) {
            return rightPad(str, size, padStr.charAt(0));
         } else if (pads == padLen) {
            return str.concat(padStr);
         } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
         } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();

            for(int i = 0; i < pads; ++i) {
               padding[i] = padChars[i % padLen];
            }

            return str.concat(new String(padding));
         }
      }
   }

   public static String leftPad(String str, int size) {
      return leftPad(str, size, ' ');
   }

   public static String leftPad(String str, int size, char padChar) {
      if (str == null) {
         return null;
      } else {
         int pads = size - str.length();
         if (pads <= 0) {
            return str;
         } else {
            return pads > 8192 ? leftPad(str, size, String.valueOf(padChar)) : padding(pads, padChar).concat(str);
         }
      }
   }

   public static String leftPad(String str, int size, String padStr) {
      if (str == null) {
         return null;
      } else {
         if (isEmpty(padStr)) {
            padStr = " ";
         }

         int padLen = padStr.length();
         int strLen = str.length();
         int pads = size - strLen;
         if (pads <= 0) {
            return str;
         } else if (padLen == 1 && pads <= 8192) {
            return leftPad(str, size, padStr.charAt(0));
         } else if (pads == padLen) {
            return padStr.concat(str);
         } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
         } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();

            for(int i = 0; i < pads; ++i) {
               padding[i] = padChars[i % padLen];
            }

            return new String(padding).concat(str);
         }
      }
   }

   public static int length(String str) {
      return str == null ? 0 : str.length();
   }

   public static String center(String str, int size) {
      return center(str, size, ' ');
   }

   public static String center(String str, int size, char padChar) {
      if (str != null && size > 0) {
         int strLen = str.length();
         int pads = size - strLen;
         if (pads <= 0) {
            return str;
         } else {
            str = leftPad(str, strLen + pads / 2, padChar);
            return rightPad(str, size, padChar);
         }
      } else {
         return str;
      }
   }

   public static String center(String str, int size, String padStr) {
      if (str != null && size > 0) {
         if (isEmpty(padStr)) {
            padStr = " ";
         }

         int strLen = str.length();
         int pads = size - strLen;
         if (pads <= 0) {
            return str;
         } else {
            str = leftPad(str, strLen + pads / 2, padStr);
            return rightPad(str, size, padStr);
         }
      } else {
         return str;
      }
   }

   public static String upperCase(String str) {
      return str == null ? null : str.toUpperCase();
   }

   public static String lowerCase(String str) {
      return str == null ? null : str.toLowerCase();
   }

   public static String capitalize(String str) {
      int strLen;
      return str != null && (strLen = str.length()) != 0
         ? new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString()
         : str;
   }

   /** @deprecated */
   public static String capitalise(String str) {
      return capitalize(str);
   }

   public static String uncapitalize(String str) {
      int strLen;
      return str != null && (strLen = str.length()) != 0
         ? new StringBuffer(strLen).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString()
         : str;
   }

   /** @deprecated */
   public static String uncapitalise(String str) {
      return uncapitalize(str);
   }

   public static String swapCase(String str) {
      int strLen;
      if (str != null && (strLen = str.length()) != 0) {
         StringBuffer buffer = new StringBuffer(strLen);
         char ch = '\u0000';

         for(int i = 0; i < strLen; ++i) {
            ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
               ch = Character.toLowerCase(ch);
            } else if (Character.isTitleCase(ch)) {
               ch = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
               ch = Character.toUpperCase(ch);
            }

            buffer.append(ch);
         }

         return buffer.toString();
      } else {
         return str;
      }
   }

   /** @deprecated */
   public static String capitaliseAllWords(String str) {
      return WordUtils.capitalize(str);
   }

   public static int countMatches(String str, String sub) {
      if (!isEmpty(str) && !isEmpty(sub)) {
         int count = 0;

         int idx;
         for(idx = 0; (idx = str.indexOf(sub, idx)) != true; idx += sub.length()) {
            ++count;
         }

         return count;
      } else {
         return 0;
      }
   }

   public static boolean isAlpha(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isLetter(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isAlphaSpace(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isLetter(str.charAt(i)) && str.charAt(i) != ' ') {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isAlphanumeric(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isLetterOrDigit(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isAlphanumericSpace(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isLetterOrDigit(str.charAt(i)) && str.charAt(i) != ' ') {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isAsciiPrintable(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!CharUtils.isAsciiPrintable(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isNumeric(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isDigit(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isNumericSpace(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isDigit(str.charAt(i)) && str.charAt(i) != ' ') {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isWhitespace(String str) {
      if (str == null) {
         return false;
      } else {
         int sz = str.length();

         for(int i = 0; i < sz; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static String defaultString(String str) {
      return str == null ? "" : str;
   }

   public static String defaultString(String str, String defaultStr) {
      return str == null ? defaultStr : str;
   }

   public static String defaultIfEmpty(String str, String defaultStr) {
      return isEmpty(str) ? defaultStr : str;
   }

   public static String reverse(String str) {
      return str == null ? null : new StringBuffer(str).reverse().toString();
   }

   public static String reverseDelimited(String str, char separatorChar) {
      if (str == null) {
         return null;
      } else {
         String[] strs = split(str, separatorChar);
         ArrayUtils.reverse((Object[])strs);
         return join(strs, separatorChar);
      }
   }

   /** @deprecated */
   public static String reverseDelimitedString(String str, String separatorChars) {
      if (str == null) {
         return null;
      } else {
         String[] strs = split(str, separatorChars);
         ArrayUtils.reverse((Object[])strs);
         return separatorChars == null ? join(strs, ' ') : join(strs, separatorChars);
      }
   }

   public static String abbreviate(String str, int maxWidth) {
      return abbreviate(str, 0, maxWidth);
   }

   public static String abbreviate(String str, int offset, int maxWidth) {
      if (str == null) {
         return null;
      } else if (maxWidth < 4) {
         throw new IllegalArgumentException("Minimum abbreviation width is 4");
      } else if (str.length() <= maxWidth) {
         return str;
      } else {
         if (offset > str.length()) {
            offset = str.length();
         }

         if (str.length() - offset < maxWidth - 3) {
            offset = str.length() - (maxWidth - 3);
         }

         if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
         } else if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
         } else {
            return offset + (maxWidth - 3) < str.length()
               ? "..." + abbreviate(str.substring(offset), maxWidth - 3)
               : "..." + str.substring(str.length() - (maxWidth - 3));
         }
      }
   }

   public static String difference(String str1, String str2) {
      if (str1 == null) {
         return str2;
      } else if (str2 == null) {
         return str1;
      } else {
         int at = indexOfDifference(str1, str2);
         return at == -1 ? "" : str2.substring(at);
      }
   }

   public static int indexOfDifference(String str1, String str2) {
      if (str1 == str2) {
         return -1;
      } else if (str1 != null && str2 != null) {
         int i = 0;

         while(i < str1.length() && i < str2.length() && str1.charAt(i) == str2.charAt(i)) {
            ++i;
         }

         return i >= str2.length() && i >= str1.length() ? -1 : i;
      } else {
         return 0;
      }
   }

   public static int indexOfDifference(String[] strs) {
      if (strs != null && strs.length > 1) {
         boolean anyStringNull = false;
         boolean allStringsNull = true;
         int arrayLen = strs.length;
         int shortestStrLen = Integer.MAX_VALUE;
         int longestStrLen = 0;

         for(int i = 0; i < arrayLen; ++i) {
            if (strs[i] == null) {
               anyStringNull = true;
               shortestStrLen = 0;
            } else {
               allStringsNull = false;
               shortestStrLen = Math.min(strs[i].length(), shortestStrLen);
               longestStrLen = Math.max(strs[i].length(), longestStrLen);
            }
         }

         if (!allStringsNull && (longestStrLen != 0 || anyStringNull)) {
            if (shortestStrLen == 0) {
               return 0;
            } else {
               int var10 = -1;

               for(int stringPos = 0; stringPos < shortestStrLen; ++stringPos) {
                  char comparisonChar = strs[0].charAt(stringPos);

                  for(int arrayPos = 1; arrayPos < arrayLen; ++arrayPos) {
                     if (strs[arrayPos].charAt(stringPos) != comparisonChar) {
                        var10 = stringPos;
                        break;
                     }
                  }

                  if (var10 != -1) {
                     break;
                  }
               }

               return var10 == -1 && shortestStrLen != longestStrLen ? shortestStrLen : var10;
            }
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static String getCommonPrefix(String[] strs) {
      if (strs != null && strs.length != 0) {
         int smallestIndexOfDiff = indexOfDifference(strs);
         if (smallestIndexOfDiff == -1) {
            return strs[0] == null ? "" : strs[0];
         } else {
            return smallestIndexOfDiff == 0 ? "" : strs[0].substring(0, smallestIndexOfDiff);
         }
      } else {
         return "";
      }
   }

   public static int getLevenshteinDistance(String s, String t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            if (n > m) {
               String tmp = s;
               s = t;
               t = tmp;
               n = m;
               m = tmp.length();
            }

            int[] p = new int[n + 1];
            int[] d = new int[n + 1];
            int i = 0;

            while(i <= n) {
               p[i] = i++;
            }

            for(int j = 1; j <= m; ++j) {
               char t_j = t.charAt(j - 1);
               d[0] = j;

               for(int var12 = 1; var12 <= n; ++var12) {
                  int cost = s.charAt(var12 - 1) == t_j ? 0 : 1;
                  d[var12] = Math.min(Math.min(d[var12 - 1] + 1, p[var12] + 1), p[var12 - 1] + cost);
               }

               int[] _d = p;
               p = d;
               d = _d;
            }

            return p[n];
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }

   public static boolean startsWith(String str, String prefix) {
      return startsWith(str, prefix, false);
   }

   public static boolean startsWithIgnoreCase(String str, String prefix) {
      return startsWith(str, prefix, true);
   }

   private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
      if (str != null && prefix != null) {
         return prefix.length() > str.length() ? false : str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
      } else {
         return str == null && prefix == null;
      }
   }

   public static boolean endsWith(String str, String suffix) {
      return endsWith(str, suffix, false);
   }

   public static boolean endsWithIgnoreCase(String str, String suffix) {
      return endsWith(str, suffix, true);
   }

   private static boolean endsWith(String str, String suffix, boolean ignoreCase) {
      if (str != null && suffix != null) {
         if (suffix.length() > str.length()) {
            return false;
         } else {
            int strOffset = str.length() - suffix.length();
            return str.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
         }
      } else {
         return str == null && suffix == null;
      }
   }
}
