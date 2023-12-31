package org.eclipse.jdt.internal.compiler.batch;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerStats;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.GenericXMLWriter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfInt;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Main implements ProblemSeverities, SuffixConstants {
   private static final String ANNOTATION_SOURCE_CLASSPATH = "CLASSPATH";
   boolean enableJavadocOn;
   boolean warnJavadocOn;
   boolean warnAllJavadocOn;
   public Compiler batchCompiler;
   public ResourceBundle bundle;
   protected FileSystem.Classpath[] checkedClasspaths;
   protected List<String> annotationPaths;
   protected boolean annotationsFromClasspath;
   public Locale compilerLocale;
   public CompilerOptions compilerOptions;
   public CompilationProgress progress;
   public String destinationPath;
   public String[] destinationPaths;
   private boolean didSpecifySource;
   private boolean didSpecifyTarget;
   public String[] encodings;
   public int exportedClassFilesCounter;
   public String[] filenames;
   public String[] classNames;
   public int globalErrorsCount;
   public int globalProblemsCount;
   public int globalTasksCount;
   public int globalWarningsCount;
   private File javaHomeCache;
   private boolean javaHomeChecked = false;
   private boolean primaryNullAnnotationsSeen = false;
   public long lineCount0;
   public String log;
   public Main.Logger logger;
   public int maxProblems;
   public Map<String, String> options;
   public char[][] ignoreOptionalProblemsFromFolders;
   protected PrintWriter out;
   public boolean proceed = true;
   public boolean proceedOnError = false;
   public boolean produceRefInfo = false;
   public int currentRepetition;
   public int maxRepetition;
   public boolean showProgress = false;
   public long startTime;
   public ArrayList pendingErrors;
   public boolean systemExitWhenFinished = true;
   public static final int TIMING_DISABLED = 0;
   public static final int TIMING_ENABLED = 1;
   public static final int TIMING_DETAILED = 2;
   public int timing = 0;
   public CompilerStats[] compilerStats;
   public boolean verbose = false;
   private String[] expandedCommandLine;
   private PrintWriter err;
   protected ArrayList extraProblems;
   public static final String bundleName = "org.eclipse.jdt.internal.compiler.batch.messages";
   public static final int DEFAULT_SIZE_CLASSPATH = 4;
   public static final String NONE = "none";

   /** @deprecated */
   public static boolean compile(String commandLine) {
      return new Main(new PrintWriter(System.out), new PrintWriter(System.err), false, null, null).compile(tokenize(commandLine));
   }

   /** @deprecated */
   public static boolean compile(String commandLine, PrintWriter outWriter, PrintWriter errWriter) {
      return new Main(outWriter, errWriter, false, null, null).compile(tokenize(commandLine));
   }

   public static boolean compile(String[] commandLineArguments, PrintWriter outWriter, PrintWriter errWriter, CompilationProgress progress) {
      return new Main(outWriter, errWriter, false, null, progress).compile(commandLineArguments);
   }

   public static File[][] getLibrariesFiles(File[] files) {
      FilenameFilter filter = new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return Util.isPotentialZipArchive(name);
         }
      };
      int filesLength = files.length;
      File[][] result = new File[filesLength][];

      for(int i = 0; i < filesLength; ++i) {
         File currentFile = files[i];
         if (currentFile.exists() && currentFile.isDirectory()) {
            result[i] = currentFile.listFiles(filter);
         }
      }

      return result;
   }

   public static void main(String[] argv) {
      new Main(new PrintWriter(System.out), new PrintWriter(System.err), true, null, null).compile(argv);
   }

   public static String[] tokenize(String commandLine) {
      int count = 0;
      String[] arguments = new String[10];
      StringTokenizer tokenizer = new StringTokenizer(commandLine, " \"", true);
      String token = Util.EMPTY_STRING;
      boolean insideQuotes = false;
      boolean startNewToken = true;

      while(tokenizer.hasMoreTokens()) {
         token = tokenizer.nextToken();
         if (token.equals(" ")) {
            if (insideQuotes) {
               arguments[count - 1] = arguments[count - 1] + token;
               startNewToken = false;
            } else {
               startNewToken = true;
            }
         } else if (token.equals("\"")) {
            if (!insideQuotes && startNewToken) {
               if (count == arguments.length) {
                  System.arraycopy(arguments, 0, arguments = new String[count * 2], 0, count);
               }

               arguments[count++] = Util.EMPTY_STRING;
            }

            insideQuotes = !insideQuotes;
            startNewToken = false;
         } else {
            if (insideQuotes) {
               arguments[count - 1] = arguments[count - 1] + token;
            } else if (token.length() > 0 && !startNewToken) {
               arguments[count - 1] = arguments[count - 1] + token;
            } else {
               if (count == arguments.length) {
                  System.arraycopy(arguments, 0, arguments = new String[count * 2], 0, count);
               }

               String trimmedToken = token.trim();
               if (trimmedToken.length() != 0) {
                  arguments[count++] = trimmedToken;
               }
            }

            startNewToken = false;
         }
      }

      String[] var8;
      System.arraycopy(arguments, 0, var8 = new String[count], 0, count);
      return var8;
   }

   /** @deprecated */
   public Main(PrintWriter outWriter, PrintWriter errWriter, boolean systemExitWhenFinished) {
      this(outWriter, errWriter, systemExitWhenFinished, null, null);
   }

   /** @deprecated */
   public Main(PrintWriter outWriter, PrintWriter errWriter, boolean systemExitWhenFinished, Map customDefaultOptions) {
      this(outWriter, errWriter, systemExitWhenFinished, customDefaultOptions, null);
   }

   public Main(
      PrintWriter outWriter,
      PrintWriter errWriter,
      boolean systemExitWhenFinished,
      Map<String, String> customDefaultOptions,
      CompilationProgress compilationProgress
   ) {
      this.initialize(outWriter, errWriter, systemExitWhenFinished, customDefaultOptions, compilationProgress);
      this.relocalize();
   }

   public void addExtraProblems(CategorizedProblem problem) {
      if (this.extraProblems == null) {
         this.extraProblems = new ArrayList();
      }

      this.extraProblems.add(problem);
   }

   protected void addNewEntry(
      ArrayList paths,
      String currentClasspathName,
      ArrayList currentRuleSpecs,
      String customEncoding,
      String destPath,
      boolean isSourceOnly,
      boolean rejectDestinationPathOnJars
   ) {
      int rulesSpecsSize = currentRuleSpecs.size();
      AccessRuleSet accessRuleSet = null;
      if (rulesSpecsSize != 0) {
         AccessRule[] accessRules = new AccessRule[currentRuleSpecs.size()];
         boolean rulesOK = true;
         Iterator i = currentRuleSpecs.iterator();
         int j = 0;

         while(i.hasNext()) {
            String ruleSpec = (String)i.next();
            char key = ruleSpec.charAt(0);
            String pattern = ruleSpec.substring(1);
            if (pattern.length() > 0) {
               switch(key) {
                  case '+':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 0);
                     break;
                  case '-':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777523);
                     break;
                  case '?':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777523, true);
                     break;
                  case '~':
                     accessRules[j++] = new AccessRule(pattern.toCharArray(), 16777496);
                     break;
                  default:
                     rulesOK = false;
               }
            } else {
               rulesOK = false;
            }
         }

         if (!rulesOK) {
            if (currentClasspathName.length() != 0) {
               this.addPendingErrors(this.bind("configure.incorrectClasspath", currentClasspathName));
            }

            return;
         }

         accessRuleSet = new AccessRuleSet(accessRules, (byte)0, currentClasspathName);
      }

      if ("none".equals(destPath)) {
         destPath = "none";
      }

      if (rejectDestinationPathOnJars && destPath != null && Util.isPotentialZipArchive(currentClasspathName)) {
         throw new IllegalArgumentException(this.bind("configure.unexpectedDestinationPathEntryFile", currentClasspathName));
      } else {
         FileSystem.Classpath currentClasspath = FileSystem.getClasspath(
            currentClasspathName, customEncoding, isSourceOnly, accessRuleSet, destPath, this.options
         );
         if (currentClasspath != null) {
            paths.add(currentClasspath);
         } else if (currentClasspathName.length() != 0) {
            this.addPendingErrors(this.bind("configure.incorrectClasspath", currentClasspathName));
         }
      }
   }

   void addPendingErrors(String message) {
      if (this.pendingErrors == null) {
         this.pendingErrors = new ArrayList();
      }

      this.pendingErrors.add(message);
   }

   public String bind(String id) {
      return this.bind(id, null);
   }

   public String bind(String id, String binding) {
      return this.bind(id, new String[]{binding});
   }

   public String bind(String id, String binding1, String binding2) {
      return this.bind(id, new String[]{binding1, binding2});
   }

   public String bind(String id, String[] arguments) {
      if (id == null) {
         return "No message available";
      } else {
         String message = null;

         try {
            message = this.bundle.getString(id);
         } catch (MissingResourceException var4) {
            return "Missing message: " + id + " in: " + "org.eclipse.jdt.internal.compiler.batch.messages";
         }

         return MessageFormat.format(message, arguments);
      }
   }

   private boolean checkVMVersion(long minimalSupportedVersion) {
      String classFileVersion = System.getProperty("java.class.version");
      if (classFileVersion == null) {
         return false;
      } else {
         int index = classFileVersion.indexOf(46);
         if (index == -1) {
            return false;
         } else {
            int majorVersion;
            try {
               majorVersion = Integer.parseInt(classFileVersion.substring(0, index));
            } catch (NumberFormatException var6) {
               return false;
            }

            switch(majorVersion) {
               case 45:
                  if (2949123L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 46:
                  if (3014656L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 47:
                  if (3080192L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 48:
                  if (3145728L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 49:
                  if (3211264L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 50:
                  if (3276800L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 51:
                  if (3342336L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               case 52:
                  if (3407872L >= minimalSupportedVersion) {
                     return true;
                  }

                  return false;
               default:
                  return false;
            }
         }
      }
   }

   public boolean compile(String[] argv) {
      try {
         this.configure(argv);
         if (this.progress != null) {
            this.progress.begin(this.filenames == null ? 0 : this.filenames.length * this.maxRepetition);
         }

         if (this.proceed) {
            if (this.showProgress) {
               this.logger.compiling();
            }

            for(this.currentRepetition = 0; this.currentRepetition < this.maxRepetition; ++this.currentRepetition) {
               this.globalProblemsCount = 0;
               this.globalErrorsCount = 0;
               this.globalWarningsCount = 0;
               this.globalTasksCount = 0;
               this.exportedClassFilesCounter = 0;
               if (this.maxRepetition > 1) {
                  this.logger.flush();
                  this.logger.logRepetition(this.currentRepetition, this.maxRepetition);
               }

               this.performCompilation();
            }

            if (this.compilerStats != null) {
               this.logger.logAverage();
            }

            if (this.showProgress) {
               this.logger.printNewLine();
            }
         }

         if (this.systemExitWhenFinished) {
            this.logger.flush();
            this.logger.close();
            System.exit(this.globalErrorsCount > 0 ? -1 : 0);
         }

         return this.globalErrorsCount == 0 && (this.progress == null || !this.progress.isCanceled());
      } catch (IllegalArgumentException var7) {
         this.logger.logException(var7);
         if (this.systemExitWhenFinished) {
            this.logger.flush();
            this.logger.close();
            System.exit(-1);
         }

         return false;
      } catch (RuntimeException var8) {
         this.logger.logException(var8);
         if (this.systemExitWhenFinished) {
            this.logger.flush();
            this.logger.close();
            System.exit(-1);
         }
      } finally {
         this.logger.flush();
         this.logger.close();
         if (this.progress != null) {
            this.progress.done();
         }
      }

      return false;
   }

   public void configure(String[] argv) {
      if (argv != null && argv.length != 0) {
         ArrayList bootclasspaths = new ArrayList(4);
         String sourcepathClasspathArg = null;
         ArrayList sourcepathClasspaths = new ArrayList(4);
         ArrayList classpaths = new ArrayList(4);
         ArrayList extdirsClasspaths = null;
         ArrayList endorsedDirClasspaths = null;
         this.annotationPaths = null;
         this.annotationsFromClasspath = false;
         int index = -1;
         int filesCount = 0;
         int classCount = 0;
         int argCount = argv.length;
         int mode = 0;
         this.maxRepetition = 0;
         boolean printUsageRequired = false;
         String usageSection = null;
         boolean printVersionRequired = false;
         boolean didSpecifyDeprecation = false;
         boolean didSpecifyCompliance = false;
         boolean didSpecifyDisabledAnnotationProcessing = false;
         String customEncoding = null;
         String customDestinationPath = null;
         String currentSourceDirectory = null;
         String currentArg = Util.EMPTY_STRING;
         Set specifiedEncodings = null;
         boolean needExpansion = false;

         for(int i = 0; i < argCount; ++i) {
            if (argv[i].startsWith("@")) {
               needExpansion = true;
               break;
            }
         }

         String[] newCommandLineArgs = null;
         if (needExpansion) {
            newCommandLineArgs = new String[argCount];
            index = 0;

            for(int i = 0; i < argCount; ++i) {
               String[] newArgs = null;
               String arg = argv[i].trim();
               if (arg.startsWith("@")) {
                  try {
                     LineNumberReader reader = new LineNumberReader(new StringReader(new String(Util.getFileCharContent(new File(arg.substring(1)), null))));
                     StringBuffer buffer = new StringBuffer();

                     String line;
                     while((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.startsWith("#")) {
                           buffer.append(line).append(" ");
                        }
                     }

                     newArgs = tokenize(buffer.toString());
                  } catch (IOException var37) {
                     throw new IllegalArgumentException(this.bind("configure.invalidexpansionargumentname", arg));
                  }
               }

               if (newArgs != null) {
                  int newCommandLineArgsLength = newCommandLineArgs.length;
                  int newArgsLength = newArgs.length;
                  System.arraycopy(newCommandLineArgs, 0, newCommandLineArgs = new String[newCommandLineArgsLength + newArgsLength - 1], 0, index);
                  System.arraycopy(newArgs, 0, newCommandLineArgs, index, newArgsLength);
                  index += newArgsLength;
               } else {
                  newCommandLineArgs[index++] = arg;
               }
            }

            index = -1;
         } else {
            newCommandLineArgs = argv;

            for(int i = 0; i < argCount; ++i) {
               newCommandLineArgs[i] = newCommandLineArgs[i].trim();
            }
         }

         argCount = newCommandLineArgs.length;
         this.expandedCommandLine = newCommandLineArgs;

         label978:
         while(++index < argCount) {
            if (customEncoding != null) {
               throw new IllegalArgumentException(this.bind("configure.unexpectedCustomEncoding", currentArg, customEncoding));
            }

            currentArg = newCommandLineArgs[index];
            switch(mode) {
               case 0:
                  if (currentArg.startsWith("-nowarn")) {
                     switch(currentArg.length()) {
                        case 7:
                           this.disableAll(0);
                           break;
                        case 8:
                           throw new IllegalArgumentException(this.bind("configure.invalidNowarnOption", currentArg));
                        default:
                           int foldersStart = currentArg.indexOf(91) + 1;
                           int foldersEnd = currentArg.lastIndexOf(93);
                           if (foldersStart <= 8 || foldersEnd == -1 || foldersStart > foldersEnd || foldersEnd < currentArg.length() - 1) {
                              throw new IllegalArgumentException(this.bind("configure.invalidNowarnOption", currentArg));
                           }

                           String folders = currentArg.substring(foldersStart, foldersEnd);
                           if (folders.length() <= 0) {
                              throw new IllegalArgumentException(this.bind("configure.invalidNowarnOption", currentArg));
                           }

                           char[][] currentFolders = decodeIgnoreOptionalProblemsFromFolders(folders);
                           if (this.ignoreOptionalProblemsFromFolders != null) {
                              int length = this.ignoreOptionalProblemsFromFolders.length + currentFolders.length;
                              char[][] tempFolders = new char[length][];
                              System.arraycopy(this.ignoreOptionalProblemsFromFolders, 0, tempFolders, 0, this.ignoreOptionalProblemsFromFolders.length);
                              System.arraycopy(currentFolders, 0, tempFolders, this.ignoreOptionalProblemsFromFolders.length, currentFolders.length);
                              this.ignoreOptionalProblemsFromFolders = tempFolders;
                           } else {
                              this.ignoreOptionalProblemsFromFolders = currentFolders;
                           }
                     }

                     mode = 0;
                     continue;
                  }

                  if (currentArg.startsWith("[")) {
                     throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
                  }

                  if (currentArg.endsWith("]")) {
                     int encodingStart = currentArg.indexOf(91) + 1;
                     if (encodingStart <= 1) {
                        throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
                     }

                     int encodingEnd = currentArg.length() - 1;
                     if (encodingStart >= 1) {
                        if (encodingStart < encodingEnd) {
                           customEncoding = currentArg.substring(encodingStart, encodingEnd);

                           try {
                              new InputStreamReader(new ByteArrayInputStream(new byte[0]), customEncoding);
                           } catch (UnsupportedEncodingException var33) {
                              throw new IllegalArgumentException(this.bind("configure.unsupportedEncoding", customEncoding));
                           }
                        }

                        currentArg = currentArg.substring(0, encodingStart - 1);
                     }
                  }

                  if (currentArg.endsWith(".java")) {
                     if (this.filenames == null) {
                        this.filenames = new String[argCount - index];
                        this.encodings = new String[argCount - index];
                        this.destinationPaths = new String[argCount - index];
                     } else if (filesCount == this.filenames.length) {
                        int length = this.filenames.length;
                        System.arraycopy(this.filenames, 0, this.filenames = new String[length + argCount - index], 0, length);
                        System.arraycopy(this.encodings, 0, this.encodings = new String[length + argCount - index], 0, length);
                        System.arraycopy(this.destinationPaths, 0, this.destinationPaths = new String[length + argCount - index], 0, length);
                     }

                     this.filenames[filesCount] = currentArg;
                     this.encodings[filesCount++] = customEncoding;
                     customEncoding = null;
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-log")) {
                     if (this.log != null) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateLog", currentArg));
                     }

                     mode = 5;
                     continue;
                  }

                  if (currentArg.equals("-repeat")) {
                     if (this.maxRepetition > 0) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateRepeat", currentArg));
                     }

                     mode = 6;
                     continue;
                  }

                  if (currentArg.equals("-maxProblems")) {
                     if (this.maxProblems > 0) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateMaxProblems", currentArg));
                     }

                     mode = 11;
                     continue;
                  }

                  if (currentArg.equals("-source")) {
                     mode = 7;
                     continue;
                  }

                  if (currentArg.equals("-encoding")) {
                     mode = 8;
                     continue;
                  }

                  if (currentArg.equals("-1.3")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.3");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-1.4")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-1.5") || currentArg.equals("-5") || currentArg.equals("-5.0")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.5");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-1.6") || currentArg.equals("-6") || currentArg.equals("-6.0")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-1.7") || currentArg.equals("-7") || currentArg.equals("-7.0")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.7");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-1.8") || currentArg.equals("-8") || currentArg.equals("-8.0")) {
                     if (didSpecifyCompliance) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateCompliance", currentArg));
                     }

                     didSpecifyCompliance = true;
                     this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.8");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-d")) {
                     if (this.destinationPath != null) {
                        StringBuffer errorMessage = new StringBuffer();
                        errorMessage.append(currentArg);
                        if (index + 1 < argCount) {
                           errorMessage.append(' ');
                           errorMessage.append(newCommandLineArgs[index + 1]);
                        }

                        throw new IllegalArgumentException(this.bind("configure.duplicateOutputPath", errorMessage.toString()));
                     }

                     mode = 3;
                     continue;
                  }

                  if (currentArg.equals("-classpath") || currentArg.equals("-cp")) {
                     mode = 1;
                     continue;
                  }

                  if (currentArg.equals("-bootclasspath")) {
                     if (bootclasspaths.size() > 0) {
                        StringBuffer errorMessage = new StringBuffer();
                        errorMessage.append(currentArg);
                        if (index + 1 < argCount) {
                           errorMessage.append(' ');
                           errorMessage.append(newCommandLineArgs[index + 1]);
                        }

                        throw new IllegalArgumentException(this.bind("configure.duplicateBootClasspath", errorMessage.toString()));
                     }

                     mode = 9;
                     continue;
                  }

                  if (currentArg.equals("-sourcepath")) {
                     if (sourcepathClasspathArg != null) {
                        StringBuffer errorMessage = new StringBuffer();
                        errorMessage.append(currentArg);
                        if (index + 1 < argCount) {
                           errorMessage.append(' ');
                           errorMessage.append(newCommandLineArgs[index + 1]);
                        }

                        throw new IllegalArgumentException(this.bind("configure.duplicateSourcepath", errorMessage.toString()));
                     }

                     mode = 13;
                     continue;
                  }

                  if (currentArg.equals("-extdirs")) {
                     if (extdirsClasspaths != null) {
                        StringBuffer errorMessage = new StringBuffer();
                        errorMessage.append(currentArg);
                        if (index + 1 < argCount) {
                           errorMessage.append(' ');
                           errorMessage.append(newCommandLineArgs[index + 1]);
                        }

                        throw new IllegalArgumentException(this.bind("configure.duplicateExtDirs", errorMessage.toString()));
                     }

                     mode = 12;
                     continue;
                  }

                  if (currentArg.equals("-endorseddirs")) {
                     if (endorsedDirClasspaths != null) {
                        StringBuffer errorMessage = new StringBuffer();
                        errorMessage.append(currentArg);
                        if (index + 1 < argCount) {
                           errorMessage.append(' ');
                           errorMessage.append(newCommandLineArgs[index + 1]);
                        }

                        throw new IllegalArgumentException(this.bind("configure.duplicateEndorsedDirs", errorMessage.toString()));
                     }

                     mode = 15;
                     continue;
                  }

                  if (currentArg.equals("-progress")) {
                     mode = 0;
                     this.showProgress = true;
                     continue;
                  }

                  if (currentArg.startsWith("-proceedOnError")) {
                     mode = 0;
                     int length = currentArg.length();
                     if (length > 15) {
                        if (!currentArg.equals("-proceedOnError:Fatal")) {
                           throw new IllegalArgumentException(this.bind("configure.invalidWarningConfiguration", currentArg));
                        }

                        this.options.put("org.eclipse.jdt.core.compiler.problem.fatalOptionalError", "enabled");
                     } else {
                        this.options.put("org.eclipse.jdt.core.compiler.problem.fatalOptionalError", "disabled");
                     }

                     this.proceedOnError = true;
                     continue;
                  }

                  if (currentArg.equals("-time")) {
                     mode = 0;
                     this.timing = 1;
                     continue;
                  }

                  if (currentArg.equals("-time:detail")) {
                     mode = 0;
                     this.timing = 3;
                     continue;
                  }

                  if (currentArg.equals("-version") || currentArg.equals("-v")) {
                     this.logger.logVersion(true);
                     this.proceed = false;
                     return;
                  }

                  if (currentArg.equals("-showversion")) {
                     printVersionRequired = true;
                     mode = 0;
                     continue;
                  }

                  if ("-deprecation".equals(currentArg)) {
                     didSpecifyDeprecation = true;
                     this.options.put("org.eclipse.jdt.core.compiler.problem.deprecation", "warning");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-help") || currentArg.equals("-?")) {
                     printUsageRequired = true;
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-help:warn") || currentArg.equals("-?:warn")) {
                     printUsageRequired = true;
                     usageSection = "misc.usage.warn";
                     continue;
                  }

                  if (currentArg.equals("-noExit")) {
                     this.systemExitWhenFinished = false;
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-verbose")) {
                     this.verbose = true;
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-referenceInfo")) {
                     this.produceRefInfo = true;
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-inlineJSR")) {
                     mode = 0;
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode", "enabled");
                     continue;
                  }

                  if (currentArg.equals("-parameters")) {
                     mode = 0;
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.methodParameters", "generate");
                     continue;
                  }

                  if (currentArg.equals("-genericsignature")) {
                     mode = 0;
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.lambda.genericSignature", "generate");
                     continue;
                  }

                  if (currentArg.startsWith("-g")) {
                     mode = 0;
                     int length = currentArg.length();
                     if (length == 2) {
                        this.options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "generate");
                        this.options.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "generate");
                        this.options.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "generate");
                        continue;
                     }

                     if (length <= 3) {
                        throw new IllegalArgumentException(this.bind("configure.invalidDebugOption", currentArg));
                     }

                     this.options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "do not generate");
                     this.options.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "do not generate");
                     this.options.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "do not generate");
                     if (length == 7 && currentArg.equals("-g:none")) {
                        continue;
                     }

                     StringTokenizer tokenizer = new StringTokenizer(currentArg.substring(3, currentArg.length()), ",");

                     while(true) {
                        if (!tokenizer.hasMoreTokens()) {
                           continue label978;
                        }

                        String token = tokenizer.nextToken();
                        if (token.equals("vars")) {
                           this.options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "generate");
                        } else if (token.equals("lines")) {
                           this.options.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "generate");
                        } else {
                           if (!token.equals("source")) {
                              throw new IllegalArgumentException(this.bind("configure.invalidDebugOption", currentArg));
                           }

                           this.options.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "generate");
                        }
                     }
                  }

                  if (currentArg.startsWith("-warn")) {
                     mode = 0;
                     int length = currentArg.length();
                     if (length == 10 && currentArg.equals("-warn:none")) {
                        this.disableAll(0);
                        continue;
                     }

                     if (length <= 6) {
                        throw new IllegalArgumentException(this.bind("configure.invalidWarningConfiguration", currentArg));
                     }

                     int warnTokenStart;
                     boolean isEnabling;
                     switch(currentArg.charAt(6)) {
                        case '+':
                           warnTokenStart = 7;
                           isEnabling = true;
                           break;
                        case ',':
                        default:
                           this.disableAll(0);
                           warnTokenStart = 6;
                           isEnabling = true;
                           break;
                        case '-':
                           warnTokenStart = 7;
                           isEnabling = false;
                     }

                     StringTokenizer tokenizer = new StringTokenizer(currentArg.substring(warnTokenStart, currentArg.length()), ",");
                     int tokenCounter = 0;
                     if (didSpecifyDeprecation) {
                        this.options.put("org.eclipse.jdt.core.compiler.problem.deprecation", "warning");
                     }

                     String token;
                     for(; tokenizer.hasMoreTokens(); this.handleWarningToken(token, isEnabling)) {
                        token = tokenizer.nextToken();
                        ++tokenCounter;
                        switch(token.charAt(0)) {
                           case '+':
                              isEnabling = true;
                              token = token.substring(1);
                           case ',':
                           default:
                              break;
                           case '-':
                              isEnabling = false;
                              token = token.substring(1);
                        }
                     }

                     if (tokenCounter == 0) {
                        throw new IllegalArgumentException(this.bind("configure.invalidWarningOption", currentArg));
                     }
                     continue;
                  }

                  if (currentArg.startsWith("-err")) {
                     mode = 0;
                     int length = currentArg.length();
                     if (length <= 5) {
                        throw new IllegalArgumentException(this.bind("configure.invalidErrorConfiguration", currentArg));
                     }

                     int errorTokenStart;
                     boolean isEnabling;
                     switch(currentArg.charAt(5)) {
                        case '+':
                           errorTokenStart = 6;
                           isEnabling = true;
                           break;
                        case ',':
                        default:
                           this.disableAll(1);
                           errorTokenStart = 5;
                           isEnabling = true;
                           break;
                        case '-':
                           errorTokenStart = 6;
                           isEnabling = false;
                     }

                     StringTokenizer tokenizer = new StringTokenizer(currentArg.substring(errorTokenStart, currentArg.length()), ",");

                     String token;
                     int tokenCounter;
                     for(tokenCounter = 0; tokenizer.hasMoreTokens(); this.handleErrorToken(token, isEnabling)) {
                        token = tokenizer.nextToken();
                        ++tokenCounter;
                        switch(token.charAt(0)) {
                           case '+':
                              isEnabling = true;
                              token = token.substring(1);
                           case ',':
                           default:
                              break;
                           case '-':
                              isEnabling = false;
                              token = token.substring(1);
                        }
                     }

                     if (tokenCounter == 0) {
                        throw new IllegalArgumentException(this.bind("configure.invalidErrorOption", currentArg));
                     }
                     continue;
                  }

                  if (currentArg.equals("-target")) {
                     mode = 4;
                     continue;
                  }

                  if (currentArg.equals("-preserveAllLocals")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.unusedLocal", "preserve");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-enableJavadoc")) {
                     mode = 0;
                     this.enableJavadocOn = true;
                     continue;
                  }

                  if (currentArg.equals("-Xemacs")) {
                     mode = 0;
                     this.logger.setEmacs();
                     continue;
                  }

                  if (currentArg.startsWith("-A")) {
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-processorpath")) {
                     mode = 17;
                     continue;
                  }

                  if (currentArg.equals("-processor")) {
                     mode = 18;
                     continue;
                  }

                  if (currentArg.equals("-proc:only")) {
                     this.options.put("org.eclipse.jdt.core.compiler.generateClassFiles", "disabled");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-proc:none")) {
                     didSpecifyDisabledAnnotationProcessing = true;
                     this.options.put("org.eclipse.jdt.core.compiler.processAnnotations", "disabled");
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-s")) {
                     mode = 19;
                     continue;
                  }

                  if (currentArg.equals("-XprintProcessorInfo") || currentArg.equals("-XprintRounds")) {
                     mode = 0;
                     continue;
                  }

                  if (currentArg.startsWith("-X")) {
                     mode = 0;
                     continue;
                  }

                  if (currentArg.startsWith("-J")) {
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-O")) {
                     mode = 0;
                     continue;
                  }

                  if (currentArg.equals("-classNames")) {
                     mode = 20;
                     continue;
                  }

                  if (currentArg.equals("-properties")) {
                     mode = 21;
                     continue;
                  }

                  if (currentArg.equals("-missingNullDefault")) {
                     this.options.put("org.eclipse.jdt.core.compiler.annotation.missingNonNullByDefaultAnnotation", "warning");
                     continue;
                  }

                  if (currentArg.equals("-annotationpath")) {
                     mode = 22;
                     continue;
                  }
                  break;
               case 1:
                  mode = 0;
                  index += this.processPaths(newCommandLineArgs, index, currentArg, classpaths);
                  continue;
               case 2:
               case 10:
               case 14:
               default:
                  break;
               case 3:
                  this.setDestinationPath(currentArg.equals("none") ? "none" : currentArg);
                  mode = 0;
                  continue;
               case 4:
                  if (this.didSpecifyTarget) {
                     throw new IllegalArgumentException(this.bind("configure.duplicateTarget", currentArg));
                  }

                  this.didSpecifyTarget = true;
                  if (currentArg.equals("1.1")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.1");
                  } else if (currentArg.equals("1.2")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.2");
                  } else if (currentArg.equals("1.3")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.3");
                  } else if (currentArg.equals("1.4")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
                  } else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
                  } else if (currentArg.equals("1.6") || currentArg.equals("6") || currentArg.equals("6.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
                  } else if (currentArg.equals("1.7") || currentArg.equals("7") || currentArg.equals("7.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7");
                  } else if (currentArg.equals("1.8") || currentArg.equals("8") || currentArg.equals("8.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8");
                  } else if (currentArg.equals("jsr14")) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "jsr14");
                  } else {
                     if (!currentArg.equals("cldc1.1")) {
                        throw new IllegalArgumentException(this.bind("configure.targetJDK", currentArg));
                     }

                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "cldc1.1");
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode", "enabled");
                  }

                  mode = 0;
                  continue;
               case 5:
                  this.log = currentArg;
                  mode = 0;
                  continue;
               case 6:
                  try {
                     this.maxRepetition = Integer.parseInt(currentArg);
                     if (this.maxRepetition <= 0) {
                        throw new IllegalArgumentException(this.bind("configure.repetition", currentArg));
                     }
                  } catch (NumberFormatException var36) {
                     throw new IllegalArgumentException(this.bind("configure.repetition", currentArg));
                  }

                  mode = 0;
                  continue;
               case 7:
                  if (this.didSpecifySource) {
                     throw new IllegalArgumentException(this.bind("configure.duplicateSource", currentArg));
                  }

                  this.didSpecifySource = true;
                  if (currentArg.equals("1.3")) {
                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.3");
                  } else if (currentArg.equals("1.4")) {
                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.4");
                  } else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.5");
                  } else if (currentArg.equals("1.6") || currentArg.equals("6") || currentArg.equals("6.0")) {
                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.6");
                  } else if (!currentArg.equals("1.7") && !currentArg.equals("7") && !currentArg.equals("7.0")) {
                     if (!currentArg.equals("1.8") && !currentArg.equals("8") && !currentArg.equals("8.0")) {
                        throw new IllegalArgumentException(this.bind("configure.source", currentArg));
                     }

                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.8");
                  } else {
                     this.options.put("org.eclipse.jdt.core.compiler.source", "1.7");
                  }

                  mode = 0;
                  continue;
               case 8:
                  if (specifiedEncodings != null) {
                     if (!specifiedEncodings.contains(currentArg)) {
                        if (specifiedEncodings.size() > 1) {
                           this.logger.logWarning(this.bind("configure.differentencodings", currentArg, getAllEncodings(specifiedEncodings)));
                        } else {
                           this.logger.logWarning(this.bind("configure.differentencoding", currentArg, getAllEncodings(specifiedEncodings)));
                        }
                     }
                  } else {
                     specifiedEncodings = new HashSet();
                  }

                  try {
                     new InputStreamReader(new ByteArrayInputStream(new byte[0]), currentArg);
                  } catch (UnsupportedEncodingException var34) {
                     throw new IllegalArgumentException(this.bind("configure.unsupportedEncoding", currentArg));
                  }

                  specifiedEncodings.add(currentArg);
                  this.options.put("org.eclipse.jdt.core.encoding", currentArg);
                  mode = 0;
                  continue;
               case 9:
                  mode = 0;
                  index += this.processPaths(newCommandLineArgs, index, currentArg, bootclasspaths);
                  continue;
               case 11:
                  try {
                     this.maxProblems = Integer.parseInt(currentArg);
                     if (this.maxProblems <= 0) {
                        throw new IllegalArgumentException(this.bind("configure.maxProblems", currentArg));
                     }

                     this.options.put("org.eclipse.jdt.core.compiler.maxProblemPerUnit", currentArg);
                  } catch (NumberFormatException var35) {
                     throw new IllegalArgumentException(this.bind("configure.maxProblems", currentArg));
                  }

                  mode = 0;
                  continue;
               case 12:
                  if (currentArg.indexOf("[-d") != -1) {
                     throw new IllegalArgumentException(this.bind("configure.unexpectedDestinationPathEntry", "-extdir"));
                  }

                  StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator, false);
                  extdirsClasspaths = new ArrayList(4);

                  while(tokenizer.hasMoreTokens()) {
                     extdirsClasspaths.add(tokenizer.nextToken());
                  }

                  mode = 0;
                  continue;
               case 13:
                  mode = 0;
                  String[] sourcePaths = new String[1];
                  index += this.processPaths(newCommandLineArgs, index, currentArg, sourcePaths);
                  sourcepathClasspathArg = sourcePaths[0];
                  continue;
               case 15:
                  if (currentArg.indexOf("[-d") != -1) {
                     throw new IllegalArgumentException(this.bind("configure.unexpectedDestinationPathEntry", "-endorseddirs"));
                  }

                  StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator, false);
                  endorsedDirClasspaths = new ArrayList(4);

                  while(tokenizer.hasMoreTokens()) {
                     endorsedDirClasspaths.add(tokenizer.nextToken());
                  }

                  mode = 0;
                  continue;
               case 16:
                  if (!currentArg.endsWith("]")) {
                     throw new IllegalArgumentException(this.bind("configure.incorrectDestinationPathEntry", "[-d " + currentArg));
                  }

                  customDestinationPath = currentArg.substring(0, currentArg.length() - 1);
                  break;
               case 17:
                  mode = 0;
                  continue;
               case 18:
                  mode = 0;
                  continue;
               case 19:
                  mode = 0;
                  continue;
               case 20:
                  StringTokenizer tokenizer = new StringTokenizer(currentArg, ",");
                  if (this.classNames == null) {
                     this.classNames = new String[4];
                  }

                  for(; tokenizer.hasMoreTokens(); this.classNames[classCount++] = tokenizer.nextToken()) {
                     if (this.classNames.length == classCount) {
                        System.arraycopy(this.classNames, 0, this.classNames = new String[classCount * 2], 0, classCount);
                     }
                  }

                  mode = 0;
                  continue;
               case 21:
                  this.initializeWarnings(currentArg);
                  mode = 0;
                  continue;
               case 22:
                  mode = 0;
                  if (!currentArg.isEmpty() && currentArg.charAt(0) != '-') {
                     if ("CLASSPATH".equals(currentArg)) {
                        this.annotationsFromClasspath = true;
                        continue;
                     }

                     if (this.annotationPaths == null) {
                        this.annotationPaths = new ArrayList<>();
                     }

                     StringTokenizer tokens = new StringTokenizer(currentArg, File.pathSeparator);

                     while(true) {
                        if (!tokens.hasMoreTokens()) {
                           continue label978;
                        }

                        this.annotationPaths.add(tokens.nextToken());
                     }
                  }

                  throw new IllegalArgumentException(this.bind("configure.missingAnnotationPath", currentArg));
            }

            if (customDestinationPath == null) {
               if (File.separatorChar != '/') {
                  currentArg = currentArg.replace('/', File.separatorChar);
               }

               if (currentArg.endsWith("[-d")) {
                  currentSourceDirectory = currentArg.substring(0, currentArg.length() - 3);
                  mode = 16;
                  continue;
               }

               currentSourceDirectory = currentArg;
            }

            File dir = new File(currentSourceDirectory);
            if (!dir.isDirectory()) {
               throw new IllegalArgumentException(this.bind("configure.unrecognizedOption", currentSourceDirectory));
            }

            String[] result = FileFinder.find(dir, ".JAVA");
            if ("none".equals(customDestinationPath)) {
               customDestinationPath = "none";
            }

            if (this.filenames != null) {
               int length = result.length;
               System.arraycopy(this.filenames, 0, this.filenames = new String[length + filesCount], 0, filesCount);
               System.arraycopy(this.encodings, 0, this.encodings = new String[length + filesCount], 0, filesCount);
               System.arraycopy(this.destinationPaths, 0, this.destinationPaths = new String[length + filesCount], 0, filesCount);
               System.arraycopy(result, 0, this.filenames, filesCount, length);

               for(int i = 0; i < length; ++i) {
                  this.encodings[filesCount + i] = customEncoding;
                  this.destinationPaths[filesCount + i] = customDestinationPath;
               }

               filesCount += length;
               customEncoding = null;
               customDestinationPath = null;
               currentSourceDirectory = null;
            } else {
               this.filenames = result;
               filesCount = this.filenames.length;
               this.encodings = new String[filesCount];
               this.destinationPaths = new String[filesCount];

               for(int i = 0; i < filesCount; ++i) {
                  this.encodings[i] = customEncoding;
                  this.destinationPaths[i] = customDestinationPath;
               }

               customEncoding = null;
               customDestinationPath = null;
               currentSourceDirectory = null;
            }

            mode = 0;
         }

         if (this.enableJavadocOn) {
            this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
         } else if (this.warnJavadocOn || this.warnAllJavadocOn) {
            this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
            this.options.put("org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference", "disabled");
            this.options.put("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference", "disabled");
         }

         if (this.warnJavadocOn) {
            this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTags", "enabled");
            this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef", "enabled");
            this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef", "enabled");
            this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility", "private");
         }

         if (!printUsageRequired && (filesCount != 0 || classCount != 0)) {
            if (this.log != null) {
               this.logger.setLog(this.log);
            } else {
               this.showProgress = false;
            }

            this.logger.logVersion(printVersionRequired);
            this.validateOptions(didSpecifyCompliance);
            if (!didSpecifyDisabledAnnotationProcessing
               && CompilerOptions.versionToJdkLevel(this.options.get("org.eclipse.jdt.core.compiler.compliance")) >= 3276800L) {
               this.options.put("org.eclipse.jdt.core.compiler.processAnnotations", "enabled");
            }

            this.logger.logCommandLineArguments(newCommandLineArgs);
            this.logger.logOptions(this.options);
            if (this.maxRepetition == 0) {
               this.maxRepetition = 1;
            }

            if (this.maxRepetition >= 3 && (this.timing & 1) != 0) {
               this.compilerStats = new CompilerStats[this.maxRepetition];
            }

            if (filesCount != 0) {
               System.arraycopy(this.filenames, 0, this.filenames = new String[filesCount], 0, filesCount);
            }

            if (classCount != 0) {
               System.arraycopy(this.classNames, 0, this.classNames = new String[classCount], 0, classCount);
            }

            this.setPaths(bootclasspaths, sourcepathClasspathArg, sourcepathClasspaths, classpaths, extdirsClasspaths, endorsedDirClasspaths, customEncoding);
            if (specifiedEncodings != null && specifiedEncodings.size() > 1) {
               this.logger
                  .logWarning(this.bind("configure.multipleencodings", this.options.get("org.eclipse.jdt.core.encoding"), getAllEncodings(specifiedEncodings)));
            }

            if (this.pendingErrors != null) {
               for(String message : this.pendingErrors) {
                  this.logger.logPendingError(message);
               }

               this.pendingErrors = null;
            }
         } else {
            if (usageSection == null) {
               this.printUsage();
            } else {
               this.printUsage(usageSection);
            }

            this.proceed = false;
         }
      } else {
         this.printUsage();
      }
   }

   private static char[][] decodeIgnoreOptionalProblemsFromFolders(String folders) {
      StringTokenizer tokenizer = new StringTokenizer(folders, File.pathSeparator);
      char[][] result = new char[tokenizer.countTokens()][];
      int count = 0;

      while(tokenizer.hasMoreTokens()) {
         String fileName = tokenizer.nextToken();
         File file = new File(fileName);
         if (file.exists()) {
            try {
               result[count++] = file.getCanonicalPath().toCharArray();
            } catch (IOException var6) {
               result[count++] = fileName.toCharArray();
            }
         } else {
            result[count++] = fileName.toCharArray();
         }
      }

      return result;
   }

   private static String getAllEncodings(Set encodings) {
      int size = encodings.size();
      String[] allEncodings = new String[size];
      encodings.toArray(allEncodings);
      Arrays.sort((Object[])allEncodings);
      StringBuffer buffer = new StringBuffer();

      for(int i = 0; i < size; ++i) {
         if (i > 0) {
            buffer.append(", ");
         }

         buffer.append(allEncodings[i]);
      }

      return String.valueOf(buffer);
   }

   private void initializeWarnings(String propertiesFile) {
      File file = new File(propertiesFile);
      if (!file.exists()) {
         throw new IllegalArgumentException(this.bind("configure.missingwarningspropertiesfile", propertiesFile));
      } else {
         BufferedInputStream stream = null;
         Properties properties = null;

         try {
            stream = new BufferedInputStream(new FileInputStream(propertiesFile));
            properties = new Properties();
            properties.load(stream);
         } catch (IOException var13) {
            var13.printStackTrace();
            throw new IllegalArgumentException(this.bind("configure.ioexceptionwarningspropertiesfile", propertiesFile));
         } finally {
            if (stream != null) {
               try {
                  stream.close();
               } catch (IOException var12) {
               }
            }
         }

         for(Entry entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith("org.eclipse.jdt.core.compiler.")) {
               this.options.put(key, entry.getValue().toString());
            }
         }

         if (!properties.containsKey("org.eclipse.jdt.core.compiler.debug.localVariable")) {
            this.options.put("org.eclipse.jdt.core.compiler.debug.localVariable", "generate");
         }

         if (!properties.containsKey("org.eclipse.jdt.core.compiler.codegen.unusedLocal")) {
            this.options.put("org.eclipse.jdt.core.compiler.codegen.unusedLocal", "preserve");
         }

         if (!properties.containsKey("org.eclipse.jdt.core.compiler.doc.comment.support")) {
            this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
         }

         if (!properties.containsKey("org.eclipse.jdt.core.compiler.problem.forbiddenReference")) {
            this.options.put("org.eclipse.jdt.core.compiler.problem.forbiddenReference", "error");
         }
      }
   }

   protected void enableAll(int severity) {
      String newValue = null;
      switch(severity) {
         case 0:
            newValue = "warning";
            break;
         case 1:
            newValue = "error";
      }

      Entry[] entries = this.options.entrySet().toArray(new Entry[this.options.size()]);
      int i = 0;

      for(int max = entries.length; i < max; ++i) {
         Entry<String, String> entry = entries[i];
         if (entry.getValue().equals("ignore")) {
            this.options.put(entry.getKey(), newValue);
         }
      }

      this.options.put("org.eclipse.jdt.core.compiler.taskTags", Util.EMPTY_STRING);
   }

   protected void disableAll(int severity) {
      String checkedValue = null;
      switch(severity) {
         case 0:
            checkedValue = "warning";
            break;
         case 1:
            checkedValue = "error";
      }

      Object[] entries = this.options.entrySet().toArray();
      int i = 0;

      for(int max = entries.length; i < max; ++i) {
         Entry entry = (Entry)entries[i];
         if (entry.getKey() instanceof String && entry.getValue() instanceof String && ((String)entry.getValue()).equals(checkedValue)) {
            this.options.put((String)entry.getKey(), "ignore");
         }
      }
   }

   public String extractDestinationPathFromSourceFile(CompilationResult result) {
      ICompilationUnit compilationUnit = result.compilationUnit;
      if (compilationUnit != null) {
         char[] fileName = compilationUnit.getFileName();
         int lastIndex = CharOperation.lastIndexOf(File.separatorChar, fileName);
         if (lastIndex != -1) {
            String outputPathName = new String(fileName, 0, lastIndex);
            File output = new File(outputPathName);
            if (output.exists() && output.isDirectory()) {
               return outputPathName;
            }
         }
      }

      return System.getProperty("user.dir");
   }

   public ICompilerRequestor getBatchRequestor() {
      return new BatchCompilerRequestor(this);
   }

   public CompilationUnit[] getCompilationUnits() {
      int fileCount = this.filenames.length;
      CompilationUnit[] units = new CompilationUnit[fileCount];
      HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);
      String defaultEncoding = this.options.get("org.eclipse.jdt.core.encoding");
      if (Util.EMPTY_STRING.equals(defaultEncoding)) {
         defaultEncoding = null;
      }

      for(int i = 0; i < fileCount; ++i) {
         char[] charName = this.filenames[i].toCharArray();
         if (knownFileNames.get(charName) != null) {
            throw new IllegalArgumentException(this.bind("unit.more", this.filenames[i]));
         }

         knownFileNames.put(charName, charName);
         File file = new File(this.filenames[i]);
         if (!file.exists()) {
            throw new IllegalArgumentException(this.bind("unit.missing", this.filenames[i]));
         }

         String encoding = this.encodings[i];
         if (encoding == null) {
            encoding = defaultEncoding;
         }

         String fileName;
         try {
            fileName = file.getCanonicalPath();
         } catch (IOException var10) {
            fileName = this.filenames[i];
         }

         units[i] = new CompilationUnit(
            null, fileName, encoding, this.destinationPaths[i], shouldIgnoreOptionalProblems(this.ignoreOptionalProblemsFromFolders, fileName.toCharArray())
         );
      }

      return units;
   }

   public IErrorHandlingPolicy getHandlingPolicy() {
      return new IErrorHandlingPolicy() {
         @Override
         public boolean proceedOnErrors() {
            return Main.this.proceedOnError;
         }

         @Override
         public boolean stopOnFirstError() {
            return false;
         }

         @Override
         public boolean ignoreAllErrors() {
            return false;
         }
      };
   }

   public File getJavaHome() {
      if (!this.javaHomeChecked) {
         this.javaHomeChecked = true;
         this.javaHomeCache = Util.getJavaHome();
      }

      return this.javaHomeCache;
   }

   public FileSystem getLibraryAccess() {
      return new FileSystem(
         this.checkedClasspaths,
         this.filenames,
         this.annotationsFromClasspath && "enabled".equals(this.options.get("org.eclipse.jdt.core.compiler.annotation.nullanalysis"))
      );
   }

   public IProblemFactory getProblemFactory() {
      return new DefaultProblemFactory(this.compilerLocale);
   }

   protected ArrayList handleBootclasspath(ArrayList bootclasspaths, String customEncoding) {
      int bootclasspathsSize;
      if (bootclasspaths != null && (bootclasspathsSize = bootclasspaths.size()) != 0) {
         String[] paths = new String[bootclasspathsSize];
         bootclasspaths.toArray(paths);
         bootclasspaths.clear();

         for(int i = 0; i < bootclasspathsSize; ++i) {
            this.processPathEntries(4, bootclasspaths, paths[i], customEncoding, false, true);
         }
      } else {
         bootclasspaths = new ArrayList(4);

         try {
            Util.collectRunningVMBootclasspath(bootclasspaths);
         } catch (IllegalStateException var6) {
            this.logger.logWrongJDK();
            this.proceed = false;
            return null;
         }
      }

      return bootclasspaths;
   }

   protected ArrayList handleClasspath(ArrayList classpaths, String customEncoding) {
      int classpathsSize;
      if (classpaths != null && (classpathsSize = classpaths.size()) != 0) {
         String[] paths = new String[classpathsSize];
         classpaths.toArray(paths);
         classpaths.clear();

         for(int i = 0; i < classpathsSize; ++i) {
            this.processPathEntries(4, classpaths, paths[i], customEncoding, false, true);
         }
      } else {
         classpaths = new ArrayList(4);
         String classProp = System.getProperty("java.class.path");
         if (classProp != null && classProp.length() != 0) {
            StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);

            while(tokenizer.hasMoreTokens()) {
               String token = tokenizer.nextToken();
               FileSystem.Classpath currentClasspath = FileSystem.getClasspath(token, customEncoding, null, this.options);
               if (currentClasspath != null) {
                  classpaths.add(currentClasspath);
               } else if (token.length() != 0) {
                  this.addPendingErrors(this.bind("configure.incorrectClasspath", token));
               }
            }
         } else {
            this.addPendingErrors(this.bind("configure.noClasspath"));
            FileSystem.Classpath classpath = FileSystem.getClasspath(System.getProperty("user.dir"), customEncoding, null, this.options);
            if (classpath != null) {
               classpaths.add(classpath);
            }
         }
      }

      ArrayList result = new ArrayList();
      HashMap knownNames = new HashMap();
      FileSystem.ClasspathSectionProblemReporter problemReporter = new FileSystem.ClasspathSectionProblemReporter() {
         @Override
         public void invalidClasspathSection(String jarFilePath) {
            Main.this.addPendingErrors(Main.this.bind("configure.invalidClasspathSection", jarFilePath));
         }

         @Override
         public void multipleClasspathSections(String jarFilePath) {
            Main.this.addPendingErrors(Main.this.bind("configure.multipleClasspathSections", jarFilePath));
         }
      };

      while(!classpaths.isEmpty()) {
         FileSystem.Classpath current = (FileSystem.Classpath)classpaths.remove(0);
         String currentPath = current.getPath();
         if (knownNames.get(currentPath) == null) {
            knownNames.put(currentPath, current);
            result.add(current);
            List linkedJars = current.fetchLinkedJars(problemReporter);
            if (linkedJars != null) {
               classpaths.addAll(0, linkedJars);
            }
         }
      }

      return result;
   }

   protected ArrayList handleEndorseddirs(ArrayList endorsedDirClasspaths) {
      File javaHome = this.getJavaHome();
      if (endorsedDirClasspaths == null) {
         endorsedDirClasspaths = new ArrayList(4);
         String endorsedDirsStr = System.getProperty("java.endorsed.dirs");
         if (endorsedDirsStr == null) {
            if (javaHome != null) {
               endorsedDirClasspaths.add(javaHome.getAbsolutePath() + "/lib/endorsed");
            }
         } else {
            StringTokenizer tokenizer = new StringTokenizer(endorsedDirsStr, File.pathSeparator);

            while(tokenizer.hasMoreTokens()) {
               endorsedDirClasspaths.add(tokenizer.nextToken());
            }
         }
      }

      if (endorsedDirClasspaths.size() != 0) {
         File[] directoriesToCheck = new File[endorsedDirClasspaths.size()];

         for(int i = 0; i < directoriesToCheck.length; ++i) {
            directoriesToCheck[i] = new File((String)endorsedDirClasspaths.get(i));
         }

         endorsedDirClasspaths.clear();
         File[][] endorsedDirsJars = getLibrariesFiles(directoriesToCheck);
         if (endorsedDirsJars != null) {
            int i = 0;

            for(int max = endorsedDirsJars.length; i < max; ++i) {
               File[] current = endorsedDirsJars[i];
               if (current != null) {
                  int j = 0;

                  for(int max2 = current.length; j < max2; ++j) {
                     FileSystem.Classpath classpath = FileSystem.getClasspath(current[j].getAbsolutePath(), null, null, this.options);
                     if (classpath != null) {
                        endorsedDirClasspaths.add(classpath);
                     }
                  }
               } else if (directoriesToCheck[i].isFile()) {
                  this.addPendingErrors(this.bind("configure.incorrectEndorsedDirsEntry", directoriesToCheck[i].getAbsolutePath()));
               }
            }
         }
      }

      return endorsedDirClasspaths;
   }

   protected ArrayList handleExtdirs(ArrayList extdirsClasspaths) {
      File javaHome = this.getJavaHome();
      if (extdirsClasspaths == null) {
         extdirsClasspaths = new ArrayList(4);
         String extdirsStr = System.getProperty("java.ext.dirs");
         if (extdirsStr == null) {
            extdirsClasspaths.add(javaHome.getAbsolutePath() + "/lib/ext");
         } else {
            StringTokenizer tokenizer = new StringTokenizer(extdirsStr, File.pathSeparator);

            while(tokenizer.hasMoreTokens()) {
               extdirsClasspaths.add(tokenizer.nextToken());
            }
         }
      }

      if (extdirsClasspaths.size() != 0) {
         File[] directoriesToCheck = new File[extdirsClasspaths.size()];

         for(int i = 0; i < directoriesToCheck.length; ++i) {
            directoriesToCheck[i] = new File((String)extdirsClasspaths.get(i));
         }

         extdirsClasspaths.clear();
         File[][] extdirsJars = getLibrariesFiles(directoriesToCheck);
         if (extdirsJars != null) {
            int i = 0;

            for(int max = extdirsJars.length; i < max; ++i) {
               File[] current = extdirsJars[i];
               if (current != null) {
                  int j = 0;

                  for(int max2 = current.length; j < max2; ++j) {
                     FileSystem.Classpath classpath = FileSystem.getClasspath(current[j].getAbsolutePath(), null, null, this.options);
                     if (classpath != null) {
                        extdirsClasspaths.add(classpath);
                     }
                  }
               } else if (directoriesToCheck[i].isFile()) {
                  this.addPendingErrors(this.bind("configure.incorrectExtDirsEntry", directoriesToCheck[i].getAbsolutePath()));
               }
            }
         }
      }

      return extdirsClasspaths;
   }

   protected void handleWarningToken(String token, boolean isEnabling) {
      this.handleErrorOrWarningToken(token, isEnabling, 0);
   }

   protected void handleErrorToken(String token, boolean isEnabling) {
      this.handleErrorOrWarningToken(token, isEnabling, 1);
   }

   private void setSeverity(String compilerOptions, int severity, boolean isEnabling) {
      if (isEnabling) {
         switch(severity) {
            case 0:
               this.options.put(compilerOptions, "warning");
               break;
            case 1:
               this.options.put(compilerOptions, "error");
               break;
            default:
               this.options.put(compilerOptions, "ignore");
         }
      } else {
         switch(severity) {
            case 0:
               String currentValue = this.options.get(compilerOptions);
               if ("warning".equals(currentValue)) {
                  this.options.put(compilerOptions, "ignore");
               }
               break;
            case 1:
               String currentValue = this.options.get(compilerOptions);
               if ("error".equals(currentValue)) {
                  this.options.put(compilerOptions, "ignore");
               }
               break;
            default:
               this.options.put(compilerOptions, "ignore");
         }
      }
   }

   private void handleErrorOrWarningToken(String token, boolean isEnabling, int severity) {
      if (token.length() != 0) {
         switch(token.charAt(0)) {
            case 'a':
               if (token.equals("allDeprecation")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.deprecation", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode", isEnabling ? "enabled" : "disabled");
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("allJavadoc")) {
                  this.warnAllJavadocOn = this.warnJavadocOn = isEnabling;
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.invalidJavadoc", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocTags", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocComments", severity, isEnabling);
                  return;
               }

               if (token.equals("assertIdentifier")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.assertIdentifier", severity, isEnabling);
                  return;
               }

               if (token.equals("allDeadCode")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.deadCode", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deadCodeInTrivialIfStatement", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("allOver-ann")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation", severity, isEnabling);
                  this.options
                     .put(
                        "org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotationForInterfaceMethodImplementation", isEnabling ? "enabled" : "disabled"
                     );
                  return;
               }

               if (token.equals("all-static-method")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.reportMethodCanBePotentiallyStatic", severity, isEnabling);
                  return;
               }

               if (token.equals("all")) {
                  if (isEnabling) {
                     this.enableAll(severity);
                  } else {
                     this.disableAll(severity);
                  }

                  return;
               }
               break;
            case 'b':
               if (token.equals("boxing")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.autoboxing", severity, isEnabling);
                  return;
               }
               break;
            case 'c':
               if (token.equals("constructorName")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.methodWithConstructorName", severity, isEnabling);
                  return;
               }

               if (token.equals("conditionAssign")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment", severity, isEnabling);
                  return;
               }

               if (token.equals("compareIdentical")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.comparingIdentical", severity, isEnabling);
                  return;
               }

               if (token.equals("charConcat")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion", severity, isEnabling);
                  return;
               }
               break;
            case 'd':
               if (token.equals("deprecation")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.deprecation", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode", "disabled");
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod", "disabled");
                  return;
               }

               if (token.equals("dep-ann")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation", severity, isEnabling);
                  return;
               }

               if (token.equals("discouraged")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.discouragedReference", severity, isEnabling);
                  return;
               }

               if (token.equals("deadCode")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.deadCode", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.deadCodeInTrivialIfStatement", "disabled");
                  return;
               }
               break;
            case 'e':
               if (token.equals("enumSwitch")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch", severity, isEnabling);
                  return;
               }

               if (token.equals("enumSwitchPedantic")) {
                  if (isEnabling) {
                     switch(severity) {
                        case 0:
                           if ("ignore".equals(this.options.get("org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch"))) {
                              this.setSeverity("org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch", severity, isEnabling);
                           }
                           break;
                        case 1:
                           this.setSeverity("org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch", severity, isEnabling);
                     }
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingEnumCaseDespiteDefault", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("emptyBlock")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock", severity, isEnabling);
                  return;
               }

               if (token.equals("enumIdentifier")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.enumIdentifier", severity, isEnabling);
                  return;
               }
               break;
            case 'f':
               if (token.equals("fieldHiding")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.fieldHiding", severity, isEnabling);
                  return;
               }

               if (token.equals("finalBound")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.finalParameterBound", severity, isEnabling);
                  return;
               }

               if (token.equals("finally")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally", severity, isEnabling);
                  return;
               }

               if (token.equals("forbidden")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.forbiddenReference", severity, isEnabling);
                  return;
               }

               if (token.equals("fallthrough")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.fallthroughCase", severity, isEnabling);
                  return;
               }
            case 'g':
            case 'k':
            case 'q':
            default:
               break;
            case 'h':
               if (token.equals("hiding")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.localVariableHiding", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.fieldHiding", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.typeParameterHiding", severity, isEnabling);
                  return;
               }

               if (token.equals("hashCode")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingHashCodeMethod", severity, isEnabling);
                  return;
               }
               break;
            case 'i':
               if (token.equals("indirectStatic")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.indirectStaticAccess", severity, isEnabling);
                  return;
               }

               if (token.equals("inheritNullAnnot")) {
                  this.options.put("org.eclipse.jdt.core.compiler.annotation.inheritNullAnnotations", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("intfNonInherited") || token.equals("interfaceNonInherited")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod", severity, isEnabling);
                  return;
               }

               if (token.equals("intfAnnotation")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.annotationSuperInterface", severity, isEnabling);
                  return;
               }

               if (token.equals("intfRedundant")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantSuperinterface", severity, isEnabling);
                  return;
               }

               if (token.equals("includeAssertNull")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.includeNullInfoFromAsserts", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("invalidJavadoc")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.invalidJavadoc", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTags", isEnabling ? "enabled" : "disabled");
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef", isEnabling ? "enabled" : "disabled");
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef", isEnabling ? "enabled" : "disabled");
                  if (isEnabling) {
                     this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
                     this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility", "private");
                  }

                  return;
               }

               if (token.equals("invalidJavadocTag")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTags", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("invalidJavadocTagDep")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("invalidJavadocTagNotVisible")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.startsWith("invalidJavadocTagVisibility")) {
                  int start = token.indexOf(40);
                  int end = token.indexOf(41);
                  String visibility = null;
                  if (isEnabling && start >= 0 && end >= 0 && start < end) {
                     visibility = token.substring(start + 1, end).trim();
                  }

                  if ((visibility == null || !visibility.equals("public"))
                     && !visibility.equals("private")
                     && !visibility.equals("protected")
                     && !visibility.equals("default")) {
                     throw new IllegalArgumentException(this.bind("configure.invalidJavadocTagVisibility", token));
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility", visibility);
                  return;
               }
               break;
            case 'j':
               if (token.equals("javadoc")) {
                  this.warnJavadocOn = isEnabling;
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.invalidJavadoc", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocTags", severity, isEnabling);
                  return;
               }
               break;
            case 'l':
               if (token.equals("localHiding")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.localVariableHiding", severity, isEnabling);
                  return;
               }
               break;
            case 'm':
               if (token.equals("maskedCatchBlock") || token.equals("maskedCatchBlocks")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock", severity, isEnabling);
                  return;
               }

               if (token.equals("missingJavadocTags")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocTags", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding", isEnabling ? "enabled" : "disabled");
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsMethodTypeParameters", isEnabling ? "enabled" : "disabled");
                  if (isEnabling) {
                     this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
                     this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility", "private");
                  }

                  return;
               }

               if (token.equals("missingJavadocTagsOverriding")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("missingJavadocTagsMethod")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsMethodTypeParameters", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.startsWith("missingJavadocTagsVisibility")) {
                  int start = token.indexOf(40);
                  int end = token.indexOf(41);
                  String visibility = null;
                  if (isEnabling && start >= 0 && end >= 0 && start < end) {
                     visibility = token.substring(start + 1, end).trim();
                  }

                  if ((visibility == null || !visibility.equals("public"))
                     && !visibility.equals("private")
                     && !visibility.equals("protected")
                     && !visibility.equals("default")) {
                     throw new IllegalArgumentException(this.bind("configure.missingJavadocTagsVisibility", token));
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility", visibility);
                  return;
               }

               if (token.equals("missingJavadocComments")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocComments", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding", isEnabling ? "enabled" : "disabled");
                  if (isEnabling) {
                     this.options.put("org.eclipse.jdt.core.compiler.doc.comment.support", "enabled");
                     this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility", "private");
                  }

                  return;
               }

               if (token.equals("missingJavadocCommentsOverriding")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingJavadocComments", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.startsWith("missingJavadocCommentsVisibility")) {
                  int start = token.indexOf(40);
                  int end = token.indexOf(41);
                  String visibility = null;
                  if (isEnabling && start >= 0 && end >= 0 && start < end) {
                     visibility = token.substring(start + 1, end).trim();
                  }

                  if ((visibility == null || !visibility.equals("public"))
                     && !visibility.equals("private")
                     && !visibility.equals("protected")
                     && !visibility.equals("default")) {
                     throw new IllegalArgumentException(this.bind("configure.missingJavadocCommentsVisibility", token));
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility", visibility);
                  return;
               }
               break;
            case 'n':
               if (token.equals("nls")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral", severity, isEnabling);
                  return;
               }

               if (token.equals("noEffectAssign")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.noEffectAssignment", severity, isEnabling);
                  return;
               }

               if (token.equals("noImplicitStringConversion")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion", severity, isEnabling);
                  return;
               }

               if (token.equals("null")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullReference", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.potentialNullReference", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantNullCheck", severity, isEnabling);
                  return;
               }

               if (token.equals("nullDereference")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullReference", severity, isEnabling);
                  if (!isEnabling) {
                     this.setSeverity("org.eclipse.jdt.core.compiler.problem.potentialNullReference", 256, isEnabling);
                     this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantNullCheck", 256, isEnabling);
                  }

                  return;
               }

               if (token.equals("nullAnnotConflict")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullAnnotationInferenceConflict", severity, isEnabling);
                  return;
               }

               if (token.equals("nullAnnotRedundant")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantNullAnnotation", severity, isEnabling);
                  return;
               }

               if (token.startsWith("nullAnnot")) {
                  String annotationNames = Util.EMPTY_STRING;
                  int start = token.indexOf(40);
                  int end = token.indexOf(41);
                  String nonNullAnnotName = null;
                  String nullableAnnotName = null;
                  String nonNullByDefaultAnnotName = null;
                  if (isEnabling && start >= 0 && end >= 0 && start < end) {
                     boolean isPrimarySet = !this.primaryNullAnnotationsSeen;
                     annotationNames = token.substring(start + 1, end).trim();
                     int separator1 = annotationNames.indexOf(124);
                     if (separator1 == -1) {
                        throw new IllegalArgumentException(this.bind("configure.invalidNullAnnot", token));
                     }

                     nullableAnnotName = annotationNames.substring(0, separator1).trim();
                     if (isPrimarySet && nullableAnnotName.length() == 0) {
                        throw new IllegalArgumentException(this.bind("configure.invalidNullAnnot", token));
                     }

                     int separator2 = annotationNames.indexOf(124, separator1 + 1);
                     if (separator2 == -1) {
                        throw new IllegalArgumentException(this.bind("configure.invalidNullAnnot", token));
                     }

                     nonNullAnnotName = annotationNames.substring(separator1 + 1, separator2).trim();
                     if (isPrimarySet && nonNullAnnotName.length() == 0) {
                        throw new IllegalArgumentException(this.bind("configure.invalidNullAnnot", token));
                     }

                     nonNullByDefaultAnnotName = annotationNames.substring(separator2 + 1).trim();
                     if (isPrimarySet && nonNullByDefaultAnnotName.length() == 0) {
                        throw new IllegalArgumentException(this.bind("configure.invalidNullAnnot", token));
                     }

                     if (isPrimarySet) {
                        this.primaryNullAnnotationsSeen = true;
                        this.options.put("org.eclipse.jdt.core.compiler.annotation.nullable", nullableAnnotName);
                        this.options.put("org.eclipse.jdt.core.compiler.annotation.nonnull", nonNullAnnotName);
                        this.options.put("org.eclipse.jdt.core.compiler.annotation.nonnullbydefault", nonNullByDefaultAnnotName);
                     } else {
                        if (nullableAnnotName.length() > 0) {
                           String nullableList = this.options.get("org.eclipse.jdt.core.compiler.annotation.nullable.secondary");
                           nullableList = nullableList.isEmpty() ? nullableAnnotName : nullableList + ',' + nullableAnnotName;
                           this.options.put("org.eclipse.jdt.core.compiler.annotation.nullable.secondary", nullableList);
                        }

                        if (nonNullAnnotName.length() > 0) {
                           String nonnullList = this.options.get("org.eclipse.jdt.core.compiler.annotation.nonnull.secondary");
                           nonnullList = nonnullList.isEmpty() ? nonNullAnnotName : nonnullList + ',' + nonNullAnnotName;
                           this.options.put("org.eclipse.jdt.core.compiler.annotation.nonnull.secondary", nonnullList);
                        }

                        if (nonNullByDefaultAnnotName.length() > 0) {
                           String nnbdList = this.options.get("org.eclipse.jdt.core.compiler.annotation.nonnullbydefault.secondary");
                           nnbdList = nnbdList.isEmpty() ? nonNullByDefaultAnnotName : nnbdList + ',' + nonNullByDefaultAnnotName;
                           this.options.put("org.eclipse.jdt.core.compiler.annotation.nonnullbydefault.secondary", nnbdList);
                        }
                     }
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.annotation.nullanalysis", isEnabling ? "enabled" : "disabled");
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullSpecViolation", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullAnnotationInferenceConflict", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullUncheckedConversion", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantNullAnnotation", severity, isEnabling);
                  return;
               }

               if (token.equals("nullUncheckedConversion")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nullUncheckedConversion", severity, isEnabling);
                  return;
               }

               if (token.equals("nonnullNotRepeated")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.nonnullParameterAnnotationDropped", severity, isEnabling);
                  return;
               }
               break;
            case 'o':
               if (token.equals("over-sync")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod", severity, isEnabling);
                  return;
               }

               if (token.equals("over-ann")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation", severity, isEnabling);
                  this.options.put("org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotationForInterfaceMethodImplementation", "disabled");
                  return;
               }
               break;
            case 'p':
               if (token.equals("pkgDefaultMethod") || token.equals("packageDefaultMethod")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod", severity, isEnabling);
                  return;
               }

               if (token.equals("paramAssign")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.parameterAssignment", severity, isEnabling);
                  return;
               }
               break;
            case 'r':
               if (token.equals("raw")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.rawTypeReference", severity, isEnabling);
                  return;
               }

               if (token.equals("redundantSuperinterface")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantSuperinterface", severity, isEnabling);
                  return;
               }

               if (token.equals("resource")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unclosedCloseable", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.potentiallyUnclosedCloseable", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.explicitlyClosedAutoCloseable", severity, isEnabling);
                  return;
               }
               break;
            case 's':
               if (token.equals("specialParamHiding")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.specialParameterHidingField", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("syntheticAccess") || token.equals("synthetic-access")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation", severity, isEnabling);
                  return;
               }

               if (token.equals("staticReceiver")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.staticAccessReceiver", severity, isEnabling);
                  return;
               }

               if (token.equals("syncOverride")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod", severity, isEnabling);
                  return;
               }

               if (token.equals("semicolon")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.emptyStatement", severity, isEnabling);
                  return;
               }

               if (token.equals("serial")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", severity, isEnabling);
                  return;
               }

               if (token.equals("suppress")) {
                  switch(severity) {
                     case 0:
                        this.options.put("org.eclipse.jdt.core.compiler.problem.suppressWarnings", isEnabling ? "enabled" : "disabled");
                        this.options.put("org.eclipse.jdt.core.compiler.problem.suppressOptionalErrors", "disabled");
                        break;
                     case 1:
                        this.options.put("org.eclipse.jdt.core.compiler.problem.suppressWarnings", isEnabling ? "enabled" : "disabled");
                        this.options.put("org.eclipse.jdt.core.compiler.problem.suppressOptionalErrors", isEnabling ? "enabled" : "disabled");
                  }

                  return;
               }

               if (token.equals("static-access")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.staticAccessReceiver", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.indirectStaticAccess", severity, isEnabling);
                  return;
               }

               if (token.equals("super")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.overridingMethodWithoutSuperInvocation", severity, isEnabling);
                  return;
               }

               if (token.equals("static-method")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic", severity, isEnabling);
                  return;
               }

               if (token.equals("switchDefault")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.missingDefaultCase", severity, isEnabling);
                  return;
               }

               if (token.equals("syntacticAnalysis")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.syntacticNullAnalysisForFields", isEnabling ? "enabled" : "disabled");
                  return;
               }
               break;
            case 't':
               if (token.startsWith("tasks")) {
                  String taskTags = Util.EMPTY_STRING;
                  int start = token.indexOf(40);
                  int end = token.indexOf(41);
                  if (start >= 0 && end >= 0 && start < end) {
                     taskTags = token.substring(start + 1, end).trim();
                     taskTags = taskTags.replace('|', ',');
                  }

                  if (taskTags.length() == 0) {
                     throw new IllegalArgumentException(this.bind("configure.invalidTaskTag", token));
                  }

                  this.options.put("org.eclipse.jdt.core.compiler.taskTags", isEnabling ? taskTags : Util.EMPTY_STRING);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.tasks", severity, isEnabling);
                  return;
               }

               if (token.equals("typeHiding")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.typeParameterHiding", severity, isEnabling);
                  return;
               }
               break;
            case 'u':
               if (token.equals("unusedLocal") || token.equals("unusedLocals")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedLocal", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedArgument") || token.equals("unusedArguments")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedParameter", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedExceptionParam")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedExceptionParameter", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedImport") || token.equals("unusedImports")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedImport", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedAllocation")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedObjectAllocation", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedPrivate")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedPrivateMember", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedLabel")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedLabel", severity, isEnabling);
                  return;
               }

               if (token.equals("uselessTypeCheck")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck", severity, isEnabling);
                  return;
               }

               if (token.equals("unchecked") || token.equals("unsafe")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation", severity, isEnabling);
                  return;
               }

               if (token.equals("unnecessaryElse")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unnecessaryElse", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedThrown")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedThrownWhenOverriding")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("unusedThrownIncludeDocComment")) {
                  this.options
                     .put("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("unusedThrownExemptExceptionThrowable")) {
                  this.options
                     .put(
                        "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable", isEnabling ? "enabled" : "disabled"
                     );
                  return;
               }

               if (token.equals("unqualifiedField") || token.equals("unqualified-field-access")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess", severity, isEnabling);
                  return;
               }

               if (token.equals("unused")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedLocal", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedParameter", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedImport", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedPrivateMember", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedLabel", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedTypeArgumentsForMethodInvocation", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantSpecificationOfTypeArguments", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedTypeParameter", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedParam")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedParameter", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedTypeParameter")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedTypeParameter", severity, isEnabling);
                  return;
               }

               if (token.equals("unusedParamIncludeDoc")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("unusedParamOverriding")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("unusedParamImplementing")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract", isEnabling ? "enabled" : "disabled");
                  return;
               }

               if (token.equals("unusedTypeArgs")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedTypeArgumentsForMethodInvocation", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.redundantSpecificationOfTypeArguments", severity, isEnabling);
                  return;
               }

               if (token.equals("unavoidableGenericProblems")) {
                  this.options.put("org.eclipse.jdt.core.compiler.problem.unavoidableGenericTypeProblems", isEnabling ? "enabled" : "disabled");
                  return;
               }
               break;
            case 'v':
               if (token.equals("varargsCast")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast", severity, isEnabling);
                  return;
               }
               break;
            case 'w':
               if (token.equals("warningToken")) {
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unhandledWarningToken", severity, isEnabling);
                  this.setSeverity("org.eclipse.jdt.core.compiler.problem.unusedWarningToken", severity, isEnabling);
                  return;
               }
         }

         String message = null;
         switch(severity) {
            case 0:
               message = this.bind("configure.invalidWarning", token);
               break;
            case 1:
               message = this.bind("configure.invalidError", token);
         }

         this.addPendingErrors(message);
      }
   }

   /** @deprecated */
   protected void initialize(PrintWriter outWriter, PrintWriter errWriter, boolean systemExit) {
      this.initialize(outWriter, errWriter, systemExit, null, null);
   }

   /** @deprecated */
   protected void initialize(PrintWriter outWriter, PrintWriter errWriter, boolean systemExit, Map customDefaultOptions) {
      this.initialize(outWriter, errWriter, systemExit, customDefaultOptions, null);
   }

   protected void initialize(
      PrintWriter outWriter, PrintWriter errWriter, boolean systemExit, Map<String, String> customDefaultOptions, CompilationProgress compilationProgress
   ) {
      this.logger = new Main.Logger(this, outWriter, errWriter);
      this.proceed = true;
      this.out = outWriter;
      this.err = errWriter;
      this.systemExitWhenFinished = systemExit;
      this.options = new CompilerOptions().getMap();
      this.ignoreOptionalProblemsFromFolders = null;
      this.progress = compilationProgress;
      if (customDefaultOptions != null) {
         this.didSpecifySource = customDefaultOptions.get("org.eclipse.jdt.core.compiler.source") != null;
         this.didSpecifyTarget = customDefaultOptions.get("org.eclipse.jdt.core.compiler.codegen.targetPlatform") != null;

         for(Entry<String, String> entry : customDefaultOptions.entrySet()) {
            this.options.put(entry.getKey(), entry.getValue());
         }
      } else {
         this.didSpecifySource = false;
         this.didSpecifyTarget = false;
      }

      this.classNames = null;
   }

   protected void initializeAnnotationProcessorManager() {
      String className = "org.eclipse.jdt.internal.compiler.apt.dispatch.BatchAnnotationProcessorManager";

      try {
         Class c = Class.forName(className);
         AbstractAnnotationProcessorManager annotationManager = (AbstractAnnotationProcessorManager)c.newInstance();
         annotationManager.configure(this, this.expandedCommandLine);
         annotationManager.setErr(this.err);
         annotationManager.setOut(this.out);
         this.batchCompiler.annotationProcessorManager = annotationManager;
      } catch (InstantiationException | ClassNotFoundException var4) {
         this.logger.logUnavaibleAPT(className);
         throw new AbortCompilation();
      } catch (IllegalAccessException var5) {
         throw new AbortCompilation();
      } catch (UnsupportedClassVersionError var6) {
         this.logger.logIncorrectVMVersionForAnnotationProcessing();
      }
   }

   private static boolean isParentOf(char[] folderName, char[] fileName) {
      if (folderName.length >= fileName.length) {
         return false;
      } else if (fileName[folderName.length] != '\\' && fileName[folderName.length] != '/') {
         return false;
      } else {
         for(int i = folderName.length - 1; i >= 0; --i) {
            if (folderName[i] != fileName[i]) {
               return false;
            }
         }

         return true;
      }
   }

   public void outputClassFiles(CompilationResult unitResult) {
      if (unitResult != null && (!unitResult.hasErrors() || this.proceedOnError)) {
         ClassFile[] classFiles = unitResult.getClassFiles();
         String currentDestinationPath = null;
         boolean generateClasspathStructure = false;
         CompilationUnit compilationUnit = (CompilationUnit)unitResult.compilationUnit;
         if (compilationUnit.destinationPath == null) {
            if (this.destinationPath == null) {
               currentDestinationPath = this.extractDestinationPathFromSourceFile(unitResult);
            } else if (this.destinationPath != "none") {
               currentDestinationPath = this.destinationPath;
               generateClasspathStructure = true;
            }
         } else if (compilationUnit.destinationPath != "none") {
            currentDestinationPath = compilationUnit.destinationPath;
            generateClasspathStructure = true;
         }

         if (currentDestinationPath != null) {
            int i = 0;

            for(int fileCount = classFiles.length; i < fileCount; ++i) {
               ClassFile classFile = classFiles[i];
               char[] filename = classFile.fileName();
               int length = filename.length;
               char[] relativeName = new char[length + 6];
               System.arraycopy(filename, 0, relativeName, 0, length);
               System.arraycopy(SuffixConstants.SUFFIX_class, 0, relativeName, length, 6);
               CharOperation.replace(relativeName, '/', File.separatorChar);
               String relativeStringName = new String(relativeName);

               try {
                  if (this.compilerOptions.verbose) {
                     this.out
                        .println(
                           Messages.bind(Messages.compilation_write, new String[]{String.valueOf(this.exportedClassFilesCounter + 1), relativeStringName})
                        );
                  }

                  Util.writeToDisk(generateClasspathStructure, currentDestinationPath, relativeStringName, classFile);
                  this.logger.logClassFile(generateClasspathStructure, currentDestinationPath, relativeStringName);
                  ++this.exportedClassFilesCounter;
               } catch (IOException var14) {
                  this.logger.logNoClassFileCreated(currentDestinationPath, relativeStringName, var14);
               }
            }

            this.batchCompiler.lookupEnvironment.releaseClassFiles(classFiles);
         }
      }
   }

   public void performCompilation() {
      this.startTime = System.currentTimeMillis();
      FileSystem environment = this.getLibraryAccess();
      this.compilerOptions = new CompilerOptions(this.options);
      this.compilerOptions.performMethodsFullRecovery = false;
      this.compilerOptions.performStatementsRecovery = false;
      this.batchCompiler = new Compiler(
         environment, this.getHandlingPolicy(), this.compilerOptions, this.getBatchRequestor(), this.getProblemFactory(), this.out, this.progress
      );
      this.batchCompiler.remainingIterations = this.maxRepetition - this.currentRepetition;
      String setting = System.getProperty("jdt.compiler.useSingleThread");
      this.batchCompiler.useSingleThread = setting != null && setting.equals("true");
      if (this.compilerOptions.complianceLevel >= 3276800L && this.compilerOptions.processAnnotations) {
         if (this.checkVMVersion(3276800L)) {
            this.initializeAnnotationProcessorManager();
            if (this.classNames != null) {
               this.batchCompiler.setBinaryTypes(this.processClassNames(this.batchCompiler.lookupEnvironment));
            }
         } else {
            this.logger.logIncorrectVMVersionForAnnotationProcessing();
         }
      }

      this.compilerOptions.verbose = this.verbose;
      this.compilerOptions.produceReferenceInfo = this.produceRefInfo;

      try {
         this.logger.startLoggingSources();
         this.batchCompiler.compile(this.getCompilationUnits());
      } finally {
         this.logger.endLoggingSources();
      }

      if (this.extraProblems != null) {
         this.loggingExtraProblems();
         this.extraProblems = null;
      }

      if (this.compilerStats != null) {
         this.compilerStats[this.currentRepetition] = this.batchCompiler.stats;
      }

      this.logger.printStats();
      environment.cleanup();
   }

   protected void loggingExtraProblems() {
      this.logger.loggingExtraProblems(this);
   }

   public void printUsage() {
      this.printUsage("misc.usage");
   }

   private void printUsage(String sectionID) {
      this.logger
         .logUsage(
            this.bind(
               sectionID,
               new String[]{System.getProperty("path.separator"), this.bind("compiler.name"), this.bind("compiler.version"), this.bind("compiler.copyright")}
            )
         );
      this.logger.flush();
   }

   private ReferenceBinding[] processClassNames(LookupEnvironment environment) {
      int length = this.classNames.length;
      ReferenceBinding[] referenceBindings = new ReferenceBinding[length];

      for(int i = 0; i < length; ++i) {
         String currentName = this.classNames[i];
         char[][] compoundName = null;
         if (currentName.indexOf(46) != -1) {
            char[] typeName = currentName.toCharArray();
            compoundName = CharOperation.splitOn('.', typeName);
         } else {
            compoundName = new char[][]{currentName.toCharArray()};
         }

         ReferenceBinding type = environment.getType(compoundName);
         if (type == null || !type.isValidBinding()) {
            throw new IllegalArgumentException(this.bind("configure.invalidClassName", currentName));
         }

         if (type.isBinaryBinding()) {
            referenceBindings[i] = type;
         }
      }

      return referenceBindings;
   }

   public void processPathEntries(
      int defaultSize, ArrayList paths, String currentPath, String customEncoding, boolean isSourceOnly, boolean rejectDestinationPathOnJars
   ) {
      String currentClasspathName = null;
      String currentDestinationPath = null;
      ArrayList currentRuleSpecs = new ArrayList(defaultSize);
      StringTokenizer tokenizer = new StringTokenizer(currentPath, File.pathSeparator + "[]", true);
      ArrayList tokens = new ArrayList();

      while(tokenizer.hasMoreTokens()) {
         tokens.add(tokenizer.nextToken());
      }

      int state = 0;
      String token = null;
      int cursor = 0;
      int tokensNb = tokens.size();
      int bracket = -1;

      while(cursor < tokensNb && state != 99) {
         token = (String)tokens.get(cursor++);
         if (token.equals(File.pathSeparator)) {
            switch(state) {
               case 0:
               case 3:
               case 10:
                  break;
               case 1:
               case 2:
               case 8:
                  state = 3;
                  this.addNewEntry(
                     paths, currentClasspathName, currentRuleSpecs, customEncoding, currentDestinationPath, isSourceOnly, rejectDestinationPathOnJars
                  );
                  currentRuleSpecs.clear();
                  break;
               case 4:
               case 5:
               case 9:
               default:
                  state = 99;
                  break;
               case 6:
                  state = 4;
                  break;
               case 7:
                  throw new IllegalArgumentException(this.bind("configure.incorrectDestinationPathEntry", currentPath));
               case 11:
                  cursor = bracket + 1;
                  state = 5;
            }
         } else if (token.equals("[")) {
            switch(state) {
               case 0:
                  currentClasspathName = "";
               case 1:
                  bracket = cursor - 1;
               case 11:
                  state = 10;
                  break;
               case 2:
                  state = 9;
                  break;
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 9:
               case 10:
               default:
                  state = 99;
                  break;
               case 8:
                  state = 5;
            }
         } else if (token.equals("]")) {
            switch(state) {
               case 6:
                  state = 2;
                  break;
               case 7:
                  state = 8;
                  break;
               case 8:
               case 9:
               case 11:
               default:
                  state = 99;
                  break;
               case 10:
                  state = 11;
            }
         } else {
            switch(state) {
               case 0:
               case 3:
                  state = 1;
                  currentClasspathName = token;
                  break;
               case 1:
               case 2:
               case 6:
               case 7:
               case 8:
               default:
                  state = 99;
                  break;
               case 5:
                  if (token.startsWith("-d ")) {
                     if (currentDestinationPath != null) {
                        throw new IllegalArgumentException(this.bind("configure.duplicateDestinationPathEntry", currentPath));
                     }

                     currentDestinationPath = token.substring(3).trim();
                     state = 7;
                     break;
                  }
               case 4:
                  if (currentDestinationPath != null) {
                     throw new IllegalArgumentException(this.bind("configure.accessRuleAfterDestinationPath", currentPath));
                  }

                  state = 6;
                  currentRuleSpecs.add(token);
                  break;
               case 9:
                  if (!token.startsWith("-d ")) {
                     state = 99;
                  } else {
                     currentDestinationPath = token.substring(3).trim();
                     state = 7;
                  }
               case 10:
                  break;
               case 11:
                  for(int i = bracket; i < cursor; ++i) {
                     currentClasspathName = currentClasspathName + (String)tokens.get(i);
                  }

                  state = 1;
            }
         }

         if (state == 11 && cursor == tokensNb) {
            cursor = bracket + 1;
            state = 5;
         }
      }

      switch(state) {
         case 1:
         case 2:
         case 8:
            this.addNewEntry(paths, currentClasspathName, currentRuleSpecs, customEncoding, currentDestinationPath, isSourceOnly, rejectDestinationPathOnJars);
         case 3:
            break;
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 11:
         default:
            if (currentPath.length() != 0) {
               this.addPendingErrors(this.bind("configure.incorrectClasspath", currentPath));
            }
      }
   }

   private int processPaths(String[] args, int index, String currentArg, ArrayList paths) {
      int localIndex = index;
      int count = 0;
      int i = 0;

      for(int max = currentArg.length(); i < max; ++i) {
         switch(currentArg.charAt(i)) {
            case '[':
               ++count;
            case '\\':
            default:
               break;
            case ']':
               --count;
         }
      }

      if (count == 0) {
         paths.add(currentArg);
         return index - index;
      } else if (count > 1) {
         throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
      } else {
         StringBuffer currentPath = new StringBuffer(currentArg);

         while(localIndex < args.length) {
            String nextArg = args[++localIndex];
            int ix = 0;

            for(int max = nextArg.length(); ix < max; ++ix) {
               switch(nextArg.charAt(ix)) {
                  case '[':
                     if (count > 1) {
                        throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", nextArg));
                     }

                     ++count;
                  case '\\':
                  default:
                     break;
                  case ']':
                     --count;
               }
            }

            if (count == 0) {
               currentPath.append(' ');
               currentPath.append(nextArg);
               paths.add(currentPath.toString());
               return localIndex - index;
            }

            if (count < 0) {
               throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", nextArg));
            }

            currentPath.append(' ');
            currentPath.append(nextArg);
         }

         throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
      }
   }

   private int processPaths(String[] args, int index, String currentArg, String[] paths) {
      int localIndex = index;
      int count = 0;
      int i = 0;

      for(int max = currentArg.length(); i < max; ++i) {
         switch(currentArg.charAt(i)) {
            case '[':
               ++count;
            case '\\':
            default:
               break;
            case ']':
               --count;
         }
      }

      if (count == 0) {
         paths[0] = currentArg;
         return index - index;
      } else {
         StringBuffer currentPath = new StringBuffer(currentArg);

         while(++localIndex < args.length) {
            String nextArg = args[localIndex];
            int ix = 0;

            for(int max = nextArg.length(); ix < max; ++ix) {
               switch(nextArg.charAt(ix)) {
                  case '[':
                     if (count > 1) {
                        throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
                     }

                     ++count;
                  case '\\':
                  default:
                     break;
                  case ']':
                     --count;
               }
            }

            if (count == 0) {
               currentPath.append(' ');
               currentPath.append(nextArg);
               paths[0] = currentPath.toString();
               return localIndex - index;
            }

            if (count < 0) {
               throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
            }

            currentPath.append(' ');
            currentPath.append(nextArg);
         }

         throw new IllegalArgumentException(this.bind("configure.unexpectedBracket", currentArg));
      }
   }

   public void relocalize() {
      this.relocalize(Locale.getDefault());
   }

   private void relocalize(Locale locale) {
      this.compilerLocale = locale;

      try {
         this.bundle = Main.ResourceBundleFactory.getBundle(locale);
      } catch (MissingResourceException var3) {
         System.out.println("Missing resource : " + "org.eclipse.jdt.internal.compiler.batch.messages".replace('.', '/') + ".properties for locale " + locale);
         throw var3;
      }
   }

   public void setDestinationPath(String dest) {
      this.destinationPath = dest;
   }

   public void setLocale(Locale locale) {
      this.relocalize(locale);
   }

   protected void setPaths(
      ArrayList bootclasspaths,
      String sourcepathClasspathArg,
      ArrayList sourcepathClasspaths,
      ArrayList classpaths,
      ArrayList extdirsClasspaths,
      ArrayList endorsedDirClasspaths,
      String customEncoding
   ) {
      bootclasspaths = this.handleBootclasspath(bootclasspaths, customEncoding);
      classpaths = this.handleClasspath(classpaths, customEncoding);
      if (sourcepathClasspathArg != null) {
         this.processPathEntries(4, sourcepathClasspaths, sourcepathClasspathArg, customEncoding, true, false);
      }

      extdirsClasspaths = this.handleExtdirs(extdirsClasspaths);
      endorsedDirClasspaths = this.handleEndorseddirs(endorsedDirClasspaths);
      bootclasspaths.addAll(0, endorsedDirClasspaths);
      bootclasspaths.addAll(extdirsClasspaths);
      bootclasspaths.addAll(sourcepathClasspaths);
      bootclasspaths.addAll(classpaths);
      classpaths = FileSystem.ClasspathNormalizer.normalize(bootclasspaths);
      this.checkedClasspaths = new FileSystem.Classpath[classpaths.size()];
      classpaths.toArray(this.checkedClasspaths);
      this.logger.logClasspath(this.checkedClasspaths);
      if (this.annotationPaths != null && "enabled".equals(this.options.get("org.eclipse.jdt.core.compiler.annotation.nullanalysis"))) {
         for(FileSystem.Classpath cp : this.checkedClasspaths) {
            if (cp instanceof ClasspathJar) {
               ((ClasspathJar)cp).annotationPaths = this.annotationPaths;
            }
         }
      }
   }

   private static boolean shouldIgnoreOptionalProblems(char[][] folderNames, char[] fileName) {
      if (folderNames != null && fileName != null) {
         int i = 0;

         for(int max = folderNames.length; i < max; ++i) {
            char[] folderName = folderNames[i];
            if (isParentOf(folderName, fileName)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   protected void validateOptions(boolean didSpecifyCompliance) {
      if (didSpecifyCompliance) {
         Object version = this.options.get("org.eclipse.jdt.core.compiler.compliance");
         if ("1.3".equals(version)) {
            if (!this.didSpecifySource) {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.3");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.1");
            }
         } else if ("1.4".equals(version)) {
            if (this.didSpecifySource) {
               Object source = this.options.get("org.eclipse.jdt.core.compiler.source");
               if ("1.3".equals(source)) {
                  if (!this.didSpecifyTarget) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.2");
                  }
               } else if ("1.4".equals(source) && !this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
               }
            } else {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.3");
               if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.2");
               }
            }
         } else if ("1.5".equals(version)) {
            if (this.didSpecifySource) {
               Object source = this.options.get("org.eclipse.jdt.core.compiler.source");
               if (!"1.3".equals(source) && !"1.4".equals(source)) {
                  if ("1.5".equals(source) && !this.didSpecifyTarget) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
                  }
               } else if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
               }
            } else {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.5");
               if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
               }
            }
         } else if ("1.6".equals(version)) {
            if (this.didSpecifySource) {
               Object source = this.options.get("org.eclipse.jdt.core.compiler.source");
               if (!"1.3".equals(source) && !"1.4".equals(source)) {
                  if (("1.5".equals(source) || "1.6".equals(source)) && !this.didSpecifyTarget) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
                  }
               } else if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
               }
            } else {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.6");
               if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
               }
            }
         } else if ("1.7".equals(version)) {
            if (this.didSpecifySource) {
               Object source = this.options.get("org.eclipse.jdt.core.compiler.source");
               if (!"1.3".equals(source) && !"1.4".equals(source)) {
                  if (!"1.5".equals(source) && !"1.6".equals(source)) {
                     if ("1.7".equals(source) && !this.didSpecifyTarget) {
                        this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7");
                     }
                  } else if (!this.didSpecifyTarget) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
                  }
               } else if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
               }
            } else {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.7");
               if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7");
               }
            }
         } else if ("1.8".equals(version)) {
            if (this.didSpecifySource) {
               Object source = this.options.get("org.eclipse.jdt.core.compiler.source");
               if (!"1.3".equals(source) && !"1.4".equals(source)) {
                  if (!"1.5".equals(source) && !"1.6".equals(source)) {
                     if ("1.7".equals(source)) {
                        if (!this.didSpecifyTarget) {
                           this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7");
                        }
                     } else if ("1.8".equals(source) && !this.didSpecifyTarget) {
                        this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8");
                     }
                  } else if (!this.didSpecifyTarget) {
                     this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
                  }
               } else if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
               }
            } else {
               this.options.put("org.eclipse.jdt.core.compiler.source", "1.8");
               if (!this.didSpecifyTarget) {
                  this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8");
               }
            }
         }
      } else if (this.didSpecifySource) {
         Object version = this.options.get("org.eclipse.jdt.core.compiler.source");
         if ("1.4".equals(version)) {
            if (!didSpecifyCompliance) {
               this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.4");
            }
         } else if ("1.5".equals(version)) {
            if (!didSpecifyCompliance) {
               this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.5");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
            }
         } else if ("1.6".equals(version)) {
            if (!didSpecifyCompliance) {
               this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.6");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.6");
            }
         } else if ("1.7".equals(version)) {
            if (!didSpecifyCompliance) {
               this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.7");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.7");
            }
         } else if ("1.8".equals(version)) {
            if (!didSpecifyCompliance) {
               this.options.put("org.eclipse.jdt.core.compiler.compliance", "1.8");
            }

            if (!this.didSpecifyTarget) {
               this.options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.8");
            }
         }
      }

      String sourceVersion = this.options.get("org.eclipse.jdt.core.compiler.source");
      String compliance = this.options.get("org.eclipse.jdt.core.compiler.compliance");
      if (sourceVersion.equals("1.8") && CompilerOptions.versionToJdkLevel(compliance) < 3407872L) {
         throw new IllegalArgumentException(
            this.bind("configure.incompatibleComplianceForSource", this.options.get("org.eclipse.jdt.core.compiler.compliance"), "1.8")
         );
      } else if (sourceVersion.equals("1.7") && CompilerOptions.versionToJdkLevel(compliance) < 3342336L) {
         throw new IllegalArgumentException(
            this.bind("configure.incompatibleComplianceForSource", this.options.get("org.eclipse.jdt.core.compiler.compliance"), "1.7")
         );
      } else if (sourceVersion.equals("1.6") && CompilerOptions.versionToJdkLevel(compliance) < 3276800L) {
         throw new IllegalArgumentException(
            this.bind("configure.incompatibleComplianceForSource", this.options.get("org.eclipse.jdt.core.compiler.compliance"), "1.6")
         );
      } else if (sourceVersion.equals("1.5") && CompilerOptions.versionToJdkLevel(compliance) < 3211264L) {
         throw new IllegalArgumentException(
            this.bind("configure.incompatibleComplianceForSource", this.options.get("org.eclipse.jdt.core.compiler.compliance"), "1.5")
         );
      } else if (sourceVersion.equals("1.4") && CompilerOptions.versionToJdkLevel(compliance) < 3145728L) {
         throw new IllegalArgumentException(
            this.bind("configure.incompatibleComplianceForSource", this.options.get("org.eclipse.jdt.core.compiler.compliance"), "1.4")
         );
      } else {
         if (this.didSpecifyTarget) {
            String targetVersion = this.options.get("org.eclipse.jdt.core.compiler.codegen.targetPlatform");
            if ("jsr14".equals(targetVersion)) {
               if (CompilerOptions.versionToJdkLevel(sourceVersion) < 3211264L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForGenericSource", targetVersion, sourceVersion));
               }
            } else if ("cldc1.1".equals(targetVersion)) {
               if (this.didSpecifySource && CompilerOptions.versionToJdkLevel(sourceVersion) >= 3145728L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleSourceForCldcTarget", targetVersion, sourceVersion));
               }

               if (CompilerOptions.versionToJdkLevel(compliance) >= 3211264L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleComplianceForCldcTarget", targetVersion, sourceVersion));
               }
            } else {
               if (CompilerOptions.versionToJdkLevel(sourceVersion) >= 3407872L && CompilerOptions.versionToJdkLevel(targetVersion) < 3407872L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForSource", targetVersion, "1.8"));
               }

               if (CompilerOptions.versionToJdkLevel(sourceVersion) >= 3342336L && CompilerOptions.versionToJdkLevel(targetVersion) < 3342336L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForSource", targetVersion, "1.7"));
               }

               if (CompilerOptions.versionToJdkLevel(sourceVersion) >= 3276800L && CompilerOptions.versionToJdkLevel(targetVersion) < 3276800L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForSource", targetVersion, "1.6"));
               }

               if (CompilerOptions.versionToJdkLevel(sourceVersion) >= 3211264L && CompilerOptions.versionToJdkLevel(targetVersion) < 3211264L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForSource", targetVersion, "1.5"));
               }

               if (CompilerOptions.versionToJdkLevel(sourceVersion) >= 3145728L && CompilerOptions.versionToJdkLevel(targetVersion) < 3145728L) {
                  throw new IllegalArgumentException(this.bind("configure.incompatibleTargetForSource", targetVersion, "1.4"));
               }

               if (CompilerOptions.versionToJdkLevel(compliance) < CompilerOptions.versionToJdkLevel(targetVersion)) {
                  throw new IllegalArgumentException(
                     this.bind("configure.incompatibleComplianceForTarget", this.options.get("org.eclipse.jdt.core.compiler.compliance"), targetVersion)
                  );
               }
            }
         }
      }
   }

   public static class Logger {
      private PrintWriter err;
      private PrintWriter log;
      private Main main;
      private PrintWriter out;
      private HashMap parameters;
      int tagBits;
      private static final String CLASS = "class";
      private static final String CLASS_FILE = "classfile";
      private static final String CLASSPATH = "classpath";
      private static final String CLASSPATH_FILE = "FILE";
      private static final String CLASSPATH_FOLDER = "FOLDER";
      private static final String CLASSPATH_ID = "id";
      private static final String CLASSPATH_JAR = "JAR";
      private static final String CLASSPATHS = "classpaths";
      private static final String COMMAND_LINE_ARGUMENT = "argument";
      private static final String COMMAND_LINE_ARGUMENTS = "command_line";
      private static final String COMPILER = "compiler";
      private static final String COMPILER_COPYRIGHT = "copyright";
      private static final String COMPILER_NAME = "name";
      private static final String COMPILER_VERSION = "version";
      public static final int EMACS = 2;
      private static final String ERROR = "ERROR";
      private static final String ERROR_TAG = "error";
      private static final String WARNING_TAG = "warning";
      private static final String EXCEPTION = "exception";
      private static final String EXTRA_PROBLEM_TAG = "extra_problem";
      private static final String EXTRA_PROBLEMS = "extra_problems";
      private static final HashtableOfInt FIELD_TABLE = new HashtableOfInt();
      private static final String KEY = "key";
      private static final String MESSAGE = "message";
      private static final String NUMBER_OF_CLASSFILES = "number_of_classfiles";
      private static final String NUMBER_OF_ERRORS = "errors";
      private static final String NUMBER_OF_LINES = "number_of_lines";
      private static final String NUMBER_OF_PROBLEMS = "problems";
      private static final String NUMBER_OF_TASKS = "tasks";
      private static final String NUMBER_OF_WARNINGS = "warnings";
      private static final String OPTION = "option";
      private static final String OPTIONS = "options";
      private static final String OUTPUT = "output";
      private static final String PACKAGE = "package";
      private static final String PATH = "path";
      private static final String PROBLEM_ARGUMENT = "argument";
      private static final String PROBLEM_ARGUMENT_VALUE = "value";
      private static final String PROBLEM_ARGUMENTS = "arguments";
      private static final String PROBLEM_CATEGORY_ID = "categoryID";
      private static final String ID = "id";
      private static final String PROBLEM_ID = "problemID";
      private static final String PROBLEM_LINE = "line";
      private static final String PROBLEM_OPTION_KEY = "optionKey";
      private static final String PROBLEM_MESSAGE = "message";
      private static final String PROBLEM_SEVERITY = "severity";
      private static final String PROBLEM_SOURCE_END = "charEnd";
      private static final String PROBLEM_SOURCE_START = "charStart";
      private static final String PROBLEM_SUMMARY = "problem_summary";
      private static final String PROBLEM_TAG = "problem";
      private static final String PROBLEMS = "problems";
      private static final String SOURCE = "source";
      private static final String SOURCE_CONTEXT = "source_context";
      private static final String SOURCE_END = "sourceEnd";
      private static final String SOURCE_START = "sourceStart";
      private static final String SOURCES = "sources";
      private static final String STATS = "stats";
      private static final String TASK = "task";
      private static final String TASKS = "tasks";
      private static final String TIME = "time";
      private static final String VALUE = "value";
      private static final String WARNING = "WARNING";
      public static final int XML = 1;
      private static final String XML_DTD_DECLARATION = "<!DOCTYPE compiler PUBLIC \"-//Eclipse.org//DTD Eclipse JDT 3.2.004 Compiler//EN\" \"http://www.eclipse.org/jdt/core/compiler_32_004.dtd\">";

      static {
         try {
            Class c = IProblem.class;
            Field[] fields = c.getFields();
            int i = 0;

            for(int max = fields.length; i < max; ++i) {
               Field field = fields[i];
               if (field.getType().equals(Integer.TYPE)) {
                  Integer value = (Integer)field.get(null);
                  int key2 = value & 16777215;
                  if (key2 == 0) {
                     key2 = Integer.MAX_VALUE;
                  }

                  FIELD_TABLE.put(key2, field.getName());
               }
            }
         } catch (SecurityException var7) {
            var7.printStackTrace();
         } catch (IllegalArgumentException var8) {
            var8.printStackTrace();
         } catch (IllegalAccessException var9) {
            var9.printStackTrace();
         }
      }

      public Logger(Main main, PrintWriter out, PrintWriter err) {
         this.out = out;
         this.err = err;
         this.parameters = new HashMap();
         this.main = main;
      }

      public String buildFileName(String outputPath, String relativeFileName) {
         char fileSeparatorChar = File.separatorChar;
         String fileSeparator = File.separator;
         outputPath = outputPath.replace('/', fileSeparatorChar);
         StringBuffer outDir = new StringBuffer(outputPath);
         if (!outputPath.endsWith(fileSeparator)) {
            outDir.append(fileSeparator);
         }

         StringTokenizer tokenizer = new StringTokenizer(relativeFileName, fileSeparator);

         String token;
         for(token = tokenizer.nextToken(); tokenizer.hasMoreTokens(); token = tokenizer.nextToken()) {
            outDir.append(token).append(fileSeparator);
         }

         return outDir.append(token).toString();
      }

      public void close() {
         if (this.log != null) {
            if ((this.tagBits & 1) != 0) {
               this.endTag("compiler");
               this.flush();
            }

            this.log.close();
         }
      }

      public void compiling() {
         this.printlnOut(this.main.bind("progress.compiling"));
      }

      private void endLoggingExtraProblems() {
         this.endTag("extra_problems");
      }

      private void endLoggingProblems() {
         this.endTag("problems");
      }

      public void endLoggingSource() {
         if ((this.tagBits & 1) != 0) {
            this.endTag("source");
         }
      }

      public void endLoggingSources() {
         if ((this.tagBits & 1) != 0) {
            this.endTag("sources");
         }
      }

      public void endLoggingTasks() {
         if ((this.tagBits & 1) != 0) {
            this.endTag("tasks");
         }
      }

      private void endTag(String name) {
         if (this.log != null) {
            ((GenericXMLWriter)this.log).endTag(name, true, true);
         }
      }

      private String errorReportSource(CategorizedProblem problem, char[] unitSource, int bits) {
         int startPosition = problem.getSourceStart();
         int endPosition = problem.getSourceEnd();
         if (unitSource == null && problem.getOriginatingFileName() != null) {
            try {
               unitSource = Util.getFileCharContent(new File(new String(problem.getOriginatingFileName())), null);
            } catch (IOException var12) {
            }
         }

         int length;
         if (startPosition <= endPosition && (startPosition >= 0 || endPosition >= 0) && unitSource != null && (length = unitSource.length) != 0) {
            StringBuffer errorBuffer = new StringBuffer();
            if ((bits & 2) == 0) {
               errorBuffer.append(' ').append(Messages.bind(Messages.problem_atLine, String.valueOf(problem.getSourceLineNumber())));
               errorBuffer.append(Util.LINE_SEPARATOR);
            }

            errorBuffer.append('\t');
            int begin = startPosition >= length ? length - 1 : startPosition;

            char c;
            while(begin > 0 && (c = unitSource[begin - 1]) != '\n' && c != '\r') {
               --begin;
            }

            int end = endPosition >= length ? length - 1 : endPosition;

            while(end + 1 < length && (c = unitSource[end + 1]) != '\r' && c != '\n') {
               ++end;
            }

            while((c = unitSource[begin]) == ' ' || c == '\t') {
               ++begin;
            }

            errorBuffer.append(unitSource, begin, end - begin + 1);
            errorBuffer.append(Util.LINE_SEPARATOR).append("\t");

            for(int i = begin; i < startPosition; ++i) {
               errorBuffer.append((char)(unitSource[i] == '\t' ? '\t' : ' '));
            }

            for(int i = startPosition; i <= (endPosition >= length ? length - 1 : endPosition); ++i) {
               errorBuffer.append('^');
            }

            return errorBuffer.toString();
         } else {
            return Messages.problem_noSourceInformation;
         }
      }

      private void extractContext(CategorizedProblem problem, char[] unitSource) {
         int startPosition = problem.getSourceStart();
         int endPosition = problem.getSourceEnd();
         if (unitSource == null && problem.getOriginatingFileName() != null) {
            try {
               unitSource = Util.getFileCharContent(new File(new String(problem.getOriginatingFileName())), null);
            } catch (IOException var10) {
            }
         }

         int length;
         if (startPosition <= endPosition
            && (startPosition >= 0 || endPosition >= 0)
            && unitSource != null
            && (length = unitSource.length) > 0
            && endPosition <= length) {
            int begin = startPosition >= length ? length - 1 : startPosition;

            char c;
            while(begin > 0 && (c = unitSource[begin - 1]) != '\n' && c != true) {
               --begin;
            }

            int end = endPosition >= length ? length - 1 : endPosition;

            while(end + 1 < length && (c = unitSource[end + 1]) != '\r' && c != true) {
               ++end;
            }

            while((c = unitSource[begin]) == ' ' || c == true) {
               ++begin;
            }

            while((c = unitSource[end]) == true || c == true) {
               --end;
            }

            StringBuffer buffer = new StringBuffer();
            buffer.append(unitSource, begin, end - begin + 1);
            this.parameters.put("value", String.valueOf(buffer));
            this.parameters.put("sourceStart", Integer.toString(startPosition - begin));
            this.parameters.put("sourceEnd", Integer.toString(endPosition - begin));
            this.printTag("source_context", this.parameters, true, true);
         } else {
            this.parameters.put("value", Messages.problem_noSourceInformation);
            this.parameters.put("sourceStart", "-1");
            this.parameters.put("sourceEnd", "-1");
            this.printTag("source_context", this.parameters, true, true);
         }
      }

      public void flush() {
         this.out.flush();
         this.err.flush();
         if (this.log != null) {
            this.log.flush();
         }
      }

      private String getFieldName(int id) {
         int key2 = id & 16777215;
         if (key2 == 0) {
            key2 = Integer.MAX_VALUE;
         }

         return (String)FIELD_TABLE.get(key2);
      }

      private String getProblemOptionKey(int problemID) {
         int irritant = ProblemReporter.getIrritant(problemID);
         return CompilerOptions.optionKeyFromIrritant(irritant);
      }

      public void logAverage() {
         Arrays.sort((Object[])this.main.compilerStats);
         long lineCount = this.main.compilerStats[0].lineCount;
         int length = this.main.maxRepetition;
         long sum = 0L;
         long parseSum = 0L;
         long resolveSum = 0L;
         long analyzeSum = 0L;
         long generateSum = 0L;
         int i = 1;

         for(int max = length - 1; i < max; ++i) {
            CompilerStats stats = this.main.compilerStats[i];
            sum += stats.elapsedTime();
            parseSum += stats.parseTime;
            resolveSum += stats.resolveTime;
            analyzeSum += stats.analyzeTime;
            generateSum += stats.generateTime;
         }

         long time = sum / (long)(length - 2);
         long parseTime = parseSum / (long)(length - 2);
         long resolveTime = resolveSum / (long)(length - 2);
         long analyzeTime = analyzeSum / (long)(length - 2);
         long generateTime = generateSum / (long)(length - 2);
         this.printlnOut(
            this.main
               .bind(
                  "compile.averageTime",
                  new String[]{
                     String.valueOf(lineCount), String.valueOf(time), String.valueOf((double)((int)((double)lineCount * 10000.0 / (double)time)) / 10.0)
                  }
               )
         );
         if ((this.main.timing & 2) != 0) {
            this.printlnOut(
               this.main
                  .bind(
                     "compile.detailedTime",
                     new String[]{
                        String.valueOf(parseTime),
                        String.valueOf((double)((int)((double)parseTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(resolveTime),
                        String.valueOf((double)((int)((double)resolveTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(analyzeTime),
                        String.valueOf((double)((int)((double)analyzeTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(generateTime),
                        String.valueOf((double)((int)((double)generateTime * 1000.0 / (double)time)) / 10.0)
                     }
                  )
            );
         }
      }

      public void logClassFile(boolean generatePackagesStructure, String outputPath, String relativeFileName) {
         if ((this.tagBits & 1) != 0) {
            String fileName = null;
            if (generatePackagesStructure) {
               fileName = this.buildFileName(outputPath, relativeFileName);
            } else {
               char fileSeparatorChar = File.separatorChar;
               String fileSeparator = File.separator;
               outputPath = outputPath.replace('/', fileSeparatorChar);
               int indexOfPackageSeparator = relativeFileName.lastIndexOf(fileSeparatorChar);
               if (indexOfPackageSeparator == -1) {
                  if (outputPath.endsWith(fileSeparator)) {
                     fileName = outputPath + relativeFileName;
                  } else {
                     fileName = outputPath + fileSeparator + relativeFileName;
                  }
               } else {
                  int length = relativeFileName.length();
                  if (outputPath.endsWith(fileSeparator)) {
                     fileName = outputPath + relativeFileName.substring(indexOfPackageSeparator + 1, length);
                  } else {
                     fileName = outputPath + fileSeparator + relativeFileName.substring(indexOfPackageSeparator + 1, length);
                  }
               }
            }

            File f = new File(fileName);

            try {
               this.parameters.put("path", f.getCanonicalPath());
               this.printTag("classfile", this.parameters, true, true);
            } catch (IOException var9) {
               this.logNoClassFileCreated(outputPath, relativeFileName, var9);
            }
         }
      }

      public void logClasspath(FileSystem.Classpath[] classpaths) {
         if (classpaths != null) {
            if ((this.tagBits & 1) != 0) {
               int length = classpaths.length;
               if (length != 0) {
                  this.printTag("classpaths", null, true, false);

                  for(int i = 0; i < length; ++i) {
                     String classpath = classpaths[i].getPath();
                     this.parameters.put("path", classpath);
                     File f = new File(classpath);
                     String id = null;
                     if (f.isFile()) {
                        if (Util.isPotentialZipArchive(classpath)) {
                           id = "JAR";
                        } else {
                           id = "FILE";
                        }
                     } else if (f.isDirectory()) {
                        id = "FOLDER";
                     }

                     if (id != null) {
                        this.parameters.put("id", id);
                        this.printTag("classpath", this.parameters, true, true);
                     }
                  }

                  this.endTag("classpaths");
               }
            }
         }
      }

      public void logCommandLineArguments(String[] commandLineArguments) {
         if (commandLineArguments != null) {
            if ((this.tagBits & 1) != 0) {
               int length = commandLineArguments.length;
               if (length != 0) {
                  this.printTag("command_line", null, true, false);

                  for(int i = 0; i < length; ++i) {
                     this.parameters.put("value", commandLineArguments[i]);
                     this.printTag("argument", this.parameters, true, true);
                  }

                  this.endTag("command_line");
               }
            }
         }
      }

      public void logException(Exception e) {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         e.printStackTrace(printWriter);
         printWriter.flush();
         printWriter.close();
         String stackTrace = writer.toString();
         if ((this.tagBits & 1) != 0) {
            LineNumberReader reader = new LineNumberReader(new StringReader(stackTrace));
            int i = 0;
            StringBuffer buffer = new StringBuffer();
            String message = e.getMessage();
            if (message != null) {
               buffer.append(message).append(Util.LINE_SEPARATOR);
            }

            try {
               String line;
               while((line = reader.readLine()) != null && i < 4) {
                  buffer.append(line).append(Util.LINE_SEPARATOR);
                  ++i;
               }

               reader.close();
            } catch (IOException var10) {
            }

            message = buffer.toString();
            this.parameters.put("message", message);
            this.parameters.put("class", e.getClass());
            this.printTag("exception", this.parameters, true, true);
         }

         String message = e.getMessage();
         if (message == null) {
            this.printlnErr(stackTrace);
         } else {
            this.printlnErr(message);
         }
      }

      private void logExtraProblem(CategorizedProblem problem, int localErrorCount, int globalErrorCount) {
         char[] originatingFileName = problem.getOriginatingFileName();
         if (originatingFileName == null) {
            if (problem.isError()) {
               this.printErr(this.main.bind("requestor.extraerror", Integer.toString(globalErrorCount)));
            } else {
               this.printErr(this.main.bind("requestor.extrawarning", Integer.toString(globalErrorCount)));
            }

            this.printErr(" ");
            this.printlnErr(problem.getMessage());
         } else {
            String fileName = new String(originatingFileName);
            if ((this.tagBits & 2) != 0) {
               String result = fileName
                  + ":"
                  + problem.getSourceLineNumber()
                  + ": "
                  + (problem.isError() ? this.main.bind("output.emacs.error") : this.main.bind("output.emacs.warning"))
                  + ": "
                  + problem.getMessage();
               this.printlnErr(result);
               String errorReportSource = this.errorReportSource(problem, null, this.tagBits);
               this.printlnErr(errorReportSource);
            } else {
               if (localErrorCount == 0) {
                  this.printlnErr("----------");
               }

               this.printErr(
                  problem.isError()
                     ? this.main.bind("requestor.error", Integer.toString(globalErrorCount), new String(fileName))
                     : this.main.bind("requestor.warning", Integer.toString(globalErrorCount), new String(fileName))
               );
               String errorReportSource = this.errorReportSource(problem, null, 0);
               this.printlnErr(errorReportSource);
               this.printlnErr(problem.getMessage());
               this.printlnErr("----------");
            }
         }
      }

      public void loggingExtraProblems(Main currentMain) {
         ArrayList problems = currentMain.extraProblems;
         int count = problems.size();
         int localProblemCount = 0;
         if (count != 0) {
            int errors = 0;
            int warnings = 0;

            for(int i = 0; i < count; ++i) {
               CategorizedProblem problem = (CategorizedProblem)problems.get(i);
               if (problem != null) {
                  ++currentMain.globalProblemsCount;
                  this.logExtraProblem(problem, localProblemCount, currentMain.globalProblemsCount);
                  ++localProblemCount;
                  if (problem.isError()) {
                     ++errors;
                     ++currentMain.globalErrorsCount;
                  } else if (problem.isWarning()) {
                     ++currentMain.globalWarningsCount;
                     ++warnings;
                  }
               }
            }

            if ((this.tagBits & 1) != 0 && errors + warnings != 0) {
               this.startLoggingExtraProblems(count);

               for(int i = 0; i < count; ++i) {
                  CategorizedProblem problem = (CategorizedProblem)problems.get(i);
                  if (problem != null && problem.getID() != 536871362) {
                     this.logXmlExtraProblem(problem, localProblemCount, currentMain.globalProblemsCount);
                  }
               }

               this.endLoggingExtraProblems();
            }
         }
      }

      public void logUnavaibleAPT(String className) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", this.main.bind("configure.unavailableAPT", className));
            this.printTag("error", this.parameters, true, true);
         }

         this.printlnErr(this.main.bind("configure.unavailableAPT", className));
      }

      public void logIncorrectVMVersionForAnnotationProcessing() {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", this.main.bind("configure.incorrectVMVersionforAPT"));
            this.printTag("error", this.parameters, true, true);
         }

         this.printlnErr(this.main.bind("configure.incorrectVMVersionforAPT"));
      }

      public void logNoClassFileCreated(String outputDir, String relativeFileName, IOException e) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", this.main.bind("output.noClassFileCreated", new String[]{outputDir, relativeFileName, e.getMessage()}));
            this.printTag("error", this.parameters, true, true);
         }

         this.printlnErr(this.main.bind("output.noClassFileCreated", new String[]{outputDir, relativeFileName, e.getMessage()}));
      }

      public void logNumberOfClassFilesGenerated(int exportedClassFilesCounter) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("value", exportedClassFilesCounter);
            this.printTag("number_of_classfiles", this.parameters, true, true);
         }

         if (exportedClassFilesCounter == 1) {
            this.printlnOut(this.main.bind("compile.oneClassFileGenerated"));
         } else {
            this.printlnOut(this.main.bind("compile.severalClassFilesGenerated", String.valueOf(exportedClassFilesCounter)));
         }
      }

      public void logOptions(Map<String, String> options) {
         if ((this.tagBits & 1) != 0) {
            this.printTag("options", null, true, false);
            Set<Entry<String, String>> entriesSet = options.entrySet();
            Entry[] entries = entriesSet.toArray(new Entry[entriesSet.size()]);
            Arrays.sort(entries, new Comparator<Entry<String, String>>() {
               public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                  return o1.getKey().compareTo(o2.getKey());
               }
            });
            int i = 0;

            for(int max = entries.length; i < max; ++i) {
               Entry<String, String> entry = entries[i];
               String key = entry.getKey();
               this.parameters.put("key", key);
               this.parameters.put("value", entry.getValue());
               this.printTag("option", this.parameters, true, true);
            }

            this.endTag("options");
         }
      }

      public void logPendingError(String error) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", error);
            this.printTag("error", this.parameters, true, true);
         }

         this.printlnErr(error);
      }

      public void logWarning(String message) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", message);
            this.printTag("warning", this.parameters, true, true);
         }

         this.printlnOut(message);
      }

      private void logProblem(CategorizedProblem problem, int localErrorCount, int globalErrorCount, char[] unitSource) {
         if ((this.tagBits & 2) != 0) {
            String result = new String(problem.getOriginatingFileName())
               + ":"
               + problem.getSourceLineNumber()
               + ": "
               + (problem.isError() ? this.main.bind("output.emacs.error") : this.main.bind("output.emacs.warning"))
               + ": "
               + problem.getMessage();
            this.printlnErr(result);
            String errorReportSource = this.errorReportSource(problem, unitSource, this.tagBits);
            if (errorReportSource.length() != 0) {
               this.printlnErr(errorReportSource);
            }
         } else {
            if (localErrorCount == 0) {
               this.printlnErr("----------");
            }

            this.printErr(
               problem.isError()
                  ? this.main.bind("requestor.error", Integer.toString(globalErrorCount), new String(problem.getOriginatingFileName()))
                  : this.main.bind("requestor.warning", Integer.toString(globalErrorCount), new String(problem.getOriginatingFileName()))
            );

            try {
               String errorReportSource = this.errorReportSource(problem, unitSource, 0);
               this.printlnErr(errorReportSource);
               this.printlnErr(problem.getMessage());
            } catch (Exception var7) {
               this.printlnErr(this.main.bind("requestor.notRetrieveErrorMessage", problem.toString()));
            }

            this.printlnErr("----------");
         }
      }

      public int logProblems(CategorizedProblem[] problems, char[] unitSource, Main currentMain) {
         int count = problems.length;
         int localErrorCount = 0;
         int localProblemCount = 0;
         if (count != 0) {
            int errors = 0;
            int warnings = 0;
            int tasks = 0;

            for(int i = 0; i < count; ++i) {
               CategorizedProblem problem = problems[i];
               if (problem != null) {
                  ++currentMain.globalProblemsCount;
                  this.logProblem(problem, localProblemCount, currentMain.globalProblemsCount, unitSource);
                  ++localProblemCount;
                  if (problem.isError()) {
                     ++localErrorCount;
                     ++errors;
                     ++currentMain.globalErrorsCount;
                  } else if (problem.getID() == 536871362) {
                     ++currentMain.globalTasksCount;
                     ++tasks;
                  } else {
                     ++currentMain.globalWarningsCount;
                     ++warnings;
                  }
               }
            }

            if ((this.tagBits & 1) != 0) {
               if (errors + warnings != 0) {
                  this.startLoggingProblems(errors, warnings);

                  for(int i = 0; i < count; ++i) {
                     CategorizedProblem problem = problems[i];
                     if (problem != null && problem.getID() != 536871362) {
                        this.logXmlProblem(problem, unitSource);
                     }
                  }

                  this.endLoggingProblems();
               }

               if (tasks != 0) {
                  this.startLoggingTasks(tasks);

                  for(int i = 0; i < count; ++i) {
                     CategorizedProblem problem = problems[i];
                     if (problem != null && problem.getID() == 536871362) {
                        this.logXmlTask(problem, unitSource);
                     }
                  }

                  this.endLoggingTasks();
               }
            }
         }

         return localErrorCount;
      }

      public void logProblemsSummary(int globalProblemsCount, int globalErrorsCount, int globalWarningsCount, int globalTasksCount) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("problems", globalProblemsCount);
            this.parameters.put("errors", globalErrorsCount);
            this.parameters.put("warnings", globalWarningsCount);
            this.parameters.put("tasks", globalTasksCount);
            this.printTag("problem_summary", this.parameters, true, true);
         }

         if (globalProblemsCount == 1) {
            String message = null;
            if (globalErrorsCount == 1) {
               message = this.main.bind("compile.oneError");
            } else {
               message = this.main.bind("compile.oneWarning");
            }

            this.printErr(this.main.bind("compile.oneProblem", message));
         } else {
            String errorMessage = null;
            String warningMessage = null;
            if (globalErrorsCount > 0) {
               if (globalErrorsCount == 1) {
                  errorMessage = this.main.bind("compile.oneError");
               } else {
                  errorMessage = this.main.bind("compile.severalErrors", String.valueOf(globalErrorsCount));
               }
            }

            int warningsNumber = globalWarningsCount + globalTasksCount;
            if (warningsNumber > 0) {
               if (warningsNumber == 1) {
                  warningMessage = this.main.bind("compile.oneWarning");
               } else {
                  warningMessage = this.main.bind("compile.severalWarnings", String.valueOf(warningsNumber));
               }
            }

            if (errorMessage != null && warningMessage != null) {
               this.printErr(
                  this.main.bind("compile.severalProblemsErrorsAndWarnings", new String[]{String.valueOf(globalProblemsCount), errorMessage, warningMessage})
               );
            } else if (errorMessage == null) {
               this.printErr(this.main.bind("compile.severalProblemsErrorsOrWarnings", String.valueOf(globalProblemsCount), warningMessage));
            } else {
               this.printErr(this.main.bind("compile.severalProblemsErrorsOrWarnings", String.valueOf(globalProblemsCount), errorMessage));
            }
         }

         if ((this.tagBits & 1) == 0) {
            this.printlnErr();
         }
      }

      public void logProgress() {
         this.printOut('.');
      }

      public void logRepetition(int i, int repetitions) {
         this.printlnOut(this.main.bind("compile.repetition", String.valueOf(i + 1), String.valueOf(repetitions)));
      }

      public void logTiming(CompilerStats compilerStats) {
         long time = compilerStats.elapsedTime();
         long lineCount = compilerStats.lineCount;
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("value", time);
            this.printTag("time", this.parameters, true, true);
            this.parameters.put("value", lineCount);
            this.printTag("number_of_lines", this.parameters, true, true);
         }

         if (lineCount != 0L) {
            this.printlnOut(
               this.main
                  .bind(
                     "compile.instantTime",
                     new String[]{
                        String.valueOf(lineCount), String.valueOf(time), String.valueOf((double)((int)((double)lineCount * 10000.0 / (double)time)) / 10.0)
                     }
                  )
            );
         } else {
            this.printlnOut(this.main.bind("compile.totalTime", new String[]{String.valueOf(time)}));
         }

         if ((this.main.timing & 2) != 0) {
            this.printlnOut(
               this.main
                  .bind(
                     "compile.detailedTime",
                     new String[]{
                        String.valueOf(compilerStats.parseTime),
                        String.valueOf((double)((int)((double)compilerStats.parseTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(compilerStats.resolveTime),
                        String.valueOf((double)((int)((double)compilerStats.resolveTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(compilerStats.analyzeTime),
                        String.valueOf((double)((int)((double)compilerStats.analyzeTime * 1000.0 / (double)time)) / 10.0),
                        String.valueOf(compilerStats.generateTime),
                        String.valueOf((double)((int)((double)compilerStats.generateTime * 1000.0 / (double)time)) / 10.0)
                     }
                  )
            );
         }
      }

      public void logUsage(String usage) {
         this.printlnOut(usage);
      }

      public void logVersion(boolean printToOut) {
         if (this.log != null && (this.tagBits & 1) == 0) {
            String version = this.main
               .bind("misc.version", new String[]{this.main.bind("compiler.name"), this.main.bind("compiler.version"), this.main.bind("compiler.copyright")});
            this.log.println("# " + version);
            if (printToOut) {
               this.out.println(version);
               this.out.flush();
            }
         } else if (printToOut) {
            String version = this.main
               .bind("misc.version", new String[]{this.main.bind("compiler.name"), this.main.bind("compiler.version"), this.main.bind("compiler.copyright")});
            this.out.println(version);
            this.out.flush();
         }
      }

      public void logWrongJDK() {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("message", this.main.bind("configure.requiresJDK1.2orAbove"));
            this.printTag("ERROR", this.parameters, true, true);
         }

         this.printlnErr(this.main.bind("configure.requiresJDK1.2orAbove"));
      }

      private void logXmlExtraProblem(CategorizedProblem problem, int globalErrorCount, int localErrorCount) {
         int sourceStart = problem.getSourceStart();
         int sourceEnd = problem.getSourceEnd();
         boolean isError = problem.isError();
         this.parameters.put("severity", isError ? "ERROR" : "WARNING");
         this.parameters.put("line", problem.getSourceLineNumber());
         this.parameters.put("charStart", sourceStart);
         this.parameters.put("charEnd", sourceEnd);
         this.printTag("extra_problem", this.parameters, true, false);
         this.parameters.put("value", problem.getMessage());
         this.printTag("message", this.parameters, true, true);
         this.extractContext(problem, null);
         this.endTag("extra_problem");
      }

      private void logXmlProblem(CategorizedProblem problem, char[] unitSource) {
         int sourceStart = problem.getSourceStart();
         int sourceEnd = problem.getSourceEnd();
         int id = problem.getID();
         this.parameters.put("id", this.getFieldName(id));
         this.parameters.put("problemID", id);
         boolean isError = problem.isError();
         int severity = isError ? 1 : 0;
         this.parameters.put("severity", isError ? "ERROR" : "WARNING");
         this.parameters.put("line", problem.getSourceLineNumber());
         this.parameters.put("charStart", sourceStart);
         this.parameters.put("charEnd", sourceEnd);
         String problemOptionKey = this.getProblemOptionKey(id);
         if (problemOptionKey != null) {
            this.parameters.put("optionKey", problemOptionKey);
         }

         int categoryID = ProblemReporter.getProblemCategory(severity, id);
         this.parameters.put("categoryID", categoryID);
         this.printTag("problem", this.parameters, true, false);
         this.parameters.put("value", problem.getMessage());
         this.printTag("message", this.parameters, true, true);
         this.extractContext(problem, unitSource);
         String[] arguments = problem.getArguments();
         int length = arguments.length;
         if (length != 0) {
            this.printTag("arguments", null, true, false);

            for(int i = 0; i < length; ++i) {
               this.parameters.put("value", arguments[i]);
               this.printTag("argument", this.parameters, true, true);
            }

            this.endTag("arguments");
         }

         this.endTag("problem");
      }

      private void logXmlTask(CategorizedProblem problem, char[] unitSource) {
         this.parameters.put("line", problem.getSourceLineNumber());
         this.parameters.put("charStart", problem.getSourceStart());
         this.parameters.put("charEnd", problem.getSourceEnd());
         String problemOptionKey = this.getProblemOptionKey(problem.getID());
         if (problemOptionKey != null) {
            this.parameters.put("optionKey", problemOptionKey);
         }

         this.printTag("task", this.parameters, true, false);
         this.parameters.put("value", problem.getMessage());
         this.printTag("message", this.parameters, true, true);
         this.extractContext(problem, unitSource);
         this.endTag("task");
      }

      private void printErr(String s) {
         this.err.print(s);
         if ((this.tagBits & 1) == 0 && this.log != null) {
            this.log.print(s);
         }
      }

      private void printlnErr() {
         this.err.println();
         if ((this.tagBits & 1) == 0 && this.log != null) {
            this.log.println();
         }
      }

      private void printlnErr(String s) {
         this.err.println(s);
         if ((this.tagBits & 1) == 0 && this.log != null) {
            this.log.println(s);
         }
      }

      private void printlnOut(String s) {
         this.out.println(s);
         if ((this.tagBits & 1) == 0 && this.log != null) {
            this.log.println(s);
         }
      }

      public void printNewLine() {
         this.out.println();
      }

      private void printOut(char c) {
         this.out.print(c);
      }

      public void printStats() {
         boolean isTimed = (this.main.timing & 1) != 0;
         if ((this.tagBits & 1) != 0) {
            this.printTag("stats", null, true, false);
         }

         if (isTimed) {
            CompilerStats compilerStats = this.main.batchCompiler.stats;
            compilerStats.startTime = this.main.startTime;
            compilerStats.endTime = System.currentTimeMillis();
            this.logTiming(compilerStats);
         }

         if (this.main.globalProblemsCount > 0) {
            this.logProblemsSummary(this.main.globalProblemsCount, this.main.globalErrorsCount, this.main.globalWarningsCount, this.main.globalTasksCount);
         }

         if (this.main.exportedClassFilesCounter != 0 && (this.main.showProgress || isTimed || this.main.verbose)) {
            this.logNumberOfClassFilesGenerated(this.main.exportedClassFilesCounter);
         }

         if ((this.tagBits & 1) != 0) {
            this.endTag("stats");
         }
      }

      private void printTag(String name, HashMap params, boolean insertNewLine, boolean closeTag) {
         if (this.log != null) {
            ((GenericXMLWriter)this.log).printTag(name, this.parameters, true, insertNewLine, closeTag);
         }

         this.parameters.clear();
      }

      public void setEmacs() {
         this.tagBits |= 2;
      }

      public void setLog(String logFileName) {
         Date date = new Date();
         DateFormat dateFormat = DateFormat.getDateTimeInstance(3, 1, Locale.getDefault());

         try {
            int index = logFileName.lastIndexOf(46);
            if (index != -1) {
               if (logFileName.substring(index).toLowerCase().equals(".xml")) {
                  this.log = new GenericXMLWriter(new OutputStreamWriter(new FileOutputStream(logFileName, false), "UTF-8"), Util.LINE_SEPARATOR, true);
                  this.tagBits |= 1;
                  this.log.println("<!-- " + dateFormat.format(date) + " -->");
                  this.log
                     .println(
                        "<!DOCTYPE compiler PUBLIC \"-//Eclipse.org//DTD Eclipse JDT 3.2.004 Compiler//EN\" \"http://www.eclipse.org/jdt/core/compiler_32_004.dtd\">"
                     );
                  this.parameters.put("name", this.main.bind("compiler.name"));
                  this.parameters.put("version", this.main.bind("compiler.version"));
                  this.parameters.put("copyright", this.main.bind("compiler.copyright"));
                  this.printTag("compiler", this.parameters, true, false);
               } else {
                  this.log = new PrintWriter(new FileOutputStream(logFileName, false));
                  this.log.println("# " + dateFormat.format(date));
               }
            } else {
               this.log = new PrintWriter(new FileOutputStream(logFileName, false));
               this.log.println("# " + dateFormat.format(date));
            }
         } catch (FileNotFoundException var5) {
            throw new IllegalArgumentException(this.main.bind("configure.cannotOpenLog", logFileName));
         } catch (UnsupportedEncodingException var6) {
            throw new IllegalArgumentException(this.main.bind("configure.cannotOpenLogInvalidEncoding", logFileName));
         }
      }

      private void startLoggingExtraProblems(int count) {
         this.parameters.put("problems", count);
         this.printTag("extra_problems", this.parameters, true, false);
      }

      private void startLoggingProblems(int errors, int warnings) {
         this.parameters.put("problems", errors + warnings);
         this.parameters.put("errors", errors);
         this.parameters.put("warnings", warnings);
         this.printTag("problems", this.parameters, true, false);
      }

      public void startLoggingSource(CompilationResult compilationResult) {
         if ((this.tagBits & 1) != 0) {
            ICompilationUnit compilationUnit = compilationResult.compilationUnit;
            if (compilationUnit != null) {
               char[] fileName = compilationUnit.getFileName();
               File f = new File(new String(fileName));
               if (fileName != null) {
                  this.parameters.put("path", f.getAbsolutePath());
               }

               char[][] packageName = compilationResult.packageName;
               if (packageName != null) {
                  this.parameters.put("package", new String(CharOperation.concatWith(packageName, File.separatorChar)));
               }

               CompilationUnit unit = (CompilationUnit)compilationUnit;
               String destinationPath = unit.destinationPath;
               if (destinationPath == null) {
                  destinationPath = this.main.destinationPath;
               }

               if (destinationPath != null && destinationPath != "none") {
                  if (File.separatorChar == '/') {
                     this.parameters.put("output", destinationPath);
                  } else {
                     this.parameters.put("output", destinationPath.replace('/', File.separatorChar));
                  }
               }
            }

            this.printTag("source", this.parameters, true, false);
         }
      }

      public void startLoggingSources() {
         if ((this.tagBits & 1) != 0) {
            this.printTag("sources", null, true, false);
         }
      }

      public void startLoggingTasks(int tasks) {
         if ((this.tagBits & 1) != 0) {
            this.parameters.put("tasks", tasks);
            this.printTag("tasks", this.parameters, true, false);
         }
      }
   }

   public static class ResourceBundleFactory {
      private static HashMap Cache = new HashMap();

      public static synchronized ResourceBundle getBundle(Locale locale) {
         ResourceBundle bundle = (ResourceBundle)Cache.get(locale);
         if (bundle == null) {
            bundle = ResourceBundle.getBundle("org.eclipse.jdt.internal.compiler.batch.messages", locale);
            Cache.put(locale, bundle);
         }

         return bundle;
      }
   }
}
