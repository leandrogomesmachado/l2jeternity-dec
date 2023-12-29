package l2e.commons.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrTable {
   private final Map<Integer, Map<String, String>> rows = new HashMap<>();
   private final Map<String, Integer> columns = new LinkedHashMap<>();
   private final List<String> titles = new ArrayList<>();

   public StrTable(String title) {
      if (title != null) {
         this.titles.add(title);
      }
   }

   public StrTable() {
      this(null);
   }

   public StrTable set(int rowIndex, String colName, boolean val) {
      return this.set(rowIndex, colName, Boolean.toString(val));
   }

   public StrTable set(int rowIndex, String colName, byte val) {
      return this.set(rowIndex, colName, Byte.toString(val));
   }

   public StrTable set(int rowIndex, String colName, char val) {
      return this.set(rowIndex, colName, String.valueOf(val));
   }

   public StrTable set(int rowIndex, String colName, short val) {
      return this.set(rowIndex, colName, Short.toString(val));
   }

   public StrTable set(int rowIndex, String colName, int val) {
      return this.set(rowIndex, colName, Integer.toString(val));
   }

   public StrTable set(int rowIndex, String colName, long val) {
      return this.set(rowIndex, colName, Long.toString(val));
   }

   public StrTable set(int rowIndex, String colName, float val) {
      return this.set(rowIndex, colName, Float.toString(val));
   }

   public StrTable set(int rowIndex, String colName, double val) {
      return this.set(rowIndex, colName, Double.toString(val));
   }

   public StrTable set(int rowIndex, String colName, Object val) {
      return this.set(rowIndex, colName, String.valueOf(val));
   }

   public StrTable set(int rowIndex, String colName, String val) {
      Map<String, String> row;
      if (this.rows.containsKey(rowIndex)) {
         row = this.rows.get(rowIndex);
      } else {
         row = new HashMap<>();
         this.rows.put(rowIndex, row);
      }

      row.put(colName, val);
      int columnSize;
      if (!this.columns.containsKey(colName)) {
         columnSize = Math.max(colName.length(), val.length());
      } else if (this.columns.get(colName) >= (columnSize = val.length())) {
         return this;
      }

      this.columns.put(colName, columnSize);
      return this;
   }

   public StrTable addTitle(String s) {
      this.titles.add(s);
      return this;
   }

   private static StringBuilder right(StringBuilder result, String s, int sz) {
      result.append(s);
      if ((sz = sz - s.length()) > 0) {
         for(int i = 0; i < sz; ++i) {
            result.append(" ");
         }
      }

      return result;
   }

   private static StringBuilder center(StringBuilder result, String s, int sz) {
      int offset = result.length();
      result.append(s);

      int i;
      while((i = sz - (result.length() - offset)) > 0) {
         result.append(" ");
         if (i > 1) {
            result.insert(offset, " ");
         }
      }

      return result;
   }

   private static StringBuilder repeat(StringBuilder result, String s, int sz) {
      for(int i = 0; i < sz; ++i) {
         result.append(s);
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      if (this.columns.isEmpty()) {
         return result.toString();
      } else {
         StringBuilder header = new StringBuilder("|");
         StringBuilder line = new StringBuilder("|");

         for(String c : this.columns.keySet()) {
            center(header, c, this.columns.get(c) + 2).append("|");
            repeat(line, "-", this.columns.get(c) + 2).append("|");
         }

         if (!this.titles.isEmpty()) {
            result.append(" ");
            repeat(result, "-", header.length() - 2).append(" ").append("\n");

            for(String title : this.titles) {
               result.append("| ");
               right(result, title, header.length() - 3).append("|").append("\n");
            }
         }

         result.append(" ");
         repeat(result, "-", header.length() - 2).append(" ").append("\n");
         result.append((CharSequence)header).append("\n");
         result.append((CharSequence)line).append("\n");

         for(Map<String, String> row : this.rows.values()) {
            result.append("|");

            for(String c : this.columns.keySet()) {
               center(result, row.containsKey(c) ? row.get(c) : "-", this.columns.get(c) + 2).append("|");
            }

            result.append("\n");
         }

         result.append(" ");
         repeat(result, "-", header.length() - 2).append(" ").append("\n");
         return result.toString();
      }
   }
}
