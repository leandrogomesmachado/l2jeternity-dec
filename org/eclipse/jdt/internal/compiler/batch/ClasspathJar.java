package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJar extends ClasspathLocation {
   protected File file;
   protected ZipFile zipFile;
   protected ZipFile annotationZipFile;
   protected boolean closeZipFileAtEnd;
   protected Hashtable packageCache;
   protected List<String> annotationPaths;

   public ClasspathJar(File file, boolean closeZipFileAtEnd, AccessRuleSet accessRuleSet, String destinationPath) {
      super(accessRuleSet, destinationPath);
      this.file = file;
      this.closeZipFileAtEnd = closeZipFileAtEnd;
   }

   @Override
   public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
      InputStream inputStream = null;

      try {
         this.initialize();
         ArrayList result = new ArrayList();
         ZipEntry manifest = this.zipFile.getEntry("META-INF/MANIFEST.MF");
         if (manifest != null) {
            inputStream = this.zipFile.getInputStream(manifest);
            ManifestAnalyzer analyzer = new ManifestAnalyzer();
            boolean success = analyzer.analyzeManifestContents(inputStream);
            List calledFileNames = analyzer.getCalledFileNames();
            if (problemReporter != null) {
               if (success && (analyzer.getClasspathSectionsCount() != 1 || calledFileNames != null)) {
                  if (analyzer.getClasspathSectionsCount() > 1) {
                     problemReporter.multipleClasspathSections(this.getPath());
                  }
               } else {
                  problemReporter.invalidClasspathSection(this.getPath());
               }
            }

            if (calledFileNames != null) {
               Iterator calledFilesIterator = calledFileNames.iterator();
               String directoryPath = this.getPath();
               int lastSeparator = directoryPath.lastIndexOf(File.separatorChar);
               directoryPath = directoryPath.substring(0, lastSeparator + 1);

               while(calledFilesIterator.hasNext()) {
                  result.add(
                     new ClasspathJar(
                        new File(directoryPath + (String)calledFilesIterator.next()), this.closeZipFileAtEnd, this.accessRuleSet, this.destinationPath
                     )
                  );
               }
            }
         }

         return result;
      } catch (IOException var19) {
      } finally {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (IOException var18) {
            }
         }
      }

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
         try {
            ClassFileReader reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
            if (reader != null) {
               if (this.annotationPaths != null) {
                  String qualifiedClassName = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - "CLASS".length() - 1);

                  for(String annotationPath : this.annotationPaths) {
                     try {
                        this.annotationZipFile = reader.setExternalAnnotationProvider(annotationPath, qualifiedClassName, this.annotationZipFile, null);
                        if (reader.hasAnnotationProvider()) {
                           break;
                        }
                     } catch (IOException var9) {
                     }
                  }
               }

               return new NameEnvironmentAnswer(reader, this.fetchAccessRestriction(qualifiedBinaryFileName));
            }
         } catch (ClassFormatException var10) {
         } catch (IOException var11) {
         }

         return null;
      }
   }

   @Override
   public boolean hasAnnotationFileFor(String qualifiedTypeName) {
      return this.zipFile.getEntry(qualifiedTypeName + ".eea") != null;
   }

   @Override
   public char[][][] findTypeNames(String qualifiedPackageName) {
      if (!this.isPackage(qualifiedPackageName)) {
         return null;
      } else {
         ArrayList answers = new ArrayList();
         Enumeration e = this.zipFile.entries();

         while(e.hasMoreElements()) {
            String fileName = ((ZipEntry)e.nextElement()).getName();
            int last = fileName.lastIndexOf(47);

            while(last > 0) {
               String packageName = fileName.substring(0, last);
               if (!qualifiedPackageName.equals(packageName)) {
                  break;
               }

               int indexOfDot = fileName.lastIndexOf(46);
               if (indexOfDot != -1) {
                  String typeName = fileName.substring(last + 1, indexOfDot);
                  char[] packageArray = packageName.toCharArray();
                  answers.add(CharOperation.arrayConcat(CharOperation.splitOn('/', packageArray), typeName.toCharArray()));
               }
            }
         }

         int size = answers.size();
         if (size != 0) {
            char[][][] result = new char[size][][];
            answers.toArray(result);
            return null;
         } else {
            return null;
         }
      }
   }

   @Override
   public void initialize() throws IOException {
      if (this.zipFile == null) {
         this.zipFile = new ZipFile(this.file);
      }
   }

   @Override
   public boolean isPackage(String qualifiedPackageName) {
      if (this.packageCache != null) {
         return this.packageCache.containsKey(qualifiedPackageName);
      } else {
         this.packageCache = new Hashtable(41);
         this.packageCache.put(Util.EMPTY_STRING, Util.EMPTY_STRING);
         Enumeration e = this.zipFile.entries();

         while(e.hasMoreElements()) {
            String fileName = ((ZipEntry)e.nextElement()).getName();

            String packageName;
            for(int last = fileName.lastIndexOf(47); last > 0; last = packageName.lastIndexOf(47)) {
               packageName = fileName.substring(0, last);
               if (this.packageCache.containsKey(packageName)) {
                  break;
               }

               this.packageCache.put(packageName, packageName);
            }
         }

         return this.packageCache.containsKey(qualifiedPackageName);
      }
   }

   @Override
   public void reset() {
      if (this.closeZipFileAtEnd) {
         if (this.zipFile != null) {
            try {
               this.zipFile.close();
            } catch (IOException var2) {
            }

            this.zipFile = null;
         }

         if (this.annotationZipFile != null) {
            try {
               this.annotationZipFile.close();
            } catch (IOException var1) {
            }

            this.annotationZipFile = null;
         }
      }

      this.packageCache = null;
   }

   @Override
   public String toString() {
      return "Classpath for jar file " + this.file.getPath();
   }

   @Override
   public char[] normalizedPath() {
      if (this.normalizedPath == null) {
         String path2 = this.getPath();
         char[] rawName = path2.toCharArray();
         if (File.separatorChar == '\\') {
            CharOperation.replace(rawName, '\\', '/');
         }

         this.normalizedPath = CharOperation.subarray(rawName, 0, CharOperation.lastIndexOf('.', rawName));
      }

      return this.normalizedPath;
   }

   @Override
   public String getPath() {
      if (this.path == null) {
         try {
            this.path = this.file.getCanonicalPath();
         } catch (IOException var1) {
            this.path = this.file.getAbsolutePath();
         }
      }

      return this.path;
   }

   @Override
   public int getMode() {
      return 2;
   }
}
