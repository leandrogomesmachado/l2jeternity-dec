package org.eclipse.jdt.internal.compiler.parser;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ReadManager;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CombinedBinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntersectionCastTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Receiver;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.UnionTypeReference;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.diagnose.DiagnoseParser;
import org.eclipse.jdt.internal.compiler.parser.diagnose.RangeUtil;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Parser implements TerminalTokens, ParserBasicInformation, ConflictedParser, OperatorIds, TypeIds {
   protected static final int THIS_CALL = 3;
   protected static final int SUPER_CALL = 2;
   public static final char[] FALL_THROUGH_TAG = "$FALL-THROUGH$".toCharArray();
   public static final char[] CASES_OMITTED_TAG = "$CASES-OMITTED$".toCharArray();
   public static char[] asb = null;
   public static char[] asr = null;
   protected static final int AstStackIncrement = 100;
   public static char[] base_action = null;
   public static final int BracketKinds = 3;
   public static short[] check_table = null;
   public static final int CurlyBracket = 2;
   private static final boolean DEBUG = false;
   private static final boolean DEBUG_AUTOMATON = false;
   private static final String EOF_TOKEN = "$eof";
   private static final String ERROR_TOKEN = "$error";
   protected static final int ExpressionStackIncrement = 100;
   protected static final int GenericsStackIncrement = 10;
   private static final String FILEPREFIX = "parser";
   public static char[] in_symb = null;
   private static final String INVALID_CHARACTER = "Invalid Character";
   public static char[] lhs = null;
   public static String[] name = null;
   public static char[] nasb = null;
   public static char[] nasr = null;
   public static char[] non_terminal_index = null;
   private static final String READABLE_NAMES_FILE = "readableNames";
   public static String[] readableName = null;
   public static byte[] rhs = null;
   public static int[] reverse_index = null;
   public static char[] recovery_templates_index = null;
   public static char[] recovery_templates = null;
   public static char[] statements_recovery_filter = null;
   public static long[] rules_compliance = null;
   public static final int RoundBracket = 0;
   public static byte[] scope_la = null;
   public static char[] scope_lhs = null;
   public static char[] scope_prefix = null;
   public static char[] scope_rhs = null;
   public static char[] scope_state = null;
   public static char[] scope_state_set = null;
   public static char[] scope_suffix = null;
   public static final int SquareBracket = 1;
   protected static final int StackIncrement = 255;
   public static char[] term_action = null;
   public static byte[] term_check = null;
   public static char[] terminal_index = null;
   private static final String UNEXPECTED_EOF = "Unexpected End Of File";
   public static boolean VERBOSE_RECOVERY = false;
   protected static final int HALT = 0;
   protected static final int RESTART = 1;
   protected static final int RESUME = 2;
   public Scanner scanner;
   public int currentToken;
   protected int astLengthPtr;
   protected int[] astLengthStack;
   protected int astPtr;
   protected ASTNode[] astStack = new ASTNode[100];
   public CompilationUnitDeclaration compilationUnit;
   protected RecoveredElement currentElement;
   protected boolean diet = false;
   protected int dietInt = 0;
   protected int endPosition;
   protected int endStatementPosition;
   protected int expressionLengthPtr;
   protected int[] expressionLengthStack;
   protected int expressionPtr;
   protected Expression[] expressionStack = new Expression[100];
   protected int rBracketPosition;
   public int firstToken;
   protected int typeAnnotationPtr;
   protected int typeAnnotationLengthPtr;
   protected Annotation[] typeAnnotationStack = new Annotation[100];
   protected int[] typeAnnotationLengthStack;
   protected static final int TypeAnnotationStackIncrement = 100;
   protected int genericsIdentifiersLengthPtr;
   protected int[] genericsIdentifiersLengthStack = new int[10];
   protected int genericsLengthPtr;
   protected int[] genericsLengthStack = new int[10];
   protected int genericsPtr;
   protected ASTNode[] genericsStack = new ASTNode[10];
   protected boolean hasError;
   protected boolean hasReportedError;
   protected int identifierLengthPtr;
   protected int[] identifierLengthStack;
   protected long[] identifierPositionStack;
   protected int identifierPtr;
   protected char[][] identifierStack;
   protected boolean ignoreNextOpeningBrace;
   protected boolean ignoreNextClosingBrace;
   protected int intPtr;
   protected int[] intStack;
   public int lastAct;
   protected int lastCheckPoint;
   protected int lastErrorEndPosition;
   protected int lastErrorEndPositionBeforeRecovery = -1;
   protected int lastIgnoredToken;
   protected int nextIgnoredToken;
   protected int listLength;
   protected int listTypeParameterLength;
   protected int lParenPos;
   protected int rParenPos;
   protected int modifiers;
   protected int modifiersSourceStart;
   protected int colonColonStart = -1;
   protected int[] nestedMethod;
   protected int forStartPosition = 0;
   protected int nestedType;
   protected int dimensions;
   ASTNode[] noAstNodes = new ASTNode[100];
   Expression[] noExpressions = new Expression[100];
   protected boolean optimizeStringLiterals = true;
   protected CompilerOptions options;
   protected ProblemReporter problemReporter;
   protected int rBraceStart;
   protected int rBraceEnd;
   protected int rBraceSuccessorStart;
   protected int realBlockPtr;
   protected int[] realBlockStack;
   protected int recoveredStaticInitializerStart;
   public ReferenceContext referenceContext;
   public boolean reportOnlyOneSyntaxError = false;
   public boolean reportSyntaxErrorIsRequired = true;
   protected boolean restartRecovery;
   protected boolean annotationRecoveryActivated = true;
   protected int lastPosistion;
   public boolean methodRecoveryActivated = false;
   protected boolean statementRecoveryActivated = false;
   protected TypeDeclaration[] recoveredTypes;
   protected int recoveredTypePtr;
   protected int nextTypeStart;
   protected TypeDeclaration pendingRecoveredType;
   public RecoveryScanner recoveryScanner;
   protected int[] stack = new int[255];
   protected int stateStackTop;
   protected int synchronizedBlockSourceStart;
   protected int[] variablesCounter;
   protected boolean checkExternalizeStrings;
   protected boolean recordStringLiterals;
   public Javadoc javadoc;
   public JavadocParser javadocParser;
   protected int lastJavadocEnd;
   public ReadManager readManager;
   protected int valueLambdaNestDepth = -1;
   private int[] stateStackLengthStack = new int[0];
   protected boolean parsingJava8Plus;
   protected int unstackedAct = 16382;
   private boolean haltOnSyntaxError = false;
   private boolean tolerateDefaultClassMethods = false;
   private boolean processingLambdaParameterList = false;
   private boolean expectTypeAnnotation = false;
   private boolean reparsingLambdaExpression = false;

   static {
      try {
         initTables();
      } catch (IOException var1) {
         throw new ExceptionInInitializerError(var1.getMessage());
      }
   }

   public static int asi(int state) {
      return asb[original_state(state)];
   }

   public static final short base_check(int i) {
      return check_table[i - 801];
   }

   private static final void buildFile(String filename, List listToDump) {
      BufferedWriter writer = null;

      try {
         writer = new BufferedWriter(new FileWriter(filename));
         Iterator iterator = listToDump.iterator();

         while(iterator.hasNext()) {
            writer.write(String.valueOf(iterator.next()));
         }

         writer.flush();
      } catch (IOException var11) {
      } finally {
         if (writer != null) {
            try {
               writer.close();
            } catch (IOException var10) {
            }
         }
      }

      System.out.println(filename + " creation complete");
   }

   private static void buildFileForCompliance(String file, int length, String[] tokens) {
      byte[] result = new byte[length * 8];

      for(int i = 0; i < tokens.length; i += 3) {
         if ("2".equals(tokens[i])) {
            int index = Integer.parseInt(tokens[i + 1]);
            String token = tokens[i + 2].trim();
            long compliance = 0L;
            if ("1.4".equals(token)) {
               compliance = 3145728L;
            } else if ("1.5".equals(token)) {
               compliance = 3211264L;
            } else if ("1.6".equals(token)) {
               compliance = 3276800L;
            } else if ("1.7".equals(token)) {
               compliance = 3342336L;
            } else if ("1.8".equals(token)) {
               compliance = 3407872L;
            } else if ("recovery".equals(token)) {
               compliance = Long.MAX_VALUE;
            }

            int j = index * 8;
            result[j] = (byte)((int)(compliance >>> 56));
            result[j + 1] = (byte)((int)(compliance >>> 48));
            result[j + 2] = (byte)((int)(compliance >>> 40));
            result[j + 3] = (byte)((int)(compliance >>> 32));
            result[j + 4] = (byte)((int)(compliance >>> 24));
            result[j + 5] = (byte)((int)(compliance >>> 16));
            result[j + 6] = (byte)((int)(compliance >>> 8));
            result[j + 7] = (byte)((int)compliance);
         }
      }

      buildFileForTable(file, result);
   }

   private static final String[] buildFileForName(String filename, String contents) {
      String[] result = new String[contents.length()];
      result[0] = null;
      int resultCount = 1;
      StringBuffer buffer = new StringBuffer();
      int start = contents.indexOf("name[]");
      start = contents.indexOf(34, start);
      int end = contents.indexOf("};", start);
      contents = contents.substring(start, end);
      boolean addLineSeparator = false;
      int tokenStart = -1;
      StringBuffer currentToken = new StringBuffer();

      for(int i = 0; i < contents.length(); ++i) {
         char c = contents.charAt(i);
         if (c == '"') {
            if (tokenStart == -1) {
               tokenStart = i + 1;
            } else {
               if (addLineSeparator) {
                  buffer.append('\n');
                  result[resultCount++] = currentToken.toString();
                  currentToken = new StringBuffer();
               }

               String token = contents.substring(tokenStart, i);
               if (token.equals("$error")) {
                  token = "Invalid Character";
               } else if (token.equals("$eof")) {
                  token = "Unexpected End Of File";
               }

               buffer.append(token);
               currentToken.append(token);
               addLineSeparator = true;
               tokenStart = -1;
            }
         }

         if (tokenStart == -1 && c == '+') {
            addLineSeparator = false;
         }
      }

      if (currentToken.length() > 0) {
         result[resultCount++] = currentToken.toString();
      }

      buildFileForTable(filename, buffer.toString().toCharArray());
      String[] var14;
      System.arraycopy(result, 0, var14 = new String[resultCount], 0, resultCount);
      return var14;
   }

   private static void buildFileForReadableName(String file, char[] newLhs, char[] newNonTerminalIndex, String[] newName, String[] tokens) {
      ArrayList entries = new ArrayList();
      boolean[] alreadyAdded = new boolean[newName.length];

      for(int i = 0; i < tokens.length; i += 3) {
         if ("1".equals(tokens[i])) {
            int index = newNonTerminalIndex[newLhs[Integer.parseInt(tokens[i + 1])]];
            StringBuffer buffer = new StringBuffer();
            if (!alreadyAdded[index]) {
               alreadyAdded[index] = true;
               buffer.append(newName[index]);
               buffer.append('=');
               buffer.append(tokens[i + 2].trim());
               buffer.append('\n');
               entries.add(String.valueOf(buffer));
            }
         }
      }

      int i = 1;

      while(!"Invalid Character".equals(newName[i])) {
         ++i;
      }

      ++i;

      for(; i < alreadyAdded.length; ++i) {
         if (!alreadyAdded[i]) {
            System.out.println(newName[i] + " has no readable name");
         }
      }

      Collections.sort(entries);
      buildFile(file, entries);
   }

   private static final void buildFileForTable(String filename, byte[] bytes) {
      FileOutputStream stream = null;

      try {
         stream = new FileOutputStream(filename);
         stream.write(bytes);
      } catch (IOException var10) {
      } finally {
         if (stream != null) {
            try {
               stream.close();
            } catch (IOException var9) {
            }
         }
      }

      System.out.println(filename + " creation complete");
   }

   private static final void buildFileForTable(String filename, char[] chars) {
      byte[] bytes = new byte[chars.length * 2];

      for(int i = 0; i < chars.length; ++i) {
         bytes[2 * i] = (byte)(chars[i] >>> '\b');
         bytes[2 * i + 1] = (byte)(chars[i] & 255);
      }

      FileOutputStream stream = null;

      try {
         stream = new FileOutputStream(filename);
         stream.write(bytes);
      } catch (IOException var11) {
      } finally {
         if (stream != null) {
            try {
               stream.close();
            } catch (IOException var10) {
            }
         }
      }

      System.out.println(filename + " creation complete");
   }

   private static final byte[] buildFileOfByteFor(String filename, String tag, String[] tokens) {
      int i = 0;

      while(!tokens[i++].equals(tag)) {
      }

      byte[] bytes = new byte[tokens.length];

      int ic;
      String token;
      int c;
      for(ic = 0; !(token = tokens[i++]).equals("}"); bytes[ic++] = (byte)c) {
         c = Integer.parseInt(token);
      }

      byte[] var8;
      System.arraycopy(bytes, 0, var8 = new byte[ic], 0, ic);
      buildFileForTable(filename, var8);
      return var8;
   }

   private static final char[] buildFileOfIntFor(String filename, String tag, String[] tokens) {
      int i = 0;

      while(!tokens[i++].equals(tag)) {
      }

      char[] chars = new char[tokens.length];

      int ic;
      String token;
      int c;
      for(ic = 0; !(token = tokens[i++]).equals("}"); chars[ic++] = (char)c) {
         c = Integer.parseInt(token);
      }

      char[] var8;
      System.arraycopy(chars, 0, var8 = new char[ic], 0, ic);
      buildFileForTable(filename, var8);
      return var8;
   }

   private static final void buildFileOfShortFor(String filename, String tag, String[] tokens) {
      int i = 0;

      while(!tokens[i++].equals(tag)) {
      }

      char[] chars = new char[tokens.length];

      int ic;
      String token;
      int c;
      for(ic = 0; !(token = tokens[i++]).equals("}"); chars[ic++] = (char)(c + 32768)) {
         c = Integer.parseInt(token);
      }

      char[] var8;
      System.arraycopy(chars, 0, var8 = new char[ic], 0, ic);
      buildFileForTable(filename, var8);
   }

   private static void buildFilesForRecoveryTemplates(
      String indexFilename, String templatesFilename, char[] newTerminalIndex, char[] newNonTerminalIndex, String[] newName, char[] newLhs, String[] tokens
   ) {
      int[] newReverse = computeReverseTable(newTerminalIndex, newNonTerminalIndex, newName);
      char[] newRecoveyTemplatesIndex = new char[newNonTerminalIndex.length];
      char[] newRecoveyTemplates = new char[newNonTerminalIndex.length];
      int newRecoveyTemplatesPtr = 0;

      for(int i = 0; i < tokens.length; i += 3) {
         if ("3".equals(tokens[i])) {
            int length = newRecoveyTemplates.length;
            if (length == newRecoveyTemplatesPtr + 1) {
               System.arraycopy(newRecoveyTemplates, 0, newRecoveyTemplates = new char[length * 2], 0, length);
            }

            newRecoveyTemplates[newRecoveyTemplatesPtr++] = 0;
            int index = newLhs[Integer.parseInt(tokens[i + 1])];
            newRecoveyTemplatesIndex[index] = (char)newRecoveyTemplatesPtr;
            String token = tokens[i + 2].trim();
            StringTokenizer st = new StringTokenizer(token, " ");
            String[] terminalNames = new String[st.countTokens()];
            int t = 0;

            while(st.hasMoreTokens()) {
               terminalNames[t++] = st.nextToken();
            }

            for(int j = 0; j < terminalNames.length; ++j) {
               int symbol = getSymbol(terminalNames[j], newName, newReverse);
               if (symbol > -1) {
                  length = newRecoveyTemplates.length;
                  if (length == newRecoveyTemplatesPtr + 1) {
                     System.arraycopy(newRecoveyTemplates, 0, newRecoveyTemplates = new char[length * 2], 0, length);
                  }

                  newRecoveyTemplates[newRecoveyTemplatesPtr++] = (char)symbol;
               }
            }
         }
      }

      newRecoveyTemplates[newRecoveyTemplatesPtr++] = 0;
      char[] var20;
      System.arraycopy(newRecoveyTemplates, 0, var20 = new char[newRecoveyTemplatesPtr], 0, newRecoveyTemplatesPtr);
      buildFileForTable(indexFilename, newRecoveyTemplatesIndex);
      buildFileForTable(templatesFilename, var20);
   }

   private static void buildFilesForStatementsRecoveryFilter(String filename, char[] newNonTerminalIndex, char[] newLhs, String[] tokens) {
      char[] newStatementsRecoveryFilter = new char[newNonTerminalIndex.length];

      for(int i = 0; i < tokens.length; i += 3) {
         if ("4".equals(tokens[i])) {
            int index = newLhs[Integer.parseInt(tokens[i + 1])];
            newStatementsRecoveryFilter[index] = 1;
         }
      }

      buildFileForTable(filename, newStatementsRecoveryFilter);
   }

   public static final void buildFilesFromLPG(String dataFilename, String dataFilename2) {
      char[] contents = CharOperation.NO_CHAR;

      try {
         contents = Util.getFileCharContent(new File(dataFilename), null);
      } catch (IOException var13) {
         System.out.println(Messages.parser_incorrectPath);
         return;
      }

      StringTokenizer st = new StringTokenizer(new String(contents), " \t\n\r[]={,;");
      String[] tokens = new String[st.countTokens()];
      int j = 0;

      while(st.hasMoreTokens()) {
         tokens[j++] = st.nextToken();
      }

      int i = 0;
      char[] newLhs = buildFileOfIntFor("parser" + ++i + ".rsc", "lhs", tokens);
      buildFileOfShortFor("parser" + ++i + ".rsc", "check_table", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "asb", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "asr", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "nasb", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "nasr", tokens);
      char[] newTerminalIndex = buildFileOfIntFor("parser" + ++i + ".rsc", "terminal_index", tokens);
      char[] newNonTerminalIndex = buildFileOfIntFor("parser" + ++i + ".rsc", "non_terminal_index", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "term_action", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_prefix", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_suffix", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_lhs", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_state_set", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_rhs", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "scope_state", tokens);
      buildFileOfIntFor("parser" + ++i + ".rsc", "in_symb", tokens);
      byte[] newRhs = buildFileOfByteFor("parser" + ++i + ".rsc", "rhs", tokens);
      buildFileOfByteFor("parser" + ++i + ".rsc", "term_check", tokens);
      buildFileOfByteFor("parser" + ++i + ".rsc", "scope_la", tokens);
      String[] newName = buildFileForName("parser" + ++i + ".rsc", new String(contents));
      contents = CharOperation.NO_CHAR;

      try {
         contents = Util.getFileCharContent(new File(dataFilename2), null);
      } catch (IOException var12) {
         System.out.println(Messages.parser_incorrectPath);
         return;
      }

      st = new StringTokenizer(new String(contents), "\t\n\r#");
      tokens = new String[st.countTokens()];
      j = 0;

      while(st.hasMoreTokens()) {
         tokens[j++] = st.nextToken();
      }

      buildFileForCompliance("parser" + ++i + ".rsc", newRhs.length, tokens);
      buildFileForReadableName("readableNames.props", newLhs, newNonTerminalIndex, newName, tokens);
      buildFilesForRecoveryTemplates("parser" + ++i + ".rsc", "parser" + ++i + ".rsc", newTerminalIndex, newNonTerminalIndex, newName, newLhs, tokens);
      buildFilesForStatementsRecoveryFilter("parser" + ++i + ".rsc", newNonTerminalIndex, newLhs, tokens);
      System.out.println(Messages.parser_moveFiles);
   }

   protected static int[] computeReverseTable(char[] newTerminalIndex, char[] newNonTerminalIndex, String[] newName) {
      int[] newReverseTable = new int[newName.length];

      label36:
      for(int j = 0; j < newName.length; ++j) {
         for(int k = 0; k < newTerminalIndex.length; ++k) {
            if (newTerminalIndex[k] == j) {
               newReverseTable[j] = k;
               continue label36;
            }
         }

         for(int k = 0; k < newNonTerminalIndex.length; ++k) {
            if (newNonTerminalIndex[k] == j) {
               newReverseTable[j] = -k;
               break;
            }
         }
      }

      return newReverseTable;
   }

   private static int getSymbol(String terminalName, String[] newName, int[] newReverse) {
      for(int j = 0; j < newName.length; ++j) {
         if (terminalName.equals(newName[j])) {
            return newReverse[j];
         }
      }

      return -1;
   }

   public static int in_symbol(int state) {
      return in_symb[original_state(state)];
   }

   public static final void initTables() throws IOException {
      int i = 0;
      lhs = readTable("parser" + ++i + ".rsc");
      char[] chars = readTable("parser" + ++i + ".rsc");
      check_table = new short[chars.length];
      int c = chars.length;

      while(c-- > 0) {
         check_table[c] = (short)(chars[c] - '耀');
      }

      asb = readTable("parser" + ++i + ".rsc");
      asr = readTable("parser" + ++i + ".rsc");
      nasb = readTable("parser" + ++i + ".rsc");
      nasr = readTable("parser" + ++i + ".rsc");
      terminal_index = readTable("parser" + ++i + ".rsc");
      non_terminal_index = readTable("parser" + ++i + ".rsc");
      term_action = readTable("parser" + ++i + ".rsc");
      scope_prefix = readTable("parser" + ++i + ".rsc");
      scope_suffix = readTable("parser" + ++i + ".rsc");
      scope_lhs = readTable("parser" + ++i + ".rsc");
      scope_state_set = readTable("parser" + ++i + ".rsc");
      scope_rhs = readTable("parser" + ++i + ".rsc");
      scope_state = readTable("parser" + ++i + ".rsc");
      in_symb = readTable("parser" + ++i + ".rsc");
      rhs = readByteTable("parser" + ++i + ".rsc");
      term_check = readByteTable("parser" + ++i + ".rsc");
      scope_la = readByteTable("parser" + ++i + ".rsc");
      name = readNameTable("parser" + ++i + ".rsc");
      rules_compliance = readLongTable("parser" + ++i + ".rsc");
      readableName = readReadableNameTable("readableNames.props");
      reverse_index = computeReverseTable(terminal_index, non_terminal_index, name);
      recovery_templates_index = readTable("parser" + ++i + ".rsc");
      recovery_templates = readTable("parser" + ++i + ".rsc");
      statements_recovery_filter = readTable("parser" + ++i + ".rsc");
      base_action = lhs;
   }

   public static int nasi(int state) {
      return nasb[original_state(state)];
   }

   public static int ntAction(int state, int sym) {
      return base_action[state + sym];
   }

   protected static int original_state(int state) {
      return -base_check(state);
   }

   protected static byte[] readByteTable(String filename) throws IOException {
      InputStream stream = Parser.class.getResourceAsStream(filename);
      if (stream == null) {
         throw new IOException(Messages.bind(Messages.parser_missingFile, filename));
      } else {
         byte[] bytes = null;

         try {
            stream = new BufferedInputStream(stream);
            bytes = Util.getInputStreamAsByteArray(stream, -1);
         } finally {
            try {
               stream.close();
            } catch (IOException var7) {
            }
         }

         return bytes;
      }
   }

   protected static long[] readLongTable(String filename) throws IOException {
      InputStream stream = Parser.class.getResourceAsStream(filename);
      if (stream == null) {
         throw new IOException(Messages.bind(Messages.parser_missingFile, filename));
      } else {
         byte[] bytes = null;

         try {
            stream = new BufferedInputStream(stream);
            bytes = Util.getInputStreamAsByteArray(stream, -1);
         } finally {
            try {
               stream.close();
            } catch (IOException var10) {
            }
         }

         int length = bytes.length;
         if (length % 8 != 0) {
            throw new IOException(Messages.bind(Messages.parser_corruptedFile, filename));
         } else {
            long[] longs = new long[length / 8];
            int i = 0;
            int longIndex = 0;

            do {
               longs[longIndex++] = ((long)(bytes[i++] & 255) << 56)
                  + ((long)(bytes[i++] & 255) << 48)
                  + ((long)(bytes[i++] & 255) << 40)
                  + ((long)(bytes[i++] & 255) << 32)
                  + ((long)(bytes[i++] & 255) << 24)
                  + ((long)(bytes[i++] & 255) << 16)
                  + ((long)(bytes[i++] & 255) << 8)
                  + (long)(bytes[i++] & 255);
            } while(i != length);

            return longs;
         }
      }
   }

   protected static String[] readNameTable(String filename) throws IOException {
      char[] contents = readTable(filename);
      char[][] nameAsChar = CharOperation.splitOn('\n', contents);
      String[] result = new String[nameAsChar.length + 1];
      result[0] = null;

      for(int i = 0; i < nameAsChar.length; ++i) {
         result[i + 1] = new String(nameAsChar[i]);
      }

      return result;
   }

   protected static String[] readReadableNameTable(String filename) {
      String[] result = new String[name.length];
      InputStream is = Parser.class.getResourceAsStream(filename);
      Properties props = new Properties();

      try {
         props.load(is);
      } catch (IOException var6) {
         return name;
      }

      for(int i = 0; i < 119; ++i) {
         result[i] = name[i];
      }

      for(int i = 118; i < name.length; ++i) {
         String n = props.getProperty(name[i]);
         if (n != null && n.length() > 0) {
            result[i] = n;
         } else {
            result[i] = name[i];
         }
      }

      return result;
   }

   protected static char[] readTable(String filename) throws IOException {
      InputStream stream = Parser.class.getResourceAsStream(filename);
      if (stream == null) {
         throw new IOException(Messages.bind(Messages.parser_missingFile, filename));
      } else {
         byte[] bytes = null;

         try {
            stream = new BufferedInputStream(stream);
            bytes = Util.getInputStreamAsByteArray(stream, -1);
         } finally {
            try {
               stream.close();
            } catch (IOException var10) {
            }
         }

         int length = bytes.length;
         if ((length & 1) != 0) {
            throw new IOException(Messages.bind(Messages.parser_corruptedFile, filename));
         } else {
            char[] chars = new char[length / 2];
            int i = 0;
            int charIndex = 0;

            do {
               chars[charIndex++] = (char)(((bytes[i++] & 255) << 8) + (bytes[i++] & 255));
            } while(i != length);

            return chars;
         }
      }
   }

   public static int tAction(int state, int sym) {
      return term_action[term_check[base_action[state] + sym] == sym ? base_action[state] + sym : base_action[state]];
   }

   public Parser() {
   }

   public Parser(ProblemReporter problemReporter, boolean optimizeStringLiterals) {
      this.problemReporter = problemReporter;
      this.options = problemReporter.options;
      this.optimizeStringLiterals = optimizeStringLiterals;
      this.initializeScanner();
      this.parsingJava8Plus = this.options.sourceLevel >= 3407872L;
      this.astLengthStack = new int[50];
      this.expressionLengthStack = new int[30];
      this.typeAnnotationLengthStack = new int[30];
      this.intStack = new int[50];
      this.identifierStack = new char[30][];
      this.identifierLengthStack = new int[30];
      this.nestedMethod = new int[30];
      this.realBlockStack = new int[30];
      this.identifierPositionStack = new long[30];
      this.variablesCounter = new int[30];
      this.javadocParser = this.createJavadocParser();
   }

   protected void annotationRecoveryCheckPoint(int start, int end) {
      if (this.lastCheckPoint < end) {
         this.lastCheckPoint = end + 1;
      }
   }

   public void arrayInitializer(int length) {
      ArrayInitializer ai = new ArrayInitializer();
      if (length != 0) {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, ai.expressions = new Expression[length], 0, length);
      }

      this.pushOnExpressionStack(ai);
      ai.sourceEnd = this.endStatementPosition;
      ai.sourceStart = this.intStack[this.intPtr--];
   }

   protected void blockReal() {
      this.realBlockStack[this.realBlockPtr]++;
   }

   public RecoveredElement buildInitialRecoveryState() {
      this.lastCheckPoint = 0;
      this.lastErrorEndPositionBeforeRecovery = this.scanner.currentPosition;
      RecoveredElement element = null;
      if (this.referenceContext instanceof CompilationUnitDeclaration) {
         element = new RecoveredUnit(this.compilationUnit, 0, this);
         this.compilationUnit.currentPackage = null;
         this.compilationUnit.imports = null;
         this.compilationUnit.types = null;
         this.currentToken = 0;
         this.listLength = 0;
         this.listTypeParameterLength = 0;
         this.endPosition = 0;
         this.endStatementPosition = 0;
         return element;
      } else {
         if (this.referenceContext instanceof AbstractMethodDeclaration) {
            element = new RecoveredMethod((AbstractMethodDeclaration)this.referenceContext, null, 0, this);
            this.lastCheckPoint = ((AbstractMethodDeclaration)this.referenceContext).bodyStart;
            if (this.statementRecoveryActivated) {
               element = element.add(new Block(0), 0);
            }
         } else if (this.referenceContext instanceof TypeDeclaration) {
            TypeDeclaration type = (TypeDeclaration)this.referenceContext;
            FieldDeclaration[] fieldDeclarations = type.fields;
            int length = fieldDeclarations == null ? 0 : fieldDeclarations.length;

            for(int i = 0; i < length; ++i) {
               FieldDeclaration field = fieldDeclarations[i];
               if (field != null
                  && field.getKind() == 2
                  && ((Initializer)field).block != null
                  && field.declarationSourceStart <= this.scanner.initialPosition
                  && this.scanner.initialPosition <= field.declarationSourceEnd
                  && this.scanner.eofPosition <= field.declarationSourceEnd + 1) {
                  element = new RecoveredInitializer(field, null, 1, this);
                  this.lastCheckPoint = field.declarationSourceStart;
                  break;
               }
            }
         }

         if (element == null) {
            return element;
         } else {
            for(int i = 0; i <= this.astPtr; ++i) {
               ASTNode node = this.astStack[i];
               if (node instanceof AbstractMethodDeclaration) {
                  AbstractMethodDeclaration method = (AbstractMethodDeclaration)node;
                  if (method.declarationSourceEnd == 0) {
                     element = element.add(method, 0);
                     this.lastCheckPoint = method.bodyStart;
                  } else {
                     element = element.add(method, 0);
                     this.lastCheckPoint = method.declarationSourceEnd + 1;
                  }
               } else if (node instanceof Initializer) {
                  Initializer initializer = (Initializer)node;
                  if (initializer.block != null) {
                     if (initializer.declarationSourceEnd == 0) {
                        element = element.add(initializer, 1);
                        this.lastCheckPoint = initializer.sourceStart;
                     } else {
                        element = element.add(initializer, 0);
                        this.lastCheckPoint = initializer.declarationSourceEnd + 1;
                     }
                  }
               } else if (node instanceof FieldDeclaration) {
                  FieldDeclaration field = (FieldDeclaration)node;
                  if (field.declarationSourceEnd == 0) {
                     element = element.add(field, 0);
                     if (field.initialization == null) {
                        this.lastCheckPoint = field.sourceEnd + 1;
                     } else {
                        this.lastCheckPoint = field.initialization.sourceEnd + 1;
                     }
                  } else {
                     element = element.add(field, 0);
                     this.lastCheckPoint = field.declarationSourceEnd + 1;
                  }
               } else if (node instanceof TypeDeclaration) {
                  TypeDeclaration type = (TypeDeclaration)node;
                  if ((type.modifiers & 16384) == 0) {
                     if (type.declarationSourceEnd == 0) {
                        element = element.add(type, 0);
                        this.lastCheckPoint = type.bodyStart;
                     } else {
                        element = element.add(type, 0);
                        this.lastCheckPoint = type.declarationSourceEnd + 1;
                     }
                  }
               } else {
                  if (node instanceof ImportReference) {
                     ImportReference importRef = (ImportReference)node;
                     element = element.add(importRef, 0);
                     this.lastCheckPoint = importRef.declarationSourceEnd + 1;
                  }

                  if (this.statementRecoveryActivated) {
                     if (node instanceof Block) {
                        Block block = (Block)node;
                        element = element.add(block, 0);
                        this.lastCheckPoint = block.sourceEnd + 1;
                     } else if (node instanceof LocalDeclaration) {
                        LocalDeclaration statement = (LocalDeclaration)node;
                        element = element.add(statement, 0);
                        this.lastCheckPoint = statement.sourceEnd + 1;
                     } else if (node instanceof Expression) {
                        if (node instanceof Assignment
                           || node instanceof PrefixExpression
                           || node instanceof PostfixExpression
                           || node instanceof MessageSend
                           || node instanceof AllocationExpression) {
                           Expression statement = (Expression)node;
                           element = element.add(statement, 0);
                           if (statement.statementEnd != -1) {
                              this.lastCheckPoint = statement.statementEnd + 1;
                           } else {
                              this.lastCheckPoint = statement.sourceEnd + 1;
                           }
                        }
                     } else if (node instanceof Statement) {
                        Statement statement = (Statement)node;
                        element = element.add(statement, 0);
                        this.lastCheckPoint = statement.sourceEnd + 1;
                     }
                  }
               }
            }

            if (this.statementRecoveryActivated
               && this.pendingRecoveredType != null
               && this.scanner.startPosition - 1 <= this.pendingRecoveredType.declarationSourceEnd) {
               element = element.add(this.pendingRecoveredType, 0);
               this.lastCheckPoint = this.pendingRecoveredType.declarationSourceEnd + 1;
               this.pendingRecoveredType = null;
            }

            return element;
         }
      }
   }

   protected void checkAndSetModifiers(int flag) {
      if ((this.modifiers & flag) != 0) {
         this.modifiers |= 4194304;
      }

      this.modifiers |= flag;
      if (this.modifiersSourceStart < 0) {
         this.modifiersSourceStart = this.scanner.startPosition;
      }

      if (this.currentElement != null) {
         this.currentElement.addModifier(flag, this.modifiersSourceStart);
      }
   }

   public void checkComment() {
      if ((!this.diet || this.dietInt != 0) && this.scanner.commentPtr >= 0) {
         this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      }

      int lastComment = this.scanner.commentPtr;
      if (this.modifiersSourceStart >= 0) {
         while(lastComment >= 0) {
            int commentSourceStart = this.scanner.commentStarts[lastComment];
            if (commentSourceStart < 0) {
               commentSourceStart = -commentSourceStart;
            }

            if (commentSourceStart <= this.modifiersSourceStart) {
               break;
            }

            --lastComment;
         }
      }

      if (lastComment >= 0) {
         int lastCommentStart = this.scanner.commentStarts[0];
         if (lastCommentStart < 0) {
            lastCommentStart = -lastCommentStart;
         }

         if (this.forStartPosition != 0 || this.forStartPosition < lastCommentStart) {
            this.modifiersSourceStart = lastCommentStart;
         }

         while(lastComment >= 0 && this.scanner.commentStops[lastComment] < 0) {
            --lastComment;
         }

         if (lastComment >= 0 && this.javadocParser != null) {
            int commentEnd = this.scanner.commentStops[lastComment] - 1;
            if (!this.javadocParser.shouldReportProblems) {
               this.javadocParser.reportProblems = false;
            } else {
               this.javadocParser.reportProblems = this.currentElement == null || commentEnd > this.lastJavadocEnd;
            }

            if (this.javadocParser.checkDeprecation(lastComment)) {
               this.checkAndSetModifiers(1048576);
            }

            this.javadoc = this.javadocParser.docComment;
            if (this.currentElement == null) {
               this.lastJavadocEnd = commentEnd;
            }
         }
      }
   }

   protected void checkNonNLSAfterBodyEnd(int declarationEnd) {
      if (this.scanner.currentPosition - 1 <= declarationEnd) {
         this.scanner.eofPosition = declarationEnd < Integer.MAX_VALUE ? declarationEnd + 1 : declarationEnd;

         try {
            while(this.scanner.getNextToken() != 60) {
            }
         } catch (InvalidInputException var2) {
         }
      }
   }

   protected void classInstanceCreation(boolean isQualified) {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) == 1 && this.astStack[this.astPtr] == null) {
         --this.astPtr;
         AllocationExpression alloc;
         if (isQualified) {
            alloc = new QualifiedAllocationExpression();
         } else {
            alloc = new AllocationExpression();
         }

         alloc.sourceEnd = this.endPosition;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
            this.expressionPtr -= length;
            System.arraycopy(this.expressionStack, this.expressionPtr + 1, alloc.arguments = new Expression[length], 0, length);
         }

         alloc.type = this.getTypeReference(0);
         this.checkForDiamond(alloc.type);
         alloc.sourceStart = this.intStack[this.intPtr--];
         this.pushOnExpressionStack(alloc);
      } else {
         this.dispatchDeclarationInto(length);
         TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)this.astStack[this.astPtr];
         anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
         anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
         if (anonymousTypeDeclaration.allocation != null) {
            anonymousTypeDeclaration.allocation.sourceEnd = this.endStatementPosition;
            this.checkForDiamond(anonymousTypeDeclaration.allocation.type);
         }

         if (length == 0 && !this.containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
            anonymousTypeDeclaration.bits |= 8;
         }

         --this.astPtr;
         --this.astLengthPtr;
      }
   }

   protected void checkForDiamond(TypeReference allocType) {
      if (allocType instanceof ParameterizedSingleTypeReference) {
         ParameterizedSingleTypeReference type = (ParameterizedSingleTypeReference)allocType;
         if (type.typeArguments == TypeReference.NO_TYPE_ARGUMENTS) {
            if (this.options.sourceLevel < 3342336L) {
               this.problemReporter().diamondNotBelow17(allocType);
            }

            if (this.options.sourceLevel > 3145728L) {
               type.bits |= 524288;
            }
         }
      } else if (allocType instanceof ParameterizedQualifiedTypeReference) {
         ParameterizedQualifiedTypeReference type = (ParameterizedQualifiedTypeReference)allocType;
         if (type.typeArguments[type.typeArguments.length - 1] == TypeReference.NO_TYPE_ARGUMENTS) {
            if (this.options.sourceLevel < 3342336L) {
               this.problemReporter().diamondNotBelow17(allocType, type.typeArguments.length - 1);
            }

            if (this.options.sourceLevel > 3145728L) {
               type.bits |= 524288;
            }
         }
      }
   }

   protected ParameterizedQualifiedTypeReference computeQualifiedGenericsFromRightSide(
      TypeReference rightSide, int dim, Annotation[][] annotationsOnDimensions
   ) {
      int nameSize = this.identifierLengthStack[this.identifierLengthPtr];
      int tokensSize = nameSize;
      if (rightSide instanceof ParameterizedSingleTypeReference) {
         tokensSize = nameSize + 1;
      } else if (rightSide instanceof SingleTypeReference) {
         tokensSize = nameSize + 1;
      } else if (rightSide instanceof ParameterizedQualifiedTypeReference) {
         tokensSize = nameSize + ((QualifiedTypeReference)rightSide).tokens.length;
      } else if (rightSide instanceof QualifiedTypeReference) {
         tokensSize = nameSize + ((QualifiedTypeReference)rightSide).tokens.length;
      }

      TypeReference[][] typeArguments = new TypeReference[tokensSize][];
      char[][] tokens = new char[tokensSize][];
      long[] positions = new long[tokensSize];
      Annotation[][] typeAnnotations = null;
      if (rightSide instanceof ParameterizedSingleTypeReference) {
         ParameterizedSingleTypeReference singleParameterizedTypeReference = (ParameterizedSingleTypeReference)rightSide;
         tokens[nameSize] = singleParameterizedTypeReference.token;
         positions[nameSize] = ((long)singleParameterizedTypeReference.sourceStart << 32) + (long)singleParameterizedTypeReference.sourceEnd;
         typeArguments[nameSize] = singleParameterizedTypeReference.typeArguments;
         if (singleParameterizedTypeReference.annotations != null) {
            typeAnnotations = new Annotation[tokensSize][];
            typeAnnotations[nameSize] = singleParameterizedTypeReference.annotations[0];
         }
      } else if (rightSide instanceof SingleTypeReference) {
         SingleTypeReference singleTypeReference = (SingleTypeReference)rightSide;
         tokens[nameSize] = singleTypeReference.token;
         positions[nameSize] = ((long)singleTypeReference.sourceStart << 32) + (long)singleTypeReference.sourceEnd;
         if (singleTypeReference.annotations != null) {
            typeAnnotations = new Annotation[tokensSize][];
            typeAnnotations[nameSize] = singleTypeReference.annotations[0];
         }
      } else if (rightSide instanceof ParameterizedQualifiedTypeReference) {
         ParameterizedQualifiedTypeReference parameterizedTypeReference = (ParameterizedQualifiedTypeReference)rightSide;
         TypeReference[][] rightSideTypeArguments = parameterizedTypeReference.typeArguments;
         System.arraycopy(rightSideTypeArguments, 0, typeArguments, nameSize, rightSideTypeArguments.length);
         char[][] rightSideTokens = parameterizedTypeReference.tokens;
         System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
         long[] rightSidePositions = parameterizedTypeReference.sourcePositions;
         System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
         Annotation[][] rightSideAnnotations = parameterizedTypeReference.annotations;
         if (rightSideAnnotations != null) {
            typeAnnotations = new Annotation[tokensSize][];
            System.arraycopy(rightSideAnnotations, 0, typeAnnotations, nameSize, rightSideAnnotations.length);
         }
      } else if (rightSide instanceof QualifiedTypeReference) {
         QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference)rightSide;
         char[][] rightSideTokens = qualifiedTypeReference.tokens;
         System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
         long[] rightSidePositions = qualifiedTypeReference.sourcePositions;
         System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
         Annotation[][] rightSideAnnotations = qualifiedTypeReference.annotations;
         if (rightSideAnnotations != null) {
            typeAnnotations = new Annotation[tokensSize][];
            System.arraycopy(rightSideAnnotations, 0, typeAnnotations, nameSize, rightSideAnnotations.length);
         }
      }

      int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      TypeReference[] currentTypeArguments = new TypeReference[currentTypeArgumentsLength];
      this.genericsPtr -= currentTypeArgumentsLength;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, currentTypeArguments, 0, currentTypeArgumentsLength);
      if (nameSize == 1) {
         tokens[0] = this.identifierStack[this.identifierPtr];
         positions[0] = this.identifierPositionStack[this.identifierPtr--];
         typeArguments[0] = currentTypeArguments;
      } else {
         this.identifierPtr -= nameSize;
         System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, nameSize);
         System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, nameSize);
         typeArguments[nameSize - 1] = currentTypeArguments;
      }

      --this.identifierLengthPtr;

      ParameterizedQualifiedTypeReference typeRef;
      for(typeRef = new ParameterizedQualifiedTypeReference(tokens, typeArguments, dim, annotationsOnDimensions, positions); nameSize > 0; --nameSize) {
         int length;
         if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
            if (typeAnnotations == null) {
               typeAnnotations = new Annotation[tokensSize][];
            }

            System.arraycopy(
               this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, typeAnnotations[nameSize - 1] = new Annotation[length], 0, length
            );
            if (nameSize == 1) {
               typeRef.sourceStart = typeAnnotations[0][0].sourceStart;
            }
         }
      }

      if ((typeRef.annotations = typeAnnotations) != null) {
         typeRef.bits |= 1048576;
      }

      return typeRef;
   }

   protected void concatExpressionLists() {
      this.expressionLengthStack[--this.expressionLengthPtr]++;
   }

   protected void concatGenericsLists() {
      this.genericsLengthStack[this.genericsLengthPtr - 1] += this.genericsLengthStack[this.genericsLengthPtr--];
   }

   protected void concatNodeLists() {
      this.astLengthStack[this.astLengthPtr - 1] += this.astLengthStack[this.astLengthPtr--];
   }

   protected void consumeAdditionalBound() {
      this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeAdditionalBound1() {
   }

   protected void consumeAdditionalBoundList() {
      this.concatGenericsLists();
   }

   protected void consumeAdditionalBoundList1() {
      this.concatGenericsLists();
   }

   protected boolean isIndirectlyInsideLambdaExpression() {
      return false;
   }

   protected void consumeAllocationHeader() {
      if (this.currentElement != null) {
         if (this.currentToken == 49) {
            TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
            anonymousType.name = CharOperation.NO_CHAR;
            anonymousType.bits |= 768;
            anonymousType.sourceStart = this.intStack[this.intPtr--];
            anonymousType.declarationSourceStart = anonymousType.sourceStart;
            anonymousType.sourceEnd = this.rParenPos;
            QualifiedAllocationExpression alloc = new QualifiedAllocationExpression(anonymousType);
            alloc.type = this.getTypeReference(0);
            alloc.sourceStart = anonymousType.sourceStart;
            alloc.sourceEnd = anonymousType.sourceEnd;
            this.lastCheckPoint = anonymousType.bodyStart = this.scanner.currentPosition;
            this.currentElement = this.currentElement.add(anonymousType, 0);
            this.lastIgnoredToken = -1;
            if (this.isIndirectlyInsideLambdaExpression()) {
               this.ignoreNextOpeningBrace = true;
            } else {
               this.currentToken = 0;
            }
         } else {
            this.lastCheckPoint = this.scanner.startPosition;
            this.restartRecovery = true;
         }
      }
   }

   protected void consumeAnnotationAsModifier() {
      Expression expression = this.expressionStack[this.expressionPtr];
      int sourceStart = expression.sourceStart;
      if (this.modifiersSourceStart < 0) {
         this.modifiersSourceStart = sourceStart;
      }
   }

   protected void consumeAnnotationName() {
      if (this.currentElement != null && !this.expectTypeAnnotation) {
         int start = this.intStack[this.intPtr];
         int end = (int)(this.identifierPositionStack[this.identifierPtr] & 4294967295L);
         this.annotationRecoveryCheckPoint(start, end);
         if (this.annotationRecoveryActivated) {
            this.currentElement = this.currentElement.addAnnotationName(this.identifierPtr, this.identifierLengthPtr, start, 0);
         }
      }

      this.recordStringLiterals = false;
      this.expectTypeAnnotation = false;
   }

   protected void consumeAnnotationTypeDeclaration() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.dispatchDeclarationInto(length);
      }

      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      typeDecl.checkConstructors(this);
      if (this.scanner.containsAssertKeyword) {
         typeDecl.bits |= 1;
      }

      typeDecl.addClinit();
      typeDecl.bodyEnd = this.endStatementPosition;
      if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
         typeDecl.bits |= 8;
      }

      typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeAnnotationTypeDeclarationHeader() {
      TypeDeclaration annotationTypeDeclaration = (TypeDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         annotationTypeDeclaration.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         this.restartRecovery = true;
      }

      this.scanner.commentPtr = -1;
   }

   protected void consumeAnnotationTypeDeclarationHeaderName() {
      TypeDeclaration annotationTypeDeclaration = new TypeDeclaration(this.compilationUnit.compilationResult);
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            annotationTypeDeclaration.bits |= 1024;
         }
      } else {
         annotationTypeDeclaration.bits |= 256;
         this.markEnclosingMemberWithLocalType();
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      annotationTypeDeclaration.sourceEnd = (int)pos;
      annotationTypeDeclaration.sourceStart = (int)(pos >>> 32);
      annotationTypeDeclaration.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      --this.intPtr;
      --this.intPtr;
      annotationTypeDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
      annotationTypeDeclaration.modifiers = this.intStack[this.intPtr--] | 8192 | 512;
      if (annotationTypeDeclaration.modifiersSourceStart >= 0) {
         annotationTypeDeclaration.declarationSourceStart = annotationTypeDeclaration.modifiersSourceStart;
         --this.intPtr;
      } else {
         int atPosition = this.intStack[this.intPtr--];
         annotationTypeDeclaration.declarationSourceStart = atPosition;
      }

      if ((annotationTypeDeclaration.bits & 1024) == 0
         && (annotationTypeDeclaration.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(annotationTypeDeclaration.name, this.compilationUnit.getMainTypeName())) {
         annotationTypeDeclaration.bits |= 4096;
      }

      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, annotationTypeDeclaration.annotations = new Annotation[length], 0, length);
      }

      annotationTypeDeclaration.bodyStart = annotationTypeDeclaration.sourceEnd + 1;
      annotationTypeDeclaration.javadoc = this.javadoc;
      this.javadoc = null;
      this.pushOnAstStack(annotationTypeDeclaration);
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfAnnotationDeclarations(annotationTypeDeclaration);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = annotationTypeDeclaration.bodyStart;
         this.currentElement = this.currentElement.add(annotationTypeDeclaration, 0);
         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters() {
      TypeDeclaration annotationTypeDeclaration = new TypeDeclaration(this.compilationUnit.compilationResult);
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, annotationTypeDeclaration.typeParameters = new TypeParameter[length], 0, length);
      this.problemReporter().invalidUsageOfTypeParametersForAnnotationDeclaration(annotationTypeDeclaration);
      annotationTypeDeclaration.bodyStart = annotationTypeDeclaration.typeParameters[length - 1].declarationSourceEnd + 1;
      this.listTypeParameterLength = 0;
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            annotationTypeDeclaration.bits |= 1024;
         }
      } else {
         annotationTypeDeclaration.bits |= 256;
         this.markEnclosingMemberWithLocalType();
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      annotationTypeDeclaration.sourceEnd = (int)pos;
      annotationTypeDeclaration.sourceStart = (int)(pos >>> 32);
      annotationTypeDeclaration.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      --this.intPtr;
      --this.intPtr;
      annotationTypeDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
      annotationTypeDeclaration.modifiers = this.intStack[this.intPtr--] | 8192 | 512;
      if (annotationTypeDeclaration.modifiersSourceStart >= 0) {
         annotationTypeDeclaration.declarationSourceStart = annotationTypeDeclaration.modifiersSourceStart;
         --this.intPtr;
      } else {
         int atPosition = this.intStack[this.intPtr--];
         annotationTypeDeclaration.declarationSourceStart = atPosition;
      }

      if ((annotationTypeDeclaration.bits & 1024) == 0
         && (annotationTypeDeclaration.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(annotationTypeDeclaration.name, this.compilationUnit.getMainTypeName())) {
         annotationTypeDeclaration.bits |= 4096;
      }

      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, annotationTypeDeclaration.annotations = new Annotation[length], 0, length);
      }

      annotationTypeDeclaration.javadoc = this.javadoc;
      this.javadoc = null;
      this.pushOnAstStack(annotationTypeDeclaration);
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfAnnotationDeclarations(annotationTypeDeclaration);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = annotationTypeDeclaration.bodyStart;
         this.currentElement = this.currentElement.add(annotationTypeDeclaration, 0);
         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeAnnotationTypeMemberDeclaration() {
      AnnotationMethodDeclaration annotationTypeMemberDeclaration = (AnnotationMethodDeclaration)this.astStack[this.astPtr];
      annotationTypeMemberDeclaration.modifiers |= 16777216;
      int declarationEndPosition = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      annotationTypeMemberDeclaration.bodyStart = this.endStatementPosition;
      annotationTypeMemberDeclaration.bodyEnd = declarationEndPosition;
      annotationTypeMemberDeclaration.declarationSourceEnd = declarationEndPosition;
   }

   protected void consumeAnnotationTypeMemberDeclarations() {
      this.concatNodeLists();
   }

   protected void consumeAnnotationTypeMemberDeclarationsopt() {
      --this.nestedType;
   }

   protected void consumeArgumentList() {
      this.concatExpressionLists();
   }

   protected void consumeArguments() {
      this.pushOnIntStack(this.rParenPos);
   }

   protected void consumeArrayAccess(boolean unspecifiedReference) {
      Expression exp;
      if (unspecifiedReference) {
         exp = this.expressionStack[this.expressionPtr] = new ArrayReference(this.getUnspecifiedReferenceOptimized(), this.expressionStack[this.expressionPtr]);
      } else {
         --this.expressionPtr;
         --this.expressionLengthPtr;
         exp = this.expressionStack[this.expressionPtr] = new ArrayReference(
            this.expressionStack[this.expressionPtr], this.expressionStack[this.expressionPtr + 1]
         );
      }

      exp.sourceEnd = this.endStatementPosition;
   }

   protected void consumeArrayCreationExpressionWithInitializer() {
      ArrayAllocationExpression arrayAllocation = new ArrayAllocationExpression();
      --this.expressionLengthPtr;
      arrayAllocation.initializer = (ArrayInitializer)this.expressionStack[this.expressionPtr--];
      int length = this.expressionLengthStack[this.expressionLengthPtr--];
      this.expressionPtr -= length;
      System.arraycopy(this.expressionStack, this.expressionPtr + 1, arrayAllocation.dimensions = new Expression[length], 0, length);
      Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(length);
      arrayAllocation.annotationsOnDimensions = annotationsOnDimensions;
      arrayAllocation.type = this.getTypeReference(0);
      arrayAllocation.type.bits |= 1073741824;
      if (annotationsOnDimensions != null) {
         arrayAllocation.bits |= 1048576;
         arrayAllocation.type.bits |= 1048576;
      }

      arrayAllocation.sourceStart = this.intStack[this.intPtr--];
      if (arrayAllocation.initializer == null) {
         arrayAllocation.sourceEnd = this.endStatementPosition;
      } else {
         arrayAllocation.sourceEnd = arrayAllocation.initializer.sourceEnd;
      }

      this.pushOnExpressionStack(arrayAllocation);
   }

   protected void consumeArrayCreationExpressionWithoutInitializer() {
      ArrayAllocationExpression arrayAllocation = new ArrayAllocationExpression();
      int length = this.expressionLengthStack[this.expressionLengthPtr--];
      this.expressionPtr -= length;
      System.arraycopy(this.expressionStack, this.expressionPtr + 1, arrayAllocation.dimensions = new Expression[length], 0, length);
      Annotation[][] annotationsOnDimensions = this.getAnnotationsOnDimensions(length);
      arrayAllocation.annotationsOnDimensions = annotationsOnDimensions;
      arrayAllocation.type = this.getTypeReference(0);
      arrayAllocation.type.bits |= 1073741824;
      if (annotationsOnDimensions != null) {
         arrayAllocation.bits |= 1048576;
         arrayAllocation.type.bits |= 1048576;
      }

      arrayAllocation.sourceStart = this.intStack[this.intPtr--];
      if (arrayAllocation.initializer == null) {
         arrayAllocation.sourceEnd = this.endStatementPosition;
      } else {
         arrayAllocation.sourceEnd = arrayAllocation.initializer.sourceEnd;
      }

      this.pushOnExpressionStack(arrayAllocation);
   }

   protected void consumeArrayCreationHeader() {
   }

   protected void consumeArrayInitializer() {
      this.arrayInitializer(this.expressionLengthStack[this.expressionLengthPtr--]);
   }

   protected void consumeArrayTypeWithTypeArgumentsName() {
      this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr] += this.identifierLengthStack[this.identifierLengthPtr];
      this.pushOnGenericsLengthStack(0);
   }

   protected void consumeAssertStatement() {
      this.expressionLengthPtr -= 2;
      this.pushOnAstStack(
         new AssertStatement(this.expressionStack[this.expressionPtr--], this.expressionStack[this.expressionPtr--], this.intStack[this.intPtr--])
      );
   }

   protected void consumeAssignment() {
      int op = this.intStack[this.intPtr--];
      --this.expressionPtr;
      --this.expressionLengthPtr;
      Expression expression = this.expressionStack[this.expressionPtr + 1];
      this.expressionStack[this.expressionPtr] = (Expression)(op != 30
         ? new CompoundAssignment(this.expressionStack[this.expressionPtr], expression, op, expression.sourceEnd)
         : new Assignment(this.expressionStack[this.expressionPtr], expression, expression.sourceEnd));
      if (this.pendingRecoveredType != null) {
         if (this.pendingRecoveredType.allocation != null && this.scanner.startPosition - 1 <= this.pendingRecoveredType.declarationSourceEnd) {
            this.expressionStack[this.expressionPtr] = this.pendingRecoveredType.allocation;
            this.pendingRecoveredType = null;
            return;
         }

         this.pendingRecoveredType = null;
      }
   }

   protected void consumeAssignmentOperator(int pos) {
      this.pushOnIntStack(pos);
   }

   protected void consumeBinaryExpression(int op) {
      --this.expressionPtr;
      --this.expressionLengthPtr;
      Expression expr1 = this.expressionStack[this.expressionPtr];
      Expression expr2 = this.expressionStack[this.expressionPtr + 1];
      switch(op) {
         case 0:
            this.expressionStack[this.expressionPtr] = new AND_AND_Expression(expr1, expr2, op);
            break;
         case 1:
            this.expressionStack[this.expressionPtr] = new OR_OR_Expression(expr1, expr2, op);
            break;
         case 4:
         case 15:
            --this.intPtr;
            this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
            break;
         case 14:
            if (this.optimizeStringLiterals) {
               if (expr1 instanceof StringLiteral) {
                  if ((expr1.bits & 534773760) >> 21 == 0) {
                     if (expr2 instanceof CharLiteral) {
                        this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendWith((CharLiteral)expr2);
                     } else if (expr2 instanceof StringLiteral) {
                        this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendWith((StringLiteral)expr2);
                     } else {
                        this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
                     }
                  } else {
                     this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
                  }
               } else if (expr1 instanceof CombinedBinaryExpression) {
                  CombinedBinaryExpression cursor;
                  if ((cursor = (CombinedBinaryExpression)expr1).arity < cursor.arityMax) {
                     cursor.left = new BinaryExpression(cursor);
                     ++cursor.arity;
                  } else {
                     cursor.left = new CombinedBinaryExpression(cursor);
                     cursor.arity = 0;
                     cursor.tuneArityMax();
                  }

                  cursor.right = expr2;
                  cursor.sourceEnd = expr2.sourceEnd;
                  this.expressionStack[this.expressionPtr] = cursor;
               } else if (expr1 instanceof BinaryExpression && (expr1.bits & 4032) >> 6 == 14) {
                  this.expressionStack[this.expressionPtr] = new CombinedBinaryExpression(expr1, expr2, 14, 1);
               } else {
                  this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
               }
            } else if (expr1 instanceof StringLiteral) {
               if (expr2 instanceof StringLiteral && (expr1.bits & 534773760) >> 21 == 0) {
                  this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendsWith((StringLiteral)expr2);
               } else {
                  this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
               }
            } else if (expr1 instanceof CombinedBinaryExpression) {
               CombinedBinaryExpression cursor;
               if ((cursor = (CombinedBinaryExpression)expr1).arity < cursor.arityMax) {
                  cursor.left = new BinaryExpression(cursor);
                  cursor.bits &= -534773761;
                  ++cursor.arity;
               } else {
                  cursor.left = new CombinedBinaryExpression(cursor);
                  cursor.bits &= -534773761;
                  cursor.arity = 0;
                  cursor.tuneArityMax();
               }

               cursor.right = expr2;
               cursor.sourceEnd = expr2.sourceEnd;
               this.expressionStack[this.expressionPtr] = cursor;
            } else if (expr1 instanceof BinaryExpression && (expr1.bits & 4032) >> 6 == 14) {
               this.expressionStack[this.expressionPtr] = new CombinedBinaryExpression(expr1, expr2, 14, 1);
            } else {
               this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
            }
            break;
         default:
            this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
      }
   }

   protected void consumeBinaryExpressionWithName(int op) {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
      --this.expressionPtr;
      --this.expressionLengthPtr;
      Expression expr1 = this.expressionStack[this.expressionPtr + 1];
      Expression expr2 = this.expressionStack[this.expressionPtr];
      switch(op) {
         case 0:
            this.expressionStack[this.expressionPtr] = new AND_AND_Expression(expr1, expr2, op);
            break;
         case 1:
            this.expressionStack[this.expressionPtr] = new OR_OR_Expression(expr1, expr2, op);
            break;
         case 4:
         case 15:
            --this.intPtr;
            this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
            break;
         case 14:
            if (this.optimizeStringLiterals) {
               if (!(expr1 instanceof StringLiteral) || (expr1.bits & 534773760) >> 21 != 0) {
                  this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
               } else if (expr2 instanceof CharLiteral) {
                  this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendWith((CharLiteral)expr2);
               } else if (expr2 instanceof StringLiteral) {
                  this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendWith((StringLiteral)expr2);
               } else {
                  this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, 14);
               }
            } else if (expr1 instanceof StringLiteral) {
               if (expr2 instanceof StringLiteral && (expr1.bits & 534773760) >> 21 == 0) {
                  this.expressionStack[this.expressionPtr] = ((StringLiteral)expr1).extendsWith((StringLiteral)expr2);
               } else {
                  this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
               }
            } else {
               this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
            }
            break;
         default:
            this.expressionStack[this.expressionPtr] = new BinaryExpression(expr1, expr2, op);
      }
   }

   protected void consumeBlock() {
      int statementsLength = this.astLengthStack[this.astLengthPtr--];
      Block block;
      if (statementsLength == 0) {
         block = new Block(0);
         block.sourceStart = this.intStack[this.intPtr--];
         block.sourceEnd = this.endStatementPosition;
         if (!this.containsComment(block.sourceStart, block.sourceEnd)) {
            block.bits |= 8;
         }

         --this.realBlockPtr;
      } else {
         block = new Block(this.realBlockStack[this.realBlockPtr--]);
         this.astPtr -= statementsLength;
         System.arraycopy(this.astStack, this.astPtr + 1, block.statements = new Statement[statementsLength], 0, statementsLength);
         block.sourceStart = this.intStack[this.intPtr--];
         block.sourceEnd = this.endStatementPosition;
      }

      this.pushOnAstStack(block);
   }

   protected void consumeBlockStatement() {
   }

   protected void consumeBlockStatements() {
      this.concatNodeLists();
   }

   protected void consumeCaseLabel() {
      --this.expressionLengthPtr;
      Expression expression = this.expressionStack[this.expressionPtr--];
      CaseStatement caseStatement = new CaseStatement(expression, expression.sourceEnd, this.intStack[this.intPtr--]);
      if (this.hasLeadingTagComment(FALL_THROUGH_TAG, caseStatement.sourceStart)) {
         caseStatement.bits |= 536870912;
      }

      this.pushOnAstStack(caseStatement);
   }

   protected void consumeCastExpressionLL1() {
      --this.expressionPtr;
      Expression cast;
      Expression exp;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(
         exp = this.expressionStack[this.expressionPtr + 1], (TypeReference)this.expressionStack[this.expressionPtr]
      );
      --this.expressionLengthPtr;
      this.updateSourcePosition(cast);
      cast.sourceEnd = exp.sourceEnd;
   }

   public IntersectionCastTypeReference createIntersectionCastTypeReference(TypeReference[] typeReferences) {
      if (this.options.sourceLevel < 3407872L) {
         this.problemReporter().intersectionCastNotBelow18(typeReferences);
      }

      return new IntersectionCastTypeReference(typeReferences);
   }

   protected void consumeCastExpressionLL1WithBounds() {
      Expression exp = this.expressionStack[this.expressionPtr--];
      --this.expressionLengthPtr;
      int length;
      TypeReference[] bounds = new TypeReference[length = this.expressionLengthStack[this.expressionLengthPtr]];
      System.arraycopy(this.expressionStack, this.expressionPtr -= length - 1, bounds, 0, length);
      Expression cast;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(exp, this.createIntersectionCastTypeReference(bounds));
      this.expressionLengthStack[this.expressionLengthPtr] = 1;
      this.updateSourcePosition(cast);
      cast.sourceEnd = exp.sourceEnd;
   }

   protected void consumeCastExpressionWithGenericsArray() {
      TypeReference[] bounds = null;
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      if (additionalBoundsLength > 0) {
         bounds = new TypeReference[additionalBoundsLength + 1];
         this.genericsPtr -= additionalBoundsLength;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
      }

      int end = this.intStack[this.intPtr--];
      int dim = this.intStack[this.intPtr--];
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      TypeReference castType;
      if (additionalBoundsLength > 0) {
         bounds[0] = this.getTypeReference(dim);
         castType = this.createIntersectionCastTypeReference(bounds);
      } else {
         castType = this.getTypeReference(dim);
      }

      Expression exp;
      Expression cast;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(exp = this.expressionStack[this.expressionPtr], castType);
      --this.intPtr;
      castType.sourceEnd = end - 1;
      castType.sourceStart = (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
      cast.sourceEnd = exp.sourceEnd;
   }

   protected void consumeCastExpressionWithNameArray() {
      int end = this.intStack[this.intPtr--];
      TypeReference[] bounds = null;
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      if (additionalBoundsLength > 0) {
         bounds = new TypeReference[additionalBoundsLength + 1];
         this.genericsPtr -= additionalBoundsLength;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
      }

      this.pushOnGenericsLengthStack(0);
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      TypeReference castType;
      if (additionalBoundsLength > 0) {
         bounds[0] = this.getTypeReference(this.intStack[this.intPtr--]);
         castType = this.createIntersectionCastTypeReference(bounds);
      } else {
         castType = this.getTypeReference(this.intStack[this.intPtr--]);
      }

      Expression exp;
      Expression cast;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(exp = this.expressionStack[this.expressionPtr], castType);
      castType.sourceEnd = end - 1;
      castType.sourceStart = (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
      cast.sourceEnd = exp.sourceEnd;
   }

   protected void consumeCastExpressionWithPrimitiveType() {
      TypeReference[] bounds = null;
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      if (additionalBoundsLength > 0) {
         bounds = new TypeReference[additionalBoundsLength + 1];
         this.genericsPtr -= additionalBoundsLength;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
      }

      int end = this.intStack[this.intPtr--];
      TypeReference castType;
      if (additionalBoundsLength > 0) {
         bounds[0] = this.getTypeReference(this.intStack[this.intPtr--]);
         castType = this.createIntersectionCastTypeReference(bounds);
      } else {
         castType = this.getTypeReference(this.intStack[this.intPtr--]);
      }

      Expression exp;
      Expression cast;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(exp = this.expressionStack[this.expressionPtr], castType);
      castType.sourceEnd = end - 1;
      castType.sourceStart = (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
      cast.sourceEnd = exp.sourceEnd;
   }

   protected void consumeCastExpressionWithQualifiedGenericsArray() {
      TypeReference[] bounds = null;
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      if (additionalBoundsLength > 0) {
         bounds = new TypeReference[additionalBoundsLength + 1];
         this.genericsPtr -= additionalBoundsLength;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
      }

      int end = this.intStack[this.intPtr--];
      int dim = this.intStack[this.intPtr--];
      Annotation[][] annotationsOnDimensions = dim == 0 ? null : this.getAnnotationsOnDimensions(dim);
      TypeReference rightSide = this.getTypeReference(0);
      TypeReference castType = this.computeQualifiedGenericsFromRightSide(rightSide, dim, annotationsOnDimensions);
      if (additionalBoundsLength > 0) {
         bounds[0] = castType;
         castType = this.createIntersectionCastTypeReference(bounds);
      }

      --this.intPtr;
      Expression exp;
      Expression cast;
      this.expressionStack[this.expressionPtr] = cast = new CastExpression(exp = this.expressionStack[this.expressionPtr], castType);
      castType.sourceEnd = end - 1;
      castType.sourceStart = (cast.sourceStart = this.intStack[this.intPtr--]) + 1;
      cast.sourceEnd = exp.sourceEnd;
   }

   protected void consumeCatches() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeCatchFormalParameter() {
      --this.identifierLengthPtr;
      char[] identifierName = this.identifierStack[this.identifierPtr];
      long namePositions = this.identifierPositionStack[this.identifierPtr--];
      int extendedDimensions = this.intStack[this.intPtr--];
      TypeReference type = (TypeReference)this.astStack[this.astPtr--];
      if (extendedDimensions > 0) {
         type = this.augmentTypeWithAdditionalDimensions(type, extendedDimensions, null, false);
         type.sourceEnd = this.endPosition;
         if (type instanceof UnionTypeReference) {
            this.problemReporter().illegalArrayOfUnionType(identifierName, type);
         }
      }

      --this.astLengthPtr;
      int modifierPositions = this.intStack[this.intPtr--];
      --this.intPtr;
      Argument arg = new Argument(identifierName, namePositions, type, this.intStack[this.intPtr + 1] & -1048577);
      arg.bits &= -5;
      arg.declarationSourceStart = modifierPositions;
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, arg.annotations = new Annotation[length], 0, length);
      }

      this.pushOnAstStack(arg);
      ++this.listLength;
   }

   protected void consumeCatchHeader() {
      if (this.currentElement != null) {
         if (!(this.currentElement instanceof RecoveredBlock)) {
            if (!(this.currentElement instanceof RecoveredMethod)) {
               return;
            }

            RecoveredMethod rMethod = (RecoveredMethod)this.currentElement;
            if (rMethod.methodBody != null || rMethod.bracketBalance <= 0) {
               return;
            }
         }

         Argument arg = (Argument)this.astStack[this.astPtr--];
         LocalDeclaration localDeclaration = new LocalDeclaration(arg.name, arg.sourceStart, arg.sourceEnd);
         localDeclaration.type = arg.type;
         localDeclaration.declarationSourceStart = arg.declarationSourceStart;
         localDeclaration.declarationSourceEnd = arg.declarationSourceEnd;
         this.currentElement = this.currentElement.add(localDeclaration, 0);
         this.lastCheckPoint = this.scanner.startPosition;
         this.restartRecovery = true;
         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeCatchType() {
      int length = this.astLengthStack[this.astLengthPtr--];
      if (length != 1) {
         TypeReference[] typeReferences;
         System.arraycopy(this.astStack, (this.astPtr -= length) + 1, typeReferences = new TypeReference[length], 0, length);
         UnionTypeReference typeReference = new UnionTypeReference(typeReferences);
         this.pushOnAstStack(typeReference);
         if (this.options.sourceLevel < 3342336L) {
            this.problemReporter().multiCatchNotBelow17(typeReference);
         }
      } else {
         this.pushOnAstLengthStack(1);
      }
   }

   protected void consumeClassBodyDeclaration() {
      this.nestedMethod[this.nestedType]--;
      Block block = (Block)this.astStack[this.astPtr--];
      --this.astLengthPtr;
      if (this.diet) {
         block.bits &= -9;
      }

      Initializer initializer = (Initializer)this.astStack[this.astPtr];
      initializer.declarationSourceStart = initializer.sourceStart = block.sourceStart;
      initializer.block = block;
      --this.intPtr;
      initializer.bodyStart = this.intStack[this.intPtr--];
      --this.realBlockPtr;
      int javadocCommentStart = this.intStack[this.intPtr--];
      if (javadocCommentStart != -1) {
         initializer.declarationSourceStart = javadocCommentStart;
         initializer.javadoc = this.javadoc;
         this.javadoc = null;
      }

      initializer.bodyEnd = this.endPosition;
      initializer.sourceEnd = this.endStatementPosition;
      initializer.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeClassBodyDeclarations() {
      this.concatNodeLists();
   }

   protected void consumeClassBodyDeclarationsopt() {
      --this.nestedType;
   }

   protected void consumeClassBodyopt() {
      this.pushOnAstStack(null);
      this.endPosition = this.rParenPos;
   }

   protected void consumeClassDeclaration() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.dispatchDeclarationInto(length);
      }

      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      boolean hasConstructor = typeDecl.checkConstructors(this);
      if (!hasConstructor) {
         switch(TypeDeclaration.kind(typeDecl.modifiers)) {
            case 1:
            case 3:
               boolean insideFieldInitializer = false;
               if (this.diet) {
                  for(int i = this.nestedType; i > 0; --i) {
                     if (this.variablesCounter[i] > 0) {
                        insideFieldInitializer = true;
                        break;
                     }
                  }
               }

               typeDecl.createDefaultConstructor(!this.diet || this.dietInt != 0 || insideFieldInitializer, true);
            case 2:
         }
      }

      if (this.scanner.containsAssertKeyword) {
         typeDecl.bits |= 1;
      }

      typeDecl.addClinit();
      typeDecl.bodyEnd = this.endStatementPosition;
      if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
         typeDecl.bits |= 8;
      }

      typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeClassHeader() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         typeDecl.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         this.restartRecovery = true;
      }

      this.scanner.commentPtr = -1;
   }

   protected void consumeClassHeaderExtends() {
      TypeReference superClass = this.getTypeReference(0);
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      typeDecl.bits |= superClass.bits & 1048576;
      typeDecl.superclass = superClass;
      superClass.bits |= 16;
      typeDecl.bodyStart = typeDecl.superclass.sourceEnd + 1;
      if (this.currentElement != null) {
         this.lastCheckPoint = typeDecl.bodyStart;
      }
   }

   protected void consumeClassHeaderImplements() {
      int length = this.astLengthStack[this.astLengthPtr--];
      this.astPtr -= length;
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      System.arraycopy(this.astStack, this.astPtr + 1, typeDecl.superInterfaces = new TypeReference[length], 0, length);
      TypeReference[] superinterfaces = typeDecl.superInterfaces;
      int i = 0;

      for(int max = superinterfaces.length; i < max; ++i) {
         TypeReference typeReference = superinterfaces[i];
         typeDecl.bits |= typeReference.bits & 1048576;
         typeReference.bits |= 16;
      }

      typeDecl.bodyStart = typeDecl.superInterfaces[length - 1].sourceEnd + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = typeDecl.bodyStart;
      }
   }

   protected void consumeClassHeaderName1() {
      TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            typeDecl.bits |= 1024;
         }
      } else {
         typeDecl.bits |= 256;
         this.markEnclosingMemberWithLocalType();
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      typeDecl.sourceEnd = (int)pos;
      typeDecl.sourceStart = (int)(pos >>> 32);
      typeDecl.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
      --this.intPtr;
      typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
      typeDecl.modifiers = this.intStack[this.intPtr--];
      if (typeDecl.modifiersSourceStart >= 0) {
         typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
      }

      if ((typeDecl.bits & 1024) == 0
         && (typeDecl.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(typeDecl.name, this.compilationUnit.getMainTypeName())) {
         typeDecl.bits |= 4096;
      }

      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, typeDecl.annotations = new Annotation[length], 0, length);
      }

      typeDecl.bodyStart = typeDecl.sourceEnd + 1;
      this.pushOnAstStack(typeDecl);
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = typeDecl.bodyStart;
         this.currentElement = this.currentElement.add(typeDecl, 0);
         this.lastIgnoredToken = -1;
      }

      typeDecl.javadoc = this.javadoc;
      this.javadoc = null;
   }

   protected void consumeClassInstanceCreationExpression() {
      this.classInstanceCreation(false);
      this.consumeInvocationExpression();
   }

   protected void consumeClassInstanceCreationExpressionName() {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
   }

   protected void consumeClassInstanceCreationExpressionQualified() {
      this.classInstanceCreation(true);
      QualifiedAllocationExpression qae = (QualifiedAllocationExpression)this.expressionStack[this.expressionPtr];
      if (qae.anonymousType == null) {
         --this.expressionLengthPtr;
         --this.expressionPtr;
         qae.enclosingInstance = this.expressionStack[this.expressionPtr];
         this.expressionStack[this.expressionPtr] = qae;
      }

      qae.sourceStart = qae.enclosingInstance.sourceStart;
      this.consumeInvocationExpression();
   }

   protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) == 1 && this.astStack[this.astPtr] == null) {
         --this.astPtr;
         QualifiedAllocationExpression alloc = new QualifiedAllocationExpression();
         alloc.sourceEnd = this.endPosition;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
            this.expressionPtr -= length;
            System.arraycopy(this.expressionStack, this.expressionPtr + 1, alloc.arguments = new Expression[length], 0, length);
         }

         alloc.type = this.getTypeReference(0);
         this.checkForDiamond(alloc.type);
         length = this.genericsLengthStack[this.genericsLengthPtr--];
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
         alloc.sourceStart = this.intStack[this.intPtr--];
         this.pushOnExpressionStack(alloc);
      } else {
         this.dispatchDeclarationInto(length);
         TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)this.astStack[this.astPtr];
         anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
         anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
         if (length == 0 && !this.containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
            anonymousTypeDeclaration.bits |= 8;
         }

         --this.astPtr;
         --this.astLengthPtr;
         QualifiedAllocationExpression allocationExpression = anonymousTypeDeclaration.allocation;
         if (allocationExpression != null) {
            allocationExpression.sourceEnd = this.endStatementPosition;
            length = this.genericsLengthStack[this.genericsLengthPtr--];
            this.genericsPtr -= length;
            System.arraycopy(this.genericsStack, this.genericsPtr + 1, allocationExpression.typeArguments = new TypeReference[length], 0, length);
            allocationExpression.sourceStart = this.intStack[this.intPtr--];
            this.checkForDiamond(allocationExpression.type);
         }
      }

      QualifiedAllocationExpression qae = (QualifiedAllocationExpression)this.expressionStack[this.expressionPtr];
      if (qae.anonymousType == null) {
         --this.expressionLengthPtr;
         --this.expressionPtr;
         qae.enclosingInstance = this.expressionStack[this.expressionPtr];
         this.expressionStack[this.expressionPtr] = qae;
      }

      qae.sourceStart = qae.enclosingInstance.sourceStart;
      this.consumeInvocationExpression();
   }

   protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) == 1 && this.astStack[this.astPtr] == null) {
         --this.astPtr;
         AllocationExpression alloc = new AllocationExpression();
         alloc.sourceEnd = this.endPosition;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
            this.expressionPtr -= length;
            System.arraycopy(this.expressionStack, this.expressionPtr + 1, alloc.arguments = new Expression[length], 0, length);
         }

         alloc.type = this.getTypeReference(0);
         this.checkForDiamond(alloc.type);
         length = this.genericsLengthStack[this.genericsLengthPtr--];
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
         alloc.sourceStart = this.intStack[this.intPtr--];
         this.pushOnExpressionStack(alloc);
      } else {
         this.dispatchDeclarationInto(length);
         TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)this.astStack[this.astPtr];
         anonymousTypeDeclaration.declarationSourceEnd = this.endStatementPosition;
         anonymousTypeDeclaration.bodyEnd = this.endStatementPosition;
         if (length == 0 && !this.containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
            anonymousTypeDeclaration.bits |= 8;
         }

         --this.astPtr;
         --this.astLengthPtr;
         QualifiedAllocationExpression allocationExpression = anonymousTypeDeclaration.allocation;
         if (allocationExpression != null) {
            allocationExpression.sourceEnd = this.endStatementPosition;
            length = this.genericsLengthStack[this.genericsLengthPtr--];
            this.genericsPtr -= length;
            System.arraycopy(this.genericsStack, this.genericsPtr + 1, allocationExpression.typeArguments = new TypeReference[length], 0, length);
            allocationExpression.sourceStart = this.intStack[this.intPtr--];
            this.checkForDiamond(allocationExpression.type);
         }
      }

      this.consumeInvocationExpression();
   }

   protected void consumeClassOrInterface() {
      this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr] += this.identifierLengthStack[this.identifierLengthPtr];
      this.pushOnGenericsLengthStack(0);
   }

   protected void consumeClassOrInterfaceName() {
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnGenericsLengthStack(0);
   }

   protected void consumeClassTypeElt() {
      this.pushOnAstStack(this.getTypeReference(0));
      ++this.listLength;
   }

   protected void consumeClassTypeList() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeCompilationUnit() {
   }

   protected void consumeConditionalExpression(int op) {
      this.intPtr -= 2;
      this.expressionPtr -= 2;
      this.expressionLengthPtr -= 2;
      this.expressionStack[this.expressionPtr] = new ConditionalExpression(
         this.expressionStack[this.expressionPtr], this.expressionStack[this.expressionPtr + 1], this.expressionStack[this.expressionPtr + 2]
      );
   }

   protected void consumeConditionalExpressionWithName(int op) {
      this.intPtr -= 2;
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
      this.expressionPtr -= 2;
      this.expressionLengthPtr -= 2;
      this.expressionStack[this.expressionPtr] = new ConditionalExpression(
         this.expressionStack[this.expressionPtr + 2], this.expressionStack[this.expressionPtr], this.expressionStack[this.expressionPtr + 1]
      );
   }

   protected void consumeConstructorBlockStatements() {
      this.concatNodeLists();
   }

   protected void consumeConstructorBody() {
      this.nestedMethod[this.nestedType]--;
   }

   protected void consumeConstructorDeclaration() {
      --this.intPtr;
      --this.intPtr;
      --this.realBlockPtr;
      ExplicitConstructorCall constructorCall = null;
      Statement[] statements = null;
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.astPtr -= length;
         if (!this.options.ignoreMethodBodies) {
            if (this.astStack[this.astPtr + 1] instanceof ExplicitConstructorCall) {
               System.arraycopy(this.astStack, this.astPtr + 2, statements = new Statement[length - 1], 0, length - 1);
               constructorCall = (ExplicitConstructorCall)this.astStack[this.astPtr + 1];
            } else {
               System.arraycopy(this.astStack, this.astPtr + 1, statements = new Statement[length], 0, length);
               constructorCall = SuperReference.implicitSuperConstructorCall();
            }
         }
      } else {
         boolean insideFieldInitializer = false;
         if (this.diet) {
            for(int i = this.nestedType; i > 0; --i) {
               if (this.variablesCounter[i] > 0) {
                  insideFieldInitializer = true;
                  break;
               }
            }
         }

         if (!this.options.ignoreMethodBodies && (!this.diet || insideFieldInitializer)) {
            constructorCall = SuperReference.implicitSuperConstructorCall();
         }
      }

      ConstructorDeclaration cd = (ConstructorDeclaration)this.astStack[this.astPtr];
      cd.constructorCall = constructorCall;
      cd.statements = statements;
      if (constructorCall != null && cd.constructorCall.sourceEnd == 0) {
         cd.constructorCall.sourceEnd = cd.sourceEnd;
         cd.constructorCall.sourceStart = cd.sourceStart;
      }

      if ((!this.diet || this.dietInt != 0)
         && statements == null
         && (constructorCall == null || constructorCall.isImplicitSuper())
         && !this.containsComment(cd.bodyStart, this.endPosition)) {
         cd.bits |= 8;
      }

      cd.bodyEnd = this.endPosition;
      cd.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeConstructorHeader() {
      AbstractMethodDeclaration method = (AbstractMethodDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         method.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         if (this.currentToken == 28) {
            method.modifiers |= 16777216;
            method.declarationSourceEnd = this.scanner.currentPosition - 1;
            method.bodyEnd = this.scanner.currentPosition - 1;
            if (this.currentElement.parseTree() == method && this.currentElement.parent != null) {
               this.currentElement = this.currentElement.parent;
            }
         }

         this.restartRecovery = true;
      }
   }

   protected void consumeConstructorHeaderName() {
      if (this.currentElement != null && this.lastIgnoredToken == 36) {
         this.lastCheckPoint = this.scanner.startPosition;
         this.restartRecovery = true;
      } else {
         ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);
         cd.selector = this.identifierStack[this.identifierPtr];
         long selectorSource = this.identifierPositionStack[this.identifierPtr--];
         --this.identifierLengthPtr;
         cd.declarationSourceStart = this.intStack[this.intPtr--];
         cd.modifiers = this.intStack[this.intPtr--];
         int length;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, cd.annotations = new Annotation[length], 0, length);
         }

         cd.javadoc = this.javadoc;
         this.javadoc = null;
         cd.sourceStart = (int)(selectorSource >>> 32);
         this.pushOnAstStack(cd);
         cd.sourceEnd = this.lParenPos;
         cd.bodyStart = this.lParenPos + 1;
         this.listLength = 0;
         if (this.currentElement != null) {
            this.lastCheckPoint = cd.bodyStart;
            if (this.currentElement instanceof RecoveredType && this.lastIgnoredToken != 3 || cd.modifiers != 0) {
               this.currentElement = this.currentElement.add(cd, 0);
               this.lastIgnoredToken = -1;
            }
         }
      }
   }

   protected void consumeConstructorHeaderNameWithTypeParameters() {
      if (this.currentElement != null && this.lastIgnoredToken == 36) {
         this.lastCheckPoint = this.scanner.startPosition;
         this.restartRecovery = true;
      } else {
         ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);
         cd.selector = this.identifierStack[this.identifierPtr];
         long selectorSource = this.identifierPositionStack[this.identifierPtr--];
         --this.identifierLengthPtr;
         int length = this.genericsLengthStack[this.genericsLengthPtr--];
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, cd.typeParameters = new TypeParameter[length], 0, length);
         cd.declarationSourceStart = this.intStack[this.intPtr--];
         cd.modifiers = this.intStack[this.intPtr--];
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
            System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, cd.annotations = new Annotation[length], 0, length);
         }

         cd.javadoc = this.javadoc;
         this.javadoc = null;
         cd.sourceStart = (int)(selectorSource >>> 32);
         this.pushOnAstStack(cd);
         cd.sourceEnd = this.lParenPos;
         cd.bodyStart = this.lParenPos + 1;
         this.listLength = 0;
         if (this.currentElement != null) {
            this.lastCheckPoint = cd.bodyStart;
            if (this.currentElement instanceof RecoveredType && this.lastIgnoredToken != 3 || cd.modifiers != 0) {
               this.currentElement = this.currentElement.add(cd, 0);
               this.lastIgnoredToken = -1;
            }
         }
      }
   }

   protected void consumeCreateInitializer() {
      this.pushOnAstStack(new Initializer(null, 0));
   }

   protected void consumeDefaultLabel() {
      CaseStatement defaultStatement = new CaseStatement(null, this.intStack[this.intPtr--], this.intStack[this.intPtr--]);
      if (this.hasLeadingTagComment(FALL_THROUGH_TAG, defaultStatement.sourceStart)) {
         defaultStatement.bits |= 536870912;
      }

      if (this.hasLeadingTagComment(CASES_OMITTED_TAG, defaultStatement.sourceStart)) {
         defaultStatement.bits |= 1073741824;
      }

      this.pushOnAstStack(defaultStatement);
   }

   protected void consumeDefaultModifiers() {
      this.checkComment();
      this.pushOnIntStack(this.modifiers);
      this.pushOnIntStack(this.modifiersSourceStart >= 0 ? this.modifiersSourceStart : this.scanner.startPosition);
      this.resetModifiers();
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeDiet() {
      this.checkComment();
      this.pushOnIntStack(this.modifiersSourceStart);
      this.resetModifiers();
      this.jumpOverMethodBody();
   }

   protected void consumeDims() {
      this.pushOnIntStack(this.dimensions);
      this.dimensions = 0;
   }

   protected void consumeDimWithOrWithOutExpr() {
      this.pushOnExpressionStack(null);
      if (this.currentElement != null && this.currentToken == 49) {
         this.ignoreNextOpeningBrace = true;
         ++this.currentElement.bracketBalance;
      }
   }

   protected void consumeDimWithOrWithOutExprs() {
      this.concatExpressionLists();
   }

   protected void consumeUnionType() {
      this.pushOnAstStack(this.getTypeReference(this.intStack[this.intPtr--]));
      this.optimizedConcatNodeLists();
   }

   protected void consumeUnionTypeAsClassType() {
      this.pushOnAstStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeEmptyAnnotationTypeMemberDeclarationsopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyArgumentListopt() {
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeEmptyArguments() {
      FieldDeclaration fieldDeclaration = (FieldDeclaration)this.astStack[this.astPtr];
      this.pushOnIntStack(fieldDeclaration.sourceEnd);
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeEmptyArrayInitializer() {
      this.arrayInitializer(0);
   }

   protected void consumeEmptyArrayInitializeropt() {
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeEmptyBlockStatementsopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyCatchesopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyClassBodyDeclarationsopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyDimsopt() {
      this.pushOnIntStack(0);
   }

   protected void consumeEmptyEnumDeclarations() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyExpression() {
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeEmptyForInitopt() {
      this.pushOnAstLengthStack(0);
      this.forStartPosition = 0;
   }

   protected void consumeEmptyForUpdateopt() {
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumeEmptyInterfaceMemberDeclarationsopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyInternalCompilationUnit() {
      if (this.compilationUnit.isPackageInfo()) {
         this.compilationUnit.types = new TypeDeclaration[1];
         this.compilationUnit.createPackageInfoType();
      }
   }

   protected void consumeEmptyMemberValueArrayInitializer() {
      this.arrayInitializer(0);
   }

   protected void consumeEmptyMemberValuePairsopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyMethodHeaderDefaultValue() {
      AbstractMethodDeclaration method = (AbstractMethodDeclaration)this.astStack[this.astPtr];
      if (method.isAnnotationMethod()) {
         this.pushOnExpressionStackLengthStack(0);
      }

      this.recordStringLiterals = true;
   }

   protected void consumeEmptyStatement() {
      char[] source = this.scanner.source;
      if (source[this.endStatementPosition] == ';') {
         this.pushOnAstStack(new EmptyStatement(this.endStatementPosition, this.endStatementPosition));
      } else {
         if (source.length > 5) {
            int c1 = 0;
            int c2 = 0;
            int c3 = 0;
            int c4 = 0;
            int pos = this.endStatementPosition - 4;

            while(source[pos] == 'u') {
               --pos;
            }

            if (source[pos] == '\\'
               && (c1 = ScannerHelper.getHexadecimalValue(source[this.endStatementPosition - 3])) <= 15
               && c1 >= 0
               && (c2 = ScannerHelper.getHexadecimalValue(source[this.endStatementPosition - 2])) <= 15
               && c2 >= 0
               && (c3 = ScannerHelper.getHexadecimalValue(source[this.endStatementPosition - 1])) <= 15
               && c3 >= 0
               && (c4 = ScannerHelper.getHexadecimalValue(source[this.endStatementPosition])) <= 15
               && c4 >= 0
               && (char)(((c1 * 16 + c2) * 16 + c3) * 16 + c4) == ';') {
               this.pushOnAstStack(new EmptyStatement(pos, this.endStatementPosition));
               return;
            }
         }

         this.pushOnAstStack(new EmptyStatement(this.endPosition + 1, this.endStatementPosition));
      }
   }

   protected void consumeEmptySwitchBlock() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeEmptyTypeDeclaration() {
      this.pushOnAstLengthStack(0);
      if (!this.statementRecoveryActivated) {
         this.problemReporter().superfluousSemicolon(this.endPosition + 1, this.endStatementPosition);
      }

      this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeEnhancedForStatement() {
      --this.astLengthPtr;
      Statement statement = (Statement)this.astStack[this.astPtr--];
      ForeachStatement foreachStatement = (ForeachStatement)this.astStack[this.astPtr];
      foreachStatement.action = statement;
      if (statement instanceof EmptyStatement) {
         statement.bits |= 1;
      }

      foreachStatement.sourceEnd = this.endStatementPosition;
   }

   protected void consumeEnhancedForStatementHeader() {
      ForeachStatement statement = (ForeachStatement)this.astStack[this.astPtr];
      --this.expressionLengthPtr;
      Expression collection = this.expressionStack[this.expressionPtr--];
      statement.collection = collection;
      statement.elementVariable.declarationSourceEnd = collection.sourceEnd;
      statement.elementVariable.declarationEnd = collection.sourceEnd;
      statement.sourceEnd = this.rParenPos;
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfForeachStatements(statement.elementVariable, collection);
      }
   }

   protected void consumeEnhancedForStatementHeaderInit(boolean hasModifiers) {
      char[] identifierName = this.identifierStack[this.identifierPtr];
      long namePosition = this.identifierPositionStack[this.identifierPtr];
      LocalDeclaration localDeclaration = this.createLocalDeclaration(identifierName, (int)(namePosition >>> 32), (int)namePosition);
      localDeclaration.declarationSourceEnd = localDeclaration.declarationEnd;
      localDeclaration.bits |= 16;
      int extraDims = this.intStack[this.intPtr--];
      Annotation[][] annotationsOnExtendedDimensions = extraDims == 0 ? null : this.getAnnotationsOnDimensions(extraDims);
      --this.identifierPtr;
      --this.identifierLengthPtr;
      int declarationSourceStart = 0;
      int modifiersValue = 0;
      if (hasModifiers) {
         declarationSourceStart = this.intStack[this.intPtr--];
         modifiersValue = this.intStack[this.intPtr--];
      } else {
         this.intPtr -= 2;
      }

      TypeReference type = this.getTypeReference(this.intStack[this.intPtr--]);
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, localDeclaration.annotations = new Annotation[length], 0, length);
         localDeclaration.bits |= 1048576;
      }

      if (extraDims != 0) {
         type = this.augmentTypeWithAdditionalDimensions(type, extraDims, annotationsOnExtendedDimensions, false);
      }

      if (hasModifiers) {
         localDeclaration.declarationSourceStart = declarationSourceStart;
         localDeclaration.modifiers = modifiersValue;
      } else {
         localDeclaration.declarationSourceStart = type.sourceStart;
      }

      localDeclaration.type = type;
      localDeclaration.bits |= type.bits & 1048576;
      ForeachStatement iteratorForStatement = new ForeachStatement(localDeclaration, this.intStack[this.intPtr--]);
      this.pushOnAstStack(iteratorForStatement);
      iteratorForStatement.sourceEnd = localDeclaration.declarationSourceEnd;
      this.forStartPosition = 0;
   }

   protected void consumeEnterAnonymousClassBody(boolean qualified) {
      TypeReference typeReference = this.getTypeReference(0);
      TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
      anonymousType.name = CharOperation.NO_CHAR;
      anonymousType.bits |= 768;
      anonymousType.bits |= typeReference.bits & 1048576;
      QualifiedAllocationExpression alloc = new QualifiedAllocationExpression(anonymousType);
      this.markEnclosingMemberWithLocalType();
      this.pushOnAstStack(anonymousType);
      alloc.sourceEnd = this.rParenPos;
      int argumentLength;
      if ((argumentLength = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         this.expressionPtr -= argumentLength;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, alloc.arguments = new Expression[argumentLength], 0, argumentLength);
      }

      if (qualified) {
         --this.expressionLengthPtr;
         alloc.enclosingInstance = this.expressionStack[this.expressionPtr--];
      }

      alloc.type = typeReference;
      anonymousType.sourceEnd = alloc.sourceEnd;
      anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
      alloc.sourceStart = this.intStack[this.intPtr--];
      this.pushOnExpressionStack(alloc);
      anonymousType.bodyStart = this.scanner.currentPosition;
      this.listLength = 0;
      this.scanner.commentPtr = -1;
      if (this.currentElement != null) {
         this.lastCheckPoint = anonymousType.bodyStart;
         this.currentElement = this.currentElement.add(anonymousType, 0);
         if (!(this.currentElement instanceof RecoveredAnnotation)) {
            if (this.isIndirectlyInsideLambdaExpression()) {
               this.ignoreNextOpeningBrace = true;
            } else {
               this.currentToken = 0;
            }
         } else {
            this.ignoreNextOpeningBrace = true;
            ++this.currentElement.bracketBalance;
         }

         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeEnterCompilationUnit() {
   }

   protected void consumeEnterMemberValue() {
      if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
         RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation)this.currentElement;
         recoveredAnnotation.hasPendingMemberValueName = true;
      }
   }

   protected void consumeEnterMemberValueArrayInitializer() {
      if (this.currentElement != null) {
         this.ignoreNextOpeningBrace = true;
         ++this.currentElement.bracketBalance;
      }
   }

   protected void consumeEnterVariable() {
      char[] identifierName = this.identifierStack[this.identifierPtr];
      long namePosition = this.identifierPositionStack[this.identifierPtr];
      int extendedDimensions = this.intStack[this.intPtr--];
      Annotation[][] annotationsOnExtendedDimensions = extendedDimensions == 0 ? null : this.getAnnotationsOnDimensions(extendedDimensions);
      boolean isLocalDeclaration = this.nestedMethod[this.nestedType] != 0;
      AbstractVariableDeclaration declaration;
      if (isLocalDeclaration) {
         declaration = this.createLocalDeclaration(identifierName, (int)(namePosition >>> 32), (int)namePosition);
      } else {
         declaration = this.createFieldDeclaration(identifierName, (int)(namePosition >>> 32), (int)namePosition);
      }

      --this.identifierPtr;
      --this.identifierLengthPtr;
      int variableIndex = this.variablesCounter[this.nestedType];
      TypeReference type;
      if (variableIndex == 0) {
         if (isLocalDeclaration) {
            declaration.declarationSourceStart = this.intStack[this.intPtr--];
            declaration.modifiers = this.intStack[this.intPtr--];
            int length;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
               System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, declaration.annotations = new Annotation[length], 0, length);
            }

            type = this.getTypeReference(this.intStack[this.intPtr--]);
            if (declaration.declarationSourceStart == -1) {
               declaration.declarationSourceStart = type.sourceStart;
            }

            this.pushOnAstStack(type);
         } else {
            type = this.getTypeReference(this.intStack[this.intPtr--]);
            this.pushOnAstStack(type);
            declaration.declarationSourceStart = this.intStack[this.intPtr--];
            declaration.modifiers = this.intStack[this.intPtr--];
            int length;
            if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
               System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, declaration.annotations = new Annotation[length], 0, length);
            }

            FieldDeclaration fieldDeclaration = (FieldDeclaration)declaration;
            fieldDeclaration.javadoc = this.javadoc;
         }

         this.javadoc = null;
      } else {
         type = (TypeReference)this.astStack[this.astPtr - variableIndex];
         AbstractVariableDeclaration previousVariable = (AbstractVariableDeclaration)this.astStack[this.astPtr];
         declaration.declarationSourceStart = previousVariable.declarationSourceStart;
         declaration.modifiers = previousVariable.modifiers;
         Annotation[] annotations = previousVariable.annotations;
         if (annotations != null) {
            int annotationsLength = annotations.length;
            System.arraycopy(annotations, 0, declaration.annotations = new Annotation[annotationsLength], 0, annotationsLength);
         }
      }

      declaration.type = extendedDimensions == 0
         ? type
         : this.augmentTypeWithAdditionalDimensions(type, extendedDimensions, annotationsOnExtendedDimensions, false);
      declaration.bits |= type.bits & 1048576;
      this.variablesCounter[this.nestedType]++;
      this.pushOnAstStack(declaration);
      if (this.currentElement != null) {
         if (!(this.currentElement instanceof RecoveredType)
            && (
               this.currentToken == 3
                  || Util.getLineNumber(declaration.type.sourceStart, this.scanner.lineEnds, 0, this.scanner.linePtr)
                     != Util.getLineNumber((int)(namePosition >>> 32), this.scanner.lineEnds, 0, this.scanner.linePtr)
            )) {
            this.lastCheckPoint = (int)(namePosition >>> 32);
            this.restartRecovery = true;
            return;
         }

         if (isLocalDeclaration) {
            LocalDeclaration localDecl = (LocalDeclaration)this.astStack[this.astPtr];
            this.lastCheckPoint = localDecl.sourceEnd + 1;
            this.currentElement = this.currentElement.add(localDecl, 0);
         } else {
            FieldDeclaration fieldDecl = (FieldDeclaration)this.astStack[this.astPtr];
            this.lastCheckPoint = fieldDecl.sourceEnd + 1;
            this.currentElement = this.currentElement.add(fieldDecl, 0);
         }

         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeEnumBodyNoConstants() {
   }

   protected void consumeEnumBodyWithConstants() {
      this.concatNodeLists();
   }

   protected void consumeEnumConstantHeader() {
      FieldDeclaration enumConstant = (FieldDeclaration)this.astStack[this.astPtr];
      boolean foundOpeningBrace = this.currentToken == 49;
      if (foundOpeningBrace) {
         TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
         anonymousType.name = CharOperation.NO_CHAR;
         anonymousType.bits |= 768;
         int start = this.scanner.startPosition;
         anonymousType.declarationSourceStart = start;
         anonymousType.sourceStart = start;
         anonymousType.sourceEnd = start;
         anonymousType.modifiers = 0;
         anonymousType.bodyStart = this.scanner.currentPosition;
         this.markEnclosingMemberWithLocalType();
         this.consumeNestedType();
         this.variablesCounter[this.nestedType]++;
         this.pushOnAstStack(anonymousType);
         QualifiedAllocationExpression allocationExpression = new QualifiedAllocationExpression(anonymousType);
         allocationExpression.enumConstant = enumConstant;
         int length;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(this.expressionStack, this.expressionPtr + 1, allocationExpression.arguments = new Expression[length], 0, length);
         }

         enumConstant.initialization = allocationExpression;
      } else {
         AllocationExpression allocationExpression = new AllocationExpression();
         allocationExpression.enumConstant = enumConstant;
         int length;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            this.expressionPtr -= length;
            System.arraycopy(this.expressionStack, this.expressionPtr + 1, allocationExpression.arguments = new Expression[length], 0, length);
         }

         enumConstant.initialization = allocationExpression;
      }

      enumConstant.initialization.sourceStart = enumConstant.declarationSourceStart;
      if (this.currentElement != null) {
         if (foundOpeningBrace) {
            TypeDeclaration anonymousType = (TypeDeclaration)this.astStack[this.astPtr];
            this.currentElement = this.currentElement.add(anonymousType, 0);
            this.lastCheckPoint = anonymousType.bodyStart;
            this.lastIgnoredToken = -1;
            if (this.isIndirectlyInsideLambdaExpression()) {
               this.ignoreNextOpeningBrace = true;
            } else {
               this.currentToken = 0;
            }
         } else {
            if (this.currentToken == 28) {
               RecoveredType currentType = this.currentRecoveryType();
               if (currentType != null) {
                  currentType.insideEnumConstantPart = false;
               }
            }

            this.lastCheckPoint = this.scanner.startPosition;
            this.lastIgnoredToken = -1;
            this.restartRecovery = true;
         }
      }
   }

   protected void consumeEnumConstantHeaderName() {
      if (this.currentElement == null
         || (
               this.currentElement instanceof RecoveredType
                  || this.currentElement instanceof RecoveredField && ((RecoveredField)this.currentElement).fieldDeclaration.type == null
            )
            && this.lastIgnoredToken != 3) {
         long namePosition = this.identifierPositionStack[this.identifierPtr];
         char[] constantName = this.identifierStack[this.identifierPtr];
         int sourceEnd = (int)namePosition;
         FieldDeclaration enumConstant = this.createFieldDeclaration(constantName, (int)(namePosition >>> 32), sourceEnd);
         --this.identifierPtr;
         --this.identifierLengthPtr;
         enumConstant.modifiersSourceStart = this.intStack[this.intPtr--];
         enumConstant.modifiers = this.intStack[this.intPtr--];
         enumConstant.declarationSourceStart = enumConstant.modifiersSourceStart;
         int length;
         if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
            System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, enumConstant.annotations = new Annotation[length], 0, length);
            enumConstant.bits |= 1048576;
         }

         this.pushOnAstStack(enumConstant);
         if (this.currentElement != null) {
            this.lastCheckPoint = enumConstant.sourceEnd + 1;
            this.currentElement = this.currentElement.add(enumConstant, 0);
         }

         enumConstant.javadoc = this.javadoc;
         this.javadoc = null;
      } else {
         this.lastCheckPoint = this.scanner.startPosition;
         this.restartRecovery = true;
      }
   }

   protected void consumeEnumConstantNoClassBody() {
      int endOfEnumConstant = this.intStack[this.intPtr--];
      FieldDeclaration fieldDeclaration = (FieldDeclaration)this.astStack[this.astPtr];
      fieldDeclaration.declarationEnd = endOfEnumConstant;
      fieldDeclaration.declarationSourceEnd = endOfEnumConstant;
      ASTNode initialization = fieldDeclaration.initialization;
      if (initialization != null) {
         initialization.sourceEnd = endOfEnumConstant;
      }
   }

   protected void consumeEnumConstants() {
      this.concatNodeLists();
   }

   protected void consumeEnumConstantWithClassBody() {
      this.dispatchDeclarationInto(this.astLengthStack[this.astLengthPtr--]);
      TypeDeclaration anonymousType = (TypeDeclaration)this.astStack[this.astPtr--];
      --this.astLengthPtr;
      anonymousType.bodyEnd = this.endPosition;
      anonymousType.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      FieldDeclaration fieldDeclaration = (FieldDeclaration)this.astStack[this.astPtr];
      fieldDeclaration.declarationEnd = this.endStatementPosition;
      int declarationSourceEnd = anonymousType.declarationSourceEnd;
      fieldDeclaration.declarationSourceEnd = declarationSourceEnd;
      --this.intPtr;
      this.variablesCounter[this.nestedType] = 0;
      --this.nestedType;
      ASTNode initialization = fieldDeclaration.initialization;
      if (initialization != null) {
         initialization.sourceEnd = declarationSourceEnd;
      }
   }

   protected void consumeEnumDeclaration() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.dispatchDeclarationIntoEnumDeclaration(length);
      }

      TypeDeclaration enumDeclaration = (TypeDeclaration)this.astStack[this.astPtr];
      boolean hasConstructor = enumDeclaration.checkConstructors(this);
      if (!hasConstructor) {
         boolean insideFieldInitializer = false;
         if (this.diet) {
            for(int i = this.nestedType; i > 0; --i) {
               if (this.variablesCounter[i] > 0) {
                  insideFieldInitializer = true;
                  break;
               }
            }
         }

         enumDeclaration.createDefaultConstructor(!this.diet || insideFieldInitializer, true);
      }

      if (this.scanner.containsAssertKeyword) {
         enumDeclaration.bits |= 1;
      }

      enumDeclaration.addClinit();
      enumDeclaration.bodyEnd = this.endStatementPosition;
      if (length == 0 && !this.containsComment(enumDeclaration.bodyStart, enumDeclaration.bodyEnd)) {
         enumDeclaration.bits |= 8;
      }

      enumDeclaration.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeEnumDeclarations() {
   }

   protected void consumeEnumHeader() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         typeDecl.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         this.restartRecovery = true;
      }

      this.scanner.commentPtr = -1;
   }

   protected void consumeEnumHeaderName() {
      TypeDeclaration enumDeclaration = new TypeDeclaration(this.compilationUnit.compilationResult);
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            enumDeclaration.bits |= 1024;
         }
      } else {
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      enumDeclaration.sourceEnd = (int)pos;
      enumDeclaration.sourceStart = (int)(pos >>> 32);
      enumDeclaration.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      enumDeclaration.declarationSourceStart = this.intStack[this.intPtr--];
      --this.intPtr;
      enumDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
      enumDeclaration.modifiers = this.intStack[this.intPtr--] | 16384;
      if (enumDeclaration.modifiersSourceStart >= 0) {
         enumDeclaration.declarationSourceStart = enumDeclaration.modifiersSourceStart;
      }

      if ((enumDeclaration.bits & 1024) == 0
         && (enumDeclaration.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(enumDeclaration.name, this.compilationUnit.getMainTypeName())) {
         enumDeclaration.bits |= 4096;
      }

      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, enumDeclaration.annotations = new Annotation[length], 0, length);
      }

      enumDeclaration.bodyStart = enumDeclaration.sourceEnd + 1;
      this.pushOnAstStack(enumDeclaration);
      this.listLength = 0;
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfEnumDeclarations(enumDeclaration);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = enumDeclaration.bodyStart;
         this.currentElement = this.currentElement.add(enumDeclaration, 0);
         this.lastIgnoredToken = -1;
      }

      enumDeclaration.javadoc = this.javadoc;
      this.javadoc = null;
   }

   protected void consumeEnumHeaderNameWithTypeParameters() {
      TypeDeclaration enumDeclaration = new TypeDeclaration(this.compilationUnit.compilationResult);
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, enumDeclaration.typeParameters = new TypeParameter[length], 0, length);
      this.problemReporter().invalidUsageOfTypeParametersForEnumDeclaration(enumDeclaration);
      enumDeclaration.bodyStart = enumDeclaration.typeParameters[length - 1].declarationSourceEnd + 1;
      this.listTypeParameterLength = 0;
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            enumDeclaration.bits |= 1024;
         }
      } else {
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      enumDeclaration.sourceEnd = (int)pos;
      enumDeclaration.sourceStart = (int)(pos >>> 32);
      enumDeclaration.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      enumDeclaration.declarationSourceStart = this.intStack[this.intPtr--];
      --this.intPtr;
      enumDeclaration.modifiersSourceStart = this.intStack[this.intPtr--];
      enumDeclaration.modifiers = this.intStack[this.intPtr--] | 16384;
      if (enumDeclaration.modifiersSourceStart >= 0) {
         enumDeclaration.declarationSourceStart = enumDeclaration.modifiersSourceStart;
      }

      if ((enumDeclaration.bits & 1024) == 0
         && (enumDeclaration.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(enumDeclaration.name, this.compilationUnit.getMainTypeName())) {
         enumDeclaration.bits |= 4096;
      }

      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, enumDeclaration.annotations = new Annotation[length], 0, length);
      }

      enumDeclaration.bodyStart = enumDeclaration.sourceEnd + 1;
      this.pushOnAstStack(enumDeclaration);
      this.listLength = 0;
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfEnumDeclarations(enumDeclaration);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = enumDeclaration.bodyStart;
         this.currentElement = this.currentElement.add(enumDeclaration, 0);
         this.lastIgnoredToken = -1;
      }

      enumDeclaration.javadoc = this.javadoc;
      this.javadoc = null;
   }

   protected void consumeEqualityExpression(int op) {
      --this.expressionPtr;
      --this.expressionLengthPtr;
      this.expressionStack[this.expressionPtr] = new EqualExpression(
         this.expressionStack[this.expressionPtr], this.expressionStack[this.expressionPtr + 1], op
      );
   }

   protected void consumeEqualityExpressionWithName(int op) {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
      --this.expressionPtr;
      --this.expressionLengthPtr;
      this.expressionStack[this.expressionPtr] = new EqualExpression(
         this.expressionStack[this.expressionPtr + 1], this.expressionStack[this.expressionPtr], op
      );
   }

   protected void consumeExitMemberValue() {
      if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
         RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation)this.currentElement;
         recoveredAnnotation.hasPendingMemberValueName = false;
         recoveredAnnotation.memberValuPairEqualEnd = -1;
      }
   }

   protected void consumeExitTryBlock() {
      if (this.currentElement != null) {
         this.restartRecovery = true;
      }
   }

   protected void consumeExitVariableWithInitialization() {
      --this.expressionLengthPtr;
      AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration)this.astStack[this.astPtr];
      variableDecl.initialization = this.expressionStack[this.expressionPtr--];
      variableDecl.declarationSourceEnd = variableDecl.initialization.sourceEnd;
      variableDecl.declarationEnd = variableDecl.initialization.sourceEnd;
      this.recoveryExitFromVariable();
   }

   protected void consumeExitVariableWithoutInitialization() {
      AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration)this.astStack[this.astPtr];
      variableDecl.declarationSourceEnd = variableDecl.declarationEnd;
      if (this.currentElement != null && this.currentElement instanceof RecoveredField && this.endStatementPosition > variableDecl.sourceEnd) {
         this.currentElement.updateSourceEndIfNecessary(this.endStatementPosition);
      }

      this.recoveryExitFromVariable();
   }

   protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
      int startPosition = this.intStack[this.intPtr--];
      ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, ecc.arguments = new Expression[length], 0, length);
      }

      switch(flag) {
         case 0:
            ecc.sourceStart = startPosition;
            break;
         case 1:
            --this.expressionLengthPtr;
            ecc.sourceStart = (ecc.qualification = this.expressionStack[this.expressionPtr--]).sourceStart;
            break;
         case 2:
            ecc.sourceStart = (ecc.qualification = this.getUnspecifiedReferenceOptimized()).sourceStart;
      }

      this.pushOnAstStack(ecc);
      ecc.sourceEnd = this.endStatementPosition;
   }

   protected void consumeExplicitConstructorInvocationWithTypeArguments(int flag, int recFlag) {
      int startPosition = this.intStack[this.intPtr--];
      ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, ecc.arguments = new Expression[length], 0, length);
      }

      length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, ecc.typeArguments = new TypeReference[length], 0, length);
      ecc.typeArgumentsSourceStart = this.intStack[this.intPtr--];
      switch(flag) {
         case 0:
            ecc.sourceStart = startPosition;
            break;
         case 1:
            --this.expressionLengthPtr;
            ecc.sourceStart = (ecc.qualification = this.expressionStack[this.expressionPtr--]).sourceStart;
            break;
         case 2:
            ecc.sourceStart = (ecc.qualification = this.getUnspecifiedReferenceOptimized()).sourceStart;
      }

      this.pushOnAstStack(ecc);
      ecc.sourceEnd = this.endStatementPosition;
   }

   protected void consumeExpressionStatement() {
      --this.expressionLengthPtr;
      Expression expression = this.expressionStack[this.expressionPtr--];
      expression.statementEnd = this.endStatementPosition;
      expression.bits |= 16;
      this.pushOnAstStack(expression);
   }

   protected void consumeFieldAccess(boolean isSuperAccess) {
      FieldReference fr = new FieldReference(this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr--]);
      --this.identifierLengthPtr;
      if (isSuperAccess) {
         fr.sourceStart = this.intStack[this.intPtr--];
         fr.receiver = new SuperReference(fr.sourceStart, this.endPosition);
         this.pushOnExpressionStack(fr);
      } else {
         fr.receiver = this.expressionStack[this.expressionPtr];
         fr.sourceStart = fr.receiver.sourceStart;
         this.expressionStack[this.expressionPtr] = fr;
      }
   }

   protected void consumeFieldDeclaration() {
      int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];

      for(int i = variableDeclaratorsCounter - 1; i >= 0; --i) {
         FieldDeclaration fieldDeclaration = (FieldDeclaration)this.astStack[this.astPtr - i];
         fieldDeclaration.declarationSourceEnd = this.endStatementPosition;
         fieldDeclaration.declarationEnd = this.endStatementPosition;
      }

      this.updateSourceDeclarationParts(variableDeclaratorsCounter);
      int endPos = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      if (endPos != this.endStatementPosition) {
         for(int i = 0; i < variableDeclaratorsCounter; ++i) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration)this.astStack[this.astPtr - i];
            fieldDeclaration.declarationSourceEnd = endPos;
         }
      }

      int startIndex = this.astPtr - this.variablesCounter[this.nestedType] + 1;
      System.arraycopy(this.astStack, startIndex, this.astStack, startIndex - 1, variableDeclaratorsCounter);
      --this.astPtr;
      this.astLengthStack[--this.astLengthPtr] = variableDeclaratorsCounter;
      if (this.currentElement != null) {
         this.lastCheckPoint = endPos + 1;
         if (this.currentElement.parent != null && this.currentElement instanceof RecoveredField && !(this.currentElement instanceof RecoveredInitializer)) {
            this.currentElement = this.currentElement.parent;
         }

         this.restartRecovery = true;
      }

      this.variablesCounter[this.nestedType] = 0;
   }

   protected void consumeForceNoDiet() {
      ++this.dietInt;
   }

   protected void consumeForInit() {
      this.pushOnAstLengthStack(-1);
      this.forStartPosition = 0;
   }

   protected void consumeFormalParameter(boolean isVarArgs) {
      NameReference qualifyingNameReference = null;
      boolean isReceiver = this.intStack[this.intPtr--] == 0;
      if (isReceiver) {
         qualifyingNameReference = (NameReference)this.expressionStack[this.expressionPtr--];
         --this.expressionLengthPtr;
      }

      --this.identifierLengthPtr;
      char[] identifierName = this.identifierStack[this.identifierPtr];
      long namePositions = this.identifierPositionStack[this.identifierPtr--];
      int extendedDimensions = this.intStack[this.intPtr--];
      Annotation[][] annotationsOnExtendedDimensions = extendedDimensions == 0 ? null : this.getAnnotationsOnDimensions(extendedDimensions);
      Annotation[] varArgsAnnotations = null;
      int endOfEllipsis = 0;
      if (isVarArgs) {
         endOfEllipsis = this.intStack[this.intPtr--];
         int length;
         if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
            System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, varArgsAnnotations = new Annotation[length], 0, length);
         }
      }

      int firstDimensions = this.intStack[this.intPtr--];
      TypeReference type = this.getTypeReference(firstDimensions);
      if (isVarArgs || extendedDimensions != 0) {
         if (isVarArgs) {
            type = this.augmentTypeWithAdditionalDimensions(type, 1, varArgsAnnotations != null ? new Annotation[][]{varArgsAnnotations} : null, true);
         }

         if (extendedDimensions != 0) {
            type = this.augmentTypeWithAdditionalDimensions(type, extendedDimensions, annotationsOnExtendedDimensions, false);
         }

         type.sourceEnd = type.isParameterizedTypeReference() ? this.endStatementPosition : this.endPosition;
      }

      if (isVarArgs) {
         if (extendedDimensions == 0) {
            type.sourceEnd = endOfEllipsis;
         }

         type.bits |= 16384;
      }

      int modifierPositions = this.intStack[this.intPtr--];
      Argument arg;
      if (isReceiver) {
         arg = new Receiver(identifierName, namePositions, type, qualifyingNameReference, this.intStack[this.intPtr--] & -1048577);
      } else {
         arg = new Argument(identifierName, namePositions, type, this.intStack[this.intPtr--] & -1048577);
      }

      arg.declarationSourceStart = modifierPositions;
      arg.bits |= type.bits & 1048576;
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, arg.annotations = new Annotation[length], 0, length);
         arg.bits |= 1048576;
         RecoveredType currentRecoveryType = this.currentRecoveryType();
         if (currentRecoveryType != null) {
            currentRecoveryType.annotationsConsumed(arg.annotations);
         }
      }

      this.pushOnAstStack(arg);
      ++this.listLength;
      if (isVarArgs) {
         if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition
            )
          {
            this.problemReporter().invalidUsageOfVarargs(arg);
         } else if (!this.statementRecoveryActivated && extendedDimensions > 0) {
            this.problemReporter().illegalExtendedDimensions(arg);
         }
      }
   }

   protected Annotation[][] getAnnotationsOnDimensions(int dimensionsCount) {
      Annotation[][] dimensionsAnnotations = null;
      if (dimensionsCount > 0) {
         for(int i = 0; i < dimensionsCount; ++i) {
            Annotation[] annotations = null;
            int length;
            if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
               System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, annotations = new Annotation[length], 0, length);
               if (dimensionsAnnotations == null) {
                  dimensionsAnnotations = new Annotation[dimensionsCount][];
               }

               dimensionsAnnotations[dimensionsCount - i - 1] = annotations;
            }
         }
      }

      return dimensionsAnnotations;
   }

   protected void consumeFormalParameterList() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeFormalParameterListopt() {
      this.pushOnAstLengthStack(0);
   }

   protected void consumeGenericType() {
   }

   protected void consumeGenericTypeArrayType() {
   }

   protected void consumeGenericTypeNameArrayType() {
   }

   protected void consumeGenericTypeWithDiamond() {
      this.pushOnGenericsLengthStack(-1);
      this.concatGenericsLists();
      --this.intPtr;
   }

   protected void consumeImportDeclaration() {
      ImportReference impt = (ImportReference)this.astStack[this.astPtr];
      impt.declarationEnd = this.endStatementPosition;
      impt.declarationSourceEnd = this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd);
      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.currentElement = this.currentElement.add(impt, 0);
         this.lastIgnoredToken = -1;
         this.restartRecovery = true;
      }
   }

   protected void consumeImportDeclarations() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeInsideCastExpression() {
   }

   protected void consumeInsideCastExpressionLL1() {
      this.pushOnGenericsLengthStack(0);
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnExpressionStack(this.getTypeReference(0));
   }

   protected void consumeInsideCastExpressionLL1WithBounds() {
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      TypeReference[] bounds = new TypeReference[additionalBoundsLength + 1];
      this.genericsPtr -= additionalBoundsLength;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 1, additionalBoundsLength);
      this.pushOnGenericsLengthStack(0);
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      bounds[0] = this.getTypeReference(0);

      for(int i = 0; i <= additionalBoundsLength; ++i) {
         this.pushOnExpressionStack(bounds[i]);
         if (i > 0) {
            this.expressionLengthStack[--this.expressionLengthPtr]++;
         }
      }
   }

   protected void consumeInsideCastExpressionWithQualifiedGenerics() {
   }

   protected void consumeInstanceOfExpression() {
      Expression exp;
      this.expressionStack[this.expressionPtr] = exp = new InstanceOfExpression(
         this.expressionStack[this.expressionPtr], this.getTypeReference(this.intStack[this.intPtr--])
      );
      if (exp.sourceEnd == 0) {
         exp.sourceEnd = this.scanner.startPosition - 1;
      }
   }

   protected void consumeInstanceOfExpressionWithName() {
      TypeReference reference = this.getTypeReference(this.intStack[this.intPtr--]);
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
      Expression exp;
      this.expressionStack[this.expressionPtr] = exp = new InstanceOfExpression(this.expressionStack[this.expressionPtr], reference);
      if (exp.sourceEnd == 0) {
         exp.sourceEnd = this.scanner.startPosition - 1;
      }
   }

   protected void consumeInterfaceDeclaration() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.dispatchDeclarationInto(length);
      }

      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      typeDecl.checkConstructors(this);
      FieldDeclaration[] fields = typeDecl.fields;
      int fieldCount = fields == null ? 0 : fields.length;

      for(int i = 0; i < fieldCount; ++i) {
         FieldDeclaration field = fields[i];
         if (field instanceof Initializer) {
            this.problemReporter().interfaceCannotHaveInitializers(typeDecl.name, field);
         }
      }

      if (this.scanner.containsAssertKeyword) {
         typeDecl.bits |= 1;
      }

      typeDecl.addClinit();
      typeDecl.bodyEnd = this.endStatementPosition;
      if (length == 0 && !this.containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
         typeDecl.bits |= 8;
      }

      typeDecl.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
   }

   protected void consumeInterfaceHeader() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         typeDecl.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         this.restartRecovery = true;
      }

      this.scanner.commentPtr = -1;
   }

   protected void consumeInterfaceHeaderExtends() {
      int length = this.astLengthStack[this.astLengthPtr--];
      this.astPtr -= length;
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      System.arraycopy(this.astStack, this.astPtr + 1, typeDecl.superInterfaces = new TypeReference[length], 0, length);
      TypeReference[] superinterfaces = typeDecl.superInterfaces;
      int i = 0;

      for(int max = superinterfaces.length; i < max; ++i) {
         TypeReference typeReference = superinterfaces[i];
         typeDecl.bits |= typeReference.bits & 1048576;
         typeReference.bits |= 16;
      }

      typeDecl.bodyStart = typeDecl.superInterfaces[length - 1].sourceEnd + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = typeDecl.bodyStart;
      }
   }

   protected void consumeInterfaceHeaderName1() {
      TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
      if (this.nestedMethod[this.nestedType] == 0) {
         if (this.nestedType != 0) {
            typeDecl.bits |= 1024;
         }
      } else {
         typeDecl.bits |= 256;
         this.markEnclosingMemberWithLocalType();
         this.blockReal();
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      typeDecl.sourceEnd = (int)pos;
      typeDecl.sourceStart = (int)(pos >>> 32);
      typeDecl.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      typeDecl.declarationSourceStart = this.intStack[this.intPtr--];
      --this.intPtr;
      typeDecl.modifiersSourceStart = this.intStack[this.intPtr--];
      typeDecl.modifiers = this.intStack[this.intPtr--] | 512;
      if (typeDecl.modifiersSourceStart >= 0) {
         typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
      }

      if ((typeDecl.bits & 1024) == 0
         && (typeDecl.bits & 256) == 0
         && this.compilationUnit != null
         && !CharOperation.equals(typeDecl.name, this.compilationUnit.getMainTypeName())) {
         typeDecl.bits |= 4096;
      }

      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, typeDecl.annotations = new Annotation[length], 0, length);
      }

      typeDecl.bodyStart = typeDecl.sourceEnd + 1;
      this.pushOnAstStack(typeDecl);
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = typeDecl.bodyStart;
         this.currentElement = this.currentElement.add(typeDecl, 0);
         this.lastIgnoredToken = -1;
      }

      typeDecl.javadoc = this.javadoc;
      this.javadoc = null;
   }

   protected void consumeInterfaceMemberDeclarations() {
      this.concatNodeLists();
   }

   protected void consumeInterfaceMemberDeclarationsopt() {
      --this.nestedType;
   }

   protected void consumeInterfaceType() {
      this.pushOnAstStack(this.getTypeReference(0));
      ++this.listLength;
   }

   protected void consumeInterfaceTypeList() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeInternalCompilationUnit() {
      if (this.compilationUnit.isPackageInfo()) {
         this.compilationUnit.types = new TypeDeclaration[1];
         this.compilationUnit.createPackageInfoType();
      }
   }

   protected void consumeInternalCompilationUnitWithTypes() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         if (this.compilationUnit.isPackageInfo()) {
            this.compilationUnit.types = new TypeDeclaration[length + 1];
            this.astPtr -= length;
            System.arraycopy(this.astStack, this.astPtr + 1, this.compilationUnit.types, 1, length);
            this.compilationUnit.createPackageInfoType();
         } else {
            this.compilationUnit.types = new TypeDeclaration[length];
            this.astPtr -= length;
            System.arraycopy(this.astStack, this.astPtr + 1, this.compilationUnit.types, 0, length);
         }
      }
   }

   protected void consumeInvalidAnnotationTypeDeclaration() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (!this.statementRecoveryActivated) {
         this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
      }

      --this.astPtr;
      this.pushOnAstLengthStack(-1);
      this.concatNodeLists();
   }

   protected void consumeInvalidConstructorDeclaration() {
      ConstructorDeclaration cd = (ConstructorDeclaration)this.astStack[this.astPtr];
      cd.bodyEnd = this.endPosition;
      cd.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      cd.modifiers |= 16777216;
   }

   protected void consumeInvalidConstructorDeclaration(boolean hasBody) {
      if (hasBody) {
         --this.intPtr;
      }

      if (hasBody) {
         --this.realBlockPtr;
      }

      int length;
      if (hasBody && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.astPtr -= length;
      }

      ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration)this.astStack[this.astPtr];
      constructorDeclaration.bodyEnd = this.endStatementPosition;
      constructorDeclaration.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      if (!hasBody) {
         constructorDeclaration.modifiers |= 16777216;
      }
   }

   protected void consumeInvalidEnumDeclaration() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (!this.statementRecoveryActivated) {
         this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
      }

      --this.astPtr;
      this.pushOnAstLengthStack(-1);
      this.concatNodeLists();
   }

   protected void consumeInvalidInterfaceDeclaration() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      if (!this.statementRecoveryActivated) {
         this.problemReporter().illegalLocalTypeDeclaration(typeDecl);
      }

      --this.astPtr;
      this.pushOnAstLengthStack(-1);
      this.concatNodeLists();
   }

   protected void consumeInterfaceMethodDeclaration(boolean hasSemicolonBody) {
      int explicitDeclarations = 0;
      Statement[] statements = null;
      if (!hasSemicolonBody) {
         --this.intPtr;
         --this.intPtr;
         explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
         int length;
         if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            if (this.options.ignoreMethodBodies) {
               this.astPtr -= length;
            } else {
               System.arraycopy(this.astStack, (this.astPtr -= length) + 1, statements = new Statement[length], 0, length);
            }
         }
      }

      MethodDeclaration md = (MethodDeclaration)this.astStack[this.astPtr];
      md.statements = statements;
      md.explicitDeclarations = explicitDeclarations;
      md.bodyEnd = this.endPosition;
      md.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      boolean isDefault = (md.modifiers & 65536) != 0;
      boolean isStatic = (md.modifiers & 8) != 0;
      boolean bodyAllowed = isDefault || isStatic;
      if (this.parsingJava8Plus) {
         if (bodyAllowed && hasSemicolonBody) {
            md.modifiers |= 16777216;
         }
      } else {
         if (isDefault) {
            this.problemReporter().defaultMethodsNotBelow18(md);
         }

         if (isStatic) {
            this.problemReporter().staticInterfaceMethodsNotBelow18(md);
         }
      }

      if (!bodyAllowed && !this.statementRecoveryActivated && !hasSemicolonBody) {
         this.problemReporter().abstractMethodNeedingNoBody(md);
      }
   }

   protected void consumeLabel() {
   }

   protected void consumeLeftParen() {
      this.pushOnIntStack(this.lParenPos);
   }

   protected void consumeLocalVariableDeclaration() {
      int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];
      int startIndex = this.astPtr - this.variablesCounter[this.nestedType] + 1;
      System.arraycopy(this.astStack, startIndex, this.astStack, startIndex - 1, variableDeclaratorsCounter);
      --this.astPtr;
      this.astLengthStack[--this.astLengthPtr] = variableDeclaratorsCounter;
      this.variablesCounter[this.nestedType] = 0;
      this.forStartPosition = 0;
   }

   protected void consumeLocalVariableDeclarationStatement() {
      int variableDeclaratorsCounter = this.astLengthStack[this.astLengthPtr];
      if (variableDeclaratorsCounter == 1) {
         LocalDeclaration localDeclaration = (LocalDeclaration)this.astStack[this.astPtr];
         if (localDeclaration.isRecoveredFromLoneIdentifier()) {
            Expression left;
            if (localDeclaration.type instanceof QualifiedTypeReference) {
               QualifiedTypeReference qtr = (QualifiedTypeReference)localDeclaration.type;
               left = new QualifiedNameReference(qtr.tokens, qtr.sourcePositions, 0, 0);
            } else {
               left = new SingleNameReference(localDeclaration.type.getLastToken(), 0L);
            }

            left.sourceStart = localDeclaration.type.sourceStart;
            left.sourceEnd = localDeclaration.type.sourceEnd;
            Expression right = new SingleNameReference(localDeclaration.name, 0L);
            right.sourceStart = localDeclaration.sourceStart;
            right.sourceEnd = localDeclaration.sourceEnd;
            Assignment assignment = new Assignment(left, right, 0);
            int end = this.endStatementPosition;
            assignment.sourceEnd = end == localDeclaration.sourceEnd ? ++end : end;
            assignment.statementEnd = end;
            this.astStack[this.astPtr] = assignment;
            if (this.recoveryScanner != null) {
               RecoveryScannerData data = this.recoveryScanner.getData();
               int position = data.insertedTokensPtr;

               while(position > 0 && data.insertedTokensPosition[position] == data.insertedTokensPosition[position - 1]) {
                  --position;
               }

               if (position >= 0) {
                  this.recoveryScanner.insertTokenAhead(70, position);
               }
            }

            if (this.currentElement != null) {
               this.lastCheckPoint = assignment.sourceEnd + 1;
               this.currentElement = this.currentElement.add(assignment, 0);
            }

            return;
         }
      }

      this.realBlockStack[this.realBlockPtr]++;

      for(int i = variableDeclaratorsCounter - 1; i >= 0; --i) {
         LocalDeclaration localDeclaration = (LocalDeclaration)this.astStack[this.astPtr - i];
         localDeclaration.declarationSourceEnd = this.endStatementPosition;
         localDeclaration.declarationEnd = this.endStatementPosition;
      }
   }

   protected void consumeMarkerAnnotation(boolean isTypeAnnotation) {
      MarkerAnnotation markerAnnotation = null;
      int oldIndex = this.identifierPtr;
      TypeReference typeReference = this.getAnnotationType();
      markerAnnotation = new MarkerAnnotation(typeReference, this.intStack[this.intPtr--]);
      markerAnnotation.declarationSourceEnd = markerAnnotation.sourceEnd;
      if (isTypeAnnotation) {
         this.pushOnTypeAnnotationStack(markerAnnotation);
      } else {
         this.pushOnExpressionStack(markerAnnotation);
      }

      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfAnnotation(markerAnnotation);
      }

      this.recordStringLiterals = true;
      if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
         this.currentElement = ((RecoveredAnnotation)this.currentElement).addAnnotation(markerAnnotation, oldIndex);
      }
   }

   protected void consumeMemberValueArrayInitializer() {
      this.arrayInitializer(this.expressionLengthStack[this.expressionLengthPtr--]);
   }

   protected void consumeMemberValueAsName() {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
   }

   protected void consumeMemberValuePair() {
      char[] simpleName = this.identifierStack[this.identifierPtr];
      long position = this.identifierPositionStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int end = (int)position;
      int start = (int)(position >>> 32);
      Expression value = this.expressionStack[this.expressionPtr--];
      --this.expressionLengthPtr;
      MemberValuePair memberValuePair = new MemberValuePair(simpleName, start, end, value);
      this.pushOnAstStack(memberValuePair);
      if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
         RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation)this.currentElement;
         recoveredAnnotation.setKind(1);
      }
   }

   protected void consumeMemberValuePairs() {
      this.concatNodeLists();
   }

   protected void consumeMemberValues() {
      this.concatExpressionLists();
   }

   protected void consumeMethodBody() {
      this.nestedMethod[this.nestedType]--;
   }

   protected void consumeMethodDeclaration(boolean isNotAbstract, boolean isDefaultMethod) {
      if (isNotAbstract) {
         --this.intPtr;
         --this.intPtr;
      }

      int explicitDeclarations = 0;
      Statement[] statements = null;
      if (isNotAbstract) {
         explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
         int length;
         if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            if (this.options.ignoreMethodBodies) {
               this.astPtr -= length;
            } else {
               System.arraycopy(this.astStack, (this.astPtr -= length) + 1, statements = new Statement[length], 0, length);
            }
         }
      }

      MethodDeclaration md = (MethodDeclaration)this.astStack[this.astPtr];
      md.statements = statements;
      md.explicitDeclarations = explicitDeclarations;
      if (!isNotAbstract) {
         md.modifiers |= 16777216;
      } else if ((!this.diet || this.dietInt != 0) && statements == null && !this.containsComment(md.bodyStart, this.endPosition)) {
         md.bits |= 8;
      }

      md.bodyEnd = this.endPosition;
      md.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      if (isDefaultMethod && !this.tolerateDefaultClassMethods) {
         if (this.options.sourceLevel >= 3407872L) {
            this.problemReporter().defaultModifierIllegallySpecified(md.sourceStart, md.sourceEnd);
         } else {
            this.problemReporter().illegalModifierForMethod(md);
         }
      }
   }

   protected void consumeMethodHeader() {
      AbstractMethodDeclaration method = (AbstractMethodDeclaration)this.astStack[this.astPtr];
      if (this.currentToken == 49) {
         method.bodyStart = this.scanner.currentPosition;
      }

      if (this.currentElement != null) {
         if (this.currentToken == 28) {
            method.modifiers |= 16777216;
            method.declarationSourceEnd = this.scanner.currentPosition - 1;
            method.bodyEnd = this.scanner.currentPosition - 1;
            if (this.currentElement.parseTree() == method && this.currentElement.parent != null) {
               this.currentElement = this.currentElement.parent;
            }
         } else if (this.currentToken == 49
            && this.currentElement instanceof RecoveredMethod
            && ((RecoveredMethod)this.currentElement).methodDeclaration != method) {
            this.ignoreNextOpeningBrace = true;
            ++this.currentElement.bracketBalance;
         }

         this.restartRecovery = true;
      }
   }

   protected void consumeMethodHeaderDefaultValue() {
      MethodDeclaration md = (MethodDeclaration)this.astStack[this.astPtr];
      int length = this.expressionLengthStack[this.expressionLengthPtr--];
      if (length == 1) {
         --this.intPtr;
         --this.intPtr;
         if (md.isAnnotationMethod()) {
            ((AnnotationMethodDeclaration)md).defaultValue = this.expressionStack[this.expressionPtr];
            md.modifiers |= 131072;
         }

         --this.expressionPtr;
         this.recordStringLiterals = true;
      }

      if (this.currentElement != null && md.isAnnotationMethod()) {
         this.currentElement.updateSourceEndIfNecessary(((AnnotationMethodDeclaration)md).defaultValue.sourceEnd);
      }
   }

   protected void consumeMethodHeaderExtendedDims() {
      MethodDeclaration md = (MethodDeclaration)this.astStack[this.astPtr];
      int extendedDimensions = this.intStack[this.intPtr--];
      if (md.isAnnotationMethod()) {
         ((AnnotationMethodDeclaration)md).extendedDimensions = extendedDimensions;
      }

      if (extendedDimensions != 0) {
         md.sourceEnd = this.endPosition;
         md.returnType = this.augmentTypeWithAdditionalDimensions(
            md.returnType, extendedDimensions, this.getAnnotationsOnDimensions(extendedDimensions), false
         );
         md.bits |= md.returnType.bits & 1048576;
         if (this.currentToken == 49) {
            md.bodyStart = this.endPosition + 1;
         }

         if (this.currentElement != null) {
            this.lastCheckPoint = md.bodyStart;
         }
      }
   }

   protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
      MethodDeclaration md = null;
      if (isAnnotationMethod) {
         md = new AnnotationMethodDeclaration(this.compilationUnit.compilationResult);
         this.recordStringLiterals = false;
      } else {
         md = new MethodDeclaration(this.compilationUnit.compilationResult);
      }

      md.selector = this.identifierStack[this.identifierPtr];
      long selectorSource = this.identifierPositionStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      md.returnType = this.getTypeReference(this.intStack[this.intPtr--]);
      md.bits |= md.returnType.bits & 1048576;
      md.declarationSourceStart = this.intStack[this.intPtr--];
      md.modifiers = this.intStack[this.intPtr--];
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, md.annotations = new Annotation[length], 0, length);
      }

      md.javadoc = this.javadoc;
      this.javadoc = null;
      md.sourceStart = (int)(selectorSource >>> 32);
      this.pushOnAstStack(md);
      md.sourceEnd = this.lParenPos;
      md.bodyStart = this.lParenPos + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         if (!(this.currentElement instanceof RecoveredType)
            && Util.getLineNumber(md.returnType.sourceStart, this.scanner.lineEnds, 0, this.scanner.linePtr)
               != Util.getLineNumber(md.sourceStart, this.scanner.lineEnds, 0, this.scanner.linePtr)) {
            this.lastCheckPoint = md.sourceStart;
            this.restartRecovery = true;
         } else {
            this.lastCheckPoint = md.bodyStart;
            this.currentElement = this.currentElement.add(md, 0);
            this.lastIgnoredToken = -1;
         }
      }
   }

   protected void consumeMethodHeaderNameWithTypeParameters(boolean isAnnotationMethod) {
      MethodDeclaration md = null;
      if (isAnnotationMethod) {
         md = new AnnotationMethodDeclaration(this.compilationUnit.compilationResult);
         this.recordStringLiterals = false;
      } else {
         md = new MethodDeclaration(this.compilationUnit.compilationResult);
      }

      md.selector = this.identifierStack[this.identifierPtr];
      long selectorSource = this.identifierPositionStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      TypeReference returnType = this.getTypeReference(this.intStack[this.intPtr--]);
      if (isAnnotationMethod) {
         this.rejectIllegalLeadingTypeAnnotations(returnType);
      }

      md.returnType = returnType;
      md.bits |= returnType.bits & 1048576;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, md.typeParameters = new TypeParameter[length], 0, length);
      md.declarationSourceStart = this.intStack[this.intPtr--];
      md.modifiers = this.intStack[this.intPtr--];
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, md.annotations = new Annotation[length], 0, length);
      }

      md.javadoc = this.javadoc;
      this.javadoc = null;
      md.sourceStart = (int)(selectorSource >>> 32);
      this.pushOnAstStack(md);
      md.sourceEnd = this.lParenPos;
      md.bodyStart = this.lParenPos + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         boolean isType;
         if (!(isType = this.currentElement instanceof RecoveredType)
            && Util.getLineNumber(md.returnType.sourceStart, this.scanner.lineEnds, 0, this.scanner.linePtr)
               != Util.getLineNumber(md.sourceStart, this.scanner.lineEnds, 0, this.scanner.linePtr)) {
            this.lastCheckPoint = md.sourceStart;
            this.restartRecovery = true;
         } else {
            if (isType) {
               ((RecoveredType)this.currentElement).pendingTypeParameters = null;
            }

            this.lastCheckPoint = md.bodyStart;
            this.currentElement = this.currentElement.add(md, 0);
            this.lastIgnoredToken = -1;
         }
      }
   }

   protected void consumeMethodHeaderRightParen() {
      int length = this.astLengthStack[this.astLengthPtr--];
      this.astPtr -= length;
      AbstractMethodDeclaration md = (AbstractMethodDeclaration)this.astStack[this.astPtr];
      md.sourceEnd = this.rParenPos;
      if (length != 0) {
         Argument arg = (Argument)this.astStack[this.astPtr + 1];
         if (arg.isReceiver()) {
            md.receiver = (Receiver)arg;
            if (length > 1) {
               System.arraycopy(this.astStack, this.astPtr + 2, md.arguments = new Argument[length - 1], 0, length - 1);
            }

            Annotation[] annotations = arg.annotations;
            if (annotations != null && annotations.length > 0) {
               TypeReference type = arg.type;
               if (type.annotations == null) {
                  type.bits |= 1048576;
                  type.annotations = new Annotation[type.getAnnotatableLevels()][];
                  md.bits |= 1048576;
               }

               type.annotations[0] = annotations;
               int annotationSourceStart = annotations[0].sourceStart;
               if (type.sourceStart > annotationSourceStart) {
                  type.sourceStart = annotationSourceStart;
               }

               arg.annotations = null;
            }

            md.bits |= arg.type.bits & 1048576;
         } else {
            System.arraycopy(this.astStack, this.astPtr + 1, md.arguments = new Argument[length], 0, length);
            int i = 0;

            for(int max = md.arguments.length; i < max; ++i) {
               if ((md.arguments[i].bits & 1048576) != 0) {
                  md.bits |= 1048576;
                  break;
               }
            }
         }
      }

      md.bodyStart = this.rParenPos + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = md.bodyStart;
         if (this.currentElement.parseTree() == md) {
            return;
         }

         if (md.isConstructor() && (length != 0 || this.currentToken == 49 || this.currentToken == 112)) {
            this.currentElement = this.currentElement.add(md, 0);
            this.lastIgnoredToken = -1;
         }
      }
   }

   protected void consumeMethodHeaderThrowsClause() {
      int length = this.astLengthStack[this.astLengthPtr--];
      this.astPtr -= length;
      AbstractMethodDeclaration md = (AbstractMethodDeclaration)this.astStack[this.astPtr];
      System.arraycopy(this.astStack, this.astPtr + 1, md.thrownExceptions = new TypeReference[length], 0, length);
      md.sourceEnd = md.thrownExceptions[length - 1].sourceEnd;
      md.bodyStart = md.thrownExceptions[length - 1].sourceEnd + 1;
      this.listLength = 0;
      if (this.currentElement != null) {
         this.lastCheckPoint = md.bodyStart;
      }
   }

   protected void consumeInvocationExpression() {
   }

   protected void consumeMethodInvocationName() {
      MessageSend m = this.newMessageSend();
      m.sourceEnd = this.rParenPos;
      m.sourceStart = (int)((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr]) >>> 32);
      m.selector = this.identifierStack[this.identifierPtr--];
      if (this.identifierLengthStack[this.identifierLengthPtr] == 1) {
         m.receiver = ThisReference.implicitThis();
         --this.identifierLengthPtr;
      } else {
         this.identifierLengthStack[this.identifierLengthPtr]--;
         m.receiver = this.getUnspecifiedReference();
         m.sourceStart = m.receiver.sourceStart;
      }

      int length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--];
      if (length != 0) {
         Annotation[] typeAnnotations;
         System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, typeAnnotations = new Annotation[length], 0, length);
         this.problemReporter().misplacedTypeAnnotations(typeAnnotations[0], typeAnnotations[typeAnnotations.length - 1]);
      }

      this.pushOnExpressionStack(m);
      this.consumeInvocationExpression();
   }

   protected void consumeMethodInvocationNameWithTypeArguments() {
      MessageSend m = this.newMessageSendWithTypeArguments();
      m.sourceEnd = this.rParenPos;
      m.sourceStart = (int)((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr]) >>> 32);
      m.selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);
      --this.intPtr;
      m.receiver = this.getUnspecifiedReference();
      m.sourceStart = m.receiver.sourceStart;
      this.pushOnExpressionStack(m);
      this.consumeInvocationExpression();
   }

   protected void consumeMethodInvocationPrimary() {
      MessageSend m = this.newMessageSend();
      m.sourceStart = (int)((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr]) >>> 32);
      m.selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      m.receiver = this.expressionStack[this.expressionPtr];
      m.sourceStart = m.receiver.sourceStart;
      m.sourceEnd = this.rParenPos;
      this.expressionStack[this.expressionPtr] = m;
      this.consumeInvocationExpression();
   }

   protected void consumeMethodInvocationPrimaryWithTypeArguments() {
      MessageSend m = this.newMessageSendWithTypeArguments();
      m.sourceStart = (int)((m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr]) >>> 32);
      m.selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);
      --this.intPtr;
      m.receiver = this.expressionStack[this.expressionPtr];
      m.sourceStart = m.receiver.sourceStart;
      m.sourceEnd = this.rParenPos;
      this.expressionStack[this.expressionPtr] = m;
      this.consumeInvocationExpression();
   }

   protected void consumeMethodInvocationSuper() {
      MessageSend m = this.newMessageSend();
      m.sourceStart = this.intStack[this.intPtr--];
      m.sourceEnd = this.rParenPos;
      m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr];
      m.selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      m.receiver = new SuperReference(m.sourceStart, this.endPosition);
      this.pushOnExpressionStack(m);
      this.consumeInvocationExpression();
   }

   protected void consumeMethodInvocationSuperWithTypeArguments() {
      MessageSend m = this.newMessageSendWithTypeArguments();
      --this.intPtr;
      m.sourceEnd = this.rParenPos;
      m.nameSourcePosition = this.identifierPositionStack[this.identifierPtr];
      m.selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);
      m.sourceStart = this.intStack[this.intPtr--];
      m.receiver = new SuperReference(m.sourceStart, this.endPosition);
      this.pushOnExpressionStack(m);
      this.consumeInvocationExpression();
   }

   protected void consumeModifiers() {
      int savedModifiersSourceStart = this.modifiersSourceStart;
      this.checkComment();
      this.pushOnIntStack(this.modifiers);
      if (this.modifiersSourceStart >= savedModifiersSourceStart) {
         this.modifiersSourceStart = savedModifiersSourceStart;
      }

      this.pushOnIntStack(this.modifiersSourceStart);
      this.resetModifiers();
   }

   protected void consumeModifiers2() {
      this.expressionLengthStack[this.expressionLengthPtr - 1] += this.expressionLengthStack[this.expressionLengthPtr--];
   }

   protected void consumeMultipleResources() {
      this.concatNodeLists();
   }

   protected void consumeTypeAnnotation() {
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3407872L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         Annotation annotation = this.typeAnnotationStack[this.typeAnnotationPtr];
         this.problemReporter().invalidUsageOfTypeAnnotations(annotation);
      }

      this.dimensions = this.intStack[this.intPtr--];
   }

   protected void consumeOneMoreTypeAnnotation() {
      this.typeAnnotationLengthStack[--this.typeAnnotationLengthPtr]++;
   }

   protected void consumeNameArrayType() {
      this.pushOnGenericsLengthStack(0);
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
   }

   protected void consumeNestedMethod() {
      this.jumpOverMethodBody();
      this.nestedMethod[this.nestedType]++;
      this.pushOnIntStack(this.scanner.currentPosition);
      this.consumeOpenBlock();
   }

   protected void consumeNestedType() {
      int length = this.nestedMethod.length;
      if (++this.nestedType >= length) {
         System.arraycopy(this.nestedMethod, 0, this.nestedMethod = new int[length + 30], 0, length);
         System.arraycopy(this.variablesCounter, 0, this.variablesCounter = new int[length + 30], 0, length);
      }

      this.nestedMethod[this.nestedType] = 0;
      this.variablesCounter[this.nestedType] = 0;
   }

   protected void consumeNormalAnnotation(boolean isTypeAnnotation) {
      NormalAnnotation normalAnnotation = null;
      int oldIndex = this.identifierPtr;
      TypeReference typeReference = this.getAnnotationType();
      normalAnnotation = new NormalAnnotation(typeReference, this.intStack[this.intPtr--]);
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         System.arraycopy(this.astStack, (this.astPtr -= length) + 1, normalAnnotation.memberValuePairs = new MemberValuePair[length], 0, length);
      }

      normalAnnotation.declarationSourceEnd = this.rParenPos;
      if (isTypeAnnotation) {
         this.pushOnTypeAnnotationStack(normalAnnotation);
      } else {
         this.pushOnExpressionStack(normalAnnotation);
      }

      if (this.currentElement != null) {
         this.annotationRecoveryCheckPoint(normalAnnotation.sourceStart, normalAnnotation.declarationSourceEnd);
         if (this.currentElement instanceof RecoveredAnnotation) {
            this.currentElement = ((RecoveredAnnotation)this.currentElement).addAnnotation(normalAnnotation, oldIndex);
         }
      }

      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfAnnotation(normalAnnotation);
      }

      this.recordStringLiterals = true;
   }

   protected void consumeOneDimLoop(boolean isAnnotated) {
      ++this.dimensions;
      if (!isAnnotated) {
         this.pushOnTypeAnnotationLengthStack(0);
      }
   }

   protected void consumeOnlySynchronized() {
      this.pushOnIntStack(this.synchronizedBlockSourceStart);
      this.resetModifiers();
      --this.expressionLengthPtr;
   }

   protected void consumeOnlyTypeArguments() {
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         int length = this.genericsLengthStack[this.genericsLengthPtr];
         this.problemReporter()
            .invalidUsageOfTypeArguments((TypeReference)this.genericsStack[this.genericsPtr - length + 1], (TypeReference)this.genericsStack[this.genericsPtr]);
      }
   }

   protected void consumeOnlyTypeArgumentsForCastExpression() {
   }

   protected void consumeOpenBlock() {
      this.pushOnIntStack(this.scanner.startPosition);
      int stackLength = this.realBlockStack.length;
      if (++this.realBlockPtr >= stackLength) {
         System.arraycopy(this.realBlockStack, 0, this.realBlockStack = new int[stackLength + 255], 0, stackLength);
      }

      this.realBlockStack[this.realBlockPtr] = 0;
   }

   protected void consumePackageComment() {
      if (this.options.sourceLevel >= 3211264L) {
         this.checkComment();
         this.resetModifiers();
      }
   }

   protected void consumePackageDeclaration() {
      ImportReference impt = this.compilationUnit.currentPackage;
      this.compilationUnit.javadoc = this.javadoc;
      this.javadoc = null;
      impt.declarationEnd = this.endStatementPosition;
      impt.declarationSourceEnd = this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd);
   }

   protected void consumePackageDeclarationName() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, ++this.identifierPtr, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr--, positions, 0, length);
      ImportReference impt = new ImportReference(tokens, positions, false, 0);
      this.compilationUnit.currentPackage = impt;
      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      impt.declarationSourceStart = this.intStack[this.intPtr--];
      if (this.javadoc != null) {
         impt.declarationSourceStart = this.javadoc.sourceStart;
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.restartRecovery = true;
      }
   }

   protected void consumePackageDeclarationNameWithModifiers() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, ++this.identifierPtr, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr--, positions, 0, length);
      int packageModifiersSourceStart = this.intStack[this.intPtr--];
      int packageModifiers = this.intStack[this.intPtr--];
      ImportReference impt = new ImportReference(tokens, positions, false, packageModifiers);
      this.compilationUnit.currentPackage = impt;
      int packageModifiersSourceEnd;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != false) {
         System.arraycopy(this.expressionStack, (this.expressionPtr -= length) + 1, impt.annotations = new Annotation[length], 0, length);
         impt.declarationSourceStart = packageModifiersSourceStart;
         packageModifiersSourceEnd = this.intStack[this.intPtr--] - 2;
      } else {
         impt.declarationSourceStart = this.intStack[this.intPtr--];
         packageModifiersSourceEnd = impt.declarationSourceStart - 2;
         if (this.javadoc != null) {
            impt.declarationSourceStart = this.javadoc.sourceStart;
         }
      }

      if (packageModifiers != 0) {
         this.problemReporter().illegalModifiers(packageModifiersSourceStart, packageModifiersSourceEnd);
      }

      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.restartRecovery = true;
      }
   }

   protected void consumePostfixExpression() {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
   }

   protected void consumePrimaryNoNewArray() {
      Expression parenthesizedExpression = this.expressionStack[this.expressionPtr];
      this.updateSourcePosition(parenthesizedExpression);
      int numberOfParenthesis = (parenthesizedExpression.bits & 534773760) >> 21;
      parenthesizedExpression.bits &= -534773761;
      parenthesizedExpression.bits |= numberOfParenthesis + 1 << 21;
   }

   protected void consumePrimaryNoNewArrayArrayType() {
      --this.intPtr;
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnGenericsLengthStack(0);
      ClassLiteralAccess cla;
      this.pushOnExpressionStack(cla = new ClassLiteralAccess(this.intStack[this.intPtr--], this.getTypeReference(this.intStack[this.intPtr--])));
      this.rejectIllegalTypeAnnotations(cla.type);
   }

   protected void consumePrimaryNoNewArrayName() {
      --this.intPtr;
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnGenericsLengthStack(0);
      TypeReference typeReference = this.getTypeReference(0);
      this.rejectIllegalTypeAnnotations(typeReference);
      this.pushOnExpressionStack(new ClassLiteralAccess(this.intStack[this.intPtr--], typeReference));
   }

   protected void rejectIllegalLeadingTypeAnnotations(TypeReference typeReference) {
      Annotation[][] annotations = typeReference.annotations;
      if (annotations != null && annotations[0] != null) {
         this.problemReporter().misplacedTypeAnnotations(annotations[0][0], annotations[0][annotations[0].length - 1]);
         annotations[0] = null;
      }
   }

   private void rejectIllegalTypeAnnotations(TypeReference typeReference) {
      this.rejectIllegalTypeAnnotations(typeReference, false);
   }

   private void rejectIllegalTypeAnnotations(TypeReference typeReference, boolean tolerateAnnotationsOnDimensions) {
      Annotation[][] annotations = typeReference.annotations;
      int i = 0;

      for(int length = annotations == null ? 0 : annotations.length; i < length; ++i) {
         Annotation[] misplacedAnnotations = annotations[i];
         if (misplacedAnnotations != null) {
            this.problemReporter().misplacedTypeAnnotations(misplacedAnnotations[0], misplacedAnnotations[misplacedAnnotations.length - 1]);
         }
      }

      annotations = typeReference.getAnnotationsOnDimensions(true);
      boolean tolerated = false;
      int ix = 0;

      for(int length = annotations == null ? 0 : annotations.length; ix < length; ++ix) {
         Annotation[] misplacedAnnotations = annotations[ix];
         if (misplacedAnnotations != null) {
            if (tolerateAnnotationsOnDimensions) {
               this.problemReporter().toleratedMisplacedTypeAnnotations(misplacedAnnotations[0], misplacedAnnotations[misplacedAnnotations.length - 1]);
               tolerated = true;
            } else {
               this.problemReporter().misplacedTypeAnnotations(misplacedAnnotations[0], misplacedAnnotations[misplacedAnnotations.length - 1]);
            }
         }
      }

      if (!tolerated) {
         typeReference.annotations = null;
         typeReference.setAnnotationsOnDimensions(null);
         typeReference.bits &= -1048577;
      }
   }

   protected void consumeQualifiedSuperReceiver() {
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnGenericsLengthStack(0);
      TypeReference typeReference = this.getTypeReference(0);
      this.rejectIllegalTypeAnnotations(typeReference);
      this.pushOnExpressionStack(new QualifiedSuperReference(typeReference, this.intStack[this.intPtr--], this.endPosition));
   }

   protected void consumePrimaryNoNewArrayNameThis() {
      this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
      this.pushOnGenericsLengthStack(0);
      TypeReference typeReference = this.getTypeReference(0);
      this.rejectIllegalTypeAnnotations(typeReference);
      this.pushOnExpressionStack(new QualifiedThisReference(typeReference, this.intStack[this.intPtr--], this.endPosition));
   }

   protected void consumePrimaryNoNewArrayPrimitiveArrayType() {
      --this.intPtr;
      ClassLiteralAccess cla;
      this.pushOnExpressionStack(cla = new ClassLiteralAccess(this.intStack[this.intPtr--], this.getTypeReference(this.intStack[this.intPtr--])));
      this.rejectIllegalTypeAnnotations(cla.type, true);
   }

   protected void consumePrimaryNoNewArrayPrimitiveType() {
      --this.intPtr;
      ClassLiteralAccess cla;
      this.pushOnExpressionStack(cla = new ClassLiteralAccess(this.intStack[this.intPtr--], this.getTypeReference(0)));
      this.rejectIllegalTypeAnnotations(cla.type);
   }

   protected void consumePrimaryNoNewArrayThis() {
      this.pushOnExpressionStack(new ThisReference(this.intStack[this.intPtr--], this.endPosition));
   }

   protected void consumePrimaryNoNewArrayWithName() {
      this.pushOnExpressionStack(this.getUnspecifiedReferenceOptimized());
      Expression parenthesizedExpression = this.expressionStack[this.expressionPtr];
      this.updateSourcePosition(parenthesizedExpression);
      int numberOfParenthesis = (parenthesizedExpression.bits & 534773760) >> 21;
      parenthesizedExpression.bits &= -534773761;
      parenthesizedExpression.bits |= numberOfParenthesis + 1 << 21;
   }

   protected void consumePrimitiveArrayType() {
   }

   protected void consumePrimitiveType() {
      this.pushOnIntStack(0);
   }

   protected void consumePushLeftBrace() {
      this.pushOnIntStack(this.endPosition);
   }

   protected void consumePushModifiers() {
      this.pushOnIntStack(this.modifiers);
      this.pushOnIntStack(this.modifiersSourceStart);
      this.resetModifiers();
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumePushCombineModifiers() {
      --this.intPtr;
      int newModifiers = this.intStack[this.intPtr--] | 65536;
      this.intPtr -= 2;
      if ((this.intStack[this.intPtr - 1] & newModifiers) != 0) {
         newModifiers |= 4194304;
      }

      this.intStack[this.intPtr - 1] |= newModifiers;
      this.expressionLengthStack[this.expressionLengthPtr - 1] += this.expressionLengthStack[this.expressionLengthPtr--];
      if (this.currentElement != null) {
         this.currentElement.addModifier(newModifiers, this.intStack[this.intPtr]);
      }
   }

   protected void consumePushModifiersForHeader() {
      this.checkComment();
      this.pushOnIntStack(this.modifiers);
      this.pushOnIntStack(this.modifiersSourceStart);
      this.resetModifiers();
      this.pushOnExpressionStackLengthStack(0);
   }

   protected void consumePushPosition() {
      this.pushOnIntStack(this.endPosition);
   }

   protected void consumePushRealModifiers() {
      this.checkComment();
      this.pushOnIntStack(this.modifiers);
      this.pushOnIntStack(this.modifiersSourceStart);
      this.resetModifiers();
   }

   protected void consumeQualifiedName(boolean qualifiedNameIsAnnotated) {
      this.identifierLengthStack[--this.identifierLengthPtr]++;
      if (!qualifiedNameIsAnnotated) {
         this.pushOnTypeAnnotationLengthStack(0);
      }
   }

   protected void consumeUnannotatableQualifiedName() {
      this.identifierLengthStack[--this.identifierLengthPtr]++;
   }

   protected void consumeRecoveryMethodHeaderName() {
      boolean isAnnotationMethod = false;
      if (this.currentElement instanceof RecoveredType) {
         isAnnotationMethod = (((RecoveredType)this.currentElement).typeDeclaration.modifiers & 8192) != 0;
      } else {
         RecoveredType recoveredType = this.currentElement.enclosingType();
         if (recoveredType != null) {
            isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
         }
      }

      this.consumeMethodHeaderName(isAnnotationMethod);
   }

   protected void consumeRecoveryMethodHeaderNameWithTypeParameters() {
      boolean isAnnotationMethod = false;
      if (this.currentElement instanceof RecoveredType) {
         isAnnotationMethod = (((RecoveredType)this.currentElement).typeDeclaration.modifiers & 8192) != 0;
      } else {
         RecoveredType recoveredType = this.currentElement.enclosingType();
         if (recoveredType != null) {
            isAnnotationMethod = (recoveredType.typeDeclaration.modifiers & 8192) != 0;
         }
      }

      this.consumeMethodHeaderNameWithTypeParameters(isAnnotationMethod);
   }

   protected void consumeReduceImports() {
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.astPtr -= length;
         System.arraycopy(this.astStack, this.astPtr + 1, this.compilationUnit.imports = new ImportReference[length], 0, length);
      }
   }

   protected void consumeReferenceType() {
      this.pushOnIntStack(0);
   }

   protected void consumeReferenceType1() {
      this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeReferenceType2() {
      this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeReferenceType3() {
      this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeResourceAsLocalVariableDeclaration() {
      this.consumeLocalVariableDeclaration();
   }

   protected void consumeResourceSpecification() {
   }

   protected void consumeResourceOptionalTrailingSemiColon(boolean punctuated) {
      LocalDeclaration localDeclaration = (LocalDeclaration)this.astStack[this.astPtr];
      if (punctuated) {
         localDeclaration.declarationSourceEnd = this.endStatementPosition;
      }
   }

   protected void consumeRestoreDiet() {
      --this.dietInt;
   }

   protected void consumeRightParen() {
      this.pushOnIntStack(this.rParenPos);
   }

   protected void consumeNonTypeUseName() {
      for(int i = this.identifierLengthStack[this.identifierLengthPtr]; i > 0 && this.typeAnnotationLengthPtr >= 0; --i) {
         int length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--];
         if (length != 0) {
            Annotation[] typeAnnotations;
            System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, typeAnnotations = new Annotation[length], 0, length);
            this.problemReporter().misplacedTypeAnnotations(typeAnnotations[0], typeAnnotations[typeAnnotations.length - 1]);
         }
      }
   }

   protected void consumeZeroTypeAnnotations() {
      this.pushOnTypeAnnotationLengthStack(0);
   }

   protected void consumeRule(int act) {
      switch(act) {
         case 35:
            this.consumePrimitiveType();
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 50:
         case 51:
         case 52:
         case 62:
         case 64:
         case 65:
         case 66:
         case 67:
         case 72:
         case 73:
         case 74:
         case 99:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 115:
         case 117:
         case 123:
         case 124:
         case 125:
         case 126:
         case 131:
         case 132:
         case 134:
         case 135:
         case 136:
         case 138:
         case 139:
         case 140:
         case 141:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 153:
         case 157:
         case 160:
         case 161:
         case 163:
         case 164:
         case 165:
         case 170:
         case 171:
         case 172:
         case 173:
         case 174:
         case 175:
         case 177:
         case 178:
         case 180:
         case 182:
         case 183:
         case 189:
         case 190:
         case 191:
         case 192:
         case 209:
         case 218:
         case 242:
         case 245:
         case 246:
         case 249:
         case 255:
         case 256:
         case 257:
         case 258:
         case 259:
         case 260:
         case 261:
         case 262:
         case 263:
         case 264:
         case 269:
         case 275:
         case 276:
         case 277:
         case 278:
         case 279:
         case 280:
         case 290:
         case 291:
         case 292:
         case 293:
         case 294:
         case 295:
         case 296:
         case 297:
         case 298:
         case 299:
         case 300:
         case 301:
         case 302:
         case 303:
         case 304:
         case 305:
         case 306:
         case 307:
         case 308:
         case 309:
         case 310:
         case 311:
         case 312:
         case 313:
         case 314:
         case 315:
         case 321:
         case 322:
         case 323:
         case 324:
         case 325:
         case 326:
         case 327:
         case 328:
         case 334:
         case 335:
         case 337:
         case 340:
         case 350:
         case 351:
         case 352:
         case 376:
         case 378:
         case 381:
         case 384:
         case 385:
         case 386:
         case 387:
         case 391:
         case 392:
         case 399:
         case 400:
         case 401:
         case 402:
         case 403:
         case 413:
         case 419:
         case 420:
         case 421:
         case 422:
         case 423:
         case 426:
         case 427:
         case 439:
         case 442:
         case 444:
         case 452:
         case 454:
         case 457:
         case 458:
         case 475:
         case 477:
         case 478:
         case 482:
         case 483:
         case 486:
         case 489:
         case 492:
         case 500:
         case 501:
         case 502:
         case 508:
         case 512:
         case 515:
         case 519:
         case 524:
         case 526:
         case 529:
         case 531:
         case 533:
         case 535:
         case 537:
         case 539:
         case 541:
         case 542:
         case 544:
         case 559:
         case 560:
         case 562:
         case 563:
         case 564:
         case 565:
         case 571:
         case 573:
         case 575:
         case 576:
         case 577:
         case 579:
         case 580:
         case 581:
         case 586:
         case 588:
         case 589:
         case 590:
         case 592:
         case 601:
         case 609:
         case 612:
         case 624:
         case 626:
         case 629:
         case 630:
         case 631:
         case 634:
         case 636:
         case 637:
         case 640:
         case 642:
         case 643:
         case 663:
         case 665:
         case 668:
         case 671:
         case 676:
         case 679:
         case 680:
         case 681:
         case 682:
         case 683:
         case 686:
         case 687:
         case 690:
         case 691:
         case 698:
         case 703:
         case 710:
         case 719:
         case 722:
         case 727:
         case 730:
         case 733:
         case 736:
         case 739:
         case 742:
         case 745:
         case 746:
         case 747:
         case 754:
         case 757:
         case 765:
         case 766:
         case 767:
         case 768:
         case 769:
         case 770:
         case 771:
         case 775:
         case 776:
         case 781:
         case 783:
         case 784:
         case 790:
         default:
            break;
         case 49:
            this.consumeReferenceType();
            break;
         case 53:
            this.consumeClassOrInterfaceName();
            break;
         case 54:
            this.consumeClassOrInterface();
            break;
         case 55:
            this.consumeGenericType();
            break;
         case 56:
            this.consumeGenericTypeWithDiamond();
            break;
         case 57:
            this.consumeArrayTypeWithTypeArgumentsName();
            break;
         case 58:
            this.consumePrimitiveArrayType();
            break;
         case 59:
            this.consumeNameArrayType();
            break;
         case 60:
            this.consumeGenericTypeNameArrayType();
            break;
         case 61:
            this.consumeGenericTypeArrayType();
            break;
         case 63:
            this.consumeZeroTypeAnnotations();
            break;
         case 68:
            this.consumeUnannotatableQualifiedName();
            break;
         case 69:
            this.consumeQualifiedName(false);
            break;
         case 70:
            this.consumeQualifiedName(true);
            break;
         case 71:
            this.consumeZeroTypeAnnotations();
            break;
         case 75:
            this.consumeOneMoreTypeAnnotation();
            break;
         case 76:
            this.consumeTypeAnnotation();
            break;
         case 77:
            this.consumeTypeAnnotation();
            break;
         case 78:
            this.consumeTypeAnnotation();
            break;
         case 79:
            this.consumeAnnotationName();
            break;
         case 80:
            this.consumeNormalAnnotation(true);
            break;
         case 81:
            this.consumeMarkerAnnotation(true);
            break;
         case 82:
            this.consumeSingleMemberAnnotation(true);
            break;
         case 83:
            this.consumeNonTypeUseName();
            break;
         case 84:
            this.consumeZeroTypeAnnotations();
            break;
         case 85:
            this.consumeExplicitThisParameter(false);
            break;
         case 86:
            this.consumeExplicitThisParameter(true);
            break;
         case 87:
            this.consumeVariableDeclaratorIdParameter();
            break;
         case 88:
            this.consumeCompilationUnit();
            break;
         case 89:
            this.consumeInternalCompilationUnit();
            break;
         case 90:
            this.consumeInternalCompilationUnit();
            break;
         case 91:
            this.consumeInternalCompilationUnitWithTypes();
            break;
         case 92:
            this.consumeInternalCompilationUnitWithTypes();
            break;
         case 93:
            this.consumeInternalCompilationUnit();
            break;
         case 94:
            this.consumeInternalCompilationUnitWithTypes();
            break;
         case 95:
            this.consumeInternalCompilationUnitWithTypes();
            break;
         case 96:
            this.consumeEmptyInternalCompilationUnit();
            break;
         case 97:
            this.consumeReduceImports();
            break;
         case 98:
            this.consumeEnterCompilationUnit();
            break;
         case 114:
            this.consumeCatchHeader();
            break;
         case 116:
            this.consumeImportDeclarations();
            break;
         case 118:
            this.consumeTypeDeclarations();
            break;
         case 119:
            this.consumePackageDeclaration();
            break;
         case 120:
            this.consumePackageDeclarationNameWithModifiers();
            break;
         case 121:
            this.consumePackageDeclarationName();
            break;
         case 122:
            this.consumePackageComment();
            break;
         case 127:
            this.consumeImportDeclaration();
            break;
         case 128:
            this.consumeSingleTypeImportDeclarationName();
            break;
         case 129:
            this.consumeImportDeclaration();
            break;
         case 130:
            this.consumeTypeImportOnDemandDeclarationName();
            break;
         case 133:
            this.consumeEmptyTypeDeclaration();
            break;
         case 137:
            this.consumeModifiers2();
            break;
         case 149:
            this.consumeAnnotationAsModifier();
            break;
         case 150:
            this.consumeClassDeclaration();
            break;
         case 151:
            this.consumeClassHeader();
            break;
         case 152:
            this.consumeTypeHeaderNameWithTypeParameters();
            break;
         case 154:
            this.consumeClassHeaderName1();
            break;
         case 155:
            this.consumeClassHeaderExtends();
            break;
         case 156:
            this.consumeClassHeaderImplements();
            break;
         case 158:
            this.consumeInterfaceTypeList();
            break;
         case 159:
            this.consumeInterfaceType();
            break;
         case 162:
            this.consumeClassBodyDeclarations();
            break;
         case 166:
            this.consumeClassBodyDeclaration();
            break;
         case 167:
            this.consumeDiet();
            break;
         case 168:
            this.consumeClassBodyDeclaration();
            break;
         case 169:
            this.consumeCreateInitializer();
            break;
         case 176:
            this.consumeEmptyTypeDeclaration();
            break;
         case 179:
            this.consumeFieldDeclaration();
            break;
         case 181:
            this.consumeVariableDeclarators();
            break;
         case 184:
            this.consumeEnterVariable();
            break;
         case 185:
            this.consumeExitVariableWithInitialization();
            break;
         case 186:
            this.consumeExitVariableWithoutInitialization();
            break;
         case 187:
            this.consumeForceNoDiet();
            break;
         case 188:
            this.consumeRestoreDiet();
            break;
         case 193:
            this.consumeMethodDeclaration(true, false);
            break;
         case 194:
            this.consumeMethodDeclaration(true, true);
            break;
         case 195:
            this.consumeMethodDeclaration(false, false);
            break;
         case 196:
            this.consumeMethodHeader();
            break;
         case 197:
            this.consumeMethodHeader();
            break;
         case 198:
            this.consumeMethodHeaderNameWithTypeParameters(false);
            break;
         case 199:
            this.consumeMethodHeaderName(false);
            break;
         case 200:
            this.consumeMethodHeaderNameWithTypeParameters(false);
            break;
         case 201:
            this.consumeMethodHeaderName(false);
            break;
         case 202:
            this.consumePushCombineModifiers();
            break;
         case 203:
            this.consumeMethodHeaderRightParen();
            break;
         case 204:
            this.consumeMethodHeaderExtendedDims();
            break;
         case 205:
            this.consumeMethodHeaderThrowsClause();
            break;
         case 206:
            this.consumeConstructorHeader();
            break;
         case 207:
            this.consumeConstructorHeaderNameWithTypeParameters();
            break;
         case 208:
            this.consumeConstructorHeaderName();
            break;
         case 210:
            this.consumeFormalParameterList();
            break;
         case 211:
            this.consumeFormalParameter(false);
            break;
         case 212:
            this.consumeFormalParameter(true);
            break;
         case 213:
            this.consumeFormalParameter(true);
            break;
         case 214:
            this.consumeCatchFormalParameter();
            break;
         case 215:
            this.consumeCatchType();
            break;
         case 216:
            this.consumeUnionTypeAsClassType();
            break;
         case 217:
            this.consumeUnionType();
            break;
         case 219:
            this.consumeClassTypeList();
            break;
         case 220:
            this.consumeClassTypeElt();
            break;
         case 221:
            this.consumeMethodBody();
            break;
         case 222:
            this.consumeNestedMethod();
            break;
         case 223:
            this.consumeStaticInitializer();
            break;
         case 224:
            this.consumeStaticOnly();
            break;
         case 225:
            this.consumeConstructorDeclaration();
            break;
         case 226:
            this.consumeInvalidConstructorDeclaration();
            break;
         case 227:
            this.consumeExplicitConstructorInvocation(0, 3);
            break;
         case 228:
            this.consumeExplicitConstructorInvocationWithTypeArguments(0, 3);
            break;
         case 229:
            this.consumeExplicitConstructorInvocation(0, 2);
            break;
         case 230:
            this.consumeExplicitConstructorInvocationWithTypeArguments(0, 2);
            break;
         case 231:
            this.consumeExplicitConstructorInvocation(1, 2);
            break;
         case 232:
            this.consumeExplicitConstructorInvocationWithTypeArguments(1, 2);
            break;
         case 233:
            this.consumeExplicitConstructorInvocation(2, 2);
            break;
         case 234:
            this.consumeExplicitConstructorInvocationWithTypeArguments(2, 2);
            break;
         case 235:
            this.consumeExplicitConstructorInvocation(1, 3);
            break;
         case 236:
            this.consumeExplicitConstructorInvocationWithTypeArguments(1, 3);
            break;
         case 237:
            this.consumeExplicitConstructorInvocation(2, 3);
            break;
         case 238:
            this.consumeExplicitConstructorInvocationWithTypeArguments(2, 3);
            break;
         case 239:
            this.consumeInterfaceDeclaration();
            break;
         case 240:
            this.consumeInterfaceHeader();
            break;
         case 241:
            this.consumeTypeHeaderNameWithTypeParameters();
            break;
         case 243:
            this.consumeInterfaceHeaderName1();
            break;
         case 244:
            this.consumeInterfaceHeaderExtends();
            break;
         case 247:
            this.consumeInterfaceMemberDeclarations();
            break;
         case 248:
            this.consumeEmptyTypeDeclaration();
            break;
         case 250:
            this.consumeInterfaceMethodDeclaration(false);
            break;
         case 251:
            this.consumeInterfaceMethodDeclaration(false);
            break;
         case 252:
            this.consumeInterfaceMethodDeclaration(true);
            break;
         case 253:
            this.consumeInvalidConstructorDeclaration(true);
            break;
         case 254:
            this.consumeInvalidConstructorDeclaration(false);
            break;
         case 265:
            this.consumePushLeftBrace();
            break;
         case 266:
            this.consumeEmptyArrayInitializer();
            break;
         case 267:
            this.consumeArrayInitializer();
            break;
         case 268:
            this.consumeArrayInitializer();
            break;
         case 270:
            this.consumeVariableInitializers();
            break;
         case 271:
            this.consumeBlock();
            break;
         case 272:
            this.consumeOpenBlock();
            break;
         case 273:
            this.consumeBlockStatement();
            break;
         case 274:
            this.consumeBlockStatements();
            break;
         case 281:
            this.consumeInvalidInterfaceDeclaration();
            break;
         case 282:
            this.consumeInvalidAnnotationTypeDeclaration();
            break;
         case 283:
            this.consumeInvalidEnumDeclaration();
            break;
         case 284:
            this.consumeLocalVariableDeclarationStatement();
            break;
         case 285:
            this.consumeLocalVariableDeclaration();
            break;
         case 286:
            this.consumeLocalVariableDeclaration();
            break;
         case 287:
            this.consumePushModifiers();
            break;
         case 288:
            this.consumePushModifiersForHeader();
            break;
         case 289:
            this.consumePushRealModifiers();
            break;
         case 316:
            this.consumeEmptyStatement();
            break;
         case 317:
            this.consumeStatementLabel();
            break;
         case 318:
            this.consumeStatementLabel();
            break;
         case 319:
            this.consumeLabel();
            break;
         case 320:
            this.consumeExpressionStatement();
            break;
         case 329:
            this.consumeStatementIfNoElse();
            break;
         case 330:
            this.consumeStatementIfWithElse();
            break;
         case 331:
            this.consumeStatementIfWithElse();
            break;
         case 332:
            this.consumeStatementSwitch();
            break;
         case 333:
            this.consumeEmptySwitchBlock();
            break;
         case 336:
            this.consumeSwitchBlock();
            break;
         case 338:
            this.consumeSwitchBlockStatements();
            break;
         case 339:
            this.consumeSwitchBlockStatement();
            break;
         case 341:
            this.consumeSwitchLabels();
            break;
         case 342:
            this.consumeCaseLabel();
            break;
         case 343:
            this.consumeDefaultLabel();
            break;
         case 344:
            this.consumeStatementWhile();
            break;
         case 345:
            this.consumeStatementWhile();
            break;
         case 346:
            this.consumeStatementDo();
            break;
         case 347:
            this.consumeStatementFor();
            break;
         case 348:
            this.consumeStatementFor();
            break;
         case 349:
            this.consumeForInit();
            break;
         case 353:
            this.consumeStatementExpressionList();
            break;
         case 354:
            this.consumeSimpleAssertStatement();
            break;
         case 355:
            this.consumeAssertStatement();
            break;
         case 356:
            this.consumeStatementBreak();
            break;
         case 357:
            this.consumeStatementBreakWithLabel();
            break;
         case 358:
            this.consumeStatementContinue();
            break;
         case 359:
            this.consumeStatementContinueWithLabel();
            break;
         case 360:
            this.consumeStatementReturn();
            break;
         case 361:
            this.consumeStatementThrow();
            break;
         case 362:
            this.consumeStatementSynchronized();
            break;
         case 363:
            this.consumeOnlySynchronized();
            break;
         case 364:
            this.consumeStatementTry(false, false);
            break;
         case 365:
            this.consumeStatementTry(true, false);
            break;
         case 366:
            this.consumeStatementTry(false, true);
            break;
         case 367:
            this.consumeStatementTry(true, true);
            break;
         case 368:
            this.consumeResourceSpecification();
            break;
         case 369:
            this.consumeResourceOptionalTrailingSemiColon(false);
            break;
         case 370:
            this.consumeResourceOptionalTrailingSemiColon(true);
            break;
         case 371:
            this.consumeSingleResource();
            break;
         case 372:
            this.consumeMultipleResources();
            break;
         case 373:
            this.consumeResourceOptionalTrailingSemiColon(true);
            break;
         case 374:
            this.consumeResourceAsLocalVariableDeclaration();
            break;
         case 375:
            this.consumeResourceAsLocalVariableDeclaration();
            break;
         case 377:
            this.consumeExitTryBlock();
            break;
         case 379:
            this.consumeCatches();
            break;
         case 380:
            this.consumeStatementCatch();
            break;
         case 382:
            this.consumeLeftParen();
            break;
         case 383:
            this.consumeRightParen();
            break;
         case 388:
            this.consumePrimaryNoNewArrayThis();
            break;
         case 389:
            this.consumePrimaryNoNewArray();
            break;
         case 390:
            this.consumePrimaryNoNewArrayWithName();
            break;
         case 393:
            this.consumePrimaryNoNewArrayNameThis();
            break;
         case 394:
            this.consumeQualifiedSuperReceiver();
            break;
         case 395:
            this.consumePrimaryNoNewArrayName();
            break;
         case 396:
            this.consumePrimaryNoNewArrayArrayType();
            break;
         case 397:
            this.consumePrimaryNoNewArrayPrimitiveArrayType();
            break;
         case 398:
            this.consumePrimaryNoNewArrayPrimitiveType();
            break;
         case 404:
            this.consumeReferenceExpressionTypeArgumentsAndTrunk(false);
            break;
         case 405:
            this.consumeReferenceExpressionTypeArgumentsAndTrunk(true);
            break;
         case 406:
            this.consumeReferenceExpressionTypeForm(true);
            break;
         case 407:
            this.consumeReferenceExpressionTypeForm(false);
            break;
         case 408:
            this.consumeReferenceExpressionGenericTypeForm();
            break;
         case 409:
            this.consumeReferenceExpressionPrimaryForm();
            break;
         case 410:
            this.consumeReferenceExpressionPrimaryForm();
            break;
         case 411:
            this.consumeReferenceExpressionSuperForm();
            break;
         case 412:
            this.consumeEmptyTypeArguments();
            break;
         case 414:
            this.consumeIdentifierOrNew(false);
            break;
         case 415:
            this.consumeIdentifierOrNew(true);
            break;
         case 416:
            this.consumeLambdaExpression();
            break;
         case 417:
            this.consumeNestedLambda();
            break;
         case 418:
            this.consumeTypeElidedLambdaParameter(false);
            break;
         case 424:
            this.consumeFormalParameterList();
            break;
         case 425:
            this.consumeTypeElidedLambdaParameter(true);
            break;
         case 428:
            this.consumeElidedLeftBraceAndReturn();
            break;
         case 429:
            this.consumeAllocationHeader();
            break;
         case 430:
            this.consumeClassInstanceCreationExpressionWithTypeArguments();
            break;
         case 431:
            this.consumeClassInstanceCreationExpression();
            break;
         case 432:
            this.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
            break;
         case 433:
            this.consumeClassInstanceCreationExpressionQualified();
            break;
         case 434:
            this.consumeClassInstanceCreationExpressionQualified();
            break;
         case 435:
            this.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
            break;
         case 436:
            this.consumeEnterInstanceCreationArgumentList();
            break;
         case 437:
            this.consumeClassInstanceCreationExpressionName();
            break;
         case 438:
            this.consumeClassBodyopt();
            break;
         case 440:
            this.consumeEnterAnonymousClassBody(false);
            break;
         case 441:
            this.consumeClassBodyopt();
            break;
         case 443:
            this.consumeEnterAnonymousClassBody(true);
            break;
         case 445:
            this.consumeArgumentList();
            break;
         case 446:
            this.consumeArrayCreationHeader();
            break;
         case 447:
            this.consumeArrayCreationHeader();
            break;
         case 448:
            this.consumeArrayCreationExpressionWithoutInitializer();
            break;
         case 449:
            this.consumeArrayCreationExpressionWithInitializer();
            break;
         case 450:
            this.consumeArrayCreationExpressionWithoutInitializer();
            break;
         case 451:
            this.consumeArrayCreationExpressionWithInitializer();
            break;
         case 453:
            this.consumeDimWithOrWithOutExprs();
            break;
         case 455:
            this.consumeDimWithOrWithOutExpr();
            break;
         case 456:
            this.consumeDims();
            break;
         case 459:
            this.consumeOneDimLoop(false);
            break;
         case 460:
            this.consumeOneDimLoop(true);
            break;
         case 461:
            this.consumeFieldAccess(false);
            break;
         case 462:
            this.consumeFieldAccess(true);
            break;
         case 463:
            this.consumeFieldAccess(false);
            break;
         case 464:
            this.consumeMethodInvocationName();
            break;
         case 465:
            this.consumeMethodInvocationNameWithTypeArguments();
            break;
         case 466:
            this.consumeMethodInvocationPrimaryWithTypeArguments();
            break;
         case 467:
            this.consumeMethodInvocationPrimary();
            break;
         case 468:
            this.consumeMethodInvocationPrimary();
            break;
         case 469:
            this.consumeMethodInvocationPrimaryWithTypeArguments();
            break;
         case 470:
            this.consumeMethodInvocationSuperWithTypeArguments();
            break;
         case 471:
            this.consumeMethodInvocationSuper();
            break;
         case 472:
            this.consumeArrayAccess(true);
            break;
         case 473:
            this.consumeArrayAccess(false);
            break;
         case 474:
            this.consumeArrayAccess(false);
            break;
         case 476:
            this.consumePostfixExpression();
            break;
         case 479:
            this.consumeUnaryExpression(14, true);
            break;
         case 480:
            this.consumeUnaryExpression(13, true);
            break;
         case 481:
            this.consumePushPosition();
            break;
         case 484:
            this.consumeUnaryExpression(14);
            break;
         case 485:
            this.consumeUnaryExpression(13);
            break;
         case 487:
            this.consumeUnaryExpression(14, false);
            break;
         case 488:
            this.consumeUnaryExpression(13, false);
            break;
         case 490:
            this.consumeUnaryExpression(12);
            break;
         case 491:
            this.consumeUnaryExpression(11);
            break;
         case 493:
            this.consumeCastExpressionWithPrimitiveType();
            break;
         case 494:
            this.consumeCastExpressionWithGenericsArray();
            break;
         case 495:
            this.consumeCastExpressionWithQualifiedGenericsArray();
            break;
         case 496:
            this.consumeCastExpressionLL1();
            break;
         case 497:
            this.consumeCastExpressionLL1WithBounds();
            break;
         case 498:
            this.consumeCastExpressionWithNameArray();
            break;
         case 499:
            this.consumeZeroAdditionalBounds();
            break;
         case 503:
            this.consumeOnlyTypeArgumentsForCastExpression();
            break;
         case 504:
            this.consumeInsideCastExpression();
            break;
         case 505:
            this.consumeInsideCastExpressionLL1();
            break;
         case 506:
            this.consumeInsideCastExpressionLL1WithBounds();
            break;
         case 507:
            this.consumeInsideCastExpressionWithQualifiedGenerics();
            break;
         case 509:
            this.consumeBinaryExpression(15);
            break;
         case 510:
            this.consumeBinaryExpression(9);
            break;
         case 511:
            this.consumeBinaryExpression(16);
            break;
         case 513:
            this.consumeBinaryExpression(14);
            break;
         case 514:
            this.consumeBinaryExpression(13);
            break;
         case 516:
            this.consumeBinaryExpression(10);
            break;
         case 517:
            this.consumeBinaryExpression(17);
            break;
         case 518:
            this.consumeBinaryExpression(19);
            break;
         case 520:
            this.consumeBinaryExpression(4);
            break;
         case 521:
            this.consumeBinaryExpression(6);
            break;
         case 522:
            this.consumeBinaryExpression(5);
            break;
         case 523:
            this.consumeBinaryExpression(7);
            break;
         case 525:
            this.consumeInstanceOfExpression();
            break;
         case 527:
            this.consumeEqualityExpression(18);
            break;
         case 528:
            this.consumeEqualityExpression(29);
            break;
         case 530:
            this.consumeBinaryExpression(2);
            break;
         case 532:
            this.consumeBinaryExpression(8);
            break;
         case 534:
            this.consumeBinaryExpression(3);
            break;
         case 536:
            this.consumeBinaryExpression(0);
            break;
         case 538:
            this.consumeBinaryExpression(1);
            break;
         case 540:
            this.consumeConditionalExpression(23);
            break;
         case 543:
            this.consumeAssignment();
            break;
         case 545:
            this.ignoreExpressionAssignment();
            break;
         case 546:
            this.consumeAssignmentOperator(30);
            break;
         case 547:
            this.consumeAssignmentOperator(15);
            break;
         case 548:
            this.consumeAssignmentOperator(9);
            break;
         case 549:
            this.consumeAssignmentOperator(16);
            break;
         case 550:
            this.consumeAssignmentOperator(14);
            break;
         case 551:
            this.consumeAssignmentOperator(13);
            break;
         case 552:
            this.consumeAssignmentOperator(10);
            break;
         case 553:
            this.consumeAssignmentOperator(17);
            break;
         case 554:
            this.consumeAssignmentOperator(19);
            break;
         case 555:
            this.consumeAssignmentOperator(2);
            break;
         case 556:
            this.consumeAssignmentOperator(8);
            break;
         case 557:
            this.consumeAssignmentOperator(3);
            break;
         case 558:
            this.consumeExpression();
            break;
         case 561:
            this.consumeEmptyExpression();
            break;
         case 566:
            this.consumeEmptyClassBodyDeclarationsopt();
            break;
         case 567:
            this.consumeClassBodyDeclarationsopt();
            break;
         case 568:
            this.consumeDefaultModifiers();
            break;
         case 569:
            this.consumeModifiers();
            break;
         case 570:
            this.consumeEmptyBlockStatementsopt();
            break;
         case 572:
            this.consumeEmptyDimsopt();
            break;
         case 574:
            this.consumeEmptyArgumentListopt();
            break;
         case 578:
            this.consumeFormalParameterListopt();
            break;
         case 582:
            this.consumeEmptyInterfaceMemberDeclarationsopt();
            break;
         case 583:
            this.consumeInterfaceMemberDeclarationsopt();
            break;
         case 584:
            this.consumeNestedType();
            break;
         case 585:
            this.consumeEmptyForInitopt();
            break;
         case 587:
            this.consumeEmptyForUpdateopt();
            break;
         case 591:
            this.consumeEmptyCatchesopt();
            break;
         case 593:
            this.consumeEnumDeclaration();
            break;
         case 594:
            this.consumeEnumHeader();
            break;
         case 595:
            this.consumeEnumHeaderName();
            break;
         case 596:
            this.consumeEnumHeaderNameWithTypeParameters();
            break;
         case 597:
            this.consumeEnumBodyNoConstants();
            break;
         case 598:
            this.consumeEnumBodyNoConstants();
            break;
         case 599:
            this.consumeEnumBodyWithConstants();
            break;
         case 600:
            this.consumeEnumBodyWithConstants();
            break;
         case 602:
            this.consumeEnumConstants();
            break;
         case 603:
            this.consumeEnumConstantHeaderName();
            break;
         case 604:
            this.consumeEnumConstantHeader();
            break;
         case 605:
            this.consumeEnumConstantWithClassBody();
            break;
         case 606:
            this.consumeEnumConstantNoClassBody();
            break;
         case 607:
            this.consumeArguments();
            break;
         case 608:
            this.consumeEmptyArguments();
            break;
         case 610:
            this.consumeEnumDeclarations();
            break;
         case 611:
            this.consumeEmptyEnumDeclarations();
            break;
         case 613:
            this.consumeEnhancedForStatement();
            break;
         case 614:
            this.consumeEnhancedForStatement();
            break;
         case 615:
            this.consumeEnhancedForStatementHeaderInit(false);
            break;
         case 616:
            this.consumeEnhancedForStatementHeaderInit(true);
            break;
         case 617:
            this.consumeEnhancedForStatementHeader();
            break;
         case 618:
            this.consumeImportDeclaration();
            break;
         case 619:
            this.consumeSingleStaticImportDeclarationName();
            break;
         case 620:
            this.consumeImportDeclaration();
            break;
         case 621:
            this.consumeStaticImportOnDemandDeclarationName();
            break;
         case 622:
            this.consumeTypeArguments();
            break;
         case 623:
            this.consumeOnlyTypeArguments();
            break;
         case 625:
            this.consumeTypeArgumentList1();
            break;
         case 627:
            this.consumeTypeArgumentList();
            break;
         case 628:
            this.consumeTypeArgument();
            break;
         case 632:
            this.consumeReferenceType1();
            break;
         case 633:
            this.consumeTypeArgumentReferenceType1();
            break;
         case 635:
            this.consumeTypeArgumentList2();
            break;
         case 638:
            this.consumeReferenceType2();
            break;
         case 639:
            this.consumeTypeArgumentReferenceType2();
            break;
         case 641:
            this.consumeTypeArgumentList3();
            break;
         case 644:
            this.consumeReferenceType3();
            break;
         case 645:
            this.consumeWildcard();
            break;
         case 646:
            this.consumeWildcardWithBounds();
            break;
         case 647:
            this.consumeWildcardBoundsExtends();
            break;
         case 648:
            this.consumeWildcardBoundsSuper();
            break;
         case 649:
            this.consumeWildcard1();
            break;
         case 650:
            this.consumeWildcard1WithBounds();
            break;
         case 651:
            this.consumeWildcardBounds1Extends();
            break;
         case 652:
            this.consumeWildcardBounds1Super();
            break;
         case 653:
            this.consumeWildcard2();
            break;
         case 654:
            this.consumeWildcard2WithBounds();
            break;
         case 655:
            this.consumeWildcardBounds2Extends();
            break;
         case 656:
            this.consumeWildcardBounds2Super();
            break;
         case 657:
            this.consumeWildcard3();
            break;
         case 658:
            this.consumeWildcard3WithBounds();
            break;
         case 659:
            this.consumeWildcardBounds3Extends();
            break;
         case 660:
            this.consumeWildcardBounds3Super();
            break;
         case 661:
            this.consumeTypeParameterHeader();
            break;
         case 662:
            this.consumeTypeParameters();
            break;
         case 664:
            this.consumeTypeParameterList();
            break;
         case 666:
            this.consumeTypeParameterWithExtends();
            break;
         case 667:
            this.consumeTypeParameterWithExtendsAndBounds();
            break;
         case 669:
            this.consumeAdditionalBoundList();
            break;
         case 670:
            this.consumeAdditionalBound();
            break;
         case 672:
            this.consumeTypeParameterList1();
            break;
         case 673:
            this.consumeTypeParameter1();
            break;
         case 674:
            this.consumeTypeParameter1WithExtends();
            break;
         case 675:
            this.consumeTypeParameter1WithExtendsAndBounds();
            break;
         case 677:
            this.consumeAdditionalBoundList1();
            break;
         case 678:
            this.consumeAdditionalBound1();
            break;
         case 684:
            this.consumeUnaryExpression(14);
            break;
         case 685:
            this.consumeUnaryExpression(13);
            break;
         case 688:
            this.consumeUnaryExpression(12);
            break;
         case 689:
            this.consumeUnaryExpression(11);
            break;
         case 692:
            this.consumeBinaryExpression(15);
            break;
         case 693:
            this.consumeBinaryExpressionWithName(15);
            break;
         case 694:
            this.consumeBinaryExpression(9);
            break;
         case 695:
            this.consumeBinaryExpressionWithName(9);
            break;
         case 696:
            this.consumeBinaryExpression(16);
            break;
         case 697:
            this.consumeBinaryExpressionWithName(16);
            break;
         case 699:
            this.consumeBinaryExpression(14);
            break;
         case 700:
            this.consumeBinaryExpressionWithName(14);
            break;
         case 701:
            this.consumeBinaryExpression(13);
            break;
         case 702:
            this.consumeBinaryExpressionWithName(13);
            break;
         case 704:
            this.consumeBinaryExpression(10);
            break;
         case 705:
            this.consumeBinaryExpressionWithName(10);
            break;
         case 706:
            this.consumeBinaryExpression(17);
            break;
         case 707:
            this.consumeBinaryExpressionWithName(17);
            break;
         case 708:
            this.consumeBinaryExpression(19);
            break;
         case 709:
            this.consumeBinaryExpressionWithName(19);
            break;
         case 711:
            this.consumeBinaryExpression(4);
            break;
         case 712:
            this.consumeBinaryExpressionWithName(4);
            break;
         case 713:
            this.consumeBinaryExpression(6);
            break;
         case 714:
            this.consumeBinaryExpressionWithName(6);
            break;
         case 715:
            this.consumeBinaryExpression(5);
            break;
         case 716:
            this.consumeBinaryExpressionWithName(5);
            break;
         case 717:
            this.consumeBinaryExpression(7);
            break;
         case 718:
            this.consumeBinaryExpressionWithName(7);
            break;
         case 720:
            this.consumeInstanceOfExpressionWithName();
            break;
         case 721:
            this.consumeInstanceOfExpression();
            break;
         case 723:
            this.consumeEqualityExpression(18);
            break;
         case 724:
            this.consumeEqualityExpressionWithName(18);
            break;
         case 725:
            this.consumeEqualityExpression(29);
            break;
         case 726:
            this.consumeEqualityExpressionWithName(29);
            break;
         case 728:
            this.consumeBinaryExpression(2);
            break;
         case 729:
            this.consumeBinaryExpressionWithName(2);
            break;
         case 731:
            this.consumeBinaryExpression(8);
            break;
         case 732:
            this.consumeBinaryExpressionWithName(8);
            break;
         case 734:
            this.consumeBinaryExpression(3);
            break;
         case 735:
            this.consumeBinaryExpressionWithName(3);
            break;
         case 737:
            this.consumeBinaryExpression(0);
            break;
         case 738:
            this.consumeBinaryExpressionWithName(0);
            break;
         case 740:
            this.consumeBinaryExpression(1);
            break;
         case 741:
            this.consumeBinaryExpressionWithName(1);
            break;
         case 743:
            this.consumeConditionalExpression(23);
            break;
         case 744:
            this.consumeConditionalExpressionWithName(23);
            break;
         case 748:
            this.consumeAnnotationTypeDeclarationHeaderName();
            break;
         case 749:
            this.consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters();
            break;
         case 750:
            this.consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters();
            break;
         case 751:
            this.consumeAnnotationTypeDeclarationHeaderName();
            break;
         case 752:
            this.consumeAnnotationTypeDeclarationHeader();
            break;
         case 753:
            this.consumeAnnotationTypeDeclaration();
            break;
         case 755:
            this.consumeEmptyAnnotationTypeMemberDeclarationsopt();
            break;
         case 756:
            this.consumeAnnotationTypeMemberDeclarationsopt();
            break;
         case 758:
            this.consumeAnnotationTypeMemberDeclarations();
            break;
         case 759:
            this.consumeMethodHeaderNameWithTypeParameters(true);
            break;
         case 760:
            this.consumeMethodHeaderName(true);
            break;
         case 761:
            this.consumeEmptyMethodHeaderDefaultValue();
            break;
         case 762:
            this.consumeMethodHeaderDefaultValue();
            break;
         case 763:
            this.consumeMethodHeader();
            break;
         case 764:
            this.consumeAnnotationTypeMemberDeclaration();
            break;
         case 772:
            this.consumeAnnotationName();
            break;
         case 773:
            this.consumeNormalAnnotation(false);
            break;
         case 774:
            this.consumeEmptyMemberValuePairsopt();
            break;
         case 777:
            this.consumeMemberValuePairs();
            break;
         case 778:
            this.consumeMemberValuePair();
            break;
         case 779:
            this.consumeEnterMemberValue();
            break;
         case 780:
            this.consumeExitMemberValue();
            break;
         case 782:
            this.consumeMemberValueAsName();
            break;
         case 785:
            this.consumeMemberValueArrayInitializer();
            break;
         case 786:
            this.consumeMemberValueArrayInitializer();
            break;
         case 787:
            this.consumeEmptyMemberValueArrayInitializer();
            break;
         case 788:
            this.consumeEmptyMemberValueArrayInitializer();
            break;
         case 789:
            this.consumeEnterMemberValueArrayInitializer();
            break;
         case 791:
            this.consumeMemberValues();
            break;
         case 792:
            this.consumeMarkerAnnotation(false);
            break;
         case 793:
            this.consumeSingleMemberAnnotationMemberValue();
            break;
         case 794:
            this.consumeSingleMemberAnnotation(false);
            break;
         case 795:
            this.consumeRecoveryMethodHeaderNameWithTypeParameters();
            break;
         case 796:
            this.consumeRecoveryMethodHeaderName();
            break;
         case 797:
            this.consumeRecoveryMethodHeaderNameWithTypeParameters();
            break;
         case 798:
            this.consumeRecoveryMethodHeaderName();
            break;
         case 799:
            this.consumeMethodHeader();
            break;
         case 800:
            this.consumeMethodHeader();
      }
   }

   protected void consumeVariableDeclaratorIdParameter() {
      this.pushOnIntStack(1);
   }

   protected void consumeExplicitThisParameter(boolean isQualified) {
      NameReference qualifyingNameReference = null;
      if (isQualified) {
         qualifyingNameReference = this.getUnspecifiedReference(false);
      }

      this.pushOnExpressionStack(qualifyingNameReference);
      int thisStart = this.intStack[this.intPtr--];
      this.pushIdentifier(ConstantPool.This, ((long)thisStart << 32) + (long)(thisStart + 3));
      this.pushOnIntStack(0);
      this.pushOnIntStack(0);
   }

   protected boolean isAssistParser() {
      return false;
   }

   protected void consumeNestedLambda() {
      this.consumeNestedType();
      this.nestedMethod[this.nestedType]++;
      LambdaExpression lambda = new LambdaExpression(this.compilationUnit.compilationResult, this.isAssistParser());
      this.pushOnAstStack(lambda);
      this.processingLambdaParameterList = true;
   }

   protected void consumeLambdaHeader() {
      int arrowPosition = this.scanner.currentPosition - 1;
      Argument[] arguments = null;
      int length = this.astLengthStack[this.astLengthPtr--];
      this.astPtr -= length;
      if (length != 0) {
         System.arraycopy(this.astStack, this.astPtr + 1, arguments = new Argument[length], 0, length);
      }

      for(int i = 0; i < length; ++i) {
         Argument argument = arguments[i];
         if (argument.isReceiver()) {
            this.problemReporter().illegalThis(argument);
         }

         if (argument.name.length == 1 && argument.name[0] == '_') {
            this.problemReporter().illegalUseOfUnderscoreAsAnIdentifier(argument.sourceStart, argument.sourceEnd, true);
         }
      }

      LambdaExpression lexp = (LambdaExpression)this.astStack[this.astPtr];
      lexp.setArguments(arguments);
      lexp.setArrowPosition(arrowPosition);
      lexp.sourceEnd = this.intStack[this.intPtr--];
      lexp.sourceStart = this.intStack[this.intPtr--];
      lexp.hasParentheses = this.scanner.getSource()[lexp.sourceStart] == '(';
      this.listLength -= arguments == null ? 0 : arguments.length;
      this.processingLambdaParameterList = false;
      if (this.currentElement != null) {
         this.lastCheckPoint = arrowPosition + 1;
         ++this.currentElement.lambdaNestLevel;
      }
   }

   protected void consumeLambdaExpression() {
      --this.nestedType;
      --this.astLengthPtr;
      Statement body = (Statement)this.astStack[this.astPtr--];
      if (body instanceof Block && this.options.ignoreMethodBodies) {
         Statement oldBody = body;
         body = new Block(0);
         body.sourceStart = oldBody.sourceStart;
         body.sourceEnd = oldBody.sourceEnd;
      }

      LambdaExpression lexp = (LambdaExpression)this.astStack[this.astPtr--];
      --this.astLengthPtr;
      lexp.setBody(body);
      lexp.sourceEnd = body.sourceEnd;
      if (body instanceof Expression) {
         Expression expression = (Expression)body;
         expression.statementEnd = body.sourceEnd;
      }

      if (!this.parsingJava8Plus) {
         this.problemReporter().lambdaExpressionsNotBelow18(lexp);
      }

      this.pushOnExpressionStack(lexp);
      if (this.currentElement != null) {
         this.lastCheckPoint = body.sourceEnd + 1;
         --this.currentElement.lambdaNestLevel;
      }

      this.referenceContext.compilationResult().hasFunctionalTypes = true;
      this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.LAMBDA);
      if (lexp.compilationResult.getCompilationUnit() == null) {
         int length = lexp.sourceEnd - lexp.sourceStart + 1;
         System.arraycopy(this.scanner.getSource(), lexp.sourceStart, lexp.text = new char[length], 0, length);
      }
   }

   protected Argument typeElidedArgument() {
      --this.identifierLengthPtr;
      char[] identifierName = this.identifierStack[this.identifierPtr];
      long namePositions = this.identifierPositionStack[this.identifierPtr--];
      Argument arg = new Argument(identifierName, namePositions, null, 0, true);
      arg.declarationSourceStart = (int)(namePositions >>> 32);
      return arg;
   }

   protected void consumeTypeElidedLambdaParameter(boolean parenthesized) {
      int modifier = 0;
      int annotationLength = 0;
      int modifiersStart = 0;
      if (parenthesized) {
         modifiersStart = this.intStack[this.intPtr--];
         modifier = this.intStack[this.intPtr--];
         annotationLength = this.expressionLengthStack[this.expressionLengthPtr--];
         this.expressionPtr -= annotationLength;
      }

      Argument arg = this.typeElidedArgument();
      if (modifier != 0 || annotationLength != 0) {
         this.problemReporter().illegalModifiersForElidedType(arg);
         arg.declarationSourceStart = modifiersStart;
      }

      if (!parenthesized) {
         this.pushOnIntStack(arg.declarationSourceStart);
         this.pushOnIntStack(arg.declarationSourceEnd);
      }

      this.pushOnAstStack(arg);
      ++this.listLength;
   }

   protected void consumeElidedLeftBraceAndReturn() {
      int stackLength = this.stateStackLengthStack.length;
      if (++this.valueLambdaNestDepth >= stackLength) {
         System.arraycopy(this.stateStackLengthStack, 0, this.stateStackLengthStack = new int[stackLength + 4], 0, stackLength);
      }

      this.stateStackLengthStack[this.valueLambdaNestDepth] = this.stateStackTop;
   }

   protected void consumeExpression() {
      if (this.valueLambdaNestDepth >= 0 && this.stateStackLengthStack[this.valueLambdaNestDepth] == this.stateStackTop - 1) {
         --this.valueLambdaNestDepth;
         this.scanner.ungetToken(this.currentToken);
         this.currentToken = 66;
         Expression exp = this.expressionStack[this.expressionPtr--];
         --this.expressionLengthPtr;
         this.pushOnAstStack(exp);
      }
   }

   protected void consumeIdentifierOrNew(boolean newForm) {
      if (newForm) {
         int newStart = this.intStack[this.intPtr--];
         this.pushIdentifier(ConstantPool.Init, ((long)newStart << 32) + (long)(newStart + 2));
      }
   }

   protected void consumeEmptyTypeArguments() {
      this.pushOnGenericsLengthStack(0);
   }

   public ReferenceExpression newReferenceExpression() {
      return new ReferenceExpression();
   }

   protected void consumeReferenceExpressionTypeForm(boolean isPrimitive) {
      ReferenceExpression referenceExpression = this.newReferenceExpression();
      TypeReference[] typeArguments = null;
      int sourceEnd = (int)this.identifierPositionStack[this.identifierPtr];
      referenceExpression.nameSourceStart = (int)(this.identifierPositionStack[this.identifierPtr] >>> 32);
      char[] selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      if (length > 0) {
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
      }

      int dimension = this.intStack[this.intPtr--];
      boolean typeAnnotatedName = false;
      int i = this.identifierLengthStack[this.identifierLengthPtr];

      for(int j = 0; i > 0 && this.typeAnnotationLengthPtr >= 0; ++j) {
         length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr - j];
         if (length != 0) {
            typeAnnotatedName = true;
            break;
         }

         --i;
      }

      if (dimension <= 0 && !typeAnnotatedName) {
         referenceExpression.initialize(this.compilationUnit.compilationResult, this.getUnspecifiedReference(), typeArguments, selector, sourceEnd);
      } else {
         if (!isPrimitive) {
            this.pushOnGenericsLengthStack(0);
            this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
         }

         referenceExpression.initialize(this.compilationUnit.compilationResult, this.getTypeReference(dimension), typeArguments, selector, sourceEnd);
      }

      this.consumeReferenceExpression(referenceExpression);
   }

   protected void consumeReferenceExpressionPrimaryForm() {
      ReferenceExpression referenceExpression = this.newReferenceExpression();
      TypeReference[] typeArguments = null;
      int sourceEnd = (int)this.identifierPositionStack[this.identifierPtr];
      referenceExpression.nameSourceStart = (int)(this.identifierPositionStack[this.identifierPtr] >>> 32);
      char[] selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      if (length > 0) {
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
      }

      Expression primary = this.expressionStack[this.expressionPtr--];
      --this.expressionLengthPtr;
      referenceExpression.initialize(this.compilationUnit.compilationResult, primary, typeArguments, selector, sourceEnd);
      this.consumeReferenceExpression(referenceExpression);
   }

   protected void consumeReferenceExpressionSuperForm() {
      ReferenceExpression referenceExpression = this.newReferenceExpression();
      TypeReference[] typeArguments = null;
      int sourceEnd = (int)this.identifierPositionStack[this.identifierPtr];
      referenceExpression.nameSourceStart = (int)(this.identifierPositionStack[this.identifierPtr] >>> 32);
      char[] selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      if (length > 0) {
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
      }

      SuperReference superReference = new SuperReference(this.intStack[this.intPtr--], this.endPosition);
      referenceExpression.initialize(this.compilationUnit.compilationResult, superReference, typeArguments, selector, sourceEnd);
      this.consumeReferenceExpression(referenceExpression);
   }

   protected void consumeReferenceExpression(ReferenceExpression referenceExpression) {
      this.pushOnExpressionStack(referenceExpression);
      if (!this.parsingJava8Plus) {
         this.problemReporter().referenceExpressionsNotBelow18(referenceExpression);
      }

      if (referenceExpression.compilationResult.getCompilationUnit() == null) {
         int length = referenceExpression.sourceEnd - referenceExpression.sourceStart + 1;
         System.arraycopy(this.scanner.getSource(), referenceExpression.sourceStart, referenceExpression.text = new char[length], 0, length);
      }

      this.referenceContext.compilationResult().hasFunctionalTypes = true;
      this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.METHOD_REFERENCE);
   }

   protected void consumeReferenceExpressionTypeArgumentsAndTrunk(boolean qualified) {
      this.pushOnIntStack(qualified ? 1 : 0);
      this.pushOnIntStack(this.scanner.startPosition - 1);
   }

   protected void consumeReferenceExpressionGenericTypeForm() {
      ReferenceExpression referenceExpression = this.newReferenceExpression();
      TypeReference[] typeArguments = null;
      int sourceEnd = (int)this.identifierPositionStack[this.identifierPtr];
      referenceExpression.nameSourceStart = (int)(this.identifierPositionStack[this.identifierPtr] >>> 32);
      char[] selector = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      if (length > 0) {
         this.genericsPtr -= length;
         System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments = new TypeReference[length], 0, length);
         --this.intPtr;
      }

      int typeSourceEnd = this.intStack[this.intPtr--];
      boolean qualified = this.intStack[this.intPtr--] != 0;
      int dims = this.intStack[this.intPtr--];
      TypeReference type;
      if (qualified) {
         Annotation[][] annotationsOnDimensions = dims == 0 ? null : this.getAnnotationsOnDimensions(dims);
         TypeReference rightSide = this.getTypeReference(0);
         type = this.computeQualifiedGenericsFromRightSide(rightSide, dims, annotationsOnDimensions);
      } else {
         this.pushOnGenericsIdentifiersLengthStack(this.identifierLengthStack[this.identifierLengthPtr]);
         type = this.getTypeReference(dims);
      }

      --this.intPtr;
      type.sourceEnd = typeSourceEnd;
      referenceExpression.initialize(this.compilationUnit.compilationResult, type, typeArguments, selector, sourceEnd);
      this.consumeReferenceExpression(referenceExpression);
   }

   protected void consumeEnterInstanceCreationArgumentList() {
   }

   protected void consumeSimpleAssertStatement() {
      --this.expressionLengthPtr;
      this.pushOnAstStack(new AssertStatement(this.expressionStack[this.expressionPtr--], this.intStack[this.intPtr--]));
   }

   protected void consumeSingleMemberAnnotation(boolean isTypeAnnotation) {
      SingleMemberAnnotation singleMemberAnnotation = null;
      int oldIndex = this.identifierPtr;
      TypeReference typeReference = this.getAnnotationType();
      singleMemberAnnotation = new SingleMemberAnnotation(typeReference, this.intStack[this.intPtr--]);
      singleMemberAnnotation.memberValue = this.expressionStack[this.expressionPtr--];
      --this.expressionLengthPtr;
      singleMemberAnnotation.declarationSourceEnd = this.rParenPos;
      if (isTypeAnnotation) {
         this.pushOnTypeAnnotationStack(singleMemberAnnotation);
      } else {
         this.pushOnExpressionStack(singleMemberAnnotation);
      }

      if (this.currentElement != null) {
         this.annotationRecoveryCheckPoint(singleMemberAnnotation.sourceStart, singleMemberAnnotation.declarationSourceEnd);
         if (this.currentElement instanceof RecoveredAnnotation) {
            this.currentElement = ((RecoveredAnnotation)this.currentElement).addAnnotation(singleMemberAnnotation, oldIndex);
         }
      }

      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         this.problemReporter().invalidUsageOfAnnotation(singleMemberAnnotation);
      }

      this.recordStringLiterals = true;
   }

   protected void consumeSingleMemberAnnotationMemberValue() {
      if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
         RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation)this.currentElement;
         recoveredAnnotation.setKind(2);
      }
   }

   protected void consumeSingleResource() {
   }

   protected void consumeSingleStaticImportDeclarationName() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
      ImportReference impt;
      this.pushOnAstStack(impt = new ImportReference(tokens, positions, false, 8));
      this.modifiers = 0;
      this.modifiersSourceStart = -1;
      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      impt.declarationSourceStart = this.intStack[this.intPtr--];
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         impt.modifiers = 0;
         this.problemReporter().invalidUsageOfStaticImports(impt);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.currentElement = this.currentElement.add(impt, 0);
         this.lastIgnoredToken = -1;
         this.restartRecovery = true;
      }
   }

   protected void consumeSingleTypeImportDeclarationName() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
      ImportReference impt;
      this.pushOnAstStack(impt = new ImportReference(tokens, positions, false, 0));
      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      impt.declarationSourceStart = this.intStack[this.intPtr--];
      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.currentElement = this.currentElement.add(impt, 0);
         this.lastIgnoredToken = -1;
         this.restartRecovery = true;
      }
   }

   protected void consumeStatementBreak() {
      this.pushOnAstStack(new BreakStatement(null, this.intStack[this.intPtr--], this.endStatementPosition));
      if (this.pendingRecoveredType != null) {
         if (this.pendingRecoveredType.allocation == null && this.endPosition <= this.pendingRecoveredType.declarationSourceEnd) {
            this.astStack[this.astPtr] = this.pendingRecoveredType;
            this.pendingRecoveredType = null;
            return;
         }

         this.pendingRecoveredType = null;
      }
   }

   protected void consumeStatementBreakWithLabel() {
      this.pushOnAstStack(new BreakStatement(this.identifierStack[this.identifierPtr--], this.intStack[this.intPtr--], this.endStatementPosition));
      --this.identifierLengthPtr;
   }

   protected void consumeStatementCatch() {
      --this.astLengthPtr;
      this.listLength = 0;
   }

   protected void consumeStatementContinue() {
      this.pushOnAstStack(new ContinueStatement(null, this.intStack[this.intPtr--], this.endStatementPosition));
   }

   protected void consumeStatementContinueWithLabel() {
      this.pushOnAstStack(new ContinueStatement(this.identifierStack[this.identifierPtr--], this.intStack[this.intPtr--], this.endStatementPosition));
      --this.identifierLengthPtr;
   }

   protected void consumeStatementDo() {
      --this.intPtr;
      Statement statement = (Statement)this.astStack[this.astPtr];
      --this.expressionLengthPtr;
      this.astStack[this.astPtr] = new DoStatement(
         this.expressionStack[this.expressionPtr--], statement, this.intStack[this.intPtr--], this.endStatementPosition
      );
   }

   protected void consumeStatementExpressionList() {
      this.concatExpressionLists();
   }

   protected void consumeStatementFor() {
      Expression cond = null;
      boolean scope = true;
      --this.astLengthPtr;
      Statement statement = (Statement)this.astStack[this.astPtr--];
      int length;
      Statement[] updates;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) == 0) {
         updates = null;
      } else {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, updates = new Statement[length], 0, length);
      }

      if (this.expressionLengthStack[this.expressionLengthPtr--] != 0) {
         cond = this.expressionStack[this.expressionPtr--];
      }

      Statement[] inits;
      if ((length = this.astLengthStack[this.astLengthPtr--]) == false) {
         inits = null;
         scope = false;
      } else if (length == -1) {
         scope = false;
         length = this.expressionLengthStack[this.expressionLengthPtr--];
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, inits = new Statement[length], 0, length);
      } else {
         this.astPtr -= length;
         System.arraycopy(this.astStack, this.astPtr + 1, inits = new Statement[length], 0, length);
      }

      this.pushOnAstStack(new ForStatement(inits, cond, updates, statement, scope, this.intStack[this.intPtr--], this.endStatementPosition));
   }

   protected void consumeStatementIfNoElse() {
      --this.expressionLengthPtr;
      Statement thenStatement = (Statement)this.astStack[this.astPtr];
      this.astStack[this.astPtr] = new IfStatement(
         this.expressionStack[this.expressionPtr--], thenStatement, this.intStack[this.intPtr--], this.endStatementPosition
      );
   }

   protected void consumeStatementIfWithElse() {
      --this.expressionLengthPtr;
      --this.astLengthPtr;
      this.astStack[--this.astPtr] = new IfStatement(
         this.expressionStack[this.expressionPtr--],
         (Statement)this.astStack[this.astPtr],
         (Statement)this.astStack[this.astPtr + 1],
         this.intStack[this.intPtr--],
         this.endStatementPosition
      );
   }

   protected void consumeStatementLabel() {
      Statement statement = (Statement)this.astStack[this.astPtr];
      this.astStack[this.astPtr] = new LabeledStatement(
         this.identifierStack[this.identifierPtr], statement, this.identifierPositionStack[this.identifierPtr--], this.endStatementPosition
      );
      --this.identifierLengthPtr;
   }

   protected void consumeStatementReturn() {
      if (this.expressionLengthStack[this.expressionLengthPtr--] != 0) {
         this.pushOnAstStack(new ReturnStatement(this.expressionStack[this.expressionPtr--], this.intStack[this.intPtr--], this.endStatementPosition));
      } else {
         this.pushOnAstStack(new ReturnStatement(null, this.intStack[this.intPtr--], this.endStatementPosition));
      }
   }

   protected void consumeStatementSwitch() {
      SwitchStatement switchStatement = new SwitchStatement();
      --this.expressionLengthPtr;
      switchStatement.expression = this.expressionStack[this.expressionPtr--];
      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         this.astPtr -= length;
         System.arraycopy(this.astStack, this.astPtr + 1, switchStatement.statements = new Statement[length], 0, length);
      }

      switchStatement.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
      this.pushOnAstStack(switchStatement);
      switchStatement.blockStart = this.intStack[this.intPtr--];
      switchStatement.sourceStart = this.intStack[this.intPtr--];
      switchStatement.sourceEnd = this.endStatementPosition;
      if (length == 0 && !this.containsComment(switchStatement.blockStart, switchStatement.sourceEnd)) {
         switchStatement.bits |= 8;
      }
   }

   protected void consumeStatementSynchronized() {
      if (this.astLengthStack[this.astLengthPtr] == 0) {
         this.astLengthStack[this.astLengthPtr] = 1;
         --this.expressionLengthPtr;
         this.astStack[++this.astPtr] = new SynchronizedStatement(
            this.expressionStack[this.expressionPtr--], null, this.intStack[this.intPtr--], this.endStatementPosition
         );
      } else {
         --this.expressionLengthPtr;
         this.astStack[this.astPtr] = new SynchronizedStatement(
            this.expressionStack[this.expressionPtr--], (Block)this.astStack[this.astPtr], this.intStack[this.intPtr--], this.endStatementPosition
         );
      }

      this.modifiers = 0;
      this.modifiersSourceStart = -1;
   }

   protected void consumeStatementThrow() {
      --this.expressionLengthPtr;
      this.pushOnAstStack(new ThrowStatement(this.expressionStack[this.expressionPtr--], this.intStack[this.intPtr--], this.endStatementPosition));
   }

   protected void consumeStatementTry(boolean withFinally, boolean hasResources) {
      TryStatement tryStmt = new TryStatement();
      if (withFinally) {
         --this.astLengthPtr;
         tryStmt.finallyBlock = (Block)this.astStack[this.astPtr--];
      }

      int length;
      if ((length = this.astLengthStack[this.astLengthPtr--]) != 0) {
         if (length == 1) {
            tryStmt.catchBlocks = new Block[]{(Block)this.astStack[this.astPtr--]};
            tryStmt.catchArguments = new Argument[]{(Argument)this.astStack[this.astPtr--]};
         } else {
            Block[] bks = tryStmt.catchBlocks = new Block[length];

            for(Argument[] args = tryStmt.catchArguments = new Argument[length]; length-- > 0; args[length] = (Argument)this.astStack[this.astPtr--]) {
               bks[length] = (Block)this.astStack[this.astPtr--];
            }
         }
      }

      --this.astLengthPtr;
      tryStmt.tryBlock = (Block)this.astStack[this.astPtr--];
      if (hasResources) {
         length = this.astLengthStack[this.astLengthPtr--];
         LocalDeclaration[] resources = new LocalDeclaration[length];
         System.arraycopy(this.astStack, (this.astPtr -= length) + 1, resources, 0, length);
         tryStmt.resources = resources;
         if (this.options.sourceLevel < 3342336L) {
            this.problemReporter().autoManagedResourcesNotBelow17(resources);
         }
      }

      tryStmt.sourceEnd = this.endStatementPosition;
      tryStmt.sourceStart = this.intStack[this.intPtr--];
      this.pushOnAstStack(tryStmt);
   }

   protected void consumeStatementWhile() {
      --this.expressionLengthPtr;
      Statement statement = (Statement)this.astStack[this.astPtr];
      this.astStack[this.astPtr] = new WhileStatement(
         this.expressionStack[this.expressionPtr--], statement, this.intStack[this.intPtr--], this.endStatementPosition
      );
   }

   protected void consumeStaticImportOnDemandDeclarationName() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
      ImportReference impt;
      this.pushOnAstStack(impt = new ImportReference(tokens, positions, true, 8));
      impt.trailingStarPosition = this.intStack[this.intPtr--];
      this.modifiers = 0;
      this.modifiersSourceStart = -1;
      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      impt.declarationSourceStart = this.intStack[this.intPtr--];
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         impt.modifiers = 0;
         this.problemReporter().invalidUsageOfStaticImports(impt);
      }

      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.currentElement = this.currentElement.add(impt, 0);
         this.lastIgnoredToken = -1;
         this.restartRecovery = true;
      }
   }

   protected void consumeStaticInitializer() {
      Block block = (Block)this.astStack[this.astPtr];
      if (this.diet) {
         block.bits &= -9;
      }

      Initializer initializer = new Initializer(block, 8);
      this.astStack[this.astPtr] = initializer;
      initializer.sourceEnd = this.endStatementPosition;
      initializer.declarationSourceEnd = this.flushCommentsDefinedPriorTo(this.endStatementPosition);
      this.nestedMethod[this.nestedType]--;
      initializer.declarationSourceStart = this.intStack[this.intPtr--];
      initializer.bodyStart = this.intStack[this.intPtr--];
      initializer.bodyEnd = this.endPosition;
      initializer.javadoc = this.javadoc;
      this.javadoc = null;
      if (this.currentElement != null) {
         this.lastCheckPoint = initializer.declarationSourceEnd;
         this.currentElement = this.currentElement.add(initializer, 0);
         this.lastIgnoredToken = -1;
      }
   }

   protected void consumeStaticOnly() {
      int savedModifiersSourceStart = this.modifiersSourceStart;
      this.checkComment();
      if (this.modifiersSourceStart >= savedModifiersSourceStart) {
         this.modifiersSourceStart = savedModifiersSourceStart;
      }

      this.pushOnIntStack(this.scanner.currentPosition);
      this.pushOnIntStack(this.modifiersSourceStart >= 0 ? this.modifiersSourceStart : this.scanner.startPosition);
      this.jumpOverMethodBody();
      this.nestedMethod[this.nestedType]++;
      this.resetModifiers();
      --this.expressionLengthPtr;
      if (this.currentElement != null) {
         this.recoveredStaticInitializerStart = this.intStack[this.intPtr];
      }
   }

   protected void consumeSwitchBlock() {
      this.concatNodeLists();
   }

   protected void consumeSwitchBlockStatement() {
      this.concatNodeLists();
   }

   protected void consumeSwitchBlockStatements() {
      this.concatNodeLists();
   }

   protected void consumeSwitchLabels() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeToken(int type) {
      switch(type) {
         case 1:
         case 2:
            this.endPosition = this.scanner.startPosition;
            this.endStatementPosition = this.scanner.currentPosition - 1;
         case 3:
         case 8:
         case 9:
         case 10:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 23:
         case 26:
         case 30:
         case 31:
         case 33:
         case 60:
         case 61:
         case 65:
         case 66:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 96:
         case 100:
         case 109:
         case 111:
         case 112:
         default:
            break;
         case 6:
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            break;
         case 7:
            this.colonColonStart = this.scanner.currentPosition - 2;
            break;
         case 11:
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 22:
            this.pushIdentifier();
            if (this.scanner.useAssertAsAnIndentifier && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
               long positions = this.identifierPositionStack[this.identifierPtr];
               if (!this.statementRecoveryActivated) {
                  this.problemReporter().useAssertAsAnIdentifier((int)(positions >>> 32), (int)positions);
               }
            }

            if (this.scanner.useEnumAsAnIndentifier && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
               long positions = this.identifierPositionStack[this.identifierPtr];
               if (!this.statementRecoveryActivated) {
                  this.problemReporter().useEnumAsAnIdentifier((int)(positions >>> 32), (int)positions);
               }
            }
            break;
         case 24:
            this.lParenPos = this.scanner.startPosition;
            break;
         case 25:
            this.rParenPos = this.scanner.currentPosition - 1;
            break;
         case 27:
            this.expectTypeAnnotation = true;
            this.pushOnIntStack(this.dimensions);
            this.dimensions = 0;
         case 37:
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 28:
         case 32:
            this.endStatementPosition = this.scanner.currentPosition - 1;
            this.endPosition = this.scanner.startPosition - 1;
            break;
         case 29:
            this.pushOnIntStack(this.scanner.startPosition);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            break;
         case 34:
         case 35:
            this.endPosition = this.scanner.currentPosition - 1;
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 36:
            this.resetModifiers();
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 38:
            this.pushOnExpressionStack(new FalseLiteral(this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 39:
            this.pushOnExpressionStack(new NullLiteral(this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 40:
            this.checkAndSetModifiers(8);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 41:
            this.synchronizedBlockSourceStart = this.scanner.startPosition;
            this.checkAndSetModifiers(32);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 42:
            this.pushOnExpressionStack(new TrueLiteral(this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 43:
            this.pushOnExpressionStack(
               IntLiteral.buildIntLiteral(this.scanner.getCurrentTokenSource(), this.scanner.startPosition, this.scanner.currentPosition - 1)
            );
            break;
         case 44:
            this.pushOnExpressionStack(
               LongLiteral.buildLongLiteral(this.scanner.getCurrentTokenSource(), this.scanner.startPosition, this.scanner.currentPosition - 1)
            );
            break;
         case 45:
            this.pushOnExpressionStack(new FloatLiteral(this.scanner.getCurrentTokenSource(), this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 46:
            this.pushOnExpressionStack(new DoubleLiteral(this.scanner.getCurrentTokenSource(), this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 47:
            this.pushOnExpressionStack(new CharLiteral(this.scanner.getCurrentTokenSource(), this.scanner.startPosition, this.scanner.currentPosition - 1));
            break;
         case 48:
            StringLiteral stringLiteral;
            if (this.recordStringLiterals
               && !this.reparsingLambdaExpression
               && this.checkExternalizeStrings
               && this.lastPosistion < this.scanner.currentPosition
               && !this.statementRecoveryActivated) {
               stringLiteral = this.createStringLiteral(
                  this.scanner.getCurrentTokenSourceString(),
                  this.scanner.startPosition,
                  this.scanner.currentPosition - 1,
                  Util.getLineNumber(this.scanner.startPosition, this.scanner.lineEnds, 0, this.scanner.linePtr)
               );
               this.compilationUnit.recordStringLiteral(stringLiteral, this.currentElement != null);
            } else {
               stringLiteral = this.createStringLiteral(
                  this.scanner.getCurrentTokenSourceString(), this.scanner.startPosition, this.scanner.currentPosition - 1, 0
               );
            }

            this.pushOnExpressionStack(stringLiteral);
            break;
         case 49:
            this.endStatementPosition = this.scanner.currentPosition - 1;
         case 4:
         case 5:
         case 62:
         case 63:
            this.endPosition = this.scanner.startPosition;
            break;
         case 50:
            this.flushCommentsDefinedPriorTo(this.scanner.currentPosition);
            break;
         case 51:
            this.checkAndSetModifiers(1024);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 52:
            this.checkAndSetModifiers(16);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 53:
            this.checkAndSetModifiers(256);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 54:
            this.checkAndSetModifiers(2);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 55:
            this.checkAndSetModifiers(4);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 56:
            this.checkAndSetModifiers(1);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 57:
            this.checkAndSetModifiers(2048);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 58:
            this.checkAndSetModifiers(128);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 59:
            this.checkAndSetModifiers(64);
            this.pushOnExpressionStackLengthStack(0);
            break;
         case 64:
            this.rBracketPosition = this.scanner.startPosition;
            this.endPosition = this.scanner.startPosition;
            this.endStatementPosition = this.scanner.currentPosition - 1;
            break;
         case 67:
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 68:
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 69:
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 70:
            if (this.currentElement != null && this.currentElement instanceof RecoveredAnnotation) {
               RecoveredAnnotation recoveredAnnotation = (RecoveredAnnotation)this.currentElement;
               if (recoveredAnnotation.memberValuPairEqualEnd == -1) {
                  recoveredAnnotation.memberValuPairEqualEnd = this.scanner.currentPosition - 1;
               }
            }
            break;
         case 75:
            this.pushOnIntStack(this.scanner.startPosition);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            break;
         case 77:
            this.forStartPosition = this.scanner.startPosition;
         case 71:
         case 72:
         case 73:
         case 74:
         case 76:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 95:
         case 99:
         case 104:
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 97:
            this.pushIdentifier(-5);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 98:
            this.pushIdentifier(-3);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 101:
            this.pushIdentifier(-2);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 102:
            this.pushIdentifier(-8);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 103:
            this.pushIdentifier(-9);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 105:
            this.pushIdentifier(-10);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 106:
            this.pushIdentifier(-7);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 107:
            this.pushIdentifier(-4);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 108:
            this.pushIdentifier(-6);
            this.pushOnIntStack(this.scanner.currentPosition - 1);
            this.pushOnIntStack(this.scanner.startPosition);
            break;
         case 110:
            this.consumeLambdaHeader();
            break;
         case 113:
            this.pushOnIntStack(this.scanner.currentPosition - 1);
      }
   }

   protected void consumeTypeArgument() {
      this.pushOnGenericsStack(this.getTypeReference(this.intStack[this.intPtr--]));
   }

   protected void consumeTypeArgumentList() {
      this.concatGenericsLists();
   }

   protected void consumeTypeArgumentList1() {
      this.concatGenericsLists();
   }

   protected void consumeTypeArgumentList2() {
      this.concatGenericsLists();
   }

   protected void consumeTypeArgumentList3() {
      this.concatGenericsLists();
   }

   protected void consumeTypeArgumentReferenceType1() {
      this.concatGenericsLists();
      this.pushOnGenericsStack(this.getTypeReference(0));
      --this.intPtr;
   }

   protected void consumeTypeArgumentReferenceType2() {
      this.concatGenericsLists();
      this.pushOnGenericsStack(this.getTypeReference(0));
      --this.intPtr;
   }

   protected void consumeTypeArguments() {
      this.concatGenericsLists();
      --this.intPtr;
      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         int length = this.genericsLengthStack[this.genericsLengthPtr];
         this.problemReporter()
            .invalidUsageOfTypeArguments((TypeReference)this.genericsStack[this.genericsPtr - length + 1], (TypeReference)this.genericsStack[this.genericsPtr]);
      }
   }

   protected void consumeTypeDeclarations() {
      this.concatNodeLists();
   }

   protected void consumeTypeHeaderNameWithTypeParameters() {
      TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
      int length = this.genericsLengthStack[this.genericsLengthPtr--];
      this.genericsPtr -= length;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);
      typeDecl.bodyStart = typeDecl.typeParameters[length - 1].declarationSourceEnd + 1;
      this.listTypeParameterLength = 0;
      if (this.currentElement != null) {
         if (this.currentElement instanceof RecoveredType) {
            RecoveredType recoveredType = (RecoveredType)this.currentElement;
            recoveredType.pendingTypeParameters = null;
            this.lastCheckPoint = typeDecl.bodyStart;
         } else {
            this.lastCheckPoint = typeDecl.bodyStart;
            this.currentElement = this.currentElement.add(typeDecl, 0);
            this.lastIgnoredToken = -1;
         }
      }
   }

   protected void consumeTypeImportOnDemandDeclarationName() {
      int length;
      char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
      this.identifierPtr -= length;
      long[] positions = new long[length];
      System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
      System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
      ImportReference impt;
      this.pushOnAstStack(impt = new ImportReference(tokens, positions, true, 0));
      impt.trailingStarPosition = this.intStack[this.intPtr--];
      if (this.currentToken == 28) {
         impt.declarationSourceEnd = this.scanner.currentPosition - 1;
      } else {
         impt.declarationSourceEnd = impt.sourceEnd;
      }

      impt.declarationEnd = impt.declarationSourceEnd;
      impt.declarationSourceStart = this.intStack[this.intPtr--];
      if (this.currentElement != null) {
         this.lastCheckPoint = impt.declarationSourceEnd + 1;
         this.currentElement = this.currentElement.add(impt, 0);
         this.lastIgnoredToken = -1;
         this.restartRecovery = true;
      }
   }

   protected void consumeTypeParameter1() {
   }

   protected void consumeTypeParameter1WithExtends() {
      TypeReference superType = (TypeReference)this.genericsStack[this.genericsPtr--];
      --this.genericsLengthPtr;
      TypeParameter typeParameter = (TypeParameter)this.genericsStack[this.genericsPtr];
      typeParameter.declarationSourceEnd = superType.sourceEnd;
      typeParameter.type = superType;
      superType.bits |= 16;
      typeParameter.bits |= superType.bits & 1048576;
      this.genericsStack[this.genericsPtr] = typeParameter;
   }

   protected void consumeTypeParameter1WithExtendsAndBounds() {
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      TypeReference[] bounds = new TypeReference[additionalBoundsLength];
      this.genericsPtr -= additionalBoundsLength;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 0, additionalBoundsLength);
      TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
      TypeParameter typeParameter = (TypeParameter)this.genericsStack[this.genericsPtr];
      typeParameter.declarationSourceEnd = bounds[additionalBoundsLength - 1].sourceEnd;
      typeParameter.type = superType;
      typeParameter.bits |= superType.bits & 1048576;
      superType.bits |= 16;
      typeParameter.bounds = bounds;
      int i = 0;

      for(int max = bounds.length; i < max; ++i) {
         TypeReference bound = bounds[i];
         bound.bits |= 16;
         typeParameter.bits |= bound.bits & 1048576;
      }
   }

   protected void consumeTypeParameterHeader() {
      TypeParameter typeParameter = new TypeParameter();
      int length;
      if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
         System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, typeParameter.annotations = new Annotation[length], 0, length);
         typeParameter.bits |= 1048576;
      }

      long pos = this.identifierPositionStack[this.identifierPtr];
      int end = (int)pos;
      typeParameter.declarationSourceEnd = end;
      typeParameter.sourceEnd = end;
      int start = (int)(pos >>> 32);
      typeParameter.declarationSourceStart = start;
      typeParameter.sourceStart = start;
      typeParameter.name = this.identifierStack[this.identifierPtr--];
      --this.identifierLengthPtr;
      this.pushOnGenericsStack(typeParameter);
      ++this.listTypeParameterLength;
   }

   protected void consumeTypeParameterList() {
      this.concatGenericsLists();
   }

   protected void consumeTypeParameterList1() {
      this.concatGenericsLists();
   }

   protected void consumeTypeParameters() {
      int startPos = this.intStack[this.intPtr--];
      if (this.currentElement != null && this.currentElement instanceof RecoveredType) {
         RecoveredType recoveredType = (RecoveredType)this.currentElement;
         int length = this.genericsLengthStack[this.genericsLengthPtr];
         TypeParameter[] typeParameters = new TypeParameter[length];
         System.arraycopy(this.genericsStack, this.genericsPtr - length + 1, typeParameters, 0, length);
         recoveredType.add(typeParameters, startPos);
      }

      if (!this.statementRecoveryActivated && this.options.sourceLevel < 3211264L && this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
         int length = this.genericsLengthStack[this.genericsLengthPtr];
         this.problemReporter()
            .invalidUsageOfTypeParameters(
               (TypeParameter)this.genericsStack[this.genericsPtr - length + 1], (TypeParameter)this.genericsStack[this.genericsPtr]
            );
      }
   }

   protected void consumeTypeParameterWithExtends() {
      TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
      TypeParameter typeParameter = (TypeParameter)this.genericsStack[this.genericsPtr];
      typeParameter.declarationSourceEnd = superType.sourceEnd;
      typeParameter.type = superType;
      typeParameter.bits |= superType.bits & 1048576;
      superType.bits |= 16;
   }

   protected void consumeTypeParameterWithExtendsAndBounds() {
      int additionalBoundsLength = this.genericsLengthStack[this.genericsLengthPtr--];
      TypeReference[] bounds = new TypeReference[additionalBoundsLength];
      this.genericsPtr -= additionalBoundsLength;
      System.arraycopy(this.genericsStack, this.genericsPtr + 1, bounds, 0, additionalBoundsLength);
      TypeReference superType = this.getTypeReference(this.intStack[this.intPtr--]);
      TypeParameter typeParameter = (TypeParameter)this.genericsStack[this.genericsPtr];
      typeParameter.type = superType;
      typeParameter.bits |= superType.bits & 1048576;
      superType.bits |= 16;
      typeParameter.bounds = bounds;
      typeParameter.declarationSourceEnd = bounds[additionalBoundsLength - 1].sourceEnd;
      int i = 0;

      for(int max = bounds.length; i < max; ++i) {
         TypeReference bound = bounds[i];
         bound.bits |= 16;
         typeParameter.bits |= bound.bits & 1048576;
      }
   }

   protected void consumeZeroAdditionalBounds() {
      if (this.currentToken == 25) {
         this.pushOnGenericsLengthStack(0);
      }
   }

   protected void consumeUnaryExpression(int op) {
      Expression exp = this.expressionStack[this.expressionPtr];
      Expression r;
      if (op == 13) {
         if (exp instanceof IntLiteral) {
            IntLiteral intLiteral = (IntLiteral)exp;
            IntLiteral convertToMinValue = intLiteral.convertToMinValue();
            if (convertToMinValue == intLiteral) {
               r = new UnaryExpression(exp, op);
            } else {
               r = convertToMinValue;
            }
         } else if (exp instanceof LongLiteral) {
            LongLiteral longLiteral = (LongLiteral)exp;
            LongLiteral convertToMinValue = longLiteral.convertToMinValue();
            if (convertToMinValue == longLiteral) {
               r = new UnaryExpression(exp, op);
            } else {
               r = convertToMinValue;
            }
         } else {
            r = new UnaryExpression(exp, op);
         }
      } else {
         r = new UnaryExpression(exp, op);
      }

      r.sourceStart = this.intStack[this.intPtr--];
      r.sourceEnd = exp.sourceEnd;
      this.expressionStack[this.expressionPtr] = r;
   }

   protected void consumeUnaryExpression(int op, boolean post) {
      Expression leftHandSide = this.expressionStack[this.expressionPtr];
      if (leftHandSide instanceof Reference) {
         if (post) {
            this.expressionStack[this.expressionPtr] = new PostfixExpression(leftHandSide, IntLiteral.One, op, this.endStatementPosition);
         } else {
            this.expressionStack[this.expressionPtr] = new PrefixExpression(leftHandSide, IntLiteral.One, op, this.intStack[this.intPtr--]);
         }
      } else {
         if (!post) {
            --this.intPtr;
         }

         if (!this.statementRecoveryActivated) {
            this.problemReporter().invalidUnaryExpression(leftHandSide);
         }
      }
   }

   protected void consumeVariableDeclarators() {
      this.optimizedConcatNodeLists();
   }

   protected void consumeVariableInitializers() {
      this.concatExpressionLists();
   }

   protected void consumeWildcard() {
      Wildcard wildcard = new Wildcard(0);
      wildcard.sourceEnd = this.intStack[this.intPtr--];
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcard1() {
      Wildcard wildcard = new Wildcard(0);
      wildcard.sourceEnd = this.intStack[this.intPtr--];
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcard1WithBounds() {
   }

   protected void consumeWildcard2() {
      Wildcard wildcard = new Wildcard(0);
      wildcard.sourceEnd = this.intStack[this.intPtr--];
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcard2WithBounds() {
   }

   protected void consumeWildcard3() {
      Wildcard wildcard = new Wildcard(0);
      wildcard.sourceEnd = this.intStack[this.intPtr--];
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcard3WithBounds() {
   }

   protected void consumeWildcardBounds1Extends() {
      Wildcard wildcard = new Wildcard(1);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBounds1Super() {
      Wildcard wildcard = new Wildcard(2);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      --this.intPtr;
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBounds2Extends() {
      Wildcard wildcard = new Wildcard(1);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBounds2Super() {
      Wildcard wildcard = new Wildcard(2);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      --this.intPtr;
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBounds3Extends() {
      Wildcard wildcard = new Wildcard(1);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBounds3Super() {
      Wildcard wildcard = new Wildcard(2);
      wildcard.bound = (TypeReference)this.genericsStack[this.genericsPtr];
      --this.intPtr;
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.genericsStack[this.genericsPtr] = wildcard;
   }

   protected void consumeWildcardBoundsExtends() {
      Wildcard wildcard = new Wildcard(1);
      wildcard.bound = this.getTypeReference(this.intStack[this.intPtr--]);
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcardBoundsSuper() {
      Wildcard wildcard = new Wildcard(2);
      wildcard.bound = this.getTypeReference(this.intStack[this.intPtr--]);
      --this.intPtr;
      wildcard.sourceEnd = wildcard.bound.sourceEnd;
      --this.intPtr;
      wildcard.sourceStart = this.intStack[this.intPtr--];
      this.annotateTypeReference(wildcard);
      this.pushOnGenericsStack(wildcard);
   }

   protected void consumeWildcardWithBounds() {
   }

   public boolean containsComment(int sourceStart, int sourceEnd) {
      for(int iComment = this.scanner.commentPtr; iComment >= 0; --iComment) {
         int commentStart = this.scanner.commentStarts[iComment];
         if (commentStart < 0) {
            commentStart = -commentStart;
         }

         if (commentStart >= sourceStart && commentStart <= sourceEnd) {
            return true;
         }
      }

      return false;
   }

   public MethodDeclaration convertToMethodDeclaration(ConstructorDeclaration c, CompilationResult compilationResult) {
      MethodDeclaration m = new MethodDeclaration(compilationResult);
      m.typeParameters = c.typeParameters;
      m.sourceStart = c.sourceStart;
      m.sourceEnd = c.sourceEnd;
      m.bodyStart = c.bodyStart;
      m.bodyEnd = c.bodyEnd;
      m.declarationSourceEnd = c.declarationSourceEnd;
      m.declarationSourceStart = c.declarationSourceStart;
      m.selector = c.selector;
      m.statements = c.statements;
      m.modifiers = c.modifiers;
      m.annotations = c.annotations;
      m.arguments = c.arguments;
      m.thrownExceptions = c.thrownExceptions;
      m.explicitDeclarations = c.explicitDeclarations;
      m.returnType = null;
      m.javadoc = c.javadoc;
      m.bits = c.bits;
      return m;
   }

   protected TypeReference augmentTypeWithAdditionalDimensions(
      TypeReference typeReference, int additionalDimensions, Annotation[][] additionalAnnotations, boolean isVarargs
   ) {
      return typeReference.augmentTypeWithAdditionalDimensions(additionalDimensions, additionalAnnotations, isVarargs);
   }

   protected FieldDeclaration createFieldDeclaration(char[] fieldDeclarationName, int sourceStart, int sourceEnd) {
      return new FieldDeclaration(fieldDeclarationName, sourceStart, sourceEnd);
   }

   protected JavadocParser createJavadocParser() {
      return new JavadocParser(this);
   }

   protected LocalDeclaration createLocalDeclaration(char[] localDeclarationName, int sourceStart, int sourceEnd) {
      return new LocalDeclaration(localDeclarationName, sourceStart, sourceEnd);
   }

   protected StringLiteral createStringLiteral(char[] token, int start, int end, int lineNumber) {
      return new StringLiteral(token, start, end, lineNumber);
   }

   protected RecoveredType currentRecoveryType() {
      if (this.currentElement != null) {
         return this.currentElement instanceof RecoveredType ? (RecoveredType)this.currentElement : this.currentElement.enclosingType();
      } else {
         return null;
      }
   }

   public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
      boolean old = this.diet;
      int oldInt = this.dietInt;

      CompilationUnitDeclaration parsedUnit;
      try {
         this.dietInt = 0;
         this.diet = true;
         parsedUnit = this.parse(sourceUnit, compilationResult);
      } finally {
         this.diet = old;
         this.dietInt = oldInt;
      }

      return parsedUnit;
   }

   protected void dispatchDeclarationInto(int length) {
      if (length != 0) {
         int[] flag = new int[length + 1];
         int size1 = 0;
         int size2 = 0;
         int size3 = 0;
         boolean hasAbstractMethods = false;

         for(int i = length - 1; i >= 0; --i) {
            ASTNode astNode = this.astStack[this.astPtr--];
            if (astNode instanceof AbstractMethodDeclaration) {
               flag[i] = 2;
               ++size2;
               if (((AbstractMethodDeclaration)astNode).isAbstract()) {
                  hasAbstractMethods = true;
               }
            } else if (astNode instanceof TypeDeclaration) {
               flag[i] = 3;
               ++size3;
            } else {
               flag[i] = 1;
               ++size1;
            }
         }

         TypeDeclaration typeDecl = (TypeDeclaration)this.astStack[this.astPtr];
         if (size1 != 0) {
            typeDecl.fields = new FieldDeclaration[size1];
         }

         if (size2 != 0) {
            typeDecl.methods = new AbstractMethodDeclaration[size2];
            if (hasAbstractMethods) {
               typeDecl.bits |= 2048;
            }
         }

         if (size3 != 0) {
            typeDecl.memberTypes = new TypeDeclaration[size3];
         }

         size3 = 0;
         size2 = 0;
         size1 = 0;
         int flagI = flag[0];
         int start = 0;

         for(int end = 0; end <= length; ++end) {
            if (flagI != flag[end]) {
               switch(flagI) {
                  case 1: {
                     int length2;
                     size1 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, typeDecl.fields, size1 - length2, length2);
                     break;
                  }
                  case 2: {
                     int length2;
                     size2 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, typeDecl.methods, size2 - length2, length2);
                     break;
                  }
                  case 3: {
                     int length2;
                     size3 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, typeDecl.memberTypes, size3 - length2, length2);
                  }
               }

               start = end;
               flagI = flag[end];
            }
         }

         if (typeDecl.memberTypes != null) {
            for(int i = typeDecl.memberTypes.length - 1; i >= 0; --i) {
               typeDecl.memberTypes[i].enclosingType = typeDecl;
            }
         }
      }
   }

   protected void dispatchDeclarationIntoEnumDeclaration(int length) {
      if (length != 0) {
         int[] flag = new int[length + 1];
         int size1 = 0;
         int size2 = 0;
         int size3 = 0;
         TypeDeclaration enumDeclaration = (TypeDeclaration)this.astStack[this.astPtr - length];
         boolean hasAbstractMethods = false;
         int enumConstantsCounter = 0;

         for(int i = length - 1; i >= 0; --i) {
            ASTNode astNode = this.astStack[this.astPtr--];
            if (astNode instanceof AbstractMethodDeclaration) {
               flag[i] = 2;
               ++size2;
               if (((AbstractMethodDeclaration)astNode).isAbstract()) {
                  hasAbstractMethods = true;
               }
            } else if (astNode instanceof TypeDeclaration) {
               flag[i] = 3;
               ++size3;
            } else if (astNode instanceof FieldDeclaration) {
               flag[i] = 1;
               ++size1;
               if (((FieldDeclaration)astNode).getKind() == 3) {
                  ++enumConstantsCounter;
               }
            }
         }

         if (size1 != 0) {
            enumDeclaration.fields = new FieldDeclaration[size1];
         }

         if (size2 != 0) {
            enumDeclaration.methods = new AbstractMethodDeclaration[size2];
            if (hasAbstractMethods) {
               enumDeclaration.bits |= 2048;
            }
         }

         if (size3 != 0) {
            enumDeclaration.memberTypes = new TypeDeclaration[size3];
         }

         size3 = 0;
         size2 = 0;
         size1 = 0;
         int flagI = flag[0];
         int start = 0;

         for(int end = 0; end <= length; ++end) {
            if (flagI != flag[end]) {
               switch(flagI) {
                  case 1: {
                     int length2;
                     size1 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, enumDeclaration.fields, size1 - length2, length2);
                     break;
                  }
                  case 2: {
                     int length2;
                     size2 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, enumDeclaration.methods, size2 - length2, length2);
                     break;
                  }
                  case 3: {
                     int length2;
                     size3 += length2 = end - start;
                     System.arraycopy(this.astStack, this.astPtr + start + 1, enumDeclaration.memberTypes, size3 - length2, length2);
                  }
               }

               start = end;
               flagI = flag[end];
            }
         }

         if (enumDeclaration.memberTypes != null) {
            for(int i = enumDeclaration.memberTypes.length - 1; i >= 0; --i) {
               enumDeclaration.memberTypes[i].enclosingType = enumDeclaration;
            }
         }

         enumDeclaration.enumConstantsCounter = enumConstantsCounter;
      }
   }

   protected CompilationUnitDeclaration endParse(int act) {
      this.lastAct = act;
      if (this.statementRecoveryActivated) {
         RecoveredElement recoveredElement = this.buildInitialRecoveryState();
         if (recoveredElement != null) {
            recoveredElement.topElement().updateParseTree();
         }

         if (this.hasError) {
            this.resetStacks();
         }
      } else if (this.currentElement != null) {
         if (VERBOSE_RECOVERY) {
            System.out.print(Messages.parser_syntaxRecovery);
            System.out.println("--------------------------");
            System.out.println(this.compilationUnit);
            System.out.println("----------------------------------");
         }

         this.currentElement.topElement().updateParseTree();
      } else if (this.diet & VERBOSE_RECOVERY) {
         System.out.print(Messages.parser_regularParse);
         System.out.println("--------------------------");
         System.out.println(this.compilationUnit);
         System.out.println("----------------------------------");
      }

      this.persistLineSeparatorPositions();

      for(int i = 0; i < this.scanner.foundTaskCount; ++i) {
         if (!this.statementRecoveryActivated) {
            this.problemReporter()
               .task(
                  new String(this.scanner.foundTaskTags[i]),
                  new String(this.scanner.foundTaskMessages[i]),
                  this.scanner.foundTaskPriorities[i] == null ? null : new String(this.scanner.foundTaskPriorities[i]),
                  this.scanner.foundTaskPositions[i][0],
                  this.scanner.foundTaskPositions[i][1]
               );
         }
      }

      this.javadoc = null;
      return this.compilationUnit;
   }

   public int flushCommentsDefinedPriorTo(int position) {
      int lastCommentIndex = this.scanner.commentPtr;
      if (lastCommentIndex < 0) {
         return position;
      } else {
         int index = lastCommentIndex;

         int validCount;
         for(validCount = 0; index >= 0; ++validCount) {
            int commentEnd = this.scanner.commentStops[index];
            if (commentEnd < 0) {
               commentEnd = -commentEnd;
            }

            if (commentEnd <= position) {
               break;
            }

            --index;
         }

         if (validCount > 0) {
            int immediateCommentEnd = -this.scanner.commentStops[index + 1];
            if (immediateCommentEnd > 0) {
               if (Util.getLineNumber(position, this.scanner.lineEnds, 0, this.scanner.linePtr)
                  == Util.getLineNumber(--immediateCommentEnd, this.scanner.lineEnds, 0, this.scanner.linePtr)) {
                  position = immediateCommentEnd;
                  --validCount;
                  ++index;
               }
            }
         }

         if (index < 0) {
            return position;
         } else {
            switch(validCount) {
               case 0:
                  break;
               case 1:
                  this.scanner.commentStarts[0] = this.scanner.commentStarts[index + 1];
                  this.scanner.commentStops[0] = this.scanner.commentStops[index + 1];
                  this.scanner.commentTagStarts[0] = this.scanner.commentTagStarts[index + 1];
                  break;
               case 2:
                  this.scanner.commentStarts[0] = this.scanner.commentStarts[index + 1];
                  this.scanner.commentStops[0] = this.scanner.commentStops[index + 1];
                  this.scanner.commentTagStarts[0] = this.scanner.commentTagStarts[index + 1];
                  this.scanner.commentStarts[1] = this.scanner.commentStarts[index + 2];
                  this.scanner.commentStops[1] = this.scanner.commentStops[index + 2];
                  this.scanner.commentTagStarts[1] = this.scanner.commentTagStarts[index + 2];
                  break;
               default:
                  System.arraycopy(this.scanner.commentStarts, index + 1, this.scanner.commentStarts, 0, validCount);
                  System.arraycopy(this.scanner.commentStops, index + 1, this.scanner.commentStops, 0, validCount);
                  System.arraycopy(this.scanner.commentTagStarts, index + 1, this.scanner.commentTagStarts, 0, validCount);
            }

            this.scanner.commentPtr = validCount - 1;
            return position;
         }
      }
   }

   protected TypeReference getAnnotationType() {
      int length = this.identifierLengthStack[this.identifierLengthPtr--];
      if (length == 1) {
         return new SingleTypeReference(this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr--]);
      } else {
         char[][] tokens = new char[length][];
         this.identifierPtr -= length;
         long[] positions = new long[length];
         System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
         System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
         return new QualifiedTypeReference(tokens, positions);
      }
   }

   public int getFirstToken() {
      return this.firstToken;
   }

   public int[] getJavaDocPositions() {
      int javadocCount = 0;
      int max = this.scanner.commentPtr;

      for(int i = 0; i <= max; ++i) {
         if (this.scanner.commentStarts[i] >= 0 && this.scanner.commentStops[i] > 0) {
            ++javadocCount;
         }
      }

      if (javadocCount == 0) {
         return null;
      } else {
         int[] positions = new int[2 * javadocCount];
         int index = 0;

         for(int i = 0; i <= max; ++i) {
            int commentStart = this.scanner.commentStarts[i];
            if (commentStart >= 0) {
               int commentStop = this.scanner.commentStops[i];
               if (commentStop > 0) {
                  positions[index++] = commentStart;
                  positions[index++] = commentStop - 1;
               }
            }
         }

         return positions;
      }
   }

   public void getMethodBodies(CompilationUnitDeclaration unit) {
      if (unit != null) {
         if (unit.ignoreMethodBodies) {
            unit.ignoreFurtherInvestigation = true;
         } else if ((unit.bits & 16) == 0) {
            int[] oldLineEnds = this.scanner.lineEnds;
            int oldLinePtr = this.scanner.linePtr;
            CompilationResult compilationResult = unit.compilationResult;
            char[] contents = this.readManager != null
               ? this.readManager.getContents(compilationResult.compilationUnit)
               : compilationResult.compilationUnit.getContents();
            this.scanner.setSource(contents, compilationResult);
            if (this.javadocParser != null && this.javadocParser.checkDocComment) {
               this.javadocParser.scanner.setSource(contents);
            }

            if (unit.types != null) {
               int i = 0;

               for(int length = unit.types.length; i < length; ++i) {
                  unit.types[i].parseMethods(this, unit);
               }
            }

            unit.bits |= 16;
            this.scanner.lineEnds = oldLineEnds;
            this.scanner.linePtr = oldLinePtr;
         }
      }
   }

   protected char getNextCharacter(char[] comment, int[] index) {
      char nextCharacter = comment[index[0]++];
      switch(nextCharacter) {
         case '\\':
            index[0]++;

            while(comment[index[0]] == 'u') {
               index[0]++;
            }

            int c1;
            int c2;
            int c3;
            int c4;
            if ((c1 = ScannerHelper.getHexadecimalValue(comment[index[0]++])) <= 15
               && c1 >= 0
               && (c2 = ScannerHelper.getHexadecimalValue(comment[index[0]++])) <= 15
               && c2 >= 0
               && (c3 = ScannerHelper.getHexadecimalValue(comment[index[0]++])) <= 15
               && c3 >= 0
               && (c4 = ScannerHelper.getHexadecimalValue(comment[index[0]++])) <= 15
               && c4 >= 0) {
               nextCharacter = (char)(((c1 * 16 + c2) * 16 + c3) * 16 + c4);
            }
         default:
            return nextCharacter;
      }
   }

   protected Expression getTypeReference(Expression exp) {
      exp.bits &= -8;
      exp.bits |= 4;
      return exp;
   }

   protected void annotateTypeReference(Wildcard ref) {
      int length;
      if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != 0) {
         if (ref.annotations == null) {
            ref.annotations = new Annotation[ref.getAnnotatableLevels()][];
         }

         System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, ref.annotations[0] = new Annotation[length], 0, length);
         if (ref.sourceStart > ref.annotations[0][0].sourceStart) {
            ref.sourceStart = ref.annotations[0][0].sourceStart;
         }

         ref.bits |= 1048576;
      }

      if (ref.bound != null) {
         ref.bits |= ref.bound.bits & 1048576;
      }
   }

   protected TypeReference getTypeReference(int dim) {
      Annotation[][] annotationsOnDimensions = null;
      int length = this.identifierLengthStack[this.identifierLengthPtr--];
      TypeReference ref;
      if (length < 0) {
         if (dim > 0) {
            annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
         }

         ref = TypeReference.baseTypeReference(-length, dim, annotationsOnDimensions);
         ref.sourceStart = this.intStack[this.intPtr--];
         if (dim == 0) {
            ref.sourceEnd = this.intStack[this.intPtr--];
         } else {
            --this.intPtr;
            ref.sourceEnd = this.rBracketPosition;
         }
      } else {
         int numberOfIdentifiers = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr--];
         if (length != numberOfIdentifiers || this.genericsLengthStack[this.genericsLengthPtr] != 0) {
            ref = this.getTypeReferenceForGenericType(dim, length, numberOfIdentifiers);
         } else if (length == 1) {
            --this.genericsLengthPtr;
            if (dim == 0) {
               ref = new SingleTypeReference(this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr--]);
            } else {
               annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
               ref = new ArrayTypeReference(
                  this.identifierStack[this.identifierPtr], dim, annotationsOnDimensions, this.identifierPositionStack[this.identifierPtr--]
               );
               ref.sourceEnd = this.endPosition;
               if (annotationsOnDimensions != null) {
                  ref.bits |= 1048576;
               }
            }
         } else {
            --this.genericsLengthPtr;
            char[][] tokens = new char[length][];
            this.identifierPtr -= length;
            long[] positions = new long[length];
            System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
            System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
            if (dim == 0) {
               ref = new QualifiedTypeReference(tokens, positions);
            } else {
               annotationsOnDimensions = this.getAnnotationsOnDimensions(dim);
               ref = new ArrayQualifiedTypeReference(tokens, dim, annotationsOnDimensions, positions);
               ref.sourceEnd = this.endPosition;
               if (annotationsOnDimensions != null) {
                  ref.bits |= 1048576;
               }
            }
         }
      }

      int levels = ref.getAnnotatableLevels();

      for(int i = levels - 1; i >= 0; --i) {
         if ((length = this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr--]) != false) {
            if (ref.annotations == null) {
               ref.annotations = new Annotation[levels][];
            }

            System.arraycopy(this.typeAnnotationStack, (this.typeAnnotationPtr -= length) + 1, ref.annotations[i] = new Annotation[length], 0, length);
            if (i == 0) {
               ref.sourceStart = ref.annotations[0][0].sourceStart;
            }

            ref.bits |= 1048576;
         }
      }

      return ref;
   }

   protected TypeReference getTypeReferenceForGenericType(int dim, int identifierLength, int numberOfIdentifiers) {
      Annotation[][] annotationsOnDimensions = dim == 0 ? null : this.getAnnotationsOnDimensions(dim);
      if (identifierLength == 1 && numberOfIdentifiers == 1) {
         int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
         TypeReference[] typeArguments = null;
         if (currentTypeArgumentsLength < 0) {
            typeArguments = TypeReference.NO_TYPE_ARGUMENTS;
         } else {
            typeArguments = new TypeReference[currentTypeArgumentsLength];
            this.genericsPtr -= currentTypeArgumentsLength;
            System.arraycopy(this.genericsStack, this.genericsPtr + 1, typeArguments, 0, currentTypeArgumentsLength);
         }

         ParameterizedSingleTypeReference parameterizedSingleTypeReference = new ParameterizedSingleTypeReference(
            this.identifierStack[this.identifierPtr], typeArguments, dim, annotationsOnDimensions, this.identifierPositionStack[this.identifierPtr--]
         );
         if (dim != 0) {
            parameterizedSingleTypeReference.sourceEnd = this.endStatementPosition;
         }

         return parameterizedSingleTypeReference;
      } else {
         TypeReference[][] typeArguments = new TypeReference[numberOfIdentifiers][];
         char[][] tokens = new char[numberOfIdentifiers][];
         long[] positions = new long[numberOfIdentifiers];
         int index = numberOfIdentifiers;
         int currentIdentifiersLength = identifierLength;

         while(index > 0) {
            int currentTypeArgumentsLength = this.genericsLengthStack[this.genericsLengthPtr--];
            if (currentTypeArgumentsLength > 0) {
               this.genericsPtr -= currentTypeArgumentsLength;
               System.arraycopy(
                  this.genericsStack,
                  this.genericsPtr + 1,
                  typeArguments[index - 1] = new TypeReference[currentTypeArgumentsLength],
                  0,
                  currentTypeArgumentsLength
               );
            } else if (currentTypeArgumentsLength < 0) {
               typeArguments[index - 1] = TypeReference.NO_TYPE_ARGUMENTS;
            }

            switch(currentIdentifiersLength) {
               case 1:
                  tokens[index - 1] = this.identifierStack[this.identifierPtr];
                  positions[index - 1] = this.identifierPositionStack[this.identifierPtr--];
                  break;
               default:
                  this.identifierPtr -= currentIdentifiersLength;
                  System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, index - currentIdentifiersLength, currentIdentifiersLength);
                  System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, index - currentIdentifiersLength, currentIdentifiersLength);
            }

            index -= currentIdentifiersLength;
            if (index > 0) {
               currentIdentifiersLength = this.identifierLengthStack[this.identifierLengthPtr--];
            }
         }

         ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = new ParameterizedQualifiedTypeReference(
            tokens, typeArguments, dim, annotationsOnDimensions, positions
         );
         if (dim != 0) {
            parameterizedQualifiedTypeReference.sourceEnd = this.endStatementPosition;
         }

         return parameterizedQualifiedTypeReference;
      }
   }

   protected NameReference getUnspecifiedReference() {
      return this.getUnspecifiedReference(true);
   }

   protected NameReference getUnspecifiedReference(boolean rejectTypeAnnotations) {
      if (rejectTypeAnnotations) {
         this.consumeNonTypeUseName();
      }

      int length;
      NameReference ref;
      if ((length = this.identifierLengthStack[this.identifierLengthPtr--]) == 1) {
         ref = new SingleNameReference(this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr--]);
      } else {
         char[][] tokens = new char[length][];
         this.identifierPtr -= length;
         System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
         long[] positions = new long[length];
         System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
         ref = new QualifiedNameReference(
            tokens,
            positions,
            (int)(this.identifierPositionStack[this.identifierPtr + 1] >> 32),
            (int)this.identifierPositionStack[this.identifierPtr + length]
         );
      }

      return ref;
   }

   protected NameReference getUnspecifiedReferenceOptimized() {
      this.consumeNonTypeUseName();
      int length;
      if ((length = this.identifierLengthStack[this.identifierLengthPtr--]) == 1) {
         NameReference ref = new SingleNameReference(this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr--]);
         ref.bits &= -8;
         ref.bits |= 3;
         return ref;
      } else {
         char[][] tokens = new char[length][];
         this.identifierPtr -= length;
         System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
         long[] positions = new long[length];
         System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
         NameReference ref = new QualifiedNameReference(
            tokens,
            positions,
            (int)(this.identifierPositionStack[this.identifierPtr + 1] >> 32),
            (int)this.identifierPositionStack[this.identifierPtr + length]
         );
         ref.bits &= -8;
         ref.bits |= 3;
         return ref;
      }
   }

   public void goForBlockStatementsopt() {
      this.firstToken = 63;
      this.scanner.recordLineSeparator = false;
   }

   public void goForBlockStatementsOrCatchHeader() {
      this.firstToken = 6;
      this.scanner.recordLineSeparator = false;
   }

   public void goForClassBodyDeclarations() {
      this.firstToken = 21;
      this.scanner.recordLineSeparator = true;
   }

   public void goForCompilationUnit() {
      this.firstToken = 1;
      this.scanner.foundTaskCount = 0;
      this.scanner.recordLineSeparator = true;
   }

   public void goForExpression(boolean recordLineSeparator) {
      this.firstToken = 8;
      this.scanner.recordLineSeparator = recordLineSeparator;
   }

   public void goForFieldDeclaration() {
      this.firstToken = 30;
      this.scanner.recordLineSeparator = true;
   }

   public void goForGenericMethodDeclaration() {
      this.firstToken = 9;
      this.scanner.recordLineSeparator = true;
   }

   public void goForHeaders() {
      RecoveredType currentType = this.currentRecoveryType();
      if (currentType != null && currentType.insideEnumConstantPart) {
         this.firstToken = 62;
      } else {
         this.firstToken = 16;
      }

      this.scanner.recordLineSeparator = true;
   }

   public void goForImportDeclaration() {
      this.firstToken = 31;
      this.scanner.recordLineSeparator = true;
   }

   public void goForInitializer() {
      this.firstToken = 14;
      this.scanner.recordLineSeparator = false;
   }

   public void goForMemberValue() {
      this.firstToken = 31;
      this.scanner.recordLineSeparator = true;
   }

   public void goForMethodBody() {
      this.firstToken = 2;
      this.scanner.recordLineSeparator = false;
   }

   public void goForPackageDeclaration() {
      this.firstToken = 29;
      this.scanner.recordLineSeparator = true;
   }

   public void goForTypeDeclaration() {
      this.firstToken = 4;
      this.scanner.recordLineSeparator = true;
   }

   public boolean hasLeadingTagComment(char[] commentPrefixTag, int rangeEnd) {
      int iComment = this.scanner.commentPtr;
      if (iComment < 0) {
         return false;
      } else {
         int iStatement = this.astLengthPtr;
         if (iStatement >= 0 && this.astLengthStack[iStatement] > 1) {
            ASTNode lastNode = this.astStack[this.astPtr];

            label67:
            for(int rangeStart = lastNode.sourceEnd; iComment >= 0; --iComment) {
               int commentStart = this.scanner.commentStarts[iComment];
               if (commentStart < 0) {
                  commentStart = -commentStart;
               }

               if (commentStart < rangeStart) {
                  return false;
               }

               if (commentStart <= rangeEnd) {
                  char[] source = this.scanner.source;

                  int charPos;
                  for(charPos = commentStart + 2; charPos < rangeEnd; ++charPos) {
                     char c = source[charPos];
                     if (c >= 128 || (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & 256) == 0) {
                        break;
                     }
                  }

                  int iTag = 0;

                  for(int length = commentPrefixTag.length; iTag < length; ++charPos) {
                     if (charPos >= rangeEnd || source[charPos] != commentPrefixTag[iTag]) {
                        if (iTag == 0) {
                           return false;
                        }
                        continue label67;
                     }

                     ++iTag;
                  }

                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }
   }

   protected void ignoreNextClosingBrace() {
      this.ignoreNextClosingBrace = true;
   }

   protected void ignoreExpressionAssignment() {
      --this.intPtr;
      ArrayInitializer arrayInitializer = (ArrayInitializer)this.expressionStack[this.expressionPtr--];
      --this.expressionLengthPtr;
      if (!this.statementRecoveryActivated) {
         this.problemReporter().arrayConstantsOnlyInArrayInitializers(arrayInitializer.sourceStart, arrayInitializer.sourceEnd);
      }
   }

   public void initialize() {
      this.initialize(false);
   }

   public void initialize(boolean parsingCompilationUnit) {
      this.javadoc = null;
      this.astPtr = -1;
      this.astLengthPtr = -1;
      this.expressionPtr = -1;
      this.expressionLengthPtr = -1;
      this.typeAnnotationLengthPtr = -1;
      this.typeAnnotationPtr = -1;
      this.identifierPtr = -1;
      this.identifierLengthPtr = -1;
      this.intPtr = -1;
      this.nestedMethod[this.nestedType = 0] = 0;
      this.variablesCounter[this.nestedType] = 0;
      this.dimensions = 0;
      this.realBlockPtr = -1;
      this.compilationUnit = null;
      this.referenceContext = null;
      this.endStatementPosition = 0;
      this.valueLambdaNestDepth = -1;
      int astLength = this.astStack.length;
      if (this.noAstNodes.length < astLength) {
         this.noAstNodes = new ASTNode[astLength];
      }

      System.arraycopy(this.noAstNodes, 0, this.astStack, 0, astLength);
      int expressionLength = this.expressionStack.length;
      if (this.noExpressions.length < expressionLength) {
         this.noExpressions = new Expression[expressionLength];
      }

      System.arraycopy(this.noExpressions, 0, this.expressionStack, 0, expressionLength);
      this.scanner.commentPtr = -1;
      this.scanner.foundTaskCount = 0;
      this.scanner.eofPosition = Integer.MAX_VALUE;
      this.recordStringLiterals = true;
      boolean checkNLS = this.options.getSeverity(256) != 256;
      this.checkExternalizeStrings = checkNLS;
      this.scanner.checkNonExternalizedStringLiterals = parsingCompilationUnit && checkNLS;
      this.scanner.checkUninternedIdentityComparison = parsingCompilationUnit && this.options.complainOnUninternedIdentityComparison;
      this.scanner.lastPosition = -1;
      this.resetModifiers();
      this.lastCheckPoint = -1;
      this.currentElement = null;
      this.restartRecovery = false;
      this.hasReportedError = false;
      this.recoveredStaticInitializerStart = 0;
      this.lastIgnoredToken = -1;
      this.lastErrorEndPosition = -1;
      this.lastErrorEndPositionBeforeRecovery = -1;
      this.lastJavadocEnd = -1;
      this.listLength = 0;
      this.listTypeParameterLength = 0;
      this.lastPosistion = -1;
      this.rBraceStart = 0;
      this.rBraceEnd = 0;
      this.rBraceSuccessorStart = 0;
      this.rBracketPosition = 0;
      this.genericsIdentifiersLengthPtr = -1;
      this.genericsLengthPtr = -1;
      this.genericsPtr = -1;
   }

   public void initializeScanner() {
      this.scanner = new Scanner(
         false,
         false,
         false,
         this.options.sourceLevel,
         this.options.complianceLevel,
         this.options.taskTags,
         this.options.taskPriorities,
         this.options.isTaskCaseSensitive
      );
   }

   public void jumpOverMethodBody() {
      if (this.diet && this.dietInt == 0) {
         this.scanner.diet = true;
      }
   }

   private void jumpOverType() {
      if (this.recoveredTypes != null && this.nextTypeStart > -1 && this.nextTypeStart < this.scanner.currentPosition) {
         TypeDeclaration typeDeclaration = this.recoveredTypes[this.recoveredTypePtr];
         boolean isAnonymous = typeDeclaration.allocation != null;
         this.scanner.startPosition = typeDeclaration.declarationSourceEnd + 1;
         this.scanner.currentPosition = typeDeclaration.declarationSourceEnd + 1;
         this.scanner.diet = false;
         if (!isAnonymous) {
            ((RecoveryScanner)this.scanner).setPendingTokens(new int[]{28, 73});
         } else {
            ((RecoveryScanner)this.scanner).setPendingTokens(new int[]{22, 70, 22});
         }

         this.pendingRecoveredType = typeDeclaration;

         try {
            this.currentToken = this.scanner.getNextToken();
         } catch (InvalidInputException var4) {
         }

         if (++this.recoveredTypePtr < this.recoveredTypes.length) {
            TypeDeclaration nextTypeDeclaration = this.recoveredTypes[this.recoveredTypePtr];
            this.nextTypeStart = nextTypeDeclaration.allocation == null
               ? nextTypeDeclaration.declarationSourceStart
               : nextTypeDeclaration.allocation.sourceStart;
         } else {
            this.nextTypeStart = Integer.MAX_VALUE;
         }
      }
   }

   protected void markEnclosingMemberWithLocalType() {
      if (this.currentElement == null) {
         this.markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind.LOCAL);
      }
   }

   protected void markEnclosingMemberWithLocalOrFunctionalType(Parser.LocalTypeKind context) {
      for(int i = this.astPtr; i >= 0; --i) {
         ASTNode node = this.astStack[i];
         if (node instanceof AbstractMethodDeclaration
            || node instanceof FieldDeclaration
            || node instanceof TypeDeclaration && ((TypeDeclaration)node).declarationSourceEnd == 0) {
            switch(context) {
               case METHOD_REFERENCE:
                  node.bits |= 2097152;
                  break;
               case LAMBDA:
                  node.bits |= 2097152;
               case LOCAL:
                  node.bits |= 2;
            }

            return;
         }
      }

      if (this.referenceContext instanceof AbstractMethodDeclaration || this.referenceContext instanceof TypeDeclaration) {
         ASTNode node = (ASTNode)this.referenceContext;
         switch(context) {
            case METHOD_REFERENCE:
               node.bits |= 2097152;
               break;
            case LAMBDA:
               node.bits |= 2097152;
            case LOCAL:
               node.bits |= 2;
         }
      }
   }

   protected boolean moveRecoveryCheckpoint() {
      int pos = this.lastCheckPoint;
      this.scanner.startPosition = pos;
      this.scanner.currentPosition = pos;
      this.scanner.diet = false;
      if (this.restartRecovery) {
         this.lastIgnoredToken = -1;
         this.scanner.insideRecovery = true;
         return true;
      } else {
         this.lastIgnoredToken = this.nextIgnoredToken;
         this.nextIgnoredToken = -1;

         do {
            try {
               this.scanner.lookBack[0] = this.scanner.lookBack[1] = 0;
               this.nextIgnoredToken = this.scanner.getNextToken();
               if (this.scanner.currentPosition == this.scanner.startPosition) {
                  ++this.scanner.currentPosition;
                  this.nextIgnoredToken = -1;
               }
            } catch (InvalidInputException var5) {
               pos = this.scanner.currentPosition;
            } finally {
               this.scanner.lookBack[0] = this.scanner.lookBack[1] = 0;
            }
         } while(this.nextIgnoredToken < 0);

         if (this.nextIgnoredToken == 60 && this.currentToken == 60) {
            return false;
         } else {
            this.lastCheckPoint = this.scanner.currentPosition;
            this.scanner.startPosition = pos;
            this.scanner.currentPosition = pos;
            this.scanner.commentPtr = -1;
            this.scanner.foundTaskCount = 0;
            return true;
         }
      }
   }

   protected MessageSend newMessageSend() {
      MessageSend m = new MessageSend();
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, m.arguments = new Expression[length], 0, length);
      }

      return m;
   }

   protected MessageSend newMessageSendWithTypeArguments() {
      MessageSend m = new MessageSend();
      int length;
      if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
         this.expressionPtr -= length;
         System.arraycopy(this.expressionStack, this.expressionPtr + 1, m.arguments = new Expression[length], 0, length);
      }

      return m;
   }

   protected void optimizedConcatNodeLists() {
      this.astLengthStack[--this.astLengthPtr]++;
   }

   @Override
   public boolean atConflictScenario(int token) {
      if (this.unstackedAct == 16382) {
         return false;
      } else {
         if (token != 37) {
            token = token == 24 ? 50 : 83;
         }

         return this.automatonWillShift(token, this.unstackedAct);
      }
   }

   protected void parse() {
      boolean isDietParse = this.diet;
      int oldFirstToken = this.getFirstToken();
      this.hasError = false;
      this.hasReportedError = false;
      int act = 1580;
      this.unstackedAct = 16382;
      this.stateStackTop = -1;
      this.currentToken = this.getFirstToken();

      try {
         this.scanner.setActiveParser(this);

         label206:
         while(true) {
            int stackLength = this.stack.length;
            if (++this.stateStackTop >= stackLength) {
               System.arraycopy(this.stack, 0, this.stack = new int[stackLength + 255], 0, stackLength);
            }

            this.stack[this.stateStackTop] = act;
            this.unstackedAct = act = tAction(act, this.currentToken);
            if (act == 16382 || this.restartRecovery) {
               int errorPos = this.scanner.currentPosition - 1;
               if (!this.hasReportedError) {
                  this.hasError = true;
               }

               int previousToken = this.currentToken;
               switch(this.resumeOnSyntaxError()) {
                  case 0:
                     act = 16382;
                     break label206;
                  case 1:
                     if (act == 16382 && previousToken != 0) {
                        this.lastErrorEndPosition = errorPos;
                     }

                     act = 1580;
                     this.stateStackTop = -1;
                     this.currentToken = this.getFirstToken();
                     continue;
                  case 2:
                     if (act == 16382) {
                        act = this.stack[this.stateStackTop--];
                        continue;
                     }
               }
            }

            if (act <= 800) {
               --this.stateStackTop;
            } else {
               if (act <= 16382) {
                  if (act >= 16381) {
                     break;
                  }

                  this.consumeToken(this.currentToken);
                  if (this.currentElement != null) {
                     boolean oldValue = this.recordStringLiterals;
                     this.recordStringLiterals = false;
                     this.recoveryTokenCheck();
                     this.recordStringLiterals = oldValue;
                  }

                  try {
                     this.currentToken = this.scanner.getNextToken();
                  } catch (InvalidInputException var12) {
                     if (!this.hasReportedError) {
                        this.problemReporter().scannerError(this, var12.getMessage());
                        this.hasReportedError = true;
                     }

                     this.lastCheckPoint = this.scanner.currentPosition;
                     this.currentToken = 0;
                     this.restartRecovery = true;
                  }

                  if (this.statementRecoveryActivated) {
                     this.jumpOverType();
                  }
                  continue;
               }

               this.consumeToken(this.currentToken);
               if (this.currentElement != null) {
                  boolean oldValue = this.recordStringLiterals;
                  this.recordStringLiterals = false;
                  this.recoveryTokenCheck();
                  this.recordStringLiterals = oldValue;
               }

               try {
                  this.currentToken = this.scanner.getNextToken();
               } catch (InvalidInputException var11) {
                  if (!this.hasReportedError) {
                     this.problemReporter().scannerError(this, var11.getMessage());
                     this.hasReportedError = true;
                  }

                  this.lastCheckPoint = this.scanner.currentPosition;
                  this.currentToken = 0;
                  this.restartRecovery = true;
               }

               if (this.statementRecoveryActivated) {
                  this.jumpOverType();
               }

               act -= 16382;
               this.unstackedAct = act;
            }

            do {
               this.stateStackTop -= rhs[act] - 1;
               this.unstackedAct = ntAction(this.stack[this.stateStackTop], lhs[act]);
               this.consumeRule(act);
               act = this.unstackedAct;
            } while(act > 800);
         }
      } finally {
         this.unstackedAct = 16382;
         this.scanner.setActiveParser(null);
      }

      this.endParse(act);
      NLSTag[] tags = this.scanner.getNLSTags();
      if (tags != null) {
         this.compilationUnit.nlsTags = tags;
      }

      this.scanner.checkNonExternalizedStringLiterals = false;
      if (this.scanner.checkUninternedIdentityComparison) {
         this.compilationUnit.validIdentityComparisonLines = this.scanner.getIdentityComparisonLines();
         this.scanner.checkUninternedIdentityComparison = false;
      }

      if (this.reportSyntaxErrorIsRequired && this.hasError && !this.statementRecoveryActivated) {
         if (!this.options.performStatementsRecovery) {
            this.reportSyntaxErrors(isDietParse, oldFirstToken);
         } else {
            RecoveryScannerData data = this.referenceContext.compilationResult().recoveryScannerData;
            if (this.recoveryScanner == null) {
               this.recoveryScanner = new RecoveryScanner(this.scanner, data);
            } else {
               this.recoveryScanner.setData(data);
            }

            this.recoveryScanner.setSource(this.scanner.source);
            this.recoveryScanner.lineEnds = this.scanner.lineEnds;
            this.recoveryScanner.linePtr = this.scanner.linePtr;
            this.reportSyntaxErrors(isDietParse, oldFirstToken);
            if (data == null) {
               this.referenceContext.compilationResult().recoveryScannerData = this.recoveryScanner.getData();
            }

            if (this.methodRecoveryActivated && this.options.performStatementsRecovery) {
               this.methodRecoveryActivated = false;
               this.recoverStatements();
               this.methodRecoveryActivated = true;
               this.lastAct = 16382;
            }
         }
      }

      this.problemReporter.referenceContext = null;
   }

   public void parse(ConstructorDeclaration cd, CompilationUnitDeclaration unit, boolean recordLineSeparator) {
      boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
      if (this.options.performMethodsFullRecovery) {
         this.methodRecoveryActivated = true;
         this.ignoreNextOpeningBrace = true;
      }

      this.initialize();
      this.goForBlockStatementsopt();
      if (recordLineSeparator) {
         this.scanner.recordLineSeparator = true;
      }

      this.nestedMethod[this.nestedType]++;
      this.pushOnRealBlockStack(0);
      this.referenceContext = cd;
      this.compilationUnit = unit;
      this.scanner.resetTo(cd.bodyStart, cd.bodyEnd);

      try {
         this.parse();
      } catch (AbortCompilation var9) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
         if (this.options.performStatementsRecovery) {
            this.methodRecoveryActivated = oldMethodRecoveryActivated;
         }
      }

      this.checkNonNLSAfterBodyEnd(cd.declarationSourceEnd);
      if (this.lastAct == 16382) {
         cd.bits |= 524288;
         this.initialize();
      } else {
         cd.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
         int length;
         if (this.astLengthPtr > -1 && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
            this.astPtr -= length;
            if (!this.options.ignoreMethodBodies) {
               if (this.astStack[this.astPtr + 1] instanceof ExplicitConstructorCall) {
                  System.arraycopy(this.astStack, this.astPtr + 2, cd.statements = new Statement[length - 1], 0, length - 1);
                  cd.constructorCall = (ExplicitConstructorCall)this.astStack[this.astPtr + 1];
               } else {
                  System.arraycopy(this.astStack, this.astPtr + 1, cd.statements = new Statement[length], 0, length);
                  cd.constructorCall = SuperReference.implicitSuperConstructorCall();
               }
            }
         } else {
            if (!this.options.ignoreMethodBodies) {
               cd.constructorCall = SuperReference.implicitSuperConstructorCall();
            }

            if (!this.containsComment(cd.bodyStart, cd.bodyEnd)) {
               cd.bits |= 8;
            }
         }

         ExplicitConstructorCall explicitConstructorCall = cd.constructorCall;
         if (explicitConstructorCall != null && explicitConstructorCall.sourceEnd == 0) {
            explicitConstructorCall.sourceEnd = cd.sourceEnd;
            explicitConstructorCall.sourceStart = cd.sourceStart;
         }
      }
   }

   public void parse(FieldDeclaration field, TypeDeclaration type, CompilationUnitDeclaration unit, char[] initializationSource) {
      this.initialize();
      this.goForExpression(true);
      this.nestedMethod[this.nestedType]++;
      this.referenceContext = type;
      this.compilationUnit = unit;
      this.scanner.setSource(initializationSource);
      this.scanner.resetTo(0, initializationSource.length - 1);

      try {
         this.parse();
      } catch (AbortCompilation var8) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
      }

      if (this.lastAct == 16382) {
         field.bits |= 524288;
      } else {
         field.initialization = this.expressionStack[this.expressionPtr];
         if ((type.bits & 2) != 0) {
            field.bits |= 2;
         }
      }
   }

   public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
      return this.parse(sourceUnit, compilationResult, -1, -1);
   }

   public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int start, int end) {
      CompilationUnitDeclaration unit;
      try {
         this.initialize(true);
         this.goForCompilationUnit();
         this.referenceContext = this.compilationUnit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);

         char[] contents;
         try {
            contents = this.readManager != null ? this.readManager.getContents(sourceUnit) : sourceUnit.getContents();
         } catch (AbortCompilationUnit var11) {
            this.problemReporter().cannotReadSource(this.compilationUnit, var11, this.options.verbose);
            contents = CharOperation.NO_CHAR;
         }

         this.scanner.setSource(contents);
         this.compilationUnit.sourceEnd = this.scanner.source.length - 1;
         if (end != -1) {
            this.scanner.resetTo(start, end);
         }

         if (this.javadocParser != null && this.javadocParser.checkDocComment) {
            this.javadocParser.scanner.setSource(contents);
            if (end != -1) {
               this.javadocParser.scanner.resetTo(start, end);
            }
         }

         this.parse();
      } finally {
         unit = this.compilationUnit;
         this.compilationUnit = null;
         if (!this.diet) {
            unit.bits |= 16;
         }
      }

      return unit;
   }

   public void parse(Initializer initializer, TypeDeclaration type, CompilationUnitDeclaration unit) {
      boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
      if (this.options.performMethodsFullRecovery) {
         this.methodRecoveryActivated = true;
      }

      this.initialize();
      this.goForBlockStatementsopt();
      this.nestedMethod[this.nestedType]++;
      this.pushOnRealBlockStack(0);
      this.referenceContext = type;
      this.compilationUnit = unit;
      this.scanner.resetTo(initializer.bodyStart, initializer.bodyEnd);

      try {
         this.parse();
      } catch (AbortCompilation var8) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
         if (this.options.performStatementsRecovery) {
            this.methodRecoveryActivated = oldMethodRecoveryActivated;
         }
      }

      this.checkNonNLSAfterBodyEnd(initializer.declarationSourceEnd);
      if (this.lastAct == 16382) {
         initializer.bits |= 524288;
      } else {
         initializer.block.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
         int length;
         if (this.astLengthPtr > -1 && (length = this.astLengthStack[this.astLengthPtr--]) > 0) {
            System.arraycopy(this.astStack, (this.astPtr -= length) + 1, initializer.block.statements = new Statement[length], 0, length);
         } else if (!this.containsComment(initializer.block.sourceStart, initializer.block.sourceEnd)) {
            initializer.block.bits |= 8;
         }

         if ((type.bits & 2) != 0) {
            initializer.bits |= 2;
         }
      }
   }

   public void parse(MethodDeclaration md, CompilationUnitDeclaration unit) {
      if (!md.isAbstract()) {
         if (!md.isNative()) {
            if ((md.modifiers & 16777216) == 0) {
               boolean oldMethodRecoveryActivated = this.methodRecoveryActivated;
               if (this.options.performMethodsFullRecovery) {
                  this.ignoreNextOpeningBrace = true;
                  this.methodRecoveryActivated = true;
                  this.rParenPos = md.sourceEnd;
               }

               this.initialize();
               this.goForBlockStatementsopt();
               this.nestedMethod[this.nestedType]++;
               this.pushOnRealBlockStack(0);
               this.referenceContext = md;
               this.compilationUnit = unit;
               this.scanner.resetTo(md.bodyStart, md.bodyEnd);

               try {
                  this.parse();
               } catch (AbortCompilation var7) {
                  this.lastAct = 16382;
               } finally {
                  this.nestedMethod[this.nestedType]--;
                  if (this.options.performStatementsRecovery) {
                     this.methodRecoveryActivated = oldMethodRecoveryActivated;
                  }
               }

               this.checkNonNLSAfterBodyEnd(md.declarationSourceEnd);
               if (this.lastAct == 16382) {
                  md.bits |= 524288;
               } else {
                  md.explicitDeclarations = this.realBlockStack[this.realBlockPtr--];
                  int length;
                  if (this.astLengthPtr > -1 && (length = this.astLengthStack[this.astLengthPtr--]) != 0) {
                     if (this.options.ignoreMethodBodies) {
                        this.astPtr -= length;
                     } else {
                        System.arraycopy(this.astStack, (this.astPtr -= length) + 1, md.statements = new Statement[length], 0, length);
                     }
                  } else if (!this.containsComment(md.bodyStart, md.bodyEnd)) {
                     md.bits |= 8;
                  }
               }
            }
         }
      }
   }

   public ASTNode[] parseClassBodyDeclarations(char[] source, int offset, int length, CompilationUnitDeclaration unit) {
      boolean oldDiet = this.diet;
      int oldInt = this.dietInt;
      boolean oldTolerateDefaultClassMethods = this.tolerateDefaultClassMethods;
      this.initialize();
      this.goForClassBodyDeclarations();
      this.scanner.setSource(source);
      this.scanner.resetTo(offset, offset + length - 1);
      if (this.javadocParser != null && this.javadocParser.checkDocComment) {
         this.javadocParser.scanner.setSource(source);
         this.javadocParser.scanner.resetTo(offset, offset + length - 1);
      }

      this.nestedType = 1;
      TypeDeclaration referenceContextTypeDeclaration = new TypeDeclaration(unit.compilationResult);
      referenceContextTypeDeclaration.name = Util.EMPTY_STRING.toCharArray();
      referenceContextTypeDeclaration.fields = new FieldDeclaration[0];
      this.compilationUnit = unit;
      unit.types = new TypeDeclaration[1];
      unit.types[0] = referenceContextTypeDeclaration;
      this.referenceContext = unit;

      try {
         this.diet = true;
         this.dietInt = 0;
         this.tolerateDefaultClassMethods = this.parsingJava8Plus;
         this.parse();
      } catch (AbortCompilation var20) {
         this.lastAct = 16382;
      } finally {
         this.diet = oldDiet;
         this.dietInt = oldInt;
         this.tolerateDefaultClassMethods = oldTolerateDefaultClassMethods;
      }

      ASTNode[] result = null;
      if (this.lastAct == 16382) {
         if (!this.options.performMethodsFullRecovery && !this.options.performStatementsRecovery) {
            return null;
         }

         final List bodyDeclarations = new ArrayList();
         ASTVisitor visitor = new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
               if (!methodDeclaration.isDefaultConstructor()) {
                  bodyDeclarations.add(methodDeclaration);
               }

               return false;
            }

            @Override
            public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
               bodyDeclarations.add(fieldDeclaration);
               return false;
            }

            @Override
            public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
               bodyDeclarations.add(memberTypeDeclaration);
               return false;
            }
         };
         unit.ignoreFurtherInvestigation = false;
         unit.traverse(visitor, unit.scope);
         unit.ignoreFurtherInvestigation = true;
         result = bodyDeclarations.toArray(new ASTNode[bodyDeclarations.size()]);
      } else {
         int astLength;
         if (this.astLengthPtr > -1 && (astLength = this.astLengthStack[this.astLengthPtr--]) != 0) {
            result = new ASTNode[astLength];
            this.astPtr -= astLength;
            System.arraycopy(this.astStack, this.astPtr + 1, result, 0, astLength);
         } else {
            result = new ASTNode[0];
         }
      }

      boolean containsInitializers = false;
      TypeDeclaration typeDeclaration = null;
      int i = 0;

      for(int max = result.length; i < max; ++i) {
         ASTNode node = result[i];
         if (node instanceof TypeDeclaration) {
            ((TypeDeclaration)node).parseMethods(this, unit);
         } else if (node instanceof AbstractMethodDeclaration) {
            ((AbstractMethodDeclaration)node).parseStatements(this, unit);
         } else if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration)node;
            switch(fieldDeclaration.getKind()) {
               case 2:
                  containsInitializers = true;
                  if (typeDeclaration == null) {
                     typeDeclaration = referenceContextTypeDeclaration;
                  }

                  if (typeDeclaration.fields == null) {
                     typeDeclaration.fields = new FieldDeclaration[1];
                     typeDeclaration.fields[0] = fieldDeclaration;
                  } else {
                     int length2 = typeDeclaration.fields.length;
                     FieldDeclaration[] temp = new FieldDeclaration[length2 + 1];
                     System.arraycopy(typeDeclaration.fields, 0, temp, 0, length2);
                     temp[length2] = fieldDeclaration;
                     typeDeclaration.fields = temp;
                  }
            }
         }

         if ((node.bits & 524288) != 0 && !this.options.performMethodsFullRecovery && !this.options.performStatementsRecovery) {
            return null;
         }
      }

      if (containsInitializers) {
         FieldDeclaration[] fieldDeclarations = typeDeclaration.fields;
         int ix = 0;

         for(int max = fieldDeclarations.length; ix < max; ++ix) {
            Initializer initializer = (Initializer)fieldDeclarations[ix];
            initializer.parseStatements(this, typeDeclaration, unit);
            if ((initializer.bits & 524288) != 0 && !this.options.performMethodsFullRecovery && !this.options.performStatementsRecovery) {
               return null;
            }
         }
      }

      return result;
   }

   public Expression parseLambdaExpression(char[] source, int offset, int length, CompilationUnitDeclaration unit, boolean recordLineSeparators) {
      this.haltOnSyntaxError = true;
      this.reparsingLambdaExpression = true;
      return this.parseExpression(source, offset, length, unit, recordLineSeparators);
   }

   public Expression parseExpression(char[] source, int offset, int length, CompilationUnitDeclaration unit, boolean recordLineSeparators) {
      this.initialize();
      this.goForExpression(recordLineSeparators);
      this.nestedMethod[this.nestedType]++;
      this.referenceContext = unit;
      this.compilationUnit = unit;
      this.scanner.setSource(source);
      this.scanner.resetTo(offset, offset + length - 1);

      try {
         this.parse();
      } catch (AbortCompilation var9) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
      }

      return this.lastAct == 16382 ? null : this.expressionStack[this.expressionPtr];
   }

   public Expression parseMemberValue(char[] source, int offset, int length, CompilationUnitDeclaration unit) {
      this.initialize();
      this.goForMemberValue();
      this.nestedMethod[this.nestedType]++;
      this.referenceContext = unit;
      this.compilationUnit = unit;
      this.scanner.setSource(source);
      this.scanner.resetTo(offset, offset + length - 1);

      try {
         this.parse();
      } catch (AbortCompilation var8) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
      }

      return this.lastAct == 16382 ? null : this.expressionStack[this.expressionPtr];
   }

   public void parseStatements(ReferenceContext rc, int start, int end, TypeDeclaration[] types, CompilationUnitDeclaration unit) {
      boolean oldStatementRecoveryEnabled = this.statementRecoveryActivated;
      this.statementRecoveryActivated = true;
      this.initialize();
      this.goForBlockStatementsopt();
      this.nestedMethod[this.nestedType]++;
      this.pushOnRealBlockStack(0);
      this.pushOnAstLengthStack(0);
      this.referenceContext = rc;
      this.compilationUnit = unit;
      this.pendingRecoveredType = null;
      if (types != null && types.length > 0) {
         this.recoveredTypes = types;
         this.recoveredTypePtr = 0;
         this.nextTypeStart = this.recoveredTypes[0].allocation == null
            ? this.recoveredTypes[0].declarationSourceStart
            : this.recoveredTypes[0].allocation.sourceStart;
      } else {
         this.recoveredTypes = null;
         this.recoveredTypePtr = -1;
         this.nextTypeStart = -1;
      }

      this.scanner.resetTo(start, end);
      this.lastCheckPoint = this.scanner.initialPosition;
      this.stateStackTop = -1;

      try {
         this.parse();
      } catch (AbortCompilation var10) {
         this.lastAct = 16382;
      } finally {
         this.nestedMethod[this.nestedType]--;
         this.recoveredTypes = null;
         this.statementRecoveryActivated = oldStatementRecoveryEnabled;
      }

      this.checkNonNLSAfterBodyEnd(end);
   }

   public void persistLineSeparatorPositions() {
      if (this.scanner.recordLineSeparator) {
         this.compilationUnit.compilationResult.lineSeparatorPositions = this.scanner.getLineEnds();
      }
   }

   protected void prepareForBlockStatements() {
      this.nestedMethod[this.nestedType = 0] = 1;
      this.variablesCounter[this.nestedType] = 0;
      this.realBlockStack[this.realBlockPtr = 1] = 0;
   }

   public ProblemReporter problemReporter() {
      if (this.scanner.recordLineSeparator) {
         this.compilationUnit.compilationResult.lineSeparatorPositions = this.scanner.getLineEnds();
      }

      this.problemReporter.referenceContext = this.referenceContext;
      return this.problemReporter;
   }

   protected void pushIdentifier(char[] identifier, long position) {
      int stackLength = this.identifierStack.length;
      if (++this.identifierPtr >= stackLength) {
         System.arraycopy(this.identifierStack, 0, this.identifierStack = new char[stackLength + 20][], 0, stackLength);
         System.arraycopy(this.identifierPositionStack, 0, this.identifierPositionStack = new long[stackLength + 20], 0, stackLength);
      }

      this.identifierStack[this.identifierPtr] = identifier;
      this.identifierPositionStack[this.identifierPtr] = position;
      stackLength = this.identifierLengthStack.length;
      if (++this.identifierLengthPtr >= stackLength) {
         System.arraycopy(this.identifierLengthStack, 0, this.identifierLengthStack = new int[stackLength + 10], 0, stackLength);
      }

      this.identifierLengthStack[this.identifierLengthPtr] = 1;
      if (this.parsingJava8Plus && identifier.length == 1 && identifier[0] == '_' && !this.processingLambdaParameterList) {
         this.problemReporter().illegalUseOfUnderscoreAsAnIdentifier((int)(position >>> 32), (int)position, false);
      }
   }

   protected void pushIdentifier() {
      this.pushIdentifier(this.scanner.getCurrentIdentifierSource(), ((long)this.scanner.startPosition << 32) + (long)(this.scanner.currentPosition - 1));
   }

   protected void pushIdentifier(int flag) {
      int stackLength = this.identifierLengthStack.length;
      if (++this.identifierLengthPtr >= stackLength) {
         System.arraycopy(this.identifierLengthStack, 0, this.identifierLengthStack = new int[stackLength + 10], 0, stackLength);
      }

      this.identifierLengthStack[this.identifierLengthPtr] = flag;
   }

   protected void pushOnAstLengthStack(int pos) {
      int stackLength = this.astLengthStack.length;
      if (++this.astLengthPtr >= stackLength) {
         System.arraycopy(this.astLengthStack, 0, this.astLengthStack = new int[stackLength + 255], 0, stackLength);
      }

      this.astLengthStack[this.astLengthPtr] = pos;
   }

   protected void pushOnAstStack(ASTNode node) {
      int stackLength = this.astStack.length;
      if (++this.astPtr >= stackLength) {
         System.arraycopy(this.astStack, 0, this.astStack = new ASTNode[stackLength + 100], 0, stackLength);
         this.astPtr = stackLength;
      }

      this.astStack[this.astPtr] = node;
      stackLength = this.astLengthStack.length;
      if (++this.astLengthPtr >= stackLength) {
         System.arraycopy(this.astLengthStack, 0, this.astLengthStack = new int[stackLength + 100], 0, stackLength);
      }

      this.astLengthStack[this.astLengthPtr] = 1;
   }

   protected void pushOnTypeAnnotationStack(Annotation annotation) {
      int stackLength = this.typeAnnotationStack.length;
      if (++this.typeAnnotationPtr >= stackLength) {
         System.arraycopy(this.typeAnnotationStack, 0, this.typeAnnotationStack = new Annotation[stackLength + 100], 0, stackLength);
      }

      this.typeAnnotationStack[this.typeAnnotationPtr] = annotation;
      stackLength = this.typeAnnotationLengthStack.length;
      if (++this.typeAnnotationLengthPtr >= stackLength) {
         System.arraycopy(this.typeAnnotationLengthStack, 0, this.typeAnnotationLengthStack = new int[stackLength + 100], 0, stackLength);
      }

      this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr] = 1;
   }

   protected void pushOnTypeAnnotationLengthStack(int pos) {
      int stackLength = this.typeAnnotationLengthStack.length;
      if (++this.typeAnnotationLengthPtr >= stackLength) {
         System.arraycopy(this.typeAnnotationLengthStack, 0, this.typeAnnotationLengthStack = new int[stackLength + 100], 0, stackLength);
      }

      this.typeAnnotationLengthStack[this.typeAnnotationLengthPtr] = pos;
   }

   protected void pushOnExpressionStack(Expression expr) {
      int stackLength = this.expressionStack.length;
      if (++this.expressionPtr >= stackLength) {
         System.arraycopy(this.expressionStack, 0, this.expressionStack = new Expression[stackLength + 100], 0, stackLength);
      }

      this.expressionStack[this.expressionPtr] = expr;
      stackLength = this.expressionLengthStack.length;
      if (++this.expressionLengthPtr >= stackLength) {
         System.arraycopy(this.expressionLengthStack, 0, this.expressionLengthStack = new int[stackLength + 100], 0, stackLength);
      }

      this.expressionLengthStack[this.expressionLengthPtr] = 1;
   }

   protected void pushOnExpressionStackLengthStack(int pos) {
      int stackLength = this.expressionLengthStack.length;
      if (++this.expressionLengthPtr >= stackLength) {
         System.arraycopy(this.expressionLengthStack, 0, this.expressionLengthStack = new int[stackLength + 255], 0, stackLength);
      }

      this.expressionLengthStack[this.expressionLengthPtr] = pos;
   }

   protected void pushOnGenericsIdentifiersLengthStack(int pos) {
      int stackLength = this.genericsIdentifiersLengthStack.length;
      if (++this.genericsIdentifiersLengthPtr >= stackLength) {
         System.arraycopy(this.genericsIdentifiersLengthStack, 0, this.genericsIdentifiersLengthStack = new int[stackLength + 10], 0, stackLength);
      }

      this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr] = pos;
   }

   protected void pushOnGenericsLengthStack(int pos) {
      int stackLength = this.genericsLengthStack.length;
      if (++this.genericsLengthPtr >= stackLength) {
         System.arraycopy(this.genericsLengthStack, 0, this.genericsLengthStack = new int[stackLength + 10], 0, stackLength);
      }

      this.genericsLengthStack[this.genericsLengthPtr] = pos;
   }

   protected void pushOnGenericsStack(ASTNode node) {
      int stackLength = this.genericsStack.length;
      if (++this.genericsPtr >= stackLength) {
         System.arraycopy(this.genericsStack, 0, this.genericsStack = new ASTNode[stackLength + 10], 0, stackLength);
      }

      this.genericsStack[this.genericsPtr] = node;
      stackLength = this.genericsLengthStack.length;
      if (++this.genericsLengthPtr >= stackLength) {
         System.arraycopy(this.genericsLengthStack, 0, this.genericsLengthStack = new int[stackLength + 10], 0, stackLength);
      }

      this.genericsLengthStack[this.genericsLengthPtr] = 1;
   }

   protected void pushOnIntStack(int pos) {
      int stackLength = this.intStack.length;
      if (++this.intPtr >= stackLength) {
         System.arraycopy(this.intStack, 0, this.intStack = new int[stackLength + 255], 0, stackLength);
      }

      this.intStack[this.intPtr] = pos;
   }

   protected void pushOnRealBlockStack(int i) {
      int stackLength = this.realBlockStack.length;
      if (++this.realBlockPtr >= stackLength) {
         System.arraycopy(this.realBlockStack, 0, this.realBlockStack = new int[stackLength + 255], 0, stackLength);
      }

      this.realBlockStack[this.realBlockPtr] = i;
   }

   protected void recoverStatements() {
      class MethodVisitor extends ASTVisitor {
         public ASTVisitor typeVisitor;
         TypeDeclaration enclosingType;
         TypeDeclaration[] types = new TypeDeclaration[0];
         int typePtr = -1;

         @Override
         public void endVisit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
            this.endVisitMethod(constructorDeclaration, scope);
         }

         @Override
         public void endVisit(Initializer initializer, MethodScope scope) {
            if (initializer.block != null) {
               TypeDeclaration[] foundTypes = null;
               int length = 0;
               if (this.typePtr > -1) {
                  length = this.typePtr + 1;
                  foundTypes = new TypeDeclaration[length];
                  System.arraycopy(this.types, 0, foundTypes, 0, length);
               }

               ReferenceContext oldContext = Parser.this.referenceContext;
               Parser.this.recoveryScanner.resetTo(initializer.bodyStart, initializer.bodyEnd);
               Scanner oldScanner = Parser.this.scanner;
               Parser.this.scanner = Parser.this.recoveryScanner;
               Parser.this.parseStatements(this.enclosingType, initializer.bodyStart, initializer.bodyEnd, foundTypes, Parser.this.compilationUnit);
               Parser.this.scanner = oldScanner;
               Parser.this.referenceContext = oldContext;

               for(int i = 0; i < length; ++i) {
                  foundTypes[i].traverse(this.typeVisitor, scope);
               }
            }
         }

         @Override
         public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
            this.endVisitMethod(methodDeclaration, scope);
         }

         private void endVisitMethod(AbstractMethodDeclaration methodDeclaration, ClassScope scope) {
            TypeDeclaration[] foundTypes = null;
            int length = 0;
            if (this.typePtr > -1) {
               length = this.typePtr + 1;
               foundTypes = new TypeDeclaration[length];
               System.arraycopy(this.types, 0, foundTypes, 0, length);
            }

            ReferenceContext oldContext = Parser.this.referenceContext;
            Parser.this.recoveryScanner.resetTo(methodDeclaration.bodyStart, methodDeclaration.bodyEnd);
            Scanner oldScanner = Parser.this.scanner;
            Parser.this.scanner = Parser.this.recoveryScanner;
            Parser.this.parseStatements(methodDeclaration, methodDeclaration.bodyStart, methodDeclaration.bodyEnd, foundTypes, Parser.this.compilationUnit);
            Parser.this.scanner = oldScanner;
            Parser.this.referenceContext = oldContext;

            for(int i = 0; i < length; ++i) {
               foundTypes[i].traverse(this.typeVisitor, scope);
            }
         }

         @Override
         public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
            this.typePtr = -1;
            return true;
         }

         @Override
         public boolean visit(Initializer initializer, MethodScope scope) {
            this.typePtr = -1;
            return initializer.block != null;
         }

         @Override
         public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
            this.typePtr = -1;
            return true;
         }

         private boolean visit(TypeDeclaration typeDeclaration) {
            if (this.types.length <= ++this.typePtr) {
               int length = this.typePtr;
               System.arraycopy(this.types, 0, this.types = new TypeDeclaration[length * 2 + 1], 0, length);
            }

            this.types[this.typePtr] = typeDeclaration;
            return false;
         }

         @Override
         public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
            return this.visit(typeDeclaration);
         }

         @Override
         public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
            return this.visit(typeDeclaration);
         }
      }

      MethodVisitor methodVisitor = new MethodVisitor();

      class TypeVisitor extends ASTVisitor {
         public MethodVisitor methodVisitor;
         TypeDeclaration[] types = new TypeDeclaration[0];
         int typePtr = -1;

         @Override
         public void endVisit(TypeDeclaration typeDeclaration, BlockScope scope) {
            this.endVisitType();
         }

         @Override
         public void endVisit(TypeDeclaration typeDeclaration, ClassScope scope) {
            this.endVisitType();
         }

         private void endVisitType() {
            --this.typePtr;
         }

         @Override
         public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
            if (constructorDeclaration.isDefaultConstructor()) {
               return false;
            } else {
               constructorDeclaration.traverse(this.methodVisitor, scope);
               return false;
            }
         }

         @Override
         public boolean visit(Initializer initializer, MethodScope scope) {
            if (initializer.block == null) {
               return false;
            } else {
               this.methodVisitor.enclosingType = this.types[this.typePtr];
               initializer.traverse(this.methodVisitor, scope);
               return false;
            }
         }

         @Override
         public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
            methodDeclaration.traverse(this.methodVisitor, scope);
            return false;
         }

         private boolean visit(TypeDeclaration typeDeclaration) {
            if (this.types.length <= ++this.typePtr) {
               int length = this.typePtr;
               System.arraycopy(this.types, 0, this.types = new TypeDeclaration[length * 2 + 1], 0, length);
            }

            this.types[this.typePtr] = typeDeclaration;
            return true;
         }

         @Override
         public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
            return this.visit(typeDeclaration);
         }

         @Override
         public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
            return this.visit(typeDeclaration);
         }
      }

      TypeVisitor typeVisitor = new TypeVisitor();
      methodVisitor.typeVisitor = typeVisitor;
      typeVisitor.methodVisitor = methodVisitor;
      if (this.referenceContext instanceof AbstractMethodDeclaration) {
         ((AbstractMethodDeclaration)this.referenceContext).traverse(methodVisitor, null);
      } else if (this.referenceContext instanceof TypeDeclaration) {
         TypeDeclaration typeContext = (TypeDeclaration)this.referenceContext;
         int length = typeContext.fields.length;
         int i = 0;

         while(i < length) {
            FieldDeclaration fieldDeclaration = typeContext.fields[i];
            switch(fieldDeclaration.getKind()) {
               case 2:
                  Initializer initializer = (Initializer)fieldDeclaration;
                  if (initializer.block != null) {
                     methodVisitor.enclosingType = typeContext;
                     initializer.traverse(methodVisitor, null);
                  }
               default:
                  ++i;
            }
         }
      }
   }

   public void recoveryExitFromVariable() {
      if (this.currentElement != null && this.currentElement.parent != null) {
         if (this.currentElement instanceof RecoveredLocalVariable) {
            int end = ((RecoveredLocalVariable)this.currentElement).localDeclaration.sourceEnd;
            this.currentElement.updateSourceEndIfNecessary(end);
            this.currentElement = this.currentElement.parent;
         } else if (this.currentElement instanceof RecoveredField
            && !(this.currentElement instanceof RecoveredInitializer)
            && this.currentElement.bracketBalance <= 0) {
            int end = ((RecoveredField)this.currentElement).fieldDeclaration.sourceEnd;
            this.currentElement.updateSourceEndIfNecessary(end);
            this.currentElement = this.currentElement.parent;
         }
      }
   }

   public void recoveryTokenCheck() {
      switch(this.currentToken) {
         case 28:
            this.endStatementPosition = this.scanner.currentPosition - 1;
            this.endPosition = this.scanner.startPosition - 1;
            RecoveredType currentType = this.currentRecoveryType();
            if (currentType != null) {
               currentType.insideEnumConstantPart = false;
            }
         default:
            if (this.rBraceEnd > this.rBraceSuccessorStart && this.scanner.currentPosition != this.scanner.startPosition) {
               this.rBraceSuccessorStart = this.scanner.startPosition;
            }
            break;
         case 32:
            if (this.ignoreNextClosingBrace) {
               this.ignoreNextClosingBrace = false;
            } else {
               this.rBraceStart = this.scanner.startPosition - 1;
               this.rBraceEnd = this.scanner.currentPosition - 1;
               this.endPosition = this.flushCommentsDefinedPriorTo(this.rBraceEnd);
               RecoveredElement newElement = this.currentElement.updateOnClosingBrace(this.scanner.startPosition, this.rBraceEnd);
               this.lastCheckPoint = this.scanner.currentPosition;
               if (newElement != this.currentElement) {
                  this.currentElement = newElement;
               }
            }
            break;
         case 48:
            if (this.recordStringLiterals
               && this.checkExternalizeStrings
               && this.lastPosistion < this.scanner.currentPosition
               && !this.statementRecoveryActivated) {
               StringLiteral stringLiteral = this.createStringLiteral(
                  this.scanner.getCurrentTokenSourceString(),
                  this.scanner.startPosition,
                  this.scanner.currentPosition - 1,
                  Util.getLineNumber(this.scanner.startPosition, this.scanner.lineEnds, 0, this.scanner.linePtr)
               );
               this.compilationUnit.recordStringLiteral(stringLiteral, this.currentElement != null);
            }
            break;
         case 49:
            RecoveredElement newElement = null;
            if (!this.ignoreNextOpeningBrace) {
               newElement = this.currentElement.updateOnOpeningBrace(this.scanner.startPosition - 1, this.scanner.currentPosition - 1);
            }

            this.lastCheckPoint = this.scanner.currentPosition;
            if (newElement != null) {
               this.restartRecovery = true;
               this.currentElement = newElement;
            }
      }

      this.ignoreNextOpeningBrace = false;
   }

   protected void reportSyntaxErrors(boolean isDietParse, int oldFirstToken) {
      if (this.referenceContext instanceof MethodDeclaration) {
         MethodDeclaration methodDeclaration = (MethodDeclaration)this.referenceContext;
         if ((methodDeclaration.bits & 32) != 0) {
            return;
         }
      }

      this.compilationUnit.compilationResult.lineSeparatorPositions = this.scanner.getLineEnds();
      this.scanner.recordLineSeparator = false;
      int start = this.scanner.initialPosition;
      int end = this.scanner.eofPosition == Integer.MAX_VALUE ? this.scanner.eofPosition : this.scanner.eofPosition - 1;
      if (isDietParse) {
         TypeDeclaration[] types = this.compilationUnit.types;
         int[][] intervalToSkip = RangeUtil.computeDietRange(types);
         DiagnoseParser diagnoseParser = new DiagnoseParser(
            this, oldFirstToken, start, end, intervalToSkip[0], intervalToSkip[1], intervalToSkip[2], this.options
         );
         diagnoseParser.diagnoseParse(false);
         this.reportSyntaxErrorsForSkippedMethod(types);
         this.scanner.resetTo(start, end);
      } else {
         DiagnoseParser diagnoseParser = new DiagnoseParser(this, oldFirstToken, start, end, this.options);
         diagnoseParser.diagnoseParse(this.options.performStatementsRecovery);
      }
   }

   private void reportSyntaxErrorsForSkippedMethod(TypeDeclaration[] types) {
      if (types != null) {
         for(int i = 0; i < types.length; ++i) {
            TypeDeclaration[] memberTypes = types[i].memberTypes;
            if (memberTypes != null) {
               this.reportSyntaxErrorsForSkippedMethod(memberTypes);
            }

            AbstractMethodDeclaration[] methods = types[i].methods;
            if (methods != null) {
               for(int j = 0; j < methods.length; ++j) {
                  AbstractMethodDeclaration method = methods[j];
                  if ((method.bits & 32) != 0) {
                     if (method.isAnnotationMethod()) {
                        DiagnoseParser diagnoseParser = new DiagnoseParser(this, 29, method.declarationSourceStart, method.declarationSourceEnd, this.options);
                        diagnoseParser.diagnoseParse(this.options.performStatementsRecovery);
                     } else {
                        DiagnoseParser diagnoseParser = new DiagnoseParser(this, 9, method.declarationSourceStart, method.declarationSourceEnd, this.options);
                        diagnoseParser.diagnoseParse(this.options.performStatementsRecovery);
                     }
                  }
               }
            }

            FieldDeclaration[] fields = types[i].fields;
            if (fields != null) {
               int length = fields.length;

               for(int j = 0; j < length; ++j) {
                  if (fields[j] instanceof Initializer) {
                     Initializer initializer = (Initializer)fields[j];
                     if ((initializer.bits & 32) != 0) {
                        DiagnoseParser diagnoseParser = new DiagnoseParser(
                           this, 14, initializer.declarationSourceStart, initializer.declarationSourceEnd, this.options
                        );
                        diagnoseParser.diagnoseParse(this.options.performStatementsRecovery);
                     }
                  }
               }
            }
         }
      }
   }

   protected void resetModifiers() {
      this.modifiers = 0;
      this.modifiersSourceStart = -1;
      this.scanner.commentPtr = -1;
   }

   protected void resetStacks() {
      this.astPtr = -1;
      this.astLengthPtr = -1;
      this.expressionPtr = -1;
      this.expressionLengthPtr = -1;
      this.typeAnnotationLengthPtr = -1;
      this.typeAnnotationPtr = -1;
      this.identifierPtr = -1;
      this.identifierLengthPtr = -1;
      this.intPtr = -1;
      this.nestedMethod[this.nestedType = 0] = 0;
      this.variablesCounter[this.nestedType] = 0;
      this.dimensions = 0;
      this.realBlockStack[this.realBlockPtr = 0] = 0;
      this.recoveredStaticInitializerStart = 0;
      this.listLength = 0;
      this.listTypeParameterLength = 0;
      this.genericsIdentifiersLengthPtr = -1;
      this.genericsLengthPtr = -1;
      this.genericsPtr = -1;
      this.valueLambdaNestDepth = -1;
   }

   protected int resumeAfterRecovery() {
      if (!this.methodRecoveryActivated && !this.statementRecoveryActivated) {
         this.resetStacks();
         this.resetModifiers();
         if (!this.moveRecoveryCheckpoint()) {
            return 0;
         } else if (this.referenceContext instanceof CompilationUnitDeclaration) {
            this.goForHeaders();
            this.diet = true;
            this.dietInt = 0;
            return 1;
         } else {
            return 0;
         }
      } else if (!this.statementRecoveryActivated) {
         this.resetStacks();
         this.resetModifiers();
         if (!this.moveRecoveryCheckpoint()) {
            return 0;
         } else {
            this.goForHeaders();
            return 1;
         }
      } else {
         return 0;
      }
   }

   protected int resumeOnSyntaxError() {
      if (this.haltOnSyntaxError) {
         return 0;
      } else {
         if (this.currentElement == null) {
            this.javadoc = null;
            if (this.statementRecoveryActivated) {
               return 0;
            }

            this.currentElement = this.buildInitialRecoveryState();
         }

         if (this.currentElement == null) {
            return 0;
         } else {
            if (this.restartRecovery) {
               this.restartRecovery = false;
            }

            this.updateRecoveryState();
            if (this.getFirstToken() == 21 && this.referenceContext instanceof CompilationUnitDeclaration) {
               TypeDeclaration typeDeclaration = new TypeDeclaration(this.referenceContext.compilationResult());
               typeDeclaration.name = Util.EMPTY_STRING.toCharArray();
               this.currentElement = this.currentElement.add(typeDeclaration, 0);
            }

            if (this.lastPosistion < this.scanner.currentPosition) {
               this.lastPosistion = this.scanner.currentPosition;
               this.scanner.lastPosition = this.scanner.currentPosition;
            }

            return this.resumeAfterRecovery();
         }
      }
   }

   public void setMethodsFullRecovery(boolean enabled) {
      this.options.performMethodsFullRecovery = enabled;
   }

   public void setStatementsRecovery(boolean enabled) {
      if (enabled) {
         this.options.performMethodsFullRecovery = true;
      }

      this.options.performStatementsRecovery = enabled;
   }

   @Override
   public String toString() {
      String s = "lastCheckpoint : int = " + String.valueOf(this.lastCheckPoint) + "\n";
      s = s + "identifierStack : char[" + (this.identifierPtr + 1) + "][] = {";

      for(int i = 0; i <= this.identifierPtr; ++i) {
         s = s + "\"" + this.identifierStack[i] + "\",";
      }

      s = s + "}\n";
      s = s + "identifierLengthStack : int[" + (this.identifierLengthPtr + 1) + "] = {";

      for(int i = 0; i <= this.identifierLengthPtr; ++i) {
         s = s + this.identifierLengthStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "astLengthStack : int[" + (this.astLengthPtr + 1) + "] = {";

      for(int i = 0; i <= this.astLengthPtr; ++i) {
         s = s + this.astLengthStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "astPtr : int = " + this.astPtr + "\n";
      s = s + "intStack : int[" + (this.intPtr + 1) + "] = {";

      for(int i = 0; i <= this.intPtr; ++i) {
         s = s + this.intStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "expressionLengthStack : int[" + (this.expressionLengthPtr + 1) + "] = {";

      for(int i = 0; i <= this.expressionLengthPtr; ++i) {
         s = s + this.expressionLengthStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "expressionPtr : int = " + this.expressionPtr + "\n";
      s = s + "genericsIdentifiersLengthStack : int[" + (this.genericsIdentifiersLengthPtr + 1) + "] = {";

      for(int i = 0; i <= this.genericsIdentifiersLengthPtr; ++i) {
         s = s + this.genericsIdentifiersLengthStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "genericsLengthStack : int[" + (this.genericsLengthPtr + 1) + "] = {";

      for(int i = 0; i <= this.genericsLengthPtr; ++i) {
         s = s + this.genericsLengthStack[i] + ",";
      }

      s = s + "}\n";
      s = s + "genericsPtr : int = " + this.genericsPtr + "\n";
      return s + "\n\n\n----------------Scanner--------------\n" + this.scanner.toString();
   }

   protected void updateRecoveryState() {
      this.currentElement.updateFromParserState();
      this.recoveryTokenCheck();
   }

   protected void updateSourceDeclarationParts(int variableDeclaratorsCounter) {
      int endTypeDeclarationPosition = -1 + this.astStack[this.astPtr - variableDeclaratorsCounter + 1].sourceStart;

      for(int i = 0; i < variableDeclaratorsCounter - 1; ++i) {
         FieldDeclaration field = (FieldDeclaration)this.astStack[this.astPtr - i - 1];
         field.endPart1Position = endTypeDeclarationPosition;
         field.endPart2Position = -1 + this.astStack[this.astPtr - i].sourceStart;
      }

      FieldDeclaration field;
      (field = (FieldDeclaration)this.astStack[this.astPtr]).endPart1Position = endTypeDeclarationPosition;
      field.endPart2Position = field.declarationSourceEnd;
   }

   protected void updateSourcePosition(Expression exp) {
      exp.sourceEnd = this.intStack[this.intPtr--];
      exp.sourceStart = this.intStack[this.intPtr--];
   }

   public void copyState(Parser from) {
      this.stateStackTop = from.stateStackTop;
      this.unstackedAct = from.unstackedAct;
      this.identifierPtr = from.identifierPtr;
      this.identifierLengthPtr = from.identifierLengthPtr;
      this.astPtr = from.astPtr;
      this.astLengthPtr = from.astLengthPtr;
      this.expressionPtr = from.expressionPtr;
      this.expressionLengthPtr = from.expressionLengthPtr;
      this.genericsPtr = from.genericsPtr;
      this.genericsLengthPtr = from.genericsLengthPtr;
      this.genericsIdentifiersLengthPtr = from.genericsIdentifiersLengthPtr;
      this.typeAnnotationPtr = from.typeAnnotationPtr;
      this.typeAnnotationLengthPtr = from.typeAnnotationLengthPtr;
      this.intPtr = from.intPtr;
      this.nestedType = from.nestedType;
      this.realBlockPtr = from.realBlockPtr;
      this.valueLambdaNestDepth = from.valueLambdaNestDepth;
      int length;
      System.arraycopy(from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
      System.arraycopy(from.identifierStack, 0, this.identifierStack = new char[length = from.identifierStack.length][], 0, length);
      System.arraycopy(from.identifierLengthStack, 0, this.identifierLengthStack = new int[length = from.identifierLengthStack.length], 0, length);
      System.arraycopy(from.identifierPositionStack, 0, this.identifierPositionStack = new long[length = from.identifierPositionStack.length], 0, length);
      System.arraycopy(from.astStack, 0, this.astStack = new ASTNode[length = from.astStack.length], 0, length);
      System.arraycopy(from.astLengthStack, 0, this.astLengthStack = new int[length = from.astLengthStack.length], 0, length);
      System.arraycopy(from.expressionStack, 0, this.expressionStack = new Expression[length = from.expressionStack.length], 0, length);
      System.arraycopy(from.expressionLengthStack, 0, this.expressionLengthStack = new int[length = from.expressionLengthStack.length], 0, length);
      System.arraycopy(from.genericsStack, 0, this.genericsStack = new ASTNode[length = from.genericsStack.length], 0, length);
      System.arraycopy(from.genericsLengthStack, 0, this.genericsLengthStack = new int[length = from.genericsLengthStack.length], 0, length);
      System.arraycopy(
         from.genericsIdentifiersLengthStack, 0, this.genericsIdentifiersLengthStack = new int[length = from.genericsIdentifiersLengthStack.length], 0, length
      );
      System.arraycopy(from.typeAnnotationStack, 0, this.typeAnnotationStack = new Annotation[length = from.typeAnnotationStack.length], 0, length);
      System.arraycopy(from.typeAnnotationLengthStack, 0, this.typeAnnotationLengthStack = new int[length = from.typeAnnotationLengthStack.length], 0, length);
      System.arraycopy(from.intStack, 0, this.intStack = new int[length = from.intStack.length], 0, length);
      System.arraycopy(from.nestedMethod, 0, this.nestedMethod = new int[length = from.nestedMethod.length], 0, length);
      System.arraycopy(from.realBlockStack, 0, this.realBlockStack = new int[length = from.realBlockStack.length], 0, length);
      System.arraycopy(from.stateStackLengthStack, 0, this.stateStackLengthStack = new int[length = from.stateStackLengthStack.length], 0, length);
      System.arraycopy(from.variablesCounter, 0, this.variablesCounter = new int[length = from.variablesCounter.length], 0, length);
      System.arraycopy(from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
      System.arraycopy(from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
      System.arraycopy(from.stack, 0, this.stack = new int[length = from.stack.length], 0, length);
      this.listLength = from.listLength;
      this.listTypeParameterLength = from.listTypeParameterLength;
      this.dimensions = from.dimensions;
      this.recoveredStaticInitializerStart = from.recoveredStaticInitializerStart;
   }

   public int automatonState() {
      return this.stack[this.stateStackTop];
   }

   public boolean automatonWillShift(int token, int lastAction) {
      int stackTop = this.stateStackTop;
      int stackTopState = this.stack[stackTop];
      int highWaterMark = stackTop;
      if (lastAction <= 800) {
         --stackTop;
         lastAction += 16382;
      }

      while(true) {
         if (lastAction > 16382) {
            lastAction -= 16382;

            do {
               stackTop -= rhs[lastAction] - 1;
               if (stackTop < highWaterMark) {
                  highWaterMark = stackTop;
                  stackTopState = this.stack[stackTop];
               }

               lastAction = ntAction(stackTopState, lhs[lastAction]);
            } while(lastAction <= 800);
         }

         highWaterMark = ++stackTop;
         stackTopState = lastAction;
         lastAction = tAction(lastAction, token);
         if (lastAction > 800) {
            if (lastAction != 16382) {
               return true;
            }

            return false;
         }

         --stackTop;
         lastAction += 16382;
      }
   }

   private static enum LocalTypeKind {
      LOCAL,
      METHOD_REFERENCE,
      LAMBDA;
   }
}
