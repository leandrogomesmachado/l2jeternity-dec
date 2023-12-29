package org.json;

import java.util.Iterator;

public class XML {
   public static final Character AMP = '&';
   public static final Character APOS = '\'';
   public static final Character BANG = '!';
   public static final Character EQ = '=';
   public static final Character GT = '>';
   public static final Character LT = '<';
   public static final Character QUEST = '?';
   public static final Character QUOT = '"';
   public static final Character SLASH = '/';

   public static String escape(String string) {
      StringBuilder sb = new StringBuilder(string.length());
      int i = 0;

      for(int length = string.length(); i < length; ++i) {
         char c = string.charAt(i);
         switch(c) {
            case '"':
               sb.append("&quot;");
               break;
            case '&':
               sb.append("&amp;");
               break;
            case '\'':
               sb.append("&apos;");
               break;
            case '<':
               sb.append("&lt;");
               break;
            case '>':
               sb.append("&gt;");
               break;
            default:
               sb.append(c);
         }
      }

      return sb.toString();
   }

   public static void noSpace(String string) throws JSONException {
      int length = string.length();
      if (length == 0) {
         throw new JSONException("Empty string.");
      } else {
         for(int i = 0; i < length; ++i) {
            if (Character.isWhitespace(string.charAt(i))) {
               throw new JSONException("'" + string + "' contains a space character.");
            }
         }
      }
   }

   private static boolean parse(XMLTokener x, JSONObject context, String name) throws JSONException {
      JSONObject jsonobject = null;
      Object token = x.nextToken();
      if (token == BANG) {
         char c = x.next();
         if (c == '-') {
            if (x.next() == '-') {
               x.skipPast("-->");
               return false;
            }

            x.back();
         } else if (c == '[') {
            token = x.nextToken();
            if ("CDATA".equals(token) && x.next() == '[') {
               String string = x.nextCDATA();
               if (string.length() > 0) {
                  context.accumulate("content", string);
               }

               return false;
            }

            throw x.syntaxError("Expected 'CDATA['");
         }

         int i = 1;

         do {
            token = x.nextMeta();
            if (token == null) {
               throw x.syntaxError("Missing '>' after '<!'.");
            }

            if (token == LT) {
               ++i;
            } else if (token == GT) {
               --i;
            }
         } while(i > 0);

         return false;
      } else if (token == QUEST) {
         x.skipPast("?>");
         return false;
      } else if (token == SLASH) {
         token = x.nextToken();
         if (name == null) {
            throw x.syntaxError("Mismatched close tag " + token);
         } else if (!token.equals(name)) {
            throw x.syntaxError("Mismatched " + name + " and " + token);
         } else if (x.nextToken() != GT) {
            throw x.syntaxError("Misshaped close tag");
         } else {
            return true;
         }
      } else if (token instanceof Character) {
         throw x.syntaxError("Misshaped tag");
      } else {
         String tagName = (String)token;
         token = null;
         jsonobject = new JSONObject();

         while(true) {
            if (token == null) {
               token = x.nextToken();
            }

            if (!(token instanceof String)) {
               if (token == SLASH) {
                  if (x.nextToken() != GT) {
                     throw x.syntaxError("Misshaped tag");
                  }

                  if (jsonobject.length() > 0) {
                     context.accumulate(tagName, jsonobject);
                  } else {
                     context.accumulate(tagName, "");
                  }

                  return false;
               }

               if (token != GT) {
                  throw x.syntaxError("Misshaped tag");
               }

               while(true) {
                  token = x.nextContent();
                  if (token == null) {
                     if (tagName != null) {
                        throw x.syntaxError("Unclosed tag " + tagName);
                     }

                     return false;
                  }

                  if (token instanceof String) {
                     String string = (String)token;
                     if (string.length() > 0) {
                        jsonobject.accumulate("content", stringToValue(string));
                     }
                  } else if (token == LT && parse(x, jsonobject, tagName)) {
                     if (jsonobject.length() == 0) {
                        context.accumulate(tagName, "");
                     } else if (jsonobject.length() == 1 && jsonobject.opt("content") != null) {
                        context.accumulate(tagName, jsonobject.opt("content"));
                     } else {
                        context.accumulate(tagName, jsonobject);
                     }

                     return false;
                  }
               }
            }

            String string = (String)token;
            token = x.nextToken();
            if (token == EQ) {
               token = x.nextToken();
               if (!(token instanceof String)) {
                  throw x.syntaxError("Missing value");
               }

               jsonobject.accumulate(string, stringToValue((String)token));
               token = null;
            } else {
               jsonobject.accumulate(string, "");
            }
         }
      }
   }

   public static Object stringToValue(String string) {
      if ("true".equalsIgnoreCase(string)) {
         return Boolean.TRUE;
      } else if ("false".equalsIgnoreCase(string)) {
         return Boolean.FALSE;
      } else if ("null".equalsIgnoreCase(string)) {
         return JSONObject.NULL;
      } else {
         try {
            char initial = string.charAt(0);
            if (initial == '-' || initial >= '0' && initial <= '9') {
               Long value = new Long(string);
               if (value.toString().equals(string)) {
                  return value;
               }
            }
         } catch (Exception var4) {
            try {
               Double value = new Double(string);
               if (value.toString().equals(string)) {
                  return value;
               }
            } catch (Exception var3) {
            }
         }

         return string;
      }
   }

   public static JSONObject toJSONObject(String string) throws JSONException {
      JSONObject jo = new JSONObject();
      XMLTokener x = new XMLTokener(string);

      while(x.more() && x.skipPast("<")) {
         parse(x, jo, null);
      }

      return jo;
   }

   public static String toString(Object object) throws JSONException {
      return toString(object, null);
   }

   public static String toString(Object object, String tagName) throws JSONException {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: invalid constant type: Ljava/lang/Object;
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent.toJava(ConstExprent.java:360)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent.toJava(AssignmentExprent.java:131)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:967)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:65)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:905)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:240)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:905)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:905)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:115)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:905)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:905)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement.toJava(IfStatement.java:260)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.methodToJava(ClassWriter.java:1192)
      //
      // Bytecode:
      // 000: new java/lang/StringBuilder
      // 003: dup
      // 004: invokespecial java/lang/StringBuilder.<init> ()V
      // 007: astore 2
      // 008: aload 0
      // 009: instanceof org/json/JSONObject
      // 00c: ifeq 18e
      // 00f: aload 1
      // 010: ifnull 027
      // 013: aload 2
      // 014: bipush 60
      // 016: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 019: pop
      // 01a: aload 2
      // 01b: aload 1
      // 01c: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 01f: pop
      // 020: aload 2
      // 021: bipush 62
      // 023: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 026: pop
      // 027: aload 0
      // 028: checkcast org/json/JSONObject
      // 02b: astore 5
      // 02d: aload 5
      // 02f: invokevirtual org/json/JSONObject.keys ()Ljava/util/Iterator;
      // 032: astore 7
      // 034: aload 7
      // 036: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 03b: ifeq 171
      // 03e: aload 7
      // 040: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 045: checkcast java/lang/String
      // 048: astore 6
      // 04a: aload 5
      // 04c: aload 6
      // 04e: invokevirtual org/json/JSONObject.opt (Ljava/lang/String;)Ljava/lang/Object;
      // 051: astore 10
      // 053: aload 10
      // 055: ifnonnull 05c
      // 058: ldc ""
      // 05a: astore 10
      // 05c: aload 10
      // 05e: instanceof java/lang/String
      // 061: ifeq 06c
      // 064: aload 10
      // 066: checkcast java/lang/String
      // 069: goto 06d
      // 06c: aconst_null
      // 06d: astore 9
      // 06f: ldc "content"
      // 071: aload 6
      // 073: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 076: ifeq 0c9
      // 079: aload 10
      // 07b: instanceof org/json/JSONArray
      // 07e: ifeq 0b9
      // 081: aload 10
      // 083: checkcast org/json/JSONArray
      // 086: astore 4
      // 088: aload 4
      // 08a: invokevirtual org/json/JSONArray.length ()I
      // 08d: istore 8
      // 08f: bipush 0
      // 090: istore 3
      // 091: iload 3
      // 092: iload 8
      // 094: if_icmpge 034
      // 097: iload 3
      // 098: ifle 0a2
      // 09b: aload 2
      // 09c: bipush 10
      // 09e: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 0a1: pop
      // 0a2: aload 2
      // 0a3: aload 4
      // 0a5: iload 3
      // 0a6: invokevirtual org/json/JSONArray.get (I)Ljava/lang/Object;
      // 0a9: invokevirtual java/lang/Object.toString ()Ljava/lang/String;
      // 0ac: invokestatic org/json/XML.escape (Ljava/lang/String;)Ljava/lang/String;
      // 0af: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 0b2: pop
      // 0b3: iinc 3 1
      // 0b6: goto 091
      // 0b9: aload 2
      // 0ba: aload 10
      // 0bc: invokevirtual java/lang/Object.toString ()Ljava/lang/String;
      // 0bf: invokestatic org/json/XML.escape (Ljava/lang/String;)Ljava/lang/String;
      // 0c2: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 0c5: pop
      // 0c6: goto 034
      // 0c9: aload 10
      // 0cb: instanceof org/json/JSONArray
      // 0ce: ifeq 140
      // 0d1: aload 10
      // 0d3: checkcast org/json/JSONArray
      // 0d6: astore 4
      // 0d8: aload 4
      // 0da: invokevirtual org/json/JSONArray.length ()I
      // 0dd: istore 8
      // 0df: bipush 0
      // 0e0: istore 3
      // 0e1: iload 3
      // 0e2: iload 8
      // 0e4: if_icmpge 034
      // 0e7: aload 4
      // 0e9: iload 3
      // 0ea: invokevirtual org/json/JSONArray.get (I)Ljava/lang/Object;
      // 0ed: astore 10
      // 0ef: aload 10
      // 0f1: instanceof org/json/JSONArray
      // 0f4: ifeq 12e
      // 0f7: aload 2
      // 0f8: bipush 60
      // 0fa: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 0fd: pop
      // 0fe: aload 2
      // 0ff: aload 6
      // 101: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 104: pop
      // 105: aload 2
      // 106: bipush 62
      // 108: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 10b: pop
      // 10c: aload 2
      // 10d: aload 10
      // 10f: invokestatic org/json/XML.toString (Ljava/lang/Object;)Ljava/lang/String;
      // 112: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 115: pop
      // 116: aload 2
      // 117: ldc "</"
      // 119: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 11c: pop
      // 11d: aload 2
      // 11e: aload 6
      // 120: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 123: pop
      // 124: aload 2
      // 125: bipush 62
      // 127: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 12a: pop
      // 12b: goto 13a
      // 12e: aload 2
      // 12f: aload 10
      // 131: aload 6
      // 133: invokestatic org/json/XML.toString (Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
      // 136: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 139: pop
      // 13a: iinc 3 1
      // 13d: goto 0e1
      // 140: ldc ""
      // 142: aload 10
      // 144: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 147: ifeq 162
      // 14a: aload 2
      // 14b: bipush 60
      // 14d: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 150: pop
      // 151: aload 2
      // 152: aload 6
      // 154: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 157: pop
      // 158: aload 2
      // 159: ldc "/>"
      // 15b: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 15e: pop
      // 15f: goto 034
      // 162: aload 2
      // 163: aload 10
      // 165: aload 6
      // 167: invokestatic org/json/XML.toString (Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
      // 16a: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 16d: pop
      // 16e: goto 034
      // 171: aload 1
      // 172: ifnull 189
      // 175: aload 2
      // 176: ldc "</"
      // 178: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 17b: pop
      // 17c: aload 2
      // 17d: aload 1
      // 17e: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 181: pop
      // 182: aload 2
      // 183: bipush 62
      // 185: invokevirtual java/lang/StringBuilder.append (C)Ljava/lang/StringBuilder;
      // 188: pop
      // 189: aload 2
      // 18a: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 18d: areturn
      // 18e: aload 0
      // 18f: invokevirtual java/lang/Object.getClass ()Ljava/lang/Class;
      // 192: invokevirtual java/lang/Class.isArray ()Z
      // 195: ifeq 1a1
      // 198: new org/json/JSONArray
      // 19b: dup
      // 19c: aload 0
      // 19d: invokespecial org/json/JSONArray.<init> (Ljava/lang/Object;)V
      // 1a0: astore 0
      // 1a1: aload 0
      // 1a2: instanceof org/json/JSONArray
      // 1a5: ifeq 1e0
      // 1a8: aload 0
      // 1a9: checkcast org/json/JSONArray
      // 1ac: astore 4
      // 1ae: aload 4
      // 1b0: invokevirtual org/json/JSONArray.length ()I
      // 1b3: istore 8
      // 1b5: bipush 0
      // 1b6: istore 3
      // 1b7: iload 3
      // 1b8: iload 8
      // 1ba: if_icmpge 1db
      // 1bd: aload 2
      // 1be: aload 4
      // 1c0: iload 3
      // 1c1: invokevirtual org/json/JSONArray.opt (I)Ljava/lang/Object;
      // 1c4: aload 1
      // 1c5: ifnonnull 1cd
      // 1c8: ldc "array"
      // 1ca: goto 1ce
      // 1cd: aload 1
      // 1ce: invokestatic org/json/XML.toString (Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
      // 1d1: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 1d4: pop
      // 1d5: iinc 3 1
      // 1d8: goto 1b7
      // 1db: aload 2
      // 1dc: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 1df: areturn
      // 1e0: aload 0
      // 1e1: ifnonnull 1e9
      // 1e4: ldc "null"
      // 1e6: goto 1f0
      // 1e9: aload 0
      // 1ea: invokevirtual java/lang/Object.toString ()Ljava/lang/String;
      // 1ed: invokestatic org/json/XML.escape (Ljava/lang/String;)Ljava/lang/String;
      // 1f0: astore 9
      // 1f2: aload 1
      // 1f3: ifnonnull 212
      // 1f6: new java/lang/StringBuilder
      // 1f9: dup
      // 1fa: invokespecial java/lang/StringBuilder.<init> ()V
      // 1fd: ldc "\""
      // 1ff: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 202: aload 9
      // 204: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 207: ldc "\""
      // 209: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 20c: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 20f: goto 260
      // 212: aload 9
      // 214: invokevirtual java/lang/String.length ()I
      // 217: ifne 235
      // 21a: new java/lang/StringBuilder
      // 21d: dup
      // 21e: invokespecial java/lang/StringBuilder.<init> ()V
      // 221: ldc "<"
      // 223: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 226: aload 1
      // 227: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 22a: ldc "/>"
      // 22c: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 22f: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 232: goto 260
      // 235: new java/lang/StringBuilder
      // 238: dup
      // 239: invokespecial java/lang/StringBuilder.<init> ()V
      // 23c: ldc "<"
      // 23e: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 241: aload 1
      // 242: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 245: ldc ">"
      // 247: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 24a: aload 9
      // 24c: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 24f: ldc "</"
      // 251: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 254: aload 1
      // 255: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 258: ldc ">"
      // 25a: invokevirtual java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
      // 25d: invokevirtual java/lang/StringBuilder.toString ()Ljava/lang/String;
      // 260: areturn
   }
}
