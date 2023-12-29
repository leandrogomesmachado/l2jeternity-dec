package org.eclipse.jdt.internal.compiler;

import java.io.PrintWriter;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public abstract class AbstractAnnotationProcessorManager {
   public abstract void configure(Object var1, String[] var2);

   public abstract void configureFromPlatform(Compiler var1, Object var2, Object var3);

   public abstract void setOut(PrintWriter var1);

   public abstract void setErr(PrintWriter var1);

   public abstract ICompilationUnit[] getNewUnits();

   public abstract ReferenceBinding[] getNewClassFiles();

   public abstract ICompilationUnit[] getDeletedUnits();

   public abstract void reset();

   public abstract void processAnnotations(CompilationUnitDeclaration[] var1, ReferenceBinding[] var2, boolean var3);

   public abstract void setProcessors(Object[] var1);
}
