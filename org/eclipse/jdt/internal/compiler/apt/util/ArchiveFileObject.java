package org.eclipse.jdt.internal.compiler.apt.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

public class ArchiveFileObject implements JavaFileObject {
   private String entryName;
   private File file;
   private ZipFile zipFile;
   private Charset charset;

   public ArchiveFileObject(File file, String entryName, Charset charset) {
      this.entryName = entryName;
      this.file = file;
      this.charset = charset;
   }

   @Override
   protected void finalize() throws Throwable {
      if (this.zipFile != null) {
         try {
            this.zipFile.close();
         } catch (IOException var1) {
         }
      }

      super.finalize();
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public Modifier getAccessLevel() {
      if (this.getKind() != Kind.CLASS) {
         return null;
      } else {
         ClassFileReader reader = null;

         try {
            Throwable accessFlags = null;
            Object var3 = null;

            try {
               ZipFile zip = new ZipFile(this.file);

               try {
                  reader = ClassFileReader.read(zip, this.entryName);
               } finally {
                  if (zip != null) {
                     zip.close();
                  }
               }
            } catch (Throwable var14) {
               if (accessFlags == null) {
                  accessFlags = var14;
               } else if (accessFlags != var14) {
                  accessFlags.addSuppressed(var14);
               }

               throw accessFlags;
            }
         } catch (ClassFormatException var15) {
         } catch (IOException var16) {
         }

         if (reader == null) {
            return null;
         } else {
            int accessFlags = reader.accessFlags();
            if ((accessFlags & 1) != 0) {
               return Modifier.PUBLIC;
            } else if ((accessFlags & 1024) != 0) {
               return Modifier.ABSTRACT;
            } else {
               return (accessFlags & 16) != 0 ? Modifier.FINAL : null;
            }
         }
      }
   }

   @Override
   public Kind getKind() {
      String name = this.entryName.toLowerCase();
      if (name.endsWith(Kind.CLASS.extension)) {
         return Kind.CLASS;
      } else if (name.endsWith(Kind.SOURCE.extension)) {
         return Kind.SOURCE;
      } else {
         return name.endsWith(Kind.HTML.extension) ? Kind.HTML : Kind.OTHER;
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public NestingKind getNestingKind() {
      switch(this.getKind()) {
         case SOURCE:
            return NestingKind.TOP_LEVEL;
         case CLASS:
            ClassFileReader reader = null;

            try {
               Throwable var2 = null;
               Object var3 = null;

               try {
                  ZipFile zip = new ZipFile(this.file);

                  try {
                     reader = ClassFileReader.read(zip, this.entryName);
                  } finally {
                     if (zip != null) {
                        zip.close();
                     }
                  }
               } catch (Throwable var14) {
                  if (var2 == null) {
                     var2 = var14;
                  } else if (var2 != var14) {
                     var2.addSuppressed(var14);
                  }

                  throw var2;
               }
            } catch (ClassFormatException var15) {
            } catch (IOException var16) {
            }

            if (reader == null) {
               return null;
            } else if (reader.isAnonymous()) {
               return NestingKind.ANONYMOUS;
            } else if (reader.isLocal()) {
               return NestingKind.LOCAL;
            } else {
               if (reader.isMember()) {
                  return NestingKind.MEMBER;
               }

               return NestingKind.TOP_LEVEL;
            }
         default:
            return null;
      }
   }

   @Override
   public boolean isNameCompatible(String simpleName, Kind kind) {
      return this.entryName.endsWith(simpleName + kind.extension);
   }

   @Override
   public boolean delete() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ArchiveFileObject)) {
         return false;
      } else {
         ArchiveFileObject archiveFileObject = (ArchiveFileObject)o;
         return archiveFileObject.toUri().equals(this.toUri());
      }
   }

   @Override
   public int hashCode() {
      return this.toUri().hashCode();
   }

   // $VF: Inserted dummy exception handlers to handle obfuscated exceptions
   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Override
   public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      if (this.getKind() == Kind.SOURCE) {
         Throwable var2 = null;
         Object var3 = null;

         try {
            ZipFile zipFile2 = new ZipFile(this.file);

            Throwable var10000;
            label135: {
               try {
                  ZipEntry zipEntry = zipFile2.getEntry(this.entryName);
                  var18 = Util.getCharContents(
                     this, ignoreEncodingErrors, org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(zipEntry, zipFile2), this.charset.name()
                  );
               } catch (Throwable var16) {
                  var10000 = var16;
                  boolean var10001 = false;
                  break label135;
               }

               if (zipFile2 != null) {
                  zipFile2.close();
               }

               label122:
               try {
                  return var18;
               } catch (Throwable var15) {
                  var10000 = var15;
                  boolean var19 = false;
                  break label122;
               }
            }

            var2 = var10000;
            if (zipFile2 != null) {
               zipFile2.close();
            }

            throw var2;
         } catch (Throwable var17) {
            if (var2 == null) {
               var2 = var17;
            } else if (var2 != var17) {
               var2.addSuppressed(var17);
            }

            throw var2;
         }
      } else {
         return null;
      }
   }

   @Override
   public long getLastModified() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: parsing failure!
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.parseGraph(DomHelper.java:215)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:141)
      //
      // Bytecode:
      // 00: aconst_null
      // 01: astore 1
      // 02: aconst_null
      // 03: astore 2
      // 04: new java/util/zip/ZipFile
      // 07: dup
      // 08: aload 0
      // 09: getfield org/eclipse/jdt/internal/compiler/apt/util/ArchiveFileObject.file Ljava/io/File;
      // 0c: invokespecial java/util/zip/ZipFile.<init> (Ljava/io/File;)V
      // 0f: astore 3
      // 10: aload 3
      // 11: aload 0
      // 12: getfield org/eclipse/jdt/internal/compiler/apt/util/ArchiveFileObject.entryName Ljava/lang/String;
      // 15: invokevirtual java/util/zip/ZipFile.getEntry (Ljava/lang/String;)Ljava/util/zip/ZipEntry;
      // 18: astore 4
      // 1a: aload 4
      // 1c: invokevirtual java/util/zip/ZipEntry.getTime ()J
      // 1f: aload 3
      // 20: ifnull 27
      // 23: aload 3
      // 24: invokevirtual java/util/zip/ZipFile.close ()V
      // 27: lreturn
      // 28: astore 1
      // 29: aload 3
      // 2a: ifnull 31
      // 2d: aload 3
      // 2e: invokevirtual java/util/zip/ZipFile.close ()V
      // 31: aload 1
      // 32: athrow
      // 33: astore 2
      // 34: aload 1
      // 35: ifnonnull 3d
      // 38: aload 2
      // 39: astore 1
      // 3a: goto 47
      // 3d: aload 1
      // 3e: aload 2
      // 3f: if_acmpeq 47
      // 42: aload 1
      // 43: aload 2
      // 44: invokevirtual java/lang/Throwable.addSuppressed (Ljava/lang/Throwable;)V
      // 47: aload 1
      // 48: athrow
      // 49: pop
      // 4a: lconst_0
      // 4b: lreturn
   }

   @Override
   public String getName() {
      return this.entryName;
   }

   @Override
   public InputStream openInputStream() throws IOException {
      if (this.zipFile == null) {
         this.zipFile = new ZipFile(this.file);
      }

      ZipEntry zipEntry = this.zipFile.getEntry(this.entryName);
      return this.zipFile.getInputStream(zipEntry);
   }

   @Override
   public OutputStream openOutputStream() throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public Writer openWriter() throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public URI toUri() {
      try {
         return new URI("jar:" + this.file.toURI().getPath() + "!" + this.entryName);
      } catch (URISyntaxException var1) {
         return null;
      }
   }

   @Override
   public String toString() {
      return this.file.getAbsolutePath() + "[" + this.entryName + "]";
   }
}
