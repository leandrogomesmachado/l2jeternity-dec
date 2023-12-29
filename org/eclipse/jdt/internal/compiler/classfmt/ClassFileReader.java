package org.eclipse.jdt.internal.compiler.classfmt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClassFileReader extends ClassFileStruct implements IBinaryType {
   private int accessFlags;
   private char[] classFileName;
   private char[] className;
   private int classNameIndex;
   private int constantPoolCount;
   private AnnotationInfo[] annotations;
   private TypeAnnotationInfo[] typeAnnotations;
   private FieldInfo[] fields;
   private int fieldsCount;
   private InnerClassInfo innerInfo;
   private int innerInfoIndex;
   private InnerClassInfo[] innerInfos;
   private char[][] interfaceNames;
   private int interfacesCount;
   private MethodInfo[] methods;
   private int methodsCount;
   private char[] signature;
   private char[] sourceName;
   private char[] sourceFileName;
   private char[] superclassName;
   private long tagBits;
   private long version;
   private char[] enclosingTypeName;
   private char[][][] missingTypeNames;
   private int enclosingNameAndTypeIndex;
   private char[] enclosingMethod;
   private ExternalAnnotationProvider annotationProvider;
   private BinaryTypeBinding.ExternalAnnotationStatus externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.NOT_EEA_CONFIGURED;

   private static String printTypeModifiers(int modifiers) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter print = new PrintWriter(out);
      if ((modifiers & 1) != 0) {
         print.print("public ");
      }

      if ((modifiers & 2) != 0) {
         print.print("private ");
      }

      if ((modifiers & 16) != 0) {
         print.print("final ");
      }

      if ((modifiers & 32) != 0) {
         print.print("super ");
      }

      if ((modifiers & 512) != 0) {
         print.print("interface ");
      }

      if ((modifiers & 1024) != 0) {
         print.print("abstract ");
      }

      print.flush();
      return out.toString();
   }

   public static ClassFileReader read(File file) throws ClassFormatException, IOException {
      return read(file, false);
   }

   public static ClassFileReader read(File file, boolean fullyInitialize) throws ClassFormatException, IOException {
      byte[] classFileBytes = Util.getFileByteContent(file);
      ClassFileReader classFileReader = new ClassFileReader(classFileBytes, file.getAbsolutePath().toCharArray());
      if (fullyInitialize) {
         classFileReader.initialize();
      }

      return classFileReader;
   }

   public static ClassFileReader read(InputStream stream, String fileName) throws ClassFormatException, IOException {
      return read(stream, fileName, false);
   }

   public static ClassFileReader read(InputStream stream, String fileName, boolean fullyInitialize) throws ClassFormatException, IOException {
      byte[] classFileBytes = Util.getInputStreamAsByteArray(stream, -1);
      ClassFileReader classFileReader = new ClassFileReader(classFileBytes, fileName.toCharArray());
      if (fullyInitialize) {
         classFileReader.initialize();
      }

      return classFileReader;
   }

   public static ClassFileReader read(ZipFile zip, String filename) throws ClassFormatException, IOException {
      return read(zip, filename, false);
   }

   public static ClassFileReader read(ZipFile zip, String filename, boolean fullyInitialize) throws ClassFormatException, IOException {
      ZipEntry ze = zip.getEntry(filename);
      if (ze == null) {
         return null;
      } else {
         byte[] classFileBytes = Util.getZipEntryByteContent(ze, zip);
         ClassFileReader classFileReader = new ClassFileReader(classFileBytes, filename.toCharArray());
         if (fullyInitialize) {
            classFileReader.initialize();
         }

         return classFileReader;
      }
   }

   public static ClassFileReader read(String fileName) throws ClassFormatException, IOException {
      return read(fileName, false);
   }

   public static ClassFileReader read(String fileName, boolean fullyInitialize) throws ClassFormatException, IOException {
      return read(new File(fileName), fullyInitialize);
   }

   public ClassFileReader(byte[] classFileBytes, char[] fileName) throws ClassFormatException {
      this(classFileBytes, fileName, false);
   }

   public ClassFileReader(byte[] classFileBytes, char[] fileName, boolean fullyInitialize) throws ClassFormatException {
      super(classFileBytes, null, 0);
      this.classFileName = fileName;
      int readOffset = 10;

      try {
         this.version = ((long)this.u2At(6) << 16) + (long)this.u2At(4);
         this.constantPoolCount = this.u2At(8);
         this.constantPoolOffsets = new int[this.constantPoolCount];

         for(int i = 1; i < this.constantPoolCount; ++i) {
            int tag = this.u1At(readOffset);
            switch(tag) {
               case 1:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += this.u2At(readOffset + 1);
                  readOffset += 3;
               case 2:
               case 13:
               case 14:
               case 17:
               default:
                  break;
               case 3:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 4:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 5:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 9;
                  ++i;
                  break;
               case 6:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 9;
                  ++i;
                  break;
               case 7:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 3;
                  break;
               case 8:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 3;
                  break;
               case 9:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 10:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 11:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 12:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
                  break;
               case 15:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 4;
                  break;
               case 16:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 3;
                  break;
               case 18:
                  this.constantPoolOffsets[i] = readOffset;
                  readOffset += 5;
            }
         }

         this.accessFlags = this.u2At(readOffset);
         readOffset += 2;
         this.classNameIndex = this.u2At(readOffset);
         this.className = this.getConstantClassNameAt(this.classNameIndex);
         readOffset += 2;
         int superclassNameIndex = this.u2At(readOffset);
         readOffset += 2;
         if (superclassNameIndex != 0) {
            this.superclassName = this.getConstantClassNameAt(superclassNameIndex);
         }

         this.interfacesCount = this.u2At(readOffset);
         readOffset += 2;
         if (this.interfacesCount != 0) {
            this.interfaceNames = new char[this.interfacesCount][];

            for(int i = 0; i < this.interfacesCount; ++i) {
               this.interfaceNames[i] = this.getConstantClassNameAt(this.u2At(readOffset));
               readOffset += 2;
            }
         }

         this.fieldsCount = this.u2At(readOffset);
         readOffset += 2;
         if (this.fieldsCount != 0) {
            this.fields = new FieldInfo[this.fieldsCount];

            for(int i = 0; i < this.fieldsCount; ++i) {
               FieldInfo field = FieldInfo.createField(this.reference, this.constantPoolOffsets, readOffset);
               this.fields[i] = field;
               readOffset += field.sizeInBytes();
            }
         }

         this.methodsCount = this.u2At(readOffset);
         readOffset += 2;
         if (this.methodsCount != 0) {
            this.methods = new MethodInfo[this.methodsCount];
            boolean isAnnotationType = (this.accessFlags & 8192) != 0;

            for(int i = 0; i < this.methodsCount; ++i) {
               this.methods[i] = isAnnotationType
                  ? AnnotationMethodInfo.createAnnotationMethod(this.reference, this.constantPoolOffsets, readOffset)
                  : MethodInfo.createMethod(this.reference, this.constantPoolOffsets, readOffset);
               readOffset += this.methods[i].sizeInBytes();
            }
         }

         int attributesCount = this.u2At(readOffset);
         readOffset += 2;

         for(int i = 0; i < attributesCount; ++i) {
            int utf8Offset = this.constantPoolOffsets[this.u2At(readOffset)];
            char[] attributeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
            if (attributeName.length == 0) {
               readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
            } else {
               switch(attributeName[0]) {
                  case 'D':
                     if (CharOperation.equals(attributeName, AttributeNamesConstants.DeprecatedName)) {
                        this.accessFlags |= 1048576;
                     }
                     break;
                  case 'E':
                     if (CharOperation.equals(attributeName, AttributeNamesConstants.EnclosingMethodName)) {
                        utf8Offset = this.constantPoolOffsets[this.u2At(this.constantPoolOffsets[this.u2At(readOffset + 6)] + 1)];
                        this.enclosingTypeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
                        this.enclosingNameAndTypeIndex = this.u2At(readOffset + 8);
                     }
                     break;
                  case 'I':
                     if (CharOperation.equals(attributeName, AttributeNamesConstants.InnerClassName)) {
                        int innerOffset = readOffset + 6;
                        int number_of_classes = this.u2At(innerOffset);
                        if (number_of_classes != 0) {
                           innerOffset += 2;
                           this.innerInfos = new InnerClassInfo[number_of_classes];

                           for(int j = 0; j < number_of_classes; ++j) {
                              this.innerInfos[j] = new InnerClassInfo(this.reference, this.constantPoolOffsets, innerOffset);
                              if (this.classNameIndex == this.innerInfos[j].innerClassNameIndex) {
                                 this.innerInfo = this.innerInfos[j];
                                 this.innerInfoIndex = j;
                              }

                              innerOffset += 8;
                           }

                           if (this.innerInfo != null) {
                              char[] enclosingType = this.innerInfo.getEnclosingTypeName();
                              if (enclosingType != null) {
                                 this.enclosingTypeName = enclosingType;
                              }
                           }
                        }
                     } else if (CharOperation.equals(attributeName, AttributeNamesConstants.InconsistentHierarchy)) {
                        this.tagBits |= 131072L;
                     }
                     break;
                  case 'M':
                     if (CharOperation.equals(attributeName, AttributeNamesConstants.MissingTypesName)) {
                        int missingTypeOffset = readOffset + 6;
                        int numberOfMissingTypes = this.u2At(missingTypeOffset);
                        if (numberOfMissingTypes != 0) {
                           this.missingTypeNames = new char[numberOfMissingTypes][][];
                           missingTypeOffset += 2;

                           for(int j = 0; j < numberOfMissingTypes; ++j) {
                              utf8Offset = this.constantPoolOffsets[this.u2At(this.constantPoolOffsets[this.u2At(missingTypeOffset)] + 1)];
                              char[] missingTypeConstantPoolName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
                              this.missingTypeNames[j] = CharOperation.splitOn('/', missingTypeConstantPoolName);
                              missingTypeOffset += 2;
                           }
                        }
                     }
                     break;
                  case 'R':
                     if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
                        this.decodeAnnotations(readOffset, true);
                     } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleAnnotationsName)) {
                        this.decodeAnnotations(readOffset, false);
                     } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleTypeAnnotationsName)) {
                        this.decodeTypeAnnotations(readOffset, true);
                     } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleTypeAnnotationsName)) {
                        this.decodeTypeAnnotations(readOffset, false);
                     }
                     break;
                  case 'S':
                     if (attributeName.length > 2) {
                        switch(attributeName[1]) {
                           case 'i':
                              if (CharOperation.equals(attributeName, AttributeNamesConstants.SignatureName)) {
                                 utf8Offset = this.constantPoolOffsets[this.u2At(readOffset + 6)];
                                 this.signature = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
                              }
                              break;
                           case 'o':
                              if (CharOperation.equals(attributeName, AttributeNamesConstants.SourceName)) {
                                 utf8Offset = this.constantPoolOffsets[this.u2At(readOffset + 6)];
                                 this.sourceFileName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
                              }
                              break;
                           case 'y':
                              if (CharOperation.equals(attributeName, AttributeNamesConstants.SyntheticName)) {
                                 this.accessFlags |= 4096;
                              }
                        }
                     }
               }

               readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
            }
         }

         if (fullyInitialize) {
            this.initialize();
         }
      } catch (ClassFormatException var14) {
         throw var14;
      } catch (Exception var15) {
         throw new ClassFormatException(21, readOffset);
      }
   }

   public ZipFile setExternalAnnotationProvider(String basePath, String qualifiedBinaryTypeName, ZipFile zipFile, ClassFileReader.ZipFileProducer producer) throws IOException {
      this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.NO_EEA_FILE;
      String qualifiedBinaryFileName = qualifiedBinaryTypeName + ".eea";
      if (zipFile == null) {
         File annotationBase = new File(basePath);
         if (annotationBase.isDirectory()) {
            try {
               String filePath = annotationBase.getAbsolutePath() + '/' + qualifiedBinaryFileName;
               this.annotationProvider = new ExternalAnnotationProvider(new FileInputStream(filePath), String.valueOf(this.getName()));
               this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
            } catch (FileNotFoundException var8) {
            }

            return null;
         }

         if (!annotationBase.exists()) {
            return null;
         }

         zipFile = producer != null ? producer.produce() : new ZipFile(annotationBase);
      }

      ZipEntry entry = zipFile.getEntry(qualifiedBinaryFileName);
      if (entry != null) {
         this.annotationProvider = new ExternalAnnotationProvider(zipFile.getInputStream(entry), String.valueOf(this.getName()));
         this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
      }

      return zipFile;
   }

   public boolean hasAnnotationProvider() {
      return this.annotationProvider != null;
   }

   public void markAsFromSource() {
      this.externalAnnotationStatus = BinaryTypeBinding.ExternalAnnotationStatus.FROM_SOURCE;
   }

   @Override
   public BinaryTypeBinding.ExternalAnnotationStatus getExternalAnnotationStatus() {
      return this.externalAnnotationStatus;
   }

   @Override
   public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member, LookupEnvironment environment) {
      if (walker == ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER && this.annotationProvider != null) {
         if (member == null) {
            return this.annotationProvider.forTypeHeader(environment);
         }

         if (member instanceof IBinaryField) {
            IBinaryField field = (IBinaryField)member;
            char[] fieldSignature = field.getGenericSignature();
            if (fieldSignature == null) {
               fieldSignature = field.getTypeName();
            }

            return this.annotationProvider.forField(field.getName(), fieldSignature, environment);
         }

         if (member instanceof IBinaryMethod) {
            IBinaryMethod method = (IBinaryMethod)member;
            char[] methodSignature = method.getGenericSignature();
            if (methodSignature == null) {
               methodSignature = method.getMethodDescriptor();
            }

            return this.annotationProvider.forMethod(method.isConstructor() ? TypeConstants.INIT : method.getSelector(), methodSignature, environment);
         }
      }

      return walker;
   }

   public int accessFlags() {
      return this.accessFlags;
   }

   private void decodeAnnotations(int offset, boolean runtimeVisible) {
      int numberOfAnnotations = this.u2At(offset + 6);
      if (numberOfAnnotations > 0) {
         int readOffset = offset + 8;
         AnnotationInfo[] newInfos = null;
         int newInfoCount = 0;

         for(int i = 0; i < numberOfAnnotations; ++i) {
            AnnotationInfo newInfo = new AnnotationInfo(this.reference, this.constantPoolOffsets, readOffset, runtimeVisible, false);
            readOffset += newInfo.readOffset;
            long standardTagBits = newInfo.standardAnnotationTagBits;
            if (standardTagBits != 0L) {
               this.tagBits |= standardTagBits;
            } else {
               if (newInfos == null) {
                  newInfos = new AnnotationInfo[numberOfAnnotations - i];
               }

               newInfos[newInfoCount++] = newInfo;
            }
         }

         if (newInfos == null) {
            return;
         }

         if (this.annotations == null) {
            if (newInfoCount != newInfos.length) {
               System.arraycopy(newInfos, 0, newInfos = new AnnotationInfo[newInfoCount], 0, newInfoCount);
            }

            this.annotations = newInfos;
         } else {
            int length = this.annotations.length;
            AnnotationInfo[] temp = new AnnotationInfo[length + newInfoCount];
            System.arraycopy(this.annotations, 0, temp, 0, length);
            System.arraycopy(newInfos, 0, temp, length, newInfoCount);
            this.annotations = temp;
         }
      }
   }

   private void decodeTypeAnnotations(int offset, boolean runtimeVisible) {
      int numberOfAnnotations = this.u2At(offset + 6);
      if (numberOfAnnotations > 0) {
         int readOffset = offset + 8;
         TypeAnnotationInfo[] newInfos = null;
         newInfos = new TypeAnnotationInfo[numberOfAnnotations];

         for(int i = 0; i < numberOfAnnotations; ++i) {
            TypeAnnotationInfo newInfo = new TypeAnnotationInfo(this.reference, this.constantPoolOffsets, readOffset, runtimeVisible, false);
            readOffset += newInfo.readOffset;
            newInfos[i] = newInfo;
         }

         if (this.typeAnnotations == null) {
            this.typeAnnotations = newInfos;
         } else {
            int length = this.typeAnnotations.length;
            TypeAnnotationInfo[] temp = new TypeAnnotationInfo[length + numberOfAnnotations];
            System.arraycopy(this.typeAnnotations, 0, temp, 0, length);
            System.arraycopy(newInfos, 0, temp, length, numberOfAnnotations);
            this.typeAnnotations = temp;
         }
      }
   }

   @Override
   public IBinaryAnnotation[] getAnnotations() {
      return this.annotations;
   }

   @Override
   public IBinaryTypeAnnotation[] getTypeAnnotations() {
      return this.typeAnnotations;
   }

   private char[] getConstantClassNameAt(int constantPoolIndex) {
      int utf8Offset = this.constantPoolOffsets[this.u2At(this.constantPoolOffsets[constantPoolIndex] + 1)];
      return this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
   }

   public int[] getConstantPoolOffsets() {
      return this.constantPoolOffsets;
   }

   @Override
   public char[] getEnclosingMethod() {
      if (this.enclosingNameAndTypeIndex <= 0) {
         return null;
      } else {
         if (this.enclosingMethod == null) {
            StringBuffer buffer = new StringBuffer();
            int nameAndTypeOffset = this.constantPoolOffsets[this.enclosingNameAndTypeIndex];
            int utf8Offset = this.constantPoolOffsets[this.u2At(nameAndTypeOffset + 1)];
            buffer.append(this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1)));
            utf8Offset = this.constantPoolOffsets[this.u2At(nameAndTypeOffset + 3)];
            buffer.append(this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1)));
            this.enclosingMethod = String.valueOf(buffer).toCharArray();
         }

         return this.enclosingMethod;
      }
   }

   @Override
   public char[] getEnclosingTypeName() {
      return this.enclosingTypeName;
   }

   @Override
   public IBinaryField[] getFields() {
      return this.fields;
   }

   @Override
   public char[] getFileName() {
      return this.classFileName;
   }

   @Override
   public char[] getGenericSignature() {
      return this.signature;
   }

   public char[] getInnerSourceName() {
      return this.innerInfo != null ? this.innerInfo.getSourceName() : null;
   }

   @Override
   public char[][] getInterfaceNames() {
      return this.interfaceNames;
   }

   @Override
   public IBinaryNestedType[] getMemberTypes() {
      if (this.innerInfos == null) {
         return null;
      } else {
         int length = this.innerInfos.length;
         int startingIndex = this.innerInfo != null ? this.innerInfoIndex + 1 : 0;
         if (length != startingIndex) {
            IBinaryNestedType[] memberTypes = new IBinaryNestedType[length - this.innerInfoIndex];
            int memberTypeIndex = 0;

            for(int i = startingIndex; i < length; ++i) {
               InnerClassInfo currentInnerInfo = this.innerInfos[i];
               int outerClassNameIdx = currentInnerInfo.outerClassNameIndex;
               int innerNameIndex = currentInnerInfo.innerNameIndex;
               if (outerClassNameIdx != 0 && innerNameIndex != 0 && outerClassNameIdx == this.classNameIndex && currentInnerInfo.getSourceName().length != 0) {
                  memberTypes[memberTypeIndex++] = currentInnerInfo;
               }
            }

            if (memberTypeIndex == 0) {
               return null;
            } else {
               if (memberTypeIndex != memberTypes.length) {
                  System.arraycopy(memberTypes, 0, memberTypes = new IBinaryNestedType[memberTypeIndex], 0, memberTypeIndex);
               }

               return memberTypes;
            }
         } else {
            return null;
         }
      }
   }

   @Override
   public IBinaryMethod[] getMethods() {
      return this.methods;
   }

   @Override
   public char[][][] getMissingTypeNames() {
      return this.missingTypeNames;
   }

   @Override
   public int getModifiers() {
      int modifiers;
      if (this.innerInfo != null) {
         modifiers = this.innerInfo.getModifiers() | this.accessFlags & 1048576 | this.accessFlags & 4096;
      } else {
         modifiers = this.accessFlags;
      }

      return modifiers;
   }

   @Override
   public char[] getName() {
      return this.className;
   }

   @Override
   public char[] getSourceName() {
      if (this.sourceName != null) {
         return this.sourceName;
      } else {
         char[] name = this.getInnerSourceName();
         if (name == null) {
            name = this.getName();
            int start;
            if (this.isAnonymous()) {
               start = CharOperation.indexOf('$', name, CharOperation.lastIndexOf('/', name) + 1) + 1;
            } else {
               start = CharOperation.lastIndexOf('/', name) + 1;
            }

            if (start > 0) {
               char[] newName = new char[name.length - start];
               System.arraycopy(name, start, newName, 0, newName.length);
               name = newName;
            }
         }

         return this.sourceName = name;
      }
   }

   @Override
   public char[] getSuperclassName() {
      return this.superclassName;
   }

   @Override
   public long getTagBits() {
      return this.tagBits;
   }

   public long getVersion() {
      return this.version;
   }

   private boolean hasNonSyntheticFieldChanges(FieldInfo[] currentFieldInfos, FieldInfo[] otherFieldInfos) {
      int length1 = currentFieldInfos == null ? 0 : currentFieldInfos.length;
      int length2 = otherFieldInfos == null ? 0 : otherFieldInfos.length;
      int index1 = 0;
      int index2 = 0;

      label61:
      while(index1 < length1 && index2 < length2) {
         while(currentFieldInfos[index1].isSynthetic()) {
            if (++index1 >= length1) {
               break label61;
            }
         }

         while(otherFieldInfos[index2].isSynthetic()) {
            if (++index2 >= length2) {
               break label61;
            }
         }

         if (this.hasStructuralFieldChanges(currentFieldInfos[index1++], otherFieldInfos[index2++])) {
            return true;
         }
      }

      while(index1 < length1) {
         if (!currentFieldInfos[index1++].isSynthetic()) {
            return true;
         }
      }

      while(index2 < length2) {
         if (!otherFieldInfos[index2++].isSynthetic()) {
            return true;
         }
      }

      return false;
   }

   private boolean hasNonSyntheticMethodChanges(MethodInfo[] currentMethodInfos, MethodInfo[] otherMethodInfos) {
      int length1 = currentMethodInfos == null ? 0 : currentMethodInfos.length;
      int length2 = otherMethodInfos == null ? 0 : otherMethodInfos.length;
      int index1 = 0;
      int index2 = 0;

      label65:
      while(index1 < length1 && index2 < length2) {
         MethodInfo m;
         while((m = currentMethodInfos[index1]).isSynthetic() || m.isClinit()) {
            if (++index1 >= length1) {
               break label65;
            }
         }

         while((m = otherMethodInfos[index2]).isSynthetic() || m.isClinit()) {
            if (++index2 >= length2) {
               break label65;
            }
         }

         if (this.hasStructuralMethodChanges(currentMethodInfos[index1++], otherMethodInfos[index2++])) {
            return true;
         }
      }

      while(index1 < length1) {
         MethodInfo m;
         if (!(m = currentMethodInfos[index1++]).isSynthetic() && !m.isClinit()) {
            return true;
         }
      }

      while(index2 < length2) {
         MethodInfo m;
         if (!(m = otherMethodInfos[index2++]).isSynthetic() && !m.isClinit()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasStructuralChanges(byte[] newBytes) {
      return this.hasStructuralChanges(newBytes, true, true);
   }

   public boolean hasStructuralChanges(byte[] newBytes, boolean orderRequired, boolean excludesSynthetic) {
      try {
         ClassFileReader newClassFile = new ClassFileReader(newBytes, this.classFileName);
         if (this.getModifiers() != newClassFile.getModifiers()) {
            return true;
         } else {
            long OnlyStructuralTagBits = 27162300892971008L;
            if ((this.getTagBits() & OnlyStructuralTagBits) != (newClassFile.getTagBits() & OnlyStructuralTagBits)) {
               return true;
            } else if (this.hasStructuralAnnotationChanges(this.getAnnotations(), newClassFile.getAnnotations())) {
               return true;
            } else if (this.version >= 3407872L && this.hasStructuralTypeAnnotationChanges(this.getTypeAnnotations(), newClassFile.getTypeAnnotations())) {
               return true;
            } else if (!CharOperation.equals(this.getGenericSignature(), newClassFile.getGenericSignature())) {
               return true;
            } else if (!CharOperation.equals(this.getSuperclassName(), newClassFile.getSuperclassName())) {
               return true;
            } else {
               char[][] newInterfacesNames = newClassFile.getInterfaceNames();
               if (this.interfaceNames != newInterfacesNames) {
                  int newInterfacesLength = newInterfacesNames == null ? 0 : newInterfacesNames.length;
                  if (newInterfacesLength != this.interfacesCount) {
                     return true;
                  }

                  int i = 0;

                  for(int max = this.interfacesCount; i < max; ++i) {
                     if (!CharOperation.equals(this.interfaceNames[i], newInterfacesNames[i])) {
                        return true;
                     }
                  }
               }

               IBinaryNestedType[] currentMemberTypes = this.getMemberTypes();
               IBinaryNestedType[] otherMemberTypes = newClassFile.getMemberTypes();
               if (currentMemberTypes != otherMemberTypes) {
                  int currentMemberTypeLength = currentMemberTypes == null ? 0 : currentMemberTypes.length;
                  int otherMemberTypeLength = otherMemberTypes == null ? 0 : otherMemberTypes.length;
                  if (currentMemberTypeLength != otherMemberTypeLength) {
                     return true;
                  }

                  for(int i = 0; i < currentMemberTypeLength; ++i) {
                     if (!CharOperation.equals(currentMemberTypes[i].getName(), otherMemberTypes[i].getName())
                        || currentMemberTypes[i].getModifiers() != otherMemberTypes[i].getModifiers()) {
                        return true;
                     }
                  }
               }

               FieldInfo[] otherFieldInfos = (FieldInfo[])newClassFile.getFields();
               int otherFieldInfosLength = otherFieldInfos == null ? 0 : otherFieldInfos.length;
               boolean compareFields = true;
               if (this.fieldsCount == otherFieldInfosLength) {
                  int i = 0;

                  while(i < this.fieldsCount && !this.hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i])) {
                     ++i;
                  }

                  if ((compareFields = i != this.fieldsCount) && !orderRequired && !excludesSynthetic) {
                     return true;
                  }
               }

               if (compareFields) {
                  if (this.fieldsCount != otherFieldInfosLength && !excludesSynthetic) {
                     return true;
                  }

                  if (orderRequired) {
                     if (this.fieldsCount != 0) {
                        Arrays.sort((Object[])this.fields);
                     }

                     if (otherFieldInfosLength != 0) {
                        Arrays.sort((Object[])otherFieldInfos);
                     }
                  }

                  if (excludesSynthetic) {
                     if (this.hasNonSyntheticFieldChanges(this.fields, otherFieldInfos)) {
                        return true;
                     }
                  } else {
                     for(int i = 0; i < this.fieldsCount; ++i) {
                        if (this.hasStructuralFieldChanges(this.fields[i], otherFieldInfos[i])) {
                           return true;
                        }
                     }
                  }
               }

               MethodInfo[] otherMethodInfos = (MethodInfo[])newClassFile.getMethods();
               int otherMethodInfosLength = otherMethodInfos == null ? 0 : otherMethodInfos.length;
               boolean compareMethods = true;
               if (this.methodsCount == otherMethodInfosLength) {
                  int i = 0;

                  while(i < this.methodsCount && !this.hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i])) {
                     ++i;
                  }

                  if ((compareMethods = i != this.methodsCount) && !orderRequired && !excludesSynthetic) {
                     return true;
                  }
               }

               if (compareMethods) {
                  if (this.methodsCount != otherMethodInfosLength && !excludesSynthetic) {
                     return true;
                  }

                  if (orderRequired) {
                     if (this.methodsCount != 0) {
                        Arrays.sort((Object[])this.methods);
                     }

                     if (otherMethodInfosLength != 0) {
                        Arrays.sort((Object[])otherMethodInfos);
                     }
                  }

                  if (excludesSynthetic) {
                     if (this.hasNonSyntheticMethodChanges(this.methods, otherMethodInfos)) {
                        return true;
                     }
                  } else {
                     for(int i = 0; i < this.methodsCount; ++i) {
                        if (this.hasStructuralMethodChanges(this.methods[i], otherMethodInfos[i])) {
                           return true;
                        }
                     }
                  }
               }

               char[][][] missingTypes = this.getMissingTypeNames();
               char[][][] newMissingTypes = newClassFile.getMissingTypeNames();
               if (missingTypes != null) {
                  if (newMissingTypes == null) {
                     return true;
                  }

                  int length = missingTypes.length;
                  if (length != newMissingTypes.length) {
                     return true;
                  }

                  for(int i = 0; i < length; ++i) {
                     if (!CharOperation.equals(missingTypes[i], newMissingTypes[i])) {
                        return true;
                     }
                  }
               } else if (newMissingTypes != null) {
                  return true;
               }

               return false;
            }
         }
      } catch (ClassFormatException var20) {
         return true;
      }
   }

   private boolean hasStructuralAnnotationChanges(IBinaryAnnotation[] currentAnnotations, IBinaryAnnotation[] otherAnnotations) {
      if (currentAnnotations == otherAnnotations) {
         return false;
      } else {
         int currentAnnotationsLength = currentAnnotations == null ? 0 : currentAnnotations.length;
         int otherAnnotationsLength = otherAnnotations == null ? 0 : otherAnnotations.length;
         if (currentAnnotationsLength != otherAnnotationsLength) {
            return true;
         } else {
            for(int i = 0; i < currentAnnotationsLength; ++i) {
               Boolean match = this.matchAnnotations(currentAnnotations[i], otherAnnotations[i]);
               if (match != null) {
                  return match;
               }
            }

            return false;
         }
      }
   }

   private Boolean matchAnnotations(IBinaryAnnotation currentAnnotation, IBinaryAnnotation otherAnnotation) {
      if (!CharOperation.equals(currentAnnotation.getTypeName(), otherAnnotation.getTypeName())) {
         return true;
      } else {
         IBinaryElementValuePair[] currentPairs = currentAnnotation.getElementValuePairs();
         IBinaryElementValuePair[] otherPairs = otherAnnotation.getElementValuePairs();
         int currentPairsLength = currentPairs == null ? 0 : currentPairs.length;
         int otherPairsLength = otherPairs == null ? 0 : otherPairs.length;
         if (currentPairsLength != otherPairsLength) {
            return Boolean.TRUE;
         } else {
            for(int j = 0; j < currentPairsLength; ++j) {
               if (!CharOperation.equals(currentPairs[j].getName(), otherPairs[j].getName())) {
                  return Boolean.TRUE;
               }

               Object value = currentPairs[j].getValue();
               Object value2 = otherPairs[j].getValue();
               if (value instanceof Object[]) {
                  Object[] currentValues = (Object[])value;
                  if (!(value2 instanceof Object[])) {
                     return Boolean.TRUE;
                  }

                  Object[] currentValues2 = (Object[])value2;
                  int length = currentValues.length;
                  if (length != currentValues2.length) {
                     return Boolean.TRUE;
                  }

                  for(int n = 0; n < length; ++n) {
                     if (!currentValues[n].equals(currentValues2[n])) {
                        return Boolean.TRUE;
                     }
                  }

                  return Boolean.FALSE;
               }

               if (!value.equals(value2)) {
                  return Boolean.TRUE;
               }
            }

            return null;
         }
      }
   }

   private boolean hasStructuralFieldChanges(FieldInfo currentFieldInfo, FieldInfo otherFieldInfo) {
      if (!CharOperation.equals(currentFieldInfo.getGenericSignature(), otherFieldInfo.getGenericSignature())) {
         return true;
      } else if (currentFieldInfo.getModifiers() != otherFieldInfo.getModifiers()) {
         return true;
      } else if ((currentFieldInfo.getTagBits() & 70368744177664L) != (otherFieldInfo.getTagBits() & 70368744177664L)) {
         return true;
      } else if (this.hasStructuralAnnotationChanges(currentFieldInfo.getAnnotations(), otherFieldInfo.getAnnotations())) {
         return true;
      } else if (this.version >= 3407872L
         && this.hasStructuralTypeAnnotationChanges(currentFieldInfo.getTypeAnnotations(), otherFieldInfo.getTypeAnnotations())) {
         return true;
      } else if (!CharOperation.equals(currentFieldInfo.getName(), otherFieldInfo.getName())) {
         return true;
      } else if (!CharOperation.equals(currentFieldInfo.getTypeName(), otherFieldInfo.getTypeName())) {
         return true;
      } else if (currentFieldInfo.hasConstant() != otherFieldInfo.hasConstant()) {
         return true;
      } else {
         if (currentFieldInfo.hasConstant()) {
            Constant currentConstant = currentFieldInfo.getConstant();
            Constant otherConstant = otherFieldInfo.getConstant();
            if (currentConstant.typeID() != otherConstant.typeID()) {
               return true;
            }

            if (!currentConstant.getClass().equals(otherConstant.getClass())) {
               return true;
            }

            switch(currentConstant.typeID()) {
               case 2:
                  if (currentConstant.charValue() != otherConstant.charValue()) {
                     return true;
                  }

                  return false;
               case 3:
                  if (currentConstant.byteValue() != otherConstant.byteValue()) {
                     return true;
                  }

                  return false;
               case 4:
                  if (currentConstant.shortValue() != otherConstant.shortValue()) {
                     return true;
                  }

                  return false;
               case 5:
                  return currentConstant.booleanValue() ^ otherConstant.booleanValue();
               case 6:
               default:
                  break;
               case 7:
                  if (currentConstant.longValue() != otherConstant.longValue()) {
                     return true;
                  }

                  return false;
               case 8:
                  if (currentConstant.doubleValue() != otherConstant.doubleValue()) {
                     return true;
                  }

                  return false;
               case 9:
                  if (currentConstant.floatValue() != otherConstant.floatValue()) {
                     return true;
                  }

                  return false;
               case 10:
                  if (currentConstant.intValue() != otherConstant.intValue()) {
                     return true;
                  }

                  return false;
               case 11:
                  return !currentConstant.stringValue().equals(otherConstant.stringValue());
            }
         }

         return false;
      }
   }

   private boolean hasStructuralMethodChanges(MethodInfo currentMethodInfo, MethodInfo otherMethodInfo) {
      if (!CharOperation.equals(currentMethodInfo.getGenericSignature(), otherMethodInfo.getGenericSignature())) {
         return true;
      } else if (currentMethodInfo.getModifiers() != otherMethodInfo.getModifiers()) {
         return true;
      } else if ((currentMethodInfo.getTagBits() & 70368744177664L) != (otherMethodInfo.getTagBits() & 70368744177664L)) {
         return true;
      } else if (this.hasStructuralAnnotationChanges(currentMethodInfo.getAnnotations(), otherMethodInfo.getAnnotations())) {
         return true;
      } else {
         int currentAnnotatedParamsCount = currentMethodInfo.getAnnotatedParametersCount();
         int otherAnnotatedParamsCount = otherMethodInfo.getAnnotatedParametersCount();
         if (currentAnnotatedParamsCount != otherAnnotatedParamsCount) {
            return true;
         } else {
            for(int i = 0; i < currentAnnotatedParamsCount; ++i) {
               if (this.hasStructuralAnnotationChanges(
                  currentMethodInfo.getParameterAnnotations(i, this.classFileName), otherMethodInfo.getParameterAnnotations(i, this.classFileName)
               )) {
                  return true;
               }
            }

            if (this.version >= 3407872L
               && this.hasStructuralTypeAnnotationChanges(currentMethodInfo.getTypeAnnotations(), otherMethodInfo.getTypeAnnotations())) {
               return true;
            } else if (!CharOperation.equals(currentMethodInfo.getSelector(), otherMethodInfo.getSelector())) {
               return true;
            } else if (!CharOperation.equals(currentMethodInfo.getMethodDescriptor(), otherMethodInfo.getMethodDescriptor())) {
               return true;
            } else if (!CharOperation.equals(currentMethodInfo.getGenericSignature(), otherMethodInfo.getGenericSignature())) {
               return true;
            } else {
               char[][] currentThrownExceptions = currentMethodInfo.getExceptionTypeNames();
               char[][] otherThrownExceptions = otherMethodInfo.getExceptionTypeNames();
               if (currentThrownExceptions != otherThrownExceptions) {
                  int currentThrownExceptionsLength = currentThrownExceptions == null ? 0 : currentThrownExceptions.length;
                  int otherThrownExceptionsLength = otherThrownExceptions == null ? 0 : otherThrownExceptions.length;
                  if (currentThrownExceptionsLength != otherThrownExceptionsLength) {
                     return true;
                  }

                  for(int k = 0; k < currentThrownExceptionsLength; ++k) {
                     if (!CharOperation.equals(currentThrownExceptions[k], otherThrownExceptions[k])) {
                        return true;
                     }
                  }
               }

               return false;
            }
         }
      }
   }

   private boolean hasStructuralTypeAnnotationChanges(IBinaryTypeAnnotation[] currentTypeAnnotations, IBinaryTypeAnnotation[] otherTypeAnnotations) {
      if (otherTypeAnnotations != null) {
         int len = otherTypeAnnotations.length;
         System.arraycopy(otherTypeAnnotations, 0, otherTypeAnnotations = new IBinaryTypeAnnotation[len], 0, len);
      }

      if (currentTypeAnnotations != null) {
         label54:
         for(IBinaryTypeAnnotation currentAnnotation : currentTypeAnnotations) {
            if (this.affectsSignature(currentAnnotation)) {
               if (otherTypeAnnotations == null) {
                  return true;
               }

               for(int i = 0; i < otherTypeAnnotations.length; ++i) {
                  IBinaryTypeAnnotation otherAnnotation = otherTypeAnnotations[i];
                  if (otherAnnotation != null && this.matchAnnotations(currentAnnotation.getAnnotation(), otherAnnotation.getAnnotation()) == Boolean.TRUE) {
                     otherTypeAnnotations[i] = null;
                     continue label54;
                  }
               }

               return true;
            }
         }
      }

      if (otherTypeAnnotations != null) {
         for(IBinaryTypeAnnotation otherAnnotation : otherTypeAnnotations) {
            if (this.affectsSignature(otherAnnotation)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean affectsSignature(IBinaryTypeAnnotation typeAnnotation) {
      if (typeAnnotation == null) {
         return false;
      } else {
         int targetType = typeAnnotation.getTargetType();
         return targetType < 64 || targetType > 75;
      }
   }

   private void initialize() throws ClassFormatException {
      try {
         int i = 0;

         for(int max = this.fieldsCount; i < max; ++i) {
            this.fields[i].initialize();
         }

         i = 0;

         for(int max = this.methodsCount; i < max; ++i) {
            this.methods[i].initialize();
         }

         if (this.innerInfos != null) {
            i = 0;

            for(int max = this.innerInfos.length; i < max; ++i) {
               this.innerInfos[i].initialize();
            }
         }

         if (this.annotations != null) {
            i = 0;

            for(int max = this.annotations.length; i < max; ++i) {
               this.annotations[i].initialize();
            }
         }

         this.getEnclosingMethod();
         this.reset();
      } catch (RuntimeException var3) {
         ClassFormatException exception = new ClassFormatException(var3, this.classFileName);
         throw exception;
      }
   }

   @Override
   public boolean isAnonymous() {
      if (this.innerInfo == null) {
         return false;
      } else {
         char[] innerSourceName = this.innerInfo.getSourceName();
         return innerSourceName == null || innerSourceName.length == 0;
      }
   }

   @Override
   public boolean isBinaryType() {
      return true;
   }

   @Override
   public boolean isLocal() {
      if (this.innerInfo == null) {
         return false;
      } else if (this.innerInfo.getEnclosingTypeName() != null) {
         return false;
      } else {
         char[] innerSourceName = this.innerInfo.getSourceName();
         return innerSourceName != null && innerSourceName.length > 0;
      }
   }

   @Override
   public boolean isMember() {
      if (this.innerInfo == null) {
         return false;
      } else if (this.innerInfo.getEnclosingTypeName() == null) {
         return false;
      } else {
         char[] innerSourceName = this.innerInfo.getSourceName();
         return innerSourceName != null && innerSourceName.length > 0;
      }
   }

   public boolean isNestedType() {
      return this.innerInfo != null;
   }

   @Override
   public char[] sourceFileName() {
      return this.sourceFileName;
   }

   @Override
   public String toString() {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter print = new PrintWriter(out);
      print.println(this.getClass().getName() + "{");
      print.println(" this.className: " + new String(this.getName()));
      print.println(" this.superclassName: " + (this.getSuperclassName() == null ? "null" : new String(this.getSuperclassName())));
      print.println(" access_flags: " + printTypeModifiers(this.accessFlags()) + "(" + this.accessFlags() + ")");
      print.flush();
      return out.toString();
   }

   public interface ZipFileProducer {
      ZipFile produce() throws IOException;
   }
}
