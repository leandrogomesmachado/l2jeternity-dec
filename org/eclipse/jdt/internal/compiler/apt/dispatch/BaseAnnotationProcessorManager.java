package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.internal.compiler.AbstractAnnotationProcessorManager;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public abstract class BaseAnnotationProcessorManager extends AbstractAnnotationProcessorManager implements IProcessorProvider {
   protected PrintWriter _out;
   protected PrintWriter _err;
   protected BaseProcessingEnvImpl _processingEnv;
   protected boolean _isFirstRound = true;
   protected List<ProcessorInfo> _processors = new ArrayList<>();
   protected boolean _printProcessorInfo = false;
   protected boolean _printRounds = false;
   protected int _round;

   @Override
   public void configure(Object batchCompiler, String[] options) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void configureFromPlatform(Compiler compiler, Object compilationUnitLocator, Object javaProject) {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<ProcessorInfo> getDiscoveredProcessors() {
      return this._processors;
   }

   @Override
   public ICompilationUnit[] getDeletedUnits() {
      return this._processingEnv.getDeletedUnits();
   }

   @Override
   public ICompilationUnit[] getNewUnits() {
      return this._processingEnv.getNewUnits();
   }

   @Override
   public ReferenceBinding[] getNewClassFiles() {
      return this._processingEnv.getNewClassFiles();
   }

   @Override
   public void reset() {
      this._processingEnv.reset();
   }

   @Override
   public void setErr(PrintWriter err) {
      this._err = err;
   }

   @Override
   public void setOut(PrintWriter out) {
      this._out = out;
   }

   @Override
   public void setProcessors(Object[] processors) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void processAnnotations(CompilationUnitDeclaration[] units, ReferenceBinding[] referenceBindings, boolean isLastRound) {
      RoundEnvImpl roundEnv = new RoundEnvImpl(units, referenceBindings, isLastRound, this._processingEnv);
      if (this._isFirstRound) {
         this._isFirstRound = false;
      }

      PrintWriter traceProcessorInfo = this._printProcessorInfo ? this._out : null;
      PrintWriter traceRounds = this._printRounds ? this._out : null;
      if (traceRounds != null) {
         traceRounds.println("Round " + ++this._round + ':');
      }

      RoundDispatcher dispatcher = new RoundDispatcher(this, roundEnv, roundEnv.getRootAnnotations(), traceProcessorInfo, traceRounds);
      dispatcher.round();
   }
}
