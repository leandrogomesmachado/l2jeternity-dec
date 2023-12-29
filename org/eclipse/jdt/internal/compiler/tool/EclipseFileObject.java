package org.eclipse.jdt.internal.compiler.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

public class EclipseFileObject extends SimpleJavaFileObject {
   private File f = new File(this.uri);
   private Charset charset;
   private boolean parentsExist;

   public EclipseFileObject(String className, URI uri, Kind kind, Charset charset) {
      super(uri, kind);
      this.charset = charset;
      this.parentsExist = false;
   }

   @Override
   public Modifier getAccessLevel() {
      if (this.getKind() != Kind.CLASS) {
         return null;
      } else {
         ClassFileReader reader = null;

         try {
            reader = ClassFileReader.read(this.f);
         } catch (ClassFormatException var3) {
         } catch (IOException var4) {
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
   public NestingKind getNestingKind() {
      switch(this.kind) {
         case SOURCE:
            return NestingKind.TOP_LEVEL;
         case CLASS:
            ClassFileReader reader = null;

            try {
               reader = ClassFileReader.read(this.f);
            } catch (ClassFormatException var2) {
            } catch (IOException var3) {
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
   public boolean delete() {
      return this.f.delete();
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof EclipseFileObject)) {
         return false;
      } else {
         EclipseFileObject eclipseFileObject = (EclipseFileObject)o;
         return eclipseFileObject.toUri().equals(this.uri);
      }
   }

   @Override
   public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      return Util.getCharContents(this, ignoreEncodingErrors, org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(this.f), this.charset.name());
   }

   @Override
   public long getLastModified() {
      return this.f.lastModified();
   }

   @Override
   public String getName() {
      return this.f.getPath();
   }

   @Override
   public int hashCode() {
      return this.f.hashCode();
   }

   @Override
   public InputStream openInputStream() throws IOException {
      return new BufferedInputStream(new FileInputStream(this.f));
   }

   @Override
   public OutputStream openOutputStream() throws IOException {
      this.ensureParentDirectoriesExist();
      return new BufferedOutputStream(new FileOutputStream(this.f));
   }

   @Override
   public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
      return new BufferedReader(new FileReader(this.f));
   }

   @Override
   public Writer openWriter() throws IOException {
      this.ensureParentDirectoriesExist();
      return new BufferedWriter(new FileWriter(this.f));
   }

   @Override
   public String toString() {
      return this.f.getAbsolutePath();
   }

   private void ensureParentDirectoriesExist() throws IOException {
      if (!this.parentsExist) {
         File parent = this.f.getParentFile();
         if (parent != null && !parent.exists() && !parent.mkdirs() && (!parent.exists() || !parent.isDirectory())) {
            throw new IOException("Unable to create parent directories for " + this.f);
         }

         this.parentsExist = true;
      }
   }
}
