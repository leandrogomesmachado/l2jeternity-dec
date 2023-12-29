package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;

public class CompilationUnit implements ICompilationUnit {
   public char[] contents;
   public char[] fileName;
   public char[] mainTypeName;
   String encoding;
   public String destinationPath;
   private boolean ignoreOptionalProblems;

   public CompilationUnit(char[] contents, String fileName, String encoding) {
      this(contents, fileName, encoding, null);
   }

   public CompilationUnit(char[] contents, String fileName, String encoding, String destinationPath) {
      this(contents, fileName, encoding, destinationPath, false);
   }

   public CompilationUnit(char[] contents, String fileName, String encoding, String destinationPath, boolean ignoreOptionalProblems) {
      this.contents = contents;
      char[] fileNameCharArray = fileName.toCharArray();
      switch(File.separatorChar) {
         case '/':
            if (CharOperation.indexOf('\\', fileNameCharArray) != -1) {
               CharOperation.replace(fileNameCharArray, '\\', '/');
            }
            break;
         case '\\':
            if (CharOperation.indexOf('/', fileNameCharArray) != -1) {
               CharOperation.replace(fileNameCharArray, '/', '\\');
            }
      }

      this.fileName = fileNameCharArray;
      int start = CharOperation.lastIndexOf(File.separatorChar, fileNameCharArray) + 1;
      int end = CharOperation.lastIndexOf('.', fileNameCharArray);
      if (end == -1) {
         end = fileNameCharArray.length;
      }

      this.mainTypeName = CharOperation.subarray(fileNameCharArray, start, end);
      this.encoding = encoding;
      this.destinationPath = destinationPath;
      this.ignoreOptionalProblems = ignoreOptionalProblems;
   }

   @Override
   public char[] getContents() {
      if (this.contents != null) {
         return this.contents;
      } else {
         try {
            return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
         } catch (IOException var2) {
            this.contents = CharOperation.NO_CHAR;
            throw new AbortCompilationUnit(null, var2, this.encoding);
         }
      }
   }

   @Override
   public char[] getFileName() {
      return this.fileName;
   }

   @Override
   public char[] getMainTypeName() {
      return this.mainTypeName;
   }

   @Override
   public char[][] getPackageName() {
      return null;
   }

   @Override
   public boolean ignoreOptionalProblems() {
      return this.ignoreOptionalProblems;
   }

   @Override
   public String toString() {
      return "CompilationUnit[" + new String(this.fileName) + "]";
   }
}
