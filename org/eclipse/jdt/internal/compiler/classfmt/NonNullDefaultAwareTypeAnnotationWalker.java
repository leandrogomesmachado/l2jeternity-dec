package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

public class NonNullDefaultAwareTypeAnnotationWalker extends TypeAnnotationWalker {
   private int defaultNullness;
   private boolean atDefaultLocation;
   private boolean nextIsDefaultLocation;
   private boolean atTypeBound;
   private boolean nextIsTypeBound;
   private boolean isEmpty;
   IBinaryAnnotation nonNullAnnotation;
   LookupEnvironment environment;

   public NonNullDefaultAwareTypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations, int defaultNullness, LookupEnvironment environment) {
      super(typeAnnotations);
      this.nonNullAnnotation = getNonNullAnnotation(environment);
      this.defaultNullness = defaultNullness;
      this.environment = environment;
   }

   public NonNullDefaultAwareTypeAnnotationWalker(int defaultNullness, LookupEnvironment environment) {
      this(defaultNullness, getNonNullAnnotation(environment), false, false, environment);
   }

   NonNullDefaultAwareTypeAnnotationWalker(
      IBinaryTypeAnnotation[] typeAnnotations,
      long newMatches,
      int newPathPtr,
      int defaultNullness,
      IBinaryAnnotation nonNullAnnotation,
      boolean atDefaultLocation,
      boolean atTypeBound,
      LookupEnvironment environment
   ) {
      super(typeAnnotations, newMatches, newPathPtr);
      this.defaultNullness = defaultNullness;
      this.nonNullAnnotation = nonNullAnnotation;
      this.atDefaultLocation = atDefaultLocation;
      this.atTypeBound = atTypeBound;
      this.environment = environment;
   }

   NonNullDefaultAwareTypeAnnotationWalker(
      int defaultNullness, IBinaryAnnotation nonNullAnnotation, boolean atDefaultLocation, boolean atTypeBound, LookupEnvironment environment
   ) {
      super(null, 0L, 0);
      this.nonNullAnnotation = nonNullAnnotation;
      this.defaultNullness = defaultNullness;
      this.atDefaultLocation = atDefaultLocation;
      this.atTypeBound = atTypeBound;
      this.isEmpty = true;
      this.environment = environment;
   }

   private static IBinaryAnnotation getNonNullAnnotation(LookupEnvironment environment) {
      final char[] nonNullAnnotationName = CharOperation.concat('L', CharOperation.concatWith(environment.getNonNullAnnotationName(), '/'), ';');
      return new IBinaryAnnotation() {
         @Override
         public char[] getTypeName() {
            return nonNullAnnotationName;
         }

         @Override
         public IBinaryElementValuePair[] getElementValuePairs() {
            return null;
         }
      };
   }

   protected TypeAnnotationWalker restrict(long newMatches, int newPathPtr) {
      NonNullDefaultAwareTypeAnnotationWalker var5;
      try {
         if (this.matches != newMatches
            || this.pathPtr != newPathPtr
            || this.atDefaultLocation != this.nextIsDefaultLocation
            || this.atTypeBound != this.nextIsTypeBound) {
            if (newMatches != 0L && this.typeAnnotations != null && this.typeAnnotations.length != 0) {
               return new NonNullDefaultAwareTypeAnnotationWalker(
                  this.typeAnnotations,
                  newMatches,
                  newPathPtr,
                  this.defaultNullness,
                  this.nonNullAnnotation,
                  this.nextIsDefaultLocation,
                  this.nextIsTypeBound,
                  this.environment
               );
            }

            return new NonNullDefaultAwareTypeAnnotationWalker(
               this.defaultNullness, this.nonNullAnnotation, this.nextIsDefaultLocation, this.nextIsTypeBound, this.environment
            );
         }

         var5 = this;
      } finally {
         this.nextIsDefaultLocation = false;
         this.nextIsTypeBound = false;
      }

      return var5;
   }

   @Override
   public ITypeAnnotationWalker toSupertype(short index, char[] superTypeSignature) {
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toSupertype(index, superTypeSignature));
   }

   @Override
   public ITypeAnnotationWalker toMethodParameter(short index) {
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toMethodParameter(index));
   }

   @Override
   public ITypeAnnotationWalker toField() {
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toField());
   }

   @Override
   public ITypeAnnotationWalker toMethodReturn() {
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toMethodReturn());
   }

   @Override
   public ITypeAnnotationWalker toTypeBound(short boundIndex) {
      this.nextIsDefaultLocation = (this.defaultNullness & 256) != 0;
      this.nextIsTypeBound = true;
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toTypeBound(boundIndex));
   }

   @Override
   public ITypeAnnotationWalker toWildcardBound() {
      this.nextIsDefaultLocation = (this.defaultNullness & 256) != 0;
      this.nextIsTypeBound = true;
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toWildcardBound());
   }

   @Override
   public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
      this.nextIsDefaultLocation = (this.defaultNullness & 256) != 0;
      this.nextIsTypeBound = true;
      return (ITypeAnnotationWalker)(this.isEmpty
         ? this.restrict(this.matches, this.pathPtr)
         : super.toTypeParameterBounds(isClassTypeParameter, parameterRank));
   }

   @Override
   public ITypeAnnotationWalker toTypeArgument(int rank) {
      this.nextIsDefaultLocation = (this.defaultNullness & 64) != 0;
      this.nextIsTypeBound = false;
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toTypeArgument(rank));
   }

   @Override
   public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
      this.nextIsDefaultLocation = (this.defaultNullness & 128) != 0;
      this.nextIsTypeBound = false;
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toTypeParameter(isClassTypeParameter, rank));
   }

   @Override
   protected ITypeAnnotationWalker toNextDetail(int detailKind) {
      return (ITypeAnnotationWalker)(this.isEmpty ? this.restrict(this.matches, this.pathPtr) : super.toNextDetail(detailKind));
   }

   @Override
   public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
      IBinaryAnnotation[] normalAnnotations = this.isEmpty ? NO_ANNOTATIONS : super.getAnnotationsAtCursor(currentTypeId);
      if (this.atDefaultLocation && currentTypeId != -1 && (!this.atTypeBound || currentTypeId != 1)) {
         if (normalAnnotations == null || normalAnnotations.length == 0) {
            return new IBinaryAnnotation[]{this.nonNullAnnotation};
         } else if (this.environment.containsNullTypeAnnotation(normalAnnotations)) {
            return normalAnnotations;
         } else {
            int len = normalAnnotations.length;
            IBinaryAnnotation[] newAnnots = new IBinaryAnnotation[len + 1];
            System.arraycopy(normalAnnotations, 0, newAnnots, 0, len);
            newAnnots[len] = this.nonNullAnnotation;
            return newAnnots;
         }
      } else {
         return normalAnnotations;
      }
   }
}
