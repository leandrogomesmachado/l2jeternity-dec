package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public class ClasspathJsr199 extends ClasspathLocation {
   private static final Set<Kind> fileTypes = new HashSet<>();
   private JavaFileManager fileManager;
   private Location location;

   static {
      fileTypes.add(Kind.CLASS);
   }

   public ClasspathJsr199(JavaFileManager file, Location location) {
      super(null, null);
      this.fileManager = file;
      this.location = location;
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
   public NameEnvironmentAnswer findClass(char[] param1, String param2, String param3, boolean param4) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: parsing failure!
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.parseGraph(DomHelper.java:215)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:141)
      //
      // Bytecode:
      // 00: getstatic java/io/File.separatorChar C
      // 03: bipush 47
      // 05: if_icmpne 0c
      // 08: aload 3
      // 09: goto 15
      // 0c: aload 3
      // 0d: getstatic java/io/File.separatorChar C
      // 10: bipush 47
      // 12: invokevirtual java/lang/String.replace (CC)Ljava/lang/String;
      // 15: astore 5
      // 17: aload 5
      // 19: bipush 46
      // 1b: invokevirtual java/lang/String.lastIndexOf (I)I
      // 1e: istore 6
      // 20: iload 6
      // 22: ifge 2a
      // 25: aload 5
      // 27: goto 32
      // 2a: aload 5
      // 2c: bipush 0
      // 2d: iload 6
      // 2f: invokevirtual java/lang/String.substring (II)Ljava/lang/String;
      // 32: astore 7
      // 34: aconst_null
      // 35: astore 8
      // 37: aload 0
      // 38: getfield org/eclipse/jdt/internal/compiler/batch/ClasspathJsr199.fileManager Ljavax/tools/JavaFileManager;
      // 3b: aload 0
      // 3c: getfield org/eclipse/jdt/internal/compiler/batch/ClasspathJsr199.location Ljavax/tools/JavaFileManager$Location;
      // 3f: aload 7
      // 41: getstatic javax/tools/JavaFileObject$Kind.CLASS Ljavax/tools/JavaFileObject$Kind;
      // 44: invokeinterface javax/tools/JavaFileManager.getJavaFileForInput (Ljavax/tools/JavaFileManager$Location;Ljava/lang/String;Ljavax/tools/JavaFileObject$Kind;)Ljavax/tools/JavaFileObject; 4
      // 49: astore 8
      // 4b: goto 4f
      // 4e: pop
      // 4f: aload 8
      // 51: ifnonnull 56
      // 54: aconst_null
      // 55: areturn
      // 56: aconst_null
      // 57: astore 9
      // 59: aconst_null
      // 5a: astore 10
      // 5c: aload 8
      // 5e: invokeinterface javax/tools/JavaFileObject.openInputStream ()Ljava/io/InputStream; 1
      // 63: astore 11
      // 65: aload 11
      // 67: aload 5
      // 69: invokestatic org/eclipse/jdt/internal/compiler/classfmt/ClassFileReader.read (Ljava/io/InputStream;Ljava/lang/String;)Lorg/eclipse/jdt/internal/compiler/classfmt/ClassFileReader;
      // 6c: astore 12
      // 6e: aload 12
      // 70: ifnull 8d
      // 73: new org/eclipse/jdt/internal/compiler/env/NameEnvironmentAnswer
      // 76: dup
      // 77: aload 12
      // 79: aload 0
      // 7a: aload 5
      // 7c: invokevirtual org/eclipse/jdt/internal/compiler/batch/ClasspathJsr199.fetchAccessRestriction (Ljava/lang/String;)Lorg/eclipse/jdt/internal/compiler/env/AccessRestriction;
      // 7f: invokespecial org/eclipse/jdt/internal/compiler/env/NameEnvironmentAnswer.<init> (Lorg/eclipse/jdt/internal/compiler/env/IBinaryType;Lorg/eclipse/jdt/internal/compiler/env/AccessRestriction;)V
      // 82: aload 11
      // 84: ifnull 8c
      // 87: aload 11
      // 89: invokevirtual java/io/InputStream.close ()V
      // 8c: areturn
      // 8d: aload 11
      // 8f: ifnull cd
      // 92: aload 11
      // 94: invokevirtual java/io/InputStream.close ()V
      // 97: goto cd
      // 9a: astore 9
      // 9c: aload 11
      // 9e: ifnull a6
      // a1: aload 11
      // a3: invokevirtual java/io/InputStream.close ()V
      // a6: aload 9
      // a8: athrow
      // a9: astore 10
      // ab: aload 9
      // ad: ifnonnull b7
      // b0: aload 10
      // b2: astore 9
      // b4: goto c5
      // b7: aload 9
      // b9: aload 10
      // bb: if_acmpeq c5
      // be: aload 9
      // c0: aload 10
      // c2: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // c5: aload 9
      // c7: athrow
      // c8: pop
      // c9: goto cd
      // cc: pop
      // cd: aconst_null
      // ce: areturn
   }

   @Override
   public char[][][] findTypeNames(String aQualifiedPackageName) {
      String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(File.separatorChar, '/');
      Iterable<JavaFileObject> files = null;

      try {
         files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, false);
      } catch (IOException var12) {
      }

      if (files == null) {
         return null;
      } else {
         ArrayList answers = new ArrayList();
         char[][] packageName = CharOperation.splitOn(File.separatorChar, qualifiedPackageName.toCharArray());

         for(JavaFileObject file : files) {
            String fileName = file.toUri().getPath();
            int last = fileName.lastIndexOf(47);
            if (last > 0) {
               int indexOfDot = fileName.lastIndexOf(46);
               if (indexOfDot != -1) {
                  String typeName = fileName.substring(last + 1, indexOfDot);
                  answers.add(CharOperation.arrayConcat(packageName, typeName.toCharArray()));
               }
            }
         }

         int size = answers.size();
         if (size != 0) {
            char[][][] result = new char[size][][];
            answers.toArray(result);
            return result;
         } else {
            return null;
         }
      }
   }

   @Override
   public void initialize() throws IOException {
   }

   @Override
   public boolean isPackage(String aQualifiedPackageName) {
      String qualifiedPackageName = File.separatorChar == '/' ? aQualifiedPackageName : aQualifiedPackageName.replace(File.separatorChar, '/');
      boolean result = false;

      try {
         Iterable<JavaFileObject> files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, false);
         Iterator f = files.iterator();
         if (f.hasNext()) {
            result = true;
         } else {
            files = this.fileManager.list(this.location, qualifiedPackageName, fileTypes, true);
            f = files.iterator();
            if (f.hasNext()) {
               result = true;
            }
         }
      } catch (IOException var6) {
      }

      return result;
   }

   @Override
   public void reset() {
      try {
         this.fileManager.flush();
      } catch (IOException var1) {
      }
   }

   @Override
   public String toString() {
      return "Classpath for Jsr 199 JavaFileManager: " + this.location;
   }

   @Override
   public char[] normalizedPath() {
      if (this.normalizedPath == null) {
         this.normalizedPath = this.path.toCharArray();
      }

      return this.normalizedPath;
   }

   @Override
   public String getPath() {
      if (this.path == null) {
         this.path = this.location.getName();
      }

      return this.path;
   }

   @Override
   public int getMode() {
      return 2;
   }

   @Override
   public boolean hasAnnotationFileFor(String qualifiedTypeName) {
      return false;
   }
}
