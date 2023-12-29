package org.eclipse.jdt.internal.compiler;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerStats;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeCollisionException;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Compiler implements ITypeRequestor, ProblemSeverities {
   public Parser parser;
   public ICompilerRequestor requestor;
   public CompilerOptions options;
   public ProblemReporter problemReporter;
   protected PrintWriter out;
   public CompilerStats stats;
   public CompilationProgress progress;
   public int remainingIterations = 1;
   public CompilationUnitDeclaration[] unitsToProcess;
   public int totalUnits;
   private Map<String, Compiler.APTProblem[]> aptProblems;
   public LookupEnvironment lookupEnvironment;
   public static boolean DEBUG = false;
   public int parseThreshold = -1;
   public AbstractAnnotationProcessorManager annotationProcessorManager;
   public int annotationProcessorStartIndex = 0;
   public ReferenceBinding[] referenceBindings;
   public boolean useSingleThread = true;
   public static IDebugRequestor DebugRequestor = null;

   /** @deprecated */
   public Compiler(
      INameEnvironment environment, IErrorHandlingPolicy policy, Map<String, String> settings, ICompilerRequestor requestor, IProblemFactory problemFactory
   ) {
      this(environment, policy, new CompilerOptions(settings), requestor, problemFactory, null, null);
   }

   /** @deprecated */
   public Compiler(
      INameEnvironment environment,
      IErrorHandlingPolicy policy,
      Map settings,
      ICompilerRequestor requestor,
      IProblemFactory problemFactory,
      boolean parseLiteralExpressionsAsConstants
   ) {
      this(environment, policy, new CompilerOptions(settings, parseLiteralExpressionsAsConstants), requestor, problemFactory, null, null);
   }

   public Compiler(
      INameEnvironment environment, IErrorHandlingPolicy policy, CompilerOptions options, ICompilerRequestor requestor, IProblemFactory problemFactory
   ) {
      this(environment, policy, options, requestor, problemFactory, null, null);
   }

   /** @deprecated */
   public Compiler(
      INameEnvironment environment,
      IErrorHandlingPolicy policy,
      CompilerOptions options,
      ICompilerRequestor requestor,
      IProblemFactory problemFactory,
      PrintWriter out
   ) {
      this(environment, policy, options, requestor, problemFactory, out, null);
   }

   public Compiler(
      INameEnvironment environment,
      IErrorHandlingPolicy policy,
      CompilerOptions options,
      final ICompilerRequestor requestor,
      IProblemFactory problemFactory,
      PrintWriter out,
      CompilationProgress progress
   ) {
      this.options = options;
      this.progress = progress;
      if (DebugRequestor == null) {
         this.requestor = requestor;
      } else {
         this.requestor = new ICompilerRequestor() {
            @Override
            public void acceptResult(CompilationResult result) {
               if (Compiler.DebugRequestor.isActive()) {
                  Compiler.DebugRequestor.acceptDebugResult(result);
               }

               requestor.acceptResult(result);
            }
         };
      }

      this.problemReporter = new ProblemReporter(policy, this.options, problemFactory);
      this.lookupEnvironment = new LookupEnvironment(this, this.options, this.problemReporter, environment);
      this.out = out == null ? new PrintWriter(System.out, true) : out;
      this.stats = new CompilerStats();
      this.initializeParser();
   }

   @Override
   public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
      if (this.options.verbose) {
         this.out.println(Messages.bind(Messages.compilation_loadBinary, new String(binaryType.getName())));
      }

      this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
   }

   @Override
   public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
      CompilationResult unitResult = new CompilationResult(sourceUnit, this.totalUnits, this.totalUnits, this.options.maxProblemsPerUnit);
      unitResult.checkSecondaryTypes = true;

      try {
         if (this.options.verbose) {
            String count = String.valueOf(this.totalUnits + 1);
            this.out.println(Messages.bind(Messages.compilation_request, new String[]{count, count, new String(sourceUnit.getFileName())}));
         }

         CompilationUnitDeclaration parsedUnit;
         if (this.totalUnits < this.parseThreshold) {
            parsedUnit = this.parser.parse(sourceUnit, unitResult);
         } else {
            parsedUnit = this.parser.dietParse(sourceUnit, unitResult);
         }

         this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
         this.addCompilationUnit(sourceUnit, parsedUnit);
         this.lookupEnvironment.completeTypeBindings(parsedUnit);
      } catch (AbortCompilationUnit var5) {
         if (unitResult.compilationUnit != sourceUnit) {
            throw var5;
         }

         this.requestor.acceptResult(unitResult.tagAsAccepted());
      }
   }

   @Override
   public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
      this.problemReporter
         .abortDueToInternalError(
            Messages.bind(
               Messages.abort_againstSourceModel, new String[]{String.valueOf(sourceTypes[0].getName()), String.valueOf(sourceTypes[0].getFileName())}
            )
         );
   }

   protected synchronized void addCompilationUnit(ICompilationUnit sourceUnit, CompilationUnitDeclaration parsedUnit) {
      if (this.unitsToProcess != null) {
         int size = this.unitsToProcess.length;
         if (this.totalUnits == size) {
            System.arraycopy(this.unitsToProcess, 0, this.unitsToProcess = new CompilationUnitDeclaration[size * 2], 0, this.totalUnits);
         }

         this.unitsToProcess[this.totalUnits++] = parsedUnit;
      }
   }

   protected void beginToCompile(ICompilationUnit[] sourceUnits) {
      int maxUnits = sourceUnits.length;
      this.totalUnits = 0;
      this.unitsToProcess = new CompilationUnitDeclaration[maxUnits];
      this.internalBeginToCompile(sourceUnits, maxUnits);
   }

   protected void reportProgress(String taskDecription) {
      if (this.progress != null) {
         if (this.progress.isCanceled()) {
            throw new AbortCompilation(true, null);
         }

         this.progress.setTaskName(taskDecription);
      }
   }

   protected void reportWorked(int workIncrement, int currentUnitIndex) {
      if (this.progress != null) {
         if (this.progress.isCanceled()) {
            throw new AbortCompilation(true, null);
         }

         this.progress.worked(workIncrement, this.totalUnits * this.remainingIterations - currentUnitIndex - 1);
      }
   }

   public void compile(ICompilationUnit[] sourceUnits) {
      this.compile(sourceUnits, false);
   }

   private void compile(ICompilationUnit[] sourceUnits, boolean lastRound) {
      this.stats.startTime = System.currentTimeMillis();

      try {
         this.reportProgress(Messages.compilation_beginningToCompile);
         if (this.annotationProcessorManager == null) {
            this.beginToCompile(sourceUnits);
         } else {
            ICompilationUnit[] originalUnits = (ICompilationUnit[])sourceUnits.clone();

            try {
               this.beginToCompile(sourceUnits);
               if (!lastRound) {
                  this.processAnnotations();
               }

               if (!this.options.generateClassFiles) {
                  return;
               }
            } catch (SourceTypeCollisionException var8) {
               this.backupAptProblems();
               this.reset();
               int originalLength = originalUnits.length;
               int newProcessedLength = var8.newAnnotationProcessorUnits.length;
               ICompilationUnit[] combinedUnits = new ICompilationUnit[originalLength + newProcessedLength];
               System.arraycopy(originalUnits, 0, combinedUnits, 0, originalLength);
               System.arraycopy(var8.newAnnotationProcessorUnits, 0, combinedUnits, originalLength, newProcessedLength);
               this.annotationProcessorStartIndex = originalLength;
               this.compile(combinedUnits, var8.isLastRound);
               return;
            }
         }

         this.restoreAptProblems();
         this.processCompiledUnits(0, lastRound);
      } catch (AbortCompilation var9) {
         this.handleInternalException(var9, null);
      }

      if (this.options.verbose) {
         if (this.totalUnits > 1) {
            this.out.println(Messages.bind(Messages.compilation_units, String.valueOf(this.totalUnits)));
         } else {
            this.out.println(Messages.bind(Messages.compilation_unit, String.valueOf(this.totalUnits)));
         }
      }
   }

   protected void backupAptProblems() {
      if (this.unitsToProcess != null) {
         for(int i = 0; i < this.totalUnits; ++i) {
            CompilationUnitDeclaration unitDecl = this.unitsToProcess[i];
            CompilationResult result = unitDecl.compilationResult;
            if (result != null && result.hasErrors()) {
               CategorizedProblem[] errors = result.getErrors();

               for(CategorizedProblem problem : errors) {
                  if (problem.getCategoryID() == 0) {
                     if (this.aptProblems == null) {
                        this.aptProblems = new HashMap<>();
                     }

                     Compiler.APTProblem[] problems = (Compiler.APTProblem[])this.aptProblems.get(new String(unitDecl.getFileName()));
                     if (problems == null) {
                        this.aptProblems
                           .put(new String(unitDecl.getFileName()), new Compiler.APTProblem[]{new Compiler.APTProblem(problem, result.getContext(problem))});
                     } else {
                        Compiler.APTProblem[] temp = new Compiler.APTProblem[problems.length + 1];
                        System.arraycopy(problems, 0, temp, 0, problems.length);
                        temp[problems.length] = new Compiler.APTProblem(problem, result.getContext(problem));
                        this.aptProblems.put(new String(unitDecl.getFileName()), temp);
                     }
                  }
               }
            }
         }
      }
   }

   protected void restoreAptProblems() {
      if (this.unitsToProcess != null && this.aptProblems != null) {
         for(int i = 0; i < this.totalUnits; ++i) {
            CompilationUnitDeclaration unitDecl = this.unitsToProcess[i];
            Compiler.APTProblem[] problems = (Compiler.APTProblem[])this.aptProblems.get(new String(unitDecl.getFileName()));
            if (problems != null) {
               for(Compiler.APTProblem problem : problems) {
                  unitDecl.compilationResult.record(problem.problem, problem.context);
               }
            }
         }
      }

      this.aptProblems = null;
   }

   protected void processCompiledUnits(int startingIndex, boolean lastRound) throws Error {
      CompilationUnitDeclaration unit = null;
      ProcessTaskManager processingTask = null;

      try {
         if (this.useSingleThread) {
            for(int i = startingIndex; i < this.totalUnits; ++i) {
               unit = this.unitsToProcess[i];
               if (unit.compilationResult == null || !unit.compilationResult.hasBeenAccepted) {
                  this.reportProgress(Messages.bind(Messages.compilation_processing, new String(unit.getFileName())));

                  try {
                     if (this.options.verbose) {
                        this.out
                           .println(
                              Messages.bind(
                                 Messages.compilation_process,
                                 new String[]{String.valueOf(i + 1), String.valueOf(this.totalUnits), new String(this.unitsToProcess[i].getFileName())}
                              )
                           );
                     }

                     this.process(unit, i);
                  } finally {
                     if (this.annotationProcessorManager == null || this.shouldCleanup(i)) {
                        unit.cleanUp();
                     }
                  }

                  if (this.annotationProcessorManager == null) {
                     this.unitsToProcess[i] = null;
                  }

                  this.reportWorked(1, i);
                  this.stats.lineCount += (long)unit.compilationResult.lineSeparatorPositions.length;
                  long acceptStart = System.currentTimeMillis();
                  this.requestor.acceptResult(unit.compilationResult.tagAsAccepted());
                  this.stats.generateTime += System.currentTimeMillis() - acceptStart;
                  if (this.options.verbose) {
                     this.out
                        .println(
                           Messages.bind(
                              Messages.compilation_done, new String[]{String.valueOf(i + 1), String.valueOf(this.totalUnits), new String(unit.getFileName())}
                           )
                        );
                  }
               }
            }
         } else {
            processingTask = new ProcessTaskManager(this, startingIndex);
            int acceptedCount = 0;

            while(true) {
               try {
                  unit = processingTask.removeNextUnit();
               } catch (Error var23) {
                  unit = processingTask.unitToProcess;
                  throw var23;
               } catch (RuntimeException var24) {
                  unit = processingTask.unitToProcess;
                  throw var24;
               }

               if (unit == null) {
                  break;
               }

               this.reportWorked(1, acceptedCount++);
               this.stats.lineCount += (long)unit.compilationResult.lineSeparatorPositions.length;
               this.requestor.acceptResult(unit.compilationResult.tagAsAccepted());
               if (this.options.verbose) {
                  this.out
                     .println(
                        Messages.bind(
                           Messages.compilation_done,
                           new String[]{String.valueOf(acceptedCount), String.valueOf(this.totalUnits), new String(unit.getFileName())}
                        )
                     );
               }
            }
         }

         if (!lastRound && this.annotationProcessorManager != null && this.totalUnits > this.annotationProcessorStartIndex) {
            int backup = this.annotationProcessorStartIndex;
            int prevUnits = this.totalUnits;
            this.processAnnotations();

            for(int i = backup; i < prevUnits; ++i) {
               this.unitsToProcess[i].cleanUp();
            }

            this.processCompiledUnits(backup, lastRound);
         }
      } catch (AbortCompilation var26) {
         this.handleInternalException(var26, unit);
      } catch (Error var27) {
         this.handleInternalException(var27, unit, null);
         throw var27;
      } catch (RuntimeException var28) {
         this.handleInternalException(var28, unit, null);
         throw var28;
      } finally {
         if (processingTask != null) {
            processingTask.shutdown();
            processingTask = null;
         }

         this.reset();
         this.annotationProcessorStartIndex = 0;
         this.stats.endTime = System.currentTimeMillis();
      }
   }

   public synchronized CompilationUnitDeclaration getUnitToProcess(int next) {
      if (next >= this.totalUnits) {
         return null;
      } else {
         CompilationUnitDeclaration unit = this.unitsToProcess[next];
         if (this.annotationProcessorManager == null || next < this.annotationProcessorStartIndex) {
            this.unitsToProcess[next] = null;
         }

         return unit;
      }
   }

   public boolean shouldCleanup(int index) {
      return index < this.annotationProcessorStartIndex;
   }

   public void setBinaryTypes(ReferenceBinding[] binaryTypes) {
      this.referenceBindings = binaryTypes;
   }

   protected void handleInternalException(Throwable internalException, CompilationUnitDeclaration unit, CompilationResult result) {
      if (result == null && unit != null) {
         result = unit.compilationResult;
      }

      if (result == null && this.lookupEnvironment.unitBeingCompleted != null) {
         result = this.lookupEnvironment.unitBeingCompleted.compilationResult;
      }

      if (result == null) {
         synchronized(this) {
            if (this.unitsToProcess != null && this.totalUnits > 0) {
               result = this.unitsToProcess[this.totalUnits - 1].compilationResult;
            }
         }
      }

      boolean needToPrint = true;
      if (result != null) {
         String[] pbArguments = new String[]{Messages.bind(Messages.compilation_internalError, Util.getExceptionSummary(internalException))};
         result.record(this.problemReporter.createProblem(result.getFileName(), 0, pbArguments, pbArguments, 1, 0, 0, 0, 0), unit, true);
         if (!result.hasBeenAccepted) {
            this.requestor.acceptResult(result.tagAsAccepted());
            needToPrint = false;
         }
      }

      if (needToPrint) {
         internalException.printStackTrace();
      }
   }

   protected void handleInternalException(AbortCompilation abortException, CompilationUnitDeclaration unit) {
      if (abortException.isSilent) {
         if (abortException.silentException != null) {
            throw abortException.silentException;
         }
      } else {
         CompilationResult result = abortException.compilationResult;
         if (result == null && unit != null) {
            result = unit.compilationResult;
         }

         if (result == null && this.lookupEnvironment.unitBeingCompleted != null) {
            result = this.lookupEnvironment.unitBeingCompleted.compilationResult;
         }

         if (result == null) {
            synchronized(this) {
               if (this.unitsToProcess != null && this.totalUnits > 0) {
                  result = this.unitsToProcess[this.totalUnits - 1].compilationResult;
               }
            }
         }

         if (result != null && !result.hasBeenAccepted) {
            if (abortException.problem != null) {
               CategorizedProblem distantProblem = abortException.problem;
               CategorizedProblem[] knownProblems = result.problems;
               int i = 0;

               while(true) {
                  if (i >= result.problemCount) {
                     if (distantProblem instanceof DefaultProblem) {
                        ((DefaultProblem)distantProblem).setOriginatingFileName(result.getFileName());
                     }

                     result.record(distantProblem, unit, true);
                     break;
                  }

                  if (knownProblems[i] == distantProblem) {
                     break;
                  }

                  ++i;
               }
            } else if (abortException.exception != null) {
               this.handleInternalException(abortException.exception, null, result);
               return;
            }

            if (!result.hasBeenAccepted) {
               this.requestor.acceptResult(result.tagAsAccepted());
            }
         } else {
            abortException.printStackTrace();
         }
      }
   }

   public void initializeParser() {
      this.parser = new Parser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
   }

   protected void internalBeginToCompile(ICompilationUnit[] sourceUnits, int maxUnits) {
      if (!this.useSingleThread && maxUnits >= 10) {
         this.parser.readManager = new ReadManager(sourceUnits, maxUnits);
      }

      for(int i = 0; i < maxUnits; ++i) {
         CompilationResult unitResult = null;

         try {
            if (this.options.verbose) {
               this.out
                  .println(
                     Messages.bind(
                        Messages.compilation_request, new String[]{String.valueOf(i + 1), String.valueOf(maxUnits), new String(sourceUnits[i].getFileName())}
                     )
                  );
            }

            unitResult = new CompilationResult(sourceUnits[i], i, maxUnits, this.options.maxProblemsPerUnit);
            long parseStart = System.currentTimeMillis();
            CompilationUnitDeclaration parsedUnit;
            if (this.totalUnits < this.parseThreshold) {
               parsedUnit = this.parser.parse(sourceUnits[i], unitResult);
            } else {
               parsedUnit = this.parser.dietParse(sourceUnits[i], unitResult);
            }

            long resolveStart = System.currentTimeMillis();
            this.stats.parseTime += resolveStart - parseStart;
            this.lookupEnvironment.buildTypeBindings(parsedUnit, null);
            this.stats.resolveTime += System.currentTimeMillis() - resolveStart;
            this.addCompilationUnit(sourceUnits[i], parsedUnit);
            ImportReference currentPackage = parsedUnit.currentPackage;
            if (currentPackage != null) {
               unitResult.recordPackageName(currentPackage.tokens);
            }
         } catch (AbortCompilation var14) {
            if (var14.compilationResult == null) {
               var14.compilationResult = unitResult;
            }

            throw var14;
         } finally {
            sourceUnits[i] = null;
         }
      }

      if (this.parser.readManager != null) {
         this.parser.readManager.shutdown();
         this.parser.readManager = null;
      }

      this.lookupEnvironment.completeTypeBindings();
   }

   public void process(CompilationUnitDeclaration unit, int i) {
      this.lookupEnvironment.unitBeingCompleted = unit;
      long parseStart = System.currentTimeMillis();
      this.parser.getMethodBodies(unit);
      long resolveStart = System.currentTimeMillis();
      this.stats.parseTime += resolveStart - parseStart;
      if (unit.scope != null) {
         unit.scope.faultInTypes();
      }

      if (unit.scope != null) {
         unit.scope.verifyMethods(this.lookupEnvironment.methodVerifier());
      }

      unit.resolve();
      long analyzeStart = System.currentTimeMillis();
      this.stats.resolveTime += analyzeStart - resolveStart;
      if (!this.options.ignoreMethodBodies) {
         unit.analyseCode();
      }

      long generateStart = System.currentTimeMillis();
      this.stats.analyzeTime += generateStart - analyzeStart;
      if (!this.options.ignoreMethodBodies) {
         unit.generateCode();
      }

      if (this.options.produceReferenceInfo && unit.scope != null) {
         unit.scope.storeDependencyInfo();
      }

      unit.finalizeProblems();
      this.stats.generateTime += System.currentTimeMillis() - generateStart;
      unit.compilationResult.totalUnitsKnown = this.totalUnits;
      this.lookupEnvironment.unitBeingCompleted = null;
   }

   protected void processAnnotations() {
      int newUnitSize = 0;
      int newClassFilesSize = 0;
      int bottom = this.annotationProcessorStartIndex;
      int top = this.totalUnits;
      ReferenceBinding[] binaryTypeBindingsTemp = this.referenceBindings;
      if (top != 0 || binaryTypeBindingsTemp != null) {
         this.referenceBindings = null;

         do {
            int length = top - bottom;
            CompilationUnitDeclaration[] currentUnits = new CompilationUnitDeclaration[length];
            int index = 0;

            for(int i = bottom; i < top; ++i) {
               CompilationUnitDeclaration currentUnit = this.unitsToProcess[i];
               currentUnits[index++] = currentUnit;
            }

            if (index != length) {
               System.arraycopy(currentUnits, 0, currentUnits = new CompilationUnitDeclaration[index], 0, index);
            }

            this.annotationProcessorManager.processAnnotations(currentUnits, binaryTypeBindingsTemp, false);
            if (top < this.totalUnits) {
               length = this.totalUnits - top;
               CompilationUnitDeclaration[] addedUnits = new CompilationUnitDeclaration[length];
               System.arraycopy(this.unitsToProcess, top, addedUnits, 0, length);
               this.annotationProcessorManager.processAnnotations(addedUnits, binaryTypeBindingsTemp, false);
            }

            this.annotationProcessorStartIndex = top;
            ICompilationUnit[] newUnits = this.annotationProcessorManager.getNewUnits();
            newUnitSize = newUnits.length;
            ReferenceBinding[] newClassFiles = this.annotationProcessorManager.getNewClassFiles();
            binaryTypeBindingsTemp = newClassFiles;
            newClassFilesSize = newClassFiles.length;
            if (newUnitSize != 0) {
               ICompilationUnit[] newProcessedUnits = (ICompilationUnit[])newUnits.clone();

               try {
                  this.lookupEnvironment.isProcessingAnnotations = true;
                  this.internalBeginToCompile(newUnits, newUnitSize);
               } catch (SourceTypeCollisionException var24) {
                  var24.newAnnotationProcessorUnits = newProcessedUnits;
                  throw var24;
               } finally {
                  this.lookupEnvironment.isProcessingAnnotations = false;
                  this.annotationProcessorManager.reset();
               }

               bottom = top;
               top = this.totalUnits;
               this.annotationProcessorStartIndex = top;
            } else {
               bottom = top;
               this.annotationProcessorManager.reset();
            }
         } while(newUnitSize != 0 || newClassFilesSize != 0);

         this.annotationProcessorManager.processAnnotations(null, null, true);
         ICompilationUnit[] newUnits = this.annotationProcessorManager.getNewUnits();
         newUnitSize = newUnits.length;
         if (newUnitSize != 0) {
            ICompilationUnit[] newProcessedUnits = (ICompilationUnit[])newUnits.clone();

            try {
               this.lookupEnvironment.isProcessingAnnotations = true;
               this.internalBeginToCompile(newUnits, newUnitSize);
            } catch (SourceTypeCollisionException var22) {
               var22.isLastRound = true;
               var22.newAnnotationProcessorUnits = newProcessedUnits;
               throw var22;
            } finally {
               this.lookupEnvironment.isProcessingAnnotations = false;
               this.annotationProcessorManager.reset();
            }
         } else {
            this.annotationProcessorManager.reset();
         }

         this.annotationProcessorStartIndex = this.totalUnits;
      }
   }

   public void reset() {
      this.lookupEnvironment.reset();
      this.parser.scanner.source = null;
      this.unitsToProcess = null;
      if (DebugRequestor != null) {
         DebugRequestor.reset();
      }

      this.problemReporter.reset();
   }

   public CompilationUnitDeclaration resolve(
      CompilationUnitDeclaration unit, ICompilationUnit sourceUnit, boolean verifyMethods, boolean analyzeCode, boolean generateCode
   ) {
      try {
         if (unit == null) {
            this.parseThreshold = 0;
            this.beginToCompile(new ICompilationUnit[]{sourceUnit});

            for(int i = 0; i < this.totalUnits; ++i) {
               if (this.unitsToProcess[i] != null && this.unitsToProcess[i].compilationResult.compilationUnit == sourceUnit) {
                  unit = this.unitsToProcess[i];
                  break;
               }
            }

            if (unit == null) {
               unit = this.unitsToProcess[0];
            }
         } else {
            this.lookupEnvironment.buildTypeBindings(unit, null);
            this.lookupEnvironment.completeTypeBindings();
         }

         this.lookupEnvironment.unitBeingCompleted = unit;
         this.parser.getMethodBodies(unit);
         if (unit.scope != null) {
            unit.scope.faultInTypes();
            if (unit.scope != null && verifyMethods) {
               unit.scope.verifyMethods(this.lookupEnvironment.methodVerifier());
            }

            unit.resolve();
            if (analyzeCode) {
               unit.analyseCode();
            }

            if (generateCode) {
               unit.generateCode();
            }

            unit.finalizeProblems();
         }

         if (this.unitsToProcess != null) {
            this.unitsToProcess[0] = null;
         }

         this.requestor.acceptResult(unit.compilationResult.tagAsAccepted());
         return unit;
      } catch (AbortCompilation var7) {
         this.handleInternalException(var7, unit);
         return unit == null ? this.unitsToProcess[0] : unit;
      } catch (Error var8) {
         this.handleInternalException(var8, unit, null);
         throw var8;
      } catch (RuntimeException var9) {
         this.handleInternalException(var9, unit, null);
         throw var9;
      }
   }

   public CompilationUnitDeclaration resolve(ICompilationUnit sourceUnit, boolean verifyMethods, boolean analyzeCode, boolean generateCode) {
      return this.resolve(null, sourceUnit, verifyMethods, analyzeCode, generateCode);
   }

   class APTProblem {
      CategorizedProblem problem;
      ReferenceContext context;

      APTProblem(CategorizedProblem problem, ReferenceContext context) {
         this.problem = problem;
         this.context = context;
      }
   }
}
