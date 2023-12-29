package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public interface Invocation extends InvocationSite {
   Expression[] arguments();

   MethodBinding binding();

   void registerInferenceContext(ParameterizedGenericMethodBinding var1, InferenceContext18 var2);

   InferenceContext18 getInferenceContext(ParameterizedMethodBinding var1);

   void cleanUpInferenceContexts();

   void registerResult(TypeBinding var1, MethodBinding var2);
}
