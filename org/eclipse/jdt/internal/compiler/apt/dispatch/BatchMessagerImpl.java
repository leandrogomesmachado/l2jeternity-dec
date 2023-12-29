package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class BatchMessagerImpl extends BaseMessagerImpl implements Messager {
   private final Main _compiler;
   private final BaseProcessingEnvImpl _processingEnv;

   public BatchMessagerImpl(BaseProcessingEnvImpl processingEnv, Main compiler) {
      this._compiler = compiler;
      this._processingEnv = processingEnv;
   }

   @Override
   public void printMessage(Kind kind, CharSequence msg) {
      this.printMessage(kind, msg, null, null, null);
   }

   @Override
   public void printMessage(Kind kind, CharSequence msg, Element e) {
      this.printMessage(kind, msg, e, null, null);
   }

   @Override
   public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
      this.printMessage(kind, msg, e, a, null);
   }

   @Override
   public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
      if (kind == Kind.ERROR) {
         this._processingEnv.setErrorRaised(true);
      }

      CategorizedProblem problem = createProblem(kind, msg, e, a, v);
      if (problem != null) {
         this._compiler.addExtraProblems(problem);
      }
   }
}
