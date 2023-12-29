package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.CharOperation;

public interface JavadocTagConstants {
   char[] TAG_DEPRECATED = "deprecated".toCharArray();
   char[] TAG_PARAM = "param".toCharArray();
   char[] TAG_RETURN = "return".toCharArray();
   char[] TAG_THROWS = "throws".toCharArray();
   char[] TAG_EXCEPTION = "exception".toCharArray();
   char[] TAG_SEE = "see".toCharArray();
   char[] TAG_LINK = "link".toCharArray();
   char[] TAG_LINKPLAIN = "linkplain".toCharArray();
   char[] TAG_INHERITDOC = "inheritDoc".toCharArray();
   char[] TAG_VALUE = "value".toCharArray();
   char[] TAG_AUTHOR = "author".toCharArray();
   char[] TAG_CODE = "code".toCharArray();
   char[] TAG_DOC_ROOT = "docRoot".toCharArray();
   char[] TAG_LITERAL = "literal".toCharArray();
   char[] TAG_SERIAL = "serial".toCharArray();
   char[] TAG_SERIAL_DATA = "serialData".toCharArray();
   char[] TAG_SERIAL_FIELD = "serialField".toCharArray();
   char[] TAG_SINCE = "since".toCharArray();
   char[] TAG_VERSION = "version".toCharArray();
   char[] TAG_CATEGORY = "category".toCharArray();
   int TAG_DEPRECATED_LENGTH = TAG_DEPRECATED.length;
   int TAG_PARAM_LENGTH = TAG_PARAM.length;
   int TAG_RETURN_LENGTH = TAG_RETURN.length;
   int TAG_THROWS_LENGTH = TAG_THROWS.length;
   int TAG_EXCEPTION_LENGTH = TAG_EXCEPTION.length;
   int TAG_SEE_LENGTH = TAG_SEE.length;
   int TAG_LINK_LENGTH = TAG_LINK.length;
   int TAG_LINKPLAIN_LENGTH = TAG_LINKPLAIN.length;
   int TAG_INHERITDOC_LENGTH = TAG_INHERITDOC.length;
   int TAG_VALUE_LENGTH = TAG_VALUE.length;
   int TAG_CATEGORY_LENGTH = TAG_CATEGORY.length;
   int TAG_AUTHOR_LENGTH = TAG_AUTHOR.length;
   int TAG_SERIAL_LENGTH = TAG_SERIAL.length;
   int TAG_SERIAL_DATA_LENGTH = TAG_SERIAL_DATA.length;
   int TAG_SERIAL_FIELD_LENGTH = TAG_SERIAL_FIELD.length;
   int TAG_SINCE_LENGTH = TAG_SINCE.length;
   int TAG_VERSION_LENGTH = TAG_VERSION.length;
   int TAG_CODE_LENGTH = TAG_CODE.length;
   int TAG_LITERAL_LENGTH = TAG_LITERAL.length;
   int TAG_DOC_ROOT_LENGTH = TAG_DOC_ROOT.length;
   int NO_TAG_VALUE = 0;
   int TAG_DEPRECATED_VALUE = 1;
   int TAG_PARAM_VALUE = 2;
   int TAG_RETURN_VALUE = 3;
   int TAG_THROWS_VALUE = 4;
   int TAG_EXCEPTION_VALUE = 5;
   int TAG_SEE_VALUE = 6;
   int TAG_LINK_VALUE = 7;
   int TAG_LINKPLAIN_VALUE = 8;
   int TAG_INHERITDOC_VALUE = 9;
   int TAG_VALUE_VALUE = 10;
   int TAG_CATEGORY_VALUE = 11;
   int TAG_AUTHOR_VALUE = 12;
   int TAG_SERIAL_VALUE = 13;
   int TAG_SERIAL_DATA_VALUE = 14;
   int TAG_SERIAL_FIELD_VALUE = 15;
   int TAG_SINCE_VALUE = 16;
   int TAG_VERSION_VALUE = 17;
   int TAG_CODE_VALUE = 18;
   int TAG_LITERAL_VALUE = 19;
   int TAG_DOC_ROOT_VALUE = 20;
   int TAG_OTHERS_VALUE = 100;
   char[][] TAG_NAMES = new char[][]{
      CharOperation.NO_CHAR,
      TAG_DEPRECATED,
      TAG_PARAM,
      TAG_RETURN,
      TAG_THROWS,
      TAG_EXCEPTION,
      TAG_SEE,
      TAG_LINK,
      TAG_LINKPLAIN,
      TAG_INHERITDOC,
      TAG_VALUE,
      TAG_CATEGORY,
      TAG_AUTHOR,
      TAG_SERIAL,
      TAG_SERIAL_DATA,
      TAG_SERIAL_FIELD,
      TAG_SINCE,
      TAG_VERSION,
      TAG_CODE,
      TAG_LITERAL,
      TAG_DOC_ROOT
   };
   int ORDERED_TAGS_NUMBER = 3;
   int PARAM_TAG_EXPECTED_ORDER = 0;
   int THROWS_TAG_EXPECTED_ORDER = 1;
   int SEE_TAG_EXPECTED_ORDER = 2;
   int BLOCK_IDX = 0;
   int INLINE_IDX = 1;
   char[] HREF_TAG = new char[]{'h', 'r', 'e', 'f'};
   char[][][] BLOCK_TAGS = new char[][][]{
      {TAG_AUTHOR, TAG_DEPRECATED, TAG_EXCEPTION, TAG_PARAM, TAG_RETURN, TAG_SEE, TAG_VERSION, TAG_CATEGORY},
      {TAG_SINCE},
      {TAG_SERIAL, TAG_SERIAL_DATA, TAG_SERIAL_FIELD, TAG_THROWS},
      new char[0][],
      new char[0][],
      new char[0][],
      new char[0][],
      new char[0][],
      new char[0][]
   };
   char[][][] INLINE_TAGS = new char[][][]{
      new char[0][],
      new char[0][],
      {TAG_LINK},
      {TAG_DOC_ROOT},
      {TAG_INHERITDOC, TAG_LINKPLAIN, TAG_VALUE},
      {TAG_CODE, TAG_LITERAL},
      new char[0][],
      new char[0][],
      new char[0][]
   };
   int INLINE_TAGS_LENGTH = INLINE_TAGS.length;
   int BLOCK_TAGS_LENGTH = BLOCK_TAGS.length;
   int ALL_TAGS_LENGTH = BLOCK_TAGS_LENGTH + INLINE_TAGS_LENGTH;
   short TAG_TYPE_NONE = 0;
   short TAG_TYPE_INLINE = 1;
   short TAG_TYPE_BLOCK = 2;
   short[] JAVADOC_TAG_TYPE = new short[]{0, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1};
   char[][] PACKAGE_TAGS = new char[][]{
      TAG_SEE, TAG_SINCE, TAG_SERIAL, TAG_AUTHOR, TAG_VERSION, TAG_CATEGORY, TAG_LINK, TAG_LINKPLAIN, TAG_DOC_ROOT, TAG_VALUE
   };
   char[][] COMPILATION_UNIT_TAGS = new char[0][];
   char[][] CLASS_TAGS = new char[][]{
      TAG_SEE,
      TAG_SINCE,
      TAG_DEPRECATED,
      TAG_SERIAL,
      TAG_AUTHOR,
      TAG_VERSION,
      TAG_PARAM,
      TAG_CATEGORY,
      TAG_LINK,
      TAG_LINKPLAIN,
      TAG_DOC_ROOT,
      TAG_VALUE,
      TAG_CODE,
      TAG_LITERAL
   };
   char[][] FIELD_TAGS = new char[][]{
      TAG_SEE, TAG_SINCE, TAG_DEPRECATED, TAG_SERIAL, TAG_SERIAL_FIELD, TAG_CATEGORY, TAG_LINK, TAG_LINKPLAIN, TAG_DOC_ROOT, TAG_VALUE, TAG_CODE, TAG_LITERAL
   };
   char[][] METHOD_TAGS = new char[][]{
      TAG_SEE,
      TAG_SINCE,
      TAG_DEPRECATED,
      TAG_PARAM,
      TAG_RETURN,
      TAG_THROWS,
      TAG_EXCEPTION,
      TAG_SERIAL_DATA,
      TAG_CATEGORY,
      TAG_LINK,
      TAG_LINKPLAIN,
      TAG_INHERITDOC,
      TAG_DOC_ROOT,
      TAG_VALUE,
      TAG_CODE,
      TAG_LITERAL
   };
}
