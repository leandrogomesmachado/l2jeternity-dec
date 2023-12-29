package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class FileSystem implements INameEnvironment, SuffixConstants {
   protected FileSystem.Classpath[] classpaths;
   Set knownFileNames;
   protected boolean annotationsFromClasspath;

   public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
      int classpathSize = classpathNames.length;
      this.classpaths = new FileSystem.Classpath[classpathSize];
      int counter = 0;

      for(int i = 0; i < classpathSize; ++i) {
         FileSystem.Classpath classpath = getClasspath(classpathNames[i], encoding, null, null);

         try {
            classpath.initialize();
            this.classpaths[counter++] = classpath;
         } catch (IOException var8) {
         }
      }

      if (counter != classpathSize) {
         System.arraycopy(this.classpaths, 0, this.classpaths = new FileSystem.Classpath[counter], 0, counter);
      }

      this.initializeKnownFileNames(initialFileNames);
   }

   protected FileSystem(FileSystem.Classpath[] paths, String[] initialFileNames, boolean annotationsFromClasspath) {
      int length = paths.length;
      int counter = 0;
      this.classpaths = new FileSystem.Classpath[length];

      for(int i = 0; i < length; ++i) {
         FileSystem.Classpath classpath = paths[i];

         try {
            classpath.initialize();
            this.classpaths[counter++] = classpath;
         } catch (IOException var8) {
         }
      }

      if (counter != length) {
         System.arraycopy(this.classpaths, 0, this.classpaths = new FileSystem.Classpath[counter], 0, counter);
      }

      this.initializeKnownFileNames(initialFileNames);
      this.annotationsFromClasspath = annotationsFromClasspath;
   }

   public static FileSystem.Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet) {
      return getClasspath(classpathName, encoding, false, accessRuleSet, null, null);
   }

   public static FileSystem.Classpath getClasspath(String classpathName, String encoding, AccessRuleSet accessRuleSet, Map options) {
      return getClasspath(classpathName, encoding, false, accessRuleSet, null, options);
   }

   public static FileSystem.Classpath getClasspath(
      String classpathName, String encoding, boolean isSourceOnly, AccessRuleSet accessRuleSet, String destinationPath, Map options
   ) {
      FileSystem.Classpath result = null;
      File file = new File(convertPathSeparators(classpathName));
      if (file.isDirectory()) {
         if (file.exists()) {
            result = new ClasspathDirectory(
               file,
               encoding,
               isSourceOnly ? 1 : 3,
               accessRuleSet,
               destinationPath != null && destinationPath != "none" ? convertPathSeparators(destinationPath) : destinationPath,
               options
            );
         }
      } else if (Util.isPotentialZipArchive(classpathName)) {
         if (isSourceOnly) {
            result = new ClasspathSourceJar(
               file,
               true,
               accessRuleSet,
               encoding,
               destinationPath != null && destinationPath != "none" ? convertPathSeparators(destinationPath) : destinationPath
            );
         } else if (destinationPath == null) {
            result = new ClasspathJar(file, true, accessRuleSet, null);
         }
      }

      return result;
   }

   private void initializeKnownFileNames(String[] initialFileNames) {
      if (initialFileNames == null) {
         this.knownFileNames = new HashSet(0);
      } else {
         this.knownFileNames = new HashSet(initialFileNames.length * 2);
         int i = initialFileNames.length;

         while(--i >= 0) {
            File compilationUnitFile = new File(initialFileNames[i]);
            char[] fileName = null;

            try {
               fileName = compilationUnitFile.getCanonicalPath().toCharArray();
            } catch (IOException var12) {
               continue;
            }

            char[] matchingPathName = null;
            int lastIndexOf = CharOperation.lastIndexOf('.', fileName);
            if (lastIndexOf != -1) {
               fileName = CharOperation.subarray(fileName, 0, lastIndexOf);
            }

            CharOperation.replace(fileName, '\\', '/');
            boolean globalPathMatches = false;
            int j = 0;

            for(int max = this.classpaths.length; j < max; ++j) {
               char[] matchCandidate = this.classpaths[j].normalizedPath();
               boolean currentPathMatch = false;
               if (this.classpaths[j] instanceof ClasspathDirectory && CharOperation.prefixEquals(matchCandidate, fileName)) {
                  currentPathMatch = true;
                  if (matchingPathName == null) {
                     matchingPathName = matchCandidate;
                  } else if (currentPathMatch) {
                     if (matchCandidate.length > matchingPathName.length) {
                        matchingPathName = matchCandidate;
                     }
                  } else if (!globalPathMatches && matchCandidate.length < matchingPathName.length) {
                     matchingPathName = matchCandidate;
                  }

                  if (currentPathMatch) {
                     globalPathMatches = true;
                  }
               }
            }

            if (matchingPathName == null) {
               this.knownFileNames.add(new String(fileName));
            } else {
               this.knownFileNames.add(new String(CharOperation.subarray(fileName, matchingPathName.length, fileName.length)));
            }

            Object var14 = null;
         }
      }
   }

   @Override
   public void cleanup() {
      int i = 0;

      for(int max = this.classpaths.length; i < max; ++i) {
         this.classpaths[i].reset();
      }
   }

   private static String convertPathSeparators(String path) {
      return File.separatorChar == '/' ? path.replace('\\', '/') : path.replace('/', '\\');
   }

   private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly) {
      NameEnvironmentAnswer answer = this.internalFindClass(qualifiedTypeName, typeName, asBinaryOnly);
      if (this.annotationsFromClasspath && answer != null && answer.getBinaryType() instanceof ClassFileReader) {
         int i = 0;

         for(int length = this.classpaths.length; i < length; ++i) {
            FileSystem.Classpath classpathEntry = this.classpaths[i];
            if (classpathEntry.hasAnnotationFileFor(qualifiedTypeName)) {
               ZipFile zip = classpathEntry instanceof ClasspathJar ? ((ClasspathJar)classpathEntry).zipFile : null;

               try {
                  ((ClassFileReader)answer.getBinaryType()).setExternalAnnotationProvider(classpathEntry.getPath(), qualifiedTypeName, zip, null);
                  break;
               } catch (IOException var9) {
               }
            }
         }
      }

      return answer;
   }

   private NameEnvironmentAnswer internalFindClass(String qualifiedTypeName, char[] typeName, boolean asBinaryOnly) {
      if (this.knownFileNames.contains(qualifiedTypeName)) {
         return null;
      } else {
         String qualifiedBinaryFileName = qualifiedTypeName + ".class";
         String qualifiedPackageName = qualifiedTypeName.length() == typeName.length
            ? Util.EMPTY_STRING
            : qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
         String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
         NameEnvironmentAnswer suggestedAnswer = null;
         if (qualifiedPackageName == qp2) {
            int i = 0;

            for(int length = this.classpaths.length; i < length; ++i) {
               NameEnvironmentAnswer answer = this.classpaths[i].findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly);
               if (answer != null) {
                  if (!answer.ignoreIfBetter()) {
                     if (answer.isBetter(suggestedAnswer)) {
                        return answer;
                     }
                  } else if (answer.isBetter(suggestedAnswer)) {
                     suggestedAnswer = answer;
                  }
               }
            }
         } else {
            String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
            int i = 0;

            for(int length = this.classpaths.length; i < length; ++i) {
               FileSystem.Classpath p = this.classpaths[i];
               NameEnvironmentAnswer answer = p instanceof ClasspathJar
                  ? p.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, asBinaryOnly)
                  : p.findClass(typeName, qp2, qb2, asBinaryOnly);
               if (answer != null) {
                  if (!answer.ignoreIfBetter()) {
                     if (answer.isBetter(suggestedAnswer)) {
                        return answer;
                     }
                  } else if (answer.isBetter(suggestedAnswer)) {
                     suggestedAnswer = answer;
                  }
               }
            }
         }

         return suggestedAnswer != null ? suggestedAnswer : null;
      }
   }

   @Override
   public NameEnvironmentAnswer findType(char[][] compoundName) {
      return compoundName != null
         ? this.findClass(new String(CharOperation.concatWith(compoundName, '/')), compoundName[compoundName.length - 1], false)
         : null;
   }

   public char[][][] findTypeNames(char[][] packageName) {
      char[][][] result = null;
      if (packageName != null) {
         String qualifiedPackageName = new String(CharOperation.concatWith(packageName, '/'));
         String qualifiedPackageName2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
         if (qualifiedPackageName == qualifiedPackageName2) {
            int i = 0;

            for(int length = this.classpaths.length; i < length; ++i) {
               char[][][] answers = this.classpaths[i].findTypeNames(qualifiedPackageName);
               if (answers != null) {
                  if (result == null) {
                     result = answers;
                  } else {
                     int resultLength = result.length;
                     int answersLength = answers.length;
                     System.arraycopy(result, 0, result = new char[answersLength + resultLength][][], 0, resultLength);
                     System.arraycopy(answers, 0, result, resultLength, answersLength);
                  }
               }
            }
         } else {
            int i = 0;

            for(int length = this.classpaths.length; i < length; ++i) {
               FileSystem.Classpath p = this.classpaths[i];
               char[][][] answers = p instanceof ClasspathJar ? p.findTypeNames(qualifiedPackageName) : p.findTypeNames(qualifiedPackageName2);
               if (answers != null) {
                  if (result == null) {
                     result = answers;
                  } else {
                     int resultLength = result.length;
                     int answersLength = answers.length;
                     System.arraycopy(result, 0, result = new char[answersLength + resultLength][][], 0, resultLength);
                     System.arraycopy(answers, 0, result, resultLength, answersLength);
                  }
               }
            }
         }
      }

      return result;
   }

   public NameEnvironmentAnswer findType(char[][] compoundName, boolean asBinaryOnly) {
      return compoundName != null
         ? this.findClass(new String(CharOperation.concatWith(compoundName, '/')), compoundName[compoundName.length - 1], asBinaryOnly)
         : null;
   }

   @Override
   public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
      return typeName != null ? this.findClass(new String(CharOperation.concatWith(packageName, typeName, '/')), typeName, false) : null;
   }

   @Override
   public boolean isPackage(char[][] compoundName, char[] packageName) {
      String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
      String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
      if (qualifiedPackageName == qp2) {
         int i = 0;

         for(int length = this.classpaths.length; i < length; ++i) {
            if (this.classpaths[i].isPackage(qualifiedPackageName)) {
               return true;
            }
         }
      } else {
         int i = 0;

         for(int length = this.classpaths.length; i < length; ++i) {
            FileSystem.Classpath p = this.classpaths[i];
            if (p instanceof ClasspathJar ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2)) {
               return true;
            }
         }
      }

      return false;
   }

   public interface Classpath {
      char[][][] findTypeNames(String var1);

      NameEnvironmentAnswer findClass(char[] var1, String var2, String var3);

      NameEnvironmentAnswer findClass(char[] var1, String var2, String var3, boolean var4);

      boolean isPackage(String var1);

      List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter var1);

      void reset();

      char[] normalizedPath();

      String getPath();

      void initialize() throws IOException;

      boolean hasAnnotationFileFor(String var1);
   }

   public static class ClasspathNormalizer {
      public static ArrayList normalize(ArrayList classpaths) {
         ArrayList normalizedClasspath = new ArrayList();
         HashSet cache = new HashSet();

         for(FileSystem.Classpath classpath : classpaths) {
            if (!cache.contains(classpath)) {
               normalizedClasspath.add(classpath);
               cache.add(classpath);
            }
         }

         return normalizedClasspath;
      }
   }

   public interface ClasspathSectionProblemReporter {
      void invalidClasspathSection(String var1);

      void multipleClasspathSections(String var1);
   }
}
