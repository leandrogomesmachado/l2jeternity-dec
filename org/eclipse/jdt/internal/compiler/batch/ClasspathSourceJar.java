package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathSourceJar extends ClasspathJar {
   private String encoding;

   public ClasspathSourceJar(File file, boolean closeZipFileAtEnd, AccessRuleSet accessRuleSet, String encoding, String destinationPath) {
      super(file, closeZipFileAtEnd, accessRuleSet, destinationPath);
      this.encoding = encoding;
   }

   @Override
   public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
      if (!this.isPackage(qualifiedPackageName)) {
         return null;
      } else {
         ZipEntry sourceEntry = this.zipFile.getEntry(qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6) + ".java");
         if (sourceEntry != null) {
            try {
               InputStream stream = null;
               char[] contents = null;

               try {
                  stream = this.zipFile.getInputStream(sourceEntry);
                  contents = Util.getInputStreamAsCharArray(stream, -1, this.encoding);
               } finally {
                  if (stream != null) {
                     stream.close();
                  }
               }

               return new NameEnvironmentAnswer(
                  new CompilationUnit(
                     contents, qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6) + ".java", this.encoding, this.destinationPath
                  ),
                  this.fetchAccessRestriction(qualifiedBinaryFileName)
               );
            } catch (IOException var12) {
            }
         }

         return null;
      }
   }

   @Override
   public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
      return this.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
   }

   @Override
   public int getMode() {
      return 1;
   }
}
