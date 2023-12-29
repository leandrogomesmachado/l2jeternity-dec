package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathDirectory extends ClasspathLocation {
   private Hashtable directoryCache;
   private String[] missingPackageHolder = new String[1];
   private int mode;
   private String encoding;
   private Hashtable<String, Hashtable<String, String>> packageSecondaryTypes = null;
   Map options;

   ClasspathDirectory(File directory, String encoding, int mode, AccessRuleSet accessRuleSet, String destinationPath, Map options) {
      super(accessRuleSet, destinationPath);
      this.mode = mode;
      this.options = options;

      try {
         this.path = directory.getCanonicalPath();
      } catch (IOException var7) {
         this.path = directory.getAbsolutePath();
      }

      if (!this.path.endsWith(File.separator)) {
         this.path = this.path + File.separator;
      }

      this.directoryCache = new Hashtable(11);
      this.encoding = encoding;
   }

   String[] directoryList(String qualifiedPackageName) {
      String[] dirList = (String[])this.directoryCache.get(qualifiedPackageName);
      if (dirList == this.missingPackageHolder) {
         return null;
      } else if (dirList != null) {
         return dirList;
      } else {
         File dir = new File(this.path + qualifiedPackageName);
         label32:
         if (dir.isDirectory()) {
            int index = qualifiedPackageName.length();
            int last = qualifiedPackageName.lastIndexOf(File.separatorChar);

            do {
               --index;
            } while(index > last && !ScannerHelper.isUpperCase(qualifiedPackageName.charAt(index)));

            if (index > last) {
               if (last == -1) {
                  if (!this.doesFileExist(qualifiedPackageName, Util.EMPTY_STRING)) {
                     break label32;
                  }
               } else {
                  String packageName = qualifiedPackageName.substring(last + 1);
                  String parentPackage = qualifiedPackageName.substring(0, last);
                  if (!this.doesFileExist(packageName, parentPackage)) {
                     break label32;
                  }
               }
            }

            if ((dirList = dir.list()) == null) {
               dirList = CharOperation.NO_STRINGS;
            }

            this.directoryCache.put(qualifiedPackageName, dirList);
            return dirList;
         }

         this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
         return null;
      }
   }

   boolean doesFileExist(String fileName, String qualifiedPackageName) {
      String[] dirList = this.directoryList(qualifiedPackageName);
      if (dirList == null) {
         return false;
      } else {
         int i = dirList.length;

         while(--i >= 0) {
            if (fileName.equals(dirList[i])) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
      return null;
   }

   @Override
   public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
      return this.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
   }

   @Override
   public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
      if (!this.isPackage(qualifiedPackageName)) {
         return null;
      } else {
         String fileName = new String(typeName);
         boolean binaryExists = (this.mode & 2) != 0 && this.doesFileExist(fileName + ".class", qualifiedPackageName);
         boolean sourceExists = (this.mode & 1) != 0 && this.doesFileExist(fileName + ".java", qualifiedPackageName);
         if (sourceExists && !asBinaryOnly) {
            String fullSourcePath = this.path + qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6) + ".java";
            if (!binaryExists) {
               return new NameEnvironmentAnswer(
                  new CompilationUnit(null, fullSourcePath, this.encoding, this.destinationPath), this.fetchAccessRestriction(qualifiedBinaryFileName)
               );
            }

            String fullBinaryPath = this.path + qualifiedBinaryFileName;
            long binaryModified = new File(fullBinaryPath).lastModified();
            long sourceModified = new File(fullSourcePath).lastModified();
            if (sourceModified > binaryModified) {
               return new NameEnvironmentAnswer(
                  new CompilationUnit(null, fullSourcePath, this.encoding, this.destinationPath), this.fetchAccessRestriction(qualifiedBinaryFileName)
               );
            }
         }

         if (binaryExists) {
            try {
               ClassFileReader reader = ClassFileReader.read(this.path + qualifiedBinaryFileName);
               String typeSearched = qualifiedPackageName.length() > 0 ? qualifiedPackageName.replace(File.separatorChar, '/') + "/" + fileName : fileName;
               if (!CharOperation.equals(reader.getName(), typeSearched.toCharArray())) {
                  reader = null;
               }

               if (reader != null) {
                  return new NameEnvironmentAnswer(reader, this.fetchAccessRestriction(qualifiedBinaryFileName));
               }
            } catch (IOException var14) {
            } catch (ClassFormatException var15) {
            }
         }

         return null;
      }
   }

   public NameEnvironmentAnswer findSecondaryInClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
      if (TypeConstants.PACKAGE_INFO_NAME.equals(typeName)) {
         return null;
      } else {
         String typeNameString = new String(typeName);
         boolean prereqs = this.options != null
            && this.isPackage(qualifiedPackageName)
            && (this.mode & 1) != 0
            && this.doesFileExist(typeNameString + ".java", qualifiedPackageName);
         return prereqs ? null : this.findSourceSecondaryType(typeNameString, qualifiedPackageName, qualifiedBinaryFileName);
      }
   }

   @Override
   public boolean hasAnnotationFileFor(String qualifiedTypeName) {
      int pos = qualifiedTypeName.lastIndexOf(47);
      if (pos != -1 && pos + 1 < qualifiedTypeName.length()) {
         String fileName = qualifiedTypeName.substring(pos + 1) + ".eea";
         return this.doesFileExist(fileName, qualifiedTypeName.substring(0, pos));
      } else {
         return false;
      }
   }

   private Hashtable<String, String> getPackageTypes(String qualifiedPackageName) {
      Hashtable<String, String> packageEntry = new Hashtable<>();
      String[] dirList = (String[])this.directoryCache.get(qualifiedPackageName);
      if (dirList != this.missingPackageHolder && dirList != null) {
         File dir = new File(this.path + qualifiedPackageName);
         File[] listFiles = dir.isDirectory() ? dir.listFiles() : null;
         if (listFiles == null) {
            return packageEntry;
         } else {
            int i = 0;

            for(int l = listFiles.length; i < l; ++i) {
               File f = listFiles[i];
               if (!f.isDirectory()) {
                  String s = f.getAbsolutePath();
                  if (s != null && (s.endsWith(".java") || s.endsWith(".JAVA"))) {
                     CompilationUnit cu = new CompilationUnit(null, s, this.encoding, this.destinationPath);
                     CompilationResult compilationResult = new CompilationResult(cu.getContents(), 1, 1, 10);
                     ProblemReporter problemReporter = new ProblemReporter(
                        DefaultErrorHandlingPolicies.proceedWithAllProblems(), new CompilerOptions(this.options), new DefaultProblemFactory()
                     );
                     Parser parser = new Parser(problemReporter, false);
                     parser.reportSyntaxErrorIsRequired = false;
                     CompilationUnitDeclaration unit = parser.parse(cu, compilationResult);
                     TypeDeclaration[] types = unit != null ? unit.types : null;
                     if (types != null) {
                        int j = 0;

                        for(int k = types.length; j < k; ++j) {
                           TypeDeclaration type = types[j];
                           char[] name = type.isSecondary() ? type.name : null;
                           if (name != null) {
                              packageEntry.put(new String(name), s);
                           }
                        }
                     }
                  }
               }
            }

            return packageEntry;
         }
      } else {
         return packageEntry;
      }
   }

   private NameEnvironmentAnswer findSourceSecondaryType(String typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
      if (this.packageSecondaryTypes == null) {
         this.packageSecondaryTypes = new Hashtable<>();
      }

      Hashtable<String, String> packageEntry = this.packageSecondaryTypes.get(qualifiedPackageName);
      if (packageEntry == null) {
         packageEntry = this.getPackageTypes(qualifiedPackageName);
         this.packageSecondaryTypes.put(qualifiedPackageName, packageEntry);
      }

      String fileName = packageEntry.get(typeName);
      return fileName != null
         ? new NameEnvironmentAnswer(
            new CompilationUnit(null, fileName, this.encoding, this.destinationPath), this.fetchAccessRestriction(qualifiedBinaryFileName)
         )
         : null;
   }

   @Override
   public char[][][] findTypeNames(String qualifiedPackageName) {
      if (!this.isPackage(qualifiedPackageName)) {
         return null;
      } else {
         File dir = new File(this.path + qualifiedPackageName);
         if (dir.exists() && dir.isDirectory()) {
            String[] listFiles = dir.list(new FilenameFilter() {
               @Override
               public boolean accept(File directory1, String name) {
                  String fileName = name.toLowerCase();
                  return fileName.endsWith(".class") || fileName.endsWith(".java");
               }
            });
            int length;
            if (listFiles != null && (length = listFiles.length) != 0) {
               char[][][] result = new char[length][][];
               char[][] packageName = CharOperation.splitOn(File.separatorChar, qualifiedPackageName.toCharArray());

               for(int i = 0; i < length; ++i) {
                  String fileName = listFiles[i];
                  int indexOfLastDot = fileName.indexOf(46);
                  result[i] = CharOperation.arrayConcat(packageName, fileName.substring(0, indexOfLastDot).toCharArray());
               }

               return result;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   @Override
   public void initialize() throws IOException {
   }

   @Override
   public boolean isPackage(String qualifiedPackageName) {
      return this.directoryList(qualifiedPackageName) != null;
   }

   @Override
   public void reset() {
      this.directoryCache = new Hashtable(11);
   }

   @Override
   public String toString() {
      return "ClasspathDirectory " + this.path;
   }

   @Override
   public char[] normalizedPath() {
      if (this.normalizedPath == null) {
         this.normalizedPath = this.path.toCharArray();
         if (File.separatorChar == '\\') {
            CharOperation.replace(this.normalizedPath, '\\', '/');
         }
      }

      return this.normalizedPath;
   }

   @Override
   public String getPath() {
      return this.path;
   }

   @Override
   public int getMode() {
      return this.mode;
   }
}
