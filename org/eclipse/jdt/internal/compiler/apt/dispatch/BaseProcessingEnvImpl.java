package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.apt.model.ElementsImpl;
import org.eclipse.jdt.internal.compiler.apt.model.Factory;
import org.eclipse.jdt.internal.compiler.apt.model.TypesImpl;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public abstract class BaseProcessingEnvImpl implements ProcessingEnvironment {
   protected Filer _filer;
   protected Messager _messager;
   protected Map<String, String> _processorOptions;
   protected Compiler _compiler;
   protected Elements _elementUtils;
   protected Types _typeUtils;
   private List<ICompilationUnit> _addedUnits = new ArrayList<>();
   private List<ReferenceBinding> _addedClassFiles = new ArrayList<>();
   private List<ICompilationUnit> _deletedUnits = new ArrayList<>();
   private boolean _errorRaised;
   private Factory _factory;

   public BaseProcessingEnvImpl() {
      this._elementUtils = new ElementsImpl(this);
      this._typeUtils = new TypesImpl(this);
      this._factory = new Factory(this);
      this._errorRaised = false;
   }

   public void addNewUnit(ICompilationUnit unit) {
      this._addedUnits.add(unit);
   }

   public void addNewClassFile(ReferenceBinding binding) {
      this._addedClassFiles.add(binding);
   }

   public Compiler getCompiler() {
      return this._compiler;
   }

   public ICompilationUnit[] getDeletedUnits() {
      ICompilationUnit[] result = new ICompilationUnit[this._deletedUnits.size()];
      this._deletedUnits.toArray(result);
      return result;
   }

   public ICompilationUnit[] getNewUnits() {
      ICompilationUnit[] result = new ICompilationUnit[this._addedUnits.size()];
      this._addedUnits.toArray(result);
      return result;
   }

   @Override
   public Elements getElementUtils() {
      return this._elementUtils;
   }

   @Override
   public Filer getFiler() {
      return this._filer;
   }

   @Override
   public Messager getMessager() {
      return this._messager;
   }

   @Override
   public Map<String, String> getOptions() {
      return this._processorOptions;
   }

   @Override
   public Types getTypeUtils() {
      return this._typeUtils;
   }

   public LookupEnvironment getLookupEnvironment() {
      return this._compiler.lookupEnvironment;
   }

   @Override
   public SourceVersion getSourceVersion() {
      if (this._compiler.options.sourceLevel <= 3211264L) {
         return SourceVersion.RELEASE_5;
      } else if (this._compiler.options.sourceLevel == 3276800L) {
         return SourceVersion.RELEASE_6;
      } else {
         try {
            return SourceVersion.valueOf("RELEASE_7");
         } catch (IllegalArgumentException var1) {
            return SourceVersion.RELEASE_6;
         }
      }
   }

   public void reset() {
      this._addedUnits.clear();
      this._addedClassFiles.clear();
      this._deletedUnits.clear();
   }

   public boolean errorRaised() {
      return this._errorRaised;
   }

   public void setErrorRaised(boolean b) {
      this._errorRaised = true;
   }

   public Factory getFactory() {
      return this._factory;
   }

   public ReferenceBinding[] getNewClassFiles() {
      ReferenceBinding[] result = new ReferenceBinding[this._addedClassFiles.size()];
      this._addedClassFiles.toArray(result);
      return result;
   }
}
