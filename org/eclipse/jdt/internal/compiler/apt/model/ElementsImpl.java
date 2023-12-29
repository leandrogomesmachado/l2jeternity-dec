package org.eclipse.jdt.internal.compiler.apt.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ElementsImpl implements Elements {
   private static final Pattern INITIAL_DELIMITER = Pattern.compile("^\\s*/\\*+");
   private final BaseProcessingEnvImpl _env;

   public ElementsImpl(BaseProcessingEnvImpl env) {
      this._env = env;
   }

   @Override
   public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
      if (e.getKind() == ElementKind.CLASS && e instanceof TypeElementImpl) {
         List<AnnotationBinding> annotations = new ArrayList<>();
         Set<ReferenceBinding> annotationTypes = new HashSet<>();
         ReferenceBinding binding = (ReferenceBinding)((TypeElementImpl)e)._binding;

         for(boolean checkIfInherited = false; binding != null; checkIfInherited = true) {
            if (binding instanceof ParameterizedTypeBinding) {
               binding = ((ParameterizedTypeBinding)binding).genericType();
            }

            AnnotationBinding[] var9;
            for(AnnotationBinding annotation : var9 = Factory.getPackedAnnotationBindings(binding.getAnnotations())) {
               if (annotation != null) {
                  ReferenceBinding annotationType = annotation.getAnnotationType();
                  if ((!checkIfInherited || (annotationType.getAnnotationTagBits() & 281474976710656L) != 0L) && !annotationTypes.contains(annotationType)) {
                     annotationTypes.add(annotationType);
                     annotations.add(annotation);
                  }
               }
            }

            binding = binding.superclass();
         }

         List<AnnotationMirror> list = new ArrayList<>(annotations.size());

         for(AnnotationBinding annotation : annotations) {
            list.add(this._env.getFactory().newAnnotationMirror(annotation));
         }

         return Collections.unmodifiableList(list);
      } else {
         return e.getAnnotationMirrors();
      }
   }

   @Override
   public List<? extends Element> getAllMembers(TypeElement type) {
      if (type != null && type instanceof TypeElementImpl) {
         ReferenceBinding binding = (ReferenceBinding)((TypeElementImpl)type)._binding;
         Map<String, ReferenceBinding> types = new HashMap<>();
         List<FieldBinding> fields = new ArrayList<>();
         Map<String, Set<MethodBinding>> methods = new HashMap<>();
         Set<ReferenceBinding> superinterfaces = new LinkedHashSet<>();

         for(boolean ignoreVisibility = true; binding != null; ignoreVisibility = false) {
            this.addMembers(binding, ignoreVisibility, types, fields, methods);
            Set<ReferenceBinding> newfound = new LinkedHashSet<>();
            this.collectSuperInterfaces(binding, superinterfaces, newfound);

            for(ReferenceBinding superinterface : newfound) {
               this.addMembers(superinterface, false, types, fields, methods);
            }

            superinterfaces.addAll(newfound);
            binding = binding.superclass();
         }

         List<Element> allMembers = new ArrayList<>();

         for(ReferenceBinding nestedType : types.values()) {
            allMembers.add(this._env.getFactory().newElement(nestedType));
         }

         for(FieldBinding field : fields) {
            allMembers.add(this._env.getFactory().newElement(field));
         }

         for(Set<MethodBinding> sameNamedMethods : methods.values()) {
            for(MethodBinding method : sameNamedMethods) {
               allMembers.add(this._env.getFactory().newElement(method));
            }
         }

         return allMembers;
      } else {
         return Collections.emptyList();
      }
   }

   private void collectSuperInterfaces(ReferenceBinding type, Set<ReferenceBinding> existing, Set<ReferenceBinding> newfound) {
      ReferenceBinding[] var7;
      for(ReferenceBinding superinterface : var7 = type.superInterfaces()) {
         if (!existing.contains(superinterface) && !newfound.contains(superinterface)) {
            newfound.add(superinterface);
            this.collectSuperInterfaces(superinterface, existing, newfound);
         }
      }
   }

   private void addMembers(
      ReferenceBinding binding,
      boolean ignoreVisibility,
      Map<String, ReferenceBinding> types,
      List<FieldBinding> fields,
      Map<String, Set<MethodBinding>> methods
   ) {
      ReferenceBinding[] var9;
      for(ReferenceBinding subtype : var9 = binding.memberTypes()) {
         if (ignoreVisibility || !subtype.isPrivate()) {
            String name = new String(subtype.sourceName());
            if (types.get(name) == null) {
               types.put(name, subtype);
            }
         }
      }

      for(FieldBinding field : var22 = binding.fields()) {
         if (ignoreVisibility || !field.isPrivate()) {
            fields.add(field);
         }
      }

      for(MethodBinding method : var23 = binding.methods()) {
         if (!method.isSynthetic() && (ignoreVisibility || !method.isPrivate() && !method.isConstructor())) {
            String methodName = new String(method.selector);
            Set<MethodBinding> sameNamedMethods = methods.get(methodName);
            if (sameNamedMethods == null) {
               Set<MethodBinding> var25 = new HashSet(4);
               methods.put(methodName, var25);
               var25.add(method);
            } else {
               boolean unique = true;
               if (!ignoreVisibility) {
                  for(MethodBinding existing : sameNamedMethods) {
                     MethodVerifier verifier = this._env.getLookupEnvironment().methodVerifier();
                     if (verifier.doesMethodOverride(existing, method)) {
                        unique = false;
                        break;
                     }
                  }
               }

               if (unique) {
                  sameNamedMethods.add(method);
               }
            }
         }
      }
   }

   @Override
   public Name getBinaryName(TypeElement type) {
      TypeElementImpl typeElementImpl = (TypeElementImpl)type;
      ReferenceBinding referenceBinding = (ReferenceBinding)typeElementImpl._binding;
      return new NameImpl(CharOperation.replaceOnCopy(referenceBinding.constantPoolName(), '/', '.'));
   }

   @Override
   public String getConstantExpression(Object value) {
      if (!(value instanceof Integer)
         && !(value instanceof Byte)
         && !(value instanceof Float)
         && !(value instanceof Double)
         && !(value instanceof Long)
         && !(value instanceof Short)
         && !(value instanceof Character)
         && !(value instanceof String)
         && !(value instanceof Boolean)) {
         throw new IllegalArgumentException("Not a valid wrapper type : " + value.getClass());
      } else if (value instanceof Character) {
         StringBuilder builder = new StringBuilder();
         builder.append('\'').append(value).append('\'');
         return String.valueOf(builder);
      } else if (value instanceof String) {
         StringBuilder builder = new StringBuilder();
         builder.append('"').append(value).append('"');
         return String.valueOf(builder);
      } else if (value instanceof Float) {
         StringBuilder builder = new StringBuilder();
         builder.append(value).append('f');
         return String.valueOf(builder);
      } else if (value instanceof Long) {
         StringBuilder builder = new StringBuilder();
         builder.append(value).append('L');
         return String.valueOf(builder);
      } else if (value instanceof Short) {
         StringBuilder builder = new StringBuilder();
         builder.append("(short)").append(value);
         return String.valueOf(builder);
      } else if (value instanceof Byte) {
         StringBuilder builder = new StringBuilder();
         builder.append("(byte)0x");
         int intValue = (Byte)value;
         String hexString = Integer.toHexString(intValue & 0xFF);
         if (hexString.length() < 2) {
            builder.append('0');
         }

         builder.append(hexString);
         return String.valueOf(builder);
      } else {
         return String.valueOf(value);
      }
   }

   @Override
   public String getDocComment(Element e) {
      char[] unparsed = this.getUnparsedDocComment(e);
      return formatJavadoc(unparsed);
   }

   private char[] getUnparsedDocComment(Element e) {
      Javadoc javadoc = null;
      ReferenceContext referenceContext = null;
      switch(e.getKind()) {
         case PACKAGE:
            PackageElementImpl packageElementImpl = (PackageElementImpl)e;
            PackageBinding packageBinding = (PackageBinding)packageElementImpl._binding;
            char[][] compoundName = CharOperation.arrayConcat(packageBinding.compoundName, TypeConstants.PACKAGE_INFO_NAME);
            ReferenceBinding type = this._env.getLookupEnvironment().getType(compoundName);
            if (type != null && type.isValidBinding() && type instanceof SourceTypeBinding) {
               SourceTypeBinding sourceTypeBinding = (SourceTypeBinding)type;
               referenceContext = sourceTypeBinding.scope.referenceContext;
               javadoc = ((TypeDeclaration)referenceContext).javadoc;
            }
            break;
         case ENUM:
         case CLASS:
         case ANNOTATION_TYPE:
         case INTERFACE:
            TypeElementImpl typeElementImpl = (TypeElementImpl)e;
            ReferenceBinding referenceBinding = (ReferenceBinding)typeElementImpl._binding;
            if (referenceBinding instanceof SourceTypeBinding) {
               SourceTypeBinding sourceTypeBinding = (SourceTypeBinding)referenceBinding;
               referenceContext = sourceTypeBinding.scope.referenceContext;
               javadoc = ((TypeDeclaration)referenceContext).javadoc;
            }
            break;
         case ENUM_CONSTANT:
         case FIELD:
            VariableElementImpl variableElementImpl = (VariableElementImpl)e;
            FieldBinding fieldBinding = (FieldBinding)variableElementImpl._binding;
            FieldDeclaration sourceField = fieldBinding.sourceField();
            if (sourceField != null) {
               javadoc = sourceField.javadoc;
               if (fieldBinding.declaringClass instanceof SourceTypeBinding) {
                  SourceTypeBinding sourceTypeBinding = (SourceTypeBinding)fieldBinding.declaringClass;
                  referenceContext = sourceTypeBinding.scope.referenceContext;
               }
            }
            break;
         case PARAMETER:
         case LOCAL_VARIABLE:
         case EXCEPTION_PARAMETER:
         default:
            return null;
         case METHOD:
         case CONSTRUCTOR:
            ExecutableElementImpl executableElementImpl = (ExecutableElementImpl)e;
            MethodBinding methodBinding = (MethodBinding)executableElementImpl._binding;
            AbstractMethodDeclaration sourceMethod = methodBinding.sourceMethod();
            if (sourceMethod != null) {
               javadoc = sourceMethod.javadoc;
               referenceContext = sourceMethod;
            }
      }

      if (javadoc != null && referenceContext != null) {
         char[] contents = referenceContext.compilationResult().getCompilationUnit().getContents();
         if (contents != null) {
            return CharOperation.subarray(contents, javadoc.sourceStart, javadoc.sourceEnd - 1);
         }
      }

      return null;
   }

   private static String formatJavadoc(char[] unparsed) {
      if (unparsed != null && unparsed.length >= 5) {
         String[] lines = new String(unparsed).split("\n");
         Matcher delimiterMatcher = INITIAL_DELIMITER.matcher(lines[0]);
         if (!delimiterMatcher.find()) {
            return null;
         } else {
            int iOpener = delimiterMatcher.end();
            lines[0] = lines[0].substring(iOpener);
            if (lines.length == 1) {
               StringBuilder sb = new StringBuilder();
               char[] chars = lines[0].toCharArray();
               boolean startingWhitespaces = true;

               for(char c : chars) {
                  if (Character.isWhitespace(c)) {
                     if (!startingWhitespaces) {
                        sb.append(c);
                     }
                  } else {
                     startingWhitespaces = false;
                     sb.append(c);
                  }
               }

               return sb.toString();
            } else {
               int firstLine = lines[0].trim().length() > 0 ? 0 : 1;
               int lastLine = lines[lines.length - 1].trim().length() > 0 ? lines.length - 1 : lines.length - 2;
               StringBuilder sb = new StringBuilder();
               if (lines[0].length() != 0 && firstLine == 1) {
                  sb.append('\n');
               }

               boolean preserveLineSeparator = lines[0].length() == 0;

               for(int line = firstLine; line <= lastLine; ++line) {
                  char[] chars = lines[line].toCharArray();
                  int starsIndex = getStars(chars);
                  int leadingWhitespaces = 0;
                  boolean recordLeadingWhitespaces = true;
                  int i = 0;

                  for(int max = chars.length; i < max; ++i) {
                     char c = chars[i];
                     switch(c) {
                        case ' ':
                           if (starsIndex == -1) {
                              if (recordLeadingWhitespaces) {
                                 ++leadingWhitespaces;
                              } else {
                                 sb.append(c);
                              }
                           } else if (i >= starsIndex) {
                              sb.append(c);
                           }
                           break;
                        default:
                           recordLeadingWhitespaces = false;
                           if (leadingWhitespaces == 0) {
                              if (c == '\t') {
                                 if (i >= starsIndex) {
                                    sb.append(c);
                                 }
                              } else if (c != '*' || i > starsIndex) {
                                 sb.append(c);
                              }
                           } else {
                              int numberOfTabs = leadingWhitespaces / 8;
                              if (numberOfTabs != 0) {
                                 int j = 0;

                                 for(int max2 = numberOfTabs; j < max2; ++j) {
                                    sb.append("        ");
                                 }

                                 if (leadingWhitespaces % 8 >= 1) {
                                    sb.append(' ');
                                 }
                              } else if (line != 0) {
                                 int j = 0;

                                 for(int max2 = leadingWhitespaces; j < max2; ++j) {
                                    sb.append(' ');
                                 }
                              }

                              leadingWhitespaces = 0;
                              sb.append(c);
                           }
                     }
                  }

                  i = lines.length - 1;
                  if (line < i) {
                     sb.append('\n');
                  } else if (preserveLineSeparator && line == i) {
                     sb.append('\n');
                  }
               }

               return sb.toString();
            }
         }
      } else {
         return null;
      }
   }

   private static int getStars(char[] line) {
      int i = 0;

      for(int max = line.length; i < max; ++i) {
         char c = line[i];
         if (!Character.isWhitespace(c)) {
            if (c == '*') {
               for(int j = i + 1; j < max; ++j) {
                  if (line[j] != '*') {
                     return j;
                  }
               }

               return max - 1;
            }
            break;
         }
      }

      return -1;
   }

   @Override
   public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
      return ((AnnotationMirrorImpl)a).getElementValuesWithDefaults();
   }

   @Override
   public Name getName(CharSequence cs) {
      return new NameImpl(cs);
   }

   @Override
   public PackageElement getPackageElement(CharSequence name) {
      LookupEnvironment le = this._env.getLookupEnvironment();
      if (name.length() == 0) {
         return new PackageElementImpl(this._env, le.defaultPackage);
      } else {
         char[] packageName = name.toString().toCharArray();
         PackageBinding packageBinding = le.createPackage(CharOperation.splitOn('.', packageName));
         return packageBinding == null ? null : new PackageElementImpl(this._env, packageBinding);
      }
   }

   @Override
   public PackageElement getPackageOf(Element type) {
      switch(type.getKind()) {
         case PACKAGE:
            return (PackageElement)type;
         case ENUM:
         case CLASS:
         case ANNOTATION_TYPE:
         case INTERFACE:
            TypeElementImpl typeElementImpl = (TypeElementImpl)type;
            ReferenceBinding referenceBinding = (ReferenceBinding)typeElementImpl._binding;
            return (PackageElement)this._env.getFactory().newElement(referenceBinding.fPackage);
         case ENUM_CONSTANT:
         case FIELD: {
            VariableElementImpl variableElementImpl = (VariableElementImpl)type;
            FieldBinding fieldBinding = (FieldBinding)variableElementImpl._binding;
            return (PackageElement)this._env.getFactory().newElement(fieldBinding.declaringClass.fPackage);
         }
         case PARAMETER: {
            VariableElementImpl variableElementImpl = (VariableElementImpl)type;
            LocalVariableBinding localVariableBinding = (LocalVariableBinding)variableElementImpl._binding;
            return (PackageElement)this._env.getFactory().newElement(localVariableBinding.declaringScope.classScope().referenceContext.binding.fPackage);
         }
         case LOCAL_VARIABLE:
         case EXCEPTION_PARAMETER:
         case STATIC_INIT:
         case INSTANCE_INIT:
         case TYPE_PARAMETER:
         case OTHER:
            return null;
         case METHOD:
         case CONSTRUCTOR:
            ExecutableElementImpl executableElementImpl = (ExecutableElementImpl)type;
            MethodBinding methodBinding = (MethodBinding)executableElementImpl._binding;
            return (PackageElement)this._env.getFactory().newElement(methodBinding.declaringClass.fPackage);
         default:
            return null;
      }
   }

   @Override
   public TypeElement getTypeElement(CharSequence name) {
      LookupEnvironment le = this._env.getLookupEnvironment();
      char[][] compoundName = CharOperation.splitOn('.', name.toString().toCharArray());
      ReferenceBinding binding = le.getType(compoundName);
      if (binding == null) {
         ReferenceBinding topLevelBinding = null;
         int topLevelSegments = compoundName.length;

         while(--topLevelSegments > 0) {
            char[][] topLevelName = new char[topLevelSegments][];

            for(int i = 0; i < topLevelSegments; ++i) {
               topLevelName[i] = compoundName[i];
            }

            topLevelBinding = le.getType(topLevelName);
            if (topLevelBinding != null) {
               break;
            }
         }

         if (topLevelBinding == null) {
            return null;
         }

         binding = topLevelBinding;

         for(int i = topLevelSegments; binding != null && i < compoundName.length; ++i) {
            binding = binding.getMemberType(compoundName[i]);
         }
      }

      return binding == null ? null : new TypeElementImpl(this._env, binding, null);
   }

   @Override
   public boolean hides(Element hider, Element hidden) {
      if (hidden == null) {
         throw new NullPointerException();
      } else {
         return ((ElementImpl)hider).hides(hidden);
      }
   }

   @Override
   public boolean isDeprecated(Element e) {
      if (!(e instanceof ElementImpl)) {
         return false;
      } else {
         return (((ElementImpl)e)._binding.getAnnotationTagBits() & 70368744177664L) != 0L;
      }
   }

   @Override
   public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
      if (overridden != null && type != null) {
         return ((ExecutableElementImpl)overrider).overrides(overridden, type);
      } else {
         throw new NullPointerException();
      }
   }

   @Override
   public void printElements(Writer w, Element... elements) {
      String lineSeparator = System.getProperty("line.separator");

      for(Element element : elements) {
         try {
            w.write(element.toString());
            w.write(lineSeparator);
         } catch (IOException var9) {
         }
      }

      try {
         w.flush();
      } catch (IOException var8) {
      }
   }

   @Override
   public boolean isFunctionalInterface(TypeElement type) {
      if (type != null && type.getKind() == ElementKind.INTERFACE) {
         ReferenceBinding binding = (ReferenceBinding)((TypeElementImpl)type)._binding;
         if (binding instanceof SourceTypeBinding) {
            return binding.isFunctionalInterface(((SourceTypeBinding)binding).scope);
         }
      }

      return false;
   }
}
