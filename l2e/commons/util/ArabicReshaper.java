package l2e.commons.util;

public class ArabicReshaper {
   private String _returnString = "";
   public static char DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_MDD = 1570;
   public static char DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_HAMAZA = 1571;
   public static char DEFINED_CHARACTERS_ORGINAL_ALF_LOWER_HAMAZA = 1573;
   public static char DEFINED_CHARACTERS_ORGINAL_ALF = 1575;
   public static char DEFINED_CHARACTERS_ORGINAL_LAM = 1604;
   public static final char RIGHT_LEFT_CHAR = '\u0001';
   public static final char RIGHT_NOLEFT_CHAR_ALEF = '\u0006';
   public static final char RIGHT_NOLEFT_CHAR = '\u0004';
   public static final char RIGHT_LEFT_CHAR_LAM = '\u0003';
   public static final char TANWEEN = '\f';
   public static final char TASHKEEL = '\n';
   public static final char TATWEEL_CHAR = '\b';
   public static final char NORIGHT_NOLEFT_CHAR = '\u0007';
   public static final char NOTUSED_CHAR = '\u000f';
   public static final char NOTARABIC_CHAR = '\u0000';
   public static final char RIGHT_LEFT_CHAR_MASK = '\u0880';
   public static final char RIGHT_NOLEFT_CHAR_MASK = 'ࠀ';
   public static final char LEFT_CHAR_MASK = '\u0080';
   public static char[][] LAM_ALEF_GLPHIES = new char[][]{{'㮦', 'ﻶ', 'ﻵ'}, {'㮧', 'ﻸ', 'ﻷ'}, {'ا', 'ﻼ', 'ﻻ'}, {'إ', 'ﻺ', 'ﻹ'}};
   public static char[] HARAKATE = new char[]{
      '\u0600',
      '\u0601',
      '\u0602',
      '\u0603',
      '؆',
      '؇',
      '؈',
      '؉',
      '؊',
      '؋',
      '؍',
      '؎',
      'ؐ',
      'ؑ',
      'ؒ',
      'ؓ',
      'ؔ',
      'ؕ',
      'ؖ',
      'ؗ',
      'ؘ',
      'ؙ',
      'ؚ',
      '؛',
      '؞',
      '؟',
      'ء',
      'ػ',
      'ؼ',
      'ؽ',
      'ؾ',
      'ؿ',
      'ـ',
      'ً',
      'ٌ',
      'ٍ',
      'َ',
      'ُ',
      'ِ',
      'ّ',
      'ْ',
      'ٓ',
      'ٔ',
      'ٕ',
      'ٖ',
      'ٗ',
      '٘',
      'ٙ',
      'ٚ',
      'ٛ',
      'ٜ',
      'ٝ',
      'ٞ',
      '٠',
      '٪',
      '٫',
      '٬',
      'ٯ',
      'ٰ',
      'ٲ',
      '۔',
      'ە',
      'ۖ',
      'ۗ',
      'ۘ',
      'ۙ',
      'ۚ',
      'ۛ',
      'ۜ',
      '۟',
      '۠',
      'ۡ',
      'ۢ',
      'ۣ',
      'ۤ',
      'ۥ',
      'ۦ',
      'ۧ',
      'ۨ',
      '۩',
      '۪',
      '۫',
      '۬',
      'ۭ',
      'ۮ',
      'ۯ',
      'ۖ',
      'ۗ',
      'ۘ',
      'ۙ',
      'ۚ',
      'ۛ',
      'ۜ',
      '\u06dd',
      '۞',
      '۟',
      '۰',
      '۽',
      'ﹰ',
      'ﹱ',
      'ﹲ',
      'ﹳ',
      'ﹴ',
      '\ufe75',
      'ﹶ',
      'ﹷ',
      'ﹸ',
      'ﹹ',
      'ﹺ',
      'ﹻ',
      'ﹼ',
      'ﹽ',
      'ﹾ',
      'ﹿ',
      'ﱞ',
      'ﱟ',
      'ﱠ',
      'ﱡ',
      'ﱢ',
      'ﱣ'
   };
   public static char[][] ARABIC_GLPHIES = new char[][]{
      {'آ', 'ﺁ', 'ﺁ', 'ﺂ', 'ﺂ', '\u0002'},
      {'أ', 'ﺃ', 'ﺃ', 'ﺄ', 'ﺄ', '\u0002'},
      {'ؤ', 'ﺅ', 'ﺅ', 'ﺆ', 'ﺆ', '\u0002'},
      {'إ', 'ﺇ', 'ﺇ', 'ﺈ', 'ﺈ', '\u0002'},
      {'ئ', 'ﺉ', 'ﺋ', 'ﺌ', 'ﺊ', '\u0004'},
      {'ا', 'ا', 'ا', 'ﺎ', 'ﺎ', '\u0002'},
      {'ب', 'ﺏ', 'ﺑ', 'ﺒ', 'ﺐ', '\u0004'},
      {'ة', 'ﺓ', 'ﺓ', 'ﺔ', 'ﺔ', '\u0002'},
      {'ت', 'ﺕ', 'ﺗ', 'ﺘ', 'ﺖ', '\u0004'},
      {'ث', 'ﺙ', 'ﺛ', 'ﺜ', 'ﺚ', '\u0004'},
      {'ج', 'ﺝ', 'ﺟ', 'ﺠ', 'ﺞ', '\u0004'},
      {'ح', 'ﺡ', 'ﺣ', 'ﺤ', 'ﺢ', '\u0004'},
      {'خ', 'ﺥ', 'ﺧ', 'ﺨ', 'ﺦ', '\u0004'},
      {'د', 'ﺩ', 'ﺩ', 'ﺪ', 'ﺪ', '\u0002'},
      {'ذ', 'ﺫ', 'ﺫ', 'ﺬ', 'ﺬ', '\u0002'},
      {'ر', 'ﺭ', 'ﺭ', 'ﺮ', 'ﺮ', '\u0002'},
      {'ز', 'ﺯ', 'ﺯ', 'ﺰ', 'ﺰ', '\u0002'},
      {'س', 'ﺱ', 'ﺳ', 'ﺴ', 'ﺲ', '\u0004'},
      {'ش', 'ﺵ', 'ﺷ', 'ﺸ', 'ﺶ', '\u0004'},
      {'ص', 'ﺹ', 'ﺻ', 'ﺼ', 'ﺺ', '\u0004'},
      {'ض', 'ﺽ', 'ﺿ', 'ﻀ', 'ﺾ', '\u0004'},
      {'ط', 'ﻁ', 'ﻃ', 'ﻄ', 'ﻂ', '\u0004'},
      {'ظ', 'ﻅ', 'ﻇ', 'ﻈ', 'ﻆ', '\u0004'},
      {'ع', 'ﻉ', 'ﻋ', 'ﻌ', 'ﻊ', '\u0004'},
      {'غ', 'ﻍ', 'ﻏ', 'ﻐ', 'ﻎ', '\u0004'},
      {'ف', 'ﻑ', 'ﻓ', 'ﻔ', 'ﻒ', '\u0004'},
      {'ق', 'ﻕ', 'ﻗ', 'ﻘ', 'ﻖ', '\u0004'},
      {'ك', 'ﻙ', 'ﻛ', 'ﻜ', 'ﻚ', '\u0004'},
      {'ل', 'ﻝ', 'ﻟ', 'ﻠ', 'ﻞ', '\u0004'},
      {'م', 'ﻡ', 'ﻣ', 'ﻤ', 'ﻢ', '\u0004'},
      {'ن', 'ﻥ', 'ﻧ', 'ﻨ', 'ﻦ', '\u0004'},
      {'ه', 'ﻩ', 'ﻫ', 'ﻬ', 'ﻪ', '\u0004'},
      {'و', 'ﻭ', 'ﻭ', 'ﻮ', 'ﻮ', '\u0002'},
      {'ى', 'ﻯ', 'ﻯ', 'ﻰ', 'ﻰ', '\u0002'},
      {'ٱ', 'ٱ', 'ٱ', 'ﭑ', 'ﭑ', '\u0002'},
      {'ي', 'ﻱ', 'ﻳ', 'ﻴ', 'ﻲ', '\u0004'},
      {'ٮ', 'ﯤ', 'ﯨ', 'ﯩ', 'ﯥ', '\u0004'},
      {'ٱ', 'ٱ', 'ٱ', 'ﭑ', 'ﭑ', '\u0002'},
      {'ڪ', 'ﮎ', 'ﮐ', 'ﮑ', 'ﮏ', '\u0004'},
      {'ہ', 'ﮦ', 'ﮨ', 'ﮩ', 'ﮧ', '\u0004'},
      {'ۤ', 'ۤ', 'ۤ', 'ۤ', 'ﻮ', '\u0002'}
   };

   public String getReshapedWord() {
      return this._returnString;
   }

   private char getReshapedGlphy(char target, int location) {
      for(char[] element : ARABIC_GLPHIES) {
         if (element[0] == target) {
            return element[location];
         }
      }

      return target;
   }

   private int getGlphyType(char target) {
      for(char[] element : ARABIC_GLPHIES) {
         if (element[0] == target) {
            return element[5];
         }
      }

      return 2;
   }

   public static int getCase(char ch) {
      return ch >= 1569 && ch <= 1746 ? ARABIC_GLPHIES[ch - 1569][1] : 0;
   }

   public static char getShape(char ch, int which_shape) {
      return ARABIC_GLPHIES[ch - 1569][2 + which_shape];
   }

   public static String reshape_reverse(String Str) {
      String Temp = " " + Str + "   ";
      StringBuilder reshapedString = new StringBuilder();
      int i = 0;
      int len = Str.length();

      while(i < len) {
         char pre = Temp.charAt(i + 2);
         char at = Temp.charAt(i + 1);
         char post = Temp.charAt(i);
         int which_case = getCase(at);
         int what_case_post = getCase(post);
         int what_case_pre = getCase(pre);
         int pre_step = 0;
         if (what_case_pre == 10) {
            pre = Temp.charAt(i + 3);
            what_case_pre = getCase(pre);
         }

         if ((what_case_pre & 128) == 128) {
            pre_step = 1;
         }

         switch(which_case & 15) {
            case 0:
            case 15:
               reshapedString.append(at);
               ++i;
               break;
            case 2:
            case 5:
            case 9:
            case 11:
            case 13:
            case 14:
            default:
               reshapedString.append(getShape(at, 0));
               ++i;
               break;
            case 4:
               reshapedString.append(getShape(at, pre_step));
               ++i;
               break;
            case 6:
               if ((what_case_pre & 15) == 3) {
                  pre = Temp.charAt(i + 3);
                  what_case_pre = getCase(pre);
                  int var17 = 0;
                  if ((what_case_pre & 128) == 128) {
                     var17 = 1;
                  }

                  reshapedString.append(getShape(at, var17 + 2));
                  i += 2;
                  break;
               }
            case 1:
            case 3:
               if ((what_case_post & 2048) == 2048) {
                  reshapedString.append(getShape(at, 2 + pre_step));
                  ++i;
               } else if (what_case_post == 12) {
                  reshapedString.append(getShape(at, pre_step));
                  ++i;
               } else if (what_case_post == 10) {
                  char post_post = Temp.charAt(i + 3);
                  int what_case_post_post = getCase(post_post);
                  if ((what_case_post_post & 2048) == 2048) {
                     reshapedString.append(getShape(at, 2 + pre_step));
                     ++i;
                  } else {
                     reshapedString.append(getShape(at, pre_step));
                     ++i;
                  }
               } else {
                  reshapedString.append(getShape(at, pre_step));
                  ++i;
               }
               break;
            case 7:
            case 8:
               reshapedString.append(getShape(at, 0));
               ++i;
               break;
            case 10:
               reshapedString.append(getShape(at, 0));
               ++i;
               break;
            case 12:
               reshapedString.append(getShape(at, 0));
               ++i;
         }
      }

      return reshapedString.toString();
   }

   boolean isHaraka(char target) {
      for(char element : HARAKATE) {
         if (element == target) {
            return true;
         }
      }

      return false;
   }

   private String replaceLamAlef(String unshapedWord) {
      int wordLength = unshapedWord.length();
      char[] wordLetters = new char[wordLength];
      unshapedWord.getChars(0, wordLength, wordLetters, 0);
      char letterBefore = 0;

      for(int index = 0; index < wordLetters.length - 1; ++index) {
         if (!this.isHaraka(wordLetters[index]) && DEFINED_CHARACTERS_ORGINAL_LAM != wordLetters[index]) {
            letterBefore = wordLetters[index];
         }

         if (DEFINED_CHARACTERS_ORGINAL_LAM == wordLetters[index]) {
            char candidateLam = wordLetters[index];
            int harakaPosition = index + 1;

            while(harakaPosition < wordLetters.length && this.isHaraka(wordLetters[harakaPosition])) {
               ++harakaPosition;
            }

            if (harakaPosition < wordLetters.length) {
               char lamAlef = '\u0000';
               if (index > 0 && this.getGlphyType(letterBefore) > 2) {
                  lamAlef = this.getLamAlef(wordLetters[harakaPosition], candidateLam, false);
               } else {
                  lamAlef = this.getLamAlef(wordLetters[harakaPosition], candidateLam, true);
               }

               if (lamAlef != 0) {
                  wordLetters[index] = lamAlef;
                  wordLetters[harakaPosition] = ' ';
               }
            }
         }
      }

      unshapedWord = new String(wordLetters);
      unshapedWord = unshapedWord.replaceAll(" ", "");
      return unshapedWord.trim();
   }

   private char getLamAlef(char candidateAlef, char candidateLam, boolean isEndOfWord) {
      int shiftRate = 1;
      char reshapedLamAlef = 0;
      if (isEndOfWord) {
         ++shiftRate;
      }

      if (DEFINED_CHARACTERS_ORGINAL_LAM == candidateLam) {
         if (candidateAlef == DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_MDD) {
            reshapedLamAlef = LAM_ALEF_GLPHIES[0][shiftRate];
         }

         if (candidateAlef == DEFINED_CHARACTERS_ORGINAL_ALF_UPPER_HAMAZA) {
            reshapedLamAlef = LAM_ALEF_GLPHIES[1][shiftRate];
         }

         if (candidateAlef == DEFINED_CHARACTERS_ORGINAL_ALF_LOWER_HAMAZA) {
            reshapedLamAlef = LAM_ALEF_GLPHIES[3][shiftRate];
         }

         if (candidateAlef == DEFINED_CHARACTERS_ORGINAL_ALF) {
            reshapedLamAlef = LAM_ALEF_GLPHIES[2][shiftRate];
         }
      }

      return reshapedLamAlef;
   }

   public ArabicReshaper(String unshapedWord) {
      unshapedWord = this.replaceLamAlef(unshapedWord);
      ArabicReshaper.DecomposedWord decomposedWord = new ArabicReshaper.DecomposedWord(unshapedWord);
      if (decomposedWord.stripedRegularLetters.length > 0) {
         this._returnString = this.reshapeIt(new String(decomposedWord.stripedRegularLetters));
      }

      this._returnString = decomposedWord.reconstructWord(this._returnString);
   }

   public String reshapeIt(String unshapedWord) {
      StringBuffer reshapedWord = new StringBuffer("");
      int wordLength = unshapedWord.length();
      char[] wordLetters = new char[wordLength];
      unshapedWord.getChars(0, wordLength, wordLetters, 0);
      reshapedWord.append(this.getReshapedGlphy(wordLetters[0], 2));

      for(int i = 1; i < wordLength - 1; ++i) {
         int beforeLast = i - 1;
         if (this.getGlphyType(wordLetters[beforeLast]) == 2) {
            reshapedWord.append(this.getReshapedGlphy(wordLetters[i], 2));
         } else {
            reshapedWord.append(this.getReshapedGlphy(wordLetters[i], 3));
         }
      }

      if (wordLength >= 2) {
         if (this.getGlphyType(wordLetters[wordLength - 2]) == 2) {
            reshapedWord.append(this.getReshapedGlphy(wordLetters[wordLength - 1], 1));
         } else {
            reshapedWord.append(this.getReshapedGlphy(wordLetters[wordLength - 1], 4));
         }
      }

      return reshapedWord.toString();
   }

   class DecomposedWord {
      char[] stripedHarakates;
      int[] harakatesPositions;
      char[] stripedRegularLetters;
      int[] lettersPositions;

      DecomposedWord(String unshapedWord) {
         int wordLength = unshapedWord.length();
         int harakatesCount = 0;

         for(int index = 0; index < wordLength; ++index) {
            if (ArabicReshaper.this.isHaraka(unshapedWord.charAt(index))) {
               ++harakatesCount;
            }
         }

         this.harakatesPositions = new int[harakatesCount];
         this.stripedHarakates = new char[harakatesCount];
         this.lettersPositions = new int[wordLength - harakatesCount];
         this.stripedRegularLetters = new char[wordLength - harakatesCount];
         harakatesCount = 0;
         int letterCount = 0;

         for(int index = 0; index < unshapedWord.length(); ++index) {
            if (ArabicReshaper.this.isHaraka(unshapedWord.charAt(index))) {
               this.harakatesPositions[harakatesCount] = index;
               this.stripedHarakates[harakatesCount] = unshapedWord.charAt(index);
               ++harakatesCount;
            } else {
               this.lettersPositions[letterCount] = index;
               this.stripedRegularLetters[letterCount] = unshapedWord.charAt(index);
               ++letterCount;
            }
         }
      }

      String reconstructWord(String reshapedWord) {
         char[] wordWithHarakates = null;
         wordWithHarakates = new char[reshapedWord.length() + this.stripedHarakates.length];

         for(int index = 0; index < this.lettersPositions.length; ++index) {
            wordWithHarakates[this.lettersPositions[index]] = reshapedWord.charAt(index);
         }

         for(int index = 0; index < this.harakatesPositions.length; ++index) {
            wordWithHarakates[this.harakatesPositions[index]] = this.stripedHarakates[index];
         }

         return new String(wordWithHarakates);
      }
   }
}
