package l2e.commons.util;

import java.util.regex.Pattern;

public class Strings {
   public static String stripSlashes(String s) {
      if (s == null) {
         return "";
      } else {
         s = s.replace("\\'", "'");
         return s.replace("\\\\", "\\");
      }
   }

   public static String addSlashes(String s) {
      if (s == null) {
         return "";
      } else {
         s = s.replace("\\", "\\\\");
         s = s.replace("\"", "\\\"");
         s = s.replace("@", "\\@");
         return s.replace("'", "\\'");
      }
   }

   public static String bbParse(String s) {
      if (s == null) {
         return null;
      } else {
         s = s.replace("\r", "");
         s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"LEVEL\">$2</font>$3");
         s = s.replaceAll("(\\s|\"|'|\\(|^|\n)\\$(.*?)\\$(\\s|\"|'|\\)|\\?|\\.|!|:|;|,|$|\n)", "$1<font color=\"00FFFF\">$2</font>$3");
         s = replace(s, "^!(.*?)$", 8, "<font color=\"FFFFFF\">$1</font>\n\n");
         s = s.replaceAll("%%\\s*\n", "<br1>");
         s = s.replaceAll("\n\n+", "<br>");
         s = replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", 32, "<br1><a action=\"bypass -h $1\">$2</a>");
         return s.replaceAll(" @", "\" msg=\"");
      }
   }

   public static String replace(String str, String regex, int flags, String replace) {
      return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
   }
}
