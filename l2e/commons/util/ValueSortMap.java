package l2e.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ValueSortMap {
   public Map<Integer, Integer> sortThis(Map<Integer, Integer> map, boolean asc) {
      return sortMapByValue(map, asc);
   }

   public static LinkedHashMap sortMapByValue(Map inMap, Comparator comparator) {
      return sortMapByValue(inMap, comparator, null);
   }

   public static LinkedHashMap sortMapByValue(Map inMap, boolean ascendingOrder) {
      return sortMapByValue(inMap, null, ascendingOrder);
   }

   public static LinkedHashMap sortMapByValue(Map inMap) {
      return sortMapByValue(inMap, null, null);
   }

   private static LinkedHashMap sortMapByValue(Map inMap, Comparator comparator, Boolean ascendingOrder) {
      int iSize = inMap.size();
      LinkedHashMap sortedMap = new LinkedHashMap(iSize);
      Collection values = inMap.values();
      ArrayList valueList = new ArrayList(values);
      HashSet distinctValues = new HashSet(values);
      int iNullValueCount = 0;
      if (distinctValues.contains(null)) {
         distinctValues.remove(null);

         for(int i = 0; i < valueList.size(); ++i) {
            if (valueList.get(i) == null) {
               valueList.remove(i);
               ++iNullValueCount;
               --i;
            }
         }
      }

      if (ascendingOrder == null) {
         Collections.sort(valueList, comparator);
      } else if (ascendingOrder) {
         Collections.sort(valueList);
      } else {
         Collections.sort(valueList);
         Collections.reverse(valueList);
      }

      boolean bAllDistinct = true;
      if (iSize != distinctValues.size() + iNullValueCount) {
         bAllDistinct = false;
      }

      Object key = null;
      Object value = null;
      Set keySet = null;
      Iterator itKeyList = null;
      HashMap hmTmpMap = new HashMap(iSize);
      HashMap hmNullValueMap = new HashMap();
      if (bAllDistinct) {
         for(Object var19 : inMap.keySet()) {
            value = inMap.get(var19);
            if (value != null) {
               hmTmpMap.put(value, var19);
            } else {
               hmNullValueMap.put(var19, value);
            }
         }

         if (ascendingOrder != null && !ascendingOrder) {
            sortedMap.putAll(hmNullValueMap);
         }

         for(int i = 0; i < valueList.size(); ++i) {
            value = valueList.get(i);
            key = hmTmpMap.get(value);
            sortedMap.put(key, value);
         }

         if (ascendingOrder == null || ascendingOrder) {
            sortedMap.putAll(hmNullValueMap);
         }
      } else {
         for(Object var21 : inMap.keySet()) {
            value = inMap.get(var21);
            if (value != null) {
               hmTmpMap.put(var21, value);
            } else {
               hmNullValueMap.put(var21, value);
            }
         }

         if (ascendingOrder != null && !ascendingOrder) {
            sortedMap.putAll(hmNullValueMap);
         }

         for(int i = 0; i < valueList.size(); ++i) {
            Object sortedValue = valueList.get(i);

            for(Object var22 : hmTmpMap.keySet()) {
               value = hmTmpMap.get(var22);
               if (value.equals(sortedValue)) {
                  sortedMap.put(var22, value);
                  hmTmpMap.remove(var22);
                  break;
               }
            }
         }

         if (ascendingOrder == null || ascendingOrder) {
            sortedMap.putAll(hmNullValueMap);
         }
      }

      return sortedMap;
   }
}
