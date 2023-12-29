package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaFileObject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class HookedJavaFileObject extends ForwardingJavaFileObject<JavaFileObject> {
   protected final BatchFilerImpl _filer;
   protected final String _fileName;
   private boolean _closed = false;
   private String _typeName;

   public HookedJavaFileObject(JavaFileObject fileObject, String fileName, String typeName, BatchFilerImpl filer) {
      super(fileObject);
      this._filer = filer;
      this._fileName = fileName;
      this._typeName = typeName;
   }

   @Override
   public OutputStream openOutputStream() throws IOException {
      return new HookedJavaFileObject.ForwardingOutputStream(super.openOutputStream());
   }

   @Override
   public Writer openWriter() throws IOException {
      return new HookedJavaFileObject.ForwardingWriter(super.openWriter());
   }

   protected void closed() {
      if (!this._closed) {
         this._closed = true;
         switch(this.getKind()) {
            case SOURCE:
               CompilationUnit unit = new CompilationUnit(null, this._fileName, null);
               this._filer.addNewUnit(unit);
               break;
            case CLASS:
               IBinaryType binaryType = null;

               try {
                  binaryType = ClassFileReader.read(this._fileName);
               } catch (ClassFormatException var6) {
                  ReferenceBinding type = this._filer._env._compiler.lookupEnvironment.getType(CharOperation.splitOn('.', this._typeName.toCharArray()));
                  if (type != null) {
                     this._filer.addNewClassFile(type);
                  }
               } catch (IOException var7) {
               }

               if (binaryType != null) {
                  char[] name = binaryType.getName();
                  ReferenceBinding type = this._filer._env._compiler.lookupEnvironment.getType(CharOperation.splitOn('/', name));
                  if (type != null && type.isValidBinding()) {
                     if (type.isBinaryBinding()) {
                        this._filer.addNewClassFile(type);
                     } else {
                        BinaryTypeBinding binaryBinding = new BinaryTypeBinding(
                           type.getPackage(), binaryType, this._filer._env._compiler.lookupEnvironment, true
                        );
                        if (binaryBinding != null) {
                           this._filer.addNewClassFile(binaryBinding);
                        }
                     }
                  }
               }
            case HTML:
            case OTHER:
         }
      }
   }

   private class ForwardingOutputStream extends OutputStream {
      private final OutputStream _os;

      ForwardingOutputStream(OutputStream os) {
         this._os = os;
      }

      @Override
      public void close() throws IOException {
         this._os.close();
         HookedJavaFileObject.this.closed();
      }

      @Override
      public void flush() throws IOException {
         this._os.flush();
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
         this._os.write(b, off, len);
      }

      @Override
      public void write(byte[] b) throws IOException {
         this._os.write(b);
      }

      @Override
      public void write(int b) throws IOException {
         this._os.write(b);
      }

      @Override
      protected Object clone() throws CloneNotSupportedException {
         return HookedJavaFileObject.this.new ForwardingOutputStream(this._os);
      }

      @Override
      public int hashCode() {
         return this._os.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            HookedJavaFileObject.ForwardingOutputStream other = (HookedJavaFileObject.ForwardingOutputStream)obj;
            if (this._os == null) {
               if (other._os != null) {
                  return false;
               }
            } else if (!this._os.equals(other._os)) {
               return false;
            }

            return true;
         }
      }

      @Override
      public String toString() {
         return "ForwardingOutputStream wrapping " + this._os.toString();
      }
   }

   private class ForwardingWriter extends Writer {
      private final Writer _w;

      ForwardingWriter(Writer w) {
         this._w = w;
      }

      @Override
      public Writer append(char c) throws IOException {
         return this._w.append(c);
      }

      @Override
      public Writer append(CharSequence csq, int start, int end) throws IOException {
         return this._w.append(csq, start, end);
      }

      @Override
      public Writer append(CharSequence csq) throws IOException {
         return this._w.append(csq);
      }

      @Override
      public void close() throws IOException {
         this._w.close();
         HookedJavaFileObject.this.closed();
      }

      @Override
      public void flush() throws IOException {
         this._w.flush();
      }

      @Override
      public void write(char[] cbuf) throws IOException {
         this._w.write(cbuf);
      }

      @Override
      public void write(int c) throws IOException {
         this._w.write(c);
      }

      @Override
      public void write(String str, int off, int len) throws IOException {
         this._w.write(str, off, len);
      }

      @Override
      public void write(String str) throws IOException {
         this._w.write(str);
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
         this._w.write(cbuf, off, len);
      }

      @Override
      protected Object clone() throws CloneNotSupportedException {
         return HookedJavaFileObject.this.new ForwardingWriter(this._w);
      }

      @Override
      public int hashCode() {
         return this._w.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            HookedJavaFileObject.ForwardingWriter other = (HookedJavaFileObject.ForwardingWriter)obj;
            if (this._w == null) {
               if (other._w != null) {
                  return false;
               }
            } else if (!this._w.equals(other._w)) {
               return false;
            }

            return true;
         }
      }

      @Override
      public String toString() {
         return "ForwardingWriter wrapping " + this._w.toString();
      }
   }
}
