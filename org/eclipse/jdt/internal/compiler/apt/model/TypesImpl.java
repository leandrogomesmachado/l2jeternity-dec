package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypesImpl implements Types {
   private final BaseProcessingEnvImpl _env;

   public TypesImpl(BaseProcessingEnvImpl env) {
      this._env = env;
   }

   @Override
   public Element asElement(TypeMirror t) {
      switch(t.getKind()) {
         case DECLARED:
         case TYPEVAR:
            return this._env.getFactory().newElement(((TypeMirrorImpl)t).binding());
         case ERROR:
         default:
            return null;
      }
   }

   @Override
   public TypeMirror asMemberOf(DeclaredType containing, Element element) {
      ElementImpl elementImpl = (ElementImpl)element;
      DeclaredTypeImpl declaredTypeImpl = (DeclaredTypeImpl)containing;
      ReferenceBinding referenceBinding = (ReferenceBinding)declaredTypeImpl._binding;
      switch(element.getKind()) {
         case ENUM:
         case CLASS:
         case ANNOTATION_TYPE:
         case INTERFACE:
            for(ReferenceBinding elementBinding = (ReferenceBinding)elementImpl._binding;
               referenceBinding != null;
               referenceBinding = referenceBinding.superclass()
            ) {
               ReferenceBinding[] var12;
               for(ReferenceBinding memberReferenceBinding : var12 = referenceBinding.memberTypes()) {
                  if (CharOperation.equals(elementBinding.compoundName, memberReferenceBinding.compoundName)) {
                     return this._env.getFactory().newTypeMirror(memberReferenceBinding);
                  }
               }
            }

            throw new IllegalArgumentException("element " + element + " is not a member of the containing type " + containing + " nor any of its superclasses");
         case ENUM_CONSTANT:
         case FIELD:
            for(FieldBinding fieldBinding = (FieldBinding)elementImpl._binding; referenceBinding != null; referenceBinding = referenceBinding.superclass()) {
               FieldBinding[] var11;
               for(FieldBinding field : var11 = referenceBinding.fields()) {
                  if (CharOperation.equals(field.name, fieldBinding.name)) {
                     return this._env.getFactory().newTypeMirror(field);
                  }
               }
            }

            throw new IllegalArgumentException("element " + element + " is not a member of the containing type " + containing + " nor any of its superclasses");
         case PARAMETER:
         case LOCAL_VARIABLE:
         case EXCEPTION_PARAMETER:
         default:
            throw new IllegalArgumentException("element " + element + " has unrecognized element kind " + element.getKind());
         case METHOD:
         case CONSTRUCTOR:
            for(MethodBinding methodBinding = (MethodBinding)elementImpl._binding; referenceBinding != null; referenceBinding = referenceBinding.superclass()) {
               MethodBinding[] var10;
               for(MethodBinding method : var10 = referenceBinding.methods()) {
                  if (CharOperation.equals(method.selector, methodBinding.selector)
                     && (method.original() == methodBinding || method.areParameterErasuresEqual(methodBinding))) {
                     return this._env.getFactory().newTypeMirror(method);
                  }
               }
            }

            throw new IllegalArgumentException("element " + element + " is not a member of the containing type " + containing + " nor any of its superclasses");
      }
   }

   @Override
   public TypeElement boxedClass(PrimitiveType p) {
      PrimitiveTypeImpl primitiveTypeImpl = (PrimitiveTypeImpl)p;
      BaseTypeBinding baseTypeBinding = (BaseTypeBinding)primitiveTypeImpl._binding;
      TypeBinding boxed = this._env.getLookupEnvironment().computeBoxingType(baseTypeBinding);
      return (TypeElement)this._env.getFactory().newElement(boxed);
   }

   @Override
   public TypeMirror capture(TypeMirror t) {
      throw new UnsupportedOperationException("NYI: TypesImpl.capture(...)");
   }

   @Override
   public boolean contains(TypeMirror t1, TypeMirror t2) {
      switch(t1.getKind()) {
         case PACKAGE:
         case EXECUTABLE:
            throw new IllegalArgumentException("Executable and package are illegal argument for Types.contains(..)");
         default:
            switch(t2.getKind()) {
               case PACKAGE:
               case EXECUTABLE:
                  throw new IllegalArgumentException("Executable and package are illegal argument for Types.contains(..)");
               default:
                  throw new UnsupportedOperationException("NYI: TypesImpl.contains(" + t1 + ", " + t2 + ")");
            }
      }
   }

   @Override
   public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
      ReferenceBinding referenceBinding;
      ArrayList<TypeMirror> list;
      switch(t.getKind()) {
         case PACKAGE:
         case EXECUTABLE:
            throw new IllegalArgumentException("Invalid type mirror for directSupertypes");
         default:
            TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl)t;
            Binding binding = typeMirrorImpl._binding;
            if (!(binding instanceof ReferenceBinding)) {
               return Collections.emptyList();
            }

            referenceBinding = (ReferenceBinding)binding;
            list = new ArrayList<>();
            ReferenceBinding superclass = referenceBinding.superclass();
            if (superclass != null) {
               list.add(this._env.getFactory().newTypeMirror(superclass));
            }
      }

      ReferenceBinding[] var10;
      for(ReferenceBinding interfaceBinding : var10 = referenceBinding.superInterfaces()) {
         list.add(this._env.getFactory().newTypeMirror(interfaceBinding));
      }

      return Collections.unmodifiableList(list);
   }

   @Override
   public TypeMirror erasure(TypeMirror t) {
      TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl)t;
      Binding binding = typeMirrorImpl._binding;
      if (binding instanceof ReferenceBinding) {
         TypeBinding type = ((ReferenceBinding)binding).erasure();
         if (type.isGenericType()) {
            type = this._env.getLookupEnvironment().convertToRawType(type, false);
         }

         return this._env.getFactory().newTypeMirror(type);
      } else if (binding instanceof ArrayBinding) {
         TypeBinding typeBinding = (TypeBinding)binding;
         TypeBinding leafType = typeBinding.leafComponentType().erasure();
         if (leafType.isGenericType()) {
            leafType = this._env.getLookupEnvironment().convertToRawType(leafType, false);
         }

         return this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().createArrayType(leafType, typeBinding.dimensions()));
      } else {
         return t;
      }
   }

   @Override
   public ArrayType getArrayType(TypeMirror componentType) {
      TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl)componentType;
      TypeBinding typeBinding = (TypeBinding)typeMirrorImpl._binding;
      return (ArrayType)this._env
         .getFactory()
         .newTypeMirror(this._env.getLookupEnvironment().createArrayType(typeBinding.leafComponentType(), typeBinding.dimensions() + 1));
   }

   @Override
   public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
      int typeArgsLength = typeArgs.length;
      TypeElementImpl typeElementImpl = (TypeElementImpl)typeElem;
      ReferenceBinding elementBinding = (ReferenceBinding)typeElementImpl._binding;
      TypeVariableBinding[] typeVariables = elementBinding.typeVariables();
      int typeVariablesLength = typeVariables.length;
      if (typeArgsLength == 0) {
         return elementBinding.isGenericType()
            ? (DeclaredType)this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().createRawType(elementBinding, null))
            : (DeclaredType)typeElem.asType();
      } else if (typeArgsLength != typeVariablesLength) {
         throw new IllegalArgumentException("Number of typeArguments doesn't match the number of formal parameters of typeElem");
      } else {
         TypeBinding[] typeArguments = new TypeBinding[typeArgsLength];

         for(int i = 0; i < typeArgsLength; ++i) {
            TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl)typeArgs[i];
            Binding binding = typeMirrorImpl._binding;
            if (!(binding instanceof TypeBinding)) {
               throw new IllegalArgumentException("Invalid type argument: " + typeMirrorImpl);
            }

            typeArguments[i] = (TypeBinding)binding;
         }

         return (DeclaredType)this._env
            .getFactory()
            .newTypeMirror(this._env.getLookupEnvironment().createParameterizedType(elementBinding, typeArguments, null));
      }
   }

   @Override
   public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
      int typeArgsLength = typeArgs.length;
      TypeElementImpl typeElementImpl = (TypeElementImpl)typeElem;
      ReferenceBinding elementBinding = (ReferenceBinding)typeElementImpl._binding;
      TypeVariableBinding[] typeVariables = elementBinding.typeVariables();
      int typeVariablesLength = typeVariables.length;
      DeclaredTypeImpl declaredTypeImpl = (DeclaredTypeImpl)containing;
      ReferenceBinding enclosingType = (ReferenceBinding)declaredTypeImpl._binding;
      if (typeArgsLength == 0) {
         if (elementBinding.isGenericType()) {
            return (DeclaredType)this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().createRawType(elementBinding, enclosingType));
         } else {
            ParameterizedTypeBinding ptb = this._env.getLookupEnvironment().createParameterizedType(elementBinding, null, enclosingType);
            return (DeclaredType)this._env.getFactory().newTypeMirror(ptb);
         }
      } else if (typeArgsLength != typeVariablesLength) {
         throw new IllegalArgumentException("Number of typeArguments doesn't match the number of formal parameters of typeElem");
      } else {
         TypeBinding[] typeArguments = new TypeBinding[typeArgsLength];

         for(int i = 0; i < typeArgsLength; ++i) {
            TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl)typeArgs[i];
            Binding binding = typeMirrorImpl._binding;
            if (!(binding instanceof TypeBinding)) {
               throw new IllegalArgumentException("Invalid type for a type arguments : " + typeMirrorImpl);
            }

            typeArguments[i] = (TypeBinding)binding;
         }

         return (DeclaredType)this._env
            .getFactory()
            .newTypeMirror(this._env.getLookupEnvironment().createParameterizedType(elementBinding, typeArguments, enclosingType));
      }
   }

   @Override
   public NoType getNoType(TypeKind kind) {
      return this._env.getFactory().getNoType(kind);
   }

   @Override
   public NullType getNullType() {
      return this._env.getFactory().getNullType();
   }

   @Override
   public PrimitiveType getPrimitiveType(TypeKind kind) {
      return this._env.getFactory().getPrimitiveType(kind);
   }

   @Override
   public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
      if (extendsBound != null && superBound != null) {
         throw new IllegalArgumentException("Extends and super bounds cannot be set at the same time");
      } else if (extendsBound != null) {
         TypeMirrorImpl extendsBoundMirrorType = (TypeMirrorImpl)extendsBound;
         TypeBinding typeBinding = (TypeBinding)extendsBoundMirrorType._binding;
         return (WildcardType)this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().createWildcard(null, 0, typeBinding, null, 1));
      } else if (superBound != null) {
         TypeMirrorImpl superBoundMirrorType = (TypeMirrorImpl)superBound;
         TypeBinding typeBinding = (TypeBinding)superBoundMirrorType._binding;
         return new WildcardTypeImpl(this._env, this._env.getLookupEnvironment().createWildcard(null, 0, typeBinding, null, 2));
      } else {
         return new WildcardTypeImpl(this._env, this._env.getLookupEnvironment().createWildcard(null, 0, null, null, 0));
      }
   }

   @Override
   public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
      if (t1 instanceof TypeMirrorImpl && t2 instanceof TypeMirrorImpl) {
         Binding b1 = ((TypeMirrorImpl)t1).binding();
         Binding b2 = ((TypeMirrorImpl)t2).binding();
         if (b1 instanceof TypeBinding && b2 instanceof TypeBinding) {
            if (((TypeBinding)b1).isCompatibleWith((TypeBinding)b2)) {
               return true;
            } else {
               TypeBinding convertedType = this._env.getLookupEnvironment().computeBoxingType((TypeBinding)b1);
               return convertedType != null && convertedType.isCompatibleWith((TypeBinding)b2);
            }
         } else {
            throw new IllegalArgumentException();
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isSameType(TypeMirror t1, TypeMirror t2) {
      if (t1.getKind() == TypeKind.WILDCARD || t2.getKind() == TypeKind.WILDCARD) {
         return false;
      } else if (t1 == t2) {
         return true;
      } else if (t1 instanceof TypeMirrorImpl && t2 instanceof TypeMirrorImpl) {
         Binding b1 = ((TypeMirrorImpl)t1).binding();
         Binding b2 = ((TypeMirrorImpl)t2).binding();
         if (b1 == b2) {
            return true;
         } else if (b1 instanceof TypeBinding && b2 instanceof TypeBinding) {
            TypeBinding type1 = (TypeBinding)b1;
            TypeBinding type2 = (TypeBinding)b2;
            return TypeBinding.equalsEquals(type1, type2) ? true : CharOperation.equals(type1.computeUniqueKey(), type2.computeUniqueKey());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
      MethodBinding methodBinding1 = (MethodBinding)((ExecutableTypeImpl)m1)._binding;
      MethodBinding methodBinding2 = (MethodBinding)((ExecutableTypeImpl)m2)._binding;
      if (!CharOperation.equals(methodBinding1.selector, methodBinding2.selector)) {
         return false;
      } else {
         return methodBinding1.areParameterErasuresEqual(methodBinding2) && methodBinding1.areTypeVariableErasuresEqual(methodBinding2);
      }
   }

   @Override
   public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
      if (t1 instanceof NoTypeImpl) {
         if (t2 instanceof NoTypeImpl) {
            return ((NoTypeImpl)t1).getKind() == ((NoTypeImpl)t2).getKind();
         } else {
            return false;
         }
      } else if (t2 instanceof NoTypeImpl) {
         return false;
      } else if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
         return false;
      } else if (t1 == t2) {
         return true;
      } else {
         Binding b1 = ((TypeMirrorImpl)t1).binding();
         Binding b2 = ((TypeMirrorImpl)t2).binding();
         if (b1 == b2) {
            return true;
         } else if (b1 instanceof TypeBinding && b2 instanceof TypeBinding) {
            if (b1.kind() != 132 && b2.kind() != 132) {
               return ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
            } else {
               return b1.kind() != b2.kind() ? false : ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public PrimitiveType unboxedType(TypeMirror t) {
      if (!(((TypeMirrorImpl)t)._binding instanceof ReferenceBinding)) {
         throw new IllegalArgumentException("Given type mirror cannot be unboxed");
      } else {
         ReferenceBinding boxed = (ReferenceBinding)((TypeMirrorImpl)t)._binding;
         TypeBinding unboxed = this._env.getLookupEnvironment().computeBoxingType(boxed);
         if (unboxed.kind() != 132) {
            throw new IllegalArgumentException();
         } else {
            return (PrimitiveType)this._env.getFactory().newTypeMirror((BaseTypeBinding)unboxed);
         }
      }
   }
}
