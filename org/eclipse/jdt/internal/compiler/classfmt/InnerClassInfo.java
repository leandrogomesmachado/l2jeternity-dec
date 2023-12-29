package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

public class InnerClassInfo extends ClassFileStruct implements IBinaryNestedType {
   int innerClassNameIndex = -1;
   int outerClassNameIndex = -1;
   int innerNameIndex = -1;
   private char[] innerClassName;
   private char[] outerClassName;
   private char[] innerName;
   private int accessFlags = -1;
   private boolean readInnerClassName = false;
   private boolean readOuterClassName = false;
   private boolean readInnerName = false;

   public InnerClassInfo(byte[] classFileBytes, int[] offsets, int offset) {
      super(classFileBytes, offsets, offset);
      this.innerClassNameIndex = this.u2At(0);
      this.outerClassNameIndex = this.u2At(2);
      this.innerNameIndex = this.u2At(4);
   }

   @Override
   public char[] getEnclosingTypeName() {
      if (!this.readOuterClassName) {
         this.readOuterClassName = true;
         if (this.outerClassNameIndex != 0) {
            int utf8Offset = this.constantPoolOffsets[this.u2At(this.constantPoolOffsets[this.outerClassNameIndex] - this.structOffset + 1)]
               - this.structOffset;
            this.outerClassName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         }
      }

      return this.outerClassName;
   }

   @Override
   public int getModifiers() {
      if (this.accessFlags == -1) {
         this.accessFlags = this.u2At(6);
      }

      return this.accessFlags;
   }

   @Override
   public char[] getName() {
      if (!this.readInnerClassName) {
         this.readInnerClassName = true;
         if (this.innerClassNameIndex != 0) {
            int classOffset = this.constantPoolOffsets[this.innerClassNameIndex] - this.structOffset;
            int utf8Offset = this.constantPoolOffsets[this.u2At(classOffset + 1)] - this.structOffset;
            this.innerClassName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         }
      }

      return this.innerClassName;
   }

   public char[] getSourceName() {
      if (!this.readInnerName) {
         this.readInnerName = true;
         if (this.innerNameIndex != 0) {
            int utf8Offset = this.constantPoolOffsets[this.innerNameIndex] - this.structOffset;
            this.innerName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         }
      }

      return this.innerName;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      if (this.getName() != null) {
         buffer.append(this.getName());
      }

      buffer.append("\n");
      if (this.getEnclosingTypeName() != null) {
         buffer.append(this.getEnclosingTypeName());
      }

      buffer.append("\n");
      if (this.getSourceName() != null) {
         buffer.append(this.getSourceName());
      }

      return buffer.toString();
   }

   void initialize() {
      this.getModifiers();
      this.getName();
      this.getSourceName();
      this.getEnclosingTypeName();
      this.reset();
   }
}
