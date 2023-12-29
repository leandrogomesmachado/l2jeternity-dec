package l2e.commons.collections;

import java.util.HashMap;

public class MultiValueSet<T> extends HashMap<T, Object> {
   private static final long serialVersionUID = 8071544899414292397L;

   public MultiValueSet() {
   }

   public MultiValueSet(int size) {
      super(size);
   }

   public MultiValueSet(MultiValueSet<T> set) {
      super(set);
   }

   public void set(T key, Object value) {
      this.put(key, value);
   }

   public void set(T key, String value) {
      this.put(key, value);
   }

   public void set(T key, boolean value) {
      this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
   }

   public void set(T key, int value) {
      this.put(key, Integer.valueOf(value));
   }

   public void set(T key, int[] value) {
      this.put(key, value);
   }

   public void set(T key, long value) {
      this.put(key, Long.valueOf(value));
   }

   public void set(T key, double value) {
      this.put(key, Double.valueOf(value));
   }

   public void set(T key, Enum<?> value) {
      this.put(key, value);
   }

   public void unset(T key) {
      this.remove(key);
   }

   public boolean isSet(T key) {
      return this.get(key) != null;
   }

   public MultiValueSet<T> clone() {
      return new MultiValueSet<>(this);
   }

   public boolean getBool(T key) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).intValue() != 0;
      } else if (val instanceof String) {
         return Boolean.parseBoolean((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val;
      } else {
         throw new IllegalArgumentException("Boolean value required, but found: " + val + "!");
      }
   }

   public boolean getBool(T key, boolean defaultValue) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).intValue() != 0;
      } else if (val instanceof String) {
         return Boolean.parseBoolean((String)val);
      } else {
         return val instanceof Boolean ? (Boolean)val : defaultValue;
      }
   }

   public byte getByte(T key) {
      Object val = this.get(key);
      if (val == null) {
         throw new IllegalArgumentException("Byte value required, but not specified");
      } else if (val instanceof Number) {
         return ((Number)val).byteValue();
      } else {
         try {
            return Byte.parseByte((String)val);
         } catch (Exception var4) {
            throw new IllegalArgumentException("Byte value required, but found: " + val);
         }
      }
   }

   public byte getByte(T key, byte deflt) {
      Object val = this.get(key);
      if (val == null) {
         return deflt;
      } else if (val instanceof Number) {
         return ((Number)val).byteValue();
      } else {
         try {
            return Byte.parseByte((String)val);
         } catch (Exception var5) {
            throw new IllegalArgumentException("Byte value required, but found: " + val);
         }
      }
   }

   public int getInteger(T key) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).intValue();
      } else if (val instanceof String) {
         return Integer.parseInt((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1 : 0;
      } else {
         throw new IllegalArgumentException("Integer value required, but found: " + val + "!");
      }
   }

   public int getInteger(T key, int defaultValue) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).intValue();
      } else if (val instanceof String) {
         return Integer.parseInt((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1 : 0;
      } else {
         return defaultValue;
      }
   }

   public int[] getIntegerArray(T key) {
      Object val = this.get(key);
      if (val instanceof int[]) {
         return (int[])val;
      } else if (val instanceof Number) {
         return new int[]{((Number)val).intValue()};
      } else if (!(val instanceof String)) {
         throw new IllegalArgumentException("Integer array required, but found: " + val + "!");
      } else {
         String[] vals = ((String)val).split(";");
         int[] result = new int[vals.length];
         int i = 0;

         for(String v : vals) {
            result[i++] = Integer.parseInt(v);
         }

         return result;
      }
   }

   public int[] getIntegerArray(T key, int[] defaultArray) {
      try {
         return this.getIntegerArray(key);
      } catch (IllegalArgumentException var4) {
         return defaultArray;
      }
   }

   public long getLong(T key) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).longValue();
      } else if (val instanceof String) {
         return Long.parseLong((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1L : 0L;
      } else {
         throw new IllegalArgumentException("Long value required, but found: " + val + "!");
      }
   }

   public long getLong(T key, long defaultValue) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).longValue();
      } else if (val instanceof String) {
         return Long.parseLong((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1L : 0L;
      } else {
         return defaultValue;
      }
   }

   public float getFloat(String key) {
      Object val = this.get(key);
      if (val == null) {
         throw new IllegalArgumentException("Float value required, but not specified");
      } else if (val instanceof Number) {
         return ((Number)val).floatValue();
      } else {
         try {
            return (float)Double.parseDouble((String)val);
         } catch (Exception var4) {
            throw new IllegalArgumentException("Float value required, but found: " + val);
         }
      }
   }

   public float getFloat(String key, float defaultValue) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).floatValue();
      } else if (val instanceof String) {
         return Float.parseFloat((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1.0F : 0.0F;
      } else {
         return defaultValue;
      }
   }

   public double getDouble(T key) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).doubleValue();
      } else if (val instanceof String) {
         return Double.parseDouble((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1.0 : 0.0;
      } else {
         throw new IllegalArgumentException("Double value required, but found: " + val + "!");
      }
   }

   public double getDouble(T key, double defaultValue) {
      Object val = this.get(key);
      if (val instanceof Number) {
         return ((Number)val).doubleValue();
      } else if (val instanceof String) {
         return Double.parseDouble((String)val);
      } else if (val instanceof Boolean) {
         return (Boolean)val ? 1.0 : 0.0;
      } else {
         return defaultValue;
      }
   }

   public String getString(T key) {
      Object val = this.get(key);
      if (val != null) {
         return String.valueOf(val);
      } else {
         throw new IllegalArgumentException("String value required, but not specified!");
      }
   }

   public String getString(T key, String defaultValue) {
      Object val = this.get(key);
      return val != null ? String.valueOf(val) : defaultValue;
   }

   public Object getObject(T key) {
      return this.get(key);
   }

   public Object getObject(T key, Object defaultValue) {
      Object val = this.get(key);
      return val != null ? val : defaultValue;
   }

   public final <A> A getObject(String name, Class<A> type) {
      Object obj = this.get(name);
      return (A)(obj != null && type.isAssignableFrom(obj.getClass()) ? obj : null);
   }

   public short getShort(String key) {
      Object val = this.get(key);
      if (val == null) {
         throw new IllegalArgumentException("Short value required, but not specified");
      } else if (val instanceof Number) {
         return ((Number)val).shortValue();
      } else {
         try {
            return Short.parseShort((String)val);
         } catch (Exception var4) {
            throw new IllegalArgumentException("Short value required, but found: " + val);
         }
      }
   }

   public <E extends Enum<E>> E getEnum(T name, Class<E> enumClass) {
      Object val = this.get(name);
      if (val != null && enumClass.isInstance(val)) {
         return (E)val;
      } else if (val instanceof String) {
         return Enum.valueOf(enumClass, (String)val);
      } else {
         throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val + "!");
      }
   }

   public <E extends Enum<E>> E getEnum(T name, Class<E> enumClass, E defaultValue) {
      Object val = this.get(name);
      if (val != null && enumClass.isInstance(val)) {
         return (E)val;
      } else {
         return (E)(val instanceof String ? Enum.valueOf(enumClass, (String)val) : defaultValue);
      }
   }

   public int[] getIntegerArray(T key, String splitOn) {
      Object val = this.get(key);
      if (val == null) {
         throw new IllegalArgumentException("Integer value required, but not specified");
      } else if (val instanceof Number) {
         return new int[]{((Number)val).intValue()};
      } else {
         int c = 0;
         String[] vals = ((String)val).split(splitOn);
         int[] result = new int[vals.length];

         for(String v : vals) {
            try {
               result[c++] = Integer.parseInt(v);
            } catch (Exception var12) {
               throw new IllegalArgumentException("Integer value required, but found: " + val);
            }
         }

         return result;
      }
   }
}
