package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.AptBinaryLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.AptSourceLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class VariableElementImpl extends ElementImpl implements VariableElement {
   VariableElementImpl(BaseProcessingEnvImpl env, VariableBinding binding) {
      super(env, binding);
   }

   @Override
   public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitVariable(this, p);
   }

   @Override
   protected AnnotationBinding[] getAnnotationBindings() {
      return ((VariableBinding)this._binding).getAnnotations();
   }

   @Override
   public Object getConstantValue() {
      VariableBinding variableBinding = (VariableBinding)this._binding;
      Constant constant = variableBinding.constant();
      if (constant != null && constant != Constant.NotAConstant) {
         TypeBinding type = variableBinding.type;
         switch(type.id) {
            case 2:
               return constant.charValue();
            case 3:
               return constant.byteValue();
            case 4:
               return constant.shortValue();
            case 5:
               return constant.booleanValue();
            case 6:
            default:
               return null;
            case 7:
               return constant.longValue();
            case 8:
               return constant.doubleValue();
            case 9:
               return constant.floatValue();
            case 10:
               return constant.intValue();
            case 11:
               return constant.stringValue();
         }
      } else {
         return null;
      }
   }

   @Override
   public List<? extends Element> getEnclosedElements() {
      return Collections.emptyList();
   }

   @Override
   public Element getEnclosingElement() {
      if (this._binding instanceof FieldBinding) {
         return this._env.getFactory().newElement(((FieldBinding)this._binding).declaringClass);
      } else if (this._binding instanceof AptSourceLocalVariableBinding) {
         return this._env.getFactory().newElement(((AptSourceLocalVariableBinding)this._binding).methodBinding);
      } else {
         return this._binding instanceof AptBinaryLocalVariableBinding
            ? this._env.getFactory().newElement(((AptBinaryLocalVariableBinding)this._binding).methodBinding)
            : null;
      }
   }

   @Override
   public ElementKind getKind() {
      if (this._binding instanceof FieldBinding) {
         return (((FieldBinding)this._binding).modifiers & 16384) != 0 ? ElementKind.ENUM_CONSTANT : ElementKind.FIELD;
      } else {
         return ElementKind.PARAMETER;
      }
   }

   @Override
   public Set<Modifier> getModifiers() {
      return this._binding instanceof VariableBinding
         ? Factory.getModifiers(((VariableBinding)this._binding).modifiers, this.getKind())
         : Collections.emptySet();
   }

   @Override
   PackageElement getPackage() {
      if (this._binding instanceof FieldBinding) {
         PackageBinding pkgBinding = ((FieldBinding)this._binding).declaringClass.fPackage;
         return this._env.getFactory().newPackageElement(pkgBinding);
      } else {
         throw new UnsupportedOperationException("NYI: VariableElmentImpl.getPackage() for method parameter");
      }
   }

   @Override
   public Name getSimpleName() {
      return new NameImpl(((VariableBinding)this._binding).name);
   }

   @Override
   public boolean hides(Element hiddenElement) {
      if (this._binding instanceof FieldBinding) {
         if (!(((ElementImpl)hiddenElement)._binding instanceof FieldBinding)) {
            return false;
         } else {
            FieldBinding hidden = (FieldBinding)((ElementImpl)hiddenElement)._binding;
            if (hidden.isPrivate()) {
               return false;
            } else {
               FieldBinding hider = (FieldBinding)this._binding;
               if (hidden == hider) {
                  return false;
               } else if (!CharOperation.equals(hider.name, hidden.name)) {
                  return false;
               } else {
                  return hider.declaringClass.findSuperTypeOriginatingFrom(hidden.declaringClass) != null;
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return new String(((VariableBinding)this._binding).name);
   }
}
