package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

public final class ExtraFlags {
   public static final int HasNonPrivateStaticMemberTypes = 1;
   public static final int IsMemberType = 2;
   public static final int IsLocalType = 4;
   public static final int ParameterTypesStoredAsSignature = 16;

   public static int getExtraFlags(ClassFileReader reader) {
      int extraFlags = 0;
      if (reader.isNestedType()) {
         extraFlags |= 2;
      }

      if (reader.isLocal()) {
         extraFlags |= 4;
      }

      IBinaryNestedType[] memberTypes = reader.getMemberTypes();
      int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
      if (memberTypeCounter > 0) {
         for(int i = 0; i < memberTypeCounter; ++i) {
            int modifiers = memberTypes[i].getModifiers();
            if ((modifiers & 8) != 0 && (modifiers & 2) == 0) {
               extraFlags |= 1;
               break;
            }
         }
      }

      return extraFlags;
   }

   public static int getExtraFlags(IType type) throws JavaModelException {
      int extraFlags = 0;
      if (type.isMember()) {
         extraFlags |= 2;
      }

      if (type.isLocal()) {
         extraFlags |= 4;
      }

      IType[] memberTypes = type.getTypes();
      int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
      if (memberTypeCounter > 0) {
         for(int i = 0; i < memberTypeCounter; ++i) {
            int flags = memberTypes[i].getFlags();
            if ((flags & 8) != 0 && (flags & 2) == 0) {
               extraFlags |= 1;
               break;
            }
         }
      }

      return extraFlags;
   }

   public static int getExtraFlags(TypeDeclaration typeDeclaration) {
      int extraFlags = 0;
      if (typeDeclaration.enclosingType != null) {
         extraFlags |= 2;
      }

      TypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
      int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
      if (memberTypeCounter > 0) {
         for(int i = 0; i < memberTypeCounter; ++i) {
            int modifiers = memberTypes[i].modifiers;
            if ((modifiers & 8) != 0 && (modifiers & 2) == 0) {
               extraFlags |= 1;
               break;
            }
         }
      }

      return extraFlags;
   }
}
