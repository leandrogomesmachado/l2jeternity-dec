package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class OperatorExpression extends Expression implements OperatorIds {
   public static int[][] OperatorSignatures = new int[20][];

   static {
      classInitialize();
   }

   public static final void classInitialize() {
      OperatorSignatures[2] = get_AND();
      OperatorSignatures[0] = get_AND_AND();
      OperatorSignatures[9] = get_DIVIDE();
      OperatorSignatures[18] = get_EQUAL_EQUAL();
      OperatorSignatures[6] = get_GREATER();
      OperatorSignatures[7] = get_GREATER_EQUAL();
      OperatorSignatures[10] = get_LEFT_SHIFT();
      OperatorSignatures[4] = get_LESS();
      OperatorSignatures[5] = get_LESS_EQUAL();
      OperatorSignatures[13] = get_MINUS();
      OperatorSignatures[15] = get_MULTIPLY();
      OperatorSignatures[3] = get_OR();
      OperatorSignatures[1] = get_OR_OR();
      OperatorSignatures[14] = get_PLUS();
      OperatorSignatures[16] = get_REMAINDER();
      OperatorSignatures[17] = get_RIGHT_SHIFT();
      OperatorSignatures[19] = get_UNSIGNED_RIGHT_SHIFT();
      OperatorSignatures[8] = get_XOR();
   }

   public static final String generateTableTestCase() {
      int[] operators = new int[]{2, 0, 9, 6, 7, 10, 4, 5, 13, 15, 3, 1, 14, 16, 17, 19, 8};

      class Decode {
         public final String constant(int code) {
            switch(code) {
               case 1:
                  return "null";
               case 2:
                  return "'A'";
               case 3:
                  return "((byte) 3)";
               case 4:
                  return "((short) 5)";
               case 5:
                  return "true";
               case 6:
               default:
                  return Util.EMPTY_STRING;
               case 7:
                  return "7L";
               case 8:
                  return "300.0d";
               case 9:
                  return "100.0f";
               case 10:
                  return "1";
               case 11:
                  return "\"hello-world\"";
               case 12:
                  return "null";
            }
         }

         public final String type(int code) {
            switch(code) {
               case 1:
                  return "obj";
               case 2:
                  return "c";
               case 3:
                  return "b";
               case 4:
                  return "s";
               case 5:
                  return "z";
               case 6:
               default:
                  return "xxx";
               case 7:
                  return "l";
               case 8:
                  return "d";
               case 9:
                  return "f";
               case 10:
                  return "i";
               case 11:
                  return "str";
               case 12:
                  return "null";
            }
         }

         public final String operator(int operator) {
            switch(operator) {
               case 0:
                  return "&&";
               case 1:
                  return "||";
               case 2:
                  return "&";
               case 3:
                  return "|";
               case 4:
                  return "<";
               case 5:
                  return "<=";
               case 6:
                  return ">";
               case 7:
                  return ">=";
               case 8:
                  return "^";
               case 9:
                  return "/";
               case 10:
                  return "<<";
               case 11:
                  return "!";
               case 12:
                  return "~";
               case 13:
                  return "-";
               case 14:
                  return "+";
               case 15:
                  return "*";
               case 16:
                  return "%";
               case 17:
                  return ">>";
               case 18:
                  return "==";
               case 19:
                  return ">>>";
               default:
                  return "????";
            }
         }
      }

      Decode decode = new Decode();
      String s = "\tpublic static void binaryOperationTablesTestCase(){\n\t\t//TC test : all binary operation (described in tables)\n\t\t//method automatically generated by\n\t\t//org.eclipse.jdt.internal.compiler.ast.OperatorExpression.generateTableTestCase();\n\t\tString str0;\t String str\t= "
         + decode.constant(11)
         + ";\n"
         + "\t\tint i0;\t int i\t= "
         + decode.constant(10)
         + ";\n"
         + "\t\tboolean z0;\t boolean z\t= "
         + decode.constant(5)
         + ";\n"
         + "\t\tchar c0; \t char  c\t= "
         + decode.constant(2)
         + ";\n"
         + "\t\tfloat f0; \t float f\t= "
         + decode.constant(9)
         + ";\n"
         + "\t\tdouble d0;\t double d\t= "
         + decode.constant(8)
         + ";\n"
         + "\t\tbyte b0; \t byte b\t= "
         + decode.constant(3)
         + ";\n"
         + "\t\tshort s0; \t short s\t= "
         + decode.constant(4)
         + ";\n"
         + "\t\tlong l0; \t long l\t= "
         + decode.constant(7)
         + ";\n"
         + "\t\tObject obj0; \t Object obj\t= "
         + decode.constant(1)
         + ";\n"
         + "\n";
      int error = 0;

      for(int i = 0; i < operators.length; ++i) {
         int operator = operators[i];

         for(int left = 0; left < 16; ++left) {
            for(int right = 0; right < 16; ++right) {
               int result = OperatorSignatures[operator][(left << 4) + right] & 15;
               if (result != 0) {
                  s = s + "\t\t" + decode.type(result) + "0" + " = " + decode.type(left);
                  s = s + " " + decode.operator(operator) + " " + decode.type(right) + ";\n";
                  String begin = result == 11 ? "\t\tif (! " : "\t\tif ( ";
                  String test = result == 11 ? ".equals(" : " != (";
                  s = s
                     + begin
                     + decode.type(result)
                     + "0"
                     + test
                     + decode.constant(left)
                     + " "
                     + decode.operator(operator)
                     + " "
                     + decode.constant(right)
                     + "))\n";
                  s = s + "\t\t\tSystem.out.println(" + ++error + ");\n";
               }
            }
         }
      }

      return s + "\n\t\tSystem.out.println(\"binary tables test : done\");}";
   }

   public static final int[] get_AND() {
      int[] table = new int[256];
      table[51] = 670266;
      table[55] = 472951;
      table[52] = 670282;
      table[50] = 670250;
      table[58] = 670378;
      table[115] = 489271;
      table[119] = 489335;
      table[116] = 489287;
      table[114] = 489255;
      table[122] = 489383;
      table[67] = 674362;
      table[71] = 477047;
      table[68] = 674378;
      table[66] = 674346;
      table[74] = 674474;
      table[85] = 349525;
      table[35] = 666170;
      table[39] = 468855;
      table[36] = 666186;
      table[34] = 666154;
      table[42] = 666282;
      table[163] = 698938;
      table[167] = 501623;
      table[164] = 698954;
      table[162] = 698922;
      table[170] = 699050;
      return table;
   }

   public static final int[] get_AND_AND() {
      int[] table = new int[256];
      table[85] = 349525;
      return table;
   }

   public static final int[] get_DIVIDE() {
      return get_MINUS();
   }

   public static final int[] get_EQUAL_EQUAL() {
      int[] table = new int[256];
      table[51] = 670261;
      table[55] = 472949;
      table[52] = 670277;
      table[56] = 538757;
      table[57] = 604565;
      table[50] = 670245;
      table[58] = 670373;
      table[115] = 489269;
      table[119] = 489333;
      table[116] = 489285;
      table[120] = 555141;
      table[121] = 620949;
      table[114] = 489253;
      table[122] = 489381;
      table[67] = 674357;
      table[71] = 477045;
      table[68] = 674373;
      table[72] = 542853;
      table[73] = 608661;
      table[66] = 674341;
      table[74] = 674469;
      table[187] = 111029;
      table[177] = 110869;
      table[188] = 111045;
      table[27] = 70069;
      table[17] = 69909;
      table[28] = 70085;
      table[131] = 559157;
      table[135] = 559221;
      table[132] = 559173;
      table[136] = 559237;
      table[137] = 559253;
      table[130] = 559141;
      table[138] = 559269;
      table[147] = 629045;
      table[151] = 629109;
      table[148] = 629061;
      table[152] = 563333;
      table[153] = 629141;
      table[146] = 629029;
      table[154] = 629157;
      table[85] = 349525;
      table[35] = 666165;
      table[39] = 468853;
      table[36] = 666181;
      table[40] = 534661;
      table[41] = 600469;
      table[34] = 666149;
      table[42] = 666277;
      table[163] = 698933;
      table[167] = 501621;
      table[164] = 698949;
      table[168] = 567429;
      table[169] = 633237;
      table[162] = 698917;
      table[170] = 699045;
      table[203] = 115125;
      table[193] = 114965;
      table[204] = 115141;
      return table;
   }

   public static final int[] get_GREATER() {
      return get_LESS();
   }

   public static final int[] get_GREATER_EQUAL() {
      return get_LESS();
   }

   public static final int[] get_LEFT_SHIFT() {
      int[] table = new int[256];
      table[51] = 670266;
      table[55] = 670330;
      table[52] = 670282;
      table[50] = 670250;
      table[58] = 670378;
      table[115] = 490039;
      table[119] = 490103;
      table[116] = 490055;
      table[114] = 490023;
      table[122] = 490151;
      table[67] = 674362;
      table[71] = 674426;
      table[68] = 674378;
      table[66] = 674346;
      table[74] = 674474;
      table[35] = 666170;
      table[39] = 666234;
      table[36] = 666186;
      table[34] = 666154;
      table[42] = 666282;
      table[163] = 698938;
      table[167] = 699002;
      table[164] = 698954;
      table[162] = 698922;
      table[170] = 699050;
      return table;
   }

   public static final int[] get_LESS() {
      int[] table = new int[256];
      table[51] = 670261;
      table[55] = 472949;
      table[52] = 670277;
      table[56] = 538757;
      table[57] = 604565;
      table[50] = 670245;
      table[58] = 670373;
      table[115] = 489269;
      table[119] = 489333;
      table[116] = 489285;
      table[120] = 555141;
      table[121] = 620949;
      table[114] = 489253;
      table[122] = 489381;
      table[67] = 674357;
      table[71] = 477045;
      table[68] = 674373;
      table[72] = 542853;
      table[73] = 608661;
      table[66] = 674341;
      table[74] = 674469;
      table[131] = 559157;
      table[135] = 559221;
      table[132] = 559173;
      table[136] = 559237;
      table[137] = 559253;
      table[130] = 559141;
      table[138] = 559269;
      table[147] = 629045;
      table[151] = 629109;
      table[148] = 629061;
      table[152] = 563333;
      table[153] = 629141;
      table[146] = 629029;
      table[154] = 629157;
      table[35] = 666165;
      table[39] = 468853;
      table[36] = 666181;
      table[40] = 534661;
      table[41] = 600469;
      table[34] = 666149;
      table[42] = 666277;
      table[163] = 698933;
      table[167] = 501621;
      table[164] = 698949;
      table[168] = 567429;
      table[169] = 633237;
      table[162] = 698917;
      table[170] = 699045;
      return table;
   }

   public static final int[] get_LESS_EQUAL() {
      return get_LESS();
   }

   public static final int[] get_MINUS() {
      int[] table = (int[])get_PLUS().clone();
      table[179] = 0;
      table[183] = 0;
      table[180] = 0;
      table[182] = 0;
      table[187] = 0;
      table[177] = 0;
      table[184] = 0;
      table[185] = 0;
      table[181] = 0;
      table[178] = 0;
      table[186] = 0;
      table[188] = 0;
      table[59] = 0;
      table[123] = 0;
      table[75] = 0;
      table[107] = 0;
      table[27] = 0;
      table[139] = 0;
      table[155] = 0;
      table[91] = 0;
      table[43] = 0;
      table[171] = 0;
      table[203] = 0;
      table[204] = 0;
      return table;
   }

   public static final int[] get_MULTIPLY() {
      return get_MINUS();
   }

   public static final int[] get_OR() {
      return get_AND();
   }

   public static final int[] get_OR_OR() {
      return get_AND_AND();
   }

   public static final int[] get_PLUS() {
      int[] table = new int[256];
      table[51] = 670266;
      table[55] = 472951;
      table[52] = 670282;
      table[59] = 211899;
      table[56] = 538760;
      table[57] = 604569;
      table[50] = 670250;
      table[58] = 670378;
      table[115] = 489271;
      table[119] = 489335;
      table[116] = 489287;
      table[123] = 490427;
      table[120] = 555144;
      table[121] = 620953;
      table[114] = 489255;
      table[122] = 489383;
      table[67] = 674362;
      table[71] = 477047;
      table[68] = 674378;
      table[75] = 281531;
      table[72] = 542856;
      table[73] = 608665;
      table[66] = 674346;
      table[74] = 674474;
      table[179] = 766779;
      table[183] = 767867;
      table[180] = 767051;
      table[187] = 768955;
      table[177] = 766235;
      table[184] = 768139;
      table[185] = 768411;
      table[181] = 767323;
      table[178] = 766507;
      table[186] = 768683;
      table[188] = 769227;
      table[27] = 72635;
      table[131] = 559160;
      table[135] = 559224;
      table[132] = 559176;
      table[139] = 560059;
      table[136] = 559240;
      table[137] = 559256;
      table[130] = 559144;
      table[138] = 559272;
      table[147] = 629049;
      table[151] = 629113;
      table[148] = 629065;
      table[155] = 629691;
      table[152] = 563336;
      table[153] = 629145;
      table[146] = 629033;
      table[154] = 629161;
      table[91] = 351163;
      table[35] = 666170;
      table[39] = 468855;
      table[36] = 666186;
      table[43] = 142267;
      table[40] = 534664;
      table[41] = 600473;
      table[34] = 666154;
      table[42] = 666282;
      table[163] = 698938;
      table[167] = 501623;
      table[164] = 698954;
      table[171] = 699323;
      table[168] = 567432;
      table[169] = 633241;
      table[162] = 698922;
      table[170] = 699050;
      table[203] = 838587;
      return table;
   }

   public static final int[] get_REMAINDER() {
      return get_MINUS();
   }

   public static final int[] get_RIGHT_SHIFT() {
      return get_LEFT_SHIFT();
   }

   public static final int[] get_UNSIGNED_RIGHT_SHIFT() {
      return get_LEFT_SHIFT();
   }

   public static final int[] get_XOR() {
      return get_AND();
   }

   public String operatorToString() {
      switch((this.bits & 4032) >> 6) {
         case 0:
            return "&&";
         case 1:
            return "||";
         case 2:
            return "&";
         case 3:
            return "|";
         case 4:
            return "<";
         case 5:
            return "<=";
         case 6:
            return ">";
         case 7:
            return ">=";
         case 8:
            return "^";
         case 9:
            return "/";
         case 10:
            return "<<";
         case 11:
            return "!";
         case 12:
            return "~";
         case 13:
            return "-";
         case 14:
            return "+";
         case 15:
            return "*";
         case 16:
            return "%";
         case 17:
            return ">>";
         case 18:
            return "==";
         case 19:
            return ">>>";
         case 20:
         case 21:
         case 22:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         default:
            return "unknown operator";
         case 23:
            return "?:";
         case 29:
            return "!=";
         case 30:
            return "=";
      }
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      output.append('(');
      return this.printExpressionNoParenthesis(0, output).append(')');
   }

   public abstract StringBuffer printExpressionNoParenthesis(int var1, StringBuffer var2);
}
