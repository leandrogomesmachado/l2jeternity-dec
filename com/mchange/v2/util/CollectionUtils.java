package com.mchange.v2.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public final class CollectionUtils {
   public static final SortedSet EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet());
   static final Class[] EMPTY_ARG_CLASSES = new Class[0];
   static final Object[] EMPTY_ARGS = new Object[0];
   static final Class[] COMPARATOR_ARG_CLASSES = new Class[]{Comparator.class};
   static final Class[] COLLECTION_ARG_CLASSES = new Class[]{Collection.class};
   static final Class[] SORTED_SET_ARG_CLASSES = new Class[]{SortedSet.class};
   static final Class[] MAP_ARG_CLASSES = new Class[]{Map.class};
   static final Class[] SORTED_MAP_ARG_CLASSES = new Class[]{SortedMap.class};
   static final Class STD_UNMODIFIABLE_COLLECTION_CL;
   static final Class STD_UNMODIFIABLE_SET_CL;
   static final Class STD_UNMODIFIABLE_LIST_CL;
   static final Class STD_UNMODIFIABLE_RA_LIST_CL;
   static final Class STD_UNMODIFIABLE_SORTED_SET_CL;
   static final Class STD_UNMODIFIABLE_MAP_CL;
   static final Class STD_UNMODIFIABLE_SORTED_MAP_CL;
   static final Class STD_SYNCHRONIZED_COLLECTION_CL;
   static final Class STD_SYNCHRONIZED_SET_CL;
   static final Class STD_SYNCHRONIZED_LIST_CL;
   static final Class STD_SYNCHRONIZED_RA_LIST_CL;
   static final Class STD_SYNCHRONIZED_SORTED_SET_CL;
   static final Class STD_SYNCHRONIZED_MAP_CL;
   static final Class STD_SYNCHRONIZED_SORTED_MAP_CL;
   static final Set UNMODIFIABLE_WRAPPERS;
   static final Set SYNCHRONIZED_WRAPPERS;
   static final Set ALL_COLLECTIONS_WRAPPERS;

   public static boolean isCollectionsWrapper(Class var0) {
      return ALL_COLLECTIONS_WRAPPERS.contains(var0);
   }

   public static boolean isCollectionsWrapper(Collection var0) {
      return isCollectionsWrapper(var0.getClass());
   }

   public static boolean isCollectionsWrapper(Map var0) {
      return isCollectionsWrapper(var0.getClass());
   }

   public static boolean isSynchronizedWrapper(Class var0) {
      return SYNCHRONIZED_WRAPPERS.contains(var0);
   }

   public static boolean isSynchronizedWrapper(Collection var0) {
      return isSynchronizedWrapper(var0.getClass());
   }

   public static boolean isSynchronizedWrapper(Map var0) {
      return isSynchronizedWrapper(var0.getClass());
   }

   public static boolean isUnmodifiableWrapper(Class var0) {
      return UNMODIFIABLE_WRAPPERS.contains(var0);
   }

   public static boolean isUnmodifiableWrapper(Collection var0) {
      return isUnmodifiableWrapper(var0.getClass());
   }

   public static boolean isUnmodifiableWrapper(Map var0) {
      return isUnmodifiableWrapper(var0.getClass());
   }

   public static Collection narrowUnmodifiableCollection(Collection var0) {
      if (var0 instanceof SortedSet) {
         return Collections.unmodifiableSortedSet((SortedSet)var0);
      } else if (var0 instanceof Set) {
         return Collections.unmodifiableSet((Set)var0);
      } else {
         return (Collection)(var0 instanceof List ? Collections.unmodifiableList((List)var0) : Collections.unmodifiableCollection(var0));
      }
   }

   public static Collection narrowSynchronizedCollection(Collection var0) {
      if (var0 instanceof SortedSet) {
         return Collections.synchronizedSortedSet((SortedSet)var0);
      } else if (var0 instanceof Set) {
         return Collections.synchronizedSet((Set)var0);
      } else {
         return (Collection)(var0 instanceof List ? Collections.synchronizedList((List)var0) : Collections.synchronizedCollection(var0));
      }
   }

   public static Map narrowUnmodifiableMap(Map var0) {
      return (Map)(var0 instanceof SortedMap ? Collections.unmodifiableSortedMap((SortedMap)var0) : Collections.unmodifiableMap(var0));
   }

   public static Map narrowSynchronizedMap(Map var0) {
      return (Map)(var0 instanceof SortedMap ? Collections.synchronizedSortedMap((SortedMap)var0) : Collections.synchronizedMap(var0));
   }

   public static Collection attemptClone(Collection var0) throws NoSuchMethodException {
      if (var0 instanceof Vector) {
         return (Collection)((Vector)var0).clone();
      } else if (var0 instanceof ArrayList) {
         return (Collection)((ArrayList)var0).clone();
      } else if (var0 instanceof LinkedList) {
         return (Collection)((LinkedList)var0).clone();
      } else if (var0 instanceof HashSet) {
         return (Collection)((HashSet)var0).clone();
      } else if (var0 instanceof TreeSet) {
         return (Collection)((TreeSet)var0).clone();
      } else {
         Collection var1 = null;
         Class var2 = var0.getClass();

         try {
            Method var3 = var2.getMethod("clone", EMPTY_ARG_CLASSES);
            var1 = (Collection)var3.invoke(var0, EMPTY_ARGS);
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         if (var1 == null) {
            try {
               Constructor var7 = var2.getConstructor(var0 instanceof SortedSet ? SORTED_SET_ARG_CLASSES : COLLECTION_ARG_CLASSES);
               var1 = (Collection)var7.newInstance(var0);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         if (var1 == null) {
            try {
               Constructor var8 = var2.getConstructor(var2);
               var1 = (Collection)var8.newInstance(var0);
            } catch (Exception var4) {
               var4.printStackTrace();
            }
         }

         if (var1 == null) {
            throw new NoSuchMethodException("No accessible clone() method or reasonable copy constructor could be called on Collection " + var0);
         } else {
            return var1;
         }
      }
   }

   public static Map attemptClone(Map var0) throws NoSuchMethodException {
      if (var0 instanceof Properties) {
         return (Map)((Properties)var0).clone();
      } else if (var0 instanceof Hashtable) {
         return (Map)((Hashtable)var0).clone();
      } else if (var0 instanceof HashMap) {
         return (Map)((HashMap)var0).clone();
      } else if (var0 instanceof TreeMap) {
         return (Map)((TreeMap)var0).clone();
      } else {
         Map var1 = null;
         Class var2 = var0.getClass();

         try {
            Method var3 = var2.getMethod("clone", EMPTY_ARG_CLASSES);
            var1 = (Map)var3.invoke(var0, EMPTY_ARGS);
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         if (var1 == null) {
            try {
               Constructor var7 = var2.getConstructor(var0 instanceof SortedMap ? SORTED_MAP_ARG_CLASSES : MAP_ARG_CLASSES);
               var1 = (Map)var7.newInstance(var0);
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         if (var1 == null) {
            try {
               Constructor var8 = var2.getConstructor(var2);
               var1 = (Map)var8.newInstance(var0);
            } catch (Exception var4) {
               var4.printStackTrace();
            }
         }

         if (var1 == null) {
            throw new NoSuchMethodException("No accessible clone() method or reasonable copy constructor could be called on Map " + var0);
         } else {
            return var1;
         }
      }
   }

   public static void add(Collection var0, Object var1) {
      var0.add(var1);
   }

   public static void remove(Collection var0, Object var1) {
      var0.remove(var1);
   }

   public static int size(Object var0) {
      if (var0 instanceof Collection) {
         return ((Collection)var0).size();
      } else if (var0 instanceof Map) {
         return ((Map)var0).size();
      } else if (var0 instanceof Object[]) {
         return ((Object[])var0).length;
      } else if (var0 instanceof boolean[]) {
         return ((boolean[])var0).length;
      } else if (var0 instanceof byte[]) {
         return ((byte[])var0).length;
      } else if (var0 instanceof char[]) {
         return ((char[])var0).length;
      } else if (var0 instanceof short[]) {
         return ((short[])var0).length;
      } else if (var0 instanceof int[]) {
         return ((int[])var0).length;
      } else if (var0 instanceof long[]) {
         return ((long[])var0).length;
      } else if (var0 instanceof float[]) {
         return ((float[])var0).length;
      } else if (var0 instanceof double[]) {
         return ((double[])var0).length;
      } else {
         throw new IllegalArgumentException(var0 + " must be a Collection, Map, or array!");
      }
   }

   private CollectionUtils() {
   }

   static {
      HashSet var0 = new HashSet();
      TreeSet var1 = new TreeSet();
      LinkedList var2 = new LinkedList();
      ArrayList var3 = new ArrayList();
      HashMap var4 = new HashMap();
      TreeMap var5 = new TreeMap();
      HashSet var6 = new HashSet();
      HashSet var7 = new HashSet();
      var6.add(STD_UNMODIFIABLE_COLLECTION_CL = Collections.unmodifiableCollection(var3).getClass());
      var6.add(STD_UNMODIFIABLE_SET_CL = Collections.unmodifiableSet(var0).getClass());
      var6.add(STD_UNMODIFIABLE_LIST_CL = Collections.unmodifiableList(var2).getClass());
      var6.add(STD_UNMODIFIABLE_RA_LIST_CL = Collections.unmodifiableList(var3).getClass());
      var6.add(STD_UNMODIFIABLE_SORTED_SET_CL = Collections.unmodifiableSortedSet(var1).getClass());
      var6.add(STD_UNMODIFIABLE_MAP_CL = Collections.unmodifiableMap(var4).getClass());
      var6.add(STD_UNMODIFIABLE_SORTED_MAP_CL = Collections.unmodifiableSortedMap(var5).getClass());
      var7.add(STD_SYNCHRONIZED_COLLECTION_CL = Collections.synchronizedCollection(var3).getClass());
      var7.add(STD_SYNCHRONIZED_SET_CL = Collections.synchronizedSet(var0).getClass());
      var7.add(STD_SYNCHRONIZED_LIST_CL = Collections.synchronizedList(var2).getClass());
      var7.add(STD_SYNCHRONIZED_RA_LIST_CL = Collections.synchronizedList(var3).getClass());
      var7.add(STD_SYNCHRONIZED_SORTED_SET_CL = Collections.synchronizedSortedSet(var1).getClass());
      var7.add(STD_SYNCHRONIZED_MAP_CL = Collections.synchronizedMap(var4).getClass());
      var7.add(STD_SYNCHRONIZED_SORTED_MAP_CL = Collections.synchronizedMap(var5).getClass());
      UNMODIFIABLE_WRAPPERS = Collections.unmodifiableSet(var6);
      SYNCHRONIZED_WRAPPERS = Collections.unmodifiableSet(var7);
      HashSet var8 = new HashSet(var6);
      var8.addAll(var7);
      ALL_COLLECTIONS_WRAPPERS = Collections.unmodifiableSet(var8);
   }
}
